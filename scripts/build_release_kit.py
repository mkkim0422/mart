# -*- coding: utf-8 -*-
"""출시 자료 폴더(출시자료_v1.4.0/) 만들기 — 문구는 docs/playstore_listing.md에서 그대로 가져온다.

결과:
  출시자료_v1.4.0/
    index.html            ← 브라우저로 열어 복사 버튼으로 붙여넣기
    1_스크린샷/01~05.png    (1080x2400, 이 순서대로 업로드)
    2_피처그래픽/feature_graphic_1024x500.png
    3_앱아이콘/icon_512.png
    4_문구/*.txt           (출시노트·앱이름·짧은설명·자세한설명)
    5_업로드파일/app-release.aab (gitignore — 서명된 번들, 직접 빌드 후 복사)

Run: python scripts/build_release_kit.py
"""
import html
import os
import re
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
VERSION, VC = "1.4.0", 10
OUT = os.path.join(ROOT, f"출시자료_v{VERSION}")
SRC = os.path.join(ROOT, "docs", "playstore_assets")

md = open(os.path.join(ROOT, "docs", "playstore_listing.md"), encoding="utf-8").read()


def block_after(heading):
    m = re.search(re.escape(heading) + r".*?```\n(.*?)```", md, re.S)
    assert m, heading
    return m.group(1).rstrip("\n")


texts = [
    ("출시노트", "출시 노트 (이번 업데이트 내용)", 500, block_after("## 0. 이번 업데이트 내용")),
    ("앱이름", "앱 이름", 30, block_after("### 앱 이름")),
    ("짧은설명", "짧은 설명", 80, block_after("### 짧은 설명")),
    ("자세한설명", "자세한 설명", 4000, block_after("### 자세한 설명")),
]

SHOTS = [
    ("01_widget_home.png", "홈 화면 위젯", "마트 와서 뭐 사러 왔더라?"),
    ("02_vs_memo.png", "나에게 보내기 vs 위젯", "나에게 보내기로 보낸 장보기 또 찾고 계셨죠?"),
    ("03_widget_check.png", "위젯에서 체크", "산 건 위젯에서 동그라미 톡"),
    ("04_name_only.png", "이름만 입력", "생각날 때 이름만 톡 적으면 끝"),
    ("05_done.png", "완료", "다 산 건 '완료' 한 번"),
]

# ---------- files ----------
if os.path.isdir(OUT):
    for d in ("1_스크린샷", "2_피처그래픽", "3_앱아이콘", "4_문구"):
        shutil.rmtree(os.path.join(OUT, d), ignore_errors=True)
for d in ("1_스크린샷", "2_피처그래픽", "3_앱아이콘", "4_문구", "5_업로드파일"):
    os.makedirs(os.path.join(OUT, d), exist_ok=True)
for fn, _, _ in SHOTS:
    shutil.copy2(os.path.join(SRC, "v5_store", fn), os.path.join(OUT, "1_스크린샷", fn))
shutil.copy2(os.path.join(SRC, "v5_store", "feature_graphic_1024x500.png"),
             os.path.join(OUT, "2_피처그래픽", "feature_graphic_1024x500.png"))
shutil.copy2(os.path.join(SRC, "icon_512.png"), os.path.join(OUT, "3_앱아이콘", "icon_512.png"))
for key, _, _, body in texts:
    with open(os.path.join(OUT, "4_문구", key + ".txt"), "w", encoding="utf-8") as fp:
        fp.write(body + "\n")
aab_src = os.path.join(ROOT, "app", "build", "outputs", "bundle", "release", "app-release.aab")
aab_dst = os.path.join(OUT, "5_업로드파일", f"martnote-{VERSION}-vc{VC}.aab")
has_aab = os.path.exists(aab_src)
if has_aab:
    shutil.copy2(aab_src, aab_dst)

# ---------- html ----------
def esc(s):
    return html.escape(s)


text_cards = "\n".join(f"""
    <section class="card">
      <div class="row">
        <h3>{esc(label)}</h3>
        <span class="meta">{len(body)} / {limit}자</span>
        <button class="copy" data-target="t-{key}">복사</button>
      </div>
      <pre id="t-{key}">{esc(body)}</pre>
    </section>""" for key, label, limit, body in texts)

shot_cards = "\n".join(f"""
      <figure>
        <a href="1_스크린샷/{fn}" target="_blank"><img src="1_스크린샷/{fn}" alt="{esc(t)}" loading="lazy"></a>
        <figcaption><b>{i + 1}. {esc(t)}</b><br>{esc(cap)}</figcaption>
      </figure>""" for i, (fn, t, cap) in enumerate(SHOTS))

aab_line = (f'<code>5_업로드파일/martnote-{VERSION}-vc{VC}.aab</code>' if has_aab
            else '<b>아직 없음</b> — <code>./gradlew :app:bundleRelease</code> 후 이 스크립트를 다시 실행')

page = f"""<!doctype html>
<html lang="ko">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>마트노트 {VERSION} 출시 자료</title>
<style>
  :root {{
    --bg:#F9FAFB; --card:#FFFFFF; --line:#E5E8EB; --text:#191F28; --sub:#4E5968; --muted:#8B95A1;
    --brand:#3182F6; --soft:#E8F3FF; --ok:#2BA471; --warn:#FFA000;
  }}
  @media (prefers-color-scheme: dark) {{
    :root {{ --bg:#17171B; --card:#1F1F23; --line:#3A3D42; --text:#F2F4F6; --sub:#A8B0BA; --muted:#6B7684; --brand:#4592FF; --soft:#1A2D4D; }}
  }}
  * {{ box-sizing:border-box; }}
  body {{ margin:0; background:var(--bg); color:var(--text);
         font-family:Pretendard, "Malgun Gothic", system-ui, sans-serif; line-height:1.55; letter-spacing:-0.01em; }}
  main {{ max-width:1080px; margin:0 auto; padding:32px 16px 80px; }}
  h1 {{ font-size:26px; margin:0 0 4px; }}
  h2 {{ font-size:19px; margin:40px 0 12px; }}
  h3 {{ font-size:16px; margin:0; }}
  .lead {{ color:var(--sub); margin:0 0 20px; }}
  .card {{ background:var(--card); border:1px solid var(--line); border-radius:16px; padding:18px 20px; margin-bottom:12px; }}
  .row {{ display:flex; align-items:center; gap:10px; margin-bottom:10px; }}
  .row h3 {{ flex:1; }}
  .meta {{ color:var(--muted); font-size:13px; }}
  pre {{ white-space:pre-wrap; word-break:keep-all; margin:0; font:15px/1.6 inherit; font-family:inherit;
        background:var(--bg); border-radius:12px; padding:14px 16px; }}
  button.copy {{ border:0; background:var(--brand); color:#fff; border-radius:10px; padding:8px 14px; font-size:14px; cursor:pointer; }}
  button.copy.done {{ background:var(--ok); }}
  .shots {{ display:grid; grid-template-columns:repeat(auto-fill, minmax(180px, 1fr)); gap:14px; }}
  figure {{ margin:0; background:var(--card); border:1px solid var(--line); border-radius:14px; padding:10px; }}
  figure img {{ width:100%; border-radius:8px; display:block; }}
  figcaption {{ font-size:13px; color:var(--sub); margin-top:8px; }}
  .wide img {{ width:100%; max-width:640px; border-radius:12px; border:1px solid var(--line); display:block; }}
  .icon img {{ width:96px; border-radius:22px; border:1px solid var(--line); }}
  ol {{ padding-left:20px; margin:0; }} li {{ margin:6px 0; }}
  code {{ background:var(--soft); color:var(--brand); padding:1px 6px; border-radius:6px; font-size:13px; word-break:break-all; }}
  table {{ border-collapse:collapse; width:100%; font-size:14px; }}
  td {{ border-top:1px solid var(--line); padding:8px 6px; vertical-align:top; }}
  td:first-child {{ color:var(--muted); width:150px; }}
  .warn {{ border-left:4px solid var(--warn); }}
</style>
</head>
<body>
<main>
  <h1>마트노트 {VERSION} 출시 자료</h1>
  <p class="lead">버전 {VERSION} (versionCode {VC}) · 패키지 <code>com.rldjrgo.grocerynote</code> · 기본 언어 한국어</p>

  <section class="card">
    <h3 style="margin-bottom:10px">올리는 순서</h3>
    <ol>
      <li>Play 콘솔 → 마트노트 → <b>설정 → 앱 서명</b>에서 "업로드 키 인증서" SHA-1이 아래 지문과 같은지 확인</li>
      <li><b>프로덕션</b>(또는 테스트 트랙) → <b>새 버전 만들기</b> → AAB 업로드: {aab_line}</li>
      <li>출시 노트 칸에 아래 <b>출시 노트</b> 복사해서 붙여넣기</li>
      <li><b>스토어 등록정보</b> → 앱 이름·짧은 설명·자세한 설명 붙여넣기</li>
      <li>휴대전화 스크린샷: <b>기존 8장 모두 삭제</b> → <code>1_스크린샷</code>의 01~05를 순서대로 업로드</li>
      <li>그래픽 이미지(피처): <code>2_피처그래픽/feature_graphic_1024x500.png</code> 교체 · 앱 아이콘은 그대로</li>
      <li>저장 → 검토 제출</li>
    </ol>
  </section>

  <section class="card warn">
    <table>
      <tr><td>서명 지문 SHA-1</td><td><code>32:EF:85:73:9C:92:84:E7:0A:30:F7:27:18:9F:31:46:D4:6E:43:F5</code></td></tr>
      <tr><td>알아둘 것</td><td>위젯이 5종 → 2종으로 바뀌어, 예전 위젯을 놓아 둔 사용자는 다시 추가해야 해요(출시 노트에 안내 포함).<br>"카톡/카카오톡" 단어는 문구·이미지 어디에도 쓰지 않았어요(타사 상표).</td></tr>
    </table>
  </section>

  <h2>문구</h2>
{text_cards}

  <h2>휴대전화 스크린샷 (1080×2400, 이 순서로)</h2>
  <div class="shots">
{shot_cards}
  </div>

  <h2>그래픽 이미지 (1024×500)</h2>
  <section class="card wide"><img src="2_피처그래픽/feature_graphic_1024x500.png" alt="피처 그래픽"></section>

  <h2>앱 아이콘 (512×512, 변경 없음)</h2>
  <section class="card icon"><img src="3_앱아이콘/icon_512.png" alt="앱 아이콘"></section>
</main>
<script>
  document.querySelectorAll("button.copy").forEach(function (b) {{
    b.addEventListener("click", function () {{
      var t = document.getElementById(b.dataset.target).innerText;
      var done = function () {{ b.textContent = "복사됨"; b.classList.add("done");
        setTimeout(function () {{ b.textContent = "복사"; b.classList.remove("done"); }}, 1500); }};
      if (navigator.clipboard) navigator.clipboard.writeText(t).then(done, function () {{ fallback(t); done(); }});
      else {{ fallback(t); done(); }}
    }});
  }});
  function fallback(t) {{
    var ta = document.createElement("textarea"); ta.value = t; document.body.appendChild(ta);
    ta.select(); try {{ document.execCommand("copy"); }} catch (e) {{}} document.body.removeChild(ta);
  }}
</script>
</body>
</html>
"""
with open(os.path.join(OUT, "index.html"), "w", encoding="utf-8") as fp:
    fp.write(page)
print("OK ->", OUT, "| aab:", has_aab)
