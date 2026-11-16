/* FILE PURPOSE: REGRESSION TEST | Connected road surfaces, protected farm plots, open gates and deterministic scenery.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

/** Road-surface connectivity, protected crop lots and deterministic cached scenery. */
public final class VillageLayoutTests {
    private static int checks;
    private static final int[][] STEPS={{0,-1},{1,0},{0,1},{-1,0}};
    // [BLOCK: TEST HELPER] VillageLayoutTests.VillageLayoutTests - Prevent instances of this test/helper class.
    private VillageLayoutTests(){}
    // [BLOCK: TEST HELPER] VillageLayoutTests.main - Entry point for this regression suite.
    public static void main(String[] args)throws Exception{
        Assets.load();System.out.println("Village layout checks passed: "+run());
    }
    // [BLOCK: TEST HELPER] VillageLayoutTests.run - Handle run for VillageLayoutTests.
    static int run()throws Exception{
        checks=0;
        for(long seed:new long[]{7,19,2026}){
            World world=new World(seed);boolean[][] reached=flood(world);
            for(int r=0;r<world.rows;r++)for(int c=0;c<world.cols;c++)if(world.roadAt(c,r)){
                check(reached[r][c],"Disconnected road surface at "+c+","+r);
                check(world.tile[r][c]==World.PAVING||world.tile[r][c]==World.DIRT||world.tile[r][c]==World.BRIDGE,"Erased road surface");
                check(!world.blocked(new Rectangle2D.Double((c+.5)*World.T-10,(r+.5)*World.T-10,20,10)),"Road center blocked at "+c+","+r);
                if(world.tile[r][c]==World.BRIDGE)check(c==World.villageCell(14)||c==World.villageCell(29)||c==World.villageCell(52),"Unplanned river crossing");
                int mask=0;
                for(int i=0;i<4;i++){
                    int x=c+STEPS[i][0],y=r+STEPS[i][1];
                    if(world.inside(x,y)&&(world.tile[y][x]==World.GRASS||world.tile[y][x]==World.WALL))mask|=1<<i;
                }
                check(mask==world.terrainEdges[r][c],"Cached street edge mismatch");
            }
            for(World.Building house:world.buildings){
                int c=World.col(house.x),r=World.row(house.y);
                check(world.roadAt(c,r)&&reached[r][c],"Doorway has no connected approach");
            }
            for(int[] site:World.VILLAGE_SITES)check(reached[site[1]][site[0]],"Landmark missing from road network");
            farms(world,reached);
        }
        World a=new World(7),b=new World(7);
        check(Arrays.deepEquals(a.tile,b.tile)&&Arrays.deepEquals(a.variant,b.variant),"Same seed changed village terrain");
        check(a.trees.size()==b.trees.size()&&a.props.size()==b.props.size(),"Same seed changed scenery counts");
        for(int i=0;i<a.farms().size();i++)check(hash(a.farms().get(i).image)==hash(b.farms().get(i).image),"Same seed changed farm artwork");
        BufferedImage ground=new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);Graphics2D g=ground.createGraphics();
        int[][] before=Arrays.stream(a.tile).map(int[]::clone).toArray(int[][]::new);
        try{a.drawGround(g,0,256,256,256,0);a.drawGround(g,0,256,256,256,0);}finally{g.dispose();}
        check(Arrays.deepEquals(before,a.tile),"Rendering mutated village layout");
        previews(a);return checks;
    }
    // [BLOCK: TEST HELPER] VillageLayoutTests.flood - Handle flood for VillageLayoutTests.
    private static boolean[][] flood(World w){
        boolean[][] reached=new boolean[w.rows][w.cols];ArrayDeque<Integer> queue=new ArrayDeque<>();
        int c=World.col(World.SPAWN_X),r=World.row(World.SPAWN_Y);
        check(w.roadAt(c,r),"HOME is off the street network");reached[r][c]=true;queue.add(r*w.cols+c);
        while(!queue.isEmpty()){
            int cell=queue.remove();
            for(int[] step:STEPS){
                int x=cell%w.cols+step[0],y=cell/w.cols+step[1];
                if(w.roadAt(x,y)&&!reached[y][x]){reached[y][x]=true;queue.add(y*w.cols+x);}
            }
        }
        return reached;
    }
    // [BLOCK: TEST HELPER] VillageLayoutTests.farms - Handle farms for VillageLayoutTests.
    private static void farms(World w,boolean[][] reached){
        List<FarmPlot> plots=w.farms();check(plots.size()==8,"Missing planned farm plots");Set<Integer> kinds=new HashSet<>();
        for(int i=0;i<plots.size();i++){
            FarmPlot farm=plots.get(i);kinds.add(farm.kind);Rectangle2D box=farm.bounds;
            check(w.roadAt(farm.gateCol,farm.gateRow)&&reached[farm.gateRow][farm.gateCol],"Farm gate has no connected street");
            for(int r=World.row(box.getMinY());r<=World.row(Math.nextDown(box.getMaxY()));r++)
                for(int c=World.col(box.getMinX());c<=World.col(Math.nextDown(box.getMaxX()));c++)
                    check(w.tile[r][c]==World.GRASS&&!w.roadAt(c,r),"Crops cover a road or water");
            for(World.Building house:w.buildings)check(!box.intersects(house.footprint),"Farm overlaps a house");
            for(World.Prop prop:w.props)check(!box.intersects(prop.footprint),"Farm overlaps a craft station");
            for(int j=i+1;j<plots.size();j++)check(!box.intersects(plots.get(j).bounds),"Farm overlap");
            for(World.Tree tree:w.trees)check(!new Rectangle2D.Double(tree.x-80,tree.y-160,160,172).intersects(box),"Canopy hides crops");
            BufferedImage cached=farm.image;long pixels=hash(cached);
            check(cached.getWidth()==(int)box.getWidth()&&cached.getHeight()==(int)box.getHeight()&&cached.getColorModel().hasAlpha(),"Bad farm canvas");
            check((cached.getRGB(0,0)>>>24)==0,"Farm lost soft transparent border");
            BufferedImage out=new BufferedImage(cached.getWidth(),cached.getHeight(),BufferedImage.TYPE_INT_ARGB);Graphics2D g=out.createGraphics();
            try{farm.draw(g,(int)box.getX(),(int)box.getY());}finally{g.dispose();}
            check(hash(out)==pixels&&farm.image==cached&&hash(cached)==pixels,"Farm draw rebuilds or changes cached artwork");
        }
        check(kinds.size()==3,"Missing crop variety");
    }
    // [BLOCK: TEST HELPER] VillageLayoutTests.hash - Handle hash for VillageLayoutTests.
    private static long hash(BufferedImage image){
        long result=1;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)result=result*31+image.getRGB(x,y);return result;
    }
    // [BLOCK: TEST HELPER] VillageLayoutTests.previews - Handle previews for VillageLayoutTests.
    private static void previews(World world)throws Exception{
        BufferedImage overview=new BufferedImage(world.cols*World.T/4,world.rows*World.T/4,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=overview.createGraphics();g.scale(.25,.25);
        try{
            world.drawGround(g,0,0,world.cols*World.T,world.rows*World.T,0);
            List<Drawable> scene=new ArrayList<>();scene.addAll(world.trees);scene.addAll(world.buildings);scene.addAll(world.props);world.collectLandscape(scene,0,0,world.cols*World.T,world.rows*World.T);
            scene.addAll(NpcPopulation.create(world));scene.add(new Player(World.SPAWN_X,World.SPAWN_Y));
            scene.sort(Comparator.comparingDouble(Drawable::sortY));for(Drawable item:scene)item.draw(g,0,0,1);
        }finally{g.dispose();}
        check(ImageIO.write(overview,"png",new File("build/preview-roads-overview.png")),"Road overview export failed");
        BufferedImage farms=new BufferedImage(960,480,BufferedImage.TYPE_INT_RGB);g=farms.createGraphics();
        try{
            g.setColor(new Color(0x26372D));g.fillRect(0,0,farms.getWidth(),farms.getHeight());
            for(int kind=0;kind<3;kind++){
                FarmPlot plot=new FarmPlot(0,0,3,3,kind,7+kind);
                for(int y=0;y<4;y++)for(int x=0;x<4;x++)g.drawImage(Assets.grass[0],kind*320+16+x*72,96+y*72,72,72,null);
                g.drawImage(plot.image,kind*320+16,96,288,288,null);
                g.setColor(new Color(0xEFDCAD));g.drawString(new String[]{"Leaf vegetables","Grain rows","Root crops"}[kind],kind*320+20,65);
            }
        }finally{g.dispose();}
        check(ImageIO.write(farms,"png",new File("build/preview-farms.png")),"Farm preview export failed");
    }
    // [BLOCK: TEST HELPER] VillageLayoutTests.check - Handle check for VillageLayoutTests.
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
}
