/* FILE PURPOSE: ITEM ACTION | Interruptible raise, sip and lower animation with one committed potion use.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
/** Owns animation timing only; Player validates and commits inventory/resources. */
final class DrinkMotion {
    static final double DURATION=.84,SIP=.44;
    private Inventory.Item item;
    private double elapsed;
    private boolean committed;
    // [BLOCK: ACTION START] DrinkMotion.begin - Start one validated potion action from the holding pose.
    void begin(Inventory.Item potion){if(potion==null||!potion.potion())throw new IllegalArgumentException("Potion required");item=potion;elapsed=0;committed=false;}
    // [BLOCK: ACTION STATE] DrinkMotion.active - Report whether an item animation owns the hands.
    boolean active(){return item!=null;}
    // [BLOCK: ACTION ITEM] DrinkMotion.item - Read the bottle being animated independently of current inventory quantity.
    Inventory.Item item(){return item;}
    // [BLOCK: ONE-SHOT COMMIT] DrinkMotion.advance - Return a sip event once even when a coarse step crosses the whole action.
    boolean advance(double dt){if(!active()||dt<=0||!Double.isFinite(dt))return false;elapsed=Math.min(DURATION,elapsed+dt);if(!committed&&elapsed+1e-9>=SIP){committed=true;return true;}return false;}
    // [BLOCK: ACTION COMPLETION] DrinkMotion.finished - Report when lowering the bottle has completed.
    boolean finished(){return active()&&elapsed+1e-9>=DURATION;}
    // [BLOCK: UI PROGRESS] DrinkMotion.progress - Read normalized action progress without advancing time.
    double progress(){return active()?elapsed/DURATION:0;}
    // [BLOCK: AUTHORED POSE] DrinkMotion.pose - Select the authored hold, lift, lip, tilt and lower frames.
    int pose(){if(elapsed<.12)return 0;if(elapsed<.24)return 3;if(elapsed<.39)return 4;if(elapsed<.60)return 5;if(elapsed<.76)return 6;return 0;}
    // [BLOCK: ACTION CANCELLATION] DrinkMotion.cancel - Release the hands without undoing an already committed sip.
    void cancel(){item=null;elapsed=0;committed=false;}
}
