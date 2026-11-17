/* FILE PURPOSE: SIGBIN AMBUSH | A night-only forest ambush followed by a persistent, fightable enemy using the supplied creature poses.
 * Course examples: finite state machine, polymorphic animation and bounded lifetime; docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** The reveal preserves control; afterward the Sigbin hunts and attacks until defeated, escaped or ended by dawn. */
final class Sigbin extends ForestBeast {
    enum Ambush { HIDDEN,REVEAL,HUNT,FADE }
    static final double SCARE_TIME=.42;
    private Ambush ambush=Ambush.HIDDEN;
    private double age,scareAge=SCARE_TIME,away;
    private boolean scared,expired,nightPresent;
    // [BLOCK: AMBUSH CONSTRUCTION] Sigbin.Sigbin - Create one night predator outside the boar herd groups.
    Sigbin(double x,double y){super(x,y,4,-4);}
    // [BLOCK: OOP OVERRIDING] Sigbin.kind - Select the supplied Sigbin animation set.
    @Override protected WildlifeArt.Kind kind(){return WildlifeArt.Kind.SIGBIN;}
    // [BLOCK: OOP OVERRIDING] Sigbin.speed - Supply the creature's hunting pace before the outdoor night modifier.
    @Override protected double speed(){return 68;}
    // [BLOCK: AMBUSH STRIKE POLICY] Sigbin.attackSpeed - Turn the supplied rush poses into a bounded claw lunge.
    @Override protected double attackSpeed(){return 280;}
    // [BLOCK: AMBUSH STRIKE POLICY] Sigbin.warningDuration - Give Agyu a readable reaction window before the claw lunge.
    @Override protected double warningDuration(){return .6;}
    // [BLOCK: NIGHT ENEMY DAMAGE] Sigbin.contactDamage - Supply claw damage through the normal night, guard and dash contracts.
    @Override protected int contactDamage(){return 1;}
    // [BLOCK: ENCAPSULATION] Sigbin.scareActive - Expose the short visual scare window without advancing it during rendering.
    boolean scareActive(){return alive()&&nightPresent&&!expired&&scared&&scareAge<SCARE_TIME;}
    // [BLOCK: ENCAPSULATION] Sigbin.hasScared - Read the one-shot scare flag for encounter verification.
    boolean hasScared(){return scared;}
    // [BLOCK: INTERRUPTIBLE AMBUSH] Sigbin.damage - Let a spear hit cancel the overlay and stagger a surviving creature without ending the fight.
    @Override public void damage(int amount,double fromX,double fromY){
        if(amount<=0||!alive())return;super.damage(amount,fromX,fromY);ambush=Ambush.HUNT;age=0;scareAge=SCARE_TIME;
    }
    // [BLOCK: ONE-SHOT SCARE] Sigbin.startle - Trigger one brief camera jolt, creature overlay and soft audio sting without hurting or freezing Agyu.
    private void startle(){if(scared)return;scared=true;scareAge=0;Fx.shake(2.6,.18);ForestAudio.scare();}
    // [BLOCK: NIGHT AMBUSH / REAL COMBAT] Sigbin.update - Reveal at night, preserve a reaction window, then use the shared chase and telegraphed claw strike.
    @Override void update(double dt,Player player,World world,int[][] flow){
        if(!Double.isFinite(dt)||dt<=0)return;nightPresent=world.dayNight.night();
        if(!alive()){super.update(dt,player,world,flow);return;}
        refreshNightPower(world);scareAge+=dt;
        if(!nightPresent){expired=true;scareAge=SCARE_TIME;return;}
        if(!player.alive()||!NightWildlife.deepForest(world,player.x,player.y)){
            scareAge=SCARE_TIME;if(ambush!=Ambush.FADE){ambush=Ambush.FADE;age=0;}
        }
        away=Math.hypot(player.x-x,player.y-y)>480?away+dt:0;
        if(away>=3&&ambush!=Ambush.FADE){ambush=Ambush.FADE;age=0;scareAge=SCARE_TIME;}
        if(ambush==Ambush.HUNT){super.update(dt,player,world,flow);return;}
        t+=dt;age+=dt;flash=Math.max(0,flash-dt);
        moveThroughWorld(kbx*dt,kby*dt,world);kbx*=Math.exp(-8*dt);kby*=Math.exp(-8*dt);
        if(ambush==Ambush.HIDDEN){face(player.x-x,player.y-y);if(age>=.75){ambush=Ambush.REVEAL;age=0;startle();}}
        else if(ambush==Ambush.REVEAL){if(age>=.55){ambush=Ambush.HUNT;age=0;}}
        else if(ambush==Ambush.FADE&&age>=.6)expired=true;
    }
    // [BLOCK: FINITE ENCOUNTER] Sigbin.removable - Remove a dawn/disengagement departure without a reward or keep a defeated enemy through all death poses.
    @Override boolean removable(){return expired||super.removable();}
    // [BLOCK: CONTEXTUAL OPACITY] Sigbin.opacity - Hide most of the stalking body, then reveal it fully before a smooth departure.
    @Override protected float opacity(){return !alive()?super.opacity():(!nightPresent||expired)?0:ambush==Ambush.HIDDEN?.07f:ambush==Ambush.FADE?(float)Math.max(0,1-age/.6):1;}
    // [BLOCK: SUPPLIED ANIMATION LOOKUP] Sigbin.body - Use the supplied attack/locomotion poses for real hunting, strikes and death.
    @Override protected BufferedImage body(){
        if(!alive()||ambush==Ambush.HUNT)return super.body();
        int frame=ambush==Ambush.REVEAL?3:0;
        return WildlifeArt.frame(kind(),WildlifeArt.Anim.ATTACK,direction,frame,flash>0);
    }
    // [BLOCK: STALKING EYES] Sigbin.draw - Depth-sort the creature normally and leave only a faint red-eyed glimpse while it is hiding.
    @Override public void draw(Graphics2D target,int cx,int cy,double alpha){
        super.draw(target,cx,cy,alpha);
        if(alive()&&nightPresent&&!expired&&ambush==Ambush.HIDDEN){Graphics2D g=(Graphics2D)target.create();try{
            int sx=(int)Math.round(px+(x-px)*alpha)-cx,sy=(int)Math.round(py+(y-py)*alpha)-cy;
            g.setColor(new Color(232,66,50,170));g.fillOval(sx-4,sy-17,3,3);g.fillOval(sx+3,sy-17,3,3);
        }finally{g.dispose();}}
    }
    // [BLOCK: READ-ONLY SCARE OVERLAY] Sigbin.drawOverlay - Briefly bring the supplied front-facing creature forward while leaving the HUD and player controls available.
    void drawOverlay(Graphics2D target,int width,int height){
        if(!scareActive())return;Graphics2D g=(Graphics2D)target.create();
        try{float opacity=(float)Math.min(1,(SCARE_TIME-scareAge)/.18);g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,opacity));
            for(int i=0;i<5;i++){g.setColor(new Color(88,18,28,10+i*5));int edge=6+i*5;g.drawRect(edge,edge,width-1-edge*2,height-1-edge*2);}
            g.translate(width/2d,height/2d+45);double scale=.95+.125*(1-scareAge/SCARE_TIME);g.scale(scale,scale);
            g.drawImage(WildlifeArt.scarePortrait(),-160,-288,null);
        }finally{g.dispose();}
    }
}
