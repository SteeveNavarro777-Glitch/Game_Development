/* FILE PURPOSE: COMBAT | Directional shield cone and block feedback.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Whole guard sprites preserve the character; this class adds only impact glints. */
public final class ShieldGuard {
    static BufferedImage[][] bodies;
    private static final Color GLINT=new Color(0xF8E4A2);
    private double flash,cooldown,effectTime;
    // [BLOCK: FUNCTION] ShieldGuard.update - Advance update state with elapsed time.
    void update(double dt){flash=Math.max(0,flash-dt);cooldown=Math.max(0,cooldown-dt);effectTime=(effectTime+dt)%8;}
    // [BLOCK: FUNCTION] ShieldGuard.load - Load or validate load data.
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
    // [BLOCK: FUNCTION] ShieldGuard.faces - Handle faces for ShieldGuard.
    static boolean faces(Player.Dir dir,double dx,double dy){
        return faces(dir,dx,dy,0.5);
    }
    // [BLOCK: EQUIPMENT PROGRESSION] Wider shield tiers lower the directional dot threshold.
    static boolean faces(Player.Dir dir,double dx,double dy,double threshold){
        double distance=Math.hypot(dx,dy);
        if(distance<0.001)return true;
        double forward;
        switch(dir){
            case LEFT:forward=-dx;break;
            case RIGHT:forward=dx;break;
            case UP:forward=-dy;break;
            default:forward=dy;break;
        }
        return forward>=distance*Math.max(-1,Math.min(1,threshold));
    }
    // [BLOCK: FUNCTION] ShieldGuard.block - Handle block for ShieldGuard.
    void block(Player owner){
        flash=0.13;
        if(cooldown>0)return;
        cooldown=0.14;
        Fx.spark(owner.x+HeldItemArt.shieldX(owner.dir)*GameScale.AGYU,owner.y+HeldItemArt.GUARD_SHIELD_Y*GameScale.AGYU);
        Fx.hitStop(0.025);
    }
    // [BLOCK: FUNCTION] ShieldGuard.drawImpact - Render draw impact using the current graphics state.
    void drawImpact(Graphics2D g,int sx,int sy,Player.Dir dir){
        drawImpact(g,sx,sy,dir,EquipmentLoadout.Level.BASIC);
    }
    // [BLOCK: EQUIPMENT VISUAL] Add a small tier emblem without repainting Agyu's original sprite.
    void drawImpact(Graphics2D g,int sx,int sy,Player.Dir dir,EquipmentLoadout.Level level){
        int x=sx+HeldItemArt.shieldX(dir),y=sy+HeldItemArt.GUARD_SHIELD_Y;
        if(level!=EquipmentLoadout.Level.BASIC){
            BufferedImage effect=EquipmentArt.shieldEffect(level,(int)(effectTime*10));
            if(effect!=null){Graphics2D aura=(Graphics2D)g.create();try{
                aura.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                aura.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,flash>0?.92f:.42f));
                aura.drawImage(effect,sx-effect.getWidth()/2,sy-effect.getHeight()+2,null);
            }finally{aura.dispose();}}
        }
        if(flash<=0)return;
        g.setColor(GLINT);
        g.drawLine(x-2,y,x+2,y);g.drawLine(x,y-2,x,y+2);
    }
}
