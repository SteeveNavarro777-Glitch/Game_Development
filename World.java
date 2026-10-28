import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/** The tile map (all units are art pixels). */
public class World {
    static final int T = Assets.TILE;
    static final int GRASS = 0, DIRT = 1, WATER = 2, ROCK = 3;

    final int cols = 64, rows = 48;
    final int[][] tile = new int[rows][cols];
    final int[][] variant = new int[rows][cols];
    final List<Tree> trees = new ArrayList<>();
    static final double SPAWN_X = 32 * T, SPAWN_Y = 27 * T;

    World(long seed) {
        Random r = new Random(seed);
        for (int y = 0; y < rows; y++) for (int x = 0; x < cols; x++) {
            int n = r.nextInt(40);
            variant[y][x] = n < 24 ? n % 4 : n < 33 ? 4 + n % 2 : n < 37 ? 6 : n < 38 ? 7 : n % 4;
        }
        pond(16, 36, 7, 5); pond(48, 10, 6, 4);
        for (int x = 2; x < cols - 2; x++) {                 // winding dirt path
            int cy = 24 + (int) Math.round(3 * Math.sin(x * 0.2));
            for (int y = cy; y <= cy + 1; y++) if (tile[y][x] == GRASS) tile[y][x] = DIRT;
        }
        for (int y = 0; y < rows; y++) for (int x = 0; x < cols; x++)
            if (x < 2 || y < 2 || x >= cols - 2 || y >= rows - 2) tile[y][x] = ROCK;

        int tries = 0;
        while (trees.size() < 85 && tries++ < 4000) {
            int c = 3 + r.nextInt(cols - 6), rw = 4 + r.nextInt(rows - 6);
            if (tile[rw][c] != GRASS) continue;
            double tx = c * T + T / 2.0, ty = rw * T + T;
            if (Math.hypot(tx - SPAWN_X, ty - SPAWN_Y) < 110) continue;
            boolean ok = true;
            for (Tree o : trees) if (Math.hypot(o.x - tx, o.y - ty) < 56) { ok = false; break; }
            if (ok) trees.add(new Tree(tx, ty, r.nextDouble() < 0.3));
        }
    }

    private void pond(double cx, double cy, double rx, double ry) {
        for (int y = 0; y < rows; y++) for (int x = 0; x < cols; x++) {
            double d = Math.pow((x - cx) / rx, 2) + Math.pow((y - cy) / ry, 2);
            if (d < 1) tile[y][x] = WATER; else if (d < 1.4 && tile[y][x] == GRASS) tile[y][x] = DIRT;
        }
    }

    private int at(int c, int r) { return (c < 0 || r < 0 || c >= cols || r >= rows) ? ROCK : tile[r][c]; }

    boolean solidTile(int c, int r) { int t = at(c, r); return t == WATER || t == ROCK; }

    boolean blocked(Rectangle2D box) {
        int c0 = (int) Math.floor(box.getMinX() / T), c1 = (int) Math.floor(box.getMaxX() / T);
        int r0 = (int) Math.floor(box.getMinY() / T), r1 = (int) Math.floor(box.getMaxY() / T);
        for (int r = r0; r <= r1; r++) for (int c = c0; c <= c1; c++) if (solidTile(c, r)) return true;
        for (Tree t : trees) if (t.trunk().intersects(box)) return true;
        return false;
    }

    void drawGround(Graphics2D g, int camX, int camY, int vw, int vh, double time) {
        int c0 = Math.max(0, Math.floorDiv(camX, T)), c1 = Math.min(cols - 1, Math.floorDiv(camX + vw, T));
        int r0 = Math.max(0, Math.floorDiv(camY, T)), r1 = Math.min(rows - 1, Math.floorDiv(camY + vh, T));
        int wf = ((int) (time * 2)) % 2;
        for (int r = r0; r <= r1; r++) for (int c = c0; c <= c1; c++) {
            int v = variant[r][c], t = tile[r][c];
            BufferedImage img = t == DIRT ? Assets.dirt[v % 3] : t == WATER ? Assets.water[wf] : t == ROCK ? Assets.rock : Assets.grass[v];
            int x = c * T - camX, y = r * T - camY;
            g.drawImage(img, x, y, null);
            if (t == DIRT) {                                   // soft lip where the path meets grass
                edges(g, x, y, c, r, GRASS, 0x8E6240, 0xD2A877);
            } else if (t == WATER) {                           // foam line where the pond meets land
                edges(g, x, y, c, r, -1, 0x3A6CB8, 0xBFE0FF);
            }
        }
    }

    /** Draws a 1px dark line + 1px highlight on each side whose neighbour is 'other' (or, for -1, "not water"). */
    private void edges(Graphics2D g, int x, int y, int c, int r, int other, int dark, int light) {
        int[][] d = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] n : d) {
            int nt = at(c + n[0], r + n[1]);
            boolean hit = other == -1 ? nt != WATER : nt == other;
            if (!hit) continue;
            int sz = T;
            if (n[1] == -1) { g.setColor(new Color(dark)); g.fillRect(x, y, sz, 1); g.setColor(new Color(light)); g.fillRect(x, y + 1, sz, 1); }
            if (n[1] == 1)  { g.setColor(new Color(dark)); g.fillRect(x, y + sz - 1, sz, 1); g.setColor(new Color(light)); g.fillRect(x, y + sz - 2, sz, 1); }
            if (n[0] == -1) { g.setColor(new Color(dark)); g.fillRect(x, y, 1, sz); g.setColor(new Color(light)); g.fillRect(x + 1, y, 1, sz); }
            if (n[0] == 1)  { g.setColor(new Color(dark)); g.fillRect(x + sz - 1, y, 1, sz); g.setColor(new Color(light)); g.fillRect(x + sz - 2, y, 1, sz); }
        }
    }

    /** Trees are depth-sorted with the characters so Agyu can walk behind them. */
    class Tree implements Drawable {
        final double x, y; final boolean pink; final double phase;
        Tree(double x, double y, boolean pink) { this.x = x; this.y = y; this.pink = pink; phase = x * 0.13; }
        Rectangle2D.Double trunk() { return new Rectangle2D.Double(x - 6, y - 8, 12, 8); }
        public double sortY() { return y; }
        public void draw(Graphics2D g, int camX, int camY, double alpha) {
            BufferedImage img = pink ? Assets.pinkTree : Assets.greenTree;
            g.setColor(new Color(0, 0, 0, 55));
            g.fillOval((int) x - camX - 15, (int) y - camY - 5, 30, 9);
            g.drawImage(img, (int) x - camX - 24, (int) y - camY - 55, null);
        }
    }
}
