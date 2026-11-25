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
    // [SECTION: OOP - POLYMORPHISM] Forest wildlife and the three villains share the Enemy abstraction.
    private final List<Enemy> enemies=new ArrayList<>();
    private final List<Npc> npcs=new ArrayList<>();
    // [SECTION: DSA - ARRAY LIST] Active coin bundles are updated and depth-sorted with actors.
    private final List<CoinDrop> coinDrops=new ArrayList<>();
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
    private boolean mapOpen,journalOpen,inventoryOpen,hint,debug;
    private final InventoryScreen inventoryScreen=new InventoryScreen();
    private final SmithShop smithShop=new SmithShop();
    private final TitleMenu titleMenu;
    // [SECTION: DSA - BOUNDED FIFO QUEUE] UI clicks are applied by the simulation thread while the world is paused.
    private final ArrayDeque<Point> inventoryClicks=new ArrayDeque<>();
    private final ArrayDeque<Point> shopClicks=new ArrayDeque<>();
    private final ArrayDeque<Point> titleClicks=new ArrayDeque<>();
    private TitleMenu.Action pendingTitleAction=TitleMenu.Action.NONE;
    private double titleLoading;
    private boolean closingTitle;
    private int hotbarDrag=-1;
    private volatile boolean running;
    private Thread thread;
    private double updateMs,renderMs;
    private long frameWindow=System.nanoTime();
    private int frames,shownFps;

    // [BLOCK: FUNCTION] GamePanel.GamePanel - Initialize GamePanel state.
    GamePanel() throws java.io.IOException {
        titleMenu=new TitleMenu();
        setPreferredSize(new Dimension(W,H));
        setBackground(INK);setFocusable(true);setFocusTraversalKeysEnabled(false);
        addKeyListener(keys);
        addMouseListener(new java.awt.event.MouseAdapter(){
            // [BLOCK: UI INPUT ROUTING] GamePanel.mousePressed - Select hotbar boxes without leaking UI clicks into combat.
            @Override public void mousePressed(java.awt.event.MouseEvent event){
                requestFocusInWindow();synchronized(lock){
                    if(mode==Mode.TITLE){
                        if(event.getButton()==java.awt.event.MouseEvent.BUTTON1&&titleClicks.size()<16)titleClicks.addLast(pointer(event));
                        return;
                    }
                    if(smithShop.isOpen()){
                        if(mode==Mode.PLAYING&&event.getButton()==java.awt.event.MouseEvent.BUTTON1&&shopClicks.size()<16)shopClicks.addLast(pointer(event));
                        return;
                    }
                    if(inventoryOpen){
                        if(mode==Mode.PLAYING&&event.getButton()==java.awt.event.MouseEvent.BUTTON1&&inventoryClicks.size()<32)inventoryClicks.addLast(pointer(event));
                        return;
                    }
                    int slot=pointerSlot(event);
                    if(event.getButton()==java.awt.event.MouseEvent.BUTTON1&&mode==Mode.PLAYING&&!mapOpen&&!journalOpen&&slot>=0){hotbarDrag=slot;keys.selectSlot(slot);}
                    else keys.mousePressed(event);
                }
            }
            // [BLOCK: HOTBAR DRAG] GamePanel.mouseReleased - Swap dropped slots through deferred input, then release held combat buttons.
            @Override public void mouseReleased(java.awt.event.MouseEvent event){synchronized(lock){
                if(smithShop.isOpen()){keys.mouseReleased(event);return;}
                if(event.getButton()==java.awt.event.MouseEvent.BUTTON1&&hotbarDrag>=0){
                    int slot=pointerSlot(event);if(mode==Mode.PLAYING&&!mapOpen&&!journalOpen&&!inventoryOpen&&slot>=0&&slot!=hotbarDrag)keys.swapSlots(hotbarDrag,slot);
                    hotbarDrag=-1;
                }
                keys.mouseReleased(event);
            }}
            @Override public void mouseExited(java.awt.event.MouseEvent event){
                synchronized(lock){if(mode==Mode.TITLE){titleMenu.hover(-1,-1);setCursor(Cursor.getDefaultCursor());}}
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter(){
            @Override public void mouseMoved(java.awt.event.MouseEvent event){
                synchronized(lock){
                    Point p=pointer(event);
                    int cursor=mode==Mode.TITLE?titleMenu.hover(p.x,p.y):Cursor.DEFAULT_CURSOR;
                    if(getCursor().getType()!=cursor)setCursor(Cursor.getPredefinedCursor(cursor));
                }
            }
        });
        addMouseWheelListener(keys);
        addFocusListener(new FocusAdapter(){
            // [BLOCK: FUNCTION] GamePanel.focusLost - Handle focus lost for GamePanel.
            @Override public void focusLost(FocusEvent event){
                keys.clear();
                synchronized(lock){hotbarDrag=-1;if(mode==Mode.PLAYING){mode=Mode.PAUSED;freezeInterpolation();}}
            }
        });
        Assets.load();reset();titleMenu.open(false);
    }
    // [BLOCK: POINTER COORDINATES] GamePanel.pointer - Scale inventory clicks with the same viewport transform used for rendering.
    private Point pointer(java.awt.event.MouseEvent event){return new Point(getWidth()<=0?-1:(int)((long)event.getX()*VIEW_W/getWidth()),getHeight()<=0?-1:(int)((long)event.getY()*VIEW_H/getHeight()));}
    // [BLOCK: SCALED UI HIT TEST] GamePanel.pointerSlot - Convert native window coordinates into the shared art-resolution hotbar layout.
    private int pointerSlot(java.awt.event.MouseEvent event){
        if(getWidth()<=0||getHeight()<=0)return -1;
        return HotbarHud.slotAt((int)((long)event.getX()*VIEW_W/getWidth()),(int)((long)event.getY()*VIEW_H/getHeight()),VIEW_W,VIEW_H);
    }
    // [BLOCK: SESSION: RESTART] GamePanel.reset - Create a fresh world, actors, inventory, quests and timing state.
    void reset(){
        synchronized(lock){
            Fx.clear();keys.clear();hotbarDrag=-1;inventoryClicks.clear();shopClicks.clear();titleClicks.clear();smithShop.close();
            pendingTitleAction=TitleMenu.Action.NONE;titleLoading=0;closingTitle=false;
            world=new World(7);villageMap=new VillageMap(world);player=new Player(World.SPAWN_X,World.SPAWN_Y);
            quests=new QuestLog();enemies.clear();coinDrops.clear();events.clear();drawList.clear();
            npcs.clear();npcs.addAll(NpcPopulation.create(world));
            forestEncounters=new ForestEncounters(world);forestEncounters.populate(enemies);
            time=flowTimer=pathTimer=0;alpha=0;
            flow=null;flowVersion=-1;flowC=flowR=-1;flowBuilds=pathBuilds=kills=ordinaryKills=0;
            path=List.of();mapOpen=journalOpen=inventoryOpen=hint=debug=false;
            camX=prevCamX=targetX();camY=prevCamY=targetY();
            mode=Mode.TITLE;
        }
    }
    // [BLOCK: FUNCTION] GamePanel.start - Handle start for GamePanel.
    synchronized void start(){
        if(running)return;
        titleMenu.startAudio();
        running=true;thread=new Thread(this,"agyu-game-loop");thread.setDaemon(true);thread.start();
    }
    // [BLOCK: FUNCTION] GamePanel.stop - Handle stop for GamePanel.
    synchronized void stop(){
        running=false;
        titleMenu.stopAudio();
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
        player.savePrev();for(Enemy enemy:enemies)enemy.savePrev();for(Npc npc:npcs)npc.savePrev();for(CoinDrop coin:coinDrops)coin.savePrev();
        prevCamX=camX;prevCamY=camY;
    }
    // [BLOCK: SIMULATION / DSA EVENT QUEUE] GamePanel.update - Apply input, update actors, enqueue defeats, then process rewards safely.
    void update(double dt){
        KeyHandler.Input input=keys.poll();
        if(input.pressed(KeyHandler.DEBUG))debug=!debug;
        if(mode==Mode.TITLE){updateTitle(input,dt);return;}
        if(mode==Mode.DEFEAT||mode==Mode.VICTORY){
            if(input.pressed(KeyHandler.RESTART)){reset();mode=Mode.PLAYING;}
            else {
                // Finish an interrupted jump under the defeat overlay and settle interpolation.
                player.savePrev();
                if(mode==Mode.DEFEAT&&player.airborne())player.update(dt,input,world,enemies);
            }
            return;
        }
        if(mode==Mode.PLAYING&&smithShop.isOpen()){
            if(input.pressed(KeyHandler.PAUSE)||input.pressed(KeyHandler.INTERACT)){closeShop();freezeInterpolation();return;}
            updateShop(input);freezeInterpolation();return;
        }
        if(mode==Mode.PLAYING&&inventoryOpen&&input.pressed(KeyHandler.PAUSE)){closeInventory();freezeInterpolation();return;}
        if(mode==Mode.PAUSED&&input.pressed(KeyHandler.TITLE_MENU)){
            closeInventory();closeShop();mapOpen=journalOpen=false;titleClicks.clear();
            titleMenu.open(true);mode=Mode.TITLE;return;
        }
        if(input.pressed(KeyHandler.PAUSE)||(mode==Mode.PAUSED&&input.pressed(KeyHandler.START))){
            mode=mode==Mode.PAUSED?Mode.PLAYING:Mode.PAUSED;freezeInterpolation();return;
        }
        if(mode!=Mode.PLAYING)return;
        if(input.pressed(KeyHandler.INVENTORY)){
            if(inventoryOpen)closeInventory();
            else {inventoryOpen=true;mapOpen=journalOpen=false;inventoryScreen.open(player);inventoryClicks.clear();keys.clearOverlayActions();hotbarDrag=-1;}
            freezeInterpolation();return;
        }
        if(input.pressed(KeyHandler.MAP)){mapOpen=!mapOpen;journalOpen=false;closeInventory();}
        if(input.pressed(KeyHandler.JOURNAL)){journalOpen=!journalOpen;mapOpen=false;closeInventory();}
        if(inventoryOpen){updateInventory(input);freezeInterpolation();return;}
        if(input.pressed(KeyHandler.HINT)){hint=!hint;pathTimer=0;}
        // Reading the map or journal pauses the world, including combat timers.
        if(mapOpen||journalOpen){freezeInterpolation();return;}
        player.savePrev();for(Enemy enemy:enemies)enemy.savePrev();for(Npc npc:npcs)npc.savePrev();for(CoinDrop coin:coinDrops)coin.savePrev();
        prevCamX=camX;prevCamY=camY;
        if(Fx.hitStop>0){
            keys.defer(input,KeyHandler.ATTACK|KeyHandler.JUMP|KeyHandler.DASH|KeyHandler.INTERACT|KeyHandler.HEAL|KeyHandler.USE_ITEM);
            Fx.hitStop=Math.max(0,Fx.hitStop-dt);Fx.update(dt);return;
        }
        time+=dt;world.updateAmbient(dt);
        for(Enemy enemy:enemies)enemy.refreshNightPower(world);
        player.update(dt,input,world,enemies);
        for(Npc npc:npcs)npc.update(dt,world,player);
        updateNavigation(dt);
        // [SECTION: OOP - DYNAMIC DISPATCH] Each enemy subtype dispatches its movement and combat policy through Enemy.update.
        for(Enemy enemy:enemies){
            enemy.update(dt,player,world,flow);
            if(enemy.claimDefeat())events.addLast(new GameEvent(enemy));
        }
        processEvents();
        for(CoinDrop coin:coinDrops)coin.update(dt,player);
        coinDrops.removeIf(CoinDrop::collected);
        enemies.removeIf(Enemy::removable);
        forestEncounters.update(dt,player,enemies);
        if(!player.alive()){mode=Mode.DEFEAT;freezeInterpolation();}
        else if(!player.airborne()&&!player.dashing()&&quests.finish("forest")){mode=Mode.VICTORY;freezeInterpolation();}
        else if(input.pressed(KeyHandler.INTERACT)&&!player.airborne()&&!player.dashing())interact();
        Fx.update(dt);
        double follow=1-Math.exp(-6*dt);
        camX+=(targetX()-camX)*follow;camY+=(targetY()-camY)*follow;
    }
    // [BLOCK: TITLE TRANSACTION] Keep the world paused through a single mouse or keyboard loading transition.
    private void updateTitle(KeyHandler.Input input,double dt){
        if(closingTitle)return;
        TitleMenu.Action action=titleMenu.update(input,dt);
        if(pendingTitleAction!=TitleMenu.Action.NONE){
            titleClicks.clear();
            titleLoading-=dt;
            if(titleLoading>1e-9)return;
            if(pendingTitleAction==TitleMenu.Action.NEW_GAME&&titleMenu.canContinue())reset();
            pendingTitleAction=TitleMenu.Action.NONE;
            titleMenu.leave();keys.clearOverlayActions();mode=Mode.PLAYING;
            java.awt.EventQueue.invokeLater(()->setCursor(Cursor.getDefaultCursor()));
            return;
        }
        while(action==TitleMenu.Action.NONE&&!titleClicks.isEmpty()){
            Point click=titleClicks.removeFirst();
            action=titleMenu.click(click.x,click.y);
        }
        if(action==TitleMenu.Action.NEW_GAME||action==TitleMenu.Action.CONTINUE){
            pendingTitleAction=action;titleLoading=TitleMenu.LOADING_SECONDS;titleClicks.clear();titleMenu.beginLoading();
        }else if(action==TitleMenu.Action.EXIT){
            closingTitle=true;
            java.awt.EventQueue.invokeLater(()->{
                java.awt.Window window=javax.swing.SwingUtilities.getWindowAncestor(this);
                if(window!=null)window.dispose();
            });
        }
    }
    // [BLOCK: MODAL INPUT] GamePanel.closeInventory - Release mouse actions and discard queued UI clicks without retriggering held keys.
    private void closeInventory(){inventoryOpen=false;inventoryClicks.clear();hotbarDrag=-1;keys.clearOverlayActions();}
    // [BLOCK: SHOP MODAL] GamePanel.closeShop - Close smith UI and discard queued pointer actions.
    private void closeShop(){smithShop.close();shopClicks.clear();hotbarDrag=-1;keys.clearOverlayActions();}
    // [BLOCK: SHOP INPUT] GamePanel.updateShop - Select and purchase upgrades while world clocks remain paused.
    private void updateShop(KeyHandler.Input input){
        if(input.slot>=0&&input.slot<4)smithShop.select(input.slot);
        if(input.scroll!=0)smithShop.cycle(input.scroll>0?1:-1);
        if(input.pressed(KeyHandler.START)||input.pressed(KeyHandler.ATTACK)||input.pressed(KeyHandler.USE_ITEM))smithShop.purchase(player);
        while(smithShop.isOpen()&&!shopClicks.isEmpty()){
            int offer=smithShop.cardAt(shopClicks.removeFirst());
            if(offer>=0){smithShop.select(offer);smithShop.purchase(player);}
        }
    }
    // [BLOCK: INVENTORY TRANSACTION] GamePanel.inventoryAction - Apply references on the simulation thread and route drinking through the normal animated use action.
    private void inventoryAction(InventoryScreen.Action action,int index){
        switch(action){
            case ITEM:inventoryScreen.selectEntry(index);break;
            case SLOT:player.hotbar().select(index);break;
            case EQUIP:
                Inventory.Item selected=inventoryScreen.item();
                int slot=player.hotbar().selectedIndex();
                if(selected==Inventory.Item.SPEAR||selected==Inventory.Item.SHIELD){
                    int currentSlot=player.hotbar().indexOf(selected);
                    player.equipGear(currentSlot>=0?currentSlot:slot,selected,inventoryScreen.selectedLevel());
                }else player.equipItem(slot,selected);
                break;
            case CLEAR:player.clearSlot(player.hotbar().selectedIndex());break;
            case TURN:inventoryScreen.turn();break;
            case CLOSE:closeInventory();break;
            case USE:
                if(player.inventoryCanUse(inventoryScreen.item())&&player.equipItem(player.hotbar().selectedIndex(),inventoryScreen.item())){
                    closeInventory();keys.useSelectedItem();
                }break;
            default:break;
        }
    }
    // [BLOCK: PAUSED UI / FIFO] GamePanel.updateInventory - Process bag selection and bounded pointer commands while gameplay clocks remain frozen.
    private void updateInventory(KeyHandler.Input input){
        if(input.slot>=0)player.hotbar().select(input.slot);
        if(input.scroll!=0)inventoryScreen.cycle(input.scroll);
        if(input.pressed(KeyHandler.MENU_UP))inventoryScreen.cycle(-3);
        if(input.pressed(KeyHandler.MENU_DOWN))inventoryScreen.cycle(3);
        if(input.pressed(KeyHandler.MENU_LEFT))inventoryScreen.cycle(-1);
        if(input.pressed(KeyHandler.MENU_RIGHT))inventoryScreen.cycle(1);
        if(input.pressed(KeyHandler.START))inventoryAction(InventoryScreen.Action.EQUIP,0);
        if(input.pressed(KeyHandler.USE_ITEM))inventoryAction(InventoryScreen.Action.USE,0);
        while(inventoryOpen&&!inventoryClicks.isEmpty()){
            Point click=inventoryClicks.removeFirst();InventoryScreen.Command command=inventoryScreen.hit(click.x,click.y);
            inventoryAction(command.action(),command.index());
        }
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
            // [SECTION: DSA - PARTITION] Split one immutable reward into bounded collectible bundles.
            coinDrops.addAll(CoinDrop.scatter(enemy.x,enemy.y,enemy.coinReward()));
            if(enemy instanceof ArenaBoss boss){
                if(quests.finish(boss.questId())){
                    player.inventory().add(Inventory.Item.HEALTH_POTION,2);
                    player.inventory().add(Inventory.Item.ENERGY_POTION,1);player.inventory().add(Inventory.Item.SHIELD_POTION,1);
                }
                pathTimer=0;
            }else{
                ordinaryKills++;
                if(ordinaryKills%2==0)player.inventory().add(Inventory.Item.HEALTH_POTION,1);
                if(ordinaryKills%4==0)player.inventory().add(ordinaryKills%8==0?Inventory.Item.SHIELD_POTION:Inventory.Item.ENERGY_POTION,1);
            }
        }
    }
    // [BLOCK: GAMEPLAY INTERACTION] GamePanel.interact - Open a nearby gate or talk to the nearest available villager.
    private void interact(){
        if(world.interactGate(player,enemies,npcs))return;
        Npc nearest=null;double range=Double.MAX_VALUE;
        for(Npc npc:npcs)if(npc.canTalk(player)){
            double d=Math.hypot(npc.x()-player.x,npc.y()-player.y);
            if(d<range){range=d;nearest=npc;}
        }
        if(nearest!=null){
            if(nearest.role()==Npc.Role.SMITH){
                mapOpen=journalOpen=false;closeInventory();smithShop.open();shopClicks.clear();keys.clearOverlayActions();
                return;
            }
            nearest.talk(player);
        }
    }
    // [BLOCK: BOSS OBJECTIVE SELECTION] GamePanel.objectiveSite - Route the optional trail to the nearest undefeated villain.
    private int[] objectiveSite(){
        ForestRegion.Arena nearest=null;double best=Double.MAX_VALUE;
        for(ForestRegion.Arena arena:world.forest.arenas())if(!quests.done(QuestLog.BOSSES[arena.id()-1])){
            double distance=Math.hypot(player.x-(arena.c()+.5)*World.T,player.y-(arena.r()+.5)*World.T);
            if(distance<best){best=distance;nearest=arena;}
        }
        return nearest==null?new int[]{World.col(World.SPAWN_X),World.row(World.SPAWN_Y)}:new int[]{nearest.c(),nearest.r()};
    }
    // [BLOCK: OOP: POLYMORPHISM / SORTING] GamePanel.paintComponent - Sort Drawable objects by feet depth and dispatch each concrete draw implementation.
    @Override protected void paintComponent(Graphics graphics){
        super.paintComponent(graphics);
        synchronized(lock){
            long begin=System.nanoTime();
            if(mode==Mode.TITLE){
                Graphics2D screen=(Graphics2D)graphics.create();
                try{
                    screen.scale(getWidth()/(double)VIEW_W,getHeight()/(double)VIEW_H);
                    titleMenu.draw(screen);
                }finally{screen.dispose();}
                return;
            }
            Graphics2D g=buffer.createGraphics();
            try{
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
                int cx=(int)Math.round(prevCamX+(camX-prevCamX)*alpha)+Fx.shakeX;
                int cy=(int)Math.round(prevCamY+(camY-prevCamY)*alpha)+Fx.shakeY;
                world.drawGround(g,cx,cy,VIEW_W,VIEW_H,time);
                for(Enemy enemy:enemies)if(enemy instanceof ArenaBoss boss&&visible(boss.x,boss.y,cx,cy,512))boss.drawGround(g,cx,cy,alpha);
                for(Enemy enemy:enemies)if(enemy instanceof ForestBeast boar&&visible(boar.x,boar.y,cx,cy,128))boar.drawGround(g,cx,cy,alpha);
                if(hint&&mode==Mode.PLAYING){
                    g.setColor(new Color(241,201,121,150));
                    for(Point p:path)g.fillRect((int)((p.x+0.5)*World.T)-cx-1,(int)((p.y+0.5)*World.T)-cy-1,3,3);
                }
                drawList.clear();
                for(World.Tree tree:world.trees)if(visible(tree.x,tree.y,cx,cy,160))drawList.add(tree);
                for(World.Building house:world.buildings)if(visible(house.x,house.y,cx,cy,224))drawList.add(house);
                for(World.Prop prop:world.props)if(visible(prop.x,prop.y,cx,cy,160))drawList.add(prop);
                for(Npc npc:npcs)if(visible(npc.x(),npc.y(),cx,cy,112))drawList.add(npc);
                for(Enemy enemy:enemies)if(visible(enemy.x,enemy.y,cx,cy,enemy instanceof ArenaBoss?512:96))drawList.add(enemy);
                for(CoinDrop coin:coinDrops)drawList.add(coin);
                drawList.add(player);
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
                for(Enemy enemy:enemies)if(enemy instanceof Sigbin creature)creature.drawOverlay(g,VIEW_W,VIEW_H);
                for(Npc npc:npcs)npc.drawDialogue(g,cx,cy,alpha,VIEW_W,VIEW_H);
                drawHud(g);
                if(mapOpen)drawMap(g);
                if(journalOpen)drawJournal(g);
                if(inventoryOpen)inventoryScreen.draw(g,player);
                if(smithShop.isOpen())smithShop.draw(g,player);
                if(debug)drawDiagnostics(g);
                if(mode==Mode.PAUSED)drawModal(g,"PAUSED","Esc / Enter to resume   |   T for title");
                else if(mode==Mode.DEFEAT)drawModal(g,"AGYU HAS FALLEN","Press R to begin a new journey");
                else if(mode==Mode.VICTORY)drawModal(g,"THE GROVE REMEMBERS","Three villains defeated. R to play again.");
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
    private void vitalBar(Graphics2D g,int x,int y,int width,int height,double ratio,Color fill,Color shine){
        g.setColor(new Color(0x080F17));g.fillRect(x-1,y-1,width+2,height+2);
        g.setColor(new Color(0x35444C));g.fillRect(x,y,width,height);
        int filled=(int)Math.round(width*Math.max(0,Math.min(1,ratio)));
        g.setColor(fill);g.fillRect(x,y,filled,height);
        if(filled>0){g.setColor(shine);g.fillRect(x,y,filled,2);}
    }
    // [BLOCK: FUNCTION] GamePanel.drawHud - Render draw hud using the current graphics state.
    private void drawHud(Graphics2D g){
        g.setFont(SMALL);
        text(g,"HEALTH",19,22,INK);text(g,"HEALTH",18,21,CREAM);
        vitalBar(g,66,13,170,10,player.health()/(double)player.maxHealth(),new Color(0x69C833),new Color(0xC3F578));
        text(g,"ENERGY",19,38,INK);text(g,"ENERGY",18,37,CREAM);
        vitalBar(g,66,29,170,10,player.energy()/player.maxEnergy(),new Color(player.energyExhausted()?0xAD6925:0xED9C23),new Color(0xFFE07B));
        text(g,"SHIELD",19,54,INK);text(g,"SHIELD",18,53,CREAM);
        vitalBar(g,66,45,170,10,player.protection()/(double)player.maxProtection(),new Color(0x22A9EB),new Color(0x8CEDFF));
        text(g,"COINS "+player.coins(),250,22,INK);text(g,"COINS "+player.coins(),249,21,new Color(0xFFD45A));
        card(g,VIEW_W-112,10,104,92);
        text(g,world.forest.contains(player.x,player.y)?"FOREST":"VILLAGE",VIEW_W-104,21,GOLD);text(g,"N",VIEW_W-22,21,MUTED);
        g.setColor(new Color(0x796848));g.drawRect(VIEW_W-109,25,98,74);
        villageMap.draw(g,player,quests,VIEW_W-108,26,96,72,alpha,false);
        drawTimeOfDay(g);
        drawBossHealth(g);
        if(!inventoryOpen&&!smithShop.isOpen())HotbarHud.draw(g,player,VIEW_W,VIEW_H);
    }
    // [BLOCK: WORLD CLOCK HUD] GamePanel.drawTimeOfDay - Show a compact numbered phase and next-change countdown below the resource bars.
    private void drawTimeOfDay(Graphics2D g){
        DayNightCycle cycle=world.dayNight;Color color=new Color(cycle.night()?0xD2B8FF:0xF1D894);
        String label=cycle.label()+"  "+cycle.countdown();
        text(g,label,19,71,INK);text(g,label,18,70,color);
        if(cycle.night()){text(g,"STRONGER FOES",139,71,INK);text(g,"STRONGER FOES",138,70,color);}
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
        text(g,name,tx+1,VIEW_H-89,INK);text(g,name,tx,VIEW_H-90,CREAM);
        vitalBar(g,VIEW_W/2-100,VIEW_H-84,200,8,nearest.health()/(double)nearest.maxHealth(),color,new Color(0xF6E5C6));
    }
    // [BLOCK: FUNCTION] GamePanel.drawMap - Render draw map using the current graphics state.
    private void drawMap(Graphics2D g){
        g.setColor(new Color(0,0,0,145));g.fillRect(0,0,VIEW_W,VIEW_H);
        card(g,66,42,348,277);g.setFont(BOLD);center(g,"VILLAGE OF THE LOST GROVE",61,GOLD);
        villageMap.draw(g,player,quests,80,72,320,240,alpha,true);
        g.setFont(SMALL);center(g,"Arrow: Agyu  Red: villains  Green: defeated  M: close",332,CREAM);
    }
    // [BLOCK: FUNCTION] GamePanel.drawJournal - Render draw journal using the current graphics state.
    private void drawJournal(Graphics2D g){
        g.setColor(new Color(0,0,0,145));g.fillRect(0,0,VIEW_W,VIEW_H);
        card(g,35,50,410,258);g.setFont(BOLD);center(g,"AGYU'S JOURNAL",72,GOLD);
        g.setFont(SMALL);int y=97;
        for(int i=0;i<QuestLog.BOSSES.length;i++){
            String id=QuestLog.BOSSES[i];text(g,(quests.done(id)?"[x] ":"[ ] ")+(i+1)+" "+id.toUpperCase(java.util.Locale.ROOT)+" / forest clearing "+(i+1),50,y,CREAM);y+=18;
        }
        text(g,(quests.done("forest")?"[x] ":"[ ] ")+"Protect the forest",50,y,CREAM);
        y+=27;text(g,"LATEST EVENTS",50,y,GOLD);
        for(String event:quests.history()){
            y+=14;if(event.length()>62)event=event.substring(0,59)+"...";
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
    // [BLOCK: FUNCTION] GamePanel.drawModal - Render draw modal using the current graphics state.
    private void drawModal(Graphics2D g,String title,String subtitle){
        g.setColor(new Color(8,15,18,195));g.fillRect(0,0,VIEW_W,VIEW_H);
        card(g,35,125,410,103);g.setFont(new Font("Serif",Font.BOLD,25));center(g,title,163,GOLD);
        g.setFont(SMALL);center(g,subtitle,191,CREAM);
        if(mode==Mode.VICTORY)center(g,"Bosses "+quests.bossesDefeated()+"/3 / enemies "+kills+" / time "+(int)time+"s",213,MUTED);
    }
}
