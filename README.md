# Agyu: Guardians of the Lost Grove

A single-player Java Swing action adventure for a combined Data Structures and Algorithms / Object Oriented Programming project.

Agyu explores the forest beyond a protected village, restores three guarded shrines, collects their seals, awakens the guardian, defeats the three main villains, and returns to the home beacon. This is an original fictional story featuring your character.

## Run

Install a **JDK 17 or newer** and put its bin directory on PATH. Double-click **run.bat**, or run it from a terminal. There are no third-party Java dependencies and no runtime network requirement. Custom artwork is included in res/village, so a fresh clone runs offline. Optional creator house overrides stay local; see docs/CREDITS.md.

From PowerShell in the project folder:

```powershell
.\run.bat                 # Compile gameplay and start the game
.\test.bat                # Compile gameplay + regression tests and run them
.\tools.bat RpgVillagePreviews  # Compile developer tools and export scene previews
```

`build.ps1` gathers Java files recursively. Gameplay, tests and tools compile into separate fresh class folders under `build/`.

## Controls

| Key | Action |
| --- | --- |
| Enter | Start; resume when paused |
| WASD / arrows | Move |
| Shift | Sprint while energy lasts |
| Q / Ctrl | Lightning dash (25 energy; movement direction or current facing) |
| Left mouse / J | Spear combo (tap again to chain) |
| Space | Jump (one hop per press) |
| Hold right mouse | Raise shield; block frontal hits; move slowly |
| E | Open/close a nearby gate / talk to villagers / restore a shrine or interact with a beacon |
| H | Consume an herb to restore 2 missing health |
| M | Full village and forest map; green arrow marks Agyu; close again to resume |
| Tab | Objective journal; close again to resume |
| N | Toggle A* trail to the next objective or closed gate |
| Esc / P | Pause / resume |
| F1 | Show algorithm and timing diagnostics |
| R | Start a fresh journey after defeat or victory |

Tap left mouse or J repeatedly to chain six spear moves, ending in a gold Sunbreaker finisher. The next press is buffered; a short pause restarts the chain. See **docs/COMBOS.md** for the moves and previews. Face toward the attacker while holding right mouse. Shielding cancels attacks and prevents running; attacks from the rear still hurt. Keyboard movement determines facing. Space makes a short moving hop. Attacks are ignored in midair; holding right mouse during a jump raises the shield on landing. Jumping preserves terrain collision and does not grant invulnerability. Clicking the game gives it keyboard focus.

Health is shown as a continuous red bar. The cyan energy bar holds 100 points: sprinting uses 25 per second, and each lightning dash costs 25. Energy recovers at 22 per second after 0.8 seconds without spending it. Empty energy forces walking until 20 energy returns. Sprinting in place does not spend energy. Dashes travel up to 115 art pixels over 0.16 seconds, have a 0.55-second cooldown, and briefly evade damage during the burst. Terrain stops the dash. Dash cancels a spear combo, cannot begin in midair or while shielding, and takes priority over simultaneous attack/jump inputs. New attacks/jumps during a dash are ignored; held shield raises afterward.

Focus loss pauses the game and clears held keys and mouse buttons. The map and journal also pause combat.

## Village map

The world has 20 homes across eight style slots, using custom Bukidnon-inspired raised homes and optional creator house overrides. This local project includes four free premade house exteriors; fresh clones use full-size custom alternatives in those slots. Twelve friendly villagers work and patrol near the market, loom, forge and river. Detailed foliage, irregular paths and animated water bring the setting to life. The top-right minimap uses a green arrow for Agyu and rotates it with his facing. M opens the labelled full map. Three continuous avenues and connected district streets link every house entrance and village court. Short dirt approaches preserve the paved junctions, and the three planned avenues carry the village river crossings. Eight grouped farms have raised planting beds, three crop styles, a walking aisle and open bamboo gates; crops no longer cover streets. A wooden palisade protects the village, with north, south, west and east gates linked to the roads. Press E nearby to open or close one; a gate stays open while an actor occupies its threshold. Tree roots now use solid bark-aligned footprints, and leaves fade when they cover Agyu. Houses have solid exteriors; they cannot be entered. Press E near an NPC for a conversation. See **docs/RPG_ART.md** for current artwork, source credits, saved prompts and previews; **docs/VILLAGE.md** retains the architectural references.


The playable world is now 192 x 160 tiles, four times the previous forest map area. A dense forest surrounds the original village on all four sides. Three-cell-wide woodland trails connect the gates, four camp clearings, three shrine encounters and the guardian clearing. All 18 ordinary enemies start outdoors: ten shrine guards and eight ambient enemies. Defeated shrine guards stay defeated; ambient camp slots can replenish after 25 active seconds while Agyu is in the forest, at least 380 art pixels away and with a clear spawn point. Closed gates block pursuit. The attack stage/name/progress card has been removed; spear combos and effects still work. Tank occupies Clearing 1 in the west, Mage occupies Clearing 2 in the east, and Fighter occupies Clearing 3 in the south. Winding trails and timber crossings connect them to the gate routes. M labels the bosses and marks defeated encounters green. Their health bars appear only during nearby fights.

A large mountain rises on the far-right edge. Its animated waterfall feeds a basin, a winding river through the village and a western outlet. Water passes below the palisade through solid culverts. An accessible viewing deck approaches the pool, and the camera gently lifts nearby to show the falls. Mountain art, water phases, mist and rock textures are cached; the cliff collision follows the opaque mountain silhouette.

## Day and night

Each phase lasts five minutes of active gameplay: Day 1, Night 1, Day 2, Night 2, and so on. The numbered phase and countdown appear below the health/energy bars. Dusk and dawn fade over the final twelve seconds of each phase; moonlight, village lamps and the waterfall keep the scene readable.

During every night, enemies outside the village gain **25% faster pursuit and +1 damage per hit**, including the three villains and awakened guardian. The bonus ends at dawn and does not stack across nights. Health, maximum health, attack warnings, projectile/charge speeds, defeat rewards and boss progress keep their normal values. Enemies inside the village do not receive the outdoor bonus. New outdoor spawns inherit the current phase immediately.

The clock pauses on the title, map, journal, pause, focus loss, hit-stop and ending screens. A new journey starts at Day 1. See [DAY_NIGHT.md](docs/DAY_NIGHT.md) for the timing and code guide.

## How to finish

1. Press E near a gate to enter the forest. Follow its trails or use M to locate GROVE, LAKE, and RIDGE.
2. Defeat each shrine's guards, stand beside its beacon, and press E.
3. Each shrine gives one seal and one herb. Every second ordinary enemy defeat also gives an herb.
4. Three seals awaken the guardian at the southeast beacon. Step outside its warning ring before its pulse.
5. Defeat Tank, Mage and Fighter in the three remote arenas. Each gives two herbs once. These encounters can be challenged at any time.
6. Once the guardian and all three villains are defeated, return to HOME and press E to win.

Boss animations use your supplied sheets, with distinct combat patterns and visible warnings. See [the boss guide](docs/BOSSES.md) and [sprite import notes](docs/BOSS_SPRITES.md).

Restoring a shrine twice does not duplicate its reward. Herbs are consumed only when health is missing. A new journey resets the entire session.

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

See **docs/SPRITES.md** for the exact row mapping and reproducible Java importer. All sprite preparation uses JDK APIs, so no Python imaging packages are required. The guardian still uses enlarged slime art.

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
