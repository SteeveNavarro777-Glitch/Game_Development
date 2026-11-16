/* FILE PURPOSE: FARM ART | Deterministic crop beds and an image cached per plot.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/** One cached decorative crop plot: raised beds, a walking aisle and an open bamboo gate. */
final class FarmPlot {
    final Rectangle2D.Double bounds;
    final int kind,gateCol,gateRow;
    final BufferedImage image;
    // [BLOCK: FUNCTION] FarmPlot.FarmPlot - Initialize FarmPlot state.
    FarmPlot(int c,int r,int cols,int rows,int kind,long seed){
        if(cols<3||rows<2||kind<0||kind>2)throw new IllegalArgumentException("Invalid farm plot");
        bounds=new Rectangle2D.Double(c*World.T,r*World.T,cols*World.T,rows*World.T);
        this.kind=kind;gateCol=c+cols/2;gateRow=r+rows;
        image=build(cols*World.T,rows*World.T,(gateCol-c)*World.T+World.T/2,seed);
    }
    // [BLOCK: RENDER: CACHE LOOKUP] FarmPlot.draw - Draw one existing farm image without rebuilding its artwork.
    void draw(Graphics2D g,int cx,int cy){g.drawImage(image,(int)bounds.x-cx,(int)bounds.y-cy,null);}
    // [BLOCK: CACHED FARM ART] FarmPlot.build - Generate textured beds, crops, aisle and open gate once for this plot.
    private BufferedImage build(int w,int h,int aisle,long seed){
        BufferedImage out=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=out.createGraphics();Random random=new Random(seed);
        try{
            // The edge is inset and uneven so the surrounding grass remains visible.
            Polygon soil=new Polygon(new int[]{7,w-10,w-5,w-7,w-12,9,4,5},
                    new int[]{7,5,12,h-11,h-5,h-6,h-14,14},8);
            color(g,0x6D593B);g.fillPolygon(soil);
            color(g,0x90704B);g.fillRoundRect(8,9,w-16,h-19,10,10);
            for(int i=0;i<w*h/3;i++){
                int x=7+random.nextInt(w-14),y=8+random.nextInt(h-16);
                if(!soil.contains(x,y))continue;
                color(g,switch(i%5){case 0->0xB08C5D;case 1->0x795E3E;case 2->0x9D7C50;case 3->0xA28053;default->0x876944;});
                g.fillRect(x,y,i%7==0?2:1,1);
            }
            // Raised horizontal planting beds have a lit ridge and a dark lower furrow.
            int spacing=kind==1?24:21;
            for(int row=23;row<h-13;row+=spacing){
                color(g,0x654E34);g.fillRoundRect(10,row-4,w-20,13,5,5);
                color(g,0x9B754B);g.fillRoundRect(9,row-6,w-18,11,5,5);
                color(g,0xBB945F);g.drawLine(13,row-6,w-14,row-6);
                color(g,0x80603E);g.drawLine(12,row+5,w-13,row+5);
                for(int x=15;x<w-12;x+=16){
                    if(Math.abs(x-aisle)<12)continue;
                    int growth=random.nextInt(3),base=row+1+random.nextInt(2);
                    crop(g,x,base,growth);
                }
            }
            // A light dirt aisle leads out through the open gate to the district street.
            color(g,0xA68658);g.fillRect(aisle-5,12,10,h-15);
            for(int y=15;y<h-4;y+=7){
                color(g,y%2==0?0xB99B6D:0x8C714B);g.fillRect(aisle-3+random.nextInt(6),y,2,1);
            }
            // Tiny stones and grass tufts soften the soil border without hiding the crops.
            for(int x=12;x<w-10;x+=17){
                color(g,0xB2A07B);g.fillRect(x,6+(x%3),3,2);
                color(g,0x716749);g.fillRect(x+1,8+(x%3),3,1);
            }
            for(int y=18;y<h-12;y+=18){
                tuft(g,3,y);tuft(g,w-5,y+3);
            }
            // Low side rails and short front sections leave a 28-pixel entrance.
            rail(g,5,11,5,h-10);rail(g,w-6,11,w-6,h-10);
            rail(g,7,h-8,aisle-15,h-8);rail(g,aisle+15,h-8,w-8,h-8);
            for(int x:new int[]{5,w-6}){post(g,x,11);post(g,x,h-7);}
            post(g,aisle-15,h-7);post(g,aisle+15,h-7);
        }finally{g.dispose();}
        return out;
    }
    // [BLOCK: FUNCTION] FarmPlot.crop - Handle crop for FarmPlot.
    private void crop(Graphics2D g,int x,int y,int growth){
        color(g,0x51482F);g.fillRect(x-5,y,11,3);g.fillRect(x-7,y+1,14,1);
        if(kind==1){
            int top=y-13-growth;
            color(g,0x385835);g.fillRect(x-1,top,3,y-top+1);
            color(g,0x83A448);g.fillRect(x,top+2,1,y-top-2);
            leaf(g,x-1,y-5,-7,-6,0x48763A,0x799948);
            leaf(g,x+1,y-7,7,-5,0x477038,0xA1B75A);
            leaf(g,x,y-2,6,-5,0x477438,0x89AA4D);
            color(g,0xB69A4D);g.fillRect(x-1,top-3,3,5);
            color(g,0xDDC270);g.fillRect(x,top-4,1,5);g.fillRect(x+2,top-2,1,2);
        }else if(kind==0){
            // Overlapping round leaves keep the vegetables organic at the native pixel scale.
            color(g,0x345B32);g.fillOval(x-7,y-6,14,8);
            color(g,0x537D3B);g.fillOval(x-7,y-7,8,6);g.fillOval(x,y-6,7,6);
            color(g,0x80A849);g.fillOval(x-4,y-10-growth,9,10);
            color(g,0xA8C365);g.fillOval(x-3,y-10-growth,5,4);
            color(g,0x638E3C);g.drawArc(x-4,y-9,9,7,200,160);
            color(g,0xBDD27A);g.fillRect(x-2,y-8-growth,2,1);g.drawLine(x,y-6,x-2,y-4);
            color(g,0x416A34);g.fillRect(x+2,y-4,3,2);
        }else{
            color(g,0xBA844D);g.fillRect(x-1,y-1,3,3);
            leaf(g,x,y-1,-6,-6,0x365F3C,0x78A055);
            leaf(g,x,y-2,5,-8-growth,0x3E7142,0x8DB363);
            leaf(g,x,y-1,7,-3,0x416C3B,0x6E974D);
            leaf(g,x,y,-4,-2,0x34583A,0x779C4B);
            color(g,0xB1BD6A);g.fillRect(x,y-5,1,4);
        }
    }
    // [BLOCK: FUNCTION] FarmPlot.leaf - Handle leaf for FarmPlot.
    private static void leaf(Graphics2D g,int x,int y,int dx,int dy,int dark,int light){
        color(g,dark);g.fillPolygon(new int[]{x,x+dx,x+dx/2,x},new int[]{y,y+dy,y+dy-2,y-2},4);
        color(g,light);g.drawLine(x,y-1,x+dx,y+dy);g.fillRect(x+dx-1,y+dy-1,3,2);
    }
    // [BLOCK: FUNCTION] FarmPlot.tuft - Handle tuft for FarmPlot.
    private static void tuft(Graphics2D g,int x,int y){
        color(g,0x4D7138);g.drawLine(x,y,x-2,y-3);g.drawLine(x,y,x+3,y-2);
        color(g,0x89A64C);g.fillRect(x,y-4,1,4);
    }
    // [BLOCK: FUNCTION] FarmPlot.rail - Handle rail for FarmPlot.
    private static void rail(Graphics2D g,int x0,int y0,int x1,int y1){
        if(x1<x0||y1<y0)return;
        color(g,0x625237);g.drawLine(x0+1,y0+1,x1+1,y1+1);
        color(g,0xAD8B52);g.drawLine(x0,y0,x1,y1);
        color(g,0xD1B57B);g.drawLine(x0,y0-1,x1,y1-1);
    }
    // [BLOCK: FUNCTION] FarmPlot.post - Handle post for FarmPlot.
    private static void post(Graphics2D g,int x,int y){
        color(g,0x5D4A31);g.fillRect(x-1,y-5,3,8);
        color(g,0xBB9B62);g.fillRect(x-1,y-5,2,7);
        color(g,0xE0C693);g.fillRect(x-1,y-5,2,1);g.fillRect(x-1,y-1,2,1);
    }
    // [BLOCK: FUNCTION] FarmPlot.color - Handle color for FarmPlot.
    private static void color(Graphics2D g,int rgb){g.setColor(new Color(rgb));}
}
