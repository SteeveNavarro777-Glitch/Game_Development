import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** One shared pose drives weapon rendering and swept damage in every direction. */
public final class SpearAttack {
    static final double ACTIVE_START=0.08, ACTIVE_END=0.28, REACH=42, RADIUS=4;
    private static final int GRIP_X=4;
    private static final BasicStroke LIGHT_STROKE=new BasicStroke(1);
    private static BufferedImage weapon;
    static final int STAGES=6;
    private record Move(String name,double duration,double start,double end,double chain,int damage,
            double reach,double knockback,int color){}
    private static final Move[] MOVES={
        new Move("CRESCENT CUT",.40,.08,.28,.30,1,42,.42,0x55CBEA),
        new Move("REVERSE FANG",.34,.06,.22,.24,1,45,.40,0xC49BFF),
        new Move("PIERCING LUNGE",.36,.08,.23,.25,1,58,.55,0x80EFFF),
        new Move("RISING TALON",.42,.10,.28,.31,1,47,.40,0xB6A3FF),
        new Move("WHIRLWIND",.50,.12,.36,.40,1,46,.48,0x73E8EE),
        new Move("SUNBREAKER",.60,.16,.34,.60,2,54,1.35,0xFFD16E)
    };
    private final Set<Enemy> hit=new HashSet<>();
    private int stage;

    record Pose(double handX,double handY,double angle,double reach) {
        double tipX(){return handX+Math.cos(angle)*reach;}
        double tipY(){return handY+Math.sin(angle)*reach;}
    }
    void begin(){begin(0);}
    void begin(int next){move(next);stage=next;hit.clear();}
    void cancel(){hit.clear();}
    int stage(){return stage;}
    private static Move move(int stage){
        if(stage<0||stage>=STAGES)throw new IllegalArgumentException("Invalid combo stage");
        return MOVES[stage];
    }
    static String name(int stage){return move(stage).name;}
    static double duration(int stage){return move(stage).duration;}
    static double activeStart(int stage){return move(stage).start;}
    static double activeEnd(int stage){return move(stage).end;}
    static double chainAt(int stage){return move(stage).chain;}
    static int damage(int stage){return move(stage).damage;}
    static int color(int stage){return move(stage).color;}
    double duration(){return duration(stage);}
    double lungeSpeed(double time){
        Move m=move(stage);
        if(time<m.start||time>m.end)return 0;
        return switch(stage){case 0 -> time>=.10&&time<.24?34:0;
            case 1 -> 18;case 2 -> time<.19?150:0;case 3 -> 35;
            case 4 -> 12;default -> time<.25?85:0;};
    }

    private static boolean blue(int pixel){
        int r=(pixel>>16)&255,g=(pixel>>8)&255,b=pixel&255;
        return (pixel>>>24)>64 && b>90 && g>85 && b>r*1.05;
    }
    /** Cache a pole using the supplied blue spearhead; no images are made during play. */
    static void loadWeapon(BufferedImage source){
        int minX=source.getWidth(),maxX=-1,minY=source.getHeight(),maxY=-1;
        for(int y=0;y<32;y++)for(int x=0;x<source.getWidth();x++)if(blue(source.getRGB(x,y))){
            minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);
        }
        if(maxX<minX)throw new IllegalArgumentException("Spearhead missing from supplied sprite");
        weapon=new BufferedImage(9,51,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=weapon.createGraphics();
        try{
            g.setColor(new Color(0x281C17));g.fillRect(3,10,3,48);
            g.setColor(new Color(0x9A602F));g.fillRect(4,10,1,47);
            g.setColor(new Color(0xDDA451));g.fillRect(4,14,1,35);
            g.setColor(new Color(0x455E66));g.fillRect(2,10,5,4);
            g.setColor(new Color(0xB7DCE1));g.fillRect(3,10,3,2);
        }finally{g.dispose();}
        int cx=(minX+maxX)/2;
        for(int y=Math.max(0,minY-1);y<=Math.min(31,maxY+1);y++)
            for(int x=cx-4;x<=cx+4;x++){
                int dx=x-cx+4,dy=y-minY+1;
                if(x>=0&&x<source.getWidth()&&dy>=0&&dy<14)
                    weapon.setRGB(dx,dy,source.getRGB(x,y));
            }
    }
    /** Attack bodies retain Agyu's art while dropping the old fixed upright pole. */
    static BufferedImage[] bodies(BufferedImage[] source){
        BufferedImage[] result=new BufferedImage[source.length];
        for(int frame=0;frame<source.length;frame++){
            BufferedImage raw=source[frame],out=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
            int tipLeft=64,baseRight=-1;
            for(int y=0;y<32;y++)for(int x=0;x<64;x++)
                if(blue(raw.getRGB(x,y)))tipLeft=Math.min(tipLeft,x);
            for(int y=50;y<64;y++)for(int x=38;x<64;x++)
                if((raw.getRGB(x,y)>>>24)>128)baseRight=Math.max(baseRight,x);
            int cut=Math.min(tipLeft-1,baseRight>=0?baseRight-3:tipLeft-1);
            if(cut<34||cut>49)throw new IllegalArgumentException("Unexpected spear position in attack body");
            for(int y=0;y<64;y++)for(int x=0;x<cut;x++)out.setRGB(x,y,raw.getRGB(x,y));
            result[frame]=out;
        }
        return result;
    }
    private static double clamp(double value){return Math.max(0,Math.min(1,value));}
    private static double smooth(double value){double p=clamp(value);return p*p*(3-2*p);}
    private static double forward(Player.Dir dir){
        return switch(dir){case RIGHT -> 0;case DOWN -> 90;case LEFT -> 180;case UP -> -90;};
    }
    private static double pulse(double time,Move m){
        return Math.sin(clamp((time-m.start)/(m.end-m.start))*Math.PI);
    }
    static Player.Dir bodyDirection(Player.Dir dir,double time,int stage){
        Move m=move(stage);
        if(stage!=4||time<m.start||time>m.end)return dir;
        double angle=forward(dir)+360*smooth((time-m.start)/(m.end-m.start));
        int quarter=Math.floorMod((int)Math.round(angle/90),4);
        return switch(quarter){case 0 -> Player.Dir.RIGHT;case 1 -> Player.Dir.DOWN;
            case 2 -> Player.Dir.LEFT;default -> Player.Dir.UP;};
    }
    static int bodyOffsetX(Player.Dir dir,double time,int stage){
        double amount=pulse(time,move(stage))*(stage==2?4:stage==5?2:stage==0?0:1);
        return (int)Math.round(Math.cos(Math.toRadians(forward(dir)))*amount);
    }
    static int bodyOffsetY(Player.Dir dir,double time,int stage){
        Move m=move(stage);
        double amount=pulse(time,m)*(stage==2?4:stage==5?2:stage==0?0:1);
        double lift=stage==3?pulse(time,m)*3:stage==5&&time<m.end?
                Math.sin(clamp(time/m.end)*Math.PI)*4:0;
        return (int)Math.round(Math.sin(Math.toRadians(forward(dir)))*amount-lift);
    }
    static Pose pose(Player.Dir dir,double time){return pose(dir,time,0);}
    static Pose pose(Player.Dir dir,double time,int stage){
        Move m=move(stage);
        double f=forward(dir),start,end;
        if(stage==0){
            switch(dir){
                case LEFT:start=-65;end=-220;break;
                case DOWN:start=-15;end=150;break;
                case UP:start=-170;end=-10;break;
                default:start=-115;end=40;break;
            }
        }else{
            start=f+switch(stage){case 1 -> 65;case 2 -> 0;case 3 -> 100;case 4 -> -110;default -> -115;};
            end=f+switch(stage){case 1 -> -100;case 2 -> 0;case 3 -> -110;case 4 -> 250;default -> 65;};
        }
        double u=clamp((time-m.start)/(m.end-m.start)),angle;
        if(time<m.start)angle=-90+(start+90)*smooth(time/m.start);
        else if(time<=m.end){
            angle=start+(end-start)*smooth(u);
            if(stage==3)angle+=Math.sin(u*Math.PI*2)*24;
        }else{
            double back=-90-end;
            if(stage!=0)back=((back+540)%360)-180;
            angle=end+back*smooth((time-m.end)/(m.duration-m.end));
        }
        double p=pulse(time,m),extend=p*2;
        double hx=dir==Player.Dir.LEFT?-9:9,hy=dir==Player.Dir.UP?-22:-20;
        if(stage==4){
            hx=Math.cos(Math.toRadians(angle))*8;
            hy=-21+Math.sin(Math.toRadians(angle))*3;
        }else{
            hx+=Math.cos(Math.toRadians(f))*extend;
            hy+=Math.sin(Math.toRadians(f))*extend;
        }
        int offsetX=bodyOffsetX(dir,time,stage),offsetY=bodyOffsetY(dir,time,stage);
        hx+=offsetX;hy+=offsetY;
        double reach=27+p*(m.reach-27),radians=Math.toRadians(angle);
        if(stage==2){
            // Drive the point out, hold it through the extended-body follow-through,
            // then retract with the recovery stance so the fist cannot reach the blade.
            reach=time<=m.end?24+(m.reach-24)*smooth(u/.45):
                    m.reach+(24-m.reach)*smooth((time-m.end)/(m.duration-m.end));
        }
        if(CombatBody.ready()){
            // Keep the continuous blade path while the authored fist changes stance.
            // Both the visible shaft and damage segment use the solved hand-to-tip pose.
            double tipX=hx+Math.cos(radians)*reach,tipY=hy+Math.sin(radians)*reach;
            CombatBody.Frame body=CombatBody.frame(dir,stage,time);
            hx=body.handX()+offsetX;hy=body.handY()+offsetY;
            radians=Math.atan2(tipY-hy,tipX-hx);
            reach=Math.hypot(tipX-hx,tipY-hy);
        }
        return new Pose(hx,hy,radians,reach);
    }
    Rectangle2D.Double bounds(double x,double y,Player.Dir dir,double time){
        Pose p=pose(dir,time,stage);
        double nearX=p.handX+Math.cos(p.angle)*8,nearY=p.handY+Math.sin(p.angle)*8;
        double scale=GameScale.AGYU;
        return new Rectangle2D.Double(x+(Math.min(nearX,p.tipX())-RADIUS)*scale,
                y+(Math.min(nearY,p.tipY())-RADIUS)*scale,
                (Math.abs(p.tipX()-nearX)+RADIUS*2)*scale,(Math.abs(p.tipY()-nearY)+RADIUS*2)*scale);
    }
    void update(Player owner,double previous,double current,List<Enemy> enemies){
        Move m=move(stage);
        double from=Math.max(m.start,previous),to=Math.min(m.end,current);
        if(to>=from&&current>=m.start&&previous<=m.end){
            int samples=Math.max(1,(int)Math.ceil((to-from)/.003));
            for(int sample=0;sample<=samples;sample++){
                Pose p=pose(owner.dir,from+(to-from)*sample/samples,stage);
                double cosine=Math.cos(p.angle),sine=Math.sin(p.angle);
                double scale=GameScale.AGYU;
                double ax=owner.x+(p.handX+cosine*8)*scale,ay=owner.y+(p.handY+sine*8)*scale;
                double bx=owner.x+p.tipX()*scale,by=owner.y+p.tipY()*scale;
                for(Enemy enemy:enemies){
                    if(!enemy.alive()||hit.contains(enemy)||(stage!=4&&behind(owner,enemy)))continue;
                    Rectangle2D box=enemy.hurtBox();
                    box.setRect(box.getX()-RADIUS*scale,box.getY()-RADIUS*scale,box.getWidth()+RADIUS*2*scale,box.getHeight()+RADIUS*2*scale);
                    if(box.intersectsLine(ax,ay,bx,by)){
                        hit.add(enemy);enemy.damage(m.damage,owner.x,owner.y);
                        enemy.kbx*=m.knockback;enemy.kby*=m.knockback;
                        ComboEffects.hit(stage,enemy.x,enemy.y-8);
                        if(stage==5){Fx.shake(3.2,.18);Fx.hitStop(.075);}
                    }
                }
            }
        }
        Pose p=pose(owner.dir,current,stage);
        Player.Dir bodyDir=bodyDirection(owner.dir,current,stage);
        BufferedImage body=null;
        if(CombatBody.ready())body=CombatBody.frame(owner.dir,stage,current).image();
        else if(Assets.attack!=null){
            BufferedImage[] sequence=Assets.attack[bodyDir.ordinal()];
            body=sequence[Math.min(sequence.length-1,Math.max(0,(int)(current/m.duration*sequence.length)))];
        }
        ComboEffects.step(stage,owner.dir,previous,current,owner.x,owner.y,
                owner.x+p.tipX()*GameScale.AGYU,owner.y+p.tipY()*GameScale.AGYU,
                body,bodyOffsetX(owner.dir,current,stage)*GameScale.AGYU,bodyOffsetY(owner.dir,current,stage)*GameScale.AGYU);
    }
    private static boolean behind(Player owner,Enemy target){
        return switch(owner.dir){case LEFT -> target.x>owner.x+4;case RIGHT -> target.x<owner.x-4;
            case UP -> target.y>owner.y+4;case DOWN -> target.y<owner.y-4;};
    }
    boolean behindBody(Player.Dir dir,double time){return Math.sin(pose(dir,time,stage).angle)<-.12;}
    private void trail(Graphics2D g,int sx,int sy,Player.Dir dir,double time){
        Move m=move(stage);
        if(time<m.start||time>m.end+.08)return;
        double end=Math.min(time,m.end),begin=Math.max(m.start,end-(stage==4?.18:.10));
        double fade=time<=m.end?1:1-(time-m.end)/.08;
        Path2D path=new Path2D.Double();
        for(int i=0;i<=18;i++){
            Pose p=pose(dir,begin+(end-begin)*i/18,stage);
            if(i==0)path.moveTo(sx+p.tipX(),sy+p.tipY());else path.lineTo(sx+p.tipX(),sy+p.tipY());
        }
        int width=stage==5?13:stage==4?10:8;
        g.setStroke(new BasicStroke(width,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g.setColor(new Color((int)(55*fade)<<24|m.color,true));g.draw(path);
        g.setStroke(new BasicStroke(stage==5?6:4,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g.setColor(new Color((int)(220*fade)<<24|m.color,true));g.draw(path);
        g.setStroke(LIGHT_STROKE);g.setColor(new Color(244,255,255,(int)(245*fade)));g.draw(path);
        if(stage==2){
            Pose p=pose(dir,end,stage);
            double a=Math.toRadians(forward(dir)),dx=Math.cos(a),dy=Math.sin(a);
            for(int sign:new int[]{-1,1}){
                int x1=(int)Math.round(sx+p.tipX()-dx*28-dy*sign*5);
                int y1=(int)Math.round(sy+p.tipY()-dy*28+dx*sign*5);
                int x2=(int)Math.round(sx+p.tipX()-dx*5-dy*sign*3);
                int y2=(int)Math.round(sy+p.tipY()-dy*5+dx*sign*3);
                g.drawLine(x1,y1,x2,y2);
            }
        }
    }
    void draw(Graphics2D graphics,int sx,int sy,Player.Dir dir,double time){
        if(weapon==null)return;
        Pose p=pose(dir,time,stage);Move m=move(stage);
        Graphics2D g=(Graphics2D)graphics.create();
        try{
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            trail(g,sx,sy,dir,time);
            if(stage==5&&time<m.start){
                int radius=3+(int)(time/m.start*4),tx=sx+(int)Math.round(p.tipX()),ty=sy+(int)Math.round(p.tipY());
                g.setColor(new Color(255,216,126,155));g.drawOval(tx-radius,ty-radius,radius*2,radius*2);
                g.drawLine(tx-radius-3,ty,tx+radius+3,ty);g.drawLine(tx,ty-radius-3,tx,ty+radius+3);
            }
            // The whole pole stays connected to the compact torso grip; never draw a skin bar.
            g.translate(sx+p.handX,sy+p.handY);g.rotate(p.angle+Math.PI/2);
            int reach=(int)Math.round(p.reach);
            if(stage==0)g.drawImage(weapon,-GRIP_X,-reach,null);
            else{
                // Stretch only the shaft; the supplied blue spearhead keeps its pixel proportions.
                g.drawImage(weapon,-GRIP_X,14-reach,9-GRIP_X,12,0,14,9,weapon.getHeight(),null);
                g.drawImage(weapon,-GRIP_X,-reach,9-GRIP_X,14-reach,0,0,9,14,null);
            }
        }finally{g.dispose();}
    }
}
