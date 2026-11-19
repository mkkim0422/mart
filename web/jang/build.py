# -*- coding: utf-8 -*-
"""메뉴 → 장볼 목록 페이지 생성기.

    python web/jang/build.py

recipes.txt 를 읽어 out/ 에 첫 화면(index.html)과 메뉴별 페이지(<id>/index.html)를 만든다.
out/ 을 gh-pages 브랜치의 jang/ 폴더로 올리면 https://mkkim0422.github.io/mart/jang/ 가 된다.
(gh-pages 의 l/index.html, privacy.html 은 건드리지 말 것)
"""
import html
import json
import math
import shutil
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent.parent
OUT = HERE / "out"
BASE_URL = "https://mkkim0422.github.io/mart/jang/"
PLAY_URL = "https://play.google.com/store/apps/details?id=com.rldjrgo.grocerynote"
FONT = '<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css">'
CATS = ["행사·모임", "찌개·국", "메인 요리", "반찬", "밥·면·분식", "전·부침"]
CAT_NOTE = {"행사·모임": "한 번에 많이 사는 날"}
FEATURED = "kimjang"
PANTRY = ("집에 있을 것","없는 것만 눌러서 담으세요")
QUESTION = {"인분": "몇 인분 하세요?", "명": "몇 명이 먹나요?", "kg": "몇 kg 하세요?"}
PRESETS = {"인분": [1, 2, 4, 6, 8], "명": [2, 4, 6, 8, 10], "kg": [10, 20, 40, 60]}

e = html.escape


def is_num(s):
    try:
        float(s)
        return True
    except ValueError:
        return False


def parse(path):
    recipes, cur, group = [], None, None
    for raw in path.read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if not line or (line.startswith("#") and not line.startswith("## ")):
            continue
        if line.startswith("@"):
            p = [x.strip() for x in line[1:].split("|")]
            unit = "" if p[5] == "없음" else p[5]
            cur = dict(id=p[0], name=p[1], emoji=p[2], cat=p[3], base=float(p[4]), unit=unit,
                       mart=p[6] if len(p) > 6 else "장보기", color=p[7] if len(p) > 7 else "3182F6",
                       hint="", question="", groups=[], items=[], side=None, explicit=False)
            if cur["base"].is_integer():
                cur["base"] = int(cur["base"])
            recipes.append(cur)
            group = None
        elif line.startswith("! "):
            cur["hint"] = line[2:].strip()
        elif line.startswith("? "):
            cur["question"] = line[2:].strip()
        elif line.startswith("## "):
            t, _, h = line[3:].partition("|")
            cur["groups"].append(dict(t=t.strip(), h=h.strip()))
            cur["explicit"] = True
            group = len(cur["groups"]) - 1
        elif line.startswith("& "):
            p = [x.strip() for x in line[2:].split("|")]
            cur["side"] = dict(mart=p[0], color=p[1], emoji=p[2], title=p[3], hint=p[4], items=[])
        elif line.startswith("+"):
            s = line[1:]
            on = not s.startswith("?")
            cur["side"]["items"].append([s.lstrip("?").strip(), on])
        else:
            opt = pantry = fixed = False
            while line[0] in "?~=":
                opt, pantry, fixed = opt or line[0] == "?", pantry or line[0] == "~", fixed or line[0] == "="
                line = line[1:]
            tok = line.split()
            if not pantry and cur["unit"] and len(tok) >= 3 and is_num(tok[-2]):
                name, q, u = " ".join(tok[:-2]), float(tok[-2]), tok[-1]
            else:
                name, q, u = " ".join(tok), None, ""
            cur["items"].append(dict(n=name, q=q, u=u, on=not (opt or pantry), fx=fixed,
                                     kind="pantry" if pantry else ("opt" if opt else "main"), g=group))

    for r in recipes:
        if r["explicit"]:
            groups = list(r["groups"])
            for it in r["items"]:
                if it["kind"] == "pantry":
                    it["g"] = len(groups)
            groups.append(dict(t=PANTRY[0], h=PANTRY[1]))
        else:
            main_hint = f"{r['unit']}에 맞춰 계산" if r["unit"] else ""
            groups = [dict(t="재료", h=main_hint), dict(t="넣으면 더 좋은 것", h="취향대로"), dict(t=PANTRY[0], h=PANTRY[1])]
            for it in r["items"]:
                it["g"] = {"main": 0, "opt": 1, "pantry": 2}[it["kind"]]
        r["groups"] = groups
        r["items"].sort(key=lambda it: it["g"])  # 안정 정렬: 묶음 안의 순서는 적은 그대로
        for it in r["items"]:
            del it["kind"]
        ids = [x["id"] for x in recipes]
        assert len(ids) == len(set(ids)), "id 중복"
        assert r["cat"] in CATS, f"{r['id']}: 분류 '{r['cat']}'"
    return recipes


def amount(it, r, n=None):
    """app.js 의 amount() 와 같은 규칙."""
    if it["q"] is None:
        return ""
    n = r["base"] if n is None else n
    v = it["q"] if (it["fx"] or not r["unit"]) else it["q"] * n / r["base"]
    if it["u"] in ("g", "ml"):
        if v >= 1000:
            return f"{round(v / 100) / 10:g}" + ("kg" if it["u"] == "g" else "L")
        return f"{(round(v / 50) * 50) if v >= 300 else max(10, round(v / 10) * 10)}{it['u']}"
    return f"{max(1, math.ceil(v - 1e-9))}{it['u']}"


def head(title, desc, canonical, css):
    return f"""<!doctype html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{e(title)}</title>
<meta name="description" content="{e(desc)}">
<link rel="canonical" href="{canonical}">
<meta property="og:type" content="website">
<meta property="og:title" content="{e(title)}">
<meta property="og:description" content="{e(desc)}">
<meta property="og:url" content="{canonical}">
<meta property="og:image" content="{BASE_URL}og.png">
<meta name="theme-color" content="#EAF3FF">
<link rel="icon" href="{css}icon.png">
{FONT}
<link rel="stylesheet" href="{css}style.css">
</head>
<body>
"""


def app_card(rel):
    return f"""<div class="app"><img src="{rel}icon.png" alt="">
<div><b>마트노트</b><span>마트별로 적고, 홈 화면에서 바로 체크하는 장보기 메모 앱이에요. 무료, 가입 없음.</span>
<a href="{PLAY_URL}">플레이스토어에서 받기 ›</a></div></div>"""


def recipe_page(r, recipes):
    unit, base = r["unit"], r["base"]
    mains = [it["n"] for it in r["items"] if it["on"]][:6]
    if unit:
        desc = f"{r['name']} {base}{unit} 기준으로 살 것: {', '.join(mains)} 등. {unit}만 고르면 양을 계산해서 마트노트에 담아 드려요."
        sub = f"{QUESTION[unit].replace(' 하세요?', '').replace(' 먹나요?', '')}인지만 고르면 살 양이 계산돼요."
        sub = {"인분": "인분만 고르면 살 양이 계산돼요.", "명": "사람 수만 고르면 살 양이 계산돼요.", "kg": "배추 양만 고르면 살 양이 계산돼요."}[unit]
    else:
        desc = f"{r['name']} 목록: {', '.join(mains)} 등. 필요한 것만 골라서 마트노트에 담아 가세요."
        sub = "필요한 것만 골라 보세요."
    title = f"{r['name']} 재료 · 장볼 목록 계산 | 마트노트"
    out = [head(title, desc, f"{BASE_URL}{r['id']}/", "../")]
    out.append(f"""<div class="page">
<div class="top"><a class="back" href="../index.html">‹ 전체 메뉴</a><a class="brand" href="{PLAY_URL}"><img src="../icon.png" alt="">마트노트</a></div>
<div class="hero">
<span class="tag"><span class="emo">{r['emoji']}</span> {e(r['cat'])}</span>
<h1>{e(r['name'])} 장볼 목록</h1>
<p>{sub} 버튼 하나로 폰에 담아 가세요.</p>
</div>
""")
    if unit:
        chips = "".join(f'<button class="kg {"on" if k == base else ""}" data-kg="{k}">{k}{unit}</button>' for k in PRESETS[unit])
        hint = f'<div class="hint">{e(r["hint"])}</div>' if r["hint"] else ""
        out.append(f"""<div class="sec">
<div class="q">{e(r['question'] or QUESTION[unit])}</div>{hint}
<div class="kgs" id="kgs">{chips}</div>
<div class="step"><button id="minus" aria-label="줄이기">−</button><div class="v" id="kgv">{base}{unit}</div><button id="plus" aria-label="늘리기">＋</button></div>
</div>
""")
    rows, last = [], -1
    for i, it in enumerate(r["items"]):
        if it["g"] != last:
            g = r["groups"][it["g"]]
            rows.append(f'<div class="gh"><b>{e(g["t"])}</b><span>{e(g["h"])}</span></div>')
            last = it["g"]
        rows.append(f'<div class="row {"on" if it["on"] else ""}" data-i="{i}"><span class="cb">{"✓" if it["on"] else ""}</span>'
                    f'<span class="nm">{e(it["n"])}</span><span class="am">{amount(it, r)}</span></div>')
    out.append(f"""<div class="sec">
<h2 class="q">살 것</h2>
<div class="hint">집에 있는 건 눌러서 빼세요</div>
<div id="list">{''.join(rows)}</div>
</div>
""")
    if r["side"]:
        s = r["side"]
        chips = "".join(f'<span class="chip {"on" if on else ""}" data-d="{i}">{e(n)}</span>' for i, (n, on) in enumerate(s["items"]))
        out.append(f"""<div class="sec">
<h2 class="q">{e(s['title'])}</h2>
<div class="hint">{e(s['hint'])}</div>
<div class="side"><div class="chips" id="side">{chips}</div><button class="btn2" id="sideBtn">{e(s['mart'])} 탭에 담기</button></div>
</div>
""")
    same = [x for x in recipes if x["cat"] == r["cat"] and x["id"] != r["id"]]
    k = next(i for i, x in enumerate(recipes) if x["id"] == r["id"])
    near = sorted(same, key=lambda x: abs(recipes.index(x) - k))[:8]
    more = "".join(f'<a href="../{x["id"]}/index.html"><span class="emo">{x["emoji"]}</span> {e(x["name"])}</a>' for x in near)
    cnt = sum(1 for it in r["items"] if it["on"])
    out.append(f"""<div class="sec">
<div class="src">양은 장볼 때 참고하는 어림값이에요. 집집마다 넣는 재료와 양이 다르니 입맛에 맞게 더하고 빼세요.</div>
<h2 class="q" style="margin-top:18px;font-size:15px">다른 메뉴</h2>
<div class="more">{more}</div>
<a class="all" href="../index.html">전체 메뉴 {len(recipes)}개 보기 ›</a>
<div style="height:16px"></div>
{app_card('../')}
</div>
<div class="cta">
<button class="btn" id="go">마트노트에 {cnt}개 담기</button>
<div class="sub"><a id="copy">목록만 복사</a><span>마트노트가 없으면 설치 화면으로 가요</span></div>
<div class="sub" id="over" style="display:none"><span>한 번에 30개까지만 담겨요. 나눠서 담아 주세요.</span></div>
</div>
</div>
<div class="toast" id="toast"></div>
""")
    data = dict(id=r["id"], name=r["name"], unit=unit, base=base, mart=r["mart"], color=r["color"], emoji=r["emoji"],
                groups=r["groups"], items=r["items"],
                side=None if not r["side"] else dict(mart=r["side"]["mart"], color=r["side"]["color"],
                                                     emoji=r["side"]["emoji"], items=r["side"]["items"]))
    out.append(f'<script>window.R={json.dumps(data, ensure_ascii=False, separators=(",", ":"))}</script>\n'
               '<script src="../app.js"></script>\n</body>\n</html>\n')
    return "".join(out)


def home_page(recipes):
    title = "메뉴만 고르면 장볼 목록이 나와요 | 마트노트"
    desc = (f"된장찌개, 김밥, 김장, 차례상, 캠핑까지 메뉴 {len(recipes)}개. 인분에 맞춰 살 양을 계산하고 "
            "버튼 하나로 마트노트에 담아 가세요.")
    out = [head(title, desc, BASE_URL, "")]
    f = next(x for x in recipes if x["id"] == FEATURED)
    out.append(f"""<div class="page">
<div class="top"><span></span><a class="brand" href="{PLAY_URL}"><img src="icon.png" alt="">마트노트</a></div>
<div class="hero">
<span class="tag"><span class="emo">🛒</span> 장보기 계산</span>
<h1>메뉴만 고르면<br>장볼 목록이 나와요</h1>
<p>인분에 맞춰 살 양을 계산하고, 버튼 하나로 폰에 담아 가세요.</p>
<input class="search" id="search" type="search" placeholder="메뉴 이름으로 찾기 (예: 된장찌개)" autocomplete="off">
</div>
<a class="feat" id="feat" href="{f['id']}/index.html"><span class="e emo">{f['emoji']}</span>
<div><b>지금은 김장철</b><span>절임배추 kg만 고르면 양념 양이 나와요</span></div><i>›</i></a>
""")
    for c in CATS:
        items = [x for x in recipes if x["cat"] == c]
        note = f"<small>{CAT_NOTE[c]}</small>" if c in CAT_NOTE else ""
        cards = "".join(f'<a class="card" href="{x["id"]}/index.html" data-n="{e(x["name"])}"><span class="e emo">{x["emoji"]}</span>{e(x["name"])}</a>'
                        for x in items)
        out.append(f'<div class="cat"><h2>{e(c)}{note}</h2><div class="grid">{cards}</div></div>\n')
    out.append(f"""<div class="none" id="none">아직 없는 메뉴예요.</div>
<div class="foot">
{app_card('')}
<div class="src" style="margin-top:14px">양은 장볼 때 참고하는 어림값이에요. 집집마다 넣는 재료와 양이 달라요.</div>
</div>
</div>
<script src="app.js"></script>
</body>
</html>
""")
    return "".join(out)


def main():
    recipes = parse(HERE / "recipes.txt")
    if OUT.exists():
        shutil.rmtree(OUT)
    OUT.mkdir()
    for name in ("style.css", "app.js"):
        shutil.copy(HERE / "assets" / name, OUT / name)
    shutil.copy(ROOT / "docs/playstore_assets/icon_512.png", OUT / "icon.png")
    shutil.copy(ROOT / "docs/playstore_assets/v6_store/feature_graphic_1024x500.png", OUT / "og.png")
    (OUT / "index.html").write_text(home_page(recipes), encoding="utf-8")
    urls = [BASE_URL]
    for r in recipes:
        d = OUT / r["id"]
        d.mkdir()
        (d / "index.html").write_text(recipe_page(r, recipes), encoding="utf-8")
        urls.append(f"{BASE_URL}{r['id']}/")
    (OUT / "sitemap.xml").write_text(
        '<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n'
        + "".join(f"<url><loc>{u}</loc></url>\n" for u in urls) + "</urlset>\n", encoding="utf-8")
    by_cat = {c: sum(1 for r in recipes if r["cat"] == c) for c in CATS}
    over = [(r["id"], n) for r in recipes if (n := sum(1 for it in r["items"] if it["on"])) > 30]
    print(f"recipes={len(recipes)} {by_cat}")
    print(f"over30={over}")


if __name__ == "__main__":
    main()
