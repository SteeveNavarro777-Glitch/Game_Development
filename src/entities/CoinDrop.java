/* FILE PURPOSE: COIN PICKUP | Deterministic enemy loot scatter, magnetic collection and pixel rendering.
 * Course examples: encapsulation, array lists and simulation state; docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/** One collectible coin bundle. Multiple bundles may visually split a large boss reward. */
final class CoinDrop implements Drawable {
    private static final double PICKUP_DELAY=.18,MAGNET_RANGE=72,PICKUP_RANGE=10;
    private static final Color SHADOW=new Color(20,24,22,85),EDGE=new Color(0x8B4D14),
            GOLD=new Color(0xF4A928),LIGHT=new Color(0xFFE486),DARK=new Color(0xC36A15);
    private double x,y,px,py,height,previousHeight,vx,vy,verticalVelocity,age;
    private final int value;
    private boolean collected;

    // [BLOCK: VALIDATED PICKUP] CoinDrop.CoinDrop - Create one deterministic launch from an enemy's feet.
    private CoinDrop(double x,double y,int value,int index,int count){
        if(value<=0||count<=0||index<0||index>=count)throw new IllegalArgumentException("Invalid coin bundle");
        this.x=px=x;this.y=py=y;this.value=value;height=previousHeight=8;
        double angle=-Math.PI*.88+(index+.5)*Math.PI*1.76/count;
        double speed=count==1?18:25+(index%2)*5;
        vx=Math.cos(angle)*speed;vy=Math.sin(angle)*speed*.55;verticalVelocity=58+(index%3)*5;
    }

    // [BLOCK: DETERMINISTIC PARTITION] CoinDrop.scatter - Split a reward into bounded visible bundles without changing its total.
    static List<CoinDrop> scatter(double x,double y,int reward){
        if(reward<=0)throw new IllegalArgumentException("Coin reward must be positive");
        int count=Math.max(1,Math.min(5,(reward+2)/3));
        List<CoinDrop> result=new ArrayList<>(count);int base=reward/count,remainder=reward%count;
        for(int i=0;i<count;i++)result.add(new CoinDrop(x,y,base+(i<remainder?1:0),i,count));
        return result;
    }

    // [BLOCK: INTERPOLATION] CoinDrop.savePrev - Retain the last fixed-step pose for smooth rendering.
    void savePrev(){px=x;py=y;previousHeight=height;}

    // [BLOCK: PICKUP SIMULATION] CoinDrop.update - Land, attract toward living Agyu and credit the wallet exactly once.
    void update(double dt,Player player){
        if(collected||player==null||!Double.isFinite(dt)||dt<=0)return;
        age+=dt;
        if(height>0||verticalVelocity>0){
            verticalVelocity-=145*dt;height+=verticalVelocity*dt;
            if(height<=0){height=0;verticalVelocity=0;}
        }
        double friction=Math.exp(-5.5*dt);x+=vx*dt;y+=vy*dt;vx*=friction;vy*=friction;
        if(!player.alive()||age<PICKUP_DELAY)return;
        double dx=player.x-x,dy=player.y-y,distance=Math.hypot(dx,dy);
        if(distance<MAGNET_RANGE&&distance>.001){
            double speed=80+(MAGNET_RANGE-distance)*3.2;
            x+=dx/distance*speed*dt;y+=dy/distance*speed*dt;
            distance=Math.hypot(player.x-x,player.y-y);
        }
        if(distance<=PICKUP_RANGE&&height<=2){
            collected=true;player.addCoins(value);
            Fx.burst(x,y-5,0xFFD45A,6,34,.28,18,1);
        }
    }

    // [BLOCK: PICKUP STATE] CoinDrop.collected - Support safe reverse-removal from the active array list.
    boolean collected(){return collected;}
    // [BLOCK: REWARD READ] CoinDrop.value - Expose immutable value for focused economy tests.
    int value(){return value;}

    // [BLOCK: PIXEL COIN RENDERING] CoinDrop.draw - Animate a crisp spinning coin and soft ground shadow.
    @Override public void draw(Graphics2D g,int camX,int camY,double alpha){
        if(collected)return;
        double rx=px+(x-px)*alpha,ry=py+(y-py)*alpha;
        double lift=previousHeight+(height-previousHeight)*alpha;
        int sx=(int)Math.round(rx)-camX,ground=(int)Math.round(ry)-camY;
        g.setColor(SHADOW);g.fillOval(sx-5,ground-2,10,4);
        int frame=((int)(age*12))&3,width=new int[]{3,5,7,5}[frame],top=(int)Math.round(ground-6-lift);
        g.setColor(EDGE);g.fillRect(sx-width/2-1,top-1,width+2,9);
        g.setColor(DARK);g.fillRect(sx-width/2,top,width,7);
        g.setColor(GOLD);g.fillRect(sx-width/2,top+1,width,5);
        if(width>=5){g.setColor(LIGHT);g.fillRect(sx-width/2+1,top+1,1,4);}
        g.setColor(LIGHT);g.fillRect(sx,top,1,2);
    }

    @Override public double sortY(){return y+.01;}
}
