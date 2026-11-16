/* FILE PURPOSE: REGRESSION TEST | Five-minute timing, reversible outdoor night strength, paused clocks and readable cached lighting.
 * Course examples: state boundaries, polymorphic combat and read-only rendering; docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.imageio.ImageIO;

/** Tests long intervals by simulation, without wall-clock sleeps or accelerated production timers. */
public final class DayNightTests {
    private static final double DT=1.0/60;
    private static final KeyHandler.Input NONE=new KeyHandler.Input(false,false,false,false,false,false,0);
    private static int checks;
    // [BLOCK: TEST UTILITY] DayNightTests.DayNightTests - Prevent construction of this focused suite.
    private DayNightTests(){}
    // [BLOCK: TEST ENTRY] DayNightTests.main - Load the real assets and run the focused cycle checks.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Day/night checks passed: "+run());}
    // [BLOCK: REGRESSION SUITE] DayNightTests.run - Verify timing, actual damage/movement, spawns, session controls and lighting.
    static int run()throws Exception{checks=0;clock();World world=new World(7);strength(world);bosses(world);spawns(world);session();lighting(world);previews();Fx.clear();return checks;}
    // [BLOCK: FIVE-MINUTE BOUNDARIES] DayNightTests.clock - Verify numbered phases, exact fixed-step crossings, coarse steps and continuous light transitions.
    private static void clock(){
        DayNightCycle cycle=new DayNightCycle();
        check(cycle.phase()==DayNightCycle.Phase.DAY&&cycle.number()==1&&cycle.countdown().equals("05:00"),"Journey does not begin at Day 1");
        for(int i=0;i<17999;i++)cycle.update(DT);
        check(!cycle.night()&&cycle.remainingSeconds()==1,"Day ends before five active minutes");
        cycle.update(DT);check(cycle.night()&&cycle.number()==1&&close(cycle.phaseSeconds(),0)&&cycle.countdown().equals("05:00"),"Night 1 misses the five-minute boundary");
        for(int i=0;i<18000;i++)cycle.update(DT);
        check(!cycle.night()&&cycle.number()==2&&close(cycle.phaseSeconds(),0),"Dawn accumulates fixed-step timing drift");
        cycle.update(3*300+17.25);check(cycle.night()&&cycle.number()==3&&close(cycle.phaseSeconds(),17.25),"Coarse clock update loses phases or remainder");
        double before=cycle.phaseSeconds();cycle.update(0);cycle.update(-5);cycle.update(Double.NaN);cycle.update(Double.POSITIVE_INFINITY);
        check(cycle.phaseSeconds()==before,"Invalid elapsed time corrupts the world clock");
        DayNightCycle light=new DayNightCycle();light.update(288);check(light.darkness()==0,"Dusk begins outside the final transition interval");
        light.update(6);check(close(light.darkness(),.5),"Dusk has no smooth midpoint");
        light.update(5.999);double dusk=light.darkness();light.update(.001);
        check(close(light.darkness(),1)&&Math.abs(light.darkness()-dusk)<.001,"Night lighting jumps at the phase boundary");
        light.update(294);check(close(light.darkness(),.5),"Dawn has no smooth midpoint");
        light.update(6);check(!light.night()&&light.darkness()==0,"Daybreak does not remove the night tint");
        for(int i=0;i<200;i++)light.update(300);
        check(!light.night()&&light.number()==102,"Repeated nights lose their cycle number");
    }
    // [BLOCK: PHASE FIXTURE] DayNightTests.phase - Advance to the next requested phase while retaining the real production interval.
    private static void phase(World world,boolean night){if(world.dayNight.night()!=night)world.dayNight.update(300-world.dayNight.phaseSeconds());}
    // [BLOCK: CONTACT / PURSUIT] DayNightTests.strength - Compare real day/night contact hits and chase travel, including dawn and village exclusion.
    private static void strength(World world){
        ForestRegion.Arena arena=world.forest.arenas().get(0);double x=(arena.c()+.5)*World.T,y=(arena.r()+.5)*World.T;
        phase(world,false);Player player=new Player(x,y);Enemy day=new Slime(x+6,y,0);day.update(DT,player,world,null);
        check(player.health()==4&&!day.nightEmpowered(),"Daytime ordinary enemy damage changed");Fx.clear();
        phase(world,true);player=new Player(x,y);Enemy night=new Slime(x+6,y,0);night.update(DT,player,world,null);
        check(player.health()==3&&night.nightEmpowered(),"Outdoor night contact does not gain one damage");Fx.clear();
        Player indoors=new Player(World.SPAWN_X,World.SPAWN_Y);Enemy indoor=new Slime(indoors.x+6,indoors.y,0);indoor.update(DT,indoors,world,null);
        check(indoors.health()==4&&!indoor.nightEmpowered(),"Village enemies receive the outdoor-only night bonus");Fx.clear();
        Player target=new Player(x+120,y);int[][] flow=Pathfinder.distances(world,World.col(target.x),World.row(target.y));
        phase(world,false);Enemy slow=new Slime(x,y,0);slow.update(.25,target,world,flow);double baseline=Math.hypot(slow.x-x,slow.y-y);
        phase(world,true);Enemy fast=new Slime(x,y,0);fast.update(.25,target,world,flow);
        check(baseline>1&&close(Math.hypot(fast.x-x,fast.y-y),baseline*1.25),"Outdoor night pursuit is not twenty-five percent faster");
        fast.damage(1,fast.x-20,fast.y);int health=fast.health(),max=fast.maxHealth();Fx.clear();
        phase(world,false);fast.refreshNightPower(world);
        check(!fast.nightEmpowered()&&fast.attackDamage(1)==1&&fast.health()==health&&fast.maxHealth()==max,"Dawn heals, changes maximum health or retains a night bonus");
        phase(world,true);fast.refreshNightPower(world);phase(world,false);phase(world,true);fast.refreshNightPower(world);
        check(fast.attackDamage(1)==2&&fast.pursuitMultiplier()==1.25&&fast.health()==health,"Repeated nights stack strength or restore health");
        fast.x=World.SPAWN_X;fast.y=World.SPAWN_Y;fast.refreshNightPower(world);check(!fast.nightEmpowered(),"Night power persists after entering the village");
        fast.x=x;fast.y=y;fast.refreshNightPower(world);fast.damage(100,0,0);fast.refreshNightPower(world);
        check(!fast.alive()&&!fast.nightEmpowered()&&fast.health()==0,"A dead enemy retains night power or revives");Fx.clear();
    }
    // [BLOCK: BOSS NIGHT ATTACKS] DayNightTests.bosses - Exercise all villain hit paths and the guardian pulse without changing warning times or health.
    private static void bosses(World world)throws Exception{
        for(int id=1;id<=3;id++){
            phase(world,false);ArenaBoss day=boss(world,id);Player target=new Player(day.x+70+(id==2?100:0),day.y);int max=day.maxHealth();
            until(day,target,world,b->b.state()==ArenaBoss.State.WINDUP,200);double dayWarning=warningDuration(day);
            until(day,target,world,b->target.health()<5,400);int damage=5-target.health();
            phase(world,true);ArenaBoss night=boss(world,id);Player victim=new Player(night.x+70+(id==2?100:0),night.y);
            until(night,victim,world,b->b.state()==ArenaBoss.State.WINDUP,200);
            check(night.nightEmpowered()&&warningDuration(night)==dayWarning&&night.maxHealth()==max&&night.health()==max,"Night alters a boss warning or its health");
            for(int i=0;i<15;i++){night.update(DT,victim,world,null);Fx.clear();}
            check(victim.health()==5,"Night boss hits before its warning finishes");
            until(night,victim,world,b->victim.health()<5,400);
            check(5-victim.health()==damage+1&&night.attackDamage(0)==0,"Boss night damage is missing or introduces contact damage");
        }
        phase(world,true);TankBoss tank=new TankBoss(world.forest.arenas().get(0));Player guard=new Player(tank.x+65,tank.y);guard.state=Player.State.GUARD;guard.dir=Player.Dir.LEFT;
        until(tank,guard,world,b->b.state()==ArenaBoss.State.RECOVER,300);check(guard.health()==5,"Night damage bypasses a frontal shield");
        TankBoss evade=new TankBoss(world.forest.arenas().get(0));Player dash=new Player(evade.x+65,evade.y);dash.dir=Player.Dir.RIGHT;
        until(evade,dash,world,b->b.state()==ArenaBoss.State.STRIKE,300);
        dash.update(DT,new KeyHandler.Input(false,false,false,false,false,false,KeyHandler.DASH),world,List.of());
        check(dash.dashing()&&close(dash.energy(),75),"Night dash fixture did not use the real energy/input path");
        for(int i=0;i<35;i++){dash.update(DT,NONE,world,List.of());evade.update(DT,dash,world,null);Fx.clear();}
        check(dash.health()==5,"Night attack bypasses a real dash evade");
        Guardian guardian=new Guardian(tank.homeX,tank.homeY);Player victim=new Player(guardian.x+40,guardian.y);set(guardian,"pulseTime",3.99);
        guardian.update(.02,victim,world,null);check(victim.health()==2&&guardian.nightEmpowered(),"Guardian pulse ignores outdoor night power");Fx.clear();
    }
    // [BLOCK: ENCOUNTER FACTORY] DayNightTests.boss - Build a fresh concrete villain in its real clearing.
    private static ArenaBoss boss(World world,int id){ForestRegion.Arena a=world.forest.arenas().get(id-1);return switch(id){case 1->new TankBoss(a);case 2->new MageBoss(a);default->new FighterBoss(a);};}
    // [BLOCK: WARNING QUERY] DayNightTests.warningDuration - Inspect an existing private timing value without modifying attack policy.
    private static double warningDuration(ArenaBoss boss)throws Exception{return (double)getFrom(ArenaBoss.class,boss,"warnDuration");}
    // [BLOCK: NIGHT SPAWNS] DayNightTests.spawns - Verify freshly populated and replenished outdoor enemies inherit the current phase once.
    private static void spawns(World world){
        phase(world,true);ForestEncounters encounters=new ForestEncounters(world);List<Enemy> enemies=new ArrayList<>();encounters.populate(enemies);
        check(enemies.size()==21&&enemies.stream().allMatch(Enemy::nightEmpowered),"A night spawn has one unpowered frame");
        Enemy ambient=enemies.stream().filter(e->e.shrineGroup==-1).findFirst().orElseThrow();ambient.damage(100,0,0);enemies.remove(ambient);Fx.clear();
        Player far=new Player(22.5*World.T,25.5*World.T);encounters.update(25,far,enemies);
        check(enemies.stream().filter(e->e.shrineGroup==-1&&e.alive()).count()==8&&enemies.stream().allMatch(Enemy::nightEmpowered),"Night ambient replenishment loses the bonus or duplicates slots");
        phase(world,false);for(Enemy enemy:enemies)enemy.refreshNightPower(world);
        check(enemies.stream().noneMatch(Enemy::nightEmpowered),"Dawn leaves existing or replenished enemies empowered");
    }
    // [BLOCK: SESSION CLOCK] DayNightTests.session - Check real start, pause, map, journal, focus, hit-stop, defeat and restart timing.
    @SuppressWarnings("unchecked")
    private static void session()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);World world=(World)get(panel,"world");panel.update(301);
        check(world.dayNight.phaseSeconds()==0&&!world.dayNight.night(),"Title screen advances the day/night clock");tap(panel,KeyEvent.VK_ENTER);
        ((List<?>)get(panel,"enemies")).clear();((List<?>)get(panel,"npcs")).clear();world.updateAmbient(299.9);panel.update(.1);
        check(world.dayNight.night()&&world.dayNight.number()==1,"Real world update misses Night 1");
        tap(panel,KeyEvent.VK_M);double before=world.dayNight.phaseSeconds();panel.update(301);check(world.dayNight.phaseSeconds()==before,"Map advances the world clock");tap(panel,KeyEvent.VK_M);
        tap(panel,KeyEvent.VK_TAB);before=world.dayNight.phaseSeconds();panel.update(301);check(world.dayNight.phaseSeconds()==before,"Journal advances the world clock");tap(panel,KeyEvent.VK_TAB);
        tap(panel,KeyEvent.VK_ESCAPE);before=world.dayNight.phaseSeconds();panel.update(301);check(world.dayNight.phaseSeconds()==before,"Pause advances the world clock");tap(panel,KeyEvent.VK_ENTER);
        for(java.awt.event.FocusListener listener:panel.getFocusListeners())listener.focusLost(new FocusEvent(panel,FocusEvent.FOCUS_LOST));
        before=world.dayNight.phaseSeconds();panel.update(301);check(world.dayNight.phaseSeconds()==before,"Lost focus advances the world clock");tap(panel,KeyEvent.VK_ENTER);
        Fx.hitStop=.1;before=world.dayNight.phaseSeconds();panel.update(.02);check(world.dayNight.phaseSeconds()==before,"Hit-stop desynchronizes the world clock");Fx.clear();
        Player player=(Player)get(panel,"player");player.damage(100,0,0);Fx.clear();panel.update(DT);before=world.dayNight.phaseSeconds();panel.update(301);
        check(world.dayNight.phaseSeconds()==before,"Defeat advances the world clock");tap(panel,KeyEvent.VK_R);
        world=(World)get(panel,"world");check(!world.dayNight.night()&&world.dayNight.number()==1&&world.dayNight.phaseSeconds()==0,"Restart preserves an old night or timer");
        check(((List<Enemy>)get(panel,"enemies")).stream().noneMatch(Enemy::nightEmpowered),"Restart preserves enemy night power");
    }
    // [BLOCK: LIGHTING CACHE] DayNightTests.lighting - Check scene tint, local visibility, cache reuse and graphics/simulation preservation.
    private static void lighting(World world)throws Exception{
        ForestRegion.Arena a=world.forest.arenas().get(0);Player player=new Player((a.c()+.5)*World.T,(a.r()+.5)*World.T);
        int cx=(int)player.x-240,cy=(int)player.y-180;WorldLighting lighting=new WorldLighting(480,360);Object cache=get(lighting,"shade");
        phase(world,false);BufferedImage day=lightFrame(lighting,world,player,cx,cy);phase(world,true);double before=world.dayNight.phaseSeconds();
        BufferedImage night=lightFrame(lighting,world,player,cx,cy),repeat=lightFrame(lighting,world,player,cx,cy);
        check(hash(day)!=hash(night)&&hash(night)==hash(repeat),"Night tint is missing or drawing advances lighting");
        check(luma(night.getRGB(240,150))>luma(night.getRGB(5,5))+60,"Night hides Agyu inside an unreadable dark screen");
        check(get(lighting,"shade")==cache&&world.dayNight.phaseSeconds()==before,"Lighting reallocates its viewport cache or advances the clock");
        Graphics2D g=night.createGraphics();try{
            g.translate(3,4);g.clipRect(0,0,450,330);g.setColor(Color.MAGENTA);g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.4f));AffineTransform transform=g.getTransform();java.awt.Rectangle clip=g.getClipBounds();
            lighting.draw(g,world,player,List.of(),cx,cy,1);
            check(g.getTransform().equals(transform)&&g.getClipBounds().equals(clip)&&g.getColor().equals(Color.MAGENTA)&&g.getComposite().equals(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.4f)),"Night rendering leaks graphics state");
        }finally{g.dispose();}
    }
    // [BLOCK: LIGHTING FIXTURE] DayNightTests.lightFrame - Shade a known scene color to compare actual visibility pixels.
    private static BufferedImage lightFrame(WorldLighting lighting,World world,Player player,int cx,int cy){BufferedImage image=new BufferedImage(480,360,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();try{g.setColor(new Color(0x8CB97C));g.fillRect(0,0,480,360);lighting.draw(g,world,player,List.of(),cx,cy,1);}finally{g.dispose();}return image;}
    // [BLOCK: BRIGHTNESS QUERY] DayNightTests.luma - Compare scene brightness without relying on one palette shade.
    private static int luma(int rgb){return ((rgb>>>16)&255)+((rgb>>>8)&255)+(rgb&255);}
    // [BLOCK: ACTUAL SCENE PREVIEWS] DayNightTests.previews - Export the same village camera by day/night and a powered night boss encounter.
    @SuppressWarnings("unchecked")
    private static void previews()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);tap(panel,KeyEvent.VK_ENTER);World world=(World)get(panel,"world");
        Player actor=(Player)get(panel,"player");List<Enemy> enemies=(List<Enemy>)get(panel,"enemies");
        World.Building home=world.buildings.get(0);actor.x=actor.px=home.x+84;actor.y=actor.py=home.y+35;actor.dir=Player.Dir.LEFT;
        camera(panel,home.x-220,home.y-230);BufferedImage day=render(panel);ImageIO.write(day,"png",new File("build/preview-day.png"));
        world.updateAmbient(300);for(Enemy enemy:enemies)enemy.refreshNightPower(world);
        BufferedImage night=render(panel);ImageIO.write(night,"png",new File("build/preview-night.png"));
        check(day.getRGB(140,32)==night.getRGB(140,32),"Night overlay darkens the health HUD");
        BufferedImage gallery=new BufferedImage(GamePanel.W*2,GamePanel.H+28,BufferedImage.TYPE_INT_RGB);Graphics2D g=gallery.createGraphics();
        try{g.setColor(new Color(0x23342E));g.fillRect(0,0,gallery.getWidth(),gallery.getHeight());g.setColor(new Color(0xF2D899));g.drawString("DAY 1 / FIVE ACTIVE MINUTES",12,18);g.drawString("NIGHT 1 / STRONGER OUTDOOR ENEMIES",GamePanel.W+12,18);g.drawImage(day,0,28,null);g.drawImage(night,GamePanel.W,28,null);}finally{g.dispose();}
        ImageIO.write(gallery,"png",new File("build/preview-day-night.png"));
        ArenaBoss boss=(ArenaBoss)enemies.stream().filter(e->e instanceof TankBoss).findFirst().orElseThrow();actor.x=actor.px=boss.x+80;actor.y=actor.py=boss.y+45;actor.invincible=1000;
        for(int i=0;i<250&&boss.state()!=ArenaBoss.State.WINDUP;i++){Fx.clear();panel.update(DT);}actor.invincible=0;
        camera(panel,boss.x-240,boss.y-205);check(boss.nightEmpowered()&&boss.engaged(),"Night boss preview is not an actual empowered encounter");
        ImageIO.write(render(panel),"png",new File("build/preview-night-boss.png"));
        actor.x=actor.px=(MountainRiver.CENTER_C+.5)*World.T;actor.y=actor.py=84.5*World.T;actor.dir=Player.Dir.UP;
        camera(panel,actor.x-240,actor.y-200-world.mountain.lookAhead(actor.x,actor.y));ImageIO.write(render(panel),"png",new File("build/preview-night-waterfall.png"));
    }
    // [BLOCK: CAMERA FIXTURE] DayNightTests.camera - Freeze the real interpolated camera at a known scene.
    private static void camera(GamePanel panel,double x,double y)throws Exception{set(panel,"camX",x);set(panel,"prevCamX",x);set(panel,"camY",y);set(panel,"prevCamY",y);set(panel,"alpha",1);}
    // [BLOCK: ACTUAL RENDER] DayNightTests.render - Paint the GamePanel rather than a simplified replacement scene.
    private static BufferedImage render(GamePanel panel){BufferedImage image=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();try{panel.paint(g);}finally{g.dispose();}return image;}
    // [BLOCK: PIXEL FINGERPRINT] DayNightTests.hash - Compare a whole rendered image without inflating assertion counts by pixel.
    private static long hash(BufferedImage image){long value=1;for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)value=value*31+image.getRGB(x,y);return value;}
    // [BLOCK: SIMULATION WAIT] DayNightTests.until - Advance real boss updates to a bounded observable condition.
    private static void until(ArenaBoss boss,Player player,World world,Predicate<ArenaBoss> condition,int limit){for(int i=0;i<limit&&!condition.test(boss);i++){boss.update(DT,player,world,null);Fx.clear();}check(condition.test(boss),"Timed out waiting for "+boss.questId()+" "+boss.state());}
    // [BLOCK: INPUT FIXTURE] DayNightTests.tap - Dispatch a fresh key edge through the real panel input.
    private static void tap(GamePanel panel,int key){panel.keys.keyPressed(new KeyEvent(panel,KeyEvent.KEY_PRESSED,0,0,key,KeyEvent.CHAR_UNDEFINED));panel.update(DT);panel.keys.clear();}
    // [BLOCK: TEST QUERY] DayNightTests.get - Inspect private cache/session state without widening production APIs.
    private static Object get(Object object,String name)throws Exception{return getFrom(object.getClass(),object,name);}
    // [BLOCK: TEST QUERY] DayNightTests.getFrom - Read a private field declared by an explicit class.
    private static Object getFrom(Class<?> type,Object object,String name)throws Exception{Field field=type.getDeclaredField(name);field.setAccessible(true);return field.get(object);}
    // [BLOCK: CAMERA / ATTACK FIXTURE] DayNightTests.set - Position the actual camera or an existing guardian attack timer.
    private static void set(Object object,String name,Object value)throws Exception{Field field=object.getClass().getDeclaredField(name);field.setAccessible(true);field.set(object,value);}
    // [BLOCK: FLOATING-POINT CHECK] DayNightTests.close - Compare elapsed-time and movement values at simulation precision.
    private static boolean close(double a,double b){return Math.abs(a-b)<1e-6;}
    // [BLOCK: ASSERTION] DayNightTests.check - Count a meaningful property and describe its failure.
    private static void check(boolean result,String message){checks++;if(!result)throw new AssertionError(message);}
}
