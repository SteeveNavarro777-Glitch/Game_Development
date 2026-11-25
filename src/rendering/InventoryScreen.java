/* FILE PURPOSE: INVENTORY UI | Paused bag inspection, explicit smith gear ownership and nine-slot assignment.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.image.BufferedImage;

/** Input and rendering share one layout; item counts and equipment ownership stay encapsulated. */
final class InventoryScreen {
    enum Action { NONE, ITEM, SLOT, EQUIP, USE, CLEAR, CLOSE, TURN }
    record Command(Action action,int index) {}
    private record Entry(Inventory.Item item,EquipmentLoadout.Gear gear,EquipmentLoadout.Level level,String name) {
        boolean equipment(){return gear!=null;}
    }
    // [SECTION: DSA - ARRAY] Every equipment tier occupies a concrete inventory card instead of hiding behind one generic item.
    private static final Entry[] ENTRIES={
        new Entry(Inventory.Item.SPEAR,EquipmentLoadout.Gear.SPEAR,EquipmentLoadout.Level.BASIC,"BASIC SPEAR"),
        new Entry(Inventory.Item.SPEAR,EquipmentLoadout.Gear.SPEAR,EquipmentLoadout.Level.RARE,"RARE SPEAR"),
        new Entry(Inventory.Item.SPEAR,EquipmentLoadout.Gear.SPEAR,EquipmentLoadout.Level.LEGENDARY,"LEGENDARY SPEAR"),
        new Entry(Inventory.Item.SHIELD,EquipmentLoadout.Gear.SHIELD,EquipmentLoadout.Level.BASIC,"WOODEN SHIELD"),
        new Entry(Inventory.Item.SHIELD,EquipmentLoadout.Gear.SHIELD,EquipmentLoadout.Level.RARE,"RARE SHIELD"),
        new Entry(Inventory.Item.SHIELD,EquipmentLoadout.Gear.SHIELD,EquipmentLoadout.Level.LEGENDARY,"LEGENDARY SHIELD"),
        new Entry(Inventory.Item.HEALTH_POTION,null,null,"HEALTH POTION"),
        new Entry(Inventory.Item.ENERGY_POTION,null,null,"ENERGY POTION"),
        new Entry(Inventory.Item.SHIELD_POTION,null,null,"SHIELD POTION")
    };
    private static final Font TITLE=new Font("Monospaced",Font.BOLD,13),TEXT=new Font("Monospaced",Font.BOLD,9),
        TINY=new Font("Monospaced",Font.PLAIN,8);
    private static final Color INK=new Color(0x111C20),FACE=new Color(0x263638),RIM=new Color(0x6C705C),
        GOLD=new Color(0xF1CC85),CREAM=new Color(0xF6EAD3),DIM=new Color(0xA5B8AD);
    private static final int CARD_X=160,CARD_Y=69,CARD_W=88,CARD_H=48,CARD_GAP_X=91,CARD_GAP_Y=51,
        BUTTON_Y=249,SLOTS_Y=277;
    private int selected;
    private Player.Dir facing=Player.Dir.RIGHT;

    // [BLOCK: BAG SELECTION] InventoryScreen.open - Inspect the selected quick item and its actually equipped smith tier.
    void open(Player player){
        Inventory.Item item=player.hotbar().selectedItem();facing=Player.Dir.RIGHT;
        if(item==Inventory.Item.SPEAR)selected=entryIndex(EquipmentLoadout.Gear.SPEAR,player.equipment().level(EquipmentLoadout.Gear.SPEAR));
        else if(item==Inventory.Item.SHIELD)selected=entryIndex(EquipmentLoadout.Gear.SHIELD,player.equipment().level(EquipmentLoadout.Gear.SHIELD));
        else selected=entryIndex(item==null?Inventory.Item.SPEAR:item);
    }
    // [BLOCK: ARRAY LOOKUP] InventoryScreen.item - Resolve the selected catalog card to the existing inventory item type.
    Inventory.Item item(){return ENTRIES[selected].item;}
    // [BLOCK: COMPATIBLE BAG SELECTION] InventoryScreen.select - Select a generic item while preserving older keyboard/test callers.
    void select(int itemIndex){
        Inventory.Item[] values=Inventory.Item.values();
        if(itemIndex>=0&&itemIndex<values.length)selected=entryIndex(values[itemIndex]);
    }
    // [BLOCK: EXPLICIT CATALOG SELECTION] InventoryScreen.selectEntry - Select one visible tier/card directly.
    void selectEntry(int index){if(index>=0&&index<ENTRIES.length)selected=index;}
    // [BLOCK: EQUIPMENT TIER SELECTION] InventoryScreen.selectLevel - Compatibility path for selecting a visible gear tier.
    void selectLevel(int index){
        if(index<0||index>=EquipmentLoadout.Level.values().length)return;
        Entry entry=ENTRIES[selected];
        if(entry.equipment())selected=entryIndex(entry.gear,EquipmentLoadout.Level.values()[index]);
    }
    // [BLOCK: EQUIPMENT TIER LOOKUP] InventoryScreen.selectedLevel - Expose the exact selected gear card tier.
    EquipmentLoadout.Level selectedLevel(){return ENTRIES[selected].equipment()?ENTRIES[selected].level:EquipmentLoadout.Level.BASIC;}
    EquipmentLoadout.Gear selectedGear(){return ENTRIES[selected].gear;}
    // [BLOCK: CYCLIC SELECTION] InventoryScreen.cycle - Move around the complete catalog with bounded mouse-wheel input.
    void cycle(int amount){selected=Math.floorMod(selected+amount,ENTRIES.length);}
    // [BLOCK: PREVIEW FACING] InventoryScreen.turn - Rotate a static equipment preview without updating the live actor.
    void turn(){facing=Player.Dir.values()[(facing.ordinal()+1)%4];}
    // [BLOCK: SHARED HIT GEOMETRY] InventoryScreen.hit - Decode clicks on cards, quick slots and action buttons.
    Command hit(int x,int y){
        if(inside(x,y,424,46,17,17))return new Command(Action.CLOSE,0);
        if(inside(x,y,42,82,106,145))return new Command(Action.TURN,0);
        for(int i=0;i<ENTRIES.length;i++)if(cardBounds(i).contains(x,y))return new Command(Action.ITEM,i);
        for(int i=0;i<Hotbar.SIZE;i++)if(inside(x,y,111+i*29,SLOTS_Y,26,26))return new Command(Action.SLOT,i);
        if(inside(x,y,160,BUTTON_Y,84,19))return new Command(Action.EQUIP,0);
        if(inside(x,y,250,BUTTON_Y,84,19))return new Command(Action.USE,0);
        if(inside(x,y,340,BUTTON_Y,92,19))return new Command(Action.CLEAR,0);
        return new Command(Action.NONE,0);
    }
    private static boolean inside(int x,int y,int left,int top,int width,int height){return x>=left&&y>=top&&x<left+width&&y<top+height;}
    static Rectangle cardBounds(int index){
        return new Rectangle(CARD_X+index%3*CARD_GAP_X,CARD_Y+index/3*CARD_GAP_Y,CARD_W,CARD_H);
    }
    static int entryIndex(Inventory.Item item){
        for(int i=0;i<ENTRIES.length;i++)if(ENTRIES[i].item==item)return i;
        return 0;
    }
    static int entryIndex(EquipmentLoadout.Gear gear,EquipmentLoadout.Level level){
        for(int i=0;i<ENTRIES.length;i++)if(ENTRIES[i].gear==gear&&ENTRIES[i].level==level)return i;
        return 0;
    }
    int entryCount(){return ENTRIES.length;}
    Inventory.Item entryItem(int index){return ENTRIES[index].item;}
    EquipmentLoadout.Level entryLevel(int index){return ENTRIES[index].level;}
    boolean entryOwned(Player player,int index){return owned(player,ENTRIES[index]);}

    // [BLOCK: ITEM PALETTE] InventoryScreen.tint - Match potions and equipment rarity to their gameplay colors.
    private static Color tint(Entry entry){
        if(entry.item==Inventory.Item.HEALTH_POTION)return new Color(0xA0E76B);
        if(entry.item==Inventory.Item.ENERGY_POTION)return new Color(0xFFBF50);
        if(entry.item==Inventory.Item.SHIELD_POTION)return new Color(0x70D9FF);
        if(entry.level==EquipmentLoadout.Level.LEGENDARY)return new Color(0xFF8B35);
        if(entry.level==EquipmentLoadout.Level.RARE)return new Color(0x65CFFF);
        return GOLD;
    }
    private static boolean owned(Player player,Entry entry){
        return entry.equipment()?player.equipment().owns(entry.gear,entry.level):player.inventory().count(entry.item)>0;
    }
    private static boolean equipped(Player player,Entry entry){
        return entry.equipment()&&player.equipment().level(entry.gear)==entry.level;
    }
    // [BLOCK: PANEL FRAME] InventoryScreen.box - Draw a crisp inset box with a one-pixel highlight.
    private static void box(Graphics2D g,int x,int y,int w,int h,Color fill,Color edge){
        g.setColor(INK);g.fillRect(x,y,w,h);g.setColor(edge);g.drawRect(x+1,y+1,w-3,h-3);
        g.setColor(fill);g.fillRect(x+3,y+3,w-6,h-6);
    }
    // [BLOCK: ACTION BUTTON] InventoryScreen.button - Show disabled actions explicitly while retaining shared pointer bounds.
    private static void button(Graphics2D g,int x,int width,String text,boolean enabled){
        box(g,x,BUTTON_Y,width,19,enabled?FACE:INK,enabled?RIM:FACE);
        g.setColor(enabled?CREAM:RIM);g.setFont(TEXT);g.drawString(text,x+(width-g.getFontMetrics().stringWidth(text))/2,BUTTON_Y+13);
    }
    // [BLOCK: EQUIPMENT ICON LOOKUP] InventoryScreen.icon - Render the card's exact smith model instead of the currently equipped model.
    private static BufferedImage icon(Entry entry){return entry.equipment()?EquipmentArt.icon(entry.gear,entry.level):PotionArt.icon(entry.item);}
    private static BufferedImage hotbarIcon(Player player,Inventory.Item item){
        if(item==Inventory.Item.SPEAR)return EquipmentArt.icon(EquipmentLoadout.Gear.SPEAR,player.equipment().level(EquipmentLoadout.Gear.SPEAR));
        if(item==Inventory.Item.SHIELD)return EquipmentArt.icon(EquipmentLoadout.Gear.SHIELD,player.equipment().level(EquipmentLoadout.Gear.SHIELD));
        return PotionArt.icon(item);
    }
    private static String shortName(Entry entry){
        if(entry.item.potion())return switch(entry.item){case HEALTH_POTION->"HEALTH";case ENERGY_POTION->"ENERGY";default->"SHIELD";};
        return entry.level==EquipmentLoadout.Level.BASIC?(entry.gear==EquipmentLoadout.Gear.SPEAR?"BASIC":"WOODEN"):entry.level.label.toUpperCase();
    }

    // [BLOCK: PAUSED INVENTORY DRAW] InventoryScreen.draw - Present all owned gear tiers, held-item preview and editable hotbar.
    void draw(Graphics2D target,Player player){
        Graphics2D g=(Graphics2D)target.create();
        try{
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setColor(new Color(5,12,17,190));g.fillRect(0,0,480,360);
            box(g,32,39,416,286,new Color(0x1B2A2D),RIM);
            g.setColor(GOLD);g.setFont(TITLE);g.drawString("AGYU'S INVENTORY",48,60);
            g.setFont(TINY);g.setColor(DIM);g.drawString("WORLD PAUSED",330,59);
            box(g,424,46,17,17,FACE,RIM);g.setColor(CREAM);g.drawString("X",430,58);
            box(g,42,82,106,145,new Color(0x203134),RIM);
            g.setColor(DIM);g.drawString("ITEM PREVIEW",54,96);
            g.setColor(new Color(0x152427));g.fillOval(66,178,60,10);
            Entry entry=ENTRIES[selected];
            EquipmentLoadout.Level previewSpear=entry.gear==EquipmentLoadout.Gear.SPEAR?entry.level:player.equipment().level(EquipmentLoadout.Gear.SPEAR);
            EquipmentLoadout.Level previewShield=entry.gear==EquipmentLoadout.Gear.SHIELD?entry.level:player.equipment().level(EquipmentLoadout.Gear.SHIELD);
            Graphics2D preview=(Graphics2D)g.create();
            try{preview.translate(92,187);preview.scale(156/128.0,156/128.0);
                if(entry.gear==EquipmentLoadout.Gear.SHIELD)
                    HeldItemArt.drawGuard(preview,0,0,entry.item,facing,0,false,previewSpear,previewShield);
                else HeldItemArt.draw(preview,0,0,entry.item,facing,0,previewSpear,previewShield);
            }finally{preview.dispose();}
            g.setColor(DIM);g.drawString("Click to turn",53,215);

            for(int i=0;i<ENTRIES.length;i++){
                Entry card=ENTRIES[i];Rectangle bounds=cardBounds(i);int x=bounds.x,y=bounds.y;
                boolean has=owned(player,card),active=equipped(player,card),chosen=i==selected;
                box(g,x,y,CARD_W,CARD_H,chosen?new Color(0x3B493B):FACE,chosen?tint(card):active?GOLD:RIM);
                if(!has)g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.24f));
                g.drawImage(icon(card),x+4,y+7,32,32,null);g.setComposite(AlphaComposite.SrcOver);
                g.setFont(TINY);g.setColor(!has?RIM:tint(card));g.drawString(shortName(card),x+39,y+15);
                g.setColor(!has?RIM:CREAM);
                if(card.equipment()){
                    g.drawString(card.gear.label.toUpperCase(),x+39,y+26);
                    g.drawString(active?"EQUIPPED":has?"OWNED":"LOCKED",x+39,y+38);
                }else{
                    g.drawString("POTION",x+39,y+26);g.drawString("x"+player.inventory().count(card.item),x+39,y+38);
                }
            }

            boolean has=owned(player,entry),gear=entry.equipment();
            g.setFont(TEXT);g.setColor(has?tint(entry):RIM);g.drawString(entry.name+(equipped(player,entry)?"  [EQUIPPED]":""),160,229);
            g.setFont(TINY);g.setColor(DIM);
            String info=switch(entry.item){case SPEAR->"Equip this model to replace Agyu's carried spear.";case SHIELD->"Equip this model to replace Agyu's carried shield.";
                case HEALTH_POTION->"Drink to restore two health points.";case ENERGY_POTION->"Drink to restore sprint and dash energy.";case SHIELD_POTION->"Drink to restore magical protection.";};
            g.drawString(has?info:"Buy this equipment from the village smith first.",160,241);
            button(g,160,84,gear?"EQUIP GEAR":"EQUIP",has);
            button(g,250,84,"DRINK [F]",player.inventoryCanUse(entry.item));
            button(g,340,92,"CLEAR SLOT",player.hotbar().selectedItem()!=null);
            g.setFont(TINY);g.setColor(DIM);g.drawString("QUICK SLOTS",42,SLOTS_Y+17);
            for(int i=0;i<Hotbar.SIZE;i++){
                int x=111+i*29;Inventory.Item quick=player.hotbar().item(i);boolean active=player.hotbar().selectedIndex()==i;
                box(g,x,SLOTS_Y,26,26,active?new Color(0x3B493B):FACE,active?GOLD:RIM);
                if(quick!=null){if(player.inventory().count(quick)==0)g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.25f));
                    g.drawImage(hotbarIcon(player,quick),x+2,SLOTS_Y+2,22,22,null);g.setComposite(AlphaComposite.SrcOver);}
                g.setColor(INK);g.fillRect(x+2,SLOTS_Y+2,6,8);g.setColor(active?GOLD:DIM);g.drawString(Integer.toString(i+1),x+2,SLOTS_Y+9);
            }
            g.setFont(TINY);g.setColor(DIM);g.drawString("1-9: slot   Arrows / wheel: browse   Enter: equip   I / Esc: close",42,312);
        }finally{g.dispose();}
    }
}
