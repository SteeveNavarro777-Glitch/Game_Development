/* FILE PURPOSE: INPUT | Keyboard/mouse snapshots, held-key hash set and press edges.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

/** Keyboard and mouse share one synchronized input snapshot. */
public final class KeyHandler extends KeyAdapter implements MouseListener {
    public static final int ATTACK=1,INTERACT=2,HEAL=4,PAUSE=8,MAP=16,
            JOURNAL=32,START=64,RESTART=128,HINT=256,DEBUG=512,JUMP=1024,DASH=2048;
    // [SECTION: DSA - HASH SET] Held membership distinguishes a fresh key press from autorepeat.
    private final Set<Integer> held=new HashSet<>();
    private int edges;
    private boolean leftMouse,rightMouse;
    public static final class Input {
        public final boolean up,down,left,right,run,guard;
        private final int edges;
        // [BLOCK: FUNCTION] KeyHandler.Input - Initialize Input state.
        Input(boolean up,boolean down,boolean left,boolean right,boolean run,boolean guard,int edges){
            this.up=up;this.down=down;this.left=left;this.right=right;
            this.run=run;this.guard=guard;this.edges=edges;
        }
        // [BLOCK: FUNCTION] KeyHandler.pressed - Handle pressed for KeyHandler.
        public boolean pressed(int action){return (edges&action)!=0;}
    }
    // [BLOCK: DSA: HASH SET / INPUT EDGES] KeyHandler.keyPressed - Add a held key once and suppress automatic-repeat action edges.
    @Override public synchronized void keyPressed(KeyEvent event){
        int code=event.getKeyCode();
        if(!held.add(code))return;
        switch(code){
            case KeyEvent.VK_SPACE:edges|=JUMP;break;
            case KeyEvent.VK_Q:case KeyEvent.VK_CONTROL:edges|=DASH;break;
            case KeyEvent.VK_J:edges|=ATTACK;break;
            case KeyEvent.VK_E:edges|=INTERACT;break;
            case KeyEvent.VK_H:edges|=HEAL;break;
            case KeyEvent.VK_ESCAPE:case KeyEvent.VK_P:edges|=PAUSE;break;
            case KeyEvent.VK_M:edges|=MAP;break;
            case KeyEvent.VK_TAB:edges|=JOURNAL;break;
            case KeyEvent.VK_ENTER:edges|=START;break;
            case KeyEvent.VK_R:edges|=RESTART;break;
            case KeyEvent.VK_N:edges|=HINT;break;
            case KeyEvent.VK_F1:edges|=DEBUG;break;
            default:break;
        }
    }
    // [BLOCK: DSA: HASH SET] KeyHandler.keyReleased - Remove the released key from held membership.
    @Override public synchronized void keyReleased(KeyEvent event){held.remove(event.getKeyCode());}
    // [BLOCK: FUNCTION] KeyHandler.mousePressed - Handle mouse pressed for KeyHandler.
    @Override public synchronized void mousePressed(MouseEvent event){
        if(event.getButton()==MouseEvent.BUTTON1){
            if(!leftMouse)edges|=ATTACK;
            leftMouse=true;
        }else if(event.getButton()==MouseEvent.BUTTON3)rightMouse=true;
    }
    // [BLOCK: FUNCTION] KeyHandler.mouseReleased - Handle mouse released for KeyHandler.
    @Override public synchronized void mouseReleased(MouseEvent event){
        if(event.getButton()==MouseEvent.BUTTON1)leftMouse=false;
        else if(event.getButton()==MouseEvent.BUTTON3)rightMouse=false;
    }
    // [BLOCK: FUNCTION] KeyHandler.mouseClicked - Handle mouse clicked for KeyHandler.
    @Override public void mouseClicked(MouseEvent event){}
    // [BLOCK: FUNCTION] KeyHandler.mouseEntered - Handle mouse entered for KeyHandler.
    @Override public void mouseEntered(MouseEvent event){}
    // [BLOCK: FUNCTION] KeyHandler.mouseExited - Handle mouse exited for KeyHandler.
    @Override public void mouseExited(MouseEvent event){}
    // [BLOCK: FUNCTION] KeyHandler.held - Handle held for KeyHandler.
    private boolean held(int a,int b){return held.contains(a)||held.contains(b);}
    // [BLOCK: INPUT: SNAPSHOT] KeyHandler.poll - Return held controls and consume one-shot action edges atomically.
    public synchronized Input poll(){
        Input input=new Input(held(KeyEvent.VK_W,KeyEvent.VK_UP),held(KeyEvent.VK_S,KeyEvent.VK_DOWN),
                held(KeyEvent.VK_A,KeyEvent.VK_LEFT),held(KeyEvent.VK_D,KeyEvent.VK_RIGHT),
                held.contains(KeyEvent.VK_SHIFT),rightMouse,edges);
        edges=0;return input;
    }
    /** Keep actions received during hit-stop until gameplay can consume them. */
    // [BLOCK: FUNCTION] KeyHandler.defer - Handle defer for KeyHandler.
    public synchronized void defer(Input input,int actions){edges|=input.edges&actions;}
    // [BLOCK: FUNCTION] KeyHandler.clear - Handle clear for KeyHandler.
    public synchronized void clear(){held.clear();edges=0;leftMouse=rightMouse=false;}
}
