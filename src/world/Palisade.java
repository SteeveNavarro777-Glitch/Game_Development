/* FILE PURPOSE: VILLAGE DEFENSE | Cached wooden palisade pieces and four safe, operable gateways.
 * Course examples: arrays, cached lookup, inheritance and Drawable/Obstacle contracts.
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Border artwork is cached; only gate state and its short feedback timer change. */
final class Palisade {
    enum Side { NORTH, SOUTH, WEST, EAST }
    private static final Font PROMPT=new Font("Monospaced",Font.BOLD,9);
    private static final BufferedImage[] H_WALL=new BufferedImage[4],V_WALL=new BufferedImage[4];
    private static final BufferedImage H_CLOSED=gateArt(true,false),H_OPEN=gateArt(true,true);
    private static final BufferedImage V_CLOSED=gateArt(false,false),V_OPEN=gateArt(false,true);
    static { for(int i=0;i<4;i++){H_WALL[i]=wallArt(false,i);V_WALL[i]=wallArt(true,i);} }
    private final Gate[] gates;
    private final List<Gate> gateList;
    // [SECTION: DSA - 2D ARRAY] Constant-time gateway lookup for navigation-clearance rules.
    private final Gate[][] passages;
    private final List<Piece> pieces=new ArrayList<>();

    // [BLOCK: WORLD DEFENSE] Palisade.Palisade - Stamp protected edges and create four gateways aligned with district roads.
    Palisade(World world){
        int t=World.T,min=World.VILLAGE_MIN,right=min+World.VILLAGE_COLS-2,bottom=min+World.VILLAGE_ROWS-2;
        int left=min+1,top=min+1;
        passages=new Gate[world.rows][world.cols];
        for(int r=top;r<=bottom;r++)for(int c=left;c<=right;c++)
            if(r==top||r==bottom||c==left||c==right)world.tile[r][c]=World.WALL;
        gates=new Gate[]{
            new Gate(Side.NORTH,new Rectangle2D.Double((min+28)*t,min*t,3*t,2*t),new Rectangle2D.Double((min+28)*t,(min+2)*t-8,3*t,8)),
            new Gate(Side.SOUTH,new Rectangle2D.Double((min+28)*t,bottom*t,3*t,2*t),new Rectangle2D.Double((min+28)*t,(bottom+1)*t-8,3*t,8)),
            new Gate(Side.WEST,new Rectangle2D.Double(min*t,(min+23)*t,2*t,3*t),new Rectangle2D.Double((min+2)*t-16,(min+23)*t,8,3*t)),
            new Gate(Side.EAST,new Rectangle2D.Double(right*t,(min+23)*t,2*t,3*t),new Rectangle2D.Double(right*t+16,(min+23)*t,8,3*t))};
        gateList=List.of(gates);
        for(Gate gate:gates){
            for(int r=World.row(gate.passage.y);r<World.row(gate.passage.getMaxY());r++)
                for(int c=World.col(gate.passage.x);c<World.col(gate.passage.getMaxX());c++){
                    world.tile[r][c]=World.GATE;passages[r][c]=gate;
                }
            pieces.add(gate);
        }
        for(int c=left;c<=right;c++){
            if(passages[top][c]==null)pieces.add(new Wall(c*t,(top+1)*t-47,(top+1)*t,H_WALL[c%4]));
            if(passages[bottom][c]==null)pieces.add(new Wall(c*t,(bottom+1)*t-47,bottom*t,H_WALL[c%4]));
        }
        for(int r=top+1;r<bottom;r++){
            if(passages[r][left]==null)pieces.add(new Wall(left*t,(r+1)*t-47,(r+1)*t,V_WALL[r%4]));
            if(passages[r][right]==null)pieces.add(new Wall(right*t,(r+1)*t-47,(r+1)*t,V_WALL[r%4]));
        }
    }
    // [BLOCK: OOP - ENCAPSULATION] Palisade.gates - Expose the stable, unmodifiable four-gate list.
    List<Gate> gates(){return gateList;}
    // [BLOCK: DSA - ARRAY LOOKUP] Palisade.openPassageNear - Use precise center clearance only at an open gateway and its landing.
    boolean openPassageNear(int c,int r){
        for(int y=r-1;y<=r+1;y++)for(int x=c-1;x<=c+1;x++)
            if(y>=0&&x>=0&&y<passages.length&&x<passages[0].length&&passages[y][x]!=null&&passages[y][x].open)return true;
        return false;
    }
    // [BLOCK: RENDER CULLING] Palisade.collect - Append only boundary pieces intersecting the current viewport.
    void collect(List<Drawable> target,int cx,int cy,int width,int height){
        for(Piece piece:pieces)if(piece.visual.intersects(cx,cy,width,height))target.add(piece);
    }
    // [BLOCK: INTERACTION SEARCH] Palisade.nearest - Find a gateway close enough to operate from either side.
    Gate nearest(double x,double y){
        Gate best=null;double distance=64;
        for(Gate gate:gates){
            Rectangle2D b=gate.closed;
            double dx=Math.max(b.getMinX()-x,Math.max(0,x-b.getMaxX()));
            double dy=Math.max(b.getMinY()-y,Math.max(0,y-b.getMaxY()));
            double d=Math.hypot(dx,dy);if(d<distance){distance=d;best=gate;}
        }
        return best;
    }
    // [BLOCK: SIMULATION] Palisade.update - Advance gate feedback only during active simulation.
    void update(double dt){for(Gate gate:gates)gate.blockedTime=Math.max(0,gate.blockedTime-dt);}
    // [BLOCK: CONTEXT PROMPT] Palisade.drawPrompt - Show an E action only beside an operable gateway.
    void drawPrompt(Graphics2D target,Player player,int cx,int cy,int width,int height){
        if(!player.alive()||player.airborne()||player.dashing()||player.state==Player.State.ATTACK||player.state==Player.State.HURT)return;
        Gate gate=nearest(player.x,player.y);if(gate==null)return;
        String text=gate.blockedTime>0?"MOVE CLEAR OF THE GATE":"[E] "+(gate.open?"CLOSE ":"OPEN ")+gate.side+" GATE";
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.setFont(PROMPT);int tw=g.getFontMetrics().stringWidth(text);
            int x=Math.max(6,Math.min(width-tw-6,(int)gate.closed.getCenterX()-cx-tw/2));
            int y=Math.max(64,Math.min(height-10,(int)player.y-cy-61));
            g.setColor(new Color(22,30,23,230));g.fillRect(x-3,y-10,tw+6,14);
            g.setColor(new Color(gate.blockedTime>0?0xF0B47A:0xEEDAA2));g.drawString(text,x,y);
        }finally{g.dispose();}
    }
    // [SECTION: OOP - ABSTRACTION / POLYMORPHISM] Static wall pieces and gates share depth sorting and cached drawing.
    private abstract static class Piece implements Drawable {
        final Rectangle2D.Double visual;final double depth;
        // [BLOCK: CACHED SCENERY] Piece.Piece - Store immutable sprite placement and depth.
        Piece(Rectangle2D.Double visual,double depth){this.visual=visual;this.depth=depth;}
        // [BLOCK: OOP CONTRACT] Piece.image - Select the already cached sprite for this piece.
        abstract BufferedImage image();
        // [BLOCK: DEPTH SORTING] Piece.sortY - Return the ground-plane depth.
        public double sortY(){return depth;}
        // [BLOCK: CACHED RENDER] Piece.draw - Blit existing artwork without allocating images or changing state.
        public void draw(Graphics2D g,int cx,int cy,double alpha){g.drawImage(image(),(int)visual.x-cx,(int)visual.y-cy,null);}
    }
    private static final class Wall extends Piece {
        private final BufferedImage sprite;
        // [BLOCK: CACHED SCENERY] Wall.Wall - Bind one wooden segment to a shared texture.
        Wall(int x,int y,double depth,BufferedImage sprite){super(new Rectangle2D.Double(x,y,32,48),depth);this.sprite=sprite;}
        // [BLOCK: OOP OVERRIDE] Wall.image - Return this static segment's texture.
        BufferedImage image(){return sprite;}
    }
    static final class Gate extends Piece implements World.Obstacle {
        final Side side;final Rectangle2D.Double passage,closed;
        private final Rectangle2D.Double empty=new Rectangle2D.Double();
        private boolean open;private double blockedTime;
        // [BLOCK: GATE GEOMETRY] Gate.Gate - Match the blocking leaf to the visible opening, not the whole outside path.
        Gate(Side side,Rectangle2D.Double passage,Rectangle2D.Double closed){
            super(gateBounds(side,closed),side==Side.NORTH||side==Side.SOUTH?closed.getMaxY():closed.getCenterY());
            this.side=side;this.passage=passage;this.closed=closed;
        }
        // [BLOCK: OOP ENCAPSULATION] Gate.open - Read gate state without exposing a writable flag.
        boolean open(){return open;}
        // [BLOCK: GATE TRANSITION] Gate.toggle - Change the leaf state and clear short feedback.
        void toggle(){open=!open;blockedTime=0;}
        // [BLOCK: SAFE CLOSURE] Gate.occupied - Refuse closure while any living actor occupies the leaf's safety margin.
        boolean occupied(Player player,List<Enemy> enemies,List<Npc> npcs){
            Rectangle2D area=new Rectangle2D.Double(closed.x-2,closed.y-2,closed.width+4,closed.height+4);
            if(area.intersects(player.feetBox()))return true;
            for(Enemy enemy:enemies)if(enemy.alive()&&area.intersects(enemy.feetBox()))return true;
            for(Npc npc:npcs)if(area.intersects(npc.feetBox()))return true;
            return false;
        }
        // [BLOCK: GATE FEEDBACK] Gate.refuse - Keep the gate open and briefly explain an occupied threshold.
        void refuse(){blockedTime=1.2;}
        // [BLOCK: DYNAMIC OBSTACLE] Gate.collider - Reuse indexed leaf cells; an open gate has an empty collider.
        public Rectangle2D.Double collider(){return open?empty:closed;}
        // [BLOCK: OOP OVERRIDE] Gate.image - Select an orientation/state cache without repainting artwork.
        BufferedImage image(){return side==Side.NORTH||side==Side.SOUTH?(open?H_OPEN:H_CLOSED):(open?V_OPEN:V_CLOSED);}
    }
    // [BLOCK: SPRITE PLACEMENT] Palisade.gateBounds - Align gate artwork to the physical leaf and adjacent wall posts.
    private static Rectangle2D.Double gateBounds(Side side,Rectangle2D b){
        if(side==Side.NORTH||side==Side.SOUTH)return new Rectangle2D.Double(b.getX()-8,b.getMaxY()-63,112,64);
        return new Rectangle2D.Double(b.getCenterX()-24,b.getY()-8,48,112);
    }
    // [BLOCK: WOOD ART CACHE] Palisade.wallArt - Build textured pointed logs, tied rails and their shadows once.
    private static BufferedImage wallArt(boolean vertical,int variation){
        BufferedImage image=new BufferedImage(32,48,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{
            g.setColor(new Color(0,0,0,55));g.fillRect(vertical?7:0,39,vertical?21:32,8);
            if(vertical){
                timber(g,10,1,9,44,variation);timber(g,20,14,8,32,variation+1);
                color(g,0x684829);g.fillRect(8,10,18,4);color(g,0xB18A4F);g.fillRect(8,10,18,1);
                color(g,0x634328);g.fillRect(8,31,19,4);color(g,0xCCA264);g.fillRect(8,31,19,1);
            }else{
                for(int x=0;x<32;x+=8)timber(g,x,2+((x/8+variation)%3),8,43-((x/8+variation)%3),variation+x);
                color(g,0x4E3928);g.fillRect(0,20,32,5);g.fillRect(0,35,32,5);
                color(g,0xB78A4E);g.fillRect(0,20,32,2);g.fillRect(0,35,32,2);
                for(int x=3;x<32;x+=16){color(g,0xD0AF73);g.fillRect(x,18,2,9);g.fillRect(x,33,2,9);}
            }
        }finally{g.dispose();}return image;
    }
    // [BLOCK: WOOD DETAIL] Palisade.timber - Shade one pointed timber with grain, a knot and a dark base.
    private static void timber(Graphics2D g,int x,int y,int w,int h,int variation){
        color(g,0x4B3524);g.fillPolygon(new int[]{x,x+w/2,x+w-1,x+w-1,x},new int[]{y+5,y,y+5,y+h,y+h},5);
        color(g,(variation&1)==0?0x98703E:0xA17A45);g.fillRect(x+1,y+6,w-2,h-6);
        color(g,0xC69C5B);g.drawLine(x+2,y+5,x+2,y+h-3);
        color(g,0x72502E);g.drawLine(x+w-2,y+8,x+w-2,y+h-1);
        for(int yy=y+11;yy<y+h-3;yy+=11){color(g,0x795831);g.drawLine(x+w/2,yy,x+w/2,yy+5);}
        color(g,0x5A4028);g.fillRect(x+w/2,y+h/2,2,2);
    }
    // [BLOCK: GATE ART CACHE] Palisade.gateArt - Cache closed braced leaves and folded-open leaves in both orientations.
    private static BufferedImage gateArt(boolean horizontal,boolean open){
        BufferedImage image=new BufferedImage(horizontal?112:48,horizontal?64:112,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{
            if(horizontal){
                timber(g,0,5,8,57,1);timber(g,104,5,8,57,0);
                color(g,0x4C3525);g.fillRect(3,5,106,8);color(g,0xC29756);g.fillRect(4,5,104,3);
                if(open){timber(g,0,29,7,32,2);timber(g,105,29,7,32,3);}
                else{
                    for(int x=8;x<104;x+=8)timber(g,x,31,8,30,x/8);
                    color(g,0x4B3526);g.fillRect(8,42,96,4);g.fillRect(8,56,96,4);
                    color(g,0xC69A56);g.fillRect(8,42,96,1);g.fillRect(8,56,96,1);
                    color(g,0xBA9151);g.drawLine(10,54,52,45);g.drawLine(59,45,101,54);
                    color(g,0xE0B873);g.fillRect(51,47,3,5);g.fillRect(58,47,3,5);
                }
                color(g,0xD3AD68);g.fillRect(1,17,6,3);g.fillRect(105,17,6,3);
            }else{
                timber(g,16,0,16,14,1);timber(g,16,96,16,15,2);
                color(g,0xD0A76A);g.fillRect(16,7,16,2);g.fillRect(16,102,16,2);
                if(open){color(g,0x9B713E);g.fillRect(18,1,12,6);g.fillRect(18,105,12,6);}
                else{
                    color(g,0x4D3525);g.fillRect(18,8,12,96);
                    for(int y=9;y<104;y+=8){color(g,(y/8%2)==0?0xA17A45:0x8C643B);g.fillRect(19,y,10,7);color(g,0xC19A59);g.fillRect(19,y,10,1);}
                    color(g,0xD8B578);g.fillRect(28,52,3,7);color(g,0x5E442C);g.drawLine(20,10,27,53);g.drawLine(27,60,20,101);
                }
            }
        }finally{g.dispose();}return image;
    }
    // [BLOCK: PALETTE] Palisade.color - Select an opaque wood palette color while building caches.
    private static void color(Graphics2D g,int rgb){g.setColor(new Color(rgb));}
}
