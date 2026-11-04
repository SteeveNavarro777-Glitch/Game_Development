import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Static tile arrays and a uniform-grid index of exact tree colliders. */
public final class World {
    static final int T = Assets.TILE, GRASS = 0, DIRT = 1, WATER = 2, ROCK = 3;
    static final int[][] LANDMARKS = {{32,27}, {12,12}, {49,26}, {28,39}, {51,39}};
    static final double SPAWN_X = 32.5 * T, SPAWN_Y = 27.5 * T;
    private static final int[][] DIRECTIONS = {{0,-1},{0,1},{-1,0},{1,0}};
    final int cols = 64, rows = 48;
    final int[][] tile = new int[rows][cols], variant = new int[rows][cols];
    final List<Tree> trees = new ArrayList<>();
    private final List<List<Tree>> buckets = new ArrayList<>();
    private final boolean[][] navigation = new boolean[rows][cols];
    long collisionChecks;

    World(long seed) {
        Random random = new Random(seed);
        for (int i = 0; i < rows * cols; i++) buckets.add(new ArrayList<>());
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) variant[r][c] = random.nextInt(8);
        pond(16,36,7,5); pond(48,10,6,4);
        for (int c = 2; c < cols-2; c++) {
            int y = 24 + (int)Math.round(3 * Math.sin(c * 0.2));
            tile[y][c] = tile[y+1][c] = DIRT;
        }
        // Explicit connecting roads make every mission location reachable.
        for (int[] landmark : LANDMARKS) {
            for (int c = Math.min(32,landmark[0]); c <= Math.max(32,landmark[0]); c++)
                clearRoad(c,27);
            for (int r = Math.min(27,landmark[1]); r <= Math.max(27,landmark[1]); r++)
                clearRoad(landmark[0],r);
            for (int r = landmark[1]-3; r <= landmark[1]+3; r++)
                for (int c = landmark[0]-3; c <= landmark[0]+3; c++) tile[r][c] = GRASS;
            tile[landmark[1]][landmark[0]] = DIRT;
        }
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++)
            if (c < 2 || r < 2 || c >= cols-2 || r >= rows-2) tile[r][c] = ROCK;
        int tries = 0;
        while (trees.size() < 85 && tries++ < 4000) {
            int c = 3+random.nextInt(cols-6), r = 4+random.nextInt(rows-6);
            if (tile[r][c] != GRASS) continue;
            double x = (c+0.5)*T, y = (r+1.0)*T;
            boolean okay = true;
            for (int[] landmark : LANDMARKS)
                if (Math.hypot(x-(landmark[0]+0.5)*T,y-(landmark[1]+0.5)*T) < 150) okay = false;
            for (Tree tree : trees) if (Math.hypot(tree.x-x,tree.y-y) < 56) okay = false;
            if (okay) trees.add(new Tree(x,y,random.nextDouble()<0.3));
        }
        for (Tree tree : trees) {
            Rectangle2D box = tree.trunk;
            for (int r = row(box.getMinY()); r <= row(Math.nextDown(box.getMaxY())); r++)
                for (int c = col(box.getMinX()); c <= col(Math.nextDown(box.getMaxX())); c++)
                    if (inside(c,r)) buckets.get(r*cols+c).add(tree);
        }
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++)
            // Reserve the whole cell plus actor clearance, including tree trunks on tile edges.
            navigation[r][c] = !blocked(new Rectangle2D.Double(c*T-10,r*T-10,T+20,T+10));
        collisionChecks = 0;
    }
    private void clearRoad(int c, int r) {
        for (int y = r-1; y <= r+1; y++) for (int x = c-1; x <= c+1; x++) tile[y][x] = DIRT;
    }
    private void pond(double cx,double cy,double rx,double ry) {
        for (int r=0;r<rows;r++) for(int c=0;c<cols;c++) {
            double d = Math.pow((c-cx)/rx,2)+Math.pow((r-cy)/ry,2);
            if(d<1) tile[r][c]=WATER; else if(d<1.4) tile[r][c]=DIRT;
        }
    }
    static int col(double x) { return (int)Math.floor(x/T); }
    static int row(double y) { return (int)Math.floor(y/T); }
    boolean inside(int c,int r) { return c>=0 && r>=0 && c<cols && r<rows; }
    private int at(int c,int r) { return inside(c,r) ? tile[r][c] : ROCK; }
    boolean solidTile(int c,int r) { int t=at(c,r); return t==WATER || t==ROCK; }
    boolean walkable(int c,int r) { return inside(c,r) && navigation[r][c]; }

    boolean blocked(Rectangle2D box) {
        int c0=col(box.getMinX()), c1=col(Math.nextDown(box.getMaxX()));
        int r0=row(box.getMinY()), r1=row(Math.nextDown(box.getMaxY()));
        for(int r=r0;r<=r1;r++) for(int c=c0;c<=c1;c++) {
            if(solidTile(c,r)) return true;
            for(Tree tree:buckets.get(r*cols+c)) {
                collisionChecks++;
                if(tree.trunk.intersects(box)) return true;
            }
        }
        return false;
    }
    // Reference implementation retained for automated index-equivalence tests.
    boolean blockedBruteForce(Rectangle2D box) {
        for(int r=row(box.getMinY());r<=row(Math.nextDown(box.getMaxY()));r++)
            for(int c=col(box.getMinX());c<=col(Math.nextDown(box.getMaxX()));c++)
                if(solidTile(c,r)) return true;
        for(Tree tree:trees) if(tree.trunk.intersects(box)) return true;
        return false;
    }
    void drawGround(Graphics2D g,int cx,int cy,int vw,int vh,double time) {
        int c0=Math.max(0,Math.floorDiv(cx,T)), c1=Math.min(cols-1,Math.floorDiv(cx+vw,T));
        int r0=Math.max(0,Math.floorDiv(cy,T)), r1=Math.min(rows-1,Math.floorDiv(cy+vh,T));
        for(int r=r0;r<=r1;r++) for(int c=c0;c<=c1;c++) {
            int kind=tile[r][c],v=variant[r][c], x=c*T-cx,y=r*T-cy;
            BufferedImage image = kind==DIRT?Assets.dirt[v%3]:kind==WATER?Assets.water[((int)(time*2))%2]:
                    kind==ROCK?Assets.rock:Assets.grass[v];
            g.drawImage(image,x,y,null);
            if(kind==WATER || kind==DIRT) for(int[] direction:DIRECTIONS) {
                int neighbor=at(c+direction[0],r+direction[1]);
                if(kind==WATER?neighbor==WATER:neighbor!=GRASS) continue;
                g.setColor(kind==WATER?new Color(0xBFE0FF):new Color(0xD2A877));
                if(direction[1]<0) g.fillRect(x,y,T,1);
                if(direction[1]>0) g.fillRect(x,y+T-1,T,1);
                if(direction[0]<0) g.fillRect(x,y,1,T);
                if(direction[0]>0) g.fillRect(x+T-1,y,1,T);
            }
        }
    }
    static final class Tree implements Drawable {
        final double x,y;
        private final boolean pink;
        final Rectangle2D.Double trunk;
        Tree(double x,double y,boolean pink) {
            this.x=x;this.y=y;this.pink=pink;
            trunk=new Rectangle2D.Double(x-6,y-8,12,8);
        }
        public double sortY(){return y;}
        public void draw(Graphics2D g,int cx,int cy,double alpha) {
            g.setColor(new Color(0,0,0,55));
            g.fillOval((int)x-cx-15,(int)y-cy-5,30,9);
            g.drawImage(pink?Assets.pinkTree:Assets.greenTree,(int)x-cx-24,(int)y-cy-55,null);
        }
    }
}
