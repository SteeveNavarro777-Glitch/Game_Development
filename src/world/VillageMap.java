/* FILE PURPOSE: MAP UI | Cached terrain map and current player/quest markers.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/** Terrain is cached once per world; Agyu and quest markers remain live. */
final class VillageMap {
    private static final Color INK=new Color(0x152920),GREEN=new Color(0x7EFF75),LIGHT=new Color(0xD5FFBC);
    private static final Color[] GROUND={
        new Color(0x416C49),new Color(0xAD925F),new Color(0x3E8190),
        new Color(0x6A482C),new Color(0xC0B38B),new Color(0xB78B52),new Color(0xAD925F),new Color(0x67715C),new Color(0x3E8190)};
    private static final Color ROOF_EDGE=new Color(0x3D3325),ROOF_DARK=new Color(0x866337),ROOF_LIGHT=new Color(0xBE9A59);
    private static final Font LABEL_FONT=new Font("Monospaced",Font.BOLD,9);
    private static final BasicStroke OUTLINE=new BasicStroke(2),THIN=new BasicStroke(1);
    private static final Polygon SMALL=new Polygon(new int[]{0,3,0,-3},new int[]{-4,3,1,3},4);
    private static final Polygon LARGE=new Polygon(new int[]{0,5,0,-5},new int[]{-7,5,2,5},4);
    private final World world;
    private final BufferedImage terrain=new BufferedImage(256,192,BufferedImage.TYPE_INT_RGB);
    // [BLOCK: FUNCTION] VillageMap.VillageMap - Initialize VillageMap state.
    VillageMap(World world){
        this.world=world;
        Graphics2D g=terrain.createGraphics();
        try{
            for(int r=0;r<world.rows;r++)for(int c=0;c<world.cols;c++){
                int left=cacheX(c*World.T),top=cacheY(r*World.T),right=cacheX((c+1)*World.T),bottom=cacheY((r+1)*World.T);
                int kind=world.tile[r][c];g.setColor(GROUND[kind]);g.fillRect(left,top,right-left,bottom-top);
                if(kind==World.WATER||kind==World.CULVERT){
                    g.setColor(new Color(0x82AFAB));
                    if(r>0&&world.tile[r-1][c]!=World.WATER)g.drawLine(left,top,right-1,top);
                    if(c>0&&world.tile[r][c-1]!=World.WATER)g.drawLine(left,top,left,bottom-1);
                }else if(kind==World.BRIDGE){
                    g.setColor(ROOF_DARK);g.drawLine(left,top,right-1,top);
                }
            }
            for(World.Tree tree:world.trees){
                int x=cacheX(tree.x),y=cacheY(tree.y)-2;
                g.setColor(new Color(0x254B35));g.fillRect(x-2,y-2,5,5);
                g.setColor(new Color(0x57834B));g.fillRect(x-1,y-2,3,2);
            }
            for(World.Building house:world.buildings){
                Rectangle2D box=house.footprint;
                int left=cacheX(box.getMinX()),top=cacheY(box.getMinY());
                int width=Math.max(3,cacheX(box.getMaxX())-left),height=Math.max(3,cacheY(box.getMaxY())-top);
                g.setColor(ROOF_EDGE);g.fillRect(left-1,top-1,width+2,height+2);
                g.setColor(ROOF_DARK);g.fillRect(left,top,width,height);
                g.setColor(ROOF_LIGHT);g.fillRect(left,top,width,Math.max(1,height/2));
                g.setColor(ROOF_EDGE);g.drawLine(left,top+height/2,left+width-1,top+height/2);
            }
            for(ForestRegion.Arena arena:world.forest.arenas()){
                int left=cacheX((arena.c()-arena.rx())*World.T),top=cacheY((arena.r()-arena.ry())*World.T);
                int width=cacheX((arena.c()+arena.rx()+1)*World.T)-left,height=cacheY((arena.r()+arena.ry()+1)*World.T)-top;
                g.setColor(new Color(0x8D7651));g.drawOval(left,top,width,height);
            }
            Rectangle2D mountain=world.mountain.bounds();int mx=cacheX(mountain.getX()),my=cacheY(mountain.getY());
            g.drawImage(MountainArt.mountain(),mx,my,cacheX(mountain.getMaxX())-mx,cacheY(mountain.getMaxY())-my,null);
            int fx=cacheX((MountainRiver.CENTER_C+.5)*World.T),fy=cacheY(68*World.T);
            g.drawImage(MountainArt.waterfall(0),fx-3,fy,7,Math.max(1,cacheY(MountainRiver.SOURCE_R*World.T)-fy),null);
        }finally{g.dispose();}
    }
    // [BLOCK: FUNCTION] VillageMap.cacheX - Handle cache x for VillageMap.
    private int cacheX(double x){return (int)Math.round(x/(world.cols*World.T)*256);}
    // [BLOCK: FUNCTION] VillageMap.cacheY - Handle cache y for VillageMap.
    private int cacheY(double y){return (int)Math.round(y/(world.rows*World.T)*192);}
    // [BLOCK: FUNCTION] VillageMap.draw - Render draw using the current graphics state.
    void draw(Graphics2D target,Player player,QuestLog quests,int x,int y,int width,int height,double alpha,boolean labels){
        if(width<=0||height<=0)return;
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.clipRect(x,y,width,height);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(terrain,x,y,width,height,null);
            boolean large=width>=256;g.setFont(LABEL_FONT);
            for(ForestRegion.Arena arena:world.forest.arenas()){
                String id=QuestLog.BOSSES[arena.id()-1];
                int ax=x+(int)Math.round((arena.c()+.5)/world.cols*width),ay=y+(int)Math.round((arena.r()+.5)/world.rows*height);
                g.setColor(INK);g.fillOval(ax-3,ay-3,7,7);
                g.setColor(new Color(quests.done(id)?0x8DC779:0xDF7965));g.fillOval(ax-2,ay-2,5,5);
                if(labels&&large)label(g,arena.id()+" "+id.toUpperCase(java.util.Locale.ROOT)+(quests.done(id)?" [X]":""),ax,ay,x,y,width,height);
            }
            if(labels&&large){
                label(g,"MOUNTAIN",x+(int)Math.round(176.5/world.cols*width),y+(int)Math.round(51.0/world.rows*height),x,y,width,height);
                label(g,"FALLS",x+(int)Math.round(176.5/world.cols*width),y+(int)Math.round(84.0/world.rows*height),x,y,width,height);
            }
            for(Palisade.Gate gate:world.palisade.gates()){
                Rectangle2D box=gate.closed;
                int gx=x+(int)Math.round(box.getCenterX()/(world.cols*World.T)*width);
                int gy=y+(int)Math.round(box.getCenterY()/(world.rows*World.T)*height);
                boolean horizontal=gate.side==Palisade.Side.NORTH||gate.side==Palisade.Side.SOUTH;
                int span=large?5:2;
                g.setColor(new Color(0x2D291C));g.fillRect(gx-(horizontal?span:1)-1,gy-(horizontal?1:span)-1,horizontal?span*2+3:5,horizontal?5:span*2+3);
                g.setColor(new Color(gate.open()?0x8DAF71:0xD2A05C));
                if(horizontal){g.fillRect(gx-span,gy-1,span*2+1,3);if(gate.open()){g.setColor(GROUND[1]);g.fillRect(gx,gy-1,1,3);}}
                else{g.fillRect(gx-1,gy-span,3,span*2+1);if(gate.open()){g.setColor(GROUND[1]);g.fillRect(gx-1,gy,3,1);}}
                if(labels&&large){
                    String text=gate.side.name().substring(0,1)+" GATE";int tw=g.getFontMetrics().stringWidth(text);
                    int tx=Math.max(x+2,Math.min(x+width-tw-2,gx-tw/2));int ty=Math.max(y+10,Math.min(y+height-3,gy+(gate.side==Palisade.Side.NORTH?13:-7)));
                    g.setColor(INK);g.fillRect(tx-1,ty-8,tw+2,10);g.setColor(new Color(0xE7CF9C));g.drawString(text,tx,ty);
                }
            }
            double blend=Math.max(0,Math.min(1,alpha));
            double feetX=player.px+(player.x-player.px)*blend,feetY=player.py+(player.y-player.py)*blend;
            g.translate(Math.round(x+feetX/(world.cols*World.T)*width),Math.round(y+feetY/(world.rows*World.T)*height));
            g.rotate(switch(player.dir){case UP->0;case RIGHT->Math.PI/2;case DOWN->Math.PI;case LEFT->-Math.PI/2;});
            Polygon arrow=large?LARGE:SMALL;
            g.setStroke(OUTLINE);g.setColor(INK);g.drawPolygon(arrow);
            g.setColor(GREEN);g.fillPolygon(arrow);g.setStroke(THIN);g.setColor(LIGHT);
            g.drawLine(0,large?-4:-2,0,0);
        }finally{g.dispose();}
    }
    // [BLOCK: MAP WAYFINDING] VillageMap.label - Keep remote clearing and mountain labels inside the full-map viewport.
    private void label(Graphics2D g,String text,int sx,int sy,int x,int y,int width,int height){
        int tw=g.getFontMetrics().stringWidth(text),tx=Math.max(x+2,Math.min(x+width-tw-2,sx-tw/2));
        int ty=Math.max(y+10,Math.min(y+height-3,sy-4));
        g.setColor(INK);g.fillRect(tx-1,ty-8,tw+2,10);g.setColor(new Color(0xE7CF9C));g.drawString(text,tx,ty);
    }
}
