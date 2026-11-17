# Artwork sources and records

source/player/ and source/village/ hold current regeneration atlases. prompts/ and provenance/ record their creation and hashes. local-village/ holds only optional creator assets and is ignored by Git. See ../docs/ASSET_GUIDE.md.

source/boar/, source/wolf/ and source/sigbin/ retain the 67 original user-supplied wildlife PNGs; source/bosses/ retains the three villain sheets. WildlifeSpriteImporter and BossSpriteImporter regenerate their runtime atlases.

source/potions/ retains the 25 original supplied health, energy and shield PNGs. PotionSpriteImporter crops three bottle icons and packs all 22 effect frames with original source hashes.

source/player/agyu-guard-forward-v3.png and agyu-held-items-v1.png are the current forward side-guard and 24-pose held-item sources. Exact built-in imagegen prompts are saved in prompts/; provenance/equipment-v1.json records their hashes.
