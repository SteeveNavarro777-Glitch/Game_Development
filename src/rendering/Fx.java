/* FILE PURPOSE: EFFECTS | Bounded particles, hit-stop and screen shake.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.util.*;
import java.util.List;

/** Dust, sparks, bounded combo accents, screen shake and hit-stop, in art pixels. */
public class Fx {
    static class P { double x,y,vx,vy,life,max,grav; int size; Color color; }
    static final int MAX_PARTICLES=256;
    static final List<P> list=new ArrayList<>(MAX_PARTICLES);
    static final Random rnd=new Random();
    static double hitStop,shakeT,shakeMax,shakeMag;
    static int shakeX,shakeY;

    // [BLOCK: FUNCTION] Fx.clear - Handle clear for Fx.
    static void clear(){
        list.clear();ComboEffects.clear();DashEffects.clear();
        hitStop=shakeT=shakeMax=shakeMag=0;shakeX=shakeY=0;
    }
    // [BLOCK: FUNCTION] Fx.burst - Handle burst for Fx.
    static void burst(double x,double y,int rgb,int n,double speed,double life,double grav,int size){
        for(int i=0;i<n&&list.size()<MAX_PARTICLES;i++){
            P p=new P();double a=rnd.nextDouble()*Math.PI*2,s=speed*(.4+rnd.nextDouble()*.6);
            p.x=x;p.y=y;p.vx=Math.cos(a)*s;p.vy=Math.sin(a)*s;
            p.life=p.max=life*(.6+rnd.nextDouble()*.4);p.grav=grav;p.size=size;p.color=new Color(rgb);
            list.add(p);
        }
    }
    // [BLOCK: FUNCTION] Fx.dust - Handle dust for Fx.
    static void dust(double x,double y){burst(x,y-1,0xE3D3AE,2,14,.35,-10,2);}
    // [BLOCK: FUNCTION] Fx.spark - Handle spark for Fx.
    static void spark(double x,double y){burst(x,y,0xFFF3B0,6,90,.25,0,1);}
    // [BLOCK: FUNCTION] Fx.poof - Handle poof for Fx.
    static void poof(double x,double y){burst(x,y,0xE58AA0,12,60,.5,40,2);}
    // [BLOCK: FUNCTION] Fx.shake - Handle shake for Fx.
    static void shake(double mag,double t){
        if(mag<=0||t<=0)return;
        if(shakeT<=0)shakeMag=shakeMax=0;
        // A regular hit must not replace a stronger finishing impact in the same frame.
        shakeMag=Math.max(shakeMag,mag);shakeT=Math.max(shakeT,t);shakeMax=Math.max(shakeMax,shakeT);
    }
    // [BLOCK: FUNCTION] Fx.hitStop - Handle hit stop for Fx.
    static void hitStop(double t){hitStop=Math.max(hitStop,t);}
    // [BLOCK: FUNCTION] Fx.update - Advance update state with elapsed time.
    static void update(double dt){
        if(dt<=0)return;
        for(int i=list.size()-1;i>=0;i--){
            P p=list.get(i);p.life-=dt;
            if(p.life<=0){list.remove(i);continue;}
            p.vy+=p.grav*dt;p.x+=p.vx*dt;p.y+=p.vy*dt;
        }
        ComboEffects.update(dt);DashEffects.update(dt);
        if(shakeT>0){
            shakeT=Math.max(0,shakeT-dt);double m=shakeMag*Math.max(0,shakeT/shakeMax);
            shakeX=(int)Math.round((rnd.nextDouble()-.5)*2*m);
            shakeY=(int)Math.round((rnd.nextDouble()-.5)*2*m);
        }else shakeX=shakeY=0;
    }
    // [BLOCK: FUNCTION] Fx.draw - Render draw using the current graphics state.
    static void draw(Graphics2D g,int camX,int camY){
        Composite original=g.getComposite();
        try{
            ComboEffects.draw(g,camX,camY);
            float global=original instanceof AlphaComposite?((AlphaComposite)original).getAlpha():1;
            for(P p:list){
                float a=(float)Math.min(1,p.life/p.max*1.5)*global;
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,a));
                g.setColor(p.color);
                g.fillRect((int)Math.round(p.x)-camX,(int)Math.round(p.y)-camY,p.size,p.size);
            }
        }finally{g.setComposite(original);}
    }
}
