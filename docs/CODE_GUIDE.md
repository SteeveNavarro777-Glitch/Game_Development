# Code and course guide

Start in `src/dsa/` for the data structures, then follow their calls in `src/app/` and `src/entities/`. The labels identify real uses in the running game. Java Collections supply the queue, stack, map, set and priority queue; CircularList is the custom node-based implementation.

## Read the labels

- `FILE PURPOSE` at the top explains what the file owns.
- `SECTION: DSA ...` and `SECTION: OOP ...` mark the relevant fields or algorithm block.
- `BLOCK: ...` before a method explains that method's job.
- [FILE_INDEX.md](FILE_INDEX.md) lists every Java file; [FUNCTION_INDEX.md](FUNCTION_INDEX.md) lists the labeled methods with source links.

## Data structures and algorithms

| Topic | Where to read | What it does in this game |
| --- | --- | --- |
| Arrays / 2D arrays | [World](../src/world/World.java), [Assets](../src/rendering/Assets.java), [CombatBody](../src/combat/CombatBody.java) | Store terrain and cached navigation; index animation by direction, move and phase. |
| Circular singly linked list | [CircularList](../src/dsa/CircularList.java) | Nodes hold `value` and `next`; the tail reconnects to the first node. `add`, `current` and `advance` take O(1). |
| Linked-list gameplay use | [Enemy](../src/entities/Enemy.java), [Npc](../src/entities/Npc.java) | Enemies and friendly villagers repeat their linked patrol waypoints. |
| FIFO queue | [Pathfinder.distances](../src/dsa/Pathfinder.java), [GamePanel.processEvents](../src/app/GamePanel.java) | `addLast/removeFirst` expand BFS cells or process defeat rewards in arrival order. |
| LIFO stack | [Pathfinder.find](../src/dsa/Pathfinder.java) | `reverse.push/pop` turn goal-to-start predecessor links into start-to-goal route order. |
| Dynamic array list | [GamePanel](../src/app/GamePanel.java) | Lists hold active actors and the reused visible draw list. |
| Hash map | [Inventory](../src/dsa/Inventory.java) | Item keys retrieve and update their private quantities. |
| Linked hash map | [QuestLog](../src/dsa/QuestLog.java) | Quest ID lookup retains definition order. This is the library map, separate from the custom patrol list. |
| Hash set | [KeyHandler](../src/app/KeyHandler.java), [QuestLog](../src/dsa/QuestLog.java), [SpearAttack](../src/combat/SpearAttack.java) | Track held keys, completed quests and enemies already hit by a combo stage. |
| Graph | [Pathfinder](../src/dsa/Pathfinder.java), [QuestLog](../src/dsa/QuestLog.java) | Four-neighbor tile routes form an implicit graph; quest prerequisites form a small directed graph. |
| Breadth-first search | [Pathfinder.distances](../src/dsa/Pathfinder.java), [World.streetRoute](../src/world/World.java) | Build a shared enemy distance field and shortest valid road connections. O(V+E). |
| Priority queue / heap | [Pathfinder.find](../src/dsa/Pathfinder.java) | `PriorityQueue<Node>` chooses the smallest cost plus Manhattan estimate. Insert/remove cost O(log V). |
| A* search | [Pathfinder.find](../src/dsa/Pathfinder.java) | Find a shortest trail through the collision-aware grid. |
| Sorting / comparator | [GamePanel.paintComponent](../src/app/GamePanel.java) | Sort visible objects by `Drawable.sortY()` so foreground objects draw last. |
| Spatial grid | [World.index / blocked](../src/world/World.java) | Index obstacle lists by grid bucket and query nearby colliders. The brute-force method remains a correctness reference. |
| Bounded deque | [QuestLog.record](../src/dsa/QuestLog.java) | Keep the newest eight journal messages; it is distinct from the path-reconstruction stack. |

Queue and stack describe different operation rules even though both use ArrayDeque. Library collections are identified honestly; they are not presented as custom implementations. Remaining syllabus topics beyond those supplied by the student still require the course rubric.

## Four OOP pillars

| Pillar | Where to read | Concrete evidence |
| --- | --- | --- |
| Encapsulation | [Inventory](../src/dsa/Inventory.java), [EnergyMeter](../src/combat/EnergyMeter.java), [Player](../src/entities/Player.java), [Enemy](../src/entities/Enemy.java) | Private resource/health fields change through validated methods such as `add`, `consume`, `spend`, `damage` and `useHerb`. |
| Inheritance | [Slime](../src/entities/Slime.java), [Guardian](../src/entities/Guardian.java) | Both extend Enemy and reuse its health, patrol, movement and rendering behavior. |
| Abstraction | [Enemy](../src/entities/Enemy.java), [Drawable](../src/contracts/Drawable.java), [Damageable](../src/contracts/Damageable.java) | Abstract operations and interfaces define the behavior required from concrete objects. |
| Polymorphism | [GamePanel](../src/app/GamePanel.java), [Guardian](../src/entities/Guardian.java) | `List<Enemy>` dispatches overridden guardian behavior; `List<Drawable>` renders players, NPCs, enemies and scenery through one contract. |

Composition is visible in Player's owned Inventory, SpearAttack, ShieldGuard, JumpMotion, EnergyMeter and DashMotion, and in each actor's patrol object. Enums describe state, direction, items and NPC roles. Constructors establish valid initial state.

## Collision and gate examples

[Palisade](../src/world/Palisade.java) stores four gates in an array and uses a 2D lookup for gateway clearance. Its abstract Piece contract is implemented by Wall and Gate, and Gate also implements World's obstacle contract. Gate open state is private; safe interaction and the navigation-version cache keep movement, routing and the map consistent.

[World](../src/world/World.java) indexes the cached bark-aligned root rectangles and gate leaves. [RpgArt](../src/rendering/RpgArt.java) measures each tree style once. Player and enemy movement subdivide coarse motion to avoid skipping thin obstacles. [TreeBoundaryTests](../tests/TreeBoundaryTests.java) verifies the real movement, E input, closure safety and cached rendering.

## Forest and encounter examples

[ForestRegion](../src/world/ForestRegion.java) reserves trail cells in a 2D array before seeded tree placement, then reuses cached ground textures and the normal root index. The world keeps village coordinates separate from forest objectives. [ForestEncounters](../src/entities/ForestEncounters.java) uses fixed parallel arrays for eight ambient enemies and their cooldowns; one-time shrine guards are ordinary polymorphic Enemy entries outside those arrays. Its update runs only during active gameplay. [ForestTests](../tests/ForestTests.java) exercises the real spawn lifecycle, gate routes, HUD removal, pause/restart and expanded map rendering.

## Larger forest, arenas and watercourse

[ForestRegion](../src/world/ForestRegion.java) stores three immutable arena records, paints their clear floors before tree generation, and uses temporary spatial buckets for local root-overlap tests. [MountainRiver](../src/world/MountainRiver.java) links a rasterized river centerline to the old watercourse, retaining solid culverts and bridge cells. [ExpansionTests](../tests/ExpansionTests.java) uses a FIFO BFS over water/bridge/culvert cells to prove hydraulic continuity independently of actor navigation.

[MountainArt](../src/rendering/MountainArt.java) owns the shared mountain and four-element waterfall/mist arrays. MountainRiver implements Drawable, so the normal render list dispatches cached scenery through the existing abstraction. Its art-derived cliff terrain, read-only render checks, clear arena approaches and visible viewing-deck actor are verified by the expansion suite.

## Boss encounters and authored animation

[ArenaBoss](../src/entities/ArenaBoss.java) extends Enemy, with TankBoss, MageBoss and FighterBoss specializing attack behavior through polymorphism. Its combat states and bounded hazard storage separate simulation from rendering. [BossArt](../src/rendering/BossArt.java) indexes cached frame views by kind, animation, facing and phase. [ForestEncounters](../src/entities/ForestEncounters.java) constructs the three arena bosses separately from its eight ambient respawn slots.

Defeats enter the existing FIFO event queue. QuestLog records independent boss objectives in its prerequisite graph, and HOME requires the three villain IDs plus the awakened guardian. The map, journal, navigation hint and health HUD read those real encounter/quest states.

## Day/night simulation and lighting

[DayNightCycle](../src/world/DayNightCycle.java) is a private state machine with enum phase queries, a numbered transition count and an elapsed-time remainder. World owns the clock; the existing active-session update advances it. [Enemy](../src/entities/Enemy.java) shares encapsulated temporary pursuit/damage modifiers through its inheritance hierarchy, including the arena villains and guardian.

[WorldLighting](../src/rendering/WorldLighting.java) composes cached radial textures into one reused viewport mask. The renderer reads time and actor state without advancing them, then draws the HUD/map above the lighting. [DayNightTests](../tests/DayNightTests.java) checks fixed-step boundaries, real combat/spawning, paused clocks, reset and image/cache properties. Details are in [DAY_NIGHT.md](DAY_NIGHT.md).

## Suggested defense walkthrough

1. Open CircularList and trace two `add` calls, then `advance` wrapping to the first node.
2. Open World and explain `[row][column]` terrain arrays and the cached collision-aware grid.
3. Compare the FIFO BFS frontier with the LIFO predecessor stack inside Pathfinder.
4. Follow one defeated enemy into GamePanel's event queue and Inventory/QuestLog updates.
5. Compare Slime and Guardian, then point to the abstract Enemy methods and interface-based draw list.
6. Run `test.bat` and show the collision, routing and progression evidence in TEST_REPORT.md.

Folders group responsibilities while the project retains one shared default Java package. Build scripts collect the source files recursively. Tests and developer import tools are separate compilation targets.
