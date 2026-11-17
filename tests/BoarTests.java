/* FILE PURPOSE: BOAR REGRESSION | Authored frames, forest habitat, telegraphed charges, shield/collision and complete death lifecycle.
 * Course examples: deterministic simulation, array caches and polymorphic populations; docs/CODE_GUIDE.md.
 */
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

/** Tests use the actual sprite cache, collision index, enemy update and player defense contracts. */
public final class BoarTests {
    private static final double DT=1.0/60;
    private static int checks;
    // [BLOCK: TEST UTILITY] BoarTests.BoarTests - Prevent instances of the focused regression suite.
    private BoarTests(){}
    // [BLOCK: TEST ENTRY] BoarTests.main - Load real assets and run the focused forest-wildlife checks.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Boar checks passed: "+run());}
    // [BLOCK: REGRESSION SUITE] BoarTests.run - Verify sprite arrays, roaming, combat, death, population replacement and a real forest camera.
    static int run()throws Exception{checks=0;art();World world=new World(7);population(world);roaming(world);charges(world);defenses(world);collision(world);death(world);preview();return checks;}
    // [BLOCK: ASSET CONTRACT] BoarTests.art - Verify all supplied source poses, directional caches, opaque feet registration and untouched rendering inputs.
    private static void art()throws Exception{
        List<String> manifest=Files.readAllLines(Path.of("res/wildlife/frames.csv"));check(manifest.stream().filter(line->line.startsWith("boar,")).count()==21,"The atlas omits a supplied boar pose");
        try(var sources=Files.list(Path.of("art/source/boar"))){check(sources.filter(p->p.toString().endsWith(".png")).count()==21,"Original boar frames were not retained");}
        for(WildlifeArt.Anim animation:WildlifeArt.Anim.values())for(Player.Dir dir:Player.Dir.values())for(int i=0;i<WildlifeArt.count(animation);i++){
            BufferedImage frame=WildlifeArt.frame(WildlifeArt.Kind.BOAR,animation,dir,i,false),flash=WildlifeArt.frame(WildlifeArt.Kind.BOAR,animation,dir,i,true);
            check(frame==WildlifeArt.frame(WildlifeArt.Kind.BOAR,animation,dir,i,false),"Rendering allocates an uncached boar frame");
            check(frame.getWidth()==80&&frame.getHeight()==80&&frame.getColorModel().hasAlpha(),"Invalid boar pose dimensions/transparency");
            int visible=0,lowest=0;boolean alphaPreserved=true,whitePreserved=true;
            for(int y=0;y<80;y++)for(int x=0;x<80;x++){
                int rgba=frame.getRGB(x,y),white=flash.getRGB(x,y);if((rgba>>>24)>=200){visible++;lowest=Math.max(lowest,y);}
                alphaPreserved&=(rgba>>>24)==(white>>>24);
                if((white>>>24)>0)whitePreserved&=(white&0xFFFFFF)==0xFFFFFF;
            }
            check(alphaPreserved&&whitePreserved,"Flash silhouette changes alpha or its white fill");
            check(visible>150&&lowest>=70&&lowest<=74,"Boar body lost scale or ground registration: "+animation+"/"+dir+"/"+i+" bottom "+lowest);
        }
        for(int f=0;f<4;f++){
            BufferedImage left=WildlifeArt.frame(WildlifeArt.Kind.BOAR,WildlifeArt.Anim.WALK,Player.Dir.LEFT,f,false),right=WildlifeArt.frame(WildlifeArt.Kind.BOAR,WildlifeArt.Anim.WALK,Player.Dir.RIGHT,f,false);
            boolean mirrored=true;for(int y=0;y<80;y++)for(int x=0;x<80;x++)mirrored&=left.getRGB(x,y)==right.getRGB(79-x,y);
            check(mirrored,"Left boar art is not an exact mirror");
        }
        check(WildlifeArt.frame(WildlifeArt.Kind.BOAR,WildlifeArt.Anim.WALK,Player.Dir.DOWN,0,false)!=WildlifeArt.frame(WildlifeArt.Kind.BOAR,WildlifeArt.Anim.WALK,Player.Dir.UP,0,false),"Front and back walking reuse the same view");
    }
    // [BLOCK: POLYMORPHIC POPULATION] BoarTests.population - Confirm every ordinary initial and replenished enemy is an outdoor boar while all villains remain.
    private static void population(World world){
        ForestEncounters forest=new ForestEncounters(world,7);List<Enemy> enemies=new ArrayList<>();forest.populate(enemies);
        check(enemies.stream().filter(e->e instanceof WildBoar).count()==forest.capacity(),"The randomized boar herd capacity is incorrect");
        check(enemies.stream().filter(e->e instanceof ArenaBoss).count()==3,"Boar replacement removed a villain");
        for(Enemy enemy:enemies)check(world.forest.contains(enemy.x,enemy.y)&&!world.blocked(enemy.feetBox()),"Forest wildlife spawned indoors or in a solid");
        enemies.forEach(e->e.damage(10000,0,0));enemies.clear();Fx.clear();
        forest.update(26,ForestRevisionTests.observer(world,forest.herds()),enemies);
        check(enemies.size()==forest.capacity()&&enemies.stream().allMatch(e->e instanceof WildBoar),"Ambient respawn retained an old enemy type or revived mission guards");
    }
    // [BLOCK: LINKED-LIST ROAMING] BoarTests.roaming - Simulate a full minute of free forest patrol and verify movement, root clearance and habitat boundaries.
    private static void roaming(World world){
        Point camp=world.forest.camp(0);double x=(camp.x+.5)*World.T,y=(camp.y+.5)*World.T;
        WildBoar boar=new WildBoar(x,y,-1);Player indoors=new Player(World.SPAWN_X,World.SPAWN_Y);double furthest=0;
        for(int tick=0;tick<3600;tick++){boar.savePrev();boar.update(DT,indoors,world,null);furthest=Math.max(furthest,Math.hypot(boar.x-x,boar.y-y));
            check(!world.blocked(boar.feetBox())&&world.forest.contains(boar.x,boar.y),"A roaming boar entered a tree, water or the village");}
        check(furthest>30&&furthest<120,"Boar patrol is motionless or escapes its home clearing");
        WildBoar interrupted=boarAt(world,0);Player near=new Player(interrupted.x+72,interrupted.y);waitFor(interrupted,near,world,WildBoar.State.WINDUP,120);
        interrupted.update(DT,indoors,world,null);for(int i=0;i<80;i++)interrupted.update(DT,indoors,world,null);
        check(interrupted.state()==WildBoar.State.ROAM,"Entering the village permanently freezes a recovering boar");
    }
    // [BLOCK: COMMITTED CHARGE] BoarTests.charges - Check a readable windup, one charge hit, all four facing choices and spear interruption.
    private static void charges(World world){
        for(int direction=0;direction<4;direction++){
            double dx=direction==2?-1:direction==3?1:0,dy=direction==1?-1:direction==0?1:0;
            WildBoar boar=boarAt(world,0);Player player=new Player(boar.x+dx*78,boar.y+dy*78);
            waitFor(boar,player,world,WildBoar.State.WINDUP,120);check(boar.facing()==Player.Dir.values()[direction],"Boar attack has the wrong directional anatomy");
            double bx=boar.x,by=boar.y;
            for(int i=0;i<20;i++){boar.update(DT,player,world,null);check(player.health()==5,"Charge hit before its warning completed");}
            check(Math.hypot(boar.x-bx,boar.y-by)<.01,"Charge warning drifts toward the target");
            for(int i=0;i<65;i++)boar.update(DT,player,world,null);
            check(player.health()==4,"Committed boar charge did not deal exactly one daytime hit");Fx.clear();
        }
        WildBoar interrupted=boarAt(world,0);Player target=new Player(interrupted.x+75,interrupted.y);waitFor(interrupted,target,world,WildBoar.State.WINDUP,120);
        interrupted.damage(1,target.x,target.y);check(interrupted.state()==WildBoar.State.RECOVER&&interrupted.health()==2,"Spear impact does not interrupt a windup");Fx.clear();
    }
    // [BLOCK: PLAYER COUNTERPLAY] BoarTests.defenses - Use real guard and dash input and verify that a grounded charge can be jumped.
    private static void defenses(World world){
        WildBoar guardBoar=boarAt(world,0);Player guard=new Player(guardBoar.x+78,guardBoar.y);guard.dir=Player.Dir.LEFT;
        guard.update(DT,new KeyHandler.Input(false,false,false,false,false,true,0),world,List.of());
        check(guard.state==Player.State.GUARD,"Guard fixture did not hold the real shield");
        waitFor(guardBoar,guard,world,WildBoar.State.CHARGE,180);for(int i=0;i<30;i++)guardBoar.update(DT,guard,world,null);
        check(guard.health()==5&&guardBoar.x<guard.x,"A charge crossed through Agyu's frontal shield");Fx.clear();
        WildBoar jumping=boarAt(world,0);Player jumper=new Player(jumping.x+78,jumping.y);waitFor(jumping,jumper,world,WildBoar.State.CHARGE,180);
        jumper.update(DT,new KeyHandler.Input(false,false,false,false,false,false,KeyHandler.JUMP),world,List.of());
        check(jumper.airborne(),"Space/jump fixture never became airborne");for(int i=0;i<25;i++)jumping.update(DT,jumper,world,null);
        check(jumper.health()==5,"Airborne Agyu was hit by a grounded charge");Fx.clear();
        WildBoar dashing=boarAt(world,0);Player dasher=new Player(dashing.x+78,dashing.y);waitFor(dashing,dasher,world,WildBoar.State.CHARGE,180);
        dasher.update(DT,new KeyHandler.Input(false,false,false,false,false,false,KeyHandler.DASH),world,List.of());
        check(dasher.dashing(),"Dash fixture never entered its real state");for(int i=0;i<25;i++)dashing.update(DT,dasher,world,null);
        check(dasher.health()==5,"A charge ignores dash protection");Fx.clear();
    }
    // [BLOCK: SWEPT COLLISION] BoarTests.collision - Reject coarse charged travel through a real forest tree and forced travel into the village.
    private static void collision(World world)throws Exception{
        World.Tree tree=null;
        for(World.Tree candidate:world.trees){Rectangle2D root=candidate.trunk;
            if(world.forest.contains(root.getMinX()-35,root.getCenterY()+4)&&!world.blocked(new Rectangle2D.Double(root.getMinX()-44,root.getCenterY()-4,18,8))){tree=candidate;break;}}
        check(tree!=null,"No valid forest tree approach exists");Rectangle2D trunk=tree.trunk;
        WildBoar boar=new WildBoar(trunk.getMinX()-35,trunk.getCenterY()+4,-1);
        set(boar,"phase",WildBoar.State.CHARGE);set(boar,"aimX",1d);set(boar,"aimY",0d);
        boar.update(.32,new Player(trunk.getMaxX()+80,boar.y),world,null);
        check(!world.blocked(boar.feetBox())&&boar.feetBox().getMaxX()<=trunk.getMinX()+1e-6,"A coarse charge tunneled through solid tree roots");
        Palisade.Gate gate=world.palisade.gates().stream().filter(g->g.side==Palisade.Side.WEST).findFirst().orElseThrow();
        if(!gate.open())gate.toggle();double edge=(World.VILLAGE_MIN+1)*World.T;
        WildBoar border=new WildBoar(gate.closed.getMinX()-30,gate.closed.getCenterY()+4,-1);
        check(!world.blocked(border.feetBox()),"Open-gate fixture starts embedded in terrain");
        border.moveThroughWorld(300,0,world);check(border.x<=edge-9&&!world.blocked(border.feetBox()),"Wildlife entered the village through an open gate");
    }
    // [BLOCK: DEATH LIFECYCLE] BoarTests.death - Check one-time rewards and a non-looping five-frame corpse before cleanup.
    private static void death(World world){
        WildBoar boar=boarAt(world,0);Player far=new Player(World.SPAWN_X,World.SPAWN_Y);boar.damage(100,0,0);Fx.clear();
        check(boar.claimDefeat()&&!boar.claimDefeat(),"Boar defeat rewards can be claimed twice");
        for(int i=0;i<5;i++){boar.update(.16,far,world,null);check(!boar.removable(),"Boar disappears before all five death frames finish");}
        boar.update(.3,far,world,null);check(boar.removable()&&!boar.alive()&&boar.health()==0,"Finished boar corpse never cleans up");
    }
    // [BLOCK: FREE CLEARING FIXTURE] BoarTests.boarAt - Use the actual reserved arena floor for deterministic combat geometry.
    private static WildBoar boarAt(World world,int group){ForestRegion.Arena arena=world.forest.arenas().get(0);return new WildBoar((arena.c()+.5)*World.T,(arena.r()+.5)*World.T,group);}
    // [BLOCK: STATE WAIT FIXTURE] BoarTests.waitFor - Advance real simulation only until the requested warning/charge state, failing on a stalled encounter.
    private static void waitFor(WildBoar boar,Player player,World world,WildBoar.State state,int limit){for(int i=0;i<limit&&boar.state()!=state;i++)boar.update(DT,player,world,null);check(boar.state()==state,"Boar never reached "+state);}
    // [BLOCK: FOCUSED FIXTURE] BoarTests.set - Set one private phase/vector only for the coarse collision regression.
    private static void set(Object target,String name,Object value)throws Exception{Class<?> type=target.getClass();while(type!=null){try{Field field=type.getDeclaredField(name);field.setAccessible(true);field.set(target,value);return;}catch(NoSuchFieldException missing){type=type.getSuperclass();}}throw new NoSuchFieldException(name);}
    // [BLOCK: RENDER EVIDENCE] BoarTests.preview - Export a real forest camera with ground warnings, depth sorting, Agyu and the authored boars.
    @SuppressWarnings("unchecked")
    private static void preview()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);
        Field keys=GamePanel.class.getDeclaredField("keys");keys.setAccessible(true);KeyHandler input=(KeyHandler)keys.get(panel);
        input.keyPressed(new KeyEvent(new Canvas(),KeyEvent.KEY_PRESSED,0,0,KeyEvent.VK_ENTER,KeyEvent.CHAR_UNDEFINED));panel.update(DT);
        input.keyReleased(new KeyEvent(new Canvas(),KeyEvent.KEY_RELEASED,0,0,KeyEvent.VK_ENTER,KeyEvent.CHAR_UNDEFINED));
        Field worldField=GamePanel.class.getDeclaredField("world"),playerField=GamePanel.class.getDeclaredField("player"),enemiesField=GamePanel.class.getDeclaredField("enemies");
        worldField.setAccessible(true);playerField.setAccessible(true);enemiesField.setAccessible(true);
        World world=(World)worldField.get(panel);Player player=(Player)playerField.get(panel);List<Enemy> enemies=(List<Enemy>)enemiesField.get(panel);
        Field populationField=GamePanel.class.getDeclaredField("forestEncounters");populationField.setAccessible(true);
        ForestEncounters population=(ForestEncounters)populationField.get(panel);ForestEncounters.Herd herd=population.herds().stream().filter(h->h.count()>=3).findFirst().orElseThrow();
        player.x=player.px=(herd.c()+.5)*World.T;player.y=player.py=(herd.r()+.5)*World.T;player.dir=Player.Dir.DOWN;
        double cx=player.x-GamePanel.VIEW_W/2d,cy=player.y-GamePanel.VIEW_H/2d;
        for(String name:new String[]{"camX","prevCamX"}){Field f=GamePanel.class.getDeclaredField(name);f.setAccessible(true);f.setDouble(panel,cx);}
        for(String name:new String[]{"camY","prevCamY"}){Field f=GamePanel.class.getDeclaredField(name);f.setAccessible(true);f.setDouble(panel,cy);}
        Field alpha=GamePanel.class.getDeclaredField("alpha");alpha.setAccessible(true);alpha.setDouble(panel,1);
        BufferedImage frame=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);Graphics2D g=frame.createGraphics();
        try{panel.paint(g);}finally{g.dispose();}check(ImageIO.write(frame,"png",new File("build/preview-forest-boars.png")),"Forest boar preview export failed");Fx.clear();
    }
    // [BLOCK: ASSERTION] BoarTests.check - Count a behavior contract and report the exact failure.
    private static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
}
