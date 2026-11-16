# Agyu sprites and spear animation

Agyu's main animations use the supplied **art/source/player/agyu-sheet.png** (941 x 1672 RGBA). The importer exports the 47 movement/hurt/death frames actually used by the game into **res/player/**; four guard images are exported separately. Dedicated blocking poses now use complete reference-matched guard artwork; see **GUARD_ART.md**.

## Actual four-direction combat

The source sprites mostly hold the spear upright, including the side action poses. The runtime therefore separates Agyu's body from the weapon for attacks:

- Cache72 dedicated whole-body combat poses from the three combat sheets, plus24 exact left mirrors. Select load, drive, follow-through and recovery from the active timing. See COMBAT_ART.md.
- Reuse the supplied blue spearhead and a matching pixel shaft as a cached weapon image.
- Solve the spear angle and reach from the authored fist to a continuous blade path: windup, active movement, recovery. The thrust remains extended through the body follow-through.
- Render the pole and cyan blade trail on the world surface, beyond the 64px body cell. Front/back attacks remain visible and cannot be clipped to the character frame.
- Use the current weapon angle to draw behind/in front of the body, including the full spinning turn.
- Use the same directional pose, reach and hand pivot for the visible blade and damage sweep.
- Sample the moving weapon during its active interval and remember hit enemies in a per-attack set, so each target takes damage once.
- Resolve movement-facing before starting the attack and lock it until recovery. Store one next-move press throughout the swing; allow 0.30 seconds to continue after recovery. Guard/jump/damage/death reset the chain.

The opening cut lasts **0.40 seconds**, with damage active from **0.08 to 0.28 seconds**. The six-stage combo adds a reverse cut, long thrust, rising cut, full spin and heavy finisher with distinct timing, reach and trails. See **COMBOS.md** for the complete move table. Shared poses keep every moving tip aligned with its damage sweep.

Current combat body poses and the weapon image are prepared once in Assets.load. No new image buffers, source slicing, or image-generation requests occur during normal rendering.

## Source row mapping

| Row | Imported prefix | Runtime use |
| --- | --- | --- |
| 1 | front | Front idle/movement pose |
| 2 | back | Back idle/movement pose |
| 3 | idle | Right idle; mirrored for left |
| 4 | walk | Side walk, 8 frames at 16 fps |
| 5 | run | Side run, 8 frames at 24 fps |
| 6 | action | Source-only row; omitted from runtime export |
| 7 | attack | Source-only row; omitted from runtime export; combat uses the shared moving spear |
| 8 | reaction | Export frames 5-8 only: front hurt then side hurt |
| 9, first six | back_reaction | Export the first two frames for back hurt |
| 9, wide final pose | die | Death pose, mirrored for left |

Vertical walking still uses the supplied front/back variations with a small bob; the sheet has no complete vertical leg cycles. Vertical combat now has an actual moving spear rather than an idle body plus a thin white line.

## Mouse combat and shield

Left-click attacks; holding right-click raises the shield in the current facing. Movement turns the guard, at 40% walking speed. Guarding cancels current/queued attacks and disables running. The shield blocks sources within a frontal 120-degree cone; side/rear hits remain dangerous. Successful blocks repel contact enemies and use a separate feedback cooldown without granting HP invincibility.

The large generated skin-colored forearm/hand overlay is removed. The spear uses the supplied arm pixels and a lower torso grip with a small extension. Complete guard poses are cached at load time. Guard rendering never masks the body or overlays a separate shield. Focus loss clears both mouse buttons, and hit-stop preserves fresh attack clicks.

build/preview-mouse-shield.png shows raised shields, successful blocks and cleaned attacks in all four directions. build/preview-guard-down.png and the corresponding up/left/right files show shields in the real game camera.

## Complete shield-holding poses

The repaired guard state uses a complete sprite per direction with the bent arm and shield already drawn together. See **GUARD_ART.md** for the source artwork, exact imagegen prompt, importer and pixel-preservation checks. No body-color mask, ellipse erasure or extra shield overlay remains.

## Import and verify

All live body frames use a 64 x 64 canvas, baseline 63, and uniform 1:3 nearest-neighbor scale. The corpse uses 96 x 64. Original import colors/alpha are retained.

```powershell
java "-Djava.awt.headless=true" -cp "build/tool-classes;." SpriteImporter
```

The importer uses only the JDK. frames.csv records crop/anchor coordinates. The retired Python slicer has been removed.

Run **test.bat** for gameplay, sprites and combat regression checks.

- **build/preview-spear-swings.gif**: actual-speed looping four-direction attacks.
- **build/preview-spear-swings.png**: eight attack phases in each direction.
- **build/preview-combat-up-in-game.png**, **build/preview-combat-down-in-game.png**: combat in the real world camera.
- **build/preview-sprite-states.png**: all actor states/directions.
- **build/sprite-contact-sheet.png**: original imported poses before runtime weapon separation.

## Jump and rear grip revision

Space jumps; J and left-click attack. JumpMotion composes a 0.54-second, 18-pixel hop with ground movement. The shadow stays on the ground and the complete sprite moves upward with interpolated height. Terrain collision remains active; attacks and shield entry wait until grounded. A held right mouse raises the shield after landing.

The rear guard pose now uses art/source/player/agyu-guard-rear-v2.png, edited from the original complete guard artwork to correct the right wrist and spear grip. GuardSpriteImporter scales this separate source to the same 51-pixel art height and records its source in guard-frames.csv. The other three guard source poses remain the same.

## Combo effects

ComboEffects emits bounded afterimages, tip motes, hit stars and gold rune rings during update events. SpearAttack renders colored weapon ribbons and straight thrust streaks. All character art remains cached; no image-generation requests or body reconstruction occur during play. See COMBOS.md and build/preview-combos.gif.

## Authored body motion

See COMBAT_ART.md for the72 complete combat sprites, cached96-directional-pose set, shared fist metadata, two explicit grip corrections and uniform-scale importer. Player rendering and afterimages use the same selected body. Ground collision and sorting stay at the world feet; changing body stances preserve a grounded shadow.
