import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/** Agyu. Units are art pixels. Smoothness comes from: acceleration, render interpolation, dust, hit-stop, shake. */
public final class Player implements Drawable, Damageable {
    public enum State { IDLE, WALK, RUN, DASH, ATTACK, GUARD, HURT, DEAD }
    public enum Dir { DOWN, UP, LEFT, RIGHT }

    static final double WALK_SPEED = 92, RUN_SPEED = 150;       // art pixels per second
    static final double ACCEL = 900, DECEL = 1300;              // how fast velocity changes (px/s^2)
    static final double ATTACK_TIME = 0.40, HIT_AT = 0.24, HURT_TIME = 0.30;

    double x, y, px, py;                // current and previous feet position (px,py are used for interpolation)
    double vx, vy;                      // current velocity
    Dir dir = Dir.DOWN;
    State state = State.IDLE;
    final int maxHp = 5;
    private int hp = maxHp;
    private final Inventory inventory = new Inventory();
    double stateTime, animTime, invincible, kbx, kby, dustT;
    private final SpearAttack spear = new SpearAttack();
    private final ShieldGuard shield = new ShieldGuard();
    private final JumpMotion jump = new JumpMotion();
    private final EnergyMeter energy = new EnergyMeter();
    private final DashMotion dash = new DashMotion();
    private double attackBuffer,comboGrace;
    private int nextCombo;
    private Dir bufferedDir = Dir.DOWN;

    Player(double x, double y) {
        this.x = px = x; this.y = py = y;
        inventory.add(Inventory.Item.HERB, 3);
    }
    public int health() { return hp; }
    public int maxHealth() { return maxHp; }
    public double energy() { return energy.value(); }
    public double maxEnergy() { return EnergyMeter.MAX; }
    boolean energyExhausted() { return energy.exhausted(); }
    boolean dashing() { return state == State.DASH; }
    double dashCooldown() { return dash.cooldown(); }
    Inventory inventory() { return inventory; }
    boolean useHerb() {
        if (!alive() || hp == maxHp || !inventory.consume(Inventory.Item.HERB)) return false;
        hp = Math.min(maxHp, hp + 2);
        Fx.burst(x, y - 16*GameScale.AGYU, 0xA4E3AE, 10, 35, 0.4, -20, 2);
        return true;
    }

    void savePrev() { px = x; py = y; jump.savePrev(); }
    boolean airborne() { return jump.airborne(); }
    double jumpHeight() { return jump.height(); }
    int comboStep() { return state == State.ATTACK || comboGrace > 0 ? spear.stage() + 1 : 0; }
    String comboName() { return comboStep() == 0 ? "" : SpearAttack.name(spear.stage()); }
    double comboGrace() { return comboGrace; }
    Rectangle2D.Double feetBox() { return new Rectangle2D.Double(x-5*GameScale.AGYU,y-6*GameScale.AGYU,10*GameScale.AGYU,6*GameScale.AGYU); }

    void update(double dt, KeyHandler.Input keys, World world, List<Enemy> enemies) {
        animTime += dt;
        double previousTime = stateTime;
        stateTime += dt;
        invincible = Math.max(0, invincible - dt);
        comboGrace = Math.max(0, comboGrace - dt);
        shield.update(dt);
        dash.updateCooldown(dt);
        if (alive()) energy.update(dt, state != State.RUN && state != State.DASH);
        boolean wasAirborne = jump.airborne();
        if (jump.update(dt) && alive()) Fx.dust(x, y);
        if (state == State.DEAD) {
            resetCombo();
            return;
        }
        if (state == State.HURT) {
            resetCombo();
            move(kbx * dt, kby * dt, world);
            kbx *= Math.exp(-6 * dt); kby *= Math.exp(-6 * dt); vx = vy = 0;
            if (stateTime >= HURT_TIME) setState(State.IDLE);
            return;
        }
        if (state == State.DASH) {
            updateDash(dt, world);
            return; // Inputs during the burst do not leave a queued jump or attack.
        }
        if (keys.pressed(KeyHandler.DASH) && !keys.guard && !wasAirborne && !jump.airborne()
                && dash.ready() && energy.spend(DashMotion.COST)) {
            resetCombo();
            dir = facingFor(keys);
            double dx = (keys.right ? 1 : 0) - (keys.left ? 1 : 0);
            double dy = (keys.down ? 1 : 0) - (keys.up ? 1 : 0);
            if (dx == 0 && dy == 0) { double[] facing = vec(); dx = facing[0]; dy = facing[1]; }
            dash.begin(dx, dy); vx = vy = 0; setState(State.DASH);
            Fx.burst(x, y - 15*GameScale.AGYU, 0x9EF3FF, 10, 65, .2, 0, 1);
            updateDash(dt, world);
            return;
        }
        // Guard wins simultaneous ground inputs, but never snaps an airborne hop down.
        boolean freeToJump = state == State.IDLE || state == State.WALK || state == State.RUN;
        if (keys.pressed(KeyHandler.JUMP) && freeToJump && !keys.guard && !wasAirborne) {
            resetCombo();jump.begin();
        }
        if (jump.airborne()) resetCombo();
        if (keys.guard && !jump.airborne()) {
            // Shield input has priority and cancels any current or queued swing.
            resetCombo();
            if (state != State.GUARD) { vx = vy = 0; setState(State.GUARD); }
            dir = facingFor(keys);
            moveFromInput(dt, keys, world, WALK_SPEED * 0.4);
            return;
        }
        if (state == State.GUARD) setState(State.IDLE);
        if (keys.pressed(KeyHandler.ATTACK) && !jump.airborne()) {
            attackBuffer = 1; // One follow-up slot; it survives until this move can chain.
            bufferedDir = facingFor(keys);
        }
        if (state != State.ATTACK) dir = facingFor(keys);
        if (state == State.ATTACK) {
            double lunge = spear.lungeSpeed(stateTime);
            double[] direction = vec();
            move(direction[0] * lunge * dt, direction[1] * lunge * dt, world);
            vx = vy = 0;
            spear.update(this, previousTime, stateTime, enemies);
            if (attackBuffer > 0 && stateTime >= SpearAttack.chainAt(spear.stage())) {
                beginAttack();
            } else if (stateTime >= spear.duration()) {
                nextCombo = (spear.stage() + 1) % SpearAttack.STAGES;
                spear.cancel();
                setState(State.IDLE);
                comboGrace = .30;
            }
            return;
        }
        if (attackBuffer > 0) {
            beginAttack();
            return;
        }
        boolean sprinting = keys.run && energy.canSprint();
        double beforeX = x, beforeY = y;
        boolean moving = moveFromInput(dt, keys, world, sprinting ? RUN_SPEED : WALK_SPEED);
        boolean traveled = Math.hypot(x - beforeX, y - beforeY) > 1e-6;
        if (moving) {
            if (sprinting && traveled) energy.sprint(dt);
            setState(sprinting && traveled ? State.RUN : State.WALK);
            dustT -= dt;
            if (dustT <= 0 && !jump.airborne()) {
                Fx.dust(x - vx * 0.03, y);
                dustT = state == State.RUN ? 0.09 : 0.22;
            }
        } else if (Math.hypot(vx, vy) < 8) setState(State.IDLE);
    }

    private void updateDash(double dt, World world) {
        double travel = dash.advance(dt);
        // At most three art pixels per collision query, even with a coarse simulation step.
        int steps = Math.max(1, (int) Math.ceil(travel / 3));
        double mx = dash.dx() * travel / steps, my = dash.dy() * travel / steps;
        double trailX = x, trailY = y;
        for (int i = 0; i < steps; i++) {
            double beforeX = x, beforeY = y;
            move(mx, my, world);
            if (Math.hypot(x - trailX, y - trailY) >= 11 || i == steps - 1) {
                BufferedImage[] frames = Assets.run[dir.ordinal()];
                BufferedImage body = frames[(int) (animTime * 30) % frames.length];
                DashEffects.emit(trailX, trailY, x, y, body); trailX = x; trailY = y;
            }
            if (Math.abs(x - beforeX - mx) > 1e-6 || Math.abs(y - beforeY - my) > 1e-6) {
                dash.cancel(); break;
            }
        }
        vx = vy = 0;
        if (!dash.active()) {
            Fx.burst(x, y - 12*GameScale.AGYU, 0x9EF3FF, 5, 34, .16, 0, 1);
            setState(State.IDLE);
        }
    }

    private boolean moveFromInput(double dt, KeyHandler.Input keys, World world, double speed) {
        double dx = (keys.right ? 1 : 0) - (keys.left ? 1 : 0);
        double dy = (keys.down ? 1 : 0) - (keys.up ? 1 : 0);
        boolean moving = dx != 0 || dy != 0;
        double length = Math.max(1, Math.hypot(dx, dy));
        vx = approach(vx, moving ? dx / length * speed : 0, (moving ? ACCEL : DECEL) * dt);
        vy = approach(vy, moving ? dy / length * speed : 0, (moving ? ACCEL : DECEL) * dt);
        move(vx * dt, vy * dt, world);
        return moving;
    }

    private Dir facingFor(KeyHandler.Input keys) {
        int dx = (keys.right ? 1 : 0) - (keys.left ? 1 : 0);
        int dy = (keys.down ? 1 : 0) - (keys.up ? 1 : 0);
        if (dx != 0) return dx < 0 ? Dir.LEFT : Dir.RIGHT;
        if (dy != 0) return dy < 0 ? Dir.UP : Dir.DOWN;
        return dir;
    }

    private void resetCombo() {
        attackBuffer = comboGrace = 0;nextCombo = 0;spear.cancel();
    }

    private void beginAttack() {
        int stage = state == State.ATTACK ? (spear.stage() + 1) % SpearAttack.STAGES :
                comboGrace > 0 ? nextCombo : 0;
        dir = bufferedDir;
        setState(State.ATTACK);
        stateTime = 0;
        attackBuffer = comboGrace = 0;
        vx = vy = 0;
        spear.begin(stage);
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
        return spear.bounds(x, y, dir, stateTime);
    }

    boolean blocksFrom(double fromX, double fromY) {
        return state == State.GUARD && alive() && ShieldGuard.faces(dir, fromX - x, fromY - y);
    }

    public void damage(int dmg, double fromX, double fromY) {
        if (dmg <= 0 || state == State.DEAD || state == State.DASH) return;
        if (blocksFrom(fromX, fromY)) { shield.block(this); return; }
        if (invincible > 0) return;
        resetCombo();
        hp -= dmg; invincible = 1.0;
        Fx.shake(3, 0.25); Fx.hitStop(0.07);
        if (hp <= 0) { hp = 0; state = State.DEAD; stateTime = 0; return; }
        state = State.HURT; stateTime = 0;
        double ax = x - fromX, ay = y - fromY, len = Math.max(1, Math.hypot(ax, ay));
        kbx = ax / len * 210; kby = ay / len * 210;
    }

    public double sortY() { return y; }

    public void draw(Graphics2D graphics, int camX, int camY, double alpha) {
        int sx = (int) Math.round(px + (x - px) * alpha) - camX;
        int sy = (int) Math.round(py + (y - py) * alpha) - camY;
        Graphics2D g=GameScale.agyuGraphics(graphics,sx,sy);
        try{
        sx=sy=0;
        g.setColor(new Color(0, 0, 0, 70));
        g.fillOval(sx - 11, sy - 4, 22, 8);
        // The shadow and collision stay on the ground; all held gear rises with Agyu.
        sy -= (int) Math.round(jump.renderHeight(alpha));
        int facing = dir.ordinal();
        boolean side = dir == Dir.LEFT || dir == Dir.RIGHT;
        BufferedImage image;
        BufferedImage[] sequence;
        int bob = 0;
        switch (state) {
            case DEAD:
                image = Assets.die[facing];
                break;
            case HURT:
                sequence = Assets.hurt[facing];
                image = sequence[Math.min(sequence.length - 1, (int) (stateTime / HURT_TIME * sequence.length))];
                break;
            case ATTACK:
                if (CombatBody.ready()) image = CombatBody.frame(dir, spear.stage(), stateTime).image();
                else {
                    int bodyFacing = SpearAttack.bodyDirection(dir, stateTime, spear.stage()).ordinal();
                    sequence = Assets.attack[bodyFacing];
                    image = sequence[Math.min(sequence.length - 1, (int) (stateTime / spear.duration() * sequence.length))];
                }
                break;
            case GUARD:
                sequence = ShieldGuard.bodies[facing];
                image = sequence[(int) (animTime * 4) % sequence.length];
                break;
            case DASH:
                sequence = Assets.run[facing];
                image = sequence[(int) (animTime * 30) % sequence.length];
                break;
            case RUN:
                sequence = Assets.run[facing];
                image = sequence[(int) (animTime * (side ? 24 : 5)) % sequence.length];
                bob = side ? 0 : (int) (Math.abs(Math.sin(animTime * 14)) * 2.4);
                break;
            case WALK:
                sequence = Assets.walk[facing];
                image = sequence[(int) (animTime * (side ? 16 : 5)) % sequence.length];
                bob = side ? 0 : (int) (Math.abs(Math.sin(animTime * 9)) * 1.9);
                break;
            default:
                sequence = Assets.idle[facing];
                image = sequence[(int) (animTime * 5) % sequence.length];
        }
        if (jump.airborne()) bob = 0;
        Composite original = g.getComposite();
        if (invincible > 0 && state != State.DEAD && ((int) (invincible * 14)) % 2 == 0)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        // Weapon angle determines layering during both directional cuts and the full turn.
        boolean attacking = state == State.ATTACK;
        if (attacking && spear.behindBody(dir, stateTime)) spear.draw(g, sx, sy, dir, stateTime);
        int leanX = attacking ? SpearAttack.bodyOffsetX(dir, stateTime, spear.stage()) : 0;
        int leanY = attacking ? SpearAttack.bodyOffsetY(dir, stateTime, spear.stage()) : 0;
        g.drawImage(image, sx - image.getWidth() / 2 + leanX, sy - image.getHeight() + 1 - bob + leanY, null);
        if (attacking && !spear.behindBody(dir, stateTime)) spear.draw(g, sx, sy, dir, stateTime);
        if (state == State.GUARD) shield.drawImpact(g, sx, sy, dir);
        g.setComposite(original);
        }finally{g.dispose();}
    }
}
