# Village map and house artwork

The playable 192 x 160 tile world surrounds a fictional 64 x 48 tile Bukidnon-inspired village with dense forest. The current eight-design house set, detailed scenery, 12 NPCs and animated river are documented in RPG_ART.md. The current architecture and cultural references are described below. House exteriors are solid; entering buildings is outside this update. The existing shrine/guardian journey remains playable in the new setting.

The top-right time/kill panel has been replaced by a live 96 x 72 minimap. Its bright green arrow follows Agyu's interpolated ground position and rotates with his facing. M opens the larger labelled village map using the same arrow. Quest markers update as shrines and the guardian are completed. The former attack combo panel has been removed. The top-left HUD shows health and energy bars plus a compact day/night countdown; the large background card, title, inventory counts, dash status and control hints have been removed.

## Architectural references

This is a fictional adaptation, not a reconstruction of one community's houses. The exterior design uses natural bamboo/wood walls, raised floors and posts, broad thatched roofs, porches and short steps.

- [NCCA: History of Philippine Architecture](https://ncca.gov.ph/about-ncca-3/subcommissions/subcommission-on-the-arts-sca/architecture-and-allied-arts-2/history-of-philippine-architecture/) discusses broader Philippine vernacular features; it does not establish a uniform house style for every Bukidnon community.
- [NCCA: The Talaandig](https://ncca.gov.ph/about-ncca-3/subcommissions/subcommission-on-cultural-communities-and-traditional-arts-sccta/central-cultural-communities/the-talaandig/), by Victorino Saway, provides community and agricultural context.
- [USTP: Talaandig community architectural project in Tagbak, Talakag](https://www.ustp.edu.ph/empowering-indigenous-communities-ustps-sustainable-development-initiative-for-the-talaandig-tribe/) describes community-requested planning that incorporates cultural identity and local needs.

The assets do not claim sacred functions, a particular house name, authentic ornament meanings, or that Bukidnon's indigenous communities share one architectural tradition.

## Current artwork and import tool

The four current traditional-house sprites come from [the custom source atlas](../art/source/village/houses-custom.png). RpgAssetImporter packs these with the current terrain, foliage, NPC and craft atlases. The saved prompts are in [art/prompts/rpg-art-prompts.json](../art/prompts/rpg-art-prompts.json); full credits and optional creator-house setup are in [CREDITS.md](CREDITS.md).

Runtime sprites: [house_0.png](../res/village/house_0.png), [house_1.png](../res/village/house_1.png), [house_2.png](../res/village/house_2.png), [house_3.png](../res/village/house_3.png). Their packing metadata is in [frames.csv](../res/village/frames.csv).

Regenerate the current artwork from the project root:

```powershell
.\tools.bat RpgAssetImporter
```

The superseded house atlas, earlier packed house copies and VillageHouseImporter were removed during cleanup. The current importer preserves complete alpha silhouettes, nearest-neighbor scale and the common ground anchor.

## Map and collision implementation

World holds the authored lot table and tile arrays. Trees, houses, props and gate leaves implement its shared Obstacle contract and enter the same uniform-grid collision index. The brute-force reference includes each obstacle kind, and navigation is built after all structures are placed. Roads and open courts connect mission areas. Three-pixel movement substeps apply to buildings, roots, closed gates and terrain.

VillageMap caches a 256 x 192 terrain image once per world. Rendering scales this cache and draws only the changing markers and player arrow. Buildings and trees join the existing depth-sorted Drawable list with culling margins sized for their art. The removed bottom instruction panel stays absent.

## Trees and wooden defenses

The old stone border is replaced by a cached wooden palisade with four cardinal gateways. E opens/closes a nearby gate from either side. Closed leaves are indexed obstacles for Agyu, enemies and NPCs; closing checks all living actors and refuses an occupied threshold. Gate state rebuilds navigation once and invalidates the shared BFS/trail cache. The minimap keeps a cached terrain layer and overlays the live gate markers; the full map labels N/E/S/W GATE.

Tree ground footprints are derived once from opaque bark and cached per style. This excludes the decorative grass below the first tree's roots, whose floor lies 20 art pixels above its old anchor. Trees store their image/style/footprint and sort at their actual roots. Walking, sprinting, lunging and knockback use collision substeps of at most three pixels, alongside the existing swept dash checks. Canopies fade when they cover Agyu; the lower roots stay opaque and solid. Gate approaches and roads reserve tree clearance.

The gates open onto connected forest trails surrounding the village. ForestRegion generates solid trees, shaded ground and rounded camp/shrine clearings once per seed. All ordinary enemies start outside the walls; ForestEncounters owns eight bounded ambient spawn slots and ten one-time shrine guards. Camp enemies replenish after 25 active seconds only while Agyu explores outdoors and the spawn point is clear and distant. The three shrines and guardian clearing now lie in the forest. Restarting creates four closed gates and a fresh encounter population. The objective trail leads to a reachable gate when it is closed, both for leaving and returning home.

- [Four gates: closed and open](../build/preview-wooden-gates.png)
- [Tree roots and canopy visibility](../build/preview-tree-roots.png)
- [Protected village overview](../build/preview-wooden-boundary-overview.png)

- [Forest overview](../build/preview-forest-overview.png)
- [Actual forest combat camera](../build/preview-forest-combat.png)

## Distant clearings and mountain river

The world is twice as wide and tall as the earlier forest map. Three broad clearings hold the main villain encounters at world tile centers (22,25), (144,24) and (96,132). Each has an elliptical floor inside a 17 x 15 tile footprint, a clear central arena and a connected approach. They sit well away from the village. The full map labels 1 TANK, 2 MAGE and 3 FIGHTER; markers turn green after their quests are completed. See BOSSES.md for the encounters.

A cached, 1024 x 1280 mountain occupies the far-right highlands. A four-phase waterfall and mist feed its basin and the connected watercourse through the old village river to the far-west outlet. Timber culverts remain solid under the village wall, and planned forest bridges preserve the trail network. A viewing jetty reaches the basin. The cliff's blocking tiles derive from the opaque artwork, and the mountain draws behind actors on valid ground. The nearby camera lifts while keeping Agyu visible.

Tree generation now uses temporary spatial buckets instead of comparing each new root with the entire forest. Normal movement retains the shared collision index and cached navigation. Full-world preview exports render at quarter size rather than allocating enormous full-resolution canvases.

- [Expanded forest and three clearings](../build/preview-forest-overview.png)
- [Waterfall and viewing deck](../build/preview-waterfall-camera.png)
- [Mountain and animated cascade](../build/preview-mountain-waterfall.png)

## Verification and previews

VillageTests checks every house for solid walls, nonoverlapping footprints, a reachable front approach and collision-safe dashing; it also compares indexed/exact collision around houses. It checks house alpha canvases, four facing arrows, interpolated position, unchanged graphics/world state, actual HUD minimap pixels, full-map arrow visibility and the removed attack panel.

- [build/preview-village-overview.png](../build/preview-village-overview.png): complete village artwork.
- [build/preview-village-street.png](../build/preview-village-street.png): actual game camera, house exterior and live minimap.
- [build/preview-game.png](../build/preview-game.png): starting plaza.
- [build/preview-map.png](../build/preview-map.png): full labelled M map.

These images are produced by the real renderer and were visually reviewed. Automated checks validate geometry and progression; they do not establish human play balance. See TEST_REPORT.md for the current full-suite result.

## Night cycle

Every five active minutes the world switches between day and night. DayNightCycle retains the numbered phase, WorldLighting supplies cached moonlight/lamps and living outdoor enemies gain temporary pursuit/damage bonuses. NPCs and village enemies keep their normal strength. See [DAY_NIGHT.md](DAY_NIGHT.md) for the exact rules and [the comparison preview](../build/preview-day-night.png).
