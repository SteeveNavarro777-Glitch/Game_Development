# Agyu spear combos

Tap left mouse or J to start. Tap again during a move to queue the next move. One follow-up is stored at a time, so a single press never plays an entire chain. You can change direction for the next move with WASD/arrows; the current move keeps its facing.

After a move finishes, there is a 0.30-second window to continue. Waiting longer restarts at Crescent Cut. The last move returns to the opener if another press is queued.

| Move | Motion | Duration | Active damage window | Damage per target |
| --- | --- | --- | --- | --- |
| Crescent Cut | Forward cyan crescent | 0.40 s | 0.08–0.28 s | 1 |
| Reverse Fang | Reverse violet sweep | 0.34 s | 0.06–0.22 s | 1 |
| Piercing Lunge | Forward dash and long straight thrust | 0.36 s | 0.08–0.23 s | 1 |
| Rising Talon | Rising crosscut with a small visual lift | 0.42 s | 0.10–0.28 s | 1 |
| Whirlwind | Full turn and circular spear sweep | 0.50 s | 0.12–0.36 s | 1 |
| Sunbreaker | Heavy gold sweep with expanding rune rings | 0.60 s | 0.16–0.34 s | 2 |

The early moves can link after their active window, before all recovery frames finish. One full chain deals at most seven damage to a target that stays in reach. Each stage can damage a given target once. Whirlwind is the only move that attacks behind Agyu; the others remain directional. The gold rings are visual accents, not a separate radial hitbox.

Hold right mouse to cancel a move and defend immediately. Guard, taking damage, death, and a new jump clear the chain. Space still jumps from a free movement state; airborne attacks remain disabled. Movement collisions continue to limit lunges, and jumping keeps its existing terrain rules.

## Effects and implementation

SpearAttack keeps six immutable move definitions. A shared pose drives the visible spear and sampled collision, including thrust reach and the full spinning sweep. SpearAttack owns the current stage; Player owns the pending input, continuation timer and movement. Weapon-angle layering places the spear on the appropriate side of the body.

Effects use Java2D with the supplied cached body and spear art: cyan/violet/gold ribbons, straight thrust streaks, weaponless body afterimages, tip motes, hit stars and two segmented gold rings. The new whole-body combat sheets provide four physical phases for every move. Knees, hips, torso and stance now change with the attack; CombatBody selects the same pose for the hero, grip and afterimages. See COMBAT_ART.md for the generated source sheets and exact prompts. No skin arm overlay is used.

Particles are capped at 256, afterimages at 24, rings at 16 and hit stars at 32. New effects are emitted when simulation time crosses events. Repainting never creates additional effects. Expired objects leave their lists. Stronger finisher feedback is preserved when ordinary hits occur in the same update.

## Verification and previews

ComboTests covers real keyboard/mouse chaining through all six moves, a single queued slot, facing capture, continuation/reset, shield/hurt/death/jump cancellation, damage windows and per-target limits in all four directions, rear spin hits, range, distinct tip trajectories, camera translation, effect caps, effect expiry and render idempotence.

GameTests also dispatches actual GamePanel mouse events through the six moves, including a queued press during hit-stop, and renders the spin and finisher inside the world.

- build/preview-combos.gif: looping four-direction combo animation at 25 fps.
- build/preview-combos.png: all six contact poses in four directions.
- build/preview-combo-spin.png: Whirlwind in the game.
- build/preview-combo-finisher.png: Sunbreaker, gold rings and combo HUD in the game.

Run test.bat for all checks. After compiling, java -Djava.awt.headless=true -cp "build;." ComboTests --previews regenerates only the combo previews.

The thrust holds its extension through the deep lunge and retracts during recovery. The visible shaft and damage segment run from the selected body''s fist to a continuous blade path, so changes of stance do not kink the spin trail.
