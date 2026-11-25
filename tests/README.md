# Regression tests

These files verify gameplay, input, resources, combat, routes, village layout, assets and off-screen rendering. Run test.bat from the project root. Test classes compile separately from the game.

InventoryGuardTests exercises real I-key toggles, repeated presses, scaled menu clicks, paused simulation, safe hotbar assignment/stowing, sip-time use, and shield footfalls in all four facings. WaterfallTests validates source hashes, animation caches, water/cliff geometry and continuous river flow. ExpansionTests exports actual day/night waterfall cameras and checks a reachable dry viewing shore.

PresentationTests compares actual rendered head geometry across all equipment selections in idle/walk/run/guard, checks cached leg motion, matching pond/outlet colors, moving rich water variants and gray-underlay removal, and exports the real river-connection camera.
