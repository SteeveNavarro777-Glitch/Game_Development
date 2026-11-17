# Day and night

The cycle starts at Day 1. Each day and each night lasts **180 active simulation seconds**. The compact HUD shows the numbered phase, time until the next switch and a stronger-foes notice during night.

| Active journey time | Phase |
| --- | --- |
| 0:00 to 5:00 | Day 1 |
| 5:00 to 10:00 | Night 1 |
| 10:00 to 15:00 | Day 2 |
| 15:00 to 20:00 | Night 2 |

Only playing advances the clock. Reading the map/journal, pausing, losing window focus, hit-stop, title, defeat and victory freeze it. Restarting creates a fresh Day 1. Boundary checks use fixed 60 Hz steps as well as coarse elapsed-time updates; the full three-minute phases are tested without waiting in real time.

## Outdoor enemy strength

From Night 1 onward, every night gives living enemies outside the palisade 25% faster pursuit and one additional point of attack damage. This includes boar herds, rare wolves, the hostile Sigbin, Tank, Mage and Fighter. Ordinary patrol movement also speeds up. Enemies inside the village keep their day strength; crossing back indoors removes the bonus. Newly populated or replenished outdoor enemies receive the current bonus immediately.

Health and maximum health do not change, so dawn and subsequent nights cannot heal or revive enemies. The bonus is derived from the current phase and location rather than accumulated. Explicitly harmless contact stays harmless. Boss warning/recovery times, projectile speeds and committed charges retain their existing values so the displayed attack footprints and counterplay remain consistent. Shields, dash protection and jumpable ground attacks still work.

## Lighting and implementation

[DayNightCycle](../src/world/DayNightCycle.java) owns private phase/remainder state and exposes read-only timing queries. World advances it through updateAmbient during the existing active simulation branch. Its final twelve seconds blend toward dusk or dawn with continuous light values at the phase boundary.

[Enemy](../src/entities/Enemy.java) exposes shared validated night-power queries and outgoing-damage/pursuit modifiers. Enemy, Guardian and ArenaBoss refresh the same rule before combat; ForestEncounters also applies it during spawning. No maximum-health mutation or duplicate reward path is involved.

[WorldLighting](../src/rendering/WorldLighting.java) reuses one viewport-sized darkness mask and two shared radial textures. It creates soft visibility around Agyu and active bosses, warm village lamp/porch pools and a lit waterfall. Empowered foes have a subtle violet ring. It shades the world before HUD, map and dialogue rendering. Source art, world state and combat clocks remain unchanged by drawing.

[DayNightTests](../tests/DayNightTests.java) verifies timing, real contact/pursuit and villain damage, defenses, spawn inheritance, dawn health preservation, non-stacking bonuses, paused clocks, fresh-session reset and readable cached composition.

- [The same village scene by day and night](../build/preview-day-night.png)
- [Night encounter with Tank](../build/preview-night-boss.png)
- [Waterfall at night](../build/preview-night-waterfall.png)

## Rare night wildlife

Each night also has a total allowance of two or three white wolves, spaced across the night. Kills do not replenish that allowance; surviving wolves stop attacking and fade away at dawn. Sigbin ambushes have their own exploration/cooldown rules and occur only at night. The reveal is followed by a real fight with normal claw damage and spear/defense counterplay. Living Sigbin encounters end at dawn. See [WILDLIFE.md](WILDLIFE.md).
