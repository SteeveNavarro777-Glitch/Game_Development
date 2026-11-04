import java.awt.Color;
import java.awt.Graphics2D;

/** A stronger enemy with a visible warning before its close-range pulse. */
public final class Guardian extends Enemy {
    private double pulseTime;
    Guardian(double x,double y){super(x,y,18,-1);}
    @Override protected double speed(){return pulseTime>3.0?0:47;}
    @Override protected int contactDamage(){return 2;}
    @Override protected double scale(){return 1.8;}
    @Override void update(double dt,Player player,World world,int[][] flow){
        if(alive()&&player.alive()){
            pulseTime+=dt;
            if(pulseTime>=4){
                pulseTime=0;Fx.shake(2,0.2);
                if(Math.hypot(player.x-x,player.y-y)<62)player.damage(2,x,y);
                Fx.burst(x,y-8,0xFFB85A,16,100,0.35,0,2);
            }
        }
        super.update(dt,player,world,flow);
    }
    @Override public void draw(Graphics2D g,int cx,int cy,double alpha){
        if(alive()&&pulseTime>3.0){
            g.setColor(new Color(255,157,73,100+(int)((pulseTime-3)*100)));
            g.drawOval((int)x-cx-62,(int)y-cy-62,124,124);
            g.drawOval((int)x-cx-61,(int)y-cy-61,122,122);
        }
        super.draw(g,cx,cy,alpha);
    }
}
