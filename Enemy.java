import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/** Shared enemy lifecycle and navigation; concrete types supply combat stats. */
public abstract class Enemy implements Drawable, Damageable {
    double x,y,px,py,kbx,kby,t,flash,deathTime;
    final int shrineGroup;
    private final int maximum;
    private int hp;
    private boolean defeatClaimed;
    private final CircularList<Point> patrol = new CircularList<>();
    private Point step;
    private static final int[][] DIRECTIONS={{0,-1},{1,0},{0,1},{-1,0}};

    protected Enemy(double x,double y,int hp,int group) {
        if(hp<=0)throw new IllegalArgumentException("Enemy health must be positive");
        this.x=px=x;this.y=py=y;this.hp=maximum=hp;shrineGroup=group;
        int c=World.col(x),r=World.row(y);
        patrol.add(new Point(c-1,r));patrol.add(new Point(c,r-1));
        patrol.add(new Point(c+1,r));patrol.add(new Point(c,r+1));
    }
    protected abstract double speed();
    protected abstract int contactDamage();
    protected double scale(){return 1;}
    public final int health(){return hp;}
    public final int maxHealth(){return maximum;}
    void savePrev(){px=x;py=y;}
    boolean removable(){return !alive()&&deathTime>0.35;}
    boolean claimDefeat(){
        if(alive()||defeatClaimed)return false;
        defeatClaimed=true;return true;
    }
    Rectangle2D.Double feetBox(){return new Rectangle2D.Double(x-9,y-8,18,8);}
    Rectangle2D.Double hurtBox(){return new Rectangle2D.Double(x-11*scale(),y-17*scale(),22*scale(),17*scale());}
    public void damage(int amount,double fromX,double fromY){
        if(amount<=0||!alive())return;
        hp=Math.max(0,hp-amount);flash=0.12;
        double dx=x-fromX,dy=y-fromY,length=Math.max(1,Math.hypot(dx,dy));
        kbx=dx/length*170;kby=dy/length*170;
        Fx.spark(x,y-8);Fx.hitStop(0.055);Fx.shake(1.5,0.12);
        if(!alive())Fx.poof(x,y-6);
    }
    void update(double dt,Player player,World world,int[][] distance){
        t+=dt;flash=Math.max(0,flash-dt);
        if(!alive()){deathTime+=dt;return;}
        double mx=kbx*dt,my=kby*dt;
        double decay=Math.exp(-8*dt);kbx*=decay;kby*=decay;
        double range=Math.hypot(player.x-x,player.y-y);
        double tx=x,ty=y,pace=0;
        if(player.alive()&&range<300){
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
        if(length>1){double travel=Math.min(length,pace*dt);mx+=dx/length*travel;my+=dy/length*travel;}
        move(mx,my,world);
        if(player.alive()&&feetBox().intersects(player.feetBox())){
            boolean blocked=player.blocksFrom(x,y);
            player.damage(contactDamage(),x,y);
            if(blocked){
                // A shield must physically stop contact enemies crossing through it.
                double repelX=x-player.x,repelY=y-player.y,repelLength=Math.hypot(repelX,repelY);
                if(repelLength<0.01){
                    repelX=player.dir==Player.Dir.LEFT?-1:player.dir==Player.Dir.RIGHT?1:0;
                    repelY=player.dir==Player.Dir.UP?-1:player.dir==Player.Dir.DOWN?1:0;
                    repelLength=1;
                }
                kbx=repelX/repelLength*110;kby=repelY/repelLength*110;
                move(repelX/repelLength*2,repelY/repelLength*2,world);
            }
        }
    }
    private void move(double mx,double my,World w){
        x+=mx;if(w.blocked(feetBox())){x-=mx;step=null;}
        y+=my;if(w.blocked(feetBox())){y-=my;step=null;}
    }
    public double sortY(){return y;}
    public void draw(Graphics2D g,int cx,int cy,double alpha){
        int sx=(int)Math.round(px+(x-px)*alpha)-cx,sy=(int)Math.round(py+(y-py)*alpha)-cy;
        int size=(int)(20*scale()),height=(int)(16*scale());
        g.setColor(new Color(0,0,0,65));g.fillOval(sx-size/2,sy-3,size,7);
        java.awt.Composite old=g.getComposite();
        if(!alive())g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER,(float)Math.max(0,1-deathTime/0.35)));
        BufferedImage image=flash>0?Assets.slimeFlash[(int)(t*3)%2]:Assets.slime[(int)(t*3)%2];
        g.drawImage(image,sx-size/2,sy-height,size,height,null);
        g.setComposite(old);
        if(alive()&&(health()<maxHealth()||this instanceof Guardian)){
            g.setColor(new Color(0x281D29));g.fillRect(sx-size/2,sy-height-7,size,3);
            g.setColor(new Color(0xF1BA67));g.fillRect(sx-size/2,sy-height-7,size*health()/maxHealth(),2);
        }
    }
}
