# Verification report

Date: 24 November 2026 (Asia/Manila)

## Blacksmith equipment progression

`SmithShopTests` passes 80 focused checks covering Rare-before-Legendary prerequisites, inventory-only equipping, exact prices, +1/+2 spear damage, wider shield arcs, guarded movement bonuses, fixed-value coin partition and exact-once wallet collection. The supplied sheet renders as four isolated models in `build/preview-smith-shop.png`; all 32 spear/shield effect frames are checked and exported in `build/preview-equipment-effects.png`. The four directional held models are exported in `build/preview-equipment-directions.png`. All 92 gameplay/test Java files compile together.

## Full-suite result

**PASS: 587,144 checks.** The latest `test.bat` run includes smith purchase/equip behavior, all 32 tier-effect frames, guarded walking, the I-key inventory, source-derived waterfall animation, the registered pond/mountain and all previous gameplay. Totals can vary slightly with randomized journey populations. The spatial-index comparison required 28,025 indexed solid checks for 20,000 reference queries.

Gameplay (60 sources), tests (85 sources: 60 gameplay + 25 regression suites) and tools (70 sources: 60 gameplay + 10 tools) compile independently with JDK 17, UTF-8 and `-Xlint:all`. Fresh output folders prevent stale classes from satisfying compilation. There are no third-party Java dependencies or runtime network calls.

```powershell
.\test.bat
```

## Potions and hotbar verification

- All 25 supplied original PNG hashes match the manifest. All three potion icons, two current gear icons and 22 supplied effect frames are present, transparent and cached.
- Health and energy restore only missing resources and clamp at capacity. Blue protection absorbs unblocked damage before health. Physical guard, invincibility and dash evasion preserve protection; excess damage reaches health exactly.
- Full resources, empty supplies, cooldown, death, jumping, attacking, dashing, hurt reactions and guarding do not waste potions. Orange recovery can clear exhaustion. Simultaneous shield input wins over healing.
- Actual number keys, primary/right mouse and wheel input select or use the correct item. Spear combos, selected-shield defense and right-click defense still work. Slot clicks never attack or drink. Scaled same-frame drags preserve item identity, selection and inventory counts, including swaps into empty slots.
- Map, journal, pause and focus loss freeze effects; hit-stop preserves a pending selection and potion-use edge. Restart restores supplies, original slots and empty protection. Painting does not advance animation, and one-shot effects expire.
- The actual HUD is rendered offscreen. Every interior pixel of all three full bars is the expected green/orange/blue fill or highlight, with no numbers. Existing partial-bar assertions still prove that damage and energy spending shorten their fills.
- The actual village and all three boss cameras were visually reviewed. The wooden-shield icon excludes Agyu's face/clothes. Boss bars sit above the selected-item labels and nine bottom boxes. The minimap and three-minute clock remain readable.

## Current behavior verified

- All five beacon structures, floor outlines, site labels, night glows and live minimap markers are removed, including the village spawn. Pressing E at their former positions does not grant rewards, spawn an extra guardian or advance a hidden quest.
- Three independent villain defeats unlock the final forest objective. The actual session completes automatically after the third defeat once Agyu is grounded and has finished a dash. NPCs and gates retain real E interactions; victory and defeat restart correctly.
- Twelve herds contain one to four boars each. Tests cover twelve layout seeds, reproducibility, distinct layouts, wide map coverage, member identity and proximity, terrain/foot clearance, bounded capacity, delayed offscreen replenishment and pause/reset state.
- The live map retains its player arrow, villain objectives, gates, mountain and waterfall. Former beacon pixels match the underlying cached terrain. Actual spawn/clearing cameras and the annotated distribution overview are exported for visual review.
- Sigbin spawning requires night and deep-forest exploration. A real reveal precedes persistent hunting and claw attacks. Player spear input can stagger and defeat it; one-time defeat events, all death poses, dawn removal, village safety, paused clocks and spawn cooldowns are checked.
- Wolves retain their total limit of two or three per night, without replacements after kills. Real clock transitions, pounces, night power, shield blocks and harmless dawn departures are exercised.
- Existing DSA/inventory contracts, collision equivalence, shared BFS/A* routing, six-stage spear combos, physical body animation, shield poses, jump, sprint/dash energy, NPC movement/dialogue, road/farm layout, trees/gates, mountain/river, villain states and three-minute day/night lighting remain in the full suite.

## Source, assets and rendering

95 Java files: 60 gameplay, 25 regression suites and 10 developer tools. 1173 method/constructor blocks have purpose labels. File/function indexes were regenerated.

Runtime `res/` contains 229 active PNGs, including three small potion icons and one 22-frame effect atlas. The 25 potion originals and 67 wildlife originals are preserved unchanged with source hashes. Potion assets regenerate deterministically using `PotionSpriteImporter`. Optional creator houses stay in ignored `art/local-village/`; build outputs and previews stay ignored.

Local Markdown targets and whitespace are checked after documentation regeneration. Visual evidence is saved in `build/preview-potions-hotbar.png`, `build/preview-potion-icons.png` and `build/preview-boss-encounters.png`. Potion images are decoded at startup and reused; no frame-by-frame image preparation is needed. Optional synthesized forest audio is disabled in headless verification.

## Held-equipment and drinking verification

- Forward side guards place the spearhead in front of the shield; the original front and corrected rear guard PNG hashes remain unchanged. The two import tools regenerate every guard/body output byte-identically.
- All selected gear, three bottles and empty hands use cached registered art. Every prepared item/direction/pose has clear image borders; swapping/depleting supplies updates what is held. Slot changes clear the old potion status label immediately.
- A real 0.84-second raise/sip/lower action consumes once at 0.44 seconds, including coarse-step crossings. Resource usefulness is checked again at the sip. Guard, damage, another slot, jump or dash can interrupt without pre-sip consumption. Committed sips are not refunded.
- Map, journal, pause and focus loss freeze an active drinking pose, and resuming completes it. Walking remains slow during sipping. A separate walking review confirmed that the two authored leg steps differ while mouth/bottle contact remains unchanged.
- Actual selected-item, all-four-direction drinking, walking-drink, village HUD and boss HUD cameras were visually reviewed. The context chip and boss health bar have a clear gap; a continuous boss-bar pixel assertion verifies it.
- Exact built-in imagegen prompts and final saved source paths are documented in EQUIPMENT_ART.md; source/prompt hashes are in art/provenance/equipment-v1.json. New review images are build/preview-equipped-items.png, preview-drinking-motion.png, preview-walking-drink.png, preview-equipment-hotbar.png and preview-equipment-boss-hud.png.

## Guard walking, inventory and waterfall verification

- All four shield facings keep their original braced torso and equipment while alternating cached leg frames during real movement. Stationary guards stay planted, guard speed remains limited, and all existing jump/guard priority checks pass.
- Real I-key events, held-key repeat suppression, scaled bag clicks, item preview selection, slot assignment, clearing/stowing and equip commands are checked. Opening the inventory freezes the game and world clock. Esc closes it without entering the pause screen.
- Drinking from the bag closes the modal, lowers released guard, starts the actual drinking action, and consumes exactly one useful potion at the sip. Rebinding a stowed item preserves bag quantities and never creates a duplicate reference.
- The source GIF remains byte-identical to the user's Downloads file. All ten prepared waterfall outputs (nine PNGs and their manifest) regenerate byte-identically. The exact built-in imagegen prompts, source images and source hashes are retained.
- Pond water, cliff solids, tree clearance and hydraulic connectivity agree with the registered art. The receiving pond has no jetty; its dry viewing shore is reachable. Existing village culverts, bridges, gates and boss approaches remain valid.
- Actual day/night waterfall cameras keep Agyu visible above the hotbar. Repeated draws preserve simulation state, and cached water phases visibly animate.

Visual evidence: build/preview-guard-walking.png, build/preview-inventory.png, build/preview-waterfall-native.png, build/preview-mountain-waterfall.png, build/preview-waterfall-camera.png and build/preview-waterfall-night.png.

## Equipment-size and river-seam revision

Agyu now uses one body atlas and one foot anchor for every normal held item, empty hands, guard gait, jumping and dash afterimages. The actual head geometry matches across all equipment in idle, walk, run and guard. Exact jump composition still verifies that the full character/equipment rises while the shadow remains grounded. The upright spear clears the face and stays in Agyu's right hand in the front and rear views.

The river and pond use the same native texture, palette and rock/moss bank samples. Eight cached pattern variants per phase, rounded shores and a tapered outlet replace the mismatched green tiles and random wide pools. Transparent scenery corners have a grass backing, hidden spill water is cleared, and gray mountain underlay plus redundant line-current effects are removed. Hydraulic connectivity and protective culverts remain valid.

The I inventory, sip-time consumption, shield walking, original combat, forest wildlife, bosses and three-minute day/night cycle remain covered. The current visual evidence includes build/preview-equipped-items.png, build/preview-guard-walking.png, build/preview-inventory.png, build/preview-river-connection.png and the day/night waterfall cameras.

SpearCarryTests verifies upright carry, directional guard aim, the right-hand grip, and the original foot colors across both profile directions. It checks that the first footfall matches the standing boot width and that every profile footfall has a grounded sole. The four-direction preview includes standing, walking, blocking and shield-only views at build/preview-upright-spear.png.
