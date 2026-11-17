/* FILE PURPOSE: IMPORT TOOL | Preserve supplied potion RGBA art in small icons and an indexed effect atlas with hashes.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.imageio.ImageIO;

/** Deterministic Java2D packing; the game never decodes the original 1024px bottles. */
public final class PotionSpriteImporter {
    private static final String[] KINDS={"health","energy","shield"},PREFIX={"heal","energy","shield"};
    private static final int[] COUNTS={6,8,8};
    // [BLOCK: STATIC TOOL] PotionSpriteImporter.PotionSpriteImporter - Prevent instances of the artwork packing tool.
    private PotionSpriteImporter(){}
    // [BLOCK: IMPORT / DSA ARRAY] PotionSpriteImporter.main - Pack three potion icons and twenty-two effect frames into deterministic runtime images.
    public static void main(String[] args)throws Exception {
        Path output=Path.of("res/items"); Files.createDirectories(output); Files.createDirectories(Path.of("build"));
        BufferedImage effects=new BufferedImage(256,96,BufferedImage.TYPE_INT_ARGB);
        BufferedImage review=new BufferedImage(360,116,BufferedImage.TYPE_INT_RGB);
        Graphics2D eg=effects.createGraphics(),rg=review.createGraphics();
        StringBuilder manifest=new StringBuilder("item,type,frame,source,output,x,y,width,height,source_sha256\n");
        try {
            eg.setComposite(AlphaComposite.Src); eg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            rg.setColor(new Color(0x23382D));rg.fillRect(0,0,360,116);rg.setFont(new Font("Monospaced",Font.BOLD,12));
            rg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            for(int kind=0;kind<KINDS.length;kind++) {
                Path source=Path.of("art/source/potions",KINDS[kind],PREFIX[kind]+".png");
                BufferedImage original=read(source); Rectangle b=bounds(original);
                BufferedImage icon=new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);Graphics2D g=icon.createGraphics();
                double scale=Math.min(28.0/b.width,28.0/b.height);int width=Math.max(1,(int)Math.round(b.width*scale)),height=Math.max(1,(int)Math.round(b.height*scale));
                try {g.setComposite(AlphaComposite.Src);g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g.drawImage(original,(32-width)/2,(32-height)/2,(32+width)/2,(32+height)/2,b.x,b.y,b.x+b.width,b.y+b.height,null);
                }finally{g.dispose();}
                ImageIO.write(icon,"png",output.resolve(KINDS[kind]+".png").toFile());
                row(manifest,KINDS[kind],"icon",0,source,KINDS[kind]+".png",0,0,32,32);
                rg.drawImage(icon,kind*120+24,5,72,72,null);rg.setColor(new Color(0xF1DBAA));rg.drawString(KINDS[kind].toUpperCase(java.util.Locale.ROOT),kind*120+30,100);
                System.out.println(KINDS[kind]+" bottle: "+original.getWidth()+"x"+original.getHeight()+", opaque bounds "+b);
                for(int frame=0;frame<COUNTS[kind];frame++) {
                    Path input=source.getParent().resolve(PREFIX[kind]+"_"+(frame+1)+".png");BufferedImage image=read(input);
                    if(image.getWidth()!=32||image.getHeight()!=32)throw new IOException("Expected a supplied 32px effect: "+input);
                    eg.drawImage(image,frame*32,kind*32,null);row(manifest,KINDS[kind],"effect",frame,input,"effects.png",frame*32,kind*32,32,32);
                }
            }
        }finally{eg.dispose();rg.dispose();}
        ImageIO.write(effects,"png",output.resolve("effects.png").toFile());ImageIO.write(review,"png",Path.of("build/preview-potion-icons.png").toFile());
        Files.writeString(output.resolve("frames.csv"),manifest.toString(),StandardCharsets.UTF_8);
        System.out.println("Packed 3 bottle icons and 22 supplied effect frames.");
    }
    // [BLOCK: VALIDATED IMAGE IO] PotionSpriteImporter.read - Require a readable transparent source PNG.
    private static BufferedImage read(Path path)throws IOException {
        BufferedImage image=ImageIO.read(path.toFile());if(image==null||!image.getColorModel().hasAlpha())throw new IOException("Expected RGBA PNG: "+path);return image;
    }
    // [BLOCK: ALPHA SCAN] PotionSpriteImporter.bounds - Crop empty bottle margins using visible alpha rather than hidden RGB pixels.
    private static Rectangle bounds(BufferedImage image)throws IOException {
        int left=image.getWidth(),top=image.getHeight(),right=-1,bottom=-1;
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,y)>>>24)>=200){left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x);bottom=Math.max(bottom,y);}
        if(right<left)throw new IOException("Potion has no visible body");return new Rectangle(left,top,right-left+1,bottom-top+1);
    }
    // [BLOCK: ASSET PROVENANCE] PotionSpriteImporter.row - Record atlas coordinates and the exact original SHA-256 for each supplied frame.
    private static void row(StringBuilder csv,String kind,String type,int frame,Path source,String output,int x,int y,int w,int h)throws Exception {
        String sha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
        csv.append(kind).append(',').append(type).append(',').append(frame).append(',').append(source.toString().replace('\\','/')).append(',').append(output).append(',').append(x).append(',').append(y).append(',').append(w).append(',').append(h).append(',').append(sha).append('\n');
    }
}
