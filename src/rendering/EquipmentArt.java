/* FILE PURPOSE: EQUIPMENT ART | Shared shop, inventory and held Rare/Legendary gear derived from the supplied sheet.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;

/**
 * Keeps one visual source of truth for every equipment tier.  The supplied
 * catalog icons are cleaned by SmithShopArt, then reduced with nearest-neighbor
 * sampling so upgraded gear remains crisp beside Agyu's native sprites.
 */
final class EquipmentArt {
    private static final SmithShopArt SHOP = new SmithShopArt();
    private static final BufferedImage[] SPEARS = new BufferedImage[EquipmentLoadout.Level.values().length];
    private static final BufferedImage[] SHIELDS = new BufferedImage[EquipmentLoadout.Level.values().length];
    // [SECTION: DSA - IDENTITY MAP] Each authored pose keeps a small cache of tier combinations.
    private static final IdentityHashMap<BufferedImage,BufferedImage[][]> CARRIED = new IdentityHashMap<>();
    private static final int SOURCE_X=HeldItemArt.FOOT_X-32, SOURCE_Y=HeldItemArt.FOOT_Y-63;

    private EquipmentArt() {}

    // [BLOCK: SHARED CATALOG ICON] EquipmentArt.icon - Use the exact purchased tier in shop, inventory and hotbar.
    static BufferedImage icon(EquipmentLoadout.Gear gear,EquipmentLoadout.Level level){
        if(level==EquipmentLoadout.Level.BASIC)
            return PotionArt.icon(gear==EquipmentLoadout.Gear.SPEAR?Inventory.Item.SPEAR:Inventory.Item.SHIELD);
        return SHOP.icon(gear,level);
    }

    // [BLOCK: CACHED HELD SPEAR] EquipmentArt.spear - Rotate and reduce the supplied diagonal weapon once per tier.
    static BufferedImage spear(EquipmentLoadout.Level level){
        int index=level.ordinal();
        if(SPEARS[index]!=null)return SPEARS[index];
        if(level==EquipmentLoadout.Level.BASIC)return SPEARS[index]=SpearAttack.icon();
        BufferedImage vertical=rotate(SHOP.icon(EquipmentLoadout.Gear.SPEAR,level),-Math.PI/4);
        // Keep a full-length pole even when the legendary banner makes the source wider.
        BufferedImage source=trim(vertical),pole=new BufferedImage(15,54,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=pole.createGraphics();
        try{
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int width=Math.min(pole.getWidth(),Math.max(1,(int)Math.round(source.getWidth()*54.0/source.getHeight())));
            g.drawImage(source,(pole.getWidth()-width)/2,0,width,pole.getHeight(),null);
        }finally{g.dispose();}
        return SPEARS[index]=pole;
    }

    // [BLOCK: CACHED HELD SHIELD] EquipmentArt.shield - Reduce the supplied shield face once per tier.
    static BufferedImage shield(EquipmentLoadout.Level level){
        int index=level.ordinal();
        if(SHIELDS[index]!=null)return SHIELDS[index];
        if(level==EquipmentLoadout.Level.BASIC)return SHIELDS[index]=WoodenShieldArt.face(false);
        return SHIELDS[index]=fit(trim(SHOP.icon(EquipmentLoadout.Gear.SHIELD,level)),26,30);
    }

    // [BLOCK: PURCHASED ATTACK EFFECT] EquipmentArt.spearEffect - Reuse the supplied Rare/Legendary crescent sequence.
    static BufferedImage spearEffect(EquipmentLoadout.Level level,int frame){
        return level==EquipmentLoadout.Level.BASIC?null:SHOP.effect(EquipmentLoadout.Gear.SPEAR,level,frame);
    }

    // [BLOCK: PURCHASED GUARD EFFECT] EquipmentArt.shieldEffect - Reuse the supplied Rare/Legendary barrier sequence.
    static BufferedImage shieldEffect(EquipmentLoadout.Level level,int frame){
        return level==EquipmentLoadout.Level.BASIC?null:SHOP.effect(EquipmentLoadout.Gear.SHIELD,level,frame);
    }

    // [BLOCK: EQUIPMENT REPLACEMENT] Remove the baked gear before drawing a purchased model at its original grip.
    static BufferedImage carried(BufferedImage frame,Player.Dir dir,
            EquipmentLoadout.Level spearLevel,EquipmentLoadout.Level shieldLevel,boolean guard){
        if(spearLevel==EquipmentLoadout.Level.BASIC&&shieldLevel==EquipmentLoadout.Level.BASIC)return frame;
        BufferedImage[][] variants=CARRIED.computeIfAbsent(frame,key->new BufferedImage[SPEARS.length][SHIELDS.length]);
        int s=spearLevel.ordinal(),d=shieldLevel.ordinal();
        if(variants[s][d]==null)variants[s][d]=compose(frame,dir,spearLevel,shieldLevel,guard);
        return variants[s][d];
    }

    static void clearCarriedCache(){CARRIED.clear();}

    /** Locations are in the original 64-pixel pose. Left-facing side frames use mirrored coordinates. */
    private static BufferedImage compose(BufferedImage original,Player.Dir dir,
            EquipmentLoadout.Level spearLevel,EquipmentLoadout.Level shieldLevel,boolean guard){
        boolean replaceSpear=spearLevel!=EquipmentLoadout.Level.BASIC;
        boolean replaceShield=shieldLevel!=EquipmentLoadout.Level.BASIC;
        boolean left=dir==Player.Dir.LEFT, profile=left||dir==Player.Dir.RIGHT;
        boolean sideGuard=guard&&profile;
        BufferedImage result=new BufferedImage(original.getWidth(),original.getHeight(),BufferedImage.TYPE_INT_ARGB);
        Point tip=sideGuard?new Point(60,39):metalCenter(original,left,4,33,49,15);
        Point butt=sideGuard?new Point(22,44):metalCenter(original,left,53,64,47,61);
        int gripY=sideGuard?42:43;
        double gripFactor=(gripY-tip.y)/(double)Math.max(1,butt.y-tip.y);
        double gripX=sideGuard?37:tip.x+(butt.x-tip.x)*gripFactor;
        int shieldX=sideGuard?47:guard&&dir==Player.Dir.DOWN?26:22;
        int shieldY=sideGuard?41:guard&&dir==Player.Dir.DOWN?45:43;
        BufferedImage hand=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<original.getHeight();y++)for(int x=0;x<original.getWidth();x++){
            int pixel=original.getRGB(x,y);
            if((pixel>>>24)==0)continue;
            int localX=left?63-(x-SOURCE_X):x-SOURCE_X, localY=y-SOURCE_Y;
            if(localX<0||localX>=64||localY<0||localY>=64){result.setRGB(x,y,pixel);continue;}
            double along=(localY-tip.y)/(double)Math.max(1,butt.y-tip.y);
            double shaft=tip.x+(butt.x-tip.x)*along;
            boolean spearPixel=sideGuard?
                    ((localX>=53&&localY>=35&&localY<=44)||(localX<=25&&localY>=42&&localY<=46)):
                    Math.abs(localX-shaft)<=(localY<=32?6:4.5);
            double dx=(localX-shieldX)/(sideGuard?8.5:9.0),dy=(localY-shieldY)/(sideGuard?11:11.5);
            boolean shieldPixel=dx*dx+dy*dy<=1.1;
            if(replaceSpear&&Math.abs(localX-gripX)<=4&&Math.abs(localY-gripY)<=3&&skin(pixel)){
                hand.setRGB(x-SOURCE_X,localY,pixel);
            }
            if((replaceSpear&&spearPixel)||(replaceShield&&shieldPixel&&!skin(pixel)&&!brightCloth(pixel)))continue;
            result.setRGB(x,y,pixel);
        }
        Graphics2D g=result.createGraphics();
        try{
            g.translate(SOURCE_X,SOURCE_Y);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            if(replaceSpear){
                Graphics2D pole=(Graphics2D)g.create();
                try{
                    pole.translate(left?63-gripX:gripX,gripY);
                    double aim=Math.atan2(tip.y-gripY,(tip.x-gripX)*(left?-1:1))+Math.PI/2;
                    pole.rotate(aim);
                    int length=sideGuard?48:Math.min(54,butt.y-tip.y+4);
                    int gripToTip=sideGuard?31:gripY-tip.y+2;
                    pole.drawImage(spear(spearLevel),-7,-gripToTip,14,length,null);
                }finally{pole.dispose();}
                g.drawImage(hand,0,0,null);
            }
            if(replaceShield){
                int center=left?63-shieldX:shieldX;
                int w=sideGuard?18:guard?21:16,h=guard?25:22;
                g.drawImage(shield(shieldLevel),center-w/2,shieldY-h/2,w,h,null);
            }
        }finally{g.dispose();}
        return result;
    }

    private static Point metalCenter(BufferedImage image,boolean left,int yMin,int yMax,int fallbackX,int fallbackY){
        int xSum=0,ySum=0,count=0;
        for(int y=yMin;y<yMax;y++)for(int x=41;x<61;x++){
            int pixel=image.getRGB(SOURCE_X+(left?63-x:x),SOURCE_Y+y);
            int r=pixel>>16&255,g=pixel>>8&255,b=pixel&255;
            if((pixel>>>24)>96&&b>75&&g>r*1.4&&b>g*.85){xSum+=x;ySum+=y;count++;}
        }
        return count==0?new Point(fallbackX,fallbackY):new Point(xSum/count,yMin<10?Math.max(9,ySum/count-8):ySum/count);
    }

    private static boolean skin(int pixel){
        int r=pixel>>16&255,g=pixel>>8&255,b=pixel&255;
        return r>=136&&g>=76&&r>g*1.22&&r<g*2.1&&g>b*1.3;
    }

    private static boolean brightCloth(int pixel){
        int r=pixel>>16&255,g=pixel>>8&255;
        return r>=180&&g<r*.42;
    }

    private static BufferedImage rotate(BufferedImage source,double angle){
        int size=(int)Math.ceil(Math.hypot(source.getWidth(),source.getHeight()));
        BufferedImage out=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);Graphics2D g=out.createGraphics();
        try{
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            AffineTransform transform=new AffineTransform();transform.translate(size/2.0,size/2.0);transform.rotate(angle);
            transform.translate(-source.getWidth()/2.0,-source.getHeight()/2.0);g.drawImage(source,transform,null);
        }finally{g.dispose();}
        return out;
    }

    private static BufferedImage trim(BufferedImage source){
        int minX=source.getWidth(),minY=source.getHeight(),maxX=-1,maxY=-1;
        for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++)if((source.getRGB(x,y)>>>24)>24){
            minX=Math.min(minX,x);minY=Math.min(minY,y);maxX=Math.max(maxX,x);maxY=Math.max(maxY,y);
        }
        if(maxX<minX)return new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
        BufferedImage out=new BufferedImage(maxX-minX+1,maxY-minY+1,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=out.createGraphics();try{g.drawImage(source,-minX,-minY,null);}finally{g.dispose();}return out;
    }

    private static BufferedImage fit(BufferedImage source,int width,int height){
        double scale=Math.min((double)width/source.getWidth(),(double)height/source.getHeight());
        int w=Math.max(1,(int)Math.round(source.getWidth()*scale)),h=Math.max(1,(int)Math.round(source.getHeight()*scale));
        BufferedImage out=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);Graphics2D g=out.createGraphics();
        try{g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(source,(width-w)/2,(height-h)/2,w,h,null);}finally{g.dispose();}return out;
    }
}
