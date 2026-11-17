/* FILE PURPOSE: FOREST REVISION REGRESSION | Seeded herd distribution, removed map markers and night-only hostile Sigbin behavior.
 * Course examples: reachability, immutable records and real session interactions; docs/CODE_GUIDE.md.
 */
import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;

/** Fixtures observe the real world and session; removed beacons do not leave hidden interactions. */
public final class ForestRevisionTests {
    private static int checks;
    // [BLOCK: TEST UTILITY] ForestRevisionTests.ForestRevisionTests - Prevent instances of this focused suite.
    private ForestRevisionTests(){}
    // [BLOCK: TEST ENTRY] ForestRevisionTests.main - Load the real assets and run the forest revision checks.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Forest revision checks passed: "+run());}
    // [BLOCK: REGRESSION SUITE] ForestRevisionTests.run - Verify distribution over multiple seeds, removed live map markers, and plain spawn/clearing interactions.
    static int run()throws Exception{checks=0;World world=new World(7);distribution(world);markers(world);interactions();nightOnly(world);distributionPreview(world);return checks;}
    // [BLOCK: RANDOM HERD CONTRACT] ForestRevisionTests.distribution - Check seeded repeatability, different journeys, one-to-four-member herds and broad map coverage.
    private static void distribution(World world){
        String previous="";Set<String> layouts=new HashSet<>();
        for(int seed=0;seed<12;seed++){
            ForestEncounters population=new ForestEncounters(world,seed);List<Enemy> enemies=new ArrayList<>();population.populate(enemies);
            check(population.herds().size()==12&&enemies.size()==population.capacity()+3,"Herd population is inconsistent");
            Set<Integer> sizes=new HashSet<>();int minC=world.cols,maxC=0,minR=world.rows,maxR=0,sum=0;StringBuilder signature=new StringBuilder();
            for(ForestEncounters.Herd herd:population.herds()){
                check(herd.count()>=1&&herd.count()<=4,"Boar group exceeds the requested size");sizes.add(herd.count());sum+=herd.count();
                minC=Math.min(minC,herd.c());maxC=Math.max(maxC,herd.c());minR=Math.min(minR,herd.r());maxR=Math.max(maxR,herd.r());signature.append(herd);
                for(int i=herd.first();i<herd.first()+herd.count();i++){
                    Enemy boar=enemies.get(i);check(boar instanceof WildBoar&&boar.spawnGroup==herd.id(),"Herd slot has the wrong actor or identity");
                    check(world.forest.contains(boar.x,boar.y)&&world.walkable(World.col(boar.x),World.row(boar.y))&&!world.blocked(boar.feetBox()),"Random boar is trapped or inside the village");
                    check(Math.hypot(boar.x-(herd.c()+.5)*World.T,boar.y-(herd.r()+.5)*World.T)<World.T*3,"Herd members are scattered across unrelated locations");
                }
            }
            check(sum==population.capacity()&&sizes.size()==4,"Random groups omit singles, pairs, trios or fours");
            check(maxC-minC>world.cols*.5&&maxR-minR>world.rows*.5,"Boars are concentrated in one part of the map");
            String current=signature.toString();layouts.add(current);if(seed==7)previous=current;
        }
        check(layouts.size()==12,"Different journey seeds reuse the same herd centers");
        StringBuilder again=new StringBuilder();for(var herd:new ForestEncounters(world,7).herds())again.append(herd);check(previous.equals(again.toString()),"Fixed seed cannot reproduce a herd layout");
    }
    // [BLOCK: OFFSCREEN OBSERVER FIXTURE] ForestRevisionTests.observer - Find real free forest ground well away from every herd for respawn checks.
    static Player observer(World world,List<ForestEncounters.Herd> herds){
        double best=-1,bx=0,by=0;
        for(int r=2;r<world.rows-2;r+=3)for(int c=2;c<world.cols-2;c+=3){
            double x=(c+.5)*World.T,y=(r+.5)*World.T;if(!world.walkable(c,r)||!world.forest.contains(x,y)||world.blocked(new java.awt.geom.Rectangle2D.Double(x-10,y-10,20,10)))continue;
            double nearest=Double.MAX_VALUE;for(var herd:herds)nearest=Math.min(nearest,Math.hypot(x-(herd.c()+.5)*World.T,y-(herd.r()+.5)*World.T));
            if(nearest>best){best=nearest;bx=x;by=y;}
        }
        if(best<550)throw new AssertionError("No offscreen forest observer location exists");return new Player(bx,by);
    }
    // [BLOCK: LIVE MAP MARKER REMOVAL] ForestRevisionTests.markers - Compare old beacon positions with the cached map terrain while retaining the actual player/boss markers elsewhere.
    private static void markers(World world)throws Exception{
        VillageMap map=new VillageMap(world);Field field=VillageMap.class.getDeclaredField("terrain");field.setAccessible(true);BufferedImage terrain=(BufferedImage)field.get(map);
        BufferedImage image=new BufferedImage(256,192,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{map.draw(g,new Player(10.5*World.T,10.5*World.T),new QuestLog(),0,0,256,192,1,false);}finally{g.dispose();}
        for(int[] site:World.TRAIL_SITES){int x=(int)Math.round((site[0]+.5)/world.cols*256),y=(int)Math.round((site[1]+.5)/world.rows*192);
            boolean same=true;for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++)same&=image.getRGB(x+dx,y+dy)==terrain.getRGB(x+dx,y+dy);
            check(same,"A retired shrine/HOME marker remains on the live map");}
    }
    // [BLOCK: RETIRED INTERACTION REMOVAL] ForestRevisionTests.interactions - Visit every former beacon and press real E input; only normal village interactions remain.
    private static void interactions()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);tap(panel,KeyEvent.VK_ENTER);Player player=(Player)get(panel,"player");QuestLog quests=(QuestLog)get(panel,"quests");
        for(int[] site:World.TRAIL_SITES){player.x=player.px=(site[0]+.5)*World.T;player.y=player.py=(site[1]+.5)*World.T;
            int healthPotions=player.inventory().count(Inventory.Item.HEALTH_POTION),events=quests.history().size();tap(panel,KeyEvent.VK_E);
            check(get(panel,"mode")==GamePanel.Mode.PLAYING&&quests.bossesDefeated()==0&&!quests.done("forest"),"A retired beacon still controls progression");
            check(player.inventory().count(Inventory.Item.HEALTH_POTION)==healthPotions&&quests.history().size()==events,"A retired beacon still grants rewards or awakens an enemy");
        }
        player.x=player.px=World.SPAWN_X;player.y=player.py=World.SPAWN_Y;camera(panel,player.x-240,player.y-180);image(panel,"build/preview-home-without-beacon.png");
        int[] clearing=World.TRAIL_SITES[3];player.x=player.px=(clearing[0]+.5)*World.T;player.y=player.py=(clearing[1]+.5)*World.T;camera(panel,player.x-240,player.y-180);image(panel,"build/preview-clearing-without-beacon.png");
    }
    // [BLOCK: NIGHT-ONLY AMBUSH] ForestRevisionTests.nightOnly - Walk through dense woodland all day and confirm the director cannot spawn a Sigbin.
    private static void nightOnly(World world){
        NightWildlife director=new NightWildlife(world);java.awt.Point camp=world.forest.camp(0);Player player=new Player((camp.x+.5)*World.T,(camp.y+.5)*World.T);List<Enemy> enemies=new ArrayList<>();double center=player.x;
        for(int second=0;second<600;second++){player.x=center+(second%2==0?40:-40);director.update(1,player,enemies);}
        check(enemies.stream().noneMatch(e->e instanceof Sigbin),"Daytime forest travel still spawns a Sigbin");
    }
    // [BLOCK: WORLD DISTRIBUTION EVIDENCE] ForestRevisionTests.distributionPreview - Render the real seeded forest and annotate herd sizes only in this exported review image.
    private static void distributionPreview(World world)throws Exception{
        ForestEncounters population=new ForestEncounters(world,7);List<Enemy> enemies=new ArrayList<>();population.populate(enemies);
        BufferedImage image=new BufferedImage(world.cols*World.T/4,world.rows*World.T/4,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();
        try{g.scale(.25,.25);world.drawGround(g,0,0,world.cols*World.T,world.rows*World.T,0);
            List<Drawable> scene=new ArrayList<>();scene.addAll(world.trees);scene.addAll(world.buildings);scene.addAll(world.props);scene.addAll(enemies);
            world.collectLandscape(scene,0,0,world.cols*World.T,world.rows*World.T);scene.sort(java.util.Comparator.comparingDouble(Drawable::sortY));
            for(Drawable item:scene)item.draw(g,0,0,1);
        }finally{g.dispose();}
        g=image.createGraphics();try{g.setFont(new java.awt.Font("Monospaced",java.awt.Font.BOLD,14));
            for(var herd:population.herds()){
                int x=(int)((herd.c()+.5)*World.T/4),y=(int)((herd.r()+.5)*World.T/4);String label=(herd.id()+1)+" / "+herd.count()+" boars";
                g.setColor(new java.awt.Color(25,30,21,220));g.fillRect(x-4,y-22,g.getFontMetrics().stringWidth(label)+8,20);
                g.setColor(new java.awt.Color(0xFFD186));g.drawOval(x-7,y-7,14,14);g.drawString(label,x,y-7);
            }
        }finally{g.dispose();}
        check(ImageIO.write(image,"png",new File("build/preview-boar-distribution.png")),"Random herd preview failed");
    }
    // [BLOCK: CAMERA FIXTURE] ForestRevisionTests.camera - Set the actual camera for a plain spawn/clearing review screenshot.
    private static void camera(GamePanel panel,double x,double y)throws Exception{for(String n:new String[]{"camX","prevCamX"})set(panel,n,x);for(String n:new String[]{"camY","prevCamY"})set(panel,n,y);set(panel,"alpha",1);}
    // [BLOCK: ACTUAL RENDER EXPORT] ForestRevisionTests.image - Paint the normal game surface for visual verification.
    private static void image(GamePanel panel,String path)throws Exception{BufferedImage image=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();try{panel.paint(g);}finally{g.dispose();}check(ImageIO.write(image,"png",new File(path)),"Beacon removal preview failed");}
    // [BLOCK: REAL KEYBOARD FIXTURE] ForestRevisionTests.tap - Deliver one E/start edge through the session input path.
    private static void tap(GamePanel panel,int key)throws Exception{KeyHandler keys=(KeyHandler)get(panel,"keys");keys.keyPressed(new KeyEvent(new Canvas(),KeyEvent.KEY_PRESSED,0,0,key,KeyEvent.CHAR_UNDEFINED));panel.update(1d/60);keys.keyReleased(new KeyEvent(new Canvas(),KeyEvent.KEY_RELEASED,0,0,key,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: SESSION INSPECTION] ForestRevisionTests.get - Inspect existing private state without production test hooks.
    private static Object get(Object target,String name)throws Exception{Field field=target.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(target);}
    // [BLOCK: CAMERA INSPECTION] ForestRevisionTests.set - Set one existing camera field for the render fixture.
    private static void set(Object target,String name,double value)throws Exception{Field field=target.getClass().getDeclaredField(name);field.setAccessible(true);field.setDouble(target,value);}
    // [BLOCK: ASSERTION] ForestRevisionTests.check - Count a real behavior contract and identify the failure.
    private static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
}
