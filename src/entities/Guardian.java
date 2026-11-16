/* FILE PURPOSE: OOP INHERITANCE | Boss subclass with overridden stats, pulse update and drawing.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Graphics2D;

/** A stronger enemy with a visible warning before its close-range pulse. */
// [SECTION: OOP - INHERITANCE] Reuse Enemy lifecycle; overridden methods specialize this enemy.
public final class Guardian extends Enemy {
    private double pulseTime;
    // [BLOCK: FUNCTION] Guardian.Guardian - Initialize Guardian state.
    Guardian(double x,double y){super(x,y,18,-1);}
    // [BLOCK: OOP: OVERRIDING] Guardian.speed - Supply the guardian movement speed.
    @Override protected double speed(){return pulseTime>3.0?0:47;}
    // [BLOCK: OOP: OVERRIDING] Guardian.contactDamage - Supply the guardian contact damage.
    @Override protected int contactDamage(){return 2;}
    // [BLOCK: OOP: OVERRIDING] Guardian.scale - Supply the guardian body/hurt-box scale.
    @Override protected double scale(){return 1.8;}
    // [BLOCK: OOP: OVERRIDING] Guardian.update - Extend shared enemy behavior with the guardian pulse attack.
    @Override void update(double dt,Player player,World world,int[][] flow){
        refreshNightPower(world);
        if(alive()&&player.alive()){
            pulseTime+=dt;
            if(pulseTime>=4){
                pulseTime=0;Fx.shake(2,0.2);
                if(Math.hypot(player.x-x,player.y-y)<62)player.damage(attackDamage(2),x,y);
                Fx.burst(x,y-8,0xFFB85A,16,100,0.35,0,2);
            }
        }
        super.update(dt,player,world,flow);
    }
    // [BLOCK: OOP: OVERRIDING] Guardian.draw - Add the pulse warning and boss appearance to shared enemy drawing.
    @Override public void draw(Graphics2D g,int cx,int cy,double alpha){
        if(alive()&&pulseTime>3.0){
            g.setColor(new Color(255,157,73,100+(int)((pulseTime-3)*100)));
            g.drawOval((int)x-cx-62,(int)y-cy-62,124,124);
            g.drawOval((int)x-cx-61,(int)y-cy-61,122,122);
        }
        super.draw(g,cx,cy,alpha);
    }
}
