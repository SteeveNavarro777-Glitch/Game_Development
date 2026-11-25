/* FILE PURPOSE: WATERFALL CACHE | Registered cove, cascade and river animation with a matching physical surface mask.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

/** Native pixel art is sliced once at startup; lookup and painting allocate no images. */
final class WaterfallArt {
    static final int WIDTH=512,HEIGHT=384,FRAMES=8,FALL_X=260,LIP=76,FOOT=171;
    private static BufferedImage[] coves,uppers,falls,mouths;
    private static BufferedImage[][] waters;
    private static BufferedImage surface,mountain,mouthSurface,banks;
    // [BLOCK: STATIC CACHE] WaterfallArt.WaterfallArt - Prevent instances of shared art.
    private WaterfallArt(){}
    // [BLOCK: ASSET STARTUP] WaterfallArt.load - Validate and slice the registered waterfall PNG caches once.
    static synchronized void load()throws IOException{
        if(coves!=null)return;
        BufferedImage cove=read("cove.png",WIDTH*FRAMES,HEIGHT),upper=read("upper.png",96*FRAMES,352),
            fall=read("fall.png",64*FRAMES,96),water=read("water.png",64*FRAMES,128),mask=read("surface.png",WIDTH,HEIGHT),mouth=read("mouth.png",192*FRAMES,288);
        BufferedImage[] c=new BufferedImage[FRAMES],u=new BufferedImage[FRAMES],f=new BufferedImage[FRAMES],m=new BufferedImage[FRAMES];
        BufferedImage[][] w=new BufferedImage[FRAMES][8];
        for(int i=0;i<FRAMES;i++){c[i]=cove.getSubimage(i*WIDTH,0,WIDTH,HEIGHT);u[i]=upper.getSubimage(i*96,0,96,352);
            f[i]=fall.getSubimage(i*64,0,64,96);m[i]=mouth.getSubimage(i*192,0,192,288);
            for(int v=0;v<8;v++)w[i][v]=water.getSubimage(i*64+v%2*32,v/2*32,32,32);}
        mountain=read("mountain.png",1024,1280);banks=read("banks.png",64,16);mouthSurface=read("mouth-surface.png",192,288);
        surface=mask;uppers=u;falls=f;waters=w;mouths=m;coves=c;
    }
    // [BLOCK: IMAGE VALIDATION] WaterfallArt.read - Load packaged resources or repository files with exact expected dimensions.
    private static BufferedImage read(String name,int width,int height)throws IOException{
        String path="res/waterfall/"+name;BufferedImage image;
        try(InputStream stream=WaterfallArt.class.getResourceAsStream("/"+path)){image=stream==null?ImageIO.read(new File(path)):ImageIO.read(stream);}
        if(image==null||image.getWidth()!=width||image.getHeight()!=height)throw new IOException("Invalid registered waterfall image: "+path);
        return image;
    }
    // [BLOCK: CACHE AVAILABILITY] WaterfallArt.ensure - Support world/map tools that request scenery before normal asset startup.
    private static void ensure(){if(coves==null)try{load();}catch(IOException error){throw new UncheckedIOException(error);}}
    // [BLOCK: MOUNTAIN CACHE] WaterfallArt.mountain - Reuse the detailed forest escarpment prepared at native pixel resolution.
    static BufferedImage mountain(){ensure();return mountain;}
    // [BLOCK: ANIMATION ARRAY] WaterfallArt.cove - Reuse one complete painted pond and lower cascade phase.
    static BufferedImage cove(int phase){ensure();return coves[Math.floorMod(phase,FRAMES)];}
    // [BLOCK: ANIMATION ARRAY] WaterfallArt.upper - Reuse the source-fed high mountain cascade.
    static BufferedImage upper(int phase){ensure();return uppers[Math.floorMod(phase,FRAMES)];}
    // [BLOCK: ANIMATION ARRAY] WaterfallArt.fall - Expose the registered lower falling-water source for validation and tooling.
    static BufferedImage fall(int phase){ensure();return falls[Math.floorMod(phase,FRAMES)];}
    // [BLOCK: ANIMATION ARRAY] WaterfallArt.water - Reuse original jade water texture throughout the river.
    static BufferedImage water(int phase){return water(phase,0);}
    // [BLOCK: WORLD TEXTURE VARIANTS] WaterfallArt.water - Reuse the matching tile within a seamless 64 by 128 river pattern.
    static BufferedImage water(int phase,int variant){ensure();return waters[Math.floorMod(phase,FRAMES)][Math.floorMod(variant,8)];}
    // [BLOCK: CACHED OUTLET] WaterfallArt.mouth - Reuse the tapered connector phase sharing the cove's native texture and banks.
    static BufferedImage mouth(int phase){ensure();return mouths[Math.floorMod(phase,FRAMES)];}
    // [BLOCK: PHYSICAL OUTLET MASK] WaterfallArt.mouthWet - Query water from the same connector mask used for painting.
    static boolean mouthWet(int x,int y){ensure();return x>=0&&y>=0&&x<192&&y<288&&(mouthSurface.getRGB(x,y)>>>24)>128;}
    // [BLOCK: NATIVE BANK STRIPS] WaterfallArt.bank - Read a source rock/moss shoreline pixel at a bounded distance from water.
    static int bank(int side,int along,int depth){ensure();return banks.getRGB(Math.floorMod(along,64),Math.floorMod(side,2)*8+Math.max(0,Math.min(7,depth)));}
    // [BLOCK: PHYSICAL SURFACE MASK] WaterfallArt.wet - Query authored water, including submerged lily-pad islands.
    static boolean wet(int x,int y){ensure();return x>=0&&y>=0&&x<WIDTH&&y<HEIGHT&&(surface.getRGB(x,y)>>>24)>128;}
}
