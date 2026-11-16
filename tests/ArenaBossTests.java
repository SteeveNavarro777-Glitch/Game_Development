/* FILE PURPOSE: REGRESSION TEST | Authored boss sprites, three assigned encounters, telegraphed combat, defenses and one-time progression.
 * Course examples: polymorphic arrays, state transitions and cached rendering; docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.imageio.ImageIO;

/** Exercises real boss updates and GamePanel events rather than replacing attack or reward logic. */
public final class ArenaBossTests {
    private static final double DT=1.0/60;
    private static int checks;
    private static World world;
    private static final KeyHandler.Input NONE=new KeyHandler.Input(false,false,false,false,false,false,0);
    // [BLOCK: TEST UTILITY] ArenaBossTests.ArenaBossTests - Prevent construction of this focused regression suite.
    private ArenaBossTests(){}
    // [BLOCK: TEST ENTRY] ArenaBossTests.main - Load the current runtime sprites and run the boss suite independently.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Arena boss checks passed: "+run());}
    // [BLOCK: REGRESSION SUITE] ArenaBossTests.run - Verify assets, encounter assignment, attacks, defenses, lifecycle and actual camera rendering.
    static int run()throws Exception{
        checks=0;world=new World(7);assets();population();spear();tank();mage();fighter();lifecycle();session();previews();Fx.clear();return checks;
    }
    // [BLOCK: CACHED AUTHORED ART] ArenaBossTests.assets - Check every supplied directional animation is nonempty, transparent and stable on lookup.
    private static void assets(){
        for(BossArt.Kind kind:BossArt.Kind.values())for(BossArt.Anim animation:BossArt.Anim.values())for(Player.Dir direction:Player.Dir.values()){
            int count=BossArt.count(kind,animation);check(count>=3&&count<=6,"Invalid authored boss animation length");
            for(int i=0;i<count;i++){
                BufferedImage frame=BossArt.frame(kind,animation,direction,i);int opaque=0,transparent=0;
                check(frame==BossArt.frame(kind,animation,direction,i),"Boss frame lookup creates a new image");
                check(frame.getWidth()==BossArt.WIDTH&&frame.getHeight()==BossArt.HEIGHT&&frame.getColorModel().hasAlpha(),"Boss canvas/alpha is invalid");
                for(int y=0;y<frame.getHeight();y++)for(int x=0;x<frame.getWidth();x++)if((frame.getRGB(x,y)>>>24)>64)opaque++;else transparent++;
                check(opaque>200&&transparent>1000,"Packed boss pose is blank or has an opaque scene background");
            }
        }
        for(BossArt.Kind kind:BossArt.Kind.values()){
            java.util.Set<Long> faces=new java.util.HashSet<>();
            for(Player.Dir direction:Player.Dir.values())faces.add(hash(BossArt.frame(kind,BossArt.Anim.IDLE,direction,0)));
            check(faces.size()==4,"A supplied boss facing was duplicated or omitted");
        }
    }
    // [BLOCK: POLYMORPHIC POPULATION] ArenaBossTests.population - Verify exact Tank/Mage/Fighter mapping and outdoor, collision-free placement.
    private static void population(){
        List<Enemy> enemies=new ArrayList<>();new ForestEncounters(world).populate(enemies);
        check(enemies.size()==21,"The initial population does not contain the ordinary enemies and three bosses");
        check(enemies.stream().filter(e->e instanceof ArenaBoss).count()==3,"Missing or duplicate arena boss");
        Class<?>[] expected={TankBoss.class,MageBoss.class,FighterBoss.class};String[] keys={"tank","mage","fighter"};
        for(int id=1;id<=3;id++){
            final int arenaId=id;ArenaBoss boss=(ArenaBoss)enemies.stream().filter(e->e instanceof ArenaBoss b&&b.arenaId()==arenaId).findFirst().orElseThrow();
            ForestRegion.Arena arena=world.forest.arenas().get(id-1);
            check(expected[id-1].isInstance(boss)&&boss.questId().equals(keys[id-1]),"Wrong villain in clearing "+id);
            check(boss.x==(arena.c()+.5)*World.T&&boss.y==(arena.r()+.5)*World.T,"Boss does not start at its clearing center");
            check(world.forest.contains(boss.x,boss.y)&&!world.blocked(boss.feetBox()),"Boss spawn is indoors or embedded in scenery");
            check(boss.shrineGroup==-2,"Boss is classified as a shrine guard or ambient respawn slot");
            Player far=new Player(World.SPAWN_X,World.SPAWN_Y);boss.update(2,far,world,null);
            check(!boss.engaged()&&boss.state()==ArenaBoss.State.DORMANT,"A distant boss attacks while Agyu is inside the village");
        }
    }
    // [BLOCK: REAL WEAPON INTEGRATION] ArenaBossTests.spear - Drive mouse attacks through Player and the swept spear against every villain hurt box.
    private static void spear(){
        ArenaBoss[] bosses={new TankBoss(world.forest.arenas().get(0)),new MageBoss(world.forest.arenas().get(1)),new FighterBoss(world.forest.arenas().get(2))};
        java.awt.Canvas source=new java.awt.Canvas();
        for(ArenaBoss boss:bosses){
            Player player=new Player(boss.x-42,boss.y);player.dir=Player.Dir.RIGHT;KeyHandler keys=new KeyHandler();
            keys.mousePressed(new java.awt.event.MouseEvent(source,java.awt.event.MouseEvent.MOUSE_PRESSED,0,0,0,0,1,false,java.awt.event.MouseEvent.BUTTON1));
            int health=boss.health();for(int i=0;i<50;i++){player.savePrev();player.update(DT,keys.poll(),world,List.of(boss));Fx.clear();}
            check(boss.health()<health&&boss.alive(),"Left mouse spear does not damage "+boss.questId());
            check(player.comboStep()>0||player.state!=Player.State.ATTACK,"Boss spear hit leaves an invalid combo state");
        }
    }
    // [BLOCK: TANK COUNTERPLAY] ArenaBossTests.tank - Exercise a warned cleave, frontal shield/dash protection and a jumpable radial slam.
    private static void tank(){
        TankBoss boss=new TankBoss(world.forest.arenas().get(0));Player victim=near(boss,60);
        until(boss,victim,b->b.state()==ArenaBoss.State.WINDUP,300);int hp=victim.health();
        ticks(boss,victim,20);check(victim.health()==hp&&boss.state()==ArenaBoss.State.WINDUP,"Tank damages during its warning");
        until(boss,victim,b->victim.health()<hp,180);check(victim.health()==hp-2,"Tank cleave has no actual heavy impact");
        boss=new TankBoss(world.forest.arenas().get(0));Player guard=near(boss,60);guard.state=Player.State.GUARD;guard.dir=Player.Dir.LEFT;
        until(boss,guard,b->b.state()==ArenaBoss.State.RECOVER,300);check(guard.health()==guard.maxHealth(),"Frontal shield does not stop a tank cleave");
        until(boss,guard,b->b.attackNumber()==2&&b.state()==ArenaBoss.State.WINDUP,300);
        until(boss,guard,b->b.state()==ArenaBoss.State.STRIKE,180);
        guard.state=Player.State.IDLE;guard.update(DT,new KeyHandler.Input(false,false,false,false,false,false,KeyHandler.JUMP),world,List.of());
        check(guard.airborne(),"Slam counterplay fixture did not perform a real jump");
        until(boss,guard,b->b.state()==ArenaBoss.State.RECOVER,120);check(guard.health()==guard.maxHealth(),"Ground slam damages an airborne player");
        boss=new TankBoss(world.forest.arenas().get(0));Player dash=near(boss,60);
        until(boss,dash,b->b.state()==ArenaBoss.State.STRIKE,300);dash.state=Player.State.DASH;
        until(boss,dash,b->b.state()==ArenaBoss.State.RECOVER,120);check(dash.health()==dash.maxHealth(),"Tank attacks bypass dash defense");
    }
    // [BLOCK: MAGE HAZARDS] ArenaBossTests.mage - Verify delayed real projectiles, solid-terrain blocking, bounded hazards and disengagement cleanup.
    private static void mage(){
        MageBoss boss=new MageBoss(world.forest.arenas().get(1));Player victim=near(boss,170);
        until(boss,victim,b->b.state()==ArenaBoss.State.WINDUP,300);ticks(boss,victim,25);
        check(victim.health()==victim.maxHealth()&&boss.hazardCount()==0,"Mage fires without its cast warning");
        until(boss,victim,b->b.hazardCount()>0,180);check(boss.hazardCount()<=18,"Mage exceeded its hazard cap");
        until(boss,victim,b->victim.health()<victim.maxHealth(),240);check(victim.health()==victim.maxHealth()-1,"Mage projectiles do not damage the target");
        boss=new MageBoss(world.forest.arenas().get(1));Player protectedPlayer=near(boss,170);
        int column=World.col(boss.x+70),center=World.row(boss.y);int[] saved=new int[5];
        try{
            for(int i=-2;i<=2;i++){saved[i+2]=world.tile[center+i][column];world.tile[center+i][column]=World.WALL;}
            until(boss,protectedPlayer,b->b.hazardCount()>0,300);ticks(boss,protectedPlayer,130);
            check(protectedPlayer.health()==protectedPlayer.maxHealth(),"Mage bolts pass through solid terrain");
        }finally{for(int i=-2;i<=2;i++)world.tile[center+i][column]=saved[i+2];}
        boss=new MageBoss(world.forest.arenas().get(1));Player safe=near(boss,170);safe.invincible=1000;
        for(int i=0;i<1800;i++){step(boss,safe);if(i%60==0)check(boss.hazardCount()<=18,"Spell population grows without a bound");}
        boss.damage(3,boss.x-20,boss.y);int hp=boss.health();safe.x=World.SPAWN_X;safe.y=World.SPAWN_Y;step(boss,safe);
        check(!boss.engaged()&&boss.hazardCount()==0,"Leaving the mage arena preserves active spells");
        ticks(boss,safe,240);check(boss.health()==hp&&boss.state()==ArenaBoss.State.DORMANT,"Disengaging duplicates health or fails to settle the boss");
    }
    // [BLOCK: FIGHTER SWEPT COLLISION] ArenaBossTests.fighter - Verify fast sword damage and a locked flame charge stopped by a thin wall.
    private static void fighter(){
        FighterBoss boss=new FighterBoss(world.forest.arenas().get(2));Player victim=near(boss,60);
        until(boss,victim,b->b.state()==ArenaBoss.State.WINDUP,300);ticks(boss,victim,15);
        check(victim.health()==victim.maxHealth(),"Fighter cuts before its sword warning");
        until(boss,victim,b->victim.health()<victim.maxHealth(),180);check(victim.health()==victim.maxHealth()-1,"Fighter sword combo has no real hit");
        boss=new FighterBoss(world.forest.arenas().get(2));Player guard=near(boss,60);guard.state=Player.State.GUARD;guard.dir=Player.Dir.LEFT;
        until(boss,guard,b->b.attackNumber()==2&&b.state()==ArenaBoss.State.WINDUP,500);
        int column=World.col(boss.x+70),row=World.row(boss.y-4),saved=world.tile[row][column];
        try{
            world.tile[row][column]=World.WALL;
            until(boss,guard,b->b.state()==ArenaBoss.State.RECOVER,180);
            check(!world.blocked(boss.feetBox())&&boss.feetBox().getMaxX()<=column*World.T+.001,"Fighter charge tunnels through a thin wall");
        }finally{world.tile[row][column]=saved;}
        Player far=new Player(World.SPAWN_X,World.SPAWN_Y);ticks(boss,far,240);
        ForestRegion.Arena arena=world.forest.arenas().get(2);
        check(!boss.engaged()&&Math.hypot(boss.x-(arena.c()+.5)*World.T,boss.y-(arena.r()+.5)*World.T)<2,"Fighter escapes its clearing or fails to return home");
    }
    // [BLOCK: DEATH / READ-ONLY RENDER] ArenaBossTests.lifecycle - Verify exact-once claims, complete death timing, and graphics/cache state preservation.
    private static void lifecycle(){
        ArenaBoss[] bosses={new TankBoss(world.forest.arenas().get(0)),new MageBoss(world.forest.arenas().get(1)),new FighterBoss(world.forest.arenas().get(2))};
        for(ArenaBoss boss:bosses){
            Player player=near(boss,60);until(boss,player,b->b.state()==ArenaBoss.State.WINDUP,300);
            double phase=boss.phaseTime();long before=bodyHash(boss);check(bodyHash(boss)==before&&boss.phaseTime()==phase,"Drawing advances a boss attack");
            boss.damage(10000,boss.x-10,boss.y);check(!boss.alive()&&boss.state()==ArenaBoss.State.DEAD&&!boss.removable()&&boss.hazardCount()==0,"Boss death interrupts incorrectly");
            check(boss.claimDefeat()&&!boss.claimDefeat(),"Boss defeat reward can be claimed twice");
            boss.update(.5,player,world,null);check(!boss.removable(),"Boss death disappears before its authored poses finish");
            boss.update(1,player,world,null);check(boss.removable()&&boss.health()==0,"Boss death never completes");
            boss.damage(Integer.MAX_VALUE,0,0);check(boss.health()==0,"Boss corpse health underflows");
        }
    }
    // [BLOCK: SESSION PROGRESSION] ArenaBossTests.session - Exercise paused attacks, one-time boss rewards, live map state and fresh-session reset.
    @SuppressWarnings("unchecked")
    private static void session()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);tap(panel,KeyEvent.VK_ENTER);
        List<Enemy> enemies=(List<Enemy>)get(panel,"enemies");Player player=(Player)get(panel,"player");QuestLog quests=(QuestLog)get(panel,"quests");
        ArenaBoss boss=(ArenaBoss)enemies.stream().filter(e->e instanceof TankBoss).findFirst().orElseThrow();
        player.x=player.px=boss.x+70;player.y=player.py=boss.y;player.invincible=1000;panel.update(DT);
        double phase=boss.phaseTime();tap(panel,KeyEvent.VK_M);panel.update(2);check(boss.phaseTime()==phase,"Map advances boss attacks");tap(panel,KeyEvent.VK_M);
        tap(panel,KeyEvent.VK_ESCAPE);phase=boss.phaseTime();panel.update(2);check(boss.phaseTime()==phase,"Pause advances boss attacks");tap(panel,KeyEvent.VK_ENTER);
        VillageMap map=new VillageMap((World)get(panel,"world"));Object cache=get(map,"terrain");long before=mapHash(map,player,quests);
        int herbs=player.inventory().count(Inventory.Item.HERB);boss.damage(10000,boss.x-20,boss.y);Fx.clear();panel.update(DT);
        check(quests.done("tank")&&player.inventory().count(Inventory.Item.HERB)==herbs+2,"Tank defeat event did not record its quest and herb reward");
        check(before!=mapHash(map,player,quests)&&cache==get(map,"terrain"),"Boss map marker is stale or rebuilds static terrain");
        herbs=player.inventory().count(Inventory.Item.HERB);boss.damage(10000,boss.x,boss.y);Fx.clear();panel.update(DT);
        check(player.inventory().count(Inventory.Item.HERB)==herbs,"Repeated boss defeat duplicated its reward");
        List<Enemy> ordinary=enemies.stream().filter(e->e instanceof Slime&&e.alive()).limit(2).toList();
        for(int i=0;i<ordinary.size();i++){
            ordinary.get(i).damage(10000,0,0);Fx.clear();panel.update(DT);
            check(player.inventory().count(Inventory.Item.HERB)==herbs+(i==0?0:1),"Boss defeat changes the two-ordinary-enemy herb reward cadence");
        }
        player.damage(10000,0,0);player.invincible=0;player.damage(10000,0,0);Fx.clear();panel.update(DT);tap(panel,KeyEvent.VK_R);
        enemies=(List<Enemy>)get(panel,"enemies");quests=(QuestLog)get(panel,"quests");
        check(enemies.stream().filter(e->e instanceof ArenaBoss&&e.alive()).count()==3&&!quests.done("tank"),"Restart retains defeated boss state");
    }
    // [BLOCK: ACTUAL CAMERA EVIDENCE] ArenaBossTests.previews - Render all three boss encounters through the real game camera and HUD.
    @SuppressWarnings("unchecked")
    private static void previews()throws Exception{
        BufferedImage gallery=new BufferedImage(1440,390,BufferedImage.TYPE_INT_RGB);Graphics2D out=gallery.createGraphics();
        try{
            out.setColor(new Color(0x23382A));out.fillRect(0,0,gallery.getWidth(),gallery.getHeight());
            for(int id=1;id<=3;id++){
                GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);tap(panel,KeyEvent.VK_ENTER);
                List<Enemy> enemies=(List<Enemy>)get(panel,"enemies");final int arenaId=id;
                ArenaBoss boss=(ArenaBoss)enemies.stream().filter(e->e instanceof ArenaBoss b&&b.arenaId()==arenaId).findFirst().orElseThrow();
                Player player=(Player)get(panel,"player");player.x=player.px=boss.x+90;player.y=player.py=boss.y+45;player.invincible=1000;
                for(int i=0;i<400&&boss.state()!=ArenaBoss.State.WINDUP;i++){Fx.clear();panel.update(DT);}
                check(boss.engaged()&&boss.state()==ArenaBoss.State.WINDUP,"Encounter preview never reaches its real warning phase");
                set(panel,"camX",boss.x-240);set(panel,"prevCamX",boss.x-240);set(panel,"camY",boss.y-205);set(panel,"prevCamY",boss.y-205);set(panel,"alpha",1);
                player.invincible=0;
                BufferedImage frame=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);Graphics2D g=frame.createGraphics();try{panel.paint(g);}finally{g.dispose();}
                check(ImageIO.write(frame,"png",new File("build/preview-boss-"+boss.questId()+".png")),"Boss camera export failed");
                out.setColor(new Color(0xF1C979));out.drawString(id+" / "+boss.bossName(),(id-1)*480+10,18);out.drawImage(frame,(id-1)*480,30,480,360,null);
            }
        }finally{out.dispose();}
        check(ImageIO.write(gallery,"png",new File("build/preview-boss-encounters.png")),"Boss encounter gallery export failed");
    }
    // [BLOCK: RENDER FINGERPRINT] ArenaBossTests.bodyHash - Render warnings/body while verifying the incoming graphics state is restored.
    private static long bodyHash(ArenaBoss boss){
        BufferedImage image=new BufferedImage(360,300,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{g.translate(2,3);g.setColor(Color.MAGENTA);g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.6f));AffineTransform before=g.getTransform();
            boss.drawGround(g,(int)boss.x-180,(int)boss.y-150,1);
            boss.draw(g,(int)boss.x-180,(int)boss.y-150,1);
            check(g.getTransform().equals(before)&&g.getColor().equals(Color.MAGENTA)&&g.getComposite().equals(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.6f)),"Boss drawing leaks graphics state");
        }finally{g.dispose();}return hash(image);
    }
    // [BLOCK: LIVE MAP FINGERPRINT] ArenaBossTests.mapHash - Compare changing boss markers without mutating the cached map layer.
    private static long mapHash(VillageMap map,Player player,QuestLog quests){BufferedImage image=new BufferedImage(320,240,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();try{map.draw(g,player,quests,0,0,320,240,1,true);}finally{g.dispose();}return hash(image);}
    // [BLOCK: PIXEL FINGERPRINT] ArenaBossTests.hash - Compare a whole rendered image without inflating assertion counts by pixel.
    private static long hash(BufferedImage image){long result=1;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)result=result*31+image.getRGB(x,y);return result;}
    // [BLOCK: COMBAT FIXTURE] ArenaBossTests.near - Place a fresh player on dry central arena ground in front of a fresh boss.
    private static Player near(ArenaBoss boss,double distance){return new Player(boss.x+distance,boss.y);}
    // [BLOCK: SIMULATION FIXTURE] ArenaBossTests.step - Advance the actual boss state machine once while keeping transient effects isolated.
    private static void step(ArenaBoss boss,Player player){boss.savePrev();boss.update(DT,player,world,null);Fx.clear();}
    // [BLOCK: SIMULATION FIXTURE] ArenaBossTests.ticks - Run a fixed number of real boss updates.
    private static void ticks(ArenaBoss boss,Player player,int ticks){for(int i=0;i<ticks;i++)step(boss,player);}
    // [BLOCK: STATE WAIT] ArenaBossTests.until - Advance until a public boss/health condition holds or report a finite timeout.
    private static void until(ArenaBoss boss,Player player,Predicate<ArenaBoss> condition,int limit){for(int i=0;i<limit&&!condition.test(boss);i++)step(boss,player);check(condition.test(boss),"Boss condition timed out: "+boss.questId()+" "+boss.state()+" attack "+boss.attackNumber());}
    // [BLOCK: INPUT FIXTURE] ArenaBossTests.tap - Route one fresh key press through GamePanel's normal input path.
    private static void tap(GamePanel panel,int code){panel.keys.keyPressed(new KeyEvent(panel,KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));panel.update(DT);panel.keys.clear();}
    // [BLOCK: TEST REFLECTION] ArenaBossTests.get - Inspect private session state without changing the runtime API.
    private static Object get(Object object,String name)throws Exception{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);}
    // [BLOCK: CAMERA FIXTURE] ArenaBossTests.set - Position the real interpolation camera for deterministic visual review.
    private static void set(Object object,String name,double value)throws Exception{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);field.setDouble(object,value);}
    // [BLOCK: ASSERTION] ArenaBossTests.check - Count a meaningful regression property and explain any failure.
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
}
