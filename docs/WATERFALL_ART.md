# Mountain waterfall and connected pond

The eastern mountain now uses detailed pixel artwork, an upper cascade, a lower cliff fall and an open receiving pond. Its irregular banks include vines, ferns, lily pads and a sandy southern shore. A southwest outlet joins the original cross-map river. The old jetty and procedural polygon mountain/fall are retired. Tree generation reserves the cove and its approach; its water and cliffs remain solid, while the viewing shore is reachable from the village.

## Source images and exact prompts

- User animation, preserved unchanged: [Falls.gif](../art/source/waterfall/Falls.gif), supplied from C:/Users/User/Downloads/Falls.gif.
- Cove terrain, generated with the **built-in imagegen tool**: [cove-v1.png](../art/source/waterfall/cove-v1.png). Exact prompt: [waterfall-cove-v1.txt](../art/prompts/waterfall-cove-v1.txt).
- Matching detailed mountain, generated with the **built-in imagegen tool**: [mountain-v1.png](../art/source/waterfall/mountain-v1.png). Exact prompt: [waterfall-mountain-v1.txt](../art/prompts/waterfall-mountain-v1.txt).
- Sources, hashes, reference inputs and generation mode: [waterfall-v1.json](../art/provenance/waterfall-v1.json).

The generated mountain uses the former whole-mountain preview and new cove as composition references, together with a frame of the supplied GIF. The cove uses the supplied waterfall scene as its art reference. User references are visual inputs, not project instructions.

## Reproducible preparation

~~~powershell
.\tools.bat WaterfallSpriteImporter
~~~

[WaterfallSpriteImporter](../tools/importers/WaterfallSpriteImporter.java) composites the GIF's delta rectangles using their offsets and disposal rules. It samples frames 0, 3, 6, 9, 12, 15, 18 and 21, registers the moving cascade inside the painted water silhouette, and preserves the original shoreline and lily-pad details. The native cove is 512 x 384. Its fall center is x260, lip y76 and foot y171; its southwest outlet reaches the left edge around y338.

The mountain is registered at 512 x 640 and enlarged to 1024 x 1280 with nearest-neighbor pixels. Runtime PNGs live in [res/waterfall](../res/waterfall/manifest.csv): one mountain, an eight-phase cove atlas, upper/lower cascade atlases, a small river texture atlas and a physical surface mask. The runtime does not decode the large GIF or generation sources.

[WaterfallArt](../src/rendering/WaterfallArt.java) validates and slices these caches once. [MountainRiver](../src/world/MountainRiver.java) uses the same scene registration for terrain and drawing, and the same full bounds for culling and map placement. The surface mask includes small lily-pad islands as water. Hydraulic BFS verifies continuous flow from the pond through protected village culverts to the western outlet. Drawing is read-only; animation advances only during active gameplay.

The scenic camera lift is limited to 84 art pixels so Agyu stays above the bottom interface. Day and night previews, native artwork, registered collisions, source hashes and cached animation are covered by WaterfallTests and ExpansionTests. Preview outputs are ignored build artifacts.

## River connection revision - 18 November 2026

The river now samples a clean native patch from the cove itself: x224,y204,32 x 64 pixels. Mirroring prepares a seamless 64 x 128 pattern with eight 32-pixel variants per phase. Westward movement advances eight pixels per phase, or 48 art pixels per second. Matching rock/moss bank strips come from the actual narrow outlet edge. The former unrelated green-water crop and simple line-current overlay are retired.

A cached 192 x 288 connector starts at cove-relative (-172,+235). It uses the measured outlet center y338 and half width22.5, then gradually widens to a 96-pixel stream. Exterior river channels keep the same three-cell width as the village river, without random wide pools. Exposed water corners use rounded bank distance.

The connector and its surface mask share world registration. Its central water pixels match the cove outlet, with a blend inside the original art. Grass is drawn beneath the precise cove/connector masks, rather than allowing rectangular water tiles to show through transparent corners. Obsolete spill-water cells are cleared, and mountain edges use forest ground beneath the opaque cliff art instead of gray square underlay.

The current runtime set has nine PNGs plus its manifest, including banks.png, mouth.png and mouth-surface.png. Drawing uses cached phases; no image preparation occurs during play. PresentationTests exports build/preview-river-connection.png and verifies matching colors, rich animated variants, water collision and removal of gray underlay.
