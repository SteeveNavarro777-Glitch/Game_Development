/* FILE PURPOSE: OOP INTERFACE | Shared depth-sort and render contract.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Graphics2D;

/** Anything drawn in the world that must be depth-sorted (player, enemies, trees). */
public interface Drawable {
    // [BLOCK: OOP: ABSTRACTION] Drawable.sortY - Supply the world depth used by the shared draw-list comparator.
    double sortY();                                               // feet Y; lower Y is drawn first
    // [BLOCK: OOP: ABSTRACTION] Drawable.draw - Define the render operation implemented by each visible object.
    void draw(Graphics2D g, int camX, int camY, double alpha);    // alpha = 0..1 blend between last and current update
}
