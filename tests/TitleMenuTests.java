/* FILE PURPOSE: REGRESSION TEST | Gold title pages, navigation, settings and loading presentation. */
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;

final class TitleMenuTests {
    private static int checks;
    private TitleMenuTests(){}

    static int run() throws Exception {
        checks=0;
        TitleMenu menu=new TitleMenu();
        check(menu.update(input(KeyHandler.START),1.0/60)==TitleMenu.Action.NEW_GAME,
                "Enter no longer starts New Game from the default title option");
        check(menu.click(300,174)==TitleMenu.Action.NONE,"Continue started an empty session");
        menu.open(true);
        check(menu.click(300,174)==TitleMenu.Action.CONTINUE,"Continue button hit area failed");
        menu.click(300,209);
        boolean music=menu.musicEnabled();menu.click(240,150);
        check(menu.musicEnabled()!=music,"Music toggle did not change the audio preference");
        boolean sound=menu.soundEnabled();menu.click(240,225);
        check(menu.soundEnabled()!=sound,"Sound toggle did not change the audio preference");
        preview(menu,"settings");
        menu.click(153,91);
        menu.click(300,243);preview(menu,"controls");menu.click(62,100);
        menu.click(300,277);preview(menu,"credits");menu.update(input(KeyHandler.PAUSE),0);
        menu.click(300,311);preview(menu,"exit");
        check(menu.update(input(KeyHandler.START),0)==TitleMenu.Action.NONE,"Exit confirmation does not default to No");
        menu.click(300,311);
        check(menu.click(304,226)==TitleMenu.Action.NONE,"Exit No did not cancel confirmation");
        menu.click(300,311);
        check(menu.click(171,226)==TitleMenu.Action.EXIT,"Exit Yes did not return the exit action");
        menu.click(67,104);
        menu.beginLoading();menu.update(input(0),.25);preview(menu,"loading");
        check(menu.update(input(KeyHandler.PAUSE),0)==TitleMenu.Action.NONE,"Escape interrupted the loading animation");
        ForestAudio.setEnabled(true);

        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);
        click(panel,300,143);
        check(((GamePanel.Mode)read(panel,"mode"))==GamePanel.Mode.TITLE,"Mouse start bypassed loading");
        panel.update(TitleMenu.LOADING_SECONDS);
        check(read(panel,"mode")==GamePanel.Mode.PLAYING,"Mouse New Game did not reach gameplay");
        Player player=(Player)read(panel,"player");
        player.addCoins(42);
        tap(panel,KeyEvent.VK_ESCAPE);tap(panel,KeyEvent.VK_T);
        check(read(panel,"mode")==GamePanel.Mode.TITLE,"Pause to title did not return to the menu");
        tap(panel,KeyEvent.VK_ENTER);panel.update(TitleMenu.LOADING_SECONDS);
        check(read(panel,"mode")==GamePanel.Mode.PLAYING&&read(panel,"player")==player&&player.coins()==42,
                "Continue replaced the current adventure");
        tap(panel,KeyEvent.VK_ESCAPE);tap(panel,KeyEvent.VK_T);
        click(panel,300,143);panel.update(TitleMenu.LOADING_SECONDS);
        check(read(panel,"mode")==GamePanel.Mode.PLAYING&&read(panel,"player")!=player&&((Player)read(panel,"player")).coins()==0,
                "New Game resumed the previous adventure");
        return checks;
    }

    static void startGame(GamePanel panel){
        tap(panel,KeyEvent.VK_ENTER);
        panel.update(TitleMenu.LOADING_SECONDS);
    }

    private static void tap(GamePanel panel,int key){
        panel.keys.keyPressed(new KeyEvent(panel,KeyEvent.KEY_PRESSED,0,0,key,KeyEvent.CHAR_UNDEFINED));
        panel.update(1.0/60);
        panel.keys.keyReleased(new KeyEvent(panel,KeyEvent.KEY_RELEASED,0,0,key,KeyEvent.CHAR_UNDEFINED));
    }

    private static void click(GamePanel panel,int x,int y){
        panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_PRESSED,0,0,x*2,y*2,1,false,MouseEvent.BUTTON1));
        panel.update(1.0/60);
    }

    private static void preview(TitleMenu menu,String name) throws IOException {
        BufferedImage preview=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=preview.createGraphics();
        try{g.scale(2,2);menu.draw(g);}finally{g.dispose();}
        check(ImageIO.write(preview,"png",new File("build/preview-title-"+name+".png")),"Could not export menu preview");
    }

    private static Object read(GamePanel panel,String name) throws ReflectiveOperationException {
        Field field=GamePanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(panel);
    }

    private static KeyHandler.Input input(int edge){return new KeyHandler.Input(false,false,false,false,false,false,edge);}
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
}
