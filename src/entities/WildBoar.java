/* FILE PURPOSE: WILD BOAR | One member of a randomized forest herd using the supplied boar poses.
 * Course examples: inheritance and concrete attack statistics; docs/CODE_GUIDE.md.
 */
/** Ordinary forest boars share collision-safe patrol, pursuit and charge attacks. */
public final class WildBoar extends ForestBeast {
    // [BLOCK: OOP INHERITANCE] WildBoar.WildBoar - Bind three health points and the current random herd identity.
    WildBoar(double x,double y,int herd){super(x,y,3,herd);}
    // [BLOCK: OOP OVERRIDING] WildBoar.kind - Select the user's boar artwork.
    @Override protected WildlifeArt.Kind kind(){return WildlifeArt.Kind.BOAR;}
    // [BLOCK: OOP OVERRIDING] WildBoar.speed - Supply ordinary pursuit speed before the outdoor night modifier.
    @Override protected double speed(){return 52;}
    // [BLOCK: OOP OVERRIDING] WildBoar.contactDamage - Supply one point of daytime tusk damage.
    @Override protected int contactDamage(){return 1;}
}
