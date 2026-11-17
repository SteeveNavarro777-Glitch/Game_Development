/* FILE PURPOSE: DSA ARRAY / OOP ENCAPSULATION | Nine selectable, swappable references to owned equipment and potions.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
/** A slot references a type; moving a slot never copies or consumes an item stack. */
final class Hotbar {
    static final int SIZE=9;
    // [SECTION: DSA - FIXED ARRAY] Constant-time access and swaps in nine numbered slots.
    private final Inventory.Item[] slots={Inventory.Item.SPEAR,Inventory.Item.SHIELD,
        Inventory.Item.HEALTH_POTION,Inventory.Item.ENERGY_POTION,Inventory.Item.SHIELD_POTION,null,null,null,null};
    private int selected;
    // [BLOCK: ARRAY ACCESS] Hotbar.item - Read a validated slot without exposing the backing array.
    Inventory.Item item(int slot) { check(slot); return slots[slot]; }
    // [BLOCK: OOP: ENCAPSULATION] Hotbar.selectedIndex - Read the currently selected slot number.
    int selectedIndex() { return selected; }
    // [BLOCK: ARRAY ACCESS] Hotbar.selectedItem - Read the selected equipment or consumable reference.
    Inventory.Item selectedItem() { return slots[selected]; }
    // [BLOCK: ARRAY SELECTION] Hotbar.select - Select one of the nine indexed slots.
    void select(int slot) { check(slot); selected=slot; }
    // [BLOCK: ARRAY SEARCH] Hotbar.indexOf - Find an item's current slot after rearrangement for the direct healing shortcut.
    int indexOf(Inventory.Item item){for(int i=0;i<SIZE;i++)if(slots[i]==item)return i;return -1;}
    // [BLOCK: MODULAR INDEXING] Hotbar.cycle - Wrap mouse-wheel selection in either direction without overflow.
    void cycle(int steps) { selected=(int)Math.floorMod((long)selected+steps,(long)SIZE); }
    // [BLOCK: ARRAY SWAP] Hotbar.swap - Exchange two references while preserving the selected item and inventory quantities.
    void swap(int from,int to) {
        check(from); check(to); Inventory.Item temporary=slots[from]; slots[from]=slots[to]; slots[to]=temporary;
        if(selected==from)selected=to; else if(selected==to)selected=from;
    }
    // [BLOCK: VALIDATION] Hotbar.check - Reject an index outside the fixed hotbar array.
    private static void check(int slot) { if(slot<0 || slot>=SIZE)throw new IllegalArgumentException("Hotbar slot outside 0..8"); }
}
