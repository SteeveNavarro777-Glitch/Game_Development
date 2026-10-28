import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Enemy: wanders, chases Agyu when close, knockback, white hit-flash, poof on death. */
public class Slime implements Drawable {
    double x, y, px, py, kbx, kby, t, wanderT, wx, wy, flash, deathT;
    int hp = 3;
    final Random rnd = new Random();

    Slime(double x, double y) { this.x = px = x; this.y = py = y; t = rnd.nextDouble() * 5; }

    void savePrev() { px = x; py = y; }
    boolean alive() { return hp > 0; }
    boolean removable() { return hp <= 0 && deathT > 0.3; }
    Rectangle2D.Double feetBox() { return new Rectangle2D.Double(x - 8, y - 6, 16, 6); }
    Rectangle2D.Double hurtBox() { return new Rectangle2D.Double(x - 10, y - 15, 20, 15); }

    void damage(int d, double fromX, double fromY) {
        hp -= d; flash = 0.12;
        double ax = x - fromX, ay = y - fromY, len = Math.max(1, Math.hypot(ax, ay));
        kbx = ax / len * 190; kby = ay / len * 190;
        Fx.spark(x, y - 8); Fx.hitStop(0.06); Fx.shake(1.5, 0.12);
        if (hp <= 0) Fx.poof(x, y - 6);
    }

    void update(double dt, Player p, World w) {
        t += dt;
        if (flash > 0) flash -= dt;
        if (hp <= 0) { deathT += dt; return; }

        double mx = kbx * dt, my = kby * dt;
        kbx *= 0.88; kby *= 0.88;

        double dist = Math.max(1, Math.hypot(p.x - x, p.y - y));
        if (p.state != Player.State.DEAD && dist < 120) {
            mx += (p.x - x) / dist * 38 * dt; my += (p.y - y) / dist * 38 * dt;
        } else {
            wanderT -= dt;
            if (wanderT <= 0) {
                wanderT = 1 + rnd.nextDouble() * 2;
                double ang = rnd.nextDouble() * Math.PI * 2, on = rnd.nextBoolean() ? 1 : 0;
                wx = Math.cos(ang) * 16 * on; wy = Math.sin(ang) * 16 * on;
            }
            mx += wx * dt; my += wy * dt;
        }
        x += mx; if (w.blocked(feetBox())) x -= mx;
        y += my; if (w.blocked(feetBox())) y -= my;

        if (feetBox().intersects(p.feetBox())) p.hurt(1, x, y);
    }

    public double sortY() { return y; }

    public void draw(Graphics2D g, int camX, int camY, double a) {
        int sx = (int) Math.round(px + (x - px) * a) - camX, sy = (int) Math.round(py + (y - py) * a) - camY;
        int f = (int) (t * 3) % 2;
        BufferedImage img = flash > 0 ? Assets.slimeFlash[f] : Assets.slime[f];
        Composite old = g.getComposite();
        if (hp <= 0) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.max(0, 1 - deathT / 0.3)));
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(sx - 9, sy - 4, 18, 7);
        g.drawImage(img, sx - 10, sy - 14, null);
        g.setComposite(old);
    }
}
