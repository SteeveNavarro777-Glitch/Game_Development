/* FILE PURPOSE: OOP ABSTRACTION | Shared enemy lifecycle, linked-list patrol and damage contract.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Point;
import java.awt.geom.Rectangle2D;

/** Shared enemy lifecycle and navigation; concrete types supply combat stats. */
// [SECTION: OOP - ABSTRACTION] Shared lifecycle depends on subclass-provided speed and damage.
public abstract class Enemy implements Drawable, Damageable {
    double x,y,px,py,kbx,kby,t,flash,deathTime;
    final int spawnGroup;
    private final int maximum;
    private int hp;
    private boolean defeatClaimed;
    private boolean empowered;
    // [SECTION: DSA - CIRCULAR LINKED LIST / COMPOSITION] Patrol repeatedly follows linked waypoints.
    private final CircularList<Point> patrol = new CircularList<>();
    private Point step;
    private static final int[][] DIRECTIONS={{0,-1},{1,0},{0,1},{-1,0}};

    // [BLOCK: FUNCTION] Enemy.Enemy - Initialize Enemy state.
    protected Enemy(double x,double y,int hp,int group) {
        this(x,y,hp,group,1);
    }
    // [BLOCK: DSA CIRCULAR LINKED LIST] Enemy.Enemy - Construct shared health and a configurable four-way roaming loop.
    protected Enemy(double x,double y,int hp,int group,int patrolRadius) {
        if(hp<=0||patrolRadius<1)throw new IllegalArgumentException("Enemy health and patrol radius must be positive");
        this.x=px=x;this.y=py=y;this.hp=maximum=hp;spawnGroup=group;
        int c=World.col(x),r=World.row(y);
        patrol.add(new Point(c-patrolRadius,r));patrol.add(new Point(c,r-patrolRadius));
        patrol.add(new Point(c+patrolRadius,r));patrol.add(new Point(c,r+patrolRadius));
    }
    // [BLOCK: OOP: ABSTRACTION] Enemy.speed - Require each enemy subtype to supply its movement speed.
    protected abstract double speed();
    // [BLOCK: OOP: ABSTRACTION] Enemy.contactDamage - Require each enemy subtype to supply its contact damage.
    protected abstract int contactDamage();
    // [BLOCK: FUNCTION] Enemy.scale - Handle scale for Enemy.
    protected double scale(){return 1;}
    // [BLOCK: OOP EXTENSION] Enemy.mayPursue - Let concrete wildlife constrain pursuit to its habitat.
    protected boolean mayPursue(Player player,World world){return player.alive();}
    // [BLOCK: OOP EXTENSION] Enemy.positionAllowed - Let concrete wildlife add habitat boundaries to solid collision.
    protected boolean positionAllowed(World world){return true;}
    // [BLOCK: OOP: ENCAPSULATION] Enemy.health - Expose current health while retaining private storage.
    public final int health(){return hp;}
    // [BLOCK: OOP: ENCAPSULATION] Enemy.maxHealth - Expose the immutable maximum health.
    public final int maxHealth(){return maximum;}
    // [BLOCK: OOP DEFAULT REWARD] Enemy.coinReward - Scale ordinary enemy rewards from immutable maximum health.
    int coinReward(){return maximum;}
    // [BLOCK: NIGHT POWER] Enemy.refreshNightPower - Apply the current outdoor night state without altering health, maximum health or rewards.
    final void refreshNightPower(World world){empowered=alive()&&world.dayNight.night()&&world.forest.contains(x,y);}
    // [BLOCK: ENCAPSULATION] Enemy.nightEmpowered - Read whether this living actor currently receives the outdoor night bonus.
    final boolean nightEmpowered(){return empowered;}
    // [BLOCK: PURSUIT MODIFIER] Enemy.pursuitMultiplier - Raise ordinary movement and boss pursuit by twenty-five percent at night.
    protected final double pursuitMultiplier(){return empowered?1.25:1;}
    // [BLOCK: OUTGOING DAMAGE] Enemy.attackDamage - Add one damage to real attacks at night while preserving zero-damage contact.
    protected final int attackDamage(int base){return base<=0?0:base+(empowered?1:0);}
    // [BLOCK: FUNCTION] Enemy.savePrev - Handle save prev for Enemy.
    void savePrev(){px=x;py=y;}
    // [BLOCK: FUNCTION] Enemy.removable - Handle removable for Enemy.
    boolean removable(){return !alive()&&deathTime>0.35;}
    // [BLOCK: OOP: ENCAPSULATION] Enemy.claimDefeat - Claim one defeat reward only after health reaches zero.
    boolean claimDefeat(){
        if(alive()||defeatClaimed)return false;
        defeatClaimed=true;return true;
    }
    // [BLOCK: FUNCTION] Enemy.feetBox - Handle feet box for Enemy.
    Rectangle2D.Double feetBox(){return new Rectangle2D.Double(x-9,y-8,18,8);}
    // [BLOCK: FUNCTION] Enemy.hurtBox - Handle hurt box for Enemy.
    Rectangle2D.Double hurtBox(){return new Rectangle2D.Double(x-11*scale(),y-17*scale(),22*scale(),17*scale());}
    // [BLOCK: OOP: ENCAPSULATION] Enemy.damage - Clamp private health and apply knockback plus damage effects.
    public void damage(int amount,double fromX,double fromY){
        if(amount<=0||!alive())return;
        hp=Math.max(0,hp-amount);flash=0.12;
        double dx=x-fromX,dy=y-fromY,length=Math.max(1,Math.hypot(dx,dy));
        kbx=dx/length*170;kby=dy/length*170;
        Fx.spark(x,y-8);Fx.hitStop(0.055);Fx.shake(1.5,0.12);
        if(!alive()){empowered=false;Fx.poof(x,y-6);}
    }
    // [BLOCK: OOP: POLYMORPHISM / LINKED LIST] Enemy.update - Use subclass stats while chasing the flow field or cycling patrol nodes.
    void update(double dt,Player player,World world,int[][] distance){
        refreshNightPower(world);t+=dt;flash=Math.max(0,flash-dt);
        if(!alive()){deathTime+=dt;return;}
        double mx=kbx*dt,my=kby*dt;
        double decay=Math.exp(-8*dt);kbx*=decay;kby*=decay;
        double range=Math.hypot(player.x-x,player.y-y);
        double tx=x,ty=y,pace=0;
        if(mayPursue(player,world)&&range<300){
            int c=World.col(x),r=World.row(y);
            if(range<World.T*1.5){tx=player.x;ty=player.y;step=null;}
            else {
                if(step==null||Math.hypot((step.x+0.5)*World.T-x,(step.y+0.5)*World.T-y)<3){
                    step=null;
                    if(world.inside(c,r)&&distance!=null&&distance[r][c]>=0){
                        int best=distance[r][c];
                        for(int[] d:DIRECTIONS){
                            int nc=c+d[0],nr=r+d[1];
                            if(world.walkable(nc,nr)&&distance[nr][nc]>=0&&distance[nr][nc]<best){
                                best=distance[nr][nc];step=new Point(nc,nr);
                            }
                        }
                    }
                }
                if(step!=null){tx=(step.x+0.5)*World.T;ty=(step.y+0.5)*World.T;}
            }
            pace=speed();
        }else{
            step=null;
            Point point=patrol.current();
            if(!world.walkable(point.x,point.y)||Math.hypot((point.x+0.5)*World.T-x,(point.y+0.5)*World.T-y)<4)
                point=patrol.advance();
            if(world.walkable(point.x,point.y)){tx=(point.x+0.5)*World.T;ty=(point.y+0.5)*World.T;pace=16;}
        }
        double dx=tx-x,dy=ty-y,length=Math.hypot(dx,dy);
        if(length>1){double travel=Math.min(length,pace*pursuitMultiplier()*dt);mx+=dx/length*travel;my+=dy/length*travel;}
        moveThroughWorld(mx,my,world);refreshNightPower(world);
        if(player.alive()&&feetBox().intersects(player.feetBox())){
            boolean blocked=player.blocksFrom(x,y);
            player.damage(attackDamage(contactDamage()),x,y);
            if(blocked){
                // A shield must physically stop contact enemies crossing through it.
                double repelX=x-player.x,repelY=y-player.y,repelLength=Math.hypot(repelX,repelY);
                if(repelLength<0.01){
                    repelX=player.dir==Player.Dir.LEFT?-1:player.dir==Player.Dir.RIGHT?1:0;
                    repelY=player.dir==Player.Dir.UP?-1:player.dir==Player.Dir.DOWN?1:0;
                    repelLength=1;
                }
                kbx=repelX/repelLength*110;kby=repelY/repelLength*110;
                moveThroughWorld(repelX/repelLength*2,repelY/repelLength*2,world);
            }
        }
    }
    // [BLOCK: FUNCTION] Enemy.moveThroughWorld - Sweep shared movement through solids and subtype habitat rules.
    protected final void moveThroughWorld(double mx,double my,World w){
        int steps=Math.max(1,(int)Math.ceil(Math.max(Math.abs(mx),Math.abs(my))/3));
        double dx=mx/steps,dy=my/steps;
        for(int i=0;i<steps;i++){
            if(dx!=0){x+=dx;if(w.blocked(feetBox())||!positionAllowed(w)){x-=dx;step=null;dx=0;}}
            if(dy!=0){y+=dy;if(w.blocked(feetBox())||!positionAllowed(w)){y-=dy;step=null;dy=0;}}
            if(dx==0&&dy==0)break;
        }
    }
    // [BLOCK: FUNCTION] Enemy.sortY - Handle sort y for Enemy.
    public double sortY(){return y;}
}
