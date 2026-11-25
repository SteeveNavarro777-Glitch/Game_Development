/* FILE PURPOSE: SHIELD ART | Native-size wooden shield faces shared by Agyu's gear poses and item icon. */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

/** The shield has its own transparent silhouette, wood grain and hardware. */
final class WoodenShieldArt {
    static final int WIDTH=16,HEIGHT=20;
    private static final BufferedImage FRONT=build(false),BACK=build(true);
    // [BLOCK: STATIC ART] WoodenShieldArt.WoodenShieldArt - Keep the shield images in one shared cache.
    private WoodenShieldArt(){}

    // [BLOCK: IMAGE CACHE] WoodenShieldArt.face - Return the shield face or the strapped reverse.
    static BufferedImage face(boolean reverse){
        return reverse?BACK:FRONT;
    }

    // [BLOCK: PIXEL SHIELD CONSTRUCTION] WoodenShieldArt.build - Draw a continuous rim around wood, a centered boss and leather straps.
    private static BufferedImage build(boolean reverse){
        BufferedImage image=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();
        try{
            Polygon rim=new Polygon(new int[]{5,10,13,15,15,13,10,5,2,0,0,2},
                    new int[]{0,0,2,6,13,17,19,19,17,13,6,2},12);
            g.setColor(new Color(0x281D18));
            g.fillPolygon(rim);
            g.setColor(new Color(0x95684B));
            g.fillPolygon(new int[]{5,10,13,14,14,12,9,5,2,1,1,3},
                    new int[]{1,1,3,6,13,16,18,18,16,13,6,3},12);
            Polygon wood=new Polygon(new int[]{5,10,12,13,13,11,9,5,3,2,2,4},
                    new int[]{3,3,4,7,12,15,17,17,15,12,7,4},12);
            g.setClip(wood);
            g.setColor(new Color(reverse?0x61412C:0x795237));
            g.fillRect(0,0,WIDTH,HEIGHT);
            g.setColor(new Color(0x503526));
            g.drawLine(5,2,5,18);
            g.drawLine(10,2,10,18);
            g.setColor(new Color(0x9E714A));
            g.drawLine(3,6,3,11);
            g.drawLine(7,4,7,7);
            g.drawLine(11,11,11,15);
            g.setClip(null);
            g.setColor(new Color(0xD2A376));
            g.drawLine(5,1,9,1);
            g.drawLine(2,4,2,5);
            g.setColor(new Color(0xB5936B));
            g.fillRect(7,3,1,1);
            g.fillRect(7,16,1,1);
            if(reverse){
                g.setColor(new Color(0x3C291E));
                g.fillRect(4,6,7,3);
                g.fillRect(4,11,7,3);
                g.setColor(new Color(0xBD8651));
                g.fillRect(5,6,5,1);
                g.fillRect(5,11,5,1);
            }else{
                g.setColor(new Color(0x3B2E25));
                g.fillRoundRect(4,6,8,8,6,6);
                g.setColor(new Color(0x82796A));
                g.fillRoundRect(5,7,6,6,5,5);
                g.setColor(new Color(0xCCC1A5));
                g.fillRect(7,8,2,2);
                g.fillRect(6,9,1,1);
            }
        }finally{g.dispose();}
        return image;
    }
}
