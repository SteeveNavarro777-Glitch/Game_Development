# Equipped items and drinking artwork

The built-in **imagegen** tool produced two project-bound transparent source sheets on 17 November 2026, using Agyu's original guard sheet as the identity and palette reference. No API/CLI fallback was used. Both final sources and the exact prompt set are saved in the project:

| Final saved source | Exact generation prompt | Purpose |
| --- | --- | --- |
| [agyu-guard-forward-v3.png](../art/source/player/agyu-guard-forward-v3.png) | [agyu-guard-forward-v3.txt](../art/prompts/agyu-guard-forward-v3.txt) | Forward-pointing side guards with the gripping arm in front of the torso |
| [agyu-held-items-v1.png](../art/source/player/agyu-held-items-v1.png) | [agyu-held-items-v1.txt](../art/prompts/agyu-held-items-v1.txt) | Twenty-four empty-handed holding, stepping, raising, sipping, tilting and lowering bodies |

The original front guard and corrected rear guard remain byte-identical. GuardSpriteImporter takes only the side-facing cells from the new guard source and packs them to the existing 64px canvas and boot baseline. The spearhead is in front of the shield in both side views, and the shaft passes through the forward fist. The spear-ready guard still renders as one complete cached sprite.

The held-item sheet uses four columns by six rows, with eight poses each for DOWN, RIGHT and UP; LEFT is mirrored. EquipmentSpriteImporter registers boots and wrists, packing a 512 x 192 body atlas and a source-hash/anchor manifest under `res/player/equipment/`. HeldItemArt prepares selected-item composites at startup. Supplied potion artwork is attached at the authored wrist, and original finger pixels cover the bottle neck. Empty or depleted slots show empty hands. The shield has a dedicated held/raised placement. Existing spear attack/combo bodies keep their authored grip geometry.

DrinkMotion selects hold, lift, lip contact, wrist tilt, lower and hold poses over 0.84 active seconds. Player consumes a useful potion at 0.44 seconds. Walking composites reuse authored leg steps while preserving the drinking upper body and hand/bottle contact. Image preparation, item rotation and small fist compositing happen once in the cache, not during painting. Selected equipment also appears in dash afterimages.

```powershell
.\tools.bat GuardSpriteImporter
.\tools.bat EquipmentSpriteImporter
```

GuardSpriteImporter accepts optional original, rear and forward-side source paths in that order. [equipment-v1.json](../art/provenance/equipment-v1.json) records source and prompt hashes.

Visual evidence remains in ignored `build/`: `preview-guard-assets.png`, `preview-equipped-items.png`, `preview-drinking-motion.png`, `preview-walking-drink.png`, `preview-equipment-hotbar.png` and `preview-equipment-boss-hud.png`. The actual Player and GamePanel render these views. EquipmentTests verifies forward tip placement, unclipped/cached item poses, sip timing, one-time consumption, interruption, modal/focus freezes, held-item selection and the boss/hotbar gap.
