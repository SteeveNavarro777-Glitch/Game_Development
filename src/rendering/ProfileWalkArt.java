/* FILE PURPOSE: PROFILE WALK ART | Isolate the original leg and boot pixels from Agyu's side walk sprites. */
import java.awt.image.BufferedImage;

/** Keeps the original stride, boot shape and foot height in each side-walk frame. */
final class ProfileWalkArt {
    static final int PHASES=8;
    static final int HIP_Y=52;
    private static final int CELL_SIZE=64,CENTER_X=32;
    private static final int FIT_TOP=49,BOOT_TOP=57;
    // The carried spear leans by a different amount in each source frame.
    private static final int[] SHAFT_START={44,44,42,42,41,42,44,41};

    // [BLOCK: ART CACHE UTILITY] ProfileWalkArt.ProfileWalkArt - Restrict construction to the profile animation cache.
    private ProfileWalkArt(){}

    // [BLOCK: SOURCE FOOTFALLS] ProfileWalkArt.steps - Fit the original walk to the standing boots without changing the ground anchor.
    static BufferedImage[] steps(BufferedImage[] originalWalk,BufferedImage standing){
        if(originalWalk.length!=PHASES)throw new IllegalArgumentException("Expected eight original side-walk frames");
        double bootScale=bootWidth(standing,CELL_SIZE)/(double)bootWidth(originalWalk[0],SHAFT_START[0]);
        BufferedImage[] result=new BufferedImage[PHASES];
        for(int phase=0;phase<PHASES;phase++){
            BufferedImage source=originalWalk[phase];
            int shaftStart=SHAFT_START[phase];
            int sole=groundedSole(source,shaftStart);
            BufferedImage legs=new BufferedImage(CELL_SIZE,CELL_SIZE,BufferedImage.TYPE_INT_ARGB);
            for(int y=HIP_Y;y<CELL_SIZE;y++){
                int sourceY=HIP_Y+(int)((y-FIT_TOP+0.5)*(sole-HIP_Y+1)/(CELL_SIZE-FIT_TOP));
                // Keep the top of the trousers fitted at the waist and widen toward the boots.
                double blend=Math.min(1,(y-HIP_Y)/(double)(BOOT_TOP-HIP_Y));
                double widthScale=1+(bootScale-1)*blend;
                for(int x=0;x<CELL_SIZE;x++){
                    int sourceX=(int)Math.floor(CENTER_X+(x+0.5-CENTER_X)/widthScale);
                    if(sourceX<0||sourceX>=shaftStart)continue;
                    int pixel=source.getRGB(sourceX,sourceY);
                    if((pixel>>>24)>=128)legs.setRGB(x,y,pixel|0xFF000000);
                }
            }
            result[phase]=legs;
        }
        return result;
    }

    // [BLOCK: GROUND REGISTRATION] ProfileWalkArt.groundedSole - Find the planted sole so the lifted foot keeps its clearance while the stride stays grounded.
    private static int groundedSole(BufferedImage image,int rightEdge){
        for(int y=CELL_SIZE-1;y>=HIP_Y;y--){
            for(int x=0;x<rightEdge;x++)if((image.getRGB(x,y)>>>24)>=128)return y;
        }
        throw new IllegalArgumentException("Missing profile footfall");
    }

    // [BLOCK: BOOT REGISTRATION] ProfileWalkArt.bootWidth - Measure the silhouette of the standing and starting walk frames.
    private static int bootWidth(BufferedImage image,int rightEdge){
        int left=rightEdge,right=-1;
        for(int y=BOOT_TOP;y<CELL_SIZE;y++){
            for(int x=0;x<rightEdge;x++){
                if((image.getRGB(x,y)>>>24)<128)continue;
                left=Math.min(left,x);
                right=Math.max(right,x);
            }
        }
        if(right<left)throw new IllegalArgumentException("Missing profile boot artwork");
        return right-left+1;
    }
}
