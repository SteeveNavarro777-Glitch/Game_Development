/* FILE PURPOSE: COMBAT | Six-move configuration array, shared spear pose and one-hit hash set.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.BasicStroke;
import java.awt.AlphaComposite;
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
    static final double RADIUS=4;
    private static final int GRIP_X=4;
    private static final BasicStroke LIGHT_STROKE=new BasicStroke(1);
    private static BufferedImage weapon;
    // [BLOCK: CACHED EQUIPMENT ART] SpearAttack.icon - Reuse the prepared spear image for the hotbar icon cache.
    static BufferedImage icon(){return weapon;}
    static final int STAGES=6;
    private record Move(String name,double duration,double start,double end,double chain,int damage,
            double reach,double knockback,int color){}
    // [SECTION: DSA - ARRAY] Six indexed immutable move definitions supply timing, damage and reach.
    private static final Move[] MOVES={
        new Move("CRESCENT CUT",.40,.08,.28,.30,1,42,.42,0x55CBEA),
        new Move("REVERSE FANG",.34,.06,.22,.24,1,45,.40,0xC49BFF),
        new Move("PIERCING LUNGE",.36,.08,.23,.25,1,58,.55,0x80EFFF),
        new Move("RISING TALON",.42,.10,.28,.31,1,47,.40,0xB6A3FF),
        new Move("WHIRLWIND",.50,.12,.36,.40,1,46,.48,0x73E8EE),
        new Move("SUNBREAKER",.60,.16,.34,.60,2,54,1.35,0xFFD16E)
    };
    // [SECTION: DSA - HASH SET] One target can be damaged only once per combo stage.
    private final Set<Enemy> hit=new HashSet<>();
    private int stage;

    record Pose(double handX,double handY,double angle,double reach) {
        // [BLOCK: FUNCTION] SpearAttack.tipX - Handle tip x for SpearAttack.
        double tipX(){return handX+Math.cos(angle)*reach;}
        // [BLOCK: FUNCTION] SpearAttack.tipY - Handle tip y for SpearAttack.
        double tipY(){return handY+Math.sin(angle)*reach;}
    }
    // [BLOCK: DSA: HASH SET / COMBAT] SpearAttack.begin - Select the combo stage and clear targets already hit by the previous stage.
    void begin(int next){move(next);stage=next;hit.clear();}
    // [BLOCK: FUNCTION] SpearAttack.cancel - Handle cancel for SpearAttack.
    void cancel(){hit.clear();}
    // [BLOCK: FUNCTION] SpearAttack.stage - Handle stage for SpearAttack.
    int stage(){return stage;}
    // [BLOCK: FUNCTION] SpearAttack.move - Handle move for SpearAttack.
    private static Move move(int stage){
        if(stage<0||stage>=STAGES)throw new IllegalArgumentException("Invalid combo stage");
        return MOVES[stage];
    }
    // [BLOCK: FUNCTION] SpearAttack.name - Handle name for SpearAttack.
    static String name(int stage){return move(stage).name;}
    // [BLOCK: FUNCTION] SpearAttack.duration - Handle duration for SpearAttack.
    static double duration(int stage){return move(stage).duration;}
    // [BLOCK: FUNCTION] SpearAttack.activeStart - Handle active start for SpearAttack.
    static double activeStart(int stage){return move(stage).start;}
    // [BLOCK: FUNCTION] SpearAttack.activeEnd - Handle active end for SpearAttack.
    static double activeEnd(int stage){return move(stage).end;}
    // [BLOCK: FUNCTION] SpearAttack.chainAt - Handle chain at for SpearAttack.
    static double chainAt(int stage){return move(stage).chain;}
    // [BLOCK: FUNCTION] SpearAttack.color - Handle color for SpearAttack.
    static int color(int stage){return move(stage).color;}
    // [BLOCK: FUNCTION] SpearAttack.duration - Handle duration for SpearAttack.
    double duration(){return duration(stage);}
    // [BLOCK: FUNCTION] SpearAttack.lungeSpeed - Handle lunge speed for SpearAttack.
    double lungeSpeed(double time){
        Move m=move(stage);
        if(time<m.start||time>m.end)return 0;
        return switch(stage){case 0 -> time>=.10&&time<.24?34:0;
            case 1 -> 18;case 2 -> time<.19?150:0;case 3 -> 35;
            case 4 -> 12;default -> time<.25?85:0;};
    }

    // [BLOCK: FUNCTION] SpearAttack.blue - Handle blue for SpearAttack.
    private static boolean blue(int pixel){
        int r=(pixel>>16)&255,g=(pixel>>8)&255,b=pixel&255;
        return (pixel>>>24)>64 && b>90 && g>85 && b>r*1.05;
    }
    /** Cache a pole using the supplied blue spearhead; no images are made during play. */
    // [BLOCK: ASSET PREPARATION] SpearAttack.loadWeapon - Cache the supplied spearhead and matching shaft before gameplay.
    static void loadWeapon(BufferedImage source){
        int tipX=-1,minY=source.getHeight(),maxY=-1;
        for(int y=0;y<32;y++)for(int x=0;x<source.getWidth();x++)if(blue(source.getRGB(x,y))){
            if(y<minY){tipX=x;minY=y;}
            maxY=y;
        }
        if(tipX<0)throw new IllegalArgumentException("Spearhead missing from supplied sprite");
        weapon=new BufferedImage(9,51,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=weapon.createGraphics();
        try{
            g.setColor(new Color(0x281C17));g.fillRect(3,10,3,48);
            g.setColor(new Color(0x9A602F));g.fillRect(4,10,1,47);
            g.setColor(new Color(0xDDA451));g.fillRect(4,14,1,35);
            g.setColor(new Color(0x455E66));g.fillRect(2,10,5,4);
            g.setColor(new Color(0xB7DCE1));g.fillRect(3,10,3,2);
        }finally{g.dispose();}
        int cx=tipX;
        for(int y=Math.max(0,minY-1);y<=Math.min(31,maxY+1);y++)
            for(int x=cx-4;x<=cx+4;x++){
                int dx=x-cx+4,dy=y-minY+1;
                if(x>=0&&x<source.getWidth()&&dy>=0&&dy<14){
                    int rgb=source.getRGB(x,y);
                    if((rgb>>>24)<128)continue;
                    int red=rgb>>16&255,green=rgb>>8&255,blue=rgb&255;
                    boolean outline=red<80&&green<90&&blue<100;
                    boolean metal=blue>red*1.1&&blue>=green*.8;
                    boolean glint=red>150&&blue>=green*.95&&green>=red*.95;
                    if(outline||metal||glint)weapon.setRGB(dx,dy,0xFF000000|(rgb&0xFFFFFF));
                }
            }
        for(int x=3;x<=5;x++)weapon.setRGB(x,13,0xFF455E66);
        weapon.setRGB(4,13,0xFF95B8BC);
    }
    // [BLOCK: FUNCTION] SpearAttack.clamp - Handle clamp for SpearAttack.
    private static double clamp(double value){return Math.max(0,Math.min(1,value));}
    // [BLOCK: FUNCTION] SpearAttack.smooth - Handle smooth for SpearAttack.
    private static double smooth(double value){double p=clamp(value);return p*p*(3-2*p);}
    // [BLOCK: FUNCTION] SpearAttack.forward - Handle forward for SpearAttack.
    private static double forward(Player.Dir dir){
        return switch(dir){case RIGHT -> 0;case DOWN -> 90;case LEFT -> 180;case UP -> -90;};
    }
    // [BLOCK: FUNCTION] SpearAttack.pulse - Handle pulse for SpearAttack.
    private static double pulse(double time,Move m){
        return Math.sin(clamp((time-m.start)/(m.end-m.start))*Math.PI);
    }
    // [BLOCK: FUNCTION] SpearAttack.bodyDirection - Handle body direction for SpearAttack.
    static Player.Dir bodyDirection(Player.Dir dir,double time,int stage){
        Move m=move(stage);
        if(stage!=4||time<m.start||time>m.end)return dir;
        double angle=forward(dir)+360*smooth((time-m.start)/(m.end-m.start));
        int quarter=Math.floorMod((int)Math.round(angle/90),4);
        return switch(quarter){case 0 -> Player.Dir.RIGHT;case 1 -> Player.Dir.DOWN;
            case 2 -> Player.Dir.LEFT;default -> Player.Dir.UP;};
    }
    // [BLOCK: FUNCTION] SpearAttack.bodyOffsetX - Handle body offset x for SpearAttack.
    static int bodyOffsetX(Player.Dir dir,double time,int stage){
        double amount=pulse(time,move(stage))*(stage==2?4:stage==5?2:stage==0?0:1);
        return (int)Math.round(Math.cos(Math.toRadians(forward(dir)))*amount);
    }
    // [BLOCK: FUNCTION] SpearAttack.bodyOffsetY - Handle body offset y for SpearAttack.
    static int bodyOffsetY(Player.Dir dir,double time,int stage){
        Move m=move(stage);
        double amount=pulse(time,m)*(stage==2?4:stage==5?2:stage==0?0:1);
        double lift=stage==3?pulse(time,m)*3:stage==5&&time<m.end?
                Math.sin(clamp(time/m.end)*Math.PI)*4:0;
        return (int)Math.round(Math.sin(Math.toRadians(forward(dir)))*amount-lift);
    }
    // [BLOCK: COMBAT: SHARED GEOMETRY] SpearAttack.pose - Solve the body hand pivot and blade path used by both damage and rendering.
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
        // Keep the continuous blade path while the authored fist changes stance.
        // Startup validates this mandatory cache before an actor can attack.
        double tipX=hx+Math.cos(radians)*reach,tipY=hy+Math.sin(radians)*reach;
        CombatBody.Frame body=CombatBody.frame(dir,stage,time);
        hx=body.handX()+offsetX;hy=body.handY()+offsetY;
        radians=Math.atan2(tipY-hy,tipX-hx);
        reach=Math.hypot(tipX-hx,tipY-hy);
        return new Pose(hx,hy,radians,reach);
    }
    // [BLOCK: COMBAT: SWEPT COLLISION / HASH SET] SpearAttack.update - Sample the actual spear path and damage each enemy once per stage.
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
                        hit.add(enemy);enemy.damage(owner.spearDamage(m.damage),owner.x,owner.y);
                        enemy.kbx*=m.knockback;enemy.kby*=m.knockback;
                        ComboEffects.hit(stage,enemy.x,enemy.y-8);
                        if(stage==5){Fx.shake(3.2,.18);Fx.hitStop(.075);}
                    }
                }
            }
        }
        Pose p=pose(owner.dir,current,stage);
        BufferedImage body=CombatBody.frame(owner.dir,stage,current).image();
        ComboEffects.step(stage,owner.dir,previous,current,owner.x,owner.y,
                owner.x+p.tipX()*GameScale.AGYU,owner.y+p.tipY()*GameScale.AGYU,
                body,bodyOffsetX(owner.dir,current,stage)*GameScale.AGYU,bodyOffsetY(owner.dir,current,stage)*GameScale.AGYU);
    }
    // [BLOCK: FUNCTION] SpearAttack.behind - Handle behind for SpearAttack.
    private static boolean behind(Player owner,Enemy target){
        return switch(owner.dir){case LEFT -> target.x>owner.x+4;case RIGHT -> target.x<owner.x-4;
            case UP -> target.y>owner.y+4;case DOWN -> target.y<owner.y-4;};
    }
    // [BLOCK: FUNCTION] SpearAttack.behindBody - Handle behind body for SpearAttack.
    boolean behindBody(Player.Dir dir,double time){return Math.sin(pose(dir,time,stage).angle)<-.12;}
    // [BLOCK: FUNCTION] SpearAttack.trail - Handle trail for SpearAttack.
    private void trail(Graphics2D g,int sx,int sy,Player.Dir dir,double time,EquipmentLoadout.Level level){
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
        drawPurchasedEffect(g,sx,sy,dir,time,m,level);
    }

    // [BLOCK: TIER COMBAT EFFECT] SpearAttack.drawPurchasedEffect - Animate the exact blue/orange shop arc while the spear is active.
    private static void drawPurchasedEffect(Graphics2D target,int sx,int sy,Player.Dir dir,double time,Move move,
            EquipmentLoadout.Level level){
        if(level==EquipmentLoadout.Level.BASIC||time<move.start||time>move.end)return;
        double progress=clamp((time-move.start)/(move.end-move.start));
        BufferedImage effect=EquipmentArt.spearEffect(level,Math.min(7,(int)Math.floor(progress*8)));
        if(effect==null)return;
        double angle=Math.toRadians(forward(dir)),distance=24;
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.translate(sx+Math.cos(angle)*distance,sy-21+Math.sin(angle)*distance);g.rotate(angle);
            float alpha=(float)(.58+.34*Math.sin(progress*Math.PI));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
            g.drawImage(effect,-effect.getWidth()/2,-effect.getHeight()/2,null);
        }finally{g.dispose();}
    }
    // [BLOCK: FUNCTION] SpearAttack.draw - Render draw using the current graphics state.
    void draw(Graphics2D graphics,int sx,int sy,Player.Dir dir,double time){
        draw(graphics,sx,sy,dir,time,EquipmentLoadout.Level.BASIC);
    }

    // [BLOCK: EQUIPPED ATTACK ART] SpearAttack.draw - Use the selected inventory tier for every combo stage.
    void draw(Graphics2D graphics,int sx,int sy,Player.Dir dir,double time,EquipmentLoadout.Level level){
        // Assets.load supplies this runtime cache. Tests may clear it deliberately
        // to isolate the authored combat body and grounded shadow.
        if(weapon==null)return;
        BufferedImage equipped=EquipmentArt.spear(level);
        if(equipped==null)return;
        Pose p=pose(dir,time,stage);Move m=move(stage);
        Graphics2D g=(Graphics2D)graphics.create();
        try{
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            trail(g,sx,sy,dir,time,level);
            if(stage==5&&time<m.start){
                int radius=3+(int)(time/m.start*4),tx=sx+(int)Math.round(p.tipX()),ty=sy+(int)Math.round(p.tipY());
                g.setColor(new Color(255,216,126,155));g.drawOval(tx-radius,ty-radius,radius*2,radius*2);
                g.drawLine(tx-radius-3,ty,tx+radius+3,ty);g.drawLine(tx,ty-radius-3,tx,ty+radius+3);
            }
            // The whole pole stays connected to the compact torso grip; never draw a skin bar.
            g.translate(sx+p.handX,sy+p.handY);g.rotate(p.angle+Math.PI/2);
            int reach=(int)Math.round(p.reach),width=equipped.getWidth(),grip=width/2;
            if(stage==0)g.drawImage(equipped,-grip,-reach,null);
            else{
                // Stretch only the shaft; the supplied blue spearhead keeps its pixel proportions.
                int head=Math.min(16,Math.max(8,equipped.getHeight()/3));
                g.drawImage(equipped,-grip,head-reach,width-grip,12,0,head,width,equipped.getHeight(),null);
                g.drawImage(equipped,-grip,-reach,width-grip,head-reach,0,0,width,head,null);
            }
        }finally{g.dispose();}
    }
}
