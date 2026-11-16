/* FILE PURPOSE: MOVEMENT | Jump height, landing and interpolated presentation.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
/** A ground-relative jump arc composed with normal movement and combat states. */
final class JumpMotion {
    static final double DURATION = 0.54, PEAK = 18;
    private boolean airborne;
    private double elapsed, height, previousHeight;

    // [BLOCK: FUNCTION] JumpMotion.begin - Handle begin for JumpMotion.
    boolean begin() {
        if (airborne) return false;
        airborne = true;
        elapsed = height = previousHeight = 0;
        return true;
    }
    // [BLOCK: FUNCTION] JumpMotion.savePrev - Handle save prev for JumpMotion.
    void savePrev() { previousHeight = height; }
    // [BLOCK: FUNCTION] JumpMotion.airborne - Handle airborne for JumpMotion.
    boolean airborne() { return airborne; }
    // [BLOCK: FUNCTION] JumpMotion.height - Handle height for JumpMotion.
    double height() { return height; }
    // [BLOCK: FUNCTION] JumpMotion.renderHeight - Handle render height for JumpMotion.
    double renderHeight(double alpha) {
        return previousHeight + (height - previousHeight) * Math.max(0, Math.min(1, alpha));
    }
    /** Returns true exactly once on landing. Ground x/y and colliders are unchanged. */
    // [BLOCK: MOVEMENT: JUMP] JumpMotion.update - Advance the hop timer and settle at landing.
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