import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** Remembers which keys are held down. The game loop reads these flags every frame. */
public class KeyHandler extends KeyAdapter {
    public boolean up, down, left, right, run, restart;
    public boolean attackPressed;          // true for ONE press (consumed by the player)
    private boolean attackHeld;

    @Override public void keyPressed(KeyEvent e) { set(e.getKeyCode(), true); }
    @Override public void keyReleased(KeyEvent e) { set(e.getKeyCode(), false); }

    private void set(int code, boolean down) {
        switch (code) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    up = down; break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  this.down = down; break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  left = down; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: right = down; break;
            case KeyEvent.VK_SHIFT: run = down; break;
            case KeyEvent.VK_R: restart = down; break;
            case KeyEvent.VK_SPACE: case KeyEvent.VK_J:
                if (down && !attackHeld) attackPressed = true;   // ignore key auto-repeat
                attackHeld = down;
                break;
        }
    }
}
