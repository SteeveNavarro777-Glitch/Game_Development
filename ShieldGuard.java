import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Whole guard sprites preserve the character; this class adds only impact glints. */
public final class ShieldGuard {
    static BufferedImage[][] bodies;
    private static final int[][] IMPACT={{-8,-10},{-12,-31},{-17,-18},{17,-18}};
    private static final Color GLINT=new Color(0xF8E4A2);
    private double flash,cooldown;
    void update(double dt){flash=Math.max(0,flash-dt);cooldown=Math.max(0,cooldown-dt);}
    static void load(BufferedImage[] completePoses){
        if(completePoses.length!=4)throw new IllegalArgumentException("Four guard directions required");
        bodies=new BufferedImage[4][1];
        for(int dir=0;dir<4;dir++){
            BufferedImage pose=completePoses[dir];
            if(pose==null||pose.getWidth()!=64||pose.getHeight()!=64)
                throw new IllegalArgumentException("Guard sprite must be a complete 64px frame");
            // Do not mask or recolor any part of Agyu's torso, limbs, gear or face.
            bodies[dir][0]=pose;
        }
    }
    static boolean faces(Player.Dir dir,double dx,double dy){
        double distance=Math.hypot(dx,dy);
        if(distance<0.001)return true;
        double forward;
        switch(dir){
            case LEFT:forward=-dx;break;
            case RIGHT:forward=dx;break;
            case UP:forward=-dy;break;
            default:forward=dy;break;
        }
        return forward>=distance*0.5;
    }
    void block(Player owner){
        flash=0.13;
        if(cooldown>0)return;
        cooldown=0.14;
        int[] point=impact(owner.dir);
        Fx.spark(owner.x+point[0]*GameScale.AGYU,owner.y+point[1]*GameScale.AGYU);
        Fx.hitStop(0.025);
    }
    private static int[] impact(Player.Dir dir){return IMPACT[dir.ordinal()];}
    void drawImpact(Graphics2D g,int sx,int sy,Player.Dir dir){
        if(flash<=0)return;
        int[] point=impact(dir);
        int x=sx+point[0],y=sy+point[1];
        g.setColor(GLINT);
        g.drawLine(x-2,y,x+2,y);g.drawLine(x,y-2,x,y+2);
    }
}
