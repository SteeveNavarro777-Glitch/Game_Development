/* FILE PURPOSE: REGRESSION TEST | Complete guard sprites, source pixels, connected body silhouettes and drawing.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/** Guard rendering must preserve whole source poses instead of cutting body pixels. */
public final class GuardVisualTests {
    private static int checks;
    // [BLOCK: TEST HELPER] GuardVisualTests.GuardVisualTests - Prevent instances of this test/helper class.
    private GuardVisualTests(){}
    // [BLOCK: TEST HELPER] GuardVisualTests.run - Handle run for GuardVisualTests.
    static int run()throws Exception{
        checks=0;
        BufferedImage preview=new BufferedImage(960,500,BufferedImage.TYPE_INT_RGB);
        Graphics2D gallery=preview.createGraphics();
        try{
            gallery.setColor(new Color(0x263A35));gallery.fillRect(0,0,960,500);
            gallery.setFont(new Font("Monospaced",Font.PLAIN,14));
            for(int direction=0;direction<4;direction++){
                String name=Player.Dir.values()[direction].name().toLowerCase(java.util.Locale.ROOT);
                BufferedImage original=ImageIO.read(new File("res/player/guard_"+name+".png"));
                check(original!=null&&original.getWidth()==64&&original.getHeight()==64,"Missing complete guard frame");
                check(original.getColorModel().hasAlpha(),"Guard frame lost alpha");
                check(ShieldGuard.bodies[direction].length==1,"Guard stance must stay steady");
                BufferedImage active=ShieldGuard.bodies[direction][0];
                boolean unchanged=true;
                for(int y=0;y<64;y++)for(int x=0;x<64;x++)
                    if(active.getRGB(x,y)!=original.getRGB(x,y))unchanged=false;
                check(unchanged,"Guard loader changed body pixels");
                for(int row:new int[]{30,40,50,59}){
                    int visible=0;
                    for(int x=16;x<49;x++)if((active.getRGB(x,row)>>>24)>128)visible++;
                    check(visible>=6,"Guard body band is broken or empty");
                }
                int boots=0;for(int x=0;x<64;x++)if((active.getRGB(x,63)>>>24)>128)boots++;
                check(boots>=3,"Guard feet are not aligned to the shared baseline");
                BufferedImage rendered=new BufferedImage(192,144,BufferedImage.TYPE_INT_ARGB);
                Graphics2D actual=rendered.createGraphics();
                Player actor=new Player(96,100);actor.dir=Player.Dir.values()[direction];
                actor.state=Player.State.GUARD;actor.animTime=1.2;actor.stateTime=0.2;
                try{actor.draw(actual,0,0,1);}finally{actual.dispose();}
                BufferedImage expected=new BufferedImage(192,144,BufferedImage.TYPE_INT_ARGB);
                Graphics2D reference=expected.createGraphics();
                try{
                    reference.translate(96,100);reference.scale(GameScale.AGYU,GameScale.AGYU);
                    reference.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    reference.setColor(new Color(0,0,0,70));reference.fillOval(-11,-4,22,8);
                    reference.drawImage(original,-32,-63,null);
                }finally{reference.dispose();}
                boolean oneWholePose=true;
                for(int y=0;y<144;y++)for(int x=0;x<192;x++)
                    if(rendered.getRGB(x,y)!=expected.getRGB(x,y))oneWholePose=false;
                check(oneWholePose,"Guard render adds an extra shield or modifies the body");
                for(int row=0;row<2;row++){
                    gallery.setColor(new Color(0xF1C979));
                    gallery.drawString(name.toUpperCase(java.util.Locale.ROOT)+(row==0?" / IDLE":" / HELD SHIELD"),
                            direction*240+18,row*250+20);
                    Graphics2D cell=(Graphics2D)gallery.create();
                    try{
                        cell.translate(direction*240,row*250+25);cell.scale(2,2);
                        cell.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                        actor=new Player(60,85);actor.dir=Player.Dir.values()[direction];
                        actor.state=row==0?Player.State.IDLE:Player.State.GUARD;
                        actor.stateTime=0.2;actor.draw(cell,0,0,1);
                    }finally{cell.dispose();}
                }
            }
        }finally{gallery.dispose();}
        check(ImageIO.write(preview,"png",new File("build/preview-guard-repair.png")),"Guard repair preview missing");
        return checks;
    }
    // [BLOCK: TEST HELPER] GuardVisualTests.check - Handle check for GuardVisualTests.
    private static void check(boolean valid,String message){
        checks++;
        if(!valid)throw new AssertionError(message);
    }
}