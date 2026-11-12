# Artwork credits

- **Agyu:** character artwork supplied by the user; combat and guard additions are documented in COMBAT_ART.md and GUARD_ART.md.
- **Free premade village houses:** Rafael Matos, [Epic RPG World — The Village](https://rafaelmatos.itch.io/epic-rpg-world-the-village-asset-pack). The project's local copy uses the creator's free house download. Consult the creator's license before distribution; it allows game use/modification and prohibits redistribution as standalone assets.
- **Custom village scenery, NPCs and Bukidnon-inspired homes:** generated for this project with the built-in imagegen tool using the user's visual references. Exact prompts and saved source paths are documented in RPG_ART.md.
- **Cultural/architectural context:** NCCA and USTP references linked in VILLAGE.md. The setting, dialogue and visual adaptations are fictional.

Custom runtime sprites and their five source atlases are included in res/rpg-village. A fresh clone runs offline using custom house alternatives for styles 4-7, with the same ground anchors and matching collision scale.

The optional creator sheet and its packed house sprites stay in the ignored res/rpg-village-local folder. They are present in this local project but are not published with the repository. To enable the same optional houses on another machine:

1. Download **Free Premade houses.png** from the creator's page linked above.
2. Save it as **res/rpg-village-local/source/Free Premade houses.png** (create the folders).
3. Compile the project, then run `java -Djava.awt.headless=true -cp "build;." RpgAssetImporter` from the project folder.

The importer keeps those four packed creator house files local. It can also regenerate all included custom sprites without the optional sheet.
