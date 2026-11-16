/* FILE PURPOSE: ARENA BOSS ABSTRACTION | Leashed encounters, explicit attack phases, bounded hazards and cached authored animation.
 * Course examples: inheritance, polymorphism, enums, lists and encapsulation; docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Bosses share safe lifecycle and collision, while concrete subclasses choose their attacks. */
abstract class ArenaBoss extends Enemy {
    enum State { DORMANT, CHASE, WINDUP, STRIKE, RECOVER, RETURN, DEAD }
    private enum HazardType { BOLT, RUNE }
    private static final int MAX_HAZARDS=18;
    protected final ForestRegion.Arena arena;
    protected final double homeX,homeY;
    protected final BossArt.Kind kind;
    private final String title,quest;
    private State phase=State.DORMANT;
    private Player.Dir direction=Player.Dir.DOWN;
    private boolean active;
    private int attacks,hitMask;
    private double clock,cooldown=.8,warnDuration,strikeDuration,recoveryDuration;
    protected double aimX,aimY,aimDx,aimDy;
    private BossArt.Anim attackAnimation=BossArt.Anim.ATTACK;
    // [SECTION: DSA - BOUNDED ARRAY LIST] Each encounter owns at most eighteen timed hazards.
    private final List<Hazard> hazards=new ArrayList<>(MAX_HAZARDS);
    private static final class Hazard {
        HazardType type;double x,y,vx,vy,age,delay,life,radius;boolean fired;
    }
    // [BLOCK: BOSS COMPOSITION] ArenaBoss.ArenaBoss - Bind one immutable clearing, authored art kind and quest identity.
    protected ArenaBoss(ForestRegion.Arena arena,BossArt.Kind kind,String title,String quest,int health){
        super((arena.c()+.5)*World.T,(arena.r()+.5)*World.T,health,-2);
        this.arena=arena;this.kind=kind;this.title=title;this.quest=quest;homeX=x;homeY=y;
    }
    // [BLOCK: ENCAPSULATION] ArenaBoss.arenaId - Read the assigned clearing number.
    final int arenaId(){return arena.id();}
    // [BLOCK: ENCAPSULATION] ArenaBoss.questId - Read the one-time defeat quest key.
    final String questId(){return quest;}
    // [BLOCK: ENCAPSULATION] ArenaBoss.bossName - Read the display name used by the boss HUD.
    final String bossName(){return title;}
    // [BLOCK: ENCAPSULATION] ArenaBoss.engaged - Report a living encounter with Agyu inside its arena.
    final boolean engaged(){return active&&alive();}
    // [BLOCK: ENCAPSULATION] ArenaBoss.state - Expose the current attack phase without allowing mutation.
    final State state(){return phase;}
    // [BLOCK: ENCAPSULATION] ArenaBoss.phaseTime - Read elapsed simulation time in the current phase.
    final double phaseTime(){return clock;}
    // [BLOCK: ENCAPSULATION] ArenaBoss.attackNumber - Read the deterministic attack-cycle index.
    final int attackNumber(){return attacks;}
    // [BLOCK: ENCAPSULATION] ArenaBoss.hazardCount - Read the bounded live hazard population.
    final int hazardCount(){return hazards.size();}
    // [BLOCK: ENCAPSULATION] ArenaBoss.facing - Read the authored facing selected for the current move.
    final Player.Dir facing(){return direction;}
    // [BLOCK: OOP CONTRACT] ArenaBoss.speed - Supply the concrete boss pursuit speed through Enemy's abstraction.
    @Override protected final double speed(){return pursueSpeed();}
    // [BLOCK: OOP CONTRACT] ArenaBoss.contactDamage - Boss contact alone causes no damage; attack windows own every hit.
    @Override protected final int contactDamage(){return 0;}
    // [BLOCK: OOP ABSTRACTION] ArenaBoss.pursueSpeed - Require a concrete pursuit pace.
    protected abstract double pursueSpeed();
    // [BLOCK: OOP ABSTRACTION] ArenaBoss.attackRange - Require a concrete attack-selection range.
    protected abstract double attackRange();
    // [BLOCK: OOP ABSTRACTION] ArenaBoss.accent - Require the concrete attack-warning palette.
    protected abstract int accent();
    // [BLOCK: OOP ABSTRACTION] ArenaBoss.planAttack - Choose the next telegraphed attack and timing.
    protected abstract void planAttack(Player player);
    // [BLOCK: OOP ABSTRACTION] ArenaBoss.strike - Resolve concrete attack windows during the active phase.
    protected abstract void strike(Player player,World world,double dt);
    // [BLOCK: OOP ABSTRACTION] ArenaBoss.warning - Draw the concrete locked attack footprint.
    protected abstract void warning(Graphics2D g,int cx,int cy);
    // [BLOCK: OOP DEFAULT] ArenaBoss.preferredDistance - Choose the pursuit stopping distance.
    protected double preferredDistance(){return 48;}
    // [BLOCK: OOP DEFAULT] ArenaBoss.retreats - Select whether this archetype backs away while recovering its spacing.
    protected boolean retreats(){return false;}
    // [BLOCK: OOP DEFAULT] ArenaBoss.knockbackFactor - Keep spear impacts visible without throwing a boss outside its arena.
    protected double knockbackFactor(){return .28;}
    // [BLOCK: COLLISION SHAPE] ArenaBoss.feetBox - Keep boss feet compatible with the arena's collision-safe routes.
    @Override Rectangle2D.Double feetBox(){return new Rectangle2D.Double(x-13,y-12,26,12);}
    // [BLOCK: AUTHORED BODY HITBOX] ArenaBoss.hurtBox - Match the substantial authored torso instead of the old slime body.
    @Override Rectangle2D.Double hurtBox(){return new Rectangle2D.Double(x-24,y-73,48,73);}
    // [BLOCK: DEATH LIFECYCLE] ArenaBoss.removable - Let every authored death frame finish before removal.
    @Override boolean removable(){return !alive()&&deathTime>=1.2;}
    // [BLOCK: CONTROLLED DAMAGE] ArenaBoss.damage - Reuse validated Enemy health and one-time reward handling with restrained knockback.
    @Override public void damage(int amount,double fromX,double fromY){
        if(amount<=0||!alive())return;
        super.damage(amount,fromX,fromY);kbx*=knockbackFactor();kby*=knockbackFactor();
        if(!alive()){phase=State.DEAD;clock=deathTime=0;active=false;hazards.clear();}
    }
    // [BLOCK: TIMED STATE MACHINE] ArenaBoss.update - Subdivide coarse simulation, leash the encounter and preserve explicit warning/strike/recovery phases.
    @Override final void update(double dt,Player player,World world,int[][] flow){
        refreshNightPower(world);
        if(dt<=0||!Double.isFinite(dt))return;
        if(!alive()){t+=dt;deathTime+=dt;clock+=dt;return;}
        int ticks=Math.max(1,(int)Math.ceil(Math.min(dt,2)/(.016666666666666666)));double step=Math.min(dt,2)/ticks;
        for(int i=0;i<ticks&&alive();i++)tick(step,player,world);
    }
    // [BLOCK: ENCOUNTER SIMULATION] ArenaBoss.tick - Advance one bounded gameplay slice and resolve attack windows exactly once.
    private void tick(double dt,Player player,World world){
        t+=dt;flash=Math.max(0,flash-dt);
        boolean present=player.alive()&&inside(player.x,player.y,1.24);
        if(!present){
            if(active){active=false;hazards.clear();phase=State.RETURN;clock=0;hitMask=0;cooldown=.8;}
            kbx=kby=0;
            if(Math.hypot(homeX-x,homeY-y)>1){phase=State.RETURN;face(homeX-x,homeY-y);moveToward(homeX,homeY,95*dt,world);}
            else{phase=State.DORMANT;clock=0;}
            return;
        }
        if(!active){active=true;phase=State.CHASE;clock=0;cooldown=.8;}
        double knockX=kbx*dt,knockY=kby*dt;kbx*=Math.exp(-10*dt);kby*=Math.exp(-10*dt);move(knockX,knockY,world);
        updateHazards(dt,player,world);clock+=dt;cooldown=Math.max(0,cooldown-dt);
        if(phase==State.CHASE){
            face(player.x-x,player.y-y);double distance=Math.hypot(player.x-x,player.y-y);
            if(cooldown<=0&&distance<=attackRange()){
                attacks++;hitMask=0;aimX=player.x;aimY=player.y;
                double length=Math.max(1,Math.hypot(aimX-x,aimY-y));aimDx=(aimX-x)/length;aimDy=(aimY-y)/length;
                if(length<=1){aimDx=0;aimDy=1;}
                planAttack(player);phase=State.WINDUP;clock=0;
            }else{
                if(distance>preferredDistance())moveToward(player.x,player.y,pursueSpeed()*pursuitMultiplier()*dt,world);
                else if(retreats()&&distance<95)move(-aimAwayX(player)*pursueSpeed()*pursuitMultiplier()*dt,-aimAwayY(player)*pursueSpeed()*pursuitMultiplier()*dt,world);
            }
        }else if(phase==State.WINDUP){
            if(clock>=warnDuration){clock-=warnDuration;phase=State.STRIKE;}
        }else if(phase==State.STRIKE){
            strike(player,world,dt);
            if(clock>=strikeDuration){clock-=strikeDuration;phase=State.RECOVER;}
        }else if(phase==State.RECOVER&&clock>=recoveryDuration){phase=State.CHASE;clock=0;cooldown=.18;}
    }
    // [BLOCK: PURSUIT VECTOR] ArenaBoss.aimAwayX - Normalize the current player-to-boss spacing vector on X.
    private double aimAwayX(Player player){return (player.x-x)/Math.max(1,Math.hypot(player.x-x,player.y-y));}
    // [BLOCK: PURSUIT VECTOR] ArenaBoss.aimAwayY - Normalize the current player-to-boss spacing vector on Y.
    private double aimAwayY(Player player){return (player.y-y)/Math.max(1,Math.hypot(player.x-x,player.y-y));}
    // [BLOCK: LOCKED ATTACK PLAN] ArenaBoss.configure - Store attack durations and the authored body animation before warning begins.
    protected final void configure(double warning,double active,double recovery,BossArt.Anim animation){
        warnDuration=warning;strikeDuration=active;recoveryDuration=recovery;attackAnimation=animation;
    }
    // [BLOCK: ONCE PER WINDOW] ArenaBoss.window - Claim one named active hit window once, even during coarse updates.
    protected final boolean window(int bit,double threshold){
        if(clock<threshold||(hitMask&bit)!=0)return false;hitMask|=bit;return true;
    }
    // [BLOCK: ELLIPTICAL LEASH] ArenaBoss.inside - Test an arena-relative point with a configurable activation margin.
    private boolean inside(double px,double py,double factor){
        double dx=(px-homeX)/((arena.rx()-.8)*World.T*factor),dy=(py-homeY)/((arena.ry()-.8)*World.T*factor);
        return dx*dx+dy*dy<=1;
    }
    // [BLOCK: AUTHORED FACING] ArenaBoss.face - Select one of the four supplied directional blocks.
    private void face(double dx,double dy){direction=Math.abs(dx)>Math.abs(dy)?(dx<0?Player.Dir.LEFT:Player.Dir.RIGHT):(dy<0?Player.Dir.UP:Player.Dir.DOWN);}
    // [BLOCK: MOVEMENT VECTOR] ArenaBoss.moveToward - Move toward a target without overshooting it.
    private void moveToward(double tx,double ty,double travel,World world){
        double dx=tx-x,dy=ty-y,distance=Math.hypot(dx,dy);if(distance<.01)return;
        travel=Math.min(travel,distance);move(dx/distance*travel,dy/distance*travel,world);
    }
    // [BLOCK: SWEPT BOSS COLLISION] ArenaBoss.move - Subdivide travel into at most three-pixel axis steps and retain the arena leash.
    protected final void move(double mx,double my,World world){
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(mx),Math.abs(my))/3));double dx=mx/steps,dy=my/steps;
        for(int i=0;i<steps;i++){
            if(dx!=0){double old=x;x+=dx;if(!inside(x,y,.96)||world.blocked(feetBox())){x=old;dx=0;}}
            if(dy!=0){double old=y;y+=dy;if(!inside(x,y,.96)||world.blocked(feetBox())){y=old;dy=0;}}
            if(dx==0&&dy==0)break;
        }
    }
    // [BLOCK: SOLID-TERRAIN OCCLUSION] ArenaBoss.clearLine - Refuse attacks that would cross a root, river, cliff or wall.
    private boolean clearLine(double ax,double ay,double bx,double by,World world){
        int steps=Math.max(1,(int)Math.ceil(Math.hypot(bx-ax,by-ay)/4));
        for(int i=1;i<=steps;i++){double q=i/(double)steps;
            if(world.blocked(new Rectangle2D.Double(ax+(bx-ax)*q-1,ay+(by-ay)*q-1,2,2)))return false;
        }return true;
    }
    // [BLOCK: ACTIVE MELEE HIT] ArenaBoss.coneHit - Apply one frontal hit using the locked warning direction and real Player defense methods.
    protected final void coneHit(Player player,World world,double radius,double cosine,int damage){
        double dx=player.x-x,dy=player.y-y,length=Math.hypot(dx,dy);
        if(length<=radius&&(length<1||(dx*aimDx+dy*aimDy)/length>=cosine)&&clearLine(x,y,player.x,player.y,world))player.damage(attackDamage(damage),x,y);
    }
    // [BLOCK: GROUND IMPACT] ArenaBoss.slamHit - Resolve a dodgeable radial impact that jumping can clear.
    protected final void slamHit(Player player,World world,double radius,int damage){
        if(!player.airborne()&&Math.hypot(player.x-x,player.y-y)<=radius&&clearLine(x,y,player.x,player.y,world))player.damage(attackDamage(damage),x,y);
    }
    // [BLOCK: BOUNDED PROJECTILE SPAWN] ArenaBoss.bolt - Add one finite-lived bolt with a locked launch angle.
    protected final void bolt(double angle,double speed){
        if(hazards.size()>=MAX_HAZARDS)return;Hazard h=new Hazard();h.type=HazardType.BOLT;h.x=x+Math.cos(angle)*26;h.y=y+Math.sin(angle)*26;
        h.vx=Math.cos(angle)*speed;h.vy=Math.sin(angle)*speed;h.life=2.6;h.radius=5;hazards.add(h);
    }
    // [BLOCK: BOUNDED RUNE SPAWN] ArenaBoss.rune - Add a visible delayed ground warning only on free terrain inside the arena.
    protected final void rune(double rx,double ry,World world){
        if(hazards.size()>=MAX_HAZARDS||!inside(rx,ry,1.05)||world.blocked(new Rectangle2D.Double(rx-4,ry-4,8,8)))return;
        Hazard h=new Hazard();h.type=HazardType.RUNE;h.x=rx;h.y=ry;h.delay=.95;h.life=1.28;h.radius=36;hazards.add(h);
    }
    // [BLOCK: HAZARD LIFECYCLE] ArenaBoss.updateHazards - Sweep bolts through solid terrain and expire each rune after one delayed impact.
    private void updateHazards(double dt,Player player,World world){
        for(int i=hazards.size()-1;i>=0;i--){Hazard h=hazards.get(i);h.age+=dt;boolean remove=h.age>=h.life;
            if(h.type==HazardType.BOLT&&!remove){
                int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(h.vx*dt),Math.abs(h.vy*dt))/3));
                for(int n=0;n<steps;n++){
                    double oldX=h.x,oldY=h.y;h.x+=h.vx*dt/steps;h.y+=h.vy*dt/steps;
                    if(!inside(h.x,h.y,1.12)||world.blocked(new Rectangle2D.Double(h.x-4,h.y-4,8,8))){remove=true;break;}
                    if(player.feetBox().intersects(h.x-6,h.y-6,12,12)){player.damage(attackDamage(1),oldX,oldY);Fx.burst(h.x,h.y-18,accent(),5,45,.2,0,1);remove=true;break;}
                }
            }else if(h.type==HazardType.RUNE&&!h.fired&&h.age>=h.delay){
                h.fired=true;Fx.burst(h.x,h.y-2,accent(),12,85,.35,-20,2);
                if(!player.airborne()&&Math.hypot(player.x-h.x,player.y-h.y)<=h.radius&&clearLine(h.x,h.y,player.x,player.y,world))player.damage(attackDamage(1),x,y);
            }
            if(remove)hazards.remove(i);
        }
    }
    // [BLOCK: WARNING GEOMETRY] ArenaBoss.coneWarning - Draw the locked frontal footprint, making its reach and direction visible before impact.
    protected final void coneWarning(Graphics2D g,int cx,int cy,double radius,double sweep){
        double center=Math.toDegrees(Math.atan2(-aimDy,aimDx));g.setColor(new Color((accent()&0xFFFFFF)|0x43000000,true));
        g.fill(new Arc2D.Double(x-cx-radius,y-cy-radius,radius*2,radius*2,center-sweep/2,sweep,Arc2D.PIE));
        g.setColor(new Color(accent()));g.draw(new Arc2D.Double(x-cx-radius,y-cy-radius,radius*2,radius*2,center-sweep/2,sweep,Arc2D.OPEN));
    }
    // [BLOCK: WARNING GEOMETRY] ArenaBoss.circleWarning - Draw the radius and a growing progress ring of a radial attack.
    protected final void circleWarning(Graphics2D g,int cx,int cy,double radius){
        int r=(int)radius,xx=(int)x-cx-r,yy=(int)y-cy-r;
        g.setColor(new Color((accent()&0xFFFFFF)|0x33000000,true));g.fillOval(xx,yy,r*2,r*2);
        g.setColor(new Color(accent()));g.drawOval(xx,yy,r*2,r*2);
        int progress=(int)(radius*Math.min(1,clock/Math.max(.01,warnDuration)));
        g.drawOval((int)x-cx-progress,(int)y-cy-progress,progress*2,progress*2);
    }
    // [BLOCK: CACHED BODY SELECTION] ArenaBoss.body - Select the authored state frame without allocating an image.
    private BufferedImage body(){
        BossArt.Anim animation=BossArt.Anim.IDLE;double frame=t*5;
        if(!alive()){animation=BossArt.Anim.DEATH;frame=Math.min(BossArt.count(kind,animation)-1,deathTime/.95*BossArt.count(kind,animation));}
        else if(flash>0&&phase==State.CHASE){animation=BossArt.Anim.HURT;frame=(.12-flash)*20;}
        else if(phase==State.WINDUP){animation=attackAnimation;frame=Math.min(1,BossArt.count(kind,animation)-1)*Math.min(.95,clock/warnDuration);}
        else if(phase==State.STRIKE){animation=attackAnimation;frame=Math.min(BossArt.count(kind,animation)-1,1+clock/strikeDuration*(BossArt.count(kind,animation)-1));}
        else if(phase==State.RECOVER){animation=attackAnimation;frame=BossArt.count(kind,animation)-1;}
        else if(phase==State.CHASE||phase==State.RETURN){animation=BossArt.Anim.WALK;frame=t*7;}
        return BossArt.frame(kind,animation,direction,(int)frame);
    }
    // [BLOCK: GROUND LAYER] ArenaBoss.drawGround - Render telegraphs and delayed rune areas before the depth-sorted actors.
    final void drawGround(Graphics2D target,int cx,int cy,double alpha){
        Graphics2D g=(Graphics2D)target.create();
        try{g.setStroke(new BasicStroke(2));if(alive()&&active&&(phase==State.WINDUP||phase==State.STRIKE))warning(g,cx,cy);drawHazards(g,cx,cy,true);}
        finally{g.dispose();}
    }
    // [BLOCK: POLYMORPHIC RENDER] ArenaBoss.draw - Render world telegraphs, bounded hazards and the supplied sprite while restoring parent graphics state.
    @Override public final void draw(Graphics2D target,int cx,int cy,double alpha){
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.setStroke(new BasicStroke(2));
            drawHazards(g,cx,cy,false);
            double blend=Math.max(0,Math.min(1,alpha));int sx=(int)Math.round(px+(x-px)*blend)-cx,sy=(int)Math.round(py+(y-py)*blend)-cy;
            g.setColor(new Color(0,0,0,75));g.fillOval(sx-25,sy-7,50,12);
            if(!alive()&&deathTime>1){float incoming=g.getComposite() instanceof AlphaComposite?((AlphaComposite)g.getComposite()).getAlpha():1;
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,incoming*(float)Math.max(0,(1.2-deathTime)/.2)));}
            g.drawImage(body(),sx-BossArt.ANCHOR_X,sy-BossArt.ANCHOR_Y,null);
        }finally{g.dispose();}
    }
    // [BLOCK: HAZARD RENDERING] ArenaBoss.drawHazards - Draw projectile glows and timed rune warnings without advancing their state.
    private void drawHazards(Graphics2D g,int cx,int cy,boolean ground){
        for(Hazard h:hazards){if((h.type==HazardType.RUNE)!=ground)continue;int sx=(int)h.x-cx,sy=(int)h.y-cy;
            if(h.type==HazardType.BOLT){
                g.setColor(new Color((accent()&0xFFFFFF)|0x45000000,true));g.fillOval(sx-10,sy-28,20,20);
                g.setColor(new Color(accent()));g.fillOval(sx-5,sy-23,10,10);g.setColor(new Color(0xF0E0FF));g.fillOval(sx-2,sy-21,4,4);
                g.drawLine(sx,sy-18,(int)(sx-h.vx*.065),(int)(sy-18-h.vy*.065));
            }else{
                int r=(int)h.radius;g.setColor(new Color((accent()&0xFFFFFF)|(h.fired?0x78000000:0x36000000),true));g.fillOval(sx-r,sy-r,r*2,r*2);
                g.setColor(new Color(accent()));g.drawOval(sx-r,sy-r,r*2,r*2);
                int inner=(int)(r*Math.min(1,h.age/h.delay));g.drawOval(sx-inner,sy-inner,inner*2,inner*2);
                for(int a=0;a<4;a++){double angle=a*Math.PI/2;g.drawLine((int)(sx+Math.cos(angle)*r),(int)(sy+Math.sin(angle)*r),(int)(sx+Math.cos(angle)*(r-8)),(int)(sy+Math.sin(angle)*(r-8)));}
            }
        }
    }
}
