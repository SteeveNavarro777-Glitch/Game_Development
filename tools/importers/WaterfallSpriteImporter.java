/* FILE PURPOSE: WATERFALL IMPORTER | Composite GIF delta frames and pack registered cove, water masks and animation atlases.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;
import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;

/** Offline only: the game loads small prepared PNG caches, never the full GIF or generated source. */
public final class WaterfallSpriteImporter {
    private static final int W=512,H=384,COUNT=8,LIP=76,FOOT=171,FALL_X=260;
    // [BLOCK: STATIC IMPORTER] WaterfallSpriteImporter.WaterfallSpriteImporter - Prevent instances of the offline converter.
    private WaterfallSpriteImporter(){}
    // [BLOCK: IMPORT ENTRY] WaterfallSpriteImporter.main - Pack matching pond/river water, sampled banks and a tapered outlet with physical geometry.
    public static void main(String[] args)throws Exception{
        Path source=Path.of("art/source/waterfall"),output=Path.of("res/waterfall");Files.createDirectories(output);
        BufferedImage cove=scale(ImageIO.read(source.resolve("cove-v1.png").toFile()),W,H);
        BufferedImage mountain=scale(scale(ImageIO.read(source.resolve("mountain-v1.png").toFile()),512,640),1024,1280);
        ImageIO.write(mountain,"png",output.resolve("mountain.png").toFile());
        List<BufferedImage> scenes=readGif(source.resolve("Falls.gif"));
        BufferedImage waterAtlas=new BufferedImage(COUNT*64,128,BufferedImage.TYPE_INT_ARGB),
            fallAtlas=new BufferedImage(COUNT*64,96,BufferedImage.TYPE_INT_ARGB),
            upperAtlas=new BufferedImage(COUNT*96,352,BufferedImage.TYPE_INT_ARGB),
            coveAtlas=new BufferedImage(COUNT*W,H,BufferedImage.TYPE_INT_ARGB),
            mouthAtlas=new BufferedImage(COUNT*192,288,BufferedImage.TYPE_INT_ARGB),
            mouthMask=new BufferedImage(192,288,BufferedImage.TYPE_INT_ARGB);
        boolean[][] mask=mask(cove);BufferedImage physical=new BufferedImage(W,H,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<H;y++)for(int x=0;x<W;x++)if(mask[y][x])physical.setRGB(x,y,0xFFFFFFFF);
        int[] edges=outlet(mask,0);double middle=(edges[0]+edges[1])/2.0,half=(edges[1]-edges[0]+1)/2.0;
        BufferedImage bank=banks(cove,mask);
        for(int phase=0;phase<COUNT;phase++){
            for(int y=0;y<128;y++)for(int x=0;x<64;x++)waterAtlas.setRGB(phase*64+x,y,riverPixel(cove,x,y,phase));
            BufferedImage gif=scenes.get(phase),fall=scale(gif.getSubimage(563,107,134,189),64,96);
            for(int y=0;y<96;y++)for(int x=0;x<64;x++)if(!wet(fall.getRGB(x,y)))fall.setRGB(x,y,0);
            paste(fallAtlas,fall,phase*64,0);
            BufferedImage upper=new BufferedImage(96,352,BufferedImage.TYPE_INT_ARGB);Graphics2D ug=upper.createGraphics();
            try{ug.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                for(int y=0;y<352;y+=118)ug.drawImage(fall,0,y,96,144,null);
            }finally{ug.dispose();}paste(upperAtlas,upper,phase*96,0);
            BufferedImage frame=copy(cove);
            for(int y=0;y<H;y++)for(int x=0;x<W;x++)if(mask[y][x]){
                int original=cove.getRGB(x,y),color=riverPixel(cove,5388+x,2453+y,phase),reference=riverPixel(cove,5388+x,2453+y,0);
                int delta=Math.max(-4,Math.min(4,(((color>>8)&255)-((reference>>8)&255))/14));
                if(y>=FOOT&&wet(original))frame.setRGB(x,y,adjust(original,delta));
                if(x<80&&y>280&&wet(original)){
                    int[] shore=outlet(mask,x);double depth=Math.min(y-shore[0],shore[1]-y);
                    double blend=Math.pow(1-x/80.0,1.4)*Math.max(0,Math.min(1,(depth-3)/7));
                    frame.setRGB(x,y,blend(frame.getRGB(x,y),color,blend));
                }
                if(x>=FALL_X-33&&x<FALL_X+33&&y>=LIP&&y<FOOT){
                    int sx=Math.min(63,(x-(FALL_X-33))*64/66),sy=(y-LIP)*96/(FOOT-LIP);
                    int moving=fall.getRGB(sx,sy);if((moving>>>24)>128)frame.setRGB(x,y,blend(original,moving,.78));
                }
            }
            paste(coveAtlas,frame,phase*W,0);
            for(int y=0;y<288;y++)for(int x=0;x<192;x++){
                double u=Math.max(0,Math.min(1,(172-x)/156.0)),t=u*u*(3-2*u),center=middle+64*t,width=half+(48-half)*t;
                double depth=width-Math.abs(235+y-center);
                if(depth<0)continue;
                int color;
                if(depth>6){
                    color=riverPixel(cove,5216+x,2688+y,phase);mouthMask.setRGB(x,y,0xFFFFFFFF);
                    if(depth<8&&Math.floorMod(x+y+phase,4)!=0)color=blend(color,0xFF8FDFC6,.5);
                }else{
                    int side=235+y<center?0:1,d=Math.max(0,Math.min(7,(int)(6-depth)));
                    color=bank.getRGB(Math.floorMod(x,64),side*8+d);
                }
                mouthAtlas.setRGB(phase*192+x,y,color);
            }
        }
        ImageIO.write(coveAtlas,"png",output.resolve("cove.png").toFile());ImageIO.write(physical,"png",output.resolve("surface.png").toFile());
        ImageIO.write(waterAtlas,"png",output.resolve("water.png").toFile());ImageIO.write(fallAtlas,"png",output.resolve("fall.png").toFile());
        ImageIO.write(upperAtlas,"png",output.resolve("upper.png").toFile());ImageIO.write(bank,"png",output.resolve("banks.png").toFile());
        ImageIO.write(mouthAtlas,"png",output.resolve("mouth.png").toFile());ImageIO.write(mouthMask,"png",output.resolve("mouth-surface.png").toFile());
        String manifest="source,sha256,role\nart/source/waterfall/Falls.gif,"+hash(source.resolve("Falls.gif"))+",user supplied animation\n"+
            "art/source/waterfall/cove-v1.png,"+hash(source.resolve("cove-v1.png"))+",built-in imagegen cove terrain\n"+
            "art/source/waterfall/mountain-v1.png,"+hash(source.resolve("mountain-v1.png"))+",built-in imagegen mountain terrain\n"+
            "registration,512x384,fall x260 lip76 foot171; outlet center "+middle+" half width "+half+"\n"+
            "gif crop,563 107 134 189,fall to64x96\nriver crop,cove 224 204 32 64,mirrored seamless64x128; eight32px variants;48px per second westward\n"+
            "mouth,-172 235 192 288,cove relative origin; matching bank samples and water mask\n"+
            "frames,0 3 6 9 12 15 18 21,delta composites; runtime6 phases per second\n";
        Files.writeString(output.resolve("manifest.csv"),manifest);
        System.out.println("Imported matching river/cove: eight water variants per phase; outlet center="+middle+" half width="+half);
    }
    // [BLOCK: SHARED WATER TEXTURE] WaterfallSpriteImporter.riverPixel - Mirror a clean native pond patch into a seamless world-registered water pattern.
    private static int riverPixel(BufferedImage cove,int x,int y,int phase){
        int sx=Math.floorMod(x+phase*8,64),sy=Math.floorMod(y,128);
        return cove.getRGB(224+(sx<32?sx:63-sx),204+(sy<64?sy:127-sy));
    }
    // [BLOCK: OUTLET REGISTRATION] WaterfallSpriteImporter.outlet - Measure the actual stream width within a native cove column.
    private static int[] outlet(boolean[][] mask,int x){
        int top=H,bottom=-1;for(int y=280;y<H;y++)if(mask[y][x]){top=Math.min(top,y);bottom=y;}
        if(bottom<top)throw new IllegalStateException("No southwest outlet at column "+x);
        return new int[]{top,bottom};
    }
    // [BLOCK: NATIVE BANK SAMPLES] WaterfallSpriteImporter.banks - Copy the cove's real rock/moss bank pixels into reusable eight-pixel edge strips.
    private static BufferedImage banks(BufferedImage cove,boolean[][] mask){
        BufferedImage image=new BufferedImage(64,16,BufferedImage.TYPE_INT_ARGB);
        for(int x=0;x<64;x++){int sourceX=x%32<16?x%16:15-x%16;int[] edge=outlet(mask,sourceX);for(int d=0;d<8;d++){
            int top=Math.max(0,edge[0]-1-d),bottom=Math.min(H-1,edge[1]+1+d);
            image.setRGB(x,d,cove.getRGB(sourceX,top));image.setRGB(x,8+d,cove.getRGB(sourceX,bottom));
        }}return image;
    }
    // [BLOCK: COLOR REGISTRATION] WaterfallSpriteImporter.wet - Identify teal water while excluding warm rocks, golden sand and yellow-green moss.
    private static boolean wet(int rgba){int r=rgba>>16&255,g=rgba>>8&255,b=rgba&255;return (rgba>>>24)>128&&g>r*1.06&&b>r*.72&&b>g*.48;}
    // [BLOCK: DSA FIFO FLOOD FILL] WaterfallSpriteImporter.mask - Follow connected water from the pond, then fill small lily-pad islands inside each shoreline span.
    private static boolean[][] mask(BufferedImage image){
        boolean[][] out=new boolean[H][W];ArrayDeque<Point> queue=new ArrayDeque<>();queue.add(new Point(260,240));out[240][260]=true;
        while(!queue.isEmpty()){Point p=queue.removeFirst();for(int[] d:new int[][]{{0,1},{1,0},{0,-1},{-1,0}}){
            int x=p.x+d[0],y=p.y+d[1];if(x>=0&&y>=0&&x<W&&y<H&&!out[y][x]&&wet(image.getRGB(x,y))){out[y][x]=true;queue.addLast(new Point(x,y));}
        }}
        for(int y=FOOT;y<H;y++){int left=W,right=-1;for(int x=0;x<W;x++)if(out[y][x]){left=Math.min(left,x);right=x;}
            for(int x=left;x<=right;x++)out[y][x]=true;}
        return out;
    }
    // [BLOCK: GIF DELTA COMPOSITION] WaterfallSpriteImporter.readGif - Honor frame offsets and disposal before sampling the original moving cascade.
    private static List<BufferedImage> readGif(Path path)throws Exception{
        List<BufferedImage> frames=new ArrayList<>();
        try(ImageInputStream input=ImageIO.createImageInputStream(path.toFile())){
            ImageReader reader=ImageIO.getImageReadersByFormatName("gif").next();reader.setInput(input);
            BufferedImage canvas=new BufferedImage(reader.getWidth(0),reader.getHeight(0),BufferedImage.TYPE_INT_ARGB),saved=null;
            Rectangle previous=new Rectangle();String disposal="none";
            for(int i=0;i<=21;i++){
                if(disposal.equals("restoreToPrevious")&&saved!=null)canvas=copy(saved);
                else if(disposal.equals("restoreToBackgroundColor")){Graphics2D g=canvas.createGraphics();try{g.setComposite(AlphaComposite.Clear);g.fill(previous);}finally{g.dispose();}}
                Node tree=reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0"),descriptor=find(tree,"ImageDescriptor"),control=find(tree,"GraphicControlExtension");
                Rectangle bounds=new Rectangle(value(descriptor,"imageLeftPosition"),value(descriptor,"imageTopPosition"),value(descriptor,"imageWidth"),value(descriptor,"imageHeight"));
                disposal=control==null?"none":control.getAttributes().getNamedItem("disposalMethod").getNodeValue();
                saved=disposal.equals("restoreToPrevious")?copy(canvas):null;
                Graphics2D g=canvas.createGraphics();try{g.drawImage(reader.read(i),bounds.x,bounds.y,null);}finally{g.dispose();}
                if(i%3==0)frames.add(copy(canvas));previous=bounds;
            }reader.dispose();
        }return frames;
    }
    // [BLOCK: METADATA TREE] WaterfallSpriteImporter.find - Locate the named GIF metadata node.
    private static Node find(Node node,String name){if(node.getNodeName().equals(name))return node;for(Node child=node.getFirstChild();child!=null;child=child.getNextSibling()){Node found=find(child,name);if(found!=null)return found;}return null;}
    // [BLOCK: METADATA ATTRIBUTE] WaterfallSpriteImporter.value - Read an integer GIF descriptor attribute.
    private static int value(Node node,String key){return Integer.parseInt(node.getAttributes().getNamedItem(key).getNodeValue());}
    // [BLOCK: NEAREST NEIGHBOR PACKING] WaterfallSpriteImporter.scale - Register existing artwork at native resolution without smoothing pixel edges.
    private static BufferedImage scale(BufferedImage input,int width,int height){BufferedImage out=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);Graphics2D g=out.createGraphics();try{g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);g.drawImage(input,0,0,width,height,null);}finally{g.dispose();}return out;}
    // [BLOCK: IMAGE COPY] WaterfallSpriteImporter.copy - Snapshot a composite only during offline import.
    private static BufferedImage copy(BufferedImage input){return scale(input,input.getWidth(),input.getHeight());}
    // [BLOCK: ATLAS PACKING] WaterfallSpriteImporter.paste - Store one registered phase in the horizontal atlas.
    private static void paste(BufferedImage out,BufferedImage frame,int x,int y){Graphics2D g=out.createGraphics();try{g.drawImage(frame,x,y,null);}finally{g.dispose();}}
    // [BLOCK: WATER SHIMMER] WaterfallSpriteImporter.adjust - Transfer a small source-texture luminance change while retaining painted pool depth and lily details.
    private static int adjust(int rgb,int amount){int r=Math.max(0,Math.min(255,(rgb>>16&255)+amount)),g=Math.max(0,Math.min(255,(rgb>>8&255)+amount)),b=Math.max(0,Math.min(255,(rgb&255)+amount));return rgb&0xFF000000|r<<16|g<<8|b;}
    // [BLOCK: CASCADE BLEND] WaterfallSpriteImporter.blend - Register moving source water over the painted waterfall inside its exact wet silhouette.
    private static int blend(int base,int moving,double alpha){int rgb=0xFF000000;for(int shift:new int[]{16,8,0})rgb|=(int)Math.round((base>>shift&255)*(1-alpha)+(moving>>shift&255)*alpha)<<shift;return rgb;}
    // [BLOCK: ART PROVENANCE] WaterfallSpriteImporter.hash - Record SHA-256 for untouched user and generated sources.
    private static String hash(Path path)throws Exception{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));}
}
