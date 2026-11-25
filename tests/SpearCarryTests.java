/* FILE PURPOSE: REGRESSION TEST | Whole original idle, walk, run, reaction and guard sprites.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

/** Prevents a later equipment change from resizing, masking or replacing Agyu's authored limbs. */
public final class SpearCarryTests {
    private static final int OFFSET_X=HeldItemArt.FOOT_X-32,OFFSET_Y=HeldItemArt.FOOT_Y-63;
    private static int checks;
    private SpearCarryTests(){}

    // [BLOCK: TEST ENTRY] SpearCarryTests.main - Run the original-sprite integration checks independently.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Original Agyu sprite checks passed: "+run());}

    // [BLOCK: ORIGINAL SPRITE SUITE] SpearCarryTests.run - Verify all authored states and export their active cycles.
    static int run()throws Exception{
        checks=0;cycles();guards();potions();preview();return checks;
    }

    // [BLOCK: COMPLETE CYCLES] SpearCarryTests.cycles - Map all eight source frames without body or foot composition.
    private static void cycles(){
        for(Player.Dir dir:Player.Dir.values()){
            Set<Long> walks=new HashSet<>(),runs=new HashSet<>();
            for(int phase=0;phase<HeldItemArt.POSES;phase++){
                BufferedImage idle=HeldItemArt.frame(Inventory.Item.SPEAR,dir,phase);
                BufferedImage walk=HeldItemArt.walkFrame(Inventory.Item.SPEAR,dir,phase);
                BufferedImage run=HeldItemArt.runFrame(Inventory.Item.SPEAR,dir,phase);
                checkWhole(idle,Assets.idle[dir.ordinal()][phase],"idle "+dir+" "+phase);
                checkWhole(walk,Assets.walk[dir.ordinal()][phase],"walk "+dir+" "+phase);
                checkWhole(run,Assets.run[dir.ordinal()][phase],"run "+dir+" "+phase);
                check(idle==HeldItemArt.frame(Inventory.Item.SHIELD,dir,phase)
                                &&idle==HeldItemArt.frame(null,dir,phase),
                        "Equipment selection replaced the original body");
                check(walk==HeldItemArt.walkFrame(Inventory.Item.SPEAR,dir,phase)
                                &&run==HeldItemArt.runFrame(Inventory.Item.SPEAR,dir,phase),
                        "Movement lookup allocates while rendering");
                check(HeldItemArt.motionPhase(phase*5.0,false)==phase
                                &&HeldItemArt.motionPhase(phase*6.0,true)==phase,
                        "Eight-frame distance phase is incomplete");
                walks.add(hash(walk));runs.add(hash(run));
            }
            check(walks.size()>=4&&runs.size()>=4,"Original movement strip is not visibly advancing: "+dir);
            for(int phase=0;phase<Assets.hurt[dir.ordinal()].length;phase++)
                checkWhole(HeldItemArt.hurtFrame(dir,phase),Assets.hurt[dir.ordinal()][phase],
                        "reaction "+dir+" "+phase);
        }
    }

    // [BLOCK: WHOLE GUARDS] SpearCarryTests.guards - Keep each supplied shield-and-forward-spear pose stable while moving.
    private static void guards(){
        for(Player.Dir dir:Player.Dir.values()){
            BufferedImage guard=HeldItemArt.guardFrame(Inventory.Item.SPEAR,dir,0,false);
            checkWhole(guard,ShieldGuard.bodies[dir.ordinal()][0],"guard "+dir);
            check(guard==HeldItemArt.guardFrame(Inventory.Item.SPEAR,dir,48,true)
                            &&guard==HeldItemArt.guardFrame(Inventory.Item.SHIELD,dir,96,true),
                    "Guard movement grafts replacement feet: "+dir);
        }
    }

    // [BLOCK: NON-DESTRUCTIVE POTIONS] SpearCarryTests.potions - Add bottles without changing the original character pixels.
    private static void potions(){
        for(Inventory.Item potion:new Inventory.Item[]{Inventory.Item.HEALTH_POTION,
                Inventory.Item.ENERGY_POTION,Inventory.Item.SHIELD_POTION}){
            for(Player.Dir dir:Player.Dir.values()){
                BufferedImage base=HeldItemArt.frame(Inventory.Item.SPEAR,dir,0);
                BufferedImage held=HeldItemArt.frame(potion,dir,0);
                int preserved=0,originalOpaque=0;
                for(int y=0;y<Assets.idle[dir.ordinal()][0].getHeight();y++)for(int x=0;x<64;x++){
                    int original=Assets.idle[dir.ordinal()][0].getRGB(x,y);
                    if((original>>>24)!=0){originalOpaque++;if(held.getRGB(x+OFFSET_X,y+OFFSET_Y)==original)preserved++;}
                }
                check(originalOpaque>0&&preserved==originalOpaque,
                        "Potion overlay changed Agyu pixels: "+potion+" "+dir+" ("+preserved+"/"+originalOpaque+" preserved)");
                check(hash(base)!=hash(held)&&hash(held)!=hash(HeldItemArt.frame(potion,dir,5)),
                        "Selected potion does not move from hand to mouth");
            }
        }
    }

    // [BLOCK: EXACT SOURCE PLACEMENT] SpearCarryTests.checkWhole - Verify native size, anchor and opaque source pixels.
    private static void checkWhole(BufferedImage wrapped,BufferedImage source,String label){
        check(wrapped.getWidth()==HeldItemArt.WIDTH&&wrapped.getHeight()==HeldItemArt.HEIGHT,
                "Wrong prepared canvas: "+label);
        int sourceOpaque=0,preparedOpaque=0;
        for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++){
            int pixel=source.getRGB(x,y);
            if((pixel>>>24)==255){
                sourceOpaque++;
                check(wrapped.getRGB(x+OFFSET_X,y+OFFSET_Y)==pixel,"Original pixel changed: "+label);
            }
        }
        for(int y=0;y<wrapped.getHeight();y++)for(int x=0;x<wrapped.getWidth();x++)
            if((wrapped.getRGB(x,y)>>>24)==255)preparedOpaque++;
        check(preparedOpaque==sourceOpaque,"Whole-frame wrapper added or removed opaque pixels: "+label);
        for(int x=0;x<wrapped.getWidth();x++)
            check((wrapped.getRGB(x,0)>>>24)==0&&(wrapped.getRGB(x,wrapped.getHeight()-1)>>>24)==0,
                    "Prepared frame clips vertically: "+label);
    }

    // [BLOCK: ORIGINAL CYCLE PREVIEW] SpearCarryTests.preview - Display all eight complete authored frames at native proportions.
    private static void preview()throws Exception{
        BufferedImage image=new BufferedImage(1280,800,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();
        try{
            g.setColor(new Color(0x243632));g.fillRect(0,0,image.getWidth(),image.getHeight());
            g.setFont(new Font("Monospaced",Font.PLAIN,12));
            Player.Dir[] dirs=Player.Dir.values();
            for(int row=0;row<8;row++){
                Player.Dir dir=dirs[row%4];boolean running=row>=4;
                for(int phase=0;phase<8;phase++){
                    int x=phase*160,y=row*100;
                    g.setColor(new Color(0xEEE0BF));g.drawString(dir+" "+(running?"RUN ":"WALK ")+(phase+1),x+4,y+14);
                    BufferedImage sprite=running?HeldItemArt.runFrame(Inventory.Item.SPEAR,dir,phase)
                            :HeldItemArt.walkFrame(Inventory.Item.SPEAR,dir,phase);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g.drawImage(sprite,x+16,y-24,128,128,null);
                }
            }
        }finally{g.dispose();}
        ImageIO.write(image,"png",new File("build/preview-original-player-sprites.png"));
    }

    // [BLOCK: RGBA HASH] SpearCarryTests.hash - Compare complete cached frames without subjective shape rules.
    private static long hash(BufferedImage image){long value=1;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)value=31*value+image.getRGB(x,y);return value;}
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
}
