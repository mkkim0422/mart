/* 메뉴 → 장볼 목록.  페이지마다 window.R(재료표)이 들어 있으면 계산 화면, 없으면 첫 화면(검색). */
(function () {
  var LIST_PAGE_URL = "https://mkkim0422.github.io/mart/l/";
  var MAX_ITEMS = 30; // 앱의 listLink()와 같은 한도
  var $ = function (id) { return document.getElementById(id); };

  function toast(t) {
    var el = $("toast"); if (!el) return;
    el.textContent = t; el.classList.add("show");
    setTimeout(function () { el.classList.remove("show"); }, 1700);
  }

  /* ── 첫 화면: 이름으로 찾기 ── */
  var search = $("search");
  if (search) {
    search.addEventListener("input", function () {
      var k = search.value.replace(/\s/g, "");
      var any = false;
      document.querySelectorAll(".cat").forEach(function (cat) {
        var shown = 0;
        cat.querySelectorAll(".card").forEach(function (c) {
          var hit = !k || c.dataset.n.replace(/\s/g, "").indexOf(k) >= 0;
          c.style.display = hit ? "" : "none";
          if (hit) shown++;
        });
        cat.style.display = shown ? "" : "none";
        if (shown) any = true;
      });
      var feat = $("feat"); if (feat) feat.style.display = k ? "none" : "";
      $("none").style.display = any ? "none" : "block";
    });
  }

  /* ── 계산 화면 ── */
  var R = window.R; if (!R) return;
  var n = R.base;
  var scaled = R.unit !== "";
  var PRESETS = { "인분": [1, 2, 4, 6, 8], "명": [2, 4, 6, 8, 10], "kg": [10, 20, 40, 60] };
  var STEP = R.unit === "kg" ? 10 : 1, MIN = STEP, MAX = R.unit === "kg" ? 200 : 30;

  /* build.py의 amount()와 같은 규칙 */
  function amount(it) {
    if (it.q == null) return "";
    var v = it.fx || !scaled ? it.q : it.q * n / R.base;
    if (it.u === "g" || it.u === "ml") {
      var big = it.u === "g" ? "kg" : "L";
      if (v >= 1000) return String(Math.round(v / 100) / 10) + big;
      var r = v >= 300 ? Math.round(v / 50) * 50 : Math.max(10, Math.round(v / 10) * 10);
      return r + it.u;
    }
    return Math.max(1, Math.ceil(v - 1e-9)) + it.u;
  }
  function label(it) { var a = amount(it); return a ? it.n + " " + a : it.n; }

  function render() {
    if (scaled) {
      $("kgs").innerHTML = PRESETS[R.unit].map(function (k) {
        return '<button class="kg ' + (k === n ? "on" : "") + '" data-kg="' + k + '">' + k + R.unit + "</button>";
      }).join("");
      $("kgv").textContent = n + R.unit;
    }
    var html = "", last = -1;
    R.items.forEach(function (it, i) {
      if (it.g !== last) {
        var g = R.groups[it.g];
        html += '<div class="gh"><b>' + g.t + "</b><span>" + g.h + "</span></div>";
        last = it.g;
      }
      html += '<div class="row ' + (it.on ? "on" : "") + '" data-i="' + i + '"><span class="cb">' + (it.on ? "✓" : "") +
        '</span><span class="nm">' + it.n + '</span><span class="am">' + amount(it) + "</span></div>";
    });
    $("list").innerHTML = html;
    var cnt = R.items.filter(function (x) { return x.on; }).length;
    $("go").textContent = "마트노트에 " + cnt + "개 담기";
    $("over").style.display = cnt > MAX_ITEMS ? "" : "none";
    if (R.side) {
      $("side").innerHTML = R.side.items.map(function (d, i) {
        return '<span class="chip ' + (d[1] ? "on" : "") + '" data-d="' + i + '">' + d[0] + "</span>";
      }).join("");
      $("sideBtn").textContent = R.side.mart + " 탭에 " + R.side.items.filter(function (d) { return d[1]; }).length + "개 담기";
    }
  }

  /* 앱이 만드는 링크와 같은 형식: base64url(UTF-8 "마트\n색hex\n이모지\n항목\n…") */
  function link(mart, color, emoji, names) {
    var text = [mart, color, emoji].concat(names.slice(0, MAX_ITEMS)).join("\n");
    var bytes = new TextEncoder().encode(text), bin = "";
    for (var i = 0; i < bytes.length; i++) bin += String.fromCharCode(bytes[i]);
    return LIST_PAGE_URL + "#" + btoa(bin).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
  }
  function picked() { return R.items.filter(function (x) { return x.on; }).map(label); }

  document.addEventListener("click", function (e) {
    var k = e.target.closest("[data-kg]"); if (k) { n = +k.dataset.kg; render(); return; }
    var r = e.target.closest("[data-i]"); if (r) { var it = R.items[+r.dataset.i]; it.on = !it.on; render(); return; }
    var d = e.target.closest("[data-d]"); if (d) { var x = R.side.items[+d.dataset.d]; x[1] = !x[1]; render(); return; }
  });
  if (scaled) {
    $("minus").onclick = function () { n = Math.max(MIN, n - STEP); render(); };
    $("plus").onclick = function () { n = Math.min(MAX, n + STEP); render(); };
  }
  $("go").onclick = function () {
    var names = picked(); if (!names.length) { toast("담을 걸 하나 이상 골라 주세요"); return; }
    location.href = link(R.mart, R.color, R.emoji, names);
  };
  if (R.side) $("sideBtn").onclick = function () {
    var names = R.side.items.filter(function (d) { return d[1]; }).map(function (d) { return d[0]; });
    if (!names.length) { toast("하나 이상 골라 주세요"); return; }
    location.href = link(R.side.mart, R.side.color, R.side.emoji, names);
  };
  $("copy").onclick = function () {
    var text = picked().join("\n");
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(function () { toast("목록을 복사했어요"); }, function () { toast("복사가 안 됐어요"); });
    } else toast("복사가 안 됐어요");
  };
  render();
})();
