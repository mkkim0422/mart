# -*- coding: utf-8 -*-
"""Play Store screenshot series v3 (v1.3.0 — 쇼핑몰 앱 바로가기).

Reuses the v2 layout (scripts/compose_store_v2.py) and only rebuilds the shots
whose screen changed; the rest of the v2 finals are kept and renumbered.

New raw captures (Galaxy S24+, ADB, 2026-10-28):
  raw/ss1_home.png  쿠팡 tab: "쿠팡 앱 바로가기" switch + 🔍 per item
  raw/ss8_shop.png  same, item ⋮ menu open → "쿠팡에서 찾기"
  raw/ss2_home.png  이마트 tab (mart colors)

Output: docs/playstore_assets/final/01..08 (1080x2400).
Run:    python scripts/compose_store_v3.py
"""
import os
import shutil

import compose_store_v2 as v2

OUT = v2.OUT
AD = (2585, 2875)  # debug test-ad band in the new captures (468x60 banner + label)

# Rebuilt from new captures
NEW = [
    dict(out="01_main_hook.png", raw="ss1_home.png", ad=AD, dark=False,
         title=["마트별로 따로 — 헷갈리지 않게"],
         sub="쿠팡·다이소·이마트·홈플러스·마켓컬리, 한 번에"),
    dict(out="02_shop_search.png", raw="ss8_shop.png", ad=AD, dark=False,
         title=["적어둔 그대로", "쇼핑 앱에서 바로 검색"],
         sub="돋보기 한 번이면 쿠팡·컬리 앱이 그 상품을 찾아줘요"),
    dict(out="07_marts.png", raw="ss2_home.png", ad=AD, dark=False,
         title=["마트마다 색으로 구분"],
         sub="이마트·홈플러스·마켓컬리까지 자유롭게 추가"),
]

# Kept from v2 (screen unchanged) — old name -> new name
KEEP = {
    "02_voice_add.png": "03_voice_add.png",
    "03_1sec_input.png": "04_1sec_input.png",
    "04_widget.png": "05_widget.png",
    "05_completed.png": "06_completed.png",
    "07_dark.png": "08_dark.png",
}

if __name__ == "__main__":
    # Renumber kept finals first (reverse order so names never collide).
    for old in sorted(KEEP, reverse=True):
        src, dst = os.path.join(OUT, old), os.path.join(OUT, KEEP[old])
        if os.path.exists(src):
            shutil.move(src, dst)
            print("kept", old, "->", KEEP[old])
    stale = os.path.join(OUT, "06_marts.png")  # replaced by 07_marts.png
    if os.path.exists(stale):
        os.remove(stale)
    for s in NEW:
        v2.build(s)
    print("ALL DONE ->", OUT)
