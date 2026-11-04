/** Ordinary shrine guard; inherits navigation and health from Enemy. */
public final class Slime extends Enemy {
    Slime(double x,double y,int shrineGroup){super(x,y,3,shrineGroup);}
    @Override protected double speed(){return 42;}
    @Override protected int contactDamage(){return 1;}
}
