/* FILE PURPOSE: OOP INTERFACE | Shared health, alive and controlled damage contract.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
/** A focused combat contract shared by Agyu and enemies. */
public interface Damageable {
    // [BLOCK: OOP: ABSTRACTION] Damageable.damage - Define controlled damage without exposing health storage.
    void damage(int amount, double fromX, double fromY);
    // [BLOCK: OOP: ABSTRACTION] Damageable.health - Require damageable objects to expose current health.
    int health();
    // [BLOCK: OOP: ABSTRACTION] Damageable.maxHealth - Require damageable objects to expose maximum health.
    int maxHealth();
    // [BLOCK: OOP: DEFAULT METHOD] Damageable.alive - Share the alive rule through the health contract.
    default boolean alive() { return health() > 0; }
}
