# Agyu: Guardians of the Lost Grove

A single-player Java Swing action adventure for a combined Data Structures and Algorithms / Object Oriented Programming project.

Agyu protects a village by exploring the outer forest and defeating Tank, Mage and Fighter in their three clearings. Random boar herds, rare night wolves and hostile night-only Sigbin encounters populate the woodland. This is an original fictional story featuring your character.

## Run

Install a **JDK 17 or newer** and put its bin directory on PATH. Double-click **run.bat**, or run it from a terminal. There are no third-party Java dependencies and no runtime network requirement. Custom artwork is included in res/village, so a fresh clone runs offline. Optional creator house overrides stay local; see docs/CREDITS.md.

From PowerShell in the project folder:

```powershell
.\run.bat                 # Compile gameplay and start the game
.\test.bat                # Compile gameplay + regression tests and run them
.\tools.bat RpgVillagePreviews  # Compile developer tools and export scene previews
```

`build.ps1` gathers Java files recursively. Gameplay, tests and tools compile into separate fresh class folders under `build/`.

## Forest wildlife

Each new journey places **12 randomized boar herds of 1?4 members** across reachable outer forest areas. Rare white wolves have a total limit of **2?3 spawns per night** and leave at dawn. The **night-only Sigbin** jumpscares Agyu, then hunts and attacks; spear combos can stagger and defeat it. See [the wildlife rules and sprite guide](docs/WILDLIFE.md).

## Controls

| Key | Action |
| --- | --- |
| Enter | Start; resume when paused |
| WASD / arrows | Move |
| Shift | Sprint while energy lasts |
| Q / Ctrl | Lightning dash (25 energy; movement direction or current facing) |
| Left mouse / J | Use selected item: spear combo, held shield defense or potion |
| 1-9 / mouse wheel / click slot | Select equipment, potion or empty hotbar slot |
| Drag between hotbar slots | Rearrange items without consuming or duplicating stacks |
| F | Drink selected potion |
| Space | Jump (one hop per press) |
| Hold right mouse | Raise shield; block frontal hits; move slowly |
| E | Open/close a nearby gate / talk to villagers |
| H | Consume a health potion to restore 2 missing health |
| M | Full village and forest map; green arrow marks Agyu; close again to resume |
| Tab | Objective journal; close again to resume |
| N | Toggle A* trail to the next objective or closed gate |
| Esc / P | Pause / resume |
| F1 | Show algorithm and timing diagnostics |
| R | Start a fresh journey after defeat or victory |

Select the spear slot, then tap left mouse or J repeatedly to chain six spear moves, ending in a gold Sunbreaker finisher. The next press is buffered; a short pause restarts the chain. See **docs/COMBOS.md** for the moves and previews. Face toward the attacker while holding right mouse. Shielding cancels attacks and prevents running; attacks from the rear still hurt. Keyboard movement determines facing. Space makes a short moving hop. Attacks are ignored in midair; holding right mouse during a jump raises the shield on landing. Jumping preserves terrain collision and does not grant invulnerability. Clicking the game gives it keyboard focus.

Health is green, energy is orange, and blue potion shield appears below energy. All three bars have no numeric text inside. The energy bar holds 100 points: sprinting uses 25 per second, and each lightning dash costs 25. Energy recovers at 22 per second after 0.8 seconds without spending it. Empty energy forces walking until 20 energy returns. Sprinting in place does not spend energy. Dashes travel up to 115 art pixels over 0.16 seconds, have a 0.55-second cooldown, and briefly evade damage during the burst. Terrain stops the dash. Dash cancels a spear combo, cannot begin in midair or while shielding, and takes priority over simultaneous attack/jump inputs. New attacks/jumps during a dash are ignored; held shield raises afterward.

The bottom hotbar has nine slots. Spear, wooden shield, green health potion, orange energy potion and blue shield potion start in slots 1-5; slots 6-9 are empty. Right-click still guards with the wooden shield regardless of selection. Slot clicks select; world clicks use the selected item. Agyu visibly holds the selected item and raises, tilts and lowers potions when drinking. He can walk slowly during the action. Guarding, damage, another selection, jumping or dashing can interrupt it. H selects the green potion in its current slot. Side guards point the spear toward the enemy. See [potions and hotbar](docs/POTIONS.md) for resource and inventory rules.

Focus loss pauses the game and clears held keys and mouse buttons. The map and journal also pause combat.

## Village map

The world has 20 homes across eight style slots, using custom Bukidnon-inspired raised homes and optional creator house overrides. This local project includes four free premade house exteriors; fresh clones use full-size custom alternatives in those slots. Twelve friendly villagers work and patrol near the market, loom, forge and river. Detailed foliage, irregular paths and animated water bring the setting to life. The top-right minimap uses a green arrow for Agyu and rotates it with his facing. M opens the labelled full map. Three continuous avenues and connected district streets link every house entrance and village court. Short dirt approaches preserve the paved junctions, and the three planned avenues carry the village river crossings. Eight grouped farms have raised planting beds, three crop styles, a walking aisle and open bamboo gates; crops no longer cover streets. A wooden palisade protects the village, with north, south, west and east gates linked to the roads. Press E nearby to open or close one; a gate stays open while an actor occupies its threshold. Tree roots now use solid bark-aligned footprints, and leaves fade when they cover Agyu. Houses have solid exteriors; they cannot be entered. Press E near an NPC for a conversation. See **docs/RPG_ART.md** for current artwork, source credits, saved prompts and previews; **docs/VILLAGE.md** retains the architectural references.


The playable world is 192 x 160 tiles, with dense woodland around the 64 x 48 village. Trails, river crossings and free clearings connect the four gates to the three villain arenas and the eastern mountain/waterfall. The old beacons, labels, floor outlines and live map markers are removed, including HOME. The village spawn remains plain open ground. Randomized boar herds use BFS-validated reachable positions; empty slots replenish after 25 active seconds on nearby free ground outside Agyu's view. Herd members stay outside the village even through open gates. M shows the three villain objectives, and N routes to the nearest undefeated villain. Their continuous health bars appear during nearby fights.

A large mountain rises on the far-right edge. Its animated waterfall feeds a basin, a winding river through the village and a western outlet. Water passes below the palisade through solid culverts. An accessible viewing deck approaches the pool, and the camera gently lifts nearby to show the falls. Mountain art, water phases, mist and rock textures are cached; the cliff collision follows the opaque mountain silhouette.

## Day and night

Each phase lasts three minutes of active gameplay: Day 1, Night 1, Day 2, Night 2, and so on. The numbered phase and countdown appear below the three resource bars. Dusk and dawn fade over the final twelve seconds of each phase; moonlight, village lamps and the waterfall keep the scene readable.

During every night, enemies outside the village gain **25% faster pursuit and +1 damage per hit**, including boars, wolves, Sigbin and the three villains. The bonus ends at dawn and does not stack across nights. Health, maximum health, attack warnings, projectile/charge speeds, defeat rewards and boss progress keep their normal values. Enemies inside the village do not receive the outdoor bonus. New outdoor spawns inherit the current phase immediately.

The clock pauses on the title, map, journal, pause, focus loss, hit-stop and ending screens. A new journey starts at Day 1. See [DAY_NIGHT.md](docs/DAY_NIGHT.md) for the timing and code guide.

## How to finish

1. Press E at a village gate to enter the forest. Use M to find Tank, Mage and Fighter, or N for a route to the nearest undefeated villain.
2. Defeat the three villains in any order. Each gives two health potions, one energy potion and one shield potion once; every second ordinary enemy defeat gives one health potion. Every fourth ordinary defeat also gives energy or shield supplies alternately.
3. Defeating the final villain completes the journey automatically once Agyu is grounded and has finished any dash.

Use shield, jump, dash and spear combos during encounters. Potions are consumed only when their resource is missing. Green restores 2 health, orange restores up to 60 energy, and blue provides 3 damage-absorption points. Drinking takes 0.84 active seconds, with recovery and consumption at the 0.44-second sip. Interrupting before the sip spends nothing. The map and journal pause simulation. R starts a fresh journey after defeat or victory, including a new boar distribution.

See [the boss guide](docs/BOSSES.md) and [sprite import notes](docs/BOSS_SPRITES.md).

## Course evidence

Start with [the code and course guide](docs/CODE_GUIDE.md) to find the labeled arrays, linked list, queue, stack and OOP sections. [The project plan](docs/PROJECT_PLAN.md) maps the course topics to actual game behavior.

The code includes an actual custom circular singly linked list, arrays, FIFO queues, LIFO stack operations, graph traversal, heap-based A*, sorting, hash maps, and hash sets. Four OOP pillars are demonstrated through the Enemy hierarchy, Damageable/Drawable contracts, controlled inventory/health mutation, and composed gameplay objects.

## Verify

See **docs/TEST_REPORT.md** for the latest verification results. Run **test.bat**. It compiles with warnings enabled and runs deterministic logic, route, input, progression, and off-screen rendering checks. PNG previews are written under build/.

The progression check drives real game update and interaction methods while programmatically positioning Agyu and defeating enemies. It verifies state transitions; it is not a substitute for a human combat/balance playtest.

F1 reports rolling update/render CPU time, paint frequency, BFS/A* builds, tree/house collision checks, and particle count. Paint frequency is not a guarantee of monitor presentation rate.

## Artwork

Agyu's idle, movement, hurt and death art comes from your supplied **art/source/player/agyu-sheet.png**. The active movement/hurt/death set contains 47 PNGs in **res/player/**, alongside four complete guard PNGs. Dedicated combat bodies are in **res/player/combat/**. Unused old frames and duplicate artwork have been removed.

Idle, side walk/run, slash attack, hurt, and death now use the replacement art. Side animations use all eight frames, and left-facing sprites mirror the supplied right-facing frames. Front/back movement uses the new facing poses with bobbing. Attacks now animate a separate spear in all four directions, with matching swept damage, a visible trail and a short recovery input buffer.

Combat now has dedicated whole-body poses for all six moves: crouched loads, planted steps, hip twists, deep lunges, rising knee lifts, spinning pivots and braced finishes. Each move has four body phases, with the spear and afterimages aligned to the actual fist. See **docs/COMBAT_ART.md** for artwork and body-motion previews.

See **docs/SPRITES.md** for the exact row mapping and reproducible Java importer. All sprite preparation uses JDK APIs, so no Python imaging packages are required. Ordinary slimes have been replaced with the supplied wild boars. Rare wolves and the hostile night Sigbin use their own supplied sprites.

The shield-holding artwork and repair notes are in **docs/GUARD_ART.md**. Blocking uses whole character poses with connected arms, rather than removing body pixels and placing a shield overlay.

## Project structure

| Folder | Function |
| --- | --- |
| `src/app/` | Window startup, game loop, session state and input |
| `src/entities/` | Agyu, enemies, friendly NPCs and their placement |
| `src/combat/` | Spear combos, shield, jump, dash, combat poses and energy |
| `src/dsa/` | Custom linked list, pathfinding, inventory and quests |
| `src/world/` | Village/forest layout, day/night clock, arenas, river, collision, gates and map |
| `src/rendering/` | Sprite caches, terrain, day/night lighting, particles and effects |
| `src/contracts/` | Drawable and Damageable interfaces |
| `tests/` | Regression checks and their rendering evidence |
| `tools/importers/`, `tools/previews/` | Current asset regeneration and preview tools |
| `res/player/`, `res/village/` | Current images and manifests loaded by the game |
| `art/source/`, `art/prompts/`, `art/provenance/` | Current source atlases and artwork records |
| `docs/` | Course guide, code index, credits and verification |
| `build/` | Generated classes, logs and previews; ignored by Git |

Java files share the default package; folders group them by responsibility. Use the supplied recursive build scripts when compiling.

- [Code guide and DSA/OOP examples](docs/CODE_GUIDE.md)
- [Every Java file and its purpose](docs/FILE_INDEX.md)
- [Labeled method/block index](docs/FUNCTION_INDEX.md)
- [Current assets and regeneration](docs/ASSET_GUIDE.md)
- [Cleanup record](docs/CLEANUP_REPORT.md)
- [Verification report](docs/TEST_REPORT.md)
