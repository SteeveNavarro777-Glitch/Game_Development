# Equipped items and drinking artwork

The built-in **imagegen** tool produced two project-bound transparent source sheets on 17 November 2026, using Agyu's original guard sheet as the identity and palette reference. No API/CLI fallback was used. Both final sources and the exact prompt set are saved in the project:

| Final saved source | Exact generation prompt | Purpose |
| --- | --- | --- |
| [agyu-guard-forward-v3.png](../art/source/player/agyu-guard-forward-v3.png) | [agyu-guard-forward-v3.txt](../art/prompts/agyu-guard-forward-v3.txt) | Forward-pointing side guards with the gripping arm in front of the torso |
| [agyu-held-items-v1.png](../art/source/player/agyu-held-items-v1.png) | [agyu-held-items-v1.txt](../art/prompts/agyu-held-items-v1.txt) | Twenty-four empty-handed holding, stepping, raising, sipping, tilting and lowering bodies |

The original front guard and corrected rear guard remain byte-identical. GuardSpriteImporter takes only the side-facing cells from the new guard source and packs them to the existing 64px canvas and boot baseline. These historical guard frames have a forward spear in the side views. The current runtime composes its own upright carry poses using the common body atlas. The revised guard pose points the spear forward while the shield is raised.

The held-item sheet uses four columns by six rows, with eight poses each for DOWN, RIGHT and UP; LEFT is mirrored. EquipmentSpriteImporter registers boots and wrists, packing a 512 x 192 body atlas and a source-hash/anchor manifest under `res/player/equipment/`. HeldItemArt prepares selected-item composites at startup. Supplied potion artwork is attached at the authored wrist, and original finger pixels cover the bottle neck. Empty or depleted slots show empty hands. The shield has a dedicated held/raised placement. Existing spear attack/combo bodies keep their authored grip geometry.

DrinkMotion selects hold, lift, lip contact, wrist tilt, lower and hold poses over 0.84 active seconds. Player consumes a useful potion at 0.44 seconds. Walking composites reuse authored leg steps while preserving the drinking upper body and hand/bottle contact. Image preparation, item rotation and small fist compositing happen once in the cache, not during painting. Selected equipment also appears in dash afterimages.

```powershell
.\tools.bat GuardSpriteImporter
.\tools.bat EquipmentSpriteImporter
```

GuardSpriteImporter accepts optional original, rear and forward-side source paths in that order. [equipment-v1.json](../art/provenance/equipment-v1.json) records source and prompt hashes.

Visual evidence remains in ignored `build/`: `preview-guard-assets.png`, `preview-equipped-items.png`, `preview-drinking-motion.png`, `preview-walking-drink.png`, `preview-equipment-hotbar.png` and `preview-equipment-boss-hud.png`. The actual Player and GamePanel render these views. EquipmentTests verifies forward tip placement, unclipped/cached item poses, sip timing, one-time consumption, interruption, modal/focus freezes, held-item selection and the boss/hotbar gap.

## One character across equipment

All normal equipment now shares the same body atlas and ground anchor, including the spear. Switching slots cannot switch to a differently proportioned character sheet. Walking, running, guarded movement, airborne equipment, dash afterimages and the title/item previews use the prepared common body. Existing attack and death poses keep their authored movement and game scale.

Agyu carries the spear upright in all four directions and points it forward while guarding. Front and back use the right hand, with the left hand available for the shield. The renderer replaces the original forearm pixels with one bent arm and a closed grip around the shaft. The shield uses its own native silhouette, with a wooden face, a metal boss and a strapped reverse. The upright weapon uses integer pixel placement and the same position across the walking frames. The renderer prepares those frames once. The attack animation controls the spear during a swing.

## Smith tiers and supplied effects

The Village Smith unlocks gear but does not silently auto-equip it. `EquipmentLoadout` stores the highest owned tier separately from the currently equipped tier, so the Inventory can switch among owned Basic, Rare and Legendary variants. `EquipmentArt` uses the exact spear and shield models from `res/items/agyu-weapons.png` for idle, walking, running, guarding, dash afterimages and attacks. It also caches all eight supplied effect frames per tier: blue crescents and barriers for Rare gear, orange crescents and barriers for Legendary gear. The game only advances cached frames while that equipped weapon is being used.

Visual evidence is exported to `build/preview-equipment-tiers.png`, `build/preview-equipment-directions.png`, `build/preview-inventory-equipment.png`, `build/preview-smith-shop.png` and `build/preview-equipment-effects.png`.
