/* FILE PURPOSE: WILDLIFE REGRESSION | Verify rare night wolf budgets, dawn departure and bounded harmless Sigbin scares.
 * Course examples: real clock transitions, quota invariants, safe spawning and read-only rendering; docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import javax.imageio.ImageIO;

/** No real three-minute waits and no test-only spawn shortcuts are added to production. */
public final class WildlifeTests {
    private static final double DT=1d/60;
    private static int checks;
    // [BLOCK: TEST UTILITY] WildlifeTests.WildlifeTests - Prevent construction of this focused test suite.
    private WildlifeTests(){}
    // [BLOCK: TEST ENTRY] WildlifeTests.main - Load real sprites and run the wildlife checks.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Wildlife checks passed: "+run());}
    // [BLOCK: REGRESSION SUITE] WildlifeTests.run - Exercise source identity, real night quotas, pounces, scares, cooldowns and paused sessions.
    static int run()throws Exception{checks=0;sourceAssets();quotas();World world=new World(7);world.dayNight.update(DayNightCycle.PHASE_SECONDS);wolfCombat(world);sigbin(world);ambushDirector(world);session();previews();return checks;}
    // [BLOCK: SUPPLIED ASSET IDENTITY] WildlifeTests.sourceAssets - Validate every original source hash and all action/direction lookups from the packed runtime atlases.
    private static void sourceAssets()throws Exception{
        check(WildlifeArt.scarePortrait().getWidth()==320&&WildlifeArt.scarePortrait()==WildlifeArt.scarePortrait(),"Scare close-up is not detailed and cached");
        List<String> lines=Files.readAllLines(Path.of("res/wildlife/frames.csv"));check(lines.size()==68,"Not all 67 supplied wildlife poses are packed");
        for(String line:lines.subList(1,lines.size())){String[] part=line.split(",");Path input=Path.of("art/source",part[0],part[10]);
            String actual=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(input)));
            check(actual.equals(part[14]),"A source pose differs from its recorded provenance: "+input);}
        for(WildlifeArt.Kind kind:WildlifeArt.Kind.values())for(WildlifeArt.Anim action:WildlifeArt.Anim.values())for(Player.Dir dir:Player.Dir.values())for(int f=0;f<WildlifeArt.count(action);f++){
            BufferedImage pose=WildlifeArt.frame(kind,action,dir,f,false);int visible=0;
            for(int y=0;y<pose.getHeight();y++)for(int x=0;x<pose.getWidth();x++)if((pose.getRGB(x,y)>>>24)>32)visible++;
            check(pose==WildlifeArt.frame(kind,action,dir,f,false)&&visible>150,"A wildlife frame is blank or allocated during lookup");
            check((pose.getRGB(0,0)>>>24)==0&&(pose.getRGB(79,79)>>>24)==0,"Source background leaked into wildlife art");
        }
    }
    // [BLOCK: PER-NIGHT TOTAL QUOTA] WildlifeTests.quotas - Kill spawned wolves, continue the night and prove the total cannot exceed two or three before resetting next night.
    private static void quotas(){
        World world=new World(7);NightWildlife director=new NightWildlife(world);Player player=camp(world);List<Enemy> enemies=new ArrayList<>();
        director.update(200,player,enemies);check(enemies.isEmpty(),"A rare predator spawned during the day");world.dayNight.update(DayNightCycle.PHASE_SECONDS);
        for(int night=1;night<=6;night++){
            director.update(DT,player,enemies);int allowance=director.wolfLimit();check(allowance>=2&&allowance<=3&&director.wolvesSpawned()==0,"New night did not reset a bounded quota");
            world.dayNight.update(11);director.update(11,player,enemies);check(director.wolvesSpawned()==0,"Wolves spawn immediately at nightfall");
            for(int spawn=0;spawn<allowance;spawn++){
                double target=12+spawn*60;world.dayNight.update(target-world.dayNight.phaseSeconds());director.update(DT,player,enemies);
                check(director.wolvesSpawned()==spawn+1,"Scheduled night wolf failed to spawn");
                Enemy newest=enemies.get(enemies.size()-1);check(newest instanceof Wolf&&newest.nightEmpowered()&&world.forest.contains(newest.x,newest.y)&&!world.blocked(newest.feetBox()),"Wolf spawn is indoor, blocked or not night-powered");
                check(Math.hypot(player.x-newest.x,player.y-newest.y)>350,"Wolf appeared on screen beside Agyu");
                if(spawn==0){newest.damage(100,player.x,player.y);check(newest.claimDefeat()&&!newest.claimDefeat(),"Rare wolf defeat reward can repeat");newest.update(1.1,player,world,null);enemies.removeIf(Enemy::removable);Fx.clear();}
            }
            world.dayNight.update(DayNightCycle.PHASE_SECONDS-1-world.dayNight.phaseSeconds());for(int i=0;i<20;i++)director.update(5,player,enemies);
            check(director.wolvesSpawned()==allowance&&enemies.stream().filter(e->e instanceof Wolf).count()==allowance-1,"Defeated wolves replenish beyond the nightly total");
            world.dayNight.update(1);int health=player.health();for(Enemy enemy:enemies)enemy.update(1.21,player,world,null);
            check(enemies.stream().allMatch(Enemy::removable)&&player.health()==health,"Wolves attack or remain indefinitely after dawn");
            for(Enemy enemy:enemies)check(!enemy.claimDefeat(),"A living dawn departure grants a kill reward");enemies.removeIf(Enemy::removable);
            director.update(100,player,enemies);check(enemies.isEmpty(),"Wolves respawn in daylight");world.dayNight.update(DayNightCycle.PHASE_SECONDS);
        }
    }
    // [BLOCK: REAL WOLF COMBAT] WildlifeTests.wolfCombat - Verify a night pounce, authored body lift, one empowered hit and frontal shield protection.
    private static void wolfCombat(World world){
        ForestRegion.Arena arena=world.forest.arenas().get(0);double x=(arena.c()+.5)*World.T,y=(arena.r()+.5)*World.T;
        Wolf wolf=new Wolf(x,y);Player player=new Player(x+85,y);waitFor(wolf,player,world,ForestBeast.State.WINDUP);
        for(int i=0;i<15;i++){wolf.update(DT,player,world,null);check(player.health()==5,"Wolf pounce hits during its warning");}
        waitFor(wolf,player,world,ForestBeast.State.CHARGE);wolf.update(.08,player,world,null);check(wolf.bodyLift()>5,"The supplied wolf leap has no physical body motion");
        for(int i=0;i<40;i++)wolf.update(DT,player,world,null);check(player.health()==3,"Night wolf pounce ignores its one-hit damage or night power");Fx.clear();
        wolf=new Wolf(x,y);player=new Player(x+85,y);player.dir=Player.Dir.LEFT;
        player.update(DT,new KeyHandler.Input(false,false,false,false,false,true,0),world,List.of());waitFor(wolf,player,world,ForestBeast.State.CHARGE);
        for(int i=0;i<50;i++)wolf.update(DT,player,world,null);check(player.health()==5&&wolf.x<player.x,"Wolf leap crosses Agyu's frontal shield");Fx.clear();
    }
    // [BLOCK: NIGHT ENEMY / REAL FIGHT] WildlifeTests.sigbin - Verify a night reveal, persistent hunting, real claw damage, spear interruption and a defeat through player attack input.
    private static void sigbin(World world)throws Exception{
        Player player=camp(world);Sigbin creature=new Sigbin(player.x+80,player.y);int[][] flow=Pathfinder.distances(world,World.col(player.x),World.row(player.y));
        for(int i=0;i<40;i++)creature.update(DT,player,world,flow);check(!creature.hasScared(),"Sigbin has no stalking glimpse before the reveal");
        for(int i=0;i<10;i++)creature.update(DT,player,world,flow);check(creature.hasScared()&&creature.scareActive()&&player.health()==5,"Reveal is missing or attacks before the reaction window");
        BufferedImage overlay=new BufferedImage(480,360,BufferedImage.TYPE_INT_ARGB);Graphics2D g=overlay.createGraphics();AffineTransform before=g.getTransform();var composite=g.getComposite();
        double x=creature.x,time=creature.t;try{creature.drawOverlay(g,480,360);creature.drawOverlay(g,480,360);check(g.getTransform().equals(before)&&g.getComposite().equals(composite),"Scare overlay corrupts parent graphics state");}finally{g.dispose();}
        check(creature.x==x&&creature.t==time&&creature.scareActive(),"Rendering advances the encounter");check((overlay.getRGB(240,200)>>>24)>0,"Scare image is invisible");
        creature.damage(1,player.x,player.y);check(!creature.scareActive()&&creature.health()==3,"Spear hit does not interrupt the overlay");Fx.clear();
        for(int i=0;i<300;i++){creature.update(DT,player,world,flow);Fx.clear();}
        check(!creature.removable()&&creature.alive()&&player.health()<5,"Sigbin retreats automatically instead of remaining a combat enemy");
        Player fighter=camp(world);Sigbin target=new Sigbin(fighter.x+34,fighter.y);fighter.dir=Player.Dir.RIGHT;
        int[][] fightFlow=Pathfinder.distances(world,World.col(fighter.x),World.row(fighter.y));
        for(int i=0;i<360&&target.alive()&&fighter.alive();i++){
            if(fighter.health()<=2)fighter.usePotion(Inventory.Item.HEALTH_POTION);int edge=i%24==0?KeyHandler.ATTACK:0;
            fighter.update(DT,new KeyHandler.Input(false,false,false,false,false,false,edge),world,List.of(target));target.update(DT,fighter,world,fightFlow);Fx.clear();
        }
        check(!target.alive()&&target.health()==0&&target.claimDefeat()&&!target.claimDefeat(),"Real spear input cannot defeat the Sigbin or duplicates its reward");
        target.update(.8,fighter,world,fightFlow);check(!target.removable(),"Sigbin death skips supplied frames");target.update(.3,fighter,world,fightFlow);check(target.removable(),"Sigbin corpse never cleans up");
        Sigbin escaped=new Sigbin(player.x+140,player.y);Player home=new Player(World.SPAWN_X,World.SPAWN_Y);
        for(int i=0;i<80;i++)escaped.update(DT,home,world,null);check(escaped.removable()&&!escaped.hasScared(),"Sigbin follows Agyu into the village");
        World day=new World(7);Sigbin daylight=new Sigbin(player.x+80,player.y);daylight.update(10,player,day,null);
        check(daylight.removable()&&!daylight.hasScared()&&!daylight.scareActive()&&!daylight.claimDefeat(),"A living Sigbin appears or grants a free kill in daylight");Fx.clear();
    }
    // [BLOCK: EXPLORATION-GATED AMBUSH] WildlifeTests.ambushDirector - Require actual movement in dense exterior woodland, then enforce one active creature and a lengthy cooldown.
    private static void ambushDirector(World world){
        NightWildlife director=new NightWildlife(world);Player player=camp(world);List<Enemy> enemies=new ArrayList<>();
        for(int i=0;i<90;i++)director.update(1,player,enemies);check(enemies.stream().noneMatch(e->e instanceof Sigbin),"An idle player receives repeated forest scares");
        double center=player.x;boolean right=true;
        for(int tick=0;tick<1200&&enemies.stream().noneMatch(e->e instanceof Sigbin);tick++){
            if(player.x>center+55)right=false;if(player.x<center-55)right=true;
            player.update(DT,new KeyHandler.Input(false,false,!right,right,false,false,0),world,List.of());director.update(DT,player,enemies);
        }
        check(enemies.stream().filter(e->e instanceof Sigbin).count()==1,"Exploring dense forest never produces a Sigbin appearance");
        for(int second=0;second<60;second++){
            player.x=center+(second%2==0?40:-40);director.update(1,player,enemies);
            for(Enemy enemy:enemies)if(enemy instanceof Sigbin)enemy.update(1,player,world,null);
        }
        check(enemies.stream().filter(e->e instanceof Sigbin).count()==1,"Sigbin scares ignore their cooldown or overlap");Fx.clear();
    }
    // [BLOCK: PAUSED SESSION / RESET] WildlifeTests.session - Confirm pause freezes the real director and a fresh journey clears its night budget.
    @SuppressWarnings("unchecked")
    private static void session()throws Exception{
        GamePanel panel=new GamePanel();TitleMenuTests.startGame(panel);World world=(World)get(panel,"world");world.dayNight.update(DayNightCycle.PHASE_SECONDS+20);panel.update(DT);
        ForestEncounters population=(ForestEncounters)get(panel,"forestEncounters");NightWildlife director=(NightWildlife)get(population,"wildlife");
        check(director.wolvesSpawned()==1,"Session never updates its night wildlife director");double time=world.dayNight.phaseSeconds();tap(panel,KeyEvent.VK_ESCAPE);panel.update(140);
        check(world.dayNight.phaseSeconds()==time&&director.wolvesSpawned()==1,"Pause advances night wildlife or the world clock");
        tap(panel,KeyEvent.VK_ESCAPE);((Player)get(panel,"player")).damage(100,0,0);Fx.clear();panel.update(DT);tap(panel,KeyEvent.VK_R);World restarted=(World)get(panel,"world");
        check(restarted!=world&&!restarted.dayNight.night()&&((List<Enemy>)get(panel,"enemies")).stream().noneMatch(e->e instanceof Wolf||e instanceof Sigbin),"Restart retains rare wildlife from the previous night");
    }
    // [BLOCK: RENDER EVIDENCE] WildlifeTests.previews - Export the actual night forest and a representative Sigbin reveal using normal GamePanel rendering.
    @SuppressWarnings("unchecked")
    private static void previews()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);TitleMenuTests.startGame(panel);World world=(World)get(panel,"world");world.dayNight.update(DayNightCycle.PHASE_SECONDS);
        Player player=(Player)get(panel,"player");Player center=camp(world);player.x=player.px=center.x-30;player.y=player.py=center.y;
        List<Enemy> enemies=(List<Enemy>)get(panel,"enemies");Wolf wolf=new Wolf(player.x+110,player.y-35);wolf.refreshNightPower(world);enemies.add(wolf);
        for(Enemy enemy:enemies)enemy.refreshNightPower(world);Sigbin creature=new Sigbin(player.x-110,player.y+35);enemies.add(creature);for(int i=0;i<50;i++)creature.update(DT,player,world,null);
        for(String name:new String[]{"camX","prevCamX"})set(panel,name,player.x-GamePanel.VIEW_W/2d);
        for(String name:new String[]{"camY","prevCamY"})set(panel,name,player.y-GamePanel.VIEW_H/2d);set(panel,"alpha",1d);
        image(panel,"build/preview-sigbin-scare.png");for(int i=0;i<35;i++)creature.update(DT,player,world,null);image(panel,"build/preview-night-wildlife.png");Fx.clear();
    }
    // [BLOCK: PREVIEW EXPORT] WildlifeTests.image - Paint the actual game surface to one review PNG.
    private static void image(GamePanel panel,String path)throws Exception{BufferedImage image=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();try{panel.paint(g);}finally{g.dispose();}check(ImageIO.write(image,"png",new File(path)),"Wildlife preview export failed");}
    // [BLOCK: CLEAR FOREST FIXTURE] WildlifeTests.camp - Place a real Player on the reserved northern camp floor.
    private static Player camp(World world){Point p=world.forest.camp(0);return new Player((p.x+.5)*World.T,(p.y+.5)*World.T);}
    // [BLOCK: COMBAT STATE WAIT] WildlifeTests.waitFor - Advance the real creature until a requested warning or pounce with a finite timeout.
    private static void waitFor(ForestBeast beast,Player player,World world,ForestBeast.State state){for(int i=0;i<180&&beast.state()!=state;i++)beast.update(DT,player,world,null);check(beast.state()==state,"Wolf never reached "+state);}
    // [BLOCK: REAL INPUT FIXTURE] WildlifeTests.tap - Deliver one keyboard edge through the actual session key handler.
    private static void tap(GamePanel panel,int key)throws Exception{KeyHandler input=(KeyHandler)get(panel,"keys");input.keyPressed(new KeyEvent(new Canvas(),KeyEvent.KEY_PRESSED,0,0,key,KeyEvent.CHAR_UNDEFINED));panel.update(DT);input.keyReleased(new KeyEvent(new Canvas(),KeyEvent.KEY_RELEASED,0,0,key,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: SESSION INSPECTION] WildlifeTests.get - Inspect existing private state without adding production test hooks.
    private static Object get(Object target,String name)throws Exception{Field field=target.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(target);}
    // [BLOCK: CAMERA FIXTURE] WildlifeTests.set - Set only the offscreen preview's camera interpolation values.
    private static void set(Object target,String name,double value)throws Exception{Field field=target.getClass().getDeclaredField(name);field.setAccessible(true);field.setDouble(target,value);}
    // [BLOCK: ASSERTION] WildlifeTests.check - Count a behavior contract and identify the exact failure.
    private static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
}
