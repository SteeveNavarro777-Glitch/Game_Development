/* FILE PURPOSE: TANK VILLAIN | Slow heavy cleaves and a telegraphed, jumpable ground slam in Clearing 1.
 * Course examples: concrete inheritance and overridden combat policy; docs/CODE_GUIDE.md.
 */
import java.awt.Graphics2D;

final class TankBoss extends ArenaBoss {
    private boolean slam;
    // [BLOCK: CONCRETE BOSS] TankBoss.TankBoss - Create the durable green tank at its assigned clearing center.
    TankBoss(ForestRegion.Arena arena){super(arena,BossArt.Kind.TANK,"TANK - ROOTBREAKER","tank",72);}
    // [BLOCK: OOP OVERRIDE] TankBoss.pursueSpeed - Give the tank a slow deliberate pursuit.
    @Override protected double pursueSpeed(){return 35;}
    // [BLOCK: OOP OVERRIDE] TankBoss.attackRange - Start heavy attacks only within their advertised reach.
    @Override protected double attackRange(){return 105;}
    // [BLOCK: OOP OVERRIDE] TankBoss.accent - Match warnings and impacts to the supplied green power frames.
    @Override protected int accent(){return 0x9FDC52;}
    // [BLOCK: OOP OVERRIDE] TankBoss.knockbackFactor - Keep the durable tank planted under spear hits.
    @Override protected double knockbackFactor(){return .12;}
    // [BLOCK: ATTACK CYCLE] TankBoss.planAttack - Alternate a frontal cleave and an explicitly warned radial slam.
    @Override protected void planAttack(Player player){
        slam=attackNumber()%2==0;
        configure(slam?1.1:.85,slam?.5:.4,slam?1.05:.9,slam?BossArt.Anim.SKILL:BossArt.Anim.ATTACK);
    }
    // [BLOCK: ACTIVE ATTACK] TankBoss.strike - Resolve a single heavy impact after the warning window.
    @Override protected void strike(Player player,World world,double dt){
        if(window(1,.12)){
            if(slam)slamHit(player,world,108,2);else coneHit(player,world,105,.28,2);
            Fx.burst(x,y-5,accent(),20,95,.4,15,2);Fx.shake(slam?3:1.7,.18);
        }
    }
    // [BLOCK: ATTACK TELEGRAPH] TankBoss.warning - Display the exact cone or radial ground footprint before impact.
    @Override protected void warning(Graphics2D g,int cx,int cy){if(slam)circleWarning(g,cx,cy,108);else coneWarning(g,cx,cy,105,148);}
}
