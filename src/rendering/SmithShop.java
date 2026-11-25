import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** Blacksmith shop overlay and its fixed array of four upgrade offers. */
final class SmithShop {
    static final class Offer {
        final EquipmentLoadout.Gear gear;
        final EquipmentLoadout.Level level;
        final int price;

        Offer(EquipmentLoadout.Gear gear, EquipmentLoadout.Level level, int price) {
            this.gear = gear;
            this.level = level;
            this.price = price;
        }
    }

    private static final Offer[] OFFERS = {
        new Offer(EquipmentLoadout.Gear.SPEAR, EquipmentLoadout.Level.RARE, 20),
        new Offer(EquipmentLoadout.Gear.SPEAR, EquipmentLoadout.Level.LEGENDARY, 35),
        new Offer(EquipmentLoadout.Gear.SHIELD, EquipmentLoadout.Level.RARE, 18),
        new Offer(EquipmentLoadout.Gear.SHIELD, EquipmentLoadout.Level.LEGENDARY, 32)
    };
    private static final Rectangle[] CARDS = {
        new Rectangle(52, 93, 180, 76), new Rectangle(248, 93, 180, 76),
        new Rectangle(52, 181, 180, 76), new Rectangle(248, 181, 180, 76)
    };

    private final SmithShopArt art = new SmithShopArt();
    private boolean open;
    private int selected;
    private String message = "Select an upgrade";

    void open() {
        open = true;
        selected = 0;
        message = "Purchased gear goes to inventory [I] before use";
    }

    void close() {
        open = false;
    }

    boolean isOpen() {
        return open;
    }

    int selected() {
        return selected;
    }

    // [BLOCK: SHOP FEEDBACK READ] SmithShop.message - Expose purchase guidance for focused regression tests.
    String message(){return message;}

    Offer offer(int index) {
        return OFFERS[Math.max(0, Math.min(index, OFFERS.length - 1))];
    }

    void select(int index) {
        if (index >= 0 && index < OFFERS.length) {
            selected = index;
        }
    }

    void cycle(int delta) {
        selected = Math.floorMod(selected + delta, OFFERS.length);
    }

    boolean purchase(Player player) {
        Offer offer = OFFERS[selected];
        EquipmentLoadout loadout = player.equipment();
        if (loadout.owns(offer.gear, offer.level)) {
            message = offer.level.label + " " + offer.gear.label + " owned - open inventory [I] to equip";
            return false;
        }
        if (!loadout.canUpgrade(offer.gear, offer.level)) {
            message = "Forge the Rare " + offer.gear.label + " first";
            return false;
        }
        if (!player.buyUpgrade(offer.gear, offer.level, offer.price)) {
            message = "Not enough coins";
            return false;
        }
        message = "PURCHASED - OPEN INVENTORY [I] TO EQUIP";
        return true;
    }

    int cardAt(Point point) {
        for (int i = 0; i < CARDS.length; i++) {
            if (CARDS[i].contains(point)) {
                return i;
            }
        }
        return -1;
    }

    void draw(Graphics2D g, Player player) {
        g.setColor(new Color(3, 7, 8, 178));
        g.fillRect(0, 0, 480, 320);
        g.setColor(new Color(25, 25, 23, 245));
        g.fillRoundRect(35, 35, 410, 255, 12, 12);
        g.setColor(new Color(197, 151, 68));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(35, 35, 410, 255, 12, 12);

        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        g.setColor(new Color(255, 224, 145));
        g.drawString("VILLAGE SMITH", 52, 65);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        g.setColor(new Color(255, 202, 62));
        g.drawString("COINS  " + player.coins(), 337, 63);

        for (int i = 0; i < OFFERS.length; i++) {
            drawOffer(g, player, i);
        }
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        g.setColor(new Color(226, 226, 206));
        g.drawString(message, 52, 274);
        g.setColor(new Color(160, 174, 166));
        g.drawString("1-4 SELECT   ENTER/CLICK BUY   E/ESC CLOSE", 183, 286);
    }

    private void drawOffer(Graphics2D g, Player player, int index) {
        Offer offer = OFFERS[index];
        Rectangle card = CARDS[index];
        boolean owned = player.equipment().owns(offer.gear, offer.level);
        boolean equipped = player.equipment().isEquipped(offer.gear, offer.level);
        boolean available = player.equipment().canUpgrade(offer.gear, offer.level);
        Color border = index == selected ? new Color(255, 206, 77)
                : new Color(92, 109, 102);
        g.setColor(new Color(41, 51, 48));
        g.fillRoundRect(card.x, card.y, card.width, card.height, 8, 8);
        g.setColor(border);
        g.setStroke(new BasicStroke(index == selected ? 3 : 1));
        g.drawRoundRect(card.x, card.y, card.width, card.height, 8, 8);

        BufferedImage icon = art.icon(offer.gear, offer.level);
        g.drawImage(icon, card.x + 6, card.y + 9, null);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
        g.setColor(offer.level == EquipmentLoadout.Level.LEGENDARY
                ? new Color(255, 126, 42) : new Color(83, 194, 255));
        g.drawString(offer.level.label.toUpperCase(), card.x + 68, card.y + 23);
        g.setColor(Color.WHITE);
        g.drawString(offer.gear.label.toUpperCase(), card.x + 68, card.y + 39);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        if (owned) {
            g.setColor(new Color(112, 224, 128));
            g.drawString(equipped?"EQUIPPED":"OWNED / INVENTORY", card.x + 68, card.y + 59);
        } else if (available) {
            g.setColor(new Color(255, 211, 77));
            g.drawString(offer.price + " COINS", card.x + 68, card.y + 59);
        } else {
            g.setColor(new Color(150, 150, 150));
            g.drawString("LOCKED", card.x + 68, card.y + 59);
        }
    }
}
