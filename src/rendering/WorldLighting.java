/* FILE PURPOSE: WORLD LIGHTING | Reused moonlight mask, soft actor visibility and warm village lights.
 * Course examples: cached images, composition and read-only rendering; docs/CODE_GUIDE.md.
 */
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/** Only the world is tinted. UI, map, dialogue and combat clocks retain their own rendering/state. */
final class WorldLighting {
    private static final BufferedImage SOFT=spot(false),WARM=spot(true);
    private final BufferedImage shade;
    private final int width,height;
    // [BLOCK: RENDER CACHE] WorldLighting.WorldLighting - Allocate one reusable viewport mask instead of one image per frame.
    WorldLighting(int width,int height){this.width=width;this.height=height;shade=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);}
    // [BLOCK: SHARED LIGHT CACHE] WorldLighting.spot - Prepare a smooth radial alpha texture once for all visibility and lamp pools.
    private static BufferedImage spot(boolean warm){
        BufferedImage image=new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<256;y++)for(int x=0;x<256;x++){
            double distance=Math.hypot((x-127.5)/127.5,(y-127.5)/127.5);
            double u=Math.max(0,Math.min(1,(distance-(warm?0:.28))/(warm?1:.72)));
            int alpha=(int)Math.round((warm?38:255)*(1-u*u*(3-2*u)));
            image.setRGB(x,y,(alpha<<24)|(warm?0xFFC47B:0xFFFFFF));
        }return image;
    }
    // [BLOCK: CACHED LIGHT BLIT] WorldLighting.pool - Cull and remove darkness with an existing soft alpha image.
    private void pool(Graphics2D mask,double x,double y,int rx,int ry,float strength){
        if(x+rx<0||y+ry<0||x-rx>=width||y-ry>=height)return;
        mask.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT,strength));
        mask.drawImage(SOFT,(int)Math.round(x)-rx,(int)Math.round(y)-ry,rx*2,ry*2,null);
    }
    // [BLOCK: VILLAGE GLOW] WorldLighting.warm - Add a faint cached lamp glow without repainting or replacing source sprites.
    private void warm(Graphics2D g,double x,double y,int radius,double amount,float incoming){
        if(x+radius<0||y+radius<0||x-radius>=width||y-radius>=height)return;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)amount*incoming));
        g.drawImage(WARM,(int)Math.round(x)-radius,(int)Math.round(y)-radius,radius*2,radius*2,null);
    }
    // [BLOCK: NIGHT COMPOSITION] WorldLighting.draw - Tint the visible world, preserve actor clarity and leave all simulation untouched.
    void draw(Graphics2D target,World world,Player player,List<Enemy> enemies,int cx,int cy,double alpha){
        double amount=world.dayNight.darkness();if(amount<=.00001)return;
        double blend=Math.max(0,Math.min(1,alpha));Graphics2D mask=shade.createGraphics();
        try{
            mask.setComposite(AlphaComposite.Src);mask.setColor(new Color(9,18,49,(int)Math.round(158*amount)));mask.fillRect(0,0,width,height);
            mask.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            pool(mask,player.px+(player.x-player.px)*blend-cx,player.py+(player.y-player.py)*blend-cy-28,132,126,1);
            for(World.Building house:world.buildings)pool(mask,house.x-cx,house.y-cy-32,76,65,.58f);
            for(World.Prop prop:world.props)if(prop.style==6)pool(mask,prop.x-cx,prop.y-cy-62,92,85,.92f);
            for(Palisade.Gate gate:world.palisade.gates())pool(mask,gate.closed.getCenterX()-cx,gate.closed.getCenterY()-cy-20,82,72,.55f);
            for(int[] site:World.LANDMARKS)pool(mask,(site[0]+.5)*World.T-cx,(site[1]+.5)*World.T-cy-18,50,48,.6f);
            pool(mask,(MountainRiver.CENTER_C+.5)*World.T-cx,MountainRiver.SOURCE_R*World.T-cy-210,100,250,.75f);
            for(Enemy enemy:enemies)if(enemy.alive()&&enemy instanceof ArenaBoss&&((ArenaBoss)enemy).engaged())
                pool(mask,enemy.px+(enemy.x-enemy.px)*blend-cx,enemy.py+(enemy.y-enemy.py)*blend-cy-38,80,94,.6f);
        }finally{mask.dispose();}
        float incoming=target.getComposite() instanceof AlphaComposite?((AlphaComposite)target.getComposite()).getAlpha():1;
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.drawImage(shade,0,0,null);
            for(World.Building house:world.buildings)warm(g,house.x-cx,house.y-cy-32,46,amount,incoming);
            for(World.Prop prop:world.props)if(prop.style==6)warm(g,prop.x-cx,prop.y-cy-62,38,amount,incoming);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,incoming));g.setColor(new Color(194,135,255,(int)Math.round(155*amount)));
            for(Enemy enemy:enemies)if(enemy.nightEmpowered()){
                int x=(int)Math.round(enemy.px+(enemy.x-enemy.px)*blend)-cx,y=(int)Math.round(enemy.py+(enemy.y-enemy.py)*blend)-cy;
                int radius=enemy instanceof ArenaBoss?25:14;
                if(x+radius>=0&&y+8>=0&&x-radius<width&&y-8<height)g.drawOval(x-radius,y-4,radius*2,9);
            }
        }finally{g.dispose();}
    }
}
