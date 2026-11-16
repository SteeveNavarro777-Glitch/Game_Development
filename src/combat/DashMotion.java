/* FILE PURPOSE: MOVEMENT | Normalized dash burst, duration and cooldown.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
/** Fixed-distance, normalized ground burst. Player owns collision and action priority. */
final class DashMotion {
    static final double SPEED = 720, DURATION = .16, COST = 25, COOLDOWN = .55;
    private double elapsed = DURATION, cooldown, dx, dy;
    // [BLOCK: FUNCTION] DashMotion.active - Handle active for DashMotion.
    boolean active() { return elapsed < DURATION; }
    // [BLOCK: FUNCTION] DashMotion.cooldown - Handle cooldown for DashMotion.
    double cooldown() { return cooldown; }
    // [BLOCK: FUNCTION] DashMotion.dx - Handle dx for DashMotion.
    double dx() { return dx; }
    // [BLOCK: FUNCTION] DashMotion.dy - Handle dy for DashMotion.
    double dy() { return dy; }
    // [BLOCK: FUNCTION] DashMotion.updateCooldown - Advance update cooldown state with elapsed time.
    void updateCooldown(double dt) { cooldown = Math.max(0, cooldown - dt); }
    // [BLOCK: FUNCTION] DashMotion.ready - Load or validate ready data.
    boolean ready() { return cooldown <= 1e-9 && !active(); }
    // [BLOCK: MOVEMENT: DASH] DashMotion.begin - Start a normalized burst and its cooldown.
    void begin(double x, double y) {
        double length = Math.hypot(x, y);
        if (!ready() || length == 0) throw new IllegalStateException("Dash is not ready");
        dx = x / length; dy = y / length; elapsed = 0; cooldown = COOLDOWN;
    }
    // [BLOCK: FUNCTION] DashMotion.advance - Handle advance for DashMotion.
    double advance(double dt) {
        double used = Math.max(0, Math.min(dt, DURATION - elapsed));
        elapsed = Math.min(DURATION, elapsed + used);
        return used * SPEED;
    }
    // [BLOCK: FUNCTION] DashMotion.cancel - Handle cancel for DashMotion.
    void cancel() { elapsed = DURATION; }
}
