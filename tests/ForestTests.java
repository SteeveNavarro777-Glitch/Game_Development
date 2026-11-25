/* FILE PURPOSE: REGRESSION TEST | Dense exterior forest, safe outdoor spawn slots, route access and hidden combo HUD.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

/** Real cached geometry, encounter timers, session input and actual renderer evidence. */
public final class ForestTests {
    private static int checks;
    // [BLOCK: TEST UTILITY] ForestTests.ForestTests - Prevent instances of this regression suite.
    private ForestTests(){}
    // [BLOCK: TEST ENTRY] ForestTests.main - Load real artwork and run the focused forest checks.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Forest checks passed: "+run());}
    // [BLOCK: REGRESSION SUITE] ForestTests.run - Verify deterministic forest construction, navigation, encounters and actual HUD rendering.
    static int run()throws Exception{checks=0;landscape();encounters();session();previews();Fx.clear();return checks;}
    // [BLOCK: GATE FIXTURE] ForestTests.openGates - Open all four actual gates using normal safe World interaction.
    static void openGates(World world){
        for(Palisade.Gate gate:world.palisade.gates())if(!gate.open()){
            Rectangle2D b=gate.closed;
            Player actor=switch(gate.side){case NORTH->new Player(b.getCenterX(),b.getMaxY()+32);case SOUTH->new Player(b.getCenterX(),b.getMinY()-32);
                case WEST->new Player(b.getMaxX()+32,b.getCenterY()+3);case EAST->new Player(b.getMinX()-32,b.getCenterY()+3);};
            if(!world.interactGate(actor,List.of(),List.of())||!gate.open())throw new AssertionError("Gate fixture did not open");
        }
    }
    // [BLOCK: WORLD / ROUTING] ForestTests.landscape - Test forest density, stable seeded geometry, clear trails and protected entrances.
    private static void landscape(){
        World world=new World(7),same=new World(7);
        check(world.cols==192&&world.rows==160,"Forest did not expand the playable world");
        check(world.forest.treeCount()>=180,"Exterior forest is too sparse");
        check(world.trees.size()==same.trees.size()&&Arrays.deepEquals(world.tile,same.tile),"Same seed changed forest generation");
        for(int i=0;i<world.trees.size();i++){
            World.Tree a=world.trees.get(i),b=same.trees.get(i);
            check(a.x==b.x&&a.y==b.y&&a.style==b.style,"Tree geometry changed for the same seed");
            if(world.forest.contains(a.x,a.y)){
                check(world.blocked(a.trunk)&&world.blockedBruteForce(a.trunk),"Forest root is passable or absent from its index");
                for(int r=World.row(a.trunk.getMinY());r<=World.row(a.trunk.getMaxY());r++)
                    for(int c=World.col(a.trunk.getMinX());c<=World.col(a.trunk.getMaxX());c++)check(!world.forest.trailAt(c,r),"Forest root overlaps a trail");
            }
        }
        int sc=World.col(World.SPAWN_X),sr=World.row(World.SPAWN_Y);
        for(int i=1;i<World.TRAIL_SITES.length;i++){
            int[] site=World.TRAIL_SITES[i];check(world.forest.contains((site[0]+.5)*World.T,(site[1]+.5)*World.T),"Combat shrine remains indoors");
            check(Pathfinder.find(world,sc,sr,site[0],site[1]).isEmpty(),"Closed wooden perimeter leaks into the forest");
        }
        openGates(world);
        for(int i=1;i<World.TRAIL_SITES.length;i++){
            int[] site=World.TRAIL_SITES[i];List<Point> route=Pathfinder.find(world,sc,sr,site[0],site[1]);
            check(!route.isEmpty(),"A forest combat clearing has no route from the village");
            int pc=sc,pr=sr;
            for(Point cell:route){
                for(int sample=0;sample<=8;sample++){
                    double x=(pc+.5+(cell.x-pc)*sample/8.0)*World.T,y=(pr+.5+(cell.y-pr)*sample/8.0)*World.T;
                    check(!world.blocked(new Rectangle2D.Double(x-10,y-10,20,10)),"Forest route crosses a solid root or wall");
                }pc=cell.x;pr=cell.y;
            }
        }
        for(int camp=0;camp<4;camp++){
            Point p=world.forest.camp(camp);check(!Pathfinder.find(world,sc,sr,p.x,p.y).isEmpty(),"Forest camp is isolated");
            p.x=0;check(world.forest.camp(camp).x!=0,"Camp query exposed a mutable spawn point");
        }
    }
    // [BLOCK: SPAWN LIFECYCLE] ForestTests.encounters - Exercise outdoor-only placement, delayed offscreen replenishment and bounded mission-safe slots.
    private static void encounters(){
        World world=new World(7);ForestEncounters population=new ForestEncounters(world,7);List<Enemy> enemies=new ArrayList<>();population.populate(enemies);int capacity=population.capacity();
        check(enemies.size()==capacity+3,"Random boars and villains have inconsistent capacity");
        for(Enemy enemy:enemies)check(world.forest.contains(enemy.x,enemy.y)&&!world.inVillage(enemy.x,enemy.y)&&!world.blocked(enemy.feetBox())&&world.walkable(World.col(enemy.x),World.row(enemy.y)),"Enemy spawned indoors or inside scenery");
        check(boars(enemies)==capacity,"Random herd membership is incorrect");
        Enemy killed=enemies.stream().filter(e->e instanceof WildBoar).findFirst().orElseThrow();double x=killed.x,y=killed.y;
        killed.damage(100,0,0);enemies.removeIf(e->!e.alive());Fx.clear();
        Player home=new Player(World.SPAWN_X,World.SPAWN_Y);population.update(24,home,enemies);check(boars(enemies)==capacity-1,"Boar respawned before its delay");
        population.update(2,home,enemies);check(boars(enemies)==capacity-1,"Village player triggered a forest respawn");
        Player near=new Player(x,y);population.update(1,near,enemies);check(boars(enemies)==capacity-1,"Enemy appeared next to Agyu");
        Player far=ForestRevisionTests.observer(world,population.herds());population.update(1,far,enemies);check(boars(enemies)==capacity,"Offscreen herd slot did not replenish");
        for(Enemy enemy:enemies)enemy.damage(100,0,0);enemies.clear();Fx.clear();population.update(ForestEncounters.RESPAWN_SECONDS,far,enemies);
        check(enemies.size()==capacity&&boars(enemies)==capacity,"Villains respawned or herd slots exceeded their bound");
        for(int i=0;i<50;i++)population.update(60,far,enemies);check(enemies.size()==capacity,"Repeated spawn updates accumulate living enemies");
    }
    // [BLOCK: SESSION INPUT] ForestTests.session - Verify pause/map timer freezing, fresh-session reset and the removed attack card.
    @SuppressWarnings("unchecked")
    private static void session()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);TitleMenuTests.startGame(panel);
        List<Enemy> enemies=(List<Enemy>)get(panel,"enemies");Player actor=(Player)get(panel,"player");
        ForestEncounters population=(ForestEncounters)get(panel,"forestEncounters");
        Enemy ambient=enemies.stream().filter(e->e instanceof WildBoar).findFirst().orElseThrow();ambient.damage(100,0,0);Fx.clear();panel.update(.1);
        double[] before=((double[])get(population,"cooldowns")).clone();
        tap(panel,KeyEvent.VK_M);panel.update(10);check(Arrays.equals(before,(double[])get(population,"cooldowns")),"Map advanced forest respawn timers");tap(panel,KeyEvent.VK_M);
        tap(panel,KeyEvent.VK_ESCAPE);before=((double[])get(population,"cooldowns")).clone();panel.update(10);
        check(Arrays.equals(before,(double[])get(population,"cooldowns")),"Pause advanced forest respawn timers");tap(panel,KeyEvent.VK_ENTER);
        tap(panel,KeyEvent.VK_J);check(actor.comboStep()>0,"Removing the combo panel disabled attacks");
        BufferedImage hud=new BufferedImage(GamePanel.VIEW_W,GamePanel.VIEW_H,BufferedImage.TYPE_INT_ARGB);Graphics2D g=hud.createGraphics();
        try{Method draw=GamePanel.class.getDeclaredMethod("drawHud",Graphics2D.class);draw.setAccessible(true);draw.invoke(panel,g);}finally{g.dispose();}
        boolean emptyAttackArea=true;for(int y=112;y<160;y++)for(int x=294;x<470;x++)if((hud.getRGB(x,y)>>>24)!=0)emptyAttackArea=false;
        check(emptyAttackArea,"Removed combo card still covers the attacking scene");
        actor.damage(100,0,0);Fx.clear();panel.update(.1);tap(panel,KeyEvent.VK_R);
        enemies=(List<Enemy>)get(panel,"enemies");check(enemies.size()==((ForestEncounters)get(panel,"forestEncounters")).capacity()+3&&enemies.stream().allMatch(Enemy::alive),"Restart retained old encounter slots");
        population=(ForestEncounters)get(panel,"forestEncounters");for(double timer:(double[])get(population,"cooldowns"))check(timer==0,"Restart retained a forest respawn cooldown");
        tap(panel,KeyEvent.VK_N);check(!((List<?>)get(panel,"path")).isEmpty(),"Closed gates hide the outbound objective trail");
        actor=(Player)get(panel,"player");int[] guardian=World.TRAIL_SITES[4];
        actor.x=actor.px=(guardian[0]+.5)*World.T;actor.y=actor.py=(guardian[1]+.5)*World.T;
        enemies.clear();set(panel,"pathTimer",0);panel.update(1.0/60);
        check(!((List<?>)get(panel,"path")).isEmpty(),"The forest route to a remaining villain is missing");
        World world=(World)get(panel,"world");BufferedImage cache=(BufferedImage)get(new VillageMap(world),"terrain");
        int mapX=(int)Math.round((guardian[0]+.5)/world.cols*256),mapY=(int)Math.round((guardian[1]+.5)/world.rows*192);
        check((cache.getRGB(mapX,mapY)&0xFFFFFF)==0xAD925F,"Minimap terrain does not include the southeast forest clearing");

    }
    // [BLOCK: RENDER EVIDENCE] ForestTests.previews - Export the expanded world and an actual forest combat camera without the combo panel.
    @SuppressWarnings("unchecked")
    private static void previews()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);TitleMenuTests.startGame(panel);
        World world=(World)get(panel,"world");Player actor=(Player)get(panel,"player");List<Enemy> enemies=(List<Enemy>)get(panel,"enemies");
        BufferedImage overview=new BufferedImage(world.cols*World.T/4,world.rows*World.T/4,BufferedImage.TYPE_INT_RGB);Graphics2D g=overview.createGraphics();g.scale(.25,.25);
        try{
            world.drawGround(g,0,0,world.cols*World.T,world.rows*World.T,0);List<Drawable> scene=new ArrayList<>();
            scene.addAll(world.trees);scene.addAll(world.buildings);scene.addAll(world.props);scene.addAll(enemies);scene.addAll(NpcPopulation.create(world));scene.add(actor);
            world.collectLandscape(scene,0,0,world.cols*World.T,world.rows*World.T);scene.sort(Comparator.comparingDouble(Drawable::sortY));
            for(Drawable item:scene)item.draw(g,0,0,1);
        }finally{g.dispose();}
        check(ImageIO.write(overview,"png",new File("build/preview-forest-overview.png")),"Forest overview export failed");
        Point camp=world.forest.camp(0);actor.x=actor.px=(camp.x+.5)*World.T;actor.y=actor.py=(camp.y+.5)*World.T;actor.dir=Player.Dir.RIGHT;
        tap(panel,KeyEvent.VK_J);double cx=actor.x-GamePanel.VIEW_W/2.0,cy=actor.y-GamePanel.VIEW_H/2.0;
        set(panel,"camX",cx);set(panel,"prevCamX",cx);set(panel,"camY",cy);set(panel,"prevCamY",cy);set(panel,"alpha",1);
        BufferedImage frame=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);g=frame.createGraphics();try{panel.paint(g);}finally{g.dispose();}
        check(ImageIO.write(frame,"png",new File("build/preview-forest-combat.png")),"Forest combat camera export failed");
    }
    // [BLOCK: TEST QUERY] ForestTests.alive - Count only living members of one spawn group.
    private static long boars(List<Enemy> enemies){return enemies.stream().filter(e->e.alive()&&e instanceof WildBoar).count();}
    // [BLOCK: INPUT FIXTURE] ForestTests.tap - Send one fresh keyboard edge through the actual panel input path.
    private static void tap(GamePanel panel,int code){panel.keys.keyPressed(new KeyEvent(panel,KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));panel.update(1.0/60);panel.keys.clear();}
    // [BLOCK: TEST REFLECTION] ForestTests.get - Inspect private session/cache state without changing the implementation API.
    private static Object get(Object target,String name)throws Exception{Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);}
    // [BLOCK: RENDER FIXTURE] ForestTests.set - Position the real interpolated camera for deterministic scene exports.
    private static void set(Object target,String name,double value)throws Exception{Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.setDouble(target,value);}
    // [BLOCK: ASSERTION] ForestTests.check - Count one regression assertion and report its concrete failure.
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
}
