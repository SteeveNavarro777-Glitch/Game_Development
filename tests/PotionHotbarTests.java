/* FILE PURPOSE: REGRESSION TEST | Potion resource rules, shield absorption, real hotbar pointer/keyboard input and three-bar rendering.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import javax.imageio.ImageIO;

/** Uses actual game input, rendering, inventory and damage paths; no third-party test framework. */
public final class PotionHotbarTests {
    private static final double DT=1.0/60;
    private static final Canvas SOURCE=new Canvas();
    private static int checks;
    // [BLOCK: STATIC TEST] PotionHotbarTests.PotionHotbarTests - Prevent instances of the regression suite.
    private PotionHotbarTests(){}
    // [BLOCK: TEST ENTRY] PotionHotbarTests.main - Run the potion and hotbar checks independently.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Potion/hotbar checks passed: "+run());}
    // [BLOCK: REGRESSION SUITE] PotionHotbarTests.run - Exercise supplied artwork, resource invariants, actual input and HUD layout.
    static int run()throws Exception{
        checks=0;art();resources();slots();actorInput();panelInput();hud();Fx.clear();return checks;
    }
    // [BLOCK: ART PROVENANCE / CACHE] PotionHotbarTests.art - Verify original hashes, all supplied effect frames and small runtime RGBA icons.
    private static void art()throws Exception{
        List<String> rows=Files.readAllLines(Path.of("res/items/frames.csv"));check(rows.size()==26,"Potion manifest omits supplied frames");
        for(String row:rows.subList(1,rows.size())){
            String[] fields=row.split(",");String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(Path.of(fields[3]))));
            check(hash.equals(fields[9]),"Source potion provenance differs: "+fields[3]);
        }
        for(Inventory.Item item:Inventory.Item.values()){
            BufferedImage icon=PotionArt.icon(item);check(icon!=null&&icon.getWidth()==32&&icon.getHeight()==32,"Missing cached hotbar icon: "+item);
            int opaque=0;for(int y=0;y<32;y++)for(int x=0;x<32;x++)if((icon.getRGB(x,y)>>>24)>0)opaque++;
            check(opaque>20&&opaque<900,"Icon is blank or keeps a solid background: "+item);
            check(PotionArt.icon(item)==icon,"Icon lookup allocates images");
        }
        BufferedImage shieldIcon=PotionArt.icon(Inventory.Item.SHIELD);int faceOrClothes=0;
        for(int y=0;y<32;y++)for(int x=0;x<32;x++){int c=shieldIcon.getRGB(x,y);if((c>>>24)>128&&((c>>16)&255)>170&&((c>>8)&255)<80)faceOrClothes++;}
        check(faceOrClothes==0,"Shield icon includes Agyu's red hat or tunic");
        for(int kind=0;kind<3;kind++)for(int frame=0;frame<PotionArt.FRAME_COUNTS[kind];frame++){
            BufferedImage effect=PotionArt.frame(kind,frame);int visible=0;
            for(int y=0;y<32;y++)for(int x=0;x<32;x++)if((effect.getRGB(x,y)>>>24)>0)visible++;
            check(visible>0&&visible<1024,"Empty effect or opaque background");check(PotionArt.frame(kind,frame)==effect,"Effect lookup allocates");
        }
    }
    // [BLOCK: RESOURCE INVARIANTS] PotionHotbarTests.resources - Test useful-only spending, recovery clamps, protection precedence and death safety.
    private static void resources()throws Exception{
        Player player=actor();Inventory bag=player.inventory();
        check(bag.count(Inventory.Item.SPEAR)==1&&bag.count(Inventory.Item.SHIELD)==1,"Initial equipment missing");
        check(!player.usePotion(Inventory.Item.HEALTH_POTION)&&bag.count(Inventory.Item.HEALTH_POTION)==3,"Full health wastes potion");
        check(!player.usePotion(Inventory.Item.ENERGY_POTION)&&bag.count(Inventory.Item.ENERGY_POTION)==3,"Full energy wastes potion");
        player.damage(4,player.x+20,player.y);settle(player,.4);
        check(player.usePotion(Inventory.Item.HEALTH_POTION)&&player.health()==1,"Healing must wait for the sip");
        check(!player.usePotion(Inventory.Item.HEALTH_POTION)&&bag.count(Inventory.Item.HEALTH_POTION)==3,"Drinking action spends twice");
        finishDrink(player);check(player.health()==3&&bag.count(Inventory.Item.HEALTH_POTION)==2,"Green potion must restore two health once");
        check(player.usePotion(Inventory.Item.HEALTH_POTION),"Second healing action cannot begin");finishDrink(player);check(player.health()==5,"Healing does not clamp at max");
        player=actor();EnergyMeter meter=(EnergyMeter)get(player,"energy");meter.spend(100);
        check(player.energyExhausted()&&player.usePotion(Inventory.Item.ENERGY_POTION),"Orange potion cannot recover exhausted energy");
        finishDrink(player);check(player.energy()>=60&&player.energy()<75&&!player.energyExhausted(),"Energy potion fails recovery or exhaustion release");
        check(player.usePotion(Inventory.Item.ENERGY_POTION),"Second energy potion cannot begin");finishDrink(player);check(player.energy()==100,"Energy exceeds capacity");
        player=actor();check(player.protection()==0&&player.usePotion(Inventory.Item.SHIELD_POTION),"Blue potion action cannot begin");
        check(player.protection()==0&&player.inventory().count(Inventory.Item.SHIELD_POTION)==2,"Blue potion is spent before sipping");
        finishDrink(player);check(player.protection()==3&&player.inventory().count(Inventory.Item.SHIELD_POTION)==1,"Blue potion quantity or capacity");
        check(!player.usePotion(Inventory.Item.SHIELD_POTION)&&player.inventory().count(Inventory.Item.SHIELD_POTION)==1,"Full protection wastes potion");
        player.damage(1,player.x+20,player.y);Fx.clear();check(player.health()==5&&player.protection()==2,"Shield does not absorb before health");
        player.damage(2,player.x+20,player.y);check(player.protection()==2,"Invincibility spends shield again");
        player.invincible=0;player.state=Player.State.GUARD;player.dir=Player.Dir.RIGHT;
        player.damage(2,player.x+20,player.y);check(player.protection()==2&&player.health()==5,"Physical guard spends potion shield");
        player.state=Player.State.DASH;player.damage(100,0,0);check(player.protection()==2,"Dash immunity spends shield");
        player.state=Player.State.IDLE;player.invincible=0;player.damage(4,player.x+20,player.y);Fx.clear();
        check(player.protection()==0&&player.health()==3,"Excess damage does not transfer to health exactly");
        player.invincible=0;player.damage(Integer.MAX_VALUE,0,0);Fx.clear();
        for(Inventory.Item item:Inventory.Item.values())if(item.potion()){
            int amount=player.inventory().count(item);check(!player.usePotion(item)&&amount==player.inventory().count(item),"Dead player consumes potion");
        }
        check(player.health()==0&&!player.alive(),"Damage overflow resurrects Agyu");
        for(Player.State state:new Player.State[]{Player.State.ATTACK,Player.State.DASH,Player.State.GUARD,Player.State.HURT}){
            player=actor();player.state=state;check(!player.usePotion(Inventory.Item.SHIELD_POTION)&&player.protection()==0,"Unsafe state consumes potion: "+state);
        }
        player=actor();player.update(DT,new KeyHandler.Input(false,false,false,false,false,false,KeyHandler.JUMP),new World(7),List.of());
        check(!player.usePotion(Inventory.Item.SHIELD_POTION),"Airborne player consumes potion");
        player=actor();while(player.inventory().consume(Inventory.Item.SHIELD_POTION)){}check(!player.usePotion(Inventory.Item.SHIELD_POTION),"Empty inventory supplies potion");
        ShieldMeter shield=new ShieldMeter();shield.restore();check(shield.absorb(Integer.MAX_VALUE)==Integer.MAX_VALUE-3&&shield.value()==0,"Shield damage overflow");Fx.clear();
    }
    // [BLOCK: ARRAY / REARRANGEMENT] PotionHotbarTests.slots - Verify wraparound, selection-preserving swaps and empty-slot hit testing.
    private static void slots(){
        Hotbar bar=new Hotbar();bar.cycle(-1);check(bar.selectedIndex()==8,"Wheel wrap left");bar.cycle(1);check(bar.selectedIndex()==0,"Wheel wrap right");
        bar.cycle(Integer.MAX_VALUE);check(bar.selectedIndex()==1,"Wheel arithmetic overflow");
        bar.select(2);bar.swap(2,8);check(bar.selectedIndex()==8&&bar.selectedItem()==Inventory.Item.HEALTH_POTION&&bar.item(2)==null,"Swap duplicates stack or loses selection");
        bar.swap(8,0);check(bar.selectedIndex()==0&&bar.item(8)==Inventory.Item.SPEAR,"Occupied swap loses equipment");
        for(int slot=0;slot<9;slot++){
            int x=HotbarHud.left(480)+slot*(HotbarHud.CELL+HotbarHud.GAP),y=HotbarHud.top(360);
            check(HotbarHud.slotAt(x+1,y+1,480,360)==slot,"Slot hit target differs from rendering");
            check(HotbarHud.slotAt(x+HotbarHud.CELL,y+1,480,360)==-1,"Gap triggers a slot");
        }
        check(HotbarHud.slotAt(-1,340,480,360)==-1&&HotbarHud.slotAt(240,HotbarHud.top(360)-1,480,360)==-1,"Outside hotbar selects an item");
        boolean rejected=false;try{bar.select(9);}catch(IllegalArgumentException expected){rejected=true;}check(rejected,"Invalid array selection accepted");
    }
    // [BLOCK: REAL ITEM INPUT] PotionHotbarTests.actorInput - Route key, wheel and mouse edges through Agyu's actual update state machine.
    private static void actorInput()throws Exception{
        World world=new World(7);Player actor=actor();KeyHandler keys=new KeyHandler();
        press(keys,KeyEvent.VK_5);step(actor,keys,world);release(keys,KeyEvent.VK_5);
        mouse(keys,MouseEvent.BUTTON1,true);step(actor,keys,world);
        check(actor.drinking()&&actor.protection()==0,"Selected blue potion attacks or skips the drinking animation");
        for(int i=0;i<55;i++)step(actor,keys,world);check(actor.inventory().count(Inventory.Item.SHIELD_POTION)==1,"Held primary repeatedly drinks");
        mouse(keys,MouseEvent.BUTTON1,false);press(keys,KeyEvent.VK_2);step(actor,keys,world);release(keys,KeyEvent.VK_2);
        mouse(keys,MouseEvent.BUTTON1,true);step(actor,keys,world);check(actor.state==Player.State.GUARD,"Selected shield primary does not defend");
        mouse(keys,MouseEvent.BUTTON1,false);step(actor,keys,world);check(actor.state!=Player.State.GUARD,"Released shield primary stuck");
        press(keys,KeyEvent.VK_J);step(actor,keys,world);check(actor.state==Player.State.GUARD,"Selected shield J does not defend");release(keys,KeyEvent.VK_J);step(actor,keys,world);
        press(keys,KeyEvent.VK_1);step(actor,keys,world);release(keys,KeyEvent.VK_1);
        mouse(keys,MouseEvent.BUTTON1,true);step(actor,keys,world);check(actor.state==Player.State.ATTACK,"Spear slot loses primary combos");
        press(keys,KeyEvent.VK_4);step(actor,keys,world);release(keys,KeyEvent.VK_4);check(actor.state!=Player.State.ATTACK,"Equipment change keeps old attack active");
        mouse(keys,MouseEvent.BUTTON1,false);mouse(keys,MouseEvent.BUTTON3,true);step(actor,keys,world);check(actor.state==Player.State.GUARD,"Potion selection disables RMB shield");
        keys.clear();step(actor,keys,world);press(keys,KeyEvent.VK_9);step(actor,keys,world);release(keys,KeyEvent.VK_9);
        mouse(keys,MouseEvent.BUTTON1,true);step(actor,keys,world);check(actor.state!=Player.State.ATTACK,"Empty slot attacks");keys.clear();
        keys.mouseWheelMoved(new MouseWheelEvent(SOURCE,MouseEvent.MOUSE_WHEEL,0,0,10,10,0,false,MouseWheelEvent.WHEEL_UNIT_SCROLL,1,1));step(actor,keys,world);
        check(actor.hotbar().selectedIndex()==0,"Actual mouse wheel fails to wrap selection");
        keys.selectSlot(4);KeyHandler.Input deferred=keys.poll();keys.defer(deferred,KeyHandler.USE_ITEM);check(keys.poll().slot==4,"Hit-stop loses queued selection");
        keys.selectSlot(2);keys.clear();check(keys.poll().slot==-1,"Focus clear retains selection edge");
        actor=actor();actor.damage(1,0,0);actor.state=Player.State.IDLE;actor.invincible=0;Fx.clear();keys.selectSlot(1);
        press(keys,KeyEvent.VK_H);mouse(keys,MouseEvent.BUTTON1,true);step(actor,keys,world);
        check(actor.state==Player.State.GUARD&&actor.health()==4&&actor.inventory().count(Inventory.Item.HEALTH_POTION)==3,"Selected shield does not win simultaneous healing input");Fx.clear();
    }
    // [BLOCK: ACTUAL PANEL INPUT] PotionHotbarTests.panelInput - Exercise scaled box clicks, fast drags, potion clicks, hit-stop, overlays and focus loss.
    @SuppressWarnings("unchecked")
    private static void panelInput()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);TitleMenuTests.startGame(panel);
        ((List<Enemy>)get(panel,"enemies")).clear();Player actor=(Player)get(panel,"player");
        point(panel,4,true);point(panel,4,false);panel.update(DT);
        check(actor.hotbar().selectedIndex()==4&&actor.protection()==0&&actor.state!=Player.State.ATTACK,"UI slot click uses item or attacks");
        panel.setSize(1440,1080);point(panel,2,true);point(panel,7,false);panel.update(DT);
        check(actor.hotbar().selectedIndex()==7&&actor.hotbar().item(7)==Inventory.Item.HEALTH_POTION&&actor.hotbar().item(2)==null,"Same-frame scaled drag loses selection or duplicates item");
        check(actor.inventory().count(Inventory.Item.HEALTH_POTION)==3,"Moving item changes stack quantity");
        point(panel,4,true);point(panel,4,false);panel.update(DT);worldMouse(panel,true);panel.update(DT);worldMouse(panel,false);
        check(actor.drinking()&&actor.protection()==0,"World click does not start drinking");
        for(int i=0;i<55;i++)panel.update(DT);
        check(actor.protection()==3&&actor.state!=Player.State.ATTACK,"World click does not commit selected potion at sip");
        PotionEffects effects=(PotionEffects)get(actor,"potionEffects");double remaining=effects.remaining(Inventory.Item.SHIELD_POTION);
        tap(panel,KeyEvent.VK_M);panel.update(3);check(effects.remaining(Inventory.Item.SHIELD_POTION)==remaining,"Map advances potion animation");tap(panel,KeyEvent.VK_M);
        remaining=effects.remaining(Inventory.Item.SHIELD_POTION);tap(panel,KeyEvent.VK_TAB);panel.update(3);
        check(effects.remaining(Inventory.Item.SHIELD_POTION)==remaining,"Journal advances potion animation");tap(panel,KeyEvent.VK_TAB);
        tap(panel,KeyEvent.VK_ESCAPE);remaining=effects.remaining(Inventory.Item.SHIELD_POTION);press(panel.keys,KeyEvent.VK_F);panel.update(2);release(panel.keys,KeyEvent.VK_F);
        check(effects.remaining(Inventory.Item.SHIELD_POTION)==remaining&&actor.inventory().count(Inventory.Item.SHIELD_POTION)==1,"Paused input uses a potion");tap(panel,KeyEvent.VK_ENTER);
        panel.update(DT);check(actor.inventory().count(Inventory.Item.SHIELD_POTION)==1,"Paused potion edge executes after resume");
        actor.invincible=0;actor.damage(10000,0,0);Fx.clear();panel.update(DT);tap(panel,KeyEvent.VK_R);actor=(Player)get(panel,"player");
        check(actor.protection()==0&&actor.hotbar().selectedIndex()==0&&actor.hotbar().item(2)==Inventory.Item.HEALTH_POTION,"Restart retains protection or rearranged slots");
        actor.damage(2,0,0);Fx.clear();for(int i=0;i<22;i++)panel.update(DT);Fx.hitStop=.035;press(panel.keys,KeyEvent.VK_3);press(panel.keys,KeyEvent.VK_F);panel.update(DT);release(panel.keys,KeyEvent.VK_3);release(panel.keys,KeyEvent.VK_F);
        check(actor.health()==3,"Hit-stop consumes potion early");for(int i=0;i<4;i++)panel.update(DT);
        check(actor.drinking()&&actor.health()==3,"Hit-stop loses the deferred drinking action");
        for(int i=0;i<55;i++)panel.update(DT);
        check(actor.health()==5&&actor.inventory().count(Inventory.Item.HEALTH_POTION)==2,"Hit-stop loses or repeats potion sip");
        panel.keys.selectSlot(4);point(panel,1,true);for(FocusListener listener:panel.getFocusListeners())listener.focusLost(new FocusEvent(panel,FocusEvent.FOCUS_LOST));
        point(panel,8,false);panel.update(DT);check(get(panel,"mode")==GamePanel.Mode.PAUSED&&panel.keys.poll().slot==-1&&actor.hotbar().item(1)==Inventory.Item.SHIELD,"Focus loss leaves pending drag or controls");Fx.clear();
    }
    // [BLOCK: REAL HUD / PREVIEW] PotionHotbarTests.hud - Assert text-free green/orange/blue bar interiors and export an actual-game screenshot.
    private static void hud()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);TitleMenuTests.startGame(panel);Player actor=(Player)get(panel,"player");
        actor.usePotion(Inventory.Item.SHIELD_POTION);finishDrink(actor);BufferedImage hud=new BufferedImage(480,360,BufferedImage.TYPE_INT_RGB);Graphics2D g=hud.createGraphics();
        Method draw=GamePanel.class.getDeclaredMethod("drawHud",Graphics2D.class);draw.setAccessible(true);
        try{draw.invoke(panel,g);}finally{g.dispose();}
        int[] colors={0x69C833,0xED9C23,0x22A9EB},shine={0xC3F578,0xFFE07B,0x8CEDFF};
        for(int row=0;row<3;row++)for(int y=0;y<10;y++)for(int x=66;x<236;x++)
            check((hud.getRGB(x,13+row*16+y)&0xFFFFFF)==(y<2?shine[row]:colors[row]),"Resource bar keeps numbers or wrong potion colors");
        PotionEffects effects=(PotionEffects)get(actor,"potionEffects");double before=effects.remaining(Inventory.Item.SHIELD_POTION);
        BufferedImage image=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);g=image.createGraphics();
        try{panel.paint(g);panel.paint(g);}finally{g.dispose();}
        check(effects.remaining(Inventory.Item.SHIELD_POTION)==before,"Painting advances potion animation");
        ImageIO.write(image,"png",new File("build/preview-potions-hotbar.png"));
        PotionEffects oneShot=new PotionEffects();oneShot.play(Inventory.Item.HEALTH_POTION);oneShot.update(2);
        check(oneShot.remaining(Inventory.Item.HEALTH_POTION)==0,"Potion animation does not expire");
    }
    // [BLOCK: DRINK TIMELINE FIXTURE] PotionHotbarTests.finishDrink - Advance real simulation through the sip and bottle-lowering frames.
    private static void finishDrink(Player player){World world=new World(7);for(int i=0;i<55;i++)player.update(DT,new KeyHandler.Input(false,false,false,false,false,false,0),world,List.of());Fx.clear();}
    // [BLOCK: TEST FIXTURE] PotionHotbarTests.actor - Create Agyu at the actual safe village spawn.
    private static Player actor(){return new Player(World.SPAWN_X,World.SPAWN_Y);}
    // [BLOCK: SIMULATION FIXTURE] PotionHotbarTests.settle - Advance cooldowns without movement or enemy interference.
    private static void settle(Player player,double dt){player.update(dt,new KeyHandler.Input(false,false,false,false,false,false,0),new World(7),List.of());Fx.clear();}
    // [BLOCK: SIMULATION FIXTURE] PotionHotbarTests.step - Advance one real player-input frame.
    private static void step(Player actor,KeyHandler keys,World world){actor.savePrev();actor.update(DT,keys.poll(),world,List.of());}
    // [BLOCK: INPUT FIXTURE] PotionHotbarTests.press - Send a fresh keyboard press through the synchronized handler.
    private static void press(KeyHandler keys,int code){keys.keyPressed(new KeyEvent(SOURCE,KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: INPUT FIXTURE] PotionHotbarTests.release - Send a matching keyboard release.
    private static void release(KeyHandler keys,int code){keys.keyReleased(new KeyEvent(SOURCE,KeyEvent.KEY_RELEASED,0,0,code,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: INPUT FIXTURE] PotionHotbarTests.tap - Send a panel key edge and advance one simulation frame.
    private static void tap(GamePanel panel,int code){press(panel.keys,code);panel.update(DT);release(panel.keys,code);}
    // [BLOCK: INPUT FIXTURE] PotionHotbarTests.mouse - Send a world mouse edge to the shared input handler.
    private static void mouse(KeyHandler keys,int button,boolean down){MouseEvent e=new MouseEvent(SOURCE,down?MouseEvent.MOUSE_PRESSED:MouseEvent.MOUSE_RELEASED,0,0,10,10,1,false,button);if(down)keys.mousePressed(e);else keys.mouseReleased(e);}
    // [BLOCK: SCALED POINTER FIXTURE] PotionHotbarTests.point - Hit a rendered slot after converting its center into native window coordinates.
    private static void point(GamePanel panel,int slot,boolean down){
        int x=(HotbarHud.left(480)+slot*(HotbarHud.CELL+HotbarHud.GAP)+15)*panel.getWidth()/480,y=(HotbarHud.top(360)+15)*panel.getHeight()/360;dispatch(panel,x,y,down);
    }
    // [BLOCK: POINTER FIXTURE] PotionHotbarTests.worldMouse - Send a primary click outside the hotbar.
    private static void worldMouse(GamePanel panel,boolean down){dispatch(panel,20,200,down);}
    // [BLOCK: REAL LISTENER FIXTURE] PotionHotbarTests.dispatch - Exercise the actual GamePanel listeners instead of bypassing UI routing.
    private static void dispatch(GamePanel panel,int x,int y,boolean down){MouseEvent e=new MouseEvent(panel,down?MouseEvent.MOUSE_PRESSED:MouseEvent.MOUSE_RELEASED,0,0,x,y,1,false,MouseEvent.BUTTON1);for(MouseListener listener:panel.getMouseListeners())if(down)listener.mousePressed(e);else listener.mouseReleased(e);}
    // [BLOCK: TEST ACCESS] PotionHotbarTests.get - Read private session state for integration assertions without runtime setters.
    private static Object get(Object object,String name)throws Exception{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(object);}
    // [BLOCK: ASSERTION] PotionHotbarTests.check - Count a behavioral check and explain any failure.
    private static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
}
