/* FILE PURPOSE: NPC PLACEMENT | Authored occupations and reachable patrol stations.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/** Authored village occupations and small connected patrols, rebuilt for each journey. */
final class NpcPopulation {
    // [BLOCK: FUNCTION] NpcPopulation.NpcPopulation - Prevent instances of this static helper class.
    private NpcPopulation(){}
    // [BLOCK: NPC: POPULATION] NpcPopulation.create - Build the authored workers and neighbors at reachable village positions.
    static List<Npc> create(World world){
        List<Npc> result=new ArrayList<>();int[][] distance=Pathfinder.distances(world,World.col(World.SPAWN_X),World.row(World.SPAWN_Y));
        add(result,world,distance,"elder","Village Elder",Npc.Role.ELDER,5,World.villageCell(30),World.villageCell(22),List.of(),
                "Welcome, Agyu. Our homes and gardens shelter a community that cares for the grove.",
                "Restore the shrines and defeat the guardian, Tank, Mage and Fighter before returning home.");
        add(result,world,distance,"smith","Village Smith",Npc.Role.SMITH,2,World.villageCell(24),World.villageCell(22),List.of(),
                "A steady grip beats a hurried swing. Tap your attack to link each spear movement.",
                "Keep your shield facing danger. A dash can carry you out of a guardian's warning.");
        add(result,world,distance,"weaver","Village Weaver",Npc.Role.WEAVER,1,World.villageCell(35),World.villageCell(22),List.of(),
                "I am weaving cloth for the market. These colors are part of our village's everyday life.",
                "Look closely at each strand. Patience makes a strong weave, just as practice makes a strong fighter.");
        add(result,world,distance,"vendor","Produce Vendor",Npc.Role.VENDOR,4,World.villageCell(24),World.villageCell(31),List.of(),
                "Bananas, roots and grain from the gardens! The market keeps our neighbors supplied.",
                "Carry your herbs carefully. Press H when you need to recover health.");
        Point shore=findFishingBank(world);
        add(result,world,distance,"fisher","River Fisher",Npc.Role.FISHER,3,shore.x,shore.y,List.of(),
                "The river runs through our village. Please use the bridges and keep its banks clear.",
                "I wait for the line to settle. Listen to the water while I work.");
        add(result,world,distance,"farmer","Garden Tender",Npc.Role.FARMER,0,World.villageCell(6),World.villageCell(11),List.of(),
                "Our garden grows food for the village. Please leave room for the seedlings.");
        walker(result,world,distance,"neighbor-west","Village Neighbor",0,new Point(World.villageCell(14),World.villageCell(16)),new Point(World.villageCell(14),World.villageCell(19)),
                "The paths link our homes, gardens and river crossings.");
        walker(result,world,distance,"neighbor-east","Village Neighbor",4,new Point(World.villageCell(52),World.villageCell(14)),new Point(World.villageCell(52),World.villageCell(17)),
                "Welcome! The market lies near the river. You can see every crossing on your map.");
        walker(result,world,distance,"field-worker","Field Worker",0,new Point(World.villageCell(6),World.villageCell(11)),new Point(World.villageCell(6),World.villageCell(13)),
                "A good harvest is shared work. The village wakes early.");
        walker(result,world,distance,"market-helper","Market Helper",4,new Point(World.villageCell(18),World.villageCell(24)),new Point(World.villageCell(21),World.villageCell(24)),
                "I am carrying supplies between the homes and market.");
        walker(result,world,distance,"north-neighbor","Village Neighbor",0,new Point(World.villageCell(32),World.villageCell(11)),new Point(World.villageCell(35),World.villageCell(13)),
                "The gathering house is just along this lane.");
        walker(result,world,distance,"south-neighbor","Village Neighbor",4,new Point(World.villageCell(43),World.villageCell(44)),new Point(World.villageCell(48),World.villageCell(44)),
                "The bridges lead back to the main square.");
        return result;
    }
    // [BLOCK: FUNCTION] NpcPopulation.findFishingBank - Handle find fishing bank for NpcPopulation.
    private static Point findFishingBank(World world){
        for(int r=World.villageCell(35);r<=World.villageCell(38);r++)for(int c=World.villageCell(5);c<World.villageCell(20);c++)
            if(world.tile[r][c]==World.WATER&&world.tile[r][c-1]!=World.WATER)return new Point(c-1,r);
        return new Point(World.villageCell(18),World.villageCell(32));
    }
    // [BLOCK: FUNCTION] NpcPopulation.walker - Handle walker for NpcPopulation.
    private static void walker(List<Npc> out,World w,int[][] distance,String id,String name,int art,Point a,Point b,String line){
        Point start=nearest(w,distance,a.x,a.y),end=nearest(w,distance,b.x,b.y);
        add(out,w,distance,id,name,Npc.Role.VILLAGER,art,start.x,start.y,List.of(start,end),line);
    }
    // [BLOCK: FUNCTION] NpcPopulation.add - Handle add for NpcPopulation.
    private static void add(List<Npc> out,World w,int[][] distance,String id,String name,Npc.Role role,int art,int c,int r,List<Point> route,String...lines){
        Point p=nearest(w,distance,c,r);
        out.add(new Npc(id,name,role,(p.x+.5)*World.T,(p.y+.5)*World.T,RpgArt.npc(art),route,lines));
    }
    // [BLOCK: FUNCTION] NpcPopulation.nearest - Handle nearest for NpcPopulation.
    private static Point nearest(World w,int[][] distance,int c,int r){
        Point best=null;double score=Double.MAX_VALUE;
        for(int radius=0;radius<=5;radius++)for(int y=r-radius;y<=r+radius;y++)for(int x=c-radius;x<=c+radius;x++){
            if(!w.inside(x,y)||!w.walkable(x,y)||distance[y][x]<0)continue;
            double d=Math.hypot(x-c,y-r);
            if(d<score&&!w.blocked(new Rectangle2D.Double((x+.5)*World.T-4,(y+.5)*World.T-5,8,5))){best=new Point(x,y);score=d;}
        }
        if(best==null)throw new IllegalStateException("NPC has no reachable village position: "+c+","+r);
        return best;
    }
}
