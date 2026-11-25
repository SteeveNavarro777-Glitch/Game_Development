/* FILE PURPOSE: WORLD / DSA ARRAYS | Terrain grids, BFS roads, crop lots and spatial collision index.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Authored village districts, connected streets, and an indexed set of static colliders. */
public final class World {
    static final int T = Assets.TILE, GRASS = 0, DIRT = 1, WATER = 2, WALL = 3, ROCK = WALL, PAVING = 4, BRIDGE = 5, GATE = 6, MOUNTAIN = 7, CULVERT = 8;
    static final int VILLAGE_MIN=48,VILLAGE_COLS=64,VILLAGE_ROWS=48;
    static final int[][] VILLAGE_SITES=shifted(new int[][]{{32,27},{12,12},{49,26},{28,39},{51,39}});
    static final int[][] TRAIL_SITES={{80,75},{41,41},{118,72},{50,103},{117,103}};
    static final double SPAWN_X=(VILLAGE_MIN+32.5)*T,SPAWN_Y=(VILLAGE_MIN+27.5)*T;
    private static final int[][] DIRECTIONS = {{0,-1},{0,1},{-1,0},{1,0}};
    private static final int[][] WORK_SITES=shifted(new int[][]{{30,22},{24,22},{35,22},{24,31},{6,11}});
    // c/r are the feet of each house; generous front lanes keep every lot accessible.
    private static final int[][] LOTS = shifted(new int[][]{
        {6,8,0},{22,9,4},{33,8,2},{45,9,5},{57,9,1},
        {6,19,6},{22,19,0},{33,20,3},{45,19,2},{57,20,7},
        {7,29,0},{20,30,2},{25,29,0},{39,29,4},{58,29,1},
        {7,41,2},{19,43,5},{36,43,0},{43,42,3},{59,43,6}
    });
    final int cols=192,rows=160;
    // [SECTION: DSA - 2D ARRAYS] Tile type and visual variant at each row/column.
    final int[][] tile = new int[rows][cols], variant = new int[rows][cols];
    final Palisade palisade;
    final ForestRegion forest;
    final MountainRiver mountain;
    final DayNightCycle dayNight=new DayNightCycle();
    private long navigationVersion;
    final List<Tree> trees = new ArrayList<>();
    final List<Building> buildings = new ArrayList<>();
    final List<Prop> props = new ArrayList<>();
    private final List<FarmPlot> gardens = new ArrayList<>();
    // [SECTION: DSA - ARRAY CACHES] Streets, terrain edges and navigation are precomputed lookups.
    private final boolean[][] streets = new boolean[rows][cols];
    final int[][] terrainEdges = new int[rows][cols];
    interface Obstacle {
        // [BLOCK: OOP CONTRACT] World.collider - Expose a static collision footprint through the Obstacle contract.
        Rectangle2D.Double collider(); }
    // [SECTION: DSA - SPATIAL GRID / LISTS] Each grid bucket stores nearby static colliders.
    private final List<List<Obstacle>> buckets = new ArrayList<>();
    private final boolean[][] navigation = new boolean[rows][cols];
    long collisionChecks;
    private double ambientTime;
    // [BLOCK: FUNCTION] World.updateAmbient - Advance update ambient state with elapsed time.
    void updateAmbient(double dt){dayNight.update(dt);ambientTime+=Math.max(0,dt);for(Prop prop:props)prop.time=ambientTime;palisade.update(Math.max(0,dt));mountain.update(Math.max(0,dt));}

    // [BLOCK: FUNCTION] World.World - Initialize World state.
    World(long seed) {
        Random random = new Random(seed);
        for (int i = 0; i < rows * cols; i++) buckets.add(new ArrayList<>());
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) variant[r][c] = random.nextInt(8);
        // A watercourse separates the lower gardens, with broad walkable timber bridges.
        for (int c=villageCell(3);c<villageCell(VILLAGE_COLS-3);c++) {
            int r=villageCell(Math.max(32,33+(int)Math.round(2*Math.sin((c-VILLAGE_MIN)*.15))));
            tile[r][c] = tile[r+1][c] = tile[r+2][c] = WATER;
        }
        pond(villageCell(13),villageCell(36),6,4);pond(villageCell(46),villageCell(33),5,3);
        // Reserve homes first. No later lawn or farm operation may erase a street.
        for (int[] lot : LOTS) {
            buildings.add(new Building((lot[0]+.5)*T,lot[1]*T,lot[2]));
            rect(lot[0]-3,lot[1]-3,lot[0]+3,lot[1],GRASS);
        }
        palisade=new Palisade(this);
        // Three continuous avenues; the center avenue clears the gathering-house lots.
        // Only these avenues can cross water, so driveways cannot create private bridges.
        for(int local:new int[]{14,29,52}){int c=villageCell(local);street(c,villageCell(10),c,villageCell(44));}
        for(int local:new int[]{13,24,44}){int r=villageCell(local);street(villageCell(4),r,villageCell(59),r);}
        street(villageCell(29),villageCell(38),villageCell(52),villageCell(38)); // south-bank lane; the western avenue crosses the lake
        for (int[] site : VILLAGE_SITES) {
            connectStreet(site[0],site[1]);
            court(site[0]-3,site[1]-3,site[0]+3,site[1]+3);
        }
        court(villageCell(28),villageCell(23),villageCell(36),villageCell(31)); // open village spawn court and its eight-direction dash space
        connectStreet(villageCell(29),villageCell(2));connectStreet(villageCell(29),villageCell(45));connectStreet(villageCell(2),villageCell(24));connectStreet(villageCell(61),villageCell(24));
        for (int[] lot : LOTS) connectStreet(lot[0],lot[1]);
        // Group crops beside district streets, with a gate facing the existing pavement.
        farm(villageCell(3),villageCell(9),3,3,0,seed);farm(villageCell(18),villageCell(10),3,2,2,seed);
        farm(villageCell(36),villageCell(9),4,3,1,seed);farm(villageCell(54),villageCell(10),3,2,0,seed);
        farm(villageCell(3),villageCell(20),3,3,1,seed);farm(villageCell(18),villageCell(20),3,3,0,seed);
        farm(villageCell(40),villageCell(20),3,3,2,seed);farm(villageCell(54),villageCell(21),3,2,2,seed);
        placeProp(0,villageCell(22),villageCell(31),92,28);placeProp(1,villageCell(24),villageCell(21),88,25);placeProp(2,villageCell(35),villageCell(21),66,20);
        placeProp(3,villageCell(26),villageCell(20),60,28);placeProp(5,villageCell(31),villageCell(16),12,10);placeProp(5,villageCell(50),villageCell(20),12,10);
        placeProp(6,villageCell(4),villageCell(8),30,12);placeProp(6,villageCell(21),villageCell(19),30,12);placeProp(6,villageCell(56),villageCell(20),30,12);
        placeProp(7,villageCell(46),villageCell(20),34,16);placeProp(8,villageCell(32),villageCell(21),48,12);placeProp(9,villageCell(17),villageCell(32),40,14);
        placeProp(10,villageCell(22),villageCell(32),28,12);placeProp(11,villageCell(7),villageCell(32),58,15);
        placeProp(12,villageCell(17),villageCell(11),16,12);placeProp(12,villageCell(55),villageCell(16),16,12);placeProp(13,villageCell(40),villageCell(15),18,10);
        int tries = 0;
        while (trees.size() < 85 && tries++ < 6000) {
            int c=villageCell(3+random.nextInt(VILLAGE_COLS-6)),r=villageCell(4+random.nextInt(VILLAGE_ROWS-6));
            if (tile[r][c] != GRASS) continue;
            double x = (c+.5)*T, y = (r+1.0)*T;
            boolean okay = true;
            for (int[] site : VILLAGE_SITES)
                if (Math.hypot(x-(site[0]+.5)*T,y-(site[1]+.5)*T) < 170) okay = false;
            for (Tree tree : trees) if (Math.hypot(tree.x-x,tree.y-y) < 64) okay = false;
            Rectangle2D clearance = new Rectangle2D.Double(x-70,y-80,140,105);
            for (Building house : buildings) if (clearance.intersects(house.footprint)) okay = false;
            Rectangle2D canopy = new Rectangle2D.Double(x-80,y-160,160,172);
            for (FarmPlot garden : gardens) if (canopy.intersects(garden.bounds)) okay = false;
            // Keep stationary workers and their interaction labels visible beneath the canopy.
            for(int[] station:WORK_SITES)
                if(canopy.intersects((station[0]+.5)*T-24,(station[1]+.5)*T-68,48,80))okay=false;
            for (Prop prop : props) if (clearance.intersects(prop.footprint)) okay = false;
            if (intersectsStreet(new Rectangle2D.Double(x-22,y-24,44,32))) okay = false;
            if (okay) {
                Tree candidate=new Tree(x,y);
                Rectangle2D trunkClearance=new Rectangle2D.Double(candidate.trunk.x-8,candidate.trunk.y-8,candidate.trunk.width+16,candidate.trunk.height+16);
                if(intersectsStreet(trunkClearance)||!dryFootprint(candidate.trunk))continue;
                boolean overlap=false;
                for(Tree other:trees)if(trunkClearance.intersects(other.trunk))overlap=true;
                if(overlap)continue;
                // Retain seeded placement sampling after selecting a valid tree.
                random.nextDouble();trees.add(candidate);
            }
        }
        mountain=new MountainRiver(this);
        forest=new ForestRegion(this,seed);
        cacheTerrainEdges();
        for (Tree tree : trees) index(tree);
        for (Building house : buildings) index(house);
        for (Prop prop : props) index(prop);
        for(Palisade.Gate gate:palisade.gates())index(gate);
        rebuildNavigation();collisionChecks = 0;
    }
    // [BLOCK: WORLD COORDINATES] World.villageCell - Translate one authored village column or row into the expanded world.
    static int villageCell(int local){return local+VILLAGE_MIN;}
    // [BLOCK: WORLD COORDINATES / ARRAYS] World.shifted - Copy authored coordinates into the village origin without changing style IDs.
    private static int[][] shifted(int[][] authored){
        int[][] translated=new int[authored.length][];
        for(int i=0;i<authored.length;i++){translated[i]=authored[i].clone();translated[i][0]+=VILLAGE_MIN;translated[i][1]+=VILLAGE_MIN;}
        return translated;
    }
    // [BLOCK: REGION BOUNDS] World.inVillage - Identify the protected interior inside the wooden perimeter.
    boolean inVillage(double x,double y){
        return x>=villageCell(2)*T&&y>=villageCell(2)*T&&x<villageCell(VILLAGE_COLS-2)*T&&y<villageCell(VILLAGE_ROWS-2)*T;
    }
    // [BLOCK: TERRAIN CLEARANCE] World.dryFootprint - Reject a tree base that would grow into water or the wooden perimeter.
    private boolean dryFootprint(Rectangle2D box){
        for(int r=row(box.getMinY());r<=row(Math.nextDown(box.getMaxY()));r++)
            for(int c=col(box.getMinX());c<=col(Math.nextDown(box.getMaxX()));c++)if(solidTile(c,r)||tile[r][c]==GATE)return false;
        return true;
    }
    // [BLOCK: DSA - NAVIGATION CACHE] World.rebuildNavigation - Refresh collision-aware nodes only when the world or a gate changes.
    private void rebuildNavigation(){
        long saved=collisionChecks;
        for(int r=0;r<rows;r++)for(int c=0;c<cols;c++){
            boolean precise=bridgeApproach(c,r)||palisade.openPassageNear(c,r)||c==0||r==0||c==cols-1||r==rows-1;
            navigation[r][c]=precise?!blocked(new Rectangle2D.Double((c+.5)*T-10,(r+.5)*T-10,20,10)):
                    !blocked(new Rectangle2D.Double(c*T-10,r*T-10,T+20,T+10));
        }
        collisionChecks=saved;navigationVersion++;
    }
    // [BLOCK: CACHE INVALIDATION] World.navigationVersion - Identify a changed gate/navigation layout for shared BFS and A* caches.
    long navigationVersion(){return navigationVersion;}
    // [BLOCK: SAFE GATE INTERACTION] World.interactGate - Toggle a nearby leaf after checking player state and every living actor in its threshold.
    boolean interactGate(Player player,List<Enemy> enemies,List<Npc> npcs){
        if(!player.alive()||player.airborne()||player.dashing()||player.state==Player.State.HURT||player.state==Player.State.ATTACK)return false;
        Palisade.Gate gate=palisade.nearest(player.x,player.y);if(gate==null)return false;
        if(gate.open()&&gate.occupied(player,enemies,npcs)){gate.refuse();return true;}
        gate.toggle();rebuildNavigation();return true;
    }
    // [BLOCK: FUNCTION] World.bridgeApproach - Handle bridge approach for World.
    private boolean bridgeApproach(int c,int r){
        if(tile[r][c]==BRIDGE)return true;
        for(int[] d:DIRECTIONS)if(inside(c+d[0],r+d[1])&&tile[r+d[1]][c+d[0]]==BRIDGE)return true;
        return false;
    }
    // [BLOCK: FUNCTION] World.placeProp - Handle place prop for World.
    private void placeProp(int style,int c,int r,int width,int height){
        Prop prop=new Prop((c+.5)*T,(r+.5)*T,style,width,height);
        if(nearLandmark(prop.footprint,145)||intersectsStreet(prop.footprint))return;
        for(FarmPlot garden:gardens)if(garden.bounds.intersects(prop.footprint))return;
        for(int y=row(prop.footprint.getMinY());y<=row(Math.nextDown(prop.footprint.getMaxY()));y++)
            for(int x=col(prop.footprint.getMinX());x<=col(Math.nextDown(prop.footprint.getMaxX()));x++)
                if(solidTile(x,y))return;
        for(Building house:buildings)if(house.footprint.intersects(prop.footprint))return;
        for(Prop other:props)if(other.footprint.intersects(prop.footprint))return;
        props.add(prop);
    }
    // [BLOCK: FUNCTION] World.nearLandmark - Handle near landmark for World.
    private boolean nearLandmark(Rectangle2D box,double radius) {
        for (int[] site : VILLAGE_SITES)
            if (box.intersects((site[0]+.5)*T-radius,(site[1]+.5)*T-radius,radius*2,radius*2)) return true;
        return false;
    }
    // [BLOCK: FUNCTION] World.rect - Handle rect for World.
    private void rect(int c0,int r0,int c1,int r1,int kind) {
        for (int r=Math.max(villageCell(2),r0);r<=Math.min(villageCell(VILLAGE_ROWS-3),r1);r++)
            for (int c=Math.max(villageCell(2),c0);c<=Math.min(villageCell(VILLAGE_COLS-3),c1);c++) tile[r][c]=kind;
    }
    // [BLOCK: FUNCTION] World.roadAt - Handle road at for World.
    boolean roadAt(int c,int r){return inside(c,r)&&streets[r][c];}
    // [BLOCK: OOP: ENCAPSULATION] World.farms - Expose an unmodifiable snapshot of the farm list.
    List<FarmPlot> farms(){return List.copyOf(gardens);}
    // [BLOCK: FUNCTION] World.intersectsStreet - Handle intersects street for World.
    private boolean intersectsStreet(Rectangle2D box){
        for(int r=row(box.getMinY());r<=row(Math.nextDown(box.getMaxY()));r++)
            for(int c=col(box.getMinX());c<=col(Math.nextDown(box.getMaxX()));c++)
                if(roadAt(c,r))return true;
        return false;
    }
    // [BLOCK: FUNCTION] World.streetClear - Handle street clear for World.
    private boolean streetClear(int c,int r,int radius,boolean crossing){
        if(c-radius<villageCell(2)||r-radius<villageCell(2)||c+radius>=villageCell(VILLAGE_COLS-2)||r+radius>=villageCell(VILLAGE_ROWS-2))return false;
        if(tile[r][c]==WALL||tile[r][c]==GATE)return false;
        if(tile[r][c]==WATER&&(!crossing||(c!=villageCell(14)&&c!=villageCell(29)&&c!=villageCell(52))))return false;
        Rectangle2D box=new Rectangle2D.Double((c-radius)*T,(r-radius)*T,(2*radius+1)*T,(2*radius+1)*T);
        for(Building home:buildings)if(home.footprint.intersects(box))return false;
        for(FarmPlot garden:gardens)if(garden.bounds.intersects(box))return false;
        return true;
    }
    /** Shortest orthogonal routes are built once, before colliders and navigation are indexed. */
    // [BLOCK: DSA: BFS / FIFO QUEUE] World.streetRoute - Find the shortest valid orthogonal street route using a predecessor array.
    private List<Integer> streetRoute(int sc,int sr,int tc,int tr,int radius,boolean crossing){
        // [SECTION: DSA - PREDECESSOR ARRAY] Encoded row/column parents reconstruct the street path.
        int[] parent=new int[rows*cols];Arrays.fill(parent,-1);
        ArrayDeque<Integer> queue=new ArrayDeque<>();int start=sr*cols+sc,end=-1;
        if(!streetClear(sc,sr,radius,crossing))throw new IllegalStateException("Blocked street endpoint: "+sc+","+sr);
        parent[start]=start;queue.addLast(start);
        while(!queue.isEmpty()){
            int cell=queue.removeFirst(),c=cell%cols,r=cell/cols;
            if(tc<0?roadAt(c,r):c==tc&&r==tr){end=cell;break;}
            for(int[] d:DIRECTIONS){
                int x=c+d[0],y=r+d[1];
                if(!inside(x,y)||parent[y*cols+x]>=0||!streetClear(x,y,radius,crossing))continue;
                parent[y*cols+x]=cell;queue.addLast(y*cols+x);
            }
        }
        if(end<0)throw new IllegalStateException("No street route from "+sc+","+sr+" to "+tc+","+tr);
        List<Integer> route=new ArrayList<>();
        for(int cell=end;;cell=parent[cell]){route.add(cell);if(cell==start)break;}
        Collections.reverse(route);return route;
    }
    // [BLOCK: FUNCTION] World.street - Handle street for World.
    private void street(int c0,int r0,int c1,int r1){
        pave(streetRoute(c0,r0,c1,r1,1,true),PAVING,1);
    }
    // [BLOCK: FUNCTION] World.connectStreet - Handle connect street for World.
    private void connectStreet(int c,int r){
        pave(streetRoute(c,r,-1,-1,0,false),DIRT,0);
    }
    // [BLOCK: FUNCTION] World.pave - Handle pave for World.
    private void pave(List<Integer> route,int kind,int radius){
        for(int cell:route){
            int c=cell%cols,r=cell/cols;
            for(int y=r-radius;y<=r+radius;y++)for(int x=c-radius;x<=c+radius;x++){
                if(tile[y][x]==WATER&&(x!=c||y!=r))continue;
                if(tile[y][x]==WATER)tile[y][x]=BRIDGE;
                else if(tile[y][x]!=BRIDGE&&(kind==PAVING||tile[y][x]!=PAVING))tile[y][x]=kind;
                streets[y][x]=true;
            }
        }
    }
    // [BLOCK: FUNCTION] World.court - Handle court for World.
    private void court(int c0,int r0,int c1,int r1){
        for(int r=r0;r<=r1;r++)for(int c=c0;c<=c1;c++){
            if(!streetClear(c,r,0,false)||tile[r][c]==BRIDGE)continue;
            tile[r][c]=PAVING;streets[r][c]=true;
        }
    }
    // [BLOCK: FUNCTION] World.farm - Handle farm for World.
    private void farm(int c,int r,int width,int height,int kind,long seed){
        FarmPlot plot=new FarmPlot(c,r,width,height,kind,seed+c*71L+r*313L);
        if(intersectsStreet(plot.bounds)||nearLandmark(plot.bounds,100))throw new IllegalStateException("Farm overlaps a street/court: "+c+","+r);
        for(int y=r;y<r+height;y++)for(int x=c;x<c+width;x++)
            if(tile[y][x]!=GRASS)throw new IllegalStateException("Farm is not on dry grass: "+c+","+r);
        for(Building home:buildings)if(home.footprint.intersects(plot.bounds))throw new IllegalStateException("Farm overlaps home");
        for(FarmPlot other:gardens)if(other.bounds.intersects(plot.bounds))throw new IllegalStateException("Farm overlap");
        if(!roadAt(plot.gateCol,plot.gateRow))connectStreet(plot.gateCol,plot.gateRow);
        gardens.add(plot);
    }
    // [BLOCK: DSA: 2D ARRAY CACHE] World.cacheTerrainEdges - Precompute terrain edge masks once per world.
    private void cacheTerrainEdges(){
        for(int r=0;r<rows;r++)for(int c=0;c<cols;c++){
            int mask=0,kind=tile[r][c]==CULVERT?WATER:tile[r][c];
            for(int i=0;i<DIRECTIONS.length;i++){
                // RpgTerrain masks use clockwise N,E,S,W rather than movement order.
                int dc=i==1?1:i==3?-1:0,dr=i==0?-1:i==2?1:0;
                int neighbor=at(c+dc,r+dr);if(neighbor==CULVERT)neighbor=WATER;
                if(kind==WATER?neighbor!=WATER&&neighbor!=BRIDGE:neighbor==GRASS||neighbor==WALL)mask|=1<<i;
            }
            terrainEdges[r][c]=mask;
        }
    }
    // [BLOCK: FUNCTION] World.pond - Handle pond for World.
    private void pond(double cx,double cy,double rx,double ry) {
        for(int r=villageCell(2);r<villageCell(VILLAGE_ROWS-2);r++)for(int c=villageCell(2);c<villageCell(VILLAGE_COLS-2);c++){
            double d=Math.pow((c-cx)/rx,2)+Math.pow((r-cy)/ry,2);
            if(d<1)tile[r][c]=WATER;else if(d<1.4&&tile[r][c]!=WATER)tile[r][c]=DIRT;
        }
    }
    // [BLOCK: DSA: SPATIAL GRID] World.index - Place each collider into only the uniform-grid buckets it overlaps.
    private void index(Obstacle object) {
        Rectangle2D box=object.collider();
        for(int r=row(box.getMinY());r<=row(Math.nextDown(box.getMaxY()));r++)
            for(int c=col(box.getMinX());c<=col(Math.nextDown(box.getMaxX()));c++)
                if(inside(c,r))buckets.get(r*cols+c).add(object);
    }
    // [BLOCK: FUNCTION] World.col - Handle col for World.
    static int col(double x){return (int)Math.floor(x/T);}
    // [BLOCK: FUNCTION] World.row - Handle row for World.
    static int row(double y){return (int)Math.floor(y/T);}
    // [BLOCK: FUNCTION] World.inside - Handle inside for World.
    boolean inside(int c,int r){return c>=0&&r>=0&&c<cols&&r<rows;}
    // [BLOCK: FUNCTION] World.at - Handle at for World.
    private int at(int c,int r){return inside(c,r)?tile[r][c]:ROCK;}
    // [BLOCK: FUNCTION] World.solidTile - Handle solid tile for World.
    boolean solidTile(int c,int r){int t=at(c,r);return t==WATER||t==ROCK||t==MOUNTAIN||t==CULVERT;}
    // [BLOCK: FUNCTION] World.walkable - Handle walkable for World.
    boolean walkable(int c,int r){return inside(c,r)&&navigation[r][c];}

    // [BLOCK: COLLISION: INDEXED SEARCH] World.blocked - Query solid terrain and nearby bucket colliders.
    boolean blocked(Rectangle2D box) {
        int c0=col(box.getMinX()),c1=col(Math.nextDown(box.getMaxX()));
        int r0=row(box.getMinY()),r1=row(Math.nextDown(box.getMaxY()));
        for(int r=r0;r<=r1;r++)for(int c=c0;c<=c1;c++){
            if(solidTile(c,r))return true;
            for(Obstacle object:buckets.get(r*cols+c)){
                collisionChecks++;
                if(object.collider().intersects(box))return true;
            }
        }
        return false;
    }
    // [BLOCK: VERIFICATION: REFERENCE SEARCH] World.blockedBruteForce - Check every obstacle for comparison with the spatial index.
    boolean blockedBruteForce(Rectangle2D box) {
        for(int r=row(box.getMinY());r<=row(Math.nextDown(box.getMaxY()));r++)
            for(int c=col(box.getMinX());c<=col(Math.nextDown(box.getMaxX()));c++)
                if(solidTile(c,r))return true;
        for(Tree tree:trees)if(tree.trunk.intersects(box))return true;
        for(Building house:buildings)if(house.footprint.intersects(box))return true;
        for(Prop prop:props)if(prop.footprint.intersects(box))return true;
        for(Palisade.Gate gate:palisade.gates())if(gate.collider().intersects(box))return true;
        return false;
    }
    // [BLOCK: LANDSCAPE CULLING] World.collectLandscape - Append visible timber defenses and the cached mountain/waterfall to depth sorting.
    void collectLandscape(List<Drawable> target,int cx,int cy,int width,int height){
        palisade.collect(target,cx,cy,width,height);mountain.collect(target,cx,cy,width,height);
    }
    // [BLOCK: FUNCTION] World.drawGround - Render draw ground using the current graphics state.
    void drawGround(Graphics2D g,int cx,int cy,int vw,int vh,double time) {
        // A small forest-colored camera margin keeps the north gate and Agyu below the HUD.
        g.setColor(new Color(0x294A34));g.fillRect(0,0,vw,vh);
        if(cy<0)for(int x=Math.floorDiv(cx,256)*256;x<cx+vw;x+=256)g.drawImage(RpgArt.forestEdge(),x-cx,-64-cy,null);
        int c0=Math.max(0,Math.floorDiv(cx,T)),c1=Math.min(cols-1,Math.floorDiv(cx+vw,T));
        int r0=Math.max(0,Math.floorDiv(cy,T)),r1=Math.min(rows-1,Math.floorDiv(cy+vh,T));
        for(int r=r0;r<=r1;r++)for(int c=c0;c<=c1;c++){
            int kind=tile[r][c],v=variant[r][c],x=c*T-cx,y=r*T-cy;
            BufferedImage image=kind==WATER&&mountain.underMouth(c,r)?ForestRegion.ground(v):switch(kind){
                case DIRT,WATER,PAVING,GATE,CULVERT->RpgTerrain.tile(this,c,r,time);
                case BRIDGE->VillageArt.bridge;
                case MOUNTAIN->ForestRegion.ground(v);
                default->kind==GRASS&&forest.contains((c+.5)*T,(r+.5)*T)?ForestRegion.ground(v):Assets.grass[v];};
            g.drawImage(image,x,y,null);
            if(kind==BRIDGE)for(int[] d:DIRECTIONS){
                int neighbor=at(c+d[0],r+d[1]);
                boolean edge=kind==WATER?neighbor!=WATER&&neighbor!=BRIDGE:
                        kind==BRIDGE?neighbor==WATER:neighbor==GRASS;
                if(!edge)continue;
                g.setColor(kind==WATER?new Color(0xA4C9B2):kind==BRIDGE?new Color(0x443D27):new Color(0xD1C29A));
                int thick=kind==BRIDGE?3:1;
                if(d[1]<0)g.fillRect(x,y,T,thick);if(d[1]>0)g.fillRect(x,y+T-thick,T,thick);
                if(d[0]<0)g.fillRect(x,y,thick,T);if(d[0]>0)g.fillRect(x+T-thick,y,thick,T);
            }
        }
        for(FarmPlot garden:gardens)if(garden.bounds.intersects(cx,cy,vw,vh))garden.draw(g,cx,cy);

    }
    static final class Prop implements Drawable,Obstacle {
        final double x,y;final int style;final Rectangle2D.Double footprint;double time;
        // [BLOCK: FUNCTION] World.Prop - Initialize Prop state.
        Prop(double x,double y,int style,int width,int height){
            this.x=x;this.y=y;this.style=style;footprint=new Rectangle2D.Double(x-width/2.0,y-height,width,height);
        }
        // [BLOCK: FUNCTION] World.collider - Expose a static collision footprint through the Obstacle contract.
        public Rectangle2D.Double collider(){return footprint;}
        // [BLOCK: FUNCTION] World.sortY - Handle sort y for World.
        public double sortY(){return y;}
        // [BLOCK: FUNCTION] World.draw - Render draw using the current graphics state.
        public void draw(Graphics2D g,int cx,int cy,double alpha){
            BufferedImage image=style<12?RpgArt.prop(style):RpgArt.foliage(style-8);
            int sx=(int)x-cx,sy=(int)y-cy;
            g.setColor(new Color(0,0,0,45));g.fillOval(sx-(int)footprint.width/2,sy-5,(int)footprint.width,10);
            g.drawImage(image,sx-image.getWidth()/2,sy-image.getHeight()+1,null);
            int phase=(int)(time*5)%4;
            if(style==3){
                g.setColor(new Color(0xC3F5ED));g.fillRect(sx-12+phase*7,sy-19+(phase%2)*3,4,1);
                g.fillRect(sx+9-phase*4,sy-12,2,1);
            }else if(style==1){
                g.setColor(new Color(phase%2==0?0xFFD27D:0xFBA248));g.fillRect(sx-28,sy-32-phase,3,2+phase);
            }
        }
    }
    static final class Building implements Drawable,Obstacle {
        final double x,y;
        final int style;
        final Rectangle2D.Double footprint;
        // [BLOCK: FUNCTION] World.Building - Initialize Building state.
        Building(double x,double y,int style){
            this.x=x;this.y=y;this.style=style;
            double scale=style<4?GameScale.TRADITIONAL_HOME:1;
            footprint=new Rectangle2D.Double(x-73*scale,y-74*scale,146*scale,74*scale);
        }
        // [BLOCK: FUNCTION] World.collider - Expose a static collision footprint through the Obstacle contract.
        public Rectangle2D.Double collider(){return footprint;}
        // [BLOCK: FUNCTION] World.sortY - Handle sort y for World.
        public double sortY(){return y;}
        // [BLOCK: FUNCTION] World.draw - Render draw using the current graphics state.
        public void draw(Graphics2D g,int cx,int cy,double alpha){
            BufferedImage image=VillageArt.houses[style];
            int sx=(int)Math.round(x)-cx,sy=(int)Math.round(y)-cy;
            double scale=style<4?GameScale.TRADITIONAL_HOME:1;
            g.setColor(new Color(0,0,0,60));g.fillOval(sx-(int)Math.round(81*scale),sy-(int)Math.round(12*scale),(int)Math.round(162*scale),(int)Math.round(20*scale));
            g.drawImage(image,sx-image.getWidth()/2,sy-image.getHeight()+1,null);
        }
    }
    static final class Tree implements Drawable,Obstacle {
        final double x,y,rootY;final int style;
        final Rectangle2D.Double trunk;
        private final BufferedImage image;
        // [BLOCK: ASSET-ALIGNED COLLISION] World.Tree - Cache the sprite/style and place a broad solid root footprint on its actual bark.
        Tree(double x,double y){
            this.x=x;this.y=y;style=Math.floorMod(col(x)+row(y)*3,4);
            image=RpgArt.tree(style);RpgArt.TreeGround ground=RpgArt.treeGround(style);
            rootY=y+ground.floor();trunk=new Rectangle2D.Double(x+ground.left(),y+ground.top(),ground.width(),ground.height());
        }
        // [BLOCK: STATIC OBSTACLE] World.collider - Return the cached tree-root collider used by all actors.
        public Rectangle2D.Double collider(){return trunk;}
        // [BLOCK: DEPTH SORTING] World.sortY - Sort at the actual roots rather than stray grass below the sprite.
        public double sortY(){return rootY;}
        // [BLOCK: CACHED RENDER] World.draw - Draw an unoccluded tree for scene exports and generic Drawable callers.
        public void draw(Graphics2D g,int cx,int cy,double alpha){draw(g,cx,cy,alpha,null);}
        // [BLOCK: PLAYER VISIBILITY] World.draw - Fade only the canopy when it hides Agyu; the root/trunk base remains opaque and solid.
        void draw(Graphics2D target,int cx,int cy,double alpha,Player player){
            Graphics2D g=(Graphics2D)target.create();
            try{
                int sx=(int)Math.round(x)-cx,sy=(int)Math.round(y)-cy;
                g.setColor(new Color(0,0,0,55));g.fillOval((int)trunk.x-cx-3,(int)rootY-cy-4,(int)trunk.width+6,10);
                boolean fade=false;
                if(player!=null){
                    double blend=Math.max(0,Math.min(1,alpha));
                    double px=player.px+(player.x-player.px)*blend,py=player.py+(player.y-player.py)*blend;
                    double width=player.state==Player.State.DEAD?84:36;
                    fade=py<rootY&&new Rectangle2D.Double(x-80,y-159,160,128).intersects(px-width/2,py-70,width,70);
                }
                int left=sx-80,top=sy-159;
                if(fade){
                    Composite original=g.getComposite();float opacity=original instanceof AlphaComposite?((AlphaComposite)original).getAlpha():1;
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,opacity*.32f));
                    g.drawImage(image,left,top,left+160,top+128,0,0,160,128,null);g.setComposite(original);
                    g.drawImage(image,left,top+128,left+160,top+160,0,128,160,160,null);
                }else g.drawImage(image,left,top,null);
            }finally{g.dispose();}
        }
    }
}
