import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Cached-body afterimages and bounded jagged lightning; emitted only by simulation. */
final class DashEffects {
    static final int MAX_GHOSTS = 20, MAX_BOLTS = 32;
    private static final Color BLUE = new Color(0x337CBA), CYAN = new Color(0x64E9FF), WHITE = new Color(0xEBFFFF);
    private static final BasicStroke GLOW = new BasicStroke(5), RIM = new BasicStroke(3), CORE = new BasicStroke(1);
    private static final List<Ghost> ghosts = new ArrayList<>(MAX_GHOSTS);
    private static final List<Bolt> bolts = new ArrayList<>(MAX_BOLTS);
    private static int sequence;
    private static final class Ghost {
        BufferedImage image; double x, y, life = .22;
    }
    private static final class Bolt {
        final int[] x = new int[5], y = new int[5]; double life = .18;
    }
    private DashEffects() {}
    static int ghostCount() { return ghosts.size(); }
    static int boltCount() { return bolts.size(); }
    static int effectCount() { return ghosts.size() + bolts.size(); }
    static void clear() { ghosts.clear(); bolts.clear(); sequence = 0; }
    static void emit(double x0, double y0, double x1, double y1, BufferedImage body) {
        if (Math.hypot(x1 - x0, y1 - y0) < .01) return;
        if (body != null && ghosts.size() < MAX_GHOSTS) {
            Ghost ghost = new Ghost(); ghost.image = body; ghost.x = x0; ghost.y = y0; ghosts.add(ghost);
        }
        double length = Math.hypot(x1 - x0, y1 - y0);
        double nx = -(y1 - y0) / length, ny = (x1 - x0) / length;
        int phase = sequence++;
        if (bolts.size() < MAX_BOLTS) {
            int rail = phase % 2;
            Bolt bolt = new Bolt();
            for (int i = 0; i < bolt.x.length; i++) {
                double u = i / (double) (bolt.x.length - 1);
                double zig = i == 0 || i == bolt.x.length - 1 ? 0 : ((i + phase) % 2 == 0 ? 2.5 : -2.5);
                double offset = ((rail == 0 ? -9 : 9) + zig)*GameScale.AGYU;
                double along = -15 + (length + 8) * u;
                bolt.x[i] = (int) Math.round(x0 + (x1 - x0) / length * along + nx * offset);
                bolt.y[i] = (int) Math.round(y0 + (y1 - y0) / length * along - 21*GameScale.AGYU + ny * offset);
            }
            bolts.add(bolt);
        }
    }
    static void update(double dt) {
        if (dt <= 0) return;
        for (int i = ghosts.size() - 1; i >= 0; i--) if ((ghosts.get(i).life -= dt) <= 0) ghosts.remove(i);
        for (int i = bolts.size() - 1; i >= 0; i--) if ((bolts.get(i).life -= dt) <= 0) bolts.remove(i);
    }
    static void draw(Graphics2D target, int cx, int cy) {
        if (effectCount() == 0) return;
        Graphics2D g = (Graphics2D) target.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            Composite incoming = g.getComposite();
            float global = incoming instanceof AlphaComposite ? ((AlphaComposite) incoming).getAlpha() : 1;
            for (Ghost ghost : ghosts) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (global * .32 * ghost.life / .22)));
                GameScale.agyuFrame(g,ghost.image,Math.round(ghost.x)-cx,Math.round(ghost.y)-cy);
            }
            for (Bolt bolt : bolts) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (global * .8 * bolt.life / .18)));
                g.setColor(BLUE); g.setStroke(GLOW); line(g, bolt, cx, cy);
                g.setColor(CYAN); g.setStroke(RIM); line(g, bolt, cx, cy);
                g.setColor(WHITE); g.setStroke(CORE); line(g, bolt, cx, cy);
            }
        } finally { g.dispose(); }
    }
    private static void line(Graphics2D g, Bolt bolt, int cx, int cy) {
        for (int i = 1; i < bolt.x.length; i++)
            g.drawLine(bolt.x[i - 1] - cx, bolt.y[i - 1] - cy, bolt.x[i] - cx, bolt.y[i] - cy);
    }
}
