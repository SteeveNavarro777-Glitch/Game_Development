/* FILE PURPOSE: DSA HASH MAP / OOP ENCAPSULATION | Private validated item quantities.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.util.HashMap;
import java.util.Map;

/** Encapsulates quantities; clients cannot mutate the backing hash table. */
public final class Inventory {
    public enum Item { HERB, SEAL }
    // [SECTION: DSA - HASH MAP / OOP - ENCAPSULATION] Item keys map to private quantities.
    private final Map<Item, Integer> counts = new HashMap<>();

    // [BLOCK: DSA: HASH MAP] Inventory.count - Read an item quantity with a zero default.
    public int count(Item item) { return counts.getOrDefault(item, 0); }
    // [BLOCK: OOP: ENCAPSULATION / HASH MAP] Inventory.add - Validate a positive amount and update quantity without integer overflow.
    public void add(Item item, int amount) {
        if (item == null || amount <= 0) throw new IllegalArgumentException("Positive item amount required");
        counts.put(item, Math.addExact(count(item), amount));
    }
    // [BLOCK: OOP: ENCAPSULATION / HASH MAP] Inventory.consume - Spend one available item and remove empty entries.
    public boolean consume(Item item) {
        int amount = count(item);
        if (amount == 0) return false;
        if (amount == 1) counts.remove(item); else counts.put(item, amount - 1);
        return true;
    }
}
