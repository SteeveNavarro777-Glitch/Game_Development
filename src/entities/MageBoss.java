/* FILE PURPOSE: MAGE VILLAIN | Ranged violet projectile fans and delayed, visible rune bursts in Clearing 2.
 * Course examples: concrete inheritance, bounded hazard lists and state-machine policy; docs/CODE_GUIDE.md.
 */
import java.awt.Color;
import java.awt.Graphics2D;

final class MageBoss extends ArenaBoss {
    private boolean runes;
    // [BLOCK: CONCRETE BOSS] MageBoss.MageBoss - Create the violet caster at its assigned clearing center.
    MageBoss(ForestRegion.Arena arena){super(arena,BossArt.Kind.MAGE,"MAGE - DUSKWEAVER","mage",48);}
    // [BLOCK: OOP OVERRIDE] MageBoss.pursueSpeed - Move the caster steadily into range.
    @Override protected double pursueSpeed(){return 40;}
    // [BLOCK: OOP OVERRIDE] MageBoss.attackRange - Cast across the broad arena rather than inflicting contact damage.
    @Override protected double attackRange(){return 300;}
    // [BLOCK: OOP OVERRIDE] MageBoss.preferredDistance - Keep room between caster and player while approaching.
    @Override protected double preferredDistance(){return 145;}
    // [BLOCK: OOP OVERRIDE] MageBoss.retreats - Let the mage back away when Agyu closes into melee range.
    @Override protected boolean retreats(){return true;}
    // [BLOCK: OOP OVERRIDE] MageBoss.accent - Match spell warnings to the supplied violet art.
    @Override protected int accent(){return 0xC487FF;}
    // [BLOCK: ATTACK SELECTION] MageBoss.planAttack - Alternate locked fan shots and three delayed rune targets.
    @Override protected void planAttack(Player player){
        runes=attackNumber()%2==0;configure(1,.45,1.1,runes?BossArt.Anim.SKILL:BossArt.Anim.POWER);
    }
    // [BLOCK: ACTIVE CAST] MageBoss.strike - Spawn bounded spell objects once after the staff windup.
    @Override protected void strike(Player player,World world,double dt){
        if(window(1,.12)){
            if(runes){rune(aimX,aimY,world);rune(aimX-aimDy*52,aimY+aimDx*52,world);rune(aimX+aimDy*52,aimY-aimDx*52,world);}
            else{double angle=Math.atan2(aimDy,aimDx);for(int i=-2;i<=2;i++)bolt(angle+i*.20,135);}
            Fx.burst(x,y-38,accent(),12,55,.35,-10,1);
        }
    }
    // [BLOCK: CAST TELEGRAPH] MageBoss.warning - Show the locked fan direction or forthcoming rune target while the spell loads.
    @Override protected void warning(Graphics2D g,int cx,int cy){
        if(!runes)coneWarning(g,cx,cy,180,52);
        else{g.setColor(new Color(196,135,255,100));g.fillOval((int)aimX-cx-36,(int)aimY-cy-36,72,72);g.setColor(new Color(accent()));g.drawOval((int)aimX-cx-36,(int)aimY-cy-36,72,72);}
    }
}
