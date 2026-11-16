# Supplied villain sprite sheets

The user supplied `tank.png`, `mage.png` and `fighter.png` for the three remote boss clearings. The original transparent 864 x 4608 PNGs are preserved unchanged in `art/source/bosses/`. The runtime reads three packed atlases from `res/bosses/`; no Downloads path or network connection is needed when playing.

## Authored grid and animation mapping

Each original sheet uses 144 x 144 cells, six columns and 32 rows. Eight-row groups supply DOWN, UP, LEFT and RIGHT, matching Player.Dir. The first four rows in each group contain idle, walking, jumping and hurt; the last four contain death, attack, power and skill.

| State | Source row within each facing group | Frames |
| --- | --- | --- |
| IDLE | 0 | 4 |
| WALK | 1 | 4 |
| JUMP | 2 | 4 |
| HURT | 3 | 3 |
| DEATH | 4 | 6 |
| ATTACK | 5 | 5 for Tank/Mage; 4 for Fighter |
| POWER | 6 | 6 |
| SKILL | 7 | 6 |

Tank and Mage each supply 152 poses; Fighter supplies 148, for 452 total. The three packed PNGs have 1024 x 2432 canvases. Valid poses are packed in facing/action/frame order into eight columns; transparent trailing atlas cells are never selected. Complete source cells are scaled uniformly from 144 to 128 pixels with nearest-neighbor sampling. This retains authored weapon reach, jump displacement, action glows, shadows and translucent death poses.

Every cached gameplay frame is 128 x 128 pixels, with its ground anchor at `(64,124)`. Render it at `(feetX-64, feetY-124)`. The Tank's visible idle body is about 88 pixels tall; Mage and Fighter are about 76 pixels, compared with Agyu's approximately 44 pixels. The fixed ground anchor prevents per-frame crop jitter.

## Shared runtime cache

[BossArt.java](../src/rendering/BossArt.java) exposes `Kind`, `Anim`, `load`, `count` and `frame`. It validates and loads all three atlases once, then publishes existing subimage references through a kind/action/facing/frame array. Gameplay frame selection does not create, crop or rescale images. The packed frames share atlas pixel storage. Asset startup calls `BossArt.load()` before bosses can render.

## Reproduce the packed art

From the project root with JDK 17 or newer:

```powershell
.\tools.bat BossSpriteImporter
```

[BossSpriteImporter.java](../tools/importers/BossSpriteImporter.java) uses only JDK ImageIO/Java2D APIs. It reads the preserved originals, writes the same three runtime PNGs and [frames.csv](../res/bosses/frames.csv), and exports [the boss sprite review gallery](../build/preview-boss-sprites.png). Each manifest row records the source cell, packed location, ground anchor and SHA-256 hash of the exact source file.

The supplied art is credited to the user as the provided project source; no claim is made here about its original artist or license.
