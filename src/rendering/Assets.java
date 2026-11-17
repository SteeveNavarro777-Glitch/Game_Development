/* FILE PURPOSE: ASSET ARRAYS | Current Agyu animation arrays and shared character asset startup.
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
        WildlifeArt.load();
        VillageArt.load();
        BossArt.load();
        PotionArt.load();
        HeldItemArt.load();
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

}
