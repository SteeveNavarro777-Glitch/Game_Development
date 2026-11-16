/* FILE PURPOSE: REGRESSION TEST | House walls/approaches, dash collision, map arrows and compact HUD placement.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;

/** Authored-house collision, connected approaches, live arrows and HUD layout. */
public final class VillageTests {
    private static final Canvas SOURCE=new Canvas();
    private static int checks;
    // [BLOCK: TEST HELPER] VillageTests.VillageTests - Prevent instances of this test/helper class.
    private VillageTests(){}
    // [BLOCK: TEST HELPER] VillageTests.main - Entry point for this regression suite.
    public static void main(String[] args)throws Exception{
        Assets.load();System.out.println("Village checks passed: "+run());
    }
    // [BLOCK: TEST HELPER] VillageTests.run - Handle run for VillageTests.
    static int run()throws Exception{
        checks=0;World world=new World(7);
        houseChecks(world);mapChecks(world);panelChecks();previews(world);Fx.clear();return checks;
    }
    // [BLOCK: TEST HELPER] VillageTests.houseChecks - Handle house checks for VillageTests.
    private static void houseChecks(World world){
        check(world.buildings.size()==20,"Missing authored village houses");
        int[][] distances=Pathfinder.distances(world,World.col(World.SPAWN_X),World.row(World.SPAWN_Y));
        Random random=new Random(9011);
        for(int i=0;i<world.buildings.size();i++){
            World.Building house=world.buildings.get(i);Rectangle2D.Double box=house.footprint;
            check(world.blocked(new Rectangle2D.Double(box.getCenterX(),box.getCenterY(),1,1)),"House interior lacks collision");
            for(int j=i+1;j<world.buildings.size();j++)check(!box.intersects(world.buildings.get(j).footprint),"Overlapping house footprints");
            for(int sample=0;sample<30;sample++){
                Rectangle2D probe=new Rectangle2D.Double(box.x-24+random.nextDouble()*(box.width+48),
                        box.y-24+random.nextDouble()*(box.height+48),1+random.nextDouble()*28,1+random.nextDouble()*18);
                check(world.blocked(probe)==world.blockedBruteForce(probe),"Indexed house collision differs");
            }
            check(!world.blocked(new Rectangle2D.Double(house.x-5,house.y+14,10,6)),"Blocked house front");
            int dc=World.col(house.x),dr=World.row(house.y+20);boolean reachable=false;
            for(int r=dr-2;r<=dr+2;r++)for(int c=dc-2;c<=dc+2;c++)
                if(world.inside(c,r)&&world.walkable(c,r)&&distances[r][c]>=0)reachable=true;
            check(reachable,"House front lacks a connected approach");
            Player actor=new Player(house.x,house.y+36);check(!world.blocked(actor.feetBox()),"House dash starts blocked");
            actor.dir=Player.Dir.UP;KeyHandler keys=new KeyHandler();press(keys,KeyEvent.VK_Q);
            actor.update(.16,keys.poll(),world,List.of());
            check(!world.blocked(actor.feetBox())&&actor.feetBox().getMinY()>=box.getMaxY()-1e-6,"Dash crossed a house wall");
            check(!actor.dashing(),"House wall failed to stop dash");Fx.clear();
        }
        for(BufferedImage image:VillageArt.houses){
            check(image.getWidth()==224&&image.getHeight()==192&&image.getColorModel().hasAlpha(),"Invalid house canvas");
            int pixels=0;
            for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.getRGB(x,y)>>>24)>128)pixels++;
            check(pixels>5000&&pixels<224*192*.8,"House alpha silhouette is blank or rectangular");
            int foot=0;for(int x=0;x<224;x++)if((image.getRGB(x,191)>>>24)>128)foot++;
            check(foot>0,"House feet float above the shared ground baseline");
        }
    }
    // [BLOCK: TEST HELPER] VillageTests.mapChecks - Handle map checks for VillageTests.
    private static void mapChecks(World world){
        VillageMap map=new VillageMap(world);QuestLog quests=new QuestLog();Player actor=new Player(30.5*World.T,21.5*World.T);
        long before=fingerprint(world);Set<Long> directions=new HashSet<>();
        for(Player.Dir dir:Player.Dir.values()){
            actor.dir=dir;BufferedImage image=mapImage(map,actor,quests,1);
            Rectangle green=greenBounds(image,16,16,128,96);
            check(green!=null&&green.width>=3&&green.height>=3,"Missing minimap arrow");
            check(Math.abs(green.getCenterX()-(16+actor.x/(world.cols*World.T)*128))<2&&Math.abs(green.getCenterY()-(16+actor.y/(world.rows*World.T)*96))<2,"Arrow not at player feet");
            long hash=1;
            for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if(isGreen(image.getRGB(x,y)))hash=hash*31+x+y*160;
            directions.add(hash);
        }
        check(directions.size()==4,"Arrow facing never changes");
        actor.dir=Player.Dir.RIGHT;actor.px=20*World.T;actor.py=18*World.T;actor.x=28*World.T;actor.y=26*World.T;
        for(double alpha:new double[]{0,.5,1}){
            Rectangle green=greenBounds(mapImage(map,actor,quests,alpha),16,16,128,96);
            double ex=16+(actor.px+(actor.x-actor.px)*alpha)/(world.cols*World.T)*128,ey=16+(actor.py+(actor.y-actor.py)*alpha)/(world.rows*World.T)*96;
            check(green!=null&&Math.abs(green.getCenterX()-ex)<2&&Math.abs(green.getCenterY()-ey)<2,"Arrow interpolation failed");
        }
        Player forestEdge=new Player((world.cols-6.5)*World.T,(world.rows-8.5)*World.T);
        Rectangle farArrow=greenBounds(mapImage(map,forestEdge,quests,1),16,16,128,96);
        check(farArrow!=null&&Math.abs(farArrow.getCenterX()-(16+forestEdge.x/(world.cols*World.T)*128))<2
                &&Math.abs(farArrow.getCenterY()-(16+forestEdge.y/(world.rows*World.T)*96))<2,"Southeast forest arrow is clipped or uses the old village dimensions");
        BufferedImage image=new BufferedImage(400,300,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{
            g.translate(2,3);g.clipRect(0,0,390,290);g.setColor(Color.MAGENTA);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.37f));g.setStroke(new BasicStroke(3));
            AffineTransform transform=g.getTransform();Rectangle clip=g.getClipBounds();Composite composite=g.getComposite();
            Stroke stroke=g.getStroke();Color color=g.getColor();
            map.draw(g,actor,quests,16,16,320,240,.5,true);
            check(g.getTransform().equals(transform)&&g.getClipBounds().equals(clip)&&g.getComposite().equals(composite)
                    &&g.getStroke().equals(stroke)&&g.getColor().equals(color),"Map leaked graphics state");
        }finally{g.dispose();}
        check(fingerprint(world)==before,"Map drawing mutated terrain");
        check(actor.x==28*World.T&&actor.px==20*World.T,"Map drawing moved player");
    }
    // [BLOCK: TEST HELPER] VillageTests.panelChecks - Handle panel checks for VillageTests.
    private static void panelChecks()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);press(panel.keys,KeyEvent.VK_ENTER);
        panel.update(1.0/60);panel.keys.clear();
        World world=(World)field(panel,"world");Player actor=(Player)field(panel,"player");QuestLog quests=(QuestLog)field(panel,"quests");
        BufferedImage hud=drawPrivate(panel,"drawHud"),expected=new BufferedImage(GamePanel.VIEW_W,GamePanel.VIEW_H,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=expected.createGraphics();
        try{new VillageMap(world).draw(g,actor,quests,372,26,96,72,0,false);}finally{g.dispose();}
        check(sameRegion(hud,expected,372,26,96,72),"HUD retains old timer or corrupts minimap");
        check(greenBounds(hud,372,26,96,72)!=null,"HUD arrow missing");
        press(panel.keys,KeyEvent.VK_J);panel.update(1.0/60);panel.keys.clear();
        BufferedImage combo=drawPrivate(panel,"drawHud");
        check(actor.comboStep()>0&&sameRegion(hud,combo,372,26,96,72),"Combo overlaps minimap");
        check(greenBounds(drawPrivate(panel,"drawMap"),80,72,320,240)!=null,"Full map arrow missing");
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] VillageTests.previews - Handle previews for VillageTests.
    private static void previews(World world)throws Exception{
        BufferedImage overview=new BufferedImage(world.cols*World.T/4,world.rows*World.T/4,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=overview.createGraphics();g.scale(.25,.25);
        try{
            world.drawGround(g,0,0,world.cols*World.T,world.rows*World.T,0);
            List<Drawable> objects=new ArrayList<>();objects.addAll(world.trees);objects.addAll(world.buildings);world.collectLandscape(objects,0,0,world.cols*World.T,world.rows*World.T);
            objects.add(new Player(World.SPAWN_X,World.SPAWN_Y));objects.sort(Comparator.comparingDouble(Drawable::sortY));
            for(Drawable object:objects)object.draw(g,0,0,1);
        }finally{g.dispose();}
        check(ImageIO.write(overview,"png",new File("build/preview-village-overview.png")),"Overview export failed");
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);press(panel.keys,KeyEvent.VK_ENTER);panel.update(1.0/60);panel.keys.clear();
        World.Building house=((World)field(panel,"world")).buildings.get(6);
        Player actor=(Player)field(panel,"player");actor.x=actor.px=house.x+105;actor.y=actor.py=house.y-25;actor.dir=Player.Dir.LEFT;
        set(panel,"camX",house.x-220);set(panel,"prevCamX",house.x-220);set(panel,"camY",house.y-305);set(panel,"prevCamY",house.y-305);
        BufferedImage street=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);g=street.createGraphics();
        try{panel.paint(g);}finally{g.dispose();}
        check(ImageIO.write(street,"png",new File("build/preview-village-street.png")),"Street export failed");
    }
    // [BLOCK: TEST HELPER] VillageTests.mapImage - Handle map image for VillageTests.
    private static BufferedImage mapImage(VillageMap map,Player actor,QuestLog quests,double alpha){
        BufferedImage image=new BufferedImage(160,130,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{map.draw(g,actor,quests,16,16,128,96,alpha,false);}finally{g.dispose();}return image;
    }
    // [BLOCK: TEST HELPER] VillageTests.drawPrivate - Render draw private using the current graphics state.
    private static BufferedImage drawPrivate(GamePanel panel,String name)throws Exception{
        BufferedImage image=new BufferedImage(GamePanel.VIEW_W,GamePanel.VIEW_H,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{Method m=GamePanel.class.getDeclaredMethod(name,Graphics2D.class);m.setAccessible(true);m.invoke(panel,g);}finally{g.dispose();}return image;
    }
    // [BLOCK: TEST HELPER] VillageTests.isGreen - Handle is green for VillageTests.
    private static boolean isGreen(int rgb){return rgb==0xFF7EFF75||rgb==0xFFD5FFBC;}
    // [BLOCK: TEST HELPER] VillageTests.greenBounds - Handle green bounds for VillageTests.
    private static Rectangle greenBounds(BufferedImage image,int x,int y,int width,int height){
        Rectangle result=null;
        for(int yy=y;yy<y+height;yy++)for(int xx=x;xx<x+width;xx++)if(isGreen(image.getRGB(xx,yy))){
            if(result==null)result=new Rectangle(xx,yy,1,1);else result.add(new Rectangle(xx,yy,1,1));
        }
        return result;
    }
    // [BLOCK: TEST HELPER] VillageTests.sameRegion - Handle same region for VillageTests.
    private static boolean sameRegion(BufferedImage a,BufferedImage b,int x,int y,int width,int height){
        for(int yy=y;yy<y+height;yy++)for(int xx=x;xx<x+width;xx++)if(a.getRGB(xx,yy)!=b.getRGB(xx,yy))return false;return true;
    }
    // [BLOCK: TEST HELPER] VillageTests.fingerprint - Handle fingerprint for VillageTests.
    private static long fingerprint(World world){
        long hash=1;for(int[] row:world.tile)for(int t:row)hash=hash*31+t;
        for(World.Building h:world.buildings)hash=hash*31+Double.doubleToLongBits(h.x+h.y+h.footprint.width);return hash;
    }
    // [BLOCK: TEST HELPER] VillageTests.field - Handle field for VillageTests.
    private static Object field(Object object,String name)throws Exception{
        Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);
    }
    // [BLOCK: TEST HELPER] VillageTests.set - Handle set for VillageTests.
    private static void set(Object object,String name,double value)throws Exception{
        Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);f.setDouble(object,value);
    }
    // [BLOCK: TEST HELPER] VillageTests.press - Handle press for VillageTests.
    private static void press(KeyHandler keys,int code){keys.keyPressed(new KeyEvent(SOURCE,KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: TEST HELPER] VillageTests.check - Handle check for VillageTests.
    private static void check(boolean valid,String message){checks++;if(!valid)throw new AssertionError(message);}
}
