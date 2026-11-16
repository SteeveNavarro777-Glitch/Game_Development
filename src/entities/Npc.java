/* FILE PURPOSE: NPC | Friendly worker/patrol state, circular route and conversation.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Friendly villagers compose cached art, patrol routes and contextual conversations. */
final class Npc implements Drawable {
    enum Action { IDLE, WALK, WORK }
    enum Role { VILLAGER, FARMER, WEAVER, SMITH, FISHER, VENDOR, ELDER }
    interface Sprites {
        // [BLOCK: OOP CONTRACT] Npc.frame - Supply the cached animation frame for a direction, action and simulation time.
        BufferedImage frame(Player.Dir dir,Action action,double time); }
    private static final double SPEED=24,TALK_RANGE=46;
    private static final Font SMALL=new Font("Monospaced",Font.PLAIN,9),BOLD=new Font("Monospaced",Font.BOLD,9);
    private static final Color INK=new Color(0x19261F),CREAM=new Color(0xF3E8CE),GOLD=new Color(0xE8C681),
            BUBBLE=new Color(24,36,29,245),SHADOW=new Color(0,0,0,65);
    private final String id,name;
    private final Role role;
    private final Sprites art;
    private final String[] dialogue;
    // [SECTION: DSA - CIRCULAR LINKED LIST / COMPOSITION] Repeat the authored friendly patrol route.
    private final CircularList<Point> patrol=new CircularList<>();
    private double x,y,animationTime,waitTime,speechTime;
    double px,py;
    private Player.Dir dir=Player.Dir.DOWN;
    private Action action=Action.IDLE;
    private List<Point> path=List.of();
    private int pathIndex,nextLine;
    private boolean nearby;
    private String currentText="";
    private List<String> wrapped=List.of();

    // [BLOCK: FUNCTION] Npc.Npc - Initialize Npc state.
    Npc(String id,String name,Role role,double x,double y,Sprites art,List<Point> tilePatrol,String...dialogue){
        if(id==null||id.isBlank()||name==null||name.isBlank()||!Double.isFinite(x)||!Double.isFinite(y))
            throw new IllegalArgumentException("Invalid NPC identity or position");
        this.id=id;this.name=name;this.role=Objects.requireNonNull(role);this.art=Objects.requireNonNull(art);
        this.x=px=x;this.y=py=y;
        Objects.requireNonNull(tilePatrol);
        if(tilePatrol.size()>16)throw new IllegalArgumentException("Patrol is too large");
        for(Point point:tilePatrol){
            if(point==null||point.x<0||point.y<0)throw new IllegalArgumentException("Invalid patrol point");
            patrol.add(new Point(point));
        }
        this.dialogue=Objects.requireNonNull(dialogue).clone();
        for(String line:this.dialogue)if(line==null||line.isBlank()||line.length()>300)
            throw new IllegalArgumentException("Invalid dialogue");
        waitTime=.8+Math.floorMod(id.hashCode(),12)*.1;
    }
    // [BLOCK: FUNCTION] Npc.id - Handle id for Npc.
    String id(){return id;}
    // [BLOCK: FUNCTION] Npc.name - Handle name for Npc.
    String name(){return name;}
    // [BLOCK: FUNCTION] Npc.role - Handle role for Npc.
    Role role(){return role;}
    // [BLOCK: FUNCTION] Npc.x - Handle x for Npc.
    double x(){return x;}
    // [BLOCK: FUNCTION] Npc.y - Handle y for Npc.
    double y(){return y;}
    // [BLOCK: FUNCTION] Npc.dialogue - Handle dialogue for Npc.
    String dialogue(){return currentText;}
    // [BLOCK: FUNCTION] Npc.action - Handle action for Npc.
    Action action(){return action;}
    // [BLOCK: FUNCTION] Npc.direction - Handle direction for Npc.
    Player.Dir direction(){return dir;}
    // [BLOCK: FUNCTION] Npc.animationTime - Handle animation time for Npc.
    double animationTime(){return animationTime;}
    // [BLOCK: FUNCTION] Npc.isSpeaking - Handle is speaking for Npc.
    boolean isSpeaking(){return speechTime>0&&!currentText.isEmpty();}
    // [BLOCK: FUNCTION] Npc.savePrev - Handle save prev for Npc.
    void savePrev(){px=x;py=y;}
    // [BLOCK: FUNCTION] Npc.canTalk - Handle can talk for Npc.
    boolean canTalk(Player player){
        return dialogue.length>0&&player.alive()&&!player.airborne()&&!player.dashing()
                &&player.state!=Player.State.HURT&&player.state!=Player.State.ATTACK
                &&Math.hypot(player.x-x,player.y-y)<=TALK_RANGE;
    }
    // [BLOCK: NPC: DIALOGUE] Npc.talk - Advance dialogue only when the player can interact.
    boolean talk(Player player){
        if(!canTalk(player))return false;
        currentText=dialogue[nextLine];nextLine=(nextLine+1)%dialogue.length;
        wrapped=wrap(currentText);speechTime=4.5;action=Action.IDLE;face(player.x-x,player.y-y);return true;
    }
    // [BLOCK: NPC: COMPOSITION / GRAPH ROUTES] Npc.update - Advance work, conversation and collision-safe circular patrol movement.
    void update(double dt,World world,Player player){
        if(dt<=0||!Double.isFinite(dt))return;
        animationTime+=dt;speechTime=Math.max(0,speechTime-dt);nearby=canTalk(player);
        if(isSpeaking()&&Math.hypot(player.x-x,player.y-y)>112)speechTime=0;
        if(isSpeaking()){action=Action.IDLE;face(player.x-x,player.y-y);return;}
        currentText="";
        if(stationaryWorker()){action=Action.WORK;return;}
        action=Action.IDLE;
        if(patrol.size()==0)return;
        if(waitTime>0){waitTime=Math.max(0,waitTime-dt);return;}
        if(pathIndex>=path.size()){
            if(!path.isEmpty()){finishLeg();return;}
            Point goal=patrol.current();int c=World.col(x),r=World.row(y);
            if(c==goal.x&&r==goal.y){
                double gx=(goal.x+.5)*World.T,gy=(goal.y+.5)*World.T;
                if(Math.hypot(gx-x,gy-y)<1){finishLeg();return;}
                path=List.of(new Point(goal));
            }else path=Pathfinder.find(world,c,r,goal.x,goal.y);
            pathIndex=0;if(path.isEmpty()){finishLeg();return;}
        }
        double remaining=SPEED*dt;
        while(remaining>1e-9&&pathIndex<path.size()){
            Point target=path.get(pathIndex);
            double dx=(target.x+.5)*World.T-x,dy=(target.y+.5)*World.T-y,distance=Math.hypot(dx,dy);
            if(distance<1e-6){pathIndex++;continue;}
            double travel=Math.min(3,Math.min(remaining,distance)),mx=dx/distance*travel,my=dy/distance*travel;
            face(dx,dy);if(!move(mx,my,world)){finishLeg();return;}
            action=Action.WALK;remaining-=travel;if(travel>=distance-1e-6)pathIndex++;
        }
    }
    // [BLOCK: FUNCTION] Npc.stationaryWorker - Handle stationary worker for Npc.
    private boolean stationaryWorker(){return role==Role.SMITH||role==Role.FISHER||role==Role.WEAVER||role==Role.VENDOR||role==Role.ELDER||(role==Role.FARMER&&patrol.size()==0);}
    // [BLOCK: FUNCTION] Npc.finishLeg - Handle finish leg for Npc.
    private void finishLeg(){path=List.of();pathIndex=0;if(patrol.size()>0)patrol.advance();waitTime=1.2;action=Action.IDLE;}
    // [BLOCK: FUNCTION] Npc.move - Handle move for Npc.
    private boolean move(double mx,double my,World world){
        boolean clear=true;x+=mx;if(world.blocked(feetBox())){x-=mx;clear=false;}
        y+=my;if(world.blocked(feetBox())){y-=my;clear=false;}return clear;
    }
    // [BLOCK: FUNCTION] Npc.feetBox - Handle feet box for Npc.
    Rectangle2D.Double feetBox(){return new Rectangle2D.Double(x-4,y-5,8,5);}
    // [BLOCK: FUNCTION] Npc.face - Handle face for Npc.
    private void face(double dx,double dy){
        if(Math.hypot(dx,dy)<.01)return;
        dir=Math.abs(dx)>Math.abs(dy)?dx<0?Player.Dir.LEFT:Player.Dir.RIGHT:dy<0?Player.Dir.UP:Player.Dir.DOWN;
    }
    // [BLOCK: FUNCTION] Npc.sortY - Handle sort y for Npc.
    public double sortY(){return y;}
    // [BLOCK: FUNCTION] Npc.renderX - Handle render x for Npc.
    private double renderX(double alpha){return px+(x-px)*blend(alpha);}
    // [BLOCK: FUNCTION] Npc.renderY - Handle render y for Npc.
    private double renderY(double alpha){return py+(y-py)*blend(alpha);}
    // [BLOCK: FUNCTION] Npc.blend - Handle blend for Npc.
    private static double blend(double alpha){return Math.max(0,Math.min(1,alpha));}
    // [BLOCK: FUNCTION] Npc.draw - Render draw using the current graphics state.
    public void draw(Graphics2D target,int camX,int camY,double alpha){
        Graphics2D g=(Graphics2D)target.create();
        try{
            int sx=(int)Math.round(renderX(alpha))-camX,sy=(int)Math.round(renderY(alpha))-camY;
            g.setColor(SHADOW);g.fillOval(sx-9,sy-3,18,6);
            BufferedImage frame=art.frame(dir,action,animationTime);
            if(frame!=null)g.drawImage(frame,sx-frame.getWidth()/2,sy-63,null);
            if(nearby&&!isSpeaking()){
                g.setFont(SMALL);String prompt="[E] "+name;int width=g.getFontMetrics().stringWidth(prompt);
                g.setColor(BUBBLE);g.fillRect(sx-width/2-3,sy-76,width+6,12);
                g.setColor(CREAM);g.drawString(prompt,sx-width/2,sy-67);
            }
        }finally{g.dispose();}
    }
    // [BLOCK: FUNCTION] Npc.drawDialogue - Render draw dialogue using the current graphics state.
    void drawDialogue(Graphics2D target,int camX,int camY,double alpha,int viewW,int viewH){
        if(!isSpeaking()||viewW<48||viewH<48)return;
        int sx=(int)Math.round(renderX(alpha))-camX,sy=(int)Math.round(renderY(alpha))-camY;
        if(sx< -32||sx>viewW+32||sy< -32||sy>viewH+64)return;
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.clipRect(0,0,viewW,viewH);g.setFont(SMALL);
            int width=g.getFontMetrics().stringWidth(name);
            for(String line:wrapped)width=Math.max(width,g.getFontMetrics().stringWidth(line));
            width=Math.min(viewW-16,width+18);
            int height=25+wrapped.size()*11,bx=Math.max(8,Math.min(viewW-width-8,sx-width/2));
            int by=Math.max(8,Math.min(viewH-height-8,sy-75-height));
            if(by<45&&bx<245)by=45;
            if(by<104&&bx+width>365)by=104;
            if(by+height>sy-58&&sy+height+18<viewH)by=sy+10;
            g.setColor(BUBBLE);g.fillRect(bx,by,width,height);g.setColor(GOLD);g.drawRect(bx,by,width,height);
            if(by+height<sy-63){
                int tail=Math.max(bx+7,Math.min(bx+width-7,sx));
                Polygon triangle=new Polygon(new int[]{tail-4,tail+4,tail},new int[]{by+height,by+height,by+height+5},3);
                g.setColor(BUBBLE);g.fillPolygon(triangle);
            }
            g.setFont(BOLD);g.setColor(GOLD);g.drawString(name,bx+9,by+12);
            g.setFont(SMALL);g.setColor(CREAM);int lineY=by+25;
            for(String line:wrapped){g.drawString(line,bx+9,lineY);lineY+=11;}
        }finally{g.dispose();}
    }
    // [BLOCK: FUNCTION] Npc.wrap - Handle wrap for Npc.
    private static List<String> wrap(String text){
        List<String> lines=new ArrayList<>();StringBuilder line=new StringBuilder();
        for(String word:text.trim().split("\\s+")){
            if(line.length()>0&&line.length()+word.length()+1>38){lines.add(line.toString());line.setLength(0);}
            while(word.length()>38){lines.add(word.substring(0,38));word=word.substring(38);}
            if(line.length()>0)line.append(' ');line.append(word);
        }
        if(line.length()>0)lines.add(line.toString());return List.copyOf(lines);
    }
}
