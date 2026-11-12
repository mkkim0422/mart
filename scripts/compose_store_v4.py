# -*- coding: utf-8 -*-
"""Play Store screenshot series v4 (v1.4.0) — 위젯이 주인공.

앱을 만든 이유("나에게 보내기에 적으면 묻힌다")와 가장 큰 장점(홈 화면 위젯)에
맞춰 다시 짠 세트. 다크 모드·음성 같은 곁가지 화면은 뺐다.

홈 화면·위젯은 실기기 캡처 대신 widget/common/WidgetCommon.kt의 실제 치수
(모서리 22dp, 마트 칸 여백 10dp, 머리글 배지 24dp/7dp, 항목 14sp 등)를 따라
그린다 — 사용자 배경화면·다른 앱 아이콘이 스토어 이미지에 들어가지 않게.

이미지·문구에 "카톡/카카오" 같은 타사 상표는 쓰지 않는다(Play 메타데이터 정책).

Output: docs/playstore_assets/v4_widget/01..05 (1080x2400)
Run:    python scripts/compose_store_v4.py
"""
import os

from PIL import Image, ImageDraw, ImageFilter, ImageFont

import compose_store_v2 as v2

ROOT = v2.ROOT
OUT = os.path.join(ROOT, "docs", "playstore_assets", "v4_widget")
os.makedirs(OUT, exist_ok=True)

FB, FR = v2.FB, v2.FR
FE = r"C:\Windows\Fonts\seguiemj.ttf"  # color emoji

D = 3.5  # px per dp on a 1440px-wide (S24+) screen
SW, SH = 1440, 2970  # screen area after the status-bar crop v2.prep uses


def dp(v):
    return int(round(v * D))


def f(path, sp):
    return ImageFont.truetype(path, dp(sp))


def hexc(h):
    h = h.lstrip("#")
    return tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def mix(c, a, bg=(255, 255, 255)):
    return tuple(round(bg[i] + (c[i] - bg[i]) * a) for i in range(3))


TEXT = hexc("#191F28")
TEXT2 = hexc("#4E5968")
TEXT3 = hexc("#8B95A1")
DIVIDER = hexc("#E5E8EB")
DOT = hexc("#C9CDD2")

COUPANG = dict(name="쿠팡", emoji="🚀", color=hexc("#3182F6"),
               items=["생수 2L", "물티슈", "키친타월", "세탁세제", "햇반", "라면", "치약"])
DAISO = dict(name="다이소", emoji="🏪", color=hexc("#F04452"),
             items=["건전지", "수세미", "지퍼백", "옷걸이", "테이프"])
EMART = dict(name="이마트", emoji="🛒", color=hexc("#FFB800"),
             items=["우유", "계란", "두부", "대파", "삼겹살", "양파"])
KURLY = dict(name="마켓컬리", emoji="🥬", color=hexc("#8B5CF6"),
             items=["샐러드", "그릭요거트", "블루베리"])


def emoji(img, x, y, ch, size_dp):
    """Color emoji, [size_dp] tall, centered at (x, y)."""
    fe = ImageFont.truetype(FE, 109)  # seguiemj renders crisp at its bitmap size
    tmp = Image.new("RGBA", (160, 160), (0, 0, 0, 0))
    ImageDraw.Draw(tmp).text((80, 80), ch, font=fe, embedded_color=True, anchor="mm")
    tmp = tmp.crop(tmp.getbbox())
    s = dp(size_dp)
    r = s / max(tmp.size)
    tmp = tmp.resize((max(1, int(tmp.size[0] * r)), max(1, int(tmp.size[1] * r))), Image.LANCZOS)
    img.paste(tmp, (int(x - tmp.size[0] / 2), int(y - tmp.size[1] / 2)), tmp)


def text(d, xy, s, font, fill, anchor="lm"):
    d.text(xy, s, font=font, fill=fill, anchor=anchor)


# ---------- widget pieces ----------

def card(img, x, y, w, h):
    sh = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(sh).rounded_rectangle([x, y + dp(2), x + w, y + h + dp(2)], radius=dp(22),
                                         fill=(0, 0, 0, 40))
    sh = sh.filter(ImageFilter.GaussianBlur(dp(6)))
    img.alpha_composite(sh)
    ImageDraw.Draw(img).rounded_rectangle([x, y, x + w, y + h], radius=dp(22), fill=(255, 255, 255))


def badge(img, d, x, cy, mart, box=24, esp=15):
    b = dp(box)
    d.rounded_rectangle([x, cy - b // 2, x + b, cy + b // 2], radius=dp(7), fill=mix(mart["color"], 0.12))
    emoji(img, x + b / 2, cy, mart["emoji"], esp)
    return x + b


def count_pill(d, right, cy, mart, n, sp=11):
    fnt = f(FB, sp)
    s = str(n)
    tw = d.textlength(s, font=fnt)
    w = max(dp(22), tw + dp(12))
    h = dp(20)
    d.rounded_rectangle([right - w, cy - h / 2, right, cy + h / 2], radius=h / 2, fill=mix(mart["color"], 0.14))
    text(d, (right - w / 2, cy), s, fnt, mart["color"], anchor="mm")


def mart_column(img, x, y, w, h, mart, max_items, pad=10, item_sp=14):
    d = ImageDraw.Draw(img)
    p = dp(pad)
    cx, cy = x + p, y + p + dp(12)
    nx = badge(img, d, cx, cy, mart) + dp(8)
    text(d, (nx, cy), mart["name"], f(FB, 13), TEXT2)
    count_pill(d, x + w - p, cy, mart, len(mart["items"]))
    hy = y + p + dp(24) + dp(10)
    d.line([x + p, hy, x + w - p, hy], fill=DIVIDER, width=dp(1))
    row = dp(item_sp * 1.9)
    iy = hy + dp(6) + row // 2
    shown = mart["items"][:max_items]
    for name in shown:
        if iy + row // 2 > y + h - p:
            break
        r = dp(3)
        d.ellipse([x + p + dp(2), iy - r, x + p + dp(2) + 2 * r, iy + r], fill=DOT)
        text(d, (x + p + dp(14), iy), name, f(FR, item_sp), TEXT)
        iy += row
    rest = len(mart["items"]) - len(shown)
    if rest > 0 and iy + row // 2 <= y + h - p:
        text(d, (x + p + dp(14), iy), f"+ {rest}개 더", f(FR, 12), TEXT3)


def widget_split(img, x, y, w, h, left, right, max_items=6):
    card(img, x, y, w, h)
    d = ImageDraw.Draw(img)
    pad = dp(8)
    iw = (w - 2 * pad - dp(1)) // 2
    mart_column(img, x + pad, y + pad, iw, h - 2 * pad, left, max_items)
    d.line([x + pad + iw, y + pad + dp(10), x + pad + iw, y + h - pad - dp(10)], fill=DIVIDER, width=dp(1))
    mart_column(img, x + pad + iw + dp(1), y + pad, iw, h - 2 * pad, right, max_items)


def widget_single(img, x, y, w, h, mart, max_items=8):
    card(img, x, y, w, h)
    pad = dp(8)
    mart_column(img, x + pad, y + pad, w - 2 * pad, h - 2 * pad, mart, max_items)


def widget_counts(img, x, y, w, h, marts):
    card(img, x, y, w, h)
    d = ImageDraw.Draw(img)
    pad = dp(14)
    n = len(marts)
    step = (h - 2 * pad) / n
    for i, m in enumerate(marts):
        cy = int(y + pad + step * (i + 0.5))
        nx = badge(img, d, x + pad, cy, m, box=24, esp=15) + dp(8)
        text(d, (nx, cy), m["name"], f(FB, 13), TEXT2)
        count_pill(d, x + w - pad, cy, m, len(m["items"]))


# ---------- home screen ----------

def wallpaper():
    # Mid-tone (One UI default-wallpaper feel) so white labels/clock stay readable.
    top, bot = hexc("#7C9CCB"), hexc("#B98FB4")
    return v2.vgrad(SW, SH, top, bot).convert("RGBA")


def status_bar(img):
    d = ImageDraw.Draw(img)
    text(d, (dp(22), dp(16)), "9:41", f(FB, 14), (255, 255, 255))


# Galaxy (One UI) stock icons, cut from a real S24+ app drawer (docs/playstore_assets/galaxy_icons/),
# plus our own launcher icon (martnote.png = icon_512 with the same squircle mask).
ICONS = os.path.join(ROOT, "docs", "playstore_assets", "galaxy_icons")
GRID = [("calendar", "캘린더"), ("clock", "시계"), ("martnote", "마트노트"), ("files", "내 파일")]
DOCK = [("phone", None), ("messages", None), ("gallery", None), ("settings", None)]


def icon_row(img, y, apps):
    """Galaxy stock icons (labels only on the grid row, like One UI)."""
    d = ImageDraw.Draw(img)
    gap = SW / len(apps)
    size = dp(58)
    for i, (key, label) in enumerate(apps):
        cx = gap * (i + 0.5)
        ic = Image.open(os.path.join(ICONS, key + ".png")).convert("RGBA").resize((size, size), Image.LANCZOS)
        img.alpha_composite(ic, (int(cx - size / 2), int(y - size / 2)))
        if label:
            fnt = f(FR, 12)
            ly = y + size / 2 + dp(12)
            sh = Image.new("RGBA", img.size, (0, 0, 0, 0))
            text(ImageDraw.Draw(sh), (cx, ly + dp(0.5)), label, fnt, (0, 0, 0, 150), anchor="mm")
            img.alpha_composite(sh.filter(ImageFilter.GaussianBlur(dp(1))))
            text(d, (cx, ly), label, fnt, (255, 255, 255), anchor="mm")


def dock(img):
    icon_row(img, SH - dp(200), GRID)
    icon_row(img, SH - dp(72), DOCK)


def home(widgets):
    img = wallpaper()
    status_bar(img)
    for fn in widgets:
        fn(img)
    dock(img)
    return img.convert("RGB")


M = 16  # side margin (dp)
FULL_W = 411 - 2 * M


def hero_screen():
    return home([
        lambda im: widget_split(im, dp(M), dp(70), dp(FULL_W), dp(250), COUPANG, EMART, 7),
        lambda im: widget_counts(im, dp(M), dp(340), dp((FULL_W - 12) / 2), dp(170), [DAISO, KURLY, EMART]),
        lambda im: widget_single(im, dp(M + (FULL_W + 12) / 2), dp(340), dp((FULL_W - 12) / 2), dp(170), DAISO, 4),
    ])


def sizes_screen():
    half = (FULL_W - 12) / 2
    return home([
        lambda im: widget_counts(im, dp(M), dp(70), dp(half), dp(80), [COUPANG, DAISO]),
        lambda im: widget_counts(im, dp(M), dp(166), dp(half), dp(160), [COUPANG, DAISO, EMART]),
        lambda im: widget_single(im, dp(M + half + 12), dp(70), dp(half), dp(256), EMART, 7),
        lambda im: widget_split(im, dp(M), dp(342), dp(FULL_W), dp(170), COUPANG, DAISO, 4),
    ])


def chat_screen():
    """Generic messenger 'note to self' — not any real app's UI."""
    img = Image.new("RGB", (SW, SH), hexc("#E9EEF5"))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, SW, dp(64)], fill=hexc("#F7F8FA"))
    text(d, (dp(20), dp(34)), "나에게 보내기", f(FB, 17), TEXT)
    msgs = [
        ("me", "이번 주 회의 자료 링크"),
        ("me", "https://docs.example.com/…"),
        ("me", "사진 3장"),
        ("me", "우유, 계란, 두부, 대파"),
        ("me", "택배 송장번호 6043-2219-8812"),
        ("me", "사진"),
        ("me", "치과 예약 목요일 2시"),
        ("me", "아이 준비물: 색종이, 풀"),
        ("me", "https://blog.example.com/…"),
        ("me", "영수증.pdf"),
        ("me", "적금 만기 확인"),
    ]
    y = dp(84)
    fnt = f(FR, 15)
    for i, (_, s) in enumerate(msgs):
        tw = d.textlength(s, font=fnt)
        w, h = tw + dp(28), dp(42)
        x2 = SW - dp(16)
        hl = s.startswith("우유")
        d.rounded_rectangle([x2 - w, y, x2, y + h], radius=dp(14),
                            fill=hexc("#FFFFFF") if not hl else hexc("#FFF4D6"))
        text(d, (x2 - w + dp(14), y + h / 2), s, fnt, TEXT)
        y += h + dp(12)
    return img


# ---------- compositing ----------

def device(screen):
    return v2.mockup(screen)


def canvas_with(title, sub):
    canvas = v2.vgrad(v2.CW, v2.CH, (233, 240, 252), (247, 250, 254)).convert("RGB")
    d = ImageDraw.Draw(canvas)
    y = 132
    ft = v2.font(FB, 70)
    for ln in title:
        while v2.text_w(d, ln, ft) > v2.CW - 150 and ft.size > 44:
            ft = v2.font(FB, ft.size - 3)
    for ln in title:
        v2.draw_center(d, v2.CW // 2, y, ln, ft, v2.T_LIGHT)
        y += int(ft.size * 1.32)
    y += 14
    fs = v2.font(FR, 40)
    while v2.text_w(d, sub, fs) > v2.CW - 120 and fs.size > 24:
        fs = v2.font(FR, fs.size - 2)
    v2.draw_center(d, v2.CW // 2, y, sub, fs, v2.S_LIGHT)
    return canvas


def place(canvas, dev, top=470, cx=None, max_h=None):
    max_h = max_h or v2.CH - top - 70
    if dev.size[1] > max_h:
        r = max_h / dev.size[1]
        dev = dev.resize((int(dev.size[0] * r), int(dev.size[1] * r)), Image.LANCZOS)
    cx = cx if cx is not None else v2.CW // 2
    dx = cx - dev.size[0] // 2
    dy = top + (max_h - dev.size[1]) // 2
    sh = Image.new("RGBA", canvas.size, (0, 0, 0, 0))
    ImageDraw.Draw(sh).rounded_rectangle([dx, dy + 18, dx + dev.size[0], dy + dev.size[1] + 18],
                                         radius=80, fill=(10, 20, 40, 90))
    sh = sh.filter(ImageFilter.GaussianBlur(34))
    canvas.paste(sh, (0, 0), sh)
    canvas.paste(dev, (dx, dy), dev)
    return dx, dy, dev.size


def save(canvas, name):
    canvas.save(os.path.join(OUT, name), "PNG")
    print("saved", name)


def shot_single(name, title, sub, screen):
    c = canvas_with(title, sub)
    place(c, device(screen))
    save(c, name)


def shot_compare(name, title, sub):
    c = canvas_with(title, sub)
    d = ImageDraw.Draw(c)
    left = device(chat_screen())
    right = device(hero_screen())
    w = 490
    for img, cx, label in ((left, 280, "메신저에 적어두면"), (right, 800, "마트노트 위젯")):
        r = w / img.size[0]
        img = img.resize((w, int(img.size[1] * r)), Image.LANCZOS)
        place(c, img, top=860, cx=cx, max_h=img.size[1])
        v2.draw_center(d, cx, 760, label, v2.font(FB, 40),
                       v2.S_LIGHT if cx < 500 else hexc("#3182F6"))
    save(c, name)


def shot_raw(name, title, sub, raw, ad=None):
    c = canvas_with(title, sub)
    scr = v2.prep(raw, ad[0] if ad else None, ad[1] if ad else None)
    place(c, device(scr))
    save(c, name)


if __name__ == "__main__":
    shot_single("01_widget_home.png",
                ["마트 와서", "뭐 사러 왔더라?"],
                "폰만 켜면 홈 화면에 다 적혀 있어요",
                hero_screen())
    shot_compare("02_vs_memo.png",
                 ["나에게 보내기로 보낸 장보기", "또 찾고 계셨죠?"],
                 "홈 화면에 붙여두면 스크롤할 일이 없어요")
    shot_single("03_widget_sizes.png",
                ["크기는 마음 가는 대로"],
                "작게 두면 개수만, 크게 두면 목록까지 보여요",
                sizes_screen())
    shot_raw("04_name_only.png",
             ["생각날 때", "이름만 톡 적으면 끝"],
             "가격이나 개수는 안 적어도 괜찮아요",
             "ss4_add.png")
    shot_raw("05_done.png",
             ["다 산 건 '완료' 한 번"],
             "목록에서 쏙 빠지고, 산 건 따로 모여요",
             "ss5_completed.png", ad=(2622, 2872))
    print("ALL DONE ->", OUT)
