/* FILE PURPOSE: WILDLIFE ANIMATION ARRAYS | Cache supplied boar, wolf and Sigbin poses by species, action, direction and phase.
 * Course examples: enums, multidimensional arrays and shared resources; docs/CODE_GUIDE.md.
 */
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import javax.imageio.ImageIO;

/** Gameplay only selects existing images; importing, registration, scaling and mirroring happen beforehand. */
final class WildlifeArt {
    enum Kind { BOAR,WOLF,SIGBIN }
    enum Anim { WALK,ATTACK,LEAP,DEATH }
    static final int CELL=80,ANCHOR_X=40,ANCHOR_Y=72;
    private static final int COLUMNS=7;
    private static boolean loaded;
    private static BufferedImage scarePortrait;
    // [SECTION: DSA - MULTIDIMENSIONAL ARRAYS] Species, action, facing and phase form constant-time animation lookups.
    private static BufferedImage[][][][] frames,flashes;
    // [BLOCK: CACHE UTILITY] WildlifeArt.WildlifeArt - Prevent instances of the shared resource provider.
    private WildlifeArt(){}
    // [BLOCK: AUTHORED COUNTS] WildlifeArt.count - Preserve all four movement/attack/leap poses and five death poses.
    static int count(Anim animation){return animation==Anim.DEATH?5:4;}
    // [BLOCK: ASSET STARTUP] WildlifeArt.load - Prepare original, mirrored and flash variants once and publish complete animation caches.
    static synchronized void load()throws IOException{
        if(loaded)return;
        frames=new BufferedImage[Kind.values().length][Anim.values().length][Player.Dir.values().length][];
        flashes=new BufferedImage[Kind.values().length][Anim.values().length][Player.Dir.values().length][];
        for(Kind kind:Kind.values()){
            String path="res/wildlife/"+kind.name().toLowerCase(Locale.ROOT)+".png";BufferedImage atlas;
            try(InputStream stream=WildlifeArt.class.getResourceAsStream("/"+path)){atlas=stream==null?ImageIO.read(new File(path)):ImageIO.read(stream);}
            if(atlas==null||atlas.getWidth()!=CELL*COLUMNS||atlas.getHeight()!=CELL*(kind==Kind.WOLF?4:3)||!atlas.getColorModel().hasAlpha())
                throw new IOException("Invalid wildlife atlas: "+path+". Run tools.bat WildlifeSpriteImporter to regenerate it.");
            BufferedImage[][] sequences=new BufferedImage[kind==Kind.WOLF?6:5][];
            for(int row=0,index=0;row<sequences.length;row++){
                sequences[row]=new BufferedImage[row==4?5:4];
                for(int f=0;f<sequences[row].length;f++,index++)sequences[row][f]=atlas.getSubimage(index%COLUMNS*CELL,index/COLUMNS*CELL,CELL,CELL);
            }
            for(Anim animation:Anim.values())for(Player.Dir direction:Player.Dir.values()){
                int source=animation==Anim.DEATH?4:(animation==Anim.ATTACK||animation==Anim.LEAP)&&direction.ordinal()>=2?
                        (animation==Anim.LEAP&&kind==Kind.WOLF?5:3):direction==Player.Dir.DOWN?0:direction==Player.Dir.UP?1:2;
                BufferedImage[] sequence=sequences[source];int k=kind.ordinal(),a=animation.ordinal(),d=direction.ordinal();
                frames[k][a][d]=new BufferedImage[sequence.length];flashes[k][a][d]=new BufferedImage[sequence.length];
                for(int f=0;f<sequence.length;f++){
                    BufferedImage pose=direction==Player.Dir.LEFT?Assets.flip(sequence[f]):sequence[f];frames[k][a][d][f]=pose;
                    BufferedImage flash=new BufferedImage(CELL,CELL,BufferedImage.TYPE_INT_ARGB);
                    for(int y=0;y<CELL;y++)for(int x=0;x<CELL;x++){int alpha=pose.getRGB(x,y)>>>24;if(alpha>0)flash.setRGB(x,y,(alpha<<24)|0xFFFFFF);}
                    flashes[k][a][d][f]=flash;
                }
            }
        }
        String portraitPath="res/wildlife/sigbin-scare.png";
        try(InputStream stream=WildlifeArt.class.getResourceAsStream("/"+portraitPath)){scarePortrait=stream==null?ImageIO.read(new File(portraitPath)):ImageIO.read(stream);}
        if(scarePortrait==null||scarePortrait.getWidth()!=320||scarePortrait.getHeight()!=320||!scarePortrait.getColorModel().hasAlpha())throw new IOException("Invalid Sigbin portrait: "+portraitPath);
        loaded=true;
    }
    // [BLOCK: CACHED CLOSE-UP] WildlifeArt.scarePortrait - Return the detailed supplied front pose prepared specifically for the brief scare overlay.
    static BufferedImage scarePortrait(){if(!loaded)throw new IllegalStateException("WildlifeArt.load must run before rendering");return scarePortrait;}
    // [BLOCK: CACHED FRAME LOOKUP] WildlifeArt.frame - Select an existing transparent pose without allocating or advancing simulation.
    static BufferedImage frame(Kind kind,Anim action,Player.Dir direction,int phase,boolean flash){
        if(!loaded)throw new IllegalStateException("WildlifeArt.load must run before wildlife is rendered");
        BufferedImage[] sequence=(flash?flashes:frames)[kind.ordinal()][action.ordinal()][direction.ordinal()];
        return sequence[Math.floorMod(phase,sequence.length)];
    }
}
