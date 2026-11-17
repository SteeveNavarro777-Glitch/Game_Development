/* FILE PURPOSE: REGRESSION TEST | Visible selected items, forward guard spear, drink timing/interruption, cached walking and actual-game previews.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import javax.imageio.ImageIO;

/** Tests actual gameplay and renderer paths rather than a separate animation mockup. */
public final class EquipmentTests {
    private static final double DT=1.0/60;
    private static final KeyHandler.Input NONE=new KeyHandler.Input(false,false,false,false,false,false,0);
    private static int checks;
    private static World world;
    // [BLOCK: STATIC TEST] EquipmentTests.EquipmentTests - Prevent instances of the regression suite.
    private EquipmentTests(){}
    // [BLOCK: TEST ENTRY] EquipmentTests.main - Run equipment and drinking checks independently.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Equipment/drinking checks passed: "+run());}
    // [BLOCK: EQUIPMENT SUITE] EquipmentTests.run - Verify registered art, sip semantics, input interruptions and actual render previews.
    static int run()throws Exception{checks=0;world=new World(7);art();drinks();interruptions();selection();panel();previews();Fx.clear();return checks;}
    // [BLOCK: CACHED ART / FORWARD GUARD] EquipmentTests.art - Check provenance, unclipped composites, mirrored guard tips and pose variation.
    private static void art()throws Exception{
        List<String> rows=Files.readAllLines(Path.of("res/player/equipment/frames.csv"));check(rows.size()==25,"Authored holding/drinking poses missing");
        String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(Path.of("art/source/player/agyu-held-items-v1.png"))));
        for(String row:rows.subList(1,rows.size()))check(row.split(",")[10].equals(hash),"Held body source hash mismatch");
        Inventory.Item[] items={Inventory.Item.SPEAR,Inventory.Item.SHIELD,Inventory.Item.HEALTH_POTION,Inventory.Item.ENERGY_POTION,Inventory.Item.SHIELD_POTION,null};
        for(Inventory.Item item:items)for(Player.Dir dir:Player.Dir.values())for(int pose=0;pose<8;pose++){
            BufferedImage image=HeldItemArt.frame(item,dir,pose);check(image==HeldItemArt.frame(item,dir,pose),"Selection prepares images during painting");
            boolean clear=true;for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,0)>>>24)!=0||(image.getRGB(x,image.getHeight()-1)>>>24)!=0)clear=false;
            for(int y=0;y<image.getHeight();y++)if((image.getRGB(0,y)>>>24)!=0||(image.getRGB(image.getWidth()-1,y)>>>24)!=0)clear=false;
            check(clear,"Held object or body touches/clips cache boundary: "+item+" "+dir+" "+pose);
        }
        for(Player.Dir dir:new Player.Dir[]{Player.Dir.LEFT,Player.Dir.RIGHT}){
            BufferedImage image=ShieldGuard.bodies[dir.ordinal()][0];int sum=0,n=0;
            for(int y=0;y<64;y++)for(int x=0;x<64;x++){int c=image.getRGB(x,y),r=(c>>16)&255,g=(c>>8)&255,b=c&255;
                if((c>>>24)>128&&b>130&&g>110&&b>r*1.4){sum+=x;n++;}}
            check(n>3&&(dir==Player.Dir.RIGHT?sum/(double)n>44:sum/(double)n<20),"Guard spear tip remains behind Agyu");
        }
        check(imageHash(HeldItemArt.frame(Inventory.Item.HEALTH_POTION,Player.Dir.RIGHT,0))!=imageHash(HeldItemArt.frame(Inventory.Item.HEALTH_POTION,Player.Dir.RIGHT,5)),"Sip has no physical body motion");
    }
    // [BLOCK: SIP COMMIT RULES] EquipmentTests.drinks - Start, raise, sip and lower once without premature resource changes or double spending.
    private static void drinks(){
        Player actor=actor();check(actor.usePotion(Inventory.Item.SHIELD_POTION)&&actor.drinking(),"Validated potion cannot begin");
        check(actor.heldItem()==Inventory.Item.SHIELD_POTION&&actor.inventory().count(Inventory.Item.SHIELD_POTION)==2&&actor.protection()==0,"Starting drink changes supply or fails to hold bottle");
        tick(actor,.18,NONE);check(actor.drinkPose()==3&&actor.protection()==0,"Raise pose or deferred sip missing");
        tick(actor,.24,NONE);check(actor.protection()==0,"Potion applied before mouth tilt");tick(actor,.04,NONE);
        check(actor.protection()==3&&actor.inventory().count(Inventory.Item.SHIELD_POTION)==1&&actor.drinkPose()==5,"Sip fails to commit exactly at tilt");
        tick(actor,.42,NONE);check(!actor.drinking()&&actor.inventory().count(Inventory.Item.SHIELD_POTION)==1,"Lowering repeats consumption or leaves action stuck");
        actor=actor();actor.usePotion(Inventory.Item.SHIELD_POTION);tick(actor,2,NONE);
        check(actor.protection()==3&&actor.inventory().count(Inventory.Item.SHIELD_POTION)==1&&!actor.drinking(),"Coarse step misses or duplicates sip");
        actor=actor();EnergyMeter meter;try{meter=(EnergyMeter)get(actor,"energy");}catch(Exception e){throw new AssertionError(e);}meter.spend(2);
        actor.usePotion(Inventory.Item.ENERGY_POTION);tick(actor,1,NONE);
        check(actor.inventory().count(Inventory.Item.ENERGY_POTION)==3&&actor.energy()==100,"A now-full resource wastes an energy potion at sip");
    }
    // [BLOCK: INTERRUPTION / INPUT PRIORITY] EquipmentTests.interruptions - Cancel an uncommitted bottle on guard, hit, slot change, jump or dash.
    private static void interruptions(){
        for(int action=0;action<5;action++){
            Player actor=actor();actor.usePotion(Inventory.Item.SHIELD_POTION);tick(actor,.18,NONE);
            if(action==0)tick(actor,DT,new KeyHandler.Input(false,false,false,false,false,true,0));
            if(action==1){actor.damage(1,actor.x+30,actor.y);Fx.clear();}
            if(action==2){actor.hotbar().select(0);tick(actor,DT,NONE);}
            if(action==3)tick(actor,DT,new KeyHandler.Input(false,false,false,false,false,false,KeyHandler.JUMP));
            if(action==4)tick(actor,DT,new KeyHandler.Input(false,false,false,false,false,false,KeyHandler.DASH));
            tick(actor,1,NONE);check(!actor.drinking()&&actor.inventory().count(Inventory.Item.SHIELD_POTION)==2&&actor.protection()==0,"Interrupted drink spends a potion: "+action);Fx.clear();
        }
        Player actor=actor();actor.usePotion(Inventory.Item.SHIELD_POTION);tick(actor,.5,NONE);actor.damage(1,actor.x+30,actor.y);Fx.clear();tick(actor,1,NONE);
        check(actor.inventory().count(Inventory.Item.SHIELD_POTION)==1&&actor.protection()==2,"A post-sip hit refunds or repeats use");
    }
    // [BLOCK: SELECTED EQUIPMENT] EquipmentTests.selection - Keep actual held items aligned with slots, depleted supplies, guard and movement.
    private static void selection(){
        Player actor=actor();for(int slot=0;slot<5;slot++){actor.hotbar().select(slot);check(actor.heldItem()==actor.hotbar().item(slot),"Selected item is not held");}
        actor.hotbar().select(8);check(actor.heldItem()==null,"Empty slot leaves a held weapon");actor.hotbar().select(4);
        while(actor.inventory().count(Inventory.Item.SHIELD_POTION)>1)actor.inventory().consume(Inventory.Item.SHIELD_POTION);
        actor.usePotion(Inventory.Item.SHIELD_POTION);tick(actor,.5,NONE);check(actor.heldItem()==Inventory.Item.SHIELD_POTION,"Last bottle vanishes before lowering");
        tick(actor,.4,NONE);check(actor.heldItem()==null,"Empty stack still holds a full bottle");
        actor=actor();actor.usePotion(Inventory.Item.SHIELD_POTION);tick(actor,.18,NONE);KeyHandler selection=new KeyHandler();selection.selectSlot(0);tick(actor,DT,selection.poll());
        check(!actor.drinking()&&actor.itemMessageTime()==0,"Slot change keeps an unrelated potion status label");
        actor=actor();actor.usePotion(Inventory.Item.SHIELD_POTION);double x=actor.x;
        for(int i=0;i<16;i++)tick(actor,DT,new KeyHandler.Input(false,false,false,true,false,false,0));
        check(actor.x>x&&actor.x-x<Player.WALK_SPEED*.3&&actor.drinking(),"Drinking movement is frozen or sprints");
        check(HotbarHud.top(360)+HotbarHud.CELL+6<360&&HotbarHud.WIDTH<400,"Hotbar overlaps bottom edge or fills the viewport");
        for(int i=0;i<9;i++)check(HotbarHud.slotAt(HotbarHud.left(480)+i*(HotbarHud.CELL+HotbarHud.GAP)+3,HotbarHud.top(360)+3,480,360)==i,"Refined slot hit geometry differs from rendering");Fx.clear();
    }
    // [BLOCK: ACTUAL SESSION PAUSE] EquipmentTests.panel - Freeze drinking progress during modal screens and preserve selected equipment after restart.
    @SuppressWarnings("unchecked")
    private static void panel()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);tap(panel,KeyEvent.VK_ENTER);
        Player actor=(Player)get(panel,"player");tap(panel,KeyEvent.VK_5);tap(panel,KeyEvent.VK_F);
        check(actor.drinking(),"F does not begin a visible drinking action");
        for(int key:new int[]{KeyEvent.VK_M,KeyEvent.VK_TAB,KeyEvent.VK_ESCAPE}){
            tap(panel,key);double progress=actor.drinkProgress();int count=actor.inventory().count(Inventory.Item.SHIELD_POTION);panel.update(2);
            check(actor.drinkProgress()==progress&&actor.inventory().count(Inventory.Item.SHIELD_POTION)==count,"Overlay advances potion action");tap(panel,key);
        }
        for(int i=0;i<55;i++)panel.update(DT);check(actor.protection()==3&&!actor.drinking(),"Resumed animation fails to finish");
        BufferedImage image=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();try{panel.paint(g);}finally{g.dispose();}
        ImageIO.write(image,"png",new File("build/preview-equipment-hotbar.png"));
        actor.damage(4,actor.x+30,actor.y);Fx.clear();for(int i=0;i<22;i++)panel.update(DT);tap(panel,KeyEvent.VK_H);
        double frozen=actor.drinkProgress();for(FocusListener listener:panel.getFocusListeners())listener.focusLost(new FocusEvent(panel,FocusEvent.FOCUS_LOST));
        panel.update(2);check(actor.drinking()&&actor.drinkProgress()==frozen,"Focus loss advances or cancels a frozen sip");tap(panel,KeyEvent.VK_ENTER);
        for(int i=0;i<55;i++)panel.update(DT);check(actor.health()==5,"Focus resume loses the healing sip");
        TankBoss boss=(TankBoss)((List<Enemy>)get(panel,"enemies")).stream().filter(e->e instanceof TankBoss).findFirst().orElseThrow();
        actor.x=actor.px=boss.x+60;actor.y=actor.py=boss.y+40;panel.update(DT);check(boss.engaged(),"Boss HUD fixture did not engage");
        BufferedImage hud=new BufferedImage(480,360,BufferedImage.TYPE_INT_RGB);g=hud.createGraphics();
        try{java.lang.reflect.Method draw=GamePanel.class.getDeclaredMethod("drawHud",Graphics2D.class);draw.setAccessible(true);draw.invoke(panel,g);}finally{g.dispose();}
        for(int x=140;x<340;x++)check((hud.getRGB(x,280)&0xFFFFFF)==0x8ABD57,"Selected-item label overlaps boss health bar");
        set(panel,"camX",actor.x-240);set(panel,"prevCamX",actor.x-240);set(panel,"camY",actor.y-200);set(panel,"prevCamY",actor.y-200);
        g=image.createGraphics();try{panel.paint(g);}finally{g.dispose();}ImageIO.write(image,"png",new File("build/preview-equipment-boss-hud.png"));Fx.clear();
    }
    // [BLOCK: ACTUAL PLAYER VISUALS] EquipmentTests.previews - Export all held items and six real drinking phases in every direction.
    private static void previews()throws Exception{
        BufferedImage items=new BufferedImage(960,600,BufferedImage.TYPE_INT_RGB),drinks=new BufferedImage(960,600,BufferedImage.TYPE_INT_RGB);
        Graphics2D ig=items.createGraphics(),dg=drinks.createGraphics();
        try{
            ig.setColor(new Color(0x263A35));ig.fillRect(0,0,960,600);dg.setColor(new Color(0x263A35));dg.fillRect(0,0,960,600);
            for(Player.Dir dir:Player.Dir.values())for(int col=0;col<6;col++){
                Player actor=actor();actor.dir=dir;actor.hotbar().select(col==5?8:col);cell(ig,actor,col,dir.ordinal(),dir+" / "+(col==5?"EMPTY":actor.heldItem().name()));
                actor=actor();actor.dir=dir;actor.usePotion(Inventory.Item.SHIELD_POTION);
                tick(actor,new double[]{.04,.18,.32,.48,.67,.81}[col],NONE);cell(dg,actor,col,dir.ordinal(),dir+" / "+col+" / "+actor.drinkPose());
            }
        }finally{ig.dispose();dg.dispose();}
        ImageIO.write(items,"png",new File("build/preview-equipped-items.png"));ImageIO.write(drinks,"png",new File("build/preview-drinking-motion.png"));
    }
    // [BLOCK: REGISTERED PREVIEW CELL] EquipmentTests.cell - Render the actual Player with the normal scale and item compositor in a labeled comparison cell.
    private static void cell(Graphics2D target,Player actor,int col,int row,String label){
        Graphics2D g=(Graphics2D)target.create();try{g.translate(col*160,row*150);g.setFont(new Font("Monospaced",Font.PLAIN,9));g.setColor(new Color(0xE2D7B4));g.drawString(label,5,15);g.scale(2,2);actor.draw(g,(int)actor.x-40,(int)actor.y-65,1);}finally{g.dispose();}
    }
    // [BLOCK: RENDER DIFFERENCE] EquipmentTests.imageHash - Compare complete RGBA images without relying on subjective pose labels.
    private static long imageHash(BufferedImage image){long value=1;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)value=value*31+image.getRGB(x,y);return value;}
    // [BLOCK: PLAYER FIXTURE] EquipmentTests.actor - Create a fresh Agyu at the reachable village spawn.
    private static Player actor(){return new Player(World.SPAWN_X,World.SPAWN_Y);}
    // [BLOCK: SIMULATION FIXTURE] EquipmentTests.tick - Advance actual player logic and settle interpolation for visual evidence.
    private static void tick(Player actor,double dt,KeyHandler.Input input){actor.savePrev();actor.update(dt,input,world,List.of());actor.savePrev();}
    // [BLOCK: INPUT FIXTURE] EquipmentTests.tap - Deliver a real keyboard press and release to the session.
    private static void tap(GamePanel panel,int code){Canvas source=new Canvas();panel.keys.keyPressed(new KeyEvent(source,KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));panel.update(DT);panel.keys.keyReleased(new KeyEvent(source,KeyEvent.KEY_RELEASED,0,0,code,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: SESSION TEST ACCESS] EquipmentTests.get - Inspect private state without adding production mutation APIs.
    private static Object get(Object object,String name)throws Exception{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);}
    // [BLOCK: CAMERA FIXTURE] EquipmentTests.set - Register the actual game camera immediately for the boss layout preview.
    private static void set(Object object,String name,double value)throws Exception{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);field.setDouble(object,value);}
    // [BLOCK: ASSERTION] EquipmentTests.check - Count a behavioral property and explain its failure.
    private static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
}
