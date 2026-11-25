# Shared character and shield walking

Agyu now uses the same authored body atlas, 51-pixel body registration and shared foot anchor for the spear, wooden shield, all potions and empty hands. Changing equipment changes the item layers. Standing, walking, sprinting, jumping and dash afterimages keep the same character proportions. Authored combat and death animations retain their existing game scale.

The shield stance keeps the left arm braced while the right hand points the spear in the facing direction. The side profiles show the shaft extending beyond the shield. Front and rear views shorten the projected shaft so it stays close to Agyu's body. Left and right reuse the leg and boot pixels from the eight original side-walk sprites. Those frames are fitted to the standing boot width and register each planted sole to the same ground anchor. The fit tapers at the waist. Ground travel advances the fitted footfalls beneath the current equipment pose.

HeldItemArt prepares every body/item/guard combination at startup. ShieldGuard handles the directional protection cone and impact feedback. The complete source poses remain as art references. WoodenShieldArt supplies the shield faces and inventory icon at a consistent size.

## Provenance and verification

The original retained guard references are res/player/guard_down.png, guard_up.png, guard_left.png and guard_right.png. The common holding/drinking body source is art/source/player/agyu-held-items-v1.png; EquipmentSpriteImporter regenerates its atlas and wrist manifest. Exact generation records remain in docs/EQUIPMENT_ART.md and art/prompts/.

PresentationTests compares the actual cap geometry across every equipment selection in idle, walk, run and guard states. InventoryGuardTests verifies cached moving legs, guard speed, planted idle feet and real inventory input. The preview at build/preview-profile-walk.png shows every left and right footfall with the spear carried and with the shield raised.
