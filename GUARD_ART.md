# Shield holding pose repair

The guard state uses one complete 64 x 64 character sprite per direction:

- res/agyu-v2/guard_down.png
- res/agyu-v2/guard_up.png
- res/agyu-v2/guard_left.png
- res/agyu-v2/guard_right.png

These include the intact torso, bent shield arm, gripping hand, shield, spear, and both legs. ShieldGuard never erases body pixels or draws a separate shield. Player renders one whole guard pose; a successful block adds only a small impact glint and sparks.

## Artwork provenance

The built-in imagegen tool created four guard poses using the user's original res/agyu-sheet.png as its character reference. The transparent source is saved as **res/agyu-guard-sheet.png**. Its exact prompt is **res/agyu-guard-prompt.txt**.

The rear-facing right arm was then revised with the built-in imagegen tool, using the original complete guard sheet as the edit reference. The corrected complete rear pose is **res/agyu-guard-rear-v2.png**, and its exact prompt is **res/agyu-guard-rear-prompt.txt**. It connects the sleeve, elbow, forearm and small spear grip with a straight wrist, replacing the reversed-looking hand. Only the rear guard PNG is replaced. Front and both side PNGs were checked byte-for-byte unchanged after import. The other animations continue using the user's supplied sprites.

## Reproduce

GuardSpriteImporter packs the original 2 x 2 cells and substitutes the corrected standalone rear source. The front, rear, left and right poses use the same 51-art-pixel target height, 64 x 64 canvas and boot baseline. The front pose is mirrored to match the existing equipment side. guard-frames.csv records each source file, bounds, scale and foot anchor. Packing preserves source pixels and alpha.

After Java compilation:

```powershell
java -Djava.awt.headless=true -cp "build;." GuardSpriteImporter
```

The two optional arguments specify the original guard sheet and the standalone corrected rear source, respectively.

## Verification

GuardVisualTests compares every loaded guard pixel to its source PNG, checks body bands and the foot baseline, and compares runtime rendering to exactly one complete guard image plus the usual shadow. This protects against detached shield overlays and destructive body masks.

The front, rear, left and right poses were visually reviewed at game scale for connected arms, intact clothes and legs, a clear face, and a supported shield. The revised rear pose was inspected after packing for a readable right spear grip. Mouse attacks, frontal blocks, rear damage, focus clearing and contact repulsion retain their regression coverage.

Previews:

- build/preview-guard-assets.png: packed artwork at 3x.
- build/preview-guard-repair.png: idle and complete blocking poses at 2x.
- build/preview-mouse-shield.png: shield, blocked hit and attack.
- build/preview-guard-down.png, up/left/right equivalents: real game camera.

Space jumps. Holding right mouse during a hop raises the shield on landing. Close any running game and start run.bat to load changed assets.
