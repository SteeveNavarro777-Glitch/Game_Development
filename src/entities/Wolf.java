/* FILE PURPOSE: RARE NIGHT WOLF | Faster forest pursuit, authored leap attacks and a harmless departure at dawn.
 * Course examples: inheritance, overridden policies and finite lifecycle; docs/CODE_GUIDE.md.
 */
/** Only the night wildlife director creates wolves; defeating one never refills that night's quota. */
final class Wolf extends ForestBeast {
    private boolean departing;private double fade;
    // [BLOCK: NIGHT WILDLIFE CONSTRUCTION] Wolf.Wolf - Supply a tougher rare predator outside shrine progression groups.
    Wolf(double x,double y){super(x,y,8,-3);}
    // [BLOCK: OOP OVERRIDING] Wolf.kind - Select the user's white wolf animation cache.
    @Override protected WildlifeArt.Kind kind(){return WildlifeArt.Kind.WOLF;}
    // [BLOCK: OOP OVERRIDING] Wolf.speed - Supply faster pursuit before the normal outdoor night multiplier.
    @Override protected double speed(){return 78;}
    // [BLOCK: OOP OVERRIDING] Wolf.contactDamage - Supply bite damage through the shared night/guard contracts.
    @Override protected int contactDamage(){return 1;}
    // [BLOCK: AUTHORED HURT BOX] Wolf.hurtBox - Match the taller, longer supplied wolf torso without widening its navigation feet.
    @Override java.awt.geom.Rectangle2D.Double hurtBox(){return new java.awt.geom.Rectangle2D.Double(x-19,y-38,38,38);}
    // [BLOCK: OOP ATTACK POLICY] Wolf.warningDuration - Give the rare predator a readable crouch before pouncing.
    @Override protected double warningDuration(){return .45;}
    // [BLOCK: OOP ATTACK POLICY] Wolf.strikeDuration - Play the complete four-frame pounce before recovery.
    @Override protected double strikeDuration(){return .42;}
    // [BLOCK: OOP ATTACK POLICY] Wolf.attackSpeed - Bound leap travel while retaining swept solid collision.
    @Override protected double attackSpeed(){return 270;}
    // [BLOCK: OOP ATTACK POLICY] Wolf.recoveryDuration - Leave a longer opening after the pounce.
    @Override protected double recoveryDuration(){return .85;}
    // [BLOCK: OOP ATTACK POLICY] Wolf.attackRange - Start a leap from a little farther away than a boar's charge.
    @Override protected double attackRange(){return 110;}
    // [BLOCK: DAWN LIFECYCLE] Wolf.update - Stop attacking immediately at dawn and fade away without a defeat reward.
    @Override void update(double dt,Player player,World world,int[][] flow){
        if(alive()&&(!world.dayNight.night()||departing)){departing=true;fade+=Math.max(0,dt);refreshNightPower(world);t+=Math.max(0,dt);return;}
        super.update(dt,player,world,flow);
    }
    // [BLOCK: DEPARTURE RENDERING] Wolf.opacity - Fade the living predator's dawn departure independently of its authored death sequence.
    @Override protected float opacity(){return departing&&alive()?(float)Math.max(0,1-fade/1.2):super.opacity();}
    // [BLOCK: BOUNDED LIFETIME] Wolf.removable - Remove a departing wolf after the fade or a defeated wolf after all death poses.
    @Override boolean removable(){return departing&&fade>=1.2||super.removable();}
}
