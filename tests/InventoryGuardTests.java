/* FILE PURPOSE: REGRESSION TEST | Whole shield poses and real paused inventory input, equipment assignment and animated drinking.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import javax.imageio.ImageIO;

public final class InventoryGuardTests {
    private static int checks;
    private static final double DT=1.0/60;
    private static final Canvas SOURCE=new Canvas();
    // [BLOCK: TEST UTILITY] InventoryGuardTests.InventoryGuardTests - Prevent instances of the suite.
    private InventoryGuardTests(){}
    // [BLOCK: TEST ENTRY] InventoryGuardTests.main - Run guarded movement and inventory checks independently.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Inventory/guard checks passed: "+run());}
    // [BLOCK: REGRESSION SUITE] InventoryGuardTests.run - Cover cached walking, modal input and actual player use.
    static int run()throws Exception{checks=0;steps();inventory();smithEquipment();Fx.clear();return checks;}
    // [BLOCK: GUARD SPRITE REGRESSION] InventoryGuardTests.steps - Keep the authored pose whole while guard movement remains limited.
    private static void steps()throws Exception{
        World world=new World(7);BufferedImage gallery=new BufferedImage(960,620,BufferedImage.TYPE_INT_RGB);Graphics2D g=gallery.createGraphics();
        try{
            g.setColor(new Color(0x243632));g.fillRect(0,0,960,620);g.setFont(new Font("Monospaced",Font.PLAIN,12));
            for(Player.Dir dir:Player.Dir.values()){
                BufferedImage first=HeldItemArt.guardFrame(Inventory.Item.SPEAR,dir,0,true),second=HeldItemArt.guardFrame(Inventory.Item.SPEAR,dir,8,true);
                check(first==second,"Guard movement replaced the supplied whole pose: "+dir);
                check(HeldItemArt.guardFrame(Inventory.Item.SPEAR,dir,0,false)==first,
                        "Idle and moving guard use different body pixels");
                check(HeldItemArt.guardFrame(Inventory.Item.SHIELD,dir,20,true)==first,
                        "Selected slot changes the authored guard body");
                for(int kind=0;kind<2;kind++)for(int step=0;step<2;step++){
                    Player player=new Player(World.SPAWN_X,World.SPAWN_Y);player.hotbar().select(kind);player.dir=dir;
                    KeyHandler.Input input=new KeyHandler.Input(dir==Player.Dir.UP,dir==Player.Dir.DOWN,dir==Player.Dir.LEFT,dir==Player.Dir.RIGHT,true,true,0);
                    for(int n=0;n<16;n++)player.update(DT,input,world,List.of());
                    check(player.state==Player.State.GUARD&&Math.hypot(player.vx,player.vy)>3,"Guard movement stopped: "+dir);
                    check(Math.hypot(player.vx,player.vy)<=Player.WALK_SPEED*.4+.01&&!player.airborne(),"Guard floats or sprints");
                    player.animTime=step*.21;player.savePrev();
                    int x=dir.ordinal()*240,y=(kind*2+step)*150;
                    g.setColor(new Color(0xEADBBE));g.drawString(dir+" / "+(kind==0?"SPEAR + SHIELD":"SHIELD")+" / "+step,x+8,y+20);
                    Graphics2D cell=(Graphics2D)g.create();try{cell.translate(x,y+25);cell.scale(3,3);player.draw(cell,(int)player.x-38,(int)player.y-38,1);}finally{cell.dispose();}
                }
            }
        }finally{g.dispose();}ImageIO.write(gallery,"png",new File("build/preview-guard-walking.png"));
    }
    // [BLOCK: INVENTORY INTEGRATION] InventoryGuardTests.inventory - Exercise real keys, scaled clicks, paused clocks, reference moves and sip-time consumption.
    private static void inventory()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(960,720);TitleMenuTests.startGame(panel);
        Player player=(Player)get(panel,"player");World world=(World)get(panel,"world");
        Field hp=Player.class.getDeclaredField("hp");hp.setAccessible(true);hp.setInt(player,2);
        tap(panel,KeyEvent.VK_I);check((boolean)get(panel,"inventoryOpen"),"I did not open inventory");
        double time=(double)get(panel,"time"),phase=world.dayNight.phaseSeconds(),x=player.x,y=player.y;
        for(int n=0;n<120;n++)panel.update(DT);
        check((double)get(panel,"time")==time&&world.dayNight.phaseSeconds()==phase&&player.x==x&&player.y==y,"Inventory advanced gameplay");
        panel.keys.keyPressed(new KeyEvent(SOURCE,KeyEvent.KEY_PRESSED,0,0,KeyEvent.VK_I,'i'));panel.update(DT);
        panel.keys.keyPressed(new KeyEvent(SOURCE,KeyEvent.KEY_PRESSED,0,0,KeyEvent.VK_I,'i'));panel.update(DT);
        check(!(boolean)get(panel,"inventoryOpen"),"I repeat reopened inventory");
        panel.keys.keyReleased(new KeyEvent(SOURCE,KeyEvent.KEY_RELEASED,0,0,KeyEvent.VK_I,'i'));
        tap(panel,KeyEvent.VK_I);
        Rectangle health=InventoryScreen.cardBounds(InventoryScreen.entryIndex(Inventory.Item.HEALTH_POTION));
        click(panel,(int)health.getCenterX(),(int)health.getCenterY());
        check(((InventoryScreen)get(panel,"inventoryScreen")).item()==Inventory.Item.HEALTH_POTION,"Bag click missed");
        tap(panel,KeyEvent.VK_9);int before=player.inventory().count(Inventory.Item.HEALTH_POTION);
        tap(panel,KeyEvent.VK_ENTER);check(player.hotbar().selectedIndex()==8&&player.hotbar().item(8)==Inventory.Item.HEALTH_POTION,"Equip did not move item to slot 9");
        check(player.inventory().count(Inventory.Item.HEALTH_POTION)==before&&player.hotbar().indexOf(Inventory.Item.HEALTH_POTION)==8,"Equip duplicated or consumed supplies");
        BufferedImage screen=new BufferedImage(960,720,BufferedImage.TYPE_INT_RGB);Graphics2D graphics=screen.createGraphics();try{panel.paint(graphics);}finally{graphics.dispose();}
        ImageIO.write(screen,"png",new File("build/preview-inventory.png"));
        click(panel,375,258);check(player.hotbar().item(8)==null&&player.inventory().count(Inventory.Item.HEALTH_POTION)==before,"Clear slot destroyed supplies");
        tap(panel,KeyEvent.VK_ENTER);player.state=Player.State.GUARD;tap(panel,KeyEvent.VK_F);
        check(!(boolean)get(panel,"inventoryOpen")&&player.inventory().count(Inventory.Item.HEALTH_POTION)==before,"Inventory use consumed before animation");
        panel.update(DT);check(player.drinking()&&player.state==Player.State.DRINK,"Inventory use did not start drinking");
        for(int n=0;n<60;n++)panel.update(DT);
        check(player.health()==4&&player.inventory().count(Inventory.Item.HEALTH_POTION)==before-1,"Sip did not restore health once");
        tap(panel,KeyEvent.VK_I);tap(panel,KeyEvent.VK_ESCAPE);
        check(!(boolean)get(panel,"inventoryOpen")&&get(panel,"mode")==GamePanel.Mode.PLAYING,"Esc paused instead of closing the bag");
        InventoryScreen ui=new InventoryScreen();
        check(ui.hit(249,85).action()==InventoryScreen.Action.NONE&&ui.hit(441,62).action()==InventoryScreen.Action.NONE,"UI border leaks into actions");
        check(ui.hit(426,52).action()==InventoryScreen.Action.CLOSE&&ui.hit(111,283).action()==InventoryScreen.Action.SLOT&&ui.hit(111,283).index()==0,
                "Shared hit geometry differs from display");
        player.equipItem(8,Inventory.Item.SPEAR);player.clearSlot(8);
        check(player.hotbar().indexOf(Inventory.Item.SPEAR)==-1&&player.equipItem(0,Inventory.Item.SPEAR),"Stowed gear cannot be re-equipped");
    }
    private static void smithEquipment()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(960,720);TitleMenuTests.startGame(panel);
        Player player=(Player)get(panel,"player");
        Npc smith=null;
        for(Object villager:(List<?>)get(panel,"npcs")){
            Npc npc=(Npc)villager;
            if(npc.role()==Npc.Role.SMITH)smith=npc;
        }
        check(smith!=null,"Village smith is missing");
        player.x=smith.x();player.y=smith.y()+8;player.savePrev();player.addCoins(105);
        tap(panel,KeyEvent.VK_E);
        SmithShop shop=(SmithShop)get(panel,"smithShop");
        check(shop.isOpen(),"Talking to the smith did not open the shop");
        click(panel,110,120);click(panel,300,120);click(panel,110,200);click(panel,300,200);
        check(player.coins()==0&&shop.message().contains("INVENTORY"),"Shop clicks did not buy gear with equip guidance");
        tap(panel,KeyEvent.VK_ESCAPE);tap(panel,KeyEvent.VK_I);
        int spear=InventoryScreen.entryIndex(EquipmentLoadout.Gear.SPEAR,EquipmentLoadout.Level.LEGENDARY);
        Rectangle card=InventoryScreen.cardBounds(spear);
        click(panel,(int)card.getCenterX(),(int)card.getCenterY());tap(panel,KeyEvent.VK_ENTER);
        int shield=InventoryScreen.entryIndex(EquipmentLoadout.Gear.SHIELD,EquipmentLoadout.Level.LEGENDARY);
        card=InventoryScreen.cardBounds(shield);
        click(panel,(int)card.getCenterX(),(int)card.getCenterY());click(panel,199,258);
        check(player.equipment().level(EquipmentLoadout.Gear.SPEAR)==EquipmentLoadout.Level.LEGENDARY
                &&player.equipment().level(EquipmentLoadout.Gear.SHIELD)==EquipmentLoadout.Level.LEGENDARY,
                "Purchased inventory cards did not activate the exact tiers");
        check(player.hotbar().item(0)==Inventory.Item.SPEAR&&player.hotbar().item(1)==Inventory.Item.SHIELD,
                "Equipping an upgrade overwrote the other gear's quick slot");
        BufferedImage preview=new BufferedImage(960,720,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=preview.createGraphics();try{panel.paint(g);}finally{g.dispose();}
        ImageIO.write(preview,"png",new File("build/preview-smith-inventory-live.png"));
    }
    // [BLOCK: REAL INPUT] InventoryGuardTests.tap - Deliver a fresh keyboard edge and release to the live session.
    private static void tap(GamePanel panel,int code){panel.keys.keyPressed(new KeyEvent(SOURCE,KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));panel.update(DT);panel.keys.keyReleased(new KeyEvent(SOURCE,KeyEvent.KEY_RELEASED,0,0,code,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: SCALED POINTER] InventoryGuardTests.click - Send an actual two-times window click through the listener queue.
    private static void click(GamePanel panel,int x,int y){panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_PRESSED,0,0,x*2,y*2,1,false,MouseEvent.BUTTON1));panel.update(DT);panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_RELEASED,0,0,x*2,y*2,1,false,MouseEvent.BUTTON1));}
    // [BLOCK: READ-ONLY TEST ACCESS] InventoryGuardTests.get - Inspect private session state without adding production test hooks.
    private static Object get(Object object,String name)throws Exception{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);}
    // [BLOCK: ASSERTION] InventoryGuardTests.check - Count and explain each behavioral assertion.
    private static void check(boolean condition,String message){checks++;if(!condition)throw new AssertionError(message);}
}
