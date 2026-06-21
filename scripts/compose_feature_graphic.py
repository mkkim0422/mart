# -*- coding: utf-8 -*-
"""Play Store feature graphic (1024x500) — required graphic asset.
Brand gradient + app icon + name + tagline, matching the screenshot series tone.
Output: docs/playstore_assets/final/feature_graphic_1024x500.png
Run:    py scripts/compose_feature_graphic.py
"""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "docs", "playstore_assets")
OUT = os.path.join(ASSETS, "final")
os.makedirs(OUT, exist_ok=True)

FB = r"C:\Windows\Fonts\malgunbd.ttf"
FR = r"C:\Windows\Fonts\malgun.ttf"

W, H = 1024, 500
BRAND = (49, 130, 246)
T_PRIMARY = (25, 31, 40)
T_SECOND = (78, 89, 104)
RED = (240, 68, 82)
YELLOW = (255, 184, 0)


def font(p, s):
    return ImageFont.truetype(p, s)


def vgrad(w, h, top, bot):
    base = Image.new("RGB", (1, h))
    for y in range(h):
        t = y / max(1, h - 1)
        base.putpixel((0, y), tuple(round(top[i] + (bot[i]-top[i])*t) for i in range(3)))
    return base.resize((w, h))


def round_mask(size, rad):
    m = Image.new("L", size, 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size[0]-1, size[1]-1], radius=rad, fill=255)
    return m


# diagonal-ish soft blue gradient bg (light, matching the series)
canvas = vgrad(W, H, (228, 238, 253), (247, 250, 254)).convert("RGB")
d = ImageDraw.Draw(canvas)

# --- app icon (left), on a white rounded card with soft shadow ---
icon = Image.open(os.path.join(ASSETS, "icon_512.png")).convert("RGBA")
ic = 300
icon = icon.resize((ic, ic), Image.LANCZOS)
ix, iy = 96, (H - ic) // 2
# shadow
sh = Image.new("RGBA", (W, H), (0, 0, 0, 0))
ImageDraw.Draw(sh).rounded_rectangle([ix, iy+14, ix+ic, iy+ic+14], radius=64, fill=(20, 50, 110, 70))
sh = sh.filter(ImageFilter.GaussianBlur(26))
canvas.paste(sh, (0, 0), sh)
# white card behind icon
card = Image.new("RGBA", (ic, ic), (255, 255, 255, 255))
card.putalpha(round_mask((ic, ic), 64))
canvas.paste(card, (ix, iy), card)
icon.putalpha(round_mask((ic, ic), 64))
canvas.paste(icon, (ix, iy), icon)

# --- text block (right) ---
tx = ix + ic + 64
RIGHT_MARGIN = 56
maxw = W - tx - RIGHT_MARGIN


def fit(path, text, start_sz):
    f = font(path, start_sz)
    while d.textlength(text, font=f) > maxw and f.size > 20:
        f = font(path, f.size - 2)
    return f


title = "마트노트"
ftitle = fit(FB, title, 108)
d.text((tx, 132), title, font=ftitle, fill=T_PRIMARY)

# accent dots under title (brand / red / yellow — the per-mart colors)
dy = 132 + int(ftitle.size * 1.18)
for i, col in enumerate((BRAND, RED, YELLOW)):
    cx = tx + 8 + i * 44
    d.ellipse([cx, dy, cx+26, dy+26], fill=col)

# two-line tagline (auto-fit to width)
l1 = "마트별 장보기 리스트"
l2 = "위젯으로 한눈에 · 말로 1초 추가"
f1 = fit(FR, l1, 46)
d.text((tx, dy + 50), l1, font=f1, fill=T_SECOND)
f2 = fit(FB, l2, 36)
d.text((tx, dy + 50 + int(f1.size * 1.35)), l2, font=f2, fill=BRAND)

canvas.save(os.path.join(OUT, "feature_graphic_1024x500.png"), "PNG")
print("saved feature_graphic_1024x500.png", canvas.size)
