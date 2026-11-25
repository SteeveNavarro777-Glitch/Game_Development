/* FILE PURPOSE: CACHED ITEM ART | Supplied potion icons/effect arrays and icons cropped from current Agyu equipment.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/** All decoding and gear icon preparation happen at startup, never during painting. */
final class PotionArt {
    static final int[] FRAME_COUNTS={6,8,8};
    private static final BufferedImage[] ICONS=new BufferedImage[Inventory.Item.values().length];
    // [SECTION: DSA - 2D ARRAY] Effect lookup uses potion kind then animation frame.
    private static final BufferedImage[][] EFFECTS=new BufferedImage[3][];
    // [BLOCK: STATIC ART] PotionArt.PotionArt - Prevent instances of the shared item cache.
    private PotionArt(){}
    // [BLOCK: CACHE STARTUP] PotionArt.load - Decode supplied runtime art and cache the current spear and wooden shield icons once.
    static void load()throws IOException {
        ICONS[Inventory.Item.HEALTH_POTION.ordinal()]=read("health",32,32);
        ICONS[Inventory.Item.ENERGY_POTION.ordinal()]=read("energy",32,32);
        ICONS[Inventory.Item.SHIELD_POTION.ordinal()]=read("shield",32,32);
        BufferedImage atlas=read("effects",256,96);
        for(int kind=0;kind<3;kind++){
            EFFECTS[kind]=new BufferedImage[FRAME_COUNTS[kind]];
            for(int frame=0;frame<EFFECTS[kind].length;frame++)EFFECTS[kind][frame]=atlas.getSubimage(frame*32,kind*32,32,32);
        }
        BufferedImage spear=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);Graphics2D g=spear.createGraphics();
        try{g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.translate(16,16);g.rotate(.55);g.drawImage(SpearAttack.icon(),-3,-15,6,30,null);
        }finally{g.dispose();}
        ICONS[Inventory.Item.SPEAR.ordinal()]=spear;
        BufferedImage shield=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);g=shield.createGraphics();
        try{g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(WoodenShieldArt.face(false),6,3,20,25,null);
        }finally{g.dispose();}
        ICONS[Inventory.Item.SHIELD.ordinal()]=shield;
    }
    // [BLOCK: VALIDATED ASSET IO] PotionArt.read - Require the packed item dimensions expected by the runtime cache.
    private static BufferedImage read(String name,int width,int height)throws IOException {
        BufferedImage image=ImageIO.read(new File("res/items/"+name+".png"));
        if(image==null||image.getWidth()!=width||image.getHeight()!=height)throw new IOException("Invalid packed potion art: "+name);
        return image;
    }
    // [BLOCK: ARRAY LOOKUP] PotionArt.icon - Read one cached equipment or potion icon by item type.
    static BufferedImage icon(Inventory.Item item){return item==null?null:ICONS[item.ordinal()];}
    // [BLOCK: ARRAY LOOKUP] PotionArt.frame - Read an indexed supplied animation frame without allocating an image.
    static BufferedImage frame(int kind,int frame){return EFFECTS[kind][frame];}
    // [BLOCK: ITEM INDEX] PotionArt.kind - Map the three consumables into compact effect array rows.
    static int kind(Inventory.Item item){return switch(item){case HEALTH_POTION->0;case ENERGY_POTION->1;case SHIELD_POTION->2;default->throw new IllegalArgumentException("Equipment is not a potion");};}
}
