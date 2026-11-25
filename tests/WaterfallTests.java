/* FILE PURPOSE: REGRESSION TEST | Registered waterfall art, receiving pond, hydraulic continuity, shore collision and source provenance.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public final class WaterfallTests {
    private static int checks;
    // [BLOCK: STATIC TEST] WaterfallTests.WaterfallTests - Prevent instances of the suite.
    private WaterfallTests(){}
    // [BLOCK: TEST ENTRY] WaterfallTests.main - Run the registered waterfall checks independently.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Waterfall checks passed: "+run());}
    // [BLOCK: REGRESSION SUITE] WaterfallTests.run - Check untouched sources, cached phases and geometry shared with the displayed cove.
    static int run()throws Exception{
        checks=0;Set<Long> coves=new HashSet<>(),falls=new HashSet<>();
        for(int i=0;i<WaterfallArt.FRAMES;i++){
            BufferedImage cove=WaterfallArt.cove(i);coves.add(hash(cove));falls.add(hash(WaterfallArt.upper(i)));
            check(cove.getWidth()==512&&cove.getHeight()==384&&cove==WaterfallArt.cove(i),"Cove cache changed dimensions or allocated");
            check(WaterfallArt.water(i)==WaterfallArt.water(i)&&WaterfallArt.fall(i)==WaterfallArt.fall(i),"Water caches allocate on lookup");
        }
        check(coves.size()>=2&&falls.size()>=2,"Source water does not animate");
        for(String line:Files.readAllLines(Path.of("res/waterfall/manifest.csv")).subList(1,4)){
            String[] fields=line.split(",");String digest=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(Path.of(fields[0]))));
            check(digest.equals(fields[1]),"Waterfall source was altered after import");
        }
        World world=new World(7);Point source=world.mountain.source();boolean[][] visited=new boolean[world.rows][world.cols];
        ArrayDeque<Point> queue=new ArrayDeque<>();queue.add(source);visited[source.y][source.x]=true;
        while(!queue.isEmpty()){Point p=queue.removeFirst();for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){
            int x=p.x+d[0],y=p.y+d[1];if(world.inside(x,y)&&!visited[y][x]&&wet(world.tile[y][x])){visited[y][x]=true;queue.addLast(new Point(x,y));}
        }}
        int pond=0;
        for(int r=82;r<=88;r++)for(int c=168;c<=184;c++){
            if(world.tile[r][c]==World.WATER){pond++;check(visited[r][c],"An isolated pond tile is disconnected from the cascade");
                check(world.solidTile(c,r)&&!world.walkable(c,r),"Water surface lets Agyu walk through it");}
            check(world.tile[r][c]!=World.BRIDGE,"A jetty still crosses the receiving pond");
        }
        check(pond>=35,"Receiving pond is too small");
        check(visited[94][0]&&visited[World.villageCell(33)][World.villageCell(32)],"Pond outlet is disconnected from the world river");
        ForestTests.openGates(world);
        check(!Pathfinder.find(world,World.col(World.SPAWN_X),World.row(World.SPAWN_Y),178,88).isEmpty(),"Dry viewing shore is unreachable");
        check(!world.blocked(new Rectangle2D.Double(178.5*32-5,88.5*32-6,10,6)),"Beach embeds player in a cliff");
        for(World.Tree tree:world.trees)check(!tree.trunk.intersects(world.mountain.coveBounds()),"A forest root occupies the painted cove");
        ImageIO.write(WaterfallArt.cove(0),"png",new File("build/preview-waterfall-native.png"));
        List<Drawable> draw=new ArrayList<>();world.mountain.collect(draw,MountainRiver.COVE_X,MountainRiver.COVE_Y+350,480,100);
        check(draw.contains(world.mountain),"The new lower shoreline is culled");
        return checks;
    }
    // [BLOCK: HYDRAULIC GRAPH] WaterfallTests.wet - Include protected culverts and river bridges in hydraulic connectivity.
    private static boolean wet(int tile){return tile==World.WATER||tile==World.CULVERT||tile==World.BRIDGE;}
    // [BLOCK: PIXEL FINGERPRINT] WaterfallTests.hash - Compare cached RGBA phases without changing their pixels.
    private static long hash(BufferedImage image){long hash=1;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)hash=31*hash+image.getRGB(x,y);return hash;}
    // [BLOCK: ASSERTION] WaterfallTests.check - Count and explain each regression property.
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
}
