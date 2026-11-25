/* FILE PURPOSE: TERRAIN CACHE | Prebaked path/bank transitions selected by cached edge masks.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.image.BufferedImage;

/** Prebaked pixel transitions: irregular grassy path edges and stone/foam riverbanks. */
final class RpgTerrain {
    private static BufferedImage[][][] paths;
    private static BufferedImage[][][] water;
    // [BLOCK: FUNCTION] RpgTerrain.RpgTerrain - Prevent instances of this static helper class.
    private RpgTerrain(){}
    // [BLOCK: FUNCTION] RpgTerrain.load - Load or validate load data.
    static void load(){
        if(paths!=null)return;
        paths=new BufferedImage[2][4][16];water=new BufferedImage[WaterfallArt.FRAMES][8][16];
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
        for(int frame=0;frame<WaterfallArt.FRAMES;frame++)for(int v=0;v<8;v++)for(int mask=0;mask<16;mask++){
            BufferedImage out=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB),base=WaterfallArt.water(frame,v);
            for(int y=0;y<32;y++)for(int x=0;x<32;x++){
                int d=shoreDistance(mask,x,y),jag=Math.floorMod(x*7+y*11,2);
                int grass=RpgArt.terrain(0,v%4).getRGB(x,y),rgb=base.getRGB(x,y);
                if(d<1+jag)rgb=grass;
                else if(d<7+jag){
                    boolean horizontal=(mask&1)!=0&&y<=d+1||(mask&4)!=0&&31-y<=d+1;
                    int side=horizontal&&((mask&4)!=0&&31-y<=d+1)?1:0;
                    int along=horizontal?v%2*32+x:v/2*32+y;
                    int bank=WaterfallArt.bank(side,along,7+jag-d);rgb=(bank>>>24)>128?bank:grass;
                }else if(d==7+jag&&Math.floorMod(x+y+frame,4)!=0)rgb=0xFF8FDFC6;
                out.setRGB(x,y,rgb);
            }water[frame][v][mask]=out;
        }
    }
    // [BLOCK: ROUNDED SHORELINE] RpgTerrain.shoreDistance - Round exposed water corners using signed arc distance rather than square corner strips.
    private static int shoreDistance(int mask,int x,int y){
        int d=distance(mask,x,y),radius=12;
        if((mask&9)==9&&x<radius&&y<radius)d=Math.min(d,(int)Math.floor(radius-Math.hypot(radius-x,radius-y)));
        if((mask&3)==3&&x>31-radius&&y<radius)d=Math.min(d,(int)Math.floor(radius-Math.hypot(radius-(31-x),radius-y)));
        if((mask&6)==6&&x>31-radius&&y>31-radius)d=Math.min(d,(int)Math.floor(radius-Math.hypot(radius-(31-x),radius-(31-y))));
        if((mask&12)==12&&x<radius&&y>31-radius)d=Math.min(d,(int)Math.floor(radius-Math.hypot(radius-x,radius-(31-y))));
        return d;
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
        if(kind==World.WATER)return water[(int)(time*6)%WaterfallArt.FRAMES][Math.floorMod(c,2)+2*Math.floorMod(r,4)][mask];
        if(kind==World.DIRT)return paths[0][variant][mask];
        if(kind==World.PAVING)return paths[1][variant][mask];
        return Assets.grass[world.variant[r][c]];
    }
}
