import java.util.HashMap;
import java.util.Map;

/** Encapsulates quantities; clients cannot mutate the backing hash table. */
public final class Inventory {
    public enum Item { HERB, SEAL }
    private final Map<Item, Integer> counts = new HashMap<>();

    public int count(Item item) { return counts.getOrDefault(item, 0); }
    public void add(Item item, int amount) {
        if (item == null || amount <= 0) throw new IllegalArgumentException("Positive item amount required");
        counts.put(item, Math.addExact(count(item), amount));
    }
    public boolean consume(Item item) {
        int amount = count(item);
        if (amount == 0) return false;
        if (amount == 1) counts.remove(item); else counts.put(item, amount - 1);
        return true;
    }
}
