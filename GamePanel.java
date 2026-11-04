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
    private final List<Drawable> drawList=new ArrayList<>(128);
    private final List<Enemy> enemies=new ArrayList<>();
    private final ArrayDeque<GameEvent> events=new ArrayDeque<>();
    private World world;
    private Player player;
    private QuestLog quests;
    private Mode mode=Mode.TITLE;
    private double time,camX,camY,prevCamX,prevCamY,alpha,flowTimer,pathTimer,noticeTimer;
    private int[][] flow;
    private int flowC=-1,flowR=-1,flowBuilds,pathBuilds,kills;
    private List<Point> path=List.of();
    private String notice="";
    private boolean mapOpen,journalOpen,hint,debug;
    private volatile boolean running;
    private Thread thread;
    private double updateMs,renderMs;
    private long frameWindow=System.nanoTime();
    private int frames,shownFps;

    GamePanel() throws java.io.IOException {
        setPreferredSize(new Dimension(W,H));
        setBackground(INK);setFocusable(true);setFocusTraversalKeysEnabled(false);
        addKeyListener(keys);
        addMouseListener(keys);
        addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mousePressed(java.awt.event.MouseEvent event){requestFocusInWindow();}
        });
        addFocusListener(new FocusAdapter(){
            @Override public void focusLost(FocusEvent event){
                keys.clear();
                synchronized(lock){if(mode==Mode.PLAYING){mode=Mode.PAUSED;freezeInterpolation();}}
            }
        });
        Assets.load();reset();
    }
    void reset(){
        synchronized(lock){
            Fx.clear();keys.clear();
            world=new World(7);player=new Player(World.SPAWN_X,World.SPAWN_Y);
            quests=new QuestLog();enemies.clear();events.clear();drawList.clear();
            for(int group=0;group<3;group++){
                int[] site=World.LANDMARKS[group+1];
                int[][] offsets={{-2,0},{2,0},{0,-2},{0,2}};
                for(int i=0;i<(group==2?4:3);i++)
                    enemies.add(new Slime((site[0]+offsets[i][0]+0.5)*World.T,
                            (site[1]+offsets[i][1]+0.5)*World.T,group));
            }
            time=flowTimer=pathTimer=noticeTimer=0;alpha=0;
            flow=null;flowC=flowR=-1;flowBuilds=pathBuilds=kills=0;
            path=List.of();notice="";mapOpen=journalOpen=hint=debug=false;
            camX=prevCamX=targetX();camY=prevCamY=targetY();
            mode=Mode.TITLE;
        }
    }
    synchronized void start(){
        if(running)return;
        running=true;thread=new Thread(this,"agyu-game-loop");thread.setDaemon(true);thread.start();
    }
    synchronized void stop(){
        running=false;
        if(thread!=null)thread.interrupt();
    }
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
    private double targetX(){return Math.max(0,Math.min(player.x-VIEW_W/2.0,world.cols*World.T-VIEW_W));}
    private double targetY(){return Math.max(0,Math.min(player.y-20-VIEW_H/2.0,world.rows*World.T-VIEW_H));}
    private void freezeInterpolation(){
        player.savePrev();for(Enemy enemy:enemies)enemy.savePrev();
        prevCamX=camX;prevCamY=camY;
    }
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
        player.savePrev();for(Enemy enemy:enemies)enemy.savePrev();
        prevCamX=camX;prevCamY=camY;
        if(Fx.hitStop>0){
            keys.defer(input,KeyHandler.ATTACK|KeyHandler.JUMP|KeyHandler.INTERACT|KeyHandler.HEAL);
            Fx.hitStop=Math.max(0,Fx.hitStop-dt);Fx.update(dt);return;
        }
        time+=dt;noticeTimer=Math.max(0,noticeTimer-dt);
        if(input.pressed(KeyHandler.HEAL))
            say(player.useHerb()?"Herb used: +2 health.":"You need missing health and an herb.");
        player.update(dt,input,world,enemies);
        updateNavigation(dt);
        for(Enemy enemy:enemies){
            enemy.update(dt,player,world,flow);
            if(enemy.claimDefeat())events.addLast(new GameEvent(enemy));
        }
        processEvents();
        enemies.removeIf(Enemy::removable);
        if(!player.alive()){mode=Mode.DEFEAT;freezeInterpolation();}
        else if(input.pressed(KeyHandler.INTERACT)&&!player.airborne())interact();
        Fx.update(dt);
        double follow=1-Math.exp(-6*dt);
        camX+=(targetX()-camX)*follow;camY+=(targetY()-camY)*follow;
    }
    private void updateNavigation(double dt){
        flowTimer-=dt;pathTimer-=dt;
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
            path=Pathfinder.find(world,c,r,goal[0],goal[1]);pathBuilds++;pathTimer=0.6;
        }
    }
    private void processEvents(){
        while(!events.isEmpty()){
            Enemy enemy=events.removeFirst().enemy();
            kills++;
            if(enemy instanceof Guardian){
                quests.finish("guardian");
                player.inventory().add(Inventory.Item.HERB,2);
                say("Guardian defeated. Return to the home beacon!");
            }else if(kills%2==0){
                player.inventory().add(Inventory.Item.HERB,1);
                say("A guard dropped an herb. Press H when hurt.");
            }
        }
    }
    private boolean guardsRemain(int group){
        for(Enemy enemy:enemies)if(enemy.shrineGroup==group&&enemy.alive())return true;
        return false;
    }
    private void interact(){
        for(int i=0;i<World.LANDMARKS.length;i++){
            int[] site=World.LANDMARKS[i];
            if(Math.hypot(player.x-(site[0]+0.5)*World.T,player.y-(site[1]+0.5)*World.T)>52)continue;
            if(i==0){
                if(quests.finish("home")){mode=Mode.VICTORY;freezeInterpolation();}
                else say("Home beacon: restore the shrines and defeat the guardian.");
            }else if(i<=3){
                String id=QuestLog.SHRINES[i-1];
                if(quests.done(id))say("This shrine is already restored.");
                else if(guardsRemain(i-1))say("Defeat this shrine's guards before restoring it.");
                else if(quests.finish(id)){
                    player.inventory().add(Inventory.Item.SEAL,1);
                    player.inventory().add(Inventory.Item.HERB,1);
                    Fx.burst(player.x,player.y-20,0xF1C979,18,60,0.6,-15,2);
                    say("Shrine restored: +1 seal and +1 herb.");
                    if(quests.ready("guardian")){
                        int[] boss=World.LANDMARKS[4];
                        enemies.add(new Guardian((boss[0]+0.5)*World.T,(boss[1]+0.5)*World.T));
                        quests.record("The guardian has awakened in the southeast.");
                        say("Three seals gathered. The guardian has awakened!");
                    }
                }
            }else say(quests.seals()<3?"The guardian sleeps. Restore all three shrines.":"Keep clear of the guardian's warning ring.");
            return;
        }
        say("Stand near a beacon to interact. M opens the map.");
    }
    private void say(String text){notice=text;noticeTimer=3.5;}
    private int[] objectiveSite(){
        if(quests.done("guardian"))return World.LANDMARKS[0];
        if(quests.ready("guardian"))return World.LANDMARKS[4];
        int best=1;double distance=Double.MAX_VALUE;
        for(int i=1;i<=3;i++)if(!quests.done(QuestLog.SHRINES[i-1])){
            int[] site=World.LANDMARKS[i];
            double d=Math.hypot(player.x-(site[0]+0.5)*World.T,player.y-(site[1]+0.5)*World.T);
            if(d<distance){distance=d;best=i;}
        }
        return World.LANDMARKS[best];
    }
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
                if(hint&&mode==Mode.PLAYING){
                    g.setColor(new Color(241,201,121,150));
                    for(Point p:path)g.fillRect((int)((p.x+0.5)*World.T)-cx-1,(int)((p.y+0.5)*World.T)-cy-1,3,3);
                }
                if(mode!=Mode.TITLE)drawBeacons(g,cx,cy);
                drawList.clear();
                for(World.Tree tree:world.trees)if(visible(tree.x,tree.y,cx,cy,65))drawList.add(tree);
                for(Enemy enemy:enemies)if(visible(enemy.x,enemy.y,cx,cy,50))drawList.add(enemy);
                if(mode!=Mode.TITLE)drawList.add(player);
                drawList.sort(Comparator.comparingDouble(Drawable::sortY));
                for(Drawable drawable:drawList)drawable.draw(g,cx,cy,alpha);
                Fx.draw(g,cx,cy);
                if(mode!=Mode.TITLE)drawHud(g);
                if(mapOpen)drawMap(g);
                if(journalOpen)drawJournal(g);
                if(debug)drawDiagnostics(g);
                if(mode==Mode.TITLE)drawTitle(g);
                else if(mode==Mode.PAUSED)drawModal(g,"PAUSED","Esc / P / Enter to resume");
                else if(mode==Mode.DEFEAT)drawModal(g,"AGYU HAS FALLEN","Press R to begin a new journey");
                else if(mode==Mode.VICTORY)drawModal(g,"THE GROVE REMEMBERS","All three shrines restored. Press R to play again.");
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
    private boolean visible(double x,double y,int cx,int cy,int margin){
        return x>cx-margin&&x<cx+VIEW_W+margin&&y>cy-margin&&y<cy+VIEW_H+margin;
    }
    private String siteName(int i){return new String[]{"HOME","GROVE","LAKE","RIDGE","GUARDIAN"}[i];}
    private Color siteColor(int i){
        if(i==0)return new Color(0xB3DDF2);
        if(i==4)return quests.done("guardian")?new Color(0xA4E3AE):new Color(0xEF8C79);
        return quests.done(QuestLog.SHRINES[i-1])?new Color(0xA4E3AE):GOLD;
    }
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
    private void card(Graphics2D g,int x,int y,int width,int height){
        g.setColor(new Color(18,26,29,235));g.fillRect(x,y,width,height);
        g.setColor(new Color(0x796848));g.drawRect(x,y,width,height);
    }
    private void text(Graphics2D g,String text,int x,int y,Color color){g.setColor(color);g.drawString(text,x,y);}
    private void center(Graphics2D g,String text,int y,Color color){
        text(g,text,(VIEW_W-g.getFontMetrics().stringWidth(text))/2,y,color);
    }
    private void drawHud(Graphics2D g){
        card(g,10,10,230,57);g.setFont(BOLD);
        text(g,"AGYU / LOST GROVE",18,25,GOLD);
        for(int i=0;i<player.maxHealth();i++){
            g.setColor(i<player.health()?new Color(0xE66A66):new Color(0x46534A));
            g.fillRect(18+i*13,34,9,8);
        }
        g.setFont(SMALL);
        text(g,"SEALS "+quests.seals()+"/3   HERBS "+player.inventory().count(Inventory.Item.HERB)+" [H]",92,42,CREAM);
        text(g,"M map  Tab journal  N trail",18,58,MUTED);
        card(g,VIEW_W-132,10,121,34);
        text(g,"LOST GROVE / "+(int)(time/60)+":"+String.format("%02d",(int)time%60),VIEW_W-124,24,CREAM);
        text(g,"Guards defeated: "+kills,VIEW_W-124,36,MUTED);
        if(player.comboStep()>0){
            card(g,VIEW_W-186,52,175,47);
            int step=player.comboStep();
            text(g,step+" / "+player.comboName(),VIEW_W-178,65,step==6?GOLD:CREAM);
            for(int i=0;i<SpearAttack.STAGES;i++){
                g.setColor(i<step?new Color(SpearAttack.color(i)):new Color(0x3C514D));
                g.fillRect(VIEW_W-178+i*26,71,21,3);
            }
            text(g,"Tap LMB / J to chain",VIEW_W-178,89,MUTED);
        }
        card(g,10,VIEW_H-66,VIEW_W-21,55);g.setFont(SMALL);
        text(g,noticeTimer>0?notice:quests.objective(),18,VIEW_H-50,noticeTimer>0?GOLD:CREAM);
        text(g,"WASD move  Shift run  Space jump  E interact",18,VIEW_H-35,MUTED);
        text(g,"Tap LMB / J combo  Hold RMB shield  H heal",18,VIEW_H-21,MUTED);
    }
    private void drawMap(Graphics2D g){
        g.setColor(new Color(0,0,0,145));g.fillRect(0,0,VIEW_W,VIEW_H);
        card(g,66,42,348,277);g.setFont(BOLD);center(g,"MAP OF THE LOST GROVE",61,GOLD);
        int ox=80,oy=72,scale=5;
        for(int r=0;r<world.rows;r++)for(int c=0;c<world.cols;c++){
            int tile=world.tile[r][c];
            g.setColor(new Color(tile==World.WATER?0x5088AB:tile==World.ROCK?0x52594F:tile==World.DIRT?0xAF9465:0x427359));
            g.fillRect(ox+c*scale,oy+r*scale,scale,scale);
        }
        for(World.Tree tree:world.trees){g.setColor(new Color(0x294D3C));g.fillRect(ox+World.col(tree.x)*scale,oy+World.row(tree.y)*scale,3,3);}
        for(int i=0;i<World.LANDMARKS.length;i++){
            int[] s=World.LANDMARKS[i];int x=ox+s[0]*scale,y=oy+s[1]*scale;
            g.setColor(INK);g.fillRect(x-2,y-2,10,10);
            g.setColor(siteColor(i));g.fillRect(x,y,6,6);
            g.setFont(SMALL);text(g,siteName(i),x-10,y-4,CREAM);
        }
        g.setColor(Color.WHITE);g.fillRect(ox+World.col(player.x)*scale,oy+World.row(player.y)*scale,4,4);
        g.setFont(SMALL);center(g,"White: Agyu   Gold: shrine   M: close / resume",332,CREAM);
    }
    private void drawJournal(Graphics2D g){
        g.setColor(new Color(0,0,0,145));g.fillRect(0,0,VIEW_W,VIEW_H);
        card(g,35,50,410,258);g.setFont(BOLD);center(g,"AGYU'S JOURNAL",72,GOLD);
        g.setFont(SMALL);int y=97;
        for(int i=0;i<QuestLog.SHRINES.length;i++){
            String id=QuestLog.SHRINES[i];
            text(g,(quests.done(id)?"[x] ":"[ ] ")+siteName(i+1)+" shrine",50,y,CREAM);y+=15;
        }
        text(g,(quests.done("guardian")?"[x] ":"[ ] ")+"Guardian",50,y,CREAM);y+=15;
        text(g,(quests.done("home")?"[x] ":"[ ] ")+"Return home",50,y,CREAM);
        y+=22;text(g,"LATEST EVENTS",50,y,GOLD);
        for(String event:quests.history()){
            y+=14;
            // Keep each rendered line within the journal card.
            if(event.length()>62)event=event.substring(0,59)+"...";
            text(g,event,50,y,MUTED);if(y>281)break;
        }
        center(g,"Tab: close / resume",330,CREAM);
    }
    private void drawDiagnostics(Graphics2D g){
        card(g,10,78,259,66);g.setFont(SMALL);
        text(g,"F1 / ALGORITHM DIAGNOSTICS",18,92,GOLD);
        text(g,String.format(java.util.Locale.ROOT,"Paint %d/s | update %.2fms | render %.2fms",shownFps,updateMs,renderMs),18,106,CREAM);
        text(g,"BFS builds "+flowBuilds+" | A* builds "+pathBuilds+" | trail "+path.size(),18,120,CREAM);
        text(g,"Tree checks "+world.collisionChecks+" | particles "+Fx.list.size(),18,134,MUTED);
    }
    private void drawTitle(Graphics2D g){
        g.setColor(new Color(10,18,20,215));g.fillRect(0,0,VIEW_W,VIEW_H);
        g.setFont(SMALL);center(g,"A JAVA ADVENTURE / DSA + OBJECT ORIENTED PROGRAMMING",52,MUTED);
        g.setFont(TITLE);center(g,"AGYU",100,GOLD);
        g.setFont(BOLD);center(g,"GUARDIANS OF THE LOST GROVE",121,CREAM);
        BufferedImage hero=Assets.front;
        g.drawImage(hero,VIEW_W/2-hero.getWidth(),133,hero.getWidth()*2,hero.getHeight()*2,null);
        g.setFont(SMALL);
        center(g,"Restore three shrines. Awaken the guardian. Return home.",267,CREAM);
        center(g,"WASD move / Shift run / Space jump / E interact",282,MUTED);
        center(g,"Tap LMB / J combo / hold RMB shield / H heal",298,MUTED);
        g.setFont(BOLD);center(g,"PRESS ENTER TO BEGIN",321,GOLD);
        g.setFont(SMALL);center(g,"An original fictional adventure featuring your Agyu character",344,MUTED);
    }
    private void drawModal(Graphics2D g,String title,String subtitle){
        g.setColor(new Color(8,15,18,195));g.fillRect(0,0,VIEW_W,VIEW_H);
        card(g,35,125,410,103);g.setFont(new Font("Serif",Font.BOLD,25));center(g,title,163,GOLD);
        g.setFont(SMALL);center(g,subtitle,191,CREAM);
        if(mode==Mode.VICTORY)center(g,"Seals "+quests.seals()+"/3 / guards "+kills+" / time "+(int)time+"s",213,MUTED);
    }
}
