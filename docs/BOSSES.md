# The three main villains

| Arena | Boss | Location | Fighting style |
| --- | --- | --- | --- |
| 1 | Tank | Western clearing | Heavy melee attacks and warned ground slams |
| 2 | Mage | Eastern clearing | Ranged magic and targeted ground hazards |
| 3 | Fighter | Southern clearing | Fast slashes, lunges and flame attacks |

All three appear in a new journey, alongside the existing shrine guards and ambient forest enemies. They stay inside their own clearings and stop attacking when Agyu leaves. Use M to find their numbered markers and Tab to track defeats. Boss health is shown as a continuous bar during a nearby encounter.

Watch the ground warnings and attack windups. Face incoming strikes or projectiles while holding the shield; use dash to evade and jump over ground attacks. Attack during recovery. Damage and animation advance through the simulation rather than rendering, and the map, pause and focus loss freeze encounters.

Bosses do not respawn after defeat. Each defeat awards two herbs once and updates its map marker and journal objective. The three boss objectives are independent; they may be fought in any order. HOME victory requires all three villains and the existing awakened guardian, whose shrine prerequisites remain active. Restarting rebuilds the entire encounter population.

The supplied sprite sheets provide idle, movement, jump, hit reaction, death, attack, power and skill poses in four directions. The importer preserves source alpha and stable feet anchors. Packed atlas storage is shared by cached frame views; gameplay never slices or rescales images. See [BOSS_SPRITES.md](BOSS_SPRITES.md) for the exact row mapping and regeneration command.

## Fighting at night

The main villains gain 25% faster pursuit and one extra damage per hit at night. Their health, warning times, projectile speeds and flame-charge travel remain unchanged. The bonus ends at dawn and does not restore health or repeat defeat rewards. Nearby boss lighting and the original ground warnings remain visible. See [DAY_NIGHT.md](DAY_NIGHT.md).
