# Verification report

Date: 4 November 2026 (Asia/Manila)

## Automated result

**PASS: 91592 checks.** Compiled with the installed Java 17 JDK using UTF-8 and -Xlint:all. No compilation warnings or errors were emitted. Git diff --check passed.

Command equivalents, from the project folder:

```powershell
javac -encoding UTF-8 -Xlint:all -d build *.java
java -Djava.awt.headless=true -cp "build;." GameTests
```

## Evidence

- 20,000 fixed-seed random rectangles: indexed tree/terrain collision agrees with the brute-force reference.
- The indexed queries made 3,291 exact trunk checks in that scenario. Many queries exit on solid terrain or map bounds. This counter is not a benchmark or a percentage speedup.
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


## Limits

The automated progression check uses reflection to inspect session state and programmatic positioning/damage to reach transitions. It does not establish human play length or combat difficulty.

A normal keyboard playthrough, real focus-switch/OS shutdown checks, presentation-laptop performance measurements, and final artwork approval remain human acceptance tasks. The supplied replacement sheet is now imported for Agyu; the guardian still uses enlarged slime art. See SPRITES.md.

The confirmed DSA and four OOP pillars are mapped in PROJECT_PLAN.md. The unspecified remaining syllabus topics and any requirement for custom queue/stack implementations still need instructor confirmation.
