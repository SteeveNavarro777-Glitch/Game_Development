/* FILE PURPOSE: ASSET CACHE | Validate and cache the current village and NPC sprites.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

/** Current shared village art cache, loaded once and reinstalled on session setup. */
final class RpgArt {
    static final String ROOT="res/village/";
    private static volatile boolean loaded;
    private static BufferedImage[] houses,foliage,props;
    private static BufferedImage[][] terrain,npcs;
    private static Npc.Sprites[] providers;
    record TreeGround(int left,int top,int width,int height,int floor){}
    private static TreeGround[] treeGrounds;
    private static BufferedImage forestEdge;
    // [BLOCK: FUNCTION] RpgArt.RpgArt - Prevent instances of this static helper class.
    private RpgArt(){}
    // [BLOCK: ASSET STARTUP / ARRAYS] RpgArt.load - Validate and publish the complete current village sprite cache.
    static synchronized void load()throws IOException{
        if(!loaded){
            BufferedImage[] h=new BufferedImage[8],f=new BufferedImage[6],p=new BufferedImage[12];
            BufferedImage[][] t=new BufferedImage[4][4],n=new BufferedImage[6][8];
            Npc.Sprites[] sprites=new Npc.Sprites[6];
            for(int i=0;i<4;i++)h[i]=read("house_"+i+".png",224,192,true);
            for(int i=4;i<8;i++){
                String path="art/local-village/house_"+i+".png";
                h[i]=new File(path).isFile()?readPath(path,224,192,true):fullSizeHouse(h[i-4]);
            }
            for(int i=0;i<6;i++)f[i]=read("foliage_"+i+".png",160,160,true);
            for(int i=0;i<12;i++)if(i!=4)p[i]=read("prop_"+i+".png",160,144,true);
            for(int r=0;r<4;r++)for(int c=0;c<4;c++)t[r][c]=read("terrain_"+r+"_"+c+".png",32,32,false);
            for(int r=0;r<6;r++){
                for(int c=0;c<8;c++)n[r][c]=read("npc_"+r+"_"+c+".png",112,64,true);
                sprites[r]=new CachedNpc(n[r]);
            }
            if(VillageArt.houses.length!=8)throw new IOException("Eight house cache slots required");
            TreeGround[] roots=new TreeGround[4];
            for(int i=0;i<roots.length;i++)roots[i]=measureRoots(f[i]);
            houses=h;foliage=f;props=p;terrain=t;npcs=n;providers=sprites;treeGrounds=roots;forestEdge=buildForestEdge(f,t[0]);loaded=true;
        }
        System.arraycopy(houses,0,VillageArt.houses,0,8);
        for(int i=0;i<8;i++)Assets.grass[i]=terrain[0][i%4];
        RpgTerrain.load();
    }
    // [BLOCK: FUNCTION] RpgArt.read - Load or validate read data.
    private static BufferedImage read(String name,int w,int h,boolean cutout)throws IOException{
        return readPath(ROOT+name,w,h,cutout);
    }
    // [BLOCK: FUNCTION] RpgArt.fullSizeHouse - Handle full size house for RpgArt.
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
    // [BLOCK: FUNCTION] RpgArt.readPath - Load or validate read path data.
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
    // [BLOCK: CACHED OUTSKIRTS ART] RpgArt.buildForestEdge - Prepare a seamless forest canopy strip for the north-gate camera margin.
    private static BufferedImage buildForestEdge(BufferedImage[] trees,BufferedImage[] grass){
        BufferedImage image=new BufferedImage(256,64,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{
            for(int r=0;r<2;r++)for(int c=0;c<8;c++)g.drawImage(grass[(c+r)%4],c*32,r*32,null);
            for(int c=-1;c<=4;c++)g.drawImage(trees[Math.floorMod(c,4)],c*64-80,-18,null);
            g.setColor(new java.awt.Color(18,43,27,52));g.fillRect(0,0,256,64);
        }finally{g.dispose();}return image;
    }
    // [BLOCK: RENDER CACHE] RpgArt.forestEdge - Return the already generated north-edge canopy strip.
    static BufferedImage forestEdge(){return forestEdge;}
    // [BLOCK: ASSET GEOMETRY CACHE] RpgArt.measureRoots - Derive the solid ground footprint from opaque bark, excluding stray grass below a tree.
    private static TreeGround measureRoots(BufferedImage image)throws IOException{
        int bottom=-1,left=160,right=-1;
        for(int y=90;y<160;y++)for(int x=32;x<128;x++)if(wood(image.getRGB(x,y)))bottom=y;
        if(bottom<0)throw new IOException("Tree sprite has no identifiable bark roots");
        for(int y=Math.max(90,bottom-17);y<=bottom;y++)for(int x=24;x<136;x++)
            if(wood(image.getRGB(x,y))){left=Math.min(left,x);right=Math.max(right,x);}
        if(right<left)throw new IOException("Tree root footprint is empty");
        return new TreeGround(left-83,bottom-159-17,right-left+7,22,bottom-159);
    }
    // [BLOCK: SOURCE PIXEL CLASSIFICATION] RpgArt.wood - Recognize opaque warm bark without counting green ground tufts or leaves.
    private static boolean wood(int pixel){
        int r=(pixel>>>16)&255,g=(pixel>>>8)&255,b=pixel&255;
        return (pixel>>>24)>128&&r>60&&r>g*1.13&&g>b*1.12;
    }
    // [BLOCK: DSA - ARRAY LOOKUP] RpgArt.treeGround - Return one immutable, cached root-footprint template.
    static TreeGround treeGround(int style){return treeGrounds[Math.floorMod(style,4)];}
    // [BLOCK: FUNCTION] RpgArt.tree - Handle tree for RpgArt.
    static BufferedImage tree(int style){return foliage[Math.floorMod(style,4)];}
    // [BLOCK: FUNCTION] RpgArt.foliage - Handle foliage for RpgArt.
    static BufferedImage foliage(int style){return foliage[style];}
    // [BLOCK: FUNCTION] RpgArt.prop - Handle prop for RpgArt.
    static BufferedImage prop(int style){
        if(style<0||style>=props.length||props[style]==null)throw new IllegalArgumentException("Unused prop style: "+style);
        return props[style];
    }
    // [BLOCK: FUNCTION] RpgArt.terrain - Handle terrain for RpgArt.
    static BufferedImage terrain(int row,int col){return terrain[row][col];}
    // [BLOCK: FUNCTION] RpgArt.npc - Handle npc for RpgArt.
    static Npc.Sprites npc(int role){return providers[role];}
    // [BLOCK: FUNCTION] RpgArt.npcFrame - Handle npc frame for RpgArt.
    static BufferedImage npcFrame(int role,int frame){return npcs[role][frame];}
    private static final class CachedNpc implements Npc.Sprites{
        private final BufferedImage[] frames,left;
        // [BLOCK: FUNCTION] RpgArt.CachedNpc - Initialize CachedNpc state.
        CachedNpc(BufferedImage[] frames){this.frames=frames.clone();left=new BufferedImage[]{Assets.flip(frames[4]),Assets.flip(frames[5])};}
        // [BLOCK: FUNCTION] RpgArt.frame - Handle frame for RpgArt.
        public BufferedImage frame(Player.Dir dir,Npc.Action action,double time){
            int phase=Double.isFinite(time)&&time>0?(int)((long)Math.floor(time*(action==Npc.Action.WORK?3:6))&1):0;
            if(action==Npc.Action.WORK)return frames[6+phase];
            int step=action==Npc.Action.WALK?phase:0;
            return switch(dir){case DOWN->frames[step];case UP->frames[2+step];case RIGHT->frames[4+step];case LEFT->left[step];};
        }
    }
}
