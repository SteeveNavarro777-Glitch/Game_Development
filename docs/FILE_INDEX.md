# Java file responsibilities

86 Java files: 56 gameplay, 21 regression suites and 9 developer tools. 1072 method/constructor blocks have purpose labels.

| File | Responsibility |
| --- | --- |
| [src/app/GamePanel.java](../src/app/GamePanel.java) | SESSION - Fixed-step loop, session state, event queue, navigation and HUD. |
| [src/app/KeyHandler.java](../src/app/KeyHandler.java) | INPUT - Synchronized keyboard/mouse snapshots and deferred hotbar selection. |
| [src/app/Main.java](../src/app/Main.java) | APPLICATION - Launch the Swing window and game lifecycle. |
| [src/combat/CombatBody.java](../src/combat/CombatBody.java) | ANIMATION ARRAYS - Cached body poses indexed by direction, stage and phase. |
| [src/combat/DashMotion.java](../src/combat/DashMotion.java) | MOVEMENT - Normalized dash burst, duration and cooldown. |
| [src/combat/DrinkMotion.java](../src/combat/DrinkMotion.java) | ITEM ACTION - Interruptible raise, sip and lower animation with one committed potion use. |
| [src/combat/EnergyMeter.java](../src/combat/EnergyMeter.java) | OOP ENCAPSULATION - Validated energy spending, recovery and exhaustion. |
| [src/combat/JumpMotion.java](../src/combat/JumpMotion.java) | MOVEMENT - Jump height, landing and interpolated presentation. |
| [src/combat/ShieldGuard.java](../src/combat/ShieldGuard.java) | COMBAT - Directional shield cone and block feedback. |
| [src/combat/ShieldMeter.java](../src/combat/ShieldMeter.java) | OOP ENCAPSULATION - Blue potion shield capacity and damage absorption. |
| [src/combat/SpearAttack.java](../src/combat/SpearAttack.java) | COMBAT - Six-move configuration array, shared spear pose and one-hit hash set. |
| [src/contracts/Damageable.java](../src/contracts/Damageable.java) | OOP INTERFACE - Shared health, alive and controlled damage contract. |
| [src/contracts/Drawable.java](../src/contracts/Drawable.java) | OOP INTERFACE - Shared depth-sort and render contract. |
| [src/dsa/CircularList.java](../src/dsa/CircularList.java) | DSA LINKED LIST - Custom circular singly linked nodes for patrol waypoints. |
| [src/dsa/Hotbar.java](../src/dsa/Hotbar.java) | DSA ARRAY / OOP ENCAPSULATION - Nine selectable, swappable references to owned equipment and potions. |
| [src/dsa/Inventory.java](../src/dsa/Inventory.java) | DSA HASH MAP / OOP ENCAPSULATION - Validated equipment and potion quantities. |
| [src/dsa/Pathfinder.java](../src/dsa/Pathfinder.java) | DSA GRAPH SEARCH - A*, priority queue, predecessor stack and BFS queue. |
| [src/dsa/QuestLog.java](../src/dsa/QuestLog.java) | DSA GRAPH / MAP / SET - Prerequisites, completion membership and bounded journal deque. |
| [src/entities/ArenaBoss.java](../src/entities/ArenaBoss.java) | ARENA BOSS ABSTRACTION - Leashed encounters, explicit attack phases, bounded hazards and cached authored animation. |
| [src/entities/Enemy.java](../src/entities/Enemy.java) | OOP ABSTRACTION - Shared enemy lifecycle, linked-list patrol and damage contract. |
| [src/entities/FighterBoss.java](../src/entities/FighterBoss.java) | FIGHTER VILLAIN - Quick two-cut sword combinations and a locked-direction flaming dash in Clearing 3. |
| [src/entities/ForestBeast.java](../src/entities/ForestBeast.java) | FOREST BEAST ABSTRACTION - Shared wildlife patrol, pursuit, committed attack phases and cached rendering. |
| [src/entities/ForestEncounters.java](../src/entities/ForestEncounters.java) | RANDOM FOREST HERDS - Distribute small boar herds across reachable woodland and replenish bounded outdoor slots. |
| [src/entities/MageBoss.java](../src/entities/MageBoss.java) | MAGE VILLAIN - Ranged violet projectile fans and delayed, visible rune bursts in Clearing 2. |
| [src/entities/NightWildlife.java](../src/entities/NightWildlife.java) | FOREST WILDLIFE DIRECTOR - Enforce nightly wolf quotas and occasional movement-driven Sigbin ambushes. |
| [src/entities/Npc.java](../src/entities/Npc.java) | NPC - Friendly worker/patrol state, circular route and conversation. |
| [src/entities/NpcPopulation.java](../src/entities/NpcPopulation.java) | NPC PLACEMENT - Authored occupations and reachable patrol stations. |
| [src/entities/Player.java](../src/entities/Player.java) | PLAYER - Selected equipment, sip-time resources and composed movement/combat state. |
| [src/entities/Sigbin.java](../src/entities/Sigbin.java) | SIGBIN AMBUSH - A night-only forest ambush followed by a persistent, fightable enemy using the supplied creature poses. |
| [src/entities/TankBoss.java](../src/entities/TankBoss.java) | TANK VILLAIN - Slow heavy cleaves and a telegraphed, jumpable ground slam in Clearing 1. |
| [src/entities/WildBoar.java](../src/entities/WildBoar.java) | WILD BOAR - One member of a randomized forest herd using the supplied boar poses. |
| [src/entities/Wolf.java](../src/entities/Wolf.java) | RARE NIGHT WOLF - Faster forest pursuit, authored leap attacks and a harmless departure at dawn. |
| [src/rendering/Assets.java](../src/rendering/Assets.java) | ASSET ARRAYS - Current Agyu animation arrays and shared character asset startup. |
| [src/rendering/BossArt.java](../src/rendering/BossArt.java) | BOSS ANIMATION CACHE - Shared authored Tank, Mage and Fighter frames indexed by state, facing and phase. |
| [src/rendering/ComboEffects.java](../src/rendering/ComboEffects.java) | EFFECTS - Bounded combo ghosts, arcs, impacts and rings. |
| [src/rendering/DashEffects.java](../src/rendering/DashEffects.java) | EFFECTS - Bounded lightning trails and cached body afterimages. |
| [src/rendering/ForestAudio.java](../src/rendering/ForestAudio.java) | FOREST AUDIO - A short synthesized scare sting with bounded asynchronous playback and no external sound dependency. |
| [src/rendering/Fx.java](../src/rendering/Fx.java) | EFFECTS - Bounded particles, hit-stop and screen shake. |
| [src/rendering/GameScale.java](../src/rendering/GameScale.java) | DISPLAY - Shared player/house scale and anchored drawing transforms. |
| [src/rendering/HeldItemArt.java](../src/rendering/HeldItemArt.java) | EQUIPMENT ANIMATION - Authored body/wrist poses and cached held potion, shield and empty-hand composites. |
| [src/rendering/HotbarHud.java](../src/rendering/HotbarHud.java) | HOTBAR UI - Centered nine-slot equipment dock, consistent icon bounds, quantity badges and drink progress. |
| [src/rendering/MountainArt.java](../src/rendering/MountainArt.java) | MOUNTAIN ART CACHE - Detailed pixel mountain, four falling-water phases and soft pool mist. |
| [src/rendering/PotionArt.java](../src/rendering/PotionArt.java) | CACHED ITEM ART - Supplied potion icons/effect arrays and icons cropped from current Agyu equipment. |
| [src/rendering/PotionEffects.java](../src/rendering/PotionEffects.java) | PLAYER EFFECTS - Bounded potion animation timers and a blue protection aura. |
| [src/rendering/RpgArt.java](../src/rendering/RpgArt.java) | ASSET CACHE - Validate and cache the current village and NPC sprites. |
| [src/rendering/RpgTerrain.java](../src/rendering/RpgTerrain.java) | TERRAIN CACHE - Prebaked path/bank transitions selected by cached edge masks. |
| [src/rendering/VillageArt.java](../src/rendering/VillageArt.java) | VILLAGE ART - Current bridge images and shared house cache slots. |
| [src/rendering/WildlifeArt.java](../src/rendering/WildlifeArt.java) | WILDLIFE ANIMATION ARRAYS - Cache supplied boar, wolf and Sigbin poses by species, action, direction and phase. |
| [src/rendering/WorldLighting.java](../src/rendering/WorldLighting.java) | WORLD LIGHTING - Reused moonlight mask, soft actor visibility and warm village lights. |
| [src/world/DayNightCycle.java](../src/world/DayNightCycle.java) | WORLD CLOCK - Three-minute day/night phases, numbered cycles and continuous dusk/dawn lighting. |
| [src/world/FarmPlot.java](../src/world/FarmPlot.java) | FARM ART - Deterministic crop beds and an image cached per plot. |
| [src/world/ForestRegion.java](../src/world/ForestRegion.java) | FOREST WORLD - Seeded dense woodland, clear gate trails and combat clearings outside the village. |
| [src/world/MountainRiver.java](../src/world/MountainRiver.java) | MOUNTAIN RIVER - Solid eastern highlands, animated waterfall and a continuous river through protected village culverts. |
| [src/world/Palisade.java](../src/world/Palisade.java) | VILLAGE DEFENSE - Cached wooden palisade pieces and four safe, operable gateways. |
| [src/world/VillageMap.java](../src/world/VillageMap.java) | MAP UI - Cached terrain map and current player/quest markers. |
| [src/world/World.java](../src/world/World.java) | WORLD / DSA ARRAYS - Terrain grids, BFS roads, crop lots and spatial collision index. |
| [tests/ArenaBossTests.java](../tests/ArenaBossTests.java) | REGRESSION TEST - Authored boss sprites, three assigned encounters, telegraphed combat, defenses and one-time progression. |
| [tests/BoarTests.java](../tests/BoarTests.java) | BOAR REGRESSION - Authored frames, forest habitat, telegraphed charges, shield/collision and complete death lifecycle. |
| [tests/CombatBodyTests.java](../tests/CombatBodyTests.java) | REGRESSION TEST - Authored body pixels, grip anchors, phases, mirroring, trajectories and afterimages. |
| [tests/CombatTests.java](../tests/CombatTests.java) | REGRESSION TEST - Four-direction input, spear damage, current combat bodies and rendered weapon motion. |
| [tests/ComboTests.java](../tests/ComboTests.java) | REGRESSION TEST - Six-stage input chaining, hit windows, move trajectories and bounded effects. |
| [tests/DayNightTests.java](../tests/DayNightTests.java) | REGRESSION TEST - Three-minute timing, reversible outdoor night strength, paused clocks and readable cached lighting. |
| [tests/EnergyDashTests.java](../tests/EnergyDashTests.java) | REGRESSION TEST - Energy recovery/exhaustion, actual dash inputs, collision, priority and HUD fills. |
| [tests/EquipmentTests.java](../tests/EquipmentTests.java) | REGRESSION TEST - Visible selected items, forward guard spear, drink timing/interruption, cached walking and actual-game previews. |
| [tests/ExpansionTests.java](../tests/ExpansionTests.java) | REGRESSION TEST - Remote boss arenas, a continuous mountain river, solid culverts and animated waterfall scenery. |
| [tests/ForestRevisionTests.java](../tests/ForestRevisionTests.java) | FOREST REVISION REGRESSION - Seeded herd distribution, removed map markers and night-only hostile Sigbin behavior. |
| [tests/ForestTests.java](../tests/ForestTests.java) | REGRESSION TEST - Dense exterior forest, safe outdoor spawn slots, route access and hidden combo HUD. |
| [tests/GameTests.java](../tests/GameTests.java) | REGRESSION TEST - Full suite entry point; collision equivalence, routing, resources and complete journey transitions. |
| [tests/GuardVisualTests.java](../tests/GuardVisualTests.java) | REGRESSION TEST - Complete guard sprites, source pixels, connected body silhouettes and drawing. |
| [tests/JumpTests.java](../tests/JumpTests.java) | REGRESSION TEST - Space input, jump interpolation, landing, collision and action priority. |
| [tests/MouseGuardTests.java](../tests/MouseGuardTests.java) | REGRESSION TEST - Mouse press edges, frontal shield cones, cancellation, contact repulsion and focus. |
| [tests/NpcTests.java](../tests/NpcTests.java) | REGRESSION TEST - NPC sprite caches, occupations, patrols, conversations, pause/focus and restart. |
| [tests/PotionHotbarTests.java](../tests/PotionHotbarTests.java) | REGRESSION TEST - Potion resource rules, shield absorption, real hotbar pointer/keyboard input and three-bar rendering. |
| [tests/TreeBoundaryTests.java](../tests/TreeBoundaryTests.java) | REGRESSION TEST - Bark-aligned tree collision, swept movement, four safe gates and live rendering/navigation. |
| [tests/VillageLayoutTests.java](../tests/VillageLayoutTests.java) | REGRESSION TEST - Connected road surfaces, protected farm plots, open gates and deterministic scenery. |
| [tests/VillageTests.java](../tests/VillageTests.java) | REGRESSION TEST - House walls/approaches, dash collision, map arrows and compact HUD placement. |
| [tests/WildlifeTests.java](../tests/WildlifeTests.java) | WILDLIFE REGRESSION - Verify rare night wolf budgets, dawn departure and bounded harmless Sigbin scares. |
| [tools/importers/BossSpriteImporter.java](../tools/importers/BossSpriteImporter.java) | BOSS IMPORT TOOL - Pack the three supplied directional villain sheets into reproducible cached atlases. |
| [tools/importers/CombatBodyImporter.java](../tools/importers/CombatBodyImporter.java) | IMPORT TOOL - Pack the 72 current body poses and record their spear-grip anchors. |
| [tools/importers/EquipmentSpriteImporter.java](../tools/importers/EquipmentSpriteImporter.java) | IMPORT TOOL - Pack authored empty-handed locomotion/drinking bodies with boot and wrist anchors. |
| [tools/importers/GuardSpriteImporter.java](../tools/importers/GuardSpriteImporter.java) | IMPORT TOOL - Pack four complete shield-holding character poses with a shared foot anchor. |
| [tools/importers/PotionSpriteImporter.java](../tools/importers/PotionSpriteImporter.java) | IMPORT TOOL - Preserve supplied potion RGBA art in small icons and an indexed effect atlas with hashes. |
| [tools/importers/RpgAssetImporter.java](../tools/importers/RpgAssetImporter.java) | IMPORT TOOL - Pack current village atlases and keep optional creator house outputs local. |
| [tools/importers/SpriteImporter.java](../tools/importers/SpriteImporter.java) | IMPORT TOOL - Pack the active movement, hurt and death frames from the supplied Agyu atlas. |
| [tools/importers/WildlifeSpriteImporter.java](../tools/importers/WildlifeSpriteImporter.java) | WILDLIFE IMPORT TOOL - Pack all supplied boar, wolf and Sigbin frames into three reproducible transparent atlases. |
| [tools/previews/RpgVillagePreviews.java](../tools/previews/RpgVillagePreviews.java) | PREVIEW TOOL - Export actual-camera work sites, NPC conversations, river animation and village overview. |
