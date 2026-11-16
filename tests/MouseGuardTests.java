/* FILE PURPOSE: REGRESSION TEST | Mouse press edges, frontal shield cones, cancellation, contact repulsion and focus.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

/** Mouse edges, directional blocking, contact response and guard visuals. */
public final class MouseGuardTests {
    private static final Canvas SOURCE=new Canvas();
    private static final double DT=1.0/60;
    private static final double[][] FORWARD={{0,1},{0,-1},{-1,0},{1,0}};
    private static int checks;
    // [BLOCK: TEST HELPER] MouseGuardTests.MouseGuardTests - Prevent instances of this test/helper class.
    private MouseGuardTests(){}
    // [BLOCK: TEST HELPER] MouseGuardTests.main - Entry point for this regression suite.
    public static void main(String[] args)throws Exception{
        Assets.load();System.out.println("Mouse/shield checks passed: "+run());
    }
    // [BLOCK: TEST HELPER] MouseGuardTests.run - Handle run for MouseGuardTests.
    static int run()throws Exception{
        checks=0;
        KeyHandler keys=new KeyHandler();
        mouse(keys,MouseEvent.BUTTON1,true);
        check(keys.poll().pressed(KeyHandler.ATTACK),"Left click did not attack");
        mouse(keys,MouseEvent.BUTTON1,true);
        check(!keys.poll().pressed(KeyHandler.ATTACK),"Held left mouse repeated");
        mouse(keys,MouseEvent.BUTTON1,false);mouse(keys,MouseEvent.BUTTON1,true);
        check(keys.poll().pressed(KeyHandler.ATTACK),"Second click lost");keys.clear();
        mouse(keys,MouseEvent.BUTTON3,true);
        var input=keys.poll();
        check(input.guard&&!input.pressed(KeyHandler.ATTACK)&&keys.poll().guard,"RMB must hold guard, without attack");
        mouse(keys,MouseEvent.BUTTON3,false);check(!keys.poll().guard,"Released RMB stuck");
        mouse(keys,MouseEvent.BUTTON1,true);input=keys.poll();keys.defer(input,KeyHandler.ATTACK);
        check(keys.poll().pressed(KeyHandler.ATTACK)&&!keys.poll().pressed(KeyHandler.ATTACK),"Hit-stop input deferred incorrectly");
        mouse(keys,MouseEvent.BUTTON3,true);key(keys,KeyEvent.VK_W);keys.clear();
        input=keys.poll();check(!input.guard&&!input.up&&!input.pressed(KeyHandler.ATTACK),"Focus clear left mouse held");
        World world=new World(7);
        for(int dir=0;dir<4;dir++){
            Player actor=player();actor.dir=Player.Dir.values()[dir];keys=new KeyHandler();
            mouse(keys,MouseEvent.BUTTON3,true);actor.update(DT,keys.poll(),world,List.of());
            check(actor.state==Player.State.GUARD,"RMB did not enter guard");
            double dx=FORWARD[dir][0],dy=FORWARD[dir][1];
            check(actor.blocksFrom(actor.x+dx*20,actor.y+dy*20),"Front outside block cone");
            check(!actor.blocksFrom(actor.x-dx*20,actor.y-dy*20),"Rear inside block cone");
            check(!actor.blocksFrom(actor.x-dy*20,actor.y+dx*20),"Side inside block cone");
            check(actor.blocksFrom(actor.x+(dx-dy)*20,actor.y+(dy+dx)*20),"Forward diagonal outside block cone");
            int hp=actor.health();actor.damage(2,actor.x+dx*20,actor.y+dy*20);
            check(actor.health()==hp&&actor.state==Player.State.GUARD,"Block changed HP or state");
            check(actor.invincible<=0,"Block incorrectly grants global invulnerability");
            actor.damage(1,actor.x-dx*20,actor.y-dy*20);
            check(actor.health()==hp-1&&actor.state==Player.State.HURT,"Rear hit after block ignored");Fx.clear();

            actor=player();actor.dir=Player.Dir.values()[dir];
            double offset=(dir<2?4:8);
            Enemy contact=new Slime(actor.x+dx*offset,actor.y+dy*offset,0);
            for(int tick=0;tick<180;tick++){
                actor.update(DT,keys.poll(),world,List.of(contact));
                contact.update(DT,actor,world,null);Fx.clear();
            }
            check(actor.health()==actor.maxHealth(),"Contact enemy crossed through a frontal shield");
        }
        Player actor=player();keys=new KeyHandler();
        mouse(keys,MouseEvent.BUTTON1,true);mouse(keys,MouseEvent.BUTTON3,true);
        actor.update(DT,keys.poll(),world,List.of());
        check(actor.state==Player.State.GUARD,"Both buttons should prioritize guard");
        mouse(keys,MouseEvent.BUTTON3,false);
        for(int tick=0;tick<30;tick++)actor.update(DT,keys.poll(),world,List.of());
        check(actor.state==Player.State.IDLE,"Releasing guard executed an old click");
        mouse(keys,MouseEvent.BUTTON1,false);mouse(keys,MouseEvent.BUTTON1,true);
        actor.update(DT,keys.poll(),world,List.of());
        check(actor.state==Player.State.ATTACK,"Fresh click after shield failed");
        actor=player();actor.dir=Player.Dir.RIGHT;keys=new KeyHandler();
        Enemy target=new Slime(actor.x+30,actor.y,0);
        mouse(keys,MouseEvent.BUTTON1,true);actor.update(DT,keys.poll(),world,List.of(target));
        mouse(keys,MouseEvent.BUTTON1,false);mouse(keys,MouseEvent.BUTTON3,true);
        actor.update(DT,keys.poll(),world,List.of(target));
        check(actor.state==Player.State.GUARD,"Shield failed to cancel attack");
        mouse(keys,MouseEvent.BUTTON3,false);
        for(int tick=0;tick<40;tick++)actor.update(DT,keys.poll(),world,List.of(target));
        check(actor.state==Player.State.IDLE&&target.health()==3,"Canceled swing dealt delayed damage");
        Player hurt=player();hurt.damage(1,hurt.x,hurt.y-20);
        keys=new KeyHandler();mouse(keys,MouseEvent.BUTTON3,true);
        hurt.update(DT,keys.poll(),world,List.of());
        check(hurt.state==Player.State.HURT,"Guard bypassed hurt recovery");
        Player dead=player();dead.damage(100,0,0);dead.update(DT,keys.poll(),world,List.of());
        check(dead.state==Player.State.DEAD,"Dead player guarded");
        Player guard=player(),runner=player();
        KeyHandler guardKeys=new KeyHandler(),runKeys=new KeyHandler();
        key(guardKeys,KeyEvent.VK_D);key(guardKeys,KeyEvent.VK_SHIFT);mouse(guardKeys,MouseEvent.BUTTON3,true);
        key(runKeys,KeyEvent.VK_D);key(runKeys,KeyEvent.VK_SHIFT);
        double start=guard.x;
        for(int tick=0;tick<60;tick++){
            guard.update(DT,guardKeys.poll(),world,List.of());runner.update(DT,runKeys.poll(),world,List.of());
            check(guard.state==Player.State.GUARD,"Walking/run canceled guard");
        }
        check(guard.x>start&&guard.x-start<(runner.x-start)*0.5,"Guard must slow movement and prevent running");
        preview();Fx.clear();return checks;
    }
    // [BLOCK: TEST HELPER] MouseGuardTests.preview - Handle preview for MouseGuardTests.
    private static void preview()throws Exception{
        BufferedImage image=new BufferedImage(960,750,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();
        try{
            g.setColor(new Color(0x263A35));g.fillRect(0,0,960,750);g.setFont(new Font("Monospaced",Font.PLAIN,14));
            for(int row=0;row<3;row++)for(int dir=0;dir<4;dir++){
                g.setColor(new Color(0xF1C979));
                String title=row==0?"SHIELD UP":row==1?"FRONT BLOCK":"NATURAL ATTACK";
                g.drawString(Player.Dir.values()[dir]+" / "+title,dir*240+10,row*250+20);
                Graphics2D cell=(Graphics2D)g.create();
                try{
                    cell.translate(dir*240,row*250+25);cell.scale(2,2);
                    Player actor=new Player(60,85);actor.dir=Player.Dir.values()[dir];
                    actor.state=row==2?Player.State.ATTACK:Player.State.GUARD;actor.stateTime=0.20;
                    if(row==1)actor.damage(1,actor.x+FORWARD[dir][0]*20,actor.y+FORWARD[dir][1]*20);
                    actor.draw(cell,0,0,1);
                }finally{cell.dispose();}
            }
        }finally{g.dispose();}
        check(ImageIO.write(image,"png",new File("build/preview-mouse-shield.png")),"Mouse/shield preview failed");
    }
    // [BLOCK: TEST HELPER] MouseGuardTests.player - Handle player for MouseGuardTests.
    private static Player player(){return new Player(World.SPAWN_X,World.SPAWN_Y);}
    // [BLOCK: TEST HELPER] MouseGuardTests.mouse - Handle mouse for MouseGuardTests.
    private static void mouse(KeyHandler keys,int button,boolean down){
        MouseEvent event=new MouseEvent(SOURCE,down?MouseEvent.MOUSE_PRESSED:MouseEvent.MOUSE_RELEASED,0,0,10,10,1,false,button);
        if(down)keys.mousePressed(event);else keys.mouseReleased(event);
    }
    // [BLOCK: TEST HELPER] MouseGuardTests.key - Handle key for MouseGuardTests.
    private static void key(KeyHandler keys,int code){
        keys.keyPressed(new KeyEvent(SOURCE,KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));
    }
    // [BLOCK: TEST HELPER] MouseGuardTests.check - Handle check for MouseGuardTests.
    private static void check(boolean valid,String message){checks++;if(!valid)throw new AssertionError(message);}
}
