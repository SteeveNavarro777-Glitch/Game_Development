# Village art, NPCs and river update

The user selected the creator's free houses plus custom artwork. The current village combines 20 homes across eight designs, 12 friendly NPCs, 15 placed craft/ambient props, planted gardens, richer foliage, animated water, irregular banks and timber bridges.

## Art sources and credits

**Rafael Matos — Epic RPG World: The Village**

- [Creator page and license](https://rafaelmatos.itch.io/epic-rpg-world-the-village-asset-pack).
- Downloaded the publicly offered **Free Premade houses.png** (2048 x 1536 RGBA).
- Four complete house exteriors are imported from that sheet into runtime styles 4–7.
- The creator permits using and modifying assets in games, while prohibiting repackaging or redistributing the standalone assets.

The user-supplied GIFs, Assets.png and House4.png supplied visual references. The GIFs are full scene previews; their later transparent frames encode changes to those scenes. They are not used as isolated NPC animations.

**Custom project artwork — built-in imagegen**

Generated terrain, broadleaf trees/banana plants, six NPC roles with directional/work poses, four Bukidnon-inspired raised homes, and village craft props. The style follows the supplied references. These are original custom game sprites rather than the paid pack's original NPC or terrain sheets.

- Exact prompts and reference paths: [art/prompts/rpg-art-prompts.json](../art/prompts/rpg-art-prompts.json) and [art/prompts/rpg-props-prompt.txt](../art/prompts/rpg-props-prompt.txt).
- Included custom source atlases: `art/source/village/{terrain,foliage,npcs,houses-custom,props}.png`.
- Included custom runtime PNGs and source-coordinate manifest: `res/village/` and `res/village/frames.csv`.
- The optional creator sheet stays in `art/local-village/source/`, and its packed houses stay in `art/local-village/`.
- The creator assets stay excluded from Git. Fresh clones use cached custom alternatives at the full collision scale for house slots 4-7 and need no download. See CREDITS.md for optional creator-house setup.

The transparent atlases retain genuine alpha. Colored RGB in alpha-zero pixels is ignored by the renderer. The importer performs rectangular packing and nearest-neighbor scaling without repainting the generated artwork.

## Bukidnon-inspired setting

The fictional houses emphasize timber posts, raised floors, woven bamboo, broad thatch roofs, verandas and practical steps. Work sites include a loom with cloth, produce baskets, a smith's hearth, a fish rack and a simple water basin. Banana plants and local food/craft themes provide village context. Clothing uses everyday woven-cloth inspiration without claiming a specific community's sacred attire or a uniform tradition.

Architectural and community references remain in [VILLAGE.md](VILLAGE.md), including the NCCA and USTP sources. These visual adaptations do not claim a historically exact reconstruction.

## Scene proportions

GameScale applies one uniform 0.85 scale to Agyu about his feet. His complete body, gear, guard, hurt/death poses, jumps and all six attacks retain their original authored images and hand anchors. Spear damage/bounds, shield spark offsets, body afterimages and dash trails use the matching world scale. The resulting visible idle height is about 42-44 art pixels, alongside villagers around 45-48 pixels. His foot collider is 8.5 x 5.1, close to the villagers' 8 x 5.

The four traditional-house sprites are packed at 80% of their earlier scale. Their collision footprints and shadows shrink by the same factor, while their entry/root positions remain fixed. The optional creator houses 4-7 remain byte-identical locally. Without them, custom alternatives are enlarged about the same foot anchor to match the full-size collision footprints. The importer locks bottom-anchored sprites to the final canvas row after rounding, so traditional houses and asset houses share row 191 and all NPC frames share row 63.

## NPC behavior

Npc composes a cached sprite provider, dialogue lines and an optional CircularList patrol. Six atlas roles supply a farmer, weaver, smith, fisher, vendor and elder; additional villagers share those clothes while patrolling short authored routes. Routes use A* only when beginning a leg and collide in steps of at most three art pixels. Friendly actors do not enter enemy damage targeting or block the player.

Press E near a villager to speak; a fresh press advances the conversation. A contextual name prompt and temporary speech bubble appear near the speaker. Shrine/home beacon interaction has priority. NPC position, animation and speech timers freeze on pause, map/journal, focus loss and hit-stop, and reset with a new journey.

## Rendering and river

RpgArt stages and validates the complete image set, caches it once, and reinstalls cached references when Assets is reloaded. Npc sprites use 112 x 64 canvases with foot row 63, allowing room for fishing rods while keeping bodies aligned. Left-facing walking images are mirrored once at startup.

RpgTerrain prebakes path/grass transitions and four water phases for 16 edge masks. Rendering uses cached image lookups; it does not slice sprites, rebuild terrain or advance simulation. Water/foam, smith fire and basin glints use the simulation clock. Trees, homes, props, NPCs, enemies and culled palisade/gate pieces share the depth-sorted draw list. Root geometry and tree image references are cached, and contextual canopy fading preserves the solid base. Wooden pieces and open/closed gate images are generated once; normal rendering selects existing images.

The river has a winding three-tile channel. Bridge decks follow one center tile with shore landings. Navigation uses actor-center clearance on bridges and their approaches, while exact collision remains active. The full sampled-edge checks validate traversable transitions. The compact health/energy HUD and small green-arrow minimap are retained.

## Connected streets and rebuilt farms

Buildings reserve their lots before streets are constructed. A separate street mask protects the three avenues, district streets, landmark courts and house approaches from farm, prop and tree placement. Breadth-first route construction keeps approaches short and avoids building footprints. Minor dirt paths retain existing stone paving at junctions; they cannot create river crossings. Only the avenues at columns 14, 29 and 52 carry bridges. Every house approach starts at its ground anchor, closing the old grass gap at doorways.

Eight authored crop plots sit beside the northern and middle district streets. FarmPlot caches each complete transparent image once per world: textured earth, raised furrows, vegetables/grain/root-crop leaves, varied growth, a dirt aisle and low bamboo borders with an open gate. Crops and borders are decorative and do not introduce invisible collision. Farms never repaint terrain or sit over a road. Terrain edge masks are also cached once, so normal ground drawing performs image lookups and culled farm blits.

## Reproduce and inspect

After compiling, regenerate packed art from the saved source files:

```powershell
java "-Djava.awt.headless=true" -cp "build/tool-classes;." RpgAssetImporter
java "-Djava.awt.headless=true" -cp "build/test-classes;." NpcTests
java "-Djava.awt.headless=true" -cp "build/tool-classes;." RpgVillagePreviews
```

- [build/preview-roads-overview.png](../build/preview-roads-overview.png): connected streets and final crop placement.
- [build/preview-farms.png](../build/preview-farms.png): three crop styles with open gates.
- [build/preview-rpg-overview.png](../build/preview-rpg-overview.png): full upgraded village.
- [build/preview-rpg-vendor.png](../build/preview-rpg-vendor.png): market and produce vendor.
- [build/preview-rpg-fisher.png](../build/preview-rpg-fisher.png): river and fisher.
- [build/preview-rpg-conversation.png](../build/preview-rpg-conversation.png): contextual conversation.
- [build/preview-rpg-river.gif](../build/preview-rpg-river.gif): actual river and NPC animation loop.

NpcTests adds 8,064 checks covering cached/directional/work art, bounded patrols, all occupations, collision, dialogue cycling and restrictions, render-state preservation, actual E input and held-key suppression, pause/map/focus/hit-stop, beacon priority and fresh restart. See TEST_REPORT.md for the full game result. Generated previews verify rendering; normal human combat/balance testing remains separate.

## Forest extension

ForestRegion reuses the current four tree styles, bark-aligned root colliders and dirt transitions. Woodland grass variants are shaded and cached once; tree art is shared rather than duplicated. The forest, trails and clearings are deterministic. ForestEncounters separates one-time shrine battles from eight bounded outdoor respawn slots. Both the compact minimap and full map include the entire expanded terrain. The former combo HUD card is absent from combat renders.

- [Forest world overview](../build/preview-forest-overview.png)
- [Forest combat without the combo card](../build/preview-forest-combat.png)

## Mountain and river cache

MountainArt constructs the eastern range at half resolution and enlarges it with nearest-neighbor pixels. Broken rock faces, mossy ledges, wooded foothills and an upper feeder frame the four waterfall and mist phases. A transparent lower outlet exposes the actual basin/river terrain. These caches are generated locally by Java and add no external asset dependency.

MountainRiver derives solid cliff tiles from opaque art, carries water below the timber wall through impassable culverts, and reserves four forest crossings plus a basin jetty. Its simulation clock drives falling water, mist and downstream streaks; rendering does not advance timers. ForestRegion's three distant arena records and map labels expose Tank, Mage and Fighter locations. Full-world previews use a 1536 x 1280 canvas for the 192 x 160 world.

- [Mountain and cascade](../build/preview-mountain-waterfall.png)
- [Actual game camera on the viewing deck](../build/preview-waterfall-camera.png)

## Supplied main villains

Tank, Mage and Fighter use the user-supplied sheets preserved in art/source/bosses. BossSpriteImporter packs 452 authored poses into three runtime PNGs with a shared ground anchor and exact source hashes. BossArt loads each atlas once and retains shared frame views for state/facing lookup. No image slicing, scaling or file access occurs during a boss update or draw. See [BOSS_SPRITES.md](BOSS_SPRITES.md) for the mapping and [BOSSES.md](BOSSES.md) for the encounters.

- [Three actual arena encounters](../build/preview-boss-encounters.png)
- [Tank](../build/preview-boss-tank.png), [Mage](../build/preview-boss-mage.png), [Fighter](../build/preview-boss-fighter.png)
