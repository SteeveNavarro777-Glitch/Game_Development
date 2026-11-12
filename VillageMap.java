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
        new Color(0x555547),new Color(0xC0B38B),new Color(0xB78B52)};
    private static final Color ROOF_EDGE=new Color(0x3D3325),ROOF_DARK=new Color(0x866337),ROOF_LIGHT=new Color(0xBE9A59);
    private static final Font LABEL_FONT=new Font("Monospaced",Font.BOLD,9);
    private static final BasicStroke OUTLINE=new BasicStroke(2),THIN=new BasicStroke(1);
    private static final Polygon SMALL=new Polygon(new int[]{0,3,0,-3},new int[]{-4,3,1,3},4);
    private static final Polygon LARGE=new Polygon(new int[]{0,5,0,-5},new int[]{-7,5,2,5},4);
    private static final String[] NAMES={"HOME","GROVE","LAKE","RIDGE","GUARDIAN"};
    private final World world;
    private final BufferedImage terrain=new BufferedImage(256,192,BufferedImage.TYPE_INT_RGB);
    VillageMap(World world){
        this.world=world;
        Graphics2D g=terrain.createGraphics();
        try{
            for(int r=0;r<world.rows;r++)for(int c=0;c<world.cols;c++){
                int kind=world.tile[r][c];g.setColor(GROUND[kind]);g.fillRect(c*4,r*4,4,4);
                if(kind==World.WATER){
                    g.setColor(new Color(0x82AFAB));
                    if(r>0&&world.tile[r-1][c]!=World.WATER)g.drawLine(c*4,r*4,c*4+3,r*4);
                    if(c>0&&world.tile[r][c-1]!=World.WATER)g.drawLine(c*4,r*4,c*4,r*4+3);
                }else if(kind==World.BRIDGE){
                    g.setColor(ROOF_DARK);g.drawLine(c*4,r*4,c*4+3,r*4);
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
        }finally{g.dispose();}
    }
    private int cacheX(double x){return (int)Math.round(x/(world.cols*World.T)*256);}
    private int cacheY(double y){return (int)Math.round(y/(world.rows*World.T)*192);}
    void draw(Graphics2D target,Player player,QuestLog quests,int x,int y,int width,int height,double alpha,boolean labels){
        if(width<=0||height<=0)return;
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.clipRect(x,y,width,height);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(terrain,x,y,width,height,null);
            boolean large=width>=256;g.setFont(LABEL_FONT);
            for(int i=0;i<World.LANDMARKS.length;i++){
                int[] site=World.LANDMARKS[i];
                int sx=x+(int)Math.round((site[0]+.5)/world.cols*width);
                int sy=y+(int)Math.round((site[1]+.5)/world.rows*height),radius=large?2:1;
                g.setColor(INK);g.fillRect(sx-radius-1,sy-radius-1,radius*2+3,radius*2+3);
                g.setColor(siteColor(i,quests));g.fillRect(sx-radius,sy-radius,radius*2+1,radius*2+1);
                if(labels&&large){
                    int tw=g.getFontMetrics().stringWidth(NAMES[i]);
                    int tx=Math.max(x+2,Math.min(x+width-tw-3,sx+6));
                    int ty=Math.max(y+10,Math.min(y+height-3,sy-5));
                    g.setColor(new Color(15,29,24,220));g.fillRect(tx-2,ty-9,tw+4,11);
                    g.setColor(new Color(0xF0EACF));g.drawString(NAMES[i],tx,ty);
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
    private static Color siteColor(int index,QuestLog quests){
        if(index==0)return new Color(0xACDBE6);
        if(index==4)return new Color(quests.done("guardian")?0xA5DD9C:0xEB9674);
        return new Color(quests.done(QuestLog.SHRINES[index-1])?0xA5DD9C:0xF2CA72);
    }
}
