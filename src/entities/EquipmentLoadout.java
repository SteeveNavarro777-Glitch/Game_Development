/**
 * OOP: Encapsulates Agyu's independently upgradeable spear and shield.
 *
 * Each gear item begins at BASIC.  The blacksmith may advance it one level at
 * a time to RARE and then LEGENDARY; skipping a level is deliberately blocked.
 */
public final class EquipmentLoadout {
    public enum Gear {
        SPEAR("Spear"),
        SHIELD("Shield");

        final String label;

        Gear(String label) {
            this.label = label;
        }
    }

    public enum Level {
        BASIC("Basic"),
        RARE("Rare"),
        LEGENDARY("Legendary");

        final String label;

        Level(String label) {
            this.label = label;
        }
    }

    // [SECTION: OOP - ENCAPSULATION] Ownership and the active loadout are separate invariants.
    private Level ownedSpear = Level.BASIC;
    private Level ownedShield = Level.BASIC;
    private Level equippedSpear = Level.BASIC;
    private Level equippedShield = Level.BASIC;

    /** The combat/rendering tier explicitly equipped by the player. */
    public Level level(Gear gear) {
        return gear == Gear.SPEAR ? equippedSpear : equippedShield;
    }

    /** Highest tier forged at the smith, whether or not it is equipped. */
    public Level ownedLevel(Gear gear) {
        return gear == Gear.SPEAR ? ownedSpear : ownedShield;
    }

    public boolean owns(Gear gear, Level level) {
        return ownedLevel(gear).ordinal() >= level.ordinal();
    }

    public boolean isEquipped(Gear gear, Level level) {
        return level(gear) == level;
    }

    public boolean canUpgrade(Gear gear, Level target) {
        return target.ordinal() == ownedLevel(gear).ordinal() + 1;
    }

    /** Purchasing advances ownership only; inventory selection performs equip(). */
    public boolean upgrade(Gear gear, Level target) {
        if (!canUpgrade(gear, target)) {
            return false;
        }
        if (gear == Gear.SPEAR) {
            ownedSpear = target;
        } else {
            ownedShield = target;
        }
        return true;
    }

    /** Equip any owned tier, including Basic, without changing ownership. */
    public boolean equip(Gear gear, Level target) {
        if (!owns(gear, target)) {
            return false;
        }
        if (gear == Gear.SPEAR) {
            equippedSpear = target;
        } else {
            equippedShield = target;
        }
        return true;
    }

    /** Rare adds one damage and Legendary adds two to every spear hit. */
    public int spearDamage(int baseDamage) {
        return Math.max(0, baseDamage) + equippedSpear.ordinal();
    }

    /**
     * Dot-product threshold used by ShieldGuard.  Lower values protect a wider
     * arc: Basic 120 degrees, Rare 180 degrees, Legendary 240 degrees.
     */
    public double guardThreshold() {
        return switch (equippedShield) {
            case BASIC -> 0.5;
            case RARE -> 0.0;
            case LEGENDARY -> -0.5;
        };
    }

    /** Higher shield levels let Agyu move more naturally while braced. */
    public double guardMoveMultiplier() {
        return switch (equippedShield) {
            case BASIC -> 0.40;
            case RARE -> 0.50;
            case LEGENDARY -> 0.60;
        };
    }
}
