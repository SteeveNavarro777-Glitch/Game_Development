/* FILE PURPOSE: FOREST ENCOUNTERS | Outdoor shrine guards and a bounded, delayed ambient spawn population.
 * Course examples: arrays, composition and polymorphic enemy lists; docs/CODE_GUIDE.md.
 */
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.List;

/** Mission guards stay defeated; only the eight ambient forest enemies can replenish. */
final class ForestEncounters {
    static final double RESPAWN_SECONDS=25;
    private final World world;
    // [SECTION: DSA - PARALLEL ARRAYS] Each fixed outdoor spawn slot owns one enemy and its cooldown.
    private final Enemy[] occupants=new Enemy[8];
    private final double[] cooldowns=new double[8];
    // [BLOCK: COMPOSITION] ForestEncounters.ForestEncounters - Bind the encounter population to one world/session.
    ForestEncounters(World world){this.world=world;}
    // [BLOCK: SPAWN POPULATION] ForestEncounters.populate - Place shrine guards and eight ambient enemies entirely outside the walls.
    void populate(List<Enemy> enemies){
        int[][] offsets={{-2,0},{2,0},{0,-2},{0,2}};
        for(int group=0;group<3;group++){
            int[] site=World.LANDMARKS[group+1];
            for(int i=0;i<(group==2?4:3);i++)enemies.add(spawn(site[0]+offsets[i][0],site[1]+offsets[i][1],group));
        }
        for(int slot=0;slot<occupants.length;slot++){
            Point p=position(slot);occupants[slot]=spawn(p.x,p.y,-1);enemies.add(occupants[slot]);cooldowns[slot]=0;
        }
        // [SECTION: OOP - POLYMORPHIC FACTORY] Three authored boss types share the Enemy list but never ambient respawn slots.
        for(ForestRegion.Arena arena:world.forest.arenas()){
            ArenaBoss boss=switch(arena.id()){
                case 1->new TankBoss(arena);case 2->new MageBoss(arena);case 3->new FighterBoss(arena);
                default->throw new IllegalArgumentException("Unknown boss arena: "+arena.id());};
            boss.refreshNightPower(world);enemies.add(boss);
        }
    }
    // [BLOCK: BOUNDED SPAWN TIMER] ForestEncounters.update - Replenish defeated ambient slots only offscreen while Agyu explores the forest.
    void update(double dt,Player player,List<Enemy> enemies){
        for(int slot=0;slot<occupants.length;slot++){
            if(occupants[slot].alive())continue;
            cooldowns[slot]+=Math.max(0,dt);
            if(cooldowns[slot]<RESPAWN_SECONDS||!player.alive()||!world.forest.contains(player.x,player.y))continue;
            Point p=position(slot);double x=(p.x+.5)*World.T,y=(p.y+.5)*World.T;
            if(Math.hypot(player.x-x,player.y-y)<380)continue;
            boolean crowded=false;
            for(Enemy enemy:enemies)if(enemy.alive()&&Math.hypot(enemy.x-x,enemy.y-y)<48){crowded=true;break;}
            if(crowded||world.blocked(new Rectangle2D.Double(x-10,y-10,20,10)))continue;
            occupants[slot]=spawn(p.x,p.y,-1);cooldowns[slot]=0;enemies.add(occupants[slot]);
        }
    }
    // [BLOCK: CACHED SPAWN LOCATION] ForestEncounters.position - Separate two enemies inside each of four authored camp clearings.
    private Point position(int slot){Point p=world.forest.camp(slot/2);p.y+=slot%2==0?-1:1;return p;}
    // [BLOCK: VALIDATED SPAWN] ForestEncounters.spawn - Reject embedded or indoor spawns before constructing the concrete enemy.
    private Enemy spawn(int c,int r,int group){
        double x=(c+.5)*World.T,y=(r+.5)*World.T;
        if(!world.forest.contains(x,y)||!world.walkable(c,r)||world.blocked(new Rectangle2D.Double(x-10,y-10,20,10)))
            throw new IllegalStateException("Forest spawn blocked: "+c+","+r);
        Enemy enemy=new Slime(x,y,group);enemy.refreshNightPower(world);return enemy;
    }
}
