import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Authored village districts, connected streets, and an indexed set of static colliders. */
public final class World {
    static final int T = Assets.TILE, GRASS = 0, DIRT = 1, WATER = 2, ROCK = 3, PAVING = 4, BRIDGE = 5;
    static final int[][] LANDMARKS = {{32,27}, {12,12}, {49,26}, {28,39}, {51,39}};
    static final double SPAWN_X = 32.5 * T, SPAWN_Y = 27.5 * T;
    private static final int[][] DIRECTIONS = {{0,-1},{0,1},{-1,0},{1,0}};
    // c/r are the feet of each house; generous front lanes keep every lot accessible.
    private static final int[][] LOTS = {
        {6,8,0},{22,9,4},{33,8,2},{45,9,5},{57,9,1},
        {6,19,6},{22,19,0},{33,20,3},{45,19,2},{57,20,7},
        {7,29,0},{20,30,2},{25,29,0},{39,29,4},{58,29,1},
        {7,41,2},{19,43,5},{36,43,0},{43,42,3},{59,43,6}
    };
    final int cols = 64, rows = 48;
    final int[][] tile = new int[rows][cols], variant = new int[rows][cols];
    final List<Tree> trees = new ArrayList<>();
    final List<Building> buildings = new ArrayList<>();
    final List<Prop> props = new ArrayList<>();
    private final List<Garden> gardens = new ArrayList<>();
    private interface Obstacle { Rectangle2D.Double collider(); }
    private final List<List<Obstacle>> buckets = new ArrayList<>();
    private final boolean[][] navigation = new boolean[rows][cols];
    long collisionChecks;
    private double ambientTime;
    void updateAmbient(double dt){ambientTime+=Math.max(0,dt);for(Prop prop:props)prop.time=ambientTime;}

    World(long seed) {
        Random random = new Random(seed);
        for (int i = 0; i < rows * cols; i++) buckets.add(new ArrayList<>());
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) variant[r][c] = random.nextInt(8);
        // A watercourse separates the lower gardens, with broad walkable timber bridges.
        for (int c = 3; c < cols - 3; c++) {
            int r = 33 + (int)Math.round(2*Math.sin(c*.15));
            tile[r][c] = tile[r+1][c] = tile[r+2][c] = WATER;
        }
        pond(13,36,6,4); pond(46,33,5,3);
        for (int r : new int[]{13,24,40,44}) road(4,r,59,r,PAVING);
        for (int c : new int[]{14,32,52}) road(c,4,c,44,PAVING);
        // Village square and the loop around the northern gathering house.
        rect(27,22,37,30,PAVING);
        road(27,16,38,16,PAVING); road(27,16,27,24,PAVING); road(38,16,38,24,PAVING);
        road(27,22,38,22,PAVING);
        for (int[] site : LANDMARKS) {
            road(32,27,site[0],27,PAVING); road(site[0],27,site[0],site[1],PAVING);
            rect(site[0]-3,site[1]-3,site[0]+3,site[1]+3,PAVING);
        }
        for (int[] lot : LOTS) {
            Building house = new Building((lot[0]+.5)*T,lot[1]*T,lot[2]);
            buildings.add(house);
            // Dry raised lots and a front approach; interiors are outside this release.
            rect(lot[0]-3,lot[1]-3,lot[0]+3,lot[1],GRASS);
            int front = Math.min(rows-4,lot[1]+1);
            int lane = front < 14 ? 13 : front < 25 ? 24 : front < 38 ? 40 : 44;
            road(lot[0],front,lot[0],lane,DIRT);
            if (lot[1] < 32) {
                Garden garden = new Garden((lot[0]-3)*T,(lot[1]+1)*T,2*T,2*T,lot[2]);
                if (!nearLandmark(garden.bounds,100)) {
                    gardens.add(garden);
                    rect(lot[0]-3,lot[1]+1,lot[0]-2,lot[1]+2,GRASS);
                }
            }
        }
        // Preserve broad combat courts and the full eight-direction dash space at HOME.
        for (int[] site : LANDMARKS) rect(site[0]-3,site[1]-3,site[0]+3,site[1]+3,PAVING);
        rect(28,23,36,31,PAVING);
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++)
            if (c < 2 || r < 2 || c >= cols-2 || r >= rows-2) tile[r][c] = ROCK;
        placeProp(0,22,31,92,28);placeProp(1,24,21,88,25);placeProp(2,35,21,66,20);
        placeProp(3,26,20,60,28);placeProp(5,31,16,12,10);placeProp(5,50,20,12,10);
        placeProp(6,4,8,30,12);placeProp(6,21,19,30,12);placeProp(6,56,20,30,12);
        placeProp(7,46,20,34,16);placeProp(8,31,21,48,12);placeProp(9,17,32,40,14);
        placeProp(10,22,32,28,12);placeProp(11,6,35,58,15);
        placeProp(12,18,11,16,12);placeProp(12,55,14,16,12);placeProp(13,40,15,18,10);
        int tries = 0;
        while (trees.size() < 85 && tries++ < 6000) {
            int c = 3+random.nextInt(cols-6), r = 4+random.nextInt(rows-6);
            if (tile[r][c] != GRASS) continue;
            double x = (c+.5)*T, y = (r+1.0)*T;
            boolean okay = true;
            for (int[] site : LANDMARKS)
                if (Math.hypot(x-(site[0]+.5)*T,y-(site[1]+.5)*T) < 170) okay = false;
            for (Tree tree : trees) if (Math.hypot(tree.x-x,tree.y-y) < 64) okay = false;
            Rectangle2D clearance = new Rectangle2D.Double(x-70,y-80,140,105);
            for (Building house : buildings) if (clearance.intersects(house.footprint)) okay = false;
            for (Garden garden : gardens) if (clearance.intersects(garden.bounds)) okay = false;
            for (Prop prop : props) if (clearance.intersects(prop.footprint)) okay = false;
            if (okay) trees.add(new Tree(x,y,random.nextDouble()<.25));
        }
        for (Tree tree : trees) index(tree);
        for (Building house : buildings) index(house);
        for (Prop prop : props) index(prop);
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++)
            navigation[r][c] = bridgeApproach(c,r)?
                    !blocked(new Rectangle2D.Double((c+.5)*T-10,(r+.5)*T-10,20,10)):
                    !blocked(new Rectangle2D.Double(c*T-10,r*T-10,T+20,T+10));
        collisionChecks = 0;
    }
    private boolean bridgeApproach(int c,int r){
        if(tile[r][c]==BRIDGE)return true;
        for(int[] d:DIRECTIONS)if(inside(c+d[0],r+d[1])&&tile[r+d[1]][c+d[0]]==BRIDGE)return true;
        return false;
    }
    private void placeProp(int style,int c,int r,int width,int height){
        Prop prop=new Prop((c+.5)*T,(r+.5)*T,style,width,height);
        if(nearLandmark(prop.footprint,145))return;
        for(int y=row(prop.footprint.getMinY());y<=row(Math.nextDown(prop.footprint.getMaxY()));y++)
            for(int x=col(prop.footprint.getMinX());x<=col(Math.nextDown(prop.footprint.getMaxX()));x++)
                if(solidTile(x,y))return;
        for(Building house:buildings)if(house.footprint.intersects(prop.footprint))return;
        for(Prop other:props)if(other.footprint.intersects(prop.footprint))return;
        props.add(prop);
    }
    private boolean nearLandmark(Rectangle2D box,double radius) {
        for (int[] site : LANDMARKS)
            if (box.intersects((site[0]+.5)*T-radius,(site[1]+.5)*T-radius,radius*2,radius*2)) return true;
        return false;
    }
    private void rect(int c0,int r0,int c1,int r1,int kind) {
        for (int r=Math.max(2,r0);r<=Math.min(rows-3,r1);r++)
            for (int c=Math.max(2,c0);c<=Math.min(cols-3,c1);c++) tile[r][c]=kind;
    }
    private void road(int c0,int r0,int c1,int r1,int kind) {
        int dc=Integer.compare(c1,c0),dr=Integer.compare(r1,r0),c=c0,r=r0;
        while (true) {
            boolean crossing=tile[r][c]==WATER;
            for(int y=r-1;y<=r+1;y++)for(int x=c-1;x<=c+1;x++)if(inside(x,y)){
                if(tile[y][x]==WATER){
                    if((dc==0&&x==c)||(dr==0&&y==r))tile[y][x]=BRIDGE;
                }else if(tile[y][x]!=BRIDGE)tile[y][x]=kind;
            }
            if(crossing)for(int sign:new int[]{-1,1}){
                int x=c+dc*sign,y=r+dr*sign;
                if(inside(x,y)&&tile[y][x]!=WATER&&tile[y][x]!=ROCK)tile[y][x]=BRIDGE;
            }
            if (c==c1&&r==r1) break;
            c+=dc;r+=dr;
        }
    }
    private void pond(double cx,double cy,double rx,double ry) {
        for (int r=2;r<rows-2;r++) for(int c=2;c<cols-2;c++) {
            double d=Math.pow((c-cx)/rx,2)+Math.pow((r-cy)/ry,2);
            if(d<1)tile[r][c]=WATER;else if(d<1.4&&tile[r][c]!=WATER)tile[r][c]=DIRT;
        }
    }
    private void index(Obstacle object) {
        Rectangle2D box=object.collider();
        for(int r=row(box.getMinY());r<=row(Math.nextDown(box.getMaxY()));r++)
            for(int c=col(box.getMinX());c<=col(Math.nextDown(box.getMaxX()));c++)
                if(inside(c,r))buckets.get(r*cols+c).add(object);
    }
    static int col(double x){return (int)Math.floor(x/T);}
    static int row(double y){return (int)Math.floor(y/T);}
    boolean inside(int c,int r){return c>=0&&r>=0&&c<cols&&r<rows;}
    private int at(int c,int r){return inside(c,r)?tile[r][c]:ROCK;}
    boolean solidTile(int c,int r){int t=at(c,r);return t==WATER||t==ROCK;}
    boolean walkable(int c,int r){return inside(c,r)&&navigation[r][c];}

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
    boolean blockedBruteForce(Rectangle2D box) {
        for(int r=row(box.getMinY());r<=row(Math.nextDown(box.getMaxY()));r++)
            for(int c=col(box.getMinX());c<=col(Math.nextDown(box.getMaxX()));c++)
                if(solidTile(c,r))return true;
        for(Tree tree:trees)if(tree.trunk.intersects(box))return true;
        for(Building house:buildings)if(house.footprint.intersects(box))return true;
        for(Prop prop:props)if(prop.footprint.intersects(box))return true;
        return false;
    }
    void drawGround(Graphics2D g,int cx,int cy,int vw,int vh,double time) {
        int c0=Math.max(0,Math.floorDiv(cx,T)),c1=Math.min(cols-1,Math.floorDiv(cx+vw,T));
        int r0=Math.max(0,Math.floorDiv(cy,T)),r1=Math.min(rows-1,Math.floorDiv(cy+vh,T));
        for(int r=r0;r<=r1;r++)for(int c=c0;c<=c1;c++){
            int kind=tile[r][c],v=variant[r][c],x=c*T-cx,y=r*T-cy;
            BufferedImage image=switch(kind){
                case DIRT,WATER,PAVING->RpgTerrain.tile(this,c,r,time);
                case ROCK->Assets.rock;case BRIDGE->VillageArt.bridge;
                default->Assets.grass[v];};
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
        for(Garden garden:gardens)if(garden.bounds.intersects(cx,cy,vw,vh))garden.draw(g,cx,cy);
        // A low, inset circular stone inlay anchors the open central court.
        int sx=(int)SPAWN_X-cx,sy=(int)SPAWN_Y-cy;
        g.setColor(new Color(0x8B8160));g.drawOval(sx-88,sy-60,176,112);g.drawOval(sx-83,sy-56,166,104);
        g.setColor(new Color(0xC8BA8D));g.drawOval(sx-85,sy-58,170,108);
        for(int[] site:LANDMARKS){
            int x=(int)((site[0]+.5)*T)-cx,y=(int)((site[1]+.5)*T)-cy;
            g.setColor(new Color(0x7E765A));g.drawRect(x-29,y-24,58,46);
            g.setColor(new Color(0xD3C595));g.drawRect(x-26,y-21,52,40);
        }
    }
    static final class Prop implements Drawable,Obstacle {
        final double x,y;final int style;final Rectangle2D.Double footprint;double time;
        Prop(double x,double y,int style,int width,int height){
            this.x=x;this.y=y;this.style=style;footprint=new Rectangle2D.Double(x-width/2.0,y-height,width,height);
        }
        public Rectangle2D.Double collider(){return footprint;}
        public double sortY(){return y;}
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
        Building(double x,double y,int style){
            this.x=x;this.y=y;this.style=style;
            double scale=style<4?GameScale.TRADITIONAL_HOME:1;
            footprint=new Rectangle2D.Double(x-73*scale,y-74*scale,146*scale,74*scale);
        }
        public Rectangle2D.Double collider(){return footprint;}
        public double sortY(){return y;}
        public void draw(Graphics2D g,int cx,int cy,double alpha){
            BufferedImage image=VillageArt.houses[style];
            int sx=(int)Math.round(x)-cx,sy=(int)Math.round(y)-cy;
            double scale=style<4?GameScale.TRADITIONAL_HOME:1;
            g.setColor(new Color(0,0,0,60));g.fillOval(sx-(int)Math.round(81*scale),sy-(int)Math.round(12*scale),(int)Math.round(162*scale),(int)Math.round(20*scale));
            g.drawImage(image,sx-image.getWidth()/2,sy-image.getHeight()+1,null);
        }
    }
    private static final class Garden {
        final Rectangle2D.Double bounds;
        final int kind;
        Garden(double x,double y,double width,double height,int kind){
            bounds=new Rectangle2D.Double(x,y,width,height);this.kind=kind;
        }
        void draw(Graphics2D g,int cx,int cy){
            int x=(int)bounds.x-cx,y=(int)bounds.y-cy,w=(int)bounds.width,h=(int)bounds.height;
            g.setColor(new Color(0x695334));g.fillRect(x,y,w,h);
            for(int row=4;row<h-5;row+=13){
                g.setColor(new Color(0x947245));g.fillRect(x+3,y+row,w-6,2);
                for(int col=8;col<w-5;col+=12){
                    g.setColor(new Color(kind%2==0?0x73964B:0xA1B463));
                    g.fillRect(x+col-2,y+row-3,5,3);g.fillRect(x+col,y+row-6,2,5);
                    g.setColor(new Color(0x355D35));g.fillRect(x+col-3,y+row-1,7,2);
                }
            }
            g.setColor(new Color(0x8D7140));g.fillRect(x-3,y+h-3,w+6,3);
            g.setColor(new Color(0xCFB578));g.fillRect(x-3,y+h-7,w+6,2);
            for(int col=0;col<=w;col+=16){
                g.setColor(new Color(0x4A4228));g.fillRect(x+col-2,y+h-13,3,15);
                g.setColor(new Color(0xC2A267));g.fillRect(x+col-2,y+h-13,1,13);
            }
        }
    }
    static final class Tree implements Drawable,Obstacle {
        final double x,y;
        private final boolean pink;
        final Rectangle2D.Double trunk;
        Tree(double x,double y,boolean pink){
            this.x=x;this.y=y;this.pink=pink;trunk=new Rectangle2D.Double(x-6,y-8,12,8);
        }
        public Rectangle2D.Double collider(){return trunk;}
        public double sortY(){return y;}
        public void draw(Graphics2D g,int cx,int cy,double alpha){
            g.setColor(new Color(0,0,0,55));g.fillOval((int)x-cx-15,(int)y-cy-5,30,9);
            BufferedImage image=RpgArt.tree(Math.floorMod((int)(x/T)+(int)(y/T)*3,4));
            g.drawImage(image,(int)x-cx-image.getWidth()/2,(int)y-cy-image.getHeight()+1,null);
        }
    }
}
