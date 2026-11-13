# -*- coding: utf-8 -*-
"""Play Store screenshot series v5 (v1.4.0) — 위젯 2종 + 위젯에서 바로 체크.

v4와 같은 틀(배경·폰 프레임·갤럭시 기본 아이콘·문구 위치)에, 위젯 그림만 지금
위젯으로 바꿨다: 목록 위젯(마트 머리글 + ○ 줄 + "+" 하나, 전체 마트는 위아래로
이어짐)과 개수 위젯(2x1, 마트별 개수). 두 마트를 나란히 놓은 옛 위젯은 없앴다.

치수는 widget/common/WidgetCommon.kt · widget/components/WidgetItemRow.kt 기준
(○ 20dp / 줄 36dp / 항목 14sp / "+" 36dp / 체크 중 = 초록 ✓ + 회색 취소선).

이미지·문구에 "카톡/카카오" 같은 타사 상표는 쓰지 않는다(Play 메타데이터 정책).

Output: docs/playstore_assets/v5_store/01..06 (1080x2400) + feature_graphic_1024x500.png
Run:    python scripts/compose_store_v5.py
"""
import os

from PIL import Image, ImageChops, ImageDraw, ImageFilter

import compose_store_v2 as v2
import compose_store_v4 as v4
from compose_store_v4 import (COUPANG, DAISO, DIVIDER, DOT, EMART, FB, FR, M, FULL_W, SH, SW,
                              TEXT, TEXT2, TEXT3, badge, card, count_pill, dp, f, hexc, home,
                              text, widget_counts)

OUT = os.path.join(v2.ROOT, "docs", "playstore_assets", "v5_store")
os.makedirs(OUT, exist_ok=True)
v4.OUT = OUT  # v4.save writes here

BRAND = hexc("#3182F6")
GREEN = hexc("#2BA471")


# ---------- 목록 위젯 ----------

def ring(d, cx, cy):
    r = dp(9.25)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=DOT, width=dp(1.5))


def check_on(d, cx, cy):
    r = dp(10)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=GREEN)
    d.line([(cx - dp(4.5), cy + dp(0.2)), (cx - dp(1.2), cy + dp(3.5)), (cx + dp(4.8), cy - dp(3.5))],
           fill=(255, 255, 255), width=dp(2), joint="curve")


def fab(d, x2, y2):
    s = dp(36)
    x, y = x2 - s, y2 - s
    d.ellipse([x, y, x + s, y + s], fill=BRAND)
    c, a = s / 2, dp(7)
    d.line([x + c - a, y + c, x + c + a, y + c], fill=(255, 255, 255), width=dp(2.2))
    d.line([x + c, y + c - a, x + c, y + c + a], fill=(255, 255, 255), width=dp(2.2))


def widget_list(img, x, y, w, h, marts, checking=()):
    """목록 위젯: marts stacked (전체) or one mart; rows clipped at the card bottom like a scroll."""
    card(img, x, y, w, h)
    layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    pad = dp(14)
    cy = dp(10)
    for i, m in enumerate(marts):
        cy += dp(14) if i else 0
        hy = cy + dp(14)
        nx = badge(layer, d, pad, hy, m) + dp(8)
        text(d, (nx, hy), m["name"], f(FB, 13), TEXT2)
        count_pill(d, w - pad, hy, m, len(m["items"]))
        cy += dp(28) + dp(6)
        d.line([pad, cy, w - pad, cy], fill=DIVIDER, width=dp(1))
        cy += dp(4)
        for name in m["items"]:
            ry = cy + dp(18)
            on = name in checking
            if on:
                check_on(d, pad + dp(8), ry)
            else:
                ring(d, pad + dp(8), ry)
            fnt = f(FR, 14)
            tx = pad + dp(28)
            text(d, (tx, ry), name, fnt, TEXT3 if on else TEXT)
            if on:
                tw = d.textlength(name, font=fnt)
                d.line([tx, ry, tx + tw, ry], fill=TEXT3, width=dp(1.2))
            cy += dp(36)
    fab(d, w - dp(10), h - dp(10))
    mask = Image.new("L", (w, h), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, w, h], radius=dp(22), fill=255)
    img.paste(layer, (x, y), ImageChops.multiply(layer.getchannel("A"), mask))


def icons_at(img, y, cols):
    """Galaxy icons in specific grid columns (0..3) of the 4-column home grid."""
    gap = SW / 4
    size = dp(58)
    d = ImageDraw.Draw(img)
    for col, (key, label) in cols:
        cx = gap * (col + 0.5)
        ic = Image.open(os.path.join(v4.ICONS, key + ".png")).convert("RGBA").resize((size, size), Image.LANCZOS)
        img.alpha_composite(ic, (int(cx - size / 2), int(y - size / 2)))
        fnt = f(FR, 12)
        ly = y + size / 2 + dp(12)
        sh = Image.new("RGBA", img.size, (0, 0, 0, 0))
        text(ImageDraw.Draw(sh), (cx, ly + dp(0.5)), label, fnt, (0, 0, 0, 150), anchor="mm")
        img.alpha_composite(sh.filter(ImageFilter.GaussianBlur(dp(1))))
        text(d, (cx, ly), label, fnt, (255, 255, 255), anchor="mm")


def callout(img, cx, y, s):
    d = ImageDraw.Draw(img)
    fnt = f(FB, 14)
    tw = d.textlength(s, font=fnt)
    w, h = tw + dp(28), dp(38)
    x = cx - w / 2
    d.polygon([(x + dp(30), y), (x + dp(40), y - dp(9)), (x + dp(50), y)], fill=hexc("#191F28"))
    d.rounded_rectangle([x, y, x + w, y + h], radius=dp(12), fill=hexc("#191F28"))
    text(d, (cx, y + h / 2), s, fnt, (255, 255, 255), anchor="mm")


HALF = (FULL_W - 12) / 2

COUPANG5 = dict(COUPANG, items=["생수 2L", "물티슈", "키친타월", "세탁세제", "햇반"])
DAISO4 = dict(DAISO, items=["건전지", "수세미", "지퍼백", "옷걸이"])
EMART6 = dict(EMART, items=["우유", "계란", "두부", "대파", "삼겹살", "양파"])


def hero_screen():
    # Top row: 개수 위젯 + 2 icons; the usual grid row then holds only the other two
    # (no icon appears twice on one home screen).
    img = v4.wallpaper()
    v4.status_bar(img)
    widget_counts(img, dp(M), dp(66), dp(HALF), dp(84), [COUPANG5, DAISO4])
    icons_at(img, dp(108), [(2, ("martnote", "마트노트")), (3, ("files", "내 파일"))])
    widget_list(img, dp(M), dp(168), dp(FULL_W), dp(372), [COUPANG5, DAISO4])
    icons_at(img, SH - dp(200), [(0, ("calendar", "캘린더")), (1, ("clock", "시계"))])
    v4.icon_row(img, SH - dp(72), v4.DOCK)
    return img.convert("RGB")


def check_screen():
    def draw(im):
        widget_list(im, dp(M), dp(96), dp(FULL_W), dp(292), [EMART6], checking=("우유",))
        callout(im, dp(M + 82), dp(398), "○ 누르면 앱 안 열고 체크")
    return home([draw])


def chat_screen_big():
    """Generic messenger 'note to self' (not any real app's UI), sized to be read on a phone:
    bigger bubbles, the grocery note buried in the middle and marked."""
    img = Image.new("RGB", (SW, SH), hexc("#E9EEF5"))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, SW, dp(70)], fill=hexc("#F7F8FA"))
    text(d, (dp(20), dp(38)), "나에게 보내기", f(FB, 19), TEXT)
    msgs = [
        "이번 주 회의 자료",
        "https://docs.example.com/…",
        "[사진 3장]",
        "택배 송장 6043-2219-8812",
        "치과 예약 목요일 2시",
        "우유, 계란, 두부, 대파",
        "아이 준비물: 색종이, 풀",
        "https://blog.example.com/…",
        "영수증.pdf",
        "적금 만기 확인",
        "주차 B2 37번",
    ]
    y = dp(92)
    fnt = f(FR, 19)
    hl_box = None
    for s_ in msgs:
        tw = d.textlength(s_, font=fnt)
        w, h = tw + dp(34), dp(52)
        x2 = SW - dp(18)
        hl = s_.startswith("우유")
        d.rounded_rectangle([x2 - w, y, x2, y + h], radius=dp(16),
                            fill=(255, 255, 255))  # same white as the rest — it's buried
        text(d, (x2 - w + dp(17), y + h / 2), s_, fnt, TEXT)
        if hl:
            hl_box = (x2 - w, y, x2, y + h)
        y += h + dp(14)
    # mark the buried grocery note
    x1, y1, x2, y2 = hl_box
    d.rounded_rectangle([x1 - dp(6), y1 - dp(6), x2 + dp(6), y2 + dp(6)], radius=dp(20),
                        outline=hexc("#F04452"), width=dp(3))
    # label on the left of the bubble, inside the screen
    lab = "내가 써놨던 메모"
    lf = f(FB, 16)
    lw = d.textlength(lab, font=lf) + dp(24)
    lx1 = dp(18)
    ly = (y1 + y2) / 2
    d.rounded_rectangle([lx1, ly - dp(17), lx1 + lw, ly + dp(17)], radius=dp(12), fill=hexc("#F04452"))
    text(d, (lx1 + lw / 2, ly), lab, lf, (255, 255, 255), anchor="mm")
    d.line([lx1 + lw + dp(2), ly, x1 - dp(8), ly], fill=hexc("#F04452"), width=dp(2.5))
    return img


def shot_compare(name, title, sub):
    c = v4.canvas_with(title, sub)
    d = ImageDraw.Draw(c)
    left = v4.device(v4.chat_screen())
    right = v4.device(hero_screen())
    w = 490
    for img, cx, label in ((left, 280, "메신저에 적어두면"), (right, 800, "마트노트 위젯")):
        r = w / img.size[0]
        img = img.resize((w, int(img.size[1] * r)), Image.LANCZOS)
        v4.place(c, img, top=860, cx=cx, max_h=img.size[1])
        v2.draw_center(d, cx, 760, label, v2.font(FB, 40),
                       v2.S_LIGHT if cx < 500 else BRAND)
    v4.save(c, name)


def feature_graphic():
    W, H = 1024, 500
    g = v2.vgrad(W, H, (233, 240, 252), (247, 250, 254)).convert("RGBA")
    icon = Image.open(os.path.join(v2.ROOT, "docs", "playstore_assets", "icon_512.png")).convert("RGBA")
    s = 250
    icon = icon.resize((s, s), Image.LANCZOS)
    mask = Image.new("L", (s, s), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, s, s], radius=58, fill=255)
    sh = Image.new("RGBA", g.size, (0, 0, 0, 0))
    ImageDraw.Draw(sh).rounded_rectangle([80, 135, 80 + s, 135 + s], radius=58, fill=(10, 20, 40, 60))
    g.alpha_composite(sh.filter(ImageFilter.GaussianBlur(18)))
    g.paste(icon, (80, 125), mask)
    d = ImageDraw.Draw(g)
    d.text((385, 150), "마트노트", font=v2.font(FB, 104), fill=TEXT, anchor="lm")
    d.text((388, 262), "마트별 장보기 리스트", font=v2.font(FR, 46), fill=TEXT2, anchor="lm")
    d.text((388, 335), "묻히던 장보기, 이제 홈 화면에", font=v2.font(FB, 38), fill=BRAND, anchor="lm")
    g.convert("RGB").save(os.path.join(OUT, "feature_graphic_1024x500.png"), "PNG")
    print("saved feature_graphic_1024x500.png")


if __name__ == "__main__":
    # Story order (user 2026-11-13: the app exists because notes sent to "나에게 보내기"
    # got lost): 01 can't find it → 02 move it into 마트노트 → 03 now it's always on the
    # home screen → 04 check it off there → 05 add more → 06 done.
    v4.shot_single("01_memo_lost.png",
                   ["나에게 보내기로 보낸 장보기", "또 찾고 계셨죠?"],
                   "다른 글 사이에 묻혀서 스크롤만 한참",
                   chat_screen_big())
    v4.shot_raw("02_share_import.png",
                ["써놨던 메모,", "공유 한 번이면 정리 끝"],
                "마트만 고르면 하나하나 나눠서 담아줘요",
                "ss9_share.png")
    v4.shot_single("03_widget_home.png",
                   ["폰만 켜면", "살 게 다 보여요"],
                   "홈 화면 위젯에 마트별로 적혀 있어요",
                   hero_screen())
    v4.shot_single("04_widget_check.png",
                   ["구매한 건 홈 화면에서", "바로 완료 체크!"],
                   "앱을 안 열어도 동그라미만 누르면 돼요",
                   check_screen())
    v4.shot_raw("05_name_only.png",
                ["생각날 때", "이름만 톡 적으면 끝"],
                "가격이나 개수는 안 적어도 괜찮아요",
                "ss4_add.png")
    v4.shot_raw("06_done.png",
                ["다 산 건 '완료' 한 번"],
                "목록에서 쏙 빠지고, 산 건 따로 모여요",
                "ss5_completed.png", ad=(2622, 2872))
    feature_graphic()
    print("ALL DONE ->", OUT)
