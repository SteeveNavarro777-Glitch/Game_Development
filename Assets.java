import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Everything is stored at ART resolution (1 image pixel = 1 art pixel).
 * The game draws into a small 480x360 buffer and scales it x2, so every pixel on screen is the same size.
 */
public class Assets {
    public static final int TILE = 32;                 // tile size in art pixels

    public static BufferedImage[][] idle, walk, run, attack, hurt;   // [DOWN, UP, LEFT, RIGHT][frame]
    public static BufferedImage[] die;
    public static BufferedImage front, back;

    public static BufferedImage[] grass = new BufferedImage[8], dirt = new BufferedImage[3], water = new BufferedImage[2];
    public static BufferedImage rock, pinkTree, greenTree;
    public static BufferedImage[] slime = new BufferedImage[2], slimeFlash = new BufferedImage[2];

    public static void load() throws IOException {
        // Direction indices match Player.Dir: DOWN, UP, LEFT, RIGHT.
        idle = directional(frames("idle", 8), frames("front", 8), frames("back", 8));
        walk = directional(frames("walk", 8), idle[0], idle[1]);
        run = directional(frames("run", 8), idle[0], idle[1]);
        SpearAttack.loadWeapon(idle[3][0]);
        // Bodies and spear are separate so the weapon can really swing in all directions.
        attack = directional(SpearAttack.bodies(idle[3]),
                SpearAttack.bodies(idle[0]), SpearAttack.bodies(idle[1]));
        CombatBody.load();
        ShieldGuard.load(new BufferedImage[]{
                load("guard_down"), load("guard_up"), load("guard_left"), load("guard_right")});
        hurt = directional(
                new BufferedImage[]{load("reaction_6"), load("reaction_7")},
                new BufferedImage[]{load("reaction_4"), load("reaction_5")},
                new BufferedImage[]{load("back_reaction_0"), load("back_reaction_1")});
        BufferedImage death = load("die");
        die = new BufferedImage[]{death, death, flip(death), death};
        front = idle[0][0]; back = idle[1][0];
        buildWorldArt();
        VillageArt.load();
    }

    private static BufferedImage[] frames(String name, int count) throws IOException {
        BufferedImage[] result = new BufferedImage[count];
        for (int i = 0; i < count; i++) result[i] = load(name + "_" + i);
        return result;
    }

    private static BufferedImage[][] directional(BufferedImage[] right, BufferedImage[] down, BufferedImage[] up) {
        BufferedImage[] left = new BufferedImage[right.length];
        for (int i = 0; i < right.length; i++) left[i] = flip(right[i]);
        return new BufferedImage[][]{down, up, left, right};
    }

    private static BufferedImage load(String name) throws IOException {
        String path = "res/agyu-v2/" + name + ".png";
        BufferedImage image;
        try (java.io.InputStream stream = Assets.class.getResourceAsStream("/" + path)) {
            if (stream != null) image = ImageIO.read(stream);
            else {
                File file = new File(path);
                if (!file.isFile()) throw new IOException("Missing " + path + ". Start with run.bat or include the project root on the classpath.");
                image = ImageIO.read(file);
            }
        }
        if (image == null) throw new IOException("Unreadable PNG: " + path);
        return image;
    }

    public static BufferedImage flip(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        // Copy RGBA directly so mirroring cannot round semi-transparent edge colors.
        for (int y = 0; y < src.getHeight(); y++)
            for (int x = 0; x < src.getWidth(); x++)
                out.setRGB(src.getWidth() - 1 - x, y, src.getRGB(x, y));
        return out;
    }

    // ---------- procedural world art ----------
    private static BufferedImage art(int w, int h) { return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB); }
    private static void put(BufferedImage im, int x, int y, int rgb) {
        if (x >= 0 && y >= 0 && x < im.getWidth() && y < im.getHeight()) im.setRGB(x, y, 0xFF000000 | rgb);
    }
    private static void fill(BufferedImage im, int rgb) {
        for (int y = 0; y < im.getHeight(); y++) for (int x = 0; x < im.getWidth(); x++) put(im, x, y, rgb);
    }

    private static void buildWorldArt() {
        Random r = new Random(42);
        for (int v = 0; v < 8; v++) {                         // grass variants: 0-3 plain, 4-5 tufts, 6 white flower, 7 yellow flower
            BufferedImage t = art(32, 32); fill(t, 0x6DB38B);
            for (int i = 0; i < 16; i++) put(t, r.nextInt(32), r.nextInt(32), 0x5FA37D);
            for (int i = 0; i < 7; i++)  put(t, r.nextInt(32), r.nextInt(32), 0x7CC49B);
            if (v == 4 || v == 5) for (int k = 0; k < 2; k++) {
                int x = 5 + r.nextInt(22), y = 7 + r.nextInt(20);
                put(t, x, y, 0x4F9170); put(t, x, y - 1, 0x8FD4A8); put(t, x - 1, y - 2, 0x8FD4A8); put(t, x + 1, y - 2, 0x8FD4A8); put(t, x, y - 2, 0x7CC49B);
            }
            if (v >= 6) { int x = 8 + r.nextInt(16), y = 8 + r.nextInt(16); int pet = v == 6 ? 0xFFFFFF : 0xFFE27A;
                put(t, x, y, 0xF2A33A); put(t, x - 1, y, pet); put(t, x + 1, y, pet); put(t, x, y - 1, pet); put(t, x, y + 1, pet); put(t, x, y + 2, 0x4F9170); }
            grass[v] = t;
        }
        for (int v = 0; v < 3; v++) {
            BufferedImage t = art(32, 32); fill(t, 0xB98B5E);
            for (int i = 0; i < 18; i++) put(t, r.nextInt(32), r.nextInt(32), 0xA27549);
            for (int i = 0; i < 8; i++)  put(t, r.nextInt(32), r.nextInt(32), 0xCDA270);
            for (int i = 0; i < 2; i++) { int x = 2 + r.nextInt(27), y = 2 + r.nextInt(27);
                put(t, x, y, 0x8E6240); put(t, x + 1, y, 0x8E6240); put(t, x, y - 1, 0xD9B07E); put(t, x + 1, y - 1, 0xD9B07E); }
            dirt[v] = t;
        }
        for (int f = 0; f < 2; f++) {
            BufferedImage t = art(32, 32); fill(t, 0x4C87D8);
            for (int i = 0; i < 12; i++) put(t, (i * 7 + 3) % 32, (i * 11 + 5) % 32, 0x4579C6);
            int[][] sp = {{3, 5}, {17, 9}, {8, 18}, {22, 24}, {26, 13}};
            for (int[] p : sp) { int x = (p[0] + f * 3) % 28; for (int k = 0; k < 4; k++) put(t, x + k, p[1], 0x8FBDF5); put(t, x + 1, p[1] + 1, 0x6FA3EB); }
            water[f] = t;
        }
        BufferedImage rk = art(32, 32); fill(rk, 0x7C5A3F);    // rocky border: cobbles
        for (int by = 0; by < 32; by += 8) for (int bx = 0; bx < 32; bx += 8) {
            int ox = (by / 8 % 2) * 4;
            for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) {
                int px = (bx + x + ox) % 32, py = by + y;
                int c = (x == 0 || y == 0) ? 0x5A3E29 : (x == 1 || y == 1) ? 0xA77D58 : (x == 7 || y == 7) ? 0x6A4A32 : 0x8A6445;
                put(rk, px, py, c);
            }
        }
        rock = rk;

        pinkTree  = tree(0xA3BB6C, 0x78994E, 0x536F3D, 0x374D2B, 0xC1D18C);
        greenTree = tree(0x89AC65, 0x628B50, 0x446C40, 0x2C4C32, 0xADCA85);

        for (int f = 0; f < 2; f++) {                           // slime 20x16, two squash frames
            BufferedImage s = art(20, 16);
            double rx = f == 0 ? 8.2 : 9.0, ry = f == 0 ? 6.4 : 5.2, cy = f == 0 ? 9 : 10;
            for (int y = 0; y < 16; y++) for (int x = 0; x < 20; x++) {
                double dx = (x - 9.5) / rx, dy = (y - cy) / ry;
                if (dx * dx + dy * dy <= 1) put(s, x, y, y > cy + 1.5 ? 0x93314A : y > cy - 1 ? 0xB8455F : 0xC95670);
            }
            outline(s, 0x5A1B2B);
            put(s, 6, (int) cy - 4, 0xF0A3B5); put(s, 7, (int) cy - 4, 0xF0A3B5); put(s, 6, (int) cy - 3, 0xF0A3B5);
            for (int ex : new int[]{6, 12}) { put(s, ex, (int) cy - 1, 0x2B1020); put(s, ex + 1, (int) cy - 1, 0x2B1020);
                put(s, ex, (int) cy, 0x2B1020); put(s, ex + 1, (int) cy, 0x2B1020); put(s, ex, (int) cy - 1, 0xFFFFFF); }
            slime[f] = s; slimeFlash[f] = tintWhite(s);
        }
    }

    /** 48x56 tree: canopy blobs shaded by height, leaf highlights, trunk, dark outline. */
    private static BufferedImage tree(int light, int base, int dark, int line, int sparkle) {
        BufferedImage t = art(48, 56); Random r = new Random(light);
        boolean[][] m = new boolean[56][48];
        double[][] blobs = {{24, 20, 18}, {13, 27, 12}, {35, 27, 12}, {24, 31, 14}};
        for (int y = 0; y < 56; y++) for (int x = 0; x < 48; x++) for (double[] b : blobs) if (Math.hypot(x - b[0], y - b[1]) <= b[2]) m[y][x] = true;
        for (int y = 40; y < 56; y++) for (int x = 20; x < 28; x++) {
            boolean flare = y > 51; if (x >= 21 && x <= 26 || flare) put(t, x, y, x >= 25 ? 0x5B3920 : x <= 21 ? 0x8C5C37 : 0x7A4E2D);
        }
        for (int y = 0; y < 56; y++) for (int x = 0; x < 48; x++) if (m[y][x]) put(t, x, y, y < 14 ? light : y < 28 ? base : dark);
        for (int i = 0; i < 26; i++) { int x = 8 + r.nextInt(32), y = 4 + r.nextInt(30); if (m[y][x]) { put(t, x, y, sparkle); put(t, x + 1, y, sparkle); } }
        for (int i = 0; i < 18; i++) { int x = 6 + r.nextInt(36), y = 24 + r.nextInt(16); if (m[y][x]) { put(t, x, y, dark); put(t, x + 1, y + 1, dark); } }
        for (int y = 0; y < 56; y++) for (int x = 0; x < 48; x++) if (m[y][x]) {
            boolean edge = y == 0 || x == 0 || x == 47 || y == 55 || !m[y - 1][x] || !m[y + 1][x] || !m[y][x - 1] || !m[y][x + 1];
            if (edge) put(t, x, y, line);
        }
        return t;
    }

    private static void outline(BufferedImage im, int rgb) {
        int w = im.getWidth(), h = im.getHeight();
        boolean[][] solid = new boolean[h][w];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) solid[y][x] = (im.getRGB(x, y) >>> 24) != 0;
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) if (!solid[y][x]) {
            boolean n = (y > 0 && solid[y - 1][x]) || (y < h - 1 && solid[y + 1][x]) || (x > 0 && solid[y][x - 1]) || (x < w - 1 && solid[y][x + 1]);
            if (n) put(im, x, y, rgb);
        }
    }

    private static BufferedImage tintWhite(BufferedImage src) {
        BufferedImage o = art(src.getWidth(), src.getHeight());
        for (int y = 0; y < o.getHeight(); y++) for (int x = 0; x < o.getWidth(); x++)
            if ((src.getRGB(x, y) >>> 24) != 0) o.setRGB(x, y, 0xFFFFFFFF);
        return o;
    }
}
