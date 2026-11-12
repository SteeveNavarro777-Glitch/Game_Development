/** Encapsulated sprint/dash resource with delayed recovery and exhaustion hysteresis. */
final class EnergyMeter {
    static final double MAX = 100, SPRINT_DRAIN = 25, REGEN = 22, REGEN_DELAY = .8, RESUME_AT = 20;
    private double value = MAX, recoveryDelay;
    private boolean exhausted;

    double value() { return value; }
    boolean exhausted() { return exhausted; }
    boolean canSprint() { return !exhausted && value > 1e-9; }
    boolean spend(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException("Invalid energy cost");
        if (value + 1e-9 < amount) return false;
        value = Math.max(0, value - amount);
        recoveryDelay = REGEN_DELAY;
        if (value <= 1e-9) { value = 0; exhausted = true; }
        return true;
    }
    void sprint(double dt) {
        if (dt <= 0 || !canSprint()) return;
        spend(Math.min(value, SPRINT_DRAIN * dt));
    }
    void update(double dt, boolean recover) {
        if (dt <= 0) return;
        double rest = Math.max(0, dt - recoveryDelay);
        recoveryDelay = Math.max(0, recoveryDelay - dt);
        if (recover) value = Math.min(MAX, value + REGEN * rest);
        if (value >= RESUME_AT) exhausted = false;
    }
}
