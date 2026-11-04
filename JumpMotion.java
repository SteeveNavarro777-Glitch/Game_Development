/** A ground-relative jump arc composed with normal movement and combat states. */
final class JumpMotion {
    static final double DURATION = 0.54, PEAK = 18;
    private boolean airborne;
    private double elapsed, height, previousHeight;

    boolean begin() {
        if (airborne) return false;
        airborne = true;
        elapsed = height = previousHeight = 0;
        return true;
    }
    void savePrev() { previousHeight = height; }
    boolean airborne() { return airborne; }
    double height() { return height; }
    double renderHeight(double alpha) {
        return previousHeight + (height - previousHeight) * Math.max(0, Math.min(1, alpha));
    }
    /** Returns true exactly once on landing. Ground x/y and colliders are unchanged. */
    boolean update(double dt) {
        if (!airborne || dt <= 0) return false;
        elapsed = Math.min(DURATION, elapsed + dt);
        double progress = elapsed / DURATION;
        height = 4 * PEAK * progress * (1 - progress);
        if (elapsed >= DURATION) {
            airborne = false;
            height = 0;
            return true;
        }
        return false;
    }
}