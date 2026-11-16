/* FILE PURPOSE: IMPORT TOOL | Pack current village atlases and keep optional creator house outputs local.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

/** Reproducible rectangular asset packing: cached PNGs, preserved alpha, nearest-neighbor scale. */
public final class RpgAssetImporter {
    private record Box(int x,int y,int width,int height){}
    private record Part(Box box,int area){}
    private static final File ROOT=new File("res/village");
    private static final File LOCAL=new File("art/local-village");
    private static final StringBuilder manifest=new StringBuilder("file,source,x,y,width,height,scale,anchor_x,anchor_y\n");
    // [BLOCK: ART TOOL] RpgAssetImporter.RpgAssetImporter - Prevent instances of this static helper class.
    private RpgAssetImporter(){}
    // [BLOCK: ART TOOL] RpgAssetImporter.main - Entry point for this developer tool.
    public static void main(String[] args)throws Exception{
        ROOT.mkdirs();
        terrain();foliage();houses();npcs();props();
        Files.writeString(new File(ROOT,"frames.csv").toPath(),manifest,StandardCharsets.UTF_8);
        System.out.println("Packed custom village art; creator house overrides are optional and stay local.");
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.read - Load or validate read data.
    private static BufferedImage read(String name)throws Exception{
        BufferedImage image=ImageIO.read(new File("art/source/village",name));
        if(image==null)throw new IllegalArgumentException("Unreadable source "+name);return image;
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.write - Handle write for RpgAssetImporter.
    private static void write(BufferedImage image,String name)throws Exception{
        if(!ImageIO.write(image,"png",new File(ROOT,name)))throw new IllegalStateException("PNG writer missing");
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.cell - Handle cell for RpgAssetImporter.
    private static Box cell(BufferedImage image,int col,int row,int cols,int rows){
        int x=col*image.getWidth()/cols,y=row*image.getHeight()/rows;
        return new Box(x,y,(col+1)*image.getWidth()/cols-x,(row+1)*image.getHeight()/rows-y);
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.bounds - Handle bounds for RpgAssetImporter.
    private static Box bounds(BufferedImage image,Box region){
        int l=region.x+region.width,t=region.y+region.height,r=-1,b=-1;
        for(int y=region.y;y<region.y+region.height;y++)for(int x=region.x;x<region.x+region.width;x++){
            if((image.getRGB(x,y)>>>24)<24)continue;
            l=Math.min(l,x);t=Math.min(t,y);r=Math.max(r,x);b=Math.max(b,y);
        }
        if(r<l||b<t)throw new IllegalArgumentException("Blank sprite cell at "+region);
        return new Box(l,t,r-l+1,b-t+1);
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.scale - Handle scale for RpgAssetImporter.
    private static BufferedImage scale(BufferedImage sheet,Box b,int width,int height){
        BufferedImage out=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=out.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(sheet,0,0,width,height,b.x,b.y,b.x+b.width,b.y+b.height,null);g.dispose();return out;
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.pack - Handle pack for RpgAssetImporter.
    private static void pack(BufferedImage sheet,Box box,String name,String source,double factor,
            int width,int height,double rootX,double rootY)throws Exception{
        BufferedImage out=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=out.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int dw=Math.max(1,(int)Math.round(box.width*factor)),dh=Math.max(1,(int)Math.round(box.height*factor));
        int dx=(int)Math.round(width/2.0+(box.x-rootX)*factor);
        // Lock bottom-anchored art to the last canvas row after rounding its height.
        int dy=Math.abs(rootY-(box.y+box.height-1))<1e-9?height-dh:
                (int)Math.round(height-1+(box.y-rootY)*factor);
        if(dx<0||dy<0||dx+dw>width||dy+dh>height)throw new IllegalStateException("Sprite clipped: "+name+" "+dx+","+dy+" "+dw+"x"+dh);
        g.drawImage(sheet,dx,dy,dx+dw,dy+dh,box.x,box.y,box.x+box.width,box.y+box.height,null);g.dispose();write(out,name);
        manifest.append(name).append(',').append(source).append(',').append(box.x).append(',').append(box.y)
            .append(',').append(box.width).append(',').append(box.height).append(',').append(factor)
            .append(',').append(rootX).append(',').append(rootY).append('\n');
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.terrain - Handle terrain for RpgAssetImporter.
    private static void terrain()throws Exception{
        BufferedImage sheet=read("terrain.png");
        for(int row=0;row<4;row++)for(int col=0;col<4;col++){
            Box box=cell(sheet,col,row,4,4);String file="terrain_"+row+"_"+col+".png";
            write(scale(sheet,box,32,32),file);
            manifest.append(file).append(",terrain.png,").append(box.x).append(',').append(box.y).append(',').append(box.width).append(',').append(box.height).append(",32px,0,0\n");
        }
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.foliage - Handle foliage for RpgAssetImporter.
    private static void foliage()throws Exception{
        BufferedImage sheet=read("foliage.png");Box[] boxes=new Box[6];int maxH=0,maxW=0;
        for(int i=0;i<6;i++){boxes[i]=bounds(sheet,cell(sheet,i%3,i/3,3,2));maxH=Math.max(maxH,boxes[i].height);maxW=Math.max(maxW,boxes[i].width);}
        double factor=Math.min(144.0/maxW,144.0/maxH);
        for(int i=0;i<6;i++){Box b=boxes[i];pack(sheet,b,"foliage_"+i+".png","foliage.png",factor,160,160,b.x+(b.width-1)/2.0,b.y+b.height-1);}
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.components - Handle components for RpgAssetImporter.
    private static Box[] components(BufferedImage image,int required){
        int w=image.getWidth(),h=image.getHeight();boolean[] seen=new boolean[w*h];int[] queue=new int[w*h];List<Part> parts=new ArrayList<>();
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            int start=y*w+x;if(seen[start]||(image.getRGB(x,y)>>>24)<32)continue;
            int first=0,last=0,l=x,t=y,r=x,b=y;queue[last++]=start;seen[start]=true;
            while(first<last){
                int index=queue[first++],px=index%w,py=index/w;l=Math.min(l,px);t=Math.min(t,py);r=Math.max(r,px);b=Math.max(b,py);
                for(int d=0;d<4;d++){
                    int nx=px+(d==0?-1:d==1?1:0),ny=py+(d==2?-1:d==3?1:0);
                    if(nx<0||ny<0||nx>=w||ny>=h)continue;int next=ny*w+nx;
                    if(!seen[next]&&(image.getRGB(nx,ny)>>>24)>=32){seen[next]=true;queue[last++]=next;}
                }
            }
            if(last>5000)parts.add(new Part(new Box(l,t,r-l+1,b-t+1),last));
        }
        parts.sort(Comparator.comparingInt(Part::area).reversed());
        if(parts.size()<required)throw new IllegalArgumentException("Atlas has fewer separate buildings than expected");
        Box[] result=new Box[required];
        for(int i=0;i<required;i++){
            Box b=parts.get(i).box;int slot=((b.y+b.height/2)<h/2?0:2)+((b.x+b.width/2)<w/2?0:1);
            if(result[slot]!=null)throw new IllegalArgumentException("Ambiguous house quadrant");result[slot]=b;
        }
        return result;
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.houses - Handle houses for RpgAssetImporter.
    private static void houses()throws Exception{
        BufferedImage custom=read("houses-custom.png");Box[] boxes=components(custom,4);int maxH=0,maxW=0;
        for(Box b:boxes){maxH=Math.max(maxH,b.height);maxW=Math.max(maxW,b.width);}
        double factor=Math.min(210.0/maxW,184.0/maxH)*GameScale.TRADITIONAL_HOME;
        for(int i=0;i<4;i++){Box b=boxes[i];pack(custom,b,"house_"+i+".png","houses-custom.png",factor,224,192,b.x+(b.width-1)/2.0,b.y+b.height-1);}
        File originalFile=new File(LOCAL,"source/Free Premade houses.png");
        if(!originalFile.isFile()){
            System.out.println("Optional free house sheet absent; the game uses custom house alternatives.");
            return;
        }
        LOCAL.mkdirs();
        BufferedImage original=ImageIO.read(originalFile);
        if(original==null)throw new IllegalArgumentException("Unreadable optional house sheet");
        Box[] regions={new Box(0,0,480,384),new Box(480,0,384,336),new Box(1024,0,352,320),new Box(1408,288,480,352)};
        for(int i=0;i<4;i++){
            Box b=bounds(original,regions[i]);factor=Math.min(212.0/b.width,182.0/b.height);
            pack(original,b,"../../art/local-village/house_"+(i+4)+".png","Free Premade houses.png",factor,224,192,b.x+(b.width-1)/2.0,b.y+b.height-1);
        }
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.footX - Handle foot x for RpgAssetImporter.
    private static double footX(BufferedImage image,Box box){
        long sum=0;int count=0;
        for(int y=box.y+box.height-3;y<box.y+box.height;y++)for(int x=box.x;x<box.x+box.width;x++)
            if((image.getRGB(x,y)>>>24)>=128){sum+=x;count++;}
        return count==0?box.x+(box.width-1)/2.0:sum/(double)count;
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.npcs - Handle npcs for RpgAssetImporter.
    private static void npcs()throws Exception{
        BufferedImage sheet=read("npcs.png");Box[][] boxes=new Box[6][8];double[][] roots=new double[6][8];double factor=1;
        for(int row=0;row<6;row++)for(int col=0;col<8;col++){
            Box b=bounds(sheet,cell(sheet,col,row,8,6));boxes[row][col]=b;roots[row][col]=footX(sheet,b);
            factor=Math.min(factor,48.0/Math.max(1,b.height-1));
            factor=Math.min(factor,53.0/Math.max(1,Math.max(roots[row][col]-b.x,b.x+b.width-roots[row][col])));
        }
        for(int row=0;row<6;row++)for(int col=0;col<8;col++){
            Box b=boxes[row][col];pack(sheet,b,"npc_"+row+"_"+col+".png","npcs.png",factor,112,64,roots[row][col],b.y+b.height-1);
        }
    }
    // [BLOCK: ART TOOL] RpgAssetImporter.props - Handle props for RpgAssetImporter.
    private static void props()throws Exception{
        BufferedImage sheet=read("props.png");Box[] boxes=new Box[12];int maxW=0,maxH=0;
        for(int i=0;i<12;i++){boxes[i]=bounds(sheet,cell(sheet,i%4,i/4,4,3));maxW=Math.max(maxW,boxes[i].width);maxH=Math.max(maxH,boxes[i].height);}
        int[] widths={128,120,96,86,72,40,46,48,70,64,44,78};
        int[] heights={108,96,96,58,32,104,42,44,26,40,32,60};
        for(int i=0;i<12;i++){if(i==4)continue;Box b=boxes[i];double factor=Math.min(widths[i]/(double)b.width,heights[i]/(double)b.height);pack(sheet,b,"prop_"+i+".png","props.png",factor,160,144,b.x+(b.width-1)/2.0,b.y+b.height-1);}
    }
}
