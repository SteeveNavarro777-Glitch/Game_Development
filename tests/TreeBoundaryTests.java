/* FILE PURPOSE: REGRESSION TEST | Bark-aligned tree collision, swept movement, four safe gates and live rendering/navigation.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

/** Exercises real actor updates, gate input and the indexed collision/reference pair. */
public final class TreeBoundaryTests {
    private static int checks;
    private static final int[][] DIRS={{1,0},{-1,0},{0,1},{0,-1}};
    private static final int[] KEYS={KeyEvent.VK_D,KeyEvent.VK_A,KeyEvent.VK_S,KeyEvent.VK_W};
    // [BLOCK: TEST UTILITY] TreeBoundaryTests.TreeBoundaryTests - Prevent instances of the regression suite.
    private TreeBoundaryTests(){}
    // [BLOCK: TEST ENTRY] TreeBoundaryTests.main - Load the current art and run the focused suite.
    public static void main(String[] args)throws Exception{Assets.load();System.out.println("Tree/boundary checks passed: "+run());}
    // [BLOCK: REGRESSION SUITE] TreeBoundaryTests.run - Verify physical roots, gate safety/input, map caches and actual renders.
    static int run()throws Exception{
        checks=0;roots();movement();gates();visibility();input();previews();Fx.clear();return checks;
    }
    // [BLOCK: ASSET-ALIGNED COLLISION] TreeBoundaryTests.roots - Test every placed tree and the opaque bark of all four supplied styles.
    private static void roots(){
        World world=new World(7);
        boolean[] styles=new boolean[4];
        for(World.Tree tree:world.trees){
            styles[tree.style]=true;
            check(tree.trunk.width>=40&&tree.trunk.height>=20,"Tree still has a tiny root collider");
            check(world.blocked(new Rectangle2D.Double(tree.trunk.getCenterX()-1,tree.trunk.getCenterY()-1,2,2)),"Tree roots are passable");
            check(tree.sortY()==tree.rootY,"Tree sorts at stray grass rather than the roots");
            for(int i=0;i<12;i++){
                Rectangle2D b=new Rectangle2D.Double(tree.trunk.x-12+i*6,tree.trunk.y-5,8,8);
                check(world.blocked(b)==world.blockedBruteForce(b),"Expanded root missing from collision buckets");
            }
        }
        int[] bottom={139,158,159,159};
        for(int style=0;style<4;style++){
            check(styles[style],"A tree style vanished from the village");
            RpgArt.TreeGround ground=RpgArt.treeGround(style);
            check(ground==RpgArt.treeGround(style),"Root geometry was rebuilt on lookup");
            check(ground.floor()==bottom[style]-159,"Root anchor ignores the actual bark floor");
            Rectangle2D footprint=new Rectangle2D.Double(ground.left(),ground.top(),ground.width(),ground.height());
            BufferedImage image=RpgArt.tree(style);int bark=0;
            for(int y=bottom[style]-17;y<=bottom[style];y++)for(int x=24;x<136;x++){
                int p=image.getRGB(x,y),r=(p>>>16)&255,g=(p>>>8)&255,b=p&255;
                if((p>>>24)>128&&r>60&&r>g*1.13&&g>b*1.12){
                    bark++;check(footprint.contains(x-80,y-159),"Solid bark lies outside its ground footprint");
                }
            }
            check(bark>40,"Tree bark fixture is empty");
        }
    }
    // [BLOCK: PHYSICS FIXTURE] TreeBoundaryTests.approach - Find a clear approach whose last few pixels lead directly into one tree.
    private static Player approach(World world,World.Tree tree,int direction){
        double x=tree.trunk.getCenterX(),y=tree.trunk.getCenterY()+2.55;
        switch(direction){case 0:x=tree.trunk.getMinX()-14;break;case 1:x=tree.trunk.getMaxX()+14;break;
            case 2:y=tree.trunk.getMinY()-12;break;default:y=tree.trunk.getMaxY()+13;}
        Player actor=new Player(x,y);
        if(world.blocked(actor.feetBox())||world.blocked(new Rectangle2D.Double(x-9,y-8,18,8)))return null;
        for(int i=1;i<=2;i++){
            Rectangle2D b=new Rectangle2D.Double(actor.feetBox().x+DIRS[direction][0]*i*2,
                    actor.feetBox().y+DIRS[direction][1]*i*2,actor.feetBox().width,actor.feetBox().height);
            Rectangle2D enemyBox=new Rectangle2D.Double(x-9+DIRS[direction][0]*i*2,y-8+DIRS[direction][1]*i*2,18,8);
            if(world.blocked(b)||world.blocked(enemyBox))return null;
        }
        return actor;
    }
    // [BLOCK: SWEPT MOVEMENT] TreeBoundaryTests.movement - Coarse walking, sprint, jump, knockback, enemy motion and dash cannot skip roots.
    private static void movement(){
        for(int style=0;style<4;style++)for(int direction=0;direction<4;direction++){
            World world=new World(7);World.Tree target=null;Player start=null;
            for(World.Tree tree:world.trees)if(tree.style==style&&(start=approach(world,tree,direction))!=null){target=tree;break;}
            check(target!=null,"No clear tree approach for style/direction fixture: "+style+"/"+direction);
            for(int mode=0;mode<5;mode++){
                Player actor=new Player(start.x,start.y);KeyHandler keys=new KeyHandler();press(keys,KEYS[direction]);
                if(mode==1)press(keys,KeyEvent.VK_SHIFT);
                if(mode==2)press(keys,KeyEvent.VK_SPACE);
                if(mode==3)press(keys,KeyEvent.VK_Q);
                if(mode==4){actor.damage(1,actor.x-DIRS[direction][0]*20,actor.y-DIRS[direction][1]*20);actor.update(.7,keys.poll(),world,List.of());}
                else actor.update(mode==3?.16:.7,keys.poll(),world,List.of());
                check(!world.blocked(actor.feetBox()),"Actor ended inside roots");
                check(before(actor.feetBox(),target.trunk,direction),"Movement tunneled through the tree: "+style+"/"+direction+"/"+mode);
                if(mode==3)check(!actor.dashing(),"Tree did not stop a dash");Fx.clear();
            }
            Enemy enemy=new WildBoar(start.x,start.y,0);enemy.kbx=DIRS[direction][0]*250;enemy.kby=DIRS[direction][1]*250;
            enemy.update(.7,new Player(start.x+DIRS[direction][0]*200,start.y+DIRS[direction][1]*200),world,null);
            check(!world.blocked(enemy.feetBox())&&before(enemy.feetBox(),target.trunk,direction),"Enemy knockback crossed or entered roots: "+style+"/"+direction+" at "+enemy.feetBox()+" target "+target.trunk);Fx.clear();
        }
    }
    // [BLOCK: COLLISION ASSERTION] TreeBoundaryTests.before - Check the actor stopped on its approach side of an obstacle.
    private static boolean before(Rectangle2D actor,Rectangle2D obstacle,int direction){
        return switch(direction){case 0->actor.getMaxX()<=obstacle.getMinX()+1e-6;case 1->actor.getMinX()>=obstacle.getMaxX()-1e-6;
            case 2->actor.getMaxY()<=obstacle.getMinY()+1e-6;default->actor.getMinY()>=obstacle.getMaxY()-1e-6;};
    }
    // [BLOCK: GATE FIXTURE] TreeBoundaryTests.inside - Position Agyu within operating range on the village side.
    private static Player inside(Palisade.Gate gate){
        Rectangle2D b=gate.closed;
        return switch(gate.side){case NORTH->new Player(b.getCenterX(),b.getMaxY()+32);case SOUTH->new Player(b.getCenterX(),b.getMinY()-32);
            case WEST->new Player(b.getMaxX()+32,b.getCenterY()+2.55);case EAST->new Player(b.getMinX()-32,b.getCenterY()+2.55);};
    }
    // [BLOCK: GATE FIXTURE] TreeBoundaryTests.outside - Position Agyu on the short exterior path, clear of the closing leaf.
    private static Player outside(Palisade.Gate gate){
        Rectangle2D b=gate.closed;
        return switch(gate.side){case NORTH->new Player(b.getCenterX(),b.getMinY()-24);case SOUTH->new Player(b.getCenterX(),b.getMaxY()+24);
            case WEST->new Player(b.getMinX()-24,b.getCenterY()+2.55);case EAST->new Player(b.getMaxX()+24,b.getCenterY()+2.55);};
    }
    // [BLOCK: SAFE GATES / INDEX] TreeBoundaryTests.gates - Verify four directions, closed/open collision, routing, safe closure and cached map markers.
    private static void gates()throws Exception{
        for(Palisade.Side side:Palisade.Side.values()){
            World world=new World(7);Palisade.Gate gate=world.palisade.gates().stream().filter(g->g.side==side).findFirst().orElseThrow();
            check(!gate.open()&&world.blocked(gate.closed),"Gate is not protective at startup");
            Player actor=inside(gate);check(!world.blocked(actor.feetBox()),"Gate approach is blocked");
            Player exterior=outside(gate);int c=World.col(exterior.x),r=World.row(exterior.y);
            check(Pathfinder.find(world,World.col(World.SPAWN_X),World.row(World.SPAWN_Y),c,r).isEmpty(),"Closed perimeter has an unguarded route");
            VillageMap map=new VillageMap(world);Object cache=get(map,"terrain");long closedMap=mapHash(map,actor);
            long version=world.navigationVersion();
            check(world.interactGate(actor,List.of(),List.of())&&gate.open(),"Gate did not open");
            check(world.navigationVersion()==version+1&&!world.blocked(gate.closed),"Open leaf remained in collision/navigation");
            check(mapHash(map,actor)!=closedMap&&get(map,"terrain")==cache,"Live gate map state is stale or rebuilt the terrain cache");
            List<Point> route=Pathfinder.find(world,World.col(World.SPAWN_X),World.row(World.SPAWN_Y),c,r);
            check(!route.isEmpty(),"Open gate is not linked to the roads/exterior path");
            int pc=World.col(World.SPAWN_X),pr=World.row(World.SPAWN_Y);
            for(Point step:route){
                for(int sample=0;sample<=16;sample++){
                    double x=(pc+.5+(step.x-pc)*sample/16.0)*World.T,y=(pr+.5+(step.y-pr)*sample/16.0)*World.T;
                    check(!world.blocked(new Rectangle2D.Double(x-10,y-10,20,10)),"Open-gate route crosses an obstacle");
                }pc=step.x;pr=step.y;
            }
            version=world.navigationVersion();Player occupant=new Player(gate.closed.getCenterX(),gate.closed.getCenterY()+2.55);
            check(world.interactGate(occupant,List.of(),List.of())&&gate.open()&&world.navigationVersion()==version,"Gate closed on Agyu");
            Enemy enemy=new WildBoar(gate.closed.getCenterX(),gate.closed.getCenterY()+4,0);
            check(world.interactGate(actor,List.of(enemy),List.of())&&gate.open(),"Gate closed on a living enemy");
            Npc npc=new Npc("gate-worker","Worker",Npc.Role.FARMER,gate.closed.getCenterX(),gate.closed.getCenterY()+2.5,RpgArt.npc(0),List.of(),"Hello.");
            check(world.interactGate(actor,List.of(),List.of(npc))&&gate.open(),"Gate closed on an NPC");
            check(world.interactGate(exterior,List.of(),List.of())&&!gate.open(),"Gate cannot be closed from the outside");
            check(!world.blocked(exterior.feetBox()),"Closing a gate trapped an exterior actor");
            Random random=new Random(73+side.ordinal());
            for(int i=0;i<150;i++){
                Rectangle2D b=new Rectangle2D.Double(gate.closed.getCenterX()-70+random.nextDouble()*140,
                        gate.closed.getCenterY()-70+random.nextDouble()*140,1+random.nextDouble()*30,1+random.nextDouble()*18);
                check(world.blocked(b)==world.blockedBruteForce(b),"Dynamic gate differs from brute-force collision");
            }
            actor=inside(gate);int direction=switch(side){case NORTH->3;case SOUTH->2;case WEST->1;case EAST->0;};
            KeyHandler keys=new KeyHandler();press(keys,KEYS[direction]);press(keys,KeyEvent.VK_Q);actor.update(.16,keys.poll(),world,List.of());
            check(!world.blocked(actor.feetBox())&&before(actor.feetBox(),gate.closed,direction)&&!actor.dashing(),"Dash passed a closed gate");Fx.clear();
        }
        World world=new World(7);check(world.palisade.gates().size()==4,"Expected four cardinal gates");
        for(int r=0;r<world.rows;r++)for(int c=0;c<world.cols;c++)if(world.tile[r][c]==World.WALL)
            check(world.blocked(new Rectangle2D.Double((c+.5)*World.T-1,(r+.5)*World.T-1,2,2)),"Wood wall is passable");
    }
    // [BLOCK: RENDER STATE / VISIBILITY] TreeBoundaryTests.visibility - Canopies fade without making roots transparent or changing graphics state.
    private static void visibility(){
        World world=new World(7);
        for(int style=0;style<4;style++){
            World.Tree source=null;for(World.Tree tree:world.trees)if(tree.style==style){source=tree;break;}
            BufferedImage plain=new BufferedImage(180,180,BufferedImage.TYPE_INT_ARGB),fade=new BufferedImage(180,180,BufferedImage.TYPE_INT_ARGB);
            int cx=(int)source.x-90,cy=(int)source.y-160;Graphics2D g=plain.createGraphics();source.draw(g,cx,cy,1);g.dispose();
            Player player=new Player(source.x,source.rootY-30);g=fade.createGraphics();
            Composite composite=g.getComposite();AffineTransform transform=g.getTransform();Color color=g.getColor();
            source.draw(g,cx,cy,1,player);check(g.getComposite().equals(composite)&&g.getTransform().equals(transform)&&g.getColor().equals(color),"Tree rendering leaked graphics state");g.dispose();
            int changed=0;for(int y=0;y<180;y++)for(int x=0;x<180;x++){
                if(y>=129)check(plain.getRGB(x,y)==fade.getRGB(x,y),"Canopy fade altered the solid tree base");
                if(plain.getRGB(x,y)!=fade.getRGB(x,y))changed++;
            }
            check(changed>500,"Agyu stays hidden behind an opaque canopy");
        }
    }
    // [BLOCK: REAL INPUT] TreeBoundaryTests.input - E edges toggle gates, held presses do not repeat, and pause/map/restart preserve expected state.
    private static void input()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);press(panel.keys,KeyEvent.VK_ENTER);panel.update(1.0/60);panel.keys.clear();
        ((List<?>)get(panel,"enemies")).clear();World world=(World)get(panel,"world");Player actor=(Player)get(panel,"player");
        Palisade.Gate gate=world.palisade.gates().get(0);position(panel,actor,inside(gate));
        int builds=(Integer)get(panel,"flowBuilds");
        press(panel.keys,KeyEvent.VK_E);panel.update(1.0/60);check(gate.open(),"Real E did not open the gate");
        press(panel.keys,KeyEvent.VK_E);panel.update(1.0/60);check(gate.open(),"Held E repeated a gate toggle");panel.keys.clear();
        panel.update(1.0/60);check((Integer)get(panel,"flowBuilds")>builds&&(Long)get(panel,"flowVersion")==world.navigationVersion(),"Gate left the shared BFS cache stale");
        press(panel.keys,KeyEvent.VK_P);panel.update(1.0/60);panel.keys.clear();press(panel.keys,KeyEvent.VK_E);panel.update(1.0/60);
        check(gate.open(),"Paused gate changed");panel.keys.clear();press(panel.keys,KeyEvent.VK_P);panel.update(1.0/60);panel.keys.clear();
        press(panel.keys,KeyEvent.VK_M);panel.update(1.0/60);panel.keys.clear();press(panel.keys,KeyEvent.VK_E);panel.update(1.0/60);check(gate.open(),"Map input changed a gate");
        panel.keys.clear();press(panel.keys,KeyEvent.VK_M);panel.update(1.0/60);panel.keys.clear();press(panel.keys,KeyEvent.VK_E);panel.update(1.0/60);
        check(!gate.open(),"Fresh E did not close the gate");panel.keys.clear();press(panel.keys,KeyEvent.VK_E);panel.update(1.0/60);check(gate.open(),"Gate did not reopen");
        set(panel,"mode",GamePanel.Mode.DEFEAT);panel.keys.clear();press(panel.keys,KeyEvent.VK_R);panel.update(1.0/60);
        World restarted=(World)get(panel,"world");for(Palisade.Gate fresh:restarted.palisade.gates())check(!fresh.open(),"Restart retained an open gate");Fx.clear();
    }
    // [BLOCK: MAP CACHE EVIDENCE] TreeBoundaryTests.mapHash - Render a map to verify live gate markers without replacing its terrain image.
    private static long mapHash(VillageMap map,Player actor){
        BufferedImage image=new BufferedImage(320,240,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();map.draw(g,actor,new QuestLog(),0,0,320,240,1,false);g.dispose();
        long hash=1;for(int y=0;y<240;y++)for(int x=0;x<320;x++)hash=hash*31+image.getRGB(x,y);return hash;
    }
    // [BLOCK: RENDER FIXTURE] TreeBoundaryTests.position - Set an actual game camera and settled actor pose at a gateway.
    private static void position(GamePanel panel,Player actor,Player location)throws Exception{
        actor.x=actor.px=location.x;actor.y=actor.py=location.y;actor.vx=actor.vy=0;actor.state=Player.State.IDLE;actor.dir=Player.Dir.DOWN;
        World world=(World)get(panel,"world");double cx=Math.max(0,Math.min(actor.x-240,world.cols*World.T-GamePanel.VIEW_W));
        double cy=Math.max(-64,Math.min(actor.y-180,world.rows*World.T-GamePanel.VIEW_H));
        set(panel,"camX",cx);set(panel,"prevCamX",cx);set(panel,"camY",cy);set(panel,"prevCamY",cy);set(panel,"alpha",1d);
    }
    // [BLOCK: ACTUAL RENDER PREVIEWS] TreeBoundaryTests.previews - Export four gate states, the protected village and root/canopy behavior.
    private static void previews()throws Exception{
        GamePanel panel=new GamePanel();panel.setSize(GamePanel.W,GamePanel.H);press(panel.keys,KeyEvent.VK_ENTER);panel.update(1.0/60);panel.keys.clear();
        ((List<?>)get(panel,"enemies")).clear();World world=(World)get(panel,"world");Player actor=(Player)get(panel,"player");
        BufferedImage gallery=new BufferedImage(1920,800,BufferedImage.TYPE_INT_RGB);Graphics2D out=gallery.createGraphics();out.setColor(new Color(0x203729));out.fillRect(0,0,1920,800);
        for(int i=0;i<4;i++){
            Palisade.Gate gate=world.palisade.gates().get(i);
            for(int state=0;state<2;state++){
                position(panel,actor,state==0?inside(gate):outside(gate));
                if(state==1)world.interactGate(inside(gate),List.of(),List.of());
                BufferedImage frame=new BufferedImage(GamePanel.W,GamePanel.H,BufferedImage.TYPE_INT_RGB);Graphics2D g=frame.createGraphics();panel.paint(g);g.dispose();
                out.setColor(new Color(0xF0D6A2));out.drawString(gate.side+" / "+(state==0?"CLOSED":"OPEN"),i*480+10,state*400+20);
                out.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);out.drawImage(frame,i*480,state*400+28,480,360,null);
                if(state==0)ImageIO.write(frame,"png",new File("build/preview-gate-"+gate.side.name().toLowerCase(java.util.Locale.ROOT)+".png"));
            }
        }out.dispose();ImageIO.write(gallery,"png",new File("build/preview-wooden-gates.png"));
        BufferedImage overview=new BufferedImage(world.cols*World.T/4,world.rows*World.T/4,BufferedImage.TYPE_INT_RGB);Graphics2D g=overview.createGraphics();g.scale(.25,.25);world.drawGround(g,0,0,world.cols*World.T,world.rows*World.T,0);
        List<Drawable> scene=new ArrayList<>();scene.addAll(world.trees);scene.addAll(world.buildings);scene.addAll(world.props);scene.addAll(NpcPopulation.create(world));world.collectLandscape(scene,0,0,world.cols*World.T,world.rows*World.T);
        scene.sort(Comparator.comparingDouble(Drawable::sortY));for(Drawable d:scene)d.draw(g,0,0,1);g.dispose();ImageIO.write(overview,"png",new File("build/preview-wooden-boundary-overview.png"));
        BufferedImage roots=new BufferedImage(1280,400,BufferedImage.TYPE_INT_RGB);g=roots.createGraphics();g.setColor(new Color(0x29432E));g.fillRect(0,0,1280,400);
        for(int style=0;style<4;style++){
            World.Tree t=null;for(World.Tree candidate:world.trees)if(candidate.style==style){t=candidate;break;}
            Graphics2D cell=(Graphics2D)g.create();cell.translate(style*320,30);cell.scale(2,2);int cx=(int)t.x-80,cy=(int)t.y-160;
            Player behind=new Player(t.x,t.rootY-30);behind.px=behind.x;behind.py=behind.y;behind.draw(cell,cx,cy,1);t.draw(cell,cx,cy,1,behind);
            cell.setColor(new Color(0xF1B36B));cell.draw(new Rectangle2D.Double(t.trunk.x-cx,t.trunk.y-cy,t.trunk.width,t.trunk.height));cell.dispose();
            g.setColor(new Color(0xECD5A7));g.drawString("TREE "+style+" / SOLID ROOTS",style*320+12,20);
        }g.dispose();ImageIO.write(roots,"png",new File("build/preview-tree-roots.png"));
    }
    // [BLOCK: TEST REFLECTION] TreeBoundaryTests.get - Inspect session state without adding production test hooks.
    private static Object get(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    // [BLOCK: TEST REFLECTION] TreeBoundaryTests.set - Set an integration fixture's private state.
    private static void set(Object object,String name,Object value)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);f.set(object,value);}
    // [BLOCK: REAL KEY INPUT] TreeBoundaryTests.press - Send the same key event used by the running game.
    private static void press(KeyHandler keys,int code){keys.keyPressed(new KeyEvent(new Canvas(),KeyEvent.KEY_PRESSED,0,0,code,KeyEvent.CHAR_UNDEFINED));}
    // [BLOCK: ASSERTION] TreeBoundaryTests.check - Count and enforce a meaningful regression invariant.
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
}
