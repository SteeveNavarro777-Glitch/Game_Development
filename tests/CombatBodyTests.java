/* FILE PURPOSE: REGRESSION TEST | Authored body pixels, grip anchors, phases, mirroring, trajectories and afterimages.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

/** Authored body animation, grip/weapon alignment and cached-frame rendering checks. */
public final class CombatBodyTests {
    private static int checks;
    private static final String[] PHASE_NAMES={"LOAD","DRIVE","FOLLOW","RECOVER"};
    // [BLOCK: TEST HELPER] CombatBodyTests.CombatBodyTests - Prevent instances of this test/helper class.
    private CombatBodyTests(){}
    // [BLOCK: TEST HELPER] CombatBodyTests.main - Entry point for this regression suite.
    public static void main(String[] args)throws Exception{
        Assets.load();
        if(args.length>0&&"--previews".equals(args[0])){
            previews();System.out.println("Body motion previews regenerated.");return;
        }
        System.out.println("Combat body checks passed: "+run());
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.run - Handle run for CombatBodyTests.
    static int run()throws Exception{
        checks=0;
        check(CombatBody.ready(),"Combat body cache did not load");
        assetChecks();
        phaseChecks();
        weaponPathChecks();
        pivotAndRenderChecks();
        ghostChecks();
        previews();
        Fx.clear();
        return checks;
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.assetChecks - Handle asset checks for CombatBodyTests.
    private static void assetChecks()throws Exception{
        int sources=0;
        for(Player.Dir dir:Player.Dir.values())for(int stage=0;stage<SpearAttack.STAGES;stage++){
            BufferedImage[] sequence=new BufferedImage[4];
            for(int phase=0;phase<4;phase++){
                CombatBody.Frame frame=CombatBody.at(dir,stage,phase);
                BufferedImage image=frame.image();
                sequence[phase]=image;
                check(image==CombatBody.at(dir,stage,phase).image(),"Body lookup rebuilt an image");
                check(image.getWidth()==64&&image.getHeight()==64&&image.getColorModel().hasAlpha(),
                        "Combat body must be a complete transparent64px frame");
                check((image.getRGB(0,0)>>>24)==0&&(image.getRGB(63,0)>>>24)==0,
                        "Combat body has an opaque export background");
                int visible=0,edge=0;
                for(int y=0;y<64;y++)for(int x=0;x<64;x++)if((image.getRGB(x,y)>>>24)>128){
                    visible++;if(x==0||x==63||y==0)edge++;
                }
                check(visible>=150,"Combat body is blank or badly cropped");
                check(edge==0,"Combat body is clipped at a canvas edge");
                check(frame.handX()>-32&&frame.handX()<32&&frame.handY()>-45&&frame.handY()<-5,
                        "Spear grip lies outside the body region");
                int gx=frame.handX()+32,gy=frame.handY()+63;
                boolean skin=false;
                for(int dy=-3;dy<=3;dy++)for(int dx=-3;dx<=3;dx++){
                    int x=gx+dx,y=gy+dy;
                    if(x>=0&&x<64&&y>=0&&y<64&&skin(image.getRGB(x,y)))skin=true;
                }
                check(skin,"Body spear grip is detached from its hand");
                if(dir==Player.Dir.LEFT){
                    CombatBody.Frame right=CombatBody.at(Player.Dir.RIGHT,stage,phase);
                    boolean mirror=true;
                    for(int y=0;y<64;y++)for(int x=0;x<64;x++)
                        if(image.getRGB(x,y)!=right.image().getRGB(63-x,y))mirror=false;
                    check(mirror,"Left body pixels are not an exact mirror");
                    check(Math.abs(frame.handX()+right.handX()+1)<1e-9&&
                            Math.abs(frame.handY()-right.handY())<1e-9,"Mirrored grip anchor is inconsistent");
                }else{
                    File path=new File("res/player/combat/"+dir.name().toLowerCase(Locale.ROOT)+"_"+stage+"_"+phase+".png");
                    BufferedImage saved=ImageIO.read(path);
                    check(saved!=null&&same(image,saved),"Loaded combat body changed its source pixels");
                    sources++;
                }
            }
            // Alpha silhouettes test physical posture, independently of palette and effects.
            int driveChange=silhouetteDifference(sequence[0],sequence[1]);
            int followChange=silhouetteDifference(sequence[0],sequence[2]);
            int recoveryChange=silhouetteDifference(sequence[2],sequence[3]);
            check(driveChange>=8&&followChange>=8&&recoveryChange>=8,
                    "Combat body repeats a standing silhouette instead of weight transfer: "+dir+"/"+stage);
        }
        check(sources==72,"Expected72 authored combat body source frames");
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.phaseChecks - Handle phase checks for CombatBodyTests.
    private static void phaseChecks(){
        for(int stage=0;stage<SpearAttack.STAGES;stage++){
            double start=SpearAttack.activeStart(stage),end=SpearAttack.activeEnd(stage),mid=(start+end)/2;
            check(CombatBody.phase(stage,0)==0&&CombatBody.phase(stage,start-1e-6)==0,"Body load phase ends too early");
            check(CombatBody.phase(stage,start+1e-6)==1&&CombatBody.phase(stage,mid-1e-6)==1,
                    "Body drive is not synchronized to active damage");
            check(CombatBody.phase(stage,mid+1e-6)==2&&CombatBody.phase(stage,end-1e-6)==2,
                    "Body follow-through is not synchronized to active damage");
            check(CombatBody.phase(stage,end+1e-6)==3&&CombatBody.phase(stage,SpearAttack.duration(stage))==3,
                    "Body recovery is not synchronized to the end of damage");
            for(Player.Dir dir:Player.Dir.values())for(int phase=0;phase<4;phase++){
                double time=time(stage,phase);
                Player.Dir facing=SpearAttack.bodyDirection(dir,time,stage);
                check(CombatBody.frame(dir,stage,time).image()==CombatBody.at(facing,stage,phase).image(),
                        "Body selector and spinning facing disagree");
            }
        }
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.weaponPathChecks - Handle weapon path checks for CombatBodyTests.
    private static void weaponPathChecks(){
        for(int stage=0;stage<SpearAttack.STAGES;stage++)for(Player.Dir dir:Player.Dir.values()){
            double shortest=Double.POSITIVE_INFINITY,biggestStep=0;
            SpearAttack.Pose previous=null;
            int samples=(int)Math.ceil(SpearAttack.duration(stage)/.001);
            for(int sample=0;sample<=samples;sample++){
                double time=Math.min(SpearAttack.duration(stage),sample*.001);
                SpearAttack.Pose pose=SpearAttack.pose(dir,time,stage);
                shortest=Math.min(shortest,pose.reach());
                if(previous!=null)biggestStep=Math.max(biggestStep,
                        Math.hypot(pose.tipX()-previous.tipX(),pose.tipY()-previous.tipY()));
                previous=pose;
            }
            check(Double.isFinite(shortest)&&shortest>=14,
                    "Animated fist overlaps the14px spearhead: "+dir+"/"+stage);
            check(Double.isFinite(biggestStep)&&biggestStep<=5,
                    "Body phase change jumps the blade trail: "+dir+"/"+stage);
        }
    }

    // [BLOCK: TEST HELPER] CombatBodyTests.pivotAndRenderChecks - Handle pivot and render checks for CombatBodyTests.
    private static void pivotAndRenderChecks()throws Exception{
        Field weapon=field(SpearAttack.class,"weapon");
        Object original=weapon.get(null);
        try{
            // Isolate physical motion from the weapon/trail when comparing the runtime body.
            weapon.set(null,null);
            for(Player.Dir dir:Player.Dir.values())for(int stage=0;stage<SpearAttack.STAGES;stage++)
                for(int phase=0;phase<4;phase++){
                    double time=time(stage,phase);
                    CombatBody.Frame frame=CombatBody.frame(dir,stage,time);
                    SpearAttack.Pose pose=SpearAttack.pose(dir,time,stage);
                    int offsetX=SpearAttack.bodyOffsetX(dir,time,stage),offsetY=SpearAttack.bodyOffsetY(dir,time,stage);
                    check(Math.abs(pose.handX()-frame.handX()-offsetX)<1e-9&&
                            Math.abs(pose.handY()-frame.handY()-offsetY)<1e-9,
                            "Visible body hand and swept weapon use different pivots");
                    Player actor=actor(dir,stage,time,96,100);
                    Rectangle2D ground=(Rectangle2D)actor.feetBox().clone();double sort=actor.sortY();
                    BufferedImage actual=render(actor,0,0);
                    BufferedImage expected=new BufferedImage(192,144,BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g=expected.createGraphics();
                    try{
                        g.translate(96,100);g.scale(GameScale.AGYU,GameScale.AGYU);
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                        g.setColor(new Color(0,0,0,70));g.fillOval(-11,-4,22,8);
                        g.drawImage(frame.image(),-32+offsetX,-63+offsetY,null);
                    }finally{g.dispose();}
                    check(same(actual,expected),"Player did not render the selected complete body and grounded shadow");
                    check(actor.feetBox().equals(ground)&&actor.sortY()==sort&&actor.x==96&&actor.y==100,
                            "Drawing a combat pose moved the ground collider or sort position");
                    Player translated=actor(dir,stage,time,224,196);
                    check(same(actual,render(translated,128,96)),"Combat body moved relative to the camera");
                }
        }finally{weapon.set(null,original);}
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.ghostChecks - Handle ghost checks for CombatBodyTests.
    private static void ghostChecks()throws Exception{
        double[] events={.11,.08,.10,.12,.14,.17};
        Field ghosts=field(ComboEffects.class,"ghosts");
        for(Player.Dir dir:Player.Dir.values())for(int stage=0;stage<SpearAttack.STAGES;stage++){
            Fx.clear();
            double current=events[stage]+.001;
            Player actor=actor(dir,stage,current,World.SPAWN_X,World.SPAWN_Y);
            spear(actor).update(actor,events[stage]-.001,current,List.of());
            List<?> active=(List<?>)ghosts.get(null);
            check(!active.isEmpty(),"Combat motion did not emit its scheduled afterimage");
            Object ghost=active.get(active.size()-1);
            BufferedImage source=(BufferedImage)field(ghost.getClass(),"image").get(ghost);
            check(source==CombatBody.frame(dir,stage,current).image(),"Afterimage still uses an idle body");
            double x=field(ghost.getClass(),"x").getDouble(ghost),y=field(ghost.getClass(),"y").getDouble(ghost);
            check(Math.abs(x-actor.x-SpearAttack.bodyOffsetX(dir,current,stage)*GameScale.AGYU)<1e-9&&
                    Math.abs(y-actor.y-SpearAttack.bodyOffsetY(dir,current,stage)*GameScale.AGYU)<1e-9,
                    "Afterimage ignores the body motion offset");
        }
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.previews - Handle previews for CombatBodyTests.
    private static void previews()throws Exception{
        new File("build").mkdirs();
        final int cellW=160,rowH=172,panelW=cellW*4,header=50;
        BufferedImage gallery=new BufferedImage(panelW*4,header+rowH*6,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=gallery.createGraphics();
        try{
            g.setColor(new Color(0x172B2B));g.fillRect(0,0,gallery.getWidth(),gallery.getHeight());
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            for(Player.Dir dir:Player.Dir.values()){
                int base=dir.ordinal()*panelW;
                g.setFont(new Font("Monospaced",Font.BOLD,18));g.setColor(new Color(0xF1C979));
                g.drawString(dir+" / BODY MOTION",base+14,25);
                for(int stage=0;stage<SpearAttack.STAGES;stage++)for(int phase=0;phase<4;phase++){
                    int x=base+phase*cellW,y=header+stage*rowH;
                    g.setFont(new Font("Monospaced",Font.BOLD,12));g.setColor(new Color(0xF1C979));
                    if(phase==0)g.drawString((stage+1)+" "+SpearAttack.name(stage),x+6,y+15);
                    g.setFont(new Font("Monospaced",Font.PLAIN,11));g.setColor(new Color(0xB8C9BB));
                    g.drawString(PHASE_NAMES[phase],x+6,y+31);
                    BufferedImage image=CombatBody.at(dir,stage,phase).image();
                    g.setColor(new Color(0,0,0,70));g.fillOval(x+58,y+155,44,12);
                    g.drawImage(image,x+16,y+35,128,128,null);
                }
            }
        }finally{g.dispose();}
        check(ImageIO.write(gallery,"png",new File("build/preview-body-motion.png")),"Body motion gallery was not written");
        animation();
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.animation - Handle animation for CombatBodyTests.
    private static void animation()throws Exception{
        Iterator<ImageWriter> writers=ImageIO.getImageWritersByFormatName("gif");
        if(!writers.hasNext())throw new IllegalStateException("GIF writer unavailable");
        ImageWriter writer=writers.next();
        Field weapon=field(SpearAttack.class,"weapon");Object original=weapon.get(null);
        double total=0;for(int stage=0;stage<SpearAttack.STAGES;stage++)total+=SpearAttack.duration(stage);
        int frames=(int)Math.ceil((total+.5)*25);
        try(ImageOutputStream output=ImageIO.createImageOutputStream(new File("build/preview-body-motion.gif"))){
            weapon.set(null,null);writer.setOutput(output);writer.prepareWriteSequence(null);
            for(int number=0;number<frames;number++){
                double elapsed=Math.min(total-1e-6,number*.04),time=elapsed;int stage=0;
                while(stage<SpearAttack.STAGES-1&&time>=SpearAttack.duration(stage)){time-=SpearAttack.duration(stage);stage++;}
                BufferedImage picture=new BufferedImage(1024,276,BufferedImage.TYPE_INT_RGB);
                Graphics2D g=picture.createGraphics();
                try{
                    g.setColor(new Color(0x172B2B));g.fillRect(0,0,1024,276);
                    g.setFont(new Font("Monospaced",Font.BOLD,12));
                    for(Player.Dir dir:Player.Dir.values()){
                        int x=dir.ordinal()*256;
                        g.setColor(new Color(0xF1C979));g.drawString(dir+" / "+SpearAttack.name(stage),x+10,18);
                        g.setColor(new Color(0xB8C9BB));g.drawString(PHASE_NAMES[CombatBody.phase(stage,time)],x+10,34);
                        Graphics2D cell=(Graphics2D)g.create();
                        try{
                            cell.translate(x+8,42);cell.scale(3,3);
                            Player actor=actor(dir,stage,time,40,67);
                            actor.draw(cell,0,0,1);
                        }finally{cell.dispose();}
                    }
                }finally{g.dispose();}
                IIOMetadata metadata=writer.getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier.createFromRenderedImage(picture),writer.getDefaultWriteParam());
                String format=metadata.getNativeMetadataFormatName();IIOMetadataNode root=(IIOMetadataNode)metadata.getAsTree(format);
                IIOMetadataNode control=(IIOMetadataNode)root.getElementsByTagName("GraphicControlExtension").item(0);
                control.setAttribute("disposalMethod","none");control.setAttribute("userInputFlag","FALSE");
                control.setAttribute("transparentColorFlag","FALSE");control.setAttribute("delayTime","4");control.setAttribute("transparentColorIndex","0");
                if(number==0){
                    IIOMetadataNode extensions=new IIOMetadataNode("ApplicationExtensions"),loop=new IIOMetadataNode("ApplicationExtension");
                    loop.setAttribute("applicationID","NETSCAPE");loop.setAttribute("authenticationCode","2.0");
                    loop.setUserObject(new byte[]{1,0,0});extensions.appendChild(loop);root.appendChild(extensions);
                }
                metadata.setFromTree(format,root);writer.writeToSequence(new IIOImage(picture,null,metadata),writer.getDefaultWriteParam());
            }
            writer.endWriteSequence();
        }finally{weapon.set(null,original);writer.dispose();}
        check(new File("build/preview-body-motion.gif").length()>0,"Body motion animation was not written");
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.time - Handle time for CombatBodyTests.
    private static double time(int stage,int phase){
        double start=SpearAttack.activeStart(stage),end=SpearAttack.activeEnd(stage);
        return switch(phase){case 0 -> start*.5;case 1 -> start+(end-start)*.25;
            case 2 -> start+(end-start)*.75;default -> end+(SpearAttack.duration(stage)-end)*.5;};
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.actor - Handle actor for CombatBodyTests.
    private static Player actor(Player.Dir dir,int stage,double time,double x,double y)throws Exception{
        Player actor=new Player(x,y);actor.dir=dir;actor.state=Player.State.ATTACK;actor.stateTime=time;spear(actor).begin(stage);return actor;
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.spear - Handle spear for CombatBodyTests.
    private static SpearAttack spear(Player actor)throws Exception{return (SpearAttack)field(Player.class,"spear").get(actor);}
    // [BLOCK: TEST HELPER] CombatBodyTests.field - Handle field for CombatBodyTests.
    private static Field field(Class<?> type,String name)throws Exception{
        Field field=type.getDeclaredField(name);field.setAccessible(true);return field;
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.render - Handle render for CombatBodyTests.
    private static BufferedImage render(Player actor,int cx,int cy){
        BufferedImage image=new BufferedImage(192,144,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{actor.draw(g,cx,cy,1);}finally{g.dispose();}return image;
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.skin - Handle skin for CombatBodyTests.
    private static boolean skin(int p){
        int r=(p>>16)&255,g=(p>>8)&255,b=p&255;
        return (p>>>24)>128&&r>=140&&g>=65&&b<=185&&r>g*1.10&&g>b*1.10;
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.silhouetteDifference - Handle silhouette difference for CombatBodyTests.
    private static int silhouetteDifference(BufferedImage a,BufferedImage b){
        int changed=0;
        for(int y=0;y<64;y++)for(int x=0;x<64;x++)if(((a.getRGB(x,y)>>>24)>128)!=((b.getRGB(x,y)>>>24)>128))changed++;
        return changed;
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.same - Handle same for CombatBodyTests.
    private static boolean same(BufferedImage a,BufferedImage b){
        if(a==null||b==null||a.getWidth()!=b.getWidth()||a.getHeight()!=b.getHeight())return false;
        for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++)if(a.getRGB(x,y)!=b.getRGB(x,y))return false;
        return true;
    }
    // [BLOCK: TEST HELPER] CombatBodyTests.check - Handle check for CombatBodyTests.
    private static void check(boolean valid,String message){checks++;if(!valid)throw new AssertionError(message);}
}
