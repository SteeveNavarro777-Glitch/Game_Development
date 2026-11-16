/* FILE PURPOSE: VILLAGE ART | Current bridge images and shared house cache slots.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/** Current village bridge art and the eight shared house cache slots. */
final class VillageArt {
    static final BufferedImage[] houses=new BufferedImage[8];
    static BufferedImage bridge;
    // [BLOCK: FUNCTION] VillageArt.VillageArt - Prevent instances of this static helper class.
    private VillageArt(){}
    // [BLOCK: FUNCTION] VillageArt.load - Load or validate load data.
    static void load() throws IOException {
        bridge=tile();Graphics2D g=bridge.createGraphics();g.setColor(new Color(0x4B442C));g.fillRect(0,0,32,32);
        for(int y=0;y<32;y+=6){
            g.setColor(new Color(y%12==0?0x9F8151:0xA88B59));g.fillRect(0,y+1,32,5);
            g.setColor(new Color(0xC1A36B));g.drawLine(0,y+1,31,y+1);
            g.setColor(new Color(0x75613E));g.drawLine(7,y+3,21,y+3);
        }
        g.dispose();
        RpgArt.load();
    }
    // [BLOCK: FUNCTION] VillageArt.tile - Handle tile for VillageArt.
    private static BufferedImage tile(){return new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);}
}
