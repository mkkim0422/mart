# 마트노트 변경 기록 (CHANGELOG)

> 사용자 지시 1건 = 1줄. 최신이 맨 위. 탐색·빌드·실기기 검증은 기록하지 않음.
> 형식: `- YYYY-MM-DD · {한 줄 요약} · {대표 파일들}` (커밋되면 끝에 ` (커밋 abc1234)` 추가)

## iOS (진행 중)

- 2026-11-13 · **iOS 앱 첫 코드** (SwiftUI·iOS 17·SwiftData·위젯 2종 ○ 체크) + GitHub Actions 맥 빌드(XcodeGen, 시뮬레이터, 서명 없음) — 2번째 빌드에서 통과. 안드로이드 1.5.0 기능 대부분, 광고·공유 확장·서명은 아직 · `ios/`, `.github/workflows/ios.yml`, `CLAUDE.md` §12.5

## 2026-11-13 · v1.5.0 (vc12) — 받은 목록 링크 — Play 프로덕션 등록

- 2026-11-13 · (출시) **Play 콘솔 프로덕션에 12 (1.5.0) 등록**: 처음 올린 11 (1.5.0)은 검토 전송 전이었지만 같은 버전 코드는 다시 못 올려서 versionCode 12로 새로 빌드해 "제목 없는 버전" 초안에 올려 교체. 스토어 문구(짧은·자세한 설명), 휴대전화 스크린샷 6장, 그래픽 이미지 교체, 태블릿 스크린샷 정리. AI 애셋 선언 = 라벨 지정 안 함(코드로 그린 그림·실기기 캡처). 별점 창 추가 뒤 릴리스 빌드가 R8에서 실패하던 것(인앱 리뷰 라이브러리의 컴파일 전용 어노테이션 누락)을 `-dontwarn`으로 수정 — 그 사이 "AAB 다시 만듦"은 실제로는 예전 파일이었음(빌드 오류를 grep으로 걸러 놓침) · `app/build.gradle.kts`, `app/proguard-rules.pro`, `출시자료_v1.5.0/`
- 2026-11-13 · (조사) Play KR 검색: "마트노트" 1위, "장보기 메모/리스트/쇼핑 체크리스트" 상위 20 밖 — 다운로드 50+·리뷰 13이 원인. 경쟁 "쇼핑 목록"(10만+, 같은 위젯 체크). 이름은 유지, 앱 안 별점 창 추가, 유료 광고는 배너 수익 구조상 회수 어려워 보류(무료 홍보 우선) · `CLAUDE.md`
- 2026-11-13 · **앱 안 별점 창**(Google Play 인앱 리뷰): 앱에서 항목을 **10개째 완료**하면 2.5초 뒤 한 번만 별점 창(그 자리에서 별 누르고 제출). 이유: Play 검색 확인 결과 다운로드 50+·리뷰 13개라 키워드가 이름에 있어도 상위에 못 나옴 — 리뷰·설치 수가 막힌 원인. 이름 변경("장보기 위젯" 넣기)은 검색량이 적을 것 같아 보류 · `HomeViewModel.kt`, `HomeScreen.kt`, `SettingsDataStore.kt`(완료 수·요청 여부), `libs.versions.toml`(review-ktx 2.0.2), `app/build.gradle.kts`
- 2026-11-13 · (코드 아님) 출시 자료 최종: 출시 노트를 1.5.0 전체(링크 담기·새 마트·중복 빼기·위젯 체크·위젯 2종·항목 편집·≡ 순서·알리 검색)로 다시 씀, 데이터 보안 안내(공유 링크 단축 시 항목 이름이 is.gd/tinyurl로 감) 추가 · `docs/playstore_listing.md`, `scripts/build_release_kit.py`, `출시자료_v1.5.0/`
- 2026-11-13 · **항목 줄 ⋮ 메뉴 없앰**: 줄 = 이름 · 🔍 · 완료 · **≡**(잡고 끌면 바로 순서 변경, 길게 누르기도 그대로). **이름을 누르면 "항목 편집"** 창 — 이름 칸, 마트 칩(옮기기), 알림 줄, 삭제/저장. "○○에서 찾기"는 🔍와 같아서 뺌. 시안 `sample/항목편집_시안.html` · `components/{ItemRow,ItemList,ItemEditSheet}.kt`(ItemEditSheet 신규), `HomeScreen.kt`(이름 수정·마트 이동 대화상자 삭제), `CLAUDE.md`
- 2026-11-13 · **같은 목록 두 번 담기 방지**: 받은/공유한 목록을 담을 때 그 마트에 이미 있는(남은) 같은 이름은 빼고 담음(앞뒤 공백·대소문자 무시). 알림 "N개를 담았어요 · M개는 이미 있어요" / 전부 있으면 "이미 다 담겨 있어요". 완료된 항목은 다시 담김(다시 사야 하니까) · `HomeViewModel.kt`, `HomeScreen.kt`
- 2026-11-13 · **공유 링크 단축**: 공유할 때 is.gd(가입·키 없음)로 줄임 → 예 `https://is.gd/CORFbl`, 실패하면 tinyurl, 그것도 안 되면(인터넷 없음 등) 긴 링크 그대로. 세 서비스 모두 `#` 뒤 목록을 301 그대로 넘기는 것 확인. 장보기 목록은 사용자가 공유를 누를 때만 단축 서비스로 감(사용자 판단: 개인정보 아님, 서비스가 죽으면 그때 교체). buly.kr은 앱에 계정 API 키를 넣어야 해서 안 씀 · `util/LinkShortener.kt`(신규), `HomeScreen.kt`
- 2026-11-13 · 공유 메시지: "📝 마트노트에서 보냄" 다음 **빈 줄**, "👉 눌러서 마트노트에 바로 담기" 다음 줄에 링크. **링크 약 절반 길이**(예: 5개 203자 → 115자) — 목록을 `#s=…&i=…` 대신 `#<base64url("마트
색
이모지
항목…")>`로(한글 1자 9자 → 4자). 페이지는 두 형식 모두 읽음 · `HomeScreen.kt`, `gh-pages:l/index.html`
- 2026-11-13 · **받은 목록 — 내게 없는 마트면 "＋ 다이소 새로 만들기"**: 보낸 마트와 같은 이름 마트가 없으면 그 칩이 먼저 골라짐(제목 "받은 다이소 장보기 N개", 안내 "다이소 마트를 새로 만들고 여기에 담아요", 버튼 "다이소 만들고 N개 담기" 마트 색) → 담기 = 마트 생성 + 항목 + 그 탭으로, "다이소를 만들고 N개를 담았어요". 다른 마트를 누르면 평소 "N개 담기". 링크에 보낸 마트 색·이모지(`c`, `e`)를 실어 똑같은 모양으로 생성(없으면 회색 🛒). 시안 `sample/받은목록_새마트_시안.html` · `SharedItemsSheet.kt`, `HomeScreen.kt`, `HomeViewModel.kt`, `MainActivity.kt`, `gh-pages:l/index.html`
- 2026-11-13 · **v1.5.0 (vc11) 받은 목록 링크 — 눌러서 바로 담기**: 공유 메시지 끝 줄을 Play 링크에서 **"👉 눌러서 마트노트에 바로 담기: https://mkkim0422.github.io/mart/l/#s=마트&i=항목…"**으로. 목록은 `#` 뒤라 서버로 안 감. 페이지(`gh-pages` 브랜치)가 폰을 구분: 안드로이드 → 마트노트 열기(없으면 Play 스토어), 아이폰·PC → 목록만(눌러서 체크) + "갤럭시에서만" 안내. 앱: `martnote://add` 받기 → "받은 장보기 리스트 N개" 시트, 보낸 마트와 이름이 같은 마트 자동 선택. 크롬에서 앱 열림·앱 없을 때 Play 스토어(S24+) 확인. Analytics `list_link_opened`. 1.4.0은 출시하지 않고 함께 묶음(출시 노트 통합). 스토어 스크린샷 04 "가족이 보낸 장보기, 누르면 바로 담겨요" 추가(6장), 출시 자료 폴더 `출시자료_v1.5.0` · `AndroidManifest.xml`, `MainActivity.kt`, `HomeScreen.kt`, `SharedItemsSheet.kt`, `util/Analytics.kt`, `app/build.gradle.kts`, `gh-pages:l/index.html`, `scripts/{compose_store_v5,build_release_kit}.py`, `docs/playstore_listing.md`, `CLAUDE.md`

## 2026-11-13 · v1.4.0 (vc10) 마무리 — 쇼핑 앱 열기 수정·스토어 이미지 5장

- 2026-11-13 · (코드 아님) 스토어 스크린샷에서 "써놨던 메모, 공유 한 번이면 정리 끝" 장 제거 → **5장**(01 묻힌 메모 → 02 폰만 켜면 살 게 다 보여요 → 03 홈 화면에서 바로 완료 체크! → 04 이름만 → 05 완료). 공유로 옮기기는 공유 메뉴를 알아야 해서 실효성이 낮다는 판단 — 다음 버전 1.5.0에 **"링크 눌러 바로 담기"**(받는 사람이 링크를 누르면 마트노트로 담기, 없으면 Play 스토어) 계획을 CLAUDE.md에 기록 · `scripts/compose_store_v5.py`, `scripts/build_release_kit.py`, `docs/playstore_listing.md`, `CLAUDE.md`
- 2026-11-13 · (코드 아님) 스토어 스크린샷 순서를 **앱을 만든 이유 순서**로: 01 나에게 보내기에 묻힌 메모 → 02 공유로 옮기기 → 03 "이제 장보기는 홈 화면에 늘 보여요"(위젯) → 04 위젯 체크 → 05 이름만 → 06 완료. 피처 그래픽 문구 "묻히던 장보기, 이제 홈 화면에" · `scripts/compose_store_v5.py`, `scripts/build_release_kit.py`, `docs/playstore_listing.md`
- 2026-11-13 · **알리 🔍 → 알리 앱 검색 결과로 바로**: `https://www.aliexpress.com/w/wholesale-<검색어>.html`을 알리 앱으로 보내면 앱 안 검색 결과가 열림(S24+ 확인, 띄어쓰기 포함). `aliexpress://search?…`는 첫 화면에서 멈춰서 안 씀 · `util/StoreSearch.kt`
- 2026-11-13 · (코드 아님) 스토어 스크린샷 6장으로: 02 표시 "장보기 메모" → **"내가 써놨던 메모"**, 새 03 **"써놨던 메모, 공유 한 번이면 정리 끝"**(02의 메모를 공유해 여러 항목 담기 시트, 실기기 캡처 `raw/ss9_share.png`) — "불편 → 마트노트는 이렇게 편함" 흐름. 이후 번호 하나씩 밀림 · `scripts/compose_store_v5.py`, `scripts/build_release_kit.py`, `docs/playstore_listing.md`
- 2026-11-13 · (코드 아님) 스토어 스크린샷 02: 폰 두 대(메신저 vs 위젯) 비교는 폰 화면에서 글씨가 안 읽혀서 **메신저 폰 한 대 + 큰 글씨 + "장보기 메모" 표시**로 바꿈(위젯은 01에서 보여줌), 부제 "다른 글 사이에 묻혀서 스크롤만 한참" · `scripts/compose_store_v5.py`, `docs/playstore_assets/v5_store/02_vs_memo.png`
- 2026-11-13 · **쇼핑몰 앱 바로가기: 앱이 깔려 있는데 Play 스토어로 가던 문제**(알리, S24+ 사용자 제보) → 앱 첫 화면 열기를 `getLaunchIntentForPackage`로 바꾸고 매니페스트 `<queries>`에 연결 앱 24개 패키지 등록(Android 11+는 등록 안 한 앱을 숨김) · `util/StoreSearch.kt`, `AndroidManifest.xml`

## 2026-11-12 · v1.4.0 (vc10) — 위젯 2종·위젯에서 체크·여러 줄 공유 — 커밋 c13dcc0

- 2026-11-12 · (코드 아님) **스토어 자료 v1.4.0 최종 — 스크린샷 v5 + 피처 그래픽 + 문구**: 위젯 그림을 지금 위젯(목록 위젯 ○ 줄·"+" 하나, 전체는 마트가 위아래로 이어짐 / 개수 위젯 2x1)으로 다시 그림, 03을 "위젯 크기" → "산 건 위젯에서 동그라미 톡"(체크 중 ✓), 피처 문구 "홈 화면 위젯에서 보고, 바로 체크". 출시 노트·짧은 설명·자세한 설명 위젯 부분을 위젯 2종·위젯 체크로 갱신(예전 위젯 사용자 재추가 안내 포함) · `scripts/compose_store_v5.py`, `docs/playstore_assets/v5_store/`, `docs/playstore_listing.md`
- 2026-11-12 · **위젯 추가 창·"+" 다듬기**: 위젯을 누르면 고르기만(파란 테두리+동그라미), 아래 "홈 화면에 추가" 버튼으로 추가. "어떤 마트를 보여줄까요?" 화면·마트 칩 제거 → 목록 위젯은 항상 전체로 놓임(한 마트로 바꾸려면 위젯 길게 눌러 설정). 전체 목록 위젯·개수 위젯의 "+"는 첫 마트(쿠팡) 추가 창 대신 **앱만 열기**(한 마트 목록 위젯은 그 마트 추가 창). 미리보기: 목록 위젯 줄 간격 줄임(작은 글씨가 앱 기본 줄 높이를 물려받던 문제)·"+" 추가, 개수 위젯 이마트 → 쿠팡 4·다이소 3 · `ui/components/WidgetSizePickerSheet.kt`, `widget/common/WidgetCommon.kt`, `res/layout/widget_preview_{2x1,medium}.xml`, `CLAUDE.md`
- 2026-11-12 · (코드 아님) **스토어 스크린샷 v4 — 위젯 중심 5장**: 01 홈 화면 위젯 / 02 메신저 나에게 보내기 vs 위젯 / 03 위젯 크기 / 04 이름만 입력 / 05 완료. 다크 모드·음성·쇼핑앱 장 제외. 홈 화면·위젯은 실제 위젯 치수대로 그림(사용자 배경화면·타 앱 노출 방지), 04·05는 기존 캡처. 홈 화면 아이콘은 S24+ 앱 목록에서 잘라낸 **갤럭시 기본 아이콘**(캘린더·시계·날씨·내 파일 / 전화·메시지·갤러리·설정, `docs/playstore_assets/galaxy_icons/`), 문구는 덜 딱딱하게("마트 와서 뭐 사러 왔더라?", "나에게 보내기로 보낸 장보기 또 찾고 계셨죠?" 등). **"카톡" 단어는 스크린샷·스토어 문구에서 전부 제거**(타사 상표, Play 메타데이터 정책) · `scripts/compose_store_v4.py`, `docs/playstore_assets/v4_widget/`
- 2026-11-12 · **위젯 5종 → 2종 + 위젯에서 바로 체크**: 개수 위젯(처음엔 2x1로 놓임 — 2x2는 갤럭시의 세로로 긴 칸에서 2x4처럼 보여서; 늘리면 줄이 늘고 "+" 생김)과 목록 위젯(처음 4x2 — 4x3은 "이 페이지에는 공간이 없어요"가 자주 떠서, 아무 크기, 놓을 때 마트 하나 또는 전체를 고름, 스크롤 목록 하나, ○ 누르면 ✓ 0.6초 뒤 완료, 이름 = 그 마트 열기, "+" 하나 = 추가 창). 전체는 마트별로 이어지는 한 목록(두 마트 나란히·위젯 안 탭은 시안에서 사용자가 뺌). 마트 고르기: 앱 안 위젯 추가 창(`MartPicker`) 또는 갤럭시 위젯 목록에서 놓을 때/길게 눌러 "설정"(`WidgetConfigActivity`). 2x2·2x4·4x4 위젯 클래스 삭제(놓아 둔 사용자는 다시 추가해야 함). 위젯 추가 창에 "다음 창에서 위젯 그림을 꾹 누르면 홈 화면에서 원하는 자리에 놓을 수 있어요" 안내(삼성 '홈 화면에 추가' 창에서 확인). 온보딩 마지막 장: 오른쪽 큰 파란 '위젯 추가하기' + 왼쪽 회색 글씨 '바로 시작하기'. 온보딩 그림: 1장 = 알림 종 없는 지금 화면으로 다시 캡처, 3장(쇼핑 앱 검색) = 코드로 그린 그림 → 실제 캡처 + 돋보기 말풍선(`onboarding_page_shop.png`), 4장 = 한 마트 목록 위젯(○ 체크 중 ✓, + 하나)으로 다시 그림, 문구 "홈 화면에서 보고, 바로 체크 / 위젯에서 동그라미만 누르면 앱을 안 열어도 체크돼요". Analytics `item_completed_widget` · `widget/{BaseGroceryWidget,GroceryWidget2x1,GroceryWidgetMedium,WidgetConfigActivity,WidgetPinSuccessReceiver,WidgetSizes}.kt`, `widget/actions/CheckItemAction.kt`(신규), `widget/common/WidgetCommon.kt`, `widget/components/WidgetItemRow.kt`, `util/{WidgetPinHelper,WidgetUpdater}.kt`, `ui/components/{WidgetSizePickerSheet,WidgetNudgeSheet}.kt`, 홈·설정·온보딩 호출부, `res/xml`·`res/layout` 위젯 미리보기, `res/drawable/widget_{check_off,check_on,fab}.xml`, `AndroidManifest.xml`, `proguard-rules.pro`, `CLAUDE.md`, 시안 `sample/위젯체크_시안.html`·`sample/위젯2종_시안.html`
- 2026-11-12 · **v1.4.0 (vc10)** "카톡 나에게 보내기 대신" 방향 업데이트 · `app/build.gradle.kts`, `CLAUDE.md`, `docs/playstore_listing.md`
- 2026-11-12 · **여러 줄 공유 → 항목 여러 개**: 카톡 메모·가족이 보낸 리스트를 마트노트로 공유하면 줄마다(한 줄이면 쉼표마다) 항목 후보로 나눠 "받은 글에서 N개를 찾았어요" 시트(전부 체크된 상태, 마트 선택, "N개 담기")로 한 번에 담기. 글머리표·번호·카톡 `[이름] [시간]` 머리말·마트노트 공유 머리글/꼬리말·링크·30자 넘는 문장은 걸러냄. 한 줄이면 예전처럼 추가 시트에 채워짐. 공유 글 최대 길이 100→2000자. Analytics `shared_items_added` · `util/SharedTextParser.kt`(신규), `ui/screens/home/components/SharedItemsSheet.kt`(신규), `MainActivity.kt`, `HomeScreen.kt`, `HomeViewModel.kt`, `util/Analytics.kt`, `app/src/test/.../SharedTextParserTest.kt`(신규)
- 2026-11-12 · **항목 길게 눌러 순서 바꾸기**: 줄을 꾹 누르면 진동과 함께 떠오르고 위아래로 끌어 순서 변경(마트 탭 순서 바꾸기와 같은 reorderable 라이브러리), 놓으면 저장 → 위젯 순서에도 반영. Analytics `items_reordered` · `ItemList.kt`, `ItemRow.kt`, `data/local/ItemDao.kt`, `data/repository/ItemRepository.kt`, `HomeViewModel.kt`
- 2026-11-12 · **항목 줄의 알림(🔔) 버튼 제거 → ⋮ 메뉴 "알림 설정/알림 변경"**으로 이동(줄 컨트롤 4개 → 3개, 1초 체크 집중). 알림이 걸린 항목은 이름 아래 🔔 시간 표시 그대로. Analytics `reminder_set` 추가(사용량 확인용) · `ItemRow.kt`, `HomeViewModel.kt`, `util/Analytics.kt`
- 2026-11-12 · **공유 메시지 끝에 Play 설치 링크** "👉 갤럭시(안드로이드)라면 마트노트 받기: …" 추가(받은 가족이 바로 설치). 보내는 쪽은 받는 사람이 아이폰인지 알 수 없고 iOS 앱도 없어서, 링크를 OS별로 나누는 대신 "갤럭시용"이라고 문구로 밝힘 · `HomeScreen.kt`

## 2026-10-28 · v1.3.0 (vc9) — 쇼핑몰 앱 바로가기 — 커밋 dcc19d1

- 2026-10-28 · **v1.3.0 (vc9)** 버전업 + 스토어 자료: 출시 노트·짧은 설명·자세한 설명(쇼핑 앱 바로가기 추가) `docs/playstore_listing.md`, 스크린샷 8장(01 메인·02 쇼핑 앱 검색 NEW·07 마트구분은 실기기 새 캡처, 나머지 번호만 이동) `docs/playstore_assets/final/`, 합성 스크립트 `scripts/compose_store_v3.py`, 새 원본 `raw/ss1_home.png`·`ss2_home.png`·`ss8_shop.png` · `app/build.gradle.kts`
- 2026-10-28 · **위젯 "+" 버튼 전부 제거**(중간·세로·큰 위젯 마트 머리글 + 크기 선택 미리보기). 항목이 하나도 없을 때 위젯 전체를 덮던 "항목 추가 / 탭해서 추가하세요" 카드 제거 → 평소처럼 마트 줄/칸을 보여주고 빈 마트 칸엔 "아직 적은 게 없어요"(누르면 그 마트 추가 시트). 온보딩 마지막 장 그림·설명도 "+" 없이 "마트를 누르면 바로 그 목록이 열려요"로 · `widget/components/{WidgetStoreHeader,WidgetStoreCountRow}.kt`, `widget/common/WidgetCommon.kt`, `ui/components/WidgetSizePickerSheet.kt`, `ui/screens/onboarding/OnboardingScreen.kt`, `CLAUDE.md`

- 2026-10-28 · **온보딩 개편**: 1·2장 이미지를 현재 앱 실기기 캡처로 교체(예전 캡처는 앱 이름 없음·이마트 탭·추가 버튼 잘림; 새 1장 = 홈 쿠팡 탭+쇼핑앱 토글+FAB 전체, 2장 = 추가 시트) `res/drawable/onboarding_page{1,3}.png`. 다크 모드 장 삭제, "이름만 적으면 끝나요 / …빠르고 심플하게!", 새 3장 "적어둔 그대로 쇼핑 앱에서 검색"(쇼핑몰 앱 바로가기, 그림은 Compose로 그림 `ShopShortcutIllustration`), 마지막 장을 "생각날 때 바로 구매물품 추가해요"(그림 = 위젯 선택창 대신 **폰 홈 화면에 위젯이 놓인 모습**, `HomeWidgetIllustration`)로 바꾸고 하단을 **"위젯 추가하기" / "바로 시작하기" 같은 크기 두 버튼**으로(기존 작은 "위젯 추가 (선택)" 가독성 문제). 위젯 추가는 크기 선택 창을 먼저 띄우고, 추가 요청을 보낸 뒤(또는 창을 닫으면) 온보딩 완료 처리(먼저 완료 처리하면 AppRoot가 즉시 메인으로 바꿔 창이 안 뜨던 문제) · `ui/screens/onboarding/OnboardingScreen.kt`
- 2026-10-28 · 🔍 검색어 오류 수정: 쇼핑 앱이 이미 켜져 있으면 새 검색어를 무시하고 이전 화면(예: "우유")을 띄우던 문제 → 검색 링크에 `FLAG_ACTIVITY_CLEAR_TASK`. S24+에서 직접 확인: 쿠팡·컬리·11번가·옥션(`auction://openwindow.v2?targeturl=`)·테무(`temu://…/search_result.html?search_key=`) 검색 결과, G마켓(`gmarket://search?type=page&keyword=`) 검색창에 입력된 상태 · `util/StoreSearch.kt`

- 2026-10-28 · **"쇼핑몰 앱 바로가기"**: 마트 이름으로 쇼핑몰 앱을 정하고(쿠팡·컬리·이마트·다이소몰·홈플러스·롯데마트GO·하나로마트 등 24개, Play 스토어에서 패키지 확인), 그 마트 탭 목록 맨 위에 "○○ 앱 바로가기" 토글(마트별, 기본 ON). 🔍 → 앱(검색 링크는 아래 "검색어 오류 수정" 항목의 실기기 확인 결과 참고, 링크 없는 앱은 첫 화면), 앱 없으면 Play 스토어(웹사이트 폴백 제거). 같은 날 시안 단계였던 "마트 수정 창에서 쇼핑몰 고르기"는 넣지 않음. DB v2→v3(`stores.shop_app_enabled` 기본 1), 백업 JSON `shopAppEnabled`, 설정의 토글 스위치를 공용 컴포넌트로 이동, 1회 안내 카드는 토글 줄로 대체 · `util/StoreSearch.kt`, `data/local/{StoreEntity,StoreDao,AppDatabase}.kt`, `domain/model/Store.kt`, `data/repository/{StoreRepository,BackupRepository}.kt`, `ui/components/ToggleSwitch.kt`(신규), `HomeScreen.kt`, `HomeViewModel.kt`, `ItemRow.kt`, `SettingsScreen.kt`

- 2026-10-28 · 홈 하단 버튼(공유·음성추가·추가) 색을 마트 색 → **브랜드 파랑 고정**(CLAUDE.md FAB 규칙과 맞춤, 탭마다 버튼 색이 바뀌는 게 어색하다는 피드백) · `ui/screens/home/HomeScreen.kt`
- 2026-10-27~28 · (코드 아님) `sample/온보딩흐름_적고붙이기.html`: 온보딩 1장 → 바로 적기 → 적은 개수가 든 2×2 위젯을 홈 화면에 추가하는 첫 실행 흐름 시안 · `sample/온보딩흐름_적고붙이기.html`

## 2026-10-27~28 · v1.2.2 (vc8) — 커밋 0017aea

- 2026-10-27 · **v1.2.2 (vc8)** 위젯 다크 모드가 앱 안의 "다크 모드" 설정 대신 **폰(시스템) 다크 모드만** 따르도록 변경 — 라이트 홈 화면에 검은 위젯(또는 그 반대)이 뜨던 문제. 폰 테마 전환 시 `App.onConfigurationChanged`가 위젯 재렌더 · `widget/BaseGroceryWidget.kt`, `App.kt`, `widget/WidgetColors.kt`, `app/build.gradle.kts`, `CLAUDE.md`
- 2026-10-27 · **쿠팡·컬리 탭 "N에서 찾기"**: 온라인 마트(이름에 쿠팡/컬리 포함) 탭에서만 항목 줄에 🔍 아이콘 + ⋮ 메뉴 "쿠팡에서 찾기" → 마트 앱 검색 링크(쿠팡 `coupang://search?q=`, 컬리 `kurly://search?keyword=`, 실기기 S24+에서 검색 결과 화면 직행 확인) → 웹 검색(컬리만; 쿠팡 웹은 브라우저에 403) → Play 스토어 순으로 열기. 서버·가입 없음, Analytics `store_search`(target=쿠팡/컬리만) · `util/StoreSearch.kt`(신규), `ui/screens/home/components/{ItemRow,ItemList}.kt`, `ui/screens/home/HomeScreen.kt`, `util/Analytics.kt`
- 2026-10-28 · **상품 키워드 사전 자동완성**: `data/local/ProductDictionary.kt`(신규, 실제 장보기·생활용품 키워드 767개, 카테고리별 흔한 순) — 입력 중 "비슷한 항목" 칩 = 이 마트의 최근 등록 상품(앞글자 일치) + 사전(앞글자 → 포함 순, 최대 8개). 다른 마트 기록은 쓰지 않음("3844" 같은 1회성 입력이 제안으로 뜨는 게 거슬린다는 피드백). 사전은 Phase 15 메뉴→재료 데이터의 재료명과 공유 예정 · `AddItemSheet.kt`
- 2026-10-28 · 추가 시트 자동완성을 패널 위 떠 있는 카드(탭 줄 위에 반투명으로 겹쳐 가독성 나쁨)에서 **패널 안 "최근 등록 상품" 칩 줄 자리**로 이동: 입력 중 일치 항목이 있으면 그 줄이 "비슷한 항목 · 누르면 바로 추가" 파란 칩으로 바뀜(탭=바로 등록). 같은 36dp 줄이라 패널 높이 불변 · `ui/screens/home/components/AddItemSheet.kt`
- 2026-10-28 · 🔍 아이콘을 마트 색 + 옅은 원 배경으로(회색이라 안 보인다는 피드백), 줄 컨트롤 간격 6→10dp, 온라인 마트 탭 첫 진입 시 1회 안내 카드 "항목 옆 🔍를 누르면 쿠팡 앱에서 바로 검색돼요"(닫기/첫 사용 시 종료, `has_seen_search_hint`) · `ItemRow.kt`, `ItemList.kt`, `HomeScreen.kt`, `HomeViewModel.kt`, `SettingsDataStore.kt`
- 2026-10-27 · (코드 아님) `sample/` 시안 3종: 완료 애니메이션 20종+최종추천, 설치 직후 위젯 붙이기 흐름(온보딩 1장+2×2 개수형), Nemotron-Personas-Korea 150명 합성 사용자 시뮬레이션 결과 · `sample/*.html`

- 2026-09-27 · 추가 시트가 입력 중 위아래로 튀던 문제 수정: 자동완성(0~5줄)을 패널 안 대신 패널 위 떠 있는 카드로 분리(패널 높이 고정), "최근 등록 상품" 칩 줄은 비어 있어도 같은 높이 유지(첫 등록 시 한 번 커지던 것 제거) · `ui/screens/home/components/AddItemSheet.kt` (커밋 0346413)

- 2026-09-24 · **v1.2.1 (vc7)** 위젯 미표시 버그 수정: 릴리스 R8이 WorkManager `OverwritingInputMerger` 생성자를 제거해 Glance SessionWorker가 시작 즉시 실패 → 배치된 위젯이 실제 데이터를 한 번도 못 그리고 미리보기 견본(이마트 5/다이소 3)에 고정돼 있던 문제(스토어 1.2.0에도 존재, "위젯 카운트≠앱 카운트"의 진짜 원인). keep 규칙 추가 + 프로세스 시작·앱 이탈 시 자가 복구 렌더 + 위젯 데이터 Flow 재시도/세션당 1개 · `app/proguard-rules.pro`, `util/WidgetUpdater.kt`, `MainActivity.kt`, `widget/BaseGroceryWidget.kt`, `widget/common/WidgetCommon.kt`
- 2026-09-24 · 완료 탭 필터 줄에 "선택 필 따라가기" 최소 스크롤 추가(맨 오른쪽 마트로 스와이프 시 필이 잘린 채 남던 문제). 필터 줄은 완료 0개 마트도 계속 표시(잠깐 숨겼다가 "허전하다"로 원복) · `ui/screens/completed/CompletedScreen.kt`
- 2026-09-24 · (테스트 전용, 숨김) 설정→데이터 JSON 내보내기/가져오기(병합·중복 없음). `SHOW_BACKUP` 빌드 플래그(현재 false)로 노출 제어 — 정식 기능 아님 · `data/repository/BackupRepository.kt`(신규), `ui/screens/settings/{SettingsScreen,SettingsViewModel}.kt`, `data/local/{StoreDao,ItemDao}.kt`, `app/build.gradle.kts`

- 2026-06-19 · Play 출시 자료 일괄 제작: 피처 그래픽 1024×500(`scripts/compose_feature_graphic.py`→`docs/playstore_assets/final/feature_graphic_1024x500.png`), 스토어 등록 복붙 문서(앱이름·짧은/긴 설명·데이터보안·콘텐츠등급·인앱상품 가이드 `docs/playstore_listing.md`), 릴리스 서명 키스토어 생성(`grocery-note-release.jks`, alias grocerynote, gitignore됨) + `keystore.properties` 채움 → **서명된 업로드용 AAB** 빌드(`app/build/outputs/bundle/release/app-release.aab`, 13.8MB, jar verified) · 신규 자료/스크립트

- 2026-06-19 · 리스트 하단 항목이 음성추가/추가 FAB에 가려지던 문제 수정: `ItemList` LazyColumn 하단 contentPadding 8dp→92dp(FAB 56dp+여백)로 마지막 항목이 버튼 위로 스크롤됨. 실기기 확인 완료 · `ui/screens/home/components/ItemList.kt`

- 2026-06-18 · Play스토어 스크린샷 시리즈 v2 재생성(현재 버전·5마트 기준): 실기기(S24+) 캡처 7장(홈/음성인트로/추가시트/위젯피커/완료/이마트홈/다크) → 폰목업+그라데이션+한글카피 합성. 01 마트별 / 02 음성추가 / 03 1초 입력 / 04 위젯 5크기 / 05 완료 / 06 마트색구분 / 07 다크. 광고밴드 자동 제거(y2622–2872) · `scripts/compose_store_v2.py`(신규), `docs/playstore_assets/final/01–07*.png`(교체), `docs/playstore_assets/raw/ss1–7*.png`(교체)

- 2026-06-20 · (세션 중 시도→되돌림) 공유 버튼 상단 ⋮ 이동 / 음성 입력칸 마이크 이동 / 음성 인트로·설정 가이드 추가 등을 검토했으나, 최종적으로 **원래 레이아웃(음성추가 pill + 공유 FAB)** 유지로 결정해 `git checkout HEAD`로 `HomeScreen.kt`·`AddItemSheet.kt` 복원. 리스트 하단 패딩 수정은 유지. versionCode 2→3 · `app/build.gradle.kts`

- 2026-06-16 · 구글 플레이 스토어용 음성추가 스크린샷(07) 제작: 기존 6장과 동일 스타일(그라데이션 배경+폰 목업+캡션)로 음성추가 홈화면을 렌더링, 🎤 "라면 추가" 말풍선 오버레이. 기기 캡처 대신 결정론적 렌더(폰 분리·에뮬 ANR로) · `scripts/compose_voice_shot.py`(신규), `docs/playstore_assets/final/07_voice_add.png`(신규)

- 2026-06-16 · 음성으로 항목 추가: 홈 `추가` 버튼 옆 같은 레벨에 `음성추가` 버튼(마트색 테두리), 시스템 음성인식(RecognizerIntent, ko-KR, RECORD_AUDIO 권한·라이브러리 불필요)으로 현재 마트에 즉시 추가. "라면 추가"→끝 명령어("추가/담아/넣어줘" 등) 제거 후 "라면"만 추가, 인식 성공 시 자동 재실행(계속 듣기). "끝/완료/그만/종료" 등 종료어를 말하면 추가 안 하고 종료(뒤로가기로도 종료) · `ui/screens/home/HomeScreen.kt`

## 커밋됨 (참고용, git log와 동기화 안 됨 — 정확한 이력은 `git log` 참조)

- 2026-06-16 · 탭/필터 스와이프 전환을 네이트식 가로 슬라이드로 변경: 탭 줄 고정·본문만 손가락 방향대로 280ms 슬라이드+페이드(FastOutSlowIn), 부트스트랩은 페이드만. (제거→220ms 크로스페이드→0.97 스케일페이드를 거쳐 최종 슬라이드로 확정) · `ui/screens/home/HomeScreen.kt`, `ui/screens/completed/CompletedScreen.kt` (커밋 `cc6ffb8`)
- 2026-06-09 · 알림 진동·소리 수정: 채널 enableVibration(true)+패턴, 채널 id item_reminders→item_reminders_v2 교체 · `reminder/ReminderScheduler.kt` (커밋 `722a02a`)
- 2026-06-09 · 알림 진입 UX 변경: 항목 행 종 아이콘으로 진입(테두리=미설정/꽉찬종=설정·마트색), ⋮ 메뉴 알림 항목 제거 · `ui/screens/home/components/{ItemRow,ItemList}.kt`, `ui/screens/home/HomeScreen.kt` (커밋 `2e675f5`)
- 2026-06-09 · BootReceiver LOCKED_BOOT_COMPLETED 제거 (Direct Boot 시 DB 접근 불가) · `reminder/BootReceiver.kt` (커밋 `bbfc8e1`)
- 2026-06-09 · 항목별 일회성 푸시 알림 기능 (v1.0.0→1.1.0/vc2): 날짜·시각 시트 + 🔔 칩 + 로컬 알림(AlarmManager inexact, 재부팅 재등록) + 알림 탭→마트·항목 하이라이트 + POST_NOTIFICATIONS 13+ 권한. Room v1→v2(reminder_at) · `reminder/*`(신규), `di/ReminderEntryPoint.kt`, `data/local/*`, `util/ReminderFormat.kt`, `AndroidManifest.xml` 외 (커밋 `0d450b8`)
- 2026-05-26 · 위젯 XLarge breakpoint(320×320, 최대 6마트) + 공유 다이얼로그(부탁 문구 200자·15개 회전) + 공유 SmallFAB + 홈 타이틀 "마트노트" + 위젯 클릭 라우팅 개선 등 5/21~5/26 작업 일괄 · `widget/*`, `ui/screens/home/components/ShareRequestDialog.kt` 외 (커밋 `e2a0b14`~`cfc3a13` 사이)
- 2026-05-19 · 완료 화면 리스트 스와이프 삭제/되돌리기 제거 (실기기 미작동) (커밋 `019e321`)
- 2026-05-19 · 온보딩 위젯 페이지를 마지막으로 이동 + 다크모드 이미지 실기기 캡처로 교체 (커밋 `a4ff9de`)
- 2026-05-19 · 다크모드 온보딩 페이지 추가 + 온보딩/스플래시 구조 개선 + 스샷2 실기기화 (커밋 `e2a0b14`)
- 2026-05-19 · AdMob ID debug/release 완전 분리 + 릴리스 로그·분석 하드닝 (커밋 `ad2bd80`)
- 2026-05-19 · 아이콘 여백 + 스플래시 일치 + 온보딩→메인 전환 매끄럽게 (커밋 `6a60d70`)
- 2026-05-19 · 디자인 원본 자산 + 디자인 인수인계서 보관 (커밋 `9c3c34b`)
- 2026-05-19 · Phase 10 실기기 피드백 수정 (시트 마트선택·배너 노출·위젯 핀 크기) (커밋 `cfc3a13`)
- 2026-05-18 · 온보딩 일러스트·어댑티브 아이콘 + Play Store 스크린샷 6장 (커밋 `f60f4f4`)
- 2026-05-18 · 위젯 핀 사례 교훈 인수인계서 추가 (커밋 `d448a6f`)
- 2026-05-18 · 위젯 핀 성공 후 홈화면 자동 이동 (커밋 `4a95cee`)
- 2026-05-18 · Long(2x4) 위젯 추가 + 5종 적응형 리사이즈, 피커/온보딩 카피 정리 (커밋 `ae52437`)
- 2026-05-18 · 위젯 모델 표시 전용 피벗, 완료 애니메이션 V2, 카피·문서 동기화 (커밋 `541b713`)
- 2026-05-18 · 앱명을 마트노트로 변경, 4사이즈 위젯, 마트 관리, UX 폴리싱 (커밋 `24eb07e`)
- 2026-05-14 · Phase 3-6 일괄: 풀 앱, 위젯, 광고/결제, 릴리스 (커밋 `41dbcf1`)
- 2026-05-14 · Phase 2: Room 데이터 계층 + Hilt + 개발 테스트 화면 (커밋 `06934dd`)
- 2026-05-14 · Phase 1: 프로젝트 부트스트랩 + 토스 디자인 시스템 (커밋 `94e3b4a`)
