import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

/** Deterministic, dependency-free logic, progression and off-screen render checks. */
public final class GameTests {
    private static int checks;
    private static final Canvas SOURCE=new Canvas();
    private GameTests(){}
    public static void main(String[] args)throws Exception{
        World w=new World(7);Random random=new Random(20261102L);
        for(int i=0;i<20000;i++){
            Rectangle2D box=new Rectangle2D.Double(random.nextDouble()*(w.cols*World.T+256)-128,
                    random.nextDouble()*(w.rows*World.T+256)-128,0.1+random.nextDouble()*128,0.1+random.nextDouble()*128);
            check(w.blocked(box)==w.blockedBruteForce(box),"Spatial index differs from exact reference");
        }
        long indexedChecks=w.collisionChecks;
        for(int[] start:World.LANDMARKS){
            int[][] distance=Pathfinder.distances(w,start[0],start[1]);
            for(int[] goal:World.LANDMARKS){
                check(distance[goal[1]][goal[0]]>=0,"Mission landmark unreachable");
                List<Point> path=Pathfinder.find(w,start[0],start[1],goal[0],goal[1]);
                check(path.size()==distance[goal[1]][goal[0]],"A* differs from shortest BFS distance");
                int c=start[0],r=start[1];
                for(Point step:path){
                    check(Math.abs(step.x-c)+Math.abs(step.y-r)==1&&w.walkable(step.x,step.y),"Invalid path step");
                    c=step.x;r=step.y;
                }
                check(c==goal[0]&&r==goal[1],"Path ends at wrong goal");
            }
        }
        check(Pathfinder.find(w,-1,0,32,27).isEmpty(),"Invalid A* start");
        check(Pathfinder.find(w,32,27,0,0).isEmpty(),"Blocked A* goal");
        for(int[] row:Pathfinder.distances(w,-1,0))for(int d:row)check(d==-1,"Invalid BFS root");
        CircularList<Integer> cycle=new CircularList<>();
        expect(java.util.NoSuchElementException.class,cycle::current);
        cycle.add(1);check(cycle.advance()==1,"Singleton wrap");
        cycle.add(2);cycle.add(3);
        check(cycle.size()==3&&cycle.current()==1&&cycle.advance()==2&&cycle.advance()==3&&cycle.advance()==1,"Linked-list wrap");
        Inventory inventory=new Inventory();
        check(!inventory.consume(Inventory.Item.HERB),"Empty inventory consumed");
        inventory.add(Inventory.Item.HERB,2);
        check(inventory.consume(Inventory.Item.HERB)&&inventory.count(Inventory.Item.HERB)==1,"Item consumption");
        expect(IllegalArgumentException.class,()->inventory.add(Inventory.Item.HERB,0));
        expect(IllegalArgumentException.class,()->inventory.add(null,1));
        inventory.add(Inventory.Item.SEAL,Integer.MAX_VALUE);
        expect(ArithmeticException.class,()->inventory.add(Inventory.Item.SEAL,1));
        check(inventory.count(Inventory.Item.SEAL)==Integer.MAX_VALUE,"Overflow modified inventory");
        QuestLog log=new QuestLog();
        check(!log.finish("guardian")&&!log.finish("home")&&!log.finish("unknown"),"Quest prerequisites");
        for(String id:QuestLog.SHRINES){check(log.finish(id),"Independent shrine");check(!log.finish(id),"Duplicate quest");}
        check(log.seals()==3&&log.finish("guardian")&&log.finish("home")&&!log.finish("home"),"Quest progression");
        KeyHandler keys=new KeyHandler();press(keys,KeyEvent.VK_J);
        check(keys.poll().pressed(KeyHandler.ATTACK),"Attack edge lost");
        press(keys,KeyEvent.VK_J);check(!keys.poll().pressed(KeyHandler.ATTACK),"Autorepeat attack");
        release(keys,KeyEvent.VK_J);press(keys,KeyEvent.VK_J);
        check(keys.poll().pressed(KeyHandler.ATTACK),"New attack press");
        press(keys,KeyEvent.VK_W);press(keys,KeyEvent.VK_SHIFT);
        check(keys.poll().up&&keys.poll().run,"Held inputs lost");
        keys.clear();check(!keys.poll().up&&!keys.poll().run&&!keys.poll().pressed(KeyHandler.ATTACK),"Focus clear");
        Enemy target=new Slime(100,100,0);
        check(!target.claimDefeat(),"Living enemy rewarded");
        target.damage(3,0,0);
        check(target.health()==0&&target.claimDefeat()&&!target.claimDefeat(),"Duplicate defeat reward");
        target.damage(Integer.MAX_VALUE,0,0);check(target.health()==0,"Corpse health underflow");
        testNavigationEdges(w);
        testCombat();
        testSprites();
        checks += CombatTests.run();
        checks += ComboTests.run();
        checks += CombatBodyTests.run();
        checks += MouseGuardTests.run();
        checks += JumpTests.run();
        checks += EnergyDashTests.run();
        checks += VillageTests.run();
        checks += NpcTests.run();
        checks += GuardVisualTests.run();
        testSession();
        System.out.println("PASS: "+checks+" checks. Indexed solid checks in 20,000 queries: "+indexedChecks);
    }
    private static void testNavigationEdges(World w){
        for(int r=0;r<w.rows;r++)for(int c=0;c<w.cols;c++)if(w.walkable(c,r)){
            for(int[] direction:new int[][]{{1,0},{0,1}}){
                int nc=c+direction[0],nr=r+direction[1];
                if(!w.walkable(nc,nr))continue;
                for(int sample=0;sample<=16;sample++){
                    double x=(c+0.5+direction[0]*sample/16.0)*World.T;
                    double y=(r+0.5+direction[1]*sample/16.0)*World.T;
                    check(!w.blocked(new Rectangle2D.Double(x-10,y-10,20,10)),"Navigation edge crosses collision geometry");
                }
            }
        }
    }
    private static void testCombat(){
        World w=new World(7);
        Player.Dir[] directions=Player.Dir.values();
        double[][] offsets={{0,25},{0,-28},{-25,0},{25,0}};
        for(int i=0;i<directions.length;i++){
            Player player=new Player(World.SPAWN_X,World.SPAWN_Y);player.dir=directions[i];
            Enemy enemy=new Slime(player.x+offsets[i][0],player.y+offsets[i][1],0);
            KeyHandler input=new KeyHandler();press(input,KeyEvent.VK_J);
            for(int tick=0;tick<40;tick++)player.update(1.0/60,input.poll(),w,List.of(enemy));
            check(enemy.health()==2,"A directional spear attack must hit exactly once");
        }
        Player player=new Player(World.SPAWN_X,World.SPAWN_Y);
        check(!player.useHerb()&&player.inventory().count(Inventory.Item.HERB)==3,"Full health consumed an herb");
        player.damage(1,0,0);
        check(player.useHerb()&&player.health()==player.maxHealth()&&player.inventory().count(Inventory.Item.HERB)==2,"Healing failed");
        Fx.clear();
    }
    private static void testSprites() throws Exception {
        Assets.load();
        BufferedImage[][][] animations = {Assets.idle, Assets.walk, Assets.run, Assets.attack, Assets.hurt};
        for (BufferedImage[][] animation : animations) {
            check(animation.length == 4, "Animation must support four directions");
            for (BufferedImage[] sequence : animation) {
                check(sequence.length > 0, "Empty animation");
                for (BufferedImage image : sequence) {
                    check(image.getWidth() == 64 && image.getHeight() == 64, "Inconsistent sprite canvas");
                    check(image.getColorModel().hasAlpha() && (image.getRGB(0, 0) >>> 24) == 0, "Sprite lost transparency");
                    int opaque = 0;
                    for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++)
                        if ((image.getRGB(x, y) >>> 24) > 128) opaque++;
                    check(opaque > 200, "Blank or badly cropped sprite");
                }
            }
            for (int frame = 0; frame < animation[3].length; frame++) {
                BufferedImage left = animation[2][frame], right = animation[3][frame];
                boolean equal = true;
                for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++)
                    if (left.getRGB(x, y) != right.getRGB(63 - x, y)) equal = false;
                check(equal, "Left-facing frame is not a mirror of the supplied right-facing frame");
            }
        }
        check(Assets.idle[0].length == 8 && Assets.walk[3].length == 8
                && Assets.run[3].length == 8 && Assets.attack[3].length == 8, "New eight-frame strips are not active");
        check(Assets.die.length == 4 && Assets.die[3].getWidth() == 96, "Wide death pose is missing");
        Player.State[] states = {Player.State.IDLE, Player.State.WALK, Player.State.RUN,
                Player.State.ATTACK, Player.State.HURT, Player.State.DEAD};
        BufferedImage gallery = new BufferedImage(1200, 680, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = gallery.createGraphics();
        try {
            g.setColor(new java.awt.Color(0x263A35)); g.fillRect(0, 0, 1200, 680);
            g.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));
            g.setColor(new java.awt.Color(0xF1C979));
            for (int col = 0; col < states.length; col++) g.drawString(states[col].name(), col * 200 + 55, 20);
            for (int row = 0; row < 4; row++) for (int col = 0; col < states.length; col++) {
                Graphics2D cell = (Graphics2D) g.create();
                try {
                    cell.translate(col * 200, row * 160 + 25); cell.scale(2, 2);
                    Player actor = new Player(50, 70);
                    actor.dir = Player.Dir.values()[row]; actor.state = states[col];
                    actor.stateTime = 0.21; actor.animTime = 0.21;
                    actor.draw(cell, 0, 0, 1);
                } finally {cell.dispose();}
            }
        } finally {g.dispose();}
        check(ImageIO.write(gallery, "png", new File("build/preview-sprite-states.png")), "Sprite-state gallery failed");
    }
    @SuppressWarnings("unchecked")
    private static void testSession()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);
        render(panel,"build/preview-title.png");
        tap(panel,KeyEvent.VK_ENTER);
        check(field(panel,"mode")==GamePanel.Mode.PLAYING,"Enter did not start");
        Player player=(Player)field(panel,"player");
        QuestLog log=(QuestLog)field(panel,"quests");
        List<Enemy> enemies=(List<Enemy>)field(panel,"enemies");
        render(panel,"build/preview-game.png");
        player.state=Player.State.ATTACK;player.dir=Player.Dir.UP;player.stateTime=0.18;
        render(panel,"build/preview-combat-up-in-game.png");
        player.dir=Player.Dir.DOWN;player.stateTime=0.20;
        render(panel,"build/preview-combat-down-in-game.png");
        player.state=Player.State.IDLE;player.dir=Player.Dir.DOWN;player.stateTime=0;
        testPanelMouse(panel);
        testPanelCombos(panel);
        tap(panel,KeyEvent.VK_M);render(panel,"build/preview-map.png");
        double pausedTime=(double)field(panel,"time");panel.update(1.0);
        check((double)field(panel,"time")==pausedTime,"Map failed to pause simulation");
        tap(panel,KeyEvent.VK_M);
        tap(panel,KeyEvent.VK_TAB);render(panel,"build/preview-journal.png");tap(panel,KeyEvent.VK_TAB);
        tap(panel,KeyEvent.VK_ESCAPE);
        check(field(panel,"mode")==GamePanel.Mode.PAUSED,"Pause failed");
        tap(panel,KeyEvent.VK_ENTER);
        for(int group=0;group<3;group++){
            for(Enemy enemy:enemies)if(enemy.shrineGroup==group)enemy.damage(100,0,0);
            Fx.clear();panel.update(1.0/60);
            int[] site=World.LANDMARKS[group+1];player.x=(site[0]+0.5)*World.T;player.y=(site[1]+0.5)*World.T;
            tap(panel,KeyEvent.VK_E);
            check(log.seals()==group+1,"Shrine interaction failed");
            int seals=player.inventory().count(Inventory.Item.SEAL);
            tap(panel,KeyEvent.VK_E);
            check(player.inventory().count(Inventory.Item.SEAL)==seals,"Repeated shrine duplicated seal");
        }
        Enemy guardian=null;
        for(Enemy enemy:enemies)if(enemy instanceof Guardian)guardian=enemy;
        check(guardian!=null,"Guardian was not spawned");
        guardian.damage(100,0,0);Fx.clear();panel.update(1.0/60);
        check(log.done("guardian"),"Guardian event not processed");
        player.x=World.SPAWN_X;player.y=World.SPAWN_Y;tap(panel,KeyEvent.VK_E);
        check(field(panel,"mode")==GamePanel.Mode.VICTORY&&log.done("home"),"Return home did not win");
        render(panel,"build/preview-victory.png");
        tap(panel,KeyEvent.VK_R);
        check(field(panel,"mode")==GamePanel.Mode.PLAYING,"Victory restart failed");
        player=(Player)field(panel,"player");
        player.damage(100,0,0);Fx.clear();panel.update(1.0/60);
        check(field(panel,"mode")==GamePanel.Mode.DEFEAT,"Death transition failed");
        tap(panel,KeyEvent.VK_R);
        check(field(panel,"mode")==GamePanel.Mode.PLAYING&&((QuestLog)field(panel,"quests")).seals()==0,"Death restart did not reset quest");
        panel.start();panel.start();panel.stop();
    }
    private static void testPanelMouse(GamePanel panel) throws Exception {
        Player actor=(Player)field(panel,"player");
        Fx.clear();
        mousePanel(panel,java.awt.event.MouseEvent.BUTTON3,true);
        panel.update(1.0/60);
        check(actor.state==Player.State.GUARD,"Real panel did not route right mouse to guard");
        for(Player.Dir dir:Player.Dir.values()){
            actor.dir=dir;actor.stateTime=0.20;
            render(panel,"build/preview-guard-"+dir.name().toLowerCase(java.util.Locale.ROOT)+".png");
        }
        for(java.awt.event.FocusListener listener:panel.getFocusListeners())
            listener.focusLost(new java.awt.event.FocusEvent(panel,java.awt.event.FocusEvent.FOCUS_LOST));
        check(field(panel,"mode")==GamePanel.Mode.PAUSED&&!panel.keys.poll().guard,"Focus loss left guard held");
        tap(panel,KeyEvent.VK_ENTER);panel.update(1.0/60);
        check(actor.state!=Player.State.GUARD,"Guard stuck after focus returned");
        Fx.hitStop=0.04;
        mousePanel(panel,java.awt.event.MouseEvent.BUTTON1,true);
        panel.update(1.0/60);
        mousePanel(panel,java.awt.event.MouseEvent.BUTTON1,false);
        for(int tick=0;tick<4;tick++)panel.update(1.0/60);
        check(actor.state==Player.State.ATTACK,"Panel lost left click during hit-stop");
        for(int tick=0;tick<40;tick++){Fx.clear();panel.update(1.0/60);}
        check(actor.state==Player.State.IDLE,"One panel mouse click repeated forever");
    }
    private static void testPanelCombos(GamePanel panel)throws Exception{
        Player actor=(Player)field(panel,"player");
        for(int i=0;i<25;i++)panel.update(1.0/60);
        for(int wanted=1;wanted<=SpearAttack.STAGES;wanted++){
            if(wanted==2)Fx.hitStop=.035;
            mousePanel(panel,java.awt.event.MouseEvent.BUTTON1,true);panel.update(1.0/60);
            mousePanel(panel,java.awt.event.MouseEvent.BUTTON1,false);
            for(int tick=0;tick<90&&(actor.state!=Player.State.ATTACK||actor.comboStep()!=wanted);tick++)
                panel.update(1.0/60);
            check(actor.state==Player.State.ATTACK&&actor.comboStep()==wanted,"Real mouse chain lost stage "+wanted);
            if(wanted>=5){
                while(actor.state==Player.State.ATTACK&&actor.stateTime<.24)panel.update(1.0/60);
                render(panel,wanted==5?"build/preview-combo-spin.png":"build/preview-combo-finisher.png");
            }
        }
        for(int tick=0;tick<100;tick++)panel.update(1.0/60);
        check(actor.state==Player.State.IDLE&&actor.comboStep()==0,"Real mouse combo did not stop");
    }

    private static void mousePanel(GamePanel panel,int button,boolean down){
        panel.dispatchEvent(new java.awt.event.MouseEvent(panel,
                down?java.awt.event.MouseEvent.MOUSE_PRESSED:java.awt.event.MouseEvent.MOUSE_RELEASED,
                0,0,GamePanel.W/2,GamePanel.H/2,1,false,button));
    }
    private static Object field(Object target,String name)throws Exception{
        Field field=target.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(target);
    }
    private static void render(GamePanel panel,String path)throws Exception{
        BufferedImage image=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();try{panel.paint(g);}finally{g.dispose();}
        check(ImageIO.write(image,"png",new File(path)),"Could not render preview");
    }
    private static void tap(GamePanel panel,int code){
        Fx.clear();press(panel.keys,code);panel.update(1.0/60);release(panel.keys,code);
    }
    private static void press(KeyHandler keys,int code){keys.keyPressed(new KeyEvent(SOURCE,KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));}
    private static void release(KeyHandler keys,int code){keys.keyReleased(new KeyEvent(SOURCE,KeyEvent.KEY_RELEASED,0,0,code,KeyEvent.CHAR_UNDEFINED));}
    private static void expect(Class<? extends RuntimeException> type,Runnable action){
        try{action.run();}catch(RuntimeException e){check(type.isInstance(e),"Wrong exception type");return;}
        throw new AssertionError("Missing "+type.getSimpleName());
    }
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
}
