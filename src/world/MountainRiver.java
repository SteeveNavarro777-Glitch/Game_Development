/* FILE PURPOSE: MOUNTAIN RIVER | Solid eastern highlands, animated waterfall and a continuous river through protected village culverts.
 * Course examples: cached arrays, immutable geometry and Drawable polymorphism; docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Terrain and artwork are prepared once; only simulation time controls water animation. */
final class MountainRiver implements Drawable {
    static final int CENTER_C=176,SOURCE_R=82;
    private static final BufferedImage[] ROCK=buildRock();
    private final World world;
    private final Rectangle2D.Double visual=new Rectangle2D.Double((CENTER_C+.5)*World.T-512,46*World.T,1024,1280);
    private record Flow(double x,double y,double dx,double dy) {}
    private final List<Flow> currents=new ArrayList<>();
    private double time;
    // [BLOCK: TERRAIN CONSTRUCTION] MountainRiver.MountainRiver - Raise eastern rock and link the waterfall basin to the existing river and western outflow.
    MountainRiver(World world,long seed){
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
        pool(CENTER_C,84,6,5);
        channel(new int[][]{{CENTER_C,SOURCE_R},{176,86},{166,90},{153,87},{141,82},{128,84},{115,84},{108,83}},seed);
        channel(new int[][]{{51,83},{39,84},{29,89},{16,90},{0,94}},seed^0x52L);
        // Existing city crossings stay untouched; these four forest bridges preserve the outer/inner trails.
        for(int c:new int[]{16,42,117,144})bridge(c);
        // A small timber jetty reaches the basin without making the waterfall/cliff walkable.
        for(int r=84;r<=91;r++)for(int c=CENTER_C-1;c<=CENTER_C+1;c++)
            if(world.tile[r][c]==World.WATER)world.tile[r][c]=World.BRIDGE;
    }
    // [BLOCK: ENCAPSULATION] MountainRiver.source - Return a copy of the waterfall basin's river source coordinate.
    Point source(){return new Point(CENTER_C,SOURCE_R);}
    // [BLOCK: ENCAPSULATION] MountainRiver.bounds - Return a copy of the full mountain artwork bounds.
    Rectangle2D bounds(){return (Rectangle2D)visual.clone();}
    // [BLOCK: SIMULATION CLOCK] MountainRiver.update - Advance animation only when the world simulation is active.
    void update(double dt){time+=Math.max(0,dt);}
    // [BLOCK: RENDER CULLING] MountainRiver.collect - Add the huge mountain only when its art intersects the camera.
    void collect(List<Drawable> target,int cx,int cy,int width,int height){if(visual.intersects(cx,cy,width,height))target.add(this);}
    // [BLOCK: SCENERY DEPTH] MountainRiver.sortY - Draw the solid cliff behind actors walking on its valid shore and viewing deck.
    public double sortY(){return visual.y-1;}
    // [BLOCK: SCENIC CAMERA] MountainRiver.lookAhead - Gently lift the camera beside the basin while keeping Agyu on screen.
    double lookAhead(double x,double y){
        double dx=Math.abs(x-(CENTER_C+.5)*World.T),dy=Math.abs(y-86*World.T);
        return 130*Math.max(0,Math.min(1,Math.min((8*World.T-dx)/(3*World.T),(10*World.T-dy)/(4*World.T))));
    }
    // [BLOCK: CACHED RENDER] MountainRiver.draw - Select prebuilt rock, falling-water and spray frames without mutating simulation.
    public void draw(Graphics2D target,int cx,int cy,double alpha){
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.drawImage(MountainArt.mountain(),(int)visual.x-cx,(int)visual.y-cy,null);
            int phase=(int)(time*6)%4,x=(int)((CENTER_C+.5)*World.T)-cx,base=SOURCE_R*World.T-cy;
            g.drawImage(MountainArt.waterfall(phase),x-80,base-448,null);
            g.drawImage(MountainArt.mist(phase),x-128,base-24,null);
        }finally{g.dispose();}
    }
    // [BLOCK: CACHED ROCK LOOKUP] MountainRiver.rock - Read a generated rock tile without creating images during rendering.
    static BufferedImage rock(int variant){return ROCK[Math.floorMod(variant,ROCK.length)];}
    // [BLOCK: CACHE CONSTRUCTION] MountainRiver.buildRock - Prepare muted rock grain beneath the cached mountain silhouette.
    private static BufferedImage[] buildRock(){
        BufferedImage[] out=new BufferedImage[4];
        for(int i=0;i<out.length;i++){
            out[i]=new BufferedImage(World.T,World.T,BufferedImage.TYPE_INT_ARGB);Graphics2D g=out[i].createGraphics();
            Random random=new Random(577+i);
            try{g.setColor(new Color(0x606957));g.fillRect(0,0,World.T,World.T);
                for(int n=0;n<120;n++){g.setColor(new Color(n%3==0?0x77806B:n%3==1?0x4F5B4D:0x64725C));g.fillRect(random.nextInt(32),random.nextInt(32),2,1);}
            }finally{g.dispose();}
        }return out;
    }
    // [BLOCK: SOURCE BASIN] MountainRiver.pool - Stamp a round connected pool under the falling water.
    private void pool(int c,int r,int rx,int ry){
        for(int y=r-ry;y<=r+ry;y++)for(int x=c-rx;x<=c+rx;x++)
            if(world.inside(x,y)&&Math.pow((x-c)/(rx+.4),2)+Math.pow((y-r)/(ry+.4),2)<=1)water(x,y);
    }
    // [BLOCK: RIVER CENTERLINE] MountainRiver.channel - Rasterize winding centerline segments with seeded bank-width variation.
    private void channel(int[][] points,long seed){
        Random random=new Random(seed);
        for(int i=1;i<points.length;i++){
            int x=points[i-1][0],y=points[i-1][1],tx=points[i][0],ty=points[i][1];
            double length=Math.hypot(tx-x,ty-y),vx=(tx-x)/Math.max(1,length),vy=(ty-y)/Math.max(1,length);
            int dx=Math.abs(tx-x),dy=-Math.abs(ty-y),sx=Integer.compare(tx,x),sy=Integer.compare(ty,y),error=dx+dy;
            while(true){
                int radius=2+(random.nextInt(5)==0?1:0);
                for(int r=y-radius;r<=y+radius;r++)for(int c=x-radius;c<=x+radius;c++)
                    if(Math.hypot(c-x,r-y)<=radius+.4)water(c,r);
                currents.add(new Flow((x+.5)*World.T,(y+.5)*World.T,vx,vy));
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
    // [BLOCK: RIVER MOTION] MountainRiver.drawCurrents - Animate small downstream streaks on visible open river cells using cached centerline directions.
    void drawCurrents(Graphics2D target,int cx,int cy,int width,int height){
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.setColor(new Color(193,247,237,125));int i=0;
            for(Flow flow:currents){double offset=(time*22+i++*7)%28;
                int x=(int)(flow.x+flow.dx*offset),y=(int)(flow.y+flow.dy*offset);
                if(x<cx||y<cy||x>=cx+width||y>=cy+height)continue;
                if(world.tile[World.row(y)][World.col(x)]!=World.WATER)continue;
                g.drawLine(x-cx,y-cy,(int)(x-flow.dx*5)-cx,(int)(y-flow.dy*5)-cy);
            }
        }finally{g.dispose();}
    }
}
