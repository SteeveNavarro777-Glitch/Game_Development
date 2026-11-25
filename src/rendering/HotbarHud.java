/* FILE PURPOSE: HOTBAR UI | Centered nine-slot equipment dock, consistent icon bounds, quantity badges and drink progress.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.*;
import java.awt.image.BufferedImage;

/** Box geometry is shared with scaled pointer hit tests; icons are cached by PotionArt. */
final class HotbarHud {
    static final int CELL=32,GAP=3,WIDTH=Hotbar.SIZE*CELL+(Hotbar.SIZE-1)*GAP;
    private static final Font SMALL=new Font("Monospaced",Font.BOLD,8),LABEL=new Font("Monospaced",Font.BOLD,9);
    private static final Color INK=new Color(0x10191C),RIM=new Color(0x756851),GOLD=new Color(0xF4D08A),TEXT=new Color(0xFAEBCF),
        PANEL=new Color(13,21,23,225),FACE=new Color(0x293735),EMPTY=new Color(0x202D2C),SELECTED=new Color(0x3B4336),
        EDGE=new Color(0x49564B),DIM=new Color(0xA6B09E),MISSING=new Color(0xA18B7C),KEY=new Color(14,21,23,225);
    private static final Color[] TINTS={new Color(0x8DE148),new Color(0xFFBE49),new Color(0x6DD7FF)};
    // [BLOCK: STATIC HUD] HotbarHud.HotbarHud - Prevent instances of the shared item dock renderer.
    private HotbarHud(){}
    // [BLOCK: EQUIPPED HOTBAR ICON] HotbarHud.icon - Keep the dock synchronized with the tier equipped from inventory.
    private static BufferedImage icon(Player player,Inventory.Item item){
        if(item==Inventory.Item.SPEAR)return EquipmentArt.icon(EquipmentLoadout.Gear.SPEAR,player.equipment().level(EquipmentLoadout.Gear.SPEAR));
        if(item==Inventory.Item.SHIELD)return EquipmentArt.icon(EquipmentLoadout.Gear.SHIELD,player.equipment().level(EquipmentLoadout.Gear.SHIELD));
        return PotionArt.icon(item);
    }
    // [BLOCK: SHARED LAYOUT] HotbarHud.left - Center all nine slots as a single dock.
    static int left(int width){return (width-WIDTH)/2;}
    // [BLOCK: SAFE AREA] HotbarHud.top - Leave a clear bottom margin at every window size.
    static int top(int height){return height-47;}
    // [BLOCK: SHARED HIT GEOMETRY] HotbarHud.slotAt - Match only the visible slot interior and reject gaps, text and margins.
    static int slotAt(int x,int y,int width,int height){
        int dx=x-left(width);if(dx<0||dx>=WIDTH||y<top(height)||y>=top(height)+CELL)return -1;
        int slot=dx/(CELL+GAP);return dx%(CELL+GAP)<CELL?slot:-1;
    }
    // [BLOCK: EQUIPMENT HUD] HotbarHud.draw - Place keys and counts in their own corners and show only one compact context label.
    static void draw(Graphics2D target,Player player,int width,int height){
        Graphics2D g=(Graphics2D)target.create();
        try{
            Hotbar bar=player.hotbar();Inventory bag=player.inventory();int left=left(width),top=top(height);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setColor(PANEL);g.fillRect(left-5,top-5,WIDTH+10,CELL+11);g.setColor(RIM);g.drawRect(left-5,top-5,WIDTH+9,CELL+10);g.setFont(SMALL);
            for(int slot=0;slot<Hotbar.SIZE;slot++){
                int x=left+slot*(CELL+GAP);boolean selected=slot==bar.selectedIndex();Inventory.Item item=bar.item(slot);int count=bag.count(item);
                Color accent=item!=null&&item.potion()?TINTS[PotionArt.kind(item)]:GOLD;
                g.setColor(selected?accent:RIM);g.fillRect(x,top,CELL,CELL);g.setColor(INK);g.fillRect(x+1,top+1,CELL-2,CELL-2);
                g.setColor(selected?SELECTED:item==null?EMPTY:FACE);g.fillRect(x+3,top+3,CELL-6,CELL-6);
                g.setColor(selected?accent:EDGE);g.drawLine(x+2,top+2,x+CELL-3,top+2);g.drawLine(x+2,top+2,x+2,top+CELL-3);
                BufferedImage icon=icon(player,item);
                if(icon!=null){if(count==0)g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,.24f));g.drawImage(icon,x+2,top+1,28,28,null);g.setComposite(AlphaComposite.SrcOver);}
                g.setColor(KEY);g.fillRect(x+2,top+2,7,9);g.setColor(selected?accent:DIM);g.drawString(Integer.toString(slot+1),x+3,top+9);
                if(item!=null&&item.potion()){
                    String amount=count>999?"999+":Integer.toString(count);int tw=g.getFontMetrics().stringWidth(amount),tx=x+CELL-3-tw;
                    g.setColor(KEY);g.fillRect(tx-1,top+CELL-10,tw+2,8);g.setColor(count==0?MISSING:TEXT);g.drawString(amount,tx,top+CELL-3);
                }
                if(selected){g.setColor(accent);g.fillRect(x,top+CELL+2,CELL,2);}
                if(selected&&player.drinking()){
                    g.setColor(INK);g.fillRect(x+3,top+CELL-3,CELL-6,2);g.setColor(accent);g.fillRect(x+3,top+CELL-3,(int)Math.round((CELL-6)*player.drinkProgress()),2);
                }
            }
            Inventory.Item item=bar.selectedItem();String label=player.itemLabel(item).toUpperCase(java.util.Locale.ROOT);
            if(player.drinking())label="DRINKING "+label;else if(player.itemMessageTime()>0)label=player.itemMessage();
            g.setFont(LABEL);int tw=g.getFontMetrics().stringWidth(label),x=(width-tw)/2,y=top-11;
            g.setColor(PANEL);g.fillRect(x-5,y-10,tw+10,14);g.setColor(item!=null&&item.potion()?TINTS[PotionArt.kind(item)]:TEXT);g.drawString(label,x,y);
        }finally{g.dispose();}
    }
}
