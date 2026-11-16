/* FILE PURPOSE: FIGHTER VILLAIN | Quick two-cut sword combinations and a locked-direction flaming dash in Clearing 3.
 * Course examples: concrete inheritance, active windows and swept collision; docs/CODE_GUIDE.md.
 */
import java.awt.Graphics2D;
import java.awt.Color;

final class FighterBoss extends ArenaBoss {
    private boolean charge;
    // [BLOCK: CONCRETE BOSS] FighterBoss.FighterBoss - Create the golden fighter at its assigned clearing center.
    FighterBoss(ForestRegion.Arena arena){super(arena,BossArt.Kind.FIGHTER,"FIGHTER - ASHEN BLADE","fighter",60);}
    // [BLOCK: OOP OVERRIDE] FighterBoss.pursueSpeed - Let the fighter close ground faster than the other villains.
    @Override protected double pursueSpeed(){return 69;}
    // [BLOCK: OOP OVERRIDE] FighterBoss.attackRange - Start a sword sequence or dash within its warned approach distance.
    @Override protected double attackRange(){return 145;}
    // [BLOCK: OOP OVERRIDE] FighterBoss.accent - Match blade and flame warnings to the supplied golden/orange art.
    @Override protected int accent(){return 0xFFBB52;}
    // [BLOCK: ATTACK SELECTION] FighterBoss.planAttack - Alternate two-cut sword sequences with a committed flame charge.
    @Override protected void planAttack(Player player){
        charge=attackNumber()%2==0;configure(charge?.85:.6,charge?.45:.5,charge?.95:.7,charge?BossArt.Anim.SKILL:BossArt.Anim.POWER);
    }
    // [BLOCK: ACTIVE COMBO] FighterBoss.strike - Resolve separate sword windows or sweep a fast dash against real terrain.
    @Override protected void strike(Player player,World world,double dt){
        if(charge){
            move(aimDx*330*dt,aimDy*330*dt,world);
            if(Math.hypot(player.x-x,player.y-y)<48&&window(2,0))coneHit(player,world,52,-.15,2);
            if(window(1,.08))Fx.burst(x,y-10,accent(),18,75,.35,-15,2);
        }else{
            if(window(1,.10)){coneHit(player,world,94,.087,1);Fx.burst(x+aimDx*42,y+aimDy*42-12,accent(),9,70,.2,0,1);}
            if(window(2,.34)){coneHit(player,world,94,.087,1);Fx.burst(x+aimDx*45,y+aimDy*45-10,accent(),9,75,.2,0,1);}
        }
    }
    // [BLOCK: COMMITTED ATTACK TELEGRAPH] FighterBoss.warning - Show the slash footprint or the longer locked flame-charge lane.
    @Override protected void warning(Graphics2D g,int cx,int cy){
        if(!charge){coneWarning(g,cx,cy,94,170);return;}
        Graphics2D lane=(Graphics2D)g.create();
        try{lane.translate(x-cx,y-cy);lane.rotate(Math.atan2(aimDy,aimDx));lane.setColor(new Color(255,187,82,65));
            lane.fillRoundRect(-20,-48,222,96,48,48);lane.setColor(new Color(accent()));lane.drawRoundRect(-20,-48,222,96,48,48);
        }finally{lane.dispose();}
    }
}
