/* FILE PURPOSE: PLAYER EFFECTS | Bounded potion animation timers and a blue protection aura.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;

/** Paint is read-only; active simulation alone advances the three fixed timers. */
final class PotionEffects {
    static final double DURATION=.72;
    // [SECTION: DSA - ARRAY / OOP - COMPOSITION] Agyu owns exactly one animation timer per potion kind.
    private final double[] timers=new double[3];
    private double phase;
    // [BLOCK: ANIMATION START] PotionEffects.play - Restart just the consumed or impacted potion effect.
    void play(Inventory.Item item){timers[PotionArt.kind(item)]=DURATION;}
    // [BLOCK: ACTIVE SIMULATION] PotionEffects.update - Advance bounded animation phases and expire one-shot effects.
    void update(double dt){if(dt<=0||!Double.isFinite(dt))return;phase=(phase+dt)%1;for(int i=0;i<timers.length;i++)timers[i]=Math.max(0,timers[i]-dt);}
    // [BLOCK: ANIMATION READ] PotionEffects.remaining - Read a timer for deterministic pause and expiry checks.
    double remaining(Inventory.Item item){return timers[PotionArt.kind(item)];}
    // [BLOCK: CACHED FRAME RENDER] PotionEffects.draw - Draw supplied recovery effects and loop a subdued shield aura while protection remains.
    void draw(Graphics2D target,int x,int feet,boolean protectedByPotion){
        if(!protectedByPotion&&timers[0]<=0&&timers[1]<=0&&timers[2]<=0)return;
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            for(int kind=0;kind<3;kind++){
                boolean active=timers[kind]>0;if(!active&&!(kind==2&&protectedByPotion))continue;
                double progress=active?1-timers[kind]/DURATION:phase;
                int frame=Math.min(PotionArt.FRAME_COUNTS[kind]-1,(int)(progress*PotionArt.FRAME_COUNTS[kind]));
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,active?.9f:.28f));
                int size=kind==1?44:60,top=kind==1?feet-35:feet-57;
                g.drawImage(PotionArt.frame(kind,frame),x-size/2,top,size,size,null);
            }
        }finally{g.dispose();}
    }
}
