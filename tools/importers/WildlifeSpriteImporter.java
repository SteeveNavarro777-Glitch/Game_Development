/* FILE PURPOSE: WILDLIFE IMPORT TOOL | Pack all supplied boar, wolf and Sigbin frames into three reproducible transparent atlases.
 * Course examples: arrays, FIFO flood fill, bounds and SHA-256 provenance; docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.imageio.ImageIO;

/** All poses share a 1:16 scale; opaque body bounds establish ground registration, including the falling poses. */
public final class WildlifeSpriteImporter {
    private static final String[] SEQUENCES={"front","back","side","attack","death","jump"};
    private static final String[] KINDS={"boar","wolf","sigbin"};
    private static final int DIVISOR=16,CELL=80,COLUMNS=7,ANCHOR_X=40,ANCHOR_Y=72;
    // [BLOCK: TOOL UTILITY] WildlifeSpriteImporter.WildlifeSpriteImporter - Prevent construction of this offline import tool.
    private WildlifeSpriteImporter(){}
    // [BLOCK: DSA FIFO FLOOD FILL] WildlifeSpriteImporter.bodyBounds - Register the largest opaque connected body, excluding detached dust and the death puff.
    private static Rectangle bodyBounds(BufferedImage image){
        int width=image.getWidth(),height=image.getHeight();int[] pixels=image.getRGB(0,0,width,height,null,0,width);
        boolean[] seen=new boolean[pixels.length];int[] queue=new int[pixels.length];int best=0;Rectangle result=null;
        for(int start=0;start<pixels.length;start++){
            if(seen[start]||(pixels[start]>>>24)<200)continue;
            int head=0,tail=0,minX=width,minY=height,maxX=0,maxY=0;queue[tail++]=start;seen[start]=true;
            while(head<tail){
                int at=queue[head++],x=at%width,y=at/width;minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);
                if(x>0)tail=offer(at-1,pixels,seen,queue,tail);
                if(x+1<width)tail=offer(at+1,pixels,seen,queue,tail);
                if(y>0)tail=offer(at-width,pixels,seen,queue,tail);
                if(y+1<height)tail=offer(at+width,pixels,seen,queue,tail);
            }
            if(tail>best){best=tail;result=new Rectangle(minX,minY,maxX-minX+1,maxY-minY+1);}
        }
        if(result==null||best<1000)throw new IllegalArgumentException("Missing substantial opaque boar body");
        return result;
    }
    // [BLOCK: DSA ARRAY QUEUE] WildlifeSpriteImporter.offer - Append one unseen opaque neighbor to the flood-fill FIFO in constant time.
    private static int offer(int at,int[] pixels,boolean[] seen,int[] queue,int tail){
        if(!seen[at]&&(pixels[at]>>>24)>=200){seen[at]=true;queue[tail++]=at;}return tail;
    }
    // [BLOCK: IMPORT ENTRY] WildlifeSpriteImporter.main - Pack original RGBA poses once, record source hashes and export a transparent-art review gallery.
    public static void main(String[] args)throws Exception{
        File output=new File("res/wildlife"),build=new File("build");
        Files.createDirectories(output.toPath());Files.createDirectories(build.toPath());
        StringBuilder csv=new StringBuilder("kind,sequence,frame,atlas,x,y,width,height,anchor_x,anchor_y,source,source_ground_x,source_ground_y,scale_divisor,source_sha256\n");
        int total=0;
        for(String kind:KINDS){
            File source=new File("art/source/"+kind);int rows=kind.equals("wolf")?6:5;
            BufferedImage atlas=new BufferedImage(CELL*COLUMNS,CELL*(kind.equals("wolf")?4:3),BufferedImage.TYPE_INT_ARGB);
            BufferedImage gallery=new BufferedImage(850,rows*100+10,BufferedImage.TYPE_INT_RGB);
            Graphics2D g=atlas.createGraphics(),review=gallery.createGraphics();
            review.setFont(new Font("Monospaced",Font.BOLD,14));
            g.setComposite(AlphaComposite.Src);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            review.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int index=0;
            try{
                review.setColor(new Color(0x1E3428));review.fillRect(0,0,850,gallery.getHeight());
                for(int row=0;row<rows;row++){
                    review.setColor(new Color(0xE5D7AB));review.drawString(SEQUENCES[row],12,row*100+35);
                    int count=row==4?5:4;
                    for(int frame=0;frame<count;frame++,index++){
                        File input=new File(source,SEQUENCES[row]+frame+".png");BufferedImage image=ImageIO.read(input);
                        if(image==null||image.getWidth()<512||image.getHeight()<512||!image.getColorModel().hasAlpha())
                            throw new IllegalArgumentException("Expected a transparent wildlife pose: "+input);
                        Rectangle bounds=bodyBounds(image);int groundX=(int)Math.round(bounds.getCenterX()),groundY=bounds.y+bounds.height;
                        int x=index%COLUMNS*CELL,y=index/COLUMNS*CELL;
                        int dx=ANCHOR_X-(int)Math.round(groundX/(double)DIVISOR),dy=ANCHOR_Y-(int)Math.round(groundY/(double)DIVISOR);
                        g.setClip(x,y,CELL,CELL);g.drawImage(image,x+dx,y+dy,(int)Math.round(image.getWidth()/(double)DIVISOR),(int)Math.round(image.getHeight()/(double)DIVISOR),null);
                        String sha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(input.toPath())));
                        csv.append(kind).append(',').append(SEQUENCES[row]).append(',').append(frame).append(',').append(kind).append(".png,").append(x).append(',').append(y)
                            .append(',').append(CELL).append(',').append(CELL).append(',').append(ANCHOR_X).append(',').append(ANCHOR_Y)
                            .append(',').append(input.getName()).append(',').append(groundX).append(',').append(groundY).append(',').append(DIVISOR).append(',').append(sha).append('\n');
                        int gx=120+frame*140,gy=row*100;
                        review.setColor(new Color(0x497248));review.fillRect(gx,gy+3,132,93);
                        review.drawImage(atlas,gx-14,gy-52,gx+146,gy+108,x,y,x+CELL,y+CELL,null);
                        review.setColor(new Color(0xE5D7AB));review.drawString(Integer.toString(frame),gx+5,gy+17);
                    }
                }
            }finally{g.dispose();review.dispose();}
            ImageIO.write(atlas,"png",new File(output,kind+".png"));
            ImageIO.write(gallery,"png",new File(build,"preview-"+kind+"-sprites.png"));total+=index;
        }
        // [SECTION: DETAIL PORTRAIT] Reuse the original front pose at a denser scale for the brief close-up, avoiding a magnified tiny gameplay frame.
        File portraitSource=new File("art/source/sigbin/front3.png");BufferedImage original=ImageIO.read(portraitSource);
        Rectangle bounds=bodyBounds(original);int portraitX=160-(int)Math.round(bounds.getCenterX()/4),portraitY=288-(int)Math.round((bounds.y+bounds.height)/4d);
        BufferedImage portrait=new BufferedImage(320,320,BufferedImage.TYPE_INT_ARGB);Graphics2D closeup=portrait.createGraphics();
        try{closeup.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);closeup.drawImage(original,portraitX,portraitY,(int)Math.round(original.getWidth()/4d),(int)Math.round(original.getHeight()/4d),null);}finally{closeup.dispose();}
        ImageIO.write(portrait,"png",new File(output,"sigbin-scare.png"));
        String portraitHash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(portraitSource.toPath())));
        Files.writeString(new File(output,"portrait.csv").toPath(),"source,output,scale_divisor,anchor_x,anchor_y,source_sha256\nfront3.png,sigbin-scare.png,4,160,288,"+portraitHash+"\n",StandardCharsets.UTF_8);
        Files.writeString(new File(output,"frames.csv").toPath(),csv.toString(),StandardCharsets.UTF_8);
        System.out.println("Packed "+total+" supplied wildlife poses into three atlases; fixed 1:16 scale, ground anchor 40,72.");
    }
}
