/* FILE PURPOSE: DSA HASH MAP / OOP ENCAPSULATION | Validated equipment and potion quantities.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.util.HashMap;
import java.util.Map;

/** Owns item quantities; hotbar slots only reference these item types. */
public final class Inventory {
    public enum Item {
        SPEAR("SPEAR", "LMB / J: combo"), SHIELD("WOODEN SHIELD", "Hold LMB / J: defend"),
        HEALTH_POTION("HEALTH POTION", "LMB / F: restore health"),
        ENERGY_POTION("ENERGY POTION", "LMB / F: restore energy"),
        SHIELD_POTION("SHIELD POTION", "LMB / F: restore shield");
        final String label, hint;
        // [BLOCK: ITEM DATA] Inventory.Item - Keep each equipment or potion name beside its use hint.
        Item(String label, String hint) { this.label=label; this.hint=hint; }
        // [BLOCK: ITEM TYPE] Inventory.potion - Identify consumable resources without exposing inventory mutation.
        boolean potion() { return this==HEALTH_POTION || this==ENERGY_POTION || this==SHIELD_POTION; }
    }
    // [SECTION: DSA - HASH MAP / OOP - ENCAPSULATION] Quantities belong to one validated private hash table.
    private final Map<Item, Integer> counts = new HashMap<>();
    // [BLOCK: DSA: HASH MAP] Inventory.count - Read an item quantity with a zero default.
    public int count(Item item) { return counts.getOrDefault(item, 0); }
    // [BLOCK: OOP: ENCAPSULATION / HASH MAP] Inventory.add - Validate a positive amount and prevent integer overflow.
    public void add(Item item, int amount) {
        if (item==null || amount<=0) throw new IllegalArgumentException("Positive item amount required");
        counts.put(item, Math.addExact(count(item), amount));
    }
    // [BLOCK: OOP: ENCAPSULATION / HASH MAP] Inventory.consume - Spend one available item and remove empty entries.
    public boolean consume(Item item) {
        int amount=count(item);
        if (amount==0) return false;
        if (amount==1) counts.remove(item); else counts.put(item, amount-1);
        return true;
    }
}
