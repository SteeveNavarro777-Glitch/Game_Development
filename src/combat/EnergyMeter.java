/* FILE PURPOSE: OOP ENCAPSULATION | Validated energy spending, recovery and exhaustion.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
/** Encapsulated sprint/dash resource with delayed recovery and exhaustion hysteresis. */
final class EnergyMeter {
    static final double MAX = 100, SPRINT_DRAIN = 25, REGEN = 22, REGEN_DELAY = .8, RESUME_AT = 20;
    private double value = MAX, recoveryDelay;
    private boolean exhausted;

    // [BLOCK: FUNCTION] EnergyMeter.value - Handle value for EnergyMeter.
    double value() { return value; }
    // [BLOCK: FUNCTION] EnergyMeter.exhausted - Handle exhausted for EnergyMeter.
    boolean exhausted() { return exhausted; }
    // [BLOCK: FUNCTION] EnergyMeter.canSprint - Handle can sprint for EnergyMeter.
    boolean canSprint() { return !exhausted && value > 1e-9; }
    // [BLOCK: OOP: ENCAPSULATION] EnergyMeter.spend - Spend energy only when the requested amount is valid and available.
    boolean spend(double amount) {
        if (!Double.isFinite(amount) || amount <= 0) throw new IllegalArgumentException("Invalid energy cost");
        if (value + 1e-9 < amount) return false;
        value = Math.max(0, value - amount);
        recoveryDelay = REGEN_DELAY;
        if (value <= 1e-9) { value = 0; exhausted = true; }
        return true;
    }
    // [BLOCK: RESOURCE RESTORE] EnergyMeter.restore - Clamp potion recovery and release exhaustion when enough energy returns.
    void restore(double amount) {
        if(!Double.isFinite(amount)||amount<=0)throw new IllegalArgumentException("Invalid energy recovery");
        value=Math.min(MAX,value+amount);recoveryDelay=0;
        if(value>=RESUME_AT)exhausted=false;
    }
    // [BLOCK: RESOURCE: SPRINT] EnergyMeter.sprint - Spend energy for the time actually spent moving.
    void sprint(double dt) {
        if (dt <= 0 || !canSprint()) return;
        spend(Math.min(value, SPRINT_DRAIN * dt));
    }
    // [BLOCK: OOP: ENCAPSULATION] EnergyMeter.update - Apply delayed recovery and release exhaustion at the resume threshold.
    void update(double dt, boolean recover) {
        if (dt <= 0) return;
        double rest = Math.max(0, dt - recoveryDelay);
        recoveryDelay = Math.max(0, recoveryDelay - dt);
        if (recover) value = Math.min(MAX, value + REGEN * rest);
        if (value >= RESUME_AT) exhausted = false;
    }
}
