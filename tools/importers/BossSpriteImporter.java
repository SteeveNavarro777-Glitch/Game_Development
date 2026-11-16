/* FILE PURPOSE: BOSS IMPORT TOOL | Pack the three supplied directional villain sheets into reproducible cached atlases.
 * Course examples: frame arrays, deterministic indexing and SHA-256 provenance; docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.imageio.ImageIO;

/** Original alpha and the complete 144-pixel cells are retained before one uniform nearest-neighbor scale. */
public final class BossSpriteImporter {
    private static final String[] KINDS={"tank","mage","fighter"};
    private static final String[] DIRECTIONS={"down","up","left","right"};
    private static final String[] ANIMATIONS={"idle","walk","jump","hurt","death","attack","power","skill"};
    private static final int CELL=144,SIZE=128,COLUMNS=8,ANCHOR_X=64,ANCHOR_Y=124;
    private static final File SOURCE=new File("art/source/bosses"),OUTPUT=new File("res/bosses");
    // [BLOCK: IMPORT UTILITY] BossSpriteImporter.BossSpriteImporter - Prevent instances of the deterministic packing tool.
    private BossSpriteImporter(){}
    // [BLOCK: FRAME COUNTS] BossSpriteImporter.count - Preserve each authored state length, including the fighter's four-frame attack.
    private static int count(int kind,int animation){return switch(animation){case 0,1,2->4;case 3->3;case 4,6,7->6;default->kind==2?4:5;};}
    // [BLOCK: IMPORT ENTRY] BossSpriteImporter.main - Pack all three sheets, write exact source coordinates and export a review gallery.
    public static void main(String[] args)throws Exception{
        Files.createDirectories(OUTPUT.toPath());Files.createDirectories(new File("build").toPath());
        StringBuilder manifest=new StringBuilder("kind,animation,direction,frame,atlas,x,y,width,height,anchor_x,anchor_y,source,source_x,source_y,source_width,source_height,source_sha256\n");
        BufferedImage gallery=new BufferedImage(1200,960,BufferedImage.TYPE_INT_RGB);Graphics2D review=gallery.createGraphics();
        review.setColor(new Color(0x24392F));review.fillRect(0,0,gallery.getWidth(),gallery.getHeight());review.setFont(new Font("Monospaced",Font.BOLD,14));
        int total=0;
        try{
            for(int kind=0;kind<KINDS.length;kind++){
                File input=new File(SOURCE,KINDS[kind]+".png");byte[] bytes=Files.readAllBytes(input.toPath());
                String sha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
                BufferedImage sheet=ImageIO.read(input);
                if(sheet==null||sheet.getWidth()!=CELL*6||sheet.getHeight()!=CELL*32||!sheet.getColorModel().hasAlpha())
                    throw new IllegalArgumentException("Expected transparent 864x4608 boss sheet: "+input);
                int frames=0;for(int a=0;a<8;a++)frames+=count(kind,a);frames*=4;
                BufferedImage atlas=new BufferedImage(SIZE*COLUMNS,((frames+COLUMNS-1)/COLUMNS)*SIZE,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g=atlas.createGraphics();g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                int index=0;
                try{
                    for(int direction=0;direction<4;direction++)for(int animation=0;animation<8;animation++)for(int frame=0;frame<count(kind,animation);frame++){
                        int sx=frame*CELL,sy=(direction*8+animation)*CELL,x=(index%COLUMNS)*SIZE,y=(index/COLUMNS)*SIZE;
                        g.drawImage(sheet,x,y,x+SIZE,y+SIZE,sx,sy,sx+CELL,sy+CELL,null);
                        manifest.append(KINDS[kind]).append(',').append(ANIMATIONS[animation]).append(',').append(DIRECTIONS[direction]).append(',').append(frame).append(',')
                            .append(KINDS[kind]).append(".png,").append(x).append(',').append(y).append(',').append(SIZE).append(',').append(SIZE).append(',')
                            .append(ANCHOR_X).append(',').append(ANCHOR_Y).append(',').append(KINDS[kind]).append(".png,").append(sx).append(',').append(sy).append(',')
                            .append(CELL).append(',').append(CELL).append(',').append(sha).append('\n');
                        index++;
                    }
                }finally{g.dispose();}
                if(!ImageIO.write(atlas,"png",new File(OUTPUT,KINDS[kind]+".png")))throw new IllegalStateException("PNG writer unavailable");
                review(review,atlas,kind);total+=frames;
            }
        }finally{review.dispose();}
        Files.writeString(new File(OUTPUT,"frames.csv").toPath(),manifest.toString(),StandardCharsets.UTF_8);
        ImageIO.write(gallery,"png",new File("build/preview-boss-sprites.png"));
        System.out.println("Packed "+total+" authored boss frames into three atlases; anchor "+ANCHOR_X+","+ANCHOR_Y+" on "+SIZE+"x"+SIZE+" cells.");
    }
    // [BLOCK: REVIEW ART] BossSpriteImporter.review - Show facing and action examples without modifying packed gameplay images.
    private static void review(Graphics2D g,BufferedImage atlas,int kind){
        int[] animations={0,1,2,5,6,7};
        for(int row=0;row<6;row++){
            int animation=animations[row],direction=row<3?row==2?1:0:row==3?2:3,frame=row<2?0:2,index=0;
            for(int d=0;d<direction;d++)for(int a=0;a<8;a++)index+=count(kind,a);
            for(int a=0;a<animation;a++)index+=count(kind,a);index+=Math.min(frame,count(kind,animation)-1);
            int x=kind*400+138,y=row*160+26;
            g.setColor(new Color(0xA1B68D));g.drawLine(kind*400+30,y+ANCHOR_Y,kind*400+370,y+ANCHOR_Y);
            g.setColor(new Color(0xEED69F));g.drawString(KINDS[kind]+" / "+DIRECTIONS[direction]+" / "+ANIMATIONS[animation],kind*400+14,row*160+20);
            g.drawImage(atlas,x,y,x+SIZE,y+SIZE,(index%COLUMNS)*SIZE,(index/COLUMNS)*SIZE,(index%COLUMNS+1)*SIZE,(index/COLUMNS+1)*SIZE,null);
        }
    }
}
