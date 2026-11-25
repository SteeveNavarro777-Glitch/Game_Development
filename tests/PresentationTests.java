/* FILE PURPOSE: REGRESSION TEST | Identical equipped body proportions, forward grips and continuous river/pond presentation.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

/** Compare actual rendered character pixels and registered water at the previously visible seam. */
public final class PresentationTests {
    private static int checks;
    // [BLOCK: TEST UTILITY] PresentationTests.PresentationTests - Prevent instances of this suite.
    private PresentationTests(){}
    // [BLOCK: TEST ENTRY] PresentationTests.main - Run equipment proportions and river appearance checks independently.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Presentation checks passed: "+run());}
    // [BLOCK: PRESENTATION SUITE] PresentationTests.run - Verify the live renderer and export the previously mismatched scenes.
    static int run()throws Exception{checks=0;bodies();river();preview();Fx.clear();return checks;}
    // [BLOCK: ACTUAL BODY CONSISTENCY] PresentationTests.bodies - Compare Agyu's red cap geometry across items, movement and guard without including longer weapons.
    private static void bodies(){
        for(Player.Dir dir:Player.Dir.values())for(Player.State state:new Player.State[]{Player.State.IDLE,Player.State.WALK,Player.State.RUN,Player.State.GUARD}){
            Set<Integer> expected=null;
            for(int slot:new int[]{1,0,2,3,4,8}){
                Player actor=new Player(64,100);actor.dir=dir;actor.state=state;actor.animTime=.21;actor.hotbar().select(slot);
                if(state!=Player.State.IDLE){actor.vx=30;actor.vy=0;}actor.savePrev();
                BufferedImage image=new BufferedImage(128,128,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
                try{actor.draw(g,0,0,1);}finally{g.dispose();}
                Set<Integer> cap=new HashSet<>();
                for(int y=44;y<70;y++)for(int x=44;x<85;x++){int rgb=image.getRGB(x,y),r=rgb>>16&255,green=rgb>>8&255,b=rgb&255;
                    if((rgb>>>24)>128&&r>145&&green<75&&b<75)cap.add(y*128+x);}
                check(cap.size()>12,"Agyu cap disappeared: "+dir+" "+state+" slot"+slot);
                if(expected==null)expected=cap;else check(cap.equals(expected),"Equipment changes Agyu's body/head proportions: "+dir+" "+state+" slot"+slot);
            }
        }
        for(Player.Dir dir:Player.Dir.values()){
            BufferedImage first=HeldItemArt.guardFrame(Inventory.Item.SPEAR,dir,0,true),second=HeldItemArt.guardFrame(Inventory.Item.SPEAR,dir,8,true);
            check(first==second,"Guard movement splices non-authored feet into the supplied pose");
        }
    }
    // [BLOCK: SHARED TEXTURE SEAM] PresentationTests.river - Check matching outlet pixels, rich water detail, animation and prepared topology variants.
    private static void river(){
        Set<Integer> colors=new HashSet<>();Set<Long> phases=new HashSet<>();
        for(int frame=0;frame<WaterfallArt.FRAMES;frame++){
            BufferedImage cove=WaterfallArt.cove(frame),mouth=WaterfallArt.mouth(frame);int matching=0;
            for(int y=330;y<=346;y++)if(colorDistance(cove.getRGB(0,y),mouth.getRGB(172,y-235))<8)matching++;
            check(matching>=12,"Painted waterfall outlet still meets a different water palette");
            long hash=1;for(int v=0;v<8;v++){
                BufferedImage water=WaterfallArt.water(frame,v);
                check(water==WaterfallArt.water(frame,v)&&water.getWidth()==32&&water.getHeight()==32,"River variants are not cached native tiles");
                for(int y=0;y<32;y++)for(int x=0;x<32;x++){int rgb=water.getRGB(x,y);colors.add(rgb);hash=31*hash+rgb;}
            }phases.add(hash);
        }
        check(colors.size()>150&&phases.size()==8,"River is flat, repetitive or lacks moving water");
        World world=new World(7);for(int r=84;r<93;r++)for(int c=163;c<169;c++)if(world.tile[r][c]==World.WATER)
            check(world.solidTile(c,r),"Tapered river lost water collision");
        BufferedImage ground=new BufferedImage(32,32,BufferedImage.TYPE_INT_RGB);boolean checked=false;
        for(int r=46;r<86&&!checked;r++)for(int c=160;c<192&&!checked;c++)if(world.tile[r][c]==World.MOUNTAIN){
            Graphics2D g=ground.createGraphics();try{world.drawGround(g,c*32,r*32,31,31,0);}finally{g.dispose();}
            int greenPixels=0;for(int y=0;y<32;y++)for(int x=0;x<32;x++){int rgb=ground.getRGB(x,y);if((rgb>>8&255)>(rgb>>16&255)+10)greenPixels++;}
            check(greenPixels>200,"Gray square mountain underlay is still visible");checked=true;
        }check(checked,"No mountain cell checked");
    }
    // [BLOCK: PIXEL COLOR COMPARISON] PresentationTests.colorDistance - Bound the largest channel change at the artwork-to-river join.
    private static int colorDistance(int a,int b){int difference=0;for(int shift:new int[]{0,8,16})difference=Math.max(difference,Math.abs((a>>shift&255)-(b>>shift&255)));return difference;}
    // [BLOCK: ACTUAL RIVER CAMERA] PresentationTests.preview - Export the real game at the waterfall outlet and its accessible southern bank.
    private static void preview()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(960,720);
        TitleMenuTests.startGame(panel);
        Player actor=(Player)get(panel,"player");World world=(World)get(panel,"world");
        actor.x=actor.px=MountainRiver.COVE_X-60;actor.y=actor.py=MountainRiver.COVE_Y+440;actor.dir=Player.Dir.LEFT;
        check(!world.blocked(actor.feetBox()),"Review camera embeds Agyu in the outlet bank");
        double cx=MountainRiver.COVE_X-256,cy=MountainRiver.COVE_Y+190;
        set(panel,"camX",cx);set(panel,"prevCamX",cx);set(panel,"camY",cy);set(panel,"prevCamY",cy);set(panel,"alpha",1);
        BufferedImage frame=new BufferedImage(960,720,BufferedImage.TYPE_INT_RGB);Graphics2D g=frame.createGraphics();
        try{panel.paint(g);}finally{g.dispose();}ImageIO.write(frame,"png",new File("build/preview-river-connection.png"));
    }
    // [BLOCK: TEST SESSION ACCESS] PresentationTests.get - Read the real session state without exposing production mutation.
    private static Object get(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    // [BLOCK: REVIEW CAMERA] PresentationTests.set - Place the actual interpolated camera for a deterministic seam view.
    private static void set(Object object,String name,double value)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);f.setDouble(object,value);}
    // [BLOCK: ASSERTION] PresentationTests.check - Count and explain one presentation property.
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
}
