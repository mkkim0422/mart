# -*- coding: utf-8 -*-
"""Play Store screenshot #07 — voice add ("음성추가").

Renders a faithful 마트노트 home screen (per CLAUDE.md design tokens) WITH the new
음성추가 button next to 추가, frames it in the same phone-mockup + gradient style as
scripts/compose_playstore.py (01..06), and adds a 🎤 speech-bubble overlay so the
voice feature is unmistakable.

Output: docs/playstore_assets/final/07_voice_add.png  (1080x2400)

Why rendered (not a device capture): the test device kept disconnecting and the
emulator ANR'd, so this is generated deterministically to match the series. Swap in
a real capture later (raw/ss7_voice.png + the prep() path) if you want a literal shot.
"""
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "docs", "playstore_assets", "final")
os.makedirs(OUT, exist_ok=True)

FB = r"C:\Windows\Fonts\malgunbd.ttf"   # Bold
FR = r"C:\Windows\Fonts\malgun.ttf"     # Regular
EMOJI = r"C:\Windows\Fonts\seguiemj.ttf"  # Segoe UI Emoji (color glyphs)

CW, CH = 1080, 2400

# ---- design tokens (CLAUDE.md §10, light) ----
WHITE = (255, 255, 255)
SCREEN_BG = (250, 250, 247)     # #FAFAF7 warm off-white (real app bg)
BG_TERT = (242, 244, 246)       # #F2F4F6
DIVIDER = (229, 232, 235)       # #E5E8EB
T_PRIMARY = (25, 31, 40)        # #191F28
T_SECOND = (78, 89, 104)        # #4E5968
T_TERT = (139, 149, 161)        # #8B95A1
BRAND = (49, 130, 246)          # #3182F6
RED = (240, 68, 82)             # #F04452 (다이소)
YELLOW = (255, 184, 0)          # #FFB800 (이마트)


def draw_emoji(d, x, midy, ch, size):
    """Draw a color emoji left-anchored, vertically centered at midy."""
    try:
        f = ImageFont.truetype(EMOJI, size)
        d.text((x, midy), ch, font=f, embedded_color=True, anchor="lm")
        return True
    except Exception:
        return False

# composition copy
TITLE = ["말로 담으면 끝 — 음성으로 추가"]
SUB = "타이핑 없이, “라면 추가” 한마디면 바로 담겨요"


def font(path, sz):
    try:
        return ImageFont.truetype(path, sz)
    except OSError:
        return ImageFont.truetype(FR, sz)


def text_w(d, s, f):
    b = d.textbbox((0, 0), s, font=f)
    return b[2] - b[0]


def draw_center(d, cx, y, s, f, fill):
    w = text_w(d, s, f)
    d.text((cx - w // 2, y), s, font=f, fill=fill)
    b = d.textbbox((0, 0), s, font=f)
    return b[3] - b[1]


def vgrad(w, h, top, bot):
    base = Image.new("RGB", (1, h))
    for y in range(h):
        t = y / max(1, h - 1)
        base.putpixel((0, y), tuple(round(top[i] + (bot[i] - top[i]) * t) for i in range(3)))
    return base.resize((w, h))


def round_rect_mask(size, rad):
    m = Image.new("L", size, 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size[0] - 1, size[1] - 1], radius=rad, fill=255)
    return m


# ---------------- home screen render (no status bar) ----------------
def render_home():
    """Render the home screen content at 1080x2260 (3x of 360x753 dp)."""
    S = 3  # px per dp
    W, H = 1080, 2260
    im = Image.new("RGB", (W, H), SCREEN_BG)
    d = ImageDraw.Draw(im)
    LP = 20 * S

    # title "구매예정"
    f_title = font(FB, 60)
    d.text((LP, 22 * S), "구매예정", font=f_title, fill=T_PRIMARY)

    # ---- tab chips (emoji icon + name + count) ----
    chips = [("🚀", "쿠팡", 5, BRAND, True), ("🏪", "다이소", 3, RED, False),
             ("🛒", "이마트", 1, YELLOW, False)]
    cx = LP
    cy = 68 * S
    ch = 34 * S
    f_chip = font(FB, 40)
    f_badge = font(FR, 30)
    emj = 38
    for icon, name, cnt, col, sel in chips:
        name_w = text_w(d, name, f_chip)
        badge_d = 24 * S
        pad = 14 * S
        gap = 8 * S
        cw_chip = pad + emj + gap + name_w + gap + badge_d + pad
        midy = cy + ch // 2
        if sel:
            d.rounded_rectangle([cx, cy, cx + cw_chip, cy + ch], radius=ch // 2, fill=col)
            tcol, badge_bg, badge_tx = WHITE, WHITE, col
        else:
            d.rounded_rectangle([cx, cy, cx + cw_chip, cy + ch], radius=ch // 2,
                                fill=WHITE, outline=DIVIDER, width=2)
            tcol, badge_bg, badge_tx = T_SECOND, BG_TERT, T_SECOND
        ix = cx + pad
        if not draw_emoji(d, ix, midy, icon, emj):
            d.ellipse([ix, midy - emj // 2, ix + emj, midy + emj // 2], fill=col)
        tx = ix + emj + gap
        d.text((tx, midy), name, font=f_chip, fill=tcol, anchor="lm")
        bx = tx + name_w + gap
        d.ellipse([bx, midy - badge_d // 2, bx + badge_d, midy + badge_d // 2], fill=badge_bg)
        d.text((bx + badge_d // 2, midy), str(cnt), font=f_badge, fill=badge_tx, anchor="mm")
        cx += cw_chip + 8 * S
    # "+ 추가" dashed chip + ⋮ manage chip (like the real tab strip)
    add_w = 30 * S + text_w(d, "추가", f_chip) + 8 * S + 30 * S
    midy = cy + ch // 2
    _dashed_rrect(d, cx, cy, cx + add_w, cy + ch, ch // 2, T_TERT)
    d.text((cx + 16 * S, midy), "＋ 추가", font=f_chip, fill=T_TERT, anchor="lm")
    cx += add_w + 8 * S
    mk = cx + ch // 2
    for off in (-10 * S, 0, 10 * S):
        d.ellipse([mk - 3, midy + off - 3, mk + 3, midy + off + 3], fill=T_TERT)

    # divider under tabs
    dy = cy + ch + 16 * S
    d.line([0, dy, W, dy], fill=DIVIDER, width=2)

    # ---- item rows ----
    items = ["라면", "계란", "우유", "두부", "대파", "양파"]
    f_item = font(FR, 46)
    f_done = font(FR, 34)
    ry = dy + 8 * S
    row_h = 64 * S
    for it in items:
        midy = ry + row_h // 2
        d.text((LP, midy), it, font=f_item, fill=T_PRIMARY, anchor="lm")
        bw_txt = text_w(d, "완료", f_done)
        bpad = 18 * S
        bh = 38 * S
        bx2 = W - LP - 30 * S - 12 * S - (bw_txt + bpad * 2)
        d.rounded_rectangle([bx2, midy - bh // 2, bx2 + bw_txt + bpad * 2, midy + bh // 2],
                            radius=10 * S, fill=WHITE, outline=DIVIDER, width=2)
        d.text((bx2 + bpad, midy), "완료", font=f_done, fill=T_TERT, anchor="lm")
        kx = W - LP - 6 * S
        for off in (-9 * S, 0, 9 * S):
            d.ellipse([kx - 3, midy + off - 3, kx + 3, midy + off + 3], fill=T_TERT)
        ry += row_h

    # ---- bottom nav (구매예정 / 완료 / 설정) ----
    nav_h = 84 * S
    ny = H - nav_h
    d.line([0, ny, W, ny], fill=DIVIDER, width=2)
    f_nav = font(FR, 30)
    tabs = [("", "구매예정", True), ("", "완료", False), ("", "설정", False)]
    labels = ["구매예정", "완료", "설정"]
    for i, lbl in enumerate(labels):
        col = BRAND if i == 0 else T_TERT
        ncx = int(W * (i + 0.5) / 3)
        _nav_icon(d, ncx, ny + 28 * S, i, col)
        d.text((ncx, ny + 58 * S), lbl, font=f_nav, fill=col, anchor="mm")

    # ---- FAB row: 음성추가 (bordered) + 추가 (filled), above nav ----
    fab_h = 56 * S
    fy = ny - 16 * S - fab_h
    f_fab = font(FB, 44)
    rpad = 18 * S

    add_txt = "추가"
    add_tw = text_w(d, add_txt, f_fab)
    plus = 22 * S
    add_inner = 22 * S
    add_w2 = add_inner + plus + 8 * S + add_tw + add_inner
    add_x1 = W - rpad - add_w2
    _shadow_rrect(im, add_x1, fy, add_x1 + add_w2, fy + fab_h, 18 * S)
    d.rounded_rectangle([add_x1, fy, add_x1 + add_w2, fy + fab_h], radius=18 * S, fill=BRAND)
    pcx = add_x1 + add_inner + plus // 2
    pcy = fy + fab_h // 2
    d.line([pcx - plus // 2, pcy, pcx + plus // 2, pcy], fill=WHITE, width=6)
    d.line([pcx, pcy - plus // 2, pcx, pcy + plus // 2], fill=WHITE, width=6)
    d.text((add_x1 + add_inner + plus + 8 * S, pcy), add_txt, font=f_fab, fill=WHITE, anchor="lm")

    v_txt = "음성추가"
    v_tw = text_w(d, v_txt, f_fab)
    mic_w = 26 * S
    v_inner = 20 * S
    v_w = v_inner + mic_w + 8 * S + v_tw + v_inner
    v_x1 = add_x1 - 12 * S - v_w
    _shadow_rrect(im, v_x1, fy, v_x1 + v_w, fy + fab_h, 18 * S)
    d.rounded_rectangle([v_x1, fy, v_x1 + v_w, fy + fab_h], radius=18 * S,
                        fill=WHITE, outline=BRAND, width=4)
    draw_mic(d, v_x1 + v_inner, fy + fab_h // 2, mic_w, BRAND)
    d.text((v_x1 + v_inner + mic_w + 8 * S, fy + fab_h // 2), v_txt, font=f_fab, fill=BRAND,
           anchor="lm")

    return im, (v_x1, fy, v_x1 + v_w, fy + fab_h)


def _dashed_rrect(d, x0, y0, x1, y1, rad, col):
    """Approximate a dashed rounded rect outline (top+bottom dashes + side arcs)."""
    dash = 14
    gap = 10
    x = x0 + rad
    while x < x1 - rad:
        d.line([x, y0, min(x + dash, x1 - rad), y0], fill=col, width=2)
        d.line([x, y1, min(x + dash, x1 - rad), y1], fill=col, width=2)
        x += dash + gap
    d.arc([x0, y0, x0 + rad * 2, y1], 90, 270, fill=col, width=2)
    d.arc([x1 - rad * 2, y0, x1, y1], 270, 90, fill=col, width=2)


def _shadow_rrect(im, x0, y0, x1, y1, rad):
    sh = Image.new("RGBA", im.size, (0, 0, 0, 0))
    ImageDraw.Draw(sh).rounded_rectangle([x0, y0 + 8, x1, y1 + 8], radius=rad, fill=(20, 40, 80, 60))
    sh = sh.filter(ImageFilter.GaussianBlur(12))
    im.paste(sh, (0, 0), sh)


def _nav_icon(d, cx, cy, idx, col):
    if idx == 0:      # list
        for j in range(3):
            yy = cy - 12 + j * 12
            d.ellipse([cx - 26, yy - 2, cx - 22, yy + 2], fill=col)
            d.line([cx - 14, yy, cx + 26, yy], fill=col, width=4)
    elif idx == 1:    # check circle
        d.ellipse([cx - 18, cy - 18, cx + 18, cy + 18], outline=col, width=4)
        d.line([cx - 8, cy + 1, cx - 1, cy + 8], fill=col, width=4)
        d.line([cx - 1, cy + 8, cx + 10, cy - 8], fill=col, width=4)
    else:             # gear (simple)
        d.ellipse([cx - 16, cy - 16, cx + 16, cy + 16], outline=col, width=4)
        d.ellipse([cx - 5, cy - 5, cx + 5, cy + 5], fill=col)


def draw_mic(d, x, cy, w, col):
    """Draw a simple mic glyph centered vertically at cy, left edge x, width ~w."""
    cap_w = int(w * 0.55)
    cap_h = int(w * 0.95)
    cx = x + w // 2
    top = cy - cap_h // 2 - int(w * 0.12)
    # capsule body
    d.rounded_rectangle([cx - cap_w // 2, top, cx + cap_w // 2, top + cap_h],
                        radius=cap_w // 2, fill=col)
    # arc cradle
    arc_w = int(w * 0.92)
    ay0 = top + int(cap_h * 0.35)
    d.arc([cx - arc_w // 2, ay0, cx + arc_w // 2, ay0 + int(arc_w * 0.9)],
          start=20, end=160, fill=col, width=max(3, w // 8))
    # stand + base
    sy = ay0 + int(arc_w * 0.55)
    d.line([cx, sy, cx, sy + int(w * 0.18)], fill=col, width=max(3, w // 8))
    d.line([cx - int(w * 0.22), sy + int(w * 0.18), cx + int(w * 0.22), sy + int(w * 0.18)],
           fill=col, width=max(3, w // 8))


# ---------------- phone mockup (matches compose_playstore.py) ----------------
def mockup(screen_img):
    sw, sh = screen_img.size
    target_w = 686
    scale = target_w / sw
    sw2, sh2 = target_w, int(sh * scale)
    screen = screen_img.resize((sw2, sh2), Image.LANCZOS).convert("RGB")
    bez = 16
    rad_out, rad_in = 78, 60
    dev_w, dev_h = sw2 + bez * 2, sh2 + bez * 2
    dev = Image.new("RGBA", (dev_w, dev_h), (0, 0, 0, 0))
    body = Image.new("RGB", (dev_w, dev_h), (16, 18, 22))
    dev.paste(body, (0, 0))
    dev.putalpha(round_rect_mask((dev_w, dev_h), rad_out))
    sc = screen.copy()
    sc.putalpha(round_rect_mask((sw2, sh2), rad_in))
    dev.paste(sc, (bez, bez), sc)
    dd = ImageDraw.Draw(dev)
    dd.ellipse([dev_w // 2 - 9, bez + 20, dev_w // 2 + 9, bez + 38], fill=(8, 9, 11))
    return dev, scale, bez


def speech_bubble(canvas, anchor_xy):
    """White rounded bubble with a mic + '\"라면\" 추가' pointing down-right toward the mic FAB."""
    d = ImageDraw.Draw(canvas)
    fb = font(FB, 46)
    txt = "“라면” 추가"
    mic_w = 48
    pad = 30
    tw = text_w(d, txt, fb)
    bw = pad + mic_w + 14 + tw + pad
    bh = 100
    bx, by = anchor_xy[0] - bw // 2, anchor_xy[1] - bh - 40
    # shadow
    sh = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    ImageDraw.Draw(sh).rounded_rectangle([bx, by + 10, bx + bw, by + bh + 10],
                                         radius=34, fill=(10, 20, 40, 70))
    sh = sh.filter(ImageFilter.GaussianBlur(18))
    canvas.paste(sh, (0, 0), sh)
    # bubble
    layer = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    ld = ImageDraw.Draw(layer)
    ld.rounded_rectangle([bx, by, bx + bw, by + bh], radius=34, fill=(255, 255, 255, 255))
    # tail (triangle pointing down toward the button)
    tail_cx = bx + int(bw * 0.72)
    ld.polygon([(tail_cx - 22, by + bh - 2), (tail_cx + 22, by + bh - 2),
                (tail_cx + 4, by + bh + 34)], fill=(255, 255, 255, 255))
    canvas.paste(layer, (0, 0), layer)
    d = ImageDraw.Draw(canvas)
    if not draw_emoji(d, bx + pad, by + bh // 2, "🎤", mic_w):
        draw_mic(d, bx + pad, by + bh // 2, 40, BRAND)
    d.text((bx + pad + mic_w + 14, by + bh // 2), txt, font=fb, fill=T_PRIMARY, anchor="lm")


def build():
    bg = vgrad(CW, CH, (233, 240, 252), (247, 250, 254)).convert("RGB")
    canvas = bg
    d = ImageDraw.Draw(canvas)

    # copy block
    y = 132
    ft = font(FB, 70)
    for ln in TITLE:
        while text_w(d, ln, ft) > CW - 150 and ft.size > 44:
            ft = font(FB, ft.size - 3)
    for ln in TITLE:
        h = draw_center(d, CW // 2, y, ln, ft, T_PRIMARY)
        y += int(ft.size * 1.32)
    y += 14
    fs = font(FR, 40)
    while text_w(d, SUB, fs) > CW - 120 and fs.size > 24:
        fs = font(FR, fs.size - 2)
    draw_center(d, CW // 2, y, SUB, fs, T_SECOND)

    # screen + mockup
    screen, fab_box = render_home()
    dev, scale, bez = mockup(screen)

    avail_top = 470
    max_h = CH - avail_top - 70
    extra = 1.0
    if dev.size[1] > max_h:
        extra = max_h / dev.size[1]
        dev = dev.resize((int(dev.size[0] * extra), int(dev.size[1] * extra)), Image.LANCZOS)
    dx = (CW - dev.size[0]) // 2
    dy = avail_top + (max_h - dev.size[1]) // 2

    # shadow under device
    sh = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    ImageDraw.Draw(sh).rounded_rectangle([dx, dy + 18, dx + dev.size[0], dy + dev.size[1] + 18],
                                         radius=80, fill=(10, 20, 40, 90))
    sh = sh.filter(ImageFilter.GaussianBlur(34))
    canvas.paste(sh, (0, 0), sh)
    canvas.paste(dev, (dx, dy), dev)

    # speech bubble anchored above the 음성추가 button
    # map fab_box (in screen px) -> canvas px
    fx = (fab_box[0] + fab_box[2]) / 2
    fy = fab_box[1]
    total_scale = scale * extra
    ax = dx + bez * extra + fx * total_scale
    ay = dy + bez * extra + fy * total_scale
    speech_bubble(canvas, (int(ax), int(ay)))

    out = os.path.join(OUT, "07_voice_add.png")
    canvas.save(out, "PNG")
    print("saved", out, canvas.size)


if __name__ == "__main__":
    build()
    print("DONE")
