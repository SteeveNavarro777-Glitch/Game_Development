/* FILE PURPOSE: REGRESSION TEST | Four-direction input, spear damage, current combat bodies and rendered weapon motion.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/** Regression checks exercise real input, damage windows and rendered motion. */
public final class CombatTests {
    private static final Canvas SOURCE=new Canvas();
    private static int checks;
    private static final int[] KEYS={KeyEvent.VK_S,KeyEvent.VK_W,KeyEvent.VK_A,KeyEvent.VK_D};
    private static final double[][] FORWARD={{0,1},{0,-1},{-1,0},{1,0}};
    // [BLOCK: TEST HELPER] CombatTests.CombatTests - Prevent instances of this test/helper class.
    private CombatTests(){}
    // [BLOCK: TEST HELPER] CombatTests.main - Entry point for this regression suite.
    public static void main(String[] args)throws Exception{
        Assets.load();System.out.println("Combat checks passed: "+run());
    }
    // [BLOCK: TEST HELPER] CombatTests.run - Handle run for CombatTests.
    static int run()throws Exception{
        checks=0;
        World world=new World(7);
        for(int direction=0;direction<4;direction++){
            Player actor=new Player(World.SPAWN_X,World.SPAWN_Y);
            actor.dir=Player.Dir.values()[(direction+1)%4];
            KeyHandler input=new KeyHandler();
            press(input,KEYS[direction]);press(input,KeyEvent.VK_J);
            actor.update(1.0/60,input.poll(),world,List.of());
            check(actor.state==Player.State.ATTACK&&actor.dir==Player.Dir.values()[direction],"Movement+attack used old facing");
            input.clear();press(input,KEYS[(direction+1)%4]);
            actor.update(1.0/60,input.poll(),world,List.of());
            check(actor.dir==Player.Dir.values()[direction],"Facing changed during an active swing");

            actor=new Player(World.SPAWN_X,World.SPAWN_Y);
            actor.dir=Player.Dir.values()[direction];
            double dx=FORWARD[direction][0],dy=FORWARD[direction][1];
            Enemy ahead=new WildBoar(actor.x+dx*32,actor.y+dy*32,0);
            Enemy behind=new WildBoar(actor.x-dx*32,actor.y-dy*32,0);
            Enemy distant=new WildBoar(actor.x+dx*100,actor.y+dy*100,0);
            input=new KeyHandler();press(input,KeyEvent.VK_J);
            actor.update(1.0/60,input.poll(),world,List.of(ahead,behind,distant));
            for(int tick=0;tick<4;tick++)actor.update(1.0/60,input.poll(),world,List.of(ahead,behind,distant));
            check(ahead.health()==3,"Windup dealt damage");
            for(int tick=0;tick<40;tick++)actor.update(1.0/60,input.poll(),world,List.of(ahead,behind,distant));
            check(ahead.health()==2,"Visible forward sweep must damage exactly once");
            check(behind.health()==3&&distant.health()==3,"Swing damaged a target behind or outside reach");

            BufferedImage windup=renderAttack(Player.Dir.values()[direction],0.02);
            BufferedImage contact=renderAttack(Player.Dir.values()[direction],0.20);
            int changed=0;
            for(int y=0;y<windup.getHeight();y++)for(int x=0;x<windup.getWidth();x++)
                if(windup.getRGB(x,y)!=contact.getRGB(x,y))changed++;
            check(changed>100,"Directional attack has no readable weapon motion");
            BufferedImage translated=new BufferedImage(160,160,BufferedImage.TYPE_INT_ARGB);
            Graphics2D camera=translated.createGraphics();
            try{
                Player shifted=new Player(198,206);
                shifted.dir=Player.Dir.values()[direction];shifted.state=Player.State.ATTACK;shifted.stateTime=0.20;
                shifted.draw(camera,128,96,1);
            }finally{camera.dispose();}
            boolean cameraMatches=true;
            for(int y=0;y<160;y++)for(int x=0;x<160;x++)
                if(translated.getRGB(x,y)!=contact.getRGB(x,y))cameraMatches=false;
            check(cameraMatches,"Weapon and body disagree under camera translation");
            // Verify the 96 current body poses, rather than removed masked idle copies.
            for(int stage=0;stage<SpearAttack.STAGES;stage++)for(int phase=0;phase<CombatBody.PHASES;phase++){
                BufferedImage body=CombatBody.at(Player.Dir.values()[direction],stage,phase).image();
                int embeddedBlade=0;
                for(int y=0;y<32;y++)for(int x=0;x<64;x++)if(cyan(body.getRGB(x,y)))embeddedBlade++;
                check(embeddedBlade==0,"Combat body contains an embedded second spearhead");
            }
        }
        BufferedImage downward=renderAttack(Player.Dir.DOWN,0.20);
        boolean belowFeet=false;
        for(int y=111;y<160;y++)for(int x=0;x<160;x++)if(cyan(downward.getRGB(x,y)))belowFeet=true;
        check(belowFeet,"Front-facing spear was clipped above the feet");
        BufferedImage upward=renderAttack(Player.Dir.UP,0.18);
        boolean aboveHead=false;
        for(int y=0;y<60;y++)for(int x=0;x<160;x++)if(cyan(upward.getRGB(x,y)))aboveHead=true;
        check(aboveHead,"Back-facing spear is hidden behind the body");

        Player actor=new Player(World.SPAWN_X,World.SPAWN_Y);
        KeyHandler input=new KeyHandler();actor.dir=Player.Dir.RIGHT;
        press(input,KeyEvent.VK_J);actor.update(1.0/60,input.poll(),world,List.of());input.clear();
        for(int tick=0;tick<12;tick++)actor.update(1.0/60,input.poll(),world,List.of());
        press(input,KeyEvent.VK_W);press(input,KeyEvent.VK_J);
        actor.update(1.0/60,input.poll(),world,List.of());
        check(actor.dir==Player.Dir.RIGHT,"Buffered attack turned the current swing");
        input.clear();
        boolean restarted=false;
        for(int tick=0;tick<12;tick++){
            double previous=actor.stateTime;
            actor.update(1.0/60,input.poll(),world,List.of());
            if(actor.state==Player.State.ATTACK&&actor.stateTime<previous){restarted=true;break;}
        }
        check(restarted&&actor.dir==Player.Dir.UP,"Recovery press was lost");
        for(int tick=0;tick<40;tick++)actor.update(1.0/60,input.poll(),world,List.of());
        check(actor.state==Player.State.IDLE,"One buffered press repeated indefinitely");

        actor=new Player(World.SPAWN_X,World.SPAWN_Y);
        actor.dir=Player.Dir.DOWN;input=new KeyHandler();press(input,KeyEvent.VK_J);
        Enemy victim=new WildBoar(actor.x,actor.y+32,0);
        actor.update(1.0/60,input.poll(),world,List.of(victim));input.clear();
        actor.update(0.03,input.poll(),world,List.of(victim));
        actor.damage(1,actor.x,actor.y-10);
        for(int tick=0;tick<40;tick++)actor.update(1.0/60,input.poll(),world,List.of(victim));
        check(victim.health()==3&&actor.state==Player.State.IDLE,"Interrupted windup produced delayed damage");
        actor.damage(100,0,0);
        // Remove the earlier test's remaining immunity before testing death input.
        actor.invincible=0;actor.damage(100,0,0);
        press(input,KeyEvent.VK_J);
        actor.update(0.1,input.poll(),world,List.of(victim));
        check(actor.state==Player.State.DEAD&&victim.health()==3,"Dead actor attacked");
        Fx.clear();
        previews();
        return checks;
    }
    // [BLOCK: TEST HELPER] CombatTests.cyan - Handle cyan for CombatTests.
    private static boolean cyan(int pixel){
        int r=(pixel>>16)&255,g=(pixel>>8)&255,b=pixel&255;
        return (pixel>>>24)>128&&g>110&&b>140&&b>r*1.10;
    }
    // [BLOCK: TEST HELPER] CombatTests.renderAttack - Handle render attack for CombatTests.
    private static BufferedImage renderAttack(Player.Dir direction,double time){
        BufferedImage image=new BufferedImage(160,160,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();
        try{
            Player actor=new Player(70,110);
            actor.dir=direction;actor.state=Player.State.ATTACK;actor.stateTime=time;
            actor.draw(g,0,0,1);
        }finally{g.dispose();}
        return image;
    }
    // [BLOCK: TEST HELPER] CombatTests.drawActor - Render draw actor using the current graphics state.
    private static void drawActor(Graphics2D target,int x,int y,Player.Dir dir,double time){
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.translate(x,y);g.scale(2,2);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            Player actor=new Player(60,85);
            actor.dir=dir;actor.state=time<SpearAttack.duration(0)?Player.State.ATTACK:Player.State.IDLE;
            actor.stateTime=Math.min(time,SpearAttack.duration(0));actor.draw(g,0,0,1);
        }finally{g.dispose();}
    }
    // [BLOCK: TEST HELPER] CombatTests.previews - Handle previews for CombatTests.
    private static void previews()throws Exception{
        double[] phases={0,0.05,0.10,0.15,0.20,0.25,0.30,0.38};
        BufferedImage strip=new BufferedImage(1920,1000,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=strip.createGraphics();
        try{
            g.setColor(new Color(0x263A35));g.fillRect(0,0,1920,1000);
            g.setFont(new Font("Monospaced",Font.PLAIN,14));g.setColor(new Color(0xF1C979));
            for(int row=0;row<4;row++)for(int col=0;col<phases.length;col++){
                g.drawString(Player.Dir.values()[row]+" / "+phases[col]+"s",col*240+12,row*250+18);
                drawActor(g,col*240,row*250+25,Player.Dir.values()[row],phases[col]);
            }
        }finally{g.dispose();}
        check(ImageIO.write(strip,"png",new File("build/preview-spear-swings.png")),"Spear strip could not be written");
        Iterator<ImageWriter> writers=ImageIO.getImageWritersByFormatName("gif");
        if(!writers.hasNext())throw new IllegalStateException("GIF writer missing");
        ImageWriter writer=writers.next();
        try(ImageOutputStream output=ImageIO.createImageOutputStream(new File("build/preview-spear-swings.gif"))){
            writer.setOutput(output);writer.prepareWriteSequence(null);
            for(int frame=0;frame<30;frame++){
                BufferedImage image=new BufferedImage(960,250,BufferedImage.TYPE_INT_RGB);
                g=image.createGraphics();
                try{
                    g.setColor(new Color(0x263A35));g.fillRect(0,0,960,250);
                    g.setColor(new Color(0xF1C979));g.setFont(new Font("Monospaced",Font.PLAIN,14));
                    for(int dir=0;dir<4;dir++){
                        g.drawString(Player.Dir.values()[dir].name(),dir*240+95,18);
                        drawActor(g,dir*240,25,Player.Dir.values()[dir],frame/25.0);
                    }
                }finally{g.dispose();}
                IIOMetadata metadata=writer.getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier.createFromRenderedImage(image),writer.getDefaultWriteParam());
                String format=metadata.getNativeMetadataFormatName();
                IIOMetadataNode root=(IIOMetadataNode)metadata.getAsTree(format);
                IIOMetadataNode control=(IIOMetadataNode)root.getElementsByTagName("GraphicControlExtension").item(0);
                control.setAttribute("disposalMethod","none");control.setAttribute("userInputFlag","FALSE");
                control.setAttribute("transparentColorFlag","FALSE");control.setAttribute("delayTime","4");control.setAttribute("transparentColorIndex","0");
                if(frame==0){
                    IIOMetadataNode extensions=new IIOMetadataNode("ApplicationExtensions"),loop=new IIOMetadataNode("ApplicationExtension");
                    loop.setAttribute("applicationID","NETSCAPE");loop.setAttribute("authenticationCode","2.0");
                    loop.setUserObject(new byte[]{1,0,0});extensions.appendChild(loop);root.appendChild(extensions);
                }
                metadata.setFromTree(format,root);
                writer.writeToSequence(new IIOImage(image,null,metadata),writer.getDefaultWriteParam());
            }
            writer.endWriteSequence();
        }finally{writer.dispose();}
        check(new File("build/preview-spear-swings.gif").length()>0,"Animated attack preview missing");
    }
    // [BLOCK: TEST HELPER] CombatTests.press - Handle press for CombatTests.
    private static void press(KeyHandler input,int code){
        input.keyPressed(new KeyEvent(SOURCE,KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));
    }
    // [BLOCK: TEST HELPER] CombatTests.check - Handle check for CombatTests.
    private static void check(boolean valid,String message){checks++;if(!valid)throw new AssertionError(message);}
}
