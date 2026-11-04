import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import javax.imageio.ImageIO;

/** Real input, jump/guard/combat transitions, ground collision and render checks. */
public final class JumpTests {
    private static final Canvas SOURCE = new Canvas();
    private static final double DT = 1.0 / 60;
    private static int checks;
    private JumpTests() {}
    static int run() throws Exception {
        checks = 0;
        inputChecks();
        arcChecks();
        playerChecks();
        panelChecks();
        preview();
        Fx.clear();
        return checks;
    }
    private static void inputChecks() {
        KeyHandler keys = new KeyHandler();
        press(keys, KeyEvent.VK_SPACE);
        KeyHandler.Input input = keys.poll();
        check(input.pressed(KeyHandler.JUMP) && !input.pressed(KeyHandler.ATTACK), "Space must jump instead of attack");
        press(keys, KeyEvent.VK_SPACE);
        check(!keys.poll().pressed(KeyHandler.JUMP), "Space autorepeat produced another jump");
        release(keys, KeyEvent.VK_SPACE); press(keys, KeyEvent.VK_SPACE);
        check(keys.poll().pressed(KeyHandler.JUMP), "Second Space press was lost");
        keys.clear(); press(keys, KeyEvent.VK_J);
        input = keys.poll();
        check(input.pressed(KeyHandler.ATTACK) && !input.pressed(KeyHandler.JUMP), "J attack mapping changed");
        keys.clear(); mouse(keys, MouseEvent.BUTTON1, true);
        check(keys.poll().pressed(KeyHandler.ATTACK), "Left click attack mapping changed");
        keys.clear(); press(keys, KeyEvent.VK_SPACE); input = keys.poll();
        keys.defer(input, KeyHandler.JUMP);
        check(keys.poll().pressed(KeyHandler.JUMP) && !keys.poll().pressed(KeyHandler.JUMP), "Deferred jump repeated or disappeared");
        keys.clear();
        check(!keys.poll().pressed(KeyHandler.JUMP), "Clearing focus left a jump edge");
    }
    private static void arcChecks() {
        JumpMotion jump = new JumpMotion();
        check(jump.begin() && !jump.begin(), "Double jump allowed");
        check(!jump.update(JumpMotion.DURATION / 2), "Arc landed before its midpoint");
        check(Math.abs(jump.height() - JumpMotion.PEAK) < 1e-9, "Jump peak is incorrect");
        check(jump.renderHeight(0) == 0 && Math.abs(jump.renderHeight(.5) - JumpMotion.PEAK / 2) < 1e-9,
                "Jump interpolation skipped the previous height");
        jump.savePrev();
        check(jump.update(JumpMotion.DURATION), "Large step failed to land");
        check(!jump.airborne() && jump.height() == 0 && !jump.update(DT), "Landing was not exact or repeated");
        check(Math.abs(jump.renderHeight(.5) - JumpMotion.PEAK / 2) < 1e-9, "Landing interpolation snapped");
        jump.savePrev();
        check(jump.renderHeight(.5) == 0 && jump.begin(), "Landed jump failed to reset");
        JumpMotion whole = new JumpMotion(), split = new JumpMotion();
        whole.begin(); split.begin(); whole.update(.2);
        for (int i = 0; i < 12; i++) split.update(DT);
        check(Math.abs(whole.height() - split.height()) < 1e-9, "Arc depends on update subdivision");
    }
    private static void playerChecks() throws Exception {
        World world = new World(7);
        for (Player.Dir direction : Player.Dir.values()) {
            Player actor = player(); actor.dir = direction;
            KeyHandler keys = new KeyHandler(); press(keys, KeyEvent.VK_SPACE);
            step(actor, keys, world);
            check(actor.airborne() && actor.state == Player.State.IDLE, "Standing Space did not begin a hop");
            double x = actor.x, y = actor.y;
            for (int i = 0; i < 16; i++) step(actor, keys, world);
            check(actor.jumpHeight() > 17.9 && actor.x == x && actor.y == y, "Jump moved the ground position");
            check(actor.sortY() == y && actor.feetBox().getMaxY() == y, "Jump changed ground sorting or collision");
            BufferedImage actual = new BufferedImage(192, 144, BufferedImage.TYPE_INT_ARGB);
            Player view = new Player(96, 100); view.dir = direction;
            KeyHandler viewKeys = new KeyHandler(); press(viewKeys, KeyEvent.VK_SPACE);
            view.update(DT, viewKeys.poll(), world, List.of());
            view.savePrev(); view.update(JumpMotion.DURATION / 2, viewKeys.poll(), world, List.of());
            Graphics2D g = actual.createGraphics();
            try { view.draw(g, 0, 0, 1); } finally { g.dispose(); }
            BufferedImage expected = new BufferedImage(192, 144, BufferedImage.TYPE_INT_ARGB);
            g = expected.createGraphics();
            try {
                g.setColor(new Color(0, 0, 0, 70)); g.fillOval(85, 96, 22, 8);
                BufferedImage body = Assets.idle[direction.ordinal()][(int) (view.animTime * 5) % Assets.idle[direction.ordinal()].length];
                g.drawImage(body, 96 - body.getWidth() / 2, 100 - body.getHeight() + 1 - 18, null);
            } finally { g.dispose(); }
            boolean equal = true;
            for (int py = 0; py < 144; py++) for (int px = 0; px < 192; px++)
                if (actual.getRGB(px, py) != expected.getRGB(px, py)) equal = false;
            check(equal, "Jump body, spear and shield did not rise together above the ground shadow");
            for (int i = 0; i < 80; i++) {
                step(actor, keys, world);
                check(actor.jumpHeight() >= 0 && actor.jumpHeight() <= JumpMotion.PEAK, "Jump height left its bounds");
            }
            check(!actor.airborne() && actor.jumpHeight() == 0 && actor.state == Player.State.IDLE, "Held Space bounced or attacked after landing");
            release(keys, KeyEvent.VK_SPACE); press(keys, KeyEvent.VK_SPACE); step(actor, keys, world);
            check(actor.airborne(), "Fresh Space did not jump after landing");
        }
        Player actor = player(), ground = player();
        KeyHandler airKeys = new KeyHandler(), groundKeys = new KeyHandler();
        press(airKeys, KeyEvent.VK_D); press(airKeys, KeyEvent.VK_SHIFT); press(airKeys, KeyEvent.VK_SPACE);
        press(groundKeys, KeyEvent.VK_D); press(groundKeys, KeyEvent.VK_SHIFT);
        Fx.clear();
        for (int i = 0; i < 25; i++) {
            step(actor, airKeys, world);
            check(Fx.list.isEmpty(), "Airborne running emitted ground dust");
            step(ground, groundKeys, world); Fx.clear();
        }
        check(actor.x > World.SPAWN_X + 30 && actor.x == ground.x && actor.y == ground.y, "Jump interrupted horizontal running");
        world.tile[World.row(actor.y)][World.col(actor.x) + 1] = World.ROCK;
        for (int i = 0; i < 30; i++) {
            step(actor, airKeys, world);
            check(!world.blocked(actor.feetBox()), "Jump bypassed a solid tile");
        }
        world = new World(7);
        actor = player(); airKeys = new KeyHandler(); press(airKeys, KeyEvent.VK_SPACE);
        step(actor, airKeys, world);
        for (int i = 0; i < 10; i++) step(actor, airKeys, world);
        double before = actor.jumpHeight();
        release(airKeys, KeyEvent.VK_SPACE); press(airKeys, KeyEvent.VK_SPACE); press(airKeys, KeyEvent.VK_J);
        mouse(airKeys, MouseEvent.BUTTON3, true); step(actor, airKeys, world);
        check(actor.jumpHeight() > before && actor.state != Player.State.ATTACK && actor.state != Player.State.GUARD,
                "An airborne jump/attack/guard snapped or interrupted the arc");
        for (int i = 0; i < 40; i++) step(actor, airKeys, world);
        check(!actor.airborne() && actor.state == Player.State.GUARD, "Held shield did not raise on landing");
        mouse(airKeys, MouseEvent.BUTTON3, false); step(actor, airKeys, world);
        check(actor.state == Player.State.IDLE, "Old airborne attack executed after guard release");
        release(airKeys, KeyEvent.VK_J); press(airKeys, KeyEvent.VK_J); step(actor, airKeys, world);
        check(actor.state == Player.State.ATTACK, "Fresh attack after landing failed");
        for (Player.State state : new Player.State[]{Player.State.ATTACK, Player.State.GUARD, Player.State.HURT, Player.State.DEAD}) {
            actor = player(); actor.state = state; airKeys = new KeyHandler(); press(airKeys, KeyEvent.VK_SPACE);
            step(actor, airKeys, world);
            check(!actor.airborne(), "Invalid combat state allowed jump");
        }
        actor = player(); airKeys = new KeyHandler();
        press(airKeys, KeyEvent.VK_SPACE); mouse(airKeys, MouseEvent.BUTTON3, true); step(actor, airKeys, world);
        check(!actor.airborne() && actor.state == Player.State.GUARD, "Simultaneous Space and shield lost guard priority");
        for (boolean lethal : new boolean[]{false, true}) {
            actor = player(); airKeys = new KeyHandler(); press(airKeys, KeyEvent.VK_SPACE);
            step(actor, airKeys, world);
            for (int i = 0; i < 8; i++) step(actor, airKeys, world);
            before = actor.jumpHeight(); actor.damage(lethal ? 100 : 1, actor.x - 20, actor.y);
            check(actor.health() == (lethal ? 0 : actor.maxHealth() - 1) && actor.jumpHeight() == before,
                    "Jump silently granted damage immunity or snapped on damage");
            Fx.clear();
            for (int i = 0; i < 40; i++) step(actor, airKeys, world);
            check(!actor.airborne() && actor.jumpHeight() == 0, "Hurt/dead actor did not finish falling");
            if (lethal) check(Fx.list.isEmpty(), "Dead jump emitted landing dust");
            Fx.clear();
        }
    }
    private static void panelChecks() throws Exception {
        GamePanel panel = new GamePanel(); panel.setSize(GamePanel.W, GamePanel.H);
        tap(panel, KeyEvent.VK_ENTER);
        Player actor = (Player) field(panel, "player");
        Fx.hitStop = .04; press(panel.keys, KeyEvent.VK_SPACE); panel.update(DT); release(panel.keys, KeyEvent.VK_SPACE);
        check(!actor.airborne(), "Jump ignored hit-stop");
        for (int i = 0; i < 4; i++) panel.update(DT);
        check(actor.airborne() && actor.state != Player.State.ATTACK, "Real panel lost a jump during hit-stop");
        for (int i = 0; i < 8; i++) panel.update(DT);
        double height = actor.jumpHeight();
        tap(panel, KeyEvent.VK_ESCAPE);
        for (int i = 0; i < 5; i++) panel.update(DT);
        check(field(panel, "mode") == GamePanel.Mode.PAUSED && actor.jumpHeight() == height, "Pause advanced a jump");
        tap(panel, KeyEvent.VK_ENTER); panel.update(DT);
        check(actor.jumpHeight() != height, "Resuming left jump frozen");
        height = actor.jumpHeight(); tap(panel, KeyEvent.VK_M);
        for (int i = 0; i < 5; i++) panel.update(DT);
        check(actor.jumpHeight() == height, "Map advanced a jump");
        tap(panel, KeyEvent.VK_M);
        for (java.awt.event.FocusListener listener : panel.getFocusListeners())
            listener.focusLost(new FocusEvent(panel, FocusEvent.FOCUS_LOST));
        height = actor.jumpHeight(); panel.update(DT);
        check(field(panel, "mode") == GamePanel.Mode.PAUSED && actor.jumpHeight() == height &&
                !panel.keys.poll().pressed(KeyHandler.JUMP), "Focus loss advanced jump or retained input");
        tap(panel, KeyEvent.VK_ENTER);
        for (int i = 0; i < 50; i++) panel.update(DT);
        check(!actor.airborne(), "Jump did not settle after focus returned");
        tap(panel, KeyEvent.VK_SPACE);
        for (int i = 0; i < 16; i++) panel.update(DT);
        BufferedImage image = new BufferedImage(GamePanel.W, GamePanel.H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try { panel.paint(g); } finally { g.dispose(); }
        check(ImageIO.write(image, "png", new File("build/preview-jump-in-game.png")), "Real-camera jump preview failed");
        actor.damage(100, actor.x - 20, actor.y); Fx.clear(); panel.update(DT);
        check(field(panel, "mode") == GamePanel.Mode.DEFEAT, "Airborne defeat did not enter defeat mode");
        for (int i = 0; i < 50; i++) panel.update(DT);
        check(!actor.airborne() && actor.jumpHeight() == 0, "Defeat overlay froze Agyu above the ground");
        BufferedImage first = renderPlayer(actor, 0), last = renderPlayer(actor, 1);
        check(same(first, last), "Landed corpse retained stale jump interpolation");
        panel = new GamePanel(); panel.setSize(GamePanel.W, GamePanel.H); tap(panel, KeyEvent.VK_ENTER);
        actor = (Player) field(panel, "player");
        QuestLog log = (QuestLog) field(panel, "quests");
        for (String shrine : QuestLog.SHRINES) log.finish(shrine);
        log.finish("guardian"); tap(panel, KeyEvent.VK_SPACE); tap(panel, KeyEvent.VK_E);
        check(field(panel, "mode") == GamePanel.Mode.PLAYING && !log.done("home"), "Airborne home interaction froze a victory pose");
        for (int i = 0; i < 40; i++) panel.update(DT);
        tap(panel, KeyEvent.VK_E);
        check(field(panel, "mode") == GamePanel.Mode.VICTORY && !actor.airborne(), "Grounded home interaction failed after landing");
    }
    private static void preview() throws Exception {
        BufferedImage image = new BufferedImage(1200, 880, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        double[] phases = {0, .135, .27, .405, .54};
        try {
            g.setColor(new Color(0x263A35)); g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setFont(new Font("Monospaced", Font.PLAIN, 14));
            World world = new World(7);
            for (int row = 0; row < 4; row++) for (int col = 0; col < phases.length; col++) {
                g.setColor(new Color(0xF1C979));
                g.drawString(Player.Dir.values()[row] + " / " + phases[col] + "s", col * 240 + 12, row * 220 + 20);
                Graphics2D cell = (Graphics2D) g.create();
                try {
                    cell.translate(col * 240, row * 220 + 25); cell.scale(2, 2);
                    Player actor = new Player(60, 85); actor.dir = Player.Dir.values()[row];
                    KeyHandler keys = new KeyHandler(); press(keys, KeyEvent.VK_SPACE);
                    actor.update(DT, keys.poll(), world, List.of());
                    actor.savePrev(); actor.update(phases[col], keys.poll(), world, List.of());
                    actor.draw(cell, 0, 0, 1);
                } finally { cell.dispose(); }
            }
        } finally { g.dispose(); }
        check(ImageIO.write(image, "png", new File("build/preview-jump.png")), "Jump phase preview failed");
    }
    private static BufferedImage renderPlayer(Player actor, double alpha) {
        BufferedImage image = new BufferedImage(192, 144, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try { actor.draw(g, (int) actor.x - 96, (int) actor.y - 100, alpha); } finally { g.dispose(); }
        return image;
    }
    private static boolean same(BufferedImage first, BufferedImage second) {
        for (int y = 0; y < first.getHeight(); y++) for (int x = 0; x < first.getWidth(); x++)
            if (first.getRGB(x, y) != second.getRGB(x, y)) return false;
        return true;
    }
    private static Player player() { return new Player(World.SPAWN_X, World.SPAWN_Y); }
    private static void step(Player actor, KeyHandler keys, World world) {
        actor.savePrev(); actor.update(DT, keys.poll(), world, List.of());
    }
    private static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(object);
    }
    private static void tap(GamePanel panel, int key) { press(panel.keys, key); panel.update(DT); release(panel.keys, key); }
    private static void press(KeyHandler keys, int key) {
        keys.keyPressed(new KeyEvent(SOURCE, KeyEvent.KEY_PRESSED, 0, 0, key, KeyEvent.CHAR_UNDEFINED));
    }
    private static void release(KeyHandler keys, int key) {
        keys.keyReleased(new KeyEvent(SOURCE, KeyEvent.KEY_RELEASED, 0, 0, key, KeyEvent.CHAR_UNDEFINED));
    }
    private static void mouse(KeyHandler keys, int button, boolean down) {
        MouseEvent event = new MouseEvent(SOURCE, down ? MouseEvent.MOUSE_PRESSED : MouseEvent.MOUSE_RELEASED,
                0, 0, 10, 10, 1, false, button);
        if (down) keys.mousePressed(event); else keys.mouseReleased(event);
    }
    private static void check(boolean valid, String message) { checks++; if (!valid) throw new AssertionError(message); }
}
