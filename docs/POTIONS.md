# Potions, resource bars and hotbar

The top-left HUD shows green health, orange energy and blue potion shield, in that order. The fills have no numbers inside. Labels remain outside, and the day/night clock sits below all three. The existing minimap remains top-right. A nearby boss bar sits above the bottom item interface.

## Item behavior

| Item | Starting supply | Result |
| --- | --- | --- |
| Spear | 1 | Primary click / J performs the existing six-move spear combo |
| Wooden shield | 1 | Hold primary click / J to defend; right-click guards from every selected slot |
| Green health potion | 3 | Restore 2 missing health, clamped to the 5-point maximum; H is a direct shortcut |
| Orange energy potion | 3 | Restore up to 60 energy, clamped to 100; sufficient recovery clears exhaustion |
| Blue shield potion | 2 | Fill 3 points of damage absorption; no timed expiry or passive regeneration |

Potion protection begins empty. Frontal wooden-shield blocks and dash evasion resolve before potion protection. An unblocked hit spends blue protection first, and only excess damage reaches health. The normal hit reaction and brief invincibility still apply. A blue aura uses the supplied shield-ring frames while protection remains.

Select slots using **1-9**, numpad 1-9, the mouse wheel or a click on the box. Click in the world or press **F** to drink the selected potion. Slot clicks only select and never attack or drink. Drag from one box to another to swap their item references, including empty slots. The selected item follows the moved stack. Counts belong to Inventory, so swapping references cannot duplicate supplies. Empty slots do not attack. Keep the spear selected for the existing primary-click combat controls.

A 0.84-second physical drinking action raises, tilts and lowers the selected bottle. Consumption and resource recovery occur once at the sip frame, 0.44 seconds after starting. Empty supplies and full resources consume nothing; usefulness is checked again at the sip. Drinking allows walking at 35% of normal speed, with animated leg steps. A press during an attack, dash, guard, jump, hurt reaction or another drink is rejected rather than queued.

Guarding, jumping, dashing, switching to another item or taking a real hit interrupts drinking. Interruption before the sip spends nothing; a committed sip is not refunded. The action cooldown lasts 0.84 seconds from its start. H selects the health potion in its current rearranged slot and starts the same animation. Selected potions are visibly held, the spear and wooden shield have their own poses, and empty/depleted slots show empty hands. Right-click stows the bottle for defense.

Pause, full map, journal, focus loss and hit-stop freeze the action, effects and cooldowns. Hit-stop preserves a pending selection/use edge until simulation resumes. Restart restores original slots, supplies and empty protection. The refined dock uses larger evenly spaced boxes, corner key/quantity badges, one compact item/status chip and a thin drinking-progress strip. Its layout leaves clear space below the boss bar.

Each villain awards two health potions, one energy potion and one shield potion once. Every second ordinary wildlife defeat awards one health potion. Every fourth ordinary defeat adds one bonus potion, alternating energy and shield. Defeat-event claiming prevents duplicate rewards.

## Code and course labels

- [Hotbar](../src/dsa/Hotbar.java): nine-element private array; constant-time lookup, selection, wraparound and reference swaps.
- [Inventory](../src/dsa/Inventory.java): private HashMap of item quantities with validated additions and consumption.
- [Player](../src/entities/Player.java): owns equipment selection, resources and effects; controls useful-only potion consumption and damage order.
- [EnergyMeter](../src/combat/EnergyMeter.java) and [ShieldMeter](../src/combat/ShieldMeter.java): encapsulated resource recovery and absorption.
- [KeyHandler](../src/app/KeyHandler.java): synchronized immutable snapshots bridge EDT input and fixed-step gameplay.
- [HotbarHud](../src/rendering/HotbarHud.java): the same box geometry drives drawing and scaled mouse hit tests.
- [DrinkMotion](../src/combat/DrinkMotion.java) and [HeldItemArt](../src/rendering/HeldItemArt.java): authored equipment poses, interruptible sip timing and cached walking/drinking frames.
- [PotionArt](../src/rendering/PotionArt.java) and [PotionEffects](../src/rendering/PotionEffects.java): cached icon/effect arrays and three bounded simulation timers.
- [PotionHotbarTests](../tests/PotionHotbarTests.java): original source hashes, damage/restore rules, input, fast scaled drags, pause/reset and text-free bar interiors.

## Supplied art and regeneration

The 25 original user-supplied PNGs are preserved unchanged under `art/source/potions/health`, `energy` and `shield`. Three large bottle icons are cropped using alpha and packed at 32 x 32. All six green, eight orange and eight blue effect frames are preserved in `res/items/effects.png`, a 256 x 96 atlas. `res/items/frames.csv` records coordinates and original SHA-256 hashes. The spear and wooden-shield icons come from current equipment caches; the shield icon excludes Agyu's face and clothing.

```powershell
.\tools.bat PotionSpriteImporter
```

The runtime decodes only three small bottle icons and one effect atlas, and reuses cached subimages for every animation. There is no per-frame disk access, resizing cache creation or runtime download. Generated review images remain in ignored `build/`.

See [EQUIPMENT_ART.md](EQUIPMENT_ART.md) for final sprite source paths, exact built-in imagegen prompts and reproducible packing.
