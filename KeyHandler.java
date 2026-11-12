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
    private final Set<Integer> held=new HashSet<>();
    private int edges;
    private boolean leftMouse,rightMouse;
    public static final class Input {
        public final boolean up,down,left,right,run,guard;
        private final int edges;
        Input(boolean up,boolean down,boolean left,boolean right,boolean run,boolean guard,int edges){
            this.up=up;this.down=down;this.left=left;this.right=right;
            this.run=run;this.guard=guard;this.edges=edges;
        }
        public boolean pressed(int action){return (edges&action)!=0;}
    }
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
    @Override public synchronized void keyReleased(KeyEvent event){held.remove(event.getKeyCode());}
    @Override public synchronized void mousePressed(MouseEvent event){
        if(event.getButton()==MouseEvent.BUTTON1){
            if(!leftMouse)edges|=ATTACK;
            leftMouse=true;
        }else if(event.getButton()==MouseEvent.BUTTON3)rightMouse=true;
    }
    @Override public synchronized void mouseReleased(MouseEvent event){
        if(event.getButton()==MouseEvent.BUTTON1)leftMouse=false;
        else if(event.getButton()==MouseEvent.BUTTON3)rightMouse=false;
    }
    @Override public void mouseClicked(MouseEvent event){}
    @Override public void mouseEntered(MouseEvent event){}
    @Override public void mouseExited(MouseEvent event){}
    private boolean held(int a,int b){return held.contains(a)||held.contains(b);}
    public synchronized Input poll(){
        Input input=new Input(held(KeyEvent.VK_W,KeyEvent.VK_UP),held(KeyEvent.VK_S,KeyEvent.VK_DOWN),
                held(KeyEvent.VK_A,KeyEvent.VK_LEFT),held(KeyEvent.VK_D,KeyEvent.VK_RIGHT),
                held.contains(KeyEvent.VK_SHIFT),rightMouse,edges);
        edges=0;return input;
    }
    /** Keep actions received during hit-stop until gameplay can consume them. */
    public synchronized void defer(Input input,int actions){edges|=input.edges&actions;}
    public synchronized void clear(){held.clear();edges=0;leftMouse=rightMouse=false;}
}
