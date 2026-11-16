/* FILE PURPOSE: REGRESSION TEST | NPC sprite caches, occupations, patrols, conversations, pause/focus and restart.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/** Real NPC movement, art, conversations, pause handling and beacon priority. */
public final class NpcTests {
    private static final Canvas SOURCE=new Canvas();
    private static final double DT=1.0/60;
    private static int checks;
    // [BLOCK: TEST HELPER] NpcTests.NpcTests - Prevent instances of this test/helper class.
    private NpcTests(){}
    // [BLOCK: TEST HELPER] NpcTests.main - Entry point for this regression suite.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("NPC checks passed: "+run());}
    // [BLOCK: TEST HELPER] NpcTests.run - Handle run for NpcTests.
    static int run()throws Exception{
        checks=0;art();patrols();dialogue();render();session();Fx.clear();return checks;
    }
    // [BLOCK: TEST HELPER] NpcTests.art - Handle art for NpcTests.
    private static void art(){
        for(int role=0;role<6;role++){
            Npc.Sprites sprites=RpgArt.npc(role);Set<Long> facings=new HashSet<>(),work=new HashSet<>();
            for(int frame=0;frame<8;frame++){
                BufferedImage raw=RpgArt.npcFrame(role,frame);int foot=0;
                for(int x=0;x<raw.getWidth();x++)if((raw.getRGB(x,63)>>>24)>128)foot++;
                check(foot>0,"NPC feet drift off the shared ground baseline");
            }
            for(Player.Dir dir:Player.Dir.values()){
                for(Npc.Action action:Npc.Action.values()){
                    BufferedImage image=sprites.frame(dir,action,0);
                    check(image!=null&&image.getWidth()==112&&image.getHeight()==64&&image.getColorModel().hasAlpha(),"Invalid NPC canvas");
                    check(image==sprites.frame(dir,action,0),"NPC images recreated during animation");
                }
                facings.add(hash(sprites.frame(dir,Npc.Action.IDLE,0)));
            }
            for(double t:new double[]{0,.13,.26,.39,.52,.65})work.add(hash(sprites.frame(Player.Dir.RIGHT,Npc.Action.WORK,t)));
            check(facings.size()>=3,"NPC facing poses missing");check(work.size()>=2,"Worker animation is static");
        }
    }
    // [BLOCK: TEST HELPER] NpcTests.patrols - Handle patrols for NpcTests.
    private static void patrols(){
        World world=new World(7);int c=World.col(World.SPAWN_X),r=World.row(World.SPAWN_Y);
        List<Point> route=List.of(new Point(c-1,r),new Point(c+1,r),new Point(c+1,r+1),new Point(c-1,r+1));
        for(Point point:route)check(world.walkable(point.x,point.y),"Patrol fixture blocked");
        Npc walker=new Npc("walker","Walker",Npc.Role.VILLAGER,World.SPAWN_X,World.SPAWN_Y,RpgArt.npc(0),route,"Hello.");
        Player far=new Player(World.SPAWN_X+500,World.SPAWN_Y);double moved=0;
        for(int i=0;i<600;i++){
            walker.savePrev();walker.update(DT,world,far);moved=Math.max(moved,Math.hypot(walker.x()-World.SPAWN_X,walker.y()-World.SPAWN_Y));
            check(!world.blocked(walker.feetBox())&&walker.x()>=(c-.5)*World.T-1e-6&&walker.x()<=(c+1.5)*World.T+1e-6
                    &&walker.y()>=(r+.5)*World.T-1e-6&&walker.y()<=(r+1.5)*World.T+1e-6,"Patrol escaped safe route");
        }
        check(moved>20&&walker.animationTime()>9.9,"Villager never walked");
        Npc smith=new Npc("smith","Smith",Npc.Role.SMITH,World.SPAWN_X,World.SPAWN_Y,RpgArt.npc(2),route,"Hello.");
        for(int i=0;i<600;i++)smith.update(DT,world,far);
        check(smith.x()==World.SPAWN_X&&smith.y()==World.SPAWN_Y&&smith.action()==Npc.Action.WORK,"Worker left station");
        List<Npc> population=NpcPopulation.create(world);Set<String> ids=new HashSet<>();EnumSet<Npc.Role> roles=EnumSet.noneOf(Npc.Role.class);
        check(population.size()>=8,"Population missing");
        for(Npc npc:population){check(ids.add(npc.id()),"Duplicate NPC");roles.add(npc.role());}
        check(roles.containsAll(EnumSet.of(Npc.Role.FARMER,Npc.Role.WEAVER,Npc.Role.SMITH,Npc.Role.FISHER,Npc.Role.VENDOR,Npc.Role.ELDER)),"Missing occupation");
        for(int i=0;i<600;i++)for(Npc npc:population){npc.savePrev();npc.update(DT,world,far);check(!world.blocked(npc.feetBox()),"NPC entered an obstacle or river");}
    }
    // [BLOCK: TEST HELPER] NpcTests.dialogue - Handle dialogue for NpcTests.
    private static void dialogue(){
        World world=new World(7);Npc npc=new Npc("neighbor","Neighbor",Npc.Role.ELDER,World.SPAWN_X,World.SPAWN_Y,RpgArt.npc(5),List.of(),"First.","Second.");
        Player player=new Player(npc.x()+20,npc.y());
        check(npc.canTalk(player)&&npc.talk(player)&&npc.isSpeaking()&&npc.dialogue().equals("First."),"Conversation failed");
        check(npc.talk(player)&&npc.dialogue().equals("Second."),"Dialogue failed to advance");
        check(npc.talk(player)&&npc.dialogue().equals("First."),"Dialogue cycle failed");
        for(Player.State state:new Player.State[]{Player.State.HURT,Player.State.ATTACK,Player.State.DASH}){
            player.state=state;check(!npc.canTalk(player)&&!npc.talk(player),"Combat state allowed talking");
        }
        player.state=Player.State.IDLE;KeyHandler keys=new KeyHandler();press(keys,KeyEvent.VK_SPACE);
        player.update(DT,keys.poll(),world,List.of());check(player.airborne()&&!npc.canTalk(player),"Air conversation allowed");
        Player dead=new Player(npc.x()+20,npc.y());dead.damage(100,0,0);check(!npc.canTalk(dead),"Dead player talked");Fx.clear();
        player=new Player(npc.x()+150,npc.y());npc.update(DT,world,player);check(!npc.isSpeaking()&&!npc.talk(player),"Distant conversation continued");
        player.x=npc.x()+20;check(npc.talk(player),"Conversation failed to restart");
        double x=npc.x(),y=npc.y();for(int i=0;i<30;i++)npc.update(DT,world,player);
        check(npc.isSpeaking()&&npc.x()==x&&npc.y()==y,"Speaker walked away");
        npc.update(5,world,player);check(!npc.isSpeaking()&&npc.dialogue().isEmpty(),"Dialogue never expired");
    }
    // [BLOCK: TEST HELPER] NpcTests.render - Handle render for NpcTests.
    private static void render()throws Exception{
        Npc npc=new Npc("render","Village Weaver",Npc.Role.WEAVER,100,110,RpgArt.npc(1),List.of(),"These woven mats are made with care. Welcome to our village.");
        Player player=new Player(120,110);npc.update(DT,new World(7),player);npc.talk(player);
        double time=npc.animationTime();String text=npc.dialogue();
        BufferedImage image=new BufferedImage(260,220,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{
            g.translate(2,3);g.clipRect(0,0,250,210);g.setColor(Color.MAGENTA);g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.37f));
            g.setStroke(new BasicStroke(3));g.setFont(new Font("Serif",Font.BOLD,12));
            AffineTransform t=g.getTransform();Rectangle clip=g.getClipBounds();Composite comp=g.getComposite();Stroke stroke=g.getStroke();Font font=g.getFont();Color color=g.getColor();
            npc.draw(g,0,0,.5);npc.drawDialogue(g,0,0,.5,250,210);
            check(g.getTransform().equals(t)&&g.getClipBounds().equals(clip)&&g.getComposite().equals(comp)
                    &&g.getStroke().equals(stroke)&&g.getFont().equals(font)&&g.getColor().equals(color),"NPC drawing leaked graphics state");
        }finally{g.dispose();}
        check(npc.animationTime()==time&&npc.dialogue().equals(text)&&npc.isSpeaking(),"Painting advanced NPC state");
        check(ImageIO.write(image,"png",new File("build/preview-npc-dialogue.png")),"NPC preview failed");
    }
    // [BLOCK: TEST HELPER] NpcTests.session - Handle session for NpcTests.
    private static void session()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);tap(panel,KeyEvent.VK_ENTER);((List<?>)field(panel,"enemies")).clear();
        List<Npc> npcs=npcs(panel);check(npcs.size()>=8,"Panel population missing");long[] initial=snapshot(npcs);
        for(int i=0;i<20;i++)panel.update(DT);check(!Arrays.equals(initial,snapshot(npcs)),"NPC clocks frozen in play");
        tap(panel,KeyEvent.VK_P);long[] frozen=snapshot(npcs);for(int i=0;i<10;i++)panel.update(DT);check(Arrays.equals(frozen,snapshot(npcs)),"Pause advanced NPCs");
        tap(panel,KeyEvent.VK_ENTER);panel.update(DT);check(!Arrays.equals(frozen,snapshot(npcs)),"Resume failed");
        tap(panel,KeyEvent.VK_M);frozen=snapshot(npcs);for(int i=0;i<10;i++)panel.update(DT);check(Arrays.equals(frozen,snapshot(npcs)),"Map advanced NPCs");tap(panel,KeyEvent.VK_M);
        press(panel.keys,KeyEvent.VK_E);press(panel.keys,KeyEvent.VK_SHIFT);
        for(FocusListener listener:panel.getFocusListeners())listener.focusLost(new FocusEvent(panel,FocusEvent.FOCUS_LOST));
        frozen=snapshot(npcs);for(int i=0;i<10;i++)panel.update(DT);KeyHandler.Input input=panel.keys.poll();
        check(Arrays.equals(frozen,snapshot(npcs))&&!input.run&&!input.pressed(KeyHandler.INTERACT),"Focus handling failed");tap(panel,KeyEvent.VK_ENTER);
        Fx.hitStop=.04;frozen=snapshot(npcs);panel.update(DT);panel.update(DT);check(Arrays.equals(frozen,snapshot(npcs)),"Hit-stop advanced NPCs");
        for(int i=0;i<3;i++)panel.update(DT);check(!Arrays.equals(frozen,snapshot(npcs)),"NPCs stuck after hit-stop");
        Player player=(Player)field(panel,"player");Npc smith=npcs.get(1);player.x=player.px=smith.x()+20;player.y=player.py=smith.y();
        player.state=Player.State.IDLE;player.vx=player.vy=0;Fx.clear();tap(panel,KeyEvent.VK_E);check(smith.isSpeaking(),"Actual E did not talk");
        String first=smith.dialogue();press(panel.keys,KeyEvent.VK_E);panel.update(DT);
        String second=smith.dialogue();check(!first.equals(second),"Fresh E failed to advance");
        for(int i=0;i<20;i++)panel.update(DT);check(smith.dialogue().equals(second),"Held E repeated dialogue");
        panel.keys.clear();
        player.x=player.px=World.SPAWN_X;player.y=player.py=World.SPAWN_Y;player.state=Player.State.IDLE;player.vx=player.vy=0;
        Npc neighbor=npcs.get(0);set(neighbor,"x",World.SPAWN_X+16);set(neighbor,"y",World.SPAWN_Y);neighbor.px=neighbor.x();neighbor.py=neighbor.y();
        QuestLog quests=(QuestLog)field(panel,"quests");for(String shrine:QuestLog.SHRINES)quests.finish(shrine);quests.finish("guardian");for(String boss:QuestLog.BOSSES)quests.finish(boss);
        Fx.clear();tap(panel,KeyEvent.VK_E);check(field(panel,"mode")==GamePanel.Mode.VICTORY&&!neighbor.isSpeaking(),"NPC stole beacon interaction");
        tap(panel,KeyEvent.VK_R);List<Npc> restarted=npcs(panel);
        check(restarted.size()==npcs.size()&&Arrays.equals(initial,snapshot(restarted)),"Restart retained NPC state");
        for(Npc npc:restarted)check(!npc.isSpeaking(),"Restart retained dialogue");Fx.clear();
    }
    // [BLOCK: TEST HELPER] NpcTests.snapshot - Handle snapshot for NpcTests.
    private static long[] snapshot(List<Npc> npcs){
        long[] result=new long[npcs.size()*6];int i=0;
        for(Npc n:npcs){result[i++]=n.id().hashCode();result[i++]=Double.doubleToLongBits(n.x());result[i++]=Double.doubleToLongBits(n.y());result[i++]=Double.doubleToLongBits(n.animationTime());result[i++]=n.dialogue().hashCode();result[i++]=n.isSpeaking()?1:0;}return result;
    }
    // [BLOCK: TEST HELPER] NpcTests.hash - Handle hash for NpcTests.
    private static long hash(BufferedImage image){long result=1;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)result=result*31+image.getRGB(x,y);return result;}
    // [BLOCK: TEST HELPER] NpcTests.npcs - Handle npcs for NpcTests.
    @SuppressWarnings("unchecked") private static List<Npc> npcs(GamePanel panel)throws Exception{return (List<Npc>)field(panel,"npcs");}
    // [BLOCK: TEST HELPER] NpcTests.field - Handle field for NpcTests.
    private static Object field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
    // [BLOCK: TEST HELPER] NpcTests.set - Handle set for NpcTests.
    private static void set(Object o,String name,double value)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.setDouble(o,value);}
    // [BLOCK: TEST HELPER] NpcTests.tap - Handle tap for NpcTests.
    private static void tap(GamePanel p,int key){press(p.keys,key);p.update(DT);p.keys.keyReleased(new KeyEvent(SOURCE,KeyEvent.KEY_RELEASED,0,0,key,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: TEST HELPER] NpcTests.press - Handle press for NpcTests.
    private static void press(KeyHandler keys,int key){keys.keyPressed(new KeyEvent(SOURCE,KeyEvent.KEY_PRESSED,0,0,key,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: TEST HELPER] NpcTests.check - Handle check for NpcTests.
    private static void check(boolean valid,String message){checks++;if(!valid)throw new AssertionError(message);}
}
