/* FILE PURPOSE: REGRESSION TEST | Six-stage input chaining, hit windows, move trajectories and bounded effects.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/** Real-input combo chains, shared hit geometry, bounded effects and visual previews. */
public final class ComboTests {
    private static final Canvas SOURCE=new Canvas();
    private static final double DT=1.0/60;
    private static final double[][] FORWARD={{0,1},{0,-1},{-1,0},{1,0}};
    private static int checks;
    // [BLOCK: TEST HELPER] ComboTests.ComboTests - Prevent instances of this test/helper class.
    private ComboTests(){}
    private static final class Target extends ForestBeast {
        // [BLOCK: TEST HELPER] ComboTests.Target - Initialize Target state.
        Target(double x,double y){super(x,y,100,-1);}
        // [BLOCK: TEST SPRITE POLICY] ComboTests.kind - Select the current boar appearance for the stationary combat target.
        @Override protected WildlifeArt.Kind kind(){return WildlifeArt.Kind.BOAR;}
        // [BLOCK: TEST HELPER] ComboTests.speed - Handle speed for ComboTests.
        @Override protected double speed(){return 0;}
        // [BLOCK: TEST HELPER] ComboTests.contactDamage - Handle contact damage for ComboTests.
        @Override protected int contactDamage(){return 0;}
        // [BLOCK: TEST HELPER] ComboTests.update - Advance update state with elapsed time.
        @Override void update(double dt,Player player,World world,int[][] flow){
            t+=dt;flash=Math.max(0,flash-dt);kbx=kby=0;
        }
    }
    private record Clip(BufferedImage[] frames,int[] stages,String[] names){}
    // [BLOCK: TEST HELPER] ComboTests.main - Entry point for this regression suite.
    public static void main(String[] args)throws Exception{
        Assets.load();
        if(args.length>0&&"--previews".equals(args[0])){
            previews();System.out.println("Combo previews regenerated.");return;
        }
        System.out.println("Combo checks passed: "+run());
    }
    // [BLOCK: TEST HELPER] ComboTests.run - Handle run for ComboTests.
    static int run()throws Exception{
        checks=0;
        check(SpearAttack.STAGES==6,"Six distinct combo movements are required");
        chainChecks();
        cancelChecks();
        damageChecks();
        renderChecks();
        effectsChecks();
        previews();
        Fx.clear();
        return checks;
    }
    // [BLOCK: TEST HELPER] ComboTests.chainChecks - Handle chain checks for ComboTests.
    private static void chainChecks(){
        World world=new World(7);
        for(Player.Dir dir:Player.Dir.values()){
            Player actor=player();actor.dir=dir;KeyHandler keys=new KeyHandler();
            attack(actor,keys,world,false);
            check(actor.state==Player.State.ATTACK&&actor.comboStep()==1,"Fresh attack did not open the combo");
            for(int stage=2;stage<=SpearAttack.STAGES;stage++){
                attack(actor,keys,world,stage%2==0);
                int wanted=stage;
                waitFor(actor,keys,world,wanted);
                check(actor.state==Player.State.ATTACK&&actor.comboStep()==stage,"Queued press lost a combo stage");
                check(actor.dir==dir,"Combo turned without directional input");
            }
            // A fresh press during the finisher starts a new opening, not another finisher.
            attack(actor,keys,world,true);
            waitFor(actor,keys,world,1);
            check(actor.comboStep()==1&&actor.state==Player.State.ATTACK,"Finisher did not wrap to the opening");
            settle(actor,keys,world,120);
            check(actor.comboStep()==0&&actor.state==Player.State.IDLE,"Completed combo repeated without more input");
        }
        Player actor=player();KeyHandler keys=new KeyHandler();
        press(keys,KeyEvent.VK_J);step(actor,keys,world);
        settle(actor,keys,world,180);
        check(actor.comboStep()==0&&actor.state==Player.State.IDLE,"Holding attack automatically chained");
        keys.clear();actor=player();actor.dir=Player.Dir.RIGHT;
        attack(actor,keys,world,false);
        press(keys,KeyEvent.VK_W);
        attack(actor,keys,world,false);
        release(keys,KeyEvent.VK_W);
        check(actor.dir==Player.Dir.RIGHT,"Queued turn changed the current swing");
        waitFor(actor,keys,world,2);
        check(actor.dir==Player.Dir.UP,"Follow-up did not use its captured facing");
        settle(actor,keys,world,90);
        check(actor.state==Player.State.IDLE&&actor.comboStep()==0,"A single follow-up queued an entire chain");
        actor=player();keys=new KeyHandler();
        attack(actor,keys,world,false);
        // Many early clicks may fill only one pending slot.
        for(int i=0;i<4;i++)attack(actor,keys,world,i%2==0);
        waitFor(actor,keys,world,2);
        settle(actor,keys,world,90);
        check(actor.state==Player.State.IDLE&&actor.comboStep()==0,"Repeated presses overfilled the next-stage buffer");
        actor=player();keys=new KeyHandler();
        attack(actor,keys,world,false);waitUntilIdle(actor,keys,world);
        check(actor.comboStep()==1,"Grounded continuation grace disappeared");
        settle(actor,keys,world,4);attack(actor,keys,world,false);
        check(actor.comboStep()==2,"Prompt grounded follow-up restarted the opening");
        waitUntilIdle(actor,keys,world);settle(actor,keys,world,60);
        check(actor.comboStep()==0,"Grounded combo timeout did not clear");
        attack(actor,keys,world,true);
        check(actor.comboStep()==1,"Timed-out chain resumed a stale stage");
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] ComboTests.cancelChecks - Handle cancel checks for ComboTests.
    private static void cancelChecks(){
        World world=new World(7);
        Player actor=thirdStage(world);KeyHandler keys=new KeyHandler();
        attack(actor,keys,world,false);
        mouse(keys,MouseEvent.BUTTON3,true);step(actor,keys,world);
        check(actor.state==Player.State.GUARD&&actor.comboStep()==0,"Shield did not clear active and queued combo");
        mouse(keys,MouseEvent.BUTTON3,false);settle(actor,keys,world,60);
        check(actor.state==Player.State.IDLE&&actor.comboStep()==0,"Canceled combo resumed after shield release");
        attack(actor,keys,world,true);
        check(actor.comboStep()==1,"Attack after a block kept the canceled combo");
        actor=thirdStage(world);keys=new KeyHandler();
        actor.damage(1,actor.x-24,actor.y);Fx.clear();step(actor,keys,world);
        check(actor.state==Player.State.HURT&&actor.comboStep()==0,"Damage did not reset the combo");
        settle(actor,keys,world,60);attack(actor,keys,world,false);
        check(actor.comboStep()==1,"Attack after hurt resumed the old chain");
        actor=thirdStage(world);keys=new KeyHandler();
        actor.damage(100,actor.x-24,actor.y);Fx.clear();attack(actor,keys,world,false);
        check(actor.state==Player.State.DEAD&&actor.comboStep()==0,"Dead actor retained or executed a combo");
        actor=thirdStage(world);keys=new KeyHandler();
        waitUntilIdle(actor,keys,world);press(keys,KeyEvent.VK_SPACE);step(actor,keys,world);release(keys,KeyEvent.VK_SPACE);
        check(actor.airborne()&&actor.comboStep()==0,"Jump did not clear grounded continuation");
        attack(actor,keys,world,true);
        check(actor.state!=Player.State.ATTACK&&actor.comboStep()==0,"Airborne click created a combo");
        settle(actor,keys,world,60);attack(actor,keys,world,false);
        check(actor.comboStep()==1,"Landed attack resumed a stale combo");
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] ComboTests.damageChecks - Handle damage checks for ComboTests.
    private static void damageChecks(){
        for(int stage=0;stage<SpearAttack.STAGES;stage++)for(int dir=0;dir<4;dir++){
            Player actor=player();actor.dir=Player.Dir.values()[dir];actor.state=Player.State.ATTACK;
            double dx=FORWARD[dir][0],dy=FORWARD[dir][1];
            Target ahead=new Target(actor.x+dx*32,actor.y+dy*32);
            Target behind=new Target(actor.x-dx*32,actor.y-dy*32);
            Target far=new Target(actor.x+dx*150,actor.y+dy*150);
            List<Enemy> targets=List.of(ahead,behind,far);
            SpearAttack attack=new SpearAttack();attack.begin(stage);
            double active=SpearAttack.activeStart(stage),end=SpearAttack.activeEnd(stage);
            attack.update(actor,0,active-0.001,targets);
            check(ahead.health()==100&&behind.health()==100&&far.health()==100,"Combo windup dealt damage");
            attack.update(actor,active-0.001,end+0.001,targets);
            int damage=stage==5?2:1;
            check(ahead.health()==100-damage,"Combo movement failed to hit a forward target exactly once: "+stage+"/"+dir);
            check(far.health()==100,"Combo hit outside its visible reach");
            check(behind.health()==(stage==4?99:100),"Only the full spin may hit a rear target");
            attack.update(actor,active,end,targets);
            check(ahead.health()==100-damage,"Swept samples damaged one target more than once in a stage");
            if(stage==4)check(behind.health()==99,"Spin damaged a rear target repeatedly");
            Target late=new Target(actor.x+dx*32,actor.y+dy*32);
            attack.update(actor,end+0.002,SpearAttack.duration(stage),List.of(late));
            check(late.health()==100,"Recovery dealt delayed damage");
            attack.begin(stage);attack.update(actor,active,end,List.of(ahead));
            check(ahead.health()==100-2*damage,"Beginning a new stage did not reset per-target hits");
            Fx.clear();
        }
    }
    // [BLOCK: TEST HELPER] ComboTests.renderChecks - Handle render checks for ComboTests.
    private static void renderChecks()throws Exception{
        for(Player.Dir dir:Player.Dir.values()){
            long[] fingerprints=new long[SpearAttack.STAGES];
            for(int stage=0;stage<SpearAttack.STAGES;stage++){
                double start=SpearAttack.activeStart(stage),end=SpearAttack.activeEnd(stage);
                double time=start+(end-start)*0.58;
                Player actor=posePlayer(dir,stage,time,80,110);
                BufferedImage normal=render(actor,0,0,200,180,false);
                Player translated=posePlayer(dir,stage,time,208,206);
                BufferedImage camera=render(translated,128,96,200,180,false);
                check(same(normal,camera),"Combo body/effects disagree under camera translation");
                fingerprints[stage]=fingerprint(normal);
                BufferedImage early=render(posePlayer(dir,stage,start*0.25,80,110),0,0,200,180,false);
                check(!same(normal,early),"A combo stage has no visible windup-to-contact movement");
            }
            for(int stage=0;stage<SpearAttack.STAGES;stage++)for(int other=stage+1;other<SpearAttack.STAGES;other++){
                check(fingerprints[stage]!=fingerprints[other],"Two combo stages render the same movement");
                double separation=0;
                for(int sample=0;sample<=6;sample++){
                    double a=SpearAttack.activeStart(stage)+(SpearAttack.activeEnd(stage)-SpearAttack.activeStart(stage))*sample/6.0;
                    double b=SpearAttack.activeStart(other)+(SpearAttack.activeEnd(other)-SpearAttack.activeStart(other))*sample/6.0;
                    SpearAttack.Pose first=SpearAttack.pose(dir,a,stage),second=SpearAttack.pose(dir,b,other);
                    separation=Math.max(separation,Math.hypot(first.tipX()-second.tipX(),first.tipY()-second.tipY()));
                }
                check(separation>6,"Two combo movements differ only in trail color");
            }
        }
    }
    // [BLOCK: TEST HELPER] ComboTests.effectsChecks - Handle effects checks for ComboTests.
    private static void effectsChecks()throws Exception{
        Fx.clear();
        for(int i=0;i<500;i++){
            int stage=i%SpearAttack.STAGES;
            ComboEffects.step(stage,Player.Dir.RIGHT,0,SpearAttack.activeEnd(stage),80,100,120,75,
                    CombatBody.at(Player.Dir.RIGHT,stage,0).image(),0,0);
            ComboEffects.hit(stage,120,85);
        }
        check(Fx.list.size()<=Fx.MAX_PARTICLES,"Combo effects exceeded the shared particle cap");
        check(ComboEffects.effectCount()<=ComboEffects.MAX_GHOSTS+ComboEffects.MAX_RINGS+ComboEffects.MAX_IMPACTS,"Combo effects have an unbounded retained list");
        check(ComboEffects.ghostCount()<=ComboEffects.MAX_GHOSTS&&ComboEffects.ringCount()<=ComboEffects.MAX_RINGS&&ComboEffects.impactCount()<=ComboEffects.MAX_IMPACTS,"Combo effect lists exceeded their caps");
        int count=ComboEffects.effectCount(),particles=Fx.list.size();
        BufferedImage first=new BufferedImage(240,180,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=first.createGraphics();
        try{
            Composite original=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.7f);
            g.setComposite(original);Fx.draw(g,0,0);
            check(g.getComposite().equals(original),"FX rendering did not restore the incoming composite");
        }finally{g.dispose();}
        BufferedImage second=new BufferedImage(240,180,BufferedImage.TYPE_INT_ARGB);
        g=second.createGraphics();
        try{g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.7f));Fx.draw(g,0,0);}finally{g.dispose();}
        check(same(first,second)&&count==ComboEffects.effectCount()&&particles==Fx.list.size(),"Rendering emitted or mutated combo effects");
        for(int i=0;i<180;i++)Fx.update(DT);
        check(Fx.list.isEmpty()&&ComboEffects.effectCount()==0,"Expired combo effects did not leave their lists");
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] ComboTests.previews - Handle previews for ComboTests.
    private static void previews()throws Exception{
        int width=2016,height=1464;
        BufferedImage gallery=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=gallery.createGraphics();
        try{
            g.setColor(new Color(0x172B2B));g.fillRect(0,0,width,height);
            g.setFont(new Font("Monospaced",Font.BOLD,13));
            for(int dir=0;dir<4;dir++)for(int stage=0;stage<SpearAttack.STAGES;stage++){
                Fx.clear();Fx.rnd.setSeed(20261103L+dir*6+stage);
                double start=SpearAttack.activeStart(stage),end=SpearAttack.activeEnd(stage);
                double time=start+(end-start)*(stage==5?.8:.58);
                Player actor=posePlayer(Player.Dir.values()[dir],stage,0,80,105);
                SpearAttack attack=spear(actor);
                for(double t=0;t<time;){
                    double next=Math.min(time,t+1.0/120);
                    actor.stateTime=next;attack.update(actor,t,next,List.of());Fx.update(next-t);t=next;
                }
                BufferedImage art=render(actor,0,0,160,160,true);
                int x=stage*336,y=dir*366;
                g.setColor(new Color(0xF1C979));g.drawString((stage+1)+" / "+SpearAttack.name(stage).toUpperCase(java.util.Locale.ROOT),x+10,y+20);
                g.setColor(new Color(0xB8C9BB));g.drawString(Player.Dir.values()[dir].name(),x+10,y+38);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(art,x+8,y+40,320,320,null);
            }
        }finally{g.dispose();}
        check(ImageIO.write(gallery,"png",new File("build/preview-combos.png")),"Combo gallery could not be written");
        Clip[] clips=new Clip[4];for(int dir=0;dir<4;dir++)clips[dir]=clip(Player.Dir.values()[dir],100);
        Iterator<ImageWriter> writers=ImageIO.getImageWritersByFormatName("gif");
        if(!writers.hasNext())throw new IllegalStateException("GIF writer missing");
        ImageWriter writer=writers.next();
        try(ImageOutputStream output=ImageIO.createImageOutputStream(new File("build/preview-combos.gif"))){
            writer.setOutput(output);writer.prepareWriteSequence(null);
            for(int frame=0;frame<100;frame++){
                BufferedImage image=new BufferedImage(1344,366,BufferedImage.TYPE_INT_RGB);
                g=image.createGraphics();
                try{
                    g.setColor(new Color(0x172B2B));g.fillRect(0,0,image.getWidth(),image.getHeight());
                    g.setFont(new Font("Monospaced",Font.BOLD,12));
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    for(int dir=0;dir<4;dir++){
                        int x=dir*336;Clip clip=clips[dir];
                        g.setColor(new Color(0xF1C979));g.drawString(Player.Dir.values()[dir]+" / "+clip.names()[frame],x+10,18);
                        for(int dot=0;dot<6;dot++){
                            g.setColor(dot<clip.stages()[frame]?new Color(0x79E4F5):new Color(0x385453));
                            g.fillRect(x+11+dot*17,27,12,3);
                        }
                        g.drawImage(clip.frames()[frame],x+8,40,320,320,null);
                    }
                }finally{g.dispose();}
                IIOMetadata metadata=writer.getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier.createFromRenderedImage(image),writer.getDefaultWriteParam());
                String format=metadata.getNativeMetadataFormatName();IIOMetadataNode root=(IIOMetadataNode)metadata.getAsTree(format);
                IIOMetadataNode control=(IIOMetadataNode)root.getElementsByTagName("GraphicControlExtension").item(0);
                control.setAttribute("disposalMethod","none");control.setAttribute("userInputFlag","FALSE");
                control.setAttribute("transparentColorFlag","FALSE");control.setAttribute("delayTime","4");control.setAttribute("transparentColorIndex","0");
                if(frame==0){
                    IIOMetadataNode extensions=new IIOMetadataNode("ApplicationExtensions"),loop=new IIOMetadataNode("ApplicationExtension");
                    loop.setAttribute("applicationID","NETSCAPE");loop.setAttribute("authenticationCode","2.0");
                    loop.setUserObject(new byte[]{1,0,0});extensions.appendChild(loop);root.appendChild(extensions);
                }
                metadata.setFromTree(format,root);writer.writeToSequence(new IIOImage(image,null,metadata),writer.getDefaultWriteParam());
            }
            writer.endWriteSequence();
        }finally{writer.dispose();}
        check(new File("build/preview-combos.gif").length()>0,"Combo animation is missing");
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] ComboTests.clip - Handle clip for ComboTests.
    private static Clip clip(Player.Dir dir,int frames){
        Fx.clear();Fx.rnd.setSeed(20261103L+dir.ordinal());
        BufferedImage[] pictures=new BufferedImage[frames];int[] stages=new int[frames];String[] names=new String[frames];
        Player actor=player();actor.dir=dir;KeyHandler keys=new KeyHandler();World world=new World(7);
        double[] forward=FORWARD[dir.ordinal()];Target target=new Target(actor.x+forward[0]*34,actor.y+forward[1]*34);
        List<Enemy> targets=List.of(target);
        press(keys,KeyEvent.VK_J);
        int lastStage=0;boolean queued=false;
        for(int frame=0;frame<frames;frame++){
            for(int tick=0;tick<4;tick++){
                int stage=actor.comboStep();
                if(stage!=lastStage){lastStage=stage;queued=false;}
                release(keys,KeyEvent.VK_J);
                if(actor.state==Player.State.ATTACK&&stage<6&&!queued&&actor.stateTime>=.07){
                    press(keys,KeyEvent.VK_J);queued=true;
                }
                actor.savePrev();
                if(Fx.hitStop>0)Fx.hitStop=Math.max(0,Fx.hitStop-.01);
                else actor.update(.01,keys.poll(),world,targets);
                target.update(.01,actor,world,null);Fx.update(.01);
            }
            stages[frame]=actor.comboStep();names[frame]=stages[frame]==0?"READY":actor.comboName().toUpperCase(java.util.Locale.ROOT);
            BufferedImage image=new BufferedImage(160,160,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();
            try{
                g.setColor(new Color(0x263A35));g.fillRect(0,0,160,160);
                int cx=(int)Math.round(actor.x)-80+Fx.shakeX,cy=(int)Math.round(actor.y)-105+Fx.shakeY;
                g.setColor(new Color(0x304941));g.drawLine(0,106,160,106);
                target.draw(g,cx,cy,1);actor.draw(g,cx,cy,1);Fx.draw(g,cx,cy);
            }finally{g.dispose();}
            pictures[frame]=image;
        }
        return new Clip(pictures,stages,names);
    }
    // [BLOCK: TEST HELPER] ComboTests.thirdStage - Handle third stage for ComboTests.
    private static Player thirdStage(World world){
        Player actor=player();KeyHandler keys=new KeyHandler();attack(actor,keys,world,false);
        attack(actor,keys,world,false);waitFor(actor,keys,world,2);
        attack(actor,keys,world,false);waitFor(actor,keys,world,3);return actor;
    }
    // [BLOCK: TEST HELPER] ComboTests.waitFor - Handle wait for for ComboTests.
    private static void waitFor(Player actor,KeyHandler keys,World world,int stage){
        for(int tick=0;tick<90;tick++){
            if(actor.state==Player.State.ATTACK&&actor.comboStep()==stage)return;
            step(actor,keys,world);
        }
        throw new AssertionError("Combo failed to reach stage "+stage+"; current="+actor.comboStep()+" state="+actor.state);
    }
    // [BLOCK: TEST HELPER] ComboTests.waitUntilIdle - Handle wait until idle for ComboTests.
    private static void waitUntilIdle(Player actor,KeyHandler keys,World world){
        for(int tick=0;tick<90&&actor.state==Player.State.ATTACK;tick++)step(actor,keys,world);
        check(actor.state==Player.State.IDLE,"Attack failed to recover");
    }
    // [BLOCK: TEST HELPER] ComboTests.attack - Handle attack for ComboTests.
    private static void attack(Player actor,KeyHandler keys,World world,boolean useMouse){
        if(useMouse){mouse(keys,MouseEvent.BUTTON1,true);step(actor,keys,world);mouse(keys,MouseEvent.BUTTON1,false);}
        else{press(keys,KeyEvent.VK_J);step(actor,keys,world);release(keys,KeyEvent.VK_J);}
    }
    // [BLOCK: TEST HELPER] ComboTests.settle - Handle settle for ComboTests.
    private static void settle(Player actor,KeyHandler keys,World world,int ticks){for(int tick=0;tick<ticks;tick++)step(actor,keys,world);}
    // [BLOCK: TEST HELPER] ComboTests.step - Handle step for ComboTests.
    private static void step(Player actor,KeyHandler keys,World world){actor.savePrev();actor.update(DT,keys.poll(),world,List.of());}
    // [BLOCK: TEST HELPER] ComboTests.player - Handle player for ComboTests.
    private static Player player(){return new Player(World.SPAWN_X,World.SPAWN_Y);}
    // [BLOCK: TEST HELPER] ComboTests.posePlayer - Handle pose player for ComboTests.
    private static Player posePlayer(Player.Dir dir,int stage,double time,double x,double y)throws Exception{
        Player actor=new Player(x,y);actor.dir=dir;actor.state=Player.State.ATTACK;actor.stateTime=time;spear(actor).begin(stage);return actor;
    }
    // [BLOCK: TEST HELPER] ComboTests.spear - Handle spear for ComboTests.
    private static SpearAttack spear(Player actor)throws Exception{
        Field field=Player.class.getDeclaredField("spear");field.setAccessible(true);return (SpearAttack)field.get(actor);
    }
    // [BLOCK: TEST HELPER] ComboTests.render - Handle render for ComboTests.
    private static BufferedImage render(Player actor,int cx,int cy,int width,int height,boolean effects){
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{actor.draw(g,cx,cy,1);if(effects)Fx.draw(g,cx,cy);}finally{g.dispose();}return image;
    }
    // [BLOCK: TEST HELPER] ComboTests.same - Handle same for ComboTests.
    private static boolean same(BufferedImage first,BufferedImage second){
        if(first.getWidth()!=second.getWidth()||first.getHeight()!=second.getHeight())return false;
        for(int y=0;y<first.getHeight();y++)for(int x=0;x<first.getWidth();x++)if(first.getRGB(x,y)!=second.getRGB(x,y))return false;
        return true;
    }
    // [BLOCK: TEST HELPER] ComboTests.fingerprint - Handle fingerprint for ComboTests.
    private static long fingerprint(BufferedImage image){
        long hash=0xcbf29ce484222325L;
        for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){hash^=image.getRGB(x,y);hash*=0x100000001b3L;}
        return hash;
    }
    // [BLOCK: TEST HELPER] ComboTests.press - Handle press for ComboTests.
    private static void press(KeyHandler keys,int code){keys.keyPressed(new KeyEvent(SOURCE,KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: TEST HELPER] ComboTests.release - Handle release for ComboTests.
    private static void release(KeyHandler keys,int code){keys.keyReleased(new KeyEvent(SOURCE,KeyEvent.KEY_RELEASED,0,0,code,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: TEST HELPER] ComboTests.mouse - Handle mouse for ComboTests.
    private static void mouse(KeyHandler keys,int button,boolean pressed){
        MouseEvent event=new MouseEvent(SOURCE,pressed?MouseEvent.MOUSE_PRESSED:MouseEvent.MOUSE_RELEASED,0,0,10,10,1,false,button);
        if(pressed)keys.mousePressed(event);else keys.mouseReleased(event);
    }
    // [BLOCK: TEST HELPER] ComboTests.check - Handle check for ComboTests.
    private static void check(boolean valid,String message){checks++;if(!valid)throw new AssertionError(message);}
}
