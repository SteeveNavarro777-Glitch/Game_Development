/* FILE PURPOSE: OOP ENCAPSULATION | Blue potion shield capacity and damage absorption.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
/** Potion protection is separate from the directional wooden shield. */
final class ShieldMeter {
    static final int MAX=3;
    private int value;
    // [BLOCK: RESOURCE READ] ShieldMeter.value - Read remaining potion protection.
    int value() { return value; }
    // [BLOCK: RESOURCE RESTORE] ShieldMeter.restore - Fill protection to its fixed capacity.
    void restore() { value=MAX; }
    // [BLOCK: DAMAGE ABSORPTION] ShieldMeter.absorb - Spend protection first and return only damage that reaches health.
    int absorb(int damage) {
        if(damage<=0)return 0;
        int spent=Math.min(value,damage); value-=spent; return damage-spent;
    }
}
