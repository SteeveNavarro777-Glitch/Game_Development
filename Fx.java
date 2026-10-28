import java.awt.*;
import java.util.*;
import java.util.List;

/** "Game feel": dust, sparks, poofs, screen shake and hit-stop. All coordinates are art pixels. */
public class Fx {
    static class P { double x, y, vx, vy, life, max, grav; int size, rgb; }
    static final List<P> list = new ArrayList<>();
    static final Random rnd = new Random();
    static double hitStop, shakeT, shakeMax, shakeMag;
    static int shakeX, shakeY;

    static void clear() { list.clear(); hitStop = shakeT = 0; shakeX = shakeY = 0; }

    static void burst(double x, double y, int rgb, int n, double speed, double life, double grav, int size) {
        for (int i = 0; i < n; i++) {
            P p = new P(); double a = rnd.nextDouble() * Math.PI * 2, s = speed * (0.4 + rnd.nextDouble() * 0.6);
            p.x = x; p.y = y; p.vx = Math.cos(a) * s; p.vy = Math.sin(a) * s;
            p.life = p.max = life * (0.6 + rnd.nextDouble() * 0.4); p.grav = grav; p.size = size; p.rgb = rgb;
            list.add(p);
        }
    }
    static void dust(double x, double y)  { burst(x, y - 1, 0xE3D3AE, 2, 14, 0.35, -10, 2); }
    static void spark(double x, double y) { burst(x, y, 0xFFF3B0, 6, 90, 0.25, 0, 1); }
    static void poof(double x, double y)  { burst(x, y, 0xE58AA0, 12, 60, 0.5, 40, 2); }
    static void shake(double mag, double t) { shakeMag = mag; shakeT = shakeMax = t; }
    static void hitStop(double t) { hitStop = Math.max(hitStop, t); }

    static void update(double dt) {
        for (Iterator<P> it = list.iterator(); it.hasNext(); ) {
            P p = it.next(); p.life -= dt;
            if (p.life <= 0) { it.remove(); continue; }
            p.vy += p.grav * dt; p.x += p.vx * dt; p.y += p.vy * dt;
        }
        if (shakeT > 0) {
            shakeT -= dt; double m = shakeMag * Math.max(0, shakeT / shakeMax);
            shakeX = (int) Math.round((rnd.nextDouble() - 0.5) * 2 * m);
            shakeY = (int) Math.round((rnd.nextDouble() - 0.5) * 2 * m);
        } else shakeX = shakeY = 0;
    }

    static void draw(Graphics2D g, int camX, int camY) {
        for (P p : list) {
            float a = (float) Math.min(1, p.life / p.max * 1.5);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            g.setColor(new Color(p.rgb));
            g.fillRect((int) Math.round(p.x) - camX, (int) Math.round(p.y) - camY, p.size, p.size);
        }
        g.setComposite(AlphaComposite.SrcOver);
    }
}
