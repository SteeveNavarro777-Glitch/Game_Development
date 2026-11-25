/* FILE PURPOSE: PLAYER SPRITE CACHE | Whole original Agyu frames plus non-destructive potion overlays.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * The supplied movement and reaction frames share one foot anchor. Equipment upgrades
 * use cached variants of those same poses, with localized gear replacements.
 */
final class HeldItemArt {
    static final int POSES=8,WIDTH=128,HEIGHT=128,FOOT_X=64,FOOT_Y=92;
    static final int GUARD_SHIELD_Y=-22;
    private static final int SOURCE_FOOT_X=32,SOURCE_FOOT_Y=63;
    private static final Inventory.Item[] POTIONS={Inventory.Item.HEALTH_POTION,
            Inventory.Item.ENERGY_POTION,Inventory.Item.SHIELD_POTION};

    // [SECTION: DSA - MULTIDIMENSIONAL ARRAYS] Direction and phase select a cached whole-frame sprite.
    private static BufferedImage[][] idleFrames,walkFrames,runFrames,hurtFrames;
    private static BufferedImage[] guardFrames;
    private static BufferedImage[][][][] potionIdle,potionWalk,potionRun;
    private static BufferedImage[][][][] drinkIdle,drinkWalk;

    // [BLOCK: STATIC CACHE] HeldItemArt.HeldItemArt - Prevent instances of the shared art cache.
    private HeldItemArt(){}

    // [BLOCK: ORIGINAL SPRITE STARTUP] HeldItemArt.load - Cache every supplied frame without replacing body pixels.
    static void load()throws IOException{
        EquipmentArt.clearCarriedCache();
        idleFrames=wrapAll(Assets.idle);
        walkFrames=wrapAll(Assets.walk);
        runFrames=wrapAll(Assets.run);
        hurtFrames=wrapAll(Assets.hurt);
        guardFrames=new BufferedImage[Player.Dir.values().length];
        for(Player.Dir dir:Player.Dir.values())guardFrames[dir.ordinal()]=wrap(ShieldGuard.bodies[dir.ordinal()][0],null,dir,0);

        potionIdle=new BufferedImage[POTIONS.length][4][POSES][];
        potionWalk=new BufferedImage[POTIONS.length][4][POSES][];
        potionRun=new BufferedImage[POTIONS.length][4][POSES][];
        drinkIdle=new BufferedImage[POTIONS.length][4][POSES][];
        drinkWalk=new BufferedImage[POTIONS.length][4][POSES][];
        for(int kind=0;kind<POTIONS.length;kind++)for(Player.Dir dir:Player.Dir.values())for(int phase=0;phase<POSES;phase++){
            Inventory.Item item=POTIONS[kind];int facing=dir.ordinal();
            potionIdle[kind][facing][phase]=single(wrap(Assets.idle[facing][phase],item,dir,0));
            potionWalk[kind][facing][phase]=single(wrap(Assets.walk[facing][phase],item,dir,0));
            potionRun[kind][facing][phase]=single(wrap(Assets.run[facing][phase],item,dir,0));
            drinkIdle[kind][facing][phase]=single(wrap(Assets.idle[facing][phase],item,dir,phase));
            drinkWalk[kind][facing][phase]=single(wrap(Assets.walk[facing][phase],item,dir,phase));
        }
    }

    // [BLOCK: ARRAY PREPARATION] HeldItemArt.wrapAll - Preserve all direction and animation entries at one common foot anchor.
    private static BufferedImage[][] wrapAll(BufferedImage[][] sources){
        BufferedImage[][] result=new BufferedImage[sources.length][];
        for(int dir=0;dir<sources.length;dir++){
            result[dir]=new BufferedImage[sources[dir].length];
            for(int phase=0;phase<sources[dir].length;phase++)
                result[dir][phase]=wrap(sources[dir][phase],null,Player.Dir.values()[dir],0);
        }
        return result;
    }

    // [BLOCK: WHOLE-FRAME COMPOSITION] HeldItemArt.wrap - Copy the complete native PNG and optionally add one bottle.
    private static BufferedImage wrap(BufferedImage source,Inventory.Item potion,Player.Dir dir,int drinkPose){
        BufferedImage image=new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();
        try{
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            // Put the bottle behind the authored body so original fingers, face
            // and clothing remain intact and naturally occlude the held item.
            if(potion!=null)drawBottle(g,potion,dir,drinkPose);
            g.drawImage(source,FOOT_X-SOURCE_FOOT_X,FOOT_Y-SOURCE_FOOT_Y,null);
        }finally{g.dispose();}
        // Source artwork contains many semi-transparent edge pixels. SrcOver can
        // blend those with a bottle underneath, so restore every authored pixel
        // exactly while retaining the bottle only in transparent space.
        if(potion!=null)for(int sy=0;sy<source.getHeight();sy++)for(int sx=0;sx<source.getWidth();sx++){
            int pixel=source.getRGB(sx,sy);
            if((pixel>>>24)!=0)image.setRGB(FOOT_X-SOURCE_FOOT_X+sx,FOOT_Y-SOURCE_FOOT_Y+sy,pixel);
        }
        return image;
    }

    // [BLOCK: POTION OVERLAY] HeldItemArt.drawBottle - Move only the selected bottle from the hand to the mouth.
    private static void drawBottle(Graphics2D target,Inventory.Item potion,Player.Dir dir,int pose){
        double lift=switch(pose){case 3->.32;case 4->.68;case 5->1.0;case 6->.48;default->0;};
        int handX=switch(dir){case DOWN->13;case UP->-13;case RIGHT->12;case LEFT->-12;};
        int mouthX=switch(dir){case DOWN->5;case UP->-5;case RIGHT->7;case LEFT->-7;};
        int handY=-22,mouthY=-39;
        double x=handX+(mouthX-handX)*lift,y=handY+(mouthY-handY)*lift;
        double angle=(dir==Player.Dir.DOWN||dir==Player.Dir.LEFT?1:-1)*lift*1.35;
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.translate(FOOT_X+Math.rint(x),FOOT_Y+Math.rint(y));g.rotate(angle);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(PotionArt.icon(potion),-8,-8,16,16,null);
        }finally{g.dispose();}
    }

    // A final one-element dimension keeps the potion caches uniform without allocating during drawing.
    private static BufferedImage[] single(BufferedImage image){return new BufferedImage[]{image};}

    // [BLOCK: ITEM LOOKUP] HeldItemArt.potionKind - Map consumables to compact array rows.
    private static int potionKind(Inventory.Item item){
        if(item==null||!item.potion())return -1;
        return PotionArt.kind(item);
    }

    // [BLOCK: CACHED IDLE FRAME] HeldItemArt.frame - Return an original idle frame, with a bottle only for a selected potion.
    static BufferedImage frame(Inventory.Item item,Player.Dir dir,int pose){
        int phase=Math.floorMod(pose,POSES),kind=potionKind(item),facing=dir.ordinal();
        return kind<0?idleFrames[facing][phase]:potionIdle[kind][facing][phase][0];
    }

    // [BLOCK: CACHED WALK FRAME] HeldItemArt.walkFrame - Return the complete supplied walk frame for this phase.
    static BufferedImage walkFrame(Inventory.Item item,Player.Dir dir,int phase){
        phase=Math.floorMod(phase,POSES);int kind=potionKind(item),facing=dir.ordinal();
        return kind<0?walkFrames[facing][phase]:potionWalk[kind][facing][phase][0];
    }

    // [BLOCK: CACHED RUN FRAME] HeldItemArt.runFrame - Return the complete supplied run frame for this phase.
    static BufferedImage runFrame(Inventory.Item item,Player.Dir dir,int phase){
        phase=Math.floorMod(phase,POSES);int kind=potionKind(item),facing=dir.ordinal();
        return kind<0?runFrames[facing][phase]:potionRun[kind][facing][phase][0];
    }

    // [BLOCK: CACHED HURT FRAME] HeldItemArt.hurtFrame - Use the original directional reaction sequence unchanged.
    static BufferedImage hurtFrame(Player.Dir dir,int phase){
        BufferedImage[] sequence=hurtFrames[dir.ordinal()];
        return sequence[Math.floorMod(phase,sequence.length)];
    }

    // [BLOCK: CACHED AFTERIMAGE] HeldItemArt.ghost - Reuse the complete native run sprite for dash trails.
    static BufferedImage ghost(Inventory.Item item,Player.Dir dir,int pose){return Assets.run[dir.ordinal()][Math.floorMod(pose,POSES)];}

    // [BLOCK: CACHED DRAW] HeldItemArt.draw - Draw an original idle phase about the shared native foot anchor.
    static void draw(Graphics2D g,int x,int y,Inventory.Item item,Player.Dir dir,int pose){
        drawFrame(g,x,y,frame(item,dir,pose));
    }

    // [BLOCK: EQUIPPED IDLE DRAW] HeldItemArt.draw - Keep the authored body and replace only the purchased gear layer.
    static void draw(Graphics2D g,int x,int y,Inventory.Item item,Player.Dir dir,int pose,
            EquipmentLoadout.Level spearLevel,EquipmentLoadout.Level shieldLevel){
        drawEquipmentFrame(g,x,y,frame(item,dir,pose),dir,spearLevel,shieldLevel,false);
    }

    // [BLOCK: WALK DRAW] HeldItemArt.drawWalk - Draw all eight authored walking frames by traveled distance.
    static void drawWalk(Graphics2D g,int x,int y,Inventory.Item item,Player.Dir dir,double distance){
        drawFrame(g,x,y,walkFrame(item,dir,motionPhase(distance,false)));
    }

    // [BLOCK: EQUIPPED WALK DRAW] HeldItemArt.drawWalk - Preserve all eight source phases while showing the equipped tiers.
    static void drawWalk(Graphics2D g,int x,int y,Inventory.Item item,Player.Dir dir,double distance,
            EquipmentLoadout.Level spearLevel,EquipmentLoadout.Level shieldLevel){
        drawEquipmentFrame(g,x,y,walkFrame(item,dir,motionPhase(distance,false)),dir,spearLevel,shieldLevel,false);
    }

    // [BLOCK: RUN DRAW] HeldItemArt.drawRun - Draw all eight authored running frames by traveled distance.
    static void drawRun(Graphics2D g,int x,int y,Inventory.Item item,Player.Dir dir,double distance){
        drawFrame(g,x,y,runFrame(item,dir,motionPhase(distance,true)));
    }

    // [BLOCK: EQUIPPED RUN DRAW] HeldItemArt.drawRun - Keep sprint motion and update only the held equipment.
    static void drawRun(Graphics2D g,int x,int y,Inventory.Item item,Player.Dir dir,double distance,
            EquipmentLoadout.Level spearLevel,EquipmentLoadout.Level shieldLevel){
        drawEquipmentFrame(g,x,y,runFrame(item,dir,motionPhase(distance,true)),dir,spearLevel,shieldLevel,false);
    }

    // [BLOCK: HURT DRAW] HeldItemArt.drawHurt - Draw the original reaction animation without equipment replacement.
    static void drawHurt(Graphics2D g,int x,int y,Player.Dir dir,double time){
        drawFrame(g,x,y,hurtFrame(dir,(int)(time*10)));
    }

    static void drawHurt(Graphics2D g,int x,int y,Player.Dir dir,double time,
            EquipmentLoadout.Level spearLevel,EquipmentLoadout.Level shieldLevel){
        drawEquipmentFrame(g,x,y,hurtFrame(dir,(int)(time*10)),dir,spearLevel,shieldLevel,false);
    }

    // [BLOCK: COMPLETE GUARD FRAME] HeldItemArt.guardFrame - Keep the supplied shield-and-spear guard pose whole and stable.
    static BufferedImage guardFrame(Inventory.Item held,Player.Dir dir,double distance,boolean moving){return guardFrames[dir.ordinal()];}

    // [BLOCK: GUARD DRAW] HeldItemArt.drawGuard - Draw the authored guard PNG without grafted walking feet.
    static void drawGuard(Graphics2D g,int x,int y,Inventory.Item held,Player.Dir dir,double distance,boolean moving){
        drawFrame(g,x,y,guardFrame(held,dir,distance,moving));
    }

    // [BLOCK: EQUIPPED GUARD DRAW] HeldItemArt.drawGuard - Replace the guard spear and shield without altering Agyu's pose.
    static void drawGuard(Graphics2D g,int x,int y,Inventory.Item held,Player.Dir dir,double distance,boolean moving,
            EquipmentLoadout.Level spearLevel,EquipmentLoadout.Level shieldLevel){
        drawEquipmentFrame(g,x,y,guardFrame(held,dir,distance,moving),dir,spearLevel,shieldLevel,true);
    }

    // [BLOCK: PROFILE COMPATIBILITY] HeldItemArt.isProfile - Identify the source strip used for left and right walking.
    static boolean isProfile(Player.Dir dir){return dir==Player.Dir.LEFT||dir==Player.Dir.RIGHT;}

    // [BLOCK: PROFILE COMPATIBILITY] HeldItemArt.profileFrame - Retain the former API while returning a complete walk frame.
    static BufferedImage profileFrame(Inventory.Item item,Player.Dir dir,double distance){
        return walkFrame(item,dir,motionPhase(distance,false));
    }

    // [BLOCK: PROFILE COMPATIBILITY] HeldItemArt.drawProfileWalk - Draw the complete original profile frame.
    static void drawProfileWalk(Graphics2D g,int x,int y,Inventory.Item item,Player.Dir dir,double distance){
        drawWalk(g,x,y,item,dir,distance);
    }

    // [BLOCK: DRINKING MOTION] HeldItemArt.drawDrinking - Use a whole idle/walk sprite while the bottle reaches the mouth.
    static void drawDrinking(Graphics2D g,int x,int y,Inventory.Item item,Player.Dir dir,int pose,double time,boolean moving){
        int kind=potionKind(item);
        if(kind<0){draw(g,x,y,item,dir,walkPose(time,false));return;}
        int phase=Math.floorMod(pose,POSES),facing=dir.ordinal();
        BufferedImage image=(moving?drinkWalk:drinkIdle)[kind][facing][phase][0];
        drawFrame(g,x,y,image);
    }

    // [BLOCK: EQUIPPED DRINK DRAW] HeldItemArt.drawDrinking - Retain the sip animation and visible equipped gear.
    static void drawDrinking(Graphics2D g,int x,int y,Inventory.Item item,Player.Dir dir,int pose,double time,boolean moving,
            EquipmentLoadout.Level spearLevel,EquipmentLoadout.Level shieldLevel){
        int kind=potionKind(item);
        if(kind<0){draw(g,x,y,item,dir,walkPose(time,false),spearLevel,shieldLevel);return;}
        int phase=Math.floorMod(pose,POSES),facing=dir.ordinal();
        BufferedImage image=(moving?drinkWalk:drinkIdle)[kind][facing][phase][0];
        drawEquipmentFrame(g,x,y,image,dir,spearLevel,shieldLevel,false);
    }

    // [BLOCK: FRAME ANCHOR] HeldItemArt.drawFrame - Apply one common foot registration to every state.
    private static void drawFrame(Graphics2D g,int x,int y,BufferedImage image){g.drawImage(image,x-FOOT_X,y-FOOT_Y,null);}

    // [BLOCK: REGISTERED GEAR FRAME] Keep the original feet and use the cached replacement at the same anchor.
    private static void drawEquipmentFrame(Graphics2D g,int x,int y,BufferedImage image,Player.Dir dir,
            EquipmentLoadout.Level spearLevel,EquipmentLoadout.Level shieldLevel,boolean guard){
        drawFrame(g,x,y,EquipmentArt.carried(image,dir,spearLevel,shieldLevel,guard));
    }

    // [BLOCK: DISTANCE PHASE] HeldItemArt.motionPhase - Advance all eight frames without time-based foot sliding.
    static int motionPhase(double distance,boolean running){
        int pixelsPerFrame=running?6:5;
        return Math.floorMod((int)Math.floor(distance/pixelsPerFrame),POSES);
    }

    // [BLOCK: TIME PHASE] HeldItemArt.walkPose - Advance all eight original frames for idle previews and dash trails.
    static int walkPose(double time,boolean running){return Math.floorMod((int)(time*(running?12:8)),POSES);}

    // [BLOCK: SHIELD EFFECT ANCHOR] HeldItemArt.shieldX - Place block feedback over the authored shield face.
    static int shieldX(Player.Dir dir){return switch(dir){case DOWN->11;case UP->-10;case RIGHT->9;case LEFT->-10;};}
}
