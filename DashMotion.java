/** Fixed-distance, normalized ground burst. Player owns collision and action priority. */
final class DashMotion {
    static final double SPEED = 720, DURATION = .16, COST = 25, COOLDOWN = .55;
    private double elapsed = DURATION, cooldown, dx, dy;
    boolean active() { return elapsed < DURATION; }
    double cooldown() { return cooldown; }
    double dx() { return dx; }
    double dy() { return dy; }
    void updateCooldown(double dt) { cooldown = Math.max(0, cooldown - dt); }
    boolean ready() { return cooldown <= 1e-9 && !active(); }
    void begin(double x, double y) {
        double length = Math.hypot(x, y);
        if (!ready() || length == 0) throw new IllegalStateException("Dash is not ready");
        dx = x / length; dy = y / length; elapsed = 0; cooldown = COOLDOWN;
    }
    double advance(double dt) {
        double used = Math.max(0, Math.min(dt, DURATION - elapsed));
        elapsed = Math.min(DURATION, elapsed + used);
        return used * SPEED;
    }
    void cancel() { elapsed = DURATION; }
}
