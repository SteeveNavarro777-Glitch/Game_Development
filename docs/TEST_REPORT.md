# Verification report

Date: 16 November 2026 (Asia/Manila)

## Automated result

**PASS: 550,037 checks.** JDK 17 compilation with UTF-8 and `-Xlint:all` produced no warnings or errors. The updated test.bat launcher compiled the 44 gameplay files plus 16 test suites into a clean `build/test-classes` folder and ran GameTests successfully.

```powershell
.\test.bat
```

Gameplay and developer tools also compile independently through `build.ps1 -Target game` and `build.ps1 -Target tools`. Their output folders are separate from tests. Each compilation removes only that target's old class files, preventing stale legacy bytecode from hiding a missing dependency.

## Organization and cleanup checks

- 66 Java files are classified into gameplay, tests and current developer tools; every file has a purpose header and 840 method/constructor blocks have purpose labels.
- Inline sections mark the actual linked list, arrays, queues, stack, maps, sets, graph algorithms, sorting and OOP examples. CODE_GUIDE.md and the generated file/function indexes map them to their game uses.
- Removed 151 audited obsolete or duplicate files, including 146 PNGs, the old Python slicer and superseded house importer. CLEANUP_REPORT.md lists the exact paths.
- Removed overwritten procedural terrain/tree art, dead caches/constants/helpers, invisible notifications and the masked-idle attack fallback. Healing, shrine rewards and guardian spawning still run through their real state transitions.
- Current attack tests use all 96 directional authored combat poses; effects and rendering use the same mandatory body cache as the game. The assertion total changed as checks of the removed cache were replaced with current-pose checks.
- The four prior importers preserved all 208 pre-boss runtime PNGs and four local house overrides in the cleanup audit. The added BossSpriteImporter reproducibly packs the three supplied villain atlases and their manifest; regeneration is byte-identical. There are now five importers and 211 runtime PNGs. Retired frames are not recreated.
- The earlier cleanup audit ran VillageTests in an isolated public copy containing source, tests, runtime images and build.ps1 without the optional local-art folder (956 checks). The current custom house alternatives load and render correctly.
- The tools.bat preview launcher compiles the six current tools and exports the village/NPC/river views.

## Gameplay and DSA evidence

- 20,000 fixed-seed random collision probes: the indexed tree/house/prop/gate/terrain result matches brute force. The indexed queries made 27,916 exact obstacle checks in this scenario; this is a correctness counter, not a timing speedup claim.
- With the gates open, all 25 ordered pairs of mission landmarks are connected. A* route lengths match shortest BFS distances, and returned steps are adjacent and walkable.
- Every navigable right/down grid edge is sampled at 17 positions using the real 20x10 collision probe.
- Circular-list append/current/advance and wraparound, inventory validation/overflow, duplicate quest rewards, prerequisites, keyboard/mouse edges and focus clearing remain checked.
- Real update/interaction methods exercise title/start, map/journal pause, three shrine rewards, guardian unlock/defeat, home victory, defeat and fresh-session restart.
- Shared flow-field updates, path reconstruction, FIFO reward processing and sorted rendering remain active after cleanup.

## Combat, movement and actors

- Current source pixels, transparency, canvas sizes and exact left/right mirrors are checked for movement, guard and authored combat bodies.
- Four-direction spear motion, actual hand pivots, sampled blade paths, same-tick movement/facing, hit windows, exactly-once stage damage, six-move chaining and interruption/reset behavior remain covered.
- Mouse/keyboard input, shield cones and contact repulsion, grounded/airborne priorities, jump interpolation, sprint energy, exhaustion/recovery, lightning dash travel/cost/cooldown and collision substeps remain covered.
- Effects stay capped, expire, reset, restore graphics state and advance during simulation rather than rendering.
- Friendly NPC roles, cached poses, work/patrol behavior, conversation input, pause/focus/hit-stop and restart remain covered.

## Village and farms

VillageLayoutTests checks three seeds. It flood-fills authored road surfaces separately from incidental riverbank dirt and walkable grass; checks road centers against actual collision; connects all 20 doors, five village courts and eight farm gates; restricts bridges to the planned avenues; and verifies edge caches.

Farm plots are checked against roads, water, houses, props, other plots and tree canopies. Same-seed layout/art, stable cached images and rendering without terrain mutation are checked. House silhouettes/feet, clear approaches, live map arrows and compact HUD placement remain covered.

## Tree collision and wooden defenses

TreeBoundaryTests checks all four supplied tree styles and every placed root collider. Root geometry is measured from opaque bark once per style, including the first style's elevated floor. Indexed collision agrees with brute force near roots and dynamically opened/closed leaves. Coarse walking, sprinting, jumping, dash, knockback and enemy movement are sampled in four directions; three-pixel substeps prevent thin-obstacle tunneling.

All four gates begin closed, stop actors, and connect to the road network. The tests open each gate through World interaction, verify A* routes and sampled passage edges, cross to the outside landing, and close it from either side. Player, enemy and NPC occupancy independently prevents unsafe closure. Real GamePanel E input, held-key suppression, navigation-version invalidation, map/pause and restart state are checked. Minimap markers update without replacing the terrain cache.

Actual renderer previews cover all four gates both closed and open. Canopy fading restores the incoming graphics state, preserves opaque roots and keeps Agyu visible behind the leaves. Visual review confirmed gate/wall alignment and the north forest backdrop. Game and tools targets compile independently: 44 gameplay files and 50 gameplay/tool files, with no compiler warnings.

## Forest and outdoor encounters

The expanded world is 192 x 160 tiles, with the preserved 64 x 48 village inside its timber wall. ForestTests verifies dense, deterministic outdoor trees, solid roots, protected trail cells and reachable combat clearings. The three shrines and guardian now occupy the forest. Four gate feeders connect to a woodland loop; sampled route checks use actual collision, and closed gates prevent entry. Both outbound and return-HOME objective hints lead to reachable gate approaches when a gate is closed.

All 18 initial ordinary enemies spawn in free outdoor positions: ten shrine guards and eight ambient slots. Tests kill enemies and exercise the 25-second ambient cooldown, suppress spawns while inside the village or near Agyu, verify bounded replenishment, and confirm mission guards remain defeated. Real map/pause/restart inputs freeze or replace encounter state correctly.

The attack card's entire former HUD region stays transparent during an active combo. Attacks remain functional. The full terrain map includes the southeast forest clearing and correctly maps its player arrow. Visual review covers the real forest combat camera, expanded world overview and map.

## Distant arenas, mountain and continuous river

ExpansionTests verifies three large boss clearings, each at least 480 art pixels from the village after accounting for its arena bounds. At least 100 central floor cells per arena are clear and walkable. All three approaches match shortest BFS distances and pass sampled collision checks. The spaces now contain exactly one assigned Tank, Mage or Fighter, with no ordinary spawn placed inside the arena. The existing shrine/guardian mission sequence remains active alongside the three required villain objectives.

A hydraulic BFS follows WATER, BRIDGE and solid CULVERT cells from the far-right waterfall source through the existing village river to its western outlet. Both wall culverts remain impassable and the gates remain protective. Four forest bridges and the viewing jetty retain connected actor navigation. Cliff tiles derive from opaque mountain pixels, and the actual deck render verifies that Agyu remains visible.

The large mountain, all four waterfall/mist phases and rock textures are shared caches. Repeated rendering is identical until the simulation clock advances, terrain remains unchanged, graphics state is restored and the distant mountain is culled from the village view. The waterfall camera lifts only near the basin and keeps the actor on screen. Full-world PNG exports use quarter-scale canvases (1536 x 1280) to avoid huge temporary images.

The focused expansion run passed 3,835 checks before the final full-suite run. Visual review covered the full forest, the three remote spaces, the connected watercourse, labelled map and actual viewing-deck camera. Tree construction now queries local temporary root buckets rather than scanning the entire growing forest for every candidate.

## Main villain bosses

ArenaBossTests passed 1,582 focused checks before the final full suite. It validates all 452 directional authored poses for stable cached identity, nonempty alpha, dimensions and distinct facings. Real left-mouse input drives Player and the swept spear against each villain's hurt box. Population checks prove Clearing 1/Tank, 2/Mage and 3/Fighter spawn outdoors on free ground, while distant bosses remain dormant.

Actual updates verify Tank's warning-before-damage, heavy cleave, shield/dash defenses and jumpable slam; Mage's delayed projectile fan, solid-terrain blocking, bounded spell population and cleanup on disengagement; and Fighter's sword damage, swept charge collision and return to its arena. Boss death frames finish before removal, defeat claims are once-only, and repeated rendering preserves simulation and graphics state. Ground warnings render beneath the characters.

Session checks freeze encounters during map/pause, record boss quest/map transitions without rebuilding static terrain, award two herbs once and reset all three villains on a fresh journey. A mixed boss/ordinary kill test confirms that boss rewards do not alter the every-two-ordinary-enemies herb cadence. Full progression refuses HOME victory until all three villains and the awakened guardian are defeated. Existing airborne/NPC beacon fixtures include these new prerequisites.

The map labels all three bosses and colors completed markers green. The journal lists their independent objectives; the navigation trail leads to remaining villain arenas after the shrine/guardian chain. Continuous boss health bars appear only during nearby engagements. Actual-camera previews show each supplied boss and its warning footprint; the sprite importer also exports a source-pose review sheet.

## Five-minute day/night cycle

DayNightTests contributes 66 focused checks. It advances 17,999 then 18,000 fixed 60 Hz steps to prove Day 1/Night 1 timing, verifies dawn after the next interval, preserves coarse-update remainders and counts repeated numbered cycles. Dusk/dawn lighting is continuous at both boundaries; invalid elapsed inputs do not corrupt the clock.

Real ordinary-enemy contact gains one damage outdoors at night, pursuit travel increases by exactly 25%, and village enemies retain their day values. Dawn and later nights neither stack power nor change health/maximum health; dead enemies stay dead. All three villain attack paths and the guardian pulse use the same rule. Boss warnings and maximum health stay unchanged, frontal shields remain effective, and a real input/energy dash still evades damage. New and replenished night spawns inherit power immediately.

Actual GamePanel inputs verify title/map/journal/pause/focus/hit-stop/defeat clock freezing and a fresh Day 1 after restart. Lighting queries preserve simulation and graphics state, reuse the viewport mask, darken distant scenery and keep the player light pool readable. The same village camera is exported in day/night, along with an empowered boss and the waterfall. The health HUD remains unshaded.

## Rendering evidence

The tests export title, gameplay, map, journal, victory, attack, guard, jump, dash, body-motion, NPC and village previews under build/. Useful current outputs include:

- [Village by day and night](../build/preview-day-night.png)
- [Powered boss encounter at night](../build/preview-night-boss.png)
- [Night waterfall](../build/preview-night-waterfall.png)
- [All three villain encounters](../build/preview-boss-encounters.png)
- [Supplied boss sprite gallery](../build/preview-boss-sprites.png)
- [Waterfall and viewing deck](../build/preview-waterfall-camera.png)
- [Mountain and cascade](../build/preview-mountain-waterfall.png)
- [Forest combat without the attack panel](../build/preview-forest-combat.png)
- [Expanded forest and village overview](../build/preview-forest-overview.png)
- [Four cardinal gates, closed and open](../build/preview-wooden-gates.png)
- [Tree root footprints and canopy visibility](../build/preview-tree-roots.png)
- [Complete wooden perimeter](../build/preview-wooden-boundary-overview.png)
- [Roads and village overview](../build/preview-roads-overview.png)
- [Three farm crop styles](../build/preview-farms.png)
- [Current six-move combat gallery](../build/preview-combos.png)
- [Actual-camera village street](../build/preview-village-street.png)
- [Detailed village/NPC overview](../build/preview-rpg-overview.png)

## Limits

The automated journey uses programmatic positioning and enemy damage to verify transitions. It does not establish human play length, combat balance, OS/window behavior or presentation-laptop performance. A normal keyboard/mouse playthrough remains a human acceptance task.

The implemented DSA topics and four OOP pillars are mapped in CODE_GUIDE.md and PROJECT_PLAN.md. Additional syllabus requirements beyond the supplied topics remain unspecified.
