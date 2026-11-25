/* FILE PURPOSE: REGRESSION TEST | Remote boss arenas, a continuous mountain river, solid culverts and animated waterfall scenery.
 * Course examples: BFS queue, route arrays and cached Drawable dispatch; docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

/** Geometry, real navigation, hydraulic continuity and cached scenery remain consistent after expansion. */
public final class ExpansionTests {
    private static int checks;
    private static final int[][] STEPS={{0,-1},{1,0},{0,1},{-1,0}};
    // [BLOCK: TEST UTILITY] ExpansionTests.ExpansionTests - Prevent instances of the focused regression suite.
    private ExpansionTests(){}
    // [BLOCK: TEST ENTRY] ExpansionTests.main - Load the actual artwork and run expansion checks independently.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Expansion checks passed: "+run());}
    // [BLOCK: REGRESSION SUITE] ExpansionTests.run - Verify remote arenas, the river and animation before exporting actual game evidence.
    static int run()throws Exception{checks=0;arenas();river();scenery();previews();Fx.clear();return checks;}
    // [BLOCK: BOSS ARENA SPACE] ExpansionTests.arenas - Check three large, distant and usable spaces connected to the village gates.
    private static void arenas(){
        World world=new World(7);ForestTests.openGates(world);
        List<ForestRegion.Arena> arenas=world.forest.arenas();check(arenas.size()==3,"Three boss clearings were not created");
        List<Enemy> population=new ArrayList<>();new ForestEncounters(world).populate(population);
        Set<Integer> ids=new HashSet<>();int sc=World.col(World.SPAWN_X),sr=World.row(World.SPAWN_Y);
        int[][] reach=Pathfinder.distances(world,sc,sr);
        Rectangle2D village=new Rectangle2D.Double((World.VILLAGE_MIN+1)*World.T,(World.VILLAGE_MIN+1)*World.T,
                (World.VILLAGE_COLS-1)*World.T,(World.VILLAGE_ROWS-1)*World.T);
        for(ForestRegion.Arena arena:arenas){
            check(ids.add(arena.id())&&arena.name()!=null&&!arena.name().isBlank(),"Arena identifiers are missing or repeated");
            check(arena.rx()>=8&&arena.ry()>=7,"An arena boss has insufficient arena space");
            Rectangle2D area=new Rectangle2D.Double((arena.c()-arena.rx())*World.T,(arena.r()-arena.ry())*World.T,
                    (arena.rx()*2+1)*World.T,(arena.ry()*2+1)*World.T);
            double dx=Math.max(0,Math.max(village.getMinX()-area.getMaxX(),area.getMinX()-village.getMaxX()));
            double dy=Math.max(0,Math.max(village.getMinY()-area.getMaxY(),area.getMinY()-village.getMaxY()));
            check(Math.hypot(dx,dy)>=480,"Boss clearing is too close to the village wall");
            check(reach[arena.r()][arena.c()]>20,"Boss clearing is not reachable on the trail network");
            List<Point> route=Pathfinder.find(world,sc,sr,arena.c(),arena.r());
            check(route.size()==reach[arena.r()][arena.c()],"Arena A* route is not a shortest connected route");
            int pc=sc,pr=sr;
            for(Point next:route){
                for(int sample=0;sample<=8;sample++){
                    double x=(pc+.5+(next.x-pc)*sample/8.0)*World.T,y=(pr+.5+(next.y-pr)*sample/8.0)*World.T;
                    check(!world.blocked(new Rectangle2D.Double(x-10,y-10,20,10)),"Arena approach crosses a root, river or cliff");
                }pc=next.x;pr=next.y;
            }
            int open=0;
            for(int r=arena.r()-arena.ry();r<=arena.r()+arena.ry();r++)for(int c=arena.c()-arena.rx();c<=arena.c()+arena.rx();c++){
                if(Math.pow((c-arena.c())/(arena.rx()+.0),2)+Math.pow((r-arena.r())/(arena.ry()+.0),2)>.65)continue;
                Rectangle2D feet=new Rectangle2D.Double((c+.5)*World.T-10,(r+.5)*World.T-10,20,10);
                check(world.walkable(c,r)&&!world.blocked(feet),"Boss arena interior is blocked");open++;
            }
            check(open>=100,"Boss arena lacks a broad usable central floor");
            check(population.stream().filter(enemy->enemy instanceof ArenaBoss boss&&boss.arenaId()==arena.id()&&area.contains(enemy.x,enemy.y)).count()==1,
                    "Arena has no unique assigned villain boss");
            check(population.stream().noneMatch(enemy->!(enemy instanceof ArenaBoss)&&area.contains(enemy.x,enemy.y)),"Ordinary enemy spawn occupies a boss arena");
        }
        check(population.stream().filter(enemy->enemy instanceof ArenaBoss).count()==3,"Three arena bosses were not populated");
        check(population.stream().allMatch(enemy->enemy instanceof WildBoar||enemy instanceof ArenaBoss),"Initial forest population includes an obsolete encounter");
    }
    // [BLOCK: HYDRAULIC BFS] ExpansionTests.river - Follow one connected watercourse through bridges and protected wall culverts to its outlet.
    private static void river(){
        World world=new World(7);Point source=world.mountain.source();
        check(source.x>world.cols*.8&&wet(world.tile[source.y][source.x]),"Waterfall source is not on the far-right mountain");
        boolean[][] reached=new boolean[world.rows][world.cols];ArrayDeque<Point> queue=new ArrayDeque<>();
        queue.add(source);reached[source.y][source.x]=true;
        while(!queue.isEmpty()){
            Point cell=queue.removeFirst();
            for(int[] step:STEPS){int c=cell.x+step[0],r=cell.y+step[1];
                if(world.inside(c,r)&&!reached[r][c]&&wet(world.tile[r][c])){reached[r][c]=true;queue.addLast(new Point(c,r));}
            }
        }
        check(reached[World.villageCell(33)][World.villageCell(32)],"Mountain river does not reach the existing village river");
        check(reached[94][0],"Extended river has no connected western outflow");
        int culverts=0,cliffs=0;
        for(int r=0;r<world.rows;r++)for(int c=0;c<world.cols;c++){
            int tile=world.tile[r][c];
            if(tile==World.CULVERT){
                culverts++;check(reached[r][c],"Wall culvert is disconnected from its watercourse");
                Rectangle2D foot=new Rectangle2D.Double((c+.5)*World.T-9,(r+.5)*World.T-8,18,8);
                check(world.blocked(foot)&&world.blockedBruteForce(foot)&&!world.walkable(c,r),"A culvert opens an unprotected walk-through wall gap");
            }
            if(tile==World.MOUNTAIN){cliffs++;check(world.solidTile(c,r)&&!world.walkable(c,r),"Mountain cliff is passable");}
        }
        check(culverts>=2,"River does not pass under both protected village walls");
        check(cliffs>=100,"Far-right mountain does not have a substantial solid footprint");
        for(Palisade.Gate gate:world.palisade.gates())check(!gate.open()&&world.blocked(gate.closed),"River extension broke a protective gate");
    }
    // [BLOCK: WATER CLASSIFICATION] ExpansionTests.wet - Include water passing below bridges and palisade culverts in the hydraulic graph.
    private static boolean wet(int kind){return kind==World.WATER||kind==World.BRIDGE||kind==World.CULVERT;}
    // [BLOCK: CACHE / RENDER] ExpansionTests.scenery - Check large cached mountain art, distinct water phases, culling and read-only drawing.
    private static void scenery(){
        World world=new World(7);BufferedImage mountain=MountainArt.mountain();
        check(mountain==MountainArt.mountain()&&mountain.getWidth()>=900&&mountain.getHeight()>=1100,"Mountain artwork is missing or rebuilt");
        Set<Long> frames=new HashSet<>();
        for(int phase=0;phase<4;phase++){
            BufferedImage fall=MountainArt.waterfall(phase),lower=WaterfallArt.fall(phase);
            check(fall==MountainArt.waterfall(phase)&&lower==WaterfallArt.fall(phase),"Waterfall images are rebuilt during lookup");
            frames.add(hash(fall));
        }
        check(frames.size()==4,"Waterfall phases do not visibly animate");
        Rectangle2D bounds=world.mountain.bounds();List<Drawable> visible=new ArrayList<>();
        world.collectLandscape(visible,(int)bounds.getX(),(int)bounds.getY(),(int)bounds.getWidth(),(int)bounds.getHeight());
        check(visible.contains(world.mountain),"Mountain is culled while visible");visible.clear();
        world.collectLandscape(visible,(int)World.SPAWN_X-240,(int)World.SPAWN_Y-180,480,360);
        check(!visible.contains(world.mountain),"Distant mountain enters the village draw list");
        int[][] before=Arrays.stream(world.tile).map(int[]::clone).toArray(int[][]::new);
        BufferedImage first=mountainFrame(world),repeat=mountainFrame(world);check(hash(first)==hash(repeat),"Drawing advances waterfall simulation");
        world.mountain.update(.21);BufferedImage next=mountainFrame(world);check(hash(first)!=hash(next),"Waterfall simulation does not advance the rendered stream");
        check(Arrays.deepEquals(before,world.tile),"Mountain rendering or animation modifies terrain");
        Graphics2D g=first.createGraphics();
        try{
            g.translate(2,3);g.clipRect(0,0,first.getWidth()-10,first.getHeight()-10);g.setColor(Color.MAGENTA);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.37f));AffineTransform transform=g.getTransform();Rectangle clip=g.getClipBounds();
            world.mountain.draw(g,0,0,.5);
            check(g.getTransform().equals(transform)&&g.getClipBounds().equals(clip)&&g.getColor().equals(Color.MAGENTA)
                    &&g.getComposite().equals(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.37f)),"Mountain drawing leaks graphics state");
        }finally{g.dispose();}
    }
    // [BLOCK: OFFSCREEN SCENERY] ExpansionTests.mountainFrame - Render the real mountain and waterfall into a compact local camera.
    private static BufferedImage mountainFrame(World world){
        Rectangle2D bounds=world.mountain.bounds();BufferedImage out=new BufferedImage(512,(int)Math.ceil(bounds.getHeight()/2),BufferedImage.TYPE_INT_ARGB);Graphics2D g=out.createGraphics();
        try{g.scale(.5,.5);world.mountain.draw(g,(int)bounds.getX(),(int)bounds.getY(),1);}finally{g.dispose();}return out;
    }
    // [BLOCK: RENDER EVIDENCE] ExpansionTests.previews - Export a complete mountain view and the actual waterfall-side game camera.
    @SuppressWarnings("unchecked")
    private static void previews()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);TitleMenuTests.startGame(panel);World world=(World)get(panel,"world");
        BufferedImage art=mountainFrame(world);check(ImageIO.write(art,"png",new File("build/preview-mountain-waterfall.png")),"Mountain/waterfall preview export failed");
        Point bank=new Point(178,88);
        ForestTests.openGates(world);
        List<Point> approach=Pathfinder.find(world,World.col(World.SPAWN_X),World.row(World.SPAWN_Y),bank.x,bank.y);
        check(!approach.isEmpty()&&world.walkable(bank.x,bank.y),"Waterfall beach is unreachable");
        check(world.tile[bank.y][bank.x]==World.DIRT||world.tile[bank.y][bank.x]==World.GRASS,"Waterfall approach does not end on dry shore");
        Player actor=(Player)get(panel,"player");actor.x=actor.px=(bank.x+.5)*World.T;actor.y=actor.py=(bank.y+.5)*World.T;
        check(!world.blocked(actor.feetBox()),"Viewing shore embeds Agyu in the cliff");
        check(world.mountain.sortY()<actor.sortY(),"Mountain can cover an actor on a valid shore");
        double look=world.mountain.lookAhead(actor.x,actor.y);
        check(look>=0&&look<=84&&world.mountain.lookAhead(World.SPAWN_X,World.SPAWN_Y)==0,"Scenic camera moves outside the waterfall area");
        double cx=actor.x-GamePanel.VIEW_W/2.0,cy=actor.y-20-GamePanel.VIEW_H/2.0-look;
        set(panel,"camX",cx);set(panel,"prevCamX",cx);set(panel,"camY",cy);set(panel,"prevCamY",cy);set(panel,"alpha",1);
        BufferedImage frame=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);Graphics2D g=frame.createGraphics();try{panel.paint(g);}finally{g.dispose();}
        int feet=(int)Math.round((actor.y-cy)*GamePanel.SCALE);check(feet<GamePanel.SCALE*(HotbarHud.top(GamePanel.VIEW_H)-18),"Scenic camera hides Agyu under the hotbar");
        int reds=0;for(int y=Math.max(0,feet-115);y<Math.min(frame.getHeight(),feet);y++)for(int x=frame.getWidth()/2-65;x<frame.getWidth()/2+65;x++){
            int pixel=frame.getRGB(x,y),red=(pixel>>>16)&255,green=(pixel>>>8)&255,blue=pixel&255;
            if(red>120&&red>green*1.6&&red>blue*1.6)reds++;
        }
        check(reds>30,"The mountain hides Agyu on the viewing shore");
        check(ImageIO.write(frame,"png",new File("build/preview-waterfall-camera.png")),"Actual waterfall camera export failed");
        long day=hash(frame);world.dayNight.update(DayNightCycle.PHASE_SECONDS+30);
        BufferedImage night=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);g=night.createGraphics();
        try{panel.paint(g);}finally{g.dispose();}
        check(hash(night)!=day,"Night lighting does not affect the registered waterfall scene");
        int nightReds=0;for(int y=Math.max(0,feet-115);y<Math.min(night.getHeight(),feet);y++)for(int x=night.getWidth()/2-65;x<night.getWidth()/2+65;x++){
            int pixel=night.getRGB(x,y),red=pixel>>>16&255,green=pixel>>>8&255,blue=pixel&255;
            if(red>120&&red>green*1.6&&red>blue*1.6)nightReds++;
        }
        check(nightReds>30,"Night scenery hides Agyu on the viewing shore");
        check(ImageIO.write(night,"png",new File("build/preview-waterfall-night.png")),"Night waterfall preview export failed");
    }
    // [BLOCK: PIXEL FINGERPRINT] ExpansionTests.hash - Compare rendered cache contents without counting every pixel as a separate assertion.
    private static long hash(BufferedImage image){long value=1;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)value=value*31+image.getRGB(x,y);return value;}
    // [BLOCK: INPUT FIXTURE] ExpansionTests.tap - Dispatch one real key edge into the game panel.
    private static void tap(GamePanel panel,int code){panel.keys.keyPressed(new KeyEvent(panel,KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));panel.update(1.0/60);panel.keys.clear();}
    // [BLOCK: TEST REFLECTION] ExpansionTests.get - Read session state without widening runtime interfaces.
    private static Object get(Object target,String name)throws Exception{Field field=target.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(target);}
    // [BLOCK: CAMERA FIXTURE] ExpansionTests.set - Position the real interpolated camera for review renders.
    private static void set(Object target,String name,double value)throws Exception{Field field=target.getClass().getDeclaredField(name);field.setAccessible(true);field.setDouble(target,value);}
    // [BLOCK: ASSERTION] ExpansionTests.check - Count one meaningful property and explain any failure.
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
}
