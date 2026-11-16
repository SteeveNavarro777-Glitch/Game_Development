/* FILE PURPOSE: SESSION | Fixed-step loop, session state, event queue, navigation and HUD.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;
import javax.swing.JPanel;

/** Fixed-step simulation and interpolated rendering share one consistent lock. */
public final class GamePanel extends JPanel implements Runnable {
    private static final long serialVersionUID=1L;
    static final int VIEW_W=480,VIEW_H=360,SCALE=2,W=VIEW_W*SCALE,H=VIEW_H*SCALE,FPS=60;
    enum Mode { TITLE, PLAYING, PAUSED, DEFEAT, VICTORY }
    private record GameEvent(Enemy enemy) {}
    private static final Color INK=new Color(0x171D25),GOLD=new Color(0xF1C979),
            CREAM=new Color(0xF8EBD3),MUTED=new Color(0xB8C9BB);
    private static final Font SMALL=new Font("Monospaced",Font.PLAIN,10),
            BOLD=new Font("Monospaced",Font.BOLD,11),TITLE=new Font("Serif",Font.BOLD,34);
    private final Object lock=new Object();
    final KeyHandler keys=new KeyHandler();
    private final BufferedImage buffer=new BufferedImage(VIEW_W,VIEW_H,BufferedImage.TYPE_INT_RGB);
    private final WorldLighting lighting=new WorldLighting(VIEW_W,VIEW_H);
    // [SECTION: OOP - POLYMORPHISM / DSA - ARRAY LIST] One list renders every Drawable subtype.
    private final List<Drawable> drawList=new ArrayList<>(128);
    // [SECTION: OOP - POLYMORPHISM] Slime and Guardian share the Enemy abstraction.
    private final List<Enemy> enemies=new ArrayList<>();
    private final List<Npc> npcs=new ArrayList<>();
    // [SECTION: DSA - FIFO EVENT QUEUE] Defer defeat rewards until actor traversal finishes.
    private final ArrayDeque<GameEvent> events=new ArrayDeque<>();
    private World world;
    private VillageMap villageMap;
    private ForestEncounters forestEncounters;
    private Player player;
    private QuestLog quests;
    private Mode mode=Mode.TITLE;
    private double time,camX,camY,prevCamX,prevCamY,alpha,flowTimer,pathTimer;
    private int[][] flow;
    private int flowC=-1,flowR=-1,flowBuilds,pathBuilds,kills,ordinaryKills;
    private long flowVersion=-1;
    private List<Point> path=List.of();
    private boolean mapOpen,journalOpen,hint,debug;
    private volatile boolean running;
    private Thread thread;
    private double updateMs,renderMs;
    private long frameWindow=System.nanoTime();
    private int frames,shownFps;

    // [BLOCK: FUNCTION] GamePanel.GamePanel - Initialize GamePanel state.
    GamePanel() throws java.io.IOException {
        setPreferredSize(new Dimension(W,H));
        setBackground(INK);setFocusable(true);setFocusTraversalKeysEnabled(false);
        addKeyListener(keys);
        addMouseListener(keys);
        addMouseListener(new java.awt.event.MouseAdapter(){
            // [BLOCK: FUNCTION] GamePanel.mousePressed - Handle mouse pressed for GamePanel.
            @Override public void mousePressed(java.awt.event.MouseEvent event){requestFocusInWindow();}
        });
        addFocusListener(new FocusAdapter(){
            // [BLOCK: FUNCTION] GamePanel.focusLost - Handle focus lost for GamePanel.
            @Override public void focusLost(FocusEvent event){
                keys.clear();
                synchronized(lock){if(mode==Mode.PLAYING){mode=Mode.PAUSED;freezeInterpolation();}}
            }
        });
        Assets.load();reset();
    }
    // [BLOCK: SESSION: RESTART] GamePanel.reset - Create a fresh world, actors, inventory, quests and timing state.
    void reset(){
        synchronized(lock){
            Fx.clear();keys.clear();
            world=new World(7);villageMap=new VillageMap(world);player=new Player(World.SPAWN_X,World.SPAWN_Y);
            quests=new QuestLog();enemies.clear();events.clear();drawList.clear();
            npcs.clear();npcs.addAll(NpcPopulation.create(world));
            forestEncounters=new ForestEncounters(world);forestEncounters.populate(enemies);
            time=flowTimer=pathTimer=0;alpha=0;
            flow=null;flowVersion=-1;flowC=flowR=-1;flowBuilds=pathBuilds=kills=ordinaryKills=0;
            path=List.of();mapOpen=journalOpen=hint=debug=false;
            camX=prevCamX=targetX();camY=prevCamY=targetY();
            mode=Mode.TITLE;
        }
    }
    // [BLOCK: FUNCTION] GamePanel.start - Handle start for GamePanel.
    synchronized void start(){
        if(running)return;
        running=true;thread=new Thread(this,"agyu-game-loop");thread.setDaemon(true);thread.start();
    }
    // [BLOCK: FUNCTION] GamePanel.stop - Handle stop for GamePanel.
    synchronized void stop(){
        running=false;
        if(thread!=null)thread.interrupt();
    }
    // [BLOCK: TIMING: FIXED STEP] GamePanel.run - Advance simulation at 60 Hz and interpolate rendering.
    @Override public void run(){
        final double step=1.0/FPS;
        double accumulator=0;long previous=System.nanoTime(),nextPaint=previous;
        while(running){
            long now=System.nanoTime();accumulator+=Math.min(0.25,(now-previous)/1e9);previous=now;
            while(accumulator>=step&&running){
                synchronized(lock){
                    long begin=System.nanoTime();update(step);
                    updateMs=updateMs*0.9+(System.nanoTime()-begin)/1e6*0.1;
                }
                accumulator-=step;
            }
            synchronized(lock){alpha=accumulator/step;}
            long tick=System.nanoTime();
            if(tick>=nextPaint){repaint();nextPaint=tick+1_000_000_000L/120;}
            long untilUpdate=(long)((step-accumulator)*1e9);
            LockSupport.parkNanos(Math.max(100_000L,Math.min(untilUpdate,nextPaint-System.nanoTime())));
            if(Thread.interrupted()&&!running)break;
        }
    }
    // [BLOCK: FUNCTION] GamePanel.targetX - Handle target x for GamePanel.
    private double targetX(){return Math.max(0,Math.min(player.x-VIEW_W/2.0,world.cols*World.T-VIEW_W));}
    // [BLOCK: FUNCTION] GamePanel.targetY - Handle target y for GamePanel.
    private double targetY(){return Math.max(-64,Math.min(player.y-20-VIEW_H/2.0-world.mountain.lookAhead(player.x,player.y),world.rows*World.T-VIEW_H));}
    // [BLOCK: FUNCTION] GamePanel.freezeInterpolation - Handle freeze interpolation for GamePanel.
    private void freezeInterpolation(){
        player.savePrev();for(Enemy enemy:enemies)enemy.savePrev();for(Npc npc:npcs)npc.savePrev();
        prevCamX=camX;prevCamY=camY;
    }
    // [BLOCK: SIMULATION / DSA EVENT QUEUE] GamePanel.update - Apply input, update actors, enqueue defeats, then process rewards safely.
    void update(double dt){
        KeyHandler.Input input=keys.poll();
        if(input.pressed(KeyHandler.DEBUG))debug=!debug;
        if(mode==Mode.TITLE){if(input.pressed(KeyHandler.START))mode=Mode.PLAYING;return;}
        if(mode==Mode.DEFEAT||mode==Mode.VICTORY){
            if(input.pressed(KeyHandler.RESTART)){reset();mode=Mode.PLAYING;}
            else {
                // Finish an interrupted jump under the defeat overlay and settle interpolation.
                player.savePrev();
                if(mode==Mode.DEFEAT&&player.airborne())player.update(dt,input,world,enemies);
            }
            return;
        }
        if(input.pressed(KeyHandler.PAUSE)||(mode==Mode.PAUSED&&input.pressed(KeyHandler.START))){
            mode=mode==Mode.PAUSED?Mode.PLAYING:Mode.PAUSED;freezeInterpolation();return;
        }
        if(mode!=Mode.PLAYING)return;
        if(input.pressed(KeyHandler.MAP)){mapOpen=!mapOpen;journalOpen=false;}
        if(input.pressed(KeyHandler.JOURNAL)){journalOpen=!journalOpen;mapOpen=false;}
        if(input.pressed(KeyHandler.HINT)){hint=!hint;pathTimer=0;}
        // Reading the map or journal pauses the world, including combat timers.
        if(mapOpen||journalOpen){freezeInterpolation();return;}
        player.savePrev();for(Enemy enemy:enemies)enemy.savePrev();for(Npc npc:npcs)npc.savePrev();
        prevCamX=camX;prevCamY=camY;
        if(Fx.hitStop>0){
            keys.defer(input,KeyHandler.ATTACK|KeyHandler.JUMP|KeyHandler.DASH|KeyHandler.INTERACT|KeyHandler.HEAL);
            Fx.hitStop=Math.max(0,Fx.hitStop-dt);Fx.update(dt);return;
        }
        time+=dt;world.updateAmbient(dt);
        for(Enemy enemy:enemies)enemy.refreshNightPower(world);
        if(input.pressed(KeyHandler.HEAL))
            player.useHerb();
        player.update(dt,input,world,enemies);
        for(Npc npc:npcs)npc.update(dt,world,player);
        updateNavigation(dt);
        // [SECTION: OOP - DYNAMIC DISPATCH] Guardian overrides update; other enemies reuse Enemy.update.
        for(Enemy enemy:enemies){
            enemy.update(dt,player,world,flow);
            if(enemy.claimDefeat())events.addLast(new GameEvent(enemy));
        }
        processEvents();
        enemies.removeIf(Enemy::removable);
        forestEncounters.update(dt,player,enemies);
        if(!player.alive()){mode=Mode.DEFEAT;freezeInterpolation();}
        else if(input.pressed(KeyHandler.INTERACT)&&!player.airborne()&&!player.dashing())interact();
        Fx.update(dt);
        double follow=1-Math.exp(-6*dt);
        camX+=(targetX()-camX)*follow;camY+=(targetY()-camY)*follow;
    }
    // [BLOCK: DSA: BFS / A* CACHES] GamePanel.updateNavigation - Refresh enemy flow distances and the optional player trail when needed.
    private void updateNavigation(double dt){
        flowTimer-=dt;pathTimer-=dt;
        if(flowVersion!=world.navigationVersion()){flow=null;pathTimer=0;flowVersion=world.navigationVersion();}
        int c=World.col(player.x),r=World.row(player.y);
        // The player is narrower than enemies: choose a nearby valid flow root.
        if(!world.walkable(c,r)){
            search:for(int radius=1;radius<=3;radius++)for(int y=r-radius;y<=r+radius;y++)
                for(int x=c-radius;x<=c+radius;x++)if(world.walkable(x,y)){c=x;r=y;break search;}
        }
        if(flow==null||(flowTimer<=0&&(c!=flowC||r!=flowR))){
            flow=Pathfinder.distances(world,c,r);flowC=c;flowR=r;flowBuilds++;flowTimer=0.35;
        }
        if(hint&&pathTimer<=0){
            int[] goal=objectiveSite();
            path=Pathfinder.find(world,c,r,goal[0],goal[1]);
            // [SECTION: DSA - SHORTEST ROUTE SELECTION] A closed gate becomes the next reachable trail waypoint.
            if(path.isEmpty()&&(c!=goal[0]||r!=goal[1])){
                List<Point> best=List.of();double score=Double.MAX_VALUE;
                for(Palisade.Gate gate:world.palisade.gates()){
                    double gx=gate.closed.getCenterX(),gy=gate.closed.getCenterY();
                    int toward=world.forest.contains(player.x,player.y)?-1:1;
                    switch(gate.side){case NORTH->gy+=36*toward;case SOUTH->gy-=36*toward;case WEST->gx+=36*toward;case EAST->gx-=36*toward;}
                    List<Point> route=Pathfinder.find(world,c,r,World.col(gx),World.row(gy));
                    double cost=route.size()+Math.hypot(gx/World.T-goal[0],gy/World.T-goal[1]);
                    if(!route.isEmpty()&&cost<score){best=route;score=cost;}
                }
                path=best;
            }
            pathBuilds++;pathTimer=0.6;
        }
    }
    // [BLOCK: DSA: FIFO QUEUE] GamePanel.processEvents - Remove defeat events in insertion order after actor updates, then award progression.
    private void processEvents(){
        while(!events.isEmpty()){
            Enemy enemy=events.removeFirst().enemy();
            kills++;
            if(enemy instanceof ArenaBoss boss){
                if(quests.finish(boss.questId()))player.inventory().add(Inventory.Item.HERB,2);
                pathTimer=0;
            }else if(enemy instanceof Guardian){
                quests.finish("guardian");
                player.inventory().add(Inventory.Item.HERB,2);
            }else{
                ordinaryKills++;
                if(ordinaryKills%2==0)player.inventory().add(Inventory.Item.HERB,1);
            }
        }
    }
    // [BLOCK: FUNCTION] GamePanel.guardsRemain - Handle guards remain for GamePanel.
    private boolean guardsRemain(int group){
        for(Enemy enemy:enemies)if(enemy.shrineGroup==group&&enemy.alive())return true;
        return false;
    }
    // [BLOCK: GAMEPLAY: INTERACTION] GamePanel.interact - Restore eligible shrines, finish at HOME or talk to the nearest available NPC.
    private void interact(){
        for(int i=0;i<World.LANDMARKS.length;i++){
            int[] site=World.LANDMARKS[i];
            if(Math.hypot(player.x-(site[0]+0.5)*World.T,player.y-(site[1]+0.5)*World.T)>52)continue;
            if(i==0){
                if(quests.finish("home")){mode=Mode.VICTORY;freezeInterpolation();}
            }else if(i<=3){
                String id=QuestLog.SHRINES[i-1];
                if(!quests.done(id)&&!guardsRemain(i-1)&&quests.finish(id)){
                    player.inventory().add(Inventory.Item.SEAL,1);
                    player.inventory().add(Inventory.Item.HERB,1);
                    Fx.burst(player.x,player.y-20,0xF1C979,18,60,0.6,-15,2);
                    if(quests.ready("guardian")){
                        int[] boss=World.LANDMARKS[4];
                        Guardian guardian=new Guardian((boss[0]+0.5)*World.T,(boss[1]+0.5)*World.T);
                        guardian.refreshNightPower(world);enemies.add(guardian);
                        quests.record("The guardian has awakened in the southeast.");
                    }
                }
            }
            return;
        }
        if(world.interactGate(player,enemies,npcs))return;
        Npc nearest=null;double range=Double.MAX_VALUE;
        for(Npc npc:npcs)if(npc.canTalk(player)){
            double d=Math.hypot(npc.x()-player.x,npc.y()-player.y);
            if(d<range){range=d;nearest=npc;}
        }
        if(nearest!=null)nearest.talk(player);
    }
    // [BLOCK: FUNCTION] GamePanel.objectiveSite - Handle objective site for GamePanel.
    private int[] objectiveSite(){
        if(quests.done("guardian")){
            ForestRegion.Arena nearest=null;double best=Double.MAX_VALUE;
            for(ForestRegion.Arena arena:world.forest.arenas())if(!quests.done(QuestLog.BOSSES[arena.id()-1])){
                double distance=Math.hypot(player.x-(arena.c()+.5)*World.T,player.y-(arena.r()+.5)*World.T);
                if(distance<best){best=distance;nearest=arena;}
            }
            return nearest==null?World.LANDMARKS[0]:new int[]{nearest.c(),nearest.r()};
        }
        if(quests.ready("guardian"))return World.LANDMARKS[4];
        int best=1;double distance=Double.MAX_VALUE;
        for(int i=1;i<=3;i++)if(!quests.done(QuestLog.SHRINES[i-1])){
            int[] site=World.LANDMARKS[i];
            double d=Math.hypot(player.x-(site[0]+0.5)*World.T,player.y-(site[1]+0.5)*World.T);
            if(d<distance){distance=d;best=i;}
        }
        return World.LANDMARKS[best];
    }
    // [BLOCK: OOP: POLYMORPHISM / SORTING] GamePanel.paintComponent - Sort Drawable objects by feet depth and dispatch each concrete draw implementation.
    @Override protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);
        synchronized(lock){
            long begin=System.nanoTime();
            Graphics2D g=buffer.createGraphics();
            try{
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
                int cx=(int)Math.round(prevCamX+(camX-prevCamX)*alpha)+Fx.shakeX;
                int cy=(int)Math.round(prevCamY+(camY-prevCamY)*alpha)+Fx.shakeY;
                world.drawGround(g,cx,cy,VIEW_W,VIEW_H,time);
                for(Enemy enemy:enemies)if(enemy instanceof ArenaBoss boss&&visible(boss.x,boss.y,cx,cy,512))boss.drawGround(g,cx,cy,alpha);
                if(hint&&mode==Mode.PLAYING){
                    g.setColor(new Color(241,201,121,150));
                    for(Point p:path)g.fillRect((int)((p.x+0.5)*World.T)-cx-1,(int)((p.y+0.5)*World.T)-cy-1,3,3);
                }
                if(mode!=Mode.TITLE)drawBeacons(g,cx,cy);
                drawList.clear();
                for(World.Tree tree:world.trees)if(visible(tree.x,tree.y,cx,cy,160))drawList.add(tree);
                for(World.Building house:world.buildings)if(visible(house.x,house.y,cx,cy,224))drawList.add(house);
                for(World.Prop prop:world.props)if(visible(prop.x,prop.y,cx,cy,160))drawList.add(prop);
                for(Npc npc:npcs)if(visible(npc.x(),npc.y(),cx,cy,112))drawList.add(npc);
                for(Enemy enemy:enemies)if(visible(enemy.x,enemy.y,cx,cy,enemy instanceof ArenaBoss?512:50))drawList.add(enemy);
                if(mode!=Mode.TITLE)drawList.add(player);
                // [SECTION: DSA - SORTING] Ascending feet depth gives correct foreground overlap.
                world.collectLandscape(drawList,cx,cy,VIEW_W,VIEW_H);
                drawList.sort(Comparator.comparingDouble(Drawable::sortY));
                // Keep lightning and afterimages behind the complete current character.
                DashEffects.draw(g,cx,cy);
                for(Drawable drawable:drawList){
                    if(drawable instanceof World.Tree tree)tree.draw(g,cx,cy,alpha,player);
                    else drawable.draw(g,cx,cy,alpha);
                }
                if(mode==Mode.PLAYING)world.palisade.drawPrompt(g,player,cx,cy,VIEW_W,VIEW_H);
                Fx.draw(g,cx,cy);
                lighting.draw(g,world,player,enemies,cx,cy,alpha);
                if(mode!=Mode.TITLE){for(Npc npc:npcs)npc.drawDialogue(g,cx,cy,alpha,VIEW_W,VIEW_H);drawHud(g);}
                if(mapOpen)drawMap(g);
                if(journalOpen)drawJournal(g);
                if(debug)drawDiagnostics(g);
                if(mode==Mode.TITLE)drawTitle(g);
                else if(mode==Mode.PAUSED)drawModal(g,"PAUSED","Esc / P / Enter to resume");
                else if(mode==Mode.DEFEAT)drawModal(g,"AGYU HAS FALLEN","Press R to begin a new journey");
                else if(mode==Mode.VICTORY)drawModal(g,"THE GROVE REMEMBERS","Shrines restored. Three villains defeated. R to play again.");
            }finally{g.dispose();}
            Graphics2D screen=(Graphics2D)graphics.create();
            try{
                screen.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                screen.drawImage(buffer,0,0,getWidth(),getHeight(),null);
            }finally{screen.dispose();}
            renderMs=renderMs*0.9+(System.nanoTime()-begin)/1e6*0.1;
            frames++;long now=System.nanoTime();
            if(now-frameWindow>=1_000_000_000L){shownFps=(int)(frames*1e9/(now-frameWindow));frames=0;frameWindow=now;}
        }
    }
    // [BLOCK: FUNCTION] GamePanel.visible - Handle visible for GamePanel.
    private boolean visible(double x,double y,int cx,int cy,int margin){
        return x>cx-margin&&x<cx+VIEW_W+margin&&y>cy-margin&&y<cy+VIEW_H+margin;
    }
    // [BLOCK: FUNCTION] GamePanel.siteName - Handle site name for GamePanel.
    private String siteName(int i){return new String[]{"HOME","GROVE","LAKE","RIDGE","GUARDIAN"}[i];}
    // [BLOCK: FUNCTION] GamePanel.siteColor - Handle site color for GamePanel.
    private Color siteColor(int i){
        if(i==0)return new Color(0xB3DDF2);
        if(i==4)return quests.done("guardian")?new Color(0xA4E3AE):new Color(0xEF8C79);
        return quests.done(QuestLog.SHRINES[i-1])?new Color(0xA4E3AE):GOLD;
    }
    // [BLOCK: FUNCTION] GamePanel.drawBeacons - Render draw beacons using the current graphics state.
    private void drawBeacons(Graphics2D g,int cx,int cy){
        g.setFont(SMALL);
        for(int i=0;i<World.LANDMARKS.length;i++){
            int[] site=World.LANDMARKS[i];int x=(int)((site[0]+0.5)*World.T)-cx,y=(int)((site[1]+0.5)*World.T)-cy;
            if(!visible(x+cx,y+cy,cx,cy,70))continue;
            g.setColor(new Color(0,0,0,60));g.fillOval(x-15,y-4,30,10);
            g.setColor(new Color(0x4A4F4C));g.fillRect(x-8,y-14,16,14);
            g.setColor(siteColor(i));g.fillRect(x-5,y-20,10,10);g.drawOval(x-11,y-3,22,7);
            String label=siteName(i);
            if(Math.hypot(player.x-(site[0]+0.5)*World.T,player.y-(site[1]+0.5)*World.T)<60)label+=" [E]";
            g.setColor(INK);g.fillRect(x-g.getFontMetrics().stringWidth(label)/2-3,y-65,g.getFontMetrics().stringWidth(label)+6,12);
            g.setColor(CREAM);g.drawString(label,x-g.getFontMetrics().stringWidth(label)/2,y-56);
        }
    }
    // [BLOCK: FUNCTION] GamePanel.card - Handle card for GamePanel.
    private void card(Graphics2D g,int x,int y,int width,int height){
        g.setColor(new Color(18,26,29,235));g.fillRect(x,y,width,height);
        g.setColor(new Color(0x796848));g.drawRect(x,y,width,height);
    }
    // [BLOCK: FUNCTION] GamePanel.text - Handle text for GamePanel.
    private void text(Graphics2D g,String text,int x,int y,Color color){g.setColor(color);g.drawString(text,x,y);}
    // [BLOCK: FUNCTION] GamePanel.center - Handle center for GamePanel.
    private void center(Graphics2D g,String text,int y,Color color){
        text(g,text,(VIEW_W-g.getFontMetrics().stringWidth(text))/2,y,color);
    }
    // [BLOCK: FUNCTION] GamePanel.vitalBar - Handle vital bar for GamePanel.
    private void vitalBar(Graphics2D g,int x,int y,int width,int height,double ratio,String label,Color fill,Color shine){
        g.setColor(new Color(0x080F17));g.fillRect(x-1,y-1,width+2,height+2);
        g.setColor(new Color(0x35444C));g.fillRect(x,y,width,height);
        int filled=(int)Math.round(width*Math.max(0,Math.min(1,ratio)));
        g.setColor(fill);g.fillRect(x,y,filled,height);
        if(filled>0){g.setColor(shine);g.fillRect(x,y,filled,2);}
        int tx=x+width/2-g.getFontMetrics().stringWidth(label)/2;
        text(g,label,tx+1,y+9,INK);text(g,label,tx,y+8,CREAM);
    }
    // [BLOCK: FUNCTION] GamePanel.drawHud - Render draw hud using the current graphics state.
    private void drawHud(Graphics2D g){
        g.setFont(SMALL);
        text(g,"HEALTH",19,22,INK);text(g,"HEALTH",18,21,CREAM);
        vitalBar(g,66,13,170,10,player.health()/(double)player.maxHealth(),
                player.health()+" / "+player.maxHealth(),new Color(0xDB585B),new Color(0xFFA38B));
        text(g,"ENERGY",19,38,INK);text(g,"ENERGY",18,37,CREAM);
        vitalBar(g,66,29,170,10,player.energy()/player.maxEnergy(),
                (int)Math.ceil(player.energy())+" / "+(int)player.maxEnergy(),
                new Color(player.energyExhausted()?0xB77837:0x37AFC7),
                new Color(player.energyExhausted()?0xF6CB77:0x9BFAE9));
        card(g,VIEW_W-112,10,104,92);
        text(g,world.forest.contains(player.x,player.y)?"FOREST":"VILLAGE",VIEW_W-104,21,GOLD);text(g,"N",VIEW_W-22,21,MUTED);
        g.setColor(new Color(0x796848));g.drawRect(VIEW_W-109,25,98,74);
        villageMap.draw(g,player,quests,VIEW_W-108,26,96,72,alpha,false);
        drawTimeOfDay(g);
        drawBossHealth(g);
    }
    // [BLOCK: WORLD CLOCK HUD] GamePanel.drawTimeOfDay - Show a compact numbered phase and next-change countdown below the resource bars.
    private void drawTimeOfDay(Graphics2D g){
        DayNightCycle cycle=world.dayNight;Color color=new Color(cycle.night()?0xD2B8FF:0xF1D894);
        String label=cycle.label()+"  "+cycle.countdown();
        text(g,label,19,55,INK);text(g,label,18,54,color);
        if(cycle.night()){text(g,"STRONGER FOES",139,55,INK);text(g,"STRONGER FOES",138,54,color);}
    }
    // [BLOCK: BOSS HUD] GamePanel.drawBossHealth - Show one continuous health bar only during a nearby arena fight.
    private void drawBossHealth(Graphics2D g){
        ArenaBoss nearest=null;double range=420;
        for(Enemy enemy:enemies)if(enemy instanceof ArenaBoss boss&&boss.engaged()){
            double distance=Math.hypot(player.x-boss.x,player.y-boss.y);
            if(distance<range){range=distance;nearest=boss;}
        }
        if(nearest==null)return;
        Color color=new Color(new int[]{0x8ABD57,0xBC79EC,0xEB9952}[nearest.arenaId()-1]);
        String name=nearest.bossName();int tx=(VIEW_W-g.getFontMetrics().stringWidth(name))/2;
        text(g,name,tx+1,VIEW_H-28,INK);text(g,name,tx,VIEW_H-29,CREAM);
        vitalBar(g,VIEW_W/2-100,VIEW_H-22,200,10,nearest.health()/(double)nearest.maxHealth(),
                nearest.health()+" / "+nearest.maxHealth(),color,new Color(0xF6E5C6));
    }
    // [BLOCK: FUNCTION] GamePanel.drawMap - Render draw map using the current graphics state.
    private void drawMap(Graphics2D g){
        g.setColor(new Color(0,0,0,145));g.fillRect(0,0,VIEW_W,VIEW_H);
        card(g,66,42,348,277);g.setFont(BOLD);center(g,"VILLAGE OF THE LOST GROVE",61,GOLD);
        villageMap.draw(g,player,quests,80,72,320,240,alpha,true);
        g.setFont(SMALL);center(g,"Arrow: Agyu  Gold: shrines  Red: bosses  M: close",332,CREAM);
    }
    // [BLOCK: FUNCTION] GamePanel.drawJournal - Render draw journal using the current graphics state.
    private void drawJournal(Graphics2D g){
        g.setColor(new Color(0,0,0,145));g.fillRect(0,0,VIEW_W,VIEW_H);
        card(g,35,50,410,258);g.setFont(BOLD);center(g,"AGYU'S JOURNAL",72,GOLD);
        g.setFont(SMALL);int y=97;
        for(int i=0;i<QuestLog.SHRINES.length;i++){
            String id=QuestLog.SHRINES[i];
            text(g,(quests.done(id)?"[x] ":"[ ] ")+siteName(i+1)+" shrine",50,y,CREAM);y+=15;
        }
        text(g,(quests.done("guardian")?"[x] ":"[ ] ")+"Guardian",50,y,CREAM);
        int by=97;
        for(int i=0;i<QuestLog.BOSSES.length;i++){
            String id=QuestLog.BOSSES[i];text(g,(quests.done(id)?"[x] ":"[ ] ")+(i+1)+" "+id.toUpperCase(java.util.Locale.ROOT),265,by,CREAM);by+=15;
        }
        text(g,(quests.done("home")?"[x] ":"[ ] ")+"Return home",265,by,CREAM);
        y+=22;text(g,"LATEST EVENTS",50,y,GOLD);
        for(String event:quests.history()){
            y+=14;
            // Keep each rendered line within the journal card.
            if(event.length()>62)event=event.substring(0,59)+"...";
            text(g,event,50,y,MUTED);if(y>281)break;
        }
        center(g,"Tab: close / resume",330,CREAM);
    }
    // [BLOCK: FUNCTION] GamePanel.drawDiagnostics - Render draw diagnostics using the current graphics state.
    private void drawDiagnostics(Graphics2D g){
        card(g,10,112,259,66);g.setFont(SMALL);
        text(g,"F1 / ALGORITHM DIAGNOSTICS",18,126,GOLD);
        text(g,String.format(java.util.Locale.ROOT,"Paint %d/s | update %.2fms | render %.2fms",shownFps,updateMs,renderMs),18,140,CREAM);
        text(g,"BFS builds "+flowBuilds+" | A* builds "+pathBuilds+" | trail "+path.size(),18,154,CREAM);
        text(g,"Tree checks "+world.collisionChecks+" | particles "+Fx.list.size(),18,168,MUTED);
    }
    // [BLOCK: FUNCTION] GamePanel.drawTitle - Render draw title using the current graphics state.
    private void drawTitle(Graphics2D g){
        g.setColor(new Color(10,18,20,215));g.fillRect(0,0,VIEW_W,VIEW_H);
        g.setFont(SMALL);center(g,"A JAVA ADVENTURE / DSA + OBJECT ORIENTED PROGRAMMING",52,MUTED);
        g.setFont(TITLE);center(g,"AGYU",100,GOLD);
        g.setFont(BOLD);center(g,"GUARDIANS OF THE LOST GROVE",121,CREAM);
        BufferedImage hero=Assets.front;
        g.drawImage(hero,VIEW_W/2-hero.getWidth(),133,hero.getWidth()*2,hero.getHeight()*2,null);
        g.setFont(SMALL);
        center(g,"Restore the shrines. Defeat the villains. Return home.",267,CREAM);
        center(g,"WASD move / Shift sprint / Space jump / Q or Ctrl dash",282,MUTED);
        center(g,"LMB / J combo / RMB shield / E interact / H heal",298,MUTED);
        g.setFont(BOLD);center(g,"PRESS ENTER TO BEGIN",321,GOLD);
        g.setFont(SMALL);center(g,"An original fictional adventure featuring your Agyu character",344,MUTED);
    }
    // [BLOCK: FUNCTION] GamePanel.drawModal - Render draw modal using the current graphics state.
    private void drawModal(Graphics2D g,String title,String subtitle){
        g.setColor(new Color(8,15,18,195));g.fillRect(0,0,VIEW_W,VIEW_H);
        card(g,35,125,410,103);g.setFont(new Font("Serif",Font.BOLD,25));center(g,title,163,GOLD);
        g.setFont(SMALL);center(g,subtitle,191,CREAM);
        if(mode==Mode.VICTORY)center(g,"Seals "+quests.seals()+"/3 / guards "+kills+" / time "+(int)time+"s",213,MUTED);
    }
}
