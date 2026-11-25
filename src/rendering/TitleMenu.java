/* FILE PURPOSE: TITLE UI | Navigation and presentation for the supplied Agyu menu artwork. */
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

final class TitleMenu {
    enum Action { NONE, NEW_GAME, CONTINUE, EXIT }
    private enum Page { MAIN, SETTINGS, HOW_TO_PLAY, CREDITS, EXIT_CONFIRM, LOADING }
    private static final int WIDTH = GamePanel.VIEW_W, HEIGHT = GamePanel.VIEW_H;
    static final double LOADING_SECONDS = 1.1;
    private static final Color GOLD = new Color(0xFED183), CREAM = new Color(0xF6E7C9);
    private static final Font LABEL = new Font(Font.MONOSPACED, Font.BOLD, 11);
    private static final Font DETAIL = new Font(Font.MONOSPACED, Font.PLAIN, 10);
    private static final Rectangle INFO = new Rectangle(16, 60, 448, 230);
    private static final Rectangle INFO_BACK = new Rectangle(47, 87, 30, 37);
    private static final Rectangle SETTINGS = new Rectangle(103, 39, 274, 275);
    private static final Rectangle SETTINGS_BACK = new Rectangle(138, 70, 33, 42);
    private static final Rectangle[] TOGGLES = {new Rectangle(202, 131, 76, 38), new Rectangle(202, 206, 76, 38)};
    private static final Rectangle EXIT = new Rectangle(24, 75, 432, 210);
    private static final Rectangle EXIT_BACK = new Rectangle(55, 88, 28, 37);
    private static final Rectangle[] EXIT_OPTIONS = {new Rectangle(125, 215, 94, 25), new Rectangle(262, 215, 94, 25)};
    // [SECTION: DSA - ARRAYS] The button bounds drive both drawing and pointer hit testing.
    private static final Rectangle[] BUTTONS = new Rectangle[6];
    static {
        for (int i = 0; i < BUTTONS.length; i++) BUTTONS[i] = new Rectangle(248, 133 + i * 35, 182, 30);
    }
    private static final String[][] CONTROLS = {
        {"WASD / ARROWS", "Move"}, {"LEFT CLICK / J", "Attack with spear"}, {"RIGHT CLICK", "Guard with shield"},
        {"SHIFT / Q", "Sprint / dash (Q or Ctrl)"}, {"SPACE", "Jump"},
        {"E", "Interact / trade"}, {"I / 1–9", "Inventory / quick slots"}, {"F", "Use selected potion"},
        {"M / TAB", "Map / journal"}, {"ESC / P", "Pause (T for title)"}
    };

    private final Artwork art;
    private final TitleAudio audio = new TitleAudio();
    private Page page = Page.MAIN;
    private int selected, hovered = -1, nextTip, loadingTip;
    private boolean canContinue, musicEnabled = true, soundEnabled = true;
    private double animationTime;

    TitleMenu() throws IOException { art = Artwork.get(); }

    void open(boolean resumable) {
        canContinue = resumable;
        go(Page.MAIN, resumable ? 1 : 0);
        audio.setMusic(musicEnabled);
    }

    void startAudio() { audio.start(musicEnabled); }
    void stopAudio() { audio.close(); }
    void leave() { audio.setMusic(false); }
    boolean canContinue() { return canContinue; }
    boolean musicEnabled() { return musicEnabled; }
    boolean soundEnabled() { return soundEnabled; }

    void beginLoading() {
        loadingTip = nextTip++ % art.tips.length;
        go(Page.LOADING, 0);
    }

    Action update(KeyHandler.Input input, double dt) {
        animationTime += Math.max(0, dt);
        if (page == Page.LOADING) return Action.NONE;
        if (input.pressed(KeyHandler.PAUSE) && page != Page.MAIN) return back();

        boolean up = input.pressed(KeyHandler.MENU_UP), down = input.pressed(KeyHandler.MENU_DOWN);
        boolean across = input.pressed(KeyHandler.MENU_LEFT | KeyHandler.MENU_RIGHT);
        boolean enter = input.pressed(KeyHandler.START);
        switch (page) {
            case MAIN:
                if (up || down) {
                    int direction = up ? -1 : 1;
                    do { selected = Math.floorMod(selected + direction, BUTTONS.length); }
                    while (selected == 1 && !canContinue);
                    hovered = -1;
                }
                return enter ? activateMain(selected) : Action.NONE;
            case SETTINGS:
                if (up || down) { selected = 1 - selected; hovered = -1; }
                if (across || enter) toggle(selected);
                break;
            case EXIT_CONFIRM:
                if (across) { selected = 1 - selected; hovered = -1; }
                if (enter) return confirmExit(selected);
                break;
            case HOW_TO_PLAY, CREDITS:
                if (enter) return back();
                break;
            default:
                break;
        }
        return Action.NONE;
    }

    int hover(int x, int y) {
        hovered = targetAt(x, y);
        return hovered == -1 ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR;
    }

    Action click(int x, int y) {
        int target = targetAt(x, y);
        if (target == -1) return Action.NONE;
        if (target == -2) return back();
        return switch (page) {
            case MAIN -> activateMain(target);
            case SETTINGS -> { selected = target; toggle(target); yield Action.NONE; }
            case EXIT_CONFIRM -> confirmExit(target);
            default -> Action.NONE;
        };
    }

    private int targetAt(int x, int y) {
        Rectangle back = backBounds();
        if (back != null && back.contains(x, y)) return -2;
        Rectangle[] choices = switch (page) {
            case MAIN -> BUTTONS;
            case SETTINGS -> TOGGLES;
            case EXIT_CONFIRM -> EXIT_OPTIONS;
            default -> null;
        };
        if (choices != null) {
            for (int i = 0; i < choices.length; i++) {
                if (page == Page.MAIN && i == 1 && !canContinue) continue;
                if (choices[i].contains(x, y)) return i;
            }
        }
        return -1;
    }

    private Rectangle backBounds() {
        return switch (page) {
            case SETTINGS -> SETTINGS_BACK;
            case HOW_TO_PLAY, CREDITS -> INFO_BACK;
            case EXIT_CONFIRM -> EXIT_BACK;
            default -> null;
        };
    }

    private Action activateMain(int index) {
        if (index == 1 && !canContinue) return Action.NONE;
        audio.click(soundEnabled);
        return switch (index) {
            case 0 -> Action.NEW_GAME;
            case 1 -> Action.CONTINUE;
            case 2 -> { go(Page.SETTINGS, 0); yield Action.NONE; }
            case 3 -> { go(Page.HOW_TO_PLAY, 0); yield Action.NONE; }
            case 4 -> { go(Page.CREDITS, 0); yield Action.NONE; }
            default -> { go(Page.EXIT_CONFIRM, 1); yield Action.NONE; }
        };
    }

    private void toggle(int index) {
        if (index == 0) {
            musicEnabled = !musicEnabled;
            audio.setMusic(musicEnabled);
        } else {
            soundEnabled = !soundEnabled;
            ForestAudio.setEnabled(soundEnabled);
        }
        audio.click(soundEnabled);
    }

    private Action confirmExit(int choice) {
        if (choice == 1) return back();
        audio.click(soundEnabled);
        return Action.EXIT;
    }

    private Action back() {
        audio.click(soundEnabled);
        open(canContinue);
        return Action.NONE;
    }

    private void go(Page next, int selection) {
        page = next;
        selected = selection;
        hovered = -1;
        animationTime = 0;
    }

    void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (page == Page.LOADING) {
            drawLoading(g);
            return;
        }
        g.drawImage(art.grove, 0, 0, WIDTH, HEIGHT, null);
        drawMain(g);
        if (page == Page.MAIN) return;
        g.setColor(new Color(3, 7, 13, 194));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        switch (page) {
            case SETTINGS -> drawSettings(g);
            case HOW_TO_PLAY -> drawControls(g);
            case CREDITS -> drawCredits(g);
            case EXIT_CONFIRM -> {
                image(g, art.exit, EXIT);
                focus(g, EXIT_OPTIONS[hovered >= 0 ? hovered : selected], 8, 3);
            }
            default -> { }
        }
        if (hovered == -2) focus(g, backBounds(), 2, 2);
    }

    private void drawMain(Graphics2D g) {
        g.setPaint(new GradientPaint(171, 0, new Color(4, 8, 16, 0), WIDTH, 0, new Color(4, 8, 16, 202)));
        g.fillRect(171, 0, WIDTH - 171, HEIGHT);
        g.drawImage(art.logo, 206, 6, 267, 139, null);
        for (int i = 0; i < BUTTONS.length; i++) {
            if (i == 1 && !canContinue) g.setComposite(AlphaComposite.SrcOver.derive(.45f));
            image(g, art.buttons[i], BUTTONS[i]);
            g.setComposite(AlphaComposite.SrcOver);
        }
        if (page == Page.MAIN) {
            focus(g, BUTTONS[hovered >= 0 ? hovered : selected], 14, 5);
        }
    }

    private void drawSettings(Graphics2D g) {
        image(g, art.settings, SETTINGS);
        boolean[] states = {musicEnabled, soundEnabled};
        int focused = hovered >= 0 ? hovered : selected;
        for (int i = 0; i < TOGGLES.length; i++) {
            Rectangle toggle = TOGGLES[i];
            image(g, art.toggles[i][states[i] ? 1 : 0], toggle);
            center(g, states[i] ? "ON" : "OFF", toggle.x + (states[i] ? 50 : 25), toggle.y + 23,
                    LABEL, i == focused ? GOLD : CREAM);
        }
    }

    private void drawControls(Graphics2D g) {
        image(g, art.howTo, INFO);
        for (int i = 0; i < CONTROLS.length; i++) {
            int x = 55 + (i / 5) * 195, y = 130 + (i % 5) * 28;
            label(g, CONTROLS[i][0], x, y, LABEL, GOLD);
            label(g, CONTROLS[i][1], x, y + 12, DETAIL, CREAM);
        }
        center(g, "Collect coins and equip smith upgrades in the inventory.", WIDTH / 2, 274, DETAIL, CREAM);
    }

    private void drawCredits(Graphics2D g) {
        image(g, art.credits, INFO);
        center(g, "AGYU: THE EPIC HERO OF BUKIDNON", WIDTH / 2, 129, LABEL, GOLD);
        center(g, "A Data Structures & Algorithms and OOP project", WIDTH / 2, 149, DETAIL, CREAM);
        center(g, "Character and title artwork supplied by the Agyu project", WIDTH / 2, 188, DETAIL, CREAM);
        center(g, "Optional village house artwork by Rafael Matos", WIDTH / 2, 209, DETAIL, CREAM);
        center(g, "Fiction inspired by Bukidnon landscapes and folklore", WIDTH / 2, 247, DETAIL, CREAM);
    }

    private void drawLoading(Graphics2D g) {
        g.drawImage(art.loading, 0, 0, WIDTH, HEIGHT, null);
        g.setColor(new Color(2, 6, 16, 145));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.drawImage(art.labels[(int) (animationTime * 3) % art.labels.length], 28, 89, 424, 35, null);
        g.drawImage(art.run[(int) (animationTime * 11) % art.run.length], 171, 130, 138, 138, null);
        g.drawImage(art.tips[loadingTip], 24, 289, 432, 41, null);
    }

    private static void focus(Graphics2D g, Rectangle bounds, int insetX, int insetY) {
        int x = bounds.x + insetX, y = bounds.y + insetY;
        int w = bounds.width - 2 * insetX, h = bounds.height - 2 * insetY;
        g.setColor(new Color(255, 177, 79, 35));
        g.fillRoundRect(x, y, w, h, 6, 6);
        g.setColor(GOLD);
        g.drawRoundRect(x, y, w - 1, h - 1, 6, 6);
    }

    private static void image(Graphics2D g, BufferedImage image, Rectangle r) {
        g.drawImage(image, r.x, r.y, r.width, r.height, null);
    }

    private static void center(Graphics2D g, String text, int x, int y, Font font, Color color) {
        g.setFont(font);
        label(g, text, x - g.getFontMetrics().stringWidth(text) / 2, y, font, color);
    }

    private static void label(Graphics2D g, String text, int x, int y, Font font, Color color) {
        g.setFont(font);
        g.setColor(new Color(0x15110D));
        g.drawString(text, x + 1, y + 1);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    /** Cache artwork for all menu instances at twice the logical UI resolution. */
    private static final class Artwork {
        private static Artwork shared;
        final BufferedImage grove = read("grove.jpg", 960, 720, true);
        final BufferedImage loading = read("loading.jpg", 960, 720, true);
        final BufferedImage logo = read("logo.png", 534, 278, false);
        final BufferedImage settings = read("settings_panel.png", 548, 550, false);
        final BufferedImage howTo = read("howto_panel.png", 896, 460, false);
        final BufferedImage credits = read("credits_panel.png", 896, 460, false);
        final BufferedImage exit = read("exit_panel.png", 864, 420, false);
        final BufferedImage[] buttons = frames("menu", 6, 364, 60);
        final BufferedImage[] labels = frames("loading_label", 3, 848, 70);
        final BufferedImage[] tips = frames("loading_tip", 3, 864, 82);
        final BufferedImage[] run = frames("run", 6, 276, 276);
        final BufferedImage[][] toggles = {
            {read("switch_music_off.png", 152, 76, false), read("switch_music_on.png", 152, 76, false)},
            {read("switch_sound_off.png", 152, 76, false), read("switch_sound_on.png", 152, 76, false)}
        };

        private Artwork() throws IOException { }

        static synchronized Artwork get() throws IOException {
            if (shared == null) shared = new Artwork();
            return shared;
        }

        private static BufferedImage[] frames(String prefix, int count, int w, int h) throws IOException {
            BufferedImage[] result = new BufferedImage[count];
            for (int i = 0; i < count; i++) result[i] = read(prefix + "_" + i + ".png", w, h, false);
            return result;
        }

        private static BufferedImage read(String file, int w, int h, boolean cover) throws IOException {
            String path = "res/title/" + file;
            BufferedImage source;
            try (InputStream stream = Assets.openResource(path)) {
                source = ImageIO.read(stream);
            }
            if (source == null) throw new IOException("Unreadable artwork: " + path);
            BufferedImage rendered = new BufferedImage(w, h, cover ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g = rendered.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                double scale = cover ? Math.max(w / (double) source.getWidth(), h / (double) source.getHeight()) : 1;
                int dw = cover ? (int) Math.ceil(source.getWidth() * scale) : w;
                int dh = cover ? (int) Math.ceil(source.getHeight() * scale) : h;
                g.drawImage(source, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
            } finally {
                g.dispose();
            }
            return rendered;
        }
    }
}
