# Forest wildlife

A new journey places twelve boar herds over the reachable outer forest. Each contains one to four members, with singles, pairs, trios and groups of four represented. A 4-by-3 spatial partition spreads herd centers over the map; centers and member positions are randomized for each journey. Fixed seeds reproduce a layout for development and verification.

| Creature | Role and spawn rules | Combat |
| --- | --- | --- |
| Wild boar | Randomized small herds throughout the outer forest, by day and night | 3 health, directional walking, a half-second charge warning, locked rush, shield stop, spear interruption and five death poses |
| White wolf | A total of 2 or 3 per night, at 12 and 72 active night seconds, plus 132 seconds when the limit is 3 | 8 health, faster pursuit, supplied jump/bite poses, a readable pounce, normal defenses and five death poses |
| Sigbin | Night-only ambush in dense forest, at least five tiles beyond the village; at most one active encounter | 4 health, stalking eyes, a short jumpscare/reaction window, then persistent hunting and real claw attacks; spear hits interrupt and can kill it |

Boar placement uses one BFS reachability field and real foot-collider checks. Groups avoid village land, blocked terrain, disconnected pockets and the villain arenas. The complete footprint stays outside the village even through an open gate. Each herd owns a bounded set of slots in parallel actor/home/herd/cooldown arrays. After a defeat, its slot can replenish after 25 active seconds at a randomized nearby free tile, at least 380 art pixels away from Agyu. Respawning cannot increase the selected herd size or revive a defeated villain.

Wolves use a total nightly spawn budget. Defeating one cannot create extra replacements that night. Their searches are bounded and offscreen; they stop attacking at dawn and depart without awarding a kill. New journeys reset that budget.

Sigbin spawning requires 50 active night seconds of deep-forest exploration and at least 120 art pixels of movement. Standing still does not repeatedly trigger ambushes. Later encounters have a 65?100 second active-night cooldown. No Sigbin spawns or scares during the day, inside the village or in a villain arena.

The Sigbin's reveal keeps player control available and gives a brief reaction window. Afterward it uses actual pursuit, damage, warning/strike/recovery phases and the supplied attack poses. Shield, dash, jump and spear combat use the normal player contracts. A hit cancels the portrait and staggers the creature while it remains an enemy. The fight lasts until defeat, a successful disengagement, village entry or dawn. Living departures do not grant a kill reward; a killed Sigbin plays its five death poses and grants its normal one-time defeat event.

Night power remains 25% faster outdoor pursuit and one additional damage on real hits. It does not stack or heal actors. Pausing, map/journal reading and focus loss freeze wildlife along with the world clock.

## Artwork and regeneration

All 67 original PNGs remain unchanged in `art/source/boar`, `art/source/wolf` and `art/source/sigbin`: 21 boar, 25 wolf and 21 Sigbin poses. The importer preserves the supplied alpha; hidden RGB colors behind fully transparent pixels do not become backgrounds.

`WildlifeSpriteImporter` uses an array-based FIFO flood fill to find the opaque body and register its feet. Every gameplay pose uses the same 1:16 scale, packed into three 80-pixel-cell atlases. The scare close-up comes from the same original Sigbin front pose at 1:4 detail. Runtime manifests record source hashes and anchors. `WildlifeArt` caches species/action/direction/phase arrays, left mirrors and flash silhouettes; gameplay frames do not load, slice, mirror or resize files.

```powershell
.\tools.bat WildlifeSpriteImporter
```

The optional short audio sting is synthesized outside the game loop. Headless tests do not play audio, and missing audio hardware leaves the visual encounter available.

## DSA and OOP locations

- `Enemy` owns private health and circular linked-list patrols. `ForestBeast` supplies shared pursuit, committed strikes and cached rendering.
- `WildBoar`, `Wolf` and `Sigbin` specialize the abstraction through stats, attack policy and ambush behavior.
- `ForestEncounters` combines stratification, BFS reachability, immutable herd records and parallel slot arrays. `NightWildlife` encapsulates independent wolf quotas and Sigbin timing.
- `BoarTests`, `WildlifeTests` and `ForestRevisionTests` verify distribution, collision, respawns, real combat, night restrictions, map cleanup and session state.

Review PNGs in ignored `build/` include `preview-boar-distribution.png`, `preview-forest-boars.png`, `preview-night-wildlife.png`, `preview-sigbin-scare.png`, `preview-home-without-beacon.png` and `preview-clearing-without-beacon.png`. Distribution labels appear only on the exported review image.
