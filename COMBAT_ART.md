# Agyu physical combat animation

Agyu's six spear moves now use dedicated whole-body animation: anticipation, drive, follow-through and recovery. The frames change knee bends, foot placement, hip/shoulder rotation, torso lean and head height. The spear and body use the same recorded hand anchor.

## Source artwork

Created with the built-in imagegen tool using the supplied Agyu sheet and the established guard artwork as character/style references:

- [Right-facing combat sheet](res/agyu-combat-right.png)
- [Front/down combat sheet](res/agyu-combat-down.png)
- [Rear/up combat sheet](res/agyu-combat-up.png)
- [Original right-facing reference for the later sheets](res/agyu-combat-right-source.png)
- [Final generation/edit prompts](res/agyu-combat-prompts.json)

Each final sheet has four columns by six rows. Columns are load, drive, follow-through, recovery. Rows are Crescent Cut, Reverse Fang, Piercing Lunge, Rising Talon, Whirlwind and Sunbreaker.

The complete body sprites include connected arms/hands, intact legs/clothes and the shield strapped on Agyu's back. The moving spear and existing effects are rendered separately. Attack poses never erase or reconstruct body parts.

## Import and runtime

CombatBodyImporter packs72 authored poses (DOWN, UP, RIGHT), using a common scale and64 x64 canvases. LEFT uses24 exact mirrored poses at load time, giving96 directional key poses. Crouched frames keep their shorter silhouettes instead of being stretched to standing height.

res/agyu-combat/frames.csv records source cells, roots, scale and the local hand anchor for every pose. The common source scale is4.37254902 pixels per art pixel. res/agyu-combat/grip-overrides.csv explicitly corrects right stage2 phase0 to(10,-11) and right stage3 phase2 to(14,-28), using zero-based stage/phase indices. The importer reapplies these corrections reproducibly. CombatBody caches the PNGs and left mirrors during Assets.load. The selector follows each move's active timing: anticipation before activeStart, drive during the first active half, follow-through during the second half, then recovery.

Player, the weapon pose/damage sweep and afterimages all query the same selector. The visible spear pivots at the authored fist, with angle/reach solved toward a continuous blade path. The thrust holds its extension through follow-through, then retracts during recovery. Body offsets are applied equally to the body, grip and afterimage. Ground collision, Y sorting and the shadow remain tied to Agyu's actual world feet.

Reproduce after compiling:

```powershell
java -Djava.awt.headless=true -cp "build;." CombatBodyImporter
java -Djava.awt.headless=true -cp "build;." GameTests
```

## Verification and previews

CombatBodyTests checks cached/source pixels, real transparency, canvas bounds, exact mirroring, changing alpha silhouettes across phases, timing, shared hand pivots, complete body rendering, grounded shadow/collision, camera alignment and matching afterimages. Existing six-stage damage, chaining, shield and jump tests remain in the full suite.

- build/preview-grip-overlay.png: corrected lunge/rising grips with the actual spear.
- build/preview-body-motion.png: all96 body poses with weapons/effects hidden.
- build/preview-body-motion.gif: body-only four-direction motion.
- build/preview-combos.gif: full six-move fighting animation with the spear and effects.
- build/preview-combo-spin.png and preview-combo-finisher.png: actual game camera.

The PNG sources retain alpha, including colored RGB values in some fully transparent pixels. Import and rendering use the alpha channel. Solid-background previews show the game-visible silhouettes.

Close the current game and reopen run.bat after changing the assets.
