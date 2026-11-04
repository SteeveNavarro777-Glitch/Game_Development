import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Bounded combat accents. Events are emitted by simulation; drawing never advances them. */
final class ComboEffects {
    static final int MAX_GHOSTS=24,MAX_RINGS=16,MAX_IMPACTS=32;
    private static final Color[] COLORS={
            new Color(0x5ADCF4),new Color(0xAD91FF),new Color(0x88EDFF),
            new Color(0xD8A3FF),new Color(0x7AEAF7),new Color(0xFFD370)};
    private static final Color WHITE=new Color(0xF5FFFF),RIM=new Color(0x284454);
    private static final BasicStroke THIN=new BasicStroke(1),GLOW=new BasicStroke(3);
    private static final double[][] GHOST_EVENTS={
            {.11,.19},{.08,.14,.20},{.10,.14,.18,.22},
            {.12,.18,.24},{.14,.18,.22,.26,.30,.34},{.17,.22,.27,.32}};
    private static final double[][] MOTE_EVENTS={
            {.10,.16,.22},{.08,.13,.18},{.10,.15,.20},
            {.12,.18,.24},{.14,.19,.24,.29,.34},{.17,.22,.27,.32}};
    private static final List<Ghost> ghosts=new ArrayList<>(MAX_GHOSTS);
    private static final List<Ring> rings=new ArrayList<>(MAX_RINGS);
    private static final List<Impact> impacts=new ArrayList<>(MAX_IMPACTS);
    private static final class Ghost {
        BufferedImage image;
        double x,y,life,max;
        int stage,dx,dy;
    }
    private static final class Ring {
        double x,y,life,max,radius;
    }
    private static final class Impact {
        double x,y,life,max;
        int stage;
    }
    private ComboEffects(){}
    static void clear(){ghosts.clear();rings.clear();impacts.clear();}
    static int ghostCount(){return ghosts.size();}
    static int ringCount(){return rings.size();}
    static int impactCount(){return impacts.size();}
    static int effectCount(){return ghosts.size()+rings.size()+impacts.size();}
    private static void stage(int stage){
        if(stage<0||stage>=COLORS.length)throw new IllegalArgumentException("Invalid combo stage");
    }
    private static boolean crossed(double previous,double current,double event){
        return previous<event&&current>=event;
    }
    /** Tip coordinates are world coordinates. Body is an existing cached weaponless sprite. */
    static void step(int stage,Player.Dir dir,double previous,double current,double x,double y,
            double tipX,double tipY,BufferedImage body,int offsetX,int offsetY){
        stage(stage);
        if(current<=previous)return;
        for(double event:GHOST_EVENTS[stage])if(crossed(previous,current,event)&&body!=null){
            if(ghosts.size()<MAX_GHOSTS){
                Ghost ghost=new Ghost();ghost.image=body;
                ghost.x=x+offsetX;ghost.y=y+offsetY;ghost.stage=stage;
                ghost.life=ghost.max=stage>=4?.22:.16;
                ghost.dx=dir==Player.Dir.LEFT?-1:dir==Player.Dir.RIGHT?1:0;
                ghost.dy=dir==Player.Dir.UP?-1:dir==Player.Dir.DOWN?1:0;
                ghosts.add(ghost);
            }
        }
        for(double event:MOTE_EVENTS[stage])if(crossed(previous,current,event))
            Fx.burst(tipX,tipY,COLORS[stage].getRGB(),stage==5?5:2,stage>=4?36:24,.19,0,1);
        if(stage==5){
            if(crossed(previous,current,.16)){
                ring(x,y,60,.48);
                Fx.burst(x,y-10,0xFFD370,16,86,.34,14,1);
            }
            if(crossed(previous,current,.23))ring(x,y,43,.34);
        }
    }
    static void hit(int stage,double x,double y){
        stage(stage);
        if(impacts.size()<MAX_IMPACTS){
            Impact impact=new Impact();impact.x=x;impact.y=y;impact.stage=stage;
            impact.life=impact.max=stage==5?.24:.16;impacts.add(impact);
        }
        Fx.burst(x,y,COLORS[stage].getRGB(),stage==5?7:3,stage==5?110:66,.22,0,1);
    }
    private static void ring(double x,double y,double radius,double life){
        if(rings.size()>=MAX_RINGS)return;
        Ring ring=new Ring();ring.x=x;ring.y=y;ring.radius=radius;ring.life=ring.max=life;rings.add(ring);
    }
    static void update(double dt){
        if(dt<=0)return;
        for(int i=ghosts.size()-1;i>=0;i--){
            Ghost ghost=ghosts.get(i);ghost.life-=dt;if(ghost.life<=0)ghosts.remove(i);
        }
        for(int i=rings.size()-1;i>=0;i--){
            Ring ring=rings.get(i);ring.life-=dt;if(ring.life<=0)rings.remove(i);
        }
        for(int i=impacts.size()-1;i>=0;i--){
            Impact impact=impacts.get(i);impact.life-=dt;if(impact.life<=0)impacts.remove(i);
        }
    }
    static void draw(Graphics2D target,int camX,int camY){
        if(effectCount()==0)return;
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            Composite incoming=g.getComposite();
            float global=incoming instanceof AlphaComposite?((AlphaComposite)incoming).getAlpha():1;
            for(Ring ring:rings)drawRing(g,ring,camX,camY,global);
            for(Ghost ghost:ghosts){
                double fade=Math.max(0,ghost.life/ghost.max);
                int sx=(int)Math.round(ghost.x)-camX,sy=(int)Math.round(ghost.y)-camY;
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(global*.23*fade)));
                g.drawImage(ghost.image,sx-ghost.image.getWidth()/2,sy-ghost.image.getHeight()+1,null);
                g.setColor(COLORS[ghost.stage]);g.setStroke(THIN);
                int streak=3+(int)Math.round((1-fade)*8);
                g.drawLine(sx-ghost.dx*4,sy-17-ghost.dy*4,
                        sx-ghost.dx*(4+streak),sy-17-ghost.dy*(4+streak));
            }
            for(Impact impact:impacts)drawImpact(g,impact,camX,camY,global);
        }finally{g.dispose();}
    }
    private static void drawRing(Graphics2D g,Ring ring,int camX,int camY,float global){
        double progress=1-ring.life/ring.max;
        double eased=1-(1-progress)*(1-progress);
        int radius=(int)Math.round(9+(ring.radius-9)*eased),halfHeight=(int)Math.round(radius*.36);
        int sx=(int)Math.round(ring.x)-camX,sy=(int)Math.round(ring.y)-camY;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(global*.58*(1-progress))));
        for(int segment=0;segment<12;segment++){
            int start=segment*30+(int)(progress*20);
            g.setStroke(GLOW);g.setColor(RIM);
            g.drawArc(sx-radius,sy-halfHeight,radius*2,halfHeight*2,start,18);
            g.setStroke(THIN);g.setColor(COLORS[5]);
            g.drawArc(sx-radius,sy-halfHeight,radius*2,halfHeight*2,start,18);
        }
        g.setColor(WHITE);
        for(int point=0;point<4;point++){
            double angle=point*Math.PI/2+progress*.16;
            int px=sx+(int)Math.round(Math.cos(angle)*radius*.74);
            int py=sy+(int)Math.round(Math.sin(angle)*halfHeight*.74);
            g.drawLine(px-2,py,px,py-2);g.drawLine(px,py-2,px+2,py);
            g.drawLine(px+2,py,px,py+2);g.drawLine(px,py+2,px-2,py);
        }
    }
    private static void drawImpact(Graphics2D g,Impact impact,int camX,int camY,float global){
        double progress=1-impact.life/impact.max;
        double fade=1-progress;
        int sx=(int)Math.round(impact.x)-camX,sy=(int)Math.round(impact.y)-camY;
        double radius=(impact.stage==5?14:8)*(.6+.4*progress);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(global*fade)));
        g.setStroke(THIN);
        for(int point=0;point<8;point++){
            double angle=point*Math.PI/4+progress*.22;
            double length=(point%2==0?1:.56)*radius;
            int inner=point%2==0?2:1;
            g.setColor(COLORS[impact.stage]);
            g.drawLine(sx+(int)Math.round(Math.cos(angle)*inner),sy+(int)Math.round(Math.sin(angle)*inner),
                    sx+(int)Math.round(Math.cos(angle)*length),sy+(int)Math.round(Math.sin(angle)*length));
        }
        g.setColor(WHITE);g.fillRect(sx-1,sy-1,3,3);
    }
}
