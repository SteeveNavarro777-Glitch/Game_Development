/* FILE PURPOSE: ASSET ARRAYS | Current Agyu animation arrays and generated enemy images.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Everything is stored at ART resolution (1 image pixel = 1 art pixel).
 * The game draws into a small 480x360 buffer and scales it x2, so every pixel on screen is the same size.
 */
public class Assets {
    public static final int TILE = 32;                 // tile size in art pixels

    public static BufferedImage[][] idle, walk, run, hurt;   // [DOWN, UP, LEFT, RIGHT][frame]
    public static BufferedImage[] die;
    public static BufferedImage front;

    public static BufferedImage[] grass = new BufferedImage[8];
    public static BufferedImage[] slime = new BufferedImage[2], slimeFlash = new BufferedImage[2];

    // [BLOCK: ASSET STARTUP] Assets.load - Load current animation frames, combat bodies, shield poses and village images.
    public static void load() throws IOException {
        // Direction indices match Player.Dir: DOWN, UP, LEFT, RIGHT.
        idle = directional(frames("idle", 8), frames("front", 8), frames("back", 8));
        walk = directional(frames("walk", 8), idle[0], idle[1]);
        run = directional(frames("run", 8), idle[0], idle[1]);
        SpearAttack.loadWeapon(idle[3][0]);
        // Current combat bodies and spear are separate and share authored grip anchors.
        CombatBody.load();
        ShieldGuard.load(new BufferedImage[]{
                load("guard_down"), load("guard_up"), load("guard_left"), load("guard_right")});
        hurt = directional(
                new BufferedImage[]{load("reaction_6"), load("reaction_7")},
                new BufferedImage[]{load("reaction_4"), load("reaction_5")},
                new BufferedImage[]{load("back_reaction_0"), load("back_reaction_1")});
        BufferedImage death = load("die");
        die = new BufferedImage[]{death, death, flip(death), death};
        front = idle[0][0];
        buildEnemyArt();
        VillageArt.load();
        BossArt.load();
    }

    // [BLOCK: FUNCTION] Assets.frames - Handle frames for Assets.
    private static BufferedImage[] frames(String name, int count) throws IOException {
        BufferedImage[] result = new BufferedImage[count];
        for (int i = 0; i < count; i++) result[i] = load(name + "_" + i);
        return result;
    }

    // [BLOCK: DSA: 2D ARRAY / MIRRORING] Assets.directional - Assemble DOWN, UP, LEFT and RIGHT animation arrays.
    private static BufferedImage[][] directional(BufferedImage[] right, BufferedImage[] down, BufferedImage[] up) {
        BufferedImage[] left = new BufferedImage[right.length];
        for (int i = 0; i < right.length; i++) left[i] = flip(right[i]);
        return new BufferedImage[][]{down, up, left, right};
    }

    // [BLOCK: ASSET STARTUP] Assets.load - Load current animation frames, combat bodies, shield poses and village images.
    private static BufferedImage load(String name) throws IOException {
        String path = "res/player/" + name + ".png";
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

    // [BLOCK: ASSET PREPARATION] Assets.flip - Mirror exact RGBA pixels without changing semi-transparent edge colors.
    public static BufferedImage flip(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        // Copy RGBA directly so mirroring cannot round semi-transparent edge colors.
        for (int y = 0; y < src.getHeight(); y++)
            for (int x = 0; x < src.getWidth(); x++)
                out.setRGB(src.getWidth() - 1 - x, y, src.getRGB(x, y));
        return out;
    }

    // ---------- Current enemy sprite generation ----------
    // [BLOCK: ASSET PREPARATION] Assets.art - Allocate one transparent image at art resolution.
    private static BufferedImage art(int w, int h) { return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB); }
    // [BLOCK: ASSET PREPARATION] Assets.put - Write an in-bounds opaque pixel to a generated enemy image.
    private static void put(BufferedImage im, int x, int y, int rgb) {
        if (x >= 0 && y >= 0 && x < im.getWidth() && y < im.getHeight()) im.setRGB(x, y, 0xFF000000 | rgb);
    }

    // [BLOCK: CURRENT ENEMY ART] Assets.buildEnemyArt - Generate the two slime frames and their damage-flash variants.
    private static void buildEnemyArt() {
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


    // [BLOCK: ASSET PREPARATION] Assets.outline - Outline the generated enemy silhouette without changing its solid pixels.
    private static void outline(BufferedImage im, int rgb) {
        int w = im.getWidth(), h = im.getHeight();
        boolean[][] solid = new boolean[h][w];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) solid[y][x] = (im.getRGB(x, y) >>> 24) != 0;
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) if (!solid[y][x]) {
            boolean n = (y > 0 && solid[y - 1][x]) || (y < h - 1 && solid[y + 1][x]) || (x > 0 && solid[y][x - 1]) || (x < w - 1 && solid[y][x + 1]);
            if (n) put(im, x, y, rgb);
        }
    }

    // [BLOCK: ASSET PREPARATION] Assets.tintWhite - Cache a white damage-flash silhouette for an enemy frame.
    private static BufferedImage tintWhite(BufferedImage src) {
        BufferedImage o = art(src.getWidth(), src.getHeight());
        for (int y = 0; y < o.getHeight(); y++) for (int x = 0; x < o.getWidth(); x++)
            if ((src.getRGB(x, y) >>> 24) != 0) o.setRGB(x, y, 0xFFFFFFFF);
        return o;
    }
}
