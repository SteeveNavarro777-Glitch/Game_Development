/* FILE PURPOSE: MOUNTAIN ART | Shared access to the detailed registered mountain and source-fed cascade.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.image.BufferedImage;

/** Detailed source artwork replaces the old procedural polygon mountain and waterfall. */
final class MountainArt {
    // [BLOCK: STATIC CACHE] MountainArt.MountainArt - Prevent instances of the shared scenery accessors.
    private MountainArt(){}
    // [BLOCK: REGISTERED MOUNTAIN] MountainArt.mountain - Reuse the prepared 1024 by 1280 mountain silhouette.
    static BufferedImage mountain(){return WaterfallArt.mountain();}
    // [BLOCK: CASCADE CACHE] MountainArt.waterfall - Reuse one animated upper waterfall phase from the original GIF.
    static BufferedImage waterfall(int phase){return WaterfallArt.upper(phase);}
}
