/* FILE PURPOSE: TERRAIN CACHE | Prebaked path/bank transitions selected by cached edge masks.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.image.BufferedImage;

/** Prebaked pixel transitions: irregular grassy path edges and stone/foam riverbanks. */
final class RpgTerrain {
    private static final int[] BANK={0x9B825D,0xC5AF7E,0xAE9367,0x766D50};
    private static BufferedImage[][][] paths;
    private static BufferedImage[][] water;
    // [BLOCK: FUNCTION] RpgTerrain.RpgTerrain - Prevent instances of this static helper class.
    private RpgTerrain(){}
    // [BLOCK: FUNCTION] RpgTerrain.load - Load or validate load data.
    static void load(){
        if(paths!=null)return;
        paths=new BufferedImage[2][4][16];water=new BufferedImage[4][16];
        for(int type=0;type<2;type++)for(int v=0;v<4;v++)for(int mask=0;mask<16;mask++){
            BufferedImage out=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
            BufferedImage base=RpgArt.terrain(type+1,v),grass=RpgArt.terrain(0,v);
            for(int y=0;y<32;y++)for(int x=0;x<32;x++){
                int d=distance(mask,x,y),edge=1+Math.floorMod(x*31+y*19+v*13,4);
                int rgb=base.getRGB(x,y);
                if(d<edge)rgb=grass.getRGB(x,y);
                else if(d==edge&&Math.floorMod(x*11+y*7,3)==0)rgb=grass.getRGB(x,y);
                out.setRGB(x,y,rgb);
            }
            paths[type][v][mask]=out;
        }
        for(int frame=0;frame<4;frame++)for(int mask=0;mask<16;mask++){
            BufferedImage out=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<32;y++)for(int x=0;x<32;x++){
                int d=distance(mask,x,y),jag=Math.floorMod((x/4)*7+(y/4)*11,3);
                int rgb=RpgArt.terrain(3,frame).getRGB(x,y);
                if(d<2+jag)rgb=RpgArt.terrain(0,(x/8)%4).getRGB(x,y);
                else if(d<5+jag){
                    rgb=0xFF000000|BANK[Math.floorMod(x*3+y*5,4)];
                }else if(d==5+jag&&Math.floorMod(x+y+frame,3)!=0)rgb=0xFFA2E9E5;
                out.setRGB(x,y,rgb);
            }
            water[frame][mask]=out;
        }
    }
    // [BLOCK: FUNCTION] RpgTerrain.distance - Handle distance for RpgTerrain.
    private static int distance(int mask,int x,int y){
        int d=99;
        if((mask&1)!=0)d=Math.min(d,y);
        if((mask&2)!=0)d=Math.min(d,31-x);
        if((mask&4)!=0)d=Math.min(d,31-y);
        if((mask&8)!=0)d=Math.min(d,x);
        return d;
    }
    // [BLOCK: RENDER: ARRAY CACHE] RpgTerrain.tile - Select the prebuilt transition image from world edge masks.
    static BufferedImage tile(World world,int c,int r,double time){
        int kind=world.tile[r][c]==World.GATE?World.DIRT:world.tile[r][c]==World.CULVERT?World.WATER:world.tile[r][c],variant=world.variant[r][c]%4,mask=world.terrainEdges[r][c];
        if(kind==World.WATER)return water[(int)(time*6)%4][mask];
        if(kind==World.DIRT)return paths[0][variant][mask];
        if(kind==World.PAVING)return paths[1][variant][mask];
        return Assets.grass[world.variant[r][c]];
    }
}
