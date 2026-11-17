/* FILE PURPOSE: IMPORT TOOL | Pack authored empty-handed locomotion/drinking bodies with boot and wrist anchors.
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

/** Registers all poses to one foot baseline; runtime selection only reads cached frames. */
public final class EquipmentSpriteImporter {
    private static final int CELL=64,POSES=8;
    private static final String[] DIRECTIONS={"down","right","up"};
    // [SECTION: DSA - 3D ARRAY] Native wrist anchors are indexed by direction, authored pose and coordinate.
    private static final int[][][] HANDS={
        {{-7,-18},{-8,-18},{-6,-18},{-9,-24},{-6,-29},{-5,-33},{-6,-23},{-6,-18}},
        {{0,-18},{1,-18},{2,-18},{2,-24},{8,-29},{12,-35},{5,-22},{2,-18}},
        {{8,-20},{8,-20},{7,-20},{9,-25},{10,-29},{8,-33},{9,-24},{7,-20}}
    };
    // [BLOCK: STATIC TOOL] EquipmentSpriteImporter.EquipmentSpriteImporter - Prevent instances of the sprite preparation tool.
    private EquipmentSpriteImporter(){}
    // [BLOCK: ART IMPORT / ARRAY REGISTRATION] EquipmentSpriteImporter.main - Pack twenty-four authored bodies and record shared boot/wrist metadata.
    public static void main(String[] args)throws Exception{
        Path input=Path.of("art/source/player/agyu-held-items-v1.png"),output=Path.of("res/player/equipment");
        BufferedImage source=ImageIO.read(input.toFile());
        if(source==null||!source.getColorModel().hasAlpha()||(source.getRGB(0,0)>>>24)>16)throw new IOException("Transparent held-item sheet required");
        int cw=source.getWidth()/4,ch=source.getHeight()/6;Files.createDirectories(output);Files.createDirectories(Path.of("build"));
        BufferedImage atlas=new BufferedImage(CELL*POSES,CELL*3,BufferedImage.TYPE_INT_ARGB);
        BufferedImage review=new BufferedImage(1152,366,BufferedImage.TYPE_INT_RGB);Graphics2D gallery=review.createGraphics();
        String sha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(input)));
        StringBuilder csv=new StringBuilder("direction,pose,x,y,hand_x,hand_y,source_left,source_top,source_right,source_bottom,source_sha256\n");
        try{
            gallery.setColor(new Color(0x263A35));gallery.fillRect(0,0,1152,366);gallery.setFont(new Font("Monospaced",Font.PLAIN,10));
            gallery.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            for(int dir=0;dir<3;dir++){
                Rectangle[] bounds=new Rectangle[POSES];int maxHeight=0;
                for(int pose=0;pose<POSES;pose++){
                    int x=pose%4*cw,y=(dir*2+pose/4)*ch;bounds[pose]=bounds(source,x,y,cw,ch);maxHeight=Math.max(maxHeight,bounds[pose].height);
                }
                double scale=maxHeight/51.0;
                for(int pose=0;pose<POSES;pose++){
                    Rectangle b=bounds[pose];int footY=b.y+b.height-1;double footX=boots(source,b,scale);
                    int ox=pose%4*cw,oy=(dir*2+pose/4)*ch;
                    for(int y=0;y<CELL;y++)for(int x=0;x<CELL;x++){
                        int sx=(int)Math.round(footX+(x-32)*scale),sy=(int)Math.round(footY+(y-63)*scale);
                        if(sx>=ox&&sx<ox+cw&&sy>=oy&&sy<oy+ch)atlas.setRGB(pose*CELL+x,dir*CELL+y,source.getRGB(sx,sy));
                    }
                    int[] hand=HANDS[dir][pose];csv.append(DIRECTIONS[dir]).append(',').append(pose).append(',').append(pose*CELL).append(',').append(dir*CELL)
                        .append(',').append(hand[0]).append(',').append(hand[1]).append(',').append(b.x).append(',').append(b.y).append(',').append(b.x+b.width-1).append(',').append(b.y+b.height-1).append(',').append(sha).append('\n');
                    int gx=pose*144+8,gy=dir*122+9;gallery.setColor(new Color(0xD9C98F));gallery.drawString(DIRECTIONS[dir]+" "+pose,gx,gy+7);
                    gallery.drawImage(atlas,gx+15,gy+12,gx+111,gy+108,pose*64,dir*64,pose*64+64,dir*64+64,null);
                    int hx=gx+63+(int)Math.round(hand[0]*1.5),hy=gy+107+(int)Math.round(hand[1]*1.5);
                    gallery.setColor(new Color(0x74EFFF));gallery.drawLine(hx-2,hy,hx+2,hy);gallery.drawLine(hx,hy-2,hx,hy+2);
                }
            }
        }finally{gallery.dispose();}
        ImageIO.write(atlas,"png",output.resolve("bodies.png").toFile());Files.writeString(output.resolve("frames.csv"),csv.toString(),StandardCharsets.UTF_8);
        ImageIO.write(review,"png",Path.of("build/preview-held-body-anchors.png").toFile());
        System.out.println("Packed 24 empty-handed body poses at 64px with boot and wrist anchors; source "+source.getWidth()+"x"+source.getHeight()+" alpha="+(source.getRGB(0,0)>>>24));
    }
    // [BLOCK: ALPHA BOUNDS] EquipmentSpriteImporter.bounds - Measure only the visible character within one authored grid cell.
    private static Rectangle bounds(BufferedImage image,int ox,int oy,int w,int h)throws IOException{
        int left=ox+w,top=oy+h,right=-1,bottom=-1;
        for(int y=oy;y<oy+h;y++)for(int x=ox;x<ox+w;x++)if((image.getRGB(x,y)>>>24)>128){left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x);bottom=Math.max(bottom,y);}
        if(right<left)throw new IOException("Empty body frame");return new Rectangle(left,top,right-left+1,bottom-top+1);
    }
    // [BLOCK: BOOT REGISTRATION] EquipmentSpriteImporter.boots - Locate the boot midpoint without letting arms or bandana move the body anchor.
    private static double boots(BufferedImage image,Rectangle b,double scale)throws IOException{
        long sum=0;int count=0,band=Math.max(3,(int)Math.ceil(scale*3));
        for(int y=b.y+b.height-band;y<b.y+b.height;y++)for(int x=b.x;x<b.x+b.width;x++){
            int c=image.getRGB(x,y),r=(c>>16)&255,g=(c>>8)&255,blue=c&255;
            if((c>>>24)>128&&r>15&&r>=g&&g>=blue){sum+=x;count++;}
        }
        if(count==0)throw new IOException("Boots missing");return sum/(double)count;
    }
}
