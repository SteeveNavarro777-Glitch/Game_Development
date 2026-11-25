# Runtime game images

player/ holds current movement, guard and combat frames. village/ holds houses, NPCs, foliage, props and terrain. bosses/ holds the three villain atlases. wildlife/ holds boar, wolf and Sigbin atlases, a detailed scare portrait and their source manifests. These folders contain only active runtime assets; see ../docs/ASSET_GUIDE.md.

items/ contains three 32px potion icons, a 22-frame effect atlas, the source-hash manifest and `agyu-weapons.png`. PotionArt caches the consumables; SmithShopArt isolates the supplied Rare/Legendary spear and shield catalog icons at startup.

player/equipment/ packs 24 authored body poses with wrist anchors. HeldItemArt caches mirrored left poses, selected gear/bottles and walking-drink composites at startup.

waterfall/ holds the detailed mountain, eight-phase cove/cascade atlases, river textures and a matching water surface mask. WaterfallArt caches them once; large source artwork and the GIF remain outside runtime resources.

The river uses eight native pattern tiles per animation phase, sampled from the pond, plus matching bank strips and a precise tapered-connector mask. Character equipment, guard movement, title preview and dash afterimages share the same prepared body proportions.
