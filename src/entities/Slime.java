/* FILE PURPOSE: OOP INHERITANCE | Ordinary enemy subclass with its own combat stats.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
/** Ordinary shrine guard; inherits navigation and health from Enemy. */
// [SECTION: OOP - INHERITANCE] Reuse Enemy lifecycle; overridden methods specialize this enemy.
public final class Slime extends Enemy {
    // [BLOCK: FUNCTION] Slime.Slime - Initialize Slime state.
    Slime(double x,double y,int shrineGroup){super(x,y,3,shrineGroup);}
    // [BLOCK: OOP: OVERRIDING] Slime.speed - Supply the ordinary enemy movement speed.
    @Override protected double speed(){return 42;}
    // [BLOCK: OOP: OVERRIDING] Slime.contactDamage - Supply the ordinary enemy contact damage.
    @Override protected int contactDamage(){return 1;}
}
