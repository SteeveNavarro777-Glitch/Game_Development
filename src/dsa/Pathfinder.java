/* FILE PURPOSE: DSA GRAPH SEARCH | A*, priority queue, predecessor stack and BFS queue.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Four-neighbor graph algorithms on a cached, collision-aware grid. */
public final class Pathfinder {
    // [SECTION: DSA - ARRAYS / IMPLICIT GRAPH] Paired offsets define four adjacent grid vertices.
    private static final int[] DC={0,1,0,-1}, DR={-1,0,1,0};
    private record Node(int c,int r,int cost,int heuristic,long order) {
        // [BLOCK: DSA: PRIORITY QUEUE] Pathfinder.priority - Return the accumulated route cost plus Manhattan estimate (g + h).
        int priority(){return cost+heuristic;}
    }
    // [BLOCK: FUNCTION] Pathfinder.Pathfinder - Prevent instances of this static helper class.
    private Pathfinder(){}
    // [BLOCK: DSA: A* / PRIORITY QUEUE / STACK] Pathfinder.find - Find a shortest route with Manhattan estimates and reconstruct it in travel order.
    public static List<Point> find(World w,int sc,int sr,int gc,int gr) {
        if(!w.walkable(sc,sr)||!w.walkable(gc,gr)||(sc==gc&&sr==gr)) return List.of();
        // [SECTION: DSA - 2D ARRAYS] Store cheapest costs, predecessors and visited cells.
        int[][] best=new int[w.rows][w.cols], previous=new int[w.rows][w.cols];
        boolean[][] closed=new boolean[w.rows][w.cols];
        for(int r=0;r<w.rows;r++){Arrays.fill(best[r],Integer.MAX_VALUE);Arrays.fill(previous[r],-1);}
        // [SECTION: DSA - PRIORITY QUEUE / HEAP] Remove the node with the smallest g + h estimate.
        PriorityQueue<Node> open=new PriorityQueue<>(Comparator.comparingInt(Node::priority)
                .thenComparingInt(Node::heuristic).thenComparingLong(Node::order));
        long order=0;
        best[sr][sc]=0;
        open.add(new Node(sc,sr,0,Math.abs(gc-sc)+Math.abs(gr-sr),order++));
        while(!open.isEmpty()){
            Node n=open.remove();
            if(n.cost!=best[n.r][n.c]||closed[n.r][n.c])continue;
            closed[n.r][n.c]=true;
            if(n.c==gc&&n.r==gr){
                // [SECTION: DSA - STACK / LIFO] push/pop reverse goal-to-start links into travel order.
                ArrayDeque<Point> reverse=new ArrayDeque<>();
                int c=gc,r=gr;
                while(c!=sc||r!=sr){
                    reverse.push(new Point(c,r));
                    int parent=previous[r][c]; c=parent%w.cols;r=parent/w.cols;
                }
                List<Point> path=new ArrayList<>();
                while(!reverse.isEmpty())path.add(reverse.pop());
                return path;
            }
            for(int i=0;i<4;i++){
                int c=n.c+DC[i],r=n.r+DR[i];
                if(!w.walkable(c,r)||closed[r][c]||n.cost+1>=best[r][c])continue;
                best[r][c]=n.cost+1;previous[r][c]=n.r*w.cols+n.c;
                open.add(new Node(c,r,n.cost+1,Math.abs(gc-c)+Math.abs(gr-r),order++));
            }
        }
        return List.of();
    }
    // [BLOCK: DSA: BFS / FIFO QUEUE] Pathfinder.distances - Build unit-cost shortest distances from one source for the shared enemy flow field.
    public static int[][] distances(World w,int sc,int sr) {
        int[][] distance=new int[w.rows][w.cols];
        for(int[] r:distance)Arrays.fill(r,-1);
        if(!w.walkable(sc,sr))return distance;
        // [SECTION: DSA - QUEUE / FIFO] addLast enqueues; removeFirst expands the oldest discovered cell.
        ArrayDeque<Point> queue=new ArrayDeque<>();
        distance[sr][sc]=0;queue.addLast(new Point(sc,sr));
        while(!queue.isEmpty()){
            Point p=queue.removeFirst();
            for(int i=0;i<4;i++){
                int c=p.x+DC[i],r=p.y+DR[i];
                if(!w.walkable(c,r)||distance[r][c]>=0)continue;
                distance[r][c]=distance[p.y][p.x]+1;
                queue.addLast(new Point(c,r));
            }
        }
        return distance;
    }
}
