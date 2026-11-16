# Labeled function and block index

Source links identify each labeled method or constructor. Inline SECTION labels mark fields and algorithm stages; nested helpers are located in the named file.

## src/app/GamePanel.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [GamePanel.GamePanel](../src/app/GamePanel.java#L56) | FUNCTION | Initialize GamePanel state. |
| [GamePanel.mousePressed](../src/app/GamePanel.java#L63) | FUNCTION | Handle mouse pressed for GamePanel. |
| [GamePanel.focusLost](../src/app/GamePanel.java#L67) | FUNCTION | Handle focus lost for GamePanel. |
| [GamePanel.reset](../src/app/GamePanel.java#L75) | SESSION: RESTART | Create a fresh world, actors, inventory, quests and timing state. |
| [GamePanel.start](../src/app/GamePanel.java#L90) | FUNCTION | Handle start for GamePanel. |
| [GamePanel.stop](../src/app/GamePanel.java#L95) | FUNCTION | Handle stop for GamePanel. |
| [GamePanel.run](../src/app/GamePanel.java#L100) | TIMING: FIXED STEP | Advance simulation at 60 Hz and interpolate rendering. |
| [GamePanel.targetX](../src/app/GamePanel.java#L121) | FUNCTION | Handle target x for GamePanel. |
| [GamePanel.targetY](../src/app/GamePanel.java#L123) | FUNCTION | Handle target y for GamePanel. |
| [GamePanel.freezeInterpolation](../src/app/GamePanel.java#L125) | FUNCTION | Handle freeze interpolation for GamePanel. |
| [GamePanel.update](../src/app/GamePanel.java#L130) | SIMULATION / DSA EVENT QUEUE | Apply input, update actors, enqueue defeats, then process rewards safely. |
| [GamePanel.updateNavigation](../src/app/GamePanel.java#L180) | DSA: BFS / A* CACHES | Refresh enemy flow distances and the optional player trail when needed. |
| [GamePanel.processEvents](../src/app/GamePanel.java#L212) | DSA: FIFO QUEUE | Remove defeat events in insertion order after actor updates, then award progression. |
| [GamePanel.guardsRemain](../src/app/GamePanel.java#L229) | FUNCTION | Handle guards remain for GamePanel. |
| [GamePanel.interact](../src/app/GamePanel.java#L234) | GAMEPLAY: INTERACTION | Restore eligible shrines, finish at HOME or talk to the nearest available NPC. |
| [GamePanel.objectiveSite](../src/app/GamePanel.java#L265) | FUNCTION | Handle objective site for GamePanel. |
| [GamePanel.paintComponent](../src/app/GamePanel.java#L284) | OOP: POLYMORPHISM / SORTING | Sort Drawable objects by feet depth and dispatch each concrete draw implementation. |
| [GamePanel.visible](../src/app/GamePanel.java#L339) | FUNCTION | Handle visible for GamePanel. |
| [GamePanel.siteName](../src/app/GamePanel.java#L343) | FUNCTION | Handle site name for GamePanel. |
| [GamePanel.siteColor](../src/app/GamePanel.java#L345) | FUNCTION | Handle site color for GamePanel. |
| [GamePanel.drawBeacons](../src/app/GamePanel.java#L351) | FUNCTION | Render draw beacons using the current graphics state. |
| [GamePanel.card](../src/app/GamePanel.java#L366) | FUNCTION | Handle card for GamePanel. |
| [GamePanel.text](../src/app/GamePanel.java#L371) | FUNCTION | Handle text for GamePanel. |
| [GamePanel.center](../src/app/GamePanel.java#L373) | FUNCTION | Handle center for GamePanel. |
| [GamePanel.vitalBar](../src/app/GamePanel.java#L377) | FUNCTION | Handle vital bar for GamePanel. |
| [GamePanel.drawHud](../src/app/GamePanel.java#L387) | FUNCTION | Render draw hud using the current graphics state. |
| [GamePanel.drawTimeOfDay](../src/app/GamePanel.java#L405) | WORLD CLOCK HUD | Show a compact numbered phase and next-change countdown below the resource bars. |
| [GamePanel.drawBossHealth](../src/app/GamePanel.java#L412) | BOSS HUD | Show one continuous health bar only during a nearby arena fight. |
| [GamePanel.drawMap](../src/app/GamePanel.java#L426) | FUNCTION | Render draw map using the current graphics state. |
| [GamePanel.drawJournal](../src/app/GamePanel.java#L433) | FUNCTION | Render draw journal using the current graphics state. |
| [GamePanel.drawDiagnostics](../src/app/GamePanel.java#L457) | FUNCTION | Render draw diagnostics using the current graphics state. |
| [GamePanel.drawTitle](../src/app/GamePanel.java#L465) | FUNCTION | Render draw title using the current graphics state. |
| [GamePanel.drawModal](../src/app/GamePanel.java#L480) | FUNCTION | Render draw modal using the current graphics state. |

## src/app/KeyHandler.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [KeyHandler.Input](../src/app/KeyHandler.java#L23) | FUNCTION | Initialize Input state. |
| [KeyHandler.pressed](../src/app/KeyHandler.java#L28) | FUNCTION | Handle pressed for KeyHandler. |
| [KeyHandler.keyPressed](../src/app/KeyHandler.java#L31) | DSA: HASH SET / INPUT EDGES | Add a held key once and suppress automatic-repeat action edges. |
| [KeyHandler.keyReleased](../src/app/KeyHandler.java#L51) | DSA: HASH SET | Remove the released key from held membership. |
| [KeyHandler.mousePressed](../src/app/KeyHandler.java#L53) | FUNCTION | Handle mouse pressed for KeyHandler. |
| [KeyHandler.mouseReleased](../src/app/KeyHandler.java#L60) | FUNCTION | Handle mouse released for KeyHandler. |
| [KeyHandler.mouseClicked](../src/app/KeyHandler.java#L65) | FUNCTION | Handle mouse clicked for KeyHandler. |
| [KeyHandler.mouseEntered](../src/app/KeyHandler.java#L67) | FUNCTION | Handle mouse entered for KeyHandler. |
| [KeyHandler.mouseExited](../src/app/KeyHandler.java#L69) | FUNCTION | Handle mouse exited for KeyHandler. |
| [KeyHandler.held](../src/app/KeyHandler.java#L71) | FUNCTION | Handle held for KeyHandler. |
| [KeyHandler.poll](../src/app/KeyHandler.java#L73) | INPUT: SNAPSHOT | Return held controls and consume one-shot action edges atomically. |
| [KeyHandler.defer](../src/app/KeyHandler.java#L81) | FUNCTION | Handle defer for KeyHandler. |
| [KeyHandler.clear](../src/app/KeyHandler.java#L83) | FUNCTION | Handle clear for KeyHandler. |

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

## src/combat/EnergyMeter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [EnergyMeter.value](../src/combat/EnergyMeter.java#L11) | FUNCTION | Handle value for EnergyMeter. |
| [EnergyMeter.exhausted](../src/combat/EnergyMeter.java#L13) | FUNCTION | Handle exhausted for EnergyMeter. |
| [EnergyMeter.canSprint](../src/combat/EnergyMeter.java#L15) | FUNCTION | Handle can sprint for EnergyMeter. |
| [EnergyMeter.spend](../src/combat/EnergyMeter.java#L17) | OOP: ENCAPSULATION | Spend energy only when the requested amount is valid and available. |
| [EnergyMeter.sprint](../src/combat/EnergyMeter.java#L26) | RESOURCE: SPRINT | Spend energy for the time actually spent moving. |
| [EnergyMeter.update](../src/combat/EnergyMeter.java#L31) | OOP: ENCAPSULATION | Apply delayed recovery and release exhaustion at the resume threshold. |

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
| [ShieldGuard.update](../src/combat/ShieldGuard.java#L15) | FUNCTION | Advance update state with elapsed time. |
| [ShieldGuard.load](../src/combat/ShieldGuard.java#L17) | FUNCTION | Load or validate load data. |
| [ShieldGuard.faces](../src/combat/ShieldGuard.java#L29) | FUNCTION | Handle faces for ShieldGuard. |
| [ShieldGuard.block](../src/combat/ShieldGuard.java#L42) | FUNCTION | Handle block for ShieldGuard. |
| [ShieldGuard.impact](../src/combat/ShieldGuard.java#L51) | FUNCTION | Handle impact for ShieldGuard. |
| [ShieldGuard.drawImpact](../src/combat/ShieldGuard.java#L53) | FUNCTION | Render draw impact using the current graphics state. |

## src/combat/SpearAttack.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [SpearAttack.tipX](../src/combat/SpearAttack.java#L39) | FUNCTION | Handle tip x for SpearAttack. |
| [SpearAttack.tipY](../src/combat/SpearAttack.java#L41) | FUNCTION | Handle tip y for SpearAttack. |
| [SpearAttack.begin](../src/combat/SpearAttack.java#L44) | DSA: HASH SET / COMBAT | Select the combo stage and clear targets already hit by the previous stage. |
| [SpearAttack.cancel](../src/combat/SpearAttack.java#L46) | FUNCTION | Handle cancel for SpearAttack. |
| [SpearAttack.stage](../src/combat/SpearAttack.java#L48) | FUNCTION | Handle stage for SpearAttack. |
| [SpearAttack.move](../src/combat/SpearAttack.java#L50) | FUNCTION | Handle move for SpearAttack. |
| [SpearAttack.name](../src/combat/SpearAttack.java#L55) | FUNCTION | Handle name for SpearAttack. |
| [SpearAttack.duration](../src/combat/SpearAttack.java#L57) | FUNCTION | Handle duration for SpearAttack. |
| [SpearAttack.activeStart](../src/combat/SpearAttack.java#L59) | FUNCTION | Handle active start for SpearAttack. |
| [SpearAttack.activeEnd](../src/combat/SpearAttack.java#L61) | FUNCTION | Handle active end for SpearAttack. |
| [SpearAttack.chainAt](../src/combat/SpearAttack.java#L63) | FUNCTION | Handle chain at for SpearAttack. |
| [SpearAttack.color](../src/combat/SpearAttack.java#L65) | FUNCTION | Handle color for SpearAttack. |
| [SpearAttack.duration](../src/combat/SpearAttack.java#L67) | FUNCTION | Handle duration for SpearAttack. |
| [SpearAttack.lungeSpeed](../src/combat/SpearAttack.java#L69) | FUNCTION | Handle lunge speed for SpearAttack. |
| [SpearAttack.blue](../src/combat/SpearAttack.java#L78) | FUNCTION | Handle blue for SpearAttack. |
| [SpearAttack.loadWeapon](../src/combat/SpearAttack.java#L84) | ASSET PREPARATION | Cache the supplied spearhead and matching shaft before gameplay. |
| [SpearAttack.clamp](../src/combat/SpearAttack.java#L108) | FUNCTION | Handle clamp for SpearAttack. |
| [SpearAttack.smooth](../src/combat/SpearAttack.java#L110) | FUNCTION | Handle smooth for SpearAttack. |
| [SpearAttack.forward](../src/combat/SpearAttack.java#L112) | FUNCTION | Handle forward for SpearAttack. |
| [SpearAttack.pulse](../src/combat/SpearAttack.java#L116) | FUNCTION | Handle pulse for SpearAttack. |
| [SpearAttack.bodyDirection](../src/combat/SpearAttack.java#L120) | FUNCTION | Handle body direction for SpearAttack. |
| [SpearAttack.bodyOffsetX](../src/combat/SpearAttack.java#L129) | FUNCTION | Handle body offset x for SpearAttack. |
| [SpearAttack.bodyOffsetY](../src/combat/SpearAttack.java#L134) | FUNCTION | Handle body offset y for SpearAttack. |
| [SpearAttack.pose](../src/combat/SpearAttack.java#L142) | COMBAT: SHARED GEOMETRY | Solve the body hand pivot and blade path used by both damage and rendering. |
| [SpearAttack.update](../src/combat/SpearAttack.java#L194) | COMBAT: SWEPT COLLISION / HASH SET | Sample the actual spear path and damage each enemy once per stage. |
| [SpearAttack.behind](../src/combat/SpearAttack.java#L225) | FUNCTION | Handle behind for SpearAttack. |
| [SpearAttack.behindBody](../src/combat/SpearAttack.java#L230) | FUNCTION | Handle behind body for SpearAttack. |
| [SpearAttack.trail](../src/combat/SpearAttack.java#L232) | FUNCTION | Handle trail for SpearAttack. |
| [SpearAttack.draw](../src/combat/SpearAttack.java#L261) | FUNCTION | Render draw using the current graphics state. |

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

## src/dsa/Inventory.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Inventory.count](../src/dsa/Inventory.java#L14) | DSA: HASH MAP | Read an item quantity with a zero default. |
| [Inventory.add](../src/dsa/Inventory.java#L16) | OOP: ENCAPSULATION / HASH MAP | Validate a positive amount and update quantity without integer overflow. |
| [Inventory.consume](../src/dsa/Inventory.java#L21) | OOP: ENCAPSULATION / HASH MAP | Spend one available item and remove empty entries. |

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
| [QuestLog.Quest](../src/dsa/QuestLog.java#L21) | FUNCTION | Initialize Quest state. |
| [QuestLog.QuestLog](../src/dsa/QuestLog.java#L33) | FUNCTION | Initialize QuestLog state. |
| [QuestLog.ready](../src/dsa/QuestLog.java#L45) | DSA: PREREQUISITE GRAPH | Check that every required quest is complete before unlocking a quest. |
| [QuestLog.finish](../src/dsa/QuestLog.java#L50) | OOP: ENCAPSULATION / HASH SET | Complete a ready quest once and record it in the journal. |
| [QuestLog.done](../src/dsa/QuestLog.java#L57) | DSA: HASH SET | Test completion membership by quest ID. |
| [QuestLog.seals](../src/dsa/QuestLog.java#L59) | FUNCTION | Handle seals for QuestLog. |
| [QuestLog.record](../src/dsa/QuestLog.java#L65) | DSA: BOUNDED DEQUE | Add the newest journal entry first and discard the oldest beyond eight entries. |
| [QuestLog.history](../src/dsa/QuestLog.java#L70) | OOP: ENCAPSULATION | Return a copy of journal entries without exposing its deque. |

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
| [ArenaBoss.hurtBox](../src/entities/ArenaBoss.java#L83) | AUTHORED BODY HITBOX | Match the substantial authored torso instead of the old slime body. |
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
| [Enemy.Enemy](../src/entities/Enemy.java#L25) | FUNCTION | Initialize Enemy state. |
| [Enemy.speed](../src/entities/Enemy.java#L33) | OOP: ABSTRACTION | Require each enemy subtype to supply its movement speed. |
| [Enemy.contactDamage](../src/entities/Enemy.java#L35) | OOP: ABSTRACTION | Require each enemy subtype to supply its contact damage. |
| [Enemy.scale](../src/entities/Enemy.java#L37) | FUNCTION | Handle scale for Enemy. |
| [Enemy.health](../src/entities/Enemy.java#L39) | OOP: ENCAPSULATION | Expose current health while retaining private storage. |
| [Enemy.maxHealth](../src/entities/Enemy.java#L41) | OOP: ENCAPSULATION | Expose the immutable maximum health. |
| [Enemy.refreshNightPower](../src/entities/Enemy.java#L43) | NIGHT POWER | Apply the current outdoor night state without altering health, maximum health or rewards. |
| [Enemy.nightEmpowered](../src/entities/Enemy.java#L45) | ENCAPSULATION | Read whether this living actor currently receives the outdoor night bonus. |
| [Enemy.pursuitMultiplier](../src/entities/Enemy.java#L47) | PURSUIT MODIFIER | Raise ordinary movement and boss pursuit by twenty-five percent at night. |
| [Enemy.attackDamage](../src/entities/Enemy.java#L49) | OUTGOING DAMAGE | Add one damage to real attacks at night while preserving zero-damage contact. |
| [Enemy.savePrev](../src/entities/Enemy.java#L51) | FUNCTION | Handle save prev for Enemy. |
| [Enemy.removable](../src/entities/Enemy.java#L53) | FUNCTION | Handle removable for Enemy. |
| [Enemy.claimDefeat](../src/entities/Enemy.java#L55) | OOP: ENCAPSULATION | Claim one defeat reward only after health reaches zero. |
| [Enemy.feetBox](../src/entities/Enemy.java#L60) | FUNCTION | Handle feet box for Enemy. |
| [Enemy.hurtBox](../src/entities/Enemy.java#L62) | FUNCTION | Handle hurt box for Enemy. |
| [Enemy.damage](../src/entities/Enemy.java#L64) | OOP: ENCAPSULATION | Clamp private health and apply knockback plus damage effects. |
| [Enemy.update](../src/entities/Enemy.java#L73) | OOP: POLYMORPHISM / LINKED LIST | Use subclass stats while chasing the flow field or cycling patrol nodes. |
| [Enemy.move](../src/entities/Enemy.java#L126) | FUNCTION | Handle move for Enemy. |
| [Enemy.sortY](../src/entities/Enemy.java#L136) | FUNCTION | Handle sort y for Enemy. |
| [Enemy.draw](../src/entities/Enemy.java#L138) | FUNCTION | Render draw using the current graphics state. |

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

## src/entities/ForestEncounters.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ForestEncounters.ForestEncounters](../src/entities/ForestEncounters.java#L16) | COMPOSITION | Bind the encounter population to one world/session. |
| [ForestEncounters.populate](../src/entities/ForestEncounters.java#L18) | SPAWN POPULATION | Place shrine guards and eight ambient enemies entirely outside the walls. |
| [ForestEncounters.update](../src/entities/ForestEncounters.java#L36) | BOUNDED SPAWN TIMER | Replenish defeated ambient slots only offscreen while Agyu explores the forest. |
| [ForestEncounters.position](../src/entities/ForestEncounters.java#L50) | CACHED SPAWN LOCATION | Separate two enemies inside each of four authored camp clearings. |
| [ForestEncounters.spawn](../src/entities/ForestEncounters.java#L52) | VALIDATED SPAWN | Reject embedded or indoor spawns before constructing the concrete enemy. |

## src/entities/Guardian.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Guardian.Guardian](../src/entities/Guardian.java#L12) | FUNCTION | Initialize Guardian state. |
| [Guardian.speed](../src/entities/Guardian.java#L14) | OOP: OVERRIDING | Supply the guardian movement speed. |
| [Guardian.contactDamage](../src/entities/Guardian.java#L16) | OOP: OVERRIDING | Supply the guardian contact damage. |
| [Guardian.scale](../src/entities/Guardian.java#L18) | OOP: OVERRIDING | Supply the guardian body/hurt-box scale. |
| [Guardian.update](../src/entities/Guardian.java#L20) | OOP: OVERRIDING | Extend shared enemy behavior with the guardian pulse attack. |
| [Guardian.draw](../src/entities/Guardian.java#L33) | OOP: OVERRIDING | Add the pulse warning and boss appearance to shared enemy drawing. |

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
| [Player.Player](../src/entities/Player.java#L38) | FUNCTION | Initialize Player state. |
| [Player.health](../src/entities/Player.java#L43) | OOP: ENCAPSULATION | Read current private health through the damage contract. |
| [Player.maxHealth](../src/entities/Player.java#L45) | OOP: ENCAPSULATION | Read the maximum health allowed for Agyu. |
| [Player.energy](../src/entities/Player.java#L47) | OOP: COMPOSITION | Read current energy from the owned EnergyMeter. |
| [Player.maxEnergy](../src/entities/Player.java#L49) | RESOURCE LIMIT | Read the shared energy capacity. |
| [Player.energyExhausted](../src/entities/Player.java#L51) | FUNCTION | Handle energy exhausted for Player. |
| [Player.dashing](../src/entities/Player.java#L53) | FUNCTION | Handle dashing for Player. |
| [Player.dashCooldown](../src/entities/Player.java#L55) | FUNCTION | Handle dash cooldown for Player. |
| [Player.inventory](../src/entities/Player.java#L57) | FUNCTION | Handle inventory for Player. |
| [Player.useHerb](../src/entities/Player.java#L59) | OOP: ENCAPSULATION / COMPOSITION | Heal missing health only after consuming an inventory herb. |
| [Player.savePrev](../src/entities/Player.java#L67) | FUNCTION | Handle save prev for Player. |
| [Player.airborne](../src/entities/Player.java#L69) | FUNCTION | Handle airborne for Player. |
| [Player.jumpHeight](../src/entities/Player.java#L71) | FUNCTION | Handle jump height for Player. |
| [Player.comboStep](../src/entities/Player.java#L73) | FUNCTION | Handle combo step for Player. |
| [Player.comboName](../src/entities/Player.java#L75) | FUNCTION | Handle combo name for Player. |
| [Player.feetBox](../src/entities/Player.java#L77) | FUNCTION | Handle feet box for Player. |
| [Player.update](../src/entities/Player.java#L80) | PLAYER STATE MACHINE | Resolve input priority, movement, jumps, shield, energy and spear combos. |
| [Player.updateDash](../src/entities/Player.java#L174) | FUNCTION | Advance update dash state with elapsed time. |
| [Player.moveFromInput](../src/entities/Player.java#L200) | FUNCTION | Handle move from input for Player. |
| [Player.facingFor](../src/entities/Player.java#L212) | FUNCTION | Handle facing for for Player. |
| [Player.resetCombo](../src/entities/Player.java#L221) | FUNCTION | Handle reset combo for Player. |
| [Player.beginAttack](../src/entities/Player.java#L226) | COMBAT: COMPOSITION | Start the selected spear move and reset matching combo timers. |
| [Player.approach](../src/entities/Player.java#L238) | FUNCTION | Handle approach for Player. |
| [Player.setState](../src/entities/Player.java#L243) | FUNCTION | Handle set state for Player. |
| [Player.move](../src/entities/Player.java#L246) | FUNCTION | Handle move for Player. |
| [Player.vec](../src/entities/Player.java#L257) | FUNCTION | Handle vec for Player. |
| [Player.blocksFrom](../src/entities/Player.java#L268) | FUNCTION | Handle blocks from for Player. |
| [Player.damage](../src/entities/Player.java#L273) | OOP: ENCAPSULATION | Resolve dash/guard protection, update private health and apply hurt/death state. |
| [Player.sortY](../src/entities/Player.java#L287) | FUNCTION | Handle sort y for Player. |
| [Player.draw](../src/entities/Player.java#L290) | FUNCTION | Render draw using the current graphics state. |

## src/entities/Slime.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Slime.Slime](../src/entities/Slime.java#L8) | FUNCTION | Initialize Slime state. |
| [Slime.speed](../src/entities/Slime.java#L10) | OOP: OVERRIDING | Supply the ordinary enemy movement speed. |
| [Slime.contactDamage](../src/entities/Slime.java#L12) | OOP: OVERRIDING | Supply the ordinary enemy contact damage. |

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

## src/rendering/Assets.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [Assets.load](../src/rendering/Assets.java#L24) | ASSET STARTUP | Load current animation frames, combat bodies, shield poses and village images. |
| [Assets.frames](../src/rendering/Assets.java#L47) | FUNCTION | Handle frames for Assets. |
| [Assets.directional](../src/rendering/Assets.java#L54) | DSA: 2D ARRAY / MIRRORING | Assemble DOWN, UP, LEFT and RIGHT animation arrays. |
| [Assets.load](../src/rendering/Assets.java#L61) | ASSET STARTUP | Load current animation frames, combat bodies, shield poses and village images. |
| [Assets.flip](../src/rendering/Assets.java#L77) | ASSET PREPARATION | Mirror exact RGBA pixels without changing semi-transparent edge colors. |
| [Assets.art](../src/rendering/Assets.java#L88) | ASSET PREPARATION | Allocate one transparent image at art resolution. |
| [Assets.put](../src/rendering/Assets.java#L90) | ASSET PREPARATION | Write an in-bounds opaque pixel to a generated enemy image. |
| [Assets.buildEnemyArt](../src/rendering/Assets.java#L95) | CURRENT ENEMY ART | Generate the two slime frames and their damage-flash variants. |
| [Assets.outline](../src/rendering/Assets.java#L113) | ASSET PREPARATION | Outline the generated enemy silhouette without changing its solid pixels. |
| [Assets.tintWhite](../src/rendering/Assets.java#L124) | ASSET PREPARATION | Cache a white damage-flash silhouette for an enemy frame. |

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

## src/rendering/MountainArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [MountainArt.MountainArt](../src/rendering/MountainArt.java#L17) | CACHE UTILITY | Prevent instances of this shared artwork cache. |
| [MountainArt.load](../src/rendering/MountainArt.java#L19) | CACHE STARTUP | Publish one mountain and four reusable waterfall and mist frames. |
| [MountainArt.mountain](../src/rendering/MountainArt.java#L26) | RENDER LOOKUP | Return the 1024 by 1280 mountain, with the waterfall source at local 512,704. |
| [MountainArt.waterfall](../src/rendering/MountainArt.java#L28) | ANIMATION ARRAY | Return a cached 160 by 448 falling-water phase. |
| [MountainArt.mist](../src/rendering/MountainArt.java#L30) | ANIMATION ARRAY | Return a cached 256 by 96 pool mist phase, centered below the waterfall foot. |
| [MountainArt.buildMountain](../src/rendering/MountainArt.java#L32) | PIXEL ART CACHE | Build an asymmetric range with stratified cliffs, moss ledges and wooded foothills. |
| [MountainArt.texture](../src/rendering/MountainArt.java#L111) | SEEDED ROCK DETAIL | Add muted mineral chips and small fissures inside the opaque mountain silhouette. |
| [MountainArt.buildFall](../src/rendering/MountainArt.java#L122) | CACHED ANIMATION ART | Draw a continuous luminous drop with four displaced streak and splash phases. |
| [MountainArt.buildMist](../src/rendering/MountainArt.java#L149) | CACHED POOL MIST | Cache layered translucent splash clouds and foam ripples beneath the fall. |
| [MountainArt.moss](../src/rendering/MountainArt.java#L166) | PIXEL FOLIAGE | Place a broken moss clump with ochre lichen and a shaded lower edge. |
| [MountainArt.shrub](../src/rendering/MountainArt.java#L173) | PIXEL FOLIAGE | Add a small wooded ledge silhouette with layered highland foliage. |
| [MountainArt.enlarge](../src/rendering/MountainArt.java#L181) | NEAREST PIXEL SCALE | Expand a prepared half-resolution cache without smoothing or interpolation blur. |
| [MountainArt.clamp](../src/rendering/MountainArt.java#L187) | PIXEL PALETTE | Keep textured rock channels in the legal byte range. |
| [MountainArt.polygon](../src/rendering/MountainArt.java#L189) | PIXEL GEOMETRY | Build one integer-pixel silhouette from paired coordinate arrays. |
| [MountainArt.fill](../src/rendering/MountainArt.java#L191) | PIXEL GEOMETRY | Fill a flat facet before applying seeded chips and ledge detail. |
| [MountainArt.color](../src/rendering/MountainArt.java#L193) | PIXEL PALETTE | Select an opaque cached-art palette color. |

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
| [RpgTerrain.RpgTerrain](../src/rendering/RpgTerrain.java#L12) | FUNCTION | Prevent instances of this static helper class. |
| [RpgTerrain.load](../src/rendering/RpgTerrain.java#L14) | FUNCTION | Load or validate load data. |
| [RpgTerrain.distance](../src/rendering/RpgTerrain.java#L44) | FUNCTION | Handle distance for RpgTerrain. |
| [RpgTerrain.tile](../src/rendering/RpgTerrain.java#L53) | RENDER: ARRAY CACHE | Select the prebuilt transition image from world edge masks. |

## src/rendering/VillageArt.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [VillageArt.VillageArt](../src/rendering/VillageArt.java#L14) | FUNCTION | Prevent instances of this static helper class. |
| [VillageArt.load](../src/rendering/VillageArt.java#L16) | FUNCTION | Load or validate load data. |
| [VillageArt.tile](../src/rendering/VillageArt.java#L27) | FUNCTION | Handle tile for VillageArt. |

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
| [DayNightCycle.phaseSeconds](../src/world/DayNightCycle.java#L26) | ENCAPSULATION | Read active seconds elapsed in the current five-minute phase. |
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
| [ForestRegion.store](../src/world/ForestRegion.java#L63) | CONSTRUCTION INDEX | Index one root only in construction buckets that overlap its footprint. |
| [ForestRegion.arenas](../src/world/ForestRegion.java#L68) | ENCAPSULATION | Expose three immutable distant arenas assigned to the villain encounters. |
| [ForestRegion.route](../src/world/ForestRegion.java#L70) | TRAIL WAYPOINTS | Join authored bends into connected paths through each forest region. |
| [ForestRegion.cacheGround](../src/world/ForestRegion.java#L72) | TEXTURE CACHE | Prepare shaded woodland grass once without changing the source images. |
| [ForestRegion.ground](../src/world/ForestRegion.java#L80) | RENDER LOOKUP | Reuse a shaded grass tile without allocating during rendering. |
| [ForestRegion.contains](../src/world/ForestRegion.java#L82) | REGION QUERY | Identify world positions beyond the timber enclosure. |
| [ForestRegion.treeCount](../src/world/ForestRegion.java#L88) | CACHED SCENERY | Report the generated exterior population without traversing it again. |
| [ForestRegion.camp](../src/world/ForestRegion.java#L90) | ENCAPSULATION | Return a copy of an authored outdoor respawn clearing. |
| [ForestRegion.trailAt](../src/world/ForestRegion.java#L92) | CACHED ARRAY LOOKUP | Read a reserved forest trail cell without changing terrain. |
| [ForestRegion.line](../src/world/ForestRegion.java#L94) | TRAIL GENERATION | Paint a three-cell-wide orthogonal woodland trail, preserving gate and wall tiles. |
| [ForestRegion.clearing](../src/world/ForestRegion.java#L100) | CLEARING GENERATION | Reserve dry ground and approach space around trails, camps and shrines. |
| [ForestRegion.reserved](../src/world/ForestRegion.java#L109) | ROOT CLEARANCE | Keep the complete padded root rectangle out of every trail and clearing. |
| [ForestRegion.dry](../src/world/ForestRegion.java#L115) | TERRAIN VALIDATION | Require the entire padded root footprint to lie on grass inside the world. |
| [ForestRegion.touchesVillage](../src/world/ForestRegion.java#L122) | WALL CLEARANCE | Preserve an open strip immediately outside the protected village. |

## src/world/MountainRiver.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [MountainRiver.MountainRiver](../src/world/MountainRiver.java#L23) | TERRAIN CONSTRUCTION | Raise eastern rock and link the waterfall basin to the existing river and western outflow. |
| [MountainRiver.source](../src/world/MountainRiver.java#L45) | ENCAPSULATION | Return a copy of the waterfall basin's river source coordinate. |
| [MountainRiver.bounds](../src/world/MountainRiver.java#L47) | ENCAPSULATION | Return a copy of the full mountain artwork bounds. |
| [MountainRiver.update](../src/world/MountainRiver.java#L49) | SIMULATION CLOCK | Advance animation only when the world simulation is active. |
| [MountainRiver.collect](../src/world/MountainRiver.java#L51) | RENDER CULLING | Add the huge mountain only when its art intersects the camera. |
| [MountainRiver.sortY](../src/world/MountainRiver.java#L53) | SCENERY DEPTH | Draw the solid cliff behind actors walking on its valid shore and viewing deck. |
| [MountainRiver.lookAhead](../src/world/MountainRiver.java#L55) | SCENIC CAMERA | Gently lift the camera beside the basin while keeping Agyu on screen. |
| [MountainRiver.draw](../src/world/MountainRiver.java#L60) | CACHED RENDER | Select prebuilt rock, falling-water and spray frames without mutating simulation. |
| [MountainRiver.rock](../src/world/MountainRiver.java#L70) | CACHED ROCK LOOKUP | Read a generated rock tile without creating images during rendering. |
| [MountainRiver.buildRock](../src/world/MountainRiver.java#L72) | CACHE CONSTRUCTION | Prepare muted rock grain beneath the cached mountain silhouette. |
| [MountainRiver.pool](../src/world/MountainRiver.java#L83) | SOURCE BASIN | Stamp a round connected pool under the falling water. |
| [MountainRiver.channel](../src/world/MountainRiver.java#L88) | RIVER CENTERLINE | Rasterize winding centerline segments with seeded bank-width variation. |
| [MountainRiver.water](../src/world/MountainRiver.java#L105) | HYDRAULIC / WALL SAFETY | Let water pass under timber walls without creating another walkable entrance. |
| [MountainRiver.bridge](../src/world/MountainRiver.java#L116) | SAFE CROSSING | Build one three-cell-wide timber crossing only over exterior water. |
| [MountainRiver.drawCurrents](../src/world/MountainRiver.java#L121) | RIVER MOTION | Animate small downstream streaks on visible open river cells using cached centerline directions. |

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
| [VillageMap.VillageMap](../src/world/VillageMap.java#L28) | FUNCTION | Initialize VillageMap state. |
| [VillageMap.cacheX](../src/world/VillageMap.java#L69) | FUNCTION | Handle cache x for VillageMap. |
| [VillageMap.cacheY](../src/world/VillageMap.java#L71) | FUNCTION | Handle cache y for VillageMap. |
| [VillageMap.draw](../src/world/VillageMap.java#L73) | FUNCTION | Render draw using the current graphics state. |
| [VillageMap.label](../src/world/VillageMap.java#L134) | MAP WAYFINDING | Keep remote clearing and mountain labels inside the full-map viewport. |
| [VillageMap.siteColor](../src/world/VillageMap.java#L140) | FUNCTION | Handle site color for VillageMap. |

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
| [World.Prop](../src/world/World.java#L393) | FUNCTION | Initialize Prop state. |
| [World.collider](../src/world/World.java#L397) | FUNCTION | Expose a static collision footprint through the Obstacle contract. |
| [World.sortY](../src/world/World.java#L399) | FUNCTION | Handle sort y for World. |
| [World.draw](../src/world/World.java#L401) | FUNCTION | Render draw using the current graphics state. |
| [World.Building](../src/world/World.java#L420) | FUNCTION | Initialize Building state. |
| [World.collider](../src/world/World.java#L426) | FUNCTION | Expose a static collision footprint through the Obstacle contract. |
| [World.sortY](../src/world/World.java#L428) | FUNCTION | Handle sort y for World. |
| [World.draw](../src/world/World.java#L430) | FUNCTION | Render draw using the current graphics state. |
| [World.Tree](../src/world/World.java#L443) | ASSET-ALIGNED COLLISION | Cache the sprite/style and place a broad solid root footprint on its actual bark. |
| [World.collider](../src/world/World.java#L449) | STATIC OBSTACLE | Return the cached tree-root collider used by all actors. |
| [World.sortY](../src/world/World.java#L451) | DEPTH SORTING | Sort at the actual roots rather than stray grass below the sprite. |
| [World.draw](../src/world/World.java#L453) | CACHED RENDER | Draw an unoccluded tree for scene exports and generic Drawable callers. |
| [World.draw](../src/world/World.java#L455) | PLAYER VISIBILITY | Fade only the canopy when it hides Agyu; the root/trunk base remains opaque and solid. |

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
| [ComboTests.speed](../tests/ComboTests.java#L37) | TEST HELPER | Handle speed for ComboTests. |
| [ComboTests.contactDamage](../tests/ComboTests.java#L39) | TEST HELPER | Handle contact damage for ComboTests. |
| [ComboTests.update](../tests/ComboTests.java#L41) | TEST HELPER | Advance update state with elapsed time. |
| [ComboTests.main](../tests/ComboTests.java#L47) | TEST HELPER | Entry point for this regression suite. |
| [ComboTests.run](../tests/ComboTests.java#L55) | TEST HELPER | Handle run for ComboTests. |
| [ComboTests.chainChecks](../tests/ComboTests.java#L68) | TEST HELPER | Handle chain checks for ComboTests. |
| [ComboTests.cancelChecks](../tests/ComboTests.java#L121) | TEST HELPER | Handle cancel checks for ComboTests. |
| [ComboTests.damageChecks](../tests/ComboTests.java#L149) | TEST HELPER | Handle damage checks for ComboTests. |
| [ComboTests.renderChecks](../tests/ComboTests.java#L178) | TEST HELPER | Handle render checks for ComboTests. |
| [ComboTests.effectsChecks](../tests/ComboTests.java#L207) | TEST HELPER | Handle effects checks for ComboTests. |
| [ComboTests.previews](../tests/ComboTests.java#L235) | TEST HELPER | Handle previews for ComboTests. |
| [ComboTests.clip](../tests/ComboTests.java#L302) | TEST HELPER | Handle clip for ComboTests. |
| [ComboTests.thirdStage](../tests/ComboTests.java#L336) | TEST HELPER | Handle third stage for ComboTests. |
| [ComboTests.waitFor](../tests/ComboTests.java#L342) | TEST HELPER | Handle wait for for ComboTests. |
| [ComboTests.waitUntilIdle](../tests/ComboTests.java#L350) | TEST HELPER | Handle wait until idle for ComboTests. |
| [ComboTests.attack](../tests/ComboTests.java#L355) | TEST HELPER | Handle attack for ComboTests. |
| [ComboTests.settle](../tests/ComboTests.java#L360) | TEST HELPER | Handle settle for ComboTests. |
| [ComboTests.step](../tests/ComboTests.java#L362) | TEST HELPER | Handle step for ComboTests. |
| [ComboTests.player](../tests/ComboTests.java#L364) | TEST HELPER | Handle player for ComboTests. |
| [ComboTests.posePlayer](../tests/ComboTests.java#L366) | TEST HELPER | Handle pose player for ComboTests. |
| [ComboTests.spear](../tests/ComboTests.java#L370) | TEST HELPER | Handle spear for ComboTests. |
| [ComboTests.render](../tests/ComboTests.java#L374) | TEST HELPER | Handle render for ComboTests. |
| [ComboTests.same](../tests/ComboTests.java#L379) | TEST HELPER | Handle same for ComboTests. |
| [ComboTests.fingerprint](../tests/ComboTests.java#L385) | TEST HELPER | Handle fingerprint for ComboTests. |
| [ComboTests.press](../tests/ComboTests.java#L391) | TEST HELPER | Handle press for ComboTests. |
| [ComboTests.release](../tests/ComboTests.java#L393) | TEST HELPER | Handle release for ComboTests. |
| [ComboTests.mouse](../tests/ComboTests.java#L395) | TEST HELPER | Handle mouse for ComboTests. |
| [ComboTests.check](../tests/ComboTests.java#L400) | TEST HELPER | Handle check for ComboTests. |

## tests/DayNightTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [DayNightTests.DayNightTests](../tests/DayNightTests.java#L25) | TEST UTILITY | Prevent construction of this focused suite. |
| [DayNightTests.main](../tests/DayNightTests.java#L27) | TEST ENTRY | Load the real assets and run the focused cycle checks. |
| [DayNightTests.run](../tests/DayNightTests.java#L29) | REGRESSION SUITE | Verify timing, actual damage/movement, spawns, session controls and lighting. |
| [DayNightTests.clock](../tests/DayNightTests.java#L31) | FIVE-MINUTE BOUNDARIES | Verify numbered phases, exact fixed-step crossings, coarse steps and continuous light transitions. |
| [DayNightTests.phase](../tests/DayNightTests.java#L52) | PHASE FIXTURE | Advance to the next requested phase while retaining the real production interval. |
| [DayNightTests.strength](../tests/DayNightTests.java#L54) | CONTACT / PURSUIT | Compare real day/night contact hits and chase travel, including dawn and village exclusion. |
| [DayNightTests.bosses](../tests/DayNightTests.java#L76) | BOSS NIGHT ATTACKS | Exercise all villain hit paths and the guardian pulse without changing warning times or health. |
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
| [ExpansionTests.hash](../tests/ExpansionTests.java#L164) | PIXEL FINGERPRINT | Compare rendered cache contents without counting every pixel as a separate assertion. |
| [ExpansionTests.tap](../tests/ExpansionTests.java#L166) | INPUT FIXTURE | Dispatch one real key edge into the game panel. |
| [ExpansionTests.get](../tests/ExpansionTests.java#L168) | TEST REFLECTION | Read session state without widening runtime interfaces. |
| [ExpansionTests.set](../tests/ExpansionTests.java#L170) | CAMERA FIXTURE | Position the real interpolated camera for review renders. |
| [ExpansionTests.check](../tests/ExpansionTests.java#L172) | ASSERTION | Count one meaningful property and explain any failure. |

## tests/ForestTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [ForestTests.ForestTests](../tests/ForestTests.java#L22) | TEST UTILITY | Prevent instances of this regression suite. |
| [ForestTests.main](../tests/ForestTests.java#L24) | TEST ENTRY | Load real artwork and run the focused forest checks. |
| [ForestTests.run](../tests/ForestTests.java#L26) | REGRESSION SUITE | Verify deterministic forest construction, navigation, encounters and actual HUD rendering. |
| [ForestTests.openGates](../tests/ForestTests.java#L28) | GATE FIXTURE | Open all four actual gates using normal safe World interaction. |
| [ForestTests.landscape](../tests/ForestTests.java#L37) | WORLD / ROUTING | Test forest density, stable seeded geometry, clear trails and protected entrances. |
| [ForestTests.encounters](../tests/ForestTests.java#L74) | SPAWN LIFECYCLE | Exercise outdoor-only placement, delayed offscreen replenishment and bounded mission-safe slots. |
| [ForestTests.session](../tests/ForestTests.java#L92) | SESSION INPUT | Verify pause/map timer freezing, fresh-session reset and the removed attack card. |
| [ForestTests.previews](../tests/ForestTests.java#L122) | RENDER EVIDENCE | Export the expanded world and an actual forest combat camera without the combo panel. |
| [ForestTests.alive](../tests/ForestTests.java#L141) | TEST QUERY | Count only living members of one spawn group. |
| [ForestTests.tap](../tests/ForestTests.java#L143) | INPUT FIXTURE | Send one fresh keyboard edge through the actual panel input path. |
| [ForestTests.get](../tests/ForestTests.java#L145) | TEST REFLECTION | Inspect private session/cache state without changing the implementation API. |
| [ForestTests.set](../tests/ForestTests.java#L147) | RENDER FIXTURE | Position the real interpolated camera for deterministic scene exports. |
| [ForestTests.check](../tests/ForestTests.java#L149) | ASSERTION | Count one regression assertion and report its concrete failure. |

## tests/GameTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [GameTests.GameTests](../tests/GameTests.java#L21) | TEST HELPER | Prevent instances of this test/helper class. |
| [GameTests.main](../tests/GameTests.java#L23) | TEST HELPER | Entry point for this regression suite. |
| [GameTests.testNavigationEdges](../tests/GameTests.java#L106) | TEST HELPER | Handle test navigation edges for GameTests. |
| [GameTests.testCombat](../tests/GameTests.java#L120) | TEST HELPER | Handle test combat for GameTests. |
| [GameTests.testSprites](../tests/GameTests.java#L138) | TEST HELPER | Handle test sprites for GameTests. |
| [GameTests.testSession](../tests/GameTests.java#L188) | TEST HELPER | Handle test session for GameTests. |
| [GameTests.testPanelMouse](../tests/GameTests.java#L246) | TEST HELPER | Handle test panel mouse for GameTests. |
| [GameTests.testPanelCombos](../tests/GameTests.java#L271) | TEST HELPER | Handle test panel combos for GameTests. |
| [GameTests.mousePanel](../tests/GameTests.java#L291) | TEST HELPER | Handle mouse panel for GameTests. |
| [GameTests.field](../tests/GameTests.java#L297) | TEST HELPER | Handle field for GameTests. |
| [GameTests.render](../tests/GameTests.java#L301) | TEST HELPER | Handle render for GameTests. |
| [GameTests.tap](../tests/GameTests.java#L307) | TEST HELPER | Handle tap for GameTests. |
| [GameTests.press](../tests/GameTests.java#L311) | TEST HELPER | Handle press for GameTests. |
| [GameTests.release](../tests/GameTests.java#L313) | TEST HELPER | Handle release for GameTests. |
| [GameTests.expect](../tests/GameTests.java#L315) | TEST HELPER | Handle expect for GameTests. |
| [GameTests.check](../tests/GameTests.java#L320) | TEST HELPER | Handle check for GameTests. |

## tests/GuardVisualTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [GuardVisualTests.GuardVisualTests](../tests/GuardVisualTests.java#L16) | TEST HELPER | Prevent instances of this test/helper class. |
| [GuardVisualTests.run](../tests/GuardVisualTests.java#L18) | TEST HELPER | Handle run for GuardVisualTests. |
| [GuardVisualTests.check](../tests/GuardVisualTests.java#L79) | TEST HELPER | Handle check for GuardVisualTests. |

## tests/JumpTests.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [JumpTests.JumpTests](../tests/JumpTests.java#L23) | TEST HELPER | Initialize JumpTests state. |
| [JumpTests.run](../tests/JumpTests.java#L25) | TEST HELPER | Handle run for JumpTests. |
| [JumpTests.inputChecks](../tests/JumpTests.java#L36) | TEST HELPER | Handle input checks for JumpTests. |
| [JumpTests.arcChecks](../tests/JumpTests.java#L57) | TEST HELPER | Handle arc checks for JumpTests. |
| [JumpTests.playerChecks](../tests/JumpTests.java#L76) | TEST HELPER | Handle player checks for JumpTests. |
| [JumpTests.panelChecks](../tests/JumpTests.java#L169) | TEST HELPER | Handle panel checks for JumpTests. |
| [JumpTests.preview](../tests/JumpTests.java#L219) | TEST HELPER | Handle preview for JumpTests. |
| [JumpTests.renderPlayer](../tests/JumpTests.java#L244) | TEST HELPER | Handle render player for JumpTests. |
| [JumpTests.same](../tests/JumpTests.java#L251) | TEST HELPER | Handle same for JumpTests. |
| [JumpTests.player](../tests/JumpTests.java#L257) | TEST HELPER | Handle player for JumpTests. |
| [JumpTests.step](../tests/JumpTests.java#L259) | TEST HELPER | Handle step for JumpTests. |
| [JumpTests.field](../tests/JumpTests.java#L263) | TEST HELPER | Handle field for JumpTests. |
| [JumpTests.tap](../tests/JumpTests.java#L267) | TEST HELPER | Handle tap for JumpTests. |
| [JumpTests.press](../tests/JumpTests.java#L269) | TEST HELPER | Handle press for JumpTests. |
| [JumpTests.release](../tests/JumpTests.java#L273) | TEST HELPER | Handle release for JumpTests. |
| [JumpTests.mouse](../tests/JumpTests.java#L277) | TEST HELPER | Handle mouse for JumpTests. |
| [JumpTests.check](../tests/JumpTests.java#L283) | TEST HELPER | Handle check for JumpTests. |

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

## tools/importers/GuardSpriteImporter.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [GuardSpriteImporter.GuardSpriteImporter](../tools/importers/GuardSpriteImporter.java#L20) | ART TOOL | Prevent instances of this static helper class. |
| [GuardSpriteImporter.main](../tools/importers/GuardSpriteImporter.java#L22) | ART TOOL | Entry point for this developer tool. |
| [GuardSpriteImporter.read](../tools/importers/GuardSpriteImporter.java#L69) | ART TOOL | Load or validate read data. |
| [GuardSpriteImporter.bounds](../tools/importers/GuardSpriteImporter.java#L75) | ART TOOL | Handle bounds for GuardSpriteImporter. |
| [GuardSpriteImporter.pack](../tools/importers/GuardSpriteImporter.java#L85) | ART TOOL | Handle pack for GuardSpriteImporter. |

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

## tools/previews/RpgVillagePreviews.java

| Method / constructor | Label | Purpose |
| --- | --- | --- |
| [RpgVillagePreviews.RpgVillagePreviews](../tools/previews/RpgVillagePreviews.java#L18) | ART TOOL | Prevent instances of this static helper class. |
| [RpgVillagePreviews.get](../tools/previews/RpgVillagePreviews.java#L20) | ART TOOL | Handle get for RpgVillagePreviews. |
| [RpgVillagePreviews.set](../tools/previews/RpgVillagePreviews.java#L22) | ART TOOL | Handle set for RpgVillagePreviews. |
| [RpgVillagePreviews.main](../tools/previews/RpgVillagePreviews.java#L24) | ART TOOL | Entry point for this developer tool. |
| [RpgVillagePreviews.place](../tools/previews/RpgVillagePreviews.java#L70) | ART TOOL | Handle place for RpgVillagePreviews. |
| [RpgVillagePreviews.render](../tools/previews/RpgVillagePreviews.java#L81) | ART TOOL | Handle render for RpgVillagePreviews. |
