/** A focused combat contract shared by Agyu and enemies. */
public interface Damageable {
    void damage(int amount, double fromX, double fromY);
    int health();
    int maxHealth();
    default boolean alive() { return health() > 0; }
}
