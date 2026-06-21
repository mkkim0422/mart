# -*- coding: utf-8 -*-
"""Play Store screenshot series (v2) — current version (5 marts + 음성추가).

Real Galaxy S24+ ADB captures (docs/playstore_assets/raw/ss1..ss7) framed in a
phone mockup over a gradient, with a Korean title/subtitle per screen.

Output: docs/playstore_assets/final/01..07  (1080x2400, Play Store series).
Run:    py scripts/compose_store_v2.py
"""
import os, glob
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RAW = os.path.join(ROOT, "docs", "playstore_assets", "raw")
OUT = os.path.join(ROOT, "docs", "playstore_assets", "final")
os.makedirs(OUT, exist_ok=True)

FB = r"C:\Windows\Fonts\malgunbd.ttf"   # Bold (titles)
FR = r"C:\Windows\Fonts\malgun.ttf"     # Regular (subtitles)

CW, CH = 1080, 2400

T_LIGHT, S_LIGHT = (25, 31, 40), (90, 101, 115)
T_DARK,  S_DARK  = (242, 244, 246), (168, 176, 186)


def font(path, sz):
    return ImageFont.truetype(path, sz)


def vgrad(w, h, top, bot):
    base = Image.new("RGB", (1, h))
    for y in range(h):
        t = y / max(1, h - 1)
        base.putpixel((0, y), tuple(round(top[i] + (bot[i]-top[i])*t) for i in range(3)))
    return base.resize((w, h))


def round_rect_mask(size, rad):
    m = Image.new("L", size, 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size[0]-1, size[1]-1], radius=rad, fill=255)
    return m


def text_w(d, s, f):
    b = d.textbbox((0, 0), s, font=f)
    return b[2]-b[0]


def draw_center(d, cx, y, s, f, fill):
    w = text_w(d, s, f)
    d.text((cx - w//2, y), s, font=f, fill=fill)
    b = d.textbbox((0, 0), s, font=f)
    return b[3]-b[1]


def prep(name, ad_top, ad_bot):
    """Crop status bar + below-nav; paint the test-ad band with the screen bg.
    ad_top/ad_bot = None disables ad painting (sheets/keyboard cover the band)."""
    im = Image.open(os.path.join(RAW, name)).convert("RGB")
    W, H = im.size
    cand = [im.getpixel((36, yy)) for yy in (700, 1000, 1300, 1600, 1900)]
    bg = max(set(cand), key=cand.count)
    if ad_top is not None:
        ImageDraw.Draw(im).rectangle([0, ad_top, W, ad_bot], fill=bg)
    im = im.crop((0, 92, W, 3062))
    return im


def mockup(screen_img):
    sw, sh = screen_img.size
    target_w = 686
    scale = target_w / sw
    sw2, sh2 = target_w, int(sh*scale)
    screen = screen_img.resize((sw2, sh2), Image.LANCZOS).convert("RGB")
    bez = 16
    rad_out, rad_in = 78, 60
    dev_w, dev_h = sw2 + bez*2, sh2 + bez*2
    dev = Image.new("RGBA", (dev_w, dev_h), (0, 0, 0, 0))
    body = Image.new("RGB", (dev_w, dev_h), (16, 18, 22))
    dev.paste(body, (0, 0))
    dev.putalpha(round_rect_mask((dev_w, dev_h), rad_out))
    sc = screen.copy(); sc.putalpha(round_rect_mask((sw2, sh2), rad_in))
    dev.paste(sc, (bez, bez), sc)
    dd = ImageDraw.Draw(dev)
    dd.ellipse([dev_w//2-9, bez+20, dev_w//2+9, bez+38], fill=(8, 9, 11))
    return dev


# raw, ad band (top,bot) or None, dark, title lines, subtitle
SHOTS = [
    dict(out="01_main_hook.png", raw="ss1_home.png", ad=(2622, 2872), dark=False,
         title=["마트별로 따로 — 헷갈리지 않게"],
         sub="쿠팡·다이소·이마트·홈플러스·마켓컬리, 한 번에"),
    dict(out="02_voice_add.png", raw="ss7_voice.png", ad=None, dark=False,
         title=["말로 담으면 끝", "음성으로 1초 추가"],
         sub="타이핑 없이, 말 한마디면 바로 담겨요"),
    dict(out="03_1sec_input.png", raw="ss4_add.png", ad=None, dark=False,
         title=["이름만 적으면 끝, 1초"],
         sub="가격도 수량도 필요 없어요"),
    dict(out="04_widget.png", raw="ss3_picker.png", ad=None, dark=False,
         title=["홈화면 위젯, 5가지 크기"],
         sub="마트 가기 전 한 번 보고 출발하세요"),
    dict(out="05_completed.png", raw="ss5_completed.png", ad=(2622, 2872), dark=False,
         title=["체크하면 깔끔하게 정리"],
         sub="오늘·어제·이번 주, 한눈에"),
    dict(out="06_marts.png", raw="ss2_home.png", ad=(2622, 2872), dark=False,
         title=["마트마다 색으로 구분"],
         sub="이마트·홈플러스·마켓컬리까지 자유롭게 추가"),
    dict(out="07_dark.png", raw="ss6_dark.png", ad=(2622, 2872), dark=True,
         title=["다크 모드도 자연스럽게"],
         sub="눈이 편한 밤에도 편하게"),
]


def build(s):
    dark = s["dark"]
    if dark:
        bg = vgrad(CW, CH, (14, 22, 38), (20, 22, 27))
        tcol, scol = T_DARK, S_DARK
    else:
        bg = vgrad(CW, CH, (233, 240, 252), (247, 250, 254))
        tcol, scol = T_LIGHT, S_LIGHT
    canvas = bg.convert("RGB")
    d = ImageDraw.Draw(canvas)

    # copy block
    y = 132
    ftitle = font(FB, 70)
    for ln in s["title"]:
        while text_w(d, ln, ftitle) > CW - 150 and ftitle.size > 44:
            ftitle = font(FB, ftitle.size - 3)
    for ln in s["title"]:
        draw_center(d, CW//2, y, ln, ftitle, tcol)
        y += int(ftitle.size * 1.32)
    y += 14
    fsub = font(FR, 40)
    while text_w(d, s["sub"], fsub) > CW - 120 and fsub.size > 24:
        fsub = font(FR, fsub.size - 2)
    draw_center(d, CW//2, y, s["sub"], fsub, scol)

    # screen
    screen = prep(s["raw"], (s["ad"][0] if s["ad"] else None),
                  (s["ad"][1] if s["ad"] else None))
    dev = mockup(screen)

    avail_top = 470
    max_h = CH - avail_top - 70
    if dev.size[1] > max_h:
        r = max_h / dev.size[1]
        dev = dev.resize((int(dev.size[0]*r), int(dev.size[1]*r)), Image.LANCZOS)
    dx = (CW - dev.size[0]) // 2
    dy = avail_top + (max_h - dev.size[1]) // 2

    sh = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    ImageDraw.Draw(sh).rounded_rectangle([dx, dy+18, dx+dev.size[0], dy+dev.size[1]+18],
                                         radius=80, fill=(10, 20, 40, 90 if not dark else 130))
    sh = sh.filter(ImageFilter.GaussianBlur(34))
    canvas.paste(sh, (0, 0), sh)
    canvas.paste(dev, (dx, dy), dev)

    canvas.save(os.path.join(OUT, s["out"]), "PNG")
    print("saved", s["out"], "device", dev.size)


if __name__ == "__main__":
    # clear stale finals so the series is exactly SHOTS
    for p in glob.glob(os.path.join(OUT, "*.png")):
        os.remove(p)
    for s in SHOTS:
        build(s)
    print("ALL DONE ->", OUT)
