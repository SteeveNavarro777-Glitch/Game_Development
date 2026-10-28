import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/** Agyu. Units are art pixels. Smoothness comes from: acceleration, render interpolation, dust, hit-stop, shake. */
public class Player implements Drawable {
    public enum State { IDLE, WALK, RUN, ATTACK, HURT, DEAD }
    public enum Dir { DOWN, UP, LEFT, RIGHT }

    static final double WALK_SPEED = 92, RUN_SPEED = 150;       // art pixels per second
    static final double ACCEL = 900, DECEL = 1300;              // how fast velocity changes (px/s^2)
    static final double ATTACK_TIME = 0.36, HIT_AT = 0.24, HURT_TIME = 0.30;

    double x, y, px, py;                // current and previous feet position (px,py are used for interpolation)
    double vx, vy;                      // current velocity
    Dir dir = Dir.DOWN;
    State state = State.IDLE;
    final int maxHp = 5;
    int hp = maxHp;
    double stateTime, animTime, invincible, kbx, kby, dustT;
    boolean hitDone;

    Player(double x, double y) { this.x = px = x; this.y = py = y; }

    void savePrev() { px = x; py = y; }
    Rectangle2D.Double feetBox() { return new Rectangle2D.Double(x - 5, y - 6, 10, 6); }

    void update(double dt, KeyHandler k, World world, List<Slime> slimes) {
        animTime += dt; stateTime += dt;
        if (invincible > 0) invincible -= dt;
        boolean wantAttack = k.attackPressed; k.attackPressed = false;

        switch (state) {
            case DEAD: return;
            case HURT:
                move(kbx * dt, kby * dt, world); kbx *= 0.9; kby *= 0.9; vx = vy = 0;
                if (stateTime >= HURT_TIME) setState(State.IDLE);
                return;
            case ATTACK:
                if (!hitDone && stateTime >= HIT_AT) { hitDone = true; strike(slimes); }
                double lunge = (stateTime >= 0.12 && stateTime < HIT_AT + 0.06) ? 40 : 0;   // small step into the thrust
                double[] d = vec(); move(d[0] * lunge * dt, d[1] * lunge * dt, world); vx = vy = 0;
                if (stateTime >= ATTACK_TIME) setState(State.IDLE);
                return;
            default: break;
        }

        if (wantAttack) { setState(State.ATTACK); hitDone = false; return; }

        double dx = (k.right ? 1 : 0) - (k.left ? 1 : 0), dy = (k.down ? 1 : 0) - (k.up ? 1 : 0);
        boolean moving = dx != 0 || dy != 0;
        double sp = k.run ? RUN_SPEED : WALK_SPEED, len = Math.max(1, Math.hypot(dx, dy));
        vx = approach(vx, moving ? dx / len * sp : 0, (moving ? ACCEL : DECEL) * dt);   // ease in / ease out
        vy = approach(vy, moving ? dy / len * sp : 0, (moving ? ACCEL : DECEL) * dt);
        move(vx * dt, vy * dt, world);

        if (moving) {
            if (dx != 0) dir = dx < 0 ? Dir.LEFT : Dir.RIGHT; else dir = dy < 0 ? Dir.UP : Dir.DOWN;
            setState(k.run ? State.RUN : State.WALK);
            dustT -= dt;
            if (dustT <= 0) { Fx.dust(x - vx * 0.03, y); dustT = k.run ? 0.09 : 0.22; }    // footstep puffs
        } else if (Math.hypot(vx, vy) < 8) setState(State.IDLE);
    }

    private static double approach(double v, double target, double step) {
        return v < target ? Math.min(target, v + step) : Math.max(target, v - step);
    }

    private void setState(State s) { if (state != s) { state = s; stateTime = 0; } }

    private void move(double mx, double my, World w) {
        x += mx; if (w.blocked(feetBox())) { x -= mx; vx = 0; }
        y += my; if (w.blocked(feetBox())) { y -= my; vy = 0; }
    }

    private double[] vec() {
        switch (dir) {
            case UP: return new double[]{0, -1};
            case DOWN: return new double[]{0, 1};
            case LEFT: return new double[]{-1, 0};
            default: return new double[]{1, 0};
        }
    }

    Rectangle2D.Double attackBox() {
        switch (dir) {
            case RIGHT: return new Rectangle2D.Double(x + 4, y - 28, 34, 24);
            case LEFT:  return new Rectangle2D.Double(x - 38, y - 28, 34, 24);
            case UP:    return new Rectangle2D.Double(x - 11, y - 46, 22, 34);
            default:    return new Rectangle2D.Double(x - 11, y - 3, 22, 32);
        }
    }

    private void strike(List<Slime> slimes) {
        Rectangle2D.Double box = attackBox();
        for (Slime s : slimes) if (s.alive() && box.intersects(s.hurtBox())) { s.damage(1, x, y); }
    }

    void hurt(int dmg, double fromX, double fromY) {
        if (invincible > 0 || state == State.DEAD) return;
        hp -= dmg; invincible = 1.0;
        Fx.shake(3, 0.25); Fx.hitStop(0.07);
        if (hp <= 0) { hp = 0; state = State.DEAD; stateTime = 0; return; }
        state = State.HURT; stateTime = 0;
        double ax = x - fromX, ay = y - fromY, len = Math.max(1, Math.hypot(ax, ay));
        kbx = ax / len * 210; kby = ay / len * 210;
    }

    public double sortY() { return y; }

    public void draw(Graphics2D g, int camX, int camY, double a) {
        // interpolate between the previous and current update so motion is smooth at any refresh rate
        int sx = (int) Math.round(px + (x - px) * a) - camX, sy = (int) Math.round(py + (y - py) * a) - camY;
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(sx - 11, sy - 4, 22, 8);

        int side = dir == Dir.LEFT ? 1 : 0;
        boolean sideView = dir == Dir.LEFT || dir == Dir.RIGHT;
        BufferedImage vert = dir == Dir.UP ? Assets.back : Assets.front;
        BufferedImage img; int bob = 0;

        switch (state) {
            case DEAD:   img = Assets.die[side]; break;
            case HURT:   img = Assets.hurt[side][(int) (stateTime * 10) % 2]; break;
            case ATTACK: img = sideView ? Assets.attack[side][Math.min(2, (int) (stateTime / (ATTACK_TIME / 3)))] : vert; break;
            case RUN:    img = sideView ? Assets.run[side][(int) (animTime * 12) % 4] : vert;
                         bob = sideView ? 0 : (int) (Math.abs(Math.sin(animTime * 14)) * 2.4); break;
            case WALK:   img = sideView ? Assets.walk[side][(int) (animTime * 8) % 4] : vert;
                         bob = sideView ? 0 : (int) (Math.abs(Math.sin(animTime * 9)) * 1.9); break;
            default:     img = sideView ? Assets.idle[side][(int) (animTime * 4) % 3] : vert;
        }

        Composite old = g.getComposite();
        if (invincible > 0 && state != State.DEAD && ((int) (invincible * 14)) % 2 == 0)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        g.drawImage(img, sx - img.getWidth() / 2, sy - img.getHeight() + 1 - bob, null);
        g.setComposite(old);

        if (state == State.ATTACK && !sideView && stateTime >= HIT_AT - 0.04) {       // vertical thrust streak
            Rectangle2D.Double b = attackBox();
            g.setColor(new Color(235, 240, 250, 190));
            g.fillRect((int) (b.x + b.width / 2) - 1 - camX, (int) b.y - camY, 2, (int) b.height);
        }
    }
}
