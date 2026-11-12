# Verification report

Date: 12 November 2026 (Asia/Manila)

## Automated result

**PASS: 84275 checks.** Compiled with the installed Java 17 JDK using UTF-8 and -Xlint:all. No compilation warnings or errors were emitted. Git diff --check passed.

Command equivalents, from the project folder:

```powershell
javac -encoding UTF-8 -Xlint:all -d build *.java
java -Djava.awt.headless=true -cp "build;." GameTests
```

## Evidence

- 20,000 fixed-seed random rectangles: indexed tree/house/terrain collision agrees with the brute-force reference.
- The indexed queries made 6069 exact static-obstacle checks in that scenario. Many queries exit on solid terrain or map bounds. This counter is not a benchmark or a percentage speedup.
- All 25 ordered pairs of mission landmarks are connected. A* path lengths match shortest BFS distances; all returned steps are adjacent and walkable.
- Every traversable right/down grid edge is sampled in 17 positions with a 20 x 10 collision box. This catches obstacles between cell centers, including boundary tree trunks.
- Four directional spear attacks each damage their target exactly once.
- Full health does not consume an herb; healing restores health and spends one herb.
- Linked-list wraparound, inventory validation/overflow, quest prerequisites, duplicate rewards, key autorepeat/clear, and defeat claims are checked.
- An automated session exercises title/start, map/journal, pause/resume, all three shrines, guardian unlock/defeat, home victory, death and fresh-session restart.
- Repeated loop startup and shutdown execute without an exception.

## Visual review

Generated and inspected title, gameplay, map, journal, and victory screens. Corrected HUD overflow, title HUD clutter, and beacon label overlap with Agyu.

Generated images:

- build/preview-title.png
- build/preview-game.png
- build/preview-map.png
- build/preview-journal.png
- build/preview-victory.png

These renders come from the automated session. Its zero-second victory time reflects programmatic positioning and enemy defeat.

## Sprite replacement verification

Imported all 71 frames from the exact attached RGBA PNG at a uniform 1:3 scale. Checked consistent canvases, alpha, nonblank content, four-direction animation arrays, exact left/right mirroring, eight-frame strip activation, and the wide death pose. New title, gameplay and four-direction state previews were generated.

## Four-direction spear regression

Checks now cover same-tick movement plus attack, locked facing, recovery buffering, interrupted/dead attacks, exactly one hit per target, targets behind/outside reach, removal of the upright spear from attack bodies, visible windup/contact motion, unclipped front/back weapon pixels, and nonzero camera offsets. A looping GIF, eight-phase directional strip, and real gameplay UP/DOWN screenshots are generated for visual review.

## Mouse and shield regressions

Checked left-click edges and release/repress, held right-click guard, right-click priority, shield release/focus clearing, slow movement/no running, attack cancellation, hurt/dead restrictions, frontal/side/rear cone behavior, immediate rear damage after a frontal block, physical contact repulsion, and retained clicks during hit-stop. The real GamePanel receives mouse events in the integration check. Four-direction shield/block/attack previews and real-camera guard screens are generated.

## Complete guard artwork repair

Removed the destructive guard body mask and separate shield overlay. Dedicated full guard sprites now include the gripping hand and connected bent arm. Added source-to-runtime pixel preservation, torso/leg bands, baseline, and one-complete-image render checks. Front/back/side views were visually reviewed at game scale, separately from the gameplay checks. See GUARD_ART.md for source assets and the exact imagegen prompt.

## Space jump and rear grip revision

Space now emits a distinct jump edge; J and left mouse retain attacks. JumpMotion provides an interpolated 0.54-second, 18-art-pixel hop while ground movement, collision and sorting remain unchanged. The complete sprite and gear rise together above the ground shadow.

Regression checks cover jump/attack separation, key autorepeat, release/repress, exact peak/landing, no double jump or automatic bounce, continuous running and terrain collision, no airborne ground dust, attack suppression, shield on landing, hurt/death falling, and no added damage immunity. Real GamePanel checks cover jump deferral during hit-stop, pause/map/focus handling, settled defeat interpolation, and grounded-only home interaction. Updated HUD and title controls fit the 480-pixel viewport.

The rear-facing right spear hand uses a revised complete guard source. Its translucent bottom fringe is excluded from the foot anchor so opaque soles meet the shared baseline. The three other guard PNGs remain byte-identical. Source/loaded-pixel and complete-pose render checks pass. The updated guard and jump renders were visually inspected at game scale; artwork provenance and exact prompts are in GUARD_ART.md.

New previews:

- build/preview-jump.png: four directions at takeoff, ascent, peak, descent and landing.
- build/preview-jump-in-game.png: actual camera and updated controls.
- build/preview-guard-up.png: corrected rear-facing block.


## Six-move spear combo

Added six distinct moves with an input buffer and a 0.30-second continuation window. Coverage includes all six stages through real keyboard/mouse input, single-slot queue limits, captured next-facing, timeout and finisher wrap, guard/hurt/death/jump resets, all-stage/four-direction windup/active/recovery hit windows, exactly one hit per target per stage, two-damage finisher and rear coverage only for the full spin. The earlier buffered-facing check now queues during the active window because combos can link before full recovery.

Weapon-tip trajectories and windup/contact renders differ across stages. Camera translation preserves body/weapon alignment. Effects remain within the particle/ghost/ring/impact caps, expire correctly, preserve graphics compositing and are not emitted by rendering. A real GamePanel mouse chain includes hit-stop deferral and produces world-camera spin/finisher screens with the combo HUD.

Visually reviewed build/preview-combos.png, build/preview-combo-spin.png and build/preview-combo-finisher.png. The looping build/preview-combos.gif comes from real Player updates. These are deterministic rendering checks, not a human combat/balance playthrough. See COMBOS.md for controls, move data and implementation details.


## Physical body animation

Added72 authored combat bodies across FRONT/DOWN, BACK/UP and RIGHT, with24 exact left mirrors cached at load time. Each move selects anticipation, drive, follow-through and recovery from its actual active timing. Source frames use one uniform4.37254902 source-pixels-per-art-pixel scale, so crouches retain their lower head position.

Checks cover source/cache pixel preservation, true alpha silhouettes, full canvases, exact left mirroring, changing physical poses, phase selection, shared body/weapon hand pivots, camera alignment, grounded shadow/collision/sorting and matching body afterimages. Two explicit grip overrides correct the lunge windup and rising follow-through; their spear overlays were visually inspected.

The spear pose solves angle/reach from each actual fist to a continuous blade path. The thrust now holds its extension through the lunge follow-through and retracts with recovery. The path checks cover every move/direction at1ms intervals, requiring the hand to stay below the14px spearhead and rejecting large trail jumps. A separate review probe measured a minimum blade-to-hand distance14.78px and maximum1ms tip step3.94px.

The updated six-move/four-direction contact gallery, body-only key-pose gallery and real-camera spin/finisher renders were visually inspected. Full and body-only GIFs were regenerated from the real renderer. See COMBAT_ART.md for source images, prompts, importer, overrides and preview paths.



## Health, sprint energy and lightning dash

Replaced individual health markers with a continuous red health bar and a cyan 100-point energy bar. Sprinting spends 25 energy per second only while actually moving. Recovery restores 22 per second after a 0.8-second spending delay. At zero, exhaustion prevents sprinting until 20 energy returns. Health values, damage and herb healing retain their existing balance.

Q or Ctrl starts a normalized eight-direction dash, or dashes along the current facing when stationary. Each burst costs 25 energy, travels up to 115.2 art pixels in 0.16 seconds, and has a 0.55-second cooldown from activation. The active burst evades damage and cancels spear combos. Shield input wins simultaneous ground inputs. Dash cannot begin in midair, while hurt/dead, or while the shield is held. Jump/attack inputs during a burst do not execute later. Collisions stop the dash; travel is subdivided into steps of at most 3 art pixels.

New EnergyDashTests cover real Q/Ctrl edges, autorepeat, delayed hit-stop input, exact resource costs, delayed and bounded recovery, exhaustion threshold, stationary/wall sprinting, all eight dash directions, equal diagonal speed, cooldown/repress behavior, damage protection ending with the burst, coarse-step rock/tree/boundary collision, combo cancellation, jump/guard priority, paused/map/focus timers and restart. Actual HUD fill pixels respond to damage and energy spending. Lightning and cached-body afterimages are capped, expire, clear on restart, preserve incoming graphics state and advance only during simulation.

Visually reviewed build/preview-energy-dash.png at game scale. Refined the lightning spacing and placed trails behind the current character to preserve the readable body silhouette. The title and HUD expose the new controls. These checks do not replace a human balance/playthrough review.


## Village world and live minimap

The world now contains 20 raised houses across 4 generated exterior designs, planted gardens, stone courts/streets, a stream, ponds and timber bridges. Tree and house colliders share the uniform-grid index and exact brute-force reference; navigation is built after placement. All 25 ordered landmark pairs remain connected and their A* routes match BFS shortest lengths.

VillageTests adds 940 focused checks: solid/nonoverlapping house footprints, 600 indexed/exact near-house probes, clear front approaches and reachable nearby navigation cells, dash stopping at every house, valid transparent house canvases, all 4 arrow directions, position interpolation, graphics/world state preservation, exact actual-HUD map pixels, full-map arrow visibility and no combo overlap. The old top-right timer/kill panel and bottom instruction panel are absent.

The minimap caches terrain once per world and overlays current quest markers and Agyu's green facing arrow. The full M map uses the same mapping. Village street, full overview, starting plaza and labelled map images were generated and visually inspected. Source art, the exact built-in imagegen prompt, importer, references and fictional adaptation notes are in VILLAGE.md.

The total assertion count changes with the number of traversable grid edges sampled in the current map; existing navigation, combat, guard, jump, energy, dash and progression checks remain enabled.

## Rich village art, NPCs and river

Imported the creator's free house sheet and five custom imagegen atlases. Runtime caches contain eight house designs, six foliage sprites, 48 NPC poses plus cached left mirrors, 12 prop designs, terrain tiles and prebaked edge transitions. The scene contains 20 homes, 12 NPCs and 16 placed craft/ambient props. Saved sources, prompts, original-asset credits and local-art handling are in RPG_ART.md and CREDITS.md.

NpcTests passes 8,064 checks for art cache identity, facing/work changes, collision-safe patrols, requested occupations, dialogue range/cycling/combat restrictions, graphics-state preservation, actual E input and autorepeat, pause/map/focus/hit-stop, shrine priority and restart. Existing combat, guard, jump, energy, dash, house, minimap and progression tests remain enabled.

The winding river uses animated water and irregular stone/grass banks. Bridges use narrower center decks and shore landings. All mission landmarks remain connected, A* distances match BFS, and the full 17-samples-per-edge collision sweep passes with bridge/approach center-clearance exceptions. Global random collision probes include props, trees, houses and terrain.

Actual-camera market, fisher, smith, loom and conversation PNGs and a river/NPC GIF were rendered and visually reviewed. The final overview includes homes, scenery, props and NPCs. These checks do not establish human play balance or an exact reproduction of the paid asset packs.

## House and character scale alignment

Agyu now renders at 85% size around his ground anchor. The same transform includes his body, spear, shield, jump and six-move combat animation. Swept damage/bounds and world-space particle/afterimage origins scale with the visible weapon/body. His foot collider is reduced accordingly. The four traditional house designs are 20% smaller, with matching shadows and solid footprints; the four original asset house PNGs were verified byte-identical.

The complete regression suite passes after resizing. Guard, jump and combat-body pixel expectations now include the intended display transform while retaining the original source-pixel/hand-anchor checks. Added baseline checks verify every house finishes on row 191 and all 48 NPC source poses finish on row 63. The importer removes one-pixel ground drift by locking bottom alignment after rounding sprite height. A separate render probe measured Agyu's idle silhouette at 42-44 art pixels tall versus 45-48 for villagers.

Regenerated actual-camera river, market and conversation previews and the four-direction side-by-side scale gallery. The smaller traditional-house PNGs preserve their 224 x 192 transparent canvases and fixed foot anchor.

## Repository packaging verification

The game compiled without warnings and passed all 84,275 checks in both the local creator-house configuration and an isolated export of the staged repository. The isolated export contained no res/rpg-village-local folder and used only its own build directory and resources on the classpath.

All 86 included custom runtime PNGs were present before importing. RpgAssetImporter regenerated them from the five included custom source atlases without the optional creator sheet; every PNG retained its SHA-256 hash. The clean-copy test suite then checked gameplay, house silhouettes/baselines, NPCs, collision and rendering using the full-size custom alternatives for house slots 4-7. The actual clean-copy street preview was inspected. Optional original house files remain ignored; CREDITS.md documents manual setup from the creator's free download.

## Limits

The automated progression check uses reflection to inspect session state and programmatic positioning/damage to reach transitions. It does not establish human play length or combat difficulty.

A normal keyboard playthrough, real focus-switch/OS shutdown checks, presentation-laptop performance measurements, and final artwork approval remain human acceptance tasks. The supplied replacement sheet is now imported for Agyu; the guardian still uses enlarged slime art. See SPRITES.md.

The confirmed DSA and four OOP pillars are mapped in PROJECT_PLAN.md. The unspecified remaining syllabus topics and any requirement for custom queue/stack implementations still need instructor confirmation.
