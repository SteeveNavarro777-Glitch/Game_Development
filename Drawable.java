import java.awt.Graphics2D;

/** Anything drawn in the world that must be depth-sorted (player, enemies, trees). */
public interface Drawable {
    double sortY();                                               // feet Y; lower Y is drawn first
    void draw(Graphics2D g, int camX, int camY, double alpha);    // alpha = 0..1 blend between last and current update
}
