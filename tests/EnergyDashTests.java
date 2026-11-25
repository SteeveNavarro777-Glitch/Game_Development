/* FILE PURPOSE: REGRESSION TEST | Energy recovery/exhaustion, actual dash inputs, collision, priority and HUD fills.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

/** Deterministic resource, real-input dash, terrain, safety, pause and HUD regressions. */
public final class EnergyDashTests {
    private static final Canvas SOURCE = new Canvas();
    private static final double DT = 1.0 / 60;
    private static int checks;
    // [BLOCK: TEST HELPER] EnergyDashTests.EnergyDashTests - Initialize EnergyDashTests state.
    private EnergyDashTests() {}
    // [BLOCK: TEST HELPER] EnergyDashTests.main - Entry point for this regression suite.
    public static void main(String[] args) throws Exception {
        Assets.load(); System.out.println("Energy/dash checks passed: " + run());
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.run - Handle run for EnergyDashTests.
    static int run() throws Exception {
        checks = 0;
        inputChecks(); meterChecks(); sprintChecks(); dashChecks(); collisionChecks();
        priorityChecks(); effectChecks(); panelChecks(); hudAndPreview();
        Fx.clear(); return checks;
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.inputChecks - Handle input checks for EnergyDashTests.
    private static void inputChecks() {
        for (int code : new int[]{KeyEvent.VK_Q, KeyEvent.VK_CONTROL}) {
            KeyHandler keys = new KeyHandler(); press(keys, code);
            KeyHandler.Input input = keys.poll();
            check(input.pressed(KeyHandler.DASH) && !input.pressed(KeyHandler.ATTACK)
                    && !input.pressed(KeyHandler.JUMP), "Dash key produced the wrong action");
            press(keys, code); check(!keys.poll().pressed(KeyHandler.DASH), "Held dash repeated");
            release(keys, code); press(keys, code);
            check(keys.poll().pressed(KeyHandler.DASH), "Fresh dash lost");
            keys.clear(); check(!keys.poll().pressed(KeyHandler.DASH), "Focus retained dash");
        }
        KeyHandler keys = new KeyHandler(); press(keys, KeyEvent.VK_Q);
        keys.defer(keys.poll(), KeyHandler.DASH);
        check(keys.poll().pressed(KeyHandler.DASH) && !keys.poll().pressed(KeyHandler.DASH),
                "Deferred dash repeated or disappeared");
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.meterChecks - Handle meter checks for EnergyDashTests.
    private static void meterChecks() {
        EnergyMeter meter = new EnergyMeter();
        check(close(meter.value(), 100) && meter.canSprint() && !meter.exhausted(), "New meter not full");
        check(meter.spend(30) && close(meter.value(), 70), "Incorrect spend");
        check(!meter.spend(80) && close(meter.value(), 70), "Rejected spend modified energy");
        meter.update(.4, true); check(close(meter.value(), 70), "Early energy recovery");
        meter.update(10, false); check(close(meter.value(), 70), "Disabled recovery regenerated");
        meter.update(10, true); check(close(meter.value(), 100), "Recovery failed to clamp");
        meter = new EnergyMeter(); meter.sprint(.5);
        check(close(meter.value(), 87.5), "Sprint drain is inaccurate");
        meter.sprint(5);
        check(close(meter.value(), 0) && meter.exhausted() && !meter.canSprint(), "Exhaustion did not latch");
        meter.update(.4, true); check(close(meter.value(), 0), "Early exhausted recovery");
        for (int i = 0; i < 120 && meter.value() < 10; i++) meter.update(DT, true);
        check(meter.value() > 0 && meter.value() < EnergyMeter.RESUME_AT
                && meter.exhausted() && !meter.canSprint(), "Exhaustion cleared too early");
        meter.update(20, true);
        check(close(meter.value(), 100) && !meter.exhausted() && meter.canSprint(), "Full recovery failed");
        EnergyMeter whole = new EnergyMeter(), split = new EnergyMeter();
        whole.spend(75); split.spend(75); whole.update(1.5, true);
        for (int i = 0; i < 90; i++) split.update(DT, true);
        check(close(whole.value(), split.value()), "Recovery depends on step subdivision");
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.sprintChecks - Handle sprint checks for EnergyDashTests.
    private static void sprintChecks() {
        World world = new World(7); Player actor = player(); KeyHandler keys = new KeyHandler();
        press(keys, KeyEvent.VK_SHIFT);
        for (int i = 0; i < 120; i++) step(actor, keys, world);
        check(close(actor.energy(), 100), "Stationary Shift consumed energy");
        press(keys, KeyEvent.VK_D); double start = actor.x;
        for (int i = 0; i < 30; i++) step(actor, keys, world);
        check(actor.x > start + 40 && actor.state == Player.State.RUN, "Powered sprint did not run");
        check(close(actor.energy(), 87.5), "Moving sprint drain is inaccurate");
        actor = player(); keys = new KeyHandler(); direction(keys, 1, 0); press(keys, KeyEvent.VK_SHIFT);
        for (int i = 0; i < 600 && actor.energy() > 1e-6; i++) {
            home(actor); step(actor, keys, world);
            check(actor.energy() >= 0 && actor.energy() <= 100, "Sprint energy left bounds");
        }
        check(actor.energyExhausted() && actor.energy() < 1e-6, "Continuous sprint did not exhaust");
        home(actor); step(actor, keys, world);
        check(actor.state != Player.State.RUN, "Empty energy allowed running");
        press(keys, KeyEvent.VK_Q); step(actor, keys, world);
        check(!actor.dashing(), "Empty energy allowed dash");
        keys.clear();
        for (int i = 0; i < 360; i++) {
            step(actor, keys, world);
            check(actor.energy() >= 0 && actor.energy() <= 100, "Recovering energy left bounds");
        }
        check(close(actor.energy(), 100) && !actor.energyExhausted(), "Resting did not recover");
        world = new World(7);
        world.tile[World.row(World.SPAWN_Y)][World.col(World.SPAWN_X) + 1] = World.ROCK;
        actor = player(); keys = new KeyHandler(); direction(keys, 1, 0); press(keys, KeyEvent.VK_SHIFT);
        for (int i = 0; i < 30; i++) step(actor, keys, world);
        actor.vx = actor.vy = 0; double energy = actor.energy(), x = actor.x;
        for (int i = 0; i < 5; i++) step(actor, keys, world);
        check(close(actor.x, x) && actor.energy() >= energy - 1e-6, "Wall pushing spent energy");
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.dashChecks - Handle dash checks for EnergyDashTests.
    private static void dashChecks() {
        World world = new World(7); double cardinal = 0;
        for (int[] d : new int[][]{{0,1},{0,-1},{-1,0},{1,0},{1,1},{1,-1},{-1,1},{-1,-1}}) {
            Player actor = player(); KeyHandler keys = new KeyHandler(); direction(keys, d[0], d[1]);
            press(keys, KeyEvent.VK_Q); double x = actor.x, y = actor.y; step(actor, keys, world);
            check(actor.dashing(), "Ground dash did not start");
            check(close(actor.energy(), 75), "Dash cost was not exactly 25");
            check(actor.dashCooldown() > 0 && actor.dashCooldown() <= DashMotion.COOLDOWN + DT, "Cooldown not started");
            for (int i = 0; i < 9; i++) step(actor, keys, world);
            double dx = actor.x - x, dy = actor.y - y, travel = Math.hypot(dx, dy);
            check(!actor.dashing() && close(travel, DashMotion.SPEED * DashMotion.DURATION), "Dash travel is incorrect");
            check((d[0] == 0 || Math.signum(dx) == d[0]) && (d[1] == 0 || Math.signum(dy) == d[1]), "Wrong dash direction");
            if (d[0] == 0 || d[1] == 0) cardinal = travel;
            else check(close(travel, cardinal), "Diagonal dash speed was not normalized");
            Fx.clear();
        }
        Player actor = player(); actor.dir = Player.Dir.LEFT; KeyHandler keys = new KeyHandler();
        press(keys, KeyEvent.VK_Q); step(actor, keys, world);
        check(actor.x < World.SPAWN_X && close(actor.y, World.SPAWN_Y), "Standing dash ignored facing");
        for (int i = 0; i < 60; i++) step(actor, keys, world);
        double x = actor.x, y = actor.y;
        for (int i = 0; i < 30; i++) step(actor, keys, world);
        check(close(actor.x, x) && close(actor.y, y), "Held dash started another burst");
        actor = player(); keys = new KeyHandler(); press(keys, KeyEvent.VK_Q);
        for (int i = 0; i < 12; i++) step(actor, keys, world);
        double energy = actor.energy(); release(keys, KeyEvent.VK_Q); press(keys, KeyEvent.VK_Q);
        step(actor, keys, world);
        check(!actor.dashing() && actor.energy() >= energy - 1e-6, "Cooldown accepted or charged dash");
        release(keys, KeyEvent.VK_Q);
        for (int i = 0; i < 50; i++) step(actor, keys, world);
        home(actor); press(keys, KeyEvent.VK_CONTROL); step(actor, keys, world);
        check(actor.dashing(), "Fresh Ctrl dash failed");
        int hp = actor.health(); actor.damage(1, actor.x - 20, actor.y);
        check(actor.health() == hp && actor.dashing(), "Dash failed to evade damage");
        keys.clear();
        for (int i = 0; i < 20; i++) step(actor, keys, world);
        actor.damage(1, actor.x - 20, actor.y);
        check(actor.health() == hp - 1 && actor.state == Player.State.HURT, "Dash protection outlasted burst");
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.collisionChecks - Handle collision checks for EnergyDashTests.
    private static void collisionChecks() {
        for (int[] d : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
            World world = new World(7);
            int c = World.col(World.SPAWN_X), r = World.row(World.SPAWN_Y);
            world.tile[r + d[1] * 2][c + d[0] * 2] = World.ROCK;
            Player actor = player(); KeyHandler keys = new KeyHandler(); direction(keys, d[0], d[1]);
            press(keys, KeyEvent.VK_Q); actor.update(.16, keys.poll(), world, List.of());
            check(!world.blocked(actor.feetBox()), "Dash ended inside rock");
            check(Math.hypot(actor.x - World.SPAWN_X, actor.y - World.SPAWN_Y) < 80, "Dash tunneled through rock");
            check(!actor.dashing(), "Rock failed to end dash"); Fx.clear();
        }
        World world = new World(7);
        for (int[] row : world.tile) Arrays.fill(row, World.GRASS);
        World.Tree tree = null;Player actor = null;
        for(World.Tree candidate:world.trees){
            Player probe=new Player(candidate.trunk.getMinX()-12,candidate.trunk.getCenterY()+2.55);
            if(!world.blocked(probe.feetBox())){tree=candidate;actor=probe;break;}
        }
        check(actor!=null,"Tree fixture has no clear root-aligned approach");actor.dir=Player.Dir.RIGHT;
        check(!world.blocked(actor.feetBox()), "Tree fixture starts blocked");
        KeyHandler keys = new KeyHandler(); press(keys, KeyEvent.VK_Q);
        actor.update(.16, keys.poll(), world, List.of());
        check(!world.blocked(actor.feetBox()) && actor.feetBox().getMaxX() <= tree.trunk.getMinX() + 1e-6,
                "Dash crossed indexed tree trunk");
        check(!actor.dashing(), "Tree failed to end dash");
        world = new World(7); actor = new Player(World.villageCell(2) * World.T + 6, World.SPAWN_Y); actor.dir = Player.Dir.LEFT;
        keys = new KeyHandler(); press(keys, KeyEvent.VK_Q); actor.update(.16, keys.poll(), world, List.of());
        check(!world.blocked(actor.feetBox()) && actor.x >= World.villageCell(2) * World.T + actor.feetBox().width/2, "Dash crossed village wall");
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.priorityChecks - Handle priority checks for EnergyDashTests.
    private static void priorityChecks() {
        World world = new World(7); Player actor = player(); KeyHandler keys = new KeyHandler();
        press(keys, KeyEvent.VK_Q); press(keys, KeyEvent.VK_SPACE); press(keys, KeyEvent.VK_J);
        step(actor, keys, world);
        check(actor.dashing() && !actor.airborne(), "Dash lost simultaneous input priority");
        keys.clear(); press(keys, KeyEvent.VK_J); step(actor, keys, world); keys.clear();
        for (int i = 0; i < 30; i++) step(actor, keys, world);
        check(actor.state != Player.State.ATTACK && actor.comboStep() == 0, "Dash retained attack input");
        actor = player(); keys = new KeyHandler(); press(keys, KeyEvent.VK_J);
        Enemy target = new WildBoar(actor.x + 25, actor.y, 0); actor.dir = Player.Dir.RIGHT;
        actor.update(DT, keys.poll(), world, List.of(target)); press(keys, KeyEvent.VK_Q);
        actor.update(DT, keys.poll(), world, List.of(target));
        check(actor.dashing(), "Dash failed to cancel attack");
        keys.clear();
        for (int i = 0; i < 35; i++) actor.update(DT, keys.poll(), world, List.of(target));
        check(target.health() == target.maxHealth() && actor.comboStep() == 0, "Canceled attack hit later");
        actor = player(); keys = new KeyHandler(); mouse(keys, true); press(keys, KeyEvent.VK_Q);
        step(actor, keys, world);
        check(actor.state == Player.State.GUARD && close(actor.energy(), 100), "Shield lost priority or spent energy");
        mouse(keys, false);
        for (int i = 0; i < 20; i++) step(actor, keys, world);
        check(!actor.dashing(), "Rejected shield dash executed later");
        actor = player(); keys = new KeyHandler(); press(keys, KeyEvent.VK_SPACE); step(actor, keys, world);
        press(keys, KeyEvent.VK_Q); double energy = actor.energy(); step(actor, keys, world);
        check(actor.airborne() && !actor.dashing() && close(actor.energy(), energy), "Air dash accepted or charged");
        keys.clear();
        for (int i = 0; i < 40; i++) step(actor, keys, world);
        check(!actor.dashing(), "Air dash executed on landing");
        for (Player.State state : new Player.State[]{Player.State.HURT, Player.State.DEAD}) {
            actor = player(); actor.state = state; keys = new KeyHandler(); press(keys, KeyEvent.VK_Q);
            step(actor, keys, world);
            check(!actor.dashing() && close(actor.energy(), 100), "Hurt/dead dash accepted or charged");
        }
        actor = player(); keys = new KeyHandler(); press(keys, KeyEvent.VK_Q); step(actor, keys, world);
        mouse(keys, true);
        for (int i = 0; i < 12; i++) step(actor, keys, world);
        check(actor.state == Player.State.GUARD, "Held shield failed to raise after dash");
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.effectChecks - Handle effect checks for EnergyDashTests.
    private static void effectChecks() {
        Fx.clear(); World world = new World(7);
        for (int n = 0; n < 40; n++) {
            Player actor = player(); actor.dir = Player.Dir.RIGHT;
            KeyHandler keys = new KeyHandler(); press(keys, KeyEvent.VK_Q);
            for (int i = 0; i < 10; i++) step(actor, keys, world);
            check(DashEffects.ghostCount() <= DashEffects.MAX_GHOSTS
                    && DashEffects.boltCount() <= DashEffects.MAX_BOLTS, "Unbounded dash effects");
        }
        check(DashEffects.ghostCount() > 0 && DashEffects.boltCount() > 0, "No dash effects");
        int count = DashEffects.effectCount();
        BufferedImage image = new BufferedImage(240, 160, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            Composite incoming = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .37f);
            g.setComposite(incoming); DashEffects.draw(g, (int)World.SPAWN_X - 70, (int)World.SPAWN_Y - 90);
            check(g.getComposite().equals(incoming), "Effects leaked composite");
            DashEffects.draw(g, 0, 0);
            check(DashEffects.effectCount() == count, "Painting advanced effects");
        } finally { g.dispose(); }
        Fx.update(5); check(DashEffects.effectCount() == 0, "Effects failed to expire");
        DashEffects.emit(0, 0, 20, 0, Assets.front); Fx.clear();
        check(DashEffects.effectCount() == 0, "Restart retained effects");
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.panelChecks - Handle panel checks for EnergyDashTests.
    private static void panelChecks() throws Exception {
        GamePanel panel = new GamePanel(); panel.setSize(GamePanel.W, GamePanel.H); TitleMenuTests.startGame(panel);
        Player actor = (Player)field(panel, "player");
        Fx.hitStop = .04; press(panel.keys, KeyEvent.VK_Q); panel.update(DT); release(panel.keys, KeyEvent.VK_Q);
        check(!actor.dashing(), "Dash advanced during hit-stop");
        for (int i = 0; i < 4; i++) panel.update(DT);
        check(actor.dashing(), "Panel lost dash during hit-stop");
        tap(panel, KeyEvent.VK_ESCAPE);
        double x = actor.x, y = actor.y, energy = actor.energy(), cooldown = actor.dashCooldown();
        for (int i = 0; i < 20; i++) panel.update(DT);
        check(field(panel, "mode") == GamePanel.Mode.PAUSED && close(actor.x, x) && close(actor.y, y)
                && close(actor.energy(), energy) && close(actor.dashCooldown(), cooldown), "Pause advanced dash/energy");
        tap(panel, KeyEvent.VK_ENTER); tap(panel, KeyEvent.VK_M);
        x = actor.x; y = actor.y; energy = actor.energy(); cooldown = actor.dashCooldown();
        for (int i = 0; i < 20; i++) panel.update(DT);
        check(close(actor.x, x) && close(actor.y, y) && close(actor.energy(), energy)
                && close(actor.dashCooldown(), cooldown), "Map advanced dash/energy");
        tap(panel, KeyEvent.VK_M); press(panel.keys, KeyEvent.VK_Q); press(panel.keys, KeyEvent.VK_SHIFT);
        for (java.awt.event.FocusListener listener : panel.getFocusListeners())
            listener.focusLost(new FocusEvent(panel, FocusEvent.FOCUS_LOST));
        energy = actor.energy(); cooldown = actor.dashCooldown();
        for (int i = 0; i < 10; i++) panel.update(DT);
        KeyHandler.Input input = panel.keys.poll();
        check(field(panel, "mode") == GamePanel.Mode.PAUSED && close(actor.energy(), energy)
                && close(actor.dashCooldown(), cooldown) && !input.run && !input.pressed(KeyHandler.DASH), "Focus clear failed");
        tap(panel, KeyEvent.VK_ENTER);
        for (int i = 0; i < 20; i++) panel.update(DT);
        actor.damage(100, actor.x - 20, actor.y); Fx.hitStop = 0; panel.update(DT);
        check(field(panel, "mode") == GamePanel.Mode.DEFEAT, "Defeat fixture failed");
        tap(panel, KeyEvent.VK_R); actor = (Player)field(panel, "player");
        check(close(actor.energy(), 100) && !actor.energyExhausted() && !actor.dashing()
                && close(actor.dashCooldown(), 0), "Restart retained dash/energy");
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.hudAndPreview - Handle hud and preview for EnergyDashTests.
    private static void hudAndPreview() throws Exception {
        GamePanel panel = new GamePanel(); panel.setSize(GamePanel.W, GamePanel.H); TitleMenuTests.startGame(panel);
        Player actor = (Player)field(panel, "player");
        actor.damage(2, actor.x - 20, actor.y); Fx.clear();
        for (int i = 0; i < 25; i++) panel.update(DT);
        direction(panel.keys, 1, 0); press(panel.keys, KeyEvent.VK_SHIFT);
        for (int i = 0; i < 35; i++) panel.update(DT);
        press(panel.keys, KeyEvent.VK_Q);
        for (int i = 0; i < 4; i++) panel.update(DT);
        BufferedImage hud = new BufferedImage(GamePanel.VIEW_W, GamePanel.VIEW_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = hud.createGraphics();
        try {
            Method draw = GamePanel.class.getDeclaredMethod("drawHud", Graphics2D.class);
            draw.setAccessible(true); draw.invoke(panel, g);
        } finally { g.dispose(); }
        check(hud.getRGB(70, 16) == new Color(0x69C833).getRGB(), "Health bar has no green potion fill");
        check(hud.getRGB(210, 16) == new Color(0x35444C).getRGB(), "Health bar ignored damage");
        check(hud.getRGB(70, 32) == new Color(0xED9C23).getRGB(), "Energy bar has no orange potion fill");
        check(hud.getRGB(230, 32) == new Color(0x35444C).getRGB(), "Energy bar ignored spending");
        BufferedImage image = new BufferedImage(GamePanel.W, GamePanel.H, BufferedImage.TYPE_INT_RGB);
        g = image.createGraphics();
        try { panel.paint(g); } finally { g.dispose(); }
        check(ImageIO.write(image, "png", new File("build/preview-energy-dash.png")), "Preview write failed");
        Fx.clear();
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.home - Handle home for EnergyDashTests.
    private static void home(Player actor) {
        actor.x = actor.px = World.SPAWN_X; actor.y = actor.py = World.SPAWN_Y;
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.player - Handle player for EnergyDashTests.
    private static Player player() { return new Player(World.SPAWN_X, World.SPAWN_Y); }
    // [BLOCK: TEST HELPER] EnergyDashTests.close - Handle close for EnergyDashTests.
    private static boolean close(double a, double b) { return Math.abs(a - b) < 1e-6; }
    // [BLOCK: TEST HELPER] EnergyDashTests.step - Handle step for EnergyDashTests.
    private static void step(Player actor, KeyHandler keys, World world) {
        actor.savePrev(); actor.update(DT, keys.poll(), world, List.of());
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.direction - Handle direction for EnergyDashTests.
    private static void direction(KeyHandler keys, int dx, int dy) {
        if (dx < 0) press(keys, KeyEvent.VK_A);
        if (dx > 0) press(keys, KeyEvent.VK_D);
        if (dy < 0) press(keys, KeyEvent.VK_W);
        if (dy > 0) press(keys, KeyEvent.VK_S);
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.tap - Handle tap for EnergyDashTests.
    private static void tap(GamePanel panel, int code) {
        press(panel.keys, code); panel.update(DT); release(panel.keys, code);
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.field - Handle field for EnergyDashTests.
    private static Object field(Object object, String name) throws Exception {
        Field field = object.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(object);
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.press - Handle press for EnergyDashTests.
    private static void press(KeyHandler keys, int code) {
        keys.keyPressed(new KeyEvent(SOURCE, KeyEvent.KEY_PRESSED, 0, 0, code, KeyEvent.CHAR_UNDEFINED));
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.release - Handle release for EnergyDashTests.
    private static void release(KeyHandler keys, int code) {
        keys.keyReleased(new KeyEvent(SOURCE, KeyEvent.KEY_RELEASED, 0, 0, code, KeyEvent.CHAR_UNDEFINED));
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.mouse - Handle mouse for EnergyDashTests.
    private static void mouse(KeyHandler keys, boolean down) {
        MouseEvent event = new MouseEvent(SOURCE, down ? MouseEvent.MOUSE_PRESSED : MouseEvent.MOUSE_RELEASED,
                0, 0, 10, 10, 1, false, MouseEvent.BUTTON3);
        if (down) keys.mousePressed(event); else keys.mouseReleased(event);
    }
    // [BLOCK: TEST HELPER] EnergyDashTests.check - Handle check for EnergyDashTests.
    private static void check(boolean valid, String message) {
        checks++; if (!valid) throw new AssertionError(message);
    }
}
