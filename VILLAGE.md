# Village map and house artwork

The playable 64 x 48 tile world is a fictional Bukidnon-inspired village. The current eight-design house set, detailed scenery, 12 NPCs and animated river are documented in RPG_ART.md. The original village iteration and cultural references are retained below. House exteriors are solid; entering buildings is outside this update. The existing shrine/guardian journey remains playable in the new setting.

The top-right time/kill panel has been replaced by a live 96 x 72 minimap. Its bright green arrow follows Agyu's interpolated ground position and rotates with his facing. M opens the larger labelled village map using the same arrow. Quest markers update as shrines and the guardian are completed. The combo panel sits below the minimap. The top-left HUD consists only of health and energy bars with their labels; the large background card, title, inventory counts, dash status and control hints have been removed.

## Architectural references

This is a fictional adaptation, not a reconstruction of one community's houses. The exterior design uses natural bamboo/wood walls, raised floors and posts, broad thatched roofs, porches and short steps.

- [NCCA: History of Philippine Architecture](https://ncca.gov.ph/about-ncca-3/subcommissions/subcommission-on-the-arts-sca/architecture-and-allied-arts-2/history-of-philippine-architecture/) discusses broader Philippine vernacular features; it does not establish a uniform house style for every Bukidnon community.
- [NCCA: The Talaandig](https://ncca.gov.ph/about-ncca-3/subcommissions/subcommission-on-cultural-communities-and-traditional-arts-sccta/central-cultural-communities/the-talaandig/), by Victorino Saway, provides community and agricultural context.
- [USTP: Talaandig community architectural project in Tagbak, Talakag](https://www.ustp.edu.ph/empowering-indigenous-communities-ustps-sustainable-development-initiative-for-the-talaandig-tribe/) describes community-requested planning that incorporates cultural identity and local needs.

The assets do not claim sacred functions, a particular house name, authentic ornament meanings, or that Bukidnon's indigenous communities share one architectural tradition.

## Saved art and generation prompt

The **built-in imagegen tool** created the transparent four-building atlas. No external image API or CLI was used.

- Source atlas: [res/village-houses-sheet.png](res/village-houses-sheet.png), 1254 x 1254 RGBA.
- Exact generation prompt: [res/village-houses-prompt.txt](res/village-houses-prompt.txt).
- Runtime sprites: [res/village/house_0.png](res/village/house_0.png), [house_1.png](res/village/house_1.png), [house_2.png](res/village/house_2.png), [house_3.png](res/village/house_3.png).
- Source bounds and shared scale: [res/village/frames.csv](res/village/frames.csv).

VillageHouseImporter locates the four main connected alpha silhouettes rather than assuming the generator followed the requested exact grid dimensions. It copies each complete building bounding rectangle, retains alpha, and packs all four at one nearest-neighbor scale into 224 x 192 canvases with a shared foot baseline. It does not repaint the artwork. VillageArt caches these images and builds the code-native terrain textures once at startup.

Reproduce the sprite packing after compiling:

```powershell
java -Djava.awt.headless=true -cp "build;." VillageHouseImporter
```

## Map and collision implementation

World holds the authored lot table and tile arrays. Both trees and houses implement its private Obstacle contract and enter the same uniform-grid collision index. The brute-force reference includes both, and navigation is built after all structures are placed. Roads and open courts connect mission areas. Three-pixel dash substeps apply to building walls as well as terrain.

VillageMap caches a 256 x 192 terrain image once per world. Rendering scales this cache and draws only the changing markers and player arrow. Buildings and trees join the existing depth-sorted Drawable list with culling margins sized for their art. The removed bottom instruction panel stays absent.

## Verification and previews

VillageTests checks every house for solid walls, nonoverlapping footprints, a reachable front approach and collision-safe dashing; it also compares indexed/exact collision around houses. It checks house alpha canvases, four facing arrows, interpolated position, unchanged graphics/world state, actual HUD minimap pixels, full-map arrow visibility and separation from the combo panel.

- [build/preview-village-overview.png](build/preview-village-overview.png): complete village artwork.
- [build/preview-village-street.png](build/preview-village-street.png): actual game camera, house exterior and live minimap.
- [build/preview-game.png](build/preview-game.png): starting plaza.
- [build/preview-map.png](build/preview-map.png): full labelled M map.

These images are produced by the real renderer and were visually reviewed. Automated checks validate geometry and progression; they do not establish human play balance. See TEST_REPORT.md for the current full-suite result.
