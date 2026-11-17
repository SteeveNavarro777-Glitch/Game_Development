/* FILE PURPOSE: FOREST BEAST ABSTRACTION | Shared wildlife patrol, pursuit, committed attack phases and cached rendering.
 * Course examples: inheritance, enum state machine and shared navigation; docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/** Ordinary wildlife shares health, circular patrol and BFS pursuit with the Enemy abstraction. */
abstract class ForestBeast extends Enemy {
    enum State { ROAM, WINDUP, CHARGE, RECOVER }
    static final double WARNING=.5,CHARGE_TIME=.32,RECOVERY=.65,CHARGE_SPEED=320,DEATH_TIME=1.05;
    private final double homeX,homeY;
    private State phase=State.ROAM;
    protected Player.Dir direction=Player.Dir.DOWN;
    private double clock,cooldown=.6,aimX,aimY=1,dustTime;
    private boolean moving,hit;
    // [BLOCK: SPECIALIZED CONSTRUCTION] ForestBeast.ForestBeast - Construct shared wildlife health, home position and a two-tile circular patrol.
    protected ForestBeast(double x,double y,int health,int group){super(x,y,health,group,2);homeX=x;homeY=y;}
    // [BLOCK: OOP ABSTRACTION] ForestBeast.kind - Require each wildlife subtype to select its supplied artwork.
    protected abstract WildlifeArt.Kind kind();
    // [BLOCK: ATTACK TIMING] ForestBeast.warningDuration - Supply a readable default charge warning.
    protected double warningDuration(){return WARNING;}
    // [BLOCK: ATTACK TIMING] ForestBeast.strikeDuration - Supply the default committed charge window.
    protected double strikeDuration(){return CHARGE_TIME;}
    // [BLOCK: ATTACK VECTOR] ForestBeast.attackSpeed - Keep the locked strike distance independent of night pursuit power.
    protected double attackSpeed(){return CHARGE_SPEED;}
    // [BLOCK: ATTACK TIMING] ForestBeast.recoveryDuration - Supply the default punishable recovery window.
    protected double recoveryDuration(){return RECOVERY;}
    // [BLOCK: ATTACK RANGE] ForestBeast.attackRange - Supply the default charge-selection distance.
    protected double attackRange(){return 96;}
    // [BLOCK: BODY MOTION] ForestBeast.bodyLift - Raise the wolf's visible leap while leaving collision feet on the ground.
    protected double bodyLift(){return kind()==WildlifeArt.Kind.WOLF&&phase==State.CHARGE?Math.sin(Math.PI*Math.min(1,clock/strikeDuration()))*14:0;}
    // [BLOCK: RENDER OPACITY] ForestBeast.opacity - Fade only the completed corpse; transient wildlife may add a departure fade.
    protected float opacity(){return alive()?1:(float)Math.max(0,Math.min(1,(DEATH_TIME-deathTime)/.25));}
    // [BLOCK: OOP EXTENSION] ForestBeast.charges - Allow wildlife subtypes to specialize their strike policy.
    protected boolean charges(){return true;}
    // [BLOCK: FOREST LEASH] ForestBeast.mayPursue - Chase outdoor targets near the home clearing and return to the patrol after disengaging.
    @Override protected boolean mayPursue(Player player,World world){
        double leash=World.T*(spawnGroup>=0?6:10);
        return player.alive()&&world.forest.contains(player.x,player.y)&&Math.hypot(player.x-homeX,player.y-homeY)<leash;
    }
    // [BLOCK: HABITAT COLLISION] ForestBeast.positionAllowed - Keep the complete footprint outside the palisade even through an open gate.
    @Override protected boolean positionAllowed(World world){
        Rectangle2D box=feetBox();return world.forest.contains(box.getMinX(),box.getMinY())&&world.forest.contains(box.getMaxX(),box.getMaxY());
    }
    // [BLOCK: AUTHORED HURT BOX] ForestBeast.hurtBox - Match the low wildlife torso while keeping the shared narrow navigation footprint.
    @Override Rectangle2D.Double hurtBox(){return new Rectangle2D.Double(x-15*scale(),y-24*scale(),30*scale(),24*scale());}
    // [BLOCK: ENCAPSULATION] ForestBeast.state - Expose the attack phase for focused behavior checks.
    final State state(){return phase;}
    // [BLOCK: ENCAPSULATION] ForestBeast.facing - Expose the cached animation direction.
    final Player.Dir facing(){return direction;}
    // [BLOCK: COMPLETE DEATH ANIMATION] ForestBeast.removable - Keep the corpse through all five supplied death poses before removing it.
    @Override boolean removable(){return !alive()&&deathTime>=DEATH_TIME;}
    // [BLOCK: INTERRUPTIBLE COMBAT] ForestBeast.damage - Interrupt a living creature's charge when Agyu lands a spear hit.
    @Override public void damage(int amount,double fromX,double fromY){
        if(amount<=0||!alive())return;super.damage(amount,fromX,fromY);
        phase=State.RECOVER;clock=0;cooldown=recoveryDuration()+.4;hit=true;
    }
    // [BLOCK: DIRECTION SELECTION] ForestBeast.face - Select front/back/side art from actual motion or a committed attack vector.
    protected final void face(double dx,double dy){
        if(Math.abs(dx)+Math.abs(dy)<.01)return;
        direction=Math.abs(dx)>Math.abs(dy)?(dx<0?Player.Dir.LEFT:Player.Dir.RIGHT):(dy<0?Player.Dir.UP:Player.Dir.DOWN);
    }
    // [BLOCK: SOLID-TERRAIN OCCLUSION] ForestBeast.clearLine - Refuse charge windups through roots, walls, water or mountain terrain.
    private boolean clearLine(Player player,World world){
        double distance=Math.hypot(player.x-x,player.y-y);int steps=Math.max(1,(int)Math.ceil(distance/6));
        for(int i=1;i<=steps;i++){double u=i/(double)steps,px=x+(player.x-x)*u,py=y+(player.y-y)*u;
            if(world.blocked(new Rectangle2D.Double(px-9,py-8,18,8))||!world.forest.contains(px,py))return false;}
        return true;
    }
    // [BLOCK: OOP POLYMORPHISM / STATE MACHINE] ForestBeast.update - Share patrol/BFS movement, then lock a visible warning, swept charge and recovery.
    @Override void update(double dt,Player player,World world,int[][] flow){
        if(!Double.isFinite(dt)||dt<=0)return;
        cooldown=Math.max(0,cooldown-dt);
        if(!alive()||phase==State.ROAM){
            double oldX=x,oldY=y;super.update(dt,player,world,flow);
            moving=Math.hypot(x-oldX,y-oldY)>.01;
            if(moving)face(x-oldX,y-oldY);
            if(alive()&&charges()&&cooldown==0&&speed()>0&&mayPursue(player,world)&&Math.hypot(player.x-x,player.y-y)<=attackRange()&&clearLine(player,world)){
                double dx=player.x-x,dy=player.y-y,length=Math.hypot(dx,dy);
                if(length<1){dx=direction==Player.Dir.LEFT?-1:direction==Player.Dir.RIGHT?1:0;dy=direction==Player.Dir.UP?-1:direction==Player.Dir.DOWN?1:0;length=1;}
                aimX=dx/length;aimY=dy/length;face(aimX,aimY);phase=State.WINDUP;clock=0;hit=false;moving=false;
            }
            return;
        }
        refreshNightPower(world);t+=dt;flash=Math.max(0,flash-dt);clock+=dt;
        moveThroughWorld(kbx*dt,kby*dt,world);kbx*=Math.exp(-8*dt);kby*=Math.exp(-8*dt);
        if((phase==State.WINDUP||phase==State.CHARGE)&&(!player.alive()||!world.forest.contains(player.x,player.y))){phase=State.RECOVER;clock=0;cooldown=1;}
        switch(phase){
            case WINDUP->{if(clock>=warningDuration()){phase=State.CHARGE;clock=0;dustTime=0;}}
            case CHARGE->{
                double elapsed=Math.min(dt,Math.max(0,strikeDuration()-(clock-dt)));
                int steps=Math.max(1,(int)Math.ceil(attackSpeed()*elapsed/3));double step=attackSpeed()*elapsed/steps;
                for(int i=0;i<steps&&phase==State.CHARGE;i++){
                    double oldX=x,oldY=y;moveThroughWorld(aimX*step,aimY*step,world);
                    if(Math.hypot(x-oldX,y-oldY)<step*.5){phase=State.RECOVER;clock=0;cooldown=1.2;break;}
                    if(!hit&&feetBox().intersects(player.feetBox())&&!player.airborne()){
                        hit=true;boolean blocked=player.blocksFrom(oldX,oldY);player.damage(attackDamage(contactDamage()),oldX,oldY);
                        if(blocked){phase=State.RECOVER;clock=0;cooldown=1.2;kbx=-aimX*90;kby=-aimY*90;break;}
                    }
                }
                dustTime+=dt;if(dustTime>=.06){dustTime=0;Fx.burst(x-aimX*14,y-aimY*10,0xBFA16E,3,22,.22,-8,1);}
                if(phase==State.CHARGE&&clock>=strikeDuration()){phase=State.RECOVER;clock=0;cooldown=1.2;}
            }
            case RECOVER->{if(clock>=recoveryDuration()){phase=State.ROAM;clock=0;}}
            default->{}
        }
        refreshNightPower(world);
    }
    // [BLOCK: GROUND WARNING] ForestBeast.drawGround - Draw the committed charge lane below actors without an attack instruction panel.
    final void drawGround(Graphics2D target,int cx,int cy,double alpha){
        if(!alive()||phase!=State.WINDUP)return;Graphics2D g=(Graphics2D)target.create();
        try{double sx=px+(x-px)*alpha-cx,sy=py+(y-py)*alpha-cy,reach=attackSpeed()*strikeDuration();
            g.setStroke(new BasicStroke(18,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g.setColor(new Color(245,164,69,42));
            g.draw(new Line2D.Double(sx,sy,sx+aimX*reach,sy+aimY*reach));
            g.setStroke(new BasicStroke(1));g.setColor(new Color(246,188,96,180));
            g.draw(new Line2D.Double(sx+aimX*15,sy+aimY*15,sx+aimX*reach,sy+aimY*reach));
        }finally{g.dispose();}
    }
    // [BLOCK: ANIMATION ARRAY LOOKUP] ForestBeast.body - Select authored poses; vertical charges retain front/back anatomy and deaths never loop.
    protected BufferedImage body(){
        WildlifeArt.Anim action=WildlifeArt.Anim.WALK;int frame=moving?(int)(t*(nightEmpowered()?10:8)):0;
        if(!alive()){action=WildlifeArt.Anim.DEATH;frame=Math.min(4,(int)(deathTime/.16));}
        else if(phase==State.WINDUP){action=WildlifeArt.Anim.ATTACK;frame=0;}
        else if(phase==State.CHARGE){
            double progress=clock/strikeDuration();
            if(kind()==WildlifeArt.Kind.WOLF&&progress<.6){action=WildlifeArt.Anim.LEAP;frame=Math.min(3,(int)(progress/.6*4));}
            else{action=WildlifeArt.Anim.ATTACK;frame=Math.min(3,(int)((kind()==WildlifeArt.Kind.WOLF?(progress-.6)/.4:progress)*4));}
        }
        else if(phase==State.RECOVER)frame=0;
        return WildlifeArt.frame(kind(),action,direction,frame,alive()&&flash>0);
    }
    // [BLOCK: DEPTH-SORTED RENDERING] ForestBeast.draw - Draw grounded wildlife at character scale using shared cached frames.
    @Override public void draw(Graphics2D target,int cx,int cy,double alpha){
        double sx=px+(x-px)*alpha-cx,sy=py+(y-py)*alpha-cy;Graphics2D g=(Graphics2D)target.create();
        try{g.translate(Math.round(sx),Math.round(sy));g.scale(scale(),scale());
            float fade=opacity();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,fade));g.setColor(new Color(0,0,0,55));g.fillOval(-16,-4,32,8);
            g.drawImage(body(),-WildlifeArt.ANCHOR_X,-WildlifeArt.ANCHOR_Y-(int)Math.round(bodyLift()),null);
            if(alive()&&(health()<maxHealth())){
                int top=kind()==WildlifeArt.Kind.WOLF?-52:-34;g.setColor(new Color(0x281D29));g.fillRect(-16,top,32,3);g.setColor(new Color(0xF1BA67));g.fillRect(-16,top,32*health()/maxHealth(),2);
            }
        }finally{g.dispose();}
    }
}
