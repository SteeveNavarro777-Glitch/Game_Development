/* FILE PURPOSE: BOSS ANIMATION CACHE | Shared authored Tank, Mage and Fighter frames indexed by state, facing and phase.
 * Course examples: enums and multidimensional arrays; docs/CODE_GUIDE.md.
 */
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import javax.imageio.ImageIO;

/** Packed cells share atlas storage; frame selection never slices or rescales during gameplay. */
final class BossArt {
    enum Kind { TANK,MAGE,FIGHTER }
    enum Anim { IDLE,WALK,JUMP,HURT,DEATH,ATTACK,POWER,SKILL }
    static final int WIDTH=128,HEIGHT=128,ANCHOR_X=64,ANCHOR_Y=124;
    private static final int COLUMNS=8;
    private static volatile boolean loaded;
    // [SECTION: DSA - MULTIDIMENSIONAL ARRAYS] Kind, authored action, facing and frame form constant-time animation lookups.
    private static BufferedImage[][][][] frames;
    // [BLOCK: CACHE UTILITY] BossArt.BossArt - Prevent instances of the shared animation provider.
    private BossArt(){}
    // [BLOCK: AUTHORING METADATA] BossArt.count - Return the real number of source poses for a state.
    static int count(Kind kind,Anim animation){return switch(animation){case IDLE,WALK,JUMP->4;case HURT->3;case DEATH,POWER,SKILL->6;case ATTACK->kind==Kind.FIGHTER?4:5;};}
    // [BLOCK: ASSET STARTUP] BossArt.load - Validate and publish the complete three-atlas cache once.
    static synchronized void load()throws IOException{
        if(loaded)return;
        BufferedImage[][][][] prepared=new BufferedImage[Kind.values().length][Anim.values().length][][];
        for(Kind kind:Kind.values()){
            int size=0;for(Anim animation:Anim.values())size+=count(kind,animation);size*=Player.Dir.values().length;
            BufferedImage atlas=read(kind.name().toLowerCase(Locale.ROOT)+".png");
            if(atlas.getWidth()!=COLUMNS*WIDTH||atlas.getHeight()!=((size+COLUMNS-1)/COLUMNS)*HEIGHT||!atlas.getColorModel().hasAlpha())
                throw new IOException("Invalid packed boss atlas: "+kind);
            for(Anim animation:Anim.values())prepared[kind.ordinal()][animation.ordinal()]=new BufferedImage[Player.Dir.values().length][count(kind,animation)];
            int index=0;
            for(Player.Dir direction:Player.Dir.values())for(Anim animation:Anim.values())for(int frame=0;frame<count(kind,animation);frame++){
                prepared[kind.ordinal()][animation.ordinal()][direction.ordinal()][frame]=atlas.getSubimage((index%COLUMNS)*WIDTH,(index/COLUMNS)*HEIGHT,WIDTH,HEIGHT);index++;
            }
        }
        frames=prepared;loaded=true;
    }
    // [BLOCK: RESOURCE READING] BossArt.read - Load a packed atlas from a project/resource classpath with a useful startup error.
    private static BufferedImage read(String name)throws IOException{
        String path="res/bosses/"+name;BufferedImage result;
        try(InputStream stream=BossArt.class.getResourceAsStream("/"+path)){
            result=stream!=null?ImageIO.read(stream):ImageIO.read(new File(path));
        }
        if(result==null)throw new IOException("Unreadable boss artwork: "+path);return result;
    }
    // [BLOCK: CACHED FRAME LOOKUP] BossArt.frame - Return an existing authored pose without allocating images or advancing animation state.
    static BufferedImage frame(Kind kind,Anim animation,Player.Dir direction,int phase){
        if(!loaded)throw new IllegalStateException("BossArt.load must run before a boss is rendered");
        BufferedImage[] sequence=frames[kind.ordinal()][animation.ordinal()][direction.ordinal()];
        return sequence[Math.floorMod(phase,sequence.length)];
    }
}
