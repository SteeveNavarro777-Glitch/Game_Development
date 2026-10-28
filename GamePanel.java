import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.JPanel;

/**
 * Smoothness checklist implemented here:
 *  1. Draw into a small 480x360 buffer, then scale x2 with nearest-neighbour  -> every pixel is the same size.
 *  2. Fixed 60Hz update + RENDER INTERPOLATION (alpha)                          -> smooth on 60/120/144Hz monitors.
 *  3. Smoothed camera (exponential follow) snapped to whole art pixels          -> no jitter, no shimmer.
 *  4. update() and paint() share a lock                                         -> no half-updated frames (tearing).
 *  5. Game feel: hit-stop, screen shake, dust, sparks, day/night, vignette.
 */
public class GamePanel extends JPanel implements Runnable {
    static final int VIEW_W = 480, VIEW_H = 360, SCALE = 2, W = VIEW_W * SCALE, H = VIEW_H * SCALE, FPS = 60;

    final KeyHandler keys = new KeyHandler();
    final Object lock = new Object();
    World world; Player player; List<Slime> slimes;
    double time, camX, camY, prevCamX, prevCamY;
    volatile double alpha;
    final float[][] petals = new float[24][4];
    final BufferedImage buf = new BufferedImage(VIEW_W, VIEW_H, BufferedImage.TYPE_INT_RGB);
    BufferedImage vignette;
    private Thread thread;

    GamePanel() throws java.io.IOException {
        setPreferredSize(new Dimension(W, H));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(keys);
        Assets.load();
        buildVignette();
        Random r = new Random();
        for (float[] p : petals) { p[0] = r.nextInt(VIEW_W); p[1] = r.nextInt(VIEW_H); p[2] = 14 + r.nextInt(14); p[3] = r.nextFloat() * 6; }
        reset();
    }

    void reset() {
        synchronized (lock) {
            Fx.clear();
            world = new World(7);
            player = new Player(World.SPAWN_X, World.SPAWN_Y);
            slimes = new ArrayList<>();
            Random r = new Random(); int tries = 0;
            while (slimes.size() < 10 && tries++ < 800) {
                Slime s = new Slime(r.nextDouble() * world.cols * World.T, r.nextDouble() * world.rows * World.T);
                if (!world.blocked(s.feetBox()) && Math.hypot(s.x - player.x, s.y - player.y) > 180) slimes.add(s);
            }
            camX = prevCamX = targetX(); camY = prevCamY = targetY();
        }
    }

    private void buildVignette() {
        vignette = new BufferedImage(VIEW_W, VIEW_H, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < VIEW_H; y++) for (int x = 0; x < VIEW_W; x++) {
            double dx = (x - VIEW_W / 2.0) / (VIEW_W / 2.0), dy = (y - VIEW_H / 2.0) / (VIEW_H / 2.0);
            double d = Math.max(0, Math.sqrt(dx * dx + dy * dy) - 0.55) / 0.9;
            vignette.setRGB(x, y, ((int) Math.min(95, d * d * 120) << 24) | 0x101828);
        }
    }

    void start() { thread = new Thread(this, "game-loop"); thread.start(); }

    @Override public void run() {
        final double step = 1.0 / FPS;
        double acc = 0; long last = System.nanoTime();
        while (true) {
            long now = System.nanoTime();
            acc += Math.min(0.25, (now - last) / 1e9); last = now;
            while (acc >= step) { synchronized (lock) { update(step); } acc -= step; }
            alpha = acc / step;                               // how far we are between two updates
            repaint();
            Toolkit.getDefaultToolkit().sync();
            long spare = (long) ((step - acc) * 1e9) - 1_000_000;
            if (spare > 0) java.util.concurrent.locks.LockSupport.parkNanos(Math.min(spare, 8_000_000L));
        }
    }

    private double targetX() {
        double t = player.x - VIEW_W / 2.0 + Math.max(-30, Math.min(30, player.vx * 0.25));     // look slightly ahead
        return Math.max(0, Math.min(t, world.cols * World.T - VIEW_W));
    }
    private double targetY() {
        double t = player.y - 24 - VIEW_H / 2.0 + Math.max(-20, Math.min(20, player.vy * 0.18));
        return Math.max(0, Math.min(t, world.rows * World.T - VIEW_H));
    }

    void update(double dt) {
        player.savePrev(); for (Slime s : slimes) s.savePrev();
        prevCamX = camX; prevCamY = camY;
        if (keys.restart && player.state == Player.State.DEAD) { reset(); return; }
        if (Fx.hitStop > 0) { Fx.hitStop -= dt; Fx.update(dt); return; }      // hit-stop: freeze the world for a few frames

        time += dt;
        player.update(dt, keys, world, slimes);
        for (Slime s : slimes) s.update(dt, player, world);
        slimes.removeIf(Slime::removable);
        Fx.update(dt);

        double k = 1 - Math.exp(-6 * dt);                     // frame-rate independent camera smoothing
        camX += (targetX() - camX) * k; camY += (targetY() - camY) * k;

        for (float[] p : petals) {
            p[1] += p[2] * dt; p[0] += (Math.sin(time * 1.5 + p[3]) * 10 + 8) * dt;
            if (p[1] > VIEW_H) { p[1] = -4; p[0] = (float) (Math.random() * VIEW_W); }
            if (p[0] > VIEW_W) p[0] = 0;
        }
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        synchronized (lock) {
            Graphics2D b = buf.createGraphics();
            double a = alpha;
            int cx = (int) Math.round(prevCamX + (camX - prevCamX) * a) + Fx.shakeX;
            int cy = (int) Math.round(prevCamY + (camY - prevCamY) * a) + Fx.shakeY;

            b.setColor(new Color(0x5A3E29)); b.fillRect(0, 0, VIEW_W, VIEW_H);
            world.drawGround(b, cx, cy, VIEW_W, VIEW_H, time);

            List<Drawable> objs = new ArrayList<>();
            for (World.Tree t : world.trees)
                if (t.x > cx - 40 && t.x < cx + VIEW_W + 40 && t.y > cy && t.y < cy + VIEW_H + 70) objs.add(t);
            objs.addAll(slimes); objs.add(player);
            objs.sort(Comparator.comparingDouble(Drawable::sortY));
            for (Drawable d : objs) d.draw(b, cx, cy, a);

            Fx.draw(b, cx, cy);
            for (int i = 0; i < petals.length; i++) {         // drifting petals
                b.setColor(i % 2 == 0 ? new Color(255, 205, 220) : new Color(255, 170, 195));
                b.fillRect((int) petals[i][0], (int) petals[i][1], 2, 2);
            }

            double light = 0.5 + 0.5 * Math.cos(time / 75.0 * Math.PI * 2);        // day/night cycle (75s)
            int night = (int) (Math.pow(1 - light, 1.6) * 120);
            if (night > 0) { b.setColor(new Color(18, 24, 70, night)); b.fillRect(0, 0, VIEW_W, VIEW_H); }
            b.drawImage(vignette, 0, 0, null);
            b.dispose();
        }
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(buf, 0, 0, W, H, null);
        drawHud(g);
    }

    private void drawHud(Graphics2D g) {
        String[] heart = {".XX.XX.", "XXXXXXX", "XXXXXXX", ".XXXXX.", "..XXX..", "...X..."};
        for (int i = 0; i < player.maxHp; i++) {
            int ox = 16 + i * 40, oy = 16;
            for (int r = 0; r < heart.length; r++) for (int c = 0; c < 7; c++) if (heart[r].charAt(c) == 'X') {
                g.setColor(new Color(0x2A1A22)); g.fillRect(ox + c * 4 - 2, oy + r * 4 - 2, 8, 8);
            }
            for (int r = 0; r < heart.length; r++) for (int c = 0; c < 7; c++) if (heart[r].charAt(c) == 'X') {
                g.setColor(i < player.hp ? (r < 2 && c < 3 ? new Color(0xFF6B7F) : new Color(0xE0344F)) : new Color(0x4A3A44));
                g.fillRect(ox + c * 4, oy + r * 4, 4, 4);
            }
        }
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.setColor(new Color(0, 0, 0, 130)); g.fillRect(0, H - 26, W, 26);
        g.setColor(Color.WHITE);
        g.drawString("WASD / Arrows: move    Shift: run    Space / J: spear    R: restart", 12, H - 8);
        if (player.state == Player.State.DEAD) {
            g.setColor(new Color(0, 0, 0, 150)); g.fillRect(0, 0, W, H);
            g.setColor(Color.WHITE); g.setFont(new Font("Monospaced", Font.BOLD, 36));
            String s = "AGYU HAS FALLEN  -  press R";
            g.drawString(s, (W - g.getFontMetrics().stringWidth(s)) / 2, H / 2);
        }
    }
}
