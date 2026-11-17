# Runtime game images

player/ holds current movement, guard and combat frames. village/ holds houses, NPCs, foliage, props and terrain. bosses/ holds the three villain atlases. wildlife/ holds boar, wolf and Sigbin atlases, a detailed scare portrait and their source manifests. These folders contain only active runtime assets; see ../docs/ASSET_GUIDE.md.

items/ contains three 32px potion icons, a 22-frame effect atlas and the source-hash manifest. PotionArt caches these and derives the spear/wooden-shield icons from current equipment art.

player/equipment/ packs 24 authored body poses with wrist anchors. HeldItemArt caches mirrored left poses, selected gear/bottles and walking-drink composites at startup.
