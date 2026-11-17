# Current game assets

The runtime reads PNGs and manifests from `res/`. Editable source atlases and artwork records are kept in `art/`.

| Location | Contents | Loaded by |
| --- | --- | --- |
| `res/player/equipment/` | 24 authored empty-handed body poses in one atlas, wrist/boot/source manifest; cached left mirrors and item composites | HeldItemArt |
| `res/items/` | 3 potion icons, one 22-frame effect atlas and source-hash manifest | PotionArt |
| `art/source/potions/` | 25 unchanged supplied potion/effect PNGs | PotionSpriteImporter |
| `res/wildlife/` | 3 wildlife atlases, one detailed Sigbin portrait and source manifests | WildlifeArt |
| `art/source/boar/`, `art/source/wolf/`, `art/source/sigbin/` | 67 unchanged user-supplied PNG poses | WildlifeSpriteImporter |
| `res/bosses/` | 3 packed villain atlases and a 452-frame source manifest | BossArt |
| `art/source/bosses/` | The user-supplied Tank, Mage and Fighter source sheets | BossSpriteImporter |
| `res/player/` | 47 movement/hurt/death PNGs and 4 complete guard PNGs | Assets / ShieldGuard |
| `res/player/combat/` | 72 authored body PNGs, frame manifest and grip overrides; left poses are mirrored in memory | CombatBody |
| `res/village/house_*.png` | 4 current custom home designs | RpgArt |
| `res/village/npc_*.png` | 48 directional/work poses across 6 roles | RpgArt / Npc |
| `res/village/foliage_*.png` | 6 tree/plant sprites | RpgArt / World |
| `res/village/prop_*.png` | 11 active prop designs using their authored style IDs | RpgArt / World |
| `res/village/terrain_*.png` | 16 ground/water tiles | RpgArt / RpgTerrain |
| `art/source/player/` | Current Agyu sheet, guard sources and three combat sheets | Current player importers |
| `art/source/village/` | Five custom terrain/foliage/house/NPC/prop atlases | RpgAssetImporter |
| `art/prompts/`, `art/provenance/` | Saved generation prompts, source hashes and credits metadata | Documentation / artwork provenance |
| `art/local-village/` | Optional creator house styles 4-7 and original free sheet; ignored by Git | RpgArt / RpgAssetImporter |

There are **220 runtime PNGs** in the tracked `res/` folders. Wildlife flash/mirror variants, wooden palisade/gate/bridge art, the mountain and its four waterfall/mist phases, rock tiles, the night mask and soft light textures, the forest camera margin, cropped spear, mirrored poses, transitions and farm artwork are prepared or cached by code. Prop ID 4 was retired; the remaining authored IDs are preserved in map placement.

## Regenerate the current assets

Run from the project root with JDK 17+:

```powershell
.\tools.bat SpriteImporter
.\tools.bat GuardSpriteImporter
.\tools.bat CombatBodyImporter
.\tools.bat RpgAssetImporter
.\tools.bat BossSpriteImporter
.\tools.bat WildlifeSpriteImporter
.\tools.bat PotionSpriteImporter
.\tools.bat EquipmentSpriteImporter
```

The player tools export only the current runtime frame sets. RpgAssetImporter reads the five custom atlases and skips the retired prop. If the optional free creator sheet is available, its four packed houses stay in the ignored local folder. Without it, the game uses full-size custom alternatives for those styles.

The packed runtime images are necessary to run the game. Source atlases are necessary to reproduce them; removing the source artwork would break the retained import tools. Credits and optional original-house setup are in [CREDITS.md](CREDITS.md).

Tree root footprints are measured from the four current foliage sprites during cache startup; no source PNG pixels are changed. The wooden defenses use code-generated sprite caches in `src/world/Palisade.java`.
