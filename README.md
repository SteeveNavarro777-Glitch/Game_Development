# Agyu: Guardians of the Lost Grove

A single-player Java Swing action adventure for a combined Data Structures and Algorithms / Object Oriented Programming project.

Agyu restores three guarded shrines, collects their seals, awakens the guardian, and returns to the home beacon. This is an original fictional story featuring your character.

## Run

Install a **JDK 17 or newer** and put its bin directory on PATH. Double-click **run.bat**, or run it from a terminal. There are no third-party Java dependencies and no runtime network requirement. Custom artwork is included in res/rpg-village, so a fresh clone runs offline. Optional creator house overrides stay local; see CREDITS.md.

Manual commands, from this folder:

```powershell
New-Item -ItemType Directory -Force build
javac -encoding UTF-8 -Xlint:all -d build *.java
java -cp "build;." Main
```

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
| E | Talk to nearby villagers / restore a cleared shrine / interact with a beacon |
| H | Consume an herb to restore 2 missing health |
| M | Full village map; green arrow marks Agyu; close again to resume |
| Tab | Objective journal; close again to resume |
| N | Toggle A* trail to the nearest unfinished objective |
| Esc / P | Pause / resume |
| F1 | Show algorithm and timing diagnostics |
| R | Start a fresh journey after defeat or victory |

Tap left mouse or J repeatedly to chain six spear moves, ending in a gold Sunbreaker finisher. The next press is buffered; a short pause restarts the chain. See **COMBOS.md** for the moves and previews. Face toward the attacker while holding right mouse. Shielding cancels attacks and prevents running; attacks from the rear still hurt. Keyboard movement determines facing. Space makes a short moving hop. Attacks are ignored in midair; holding right mouse during a jump raises the shield on landing. Jumping preserves terrain collision and does not grant invulnerability. Clicking the game gives it keyboard focus.

Health is shown as a continuous red bar. The cyan energy bar holds 100 points: sprinting uses 25 per second, and each lightning dash costs 25. Energy recovers at 22 per second after 0.8 seconds without spending it. Empty energy forces walking until 20 energy returns. Sprinting in place does not spend energy. Dashes travel up to 115 art pixels over 0.16 seconds, have a 0.55-second cooldown, and briefly evade damage during the burst. Terrain stops the dash. Dash cancels a spear combo, cannot begin in midair or while shielding, and takes priority over simultaneous attack/jump inputs. New attacks/jumps during a dash are ignored; held shield raises afterward.

Focus loss pauses the game and clears held keys and mouse buttons. The map and journal also pause combat.

## Village map

The world has 20 homes across eight style slots, using custom Bukidnon-inspired raised homes and optional creator house overrides. This local project includes four free premade house exteriors; fresh clones use full-size custom alternatives in those slots. Twelve friendly villagers work and patrol near the market, loom, forge and river. Detailed foliage, irregular paths and animated water bring the setting to life. The top-right minimap uses a green arrow for Agyu and rotates it with his facing. M opens the labelled full map. Houses have solid exteriors; they cannot be entered. Press E near an NPC for a conversation. See **RPG_ART.md** for current artwork, source credits, saved prompts and previews; **VILLAGE.md** retains the architectural references.

## How to finish

1. Follow the roads or use M to locate GROVE, LAKE, and RIDGE.
2. Defeat each shrine's guards, stand beside its beacon, and press E.
3. Each shrine gives one seal and one herb. Every second ordinary enemy defeat also gives an herb.
4. Three seals awaken the guardian at the southeast beacon. Step outside its warning ring before its pulse.
5. Defeat it, return to HOME, and press E to win.

Restoring a shrine twice does not duplicate its reward. Herbs are consumed only when health is missing. A new journey resets the entire session.

## Course evidence

See **PROJECT_PLAN.md** for the scope, topic-to-code matrix, architecture, limitations, and defense outline.

The code includes an actual custom circular singly linked list, arrays, FIFO queues, LIFO stack operations, graph traversal, heap-based A*, sorting, hash maps, and hash sets. Four OOP pillars are demonstrated through the Enemy hierarchy, Damageable/Drawable contracts, controlled inventory/health mutation, and composed gameplay objects.

## Verify

See **TEST_REPORT.md** for the latest verification results. Run **test.bat**. It compiles with warnings enabled and runs deterministic logic, route, input, progression, and off-screen rendering checks. PNG previews are written under build/.

The progression check drives real game update and interaction methods while programmatically positioning Agyu and defeating enemies. It verifies state transitions; it is not a substitute for a human combat/balance playtest.

F1 reports rolling update/render CPU time, paint frequency, BFS/A* builds, tree/house collision checks, and particle count. Paint frequency is not a guarantee of monitor presentation rate.

## Artwork

Agyu's idle, movement, hurt and death art comes from your supplied **res/agyu-sheet.png**. Its 71 transparent frames are in **res/agyu-v2/**. The old res/agyu PNGs remain only as an unused backup.

Idle, side walk/run, slash attack, hurt, and death now use the replacement art. Side animations use all eight frames, and left-facing sprites mirror the supplied right-facing frames. Front/back movement uses the new facing poses with bobbing. Attacks now animate a separate spear in all four directions, with matching swept damage, a visible trail and a short recovery input buffer.

Combat now has dedicated whole-body poses for all six moves: crouched loads, planted steps, hip twists, deep lunges, rising knee lifts, spinning pivots and braced finishes. Each move has four body phases, with the spear and afterimages aligned to the actual fist. See **COMBAT_ART.md** for artwork and body-motion previews.

See **SPRITES.md** for the exact row mapping and reproducible Java importer. All sprite preparation uses JDK APIs, so no Python imaging packages are required. The guardian still uses enlarged slime art.

The shield-holding artwork and repair notes are in **GUARD_ART.md**. Blocking uses whole character poses with connected arms, rather than removing body pixels and placing a shield overlay.

## Project structure

The flat default-package layout keeps compilation simple for the existing classroom project. Main handles application startup; GamePanel owns the session; World owns terrain/collision data; Player and Enemy own actor behavior; Inventory and QuestLog own progression data; Pathfinder contains grid algorithms; Assets/Fx handle shared art and effects.

build/ and generated class files are ignored by Git.
