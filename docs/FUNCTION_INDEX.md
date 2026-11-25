# Labeled function and block index

Source links identify each labeled method or constructor. Inline SECTION labels mark fields and algorithm stages; nested helpers are located in the named file.

## src/app/GamePanel.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [GamePanel.GamePanel](../src/app/GamePanel.java#L60) | FUNCTION | Initialize GamePanel state. |
| [GamePanel.mousePressed](../src/app/GamePanel.java#L66) | UI INPUT ROUTING | Select hotbar boxes without leaking UI clicks into combat. |
| [GamePanel.mouseReleased](../src/app/GamePanel.java#L78) | HOTBAR DRAG | Swap dropped slots through deferred input, then release held combat buttons. |
| [GamePanel.focusLost](../src/app/GamePanel.java#L89) | FUNCTION | Handle focus lost for GamePanel. |
| [GamePanel.pointer](../src/app/GamePanel.java#L97) | POINTER COORDINATES | Scale inventory clicks with the same viewport transform used for rendering. |
| [GamePanel.pointerSlot](../src/app/GamePanel.java#L99) | SCALED UI HIT TEST | Convert native window coordinates into the shared art-resolution hotbar layout. |
| [GamePanel.reset](../src/app/GamePanel.java#L104) | SESSION: RESTART | Create a fresh world, actors, inventory, quests and timing state. |
| [GamePanel.start](../src/app/GamePanel.java#L119) | FUNCTION | Handle start for GamePanel. |
| [GamePanel.stop](../src/app/GamePanel.java#L124) | FUNCTION | Handle stop for GamePanel. |
| [GamePanel.run](../src/app/GamePanel.java#L129) | TIMING: FIXED STEP | Advance simulation at 60 Hz and interpolate rendering. |
| [GamePanel.targetX](../src/app/GamePanel.java#L150) | FUNCTION | Handle target x for GamePanel. |
| [GamePanel.targetY](../src/app/GamePanel.java#L152) | FUNCTION | Handle target y for GamePanel. |
| [GamePanel.freezeInterpolation](../src/app/GamePanel.java#L154) | FUNCTION | Handle freeze interpolation for GamePanel. |
| [GamePanel.update](../src/app/GamePanel.java#L159) | SIMULATION / DSA EVENT QUEUE | Apply input, update actors, enqueue defeats, then process rewards safely. |
| [GamePanel.closeInventory](../src/app/GamePanel.java#L215) | MODAL INPUT | Release mouse actions and discard queued UI clicks without retriggering held keys. |
| [GamePanel.inventoryAction](../src/app/GamePanel.java#L217) | INVENTORY TRANSACTION | Apply references on the simulation thread and route drinking through the normal animated use action. |
| [GamePanel.updateInventory](../src/app/GamePanel.java#L233) | PAUSED UI / FIFO | Process bag selection and bounded pointer commands while gameplay clocks remain frozen. |
| [GamePanel.updateNavigation](../src/app/GamePanel.java#L244) | DSA: BFS / A* CACHES | Refresh enemy flow distances and the optional player trail when needed. |
| [GamePanel.processEvents](../src/app/GamePanel.java#L276) | DSA: FIFO QUEUE | Remove defeat events in insertion order after actor updates, then award progression. |
| [GamePanel.interact](../src/app/GamePanel.java#L294) | GAMEPLAY INTERACTION | Open a nearby gate or talk to the nearest available villager. |
| [GamePanel.objectiveSite](../src/app/GamePanel.java#L304) | BOSS OBJECTIVE SELECTION | Route the optional trail to the nearest undefeated villain. |
| [GamePanel.paintComponent](../src/app/GamePanel.java#L313) | OOP: POLYMORPHISM / SORTING | Sort Drawable objects by feet depth and dispatch each concrete draw implementation. |
| [GamePanel.visible](../src/app/GamePanel.java#L370) | FUNCTION | Handle visible for GamePanel. |
| [GamePanel.card](../src/app/GamePanel.java#L374) | FUNCTION | Handle card for GamePanel. |
| [GamePanel.text](../src/app/GamePanel.java#L379) | FUNCTION | Handle text for GamePanel. |
| [GamePanel.center](../src/app/GamePanel.java#L381) | FUNCTION | Handle center for GamePanel. |
| [GamePanel.vitalBar](../src/app/GamePanel.java#L385) | FUNCTION | Handle vital bar for GamePanel. |
| [GamePanel.drawHud](../src/app/GamePanel.java#L393) | FUNCTION | Render draw hud using the current graphics state. |
| [GamePanel.drawTimeOfDay](../src/app/GamePanel.java#L410) | WORLD CLOCK HUD | Show a compact numbered phase and next-change countdown below the resource bars. |
| [GamePanel.drawBossHealth](../src/app/GamePanel.java#L417) | BOSS HUD | Show one continuous health bar only during a nearby arena fight. |
| [GamePanel.drawMap](../src/app/GamePanel.java#L430) | FUNCTION | Render draw map using the current graphics state. |
| [GamePanel.drawJournal](../src/app/GamePanel.java#L437) | FUNCTION | Render draw journal using the current graphics state. |
| [GamePanel.drawDiagnostics](../src/app/GamePanel.java#L453) | FUNCTION | Render draw diagnostics using the current graphics state. |
| [GamePanel.drawTitle](../src/app/GamePanel.java#L461) | FUNCTION | Render draw title using the current graphics state. |
| [GamePanel.drawModal](../src/app/GamePanel.java#L477) | FUNCTION | Render draw modal using the current graphics state. |

## src/app/KeyHandler.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [KeyHandler.Input](../src/app/KeyHandler.java#L21) | INPUT COMPATIBILITY | Create movement/combat input without any hotbar commands. |
| [KeyHandler.Input](../src/app/KeyHandler.java#L25) | IMMUTABLE SNAPSHOT | Freeze held controls, action edges and one frame of item selection. |
| [KeyHandler.pressed](../src/app/KeyHandler.java#L31) | INPUT EDGE | Test a one-shot action without consuming it twice. |
| [KeyHandler.combat](../src/app/KeyHandler.java#L33) | EQUIPMENT INPUT | Route the selected slot into spear input or held shield defense. |
| [KeyHandler.keyPressed](../src/app/KeyHandler.java#L40) | DSA: HASH SET / INPUT EDGES | Suppress repeat actions and select slots with 1 through 9. |
| [KeyHandler.keyReleased](../src/app/KeyHandler.java#L63) | HELD INPUT | Release a key from held membership. |
| [KeyHandler.mousePressed](../src/app/KeyHandler.java#L65) | MOUSE INPUT | Keep one primary edge and a held right-click shield input. |
| [KeyHandler.mouseReleased](../src/app/KeyHandler.java#L70) | MOUSE INPUT | Release mouse state independently of the pointer position. |
| [KeyHandler.selectSlot](../src/app/KeyHandler.java#L75) | HOTBAR INPUT | Queue an exact selection and discard older wheel movement. |
| [KeyHandler.swapSlots](../src/app/KeyHandler.java#L77) | HOTBAR INPUT | Queue a completed drag without copying any item counts. |
| [KeyHandler.mouseWheelMoved](../src/app/KeyHandler.java#L79) | WHEEL INPUT | Accumulate bounded wheel movement until the next simulation step. |
| [KeyHandler.clampScroll](../src/app/KeyHandler.java#L81) | INPUT BOUND | Bound wheel accumulation to a safe signed integer range. |
| [KeyHandler.mouseClicked](../src/app/KeyHandler.java#L83) | LISTENER CONTRACT | Leave click handling to fresh press edges. |
| [KeyHandler.mouseEntered](../src/app/KeyHandler.java#L85) | LISTENER CONTRACT | Keep controls unchanged when the cursor enters the game. |
| [KeyHandler.mouseExited](../src/app/KeyHandler.java#L87) | LISTENER CONTRACT | Keep held controls until release or focus loss. |
| [KeyHandler.held](../src/app/KeyHandler.java#L89) | HASH SET MEMBERSHIP | Accept WASD or arrow-key movement aliases. |
| [KeyHandler.poll](../src/app/KeyHandler.java#L91) | INPUT SNAPSHOT | Atomically consume action edges and hotbar commands while preserving held controls. |
| [KeyHandler.defer](../src/app/KeyHandler.java#L98) | DEFERRED INPUT | Retain consumable, combat and selection edges through hit-stop. |
| [KeyHandler.useSelectedItem](../src/app/KeyHandler.java#L104) | INVENTORY USE EDGE | Resume a deliberate inventory-use command through the normal player input path. |
| [KeyHandler.clearOverlayActions](../src/app/KeyHandler.java#L106) | MODAL INPUT | Release mouse/actions while preserving held-key membership to suppress I/Esc autorepeat. |
| [KeyHandler.clear](../src/app/KeyHandler.java#L108) | FOCUS / RESET | Drop all held inputs, drag commands and pending action edges. |

## src/app/Main.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Main.Main](../src/app/Main.java#L12) | FUNCTION | Prevent instances of this static helper class. |
| [Main.main](../src/app/Main.java#L14) | FUNCTION | Entry point for this application. |
| [Main.windowClosed](../src/app/Main.java#L23) | FUNCTION | Handle window closed for Main. |

## src/combat/CombatBody.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [CombatBody.CombatBody](../src/combat/CombatBody.java#L22) | FUNCTION | Prevent instances of this static helper class. |
| [CombatBody.ready](../src/combat/CombatBody.java#L24) | CACHE STATE | Report whether the mandatory combat-body cache has been loaded. |
| [CombatBody.phase](../src/combat/CombatBody.java#L26) | FUNCTION | Handle phase for CombatBody. |
| [CombatBody.at](../src/combat/CombatBody.java#L34) | DSA: ARRAY LOOKUP | Validate indexes and return one cached body pose and its grip anchor. |
| [CombatBody.frame](../src/combat/CombatBody.java#L41) | FUNCTION | Handle frame for CombatBody. |
| [CombatBody.load](../src/combat/CombatBody.java#L45) | DSA: 3D ARRAYS / HASH MAP | Parse the frame manifest and cache poses indexed by direction, stage and phase. |
| [CombatBody.value](../src/combat/CombatBody.java#L94) | FUNCTION | Handle value for CombatBody. |
| [CombatBody.open](../src/combat/CombatBody.java#L100) | FUNCTION | Handle open for CombatBody. |

## src/combat/DashMotion.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [DashMotion.active](../src/combat/DashMotion.java#L9) | FUNCTION | Handle active for DashMotion. |
| [DashMotion.cooldown](../src/combat/DashMotion.java#L11) | FUNCTION | Handle cooldown for DashMotion. |
| [DashMotion.dx](../src/combat/DashMotion.java#L13) | FUNCTION | Handle dx for DashMotion. |
| [DashMotion.dy](../src/combat/DashMotion.java#L15) | FUNCTION | Handle dy for DashMotion. |
| [DashMotion.updateCooldown](../src/combat/DashMotion.java#L17) | FUNCTION | Advance update cooldown state with elapsed time. |
| [DashMotion.ready](../src/combat/DashMotion.java#L19) | FUNCTION | Load or validate ready data. |
| [DashMotion.begin](../src/combat/DashMotion.java#L21) | MOVEMENT: DASH | Start a normalized burst and its cooldown. |
| [DashMotion.advance](../src/combat/DashMotion.java#L27) | FUNCTION | Handle advance for DashMotion. |
| [DashMotion.cancel](../src/combat/DashMotion.java#L33) | FUNCTION | Handle cancel for DashMotion. |

## src/combat/DrinkMotion.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [DrinkMotion.begin](../src/combat/DrinkMotion.java#L11) | ACTION START | Start one validated potion action from the holding pose. |
| [DrinkMotion.active](../src/combat/DrinkMotion.java#L13) | ACTION STATE | Report whether an item animation owns the hands. |
| [DrinkMotion.item](../src/combat/DrinkMotion.java#L15) | ACTION ITEM | Read the bottle being animated independently of current inventory quantity. |
| [DrinkMotion.advance](../src/combat/DrinkMotion.java#L17) | ONE-SHOT COMMIT | Return a sip event once even when a coarse step crosses the whole action. |
| [DrinkMotion.finished](../src/combat/DrinkMotion.java#L19) | ACTION COMPLETION | Report when lowering the bottle has completed. |
| [DrinkMotion.progress](../src/combat/DrinkMotion.java#L21) | UI PROGRESS | Read normalized action progress without advancing time. |
| [DrinkMotion.pose](../src/combat/DrinkMotion.java#L23) | AUTHORED POSE | Select the authored hold, lift, lip, tilt and lower frames. |
| [DrinkMotion.cancel](../src/combat/DrinkMotion.java#L25) | ACTION CANCELLATION | Release the hands without undoing an already committed sip. |

## src/combat/EnergyMeter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [EnergyMeter.value](../src/combat/EnergyMeter.java#L11) | FUNCTION | Handle value for EnergyMeter. |
| [EnergyMeter.exhausted](../src/combat/EnergyMeter.java#L13) | FUNCTION | Handle exhausted for EnergyMeter. |
| [EnergyMeter.canSprint](../src/combat/EnergyMeter.java#L15) | FUNCTION | Handle can sprint for EnergyMeter. |
| [EnergyMeter.spend](../src/combat/EnergyMeter.java#L17) | OOP: ENCAPSULATION | Spend energy only when the requested amount is valid and available. |
| [EnergyMeter.restore](../src/combat/EnergyMeter.java#L26) | RESOURCE RESTORE | Clamp potion recovery and release exhaustion when enough energy returns. |
| [EnergyMeter.sprint](../src/combat/EnergyMeter.java#L32) | RESOURCE: SPRINT | Spend energy for the time actually spent moving. |
| [EnergyMeter.update](../src/combat/EnergyMeter.java#L37) | OOP: ENCAPSULATION | Apply delayed recovery and release exhaustion at the resume threshold. |

## src/combat/JumpMotion.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [JumpMotion.begin](../src/combat/JumpMotion.java#L11) | FUNCTION | Handle begin for JumpMotion. |
| [JumpMotion.savePrev](../src/combat/JumpMotion.java#L18) | FUNCTION | Handle save prev for JumpMotion. |
| [JumpMotion.airborne](../src/combat/JumpMotion.java#L20) | FUNCTION | Handle airborne for JumpMotion. |
| [JumpMotion.height](../src/combat/JumpMotion.java#L22) | FUNCTION | Handle height for JumpMotion. |
| [JumpMotion.renderHeight](../src/combat/JumpMotion.java#L24) | FUNCTION | Handle render height for JumpMotion. |
| [JumpMotion.update](../src/combat/JumpMotion.java#L29) | MOVEMENT: JUMP | Advance the hop timer and settle at landing. |

## src/combat/ShieldGuard.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ShieldGuard.update](../src/combat/ShieldGuard.java#L14) | FUNCTION | Advance update state with elapsed time. |
| [ShieldGuard.load](../src/combat/ShieldGuard.java#L16) | FUNCTION | Load or validate load data. |
| [ShieldGuard.faces](../src/combat/ShieldGuard.java#L28) | FUNCTION | Handle faces for ShieldGuard. |
| [ShieldGuard.block](../src/combat/ShieldGuard.java#L41) | FUNCTION | Handle block for ShieldGuard. |
| [ShieldGuard.drawImpact](../src/combat/ShieldGuard.java#L49) | FUNCTION | Render draw impact using the current graphics state. |

## src/combat/ShieldMeter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ShieldMeter.value](../src/combat/ShieldMeter.java#L9) | RESOURCE READ | Read remaining potion protection. |
| [ShieldMeter.restore](../src/combat/ShieldMeter.java#L11) | RESOURCE RESTORE | Fill protection to its fixed capacity. |
| [ShieldMeter.absorb](../src/combat/ShieldMeter.java#L13) | DAMAGE ABSORPTION | Spend protection first and return only damage that reaches health. |

## src/combat/SpearAttack.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [SpearAttack.icon](../src/combat/SpearAttack.java#L22) | CACHED EQUIPMENT ART | Reuse the prepared spear image for the hotbar icon cache. |
| [SpearAttack.tipX](../src/combat/SpearAttack.java#L41) | FUNCTION | Handle tip x for SpearAttack. |
| [SpearAttack.tipY](../src/combat/SpearAttack.java#L43) | FUNCTION | Handle tip y for SpearAttack. |
| [SpearAttack.begin](../src/combat/SpearAttack.java#L46) | DSA: HASH SET / COMBAT | Select the combo stage and clear targets already hit by the previous stage. |
| [SpearAttack.cancel](../src/combat/SpearAttack.java#L48) | FUNCTION | Handle cancel for SpearAttack. |
| [SpearAttack.stage](../src/combat/SpearAttack.java#L50) | FUNCTION | Handle stage for SpearAttack. |
| [SpearAttack.move](../src/combat/SpearAttack.java#L52) | FUNCTION | Handle move for SpearAttack. |
| [SpearAttack.name](../src/combat/SpearAttack.java#L57) | FUNCTION | Handle name for SpearAttack. |
| [SpearAttack.duration](../src/combat/SpearAttack.java#L59) | FUNCTION | Handle duration for SpearAttack. |
| [SpearAttack.activeStart](../src/combat/SpearAttack.java#L61) | FUNCTION | Handle active start for SpearAttack. |
| [SpearAttack.activeEnd](../src/combat/SpearAttack.java#L63) | FUNCTION | Handle active end for SpearAttack. |
| [SpearAttack.chainAt](../src/combat/SpearAttack.java#L65) | FUNCTION | Handle chain at for SpearAttack. |
| [SpearAttack.color](../src/combat/SpearAttack.java#L67) | FUNCTION | Handle color for SpearAttack. |
| [SpearAttack.duration](../src/combat/SpearAttack.java#L69) | FUNCTION | Handle duration for SpearAttack. |
| [SpearAttack.lungeSpeed](../src/combat/SpearAttack.java#L71) | FUNCTION | Handle lunge speed for SpearAttack. |
| [SpearAttack.blue](../src/combat/SpearAttack.java#L80) | FUNCTION | Handle blue for SpearAttack. |
| [SpearAttack.loadWeapon](../src/combat/SpearAttack.java#L86) | ASSET PREPARATION | Cache the supplied spearhead and matching shaft before gameplay. |
| [SpearAttack.clamp](../src/combat/SpearAttack.java#L120) | FUNCTION | Handle clamp for SpearAttack. |
| [SpearAttack.smooth](../src/combat/SpearAttack.java#L122) | FUNCTION | Handle smooth for SpearAttack. |
| [SpearAttack.forward](../src/combat/SpearAttack.java#L124) | FUNCTION | Handle forward for SpearAttack. |
| [SpearAttack.pulse](../src/combat/SpearAttack.java#L128) | FUNCTION | Handle pulse for SpearAttack. |
| [SpearAttack.bodyDirection](../src/combat/SpearAttack.java#L132) | FUNCTION | Handle body direction for SpearAttack. |
| [SpearAttack.bodyOffsetX](../src/combat/SpearAttack.java#L141) | FUNCTION | Handle body offset x for SpearAttack. |
| [SpearAttack.bodyOffsetY](../src/combat/SpearAttack.java#L146) | FUNCTION | Handle body offset y for SpearAttack. |
| [SpearAttack.pose](../src/combat/SpearAttack.java#L154) | COMBAT: SHARED GEOMETRY | Solve the body hand pivot and blade path used by both damage and rendering. |
| [SpearAttack.update](../src/combat/SpearAttack.java#L206) | COMBAT: SWEPT COLLISION / HASH SET | Sample the actual spear path and damage each enemy once per stage. |
| [SpearAttack.behind](../src/combat/SpearAttack.java#L237) | FUNCTION | Handle behind for SpearAttack. |
| [SpearAttack.behindBody](../src/combat/SpearAttack.java#L242) | FUNCTION | Handle behind body for SpearAttack. |
| [SpearAttack.trail](../src/combat/SpearAttack.java#L244) | FUNCTION | Handle trail for SpearAttack. |
| [SpearAttack.draw](../src/combat/SpearAttack.java#L273) | FUNCTION | Render draw using the current graphics state. |

## src/contracts/Damageable.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Damageable.damage](../src/contracts/Damageable.java#L7) | OOP: ABSTRACTION | Define controlled damage without exposing health storage. |
| [Damageable.health](../src/contracts/Damageable.java#L9) | OOP: ABSTRACTION | Require damageable objects to expose current health. |
| [Damageable.maxHealth](../src/contracts/Damageable.java#L11) | OOP: ABSTRACTION | Require damageable objects to expose maximum health. |
| [Damageable.alive](../src/contracts/Damageable.java#L13) | OOP: DEFAULT METHOD | Share the alive rule through the health contract. |

## src/contracts/Drawable.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Drawable.sortY](../src/contracts/Drawable.java#L9) | OOP: ABSTRACTION | Supply the world depth used by the shared draw-list comparator. |
| [Drawable.draw](../src/contracts/Drawable.java#L11) | OOP: ABSTRACTION | Define the render operation implemented by each visible object. |

## src/dsa/CircularList.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [CircularList.Node](../src/dsa/CircularList.java#L14) | FUNCTION | Initialize Node state. |
| [CircularList.add](../src/dsa/CircularList.java#L21) | DSA: LINKED LIST | Append a node and reconnect the tail to the first node; O(1). |
| [CircularList.current](../src/dsa/CircularList.java#L36) | DSA: LINKED LIST | Read the cursor node without changing traversal; O(1). |
| [CircularList.advance](../src/dsa/CircularList.java#L41) | DSA: LINKED LIST | Follow next, wrapping naturally through the circular link; O(1). |
| [CircularList.size](../src/dsa/CircularList.java#L47) | DSA: LINKED LIST | Return the maintained node count; O(1). |

## src/dsa/Hotbar.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Hotbar.item](../src/dsa/Hotbar.java#L12) | ARRAY ACCESS | Read a validated slot without exposing the backing array. |
| [Hotbar.selectedIndex](../src/dsa/Hotbar.java#L14) | OOP: ENCAPSULATION | Read the currently selected slot number. |
| [Hotbar.selectedItem](../src/dsa/Hotbar.java#L16) | ARRAY ACCESS | Read the selected equipment or consumable reference. |
| [Hotbar.select](../src/dsa/Hotbar.java#L18) | ARRAY SELECTION | Select one of the nine indexed slots. |
| [Hotbar.indexOf](../src/dsa/Hotbar.java#L20) | ARRAY SEARCH | Find an item's current slot after rearrangement for the direct healing shortcut. |
| [Hotbar.assign](../src/dsa/Hotbar.java#L22) | INVENTORY ARRAY ASSIGNMENT | Move an existing reference or bind a bag item, preserving uniqueness and item quantities. |
| [Hotbar.clear](../src/dsa/Hotbar.java#L25) | INVENTORY ARRAY CLEAR | Unbind a slot without consuming anything from the shared inventory. |
| [Hotbar.cycle](../src/dsa/Hotbar.java#L27) | MODULAR INDEXING | Wrap mouse-wheel selection in either direction without overflow. |
| [Hotbar.swap](../src/dsa/Hotbar.java#L29) | ARRAY SWAP | Exchange two references while preserving the selected item and inventory quantities. |
| [Hotbar.check](../src/dsa/Hotbar.java#L34) | VALIDATION | Reject an index outside the fixed hotbar array. |

## src/dsa/Inventory.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Inventory.Item](../src/dsa/Inventory.java#L16) | ITEM DATA | Keep each equipment or potion name beside its use hint. |
| [Inventory.potion](../src/dsa/Inventory.java#L18) | ITEM TYPE | Identify consumable resources without exposing inventory mutation. |
| [Inventory.count](../src/dsa/Inventory.java#L23) | DSA: HASH MAP | Read an item quantity with a zero default. |
| [Inventory.add](../src/dsa/Inventory.java#L25) | OOP: ENCAPSULATION / HASH MAP | Validate a positive amount and prevent integer overflow. |
| [Inventory.consume](../src/dsa/Inventory.java#L30) | OOP: ENCAPSULATION / HASH MAP | Spend one available item and remove empty entries. |

## src/dsa/Pathfinder.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Pathfinder.priority](../src/dsa/Pathfinder.java#L18) | DSA: PRIORITY QUEUE | Return the accumulated route cost plus Manhattan estimate (g + h). |
| [Pathfinder.Pathfinder](../src/dsa/Pathfinder.java#L21) | FUNCTION | Prevent instances of this static helper class. |
| [Pathfinder.find](../src/dsa/Pathfinder.java#L23) | DSA: A* / PRIORITY QUEUE / STACK | Find a shortest route with Manhattan estimates and reconstruct it in travel order. |
| [Pathfinder.distances](../src/dsa/Pathfinder.java#L61) | DSA: BFS / FIFO QUEUE | Build unit-cost shortest distances from one source for the shared enemy flow field. |

## src/dsa/QuestLog.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [QuestLog.Quest](../src/dsa/QuestLog.java#L20) | FUNCTION | Initialize Quest state. |
| [QuestLog.QuestLog](../src/dsa/QuestLog.java#L32) | FUNCTION | Initialize QuestLog state. |
| [QuestLog.ready](../src/dsa/QuestLog.java#L40) | DSA: PREREQUISITE GRAPH | Check that every required quest is complete before unlocking a quest. |
| [QuestLog.finish](../src/dsa/QuestLog.java#L45) | OOP: ENCAPSULATION / HASH SET | Complete a ready quest once and record it in the journal. |
| [QuestLog.done](../src/dsa/QuestLog.java#L52) | DSA: HASH SET | Test completion membership by quest ID. |
| [QuestLog.bossesDefeated](../src/dsa/QuestLog.java#L54) | FUNCTION | Count completed villain objectives. |
| [QuestLog.record](../src/dsa/QuestLog.java#L60) | DSA: BOUNDED DEQUE | Add the newest journal entry first and discard the oldest beyond eight entries. |
| [QuestLog.history](../src/dsa/QuestLog.java#L65) | OOP: ENCAPSULATION | Return a copy of journal entries without exposing its deque. |

## src/entities/ArenaBoss.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ArenaBoss.ArenaBoss](../src/entities/ArenaBoss.java#L36) | BOSS COMPOSITION | Bind one immutable clearing, authored art kind and quest identity. |
| [ArenaBoss.arenaId](../src/entities/ArenaBoss.java#L41) | ENCAPSULATION | Read the assigned clearing number. |
| [ArenaBoss.questId](../src/entities/ArenaBoss.java#L43) | ENCAPSULATION | Read the one-time defeat quest key. |
| [ArenaBoss.bossName](../src/entities/ArenaBoss.java#L45) | ENCAPSULATION | Read the display name used by the boss HUD. |
| [ArenaBoss.engaged](../src/entities/ArenaBoss.java#L47) | ENCAPSULATION | Report a living encounter with Agyu inside its arena. |
| [ArenaBoss.state](../src/entities/ArenaBoss.java#L49) | ENCAPSULATION | Expose the current attack phase without allowing mutation. |
| [ArenaBoss.phaseTime](../src/entities/ArenaBoss.java#L51) | ENCAPSULATION | Read elapsed simulation time in the current phase. |
| [ArenaBoss.attackNumber](../src/entities/ArenaBoss.java#L53) | ENCAPSULATION | Read the deterministic attack-cycle index. |
| [ArenaBoss.hazardCount](../src/entities/ArenaBoss.java#L55) | ENCAPSULATION | Read the bounded live hazard population. |
| [ArenaBoss.facing](../src/entities/ArenaBoss.java#L57) | ENCAPSULATION | Read the authored facing selected for the current move. |
| [ArenaBoss.speed](../src/entities/ArenaBoss.java#L59) | OOP CONTRACT | Supply the concrete boss pursuit speed through Enemy's abstraction. |
| [ArenaBoss.contactDamage](../src/entities/ArenaBoss.java#L61) | OOP CONTRACT | Boss contact alone causes no damage; attack windows own every hit. |
| [ArenaBoss.pursueSpeed](../src/entities/ArenaBoss.java#L63) | OOP ABSTRACTION | Require a concrete pursuit pace. |
| [ArenaBoss.attackRange](../src/entities/ArenaBoss.java#L65) | OOP ABSTRACTION | Require a concrete attack-selection range. |
| [ArenaBoss.accent](../src/entities/ArenaBoss.java#L67) | OOP ABSTRACTION | Require the concrete attack-warning palette. |
| [ArenaBoss.planAttack](../src/entities/ArenaBoss.java#L69) | OOP ABSTRACTION | Choose the next telegraphed attack and timing. |
| [ArenaBoss.strike](../src/entities/ArenaBoss.java#L71) | OOP ABSTRACTION | Resolve concrete attack windows during the active phase. |
| [ArenaBoss.warning](../src/entities/ArenaBoss.java#L73) | OOP ABSTRACTION | Draw the concrete locked attack footprint. |
| [ArenaBoss.preferredDistance](../src/entities/ArenaBoss.java#L75) | OOP DEFAULT | Choose the pursuit stopping distance. |
| [ArenaBoss.retreats](../src/entities/ArenaBoss.java#L77) | OOP DEFAULT | Select whether this archetype backs away while recovering its spacing. |
| [ArenaBoss.knockbackFactor](../src/entities/ArenaBoss.java#L79) | OOP DEFAULT | Keep spear impacts visible without throwing a boss outside its arena. |
| [ArenaBoss.feetBox](../src/entities/ArenaBoss.java#L81) | COLLISION SHAPE | Keep boss feet compatible with the arena's collision-safe routes. |
| [ArenaBoss.hurtBox](../src/entities/ArenaBoss.java#L83) | AUTHORED BODY HITBOX | Match the substantial authored boss torso. |
| [ArenaBoss.removable](../src/entities/ArenaBoss.java#L85) | DEATH LIFECYCLE | Let every authored death frame finish before removal. |
| [ArenaBoss.damage](../src/entities/ArenaBoss.java#L87) | CONTROLLED DAMAGE | Reuse validated Enemy health and one-time reward handling with restrained knockback. |
| [ArenaBoss.update](../src/entities/ArenaBoss.java#L93) | TIMED STATE MACHINE | Subdivide coarse simulation, leash the encounter and preserve explicit warning/strike/recovery phases. |
| [ArenaBoss.tick](../src/entities/ArenaBoss.java#L101) | ENCOUNTER SIMULATION | Advance one bounded gameplay slice and resolve attack windows exactly once. |
| [ArenaBoss.aimAwayX](../src/entities/ArenaBoss.java#L133) | PURSUIT VECTOR | Normalize the current player-to-boss spacing vector on X. |
| [ArenaBoss.aimAwayY](../src/entities/ArenaBoss.java#L135) | PURSUIT VECTOR | Normalize the current player-to-boss spacing vector on Y. |
| [ArenaBoss.configure](../src/entities/ArenaBoss.java#L137) | LOCKED ATTACK PLAN | Store attack durations and the authored body animation before warning begins. |
| [ArenaBoss.window](../src/entities/ArenaBoss.java#L141) | ONCE PER WINDOW | Claim one named active hit window once, even during coarse updates. |
| [ArenaBoss.inside](../src/entities/ArenaBoss.java#L145) | ELLIPTICAL LEASH | Test an arena-relative point with a configurable activation margin. |
| [ArenaBoss.face](../src/entities/ArenaBoss.java#L150) | AUTHORED FACING | Select one of the four supplied directional blocks. |
| [ArenaBoss.moveToward](../src/entities/ArenaBoss.java#L152) | MOVEMENT VECTOR | Move toward a target without overshooting it. |
| [ArenaBoss.move](../src/entities/ArenaBoss.java#L157) | SWEPT BOSS COLLISION | Subdivide travel into at most three-pixel axis steps and retain the arena leash. |
| [ArenaBoss.clearLine](../src/entities/ArenaBoss.java#L166) | SOLID-TERRAIN OCCLUSION | Refuse attacks that would cross a root, river, cliff or wall. |
| [ArenaBoss.coneHit](../src/entities/ArenaBoss.java#L173) | ACTIVE MELEE HIT | Apply one frontal hit using the locked warning direction and real Player defense methods. |
| [ArenaBoss.slamHit](../src/entities/ArenaBoss.java#L178) | GROUND IMPACT | Resolve a dodgeable radial impact that jumping can clear. |
| [ArenaBoss.bolt](../src/entities/ArenaBoss.java#L182) | BOUNDED PROJECTILE SPAWN | Add one finite-lived bolt with a locked launch angle. |
| [ArenaBoss.rune](../src/entities/ArenaBoss.java#L187) | BOUNDED RUNE SPAWN | Add a visible delayed ground warning only on free terrain inside the arena. |
| [ArenaBoss.updateHazards](../src/entities/ArenaBoss.java#L192) | HAZARD LIFECYCLE | Sweep bolts through solid terrain and expire each rune after one delayed impact. |
| [ArenaBoss.coneWarning](../src/entities/ArenaBoss.java#L209) | WARNING GEOMETRY | Draw the locked frontal footprint, making its reach and direction visible before impact. |
| [ArenaBoss.circleWarning](../src/entities/ArenaBoss.java#L215) | WARNING GEOMETRY | Draw the radius and a growing progress ring of a radial attack. |
| [ArenaBoss.body](../src/entities/ArenaBoss.java#L223) | CACHED BODY SELECTION | Select the authored state frame without allocating an image. |
| [ArenaBoss.drawGround](../src/entities/ArenaBoss.java#L234) | GROUND LAYER | Render telegraphs and delayed rune areas before the depth-sorted actors. |
| [ArenaBoss.draw](../src/entities/ArenaBoss.java#L240) | POLYMORPHIC RENDER | Render world telegraphs, bounded hazards and the supplied sprite while restoring parent graphics state. |
| [ArenaBoss.drawHazards](../src/entities/ArenaBoss.java#L253) | HAZARD RENDERING | Draw projectile glows and timed rune warnings without advancing their state. |

## src/entities/Enemy.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Enemy.Enemy](../src/entities/Enemy.java#L22) | FUNCTION | Initialize Enemy state. |
| [Enemy.Enemy](../src/entities/Enemy.java#L26) | DSA CIRCULAR LINKED LIST | Construct shared health and a configurable four-way roaming loop. |
| [Enemy.speed](../src/entities/Enemy.java#L34) | OOP: ABSTRACTION | Require each enemy subtype to supply its movement speed. |
| [Enemy.contactDamage](../src/entities/Enemy.java#L36) | OOP: ABSTRACTION | Require each enemy subtype to supply its contact damage. |
| [Enemy.scale](../src/entities/Enemy.java#L38) | FUNCTION | Handle scale for Enemy. |
| [Enemy.mayPursue](../src/entities/Enemy.java#L40) | OOP EXTENSION | Let concrete wildlife constrain pursuit to its habitat. |
| [Enemy.positionAllowed](../src/entities/Enemy.java#L42) | OOP EXTENSION | Let concrete wildlife add habitat boundaries to solid collision. |
| [Enemy.health](../src/entities/Enemy.java#L44) | OOP: ENCAPSULATION | Expose current health while retaining private storage. |
| [Enemy.maxHealth](../src/entities/Enemy.java#L46) | OOP: ENCAPSULATION | Expose the immutable maximum health. |
| [Enemy.refreshNightPower](../src/entities/Enemy.java#L48) | NIGHT POWER | Apply the current outdoor night state without altering health, maximum health or rewards. |
| [Enemy.nightEmpowered](../src/entities/Enemy.java#L50) | ENCAPSULATION | Read whether this living actor currently receives the outdoor night bonus. |
| [Enemy.pursuitMultiplier](../src/entities/Enemy.java#L52) | PURSUIT MODIFIER | Raise ordinary movement and boss pursuit by twenty-five percent at night. |
| [Enemy.attackDamage](../src/entities/Enemy.java#L54) | OUTGOING DAMAGE | Add one damage to real attacks at night while preserving zero-damage contact. |
| [Enemy.savePrev](../src/entities/Enemy.java#L56) | FUNCTION | Handle save prev for Enemy. |
| [Enemy.removable](../src/entities/Enemy.java#L58) | FUNCTION | Handle removable for Enemy. |
| [Enemy.claimDefeat](../src/entities/Enemy.java#L60) | OOP: ENCAPSULATION | Claim one defeat reward only after health reaches zero. |
| [Enemy.feetBox](../src/entities/Enemy.java#L65) | FUNCTION | Handle feet box for Enemy. |
| [Enemy.hurtBox](../src/entities/Enemy.java#L67) | FUNCTION | Handle hurt box for Enemy. |
| [Enemy.damage](../src/entities/Enemy.java#L69) | OOP: ENCAPSULATION | Clamp private health and apply knockback plus damage effects. |
| [Enemy.update](../src/entities/Enemy.java#L78) | OOP: POLYMORPHISM / LINKED LIST | Use subclass stats while chasing the flow field or cycling patrol nodes. |
| [Enemy.moveThroughWorld](../src/entities/Enemy.java#L131) | FUNCTION | Sweep shared movement through solids and subtype habitat rules. |
| [Enemy.sortY](../src/entities/Enemy.java#L141) | FUNCTION | Handle sort y for Enemy. |

## src/entities/FighterBoss.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [FighterBoss.FighterBoss](../src/entities/FighterBoss.java#L10) | CONCRETE BOSS | Create the golden fighter at its assigned clearing center. |
| [FighterBoss.pursueSpeed](../src/entities/FighterBoss.java#L12) | OOP OVERRIDE | Let the fighter close ground faster than the other villains. |
| [FighterBoss.attackRange](../src/entities/FighterBoss.java#L14) | OOP OVERRIDE | Start a sword sequence or dash within its warned approach distance. |
| [FighterBoss.accent](../src/entities/FighterBoss.java#L16) | OOP OVERRIDE | Match blade and flame warnings to the supplied golden/orange art. |
| [FighterBoss.planAttack](../src/entities/FighterBoss.java#L18) | ATTACK SELECTION | Alternate two-cut sword sequences with a committed flame charge. |
| [FighterBoss.strike](../src/entities/FighterBoss.java#L22) | ACTIVE COMBO | Resolve separate sword windows or sweep a fast dash against real terrain. |
| [FighterBoss.warning](../src/entities/FighterBoss.java#L33) | COMMITTED ATTACK TELEGRAPH | Show the slash footprint or the longer locked flame-charge lane. |

## src/entities/ForestBeast.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ForestBeast.ForestBeast](../src/entities/ForestBeast.java#L22) | SPECIALIZED CONSTRUCTION | Construct shared wildlife health, home position and a two-tile circular patrol. |
| [ForestBeast.kind](../src/entities/ForestBeast.java#L24) | OOP ABSTRACTION | Require each wildlife subtype to select its supplied artwork. |
| [ForestBeast.warningDuration](../src/entities/ForestBeast.java#L26) | ATTACK TIMING | Supply a readable default charge warning. |
| [ForestBeast.strikeDuration](../src/entities/ForestBeast.java#L28) | ATTACK TIMING | Supply the default committed charge window. |
| [ForestBeast.attackSpeed](../src/entities/ForestBeast.java#L30) | ATTACK VECTOR | Keep the locked strike distance independent of night pursuit power. |
| [ForestBeast.recoveryDuration](../src/entities/ForestBeast.java#L32) | ATTACK TIMING | Supply the default punishable recovery window. |
| [ForestBeast.attackRange](../src/entities/ForestBeast.java#L34) | ATTACK RANGE | Supply the default charge-selection distance. |
| [ForestBeast.bodyLift](../src/entities/ForestBeast.java#L36) | BODY MOTION | Raise the wolf's visible leap while leaving collision feet on the ground. |
| [ForestBeast.opacity](../src/entities/ForestBeast.java#L38) | RENDER OPACITY | Fade only the completed corpse; transient wildlife may add a departure fade. |
| [ForestBeast.charges](../src/entities/ForestBeast.java#L40) | OOP EXTENSION | Allow wildlife subtypes to specialize their strike policy. |
| [ForestBeast.mayPursue](../src/entities/ForestBeast.java#L42) | FOREST LEASH | Chase outdoor targets near the home clearing and return to the patrol after disengaging. |
| [ForestBeast.positionAllowed](../src/entities/ForestBeast.java#L47) | HABITAT COLLISION | Keep the complete footprint outside the palisade even through an open gate. |
| [ForestBeast.hurtBox](../src/entities/ForestBeast.java#L51) | AUTHORED HURT BOX | Match the low wildlife torso while keeping the shared narrow navigation footprint. |
| [ForestBeast.state](../src/entities/ForestBeast.java#L53) | ENCAPSULATION | Expose the attack phase for focused behavior checks. |
| [ForestBeast.facing](../src/entities/ForestBeast.java#L55) | ENCAPSULATION | Expose the cached animation direction. |
| [ForestBeast.removable](../src/entities/ForestBeast.java#L57) | COMPLETE DEATH ANIMATION | Keep the corpse through all five supplied death poses before removing it. |
| [ForestBeast.damage](../src/entities/ForestBeast.java#L59) | INTERRUPTIBLE COMBAT | Interrupt a living creature's charge when Agyu lands a spear hit. |
| [ForestBeast.face](../src/entities/ForestBeast.java#L64) | DIRECTION SELECTION | Select front/back/side art from actual motion or a committed attack vector. |
| [ForestBeast.clearLine](../src/entities/ForestBeast.java#L69) | SOLID-TERRAIN OCCLUSION | Refuse charge windups through roots, walls, water or mountain terrain. |
| [ForestBeast.update](../src/entities/ForestBeast.java#L76) | OOP POLYMORPHISM / STATE MACHINE | Share patrol/BFS movement, then lock a visible warning, swept charge and recovery. |
| [ForestBeast.drawGround](../src/entities/ForestBeast.java#L115) | GROUND WARNING | Draw the committed charge lane below actors without an attack instruction panel. |
| [ForestBeast.body](../src/entities/ForestBeast.java#L125) | ANIMATION ARRAY LOOKUP | Select authored poses; vertical charges retain front/back anatomy and deaths never loop. |
| [ForestBeast.draw](../src/entities/ForestBeast.java#L138) | DEPTH-SORTED RENDERING | Draw grounded wildlife at character scale using shared cached frames. |

## src/entities/ForestEncounters.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ForestEncounters.ForestEncounters](../src/entities/ForestEncounters.java#L28) | RANDOM JOURNEY | Give each new journey its own wildlife distribution without changing the terrain. |
| [ForestEncounters.ForestEncounters](../src/entities/ForestEncounters.java#L30) | SEEDED DISTRIBUTION / BFS | Stratify twelve random herd centers over the map and validate their reachable feet positions. |
| [ForestEncounters.capacity](../src/entities/ForestEncounters.java#L58) | ENCAPSULATED CAPACITY | Read the bounded total selected by the current journey's one-to-four-member herd sizes. |
| [ForestEncounters.herds](../src/entities/ForestEncounters.java#L60) | IMMUTABLE HERD RECORDS | Expose immutable center/count records without exposing writable home arrays. |
| [ForestEncounters.free](../src/entities/ForestEncounters.java#L62) | REACHABLE SPAWN FILTER | Reject village land, solids, disconnected pockets and the three boss arenas. |
| [ForestEncounters.nearby](../src/entities/ForestEncounters.java#L72) | RANDOM HERD MEMBERS | Shuffle free tiles within two cells of the same center so a herd stays together without overlapping feet. |
| [ForestEncounters.populate](../src/entities/ForestEncounters.java#L77) | POLYMORPHIC POPULATION | Add the randomized boars and three concrete villains once for this session. |
| [ForestEncounters.update](../src/entities/ForestEncounters.java#L88) | BOUNDED RESPAWN | Replenish empty herd slots after a delay, on randomized nearby free ground outside Agyu's view. |
| [ForestEncounters.spawn](../src/entities/ForestEncounters.java#L107) | VALIDATED CONCRETE SPAWN | Construct a boar with the herd identity and current outdoor night strength. |

## src/entities/MageBoss.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [MageBoss.MageBoss](../src/entities/MageBoss.java#L10) | CONCRETE BOSS | Create the violet caster at its assigned clearing center. |
| [MageBoss.pursueSpeed](../src/entities/MageBoss.java#L12) | OOP OVERRIDE | Move the caster steadily into range. |
| [MageBoss.attackRange](../src/entities/MageBoss.java#L14) | OOP OVERRIDE | Cast across the broad arena rather than inflicting contact damage. |
| [MageBoss.preferredDistance](../src/entities/MageBoss.java#L16) | OOP OVERRIDE | Keep room between caster and player while approaching. |
| [MageBoss.retreats](../src/entities/MageBoss.java#L18) | OOP OVERRIDE | Let the mage back away when Agyu closes into melee range. |
| [MageBoss.accent](../src/entities/MageBoss.java#L20) | OOP OVERRIDE | Match spell warnings to the supplied violet art. |
| [MageBoss.planAttack](../src/entities/MageBoss.java#L22) | ATTACK SELECTION | Alternate locked fan shots and three delayed rune targets. |
| [MageBoss.strike](../src/entities/MageBoss.java#L26) | ACTIVE CAST | Spawn bounded spell objects once after the staff windup. |
| [MageBoss.warning](../src/entities/MageBoss.java#L34) | CAST TELEGRAPH | Show the locked fan direction or forthcoming rune target while the spell loads. |

## src/entities/NightWildlife.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [NightWildlife.NightWildlife](../src/entities/NightWildlife.java#L20) | COMPOSITION | Bind one wildlife director to the current world/session. |
| [NightWildlife.wolfLimit](../src/entities/NightWildlife.java#L22) | ENCAPSULATED QUOTA | Expose the current night's total two-or-three spawn allowance. |
| [NightWildlife.wolvesSpawned](../src/entities/NightWildlife.java#L24) | ENCAPSULATED QUOTA | Read successful spawns without allowing kills to replenish the counter. |
| [NightWildlife.update](../src/entities/NightWildlife.java#L26) | ACTIVE-TIME DIRECTOR | Spawn rare predators from the world clock and gate Sigbin scares by forest travel, cooldown and safe placement. |
| [NightWildlife.deepForest](../src/entities/NightWildlife.java#L51) | HABITAT QUERY | Keep scares at least five tiles beyond the village boundary. |
| [NightWildlife.nearArena](../src/entities/NightWildlife.java#L57) | BOSS ENCOUNTER EXCLUSION | Avoid adding an ambush to an authored villain encounter. |
| [NightWildlife.dense](../src/entities/NightWildlife.java#L65) | DENSE WOODLAND QUERY | Require multiple nearby tree crowns before a Sigbin encounter. |
| [NightWildlife.free](../src/entities/NightWildlife.java#L67) | COLLISION-SAFE SPAWN | Validate the shared navigation node, actual feet, habitat and separation from existing enemies. |
| [NightWildlife.wolfPosition](../src/entities/NightWildlife.java#L73) | BOUNDED SPAWN SEARCH | Find an offscreen forest location near exploration, with existing camp clearings as a fallback. |
| [NightWildlife.ambushPosition](../src/entities/NightWildlife.java#L84) | BOUNDED AMBUSH SEARCH | Choose free woodland on the edge of the camera instead of placing a creature on Agyu. |

## src/entities/Npc.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Npc.frame](../src/entities/Npc.java#L21) | OOP CONTRACT | Supply the cached animation frame for a direction, action and simulation time. |
| [Npc.Npc](../src/entities/Npc.java#L43) | FUNCTION | Initialize Npc state. |
| [Npc.id](../src/entities/Npc.java#L60) | FUNCTION | Handle id for Npc. |
| [Npc.name](../src/entities/Npc.java#L62) | FUNCTION | Handle name for Npc. |
| [Npc.role](../src/entities/Npc.java#L64) | FUNCTION | Handle role for Npc. |
| [Npc.x](../src/entities/Npc.java#L66) | FUNCTION | Handle x for Npc. |
| [Npc.y](../src/entities/Npc.java#L68) | FUNCTION | Handle y for Npc. |
| [Npc.dialogue](../src/entities/Npc.java#L70) | FUNCTION | Handle dialogue for Npc. |
| [Npc.action](../src/entities/Npc.java#L72) | FUNCTION | Handle action for Npc. |
| [Npc.direction](../src/entities/Npc.java#L74) | FUNCTION | Handle direction for Npc. |
| [Npc.animationTime](../src/entities/Npc.java#L76) | FUNCTION | Handle animation time for Npc. |
| [Npc.isSpeaking](../src/entities/Npc.java#L78) | FUNCTION | Handle is speaking for Npc. |
| [Npc.savePrev](../src/entities/Npc.java#L80) | FUNCTION | Handle save prev for Npc. |
| [Npc.canTalk](../src/entities/Npc.java#L82) | FUNCTION | Handle can talk for Npc. |
| [Npc.talk](../src/entities/Npc.java#L88) | NPC: DIALOGUE | Advance dialogue only when the player can interact. |
| [Npc.update](../src/entities/Npc.java#L94) | NPC: COMPOSITION / GRAPH ROUTES | Advance work, conversation and collision-safe circular patrol movement. |
| [Npc.stationaryWorker](../src/entities/Npc.java#L125) | FUNCTION | Handle stationary worker for Npc. |
| [Npc.finishLeg](../src/entities/Npc.java#L127) | FUNCTION | Handle finish leg for Npc. |
| [Npc.move](../src/entities/Npc.java#L129) | FUNCTION | Handle move for Npc. |
| [Npc.feetBox](../src/entities/Npc.java#L134) | FUNCTION | Handle feet box for Npc. |
| [Npc.face](../src/entities/Npc.java#L136) | FUNCTION | Handle face for Npc. |
| [Npc.sortY](../src/entities/Npc.java#L141) | FUNCTION | Handle sort y for Npc. |
| [Npc.renderX](../src/entities/Npc.java#L143) | FUNCTION | Handle render x for Npc. |
| [Npc.renderY](../src/entities/Npc.java#L145) | FUNCTION | Handle render y for Npc. |
| [Npc.blend](../src/entities/Npc.java#L147) | FUNCTION | Handle blend for Npc. |
| [Npc.draw](../src/entities/Npc.java#L149) | FUNCTION | Render draw using the current graphics state. |
| [Npc.drawDialogue](../src/entities/Npc.java#L164) | FUNCTION | Render draw dialogue using the current graphics state. |
| [Npc.wrap](../src/entities/Npc.java#L191) | FUNCTION | Handle wrap for Npc. |

## src/entities/NpcPopulation.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [NpcPopulation.NpcPopulation](../src/entities/NpcPopulation.java#L12) | FUNCTION | Prevent instances of this static helper class. |
| [NpcPopulation.create](../src/entities/NpcPopulation.java#L14) | NPC: POPULATION | Build the authored workers and neighbors at reachable village positions. |
| [NpcPopulation.findFishingBank](../src/entities/NpcPopulation.java#L49) | FUNCTION | Handle find fishing bank for NpcPopulation. |
| [NpcPopulation.walker](../src/entities/NpcPopulation.java#L55) | FUNCTION | Handle walker for NpcPopulation. |
| [NpcPopulation.add](../src/entities/NpcPopulation.java#L60) | FUNCTION | Handle add for NpcPopulation. |
| [NpcPopulation.nearest](../src/entities/NpcPopulation.java#L65) | FUNCTION | Handle nearest for NpcPopulation. |

## src/entities/Player.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Player.Player](../src/entities/Player.java#L45) | FUNCTION | Initialize Player state. |
| [Player.health](../src/entities/Player.java#L52) | OOP: ENCAPSULATION | Read current private health through the damage contract. |
| [Player.maxHealth](../src/entities/Player.java#L54) | OOP: ENCAPSULATION | Read the maximum health allowed for Agyu. |
| [Player.energy](../src/entities/Player.java#L56) | OOP: COMPOSITION | Read current energy from the owned EnergyMeter. |
| [Player.maxEnergy](../src/entities/Player.java#L58) | RESOURCE LIMIT | Read the shared energy capacity. |
| [Player.energyExhausted](../src/entities/Player.java#L60) | FUNCTION | Handle energy exhausted for Player. |
| [Player.dashing](../src/entities/Player.java#L62) | FUNCTION | Handle dashing for Player. |
| [Player.dashCooldown](../src/entities/Player.java#L64) | FUNCTION | Handle dash cooldown for Player. |
| [Player.inventory](../src/entities/Player.java#L66) | FUNCTION | Handle inventory for Player. |
| [Player.protection](../src/entities/Player.java#L68) | RESOURCE READ | Read blue potion damage absorption separately from directional guarding. |
| [Player.maxProtection](../src/entities/Player.java#L70) | RESOURCE READ | Read the fixed blue potion shield capacity. |
| [Player.hotbar](../src/entities/Player.java#L72) | COMPOSED ARRAY | Expose validated slot operations without exposing its backing array. |
| [Player.itemMessage](../src/entities/Player.java#L74) | ITEM FEEDBACK | Read the last short potion-use result for the hotbar. |
| [Player.itemMessageTime](../src/entities/Player.java#L76) | ITEM FEEDBACK | Read how long temporary item feedback remains visible. |
| [Player.message](../src/entities/Player.java#L78) | ITEM FEEDBACK | Keep failed or successful item actions visible briefly. |
| [Player.drinking](../src/entities/Player.java#L80) | ITEM RENDER STATE | Expose an active drinking action to the HUD and renderer. |
| [Player.drinkProgress](../src/entities/Player.java#L82) | ITEM PROGRESS | Read the active action's normalized progress for the selected slot. |
| [Player.drinkPose](../src/entities/Player.java#L84) | ITEM POSE | Read the authored pose selected by the drinking timeline. |
| [Player.heldItem](../src/entities/Player.java#L86) | VISIBLE EQUIPMENT | Show the current bottle during a sip and only owned equipment otherwise. |
| [Player.equipItem](../src/entities/Player.java#L88) | INVENTORY EQUIP | Move an owned item to a hotbar slot without duplicating supplies and cancel conflicting hand actions. |
| [Player.clearSlot](../src/entities/Player.java#L95) | INVENTORY STOW | Remove a quick reference while retaining every item quantity in the bag. |
| [Player.inventoryCanUse](../src/entities/Player.java#L97) | INVENTORY USE AVAILABILITY | Enable the inventory use action only for owned, useful potions and stable player states. |
| [Player.potionUseful](../src/entities/Player.java#L100) | RESOURCE VALIDATION | Test whether a resource still needs recovery at start and at the sip commit. |
| [Player.usePotion](../src/entities/Player.java#L102) | ITEM ACTION / ENCAPSULATION | Start a validated raise-sip-lower action; consumption happens only at the sip. |
| [Player.sipPotion](../src/entities/Player.java#L115) | ONE-SHOT ITEM COMMIT | Spend one useful potion at the mouth-contact frame and apply its matching resource/effect. |
| [Player.cancelDrink](../src/entities/Player.java#L125) | ITEM INTERRUPTION | Release the bottle animation without consuming an uncommitted potion. |
| [Player.itemsInput](../src/entities/Player.java#L128) | ARRAY SELECTION / EQUIPMENT | Apply hotbar commands and route the primary action to the selected owned item. |
| [Player.savePrev](../src/entities/Player.java#L150) | FUNCTION | Handle save prev for Player. |
| [Player.airborne](../src/entities/Player.java#L152) | FUNCTION | Handle airborne for Player. |
| [Player.jumpHeight](../src/entities/Player.java#L154) | FUNCTION | Handle jump height for Player. |
| [Player.comboStep](../src/entities/Player.java#L156) | FUNCTION | Handle combo step for Player. |
| [Player.comboName](../src/entities/Player.java#L158) | FUNCTION | Handle combo name for Player. |
| [Player.feetBox](../src/entities/Player.java#L160) | FUNCTION | Handle feet box for Player. |
| [Player.update](../src/entities/Player.java#L163) | PLAYER STATE MACHINE | Resolve input priority, movement, jumps, shield, energy and spear combos. |
| [Player.updateDash](../src/entities/Player.java#L264) | FUNCTION | Advance update dash state with elapsed time. |
| [Player.moveFromInput](../src/entities/Player.java#L289) | FUNCTION | Handle move from input for Player. |
| [Player.facingFor](../src/entities/Player.java#L304) | FUNCTION | Handle facing for for Player. |
| [Player.resetCombo](../src/entities/Player.java#L313) | FUNCTION | Handle reset combo for Player. |
| [Player.beginAttack](../src/entities/Player.java#L318) | COMBAT: COMPOSITION | Start the selected spear move and reset matching combo timers. |
| [Player.approach](../src/entities/Player.java#L330) | FUNCTION | Handle approach for Player. |
| [Player.setState](../src/entities/Player.java#L335) | FUNCTION | Handle set state for Player. |
| [Player.move](../src/entities/Player.java#L338) | FUNCTION | Handle move for Player. |
| [Player.vec](../src/entities/Player.java#L349) | FUNCTION | Handle vec for Player. |
| [Player.blocksFrom](../src/entities/Player.java#L360) | FUNCTION | Handle blocks from for Player. |
| [Player.damage](../src/entities/Player.java#L365) | OOP: ENCAPSULATION | Resolve dash/guard protection, update private health and apply hurt/death state. |
| [Player.sortY](../src/entities/Player.java#L381) | FUNCTION | Handle sort y for Player. |
| [Player.draw](../src/entities/Player.java#L384) | FUNCTION | Render draw using the current graphics state. |

## src/entities/Sigbin.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Sigbin.Sigbin](../src/entities/Sigbin.java#L17) | AMBUSH CONSTRUCTION | Create one night predator outside the boar herd groups. |
| [Sigbin.kind](../src/entities/Sigbin.java#L19) | OOP OVERRIDING | Select the supplied Sigbin animation set. |
| [Sigbin.speed](../src/entities/Sigbin.java#L21) | OOP OVERRIDING | Supply the creature's hunting pace before the outdoor night modifier. |
| [Sigbin.attackSpeed](../src/entities/Sigbin.java#L23) | AMBUSH STRIKE POLICY | Turn the supplied rush poses into a bounded claw lunge. |
| [Sigbin.warningDuration](../src/entities/Sigbin.java#L25) | AMBUSH STRIKE POLICY | Give Agyu a readable reaction window before the claw lunge. |
| [Sigbin.contactDamage](../src/entities/Sigbin.java#L27) | NIGHT ENEMY DAMAGE | Supply claw damage through the normal night, guard and dash contracts. |
| [Sigbin.scareActive](../src/entities/Sigbin.java#L29) | ENCAPSULATION | Expose the short visual scare window without advancing it during rendering. |
| [Sigbin.hasScared](../src/entities/Sigbin.java#L31) | ENCAPSULATION | Read the one-shot scare flag for encounter verification. |
| [Sigbin.damage](../src/entities/Sigbin.java#L33) | INTERRUPTIBLE AMBUSH | Let a spear hit cancel the overlay and stagger a surviving creature without ending the fight. |
| [Sigbin.startle](../src/entities/Sigbin.java#L37) | ONE-SHOT SCARE | Trigger one brief camera jolt, creature overlay and soft audio sting without hurting or freezing Agyu. |
| [Sigbin.update](../src/entities/Sigbin.java#L39) | NIGHT AMBUSH / REAL COMBAT | Reveal at night, preserve a reaction window, then use the shared chase and telegraphed claw strike. |
| [Sigbin.removable](../src/entities/Sigbin.java#L57) | FINITE ENCOUNTER | Remove a dawn/disengagement departure without a reward or keep a defeated enemy through all death poses. |
| [Sigbin.opacity](../src/entities/Sigbin.java#L59) | CONTEXTUAL OPACITY | Hide most of the stalking body, then reveal it fully before a smooth departure. |
| [Sigbin.body](../src/entities/Sigbin.java#L61) | SUPPLIED ANIMATION LOOKUP | Use the supplied attack/locomotion poses for real hunting, strikes and death. |
| [Sigbin.draw](../src/entities/Sigbin.java#L67) | STALKING EYES | Depth-sort the creature normally and leave only a faint red-eyed glimpse while it is hiding. |
| [Sigbin.drawOverlay](../src/entities/Sigbin.java#L75) | READ-ONLY SCARE OVERLAY | Briefly bring the supplied front-facing creature forward while leaving the HUD and player controls available. |

## src/entities/TankBoss.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [TankBoss.TankBoss](../src/entities/TankBoss.java#L9) | CONCRETE BOSS | Create the durable green tank at its assigned clearing center. |
| [TankBoss.pursueSpeed](../src/entities/TankBoss.java#L11) | OOP OVERRIDE | Give the tank a slow deliberate pursuit. |
| [TankBoss.attackRange](../src/entities/TankBoss.java#L13) | OOP OVERRIDE | Start heavy attacks only within their advertised reach. |
| [TankBoss.accent](../src/entities/TankBoss.java#L15) | OOP OVERRIDE | Match warnings and impacts to the supplied green power frames. |
| [TankBoss.knockbackFactor](../src/entities/TankBoss.java#L17) | OOP OVERRIDE | Keep the durable tank planted under spear hits. |
| [TankBoss.planAttack](../src/entities/TankBoss.java#L19) | ATTACK CYCLE | Alternate a frontal cleave and an explicitly warned radial slam. |
| [TankBoss.strike](../src/entities/TankBoss.java#L24) | ACTIVE ATTACK | Resolve a single heavy impact after the warning window. |
| [TankBoss.warning](../src/entities/TankBoss.java#L31) | ATTACK TELEGRAPH | Display the exact cone or radial ground footprint before impact. |

## src/entities/WildBoar.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [WildBoar.WildBoar](../src/entities/WildBoar.java#L7) | OOP INHERITANCE | Bind three health points and the current random herd identity. |
| [WildBoar.kind](../src/entities/WildBoar.java#L9) | OOP OVERRIDING | Select the user's boar artwork. |
| [WildBoar.speed](../src/entities/WildBoar.java#L11) | OOP OVERRIDING | Supply ordinary pursuit speed before the outdoor night modifier. |
| [WildBoar.contactDamage](../src/entities/WildBoar.java#L13) | OOP OVERRIDING | Supply one point of daytime tusk damage. |

## src/entities/Wolf.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Wolf.Wolf](../src/entities/Wolf.java#L8) | NIGHT WILDLIFE CONSTRUCTION | Supply a tougher rare predator outside shrine progression groups. |
| [Wolf.kind](../src/entities/Wolf.java#L10) | OOP OVERRIDING | Select the user's white wolf animation cache. |
| [Wolf.speed](../src/entities/Wolf.java#L12) | OOP OVERRIDING | Supply faster pursuit before the normal outdoor night multiplier. |
| [Wolf.contactDamage](../src/entities/Wolf.java#L14) | OOP OVERRIDING | Supply bite damage through the shared night/guard contracts. |
| [Wolf.hurtBox](../src/entities/Wolf.java#L16) | AUTHORED HURT BOX | Match the taller, longer supplied wolf torso without widening its navigation feet. |
| [Wolf.warningDuration](../src/entities/Wolf.java#L18) | OOP ATTACK POLICY | Give the rare predator a readable crouch before pouncing. |
| [Wolf.strikeDuration](../src/entities/Wolf.java#L20) | OOP ATTACK POLICY | Play the complete four-frame pounce before recovery. |
| [Wolf.attackSpeed](../src/entities/Wolf.java#L22) | OOP ATTACK POLICY | Bound leap travel while retaining swept solid collision. |
| [Wolf.recoveryDuration](../src/entities/Wolf.java#L24) | OOP ATTACK POLICY | Leave a longer opening after the pounce. |
| [Wolf.attackRange](../src/entities/Wolf.java#L26) | OOP ATTACK POLICY | Start a leap from a little farther away than a boar's charge. |
| [Wolf.update](../src/entities/Wolf.java#L28) | DAWN LIFECYCLE | Stop attacking immediately at dawn and fade away without a defeat reward. |
| [Wolf.opacity](../src/entities/Wolf.java#L33) | DEPARTURE RENDERING | Fade the living predator's dawn departure independently of its authored death sequence. |
| [Wolf.removable](../src/entities/Wolf.java#L35) | BOUNDED LIFETIME | Remove a departing wolf after the fade or a defeated wolf after all death poses. |

## src/rendering/Assets.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Assets.load](../src/rendering/Assets.java#L23) | ASSET STARTUP | Load current animation frames, combat bodies, shield poses and village images. |
| [Assets.frames](../src/rendering/Assets.java#L50) | FUNCTION | Handle frames for Assets. |
| [Assets.directional](../src/rendering/Assets.java#L57) | DSA: 2D ARRAY / MIRRORING | Assemble DOWN, UP, LEFT and RIGHT animation arrays. |
| [Assets.load](../src/rendering/Assets.java#L64) | ASSET STARTUP | Load current animation frames, combat bodies, shield poses and village images. |
| [Assets.flip](../src/rendering/Assets.java#L80) | ASSET PREPARATION | Mirror exact RGBA pixels without changing semi-transparent edge colors. |

## src/rendering/BossArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [BossArt.BossArt](../src/rendering/BossArt.java#L21) | CACHE UTILITY | Prevent instances of the shared animation provider. |
| [BossArt.count](../src/rendering/BossArt.java#L23) | AUTHORING METADATA | Return the real number of source poses for a state. |
| [BossArt.load](../src/rendering/BossArt.java#L25) | ASSET STARTUP | Validate and publish the complete three-atlas cache once. |
| [BossArt.read](../src/rendering/BossArt.java#L42) | RESOURCE READING | Load a packed atlas from a project/resource classpath with a useful startup error. |
| [BossArt.frame](../src/rendering/BossArt.java#L50) | CACHED FRAME LOOKUP | Return an existing authored pose without allocating images or advancing animation state. |

## src/rendering/ComboEffects.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ComboEffects.ComboEffects](../src/rendering/ComboEffects.java#L44) | FUNCTION | Prevent instances of this static helper class. |
| [ComboEffects.clear](../src/rendering/ComboEffects.java#L46) | FUNCTION | Handle clear for ComboEffects. |
| [ComboEffects.ghostCount](../src/rendering/ComboEffects.java#L48) | FUNCTION | Handle ghost count for ComboEffects. |
| [ComboEffects.ringCount](../src/rendering/ComboEffects.java#L50) | FUNCTION | Handle ring count for ComboEffects. |
| [ComboEffects.impactCount](../src/rendering/ComboEffects.java#L52) | FUNCTION | Handle impact count for ComboEffects. |
| [ComboEffects.effectCount](../src/rendering/ComboEffects.java#L54) | FUNCTION | Handle effect count for ComboEffects. |
| [ComboEffects.stage](../src/rendering/ComboEffects.java#L56) | FUNCTION | Handle stage for ComboEffects. |
| [ComboEffects.crossed](../src/rendering/ComboEffects.java#L60) | FUNCTION | Handle crossed for ComboEffects. |
| [ComboEffects.step](../src/rendering/ComboEffects.java#L65) | FUNCTION | Handle step for ComboEffects. |
| [ComboEffects.hit](../src/rendering/ComboEffects.java#L90) | FUNCTION | Handle hit for ComboEffects. |
| [ComboEffects.ring](../src/rendering/ComboEffects.java#L99) | FUNCTION | Handle ring for ComboEffects. |
| [ComboEffects.update](../src/rendering/ComboEffects.java#L104) | FUNCTION | Advance update state with elapsed time. |
| [ComboEffects.draw](../src/rendering/ComboEffects.java#L117) | FUNCTION | Render draw using the current graphics state. |
| [ComboEffects.drawRing](../src/rendering/ComboEffects.java#L142) | FUNCTION | Render draw ring using the current graphics state. |
| [ComboEffects.drawImpact](../src/rendering/ComboEffects.java#L165) | FUNCTION | Render draw impact using the current graphics state. |

## src/rendering/DashEffects.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [DashEffects.DashEffects](../src/rendering/DashEffects.java#L29) | FUNCTION | Initialize DashEffects state. |
| [DashEffects.ghostCount](../src/rendering/DashEffects.java#L31) | FUNCTION | Handle ghost count for DashEffects. |
| [DashEffects.boltCount](../src/rendering/DashEffects.java#L33) | FUNCTION | Handle bolt count for DashEffects. |
| [DashEffects.effectCount](../src/rendering/DashEffects.java#L35) | FUNCTION | Handle effect count for DashEffects. |
| [DashEffects.clear](../src/rendering/DashEffects.java#L37) | FUNCTION | Handle clear for DashEffects. |
| [DashEffects.emit](../src/rendering/DashEffects.java#L39) | FUNCTION | Handle emit for DashEffects. |
| [DashEffects.update](../src/rendering/DashEffects.java#L62) | FUNCTION | Advance update state with elapsed time. |
| [DashEffects.draw](../src/rendering/DashEffects.java#L68) | FUNCTION | Render draw using the current graphics state. |
| [DashEffects.line](../src/rendering/DashEffects.java#L88) | FUNCTION | Handle line for DashEffects. |

## src/rendering/ForestAudio.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ForestAudio.ForestAudio](../src/rendering/ForestAudio.java#L17) | AUDIO UTILITY | Prevent instances of the single bounded sound provider. |
| [ForestAudio.sting](../src/rendering/ForestAudio.java#L19) | CACHED PCM ARRAY | Prepare a brief low-volume descending growl with a smooth attack and release. |
| [ForestAudio.scare](../src/rendering/ForestAudio.java#L29) | BOUNDED AUDIO PLAYBACK | Play at most one sting off the game loop, release its device and tolerate missing audio hardware. |

## src/rendering/Fx.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Fx.clear](../src/rendering/Fx.java#L18) | FUNCTION | Handle clear for Fx. |
| [Fx.burst](../src/rendering/Fx.java#L23) | FUNCTION | Handle burst for Fx. |
| [Fx.dust](../src/rendering/Fx.java#L32) | FUNCTION | Handle dust for Fx. |
| [Fx.spark](../src/rendering/Fx.java#L34) | FUNCTION | Handle spark for Fx. |
| [Fx.poof](../src/rendering/Fx.java#L36) | FUNCTION | Handle poof for Fx. |
| [Fx.shake](../src/rendering/Fx.java#L38) | FUNCTION | Handle shake for Fx. |
| [Fx.hitStop](../src/rendering/Fx.java#L45) | FUNCTION | Handle hit stop for Fx. |
| [Fx.update](../src/rendering/Fx.java#L47) | FUNCTION | Advance update state with elapsed time. |
| [Fx.draw](../src/rendering/Fx.java#L62) | FUNCTION | Render draw using the current graphics state. |

## src/rendering/GameScale.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [GameScale.GameScale](../src/rendering/GameScale.java#L12) | FUNCTION | Prevent instances of this static helper class. |
| [GameScale.agyuGraphics](../src/rendering/GameScale.java#L14) | FUNCTION | Handle agyu graphics for GameScale. |
| [GameScale.agyuFrame](../src/rendering/GameScale.java#L21) | FUNCTION | Handle agyu frame for GameScale. |

## src/rendering/HeldItemArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [HeldItemArt.HeldItemArt](../src/rendering/HeldItemArt.java#L24) | STATIC CACHE | Prevent instances of the shared animation cache. |
| [HeldItemArt.load](../src/rendering/HeldItemArt.java#L26) | CACHE STARTUP / ARRAY COMPOSITION | Register source bodies and prepare every held-item combination, including mirrored left poses. |
| [HeldItemArt.withLegs](../src/rendering/HeldItemArt.java#L102) | WALK FRAME COMPOSITION | Retain the equipment pose above the hip and apply the footfall below it. |
| [HeldItemArt.compose](../src/rendering/HeldItemArt.java#L113) | STARTUP COMPOSITOR | Layer a cached selected item at the authored fist, preserving original finger pixels. |
| [HeldItemArt.pixelClean](../src/rendering/HeldItemArt.java#L134) | PIXEL ART CLEANUP | Remove translucent edge haze while retaining crisp opaque source pixels and proportions. |
| [HeldItemArt.withoutCarryArm](../src/rendering/HeldItemArt.java#L144) | ARM MASK | Remove the old bent hand in the replacement arm region and restore the tunic edge. |
| [HeldItemArt.withoutShieldHand](../src/rendering/HeldItemArt.java#L177) | SHIELD HAND OCCLUSION | Tuck the old hanging hand behind the held shield in front and rear views. |
| [HeldItemArt.shieldX](../src/rendering/HeldItemArt.java#L193) | EQUIPMENT COORDINATES | Keep the shield on Agyu's left and centered over the braced wrist in profile. |
| [HeldItemArt.carryGrip](../src/rendering/HeldItemArt.java#L197) | RIGHT-HAND GRIP | Register the right-hand shaft to a stable whole-pixel position across the walk cycle. |
| [HeldItemArt.carryArm](../src/rendering/HeldItemArt.java#L202) | BENT CARRY ARM | Replace the masked arm with a bent forearm and a small fist that overlaps the pole. |
| [HeldItemArt.drawShield](../src/rendering/HeldItemArt.java#L226) | NATIVE SHIELD LAYER | Fit the wooden shield over the forearm using a narrower profile in the side views. |
| [HeldItemArt.composeGear](../src/rendering/HeldItemArt.java#L235) | CACHED GEAR POSE | Combine the shared torso, the current step and clean gear layers at one native foot anchor. |
| [HeldItemArt.drawCarriedSpear](../src/rendering/HeldItemArt.java#L259) | SPEAR ORIENTATION | Keep the relaxed spear vertical and aim the braced spear along the facing direction. |
| [HeldItemArt.bottleAngle](../src/rendering/HeldItemArt.java#L279) | SIP DIRECTION | Bring the bottle opening toward the visible lips as the wrist lifts and tilts. |
| [HeldItemArt.index](../src/rendering/HeldItemArt.java#L284) | ARRAY ACCESS | Include the empty-hand entry after the concrete item types. |
| [HeldItemArt.frame](../src/rendering/HeldItemArt.java#L286) | CACHED FRAME | Read a prepared item/body pose without allocating during paint. |
| [HeldItemArt.ghost](../src/rendering/HeldItemArt.java#L288) | CACHED AFTERIMAGE | Reuse selected equipment in dash afterimages rather than showing an unequipped spear. |
| [HeldItemArt.draw](../src/rendering/HeldItemArt.java#L290) | CACHED DRAW | Draw a selected-item composite about its shared foot anchor. |
| [HeldItemArt.guardFrame](../src/rendering/HeldItemArt.java#L292) | SHARED GUARD CACHE | Use one body and gait for every selected item, with a carried spear only when equipped. |
| [HeldItemArt.drawGuard](../src/rendering/HeldItemArt.java#L299) | SHIELD WALK CACHE | Keep shield arms braced and advance shared authored leg steps during guarded movement. |
| [HeldItemArt.isProfile](../src/rendering/HeldItemArt.java#L303) | PROFILE FACING | Identify the directions that use the full side walk cycle. |
| [HeldItemArt.profileIndex](../src/rendering/HeldItemArt.java#L307) | MIRRORED CACHE INDEX | Map left and right onto the parallel profile animation caches. |
| [HeldItemArt.profileFrame](../src/rendering/HeldItemArt.java#L311) | PROFILE WALK CACHE | Advance each cached profile frame by traveled distance. |
| [HeldItemArt.drawProfileWalk](../src/rendering/HeldItemArt.java#L316) | PROFILE WALK DRAW | Draw the cached side cycle at the shared foot anchor. |
| [HeldItemArt.drawDrinking](../src/rendering/HeldItemArt.java#L320) | WALKING DRINK CACHE | Combine authored sipping and walking steps through cached full sprites. |
| [HeldItemArt.walkPose](../src/rendering/HeldItemArt.java#L325) | LOCOMOTION POSE | Alternate authored steps and centered passing poses at walking or sprint cadence. |

## src/rendering/HotbarHud.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [HotbarHud.HotbarHud](../src/rendering/HotbarHud.java#L16) | STATIC HUD | Prevent instances of the shared item dock renderer. |
| [HotbarHud.left](../src/rendering/HotbarHud.java#L18) | SHARED LAYOUT | Center all nine slots as a single dock. |
| [HotbarHud.top](../src/rendering/HotbarHud.java#L20) | SAFE AREA | Leave a clear bottom margin at every window size. |
| [HotbarHud.slotAt](../src/rendering/HotbarHud.java#L22) | SHARED HIT GEOMETRY | Match only the visible slot interior and reject gaps, text and margins. |
| [HotbarHud.draw](../src/rendering/HotbarHud.java#L27) | EQUIPMENT HUD | Place keys and counts in their own corners and show only one compact context label. |

## src/rendering/InventoryScreen.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [InventoryScreen.open](../src/rendering/InventoryScreen.java#L19) | BAG SELECTION | Inspect the equipped item without changing its slot or quantity. |
| [InventoryScreen.item](../src/rendering/InventoryScreen.java#L21) | ARRAY LOOKUP | Resolve the selected bag card through the fixed item catalog. |
| [InventoryScreen.select](../src/rendering/InventoryScreen.java#L23) | BAG SELECTION | Accept only a concrete item card. |
| [InventoryScreen.cycle](../src/rendering/InventoryScreen.java#L25) | CYCLIC SELECTION | Move around the bag with bounded mouse-wheel input. |
| [InventoryScreen.turn](../src/rendering/InventoryScreen.java#L27) | PREVIEW FACING | Rotate a static equipment preview without updating the live actor. |
| [InventoryScreen.hit](../src/rendering/InventoryScreen.java#L29) | SHARED HIT GEOMETRY | Decode clicks on cards, quick slots and action buttons in art-resolution coordinates. |
| [InventoryScreen.inside](../src/rendering/InventoryScreen.java#L40) | RECTANGLE TEST | Reject borders and gaps outside a concrete UI control. |
| [InventoryScreen.tint](../src/rendering/InventoryScreen.java#L42) | ITEM PALETTE | Match health, energy and shield potions to their resource colors. |
| [InventoryScreen.box](../src/rendering/InventoryScreen.java#L45) | PANEL FRAME | Draw a crisp inset box with a one-pixel highlight. |
| [InventoryScreen.button](../src/rendering/InventoryScreen.java#L50) | ACTION BUTTON | Show disabled actions explicitly while retaining shared pointer bounds. |
| [InventoryScreen.draw](../src/rendering/InventoryScreen.java#L55) | PAUSED INVENTORY DRAW | Present the bag, held-item preview and editable hotbar without mutating gameplay. |

## src/rendering/MountainArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [MountainArt.MountainArt](../src/rendering/MountainArt.java#L9) | STATIC CACHE | Prevent instances of the shared scenery accessors. |
| [MountainArt.mountain](../src/rendering/MountainArt.java#L11) | REGISTERED MOUNTAIN | Reuse the prepared 1024 by 1280 mountain silhouette. |
| [MountainArt.waterfall](../src/rendering/MountainArt.java#L13) | CASCADE CACHE | Reuse one animated upper waterfall phase from the original GIF. |

## src/rendering/PotionArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [PotionArt.PotionArt](../src/rendering/PotionArt.java#L16) | STATIC ART | Prevent instances of the shared item cache. |
| [PotionArt.load](../src/rendering/PotionArt.java#L18) | CACHE STARTUP | Decode supplied runtime art and cache the current spear and wooden shield icons once. |
| [PotionArt.read](../src/rendering/PotionArt.java#L39) | VALIDATED ASSET IO | Require the packed item dimensions expected by the runtime cache. |
| [PotionArt.icon](../src/rendering/PotionArt.java#L45) | ARRAY LOOKUP | Read one cached equipment or potion icon by item type. |
| [PotionArt.frame](../src/rendering/PotionArt.java#L47) | ARRAY LOOKUP | Read an indexed supplied animation frame without allocating an image. |
| [PotionArt.kind](../src/rendering/PotionArt.java#L49) | ITEM INDEX | Map the three consumables into compact effect array rows. |

## src/rendering/PotionEffects.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [PotionEffects.play](../src/rendering/PotionEffects.java#L13) | ANIMATION START | Restart just the consumed or impacted potion effect. |
| [PotionEffects.update](../src/rendering/PotionEffects.java#L15) | ACTIVE SIMULATION | Advance bounded animation phases and expire one-shot effects. |
| [PotionEffects.remaining](../src/rendering/PotionEffects.java#L17) | ANIMATION READ | Read a timer for deterministic pause and expiry checks. |
| [PotionEffects.draw](../src/rendering/PotionEffects.java#L19) | CACHED FRAME RENDER | Draw supplied recovery effects and loop a subdued shield aura while protection remains. |

## src/rendering/ProfileWalkArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ProfileWalkArt.ProfileWalkArt](../src/rendering/ProfileWalkArt.java#L14) | ART CACHE UTILITY | Restrict construction to the profile animation cache. |
| [ProfileWalkArt.steps](../src/rendering/ProfileWalkArt.java#L17) | SOURCE FOOTFALLS | Fit the original walk to the standing boots without changing the ground anchor. |
| [ProfileWalkArt.groundedSole](../src/rendering/ProfileWalkArt.java#L44) | GROUND REGISTRATION | Find the planted sole so the lifted foot keeps its clearance while the stride stays grounded. |
| [ProfileWalkArt.bootWidth](../src/rendering/ProfileWalkArt.java#L52) | BOOT REGISTRATION | Measure the silhouette of the standing and starting walk frames. |

## src/rendering/RpgArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [RpgArt.RpgArt](../src/rendering/RpgArt.java#L23) | FUNCTION | Prevent instances of this static helper class. |
| [RpgArt.load](../src/rendering/RpgArt.java#L25) | ASSET STARTUP / ARRAYS | Validate and publish the complete current village sprite cache. |
| [RpgArt.read](../src/rendering/RpgArt.java#L52) | FUNCTION | Load or validate read data. |
| [RpgArt.fullSizeHouse](../src/rendering/RpgArt.java#L56) | FUNCTION | Handle full size house for RpgArt. |
| [RpgArt.readPath](../src/rendering/RpgArt.java#L68) | FUNCTION | Load or validate read path data. |
| [RpgArt.buildForestEdge](../src/rendering/RpgArt.java#L83) | CACHED OUTSKIRTS ART | Prepare a seamless forest canopy strip for the north-gate camera margin. |
| [RpgArt.forestEdge](../src/rendering/RpgArt.java#L92) | RENDER CACHE | Return the already generated north-edge canopy strip. |
| [RpgArt.measureRoots](../src/rendering/RpgArt.java#L94) | ASSET GEOMETRY CACHE | Derive the solid ground footprint from opaque bark, excluding stray grass below a tree. |
| [RpgArt.wood](../src/rendering/RpgArt.java#L104) | SOURCE PIXEL CLASSIFICATION | Recognize opaque warm bark without counting green ground tufts or leaves. |
| [RpgArt.treeGround](../src/rendering/RpgArt.java#L109) | DSA - ARRAY LOOKUP | Return one immutable, cached root-footprint template. |
| [RpgArt.tree](../src/rendering/RpgArt.java#L111) | FUNCTION | Handle tree for RpgArt. |
| [RpgArt.foliage](../src/rendering/RpgArt.java#L113) | FUNCTION | Handle foliage for RpgArt. |
| [RpgArt.prop](../src/rendering/RpgArt.java#L115) | FUNCTION | Handle prop for RpgArt. |
| [RpgArt.terrain](../src/rendering/RpgArt.java#L120) | FUNCTION | Handle terrain for RpgArt. |
| [RpgArt.npc](../src/rendering/RpgArt.java#L122) | FUNCTION | Handle npc for RpgArt. |
| [RpgArt.npcFrame](../src/rendering/RpgArt.java#L124) | FUNCTION | Handle npc frame for RpgArt. |
| [RpgArt.CachedNpc](../src/rendering/RpgArt.java#L128) | FUNCTION | Initialize CachedNpc state. |
| [RpgArt.frame](../src/rendering/RpgArt.java#L130) | FUNCTION | Handle frame for RpgArt. |

## src/rendering/RpgTerrain.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [RpgTerrain.RpgTerrain](../src/rendering/RpgTerrain.java#L11) | FUNCTION | Prevent instances of this static helper class. |
| [RpgTerrain.load](../src/rendering/RpgTerrain.java#L13) | FUNCTION | Load or validate load data. |
| [RpgTerrain.shoreDistance](../src/rendering/RpgTerrain.java#L45) | ROUNDED SHORELINE | Round exposed water corners using signed arc distance rather than square corner strips. |
| [RpgTerrain.distance](../src/rendering/RpgTerrain.java#L54) | FUNCTION | Handle distance for RpgTerrain. |
| [RpgTerrain.tile](../src/rendering/RpgTerrain.java#L63) | RENDER: ARRAY CACHE | Select the prebuilt transition image from world edge masks. |

## src/rendering/VillageArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [VillageArt.VillageArt](../src/rendering/VillageArt.java#L14) | FUNCTION | Prevent instances of this static helper class. |
| [VillageArt.load](../src/rendering/VillageArt.java#L16) | FUNCTION | Load or validate load data. |
| [VillageArt.tile](../src/rendering/VillageArt.java#L27) | FUNCTION | Handle tile for VillageArt. |

## src/rendering/WaterfallArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [WaterfallArt.WaterfallArt](../src/rendering/WaterfallArt.java#L15) | STATIC CACHE | Prevent instances of shared art. |
| [WaterfallArt.load](../src/rendering/WaterfallArt.java#L17) | ASSET STARTUP | Validate and slice the registered waterfall PNG caches once. |
| [WaterfallArt.read](../src/rendering/WaterfallArt.java#L30) | IMAGE VALIDATION | Load packaged resources or repository files with exact expected dimensions. |
| [WaterfallArt.ensure](../src/rendering/WaterfallArt.java#L37) | CACHE AVAILABILITY | Support world/map tools that request scenery before normal asset startup. |
| [WaterfallArt.mountain](../src/rendering/WaterfallArt.java#L39) | MOUNTAIN CACHE | Reuse the detailed forest escarpment prepared at native pixel resolution. |
| [WaterfallArt.cove](../src/rendering/WaterfallArt.java#L41) | ANIMATION ARRAY | Reuse one complete painted pond and lower cascade phase. |
| [WaterfallArt.upper](../src/rendering/WaterfallArt.java#L43) | ANIMATION ARRAY | Reuse the source-fed high mountain cascade. |
| [WaterfallArt.fall](../src/rendering/WaterfallArt.java#L45) | ANIMATION ARRAY | Expose the registered lower falling-water source for validation and tooling. |
| [WaterfallArt.water](../src/rendering/WaterfallArt.java#L47) | ANIMATION ARRAY | Reuse original jade water texture throughout the river. |
| [WaterfallArt.water](../src/rendering/WaterfallArt.java#L49) | WORLD TEXTURE VARIANTS | Reuse the matching tile within a seamless 64 by 128 river pattern. |
| [WaterfallArt.mouth](../src/rendering/WaterfallArt.java#L51) | CACHED OUTLET | Reuse the tapered connector phase sharing the cove's native texture and banks. |
| [WaterfallArt.mouthWet](../src/rendering/WaterfallArt.java#L53) | PHYSICAL OUTLET MASK | Query water from the same connector mask used for painting. |
| [WaterfallArt.bank](../src/rendering/WaterfallArt.java#L55) | NATIVE BANK STRIPS | Read a source rock/moss shoreline pixel at a bounded distance from water. |
| [WaterfallArt.wet](../src/rendering/WaterfallArt.java#L57) | PHYSICAL SURFACE MASK | Query authored water, including submerged lily-pad islands. |

## src/rendering/WildlifeArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [WildlifeArt.WildlifeArt](../src/rendering/WildlifeArt.java#L22) | CACHE UTILITY | Prevent instances of the shared resource provider. |
| [WildlifeArt.count](../src/rendering/WildlifeArt.java#L24) | AUTHORED COUNTS | Preserve all four movement/attack/leap poses and five death poses. |
| [WildlifeArt.load](../src/rendering/WildlifeArt.java#L26) | ASSET STARTUP | Prepare original, mirrored and flash variants once and publish complete animation caches. |
| [WildlifeArt.scarePortrait](../src/rendering/WildlifeArt.java#L59) | CACHED CLOSE-UP | Return the detailed supplied front pose prepared specifically for the brief scare overlay. |
| [WildlifeArt.frame](../src/rendering/WildlifeArt.java#L61) | CACHED FRAME LOOKUP | Select an existing transparent pose without allocating or advancing simulation. |

## src/rendering/WoodenShieldArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [WoodenShieldArt.WoodenShieldArt](../src/rendering/WoodenShieldArt.java#L12) | STATIC ART | Keep the shield images in one shared cache. |
| [WoodenShieldArt.face](../src/rendering/WoodenShieldArt.java#L15) | IMAGE CACHE | Return the shield face or the strapped reverse. |
| [WoodenShieldArt.build](../src/rendering/WoodenShieldArt.java#L20) | PIXEL SHIELD CONSTRUCTION | Draw a continuous rim around wood, a centered boss and leather straps. |

## src/rendering/WorldLighting.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [WorldLighting.WorldLighting](../src/rendering/WorldLighting.java#L17) | RENDER CACHE | Allocate one reusable viewport mask instead of one image per frame. |
| [WorldLighting.spot](../src/rendering/WorldLighting.java#L19) | SHARED LIGHT CACHE | Prepare a smooth radial alpha texture once for all visibility and lamp pools. |
| [WorldLighting.pool](../src/rendering/WorldLighting.java#L29) | CACHED LIGHT BLIT | Cull and remove darkness with an existing soft alpha image. |
| [WorldLighting.warm](../src/rendering/WorldLighting.java#L35) | VILLAGE GLOW | Add a faint cached lamp glow without repainting or replacing source sprites. |
| [WorldLighting.draw](../src/rendering/WorldLighting.java#L41) | NIGHT COMPOSITION | Tint the visible world, preserve actor clarity and leave all simulation untouched. |

## src/world/DayNightCycle.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [DayNightCycle.update](../src/world/DayNightCycle.java#L14) | SIMULATION CLOCK | Preserve remainder and all phase crossings, including coarse elapsed-time steps. |
| [DayNightCycle.phase](../src/world/DayNightCycle.java#L20) | ENUM STATE | Derive the alternating phase from the transition count. |
| [DayNightCycle.night](../src/world/DayNightCycle.java#L22) | ENCAPSULATION | Identify the gameplay night phase independently of the visual transition. |
| [DayNightCycle.number](../src/world/DayNightCycle.java#L24) | NUMBERED CYCLE | Return Day 1/Night 1, Day 2/Night 2 and subsequent cycle numbers. |
| [DayNightCycle.phaseSeconds](../src/world/DayNightCycle.java#L26) | ENCAPSULATION | Read active seconds elapsed in the current three-minute phase. |
| [DayNightCycle.remainingSeconds](../src/world/DayNightCycle.java#L28) | COUNTDOWN | Round the remaining phase duration for a stable whole-second HUD. |
| [DayNightCycle.label](../src/world/DayNightCycle.java#L30) | CLOCK LABEL | Provide the numbered phase without exposing writable clock state. |
| [DayNightCycle.countdown](../src/world/DayNightCycle.java#L32) | CLOCK LABEL | Format the remaining interval as minutes and seconds. |
| [DayNightCycle.darkness](../src/world/DayNightCycle.java#L34) | CONTINUOUS LIGHTING | Fade through the final twelve seconds of each phase with no boundary jump. |

## src/world/FarmPlot.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [FarmPlot.FarmPlot](../src/world/FarmPlot.java#L17) | FUNCTION | Initialize FarmPlot state. |
| [FarmPlot.draw](../src/world/FarmPlot.java#L24) | RENDER: CACHE LOOKUP | Draw one existing farm image without rebuilding its artwork. |
| [FarmPlot.build](../src/world/FarmPlot.java#L26) | CACHED FARM ART | Generate textured beds, crops, aisle and open gate once for this plot. |
| [FarmPlot.crop](../src/world/FarmPlot.java#L76) | FUNCTION | Handle crop for FarmPlot. |
| [FarmPlot.leaf](../src/world/FarmPlot.java#L106) | FUNCTION | Handle leaf for FarmPlot. |
| [FarmPlot.tuft](../src/world/FarmPlot.java#L111) | FUNCTION | Handle tuft for FarmPlot. |
| [FarmPlot.rail](../src/world/FarmPlot.java#L116) | FUNCTION | Handle rail for FarmPlot. |
| [FarmPlot.post](../src/world/FarmPlot.java#L123) | FUNCTION | Handle post for FarmPlot. |
| [FarmPlot.color](../src/world/FarmPlot.java#L129) | FUNCTION | Handle color for FarmPlot. |

## src/world/ForestRegion.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ForestRegion.ForestRegion](../src/world/ForestRegion.java#L25) | SEEDED WORLD | Connect the four gates to a woodland loop and reserve combat clearings. |
| [ForestRegion.store](../src/world/ForestRegion.java#L64) | CONSTRUCTION INDEX | Index one root only in construction buckets that overlap its footprint. |
| [ForestRegion.arenas](../src/world/ForestRegion.java#L69) | ENCAPSULATION | Expose three immutable distant arenas assigned to the villain encounters. |
| [ForestRegion.route](../src/world/ForestRegion.java#L71) | TRAIL WAYPOINTS | Join authored bends into connected paths through each forest region. |
| [ForestRegion.cacheGround](../src/world/ForestRegion.java#L73) | TEXTURE CACHE | Prepare shaded woodland grass once without changing the source images. |
| [ForestRegion.ground](../src/world/ForestRegion.java#L81) | RENDER LOOKUP | Reuse a shaded grass tile without allocating during rendering. |
| [ForestRegion.contains](../src/world/ForestRegion.java#L83) | REGION QUERY | Identify world positions beyond the timber enclosure. |
| [ForestRegion.treeCount](../src/world/ForestRegion.java#L89) | CACHED SCENERY | Report the generated exterior population without traversing it again. |
| [ForestRegion.camp](../src/world/ForestRegion.java#L91) | ENCAPSULATION | Return a copy of an authored outdoor respawn clearing. |
| [ForestRegion.trailAt](../src/world/ForestRegion.java#L93) | CACHED ARRAY LOOKUP | Read a reserved forest trail cell without changing terrain. |
| [ForestRegion.line](../src/world/ForestRegion.java#L95) | TRAIL GENERATION | Paint a three-cell-wide orthogonal woodland trail, preserving gate and wall tiles. |
| [ForestRegion.clearing](../src/world/ForestRegion.java#L101) | CLEARING GENERATION | Reserve dry ground and approach space around trails, camps and shrines. |
| [ForestRegion.reserved](../src/world/ForestRegion.java#L110) | ROOT CLEARANCE | Keep the complete padded root rectangle out of every trail and clearing. |
| [ForestRegion.dry](../src/world/ForestRegion.java#L116) | TERRAIN VALIDATION | Require the entire padded root footprint to lie on grass inside the world. |
| [ForestRegion.touchesVillage](../src/world/ForestRegion.java#L123) | WALL CLEARANCE | Preserve an open strip immediately outside the protected village. |

## src/world/MountainRiver.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [MountainRiver.MountainRiver](../src/world/MountainRiver.java#L21) | TERRAIN CONSTRUCTION | Raise eastern rock and link the waterfall basin to the existing river and western outflow. |
| [MountainRiver.source](../src/world/MountainRiver.java#L41) | ENCAPSULATION | Return a copy of the waterfall basin's river source coordinate. |
| [MountainRiver.bounds](../src/world/MountainRiver.java#L43) | ENCAPSULATION | Return a copy of the full mountain artwork bounds. |
| [MountainRiver.coveBounds](../src/world/MountainRiver.java#L45) | COVE GEOMETRY | Return registered artwork bounds for maps and scenery checks. |
| [MountainRiver.reserved](../src/world/MountainRiver.java#L47) | FOREST CLEARANCE | Keep tree crowns and roots away from the open pond and its walkable shore. |
| [MountainRiver.underMouth](../src/world/MountainRiver.java#L49) | OUTLET GROUND LAYER | Draw forest ground beneath the exact cove and connector instead of rectangular water backing. |
| [MountainRiver.mouthBounds](../src/world/MountainRiver.java#L51) | OUTLET GEOMETRY | Return an isolated copy of the exact registered connector extent. |
| [MountainRiver.update](../src/world/MountainRiver.java#L53) | SIMULATION CLOCK | Advance animation only when the world simulation is active. |
| [MountainRiver.collect](../src/world/MountainRiver.java#L55) | RENDER CULLING | Add the huge mountain only when its art intersects the camera. |
| [MountainRiver.sortY](../src/world/MountainRiver.java#L57) | SCENERY DEPTH | Draw the solid cliff behind actors walking on its valid dry shore. |
| [MountainRiver.lookAhead](../src/world/MountainRiver.java#L59) | SCENIC CAMERA | Gently lift the camera beside the basin while keeping Agyu on screen. |
| [MountainRiver.draw](../src/world/MountainRiver.java#L64) | CACHED RENDER | Select prebuilt mountain, cascade and cove frames without mutating simulation. |
| [MountainRiver.drawArt](../src/world/MountainRiver.java#L71) | REGISTERED SCENERY | Align the upper source, lower cliff waterfall, receiving pond and southwest outlet. |
| [MountainRiver.drawOverview](../src/world/MountainRiver.java#L79) | MAP CACHE | Draw correctly registered scenery into the whole-world map without stretching the mountain. |
| [MountainRiver.registerCove](../src/world/MountainRiver.java#L83) | ART / COLLISION REGISTRATION | Derive solid water and cliffs from the same native artwork used for rendering. |
| [MountainRiver.registerMouth](../src/world/MountainRiver.java#L98) | OUTLET COLLISION REGISTRATION | Match tapered water to the prepared mask while retaining solid cliffs and the original pond. |
| [MountainRiver.channel](../src/world/MountainRiver.java#L109) | RIVER CENTERLINE | Rasterize authored stream routes at a constant width matching the village river. |
| [MountainRiver.water](../src/world/MountainRiver.java#L123) | HYDRAULIC / WALL SAFETY | Let water pass under timber walls without creating another walkable entrance. |
| [MountainRiver.bridge](../src/world/MountainRiver.java#L134) | SAFE CROSSING | Build one three-cell-wide timber crossing only over exterior water. |

## src/world/Palisade.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Palisade.Palisade](../src/world/Palisade.java#L27) | WORLD DEFENSE | Stamp protected edges and create four gateways aligned with district roads. |
| [Palisade.gates](../src/world/Palisade.java#L56) | OOP - ENCAPSULATION | Expose the stable, unmodifiable four-gate list. |
| [Palisade.openPassageNear](../src/world/Palisade.java#L58) | DSA - ARRAY LOOKUP | Use precise center clearance only at an open gateway and its landing. |
| [Palisade.collect](../src/world/Palisade.java#L64) | RENDER CULLING | Append only boundary pieces intersecting the current viewport. |
| [Palisade.nearest](../src/world/Palisade.java#L68) | INTERACTION SEARCH | Find a gateway close enough to operate from either side. |
| [Palisade.update](../src/world/Palisade.java#L79) | SIMULATION | Advance gate feedback only during active simulation. |
| [Palisade.drawPrompt](../src/world/Palisade.java#L81) | CONTEXT PROMPT | Show an E action only beside an operable gateway. |
| [Piece.Piece](../src/world/Palisade.java#L98) | CACHED SCENERY | Store immutable sprite placement and depth. |
| [Piece.image](../src/world/Palisade.java#L100) | OOP CONTRACT | Select the already cached sprite for this piece. |
| [Piece.sortY](../src/world/Palisade.java#L102) | DEPTH SORTING | Return the ground-plane depth. |
| [Piece.draw](../src/world/Palisade.java#L104) | CACHED RENDER | Blit existing artwork without allocating images or changing state. |
| [Wall.Wall](../src/world/Palisade.java#L109) | CACHED SCENERY | Bind one wooden segment to a shared texture. |
| [Wall.image](../src/world/Palisade.java#L111) | OOP OVERRIDE | Return this static segment's texture. |
| [Gate.Gate](../src/world/Palisade.java#L118) | GATE GEOMETRY | Match the blocking leaf to the visible opening, not the whole outside path. |
| [Gate.open](../src/world/Palisade.java#L123) | OOP ENCAPSULATION | Read gate state without exposing a writable flag. |
| [Gate.toggle](../src/world/Palisade.java#L125) | GATE TRANSITION | Change the leaf state and clear short feedback. |
| [Gate.occupied](../src/world/Palisade.java#L127) | SAFE CLOSURE | Refuse closure while any living actor occupies the leaf's safety margin. |
| [Gate.refuse](../src/world/Palisade.java#L135) | GATE FEEDBACK | Keep the gate open and briefly explain an occupied threshold. |
| [Gate.collider](../src/world/Palisade.java#L137) | DYNAMIC OBSTACLE | Reuse indexed leaf cells; an open gate has an empty collider. |
| [Gate.image](../src/world/Palisade.java#L139) | OOP OVERRIDE | Select an orientation/state cache without repainting artwork. |
| [Palisade.gateBounds](../src/world/Palisade.java#L142) | SPRITE PLACEMENT | Align gate artwork to the physical leaf and adjacent wall posts. |
| [Palisade.wallArt](../src/world/Palisade.java#L147) | WOOD ART CACHE | Build textured pointed logs, tied rails and their shadows once. |
| [Palisade.timber](../src/world/Palisade.java#L164) | WOOD DETAIL | Shade one pointed timber with grain, a knot and a dark base. |
| [Palisade.gateArt](../src/world/Palisade.java#L173) | GATE ART CACHE | Cache closed braced leaves and folded-open leaves in both orientations. |
| [Palisade.color](../src/world/Palisade.java#L201) | PALETTE | Select an opaque wood palette color while building caches. |

## src/world/VillageMap.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [VillageMap.VillageMap](../src/world/VillageMap.java#L27) | FUNCTION | Initialize VillageMap state. |
| [VillageMap.cacheX](../src/world/VillageMap.java#L65) | FUNCTION | Handle cache x for VillageMap. |
| [VillageMap.cacheY](../src/world/VillageMap.java#L67) | FUNCTION | Handle cache y for VillageMap. |
| [VillageMap.draw](../src/world/VillageMap.java#L69) | FUNCTION | Render draw using the current graphics state. |
| [VillageMap.label](../src/world/VillageMap.java#L116) | MAP WAYFINDING | Keep remote clearing and mountain labels inside the full-map viewport. |

## src/world/World.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [World.collider](../src/world/World.java#L50) | OOP CONTRACT | Expose a static collision footprint through the Obstacle contract. |
| [World.updateAmbient](../src/world/World.java#L57) | FUNCTION | Advance update ambient state with elapsed time. |
| [World.World](../src/world/World.java#L60) | FUNCTION | Initialize World state. |
| [World.villageCell](../src/world/World.java#L138) | WORLD COORDINATES | Translate one authored village column or row into the expanded world. |
| [World.shifted](../src/world/World.java#L140) | WORLD COORDINATES / ARRAYS | Copy authored coordinates into the village origin without changing style IDs. |
| [World.inVillage](../src/world/World.java#L146) | REGION BOUNDS | Identify the protected interior inside the wooden perimeter. |
| [World.dryFootprint](../src/world/World.java#L150) | TERRAIN CLEARANCE | Reject a tree base that would grow into water or the wooden perimeter. |
| [World.rebuildNavigation](../src/world/World.java#L156) | DSA - NAVIGATION CACHE | Refresh collision-aware nodes only when the world or a gate changes. |
| [World.navigationVersion](../src/world/World.java#L166) | CACHE INVALIDATION | Identify a changed gate/navigation layout for shared BFS and A* caches. |
| [World.interactGate](../src/world/World.java#L168) | SAFE GATE INTERACTION | Toggle a nearby leaf after checking player state and every living actor in its threshold. |
| [World.bridgeApproach](../src/world/World.java#L175) | FUNCTION | Handle bridge approach for World. |
| [World.placeProp](../src/world/World.java#L181) | FUNCTION | Handle place prop for World. |
| [World.nearLandmark](../src/world/World.java#L193) | FUNCTION | Handle near landmark for World. |
| [World.rect](../src/world/World.java#L199) | FUNCTION | Handle rect for World. |
| [World.roadAt](../src/world/World.java#L204) | FUNCTION | Handle road at for World. |
| [World.farms](../src/world/World.java#L206) | OOP: ENCAPSULATION | Expose an unmodifiable snapshot of the farm list. |
| [World.intersectsStreet](../src/world/World.java#L208) | FUNCTION | Handle intersects street for World. |
| [World.streetClear](../src/world/World.java#L215) | FUNCTION | Handle street clear for World. |
| [World.streetRoute](../src/world/World.java#L226) | DSA: BFS / FIFO QUEUE | Find the shortest valid orthogonal street route using a predecessor array. |
| [World.street](../src/world/World.java#L247) | FUNCTION | Handle street for World. |
| [World.connectStreet](../src/world/World.java#L251) | FUNCTION | Handle connect street for World. |
| [World.pave](../src/world/World.java#L255) | FUNCTION | Handle pave for World. |
| [World.court](../src/world/World.java#L267) | FUNCTION | Handle court for World. |
| [World.farm](../src/world/World.java#L274) | FUNCTION | Handle farm for World. |
| [World.cacheTerrainEdges](../src/world/World.java#L285) | DSA: 2D ARRAY CACHE | Precompute terrain edge masks once per world. |
| [World.pond](../src/world/World.java#L298) | FUNCTION | Handle pond for World. |
| [World.index](../src/world/World.java#L305) | DSA: SPATIAL GRID | Place each collider into only the uniform-grid buckets it overlaps. |
| [World.col](../src/world/World.java#L312) | FUNCTION | Handle col for World. |
| [World.row](../src/world/World.java#L314) | FUNCTION | Handle row for World. |
| [World.inside](../src/world/World.java#L316) | FUNCTION | Handle inside for World. |
| [World.at](../src/world/World.java#L318) | FUNCTION | Handle at for World. |
| [World.solidTile](../src/world/World.java#L320) | FUNCTION | Handle solid tile for World. |
| [World.walkable](../src/world/World.java#L322) | FUNCTION | Handle walkable for World. |
| [World.blocked](../src/world/World.java#L325) | COLLISION: INDEXED SEARCH | Query solid terrain and nearby bucket colliders. |
| [World.blockedBruteForce](../src/world/World.java#L338) | VERIFICATION: REFERENCE SEARCH | Check every obstacle for comparison with the spatial index. |
| [World.collectLandscape](../src/world/World.java#L349) | LANDSCAPE CULLING | Append visible timber defenses and the cached mountain/waterfall to depth sorting. |
| [World.drawGround](../src/world/World.java#L353) | FUNCTION | Render draw ground using the current graphics state. |
| [World.Prop](../src/world/World.java#L384) | FUNCTION | Initialize Prop state. |
| [World.collider](../src/world/World.java#L388) | FUNCTION | Expose a static collision footprint through the Obstacle contract. |
| [World.sortY](../src/world/World.java#L390) | FUNCTION | Handle sort y for World. |
| [World.draw](../src/world/World.java#L392) | FUNCTION | Render draw using the current graphics state. |
| [World.Building](../src/world/World.java#L411) | FUNCTION | Initialize Building state. |
| [World.collider](../src/world/World.java#L417) | FUNCTION | Expose a static collision footprint through the Obstacle contract. |
| [World.sortY](../src/world/World.java#L419) | FUNCTION | Handle sort y for World. |
| [World.draw](../src/world/World.java#L421) | FUNCTION | Render draw using the current graphics state. |
| [World.Tree](../src/world/World.java#L434) | ASSET-ALIGNED COLLISION | Cache the sprite/style and place a broad solid root footprint on its actual bark. |
| [World.collider](../src/world/World.java#L440) | STATIC OBSTACLE | Return the cached tree-root collider used by all actors. |
| [World.sortY](../src/world/World.java#L442) | DEPTH SORTING | Sort at the actual roots rather than stray grass below the sprite. |
| [World.draw](../src/world/World.java#L444) | CACHED RENDER | Draw an unoccluded tree for scene exports and generic Drawable callers. |
| [World.draw](../src/world/World.java#L446) | PLAYER VISIBILITY | Fade only the canopy when it hides Agyu; the root/trunk base remains opaque and solid. |

## tests/ArenaBossTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ArenaBossTests.ArenaBossTests](../tests/ArenaBossTests.java#L24) | TEST UTILITY | Prevent construction of this focused regression suite. |
| [ArenaBossTests.main](../tests/ArenaBossTests.java#L26) | TEST ENTRY | Load the current runtime sprites and run the boss suite independently. |
| [ArenaBossTests.run](../tests/ArenaBossTests.java#L28) | REGRESSION SUITE | Verify assets, encounter assignment, attacks, defenses, lifecycle and actual camera rendering. |
| [ArenaBossTests.assets](../tests/ArenaBossTests.java#L32) | CACHED AUTHORED ART | Check every supplied directional animation is nonempty, transparent and stable on lookup. |
| [ArenaBossTests.population](../tests/ArenaBossTests.java#L50) | POLYMORPHIC POPULATION | Verify exact Tank/Mage/Fighter mapping and outdoor, collision-free placement. |
| [ArenaBossTests.spear](../tests/ArenaBossTests.java#L67) | REAL WEAPON INTEGRATION | Drive mouse attacks through Player and the swept spear against every villain hurt box. |
| [ArenaBossTests.tank](../tests/ArenaBossTests.java#L79) | TANK COUNTERPLAY | Exercise a warned cleave, frontal shield/dash protection and a jumpable radial slam. |
| [ArenaBossTests.mage](../tests/ArenaBossTests.java#L96) | MAGE HAZARDS | Verify delayed real projectiles, solid-terrain blocking, bounded hazards and disengagement cleanup. |
| [ArenaBossTests.fighter](../tests/ArenaBossTests.java#L116) | FIGHTER SWEPT COLLISION | Verify fast sword damage and a locked flame charge stopped by a thin wall. |
| [ArenaBossTests.lifecycle](../tests/ArenaBossTests.java#L134) | DEATH / READ-ONLY RENDER | Verify exact-once claims, complete death timing, and graphics/cache state preservation. |
| [ArenaBossTests.session](../tests/ArenaBossTests.java#L147) | SESSION PROGRESSION | Exercise paused attacks, one-time boss rewards, live map state and fresh-session reset. |
| [ArenaBossTests.previews](../tests/ArenaBossTests.java#L171) | ACTUAL CAMERA EVIDENCE | Render all three boss encounters through the real game camera and HUD. |
| [ArenaBossTests.bodyHash](../tests/ArenaBossTests.java#L193) | RENDER FINGERPRINT | Render warnings/body while verifying the incoming graphics state is restored. |
| [ArenaBossTests.mapHash](../tests/ArenaBossTests.java#L202) | LIVE MAP FINGERPRINT | Compare changing boss markers without mutating the cached map layer. |
| [ArenaBossTests.hash](../tests/ArenaBossTests.java#L204) | PIXEL FINGERPRINT | Compare a whole rendered image without inflating assertion counts by pixel. |
| [ArenaBossTests.near](../tests/ArenaBossTests.java#L206) | COMBAT FIXTURE | Place a fresh player on dry central arena ground in front of a fresh boss. |
| [ArenaBossTests.step](../tests/ArenaBossTests.java#L208) | SIMULATION FIXTURE | Advance the actual boss state machine once while keeping transient effects isolated. |
| [ArenaBossTests.ticks](../tests/ArenaBossTests.java#L210) | SIMULATION FIXTURE | Run a fixed number of real boss updates. |
| [ArenaBossTests.until](../tests/ArenaBossTests.java#L212) | STATE WAIT | Advance until a public boss/health condition holds or report a finite timeout. |
| [ArenaBossTests.tap](../tests/ArenaBossTests.java#L214) | INPUT FIXTURE | Route one fresh key press through GamePanel's normal input path. |
| [ArenaBossTests.get](../tests/ArenaBossTests.java#L216) | TEST REFLECTION | Inspect private session state without changing the runtime API. |
| [ArenaBossTests.set](../tests/ArenaBossTests.java#L218) | CAMERA FIXTURE | Position the real interpolation camera for deterministic visual review. |
| [ArenaBossTests.check](../tests/ArenaBossTests.java#L220) | ASSERTION | Count a meaningful regression property and explain any failure. |

## tests/BoarTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [BoarTests.BoarTests](../tests/BoarTests.java#L25) | TEST UTILITY | Prevent instances of the focused regression suite. |
| [BoarTests.main](../tests/BoarTests.java#L27) | TEST ENTRY | Load real assets and run the focused forest-wildlife checks. |
| [BoarTests.run](../tests/BoarTests.java#L29) | REGRESSION SUITE | Verify sprite arrays, roaming, combat, death, population replacement and a real forest camera. |
| [BoarTests.art](../tests/BoarTests.java#L31) | ASSET CONTRACT | Verify all supplied source poses, directional caches, opaque feet registration and untouched rendering inputs. |
| [BoarTests.population](../tests/BoarTests.java#L55) | POLYMORPHIC POPULATION | Confirm every ordinary initial and replenished enemy is an outdoor boar while all villains remain. |
| [BoarTests.roaming](../tests/BoarTests.java#L65) | LINKED-LIST ROAMING | Simulate a full minute of free forest patrol and verify movement, root clearance and habitat boundaries. |
| [BoarTests.charges](../tests/BoarTests.java#L76) | COMMITTED CHARGE | Check a readable windup, one charge hit, all four facing choices and spear interruption. |
| [BoarTests.defenses](../tests/BoarTests.java#L91) | PLAYER COUNTERPLAY | Use real guard and dash input and verify that a grounded charge can be jumped. |
| [BoarTests.collision](../tests/BoarTests.java#L107) | SWEPT COLLISION | Reject coarse charged travel through a real forest tree and forced travel into the village. |
| [BoarTests.death](../tests/BoarTests.java#L123) | DEATH LIFECYCLE | Check one-time rewards and a non-looping five-frame corpse before cleanup. |
| [BoarTests.boarAt](../tests/BoarTests.java#L130) | FREE CLEARING FIXTURE | Use the actual reserved arena floor for deterministic combat geometry. |
| [BoarTests.waitFor](../tests/BoarTests.java#L132) | STATE WAIT FIXTURE | Advance real simulation only until the requested warning/charge state, failing on a stalled encounter. |
| [BoarTests.set](../tests/BoarTests.java#L134) | FOCUSED FIXTURE | Set one private phase/vector only for the coarse collision regression. |
| [BoarTests.preview](../tests/BoarTests.java#L136) | RENDER EVIDENCE | Export a real forest camera with ground warnings, depth sorting, Agyu and the authored boars. |
| [BoarTests.check](../tests/BoarTests.java#L156) | ASSERTION | Count a behavior contract and report the exact failure. |

## tests/CombatBodyTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [CombatBodyTests.CombatBodyTests](../tests/CombatBodyTests.java#L27) | TEST HELPER | Prevent instances of this test/helper class. |
| [CombatBodyTests.main](../tests/CombatBodyTests.java#L29) | TEST HELPER | Entry point for this regression suite. |
| [CombatBodyTests.run](../tests/CombatBodyTests.java#L37) | TEST HELPER | Handle run for CombatBodyTests. |
| [CombatBodyTests.assetChecks](../tests/CombatBodyTests.java#L50) | TEST HELPER | Handle asset checks for CombatBodyTests. |
| [CombatBodyTests.phaseChecks](../tests/CombatBodyTests.java#L103) | TEST HELPER | Handle phase checks for CombatBodyTests. |
| [CombatBodyTests.weaponPathChecks](../tests/CombatBodyTests.java#L122) | TEST HELPER | Handle weapon path checks for CombatBodyTests. |
| [CombatBodyTests.pivotAndRenderChecks](../tests/CombatBodyTests.java#L143) | TEST HELPER | Handle pivot and render checks for CombatBodyTests. |
| [CombatBodyTests.ghostChecks](../tests/CombatBodyTests.java#L178) | TEST HELPER | Handle ghost checks for CombatBodyTests. |
| [CombatBodyTests.previews](../tests/CombatBodyTests.java#L199) | TEST HELPER | Handle previews for CombatBodyTests. |
| [CombatBodyTests.animation](../tests/CombatBodyTests.java#L227) | TEST HELPER | Handle animation for CombatBodyTests. |
| [CombatBodyTests.time](../tests/CombatBodyTests.java#L273) | TEST HELPER | Handle time for CombatBodyTests. |
| [CombatBodyTests.actor](../tests/CombatBodyTests.java#L279) | TEST HELPER | Handle actor for CombatBodyTests. |
| [CombatBodyTests.spear](../tests/CombatBodyTests.java#L283) | TEST HELPER | Handle spear for CombatBodyTests. |
| [CombatBodyTests.field](../tests/CombatBodyTests.java#L285) | TEST HELPER | Handle field for CombatBodyTests. |
| [CombatBodyTests.render](../tests/CombatBodyTests.java#L289) | TEST HELPER | Handle render for CombatBodyTests. |
| [CombatBodyTests.skin](../tests/CombatBodyTests.java#L294) | TEST HELPER | Handle skin for CombatBodyTests. |
| [CombatBodyTests.silhouetteDifference](../tests/CombatBodyTests.java#L299) | TEST HELPER | Handle silhouette difference for CombatBodyTests. |
| [CombatBodyTests.same](../tests/CombatBodyTests.java#L305) | TEST HELPER | Handle same for CombatBodyTests. |
| [CombatBodyTests.check](../tests/CombatBodyTests.java#L311) | TEST HELPER | Handle check for CombatBodyTests. |

## tests/CombatTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [CombatTests.CombatTests](../tests/CombatTests.java#L28) | TEST HELPER | Prevent instances of this test/helper class. |
| [CombatTests.main](../tests/CombatTests.java#L30) | TEST HELPER | Entry point for this regression suite. |
| [CombatTests.run](../tests/CombatTests.java#L34) | TEST HELPER | Handle run for CombatTests. |
| [CombatTests.cyan](../tests/CombatTests.java#L133) | TEST HELPER | Handle cyan for CombatTests. |
| [CombatTests.renderAttack](../tests/CombatTests.java#L138) | TEST HELPER | Handle render attack for CombatTests. |
| [CombatTests.drawActor](../tests/CombatTests.java#L149) | TEST HELPER | Render draw actor using the current graphics state. |
| [CombatTests.previews](../tests/CombatTests.java#L160) | TEST HELPER | Handle previews for CombatTests. |
| [CombatTests.press](../tests/CombatTests.java#L208) | TEST HELPER | Handle press for CombatTests. |
| [CombatTests.check](../tests/CombatTests.java#L212) | TEST HELPER | Handle check for CombatTests. |

## tests/ComboTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ComboTests.ComboTests](../tests/ComboTests.java#L32) | TEST HELPER | Prevent instances of this test/helper class. |
| [ComboTests.Target](../tests/ComboTests.java#L35) | TEST HELPER | Initialize Target state. |
| [ComboTests.kind](../tests/ComboTests.java#L37) | TEST SPRITE POLICY | Select the current boar appearance for the stationary combat target. |
| [ComboTests.speed](../tests/ComboTests.java#L39) | TEST HELPER | Handle speed for ComboTests. |
| [ComboTests.contactDamage](../tests/ComboTests.java#L41) | TEST HELPER | Handle contact damage for ComboTests. |
| [ComboTests.update](../tests/ComboTests.java#L43) | TEST HELPER | Advance update state with elapsed time. |
| [ComboTests.main](../tests/ComboTests.java#L49) | TEST HELPER | Entry point for this regression suite. |
| [ComboTests.run](../tests/ComboTests.java#L57) | TEST HELPER | Handle run for ComboTests. |
| [ComboTests.chainChecks](../tests/ComboTests.java#L70) | TEST HELPER | Handle chain checks for ComboTests. |
| [ComboTests.cancelChecks](../tests/ComboTests.java#L123) | TEST HELPER | Handle cancel checks for ComboTests. |
| [ComboTests.damageChecks](../tests/ComboTests.java#L151) | TEST HELPER | Handle damage checks for ComboTests. |
| [ComboTests.renderChecks](../tests/ComboTests.java#L180) | TEST HELPER | Handle render checks for ComboTests. |
| [ComboTests.effectsChecks](../tests/ComboTests.java#L209) | TEST HELPER | Handle effects checks for ComboTests. |
| [ComboTests.previews](../tests/ComboTests.java#L237) | TEST HELPER | Handle previews for ComboTests. |
| [ComboTests.clip](../tests/ComboTests.java#L304) | TEST HELPER | Handle clip for ComboTests. |
| [ComboTests.thirdStage](../tests/ComboTests.java#L338) | TEST HELPER | Handle third stage for ComboTests. |
| [ComboTests.waitFor](../tests/ComboTests.java#L344) | TEST HELPER | Handle wait for for ComboTests. |
| [ComboTests.waitUntilIdle](../tests/ComboTests.java#L352) | TEST HELPER | Handle wait until idle for ComboTests. |
| [ComboTests.attack](../tests/ComboTests.java#L357) | TEST HELPER | Handle attack for ComboTests. |
| [ComboTests.settle](../tests/ComboTests.java#L362) | TEST HELPER | Handle settle for ComboTests. |
| [ComboTests.step](../tests/ComboTests.java#L364) | TEST HELPER | Handle step for ComboTests. |
| [ComboTests.player](../tests/ComboTests.java#L366) | TEST HELPER | Handle player for ComboTests. |
| [ComboTests.posePlayer](../tests/ComboTests.java#L368) | TEST HELPER | Handle pose player for ComboTests. |
| [ComboTests.spear](../tests/ComboTests.java#L372) | TEST HELPER | Handle spear for ComboTests. |
| [ComboTests.render](../tests/ComboTests.java#L376) | TEST HELPER | Handle render for ComboTests. |
| [ComboTests.same](../tests/ComboTests.java#L381) | TEST HELPER | Handle same for ComboTests. |
| [ComboTests.fingerprint](../tests/ComboTests.java#L387) | TEST HELPER | Handle fingerprint for ComboTests. |
| [ComboTests.press](../tests/ComboTests.java#L393) | TEST HELPER | Handle press for ComboTests. |
| [ComboTests.release](../tests/ComboTests.java#L395) | TEST HELPER | Handle release for ComboTests. |
| [ComboTests.mouse](../tests/ComboTests.java#L397) | TEST HELPER | Handle mouse for ComboTests. |
| [ComboTests.check](../tests/ComboTests.java#L402) | TEST HELPER | Handle check for ComboTests. |

## tests/DayNightTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [DayNightTests.DayNightTests](../tests/DayNightTests.java#L25) | TEST UTILITY | Prevent construction of this focused suite. |
| [DayNightTests.main](../tests/DayNightTests.java#L27) | TEST ENTRY | Load the real assets and run the focused cycle checks. |
| [DayNightTests.run](../tests/DayNightTests.java#L29) | REGRESSION SUITE | Verify timing, actual damage/movement, spawns, session controls and lighting. |
| [DayNightTests.clock](../tests/DayNightTests.java#L31) | THREE-MINUTE BOUNDARIES | Verify numbered phases, exact fixed-step crossings, coarse steps and continuous light transitions. |
| [DayNightTests.phase](../tests/DayNightTests.java#L53) | PHASE FIXTURE | Advance to the next requested phase while retaining the real production interval. |
| [DayNightTests.strength](../tests/DayNightTests.java#L55) | CONTACT / PURSUIT | Compare real day/night contact hits and chase travel, including dawn and village exclusion. |
| [DayNightTests.bosses](../tests/DayNightTests.java#L77) | BOSS NIGHT ATTACKS | Exercise all villain hit paths  without changing warning times or health. |
| [DayNightTests.boss](../tests/DayNightTests.java#L101) | ENCOUNTER FACTORY | Build a fresh concrete villain in its real clearing. |
| [DayNightTests.warningDuration](../tests/DayNightTests.java#L103) | WARNING QUERY | Inspect an existing private timing value without modifying attack policy. |
| [DayNightTests.spawns](../tests/DayNightTests.java#L105) | NIGHT SPAWNS | Verify freshly populated and replenished outdoor enemies inherit the current phase once. |
| [DayNightTests.session](../tests/DayNightTests.java#L115) | SESSION CLOCK | Check real start, pause, map, journal, focus, hit-stop, defeat and restart timing. |
| [DayNightTests.lighting](../tests/DayNightTests.java#L133) | LIGHTING CACHE | Check scene tint, local visibility, cache reuse and graphics/simulation preservation. |
| [DayNightTests.lightFrame](../tests/DayNightTests.java#L148) | LIGHTING FIXTURE | Shade a known scene color to compare actual visibility pixels. |
| [DayNightTests.luma](../tests/DayNightTests.java#L150) | BRIGHTNESS QUERY | Compare scene brightness without relying on one palette shade. |
| [DayNightTests.previews](../tests/DayNightTests.java#L152) | ACTUAL SCENE PREVIEWS | Export the same village camera by day/night and a powered night boss encounter. |
| [DayNightTests.camera](../tests/DayNightTests.java#L172) | CAMERA FIXTURE | Freeze the real interpolated camera at a known scene. |
| [DayNightTests.render](../tests/DayNightTests.java#L174) | ACTUAL RENDER | Paint the GamePanel rather than a simplified replacement scene. |
| [DayNightTests.hash](../tests/DayNightTests.java#L176) | PIXEL FINGERPRINT | Compare a whole rendered image without inflating assertion counts by pixel. |
| [DayNightTests.until](../tests/DayNightTests.java#L178) | SIMULATION WAIT | Advance real boss updates to a bounded observable condition. |
| [DayNightTests.tap](../tests/DayNightTests.java#L180) | INPUT FIXTURE | Dispatch a fresh key edge through the real panel input. |
| [DayNightTests.get](../tests/DayNightTests.java#L182) | TEST QUERY | Inspect private cache/session state without widening production APIs. |
| [DayNightTests.getFrom](../tests/DayNightTests.java#L184) | TEST QUERY | Read a private field declared by an explicit class. |
| [DayNightTests.set](../tests/DayNightTests.java#L186) | CAMERA / ATTACK FIXTURE | Position the actual camera or an existing guardian attack timer. |
| [DayNightTests.close](../tests/DayNightTests.java#L188) | FLOATING-POINT CHECK | Compare elapsed-time and movement values at simulation precision. |
| [DayNightTests.check](../tests/DayNightTests.java#L190) | ASSERTION | Count a meaningful property and describe its failure. |

## tests/EnergyDashTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [EnergyDashTests.EnergyDashTests](../tests/EnergyDashTests.java#L26) | TEST HELPER | Initialize EnergyDashTests state. |
| [EnergyDashTests.main](../tests/EnergyDashTests.java#L28) | TEST HELPER | Entry point for this regression suite. |
| [EnergyDashTests.run](../tests/EnergyDashTests.java#L32) | TEST HELPER | Handle run for EnergyDashTests. |
| [EnergyDashTests.inputChecks](../tests/EnergyDashTests.java#L39) | TEST HELPER | Handle input checks for EnergyDashTests. |
| [EnergyDashTests.meterChecks](../tests/EnergyDashTests.java#L56) | TEST HELPER | Handle meter checks for EnergyDashTests. |
| [EnergyDashTests.sprintChecks](../tests/EnergyDashTests.java#L80) | TEST HELPER | Handle sprint checks for EnergyDashTests. |
| [EnergyDashTests.dashChecks](../tests/EnergyDashTests.java#L115) | TEST HELPER | Handle dash checks for EnergyDashTests. |
| [EnergyDashTests.collisionChecks](../tests/EnergyDashTests.java#L156) | TEST HELPER | Handle collision checks for EnergyDashTests. |
| [EnergyDashTests.priorityChecks](../tests/EnergyDashTests.java#L187) | TEST HELPER | Handle priority checks for EnergyDashTests. |
| [EnergyDashTests.effectChecks](../tests/EnergyDashTests.java#L227) | TEST HELPER | Handle effect checks for EnergyDashTests. |
| [EnergyDashTests.panelChecks](../tests/EnergyDashTests.java#L252) | TEST HELPER | Handle panel checks for EnergyDashTests. |
| [EnergyDashTests.hudAndPreview](../tests/EnergyDashTests.java#L287) | TEST HELPER | Handle hud and preview for EnergyDashTests. |
| [EnergyDashTests.home](../tests/EnergyDashTests.java#L313) | TEST HELPER | Handle home for EnergyDashTests. |
| [EnergyDashTests.player](../tests/EnergyDashTests.java#L317) | TEST HELPER | Handle player for EnergyDashTests. |
| [EnergyDashTests.close](../tests/EnergyDashTests.java#L319) | TEST HELPER | Handle close for EnergyDashTests. |
| [EnergyDashTests.step](../tests/EnergyDashTests.java#L321) | TEST HELPER | Handle step for EnergyDashTests. |
| [EnergyDashTests.direction](../tests/EnergyDashTests.java#L325) | TEST HELPER | Handle direction for EnergyDashTests. |
| [EnergyDashTests.tap](../tests/EnergyDashTests.java#L332) | TEST HELPER | Handle tap for EnergyDashTests. |
| [EnergyDashTests.field](../tests/EnergyDashTests.java#L336) | TEST HELPER | Handle field for EnergyDashTests. |
| [EnergyDashTests.press](../tests/EnergyDashTests.java#L340) | TEST HELPER | Handle press for EnergyDashTests. |
| [EnergyDashTests.release](../tests/EnergyDashTests.java#L344) | TEST HELPER | Handle release for EnergyDashTests. |
| [EnergyDashTests.mouse](../tests/EnergyDashTests.java#L348) | TEST HELPER | Handle mouse for EnergyDashTests. |
| [EnergyDashTests.check](../tests/EnergyDashTests.java#L354) | TEST HELPER | Handle check for EnergyDashTests. |

## tests/EquipmentTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [EquipmentTests.EquipmentTests](../tests/EquipmentTests.java#L22) | STATIC TEST | Prevent instances of the regression suite. |
| [EquipmentTests.main](../tests/EquipmentTests.java#L24) | TEST ENTRY | Run equipment and drinking checks independently. |
| [EquipmentTests.run](../tests/EquipmentTests.java#L26) | EQUIPMENT SUITE | Verify registered art, sip semantics, input interruptions and actual render previews. |
| [EquipmentTests.art](../tests/EquipmentTests.java#L28) | CACHED ART / FORWARD GUARD | Check provenance, unclipped composites, mirrored guard tips and pose variation. |
| [EquipmentTests.drinks](../tests/EquipmentTests.java#L48) | SIP COMMIT RULES | Start, raise, sip and lower once without premature resource changes or double spending. |
| [EquipmentTests.interruptions](../tests/EquipmentTests.java#L62) | INTERRUPTION / INPUT PRIORITY | Cancel an uncommitted bottle on guard, hit, slot change, jump or dash. |
| [EquipmentTests.selection](../tests/EquipmentTests.java#L76) | SELECTED EQUIPMENT | Keep actual held items aligned with slots, depleted supplies, guard and movement. |
| [EquipmentTests.panel](../tests/EquipmentTests.java#L91) | ACTUAL SESSION PAUSE | Freeze drinking progress during modal screens and preserve selected equipment after restart. |
| [EquipmentTests.previews](../tests/EquipmentTests.java#L116) | ACTUAL PLAYER VISUALS | Export all held items and six real drinking phases in every direction. |
| [EquipmentTests.cell](../tests/EquipmentTests.java#L130) | REGISTERED PREVIEW CELL | Render the actual Player with the normal scale and item compositor in a labeled comparison cell. |
| [EquipmentTests.imageHash](../tests/EquipmentTests.java#L134) | RENDER DIFFERENCE | Compare complete RGBA images without relying on subjective pose labels. |
| [EquipmentTests.actor](../tests/EquipmentTests.java#L136) | PLAYER FIXTURE | Create a fresh Agyu at the reachable village spawn. |
| [EquipmentTests.tick](../tests/EquipmentTests.java#L138) | SIMULATION FIXTURE | Advance actual player logic and settle interpolation for visual evidence. |
| [EquipmentTests.tap](../tests/EquipmentTests.java#L140) | INPUT FIXTURE | Deliver a real keyboard press and release to the session. |
| [EquipmentTests.get](../tests/EquipmentTests.java#L142) | SESSION TEST ACCESS | Inspect private state without adding production mutation APIs. |
| [EquipmentTests.set](../tests/EquipmentTests.java#L144) | CAMERA FIXTURE | Register the actual game camera immediately for the boss layout preview. |
| [EquipmentTests.check](../tests/EquipmentTests.java#L146) | ASSERTION | Count a behavioral property and explain its failure. |

## tests/ExpansionTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ExpansionTests.ExpansionTests](../tests/ExpansionTests.java#L28) | TEST UTILITY | Prevent instances of the focused regression suite. |
| [ExpansionTests.main](../tests/ExpansionTests.java#L30) | TEST ENTRY | Load the actual artwork and run expansion checks independently. |
| [ExpansionTests.run](../tests/ExpansionTests.java#L32) | REGRESSION SUITE | Verify remote arenas, the river and animation before exporting actual game evidence. |
| [ExpansionTests.arenas](../tests/ExpansionTests.java#L34) | BOSS ARENA SPACE | Check three large, distant and usable spaces connected to the village gates. |
| [ExpansionTests.river](../tests/ExpansionTests.java#L75) | HYDRAULIC BFS | Follow one connected watercourse through bridges and protected wall culverts to its outlet. |
| [ExpansionTests.wet](../tests/ExpansionTests.java#L103) | WATER CLASSIFICATION | Include water passing below bridges and palisade culverts in the hydraulic graph. |
| [ExpansionTests.scenery](../tests/ExpansionTests.java#L105) | CACHE / RENDER | Check large cached mountain art, distinct water phases, culling and read-only drawing. |
| [ExpansionTests.mountainFrame](../tests/ExpansionTests.java#L134) | OFFSCREEN SCENERY | Render the real mountain and waterfall into a compact local camera. |
| [ExpansionTests.previews](../tests/ExpansionTests.java#L139) | RENDER EVIDENCE | Export a complete mountain view and the actual waterfall-side game camera. |
| [ExpansionTests.hash](../tests/ExpansionTests.java#L175) | PIXEL FINGERPRINT | Compare rendered cache contents without counting every pixel as a separate assertion. |
| [ExpansionTests.tap](../tests/ExpansionTests.java#L177) | INPUT FIXTURE | Dispatch one real key edge into the game panel. |
| [ExpansionTests.get](../tests/ExpansionTests.java#L179) | TEST REFLECTION | Read session state without widening runtime interfaces. |
| [ExpansionTests.set](../tests/ExpansionTests.java#L181) | CAMERA FIXTURE | Position the real interpolated camera for review renders. |
| [ExpansionTests.check](../tests/ExpansionTests.java#L183) | ASSERTION | Count one meaningful property and explain any failure. |

## tests/ForestRevisionTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ForestRevisionTests.ForestRevisionTests](../tests/ForestRevisionTests.java#L20) | TEST UTILITY | Prevent instances of this focused suite. |
| [ForestRevisionTests.main](../tests/ForestRevisionTests.java#L22) | TEST ENTRY | Load the real assets and run the forest revision checks. |
| [ForestRevisionTests.run](../tests/ForestRevisionTests.java#L24) | REGRESSION SUITE | Verify distribution over multiple seeds, removed live map markers, and plain spawn/clearing interactions. |
| [ForestRevisionTests.distribution](../tests/ForestRevisionTests.java#L26) | RANDOM HERD CONTRACT | Check seeded repeatability, different journeys, one-to-four-member herds and broad map coverage. |
| [ForestRevisionTests.observer](../tests/ForestRevisionTests.java#L49) | OFFSCREEN OBSERVER FIXTURE | Find real free forest ground well away from every herd for respawn checks. |
| [ForestRevisionTests.markers](../tests/ForestRevisionTests.java#L59) | LIVE MAP MARKER REMOVAL | Compare old beacon positions with the cached map terrain while retaining the actual player/boss markers elsewhere. |
| [ForestRevisionTests.interactions](../tests/ForestRevisionTests.java#L68) | RETIRED INTERACTION REMOVAL | Visit every former beacon and press real E input; only normal village interactions remain. |
| [ForestRevisionTests.nightOnly](../tests/ForestRevisionTests.java#L79) | NIGHT-ONLY AMBUSH | Walk through dense woodland all day and confirm the director cannot spawn a Sigbin. |
| [ForestRevisionTests.distributionPreview](../tests/ForestRevisionTests.java#L85) | WORLD DISTRIBUTION EVIDENCE | Render the real seeded forest and annotate herd sizes only in this exported review image. |
| [ForestRevisionTests.camera](../tests/ForestRevisionTests.java#L103) | CAMERA FIXTURE | Set the actual camera for a plain spawn/clearing review screenshot. |
| [ForestRevisionTests.image](../tests/ForestRevisionTests.java#L105) | ACTUAL RENDER EXPORT | Paint the normal game surface for visual verification. |
| [ForestRevisionTests.tap](../tests/ForestRevisionTests.java#L107) | REAL KEYBOARD FIXTURE | Deliver one E/start edge through the session input path. |
| [ForestRevisionTests.get](../tests/ForestRevisionTests.java#L109) | SESSION INSPECTION | Inspect existing private state without production test hooks. |
| [ForestRevisionTests.set](../tests/ForestRevisionTests.java#L111) | CAMERA INSPECTION | Set one existing camera field for the render fixture. |
| [ForestRevisionTests.check](../tests/ForestRevisionTests.java#L113) | ASSERTION | Count a real behavior contract and identify the failure. |

## tests/ForestTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ForestTests.ForestTests](../tests/ForestTests.java#L22) | TEST UTILITY | Prevent instances of this regression suite. |
| [ForestTests.main](../tests/ForestTests.java#L24) | TEST ENTRY | Load real artwork and run the focused forest checks. |
| [ForestTests.run](../tests/ForestTests.java#L26) | REGRESSION SUITE | Verify deterministic forest construction, navigation, encounters and actual HUD rendering. |
| [ForestTests.openGates](../tests/ForestTests.java#L28) | GATE FIXTURE | Open all four actual gates using normal safe World interaction. |
| [ForestTests.landscape](../tests/ForestTests.java#L37) | WORLD / ROUTING | Test forest density, stable seeded geometry, clear trails and protected entrances. |
| [ForestTests.encounters](../tests/ForestTests.java#L74) | SPAWN LIFECYCLE | Exercise outdoor-only placement, delayed offscreen replenishment and bounded mission-safe slots. |
| [ForestTests.session](../tests/ForestTests.java#L90) | SESSION INPUT | Verify pause/map timer freezing, fresh-session reset and the removed attack card. |
| [ForestTests.previews](../tests/ForestTests.java#L119) | RENDER EVIDENCE | Export the expanded world and an actual forest combat camera without the combo panel. |
| [ForestTests.alive](../tests/ForestTests.java#L138) | TEST QUERY | Count only living members of one spawn group. |
| [ForestTests.tap](../tests/ForestTests.java#L140) | INPUT FIXTURE | Send one fresh keyboard edge through the actual panel input path. |
| [ForestTests.get](../tests/ForestTests.java#L142) | TEST REFLECTION | Inspect private session/cache state without changing the implementation API. |
| [ForestTests.set](../tests/ForestTests.java#L144) | RENDER FIXTURE | Position the real interpolated camera for deterministic scene exports. |
| [ForestTests.check](../tests/ForestTests.java#L146) | ASSERTION | Count one regression assertion and report its concrete failure. |

## tests/GameTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [GameTests.GameTests](../tests/GameTests.java#L21) | TEST HELPER | Prevent instances of this test/helper class. |
| [GameTests.main](../tests/GameTests.java#L23) | TEST HELPER | Entry point for this regression suite. |
| [GameTests.testNavigationEdges](../tests/GameTests.java#L112) | TEST HELPER | Handle test navigation edges for GameTests. |
| [GameTests.testCombat](../tests/GameTests.java#L126) | TEST HELPER | Handle test combat for GameTests. |
| [GameTests.testSprites](../tests/GameTests.java#L147) | TEST HELPER | Handle test sprites for GameTests. |
| [GameTests.testSession](../tests/GameTests.java#L197) | TEST HELPER | Handle test session for GameTests. |
| [GameTests.testPanelMouse](../tests/GameTests.java#L240) | TEST HELPER | Handle test panel mouse for GameTests. |
| [GameTests.testPanelCombos](../tests/GameTests.java#L265) | TEST HELPER | Handle test panel combos for GameTests. |
| [GameTests.mousePanel](../tests/GameTests.java#L285) | TEST HELPER | Handle mouse panel for GameTests. |
| [GameTests.field](../tests/GameTests.java#L291) | TEST HELPER | Handle field for GameTests. |
| [GameTests.render](../tests/GameTests.java#L295) | TEST HELPER | Handle render for GameTests. |
| [GameTests.tap](../tests/GameTests.java#L301) | TEST HELPER | Handle tap for GameTests. |
| [GameTests.press](../tests/GameTests.java#L305) | TEST HELPER | Handle press for GameTests. |
| [GameTests.release](../tests/GameTests.java#L307) | TEST HELPER | Handle release for GameTests. |
| [GameTests.expect](../tests/GameTests.java#L309) | TEST HELPER | Handle expect for GameTests. |
| [GameTests.check](../tests/GameTests.java#L314) | TEST HELPER | Handle check for GameTests. |

## tests/GuardVisualTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [GuardVisualTests.GuardVisualTests](../tests/GuardVisualTests.java#L16) | TEST HELPER | Prevent instances of this test/helper class. |
| [GuardVisualTests.run](../tests/GuardVisualTests.java#L18) | TEST HELPER | Handle run for GuardVisualTests. |
| [GuardVisualTests.check](../tests/GuardVisualTests.java#L79) | TEST HELPER | Handle check for GuardVisualTests. |

## tests/InventoryGuardTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [InventoryGuardTests.InventoryGuardTests](../tests/InventoryGuardTests.java#L17) | TEST UTILITY | Prevent instances of the suite. |
| [InventoryGuardTests.main](../tests/InventoryGuardTests.java#L19) | TEST ENTRY | Run guarded movement and inventory checks independently. |
| [InventoryGuardTests.run](../tests/InventoryGuardTests.java#L21) | REGRESSION SUITE | Cover cached walking, modal input and actual player use. |
| [InventoryGuardTests.steps](../tests/InventoryGuardTests.java#L23) | GUARD GAIT REGRESSION | Keep the torso steady, feet moving and guard speed limited in every facing. |
| [InventoryGuardTests.inventory](../tests/InventoryGuardTests.java#L49) | INVENTORY INTEGRATION | Exercise real keys, scaled clicks, paused clocks, reference moves and sip-time consumption. |
| [InventoryGuardTests.tap](../tests/InventoryGuardTests.java#L83) | REAL INPUT | Deliver a fresh keyboard edge and release to the live session. |
| [InventoryGuardTests.click](../tests/InventoryGuardTests.java#L85) | SCALED POINTER | Send an actual two-times window click through the listener queue. |
| [InventoryGuardTests.get](../tests/InventoryGuardTests.java#L87) | READ-ONLY TEST ACCESS | Inspect private session state without adding production test hooks. |
| [InventoryGuardTests.check](../tests/InventoryGuardTests.java#L89) | ASSERTION | Count and explain each behavioral assertion. |

## tests/JumpTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [JumpTests.JumpTests](../tests/JumpTests.java#L23) | TEST HELPER | Initialize JumpTests state. |
| [JumpTests.run](../tests/JumpTests.java#L25) | TEST HELPER | Handle run for JumpTests. |
| [JumpTests.inputChecks](../tests/JumpTests.java#L36) | TEST HELPER | Handle input checks for JumpTests. |
| [JumpTests.arcChecks](../tests/JumpTests.java#L57) | TEST HELPER | Handle arc checks for JumpTests. |
| [JumpTests.playerChecks](../tests/JumpTests.java#L76) | TEST HELPER | Handle player checks for JumpTests. |
| [JumpTests.panelChecks](../tests/JumpTests.java#L168) | TEST HELPER | Handle panel checks for JumpTests. |
| [JumpTests.preview](../tests/JumpTests.java#L217) | TEST HELPER | Handle preview for JumpTests. |
| [JumpTests.renderPlayer](../tests/JumpTests.java#L242) | TEST HELPER | Handle render player for JumpTests. |
| [JumpTests.same](../tests/JumpTests.java#L249) | TEST HELPER | Handle same for JumpTests. |
| [JumpTests.player](../tests/JumpTests.java#L255) | TEST HELPER | Handle player for JumpTests. |
| [JumpTests.step](../tests/JumpTests.java#L257) | TEST HELPER | Handle step for JumpTests. |
| [JumpTests.field](../tests/JumpTests.java#L261) | TEST HELPER | Handle field for JumpTests. |
| [JumpTests.tap](../tests/JumpTests.java#L265) | TEST HELPER | Handle tap for JumpTests. |
| [JumpTests.press](../tests/JumpTests.java#L267) | TEST HELPER | Handle press for JumpTests. |
| [JumpTests.release](../tests/JumpTests.java#L271) | TEST HELPER | Handle release for JumpTests. |
| [JumpTests.mouse](../tests/JumpTests.java#L275) | TEST HELPER | Handle mouse for JumpTests. |
| [JumpTests.check](../tests/JumpTests.java#L281) | TEST HELPER | Handle check for JumpTests. |

## tests/MouseGuardTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [MouseGuardTests.MouseGuardTests](../tests/MouseGuardTests.java#L22) | TEST HELPER | Prevent instances of this test/helper class. |
| [MouseGuardTests.main](../tests/MouseGuardTests.java#L24) | TEST HELPER | Entry point for this regression suite. |
| [MouseGuardTests.run](../tests/MouseGuardTests.java#L28) | TEST HELPER | Handle run for MouseGuardTests. |
| [MouseGuardTests.preview](../tests/MouseGuardTests.java#L108) | TEST HELPER | Handle preview for MouseGuardTests. |
| [MouseGuardTests.player](../tests/MouseGuardTests.java#L130) | TEST HELPER | Handle player for MouseGuardTests. |
| [MouseGuardTests.mouse](../tests/MouseGuardTests.java#L132) | TEST HELPER | Handle mouse for MouseGuardTests. |
| [MouseGuardTests.key](../tests/MouseGuardTests.java#L137) | TEST HELPER | Handle key for MouseGuardTests. |
| [MouseGuardTests.check](../tests/MouseGuardTests.java#L141) | TEST HELPER | Handle check for MouseGuardTests. |

## tests/NpcTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [NpcTests.NpcTests](../tests/NpcTests.java#L20) | TEST HELPER | Prevent instances of this test/helper class. |
| [NpcTests.main](../tests/NpcTests.java#L22) | TEST HELPER | Entry point for this regression suite. |
| [NpcTests.run](../tests/NpcTests.java#L24) | TEST HELPER | Handle run for NpcTests. |
| [NpcTests.art](../tests/NpcTests.java#L28) | TEST HELPER | Handle art for NpcTests. |
| [NpcTests.patrols](../tests/NpcTests.java#L49) | TEST HELPER | Handle patrols for NpcTests. |
| [NpcTests.dialogue](../tests/NpcTests.java#L71) | TEST HELPER | Handle dialogue for NpcTests. |
| [NpcTests.render](../tests/NpcTests.java#L90) | TEST HELPER | Handle render for NpcTests. |
| [NpcTests.session](../tests/NpcTests.java#L107) | TEST HELPER | Handle session for NpcTests. |
| [NpcTests.snapshot](../tests/NpcTests.java#L135) | TEST HELPER | Handle snapshot for NpcTests. |
| [NpcTests.hash](../tests/NpcTests.java#L140) | TEST HELPER | Handle hash for NpcTests. |
| [NpcTests.npcs](../tests/NpcTests.java#L142) | TEST HELPER | Handle npcs for NpcTests. |
| [NpcTests.field](../tests/NpcTests.java#L144) | TEST HELPER | Handle field for NpcTests. |
| [NpcTests.set](../tests/NpcTests.java#L146) | TEST HELPER | Handle set for NpcTests. |
| [NpcTests.tap](../tests/NpcTests.java#L148) | TEST HELPER | Handle tap for NpcTests. |
| [NpcTests.press](../tests/NpcTests.java#L150) | TEST HELPER | Handle press for NpcTests. |
| [NpcTests.check](../tests/NpcTests.java#L152) | TEST HELPER | Handle check for NpcTests. |

## tests/PotionHotbarTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [PotionHotbarTests.PotionHotbarTests](../tests/PotionHotbarTests.java#L21) | STATIC TEST | Prevent instances of the regression suite. |
| [PotionHotbarTests.main](../tests/PotionHotbarTests.java#L23) | TEST ENTRY | Run the potion and hotbar checks independently. |
| [PotionHotbarTests.run](../tests/PotionHotbarTests.java#L25) | REGRESSION SUITE | Exercise supplied artwork, resource invariants, actual input and HUD layout. |
| [PotionHotbarTests.art](../tests/PotionHotbarTests.java#L29) | ART PROVENANCE / CACHE | Verify original hashes, all supplied effect frames and small runtime RGBA icons. |
| [PotionHotbarTests.resources](../tests/PotionHotbarTests.java#L51) | RESOURCE INVARIANTS | Test useful-only spending, recovery clamps, protection precedence and death safety. |
| [PotionHotbarTests.slots](../tests/PotionHotbarTests.java#L90) | ARRAY / REARRANGEMENT | Verify wraparound, selection-preserving swaps and empty-slot hit testing. |
| [PotionHotbarTests.actorInput](../tests/PotionHotbarTests.java#L104) | REAL ITEM INPUT | Route key, wheel and mouse edges through Agyu's actual update state machine. |
| [PotionHotbarTests.panelInput](../tests/PotionHotbarTests.java#L129) | ACTUAL PANEL INPUT | Exercise scaled box clicks, fast drags, potion clicks, hit-stop, overlays and focus loss. |
| [PotionHotbarTests.hud](../tests/PotionHotbarTests.java#L160) | REAL HUD / PREVIEW | Assert text-free green/orange/blue bar interiors and export an actual-game screenshot. |
| [PotionHotbarTests.finishDrink](../tests/PotionHotbarTests.java#L177) | DRINK TIMELINE FIXTURE | Advance real simulation through the sip and bottle-lowering frames. |
| [PotionHotbarTests.actor](../tests/PotionHotbarTests.java#L179) | TEST FIXTURE | Create Agyu at the actual safe village spawn. |
| [PotionHotbarTests.settle](../tests/PotionHotbarTests.java#L181) | SIMULATION FIXTURE | Advance cooldowns without movement or enemy interference. |
| [PotionHotbarTests.step](../tests/PotionHotbarTests.java#L183) | SIMULATION FIXTURE | Advance one real player-input frame. |
| [PotionHotbarTests.press](../tests/PotionHotbarTests.java#L185) | INPUT FIXTURE | Send a fresh keyboard press through the synchronized handler. |
| [PotionHotbarTests.release](../tests/PotionHotbarTests.java#L187) | INPUT FIXTURE | Send a matching keyboard release. |
| [PotionHotbarTests.tap](../tests/PotionHotbarTests.java#L189) | INPUT FIXTURE | Send a panel key edge and advance one simulation frame. |
| [PotionHotbarTests.mouse](../tests/PotionHotbarTests.java#L191) | INPUT FIXTURE | Send a world mouse edge to the shared input handler. |
| [PotionHotbarTests.point](../tests/PotionHotbarTests.java#L193) | SCALED POINTER FIXTURE | Hit a rendered slot after converting its center into native window coordinates. |
| [PotionHotbarTests.worldMouse](../tests/PotionHotbarTests.java#L197) | POINTER FIXTURE | Send a primary click outside the hotbar. |
| [PotionHotbarTests.dispatch](../tests/PotionHotbarTests.java#L199) | REAL LISTENER FIXTURE | Exercise the actual GamePanel listeners instead of bypassing UI routing. |
| [PotionHotbarTests.get](../tests/PotionHotbarTests.java#L201) | TEST ACCESS | Read private session state for integration assertions without runtime setters. |
| [PotionHotbarTests.check](../tests/PotionHotbarTests.java#L203) | ASSERTION | Count a behavioral check and explain any failure. |

## tests/PresentationTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [PresentationTests.PresentationTests](../tests/PresentationTests.java#L17) | TEST UTILITY | Prevent instances of this suite. |
| [PresentationTests.main](../tests/PresentationTests.java#L19) | TEST ENTRY | Run equipment proportions and river appearance checks independently. |
| [PresentationTests.run](../tests/PresentationTests.java#L21) | PRESENTATION SUITE | Verify the live renderer and export the previously mismatched scenes. |
| [PresentationTests.bodies](../tests/PresentationTests.java#L23) | ACTUAL BODY CONSISTENCY | Compare Agyu's red cap geometry across items, movement and guard without including longer weapons. |
| [PresentationTests.river](../tests/PresentationTests.java#L45) | SHARED TEXTURE SEAM | Check matching outlet pixels, rich water detail, animation and prepared topology variants. |
| [PresentationTests.colorDistance](../tests/PresentationTests.java#L68) | PIXEL COLOR COMPARISON | Bound the largest channel change at the artwork-to-river join. |
| [PresentationTests.preview](../tests/PresentationTests.java#L70) | ACTUAL RIVER CAMERA | Export the real game at the waterfall outlet and its accessible southern bank. |
| [PresentationTests.get](../tests/PresentationTests.java#L82) | TEST SESSION ACCESS | Read the real session state without exposing production mutation. |
| [PresentationTests.set](../tests/PresentationTests.java#L84) | REVIEW CAMERA | Place the actual interpolated camera for a deterministic seam view. |
| [PresentationTests.check](../tests/PresentationTests.java#L86) | ASSERTION | Count and explain one presentation property. |

## tests/SpearCarryTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [SpearCarryTests.SpearCarryTests](../tests/SpearCarryTests.java#L15) | TEST UTILITY | Prevent instances of the suite. |
| [SpearCarryTests.main](../tests/SpearCarryTests.java#L17) | TEST ENTRY | Verify right-handed carrying and export a large actual-player comparison. |
| [SpearCarryTests.run](../tests/SpearCarryTests.java#L19) | CARRY REGRESSION | Check every cached pose, walking stability, fingers, upright tips and pixel opacity. |
| [SpearCarryTests.verifyProfileWalk](../tests/SpearCarryTests.java#L68) | SIDE STRIDE REGRESSION | Confirm the cached stride uses every gait phase in both directions and while guarding. |
| [SpearCarryTests.hasOriginalFeet](../tests/SpearCarryTests.java#L92) | ORIGINAL FEET REGRESSION | Verify the resized walk and guard use only the supplied foot colors. |
| [SpearCarryTests.bootWidth](../tests/SpearCarryTests.java#L116) | BOOT SIZE REGRESSION | Measure the visible width of both boots at the original ground anchor. |
| [SpearCarryTests.legHash](../tests/SpearCarryTests.java#L128) | BOOT PHASE COMPARISON | Compare only the footprint below the hip. |
| [SpearCarryTests.verifyShieldSilhouette](../tests/SpearCarryTests.java#L135) | SHIELD SILHOUETTE REGRESSION | Check that neither shield face contains disconnected crop pixels. |
| [SpearCarryTests.preview](../tests/SpearCarryTests.java#L156) | ACTUAL CARRY PREVIEW | Render standing, walking and blocking in every direction using the real Player path. |
| [SpearCarryTests.profilePreview](../tests/SpearCarryTests.java#L171) | WALK CYCLE PREVIEW | Lay out every side footfall and matching braced pose at the same scale. |
| [SpearCarryTests.check](../tests/SpearCarryTests.java#L193) | ASSERTION | Count and explain each held-weapon property. |

## tests/TreeBoundaryTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [TreeBoundaryTests.TreeBoundaryTests](../tests/TreeBoundaryTests.java#L22) | TEST UTILITY | Prevent instances of the regression suite. |
| [TreeBoundaryTests.main](../tests/TreeBoundaryTests.java#L24) | TEST ENTRY | Load the current art and run the focused suite. |
| [TreeBoundaryTests.run](../tests/TreeBoundaryTests.java#L26) | REGRESSION SUITE | Verify physical roots, gate safety/input, map caches and actual renders. |
| [TreeBoundaryTests.roots](../tests/TreeBoundaryTests.java#L30) | ASSET-ALIGNED COLLISION | Test every placed tree and the opaque bark of all four supplied styles. |
| [TreeBoundaryTests.approach](../tests/TreeBoundaryTests.java#L61) | PHYSICS FIXTURE | Find a clear approach whose last few pixels lead directly into one tree. |
| [TreeBoundaryTests.movement](../tests/TreeBoundaryTests.java#L76) | SWEPT MOVEMENT | Coarse walking, sprint, jump, knockback, enemy motion and dash cannot skip roots. |
| [TreeBoundaryTests.before](../tests/TreeBoundaryTests.java#L98) | COLLISION ASSERTION | Check the actor stopped on its approach side of an obstacle. |
| [TreeBoundaryTests.inside](../tests/TreeBoundaryTests.java#L103) | GATE FIXTURE | Position Agyu within operating range on the village side. |
| [TreeBoundaryTests.outside](../tests/TreeBoundaryTests.java#L109) | GATE FIXTURE | Position Agyu on the short exterior path, clear of the closing leaf. |
| [TreeBoundaryTests.gates](../tests/TreeBoundaryTests.java#L115) | SAFE GATES / INDEX | Verify four directions, closed/open collision, routing, safe closure and cached map markers. |
| [TreeBoundaryTests.visibility](../tests/TreeBoundaryTests.java#L159) | RENDER STATE / VISIBILITY | Canopies fade without making roots transparent or changing graphics state. |
| [TreeBoundaryTests.input](../tests/TreeBoundaryTests.java#L176) | REAL INPUT | E edges toggle gates, held presses do not repeat, and pause/map/restart preserve expected state. |
| [TreeBoundaryTests.mapHash](../tests/TreeBoundaryTests.java#L193) | MAP CACHE EVIDENCE | Render a map to verify live gate markers without replacing its terrain image. |
| [TreeBoundaryTests.position](../tests/TreeBoundaryTests.java#L198) | RENDER FIXTURE | Set an actual game camera and settled actor pose at a gateway. |
| [TreeBoundaryTests.previews](../tests/TreeBoundaryTests.java#L205) | ACTUAL RENDER PREVIEWS | Export four gate states, the protected village and root/canopy behavior. |
| [TreeBoundaryTests.get](../tests/TreeBoundaryTests.java#L233) | TEST REFLECTION | Inspect session state without adding production test hooks. |
| [TreeBoundaryTests.set](../tests/TreeBoundaryTests.java#L235) | TEST REFLECTION | Set an integration fixture's private state. |
| [TreeBoundaryTests.press](../tests/TreeBoundaryTests.java#L237) | REAL KEY INPUT | Send the same key event used by the running game. |
| [TreeBoundaryTests.check](../tests/TreeBoundaryTests.java#L239) | ASSERTION | Count and enforce a meaningful regression invariant. |

## tests/VillageLayoutTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [VillageLayoutTests.VillageLayoutTests](../tests/VillageLayoutTests.java#L23) | TEST HELPER | Prevent instances of this test/helper class. |
| [VillageLayoutTests.main](../tests/VillageLayoutTests.java#L25) | TEST HELPER | Entry point for this regression suite. |
| [VillageLayoutTests.run](../tests/VillageLayoutTests.java#L29) | TEST HELPER | Handle run for VillageLayoutTests. |
| [VillageLayoutTests.flood](../tests/VillageLayoutTests.java#L63) | TEST HELPER | Handle flood for VillageLayoutTests. |
| [VillageLayoutTests.farms](../tests/VillageLayoutTests.java#L77) | TEST HELPER | Handle farms for VillageLayoutTests. |
| [VillageLayoutTests.hash](../tests/VillageLayoutTests.java#L99) | TEST HELPER | Handle hash for VillageLayoutTests. |
| [VillageLayoutTests.previews](../tests/VillageLayoutTests.java#L103) | TEST HELPER | Handle previews for VillageLayoutTests. |
| [VillageLayoutTests.check](../tests/VillageLayoutTests.java#L126) | TEST HELPER | Handle check for VillageLayoutTests. |

## tests/VillageTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [VillageTests.VillageTests](../tests/VillageTests.java#L32) | TEST HELPER | Prevent instances of this test/helper class. |
| [VillageTests.main](../tests/VillageTests.java#L34) | TEST HELPER | Entry point for this regression suite. |
| [VillageTests.run](../tests/VillageTests.java#L38) | TEST HELPER | Handle run for VillageTests. |
| [VillageTests.houseChecks](../tests/VillageTests.java#L43) | TEST HELPER | Handle house checks for VillageTests. |
| [VillageTests.mapChecks](../tests/VillageTests.java#L77) | TEST HELPER | Handle map checks for VillageTests. |
| [VillageTests.panelChecks](../tests/VillageTests.java#L114) | TEST HELPER | Handle panel checks for VillageTests. |
| [VillageTests.previews](../tests/VillageTests.java#L130) | TEST HELPER | Handle previews for VillageTests. |
| [VillageTests.mapImage](../tests/VillageTests.java#L149) | TEST HELPER | Handle map image for VillageTests. |
| [VillageTests.drawPrivate](../tests/VillageTests.java#L154) | TEST HELPER | Render draw private using the current graphics state. |
| [VillageTests.isGreen](../tests/VillageTests.java#L159) | TEST HELPER | Handle is green for VillageTests. |
| [VillageTests.greenBounds](../tests/VillageTests.java#L161) | TEST HELPER | Handle green bounds for VillageTests. |
| [VillageTests.sameRegion](../tests/VillageTests.java#L169) | TEST HELPER | Handle same region for VillageTests. |
| [VillageTests.fingerprint](../tests/VillageTests.java#L173) | TEST HELPER | Handle fingerprint for VillageTests. |
| [VillageTests.field](../tests/VillageTests.java#L178) | TEST HELPER | Handle field for VillageTests. |
| [VillageTests.set](../tests/VillageTests.java#L182) | TEST HELPER | Handle set for VillageTests. |
| [VillageTests.press](../tests/VillageTests.java#L186) | TEST HELPER | Handle press for VillageTests. |
| [VillageTests.check](../tests/VillageTests.java#L188) | TEST HELPER | Handle check for VillageTests. |

## tests/WaterfallTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [WaterfallTests.WaterfallTests](../tests/WaterfallTests.java#L17) | STATIC TEST | Prevent instances of the suite. |
| [WaterfallTests.main](../tests/WaterfallTests.java#L19) | TEST ENTRY | Run the registered waterfall checks independently. |
| [WaterfallTests.run](../tests/WaterfallTests.java#L21) | REGRESSION SUITE | Check untouched sources, cached phases and geometry shared with the displayed cove. |
| [WaterfallTests.wet](../tests/WaterfallTests.java#L56) | HYDRAULIC GRAPH | Include protected culverts and river bridges in hydraulic connectivity. |
| [WaterfallTests.hash](../tests/WaterfallTests.java#L58) | PIXEL FINGERPRINT | Compare cached RGBA phases without changing their pixels. |
| [WaterfallTests.check](../tests/WaterfallTests.java#L60) | ASSERTION | Count and explain each regression property. |

## tests/WildlifeTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [WildlifeTests.WildlifeTests](../tests/WildlifeTests.java#L27) | TEST UTILITY | Prevent construction of this focused test suite. |
| [WildlifeTests.main](../tests/WildlifeTests.java#L29) | TEST ENTRY | Load real sprites and run the wildlife checks. |
| [WildlifeTests.run](../tests/WildlifeTests.java#L31) | REGRESSION SUITE | Exercise source identity, real night quotas, pounces, scares, cooldowns and paused sessions. |
| [WildlifeTests.sourceAssets](../tests/WildlifeTests.java#L33) | SUPPLIED ASSET IDENTITY | Validate every original source hash and all action/direction lookups from the packed runtime atlases. |
| [WildlifeTests.quotas](../tests/WildlifeTests.java#L47) | PER-NIGHT TOTAL QUOTA | Kill spawned wolves, continue the night and prove the total cannot exceed two or three before resetting next night. |
| [WildlifeTests.wolfCombat](../tests/WildlifeTests.java#L69) | REAL WOLF COMBAT | Verify a night pounce, authored body lift, one empowered hit and frontal shield protection. |
| [WildlifeTests.sigbin](../tests/WildlifeTests.java#L80) | NIGHT ENEMY / REAL FIGHT | Verify a night reveal, persistent hunting, real claw damage, spear interruption and a defeat through player attack input. |
| [WildlifeTests.ambushDirector](../tests/WildlifeTests.java#L104) | EXPLORATION-GATED AMBUSH | Require actual movement in dense exterior woodland, then enforce one active creature and a lengthy cooldown. |
| [WildlifeTests.session](../tests/WildlifeTests.java#L120) | PAUSED SESSION / RESET | Confirm pause freezes the real director and a fresh journey clears its night budget. |
| [WildlifeTests.previews](../tests/WildlifeTests.java#L130) | RENDER EVIDENCE | Export the actual night forest and a representative Sigbin reveal using normal GamePanel rendering. |
| [WildlifeTests.image](../tests/WildlifeTests.java#L141) | PREVIEW EXPORT | Paint the actual game surface to one review PNG. |
| [WildlifeTests.camp](../tests/WildlifeTests.java#L143) | CLEAR FOREST FIXTURE | Place a real Player on the reserved northern camp floor. |
| [WildlifeTests.waitFor](../tests/WildlifeTests.java#L145) | COMBAT STATE WAIT | Advance the real creature until a requested warning or pounce with a finite timeout. |
| [WildlifeTests.tap](../tests/WildlifeTests.java#L147) | REAL INPUT FIXTURE | Deliver one keyboard edge through the actual session key handler. |
| [WildlifeTests.get](../tests/WildlifeTests.java#L149) | SESSION INSPECTION | Inspect existing private state without adding production test hooks. |
| [WildlifeTests.set](../tests/WildlifeTests.java#L151) | CAMERA FIXTURE | Set only the offscreen preview's camera interpolation values. |
| [WildlifeTests.check](../tests/WildlifeTests.java#L153) | ASSERTION | Count a behavior contract and identify the exact failure. |

## tools/importers/BossSpriteImporter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [BossSpriteImporter.BossSpriteImporter](../tools/importers/BossSpriteImporter.java#L24) | IMPORT UTILITY | Prevent instances of the deterministic packing tool. |
| [BossSpriteImporter.count](../tools/importers/BossSpriteImporter.java#L26) | FRAME COUNTS | Preserve each authored state length, including the fighter's four-frame attack. |
| [BossSpriteImporter.main](../tools/importers/BossSpriteImporter.java#L28) | IMPORT ENTRY | Pack all three sheets, write exact source coordinates and export a review gallery. |
| [BossSpriteImporter.review](../tools/importers/BossSpriteImporter.java#L65) | REVIEW ART | Show facing and action examples without modifying packed gameplay images. |

## tools/importers/CombatBodyImporter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [CombatBodyImporter.CombatBodyImporter](../tools/importers/CombatBodyImporter.java#L29) | ART TOOL | Prevent instances of this static helper class. |
| [CombatBodyImporter.main](../tools/importers/CombatBodyImporter.java#L31) | ART TOOL | Entry point for this developer tool. |
| [CombatBodyImporter.part](../tools/importers/CombatBodyImporter.java#L102) | ART TOOL | Handle part for CombatBodyImporter. |
| [CombatBodyImporter.num](../tools/importers/CombatBodyImporter.java#L104) | ART TOOL | Handle num for CombatBodyImporter. |
| [CombatBodyImporter.overrides](../tools/importers/CombatBodyImporter.java#L106) | ART TOOL | Handle overrides for CombatBodyImporter. |
| [CombatBodyImporter.skin](../tools/importers/CombatBodyImporter.java#L122) | ART TOOL | Handle skin for CombatBodyImporter. |
| [CombatBodyImporter.hand](../tools/importers/CombatBodyImporter.java#L127) | ART TOOL | Handle hand for CombatBodyImporter. |
| [CombatBodyImporter.mirror](../tools/importers/CombatBodyImporter.java#L157) | ART TOOL | Handle mirror for CombatBodyImporter. |
| [CombatBodyImporter.previews](../tools/importers/CombatBodyImporter.java#L162) | ART TOOL | Handle previews for CombatBodyImporter. |

## tools/importers/EquipmentSpriteImporter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [EquipmentSpriteImporter.EquipmentSpriteImporter](../tools/importers/EquipmentSpriteImporter.java#L24) | STATIC TOOL | Prevent instances of the sprite preparation tool. |
| [EquipmentSpriteImporter.main](../tools/importers/EquipmentSpriteImporter.java#L26) | ART IMPORT / ARRAY REGISTRATION | Pack twenty-four authored bodies and record shared boot/wrist metadata. |
| [EquipmentSpriteImporter.bounds](../tools/importers/EquipmentSpriteImporter.java#L65) | ALPHA BOUNDS | Measure only the visible character within one authored grid cell. |
| [EquipmentSpriteImporter.boots](../tools/importers/EquipmentSpriteImporter.java#L71) | BOOT REGISTRATION | Locate the boot midpoint without letting arms or bandana move the body anchor. |

## tools/importers/GuardSpriteImporter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [GuardSpriteImporter.GuardSpriteImporter](../tools/importers/GuardSpriteImporter.java#L20) | ART TOOL | Prevent instances of this static helper class. |
| [GuardSpriteImporter.main](../tools/importers/GuardSpriteImporter.java#L22) | ART TOOL | Entry point for this developer tool. |
| [GuardSpriteImporter.read](../tools/importers/GuardSpriteImporter.java#L72) | ART TOOL | Load or validate read data. |
| [GuardSpriteImporter.bounds](../tools/importers/GuardSpriteImporter.java#L78) | ART TOOL | Handle bounds for GuardSpriteImporter. |
| [GuardSpriteImporter.pack](../tools/importers/GuardSpriteImporter.java#L88) | ART TOOL | Handle pack for GuardSpriteImporter. |

## tools/importers/PotionSpriteImporter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [PotionSpriteImporter.PotionSpriteImporter](../tools/importers/PotionSpriteImporter.java#L18) | STATIC TOOL | Prevent instances of the artwork packing tool. |
| [PotionSpriteImporter.main](../tools/importers/PotionSpriteImporter.java#L20) | IMPORT / DSA ARRAY | Pack three potion icons and twenty-two effect frames into deterministic runtime images. |
| [PotionSpriteImporter.read](../tools/importers/PotionSpriteImporter.java#L54) | VALIDATED IMAGE IO | Require a readable transparent source PNG. |
| [PotionSpriteImporter.bounds](../tools/importers/PotionSpriteImporter.java#L58) | ALPHA SCAN | Crop empty bottle margins using visible alpha rather than hidden RGB pixels. |
| [PotionSpriteImporter.row](../tools/importers/PotionSpriteImporter.java#L64) | ASSET PROVENANCE | Record atlas coordinates and the exact original SHA-256 for each supplied frame. |

## tools/importers/RpgAssetImporter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [RpgAssetImporter.RpgAssetImporter](../tools/importers/RpgAssetImporter.java#L23) | ART TOOL | Prevent instances of this static helper class. |
| [RpgAssetImporter.main](../tools/importers/RpgAssetImporter.java#L25) | ART TOOL | Entry point for this developer tool. |
| [RpgAssetImporter.read](../tools/importers/RpgAssetImporter.java#L32) | ART TOOL | Load or validate read data. |
| [RpgAssetImporter.write](../tools/importers/RpgAssetImporter.java#L37) | ART TOOL | Handle write for RpgAssetImporter. |
| [RpgAssetImporter.cell](../tools/importers/RpgAssetImporter.java#L41) | ART TOOL | Handle cell for RpgAssetImporter. |
| [RpgAssetImporter.bounds](../tools/importers/RpgAssetImporter.java#L46) | ART TOOL | Handle bounds for RpgAssetImporter. |
| [RpgAssetImporter.scale](../tools/importers/RpgAssetImporter.java#L56) | ART TOOL | Handle scale for RpgAssetImporter. |
| [RpgAssetImporter.pack](../tools/importers/RpgAssetImporter.java#L62) | ART TOOL | Handle pack for RpgAssetImporter. |
| [RpgAssetImporter.terrain](../tools/importers/RpgAssetImporter.java#L78) | ART TOOL | Handle terrain for RpgAssetImporter. |
| [RpgAssetImporter.foliage](../tools/importers/RpgAssetImporter.java#L87) | ART TOOL | Handle foliage for RpgAssetImporter. |
| [RpgAssetImporter.components](../tools/importers/RpgAssetImporter.java#L94) | ART TOOL | Handle components for RpgAssetImporter. |
| [RpgAssetImporter.houses](../tools/importers/RpgAssetImporter.java#L119) | ART TOOL | Handle houses for RpgAssetImporter. |
| [RpgAssetImporter.footX](../tools/importers/RpgAssetImporter.java#L139) | ART TOOL | Handle foot x for RpgAssetImporter. |
| [RpgAssetImporter.npcs](../tools/importers/RpgAssetImporter.java#L146) | ART TOOL | Handle npcs for RpgAssetImporter. |
| [RpgAssetImporter.props](../tools/importers/RpgAssetImporter.java#L158) | ART TOOL | Handle props for RpgAssetImporter. |

## tools/importers/SpriteImporter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [SpriteImporter.SpriteImporter](../tools/importers/SpriteImporter.java#L38) | ART TOOL | Prevent instances of this static helper class. |
| [SpriteImporter.boot](../tools/importers/SpriteImporter.java#L40) | IMPORT: FOOT ANCHOR | Recognize opaque brown boot pixels used to locate stable feet. |
| [SpriteImporter.sample](../tools/importers/SpriteImporter.java#L45) | IMPORT: SOURCE PIXEL | Read a pixel within the selected original frame bounds. |
| [SpriteImporter.anchor](../tools/importers/SpriteImporter.java#L53) | IMPORT: FOOT ANCHOR | Find the foot row and center from the original boot pixels. |
| [SpriteImporter.frame](../tools/importers/SpriteImporter.java#L66) | IMPORT: IMAGE PACKING | Pack one active frame on a 64x64 transparent canvas at the shared baseline. |
| [SpriteImporter.death](../tools/importers/SpriteImporter.java#L73) | IMPORT: IMAGE PACKING | Pack the wider 96x64 death pose from the original source sheet. |
| [SpriteImporter.main](../tools/importers/SpriteImporter.java#L84) | ART TOOL | Entry point for this developer tool. |

## tools/importers/WaterfallSpriteImporter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [WaterfallSpriteImporter.WaterfallSpriteImporter](../tools/importers/WaterfallSpriteImporter.java#L20) | STATIC IMPORTER | Prevent instances of the offline converter. |
| [WaterfallSpriteImporter.main](../tools/importers/WaterfallSpriteImporter.java#L22) | IMPORT ENTRY | Pack matching pond/river water, sampled banks and a tapered outlet with physical geometry. |
| [WaterfallSpriteImporter.riverPixel](../tools/importers/WaterfallSpriteImporter.java#L93) | SHARED WATER TEXTURE | Mirror a clean native pond patch into a seamless world-registered water pattern. |
| [WaterfallSpriteImporter.outlet](../tools/importers/WaterfallSpriteImporter.java#L98) | OUTLET REGISTRATION | Measure the actual stream width within a native cove column. |
| [WaterfallSpriteImporter.banks](../tools/importers/WaterfallSpriteImporter.java#L104) | NATIVE BANK SAMPLES | Copy the cove's real rock/moss bank pixels into reusable eight-pixel edge strips. |
| [WaterfallSpriteImporter.wet](../tools/importers/WaterfallSpriteImporter.java#L112) | COLOR REGISTRATION | Identify teal water while excluding warm rocks, golden sand and yellow-green moss. |
| [WaterfallSpriteImporter.mask](../tools/importers/WaterfallSpriteImporter.java#L114) | DSA FIFO FLOOD FILL | Follow connected water from the pond, then fill small lily-pad islands inside each shoreline span. |
| [WaterfallSpriteImporter.readGif](../tools/importers/WaterfallSpriteImporter.java#L124) | GIF DELTA COMPOSITION | Honor frame offsets and disposal before sampling the original moving cascade. |
| [WaterfallSpriteImporter.find](../tools/importers/WaterfallSpriteImporter.java#L143) | METADATA TREE | Locate the named GIF metadata node. |
| [WaterfallSpriteImporter.value](../tools/importers/WaterfallSpriteImporter.java#L145) | METADATA ATTRIBUTE | Read an integer GIF descriptor attribute. |
| [WaterfallSpriteImporter.scale](../tools/importers/WaterfallSpriteImporter.java#L147) | NEAREST NEIGHBOR PACKING | Register existing artwork at native resolution without smoothing pixel edges. |
| [WaterfallSpriteImporter.copy](../tools/importers/WaterfallSpriteImporter.java#L149) | IMAGE COPY | Snapshot a composite only during offline import. |
| [WaterfallSpriteImporter.paste](../tools/importers/WaterfallSpriteImporter.java#L151) | ATLAS PACKING | Store one registered phase in the horizontal atlas. |
| [WaterfallSpriteImporter.adjust](../tools/importers/WaterfallSpriteImporter.java#L153) | WATER SHIMMER | Transfer a small source-texture luminance change while retaining painted pool depth and lily details. |
| [WaterfallSpriteImporter.blend](../tools/importers/WaterfallSpriteImporter.java#L155) | CASCADE BLEND | Register moving source water over the painted waterfall inside its exact wet silhouette. |
| [WaterfallSpriteImporter.hash](../tools/importers/WaterfallSpriteImporter.java#L157) | ART PROVENANCE | Record SHA-256 for untouched user and generated sources. |

## tools/importers/WildlifeSpriteImporter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [WildlifeSpriteImporter.WildlifeSpriteImporter](../tools/importers/WildlifeSpriteImporter.java#L24) | TOOL UTILITY | Prevent construction of this offline import tool. |
| [WildlifeSpriteImporter.bodyBounds](../tools/importers/WildlifeSpriteImporter.java#L26) | DSA FIFO FLOOD FILL | Register the largest opaque connected body, excluding detached dust and the death puff. |
| [WildlifeSpriteImporter.offer](../tools/importers/WildlifeSpriteImporter.java#L45) | DSA ARRAY QUEUE | Append one unseen opaque neighbor to the flood-fill FIFO in constant time. |
| [WildlifeSpriteImporter.main](../tools/importers/WildlifeSpriteImporter.java#L49) | IMPORT ENTRY | Pack original RGBA poses once, record source hashes and export a transparent-art review gallery. |

## tools/previews/RpgVillagePreviews.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [RpgVillagePreviews.RpgVillagePreviews](../tools/previews/RpgVillagePreviews.java#L18) | ART TOOL | Prevent instances of this static helper class. |
| [RpgVillagePreviews.get](../tools/previews/RpgVillagePreviews.java#L20) | ART TOOL | Handle get for RpgVillagePreviews. |
| [RpgVillagePreviews.set](../tools/previews/RpgVillagePreviews.java#L22) | ART TOOL | Handle set for RpgVillagePreviews. |
| [RpgVillagePreviews.main](../tools/previews/RpgVillagePreviews.java#L24) | ART TOOL | Entry point for this developer tool. |
| [RpgVillagePreviews.place](../tools/previews/RpgVillagePreviews.java#L70) | ART TOOL | Handle place for RpgVillagePreviews. |
| [RpgVillagePreviews.render](../tools/previews/RpgVillagePreviews.java#L81) | ART TOOL | Handle render for RpgVillagePreviews. |
