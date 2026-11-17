/* FILE PURPOSE: FOREST WILDLIFE DIRECTOR | Enforce nightly wolf quotas and occasional movement-driven Sigbin ambushes.
 * Course examples: encapsulated counters, deterministic randomness and bounded spawn searches; docs/CODE_GUIDE.md.
 */
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Random;

/** Wolves have a total per-night budget; Sigbin appearances require deep woodland exploration and a separate cooldown. */
final class NightWildlife {
    private final World world;
    private final Random random=new Random(0x57494C444C494645L);
    private long observedNight;
    private int wolfLimit,wolvesSpawned;
    private double wolfRetry,scareDelay=50,travel,lastX=Double.NaN,lastY;
    private Sigbin sigbin;
    // [SECTION: DSA - FIXED ARRAY] The rare night spawns are spread across the three-minute phase rather than replenished after kills.
    private static final double[] WOLF_TIMES={DayNightCycle.PHASE_SECONDS/15,DayNightCycle.PHASE_SECONDS*.4,DayNightCycle.PHASE_SECONDS*11/15};
    // [BLOCK: COMPOSITION] NightWildlife.NightWildlife - Bind one wildlife director to the current world/session.
    NightWildlife(World world){this.world=world;}
    // [BLOCK: ENCAPSULATED QUOTA] NightWildlife.wolfLimit - Expose the current night's total two-or-three spawn allowance.
    int wolfLimit(){return wolfLimit;}
    // [BLOCK: ENCAPSULATED QUOTA] NightWildlife.wolvesSpawned - Read successful spawns without allowing kills to replenish the counter.
    int wolvesSpawned(){return wolvesSpawned;}
    // [BLOCK: ACTIVE-TIME DIRECTOR] NightWildlife.update - Spawn rare predators from the world clock and gate Sigbin scares by forest travel, cooldown and safe placement.
    void update(double dt,Player player,List<Enemy> enemies){
        if(!Double.isFinite(dt)||dt<=0||!player.alive())return;
        wolfRetry=Math.max(0,wolfRetry-dt);
        if(world.dayNight.night()){
            if(observedNight!=world.dayNight.number()){
                observedNight=world.dayNight.number();wolfLimit=2+new Random(0x574F4C46L^(observedNight*0x9E3779B97F4A7C15L)).nextInt(2);wolvesSpawned=0;wolfRetry=0;scareDelay=50;travel=0;
            }
            while(wolvesSpawned<wolfLimit&&world.dayNight.phaseSeconds()>=WOLF_TIMES[wolvesSpawned]&&wolfRetry==0){
                Point point=wolfPosition(player,enemies);
                if(point==null){wolfRetry=2;break;}
                Wolf wolf=new Wolf((point.x+.5)*World.T,(point.y+.5)*World.T);wolf.refreshNightPower(world);enemies.add(wolf);wolvesSpawned++;
            }
        }
        if(!world.dayNight.night()){travel=0;lastX=player.x;lastY=player.y;return;}
        if(Double.isFinite(lastX))travel+=Math.min(12,Math.hypot(player.x-lastX,player.y-lastY));lastX=player.x;lastY=player.y;
        if(!deepForest(world,player.x,player.y)){travel=0;return;}
        scareDelay=Math.max(0,scareDelay-dt);
        if(scareDelay>0||travel<120||sigbin!=null&&!sigbin.removable()||nearArena(player.x,player.y))return;
        if(!dense(player.x,player.y)){scareDelay=2;return;}
        Point point=ambushPosition(player,enemies);
        if(point==null){scareDelay=3;return;}
        sigbin=new Sigbin((point.x+.5)*World.T,(point.y+.5)*World.T);sigbin.refreshNightPower(world);enemies.add(sigbin);
        travel=0;scareDelay=65+random.nextInt(36);
    }
    // [BLOCK: HABITAT QUERY] NightWildlife.deepForest - Keep scares at least five tiles beyond the village boundary.
    static boolean deepForest(World world,double x,double y){
        if(!world.forest.contains(x,y))return false;
        double left=(World.VILLAGE_MIN+1)*World.T,top=left,right=(World.VILLAGE_MIN+World.VILLAGE_COLS-1)*World.T,bottom=(World.VILLAGE_MIN+World.VILLAGE_ROWS-1)*World.T;
        double dx=Math.max(left-x,Math.max(0,x-right)),dy=Math.max(top-y,Math.max(0,y-bottom));return Math.hypot(dx,dy)>=World.T*5;
    }
    // [BLOCK: BOSS ENCOUNTER EXCLUSION] NightWildlife.nearArena - Avoid adding an ambush to an authored villain encounter.
    private boolean nearArena(double x,double y){
        for(ForestRegion.Arena arena:world.forest.arenas()){
            double dx=(x/World.T-arena.c()-.5)/(arena.rx()+3),dy=(y/World.T-arena.r()-.5)/(arena.ry()+3);
            if(dx*dx+dy*dy<1)return true;
        }
        return false;
    }
    // [BLOCK: DENSE WOODLAND QUERY] NightWildlife.dense - Require multiple nearby tree crowns before a Sigbin encounter.
    private boolean dense(double x,double y){int count=0;for(World.Tree tree:world.trees)if(Math.abs(tree.x-x)<180&&Math.abs(tree.y-y)<180&&++count>=3)return true;return false;}
    // [BLOCK: COLLISION-SAFE SPAWN] NightWildlife.free - Validate the shared navigation node, actual feet, habitat and separation from existing enemies.
    private boolean free(int c,int r,List<Enemy> enemies){
        double x=(c+.5)*World.T,y=(r+.5)*World.T;
        if(!world.walkable(c,r)||!deepForest(world,x,y)||nearArena(x,y)||world.blocked(new Rectangle2D.Double(x-10,y-10,20,10)))return false;
        for(Enemy enemy:enemies)if(enemy.alive()&&Math.hypot(enemy.x-x,enemy.y-y)<64)return false;return true;
    }
    // [BLOCK: BOUNDED SPAWN SEARCH] NightWildlife.wolfPosition - Find an offscreen forest location near exploration, with existing camp clearings as a fallback.
    private Point wolfPosition(Player player,List<Enemy> enemies){
        for(int attempt=0;attempt<64;attempt++){
            double angle=random.nextDouble()*Math.PI*2,radius=(world.forest.contains(player.x,player.y)?400:1050)+random.nextDouble()*400;
            int c=World.col(player.x+Math.cos(angle)*radius),r=World.row(player.y+Math.sin(angle)*radius);
            if(free(c,r,enemies))return new Point(c,r);
        }
        for(int i=0;i<4;i++){Point camp=world.forest.camp((i+wolvesSpawned)%4);
            for(int dy=-2;dy<=2;dy++)for(int dx=-2;dx<=2;dx++)if(free(camp.x+dx,camp.y+dy,enemies)&&Math.hypot((camp.x+dx+.5)*World.T-player.x,(camp.y+dy+.5)*World.T-player.y)>380)return new Point(camp.x+dx,camp.y+dy);}
        return null;
    }
    // [BLOCK: BOUNDED AMBUSH SEARCH] NightWildlife.ambushPosition - Choose free woodland on the edge of the camera instead of placing a creature on Agyu.
    private Point ambushPosition(Player player,List<Enemy> enemies){
        for(int attempt=0;attempt<48;attempt++){
            double angle=random.nextDouble()*Math.PI*2,radius=140+random.nextDouble()*50;
            int c=World.col(player.x+Math.cos(angle)*radius),r=World.row(player.y+Math.sin(angle)*radius);
            double x=(c+.5)*World.T,y=(r+.5)*World.T;
            if(free(c,r,enemies)&&Math.hypot(player.x-x,player.y-y)>110&&dense(x,y))return new Point(c,r);
        }
        return null;
    }
}
