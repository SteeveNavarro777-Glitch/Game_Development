/* FILE PURPOSE: RANDOM FOREST HERDS | Distribute small boar herds across reachable woodland and replenish bounded outdoor slots.
 * Course examples: spatial stratification, BFS reachability, arrays and seeded randomness; docs/CODE_GUIDE.md.
 */
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Twelve distributed herds contain one to four boars each; villains and night wildlife have separate lifecycles. */
final class ForestEncounters {
    static final double RESPAWN_SECONDS=25;
    static final int HERD_COUNT=12;
    record Herd(int id,int c,int r,int first,int count) {}
    private final World world;
    private final NightWildlife wildlife;
    private final Random random;
    private final int[][] reachable;
    private final List<Herd> herds=new ArrayList<>();
    // [SECTION: DSA - PARALLEL ARRAYS] Each bounded boar slot owns one actor, a home tile, its herd and a cooldown.
    private final Enemy[] occupants;
    private final Point[] homes;
    private final int[] herdOf;
    private final double[] cooldowns;
    private boolean populated;
    // [BLOCK: RANDOM JOURNEY] ForestEncounters.ForestEncounters - Give each new journey its own wildlife distribution without changing the terrain.
    ForestEncounters(World world){this(world,new Random().nextLong());}
    // [BLOCK: SEEDED DISTRIBUTION / BFS] ForestEncounters.ForestEncounters - Stratify twelve random herd centers over the map and validate their reachable feet positions.
    ForestEncounters(World world,long seed){
        this.world=world;wildlife=new NightWildlife(world);random=new Random(seed);
        Point camp=world.forest.camp(0);reachable=Pathfinder.distances(world,camp.x,camp.y);
        int[] sizes=new int[HERD_COUNT];int total=0;
        for(int i=0;i<sizes.length;i++)sizes[i]=i<4?i+1:1+random.nextInt(4);
        for(int i=sizes.length-1;i>0;i--){int j=random.nextInt(i+1),value=sizes[i];sizes[i]=sizes[j];sizes[j]=value;}
        for(int size:sizes)total+=size;
        occupants=new Enemy[total];homes=new Point[total];herdOf=new int[total];cooldowns=new double[total];
        // [SECTION: DSA - SPATIAL STRATIFICATION] A 4-by-3 region grid spreads encounters beyond the old fixed camps.
        List<List<Point>> regions=new ArrayList<>();for(int i=0;i<HERD_COUNT;i++)regions.add(new ArrayList<>());
        List<Point> all=new ArrayList<>();
        for(int r=2;r<world.rows-2;r++)for(int c=2;c<world.cols-2;c++)if(free(c,r)){
            Point p=new Point(c,r);all.add(p);regions.get(Math.min(2,r*3/world.rows)*4+Math.min(3,c*4/world.cols)).add(p);
        }
        Collections.shuffle(all,random);int slot=0;
        for(int id=0;id<HERD_COUNT;id++){
            List<Point> region=regions.get(id);Collections.shuffle(region,random);Point center=null;List<Point> positions=null;
            for(int pass=0;pass<2&&center==null;pass++)for(Point candidate:pass==0?region:all){
                boolean separated=true;for(Herd previous:herds)if(Math.hypot(candidate.x-previous.c(),candidate.y-previous.r())<8){separated=false;break;}
                if(!separated)continue;List<Point> nearby=nearby(candidate.x,candidate.y);
                if(nearby.size()>=sizes[id]){center=candidate;positions=nearby;break;}
            }
            if(center==null)throw new IllegalStateException("Unable to place reachable forest herd "+id);
            herds.add(new Herd(id,center.x,center.y,slot,sizes[id]));
            for(int i=0;i<sizes[id];i++,slot++){homes[slot]=positions.get(i);herdOf[slot]=id;}
        }
    }
    // [BLOCK: ENCAPSULATED CAPACITY] ForestEncounters.capacity - Read the bounded total selected by the current journey's one-to-four-member herd sizes.
    int capacity(){return occupants.length;}
    // [BLOCK: IMMUTABLE HERD RECORDS] ForestEncounters.herds - Expose immutable center/count records without exposing writable home arrays.
    List<Herd> herds(){return List.copyOf(herds);}
    // [BLOCK: REACHABLE SPAWN FILTER] ForestEncounters.free - Reject village land, solids, disconnected pockets and the three boss arenas.
    private boolean free(int c,int r){
        if(!world.inside(c,r)||reachable[r][c]<0||!world.walkable(c,r))return false;
        double x=(c+.5)*World.T,y=(r+.5)*World.T;
        if(!NightWildlife.deepForest(world,x,y)||world.blocked(new Rectangle2D.Double(x-10,y-10,20,10)))return false;
        for(ForestRegion.Arena arena:world.forest.arenas()){
            double dx=(c-arena.c())/(double)(arena.rx()+3),dy=(r-arena.r())/(double)(arena.ry()+3);if(dx*dx+dy*dy<1)return false;
        }
        return true;
    }
    // [BLOCK: RANDOM HERD MEMBERS] ForestEncounters.nearby - Shuffle free tiles within two cells of the same center so a herd stays together without overlapping feet.
    private List<Point> nearby(int c,int r){
        List<Point> result=new ArrayList<>();for(int dy=-2;dy<=2;dy++)for(int dx=-2;dx<=2;dx++)if(free(c+dx,r+dy))result.add(new Point(c+dx,r+dy));
        Collections.shuffle(result,random);return result;
    }
    // [BLOCK: POLYMORPHIC POPULATION] ForestEncounters.populate - Add the randomized boars and three concrete villains once for this session.
    void populate(List<Enemy> enemies){
        if(populated)throw new IllegalStateException("Forest population already initialized");populated=true;
        for(int slot=0;slot<occupants.length;slot++){occupants[slot]=spawn(homes[slot],herdOf[slot]);enemies.add(occupants[slot]);}
        for(ForestRegion.Arena arena:world.forest.arenas()){
            ArenaBoss boss=switch(arena.id()){
                case 1->new TankBoss(arena);case 2->new MageBoss(arena);case 3->new FighterBoss(arena);
                default->throw new IllegalArgumentException("Unknown boss arena: "+arena.id());};
            boss.refreshNightPower(world);enemies.add(boss);
        }
    }
    // [BLOCK: BOUNDED RESPAWN] ForestEncounters.update - Replenish empty herd slots after a delay, on randomized nearby free ground outside Agyu's view.
    void update(double dt,Player player,List<Enemy> enemies){
        if(!Double.isFinite(dt)||dt<=0||!populated)return;
        for(int slot=0;slot<occupants.length;slot++){
            if(occupants[slot].alive())continue;cooldowns[slot]=Math.min(RESPAWN_SECONDS,cooldowns[slot]+dt);
            if(cooldowns[slot]<RESPAWN_SECONDS||!player.alive()||!world.forest.contains(player.x,player.y))continue;
            Herd herd=herds.get(herdOf[slot]);double hx=(herd.c()+.5)*World.T,hy=(herd.r()+.5)*World.T;
            if(Math.hypot(player.x-hx,player.y-hy)<380+World.T*3)continue;
            Point selected=null;
            for(Point p:nearby(herd.c(),herd.r())){
                double x=(p.x+.5)*World.T,y=(p.y+.5)*World.T;boolean crowded=false;
                for(Enemy enemy:enemies)if(enemy.alive()&&Math.hypot(enemy.x-x,enemy.y-y)<28){crowded=true;break;}
                if(!crowded&&Math.hypot(player.x-x,player.y-y)>=380){selected=p;break;}
            }
            if(selected==null){cooldowns[slot]=RESPAWN_SECONDS-1;continue;}
            homes[slot]=selected;occupants[slot]=spawn(selected,herd.id());cooldowns[slot]=0;enemies.add(occupants[slot]);
        }
        wildlife.update(dt,player,enemies);
    }
    // [BLOCK: VALIDATED CONCRETE SPAWN] ForestEncounters.spawn - Construct a boar with the herd identity and current outdoor night strength.
    private Enemy spawn(Point point,int herd){
        if(!free(point.x,point.y))throw new IllegalStateException("Forest spawn blocked: "+point);
        Enemy enemy=new WildBoar((point.x+.5)*World.T,(point.y+.5)*World.T,herd);enemy.refreshNightPower(world);return enemy;
    }
}
