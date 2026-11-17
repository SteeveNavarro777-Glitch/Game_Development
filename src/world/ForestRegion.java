/* FILE PURPOSE: FOREST WORLD | Seeded dense woodland, clear gate trails and combat clearings outside the village.
 * Course examples: 2D arrays, seeded iteration and cached scenery; docs/CODE_GUIDE.md.
 */
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/** Forest scenery is generated once; the same root colliders and navigation serve every actor. */
final class ForestRegion {
    private final World world;
    private static BufferedImage[] ground;
    // [SECTION: DSA - 2D ARRAY] Reserve clear walking lanes before placing any solid roots.
    private final boolean[][] trails;
    private final List<Point> camps=List.of(new Point(67,40),new Point(118,61),new Point(87,103),new Point(34,76));
    record Arena(int id,String name,int c,int r,int rx,int ry) {}
    private final List<Arena> arenas=List.of(new Arena(1,"WEST CLEARING",22,25,8,7),
            new Arena(2,"EAST CLEARING",144,24,8,7),new Arena(3,"SOUTH CLEARING",96,132,8,7));
    private int treeCount;
    // [BLOCK: SEEDED WORLD] ForestRegion.ForestRegion - Connect the four gates to a woodland loop and reserve combat clearings.
    ForestRegion(World world,long seed){
        this.world=world;cacheGround();trails=new boolean[world.rows][world.cols];
        line(42,42,117,42);line(117,42,117,102);line(117,102,42,102);line(42,102,42,42);
        line(77,World.VILLAGE_MIN+1,77,42);line(77,World.VILLAGE_MIN+World.VILLAGE_ROWS-2,77,102);
        line(World.VILLAGE_MIN+1,72,42,72);line(World.VILLAGE_MIN+World.VILLAGE_COLS-2,72,117,72);
        route(new int[][]{{42,42},{32,41},{30,32},{22,25},{35,15},{68,14},{95,18},{122,14},{144,24}});
        route(new int[][]{{117,42},{130,42},{130,30},{144,24},{146,45},{144,64},{144,104},{129,122},{96,132}});
        route(new int[][]{{96,132},{64,135},{33,123},{16,110},{16,75},{17,50},{22,25}});
        route(new int[][]{{42,72},{30,72},{16,75}});
        route(new int[][]{{77,102},{78,114},{96,132}});
        route(new int[][]{{117,72},{130,72},{144,64}});
        route(new int[][]{{144,104},{157,104},{171,97},{176,93}});
        clearing(176,92,4,2);
        for(int i=1;i<World.TRAIL_SITES.length;i++){
            int[] site=World.TRAIL_SITES[i];clearing(site[0],site[1],4,4);
        }
        for(Point camp:camps)clearing(camp.x,camp.y,3,3);
        for(Arena arena:arenas)clearing(arena.c(),arena.r(),arena.rx(),arena.ry());
        // [SECTION: DSA - SPATIAL GRID] Temporary buckets keep thousands of seeded root checks local during construction.
        List<List<Rectangle2D>> roots=new ArrayList<>(world.rows*world.cols);
        for(int i=0;i<world.rows*world.cols;i++)roots.add(new ArrayList<>());
        for(World.Tree tree:world.trees)store(roots,tree.trunk);
        Random random=new Random(seed^0x46F07E57L);
        // Dense overlapping crowns retain solid, nonoverlapping root positions and navigable trail margins.
        for(int r=2;r<world.rows-1;r+=2)for(int c=1;c<world.cols-1;c+=2){
            double x=(c+.5)*World.T+random.nextInt(17)-8,y=(r+1.0)*World.T+random.nextInt(13)-6;
            if(!contains(x,y))continue;
            World.Tree tree=new World.Tree(x,y);
            Rectangle2D root=new Rectangle2D.Double(tree.trunk.x-12,tree.trunk.y-12,tree.trunk.width+24,tree.trunk.height+24);
            if(reserved(root)||!dry(root)||touchesVillage(root))continue;
            boolean overlap=false;
            for(int y0=World.row(root.getMinY());y0<=World.row(root.getMaxY())&&!overlap;y0++)
                for(int x0=World.col(root.getMinX());x0<=World.col(root.getMaxX())&&!overlap;x0++)
                    if(world.inside(x0,y0))for(Rectangle2D other:roots.get(y0*world.cols+x0))if(root.intersects(other)){overlap=true;break;}
            if(!overlap){world.trees.add(tree);store(roots,tree.trunk);treeCount++;}
        }
    }
    // [BLOCK: CONSTRUCTION INDEX] ForestRegion.store - Index one root only in construction buckets that overlap its footprint.
    private void store(List<List<Rectangle2D>> roots,Rectangle2D box){
        for(int r=World.row(box.getMinY());r<=World.row(box.getMaxY());r++)
            for(int c=World.col(box.getMinX());c<=World.col(box.getMaxX());c++)if(world.inside(c,r))roots.get(r*world.cols+c).add(box);
    }
    // [BLOCK: ENCAPSULATION] ForestRegion.arenas - Expose three immutable distant arenas assigned to the villain encounters.
    List<Arena> arenas(){return arenas;}
    // [BLOCK: TRAIL WAYPOINTS] ForestRegion.route - Join authored bends into connected paths through each forest region.
    private void route(int[][] points){for(int i=1;i<points.length;i++)line(points[i-1][0],points[i-1][1],points[i][0],points[i][1]);}
    // [BLOCK: TEXTURE CACHE] ForestRegion.cacheGround - Prepare shaded woodland grass once without changing the source images.
    private static void cacheGround(){
        if(ground!=null)return;ground=new BufferedImage[Assets.grass.length];
        for(int i=0;i<ground.length;i++){
            ground[i]=new BufferedImage(World.T,World.T,BufferedImage.TYPE_INT_ARGB);Graphics2D g=ground[i].createGraphics();
            try{g.drawImage(Assets.grass[i],0,0,null);g.setColor(new Color(9,37,23,55));g.fillRect(0,0,World.T,World.T);}finally{g.dispose();}
        }
    }
    // [BLOCK: RENDER LOOKUP] ForestRegion.ground - Reuse a shaded grass tile without allocating during rendering.
    static BufferedImage ground(int variant){return ground[Math.floorMod(variant,ground.length)];}
    // [BLOCK: REGION QUERY] ForestRegion.contains - Identify world positions beyond the timber enclosure.
    boolean contains(double x,double y){
        double t=World.T,left=(World.VILLAGE_MIN+1)*t,top=left;
        return x>=0&&y>=0&&x<world.cols*t&&y<world.rows*t&&
                (x<left||y<top||x>=(World.VILLAGE_MIN+World.VILLAGE_COLS-1)*t||y>=(World.VILLAGE_MIN+World.VILLAGE_ROWS-1)*t);
    }
    // [BLOCK: CACHED SCENERY] ForestRegion.treeCount - Report the generated exterior population without traversing it again.
    int treeCount(){return treeCount;}
    // [BLOCK: ENCAPSULATION] ForestRegion.camp - Return a copy of an authored outdoor respawn clearing.
    Point camp(int index){return new Point(camps.get(index));}
    // [BLOCK: CACHED ARRAY LOOKUP] ForestRegion.trailAt - Read a reserved forest trail cell without changing terrain.
    boolean trailAt(int c,int r){return world.inside(c,r)&&trails[r][c];}
    // [BLOCK: TRAIL GENERATION] ForestRegion.line - Paint a three-cell-wide orthogonal woodland trail, preserving gate and wall tiles.
    private void line(int x,int y,int tx,int ty){
        int dx=Math.abs(tx-x),dy=-Math.abs(ty-y),sx=Integer.compare(tx,x),sy=Integer.compare(ty,y),error=dx+dy;
        while(true){clearing(x,y,1,1);if(x==tx&&y==ty)break;int twice=2*error;
            if(twice>=dy){error+=dy;x+=sx;}if(twice<=dx){error+=dx;y+=sy;}}
    }
    // [BLOCK: CLEARING GENERATION] ForestRegion.clearing - Reserve dry ground and approach space around trails, camps and shrines.
    private void clearing(int c,int r,int rx,int ry){
        for(int y=r-ry;y<=r+ry;y++)for(int x=c-rx;x<=c+rx;x++){
            if(!world.inside(x,y)||!contains((x+.5)*World.T,(y+.5)*World.T))continue;
            if(rx>1&&Math.pow((x-c)/(rx+.45),2)+Math.pow((y-r)/(ry+.45),2)>1)continue;
            int kind=world.tile[y][x];if(kind==World.WALL||kind==World.CULVERT||kind==World.MOUNTAIN||kind==World.WATER)continue;
            trails[y][x]=true;if(kind!=World.GATE&&kind!=World.BRIDGE)world.tile[y][x]=World.DIRT;
        }
    }
    // [BLOCK: ROOT CLEARANCE] ForestRegion.reserved - Keep the complete padded root rectangle out of every trail and clearing.
    private boolean reserved(Rectangle2D box){
        for(int r=World.row(box.getMinY());r<=World.row(box.getMaxY());r++)
            for(int c=World.col(box.getMinX());c<=World.col(box.getMaxX());c++)if(trailAt(c,r))return true;
        return false;
    }
    // [BLOCK: TERRAIN VALIDATION] ForestRegion.dry - Require the entire padded root footprint to lie on grass inside the world.
    private boolean dry(Rectangle2D box){
        for(int r=World.row(box.getMinY());r<=World.row(box.getMaxY());r++)
            for(int c=World.col(box.getMinX());c<=World.col(box.getMaxX());c++)
                if(!world.inside(c,r)||world.tile[r][c]!=World.GRASS)return false;
        return true;
    }
    // [BLOCK: WALL CLEARANCE] ForestRegion.touchesVillage - Preserve an open strip immediately outside the protected village.
    private boolean touchesVillage(Rectangle2D box){
        return box.intersects((World.VILLAGE_MIN+.5)*World.T,(World.VILLAGE_MIN+.5)*World.T,
                (World.VILLAGE_COLS-1)*World.T,(World.VILLAGE_ROWS-1)*World.T);
    }
}
