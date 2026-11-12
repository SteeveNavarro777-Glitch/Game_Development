import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/** Complete shared art cache, reinstalled after legacy startup art is built. */
final class RpgArt {
    static final String ROOT="res/rpg-village/";
    private static volatile boolean loaded;
    private static BufferedImage[] houses,foliage,props;
    private static BufferedImage[][] terrain,npcs;
    private static Npc.Sprites[] providers;
    static BufferedImage[] waterFrames;
    private RpgArt(){}
    static boolean ready(){return loaded;}
    static synchronized void load()throws IOException{
        if(!loaded){
            BufferedImage[] h=new BufferedImage[8],f=new BufferedImage[6],p=new BufferedImage[12];
            BufferedImage[][] t=new BufferedImage[4][4],n=new BufferedImage[6][8];
            Npc.Sprites[] sprites=new Npc.Sprites[6];
            for(int i=0;i<4;i++)h[i]=read("house_"+i+".png",224,192,true);
            for(int i=4;i<8;i++){
                String path="res/rpg-village-local/house_"+i+".png";
                h[i]=new File(path).isFile()?readPath(path,224,192,true):fullSizeHouse(h[i-4]);
            }
            for(int i=0;i<6;i++)f[i]=read("foliage_"+i+".png",160,160,true);
            for(int i=0;i<12;i++)p[i]=read("prop_"+i+".png",160,144,true);
            for(int r=0;r<4;r++)for(int c=0;c<4;c++)t[r][c]=read("terrain_"+r+"_"+c+".png",32,32,false);
            for(int r=0;r<6;r++){
                for(int c=0;c<8;c++)n[r][c]=read("npc_"+r+"_"+c+".png",112,64,true);
                sprites[r]=new CachedNpc(n[r]);
            }
            if(VillageArt.houses.length!=8)throw new IOException("Eight house cache slots required");
            houses=h;foliage=f;props=p;terrain=t;npcs=n;providers=sprites;waterFrames=t[3].clone();loaded=true;
        }
        System.arraycopy(houses,0,VillageArt.houses,0,8);
        for(int i=0;i<8;i++)Assets.grass[i]=terrain[0][i%4];
        for(int i=0;i<3;i++){Assets.dirt[i]=terrain[1][i];VillageArt.paving[i]=terrain[2][i];}
        Assets.water=waterFrames.clone();RpgTerrain.load();
    }
    private static BufferedImage read(String name,int w,int h,boolean cutout)throws IOException{
        return readPath(ROOT+name,w,h,cutout);
    }
    private static BufferedImage fullSizeHouse(BufferedImage small){
        BufferedImage image=new BufferedImage(224,192,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();
        try{
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            // Optional creator houses use full-size colliders: keep custom alternatives at that scale.
            g.translate(112,191);g.scale(1/GameScale.TRADITIONAL_HOME,1/GameScale.TRADITIONAL_HOME);
            g.drawImage(small,-112,-191,null);
        }finally{g.dispose();}
        return image;
    }
    private static BufferedImage readPath(String path,int w,int h,boolean cutout)throws IOException{
        BufferedImage image;
        try(InputStream stream=RpgArt.class.getResourceAsStream("/"+path)){
            image=stream==null?ImageIO.read(new File(path)):ImageIO.read(stream);
        }
        if(image==null||image.getWidth()!=w||image.getHeight()!=h||!image.getColorModel().hasAlpha())
            throw new IOException("Expected "+w+"x"+h+" RGBA: "+path);
        boolean visible=false,transparent=false;
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            int a=image.getRGB(x,y)>>>24;if(a>0)visible=true;if(a==0)transparent=true;
        }
        if(!visible||(cutout&&!transparent))throw new IOException("Blank/opaque cutout: "+path);
        return image;
    }
    static BufferedImage house(int style){return houses[style];}
    static BufferedImage tree(int style){return foliage[Math.floorMod(style,4)];}
    static BufferedImage foliage(int style){return foliage[style];}
    static BufferedImage prop(int style){return props[style];}
    static BufferedImage terrain(int row,int col){return terrain[row][col];}
    static Npc.Sprites npc(int role){return providers[role];}
    static BufferedImage npcFrame(int role,int frame){return npcs[role][frame];}
    static BufferedImage[] npcFrames(int role){return npcs[role].clone();}
    private static final class CachedNpc implements Npc.Sprites{
        private final BufferedImage[] frames,left;
        CachedNpc(BufferedImage[] frames){this.frames=frames.clone();left=new BufferedImage[]{Assets.flip(frames[4]),Assets.flip(frames[5])};}
        public BufferedImage frame(Player.Dir dir,Npc.Action action,double time){
            int phase=Double.isFinite(time)&&time>0?(int)((long)Math.floor(time*(action==Npc.Action.WORK?3:6))&1):0;
            if(action==Npc.Action.WORK)return frames[6+phase];
            int step=action==Npc.Action.WALK?phase:0;
            return switch(dir){case DOWN->frames[step];case UP->frames[2+step];case RIGHT->frames[4+step];case LEFT->left[step];};
        }
    }
}
