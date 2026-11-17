/* FILE PURPOSE: EQUIPMENT ANIMATION | Authored body/wrist poses and cached held potion, shield and empty-hand composites.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import javax.imageio.ImageIO;

/** Prepared once at startup. No masking, cropping, item rotation or image allocation occurs in the game loop. */
final class HeldItemArt {
    static final int POSES=8,WIDTH=128,HEIGHT=128,FOOT_X=64,FOOT_Y=92;
    private static final Inventory.Item[] POTIONS={Inventory.Item.HEALTH_POTION,Inventory.Item.ENERGY_POTION,Inventory.Item.SHIELD_POTION};
    private static final int EMPTY=Inventory.Item.values().length;
    // [SECTION: DSA - 3D ARRAY] Composites and afterimages index item, direction and authored pose.
    private static BufferedImage[][][] frames,ghosts;
    private static int[][][] wrists;
    private static BufferedImage[][][][] movingDrinks;
    // [BLOCK: STATIC CACHE] HeldItemArt.HeldItemArt - Prevent instances of the shared animation cache.
    private HeldItemArt(){}
    // [BLOCK: CACHE STARTUP / ARRAY COMPOSITION] HeldItemArt.load - Register source bodies and prepare every held-item combination, including mirrored left poses.
    static void load()throws IOException{
        BufferedImage atlas=ImageIO.read(new File("res/player/equipment/bodies.png"));
        if(atlas==null||atlas.getWidth()!=512||atlas.getHeight()!=192)throw new IOException("Equipment body atlas is missing or invalid");
        BufferedImage[][] bodies=new BufferedImage[4][POSES];wrists=new int[4][POSES][2];
        List<String> rows=Files.readAllLines(Path.of("res/player/equipment/frames.csv"));
        if(rows.size()!=25)throw new IOException("Expected twenty-four authored held-item poses");
        for(String row:rows.subList(1,rows.size())){
            String[] c=row.split(",");Player.Dir dir=Player.Dir.valueOf(c[0].toUpperCase(java.util.Locale.ROOT));int pose=Integer.parseInt(c[1]);
            if(pose<0||pose>=POSES||bodies[dir.ordinal()][pose]!=null)throw new IOException("Duplicate/invalid equipment pose");
            bodies[dir.ordinal()][pose]=atlas.getSubimage(Integer.parseInt(c[2]),Integer.parseInt(c[3]),64,64);
            wrists[dir.ordinal()][pose][0]=Integer.parseInt(c[4]);wrists[dir.ordinal()][pose][1]=Integer.parseInt(c[5]);
        }
        for(int pose=0;pose<POSES;pose++){
            bodies[Player.Dir.LEFT.ordinal()][pose]=Assets.flip(bodies[Player.Dir.RIGHT.ordinal()][pose]);
            wrists[Player.Dir.LEFT.ordinal()][pose][0]=-1-wrists[Player.Dir.RIGHT.ordinal()][pose][0];
            wrists[Player.Dir.LEFT.ordinal()][pose][1]=wrists[Player.Dir.RIGHT.ordinal()][pose][1];
        }
        BufferedImage[][][] prepared=new BufferedImage[EMPTY+1][4][POSES],trails=new BufferedImage[EMPTY+1][4][POSES];
        for(int kind=0;kind<=EMPTY;kind++)for(Player.Dir dir:Player.Dir.values())for(int pose=0;pose<POSES;pose++){
            Inventory.Item item=kind==EMPTY?null:Inventory.Item.values()[kind];
            BufferedImage composed=compose(bodies[dir.ordinal()][pose],dir,pose,item,wrists[dir.ordinal()][pose]);
            prepared[kind][dir.ordinal()][pose]=composed;
            BufferedImage ghost=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);Graphics2D g=ghost.createGraphics();
            try{g.drawImage(composed,32-FOOT_X,63-FOOT_Y,null);}finally{g.dispose();}trails[kind][dir.ordinal()][pose]=ghost;
        }
        BufferedImage[][][][] walking=new BufferedImage[3][4][POSES][2];
        for(int kind=0;kind<3;kind++)for(Player.Dir dir:Player.Dir.values())for(int pose=0;pose<POSES;pose++)for(int step=0;step<2;step++){
            BufferedImage body=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);Graphics2D g=body.createGraphics();
            try{g.drawImage(bodies[dir.ordinal()][pose],0,0,null);g.setComposite(AlphaComposite.Src);
                g.drawImage(bodies[dir.ordinal()][step+1],0,48,64,64,0,48,64,64,null);
            }finally{g.dispose();}
            Inventory.Item item=POTIONS[kind];
            walking[kind][dir.ordinal()][pose][step]=compose(body,dir,pose,item,wrists[dir.ordinal()][pose]);
        }
        frames=prepared;ghosts=trails;movingDrinks=walking;
    }
    // [BLOCK: STARTUP COMPOSITOR] HeldItemArt.compose - Layer a cached selected item at the authored fist, preserving original finger pixels.
    private static BufferedImage compose(BufferedImage body,Player.Dir dir,int pose,Inventory.Item item,int[] hand){
        BufferedImage image=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);Graphics2D g=image.createGraphics();
        try{
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int bx=FOOT_X-32,by=FOOT_Y-63,hx=FOOT_X+hand[0],hy=FOOT_Y+hand[1];
            g.drawImage(body,bx,by,null);
            if(item==null)return image;
            if(item==Inventory.Item.SPEAR){
                Graphics2D gear=(Graphics2D)g.create();try{gear.translate(hx,hy);gear.rotate(spearAngle(dir));gear.drawImage(SpearAttack.icon(),-4,-35,null);}finally{gear.dispose();}
            }else if(item==Inventory.Item.SHIELD){
                int dx=dir==Player.Dir.RIGHT?8:dir==Player.Dir.LEFT?-8:0,dy=dir==Player.Dir.UP?-6:dir==Player.Dir.DOWN?5:0;
                g.drawImage(PotionArt.icon(item),hx-12+dx,hy-12+dy,24,24,null);
            }else{
                Graphics2D gear=(Graphics2D)g.create();
                try{gear.translate(hx,hy);gear.rotate(bottleAngle(dir,pose));gear.drawImage(PotionArt.icon(item),-8,-5,16,16,null);}finally{gear.dispose();}
            }
            if(item!=Inventory.Item.SHIELD){
                // Copy only the small original fist region over the bottle neck/shaft at startup.
                int sx=32+hand[0]-2,sy=63+hand[1]-2;
                g.drawImage(body,hx-2,hy-2,hx+3,hy+3,sx,sy,sx+5,sy+5,null);
            }
        }finally{g.dispose();}return image;
    }
    // [BLOCK: EQUIPMENT DIRECTION] HeldItemArt.spearAngle - Point a selected spear forward in the current facing direction.
    private static double spearAngle(Player.Dir dir){return switch(dir){case RIGHT->Math.PI/2;case LEFT->-Math.PI/2;case DOWN->Math.PI;case UP->0;};}
    // [BLOCK: SIP DIRECTION] HeldItemArt.bottleAngle - Bring the bottle opening toward the visible lips as the wrist lifts and tilts.
    private static double bottleAngle(Player.Dir dir,int pose){
        double angle=pose==4?1.0:pose==5?1.8:pose==6?.45:0;
        return dir==Player.Dir.DOWN||dir==Player.Dir.LEFT?angle:-angle;
    }
    // [BLOCK: ARRAY ACCESS] HeldItemArt.index - Include the empty-hand entry after the concrete item types.
    private static int index(Inventory.Item item){return item==null?EMPTY:item.ordinal();}
    // [BLOCK: CACHED FRAME] HeldItemArt.frame - Read a prepared item/body pose without allocating during paint.
    static BufferedImage frame(Inventory.Item item,Player.Dir dir,int pose){return frames[index(item)][dir.ordinal()][pose];}
    // [BLOCK: CACHED AFTERIMAGE] HeldItemArt.ghost - Reuse selected equipment in dash afterimages rather than showing an unequipped spear.
    static BufferedImage ghost(Inventory.Item item,Player.Dir dir,int pose){return ghosts[index(item)][dir.ordinal()][pose];}
    // [BLOCK: CACHED DRAW] HeldItemArt.draw - Draw a selected-item composite about its shared foot anchor.
    static void draw(Graphics2D g,int x,int y,Inventory.Item item,Player.Dir dir,int pose){g.drawImage(frame(item,dir,pose),x-FOOT_X,y-FOOT_Y,null);}
    // [BLOCK: WALKING DRINK CACHE] HeldItemArt.drawDrinking - Combine authored sipping and walking steps through cached full sprites.
    static void drawDrinking(Graphics2D g,int x,int y,Inventory.Item item,Player.Dir dir,int pose,double time,boolean moving){
        BufferedImage image=moving?movingDrinks[PotionArt.kind(item)][dir.ordinal()][pose][(int)(time*6)&1]:frame(item,dir,pose);
        g.drawImage(image,x-FOOT_X,y-FOOT_Y,null);
    }
    // [BLOCK: LOCOMOTION POSE] HeldItemArt.walkPose - Alternate authored steps and centered passing poses at walking or sprint cadence.
    static int walkPose(double time,boolean running){int step=(int)(time*(running?12:8))&3;return step==1?1:step==3?2:0;}
}
