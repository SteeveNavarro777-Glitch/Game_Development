"""Slices warrior.png into individual transparent frames.
Usage:  python slice_sprites.py res/warrior.png res/agyu
Needs:  pip install pillow numpy scipy
"""
import sys, os
import numpy as np
from PIL import Image
from scipy import ndimage as ndi

src = sys.argv[1] if len(sys.argv) > 1 else "res/warrior.png"
out = sys.argv[2] if len(sys.argv) > 2 else "res/agyu"
os.makedirs(out, exist_ok=True)

im = Image.open(src).convert("RGB")
a = np.array(im).astype(int)
bg = np.array(im.getpixel((5, 5)))            # teal background colour
fg = np.abs(a - bg).sum(2) > 105              # True where sprite pixels are
rgba = np.dstack([a, fg * 255]).astype(np.uint8)
sheet = Image.fromarray(rgba, "RGBA")

def cut(x0, x1, y0, y1, name, CH=170, p=2.95):
    """Crop a region, shrink it to the sheet's TRUE pixel grid (p = source pixels per art pixel),
    and save it with the feet anchored in a fixed cell."""
    m = fg[y0:y1, x0:x1]
    ys, xs = np.where(m)
    if len(ys) == 0: return
    top, bot = ys.min(), ys.max() + 1
    left, right = xs.min(), xs.max() + 1
    feet = xs[ys > bot - 14]                    # centre of the feet = anchor, so frames don't jitter
    ax = int(feet.mean()) - left
    crop = sheet.crop((x0 + left, y0 + top, x0 + right, y0 + bot))
    crop = crop.resize((max(1, round(crop.width / p)), max(1, round(crop.height / p))), Image.NEAREST)
    CW = 240
    cw, ch = round(CW / p), round(CH / p)
    cell = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
    cell.paste(crop, (cw // 2 - round(ax / p), ch - 1 - crop.height), crop)
    cell.save(os.path.join(out, name + ".png"))

def split(x0, x1, y0, y1, n, prefix, hint=None, p=2.95):
    """Split a strip into n frames at the emptiest columns."""
    cols = fg[y0:y1, x0:x1].sum(0)
    cuts = [0]
    for k in range(1, n):
        guess = hint[k - 1] - x0 if hint else int((x1 - x0) * k / n)
        lo, hi = max(guess - 18, cuts[-1] + 20), min(guess + 18, x1 - x0 - 20)
        cuts.append(lo + int(np.argmin(cols[lo:hi])))
    cuts.append(x1 - x0)
    for i in range(n):
        cut(x0 + cuts[i], x0 + cuts[i + 1], y0, y1, f"{prefix}_{i}", p=p)

# big poses
cut(80, 300, 150, 476, "front", CH=340, p=6.7)
cut(372, 580, 150, 476, "back", CH=340, p=6.7)
cut(640, 830, 150, 476, "side", CH=340, p=6.7)
# animation strips
split(25, 340, 570, 725, 3, "idle")
split(380, 785, 570, 725, 4, "walk")
split(815, 1225, 570, 725, 4, "run")
split(25, 450, 800, 945, 3, "attack", hint=[145, 258])
split(470, 690, 780, 945, 2, "jump")
split(750, 970, 835, 945, 2, "hurt")
cut(1010, 1185, 870, 945, "die")
# separate items
cut(80, 130, 1010, 1170, "spear")
cut(180, 275, 1020, 1170, "shield")
print("done ->", out)
