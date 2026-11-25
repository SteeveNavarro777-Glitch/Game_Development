# Artwork credits

- **Agyu:** character artwork supplied by the user; combat and guard additions are documented in COMBAT_ART.md and GUARD_ART.md.
- **Free premade village houses:** Rafael Matos, [Epic RPG World — The Village](https://rafaelmatos.itch.io/epic-rpg-world-the-village-asset-pack). The project's local copy uses the creator's free house download. Consult the creator's license before distribution; it allows game use/modification and prohibits redistribution as standalone assets.
- **Custom village scenery, NPCs and Bukidnon-inspired homes:** generated for this project with the built-in imagegen tool using the user's visual references. Exact prompts and saved source paths are documented in RPG_ART.md.
- **Cultural/architectural context:** NCCA and USTP references linked in VILLAGE.md. The setting, dialogue and visual adaptations are fictional.

Custom runtime sprites are included in res/village; the five current source atlases are in art/source/village. A fresh clone runs offline using custom house alternatives for styles 4-7, with the same ground anchors and matching collision scale.

The optional creator sheet and its packed house sprites stay in the ignored art/local-village folder. They are present in this local project but are not published with the repository. To enable the same optional houses on another machine:

1. Download **Free Premade houses.png** from the creator's page linked above.
2. Save it as **art/local-village/source/Free Premade houses.png** (create the folders).
3. Run `tools.bat RpgAssetImporter` from the project folder, then restart the game.

The importer keeps those four packed creator house files local. It can also regenerate all included custom sprites without the optional sheet.

- **Tank, Mage and Fighter:** sprite sheets supplied by the user on 16 November 2026. Original source PNGs are retained in art/source/bosses; authored frames and source hashes are recorded in res/bosses/frames.csv. No third-party creator attribution was provided with these sheets.

- **Wild boar, white wolf and Sigbin:** 67 individual transparent PNG poses supplied by the user on 16 November 2026 from their BoarCombatEclipse, WolfCombatEclipse and SigbinCombatEclipse projects. Original files remain unchanged in art/source/boar, art/source/wolf and art/source/sigbin. Runtime atlas/portrait manifests record source SHA-256 hashes. No third-party creator attribution was provided with these images.

- **Health, energy and shield potions:** 25 original PNGs supplied by the user from their Healing, Energy and Shield folders on 16 November 2026. All three bottles and 22 animation frames are retained unchanged in `art/source/potions/`; `res/items/frames.csv` records source SHA-256 hashes. No third-party creator attribution was supplied with these files. Runtime gear icons are cropped from the current Agyu spear/guard art.

- **Forward side guards and held-item/drinking bodies:** derived Agyu artwork generated using the built-in imagegen tool on 17 November 2026. Final source PNGs, exact prompts, reference, hashes and reproducible import commands are recorded in [EQUIPMENT_ART.md](EQUIPMENT_ART.md). Original front/rear guard outputs and the existing combat bodies are retained.

## Waterfall and mountain

The user supplied the original Falls.gif animation as a visual and moving-water source. Built-in imagegen produced matching custom cove and mountain terrain. Exact prompts, retained sources, transformation details and SHA-256 records are linked in [WATERFALL_ART.md](WATERFALL_ART.md). The importer prepares source water animation and registered pixel-art atlases; runtime uses those local files.
