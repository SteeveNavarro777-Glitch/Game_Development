/* FILE PURPOSE: MOUNTAIN RIVER | Solid eastern highlands, animated waterfall and a continuous river through protected village culverts.
 * Course examples: cached arrays, immutable geometry and Drawable polymorphism; docs/CODE_GUIDE.md.
 */
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/** Terrain and artwork are prepared once; only simulation time controls water animation. */
final class MountainRiver implements Drawable {
    static final int CENTER_C=176,SOURCE_R=82;
    private final World world;
    private final Rectangle2D.Double visual=new Rectangle2D.Double((CENTER_C+.5)*World.T-512,46*World.T,1024,1280);
    static final int COVE_X=(int)((CENTER_C+.5)*World.T)-WaterfallArt.FALL_X,COVE_Y=SOURCE_R*World.T-WaterfallArt.FOOT;
    private final Rectangle2D.Double cove=new Rectangle2D.Double(COVE_X,COVE_Y,WaterfallArt.WIDTH,WaterfallArt.HEIGHT);
    private final Rectangle2D.Double mouth=new Rectangle2D.Double(COVE_X-172,COVE_Y+235,192,288);
    private final Rectangle2D full=visual.createUnion(cove).createUnion(mouth);
    private double time;
    // [BLOCK: TERRAIN CONSTRUCTION] MountainRiver.MountainRiver - Raise eastern rock and link the waterfall basin to the existing river and western outflow.
    MountainRiver(World world){
        this.world=world;
        BufferedImage silhouette=MountainArt.mountain();
        for(int r=46;r<=86;r++)for(int c=160;c<world.cols;c++){
            int left=(int)(c*World.T-visual.x),top=(int)(r*World.T-visual.y);boolean opaque=false;
            for(int y=Math.max(0,top);y<Math.min(silhouette.getHeight(),top+World.T)&&!opaque;y++)
                for(int x=Math.max(0,left);x<Math.min(silhouette.getWidth(),left+World.T);x++)
                    if((silhouette.getRGB(x,y)>>>24)>128){opaque=true;break;}
            if(opaque)world.tile[r][c]=World.MOUNTAIN;
        }
        // Source water descends the cliff; the basin remains impassable to all walking actors.
        for(int r=68;r<=SOURCE_R;r++)for(int c=CENTER_C-1;c<=CENTER_C+1;c++)world.tile[r][c]=World.WATER;
        channel(new int[][]{{168,87},{163,89},{153,87},{141,82},{128,84},{115,84},{108,83}});
        registerCove();registerMouth();
        channel(new int[][]{{51,83},{39,84},{29,89},{16,90},{0,94}});
        // Existing city crossings stay untouched; these four forest bridges preserve the outer/inner trails.
        for(int c:new int[]{16,42,117,144})bridge(c);
        // The pond is open water; its dry southern beach is the viewing area.
    }
    // [BLOCK: ENCAPSULATION] MountainRiver.source - Return a copy of the waterfall basin's river source coordinate.
    Point source(){return new Point(CENTER_C,SOURCE_R);}
    // [BLOCK: ENCAPSULATION] MountainRiver.bounds - Return a copy of the full mountain artwork bounds.
    Rectangle2D bounds(){return (Rectangle2D)full.clone();}
    // [BLOCK: COVE GEOMETRY] MountainRiver.coveBounds - Return registered artwork bounds for maps and scenery checks.
    Rectangle2D coveBounds(){return (Rectangle2D)cove.clone();}
    // [BLOCK: FOREST CLEARANCE] MountainRiver.reserved - Keep tree crowns and roots away from the open pond and its walkable shore.
    boolean reserved(Rectangle2D box){Rectangle2D area=cove.createUnion(mouth);return box.intersects(area.getX()-64,area.getY()-32,area.getWidth()+128,area.getHeight()+96);}
    // [BLOCK: OUTLET GROUND LAYER] MountainRiver.underMouth - Draw forest ground beneath the exact cove and connector instead of rectangular water backing.
    boolean underMouth(int c,int r){double x=(c+.5)*World.T,y=(r+.5)*World.T;return mouth.contains(x,y)||cove.contains(x,y);}
    // [BLOCK: OUTLET GEOMETRY] MountainRiver.mouthBounds - Return an isolated copy of the exact registered connector extent.
    Rectangle2D mouthBounds(){return (Rectangle2D)mouth.clone();}
    // [BLOCK: SIMULATION CLOCK] MountainRiver.update - Advance animation only when the world simulation is active.
    void update(double dt){time+=Math.max(0,dt);}
    // [BLOCK: RENDER CULLING] MountainRiver.collect - Add the huge mountain only when its art intersects the camera.
    void collect(List<Drawable> target,int cx,int cy,int width,int height){if(full.intersects(cx,cy,width,height))target.add(this);}
    // [BLOCK: SCENERY DEPTH] MountainRiver.sortY - Draw the solid cliff behind actors walking on its valid dry shore.
    public double sortY(){return visual.y-1;}
    // [BLOCK: SCENIC CAMERA] MountainRiver.lookAhead - Gently lift the camera beside the basin while keeping Agyu on screen.
    double lookAhead(double x,double y){
        double dx=Math.abs(x-(CENTER_C+.5)*World.T),dy=Math.abs(y-86*World.T);
        return 84*Math.max(0,Math.min(1,Math.min((8*World.T-dx)/(3*World.T),(10*World.T-dy)/(4*World.T))));
    }
    // [BLOCK: CACHED RENDER] MountainRiver.draw - Select prebuilt mountain, cascade and cove frames without mutating simulation.
    public void draw(Graphics2D target,int cx,int cy,double alpha){
        Graphics2D g=(Graphics2D)target.create();
        try{
            drawArt(g,cx,cy,(int)(time*6)%WaterfallArt.FRAMES);
        }finally{g.dispose();}
    }
    // [BLOCK: REGISTERED SCENERY] MountainRiver.drawArt - Align the upper source, lower cliff waterfall, receiving pond and southwest outlet.
    private void drawArt(Graphics2D g,int cx,int cy,int phase){
        g.drawImage(MountainArt.mountain(),(int)visual.x-cx,(int)visual.y-cy,null);
        g.drawImage(WaterfallArt.mouth(phase),(int)mouth.x-cx,(int)mouth.y-cy,null);
        int center=(int)((CENTER_C+.5)*World.T)-cx;
        g.drawImage(WaterfallArt.upper(phase),center-48,68*World.T-cy,null);
        g.drawImage(WaterfallArt.cove(phase),COVE_X-cx,COVE_Y-cy,null);
    }
    // [BLOCK: MAP CACHE] MountainRiver.drawOverview - Draw correctly registered scenery into the whole-world map without stretching the mountain.
    void drawOverview(Graphics2D target,int width,int height){
        Graphics2D g=(Graphics2D)target.create();try{g.scale(width/(double)(world.cols*World.T),height/(double)(world.rows*World.T));drawArt(g,0,0,0);}finally{g.dispose();}
    }
    // [BLOCK: ART / COLLISION REGISTRATION] MountainRiver.registerCove - Derive solid water and cliffs from the same native artwork used for rendering.
    private void registerCove(){
        BufferedImage image=WaterfallArt.cove(0);
        for(int r=World.row(COVE_Y);r<=World.row(COVE_Y+WaterfallArt.HEIGHT-1);r++)
            for(int c=World.col(COVE_X);c<=World.col(COVE_X+WaterfallArt.WIDTH-1);c++){
                if(!world.inside(c,r))continue;
                int sx=(int)((c+.5)*World.T-COVE_X),sy=(int)((r+.5)*World.T-COVE_Y),wet=0;
                for(int y=r*World.T-COVE_Y;y<(r+1)*World.T-COVE_Y;y++)
                    for(int x=c*World.T-COVE_X;x<(c+1)*World.T-COVE_X;x++)if(WaterfallArt.wet(x,y))wet++;
                boolean painted=sx>=0&&sy>=0&&sx<image.getWidth()&&sy<image.getHeight()&&(image.getRGB(sx,sy)>>>24)>64;
                if(wet>=World.T*World.T*.24)world.tile[r][c]=World.WATER;
                else if(painted&&sy<WaterfallArt.FOOT-6)world.tile[r][c]=World.MOUNTAIN;
                else if(world.tile[r][c]==World.WATER||painted&&world.tile[r][c]==World.MOUNTAIN)world.tile[r][c]=World.GRASS;
            }
    }
    // [BLOCK: OUTLET COLLISION REGISTRATION] MountainRiver.registerMouth - Match tapered water to the prepared mask while retaining solid cliffs and the original pond.
    private void registerMouth(){
        for(int r=World.row(mouth.getMinY());r<World.row(mouth.getMaxY());r++)
            for(int c=World.col(mouth.getMinX());c<World.col(mouth.getMaxX());c++){
                if(!world.inside(c,r))continue;int wet=0;
                for(int y=r*World.T;y<(r+1)*World.T;y++)for(int x=c*World.T;x<(c+1)*World.T;x++)
                    if(WaterfallArt.mouthWet(x-(int)mouth.x,y-(int)mouth.y)||WaterfallArt.wet(x-COVE_X,y-COVE_Y))wet++;
                if(wet>=World.T*World.T*.24)world.tile[r][c]=World.WATER;
                else if(world.tile[r][c]==World.WATER)world.tile[r][c]=World.GRASS;
            }
    }
    // [BLOCK: RIVER CENTERLINE] MountainRiver.channel - Rasterize authored stream routes at a constant width matching the village river.
    private void channel(int[][] points){
        for(int i=1;i<points.length;i++){
            int x=points[i-1][0],y=points[i-1][1],tx=points[i][0],ty=points[i][1];
            int dx=Math.abs(tx-x),dy=-Math.abs(ty-y),sx=Integer.compare(tx,x),sy=Integer.compare(ty,y),error=dx+dy;
            while(true){
                int radius=1;
                for(int r=y-radius;r<=y+radius;r++)for(int c=x-radius;c<=x+radius;c++)
                    if(Math.hypot(c-x,r-y)<=radius+.4)water(c,r);
                if(x==tx&&y==ty)break;int twice=2*error;
                if(twice>=dy){error+=dy;x+=sx;}if(twice<=dx){error+=dx;y+=sy;}
            }
        }
    }
    // [BLOCK: HYDRAULIC / WALL SAFETY] MountainRiver.water - Let water pass under timber walls without creating another walkable entrance.
    private void water(int c,int r){
        if(!world.inside(c,r))return;
        int kind=world.tile[r][c];
        if(kind==World.WALL){world.tile[r][c]=World.CULVERT;return;}
        if(kind==World.CULVERT||kind==World.BRIDGE||kind==World.GATE)return;
        boolean bankJoin=(c==World.villageCell(2)||c==World.villageCell(World.VILLAGE_COLS-3))
                &&r>=World.villageCell(31)&&r<=World.villageCell(40);
        if(world.inVillage((c+.5)*World.T,(r+.5)*World.T)&&kind!=World.WATER&&!bankJoin)return;
        world.tile[r][c]=World.WATER;
    }
    // [BLOCK: SAFE CROSSING] MountainRiver.bridge - Build one three-cell-wide timber crossing only over exterior water.
    private void bridge(int center){
        for(int r=0;r<world.rows;r++)for(int c=center-1;c<=center+1;c++)
            if(world.tile[r][c]==World.WATER&&!world.inVillage((c+.5)*World.T,(r+.5)*World.T))world.tile[r][c]=World.BRIDGE;
    }
}
