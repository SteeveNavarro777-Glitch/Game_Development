/* FILE PURPOSE: INPUT | Synchronized keyboard/mouse snapshots and deferred hotbar selection.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

/** EDT input is consumed by the fixed-step simulation, never mutating live actors from listeners. */
public final class KeyHandler extends KeyAdapter implements MouseListener, MouseWheelListener {
    public static final int ATTACK=1,INTERACT=2,HEAL=4,PAUSE=8,MAP=16,
            JOURNAL=32,START=64,RESTART=128,HINT=256,DEBUG=512,JUMP=1024,DASH=2048,USE_ITEM=4096;
    // [SECTION: DSA - HASH SET] Held membership distinguishes a fresh press from autorepeat.
    private final Set<Integer> held=new HashSet<>();
    private int edges,slot=-1,scroll,swapFrom=-1,swapTo=-1;
    private boolean leftMouse,rightMouse;
    public static final class Input {
        public final boolean up,down,left,right,run,guard,primary;
        final int slot,scroll,swapFrom,swapTo;
        private final int edges;
        // [BLOCK: INPUT COMPATIBILITY] KeyHandler.Input - Create movement/combat input without any hotbar commands.
        Input(boolean up,boolean down,boolean left,boolean right,boolean run,boolean guard,int edges){
            this(up,down,left,right,run,guard,edges,false,-1,0,-1,-1);
        }
        // [BLOCK: IMMUTABLE SNAPSHOT] KeyHandler.Input - Freeze held controls, action edges and one frame of item selection.
        Input(boolean up,boolean down,boolean left,boolean right,boolean run,boolean guard,int edges,
                boolean primary,int slot,int scroll,int swapFrom,int swapTo){
            this.up=up;this.down=down;this.left=left;this.right=right;this.run=run;this.guard=guard;
            this.edges=edges;this.primary=primary;this.slot=slot;this.scroll=scroll;this.swapFrom=swapFrom;this.swapTo=swapTo;
        }
        // [BLOCK: INPUT EDGE] KeyHandler.pressed - Test a one-shot action without consuming it twice.
        public boolean pressed(int action){return (edges&action)!=0;}
        // [BLOCK: EQUIPMENT INPUT] KeyHandler.combat - Route the selected slot into spear input or held shield defense.
        Input combat(boolean spear,boolean defending){
            int filtered=spear?edges:edges&~ATTACK;
            if(filtered==edges&&guard==defending)return this;
            return new Input(up,down,left,right,run,defending,filtered,primary,-1,0,-1,-1);
        }
    }
    // [BLOCK: DSA: HASH SET / INPUT EDGES] KeyHandler.keyPressed - Suppress repeat actions and select slots with 1 through 9.
    @Override public synchronized void keyPressed(KeyEvent event){
        int code=event.getKeyCode();if(!held.add(code))return;
        if(code>=KeyEvent.VK_1&&code<=KeyEvent.VK_9){selectSlot(code-KeyEvent.VK_1);return;}
        if(code>=KeyEvent.VK_NUMPAD1&&code<=KeyEvent.VK_NUMPAD9){selectSlot(code-KeyEvent.VK_NUMPAD1);return;}
        switch(code){
            case KeyEvent.VK_SPACE:edges|=JUMP;break;
            case KeyEvent.VK_Q:case KeyEvent.VK_CONTROL:edges|=DASH;break;
            case KeyEvent.VK_J:edges|=ATTACK;break;
            case KeyEvent.VK_F:edges|=USE_ITEM;break;
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
    // [BLOCK: HELD INPUT] KeyHandler.keyReleased - Release a key from held membership.
    @Override public synchronized void keyReleased(KeyEvent event){held.remove(event.getKeyCode());}
    // [BLOCK: MOUSE INPUT] KeyHandler.mousePressed - Keep one primary edge and a held right-click shield input.
    @Override public synchronized void mousePressed(MouseEvent event){
        if(event.getButton()==MouseEvent.BUTTON1){if(!leftMouse)edges|=ATTACK;leftMouse=true;}
        else if(event.getButton()==MouseEvent.BUTTON3)rightMouse=true;
    }
    // [BLOCK: MOUSE INPUT] KeyHandler.mouseReleased - Release mouse state independently of the pointer position.
    @Override public synchronized void mouseReleased(MouseEvent event){
        if(event.getButton()==MouseEvent.BUTTON1)leftMouse=false;
        else if(event.getButton()==MouseEvent.BUTTON3)rightMouse=false;
    }
    // [BLOCK: HOTBAR INPUT] KeyHandler.selectSlot - Queue an exact selection and discard older wheel movement.
    synchronized void selectSlot(int index){if(index<0||index>=Hotbar.SIZE)return;slot=index;scroll=0;}
    // [BLOCK: HOTBAR INPUT] KeyHandler.swapSlots - Queue a completed drag without copying any item counts.
    synchronized void swapSlots(int from,int to){if(from<0||to<0||from>=Hotbar.SIZE||to>=Hotbar.SIZE)return;swapFrom=from;swapTo=to;}
    // [BLOCK: WHEEL INPUT] KeyHandler.mouseWheelMoved - Accumulate bounded wheel movement until the next simulation step.
    @Override public synchronized void mouseWheelMoved(MouseWheelEvent event){scroll=clampScroll((long)scroll+event.getWheelRotation());}
    // [BLOCK: INPUT BOUND] KeyHandler.clampScroll - Bound wheel accumulation to a safe signed integer range.
    private static int clampScroll(long amount){return (int)Math.max(-1000,Math.min(1000,amount));}
    // [BLOCK: LISTENER CONTRACT] KeyHandler.mouseClicked - Leave click handling to fresh press edges.
    @Override public void mouseClicked(MouseEvent event){}
    // [BLOCK: LISTENER CONTRACT] KeyHandler.mouseEntered - Keep controls unchanged when the cursor enters the game.
    @Override public void mouseEntered(MouseEvent event){}
    // [BLOCK: LISTENER CONTRACT] KeyHandler.mouseExited - Keep held controls until release or focus loss.
    @Override public void mouseExited(MouseEvent event){}
    // [BLOCK: HASH SET MEMBERSHIP] KeyHandler.held - Accept WASD or arrow-key movement aliases.
    private boolean held(int a,int b){return held.contains(a)||held.contains(b);}
    // [BLOCK: INPUT SNAPSHOT] KeyHandler.poll - Atomically consume action edges and hotbar commands while preserving held controls.
    public synchronized Input poll(){
        Input input=new Input(held(KeyEvent.VK_W,KeyEvent.VK_UP),held(KeyEvent.VK_S,KeyEvent.VK_DOWN),
                held(KeyEvent.VK_A,KeyEvent.VK_LEFT),held(KeyEvent.VK_D,KeyEvent.VK_RIGHT),
                held.contains(KeyEvent.VK_SHIFT),rightMouse,edges,leftMouse||held.contains(KeyEvent.VK_J),slot,scroll,swapFrom,swapTo);
        edges=scroll=0;slot=swapFrom=swapTo=-1;return input;
    }
    // [BLOCK: DEFERRED INPUT] KeyHandler.defer - Retain consumable, combat and selection edges through hit-stop.
    public synchronized void defer(Input input,int actions){
        edges|=input.edges&actions;
        if(slot<0){slot=input.slot;scroll=clampScroll((long)scroll+input.scroll);}
        if(swapFrom<0){swapFrom=input.swapFrom;swapTo=input.swapTo;}
    }
    // [BLOCK: FOCUS / RESET] KeyHandler.clear - Drop all held inputs, drag commands and pending action edges.
    public synchronized void clear(){held.clear();edges=scroll=0;slot=swapFrom=swapTo=-1;leftMouse=rightMouse=false;}
}
