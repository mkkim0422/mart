# 마트노트 변경 기록 (CHANGELOG)

> 사용자 지시 1건 = 1줄. 최신이 맨 위. 탐색·빌드·실기기 검증은 기록하지 않음.
> 형식: `- YYYY-MM-DD · {한 줄 요약} · {대표 파일들}` (커밋되면 끝에 ` (커밋 abc1234)` 추가)

## 미커밋 (워킹트리)

(없음)

## 2026-10-28 · v1.3.0 (vc9) — 쇼핑몰 앱 바로가기

- 2026-10-28 · **v1.3.0 (vc9)** 버전업 + 스토어 자료: 출시 노트·짧은 설명·자세한 설명(쇼핑 앱 바로가기 추가) `docs/playstore_listing.md`, 스크린샷 8장(01 메인·02 쇼핑 앱 검색 NEW·07 마트구분은 실기기 새 캡처, 나머지 번호만 이동) `docs/playstore_assets/final/`, 합성 스크립트 `scripts/compose_store_v3.py`, 새 원본 `raw/ss1_home.png`·`ss2_home.png`·`ss8_shop.png` · `app/build.gradle.kts`
- 2026-10-28 · **위젯 "+" 버튼 전부 제거**(중간·세로·큰 위젯 마트 머리글 + 크기 선택 미리보기). 항목이 하나도 없을 때 위젯 전체를 덮던 "항목 추가 / 탭해서 추가하세요" 카드 제거 → 평소처럼 마트 줄/칸을 보여주고 빈 마트 칸엔 "아직 적은 게 없어요"(누르면 그 마트 추가 시트). 온보딩 마지막 장 그림·설명도 "+" 없이 "마트를 누르면 바로 그 목록이 열려요"로 · `widget/components/{WidgetStoreHeader,WidgetStoreCountRow}.kt`, `widget/common/WidgetCommon.kt`, `ui/components/WidgetSizePickerSheet.kt`, `ui/screens/onboarding/OnboardingScreen.kt`, `CLAUDE.md`

- 2026-10-28 · **온보딩 개편**: 1·2장 이미지를 현재 앱 실기기 캡처로 교체(예전 캡처는 앱 이름 없음·이마트 탭·추가 버튼 잘림; 새 1장 = 홈 쿠팡 탭+쇼핑앱 토글+FAB 전체, 2장 = 추가 시트) `res/drawable/onboarding_page{1,3}.png`. 다크 모드 장 삭제, "이름만 적으면 끝나요 / …빠르고 심플하게!", 새 3장 "적어둔 그대로 쇼핑 앱에서 검색"(쇼핑몰 앱 바로가기, 그림은 Compose로 그림 `ShopShortcutIllustration`), 마지막 장을 "생각날 때 바로 구매물품 추가해요"(그림 = 위젯 선택창 대신 **폰 홈 화면에 위젯이 놓인 모습**, `HomeWidgetIllustration`)로 바꾸고 하단을 **"위젯 추가하기" / "바로 시작하기" 같은 크기 두 버튼**으로(기존 작은 "위젯 추가 (선택)" 가독성 문제). 위젯 추가는 크기 선택 창을 먼저 띄우고, 추가 요청을 보낸 뒤(또는 창을 닫으면) 온보딩 완료 처리(먼저 완료 처리하면 AppRoot가 즉시 메인으로 바꿔 창이 안 뜨던 문제) · `ui/screens/onboarding/OnboardingScreen.kt`
- 2026-10-28 · 🔍 검색어 오류 수정: 쇼핑 앱이 이미 켜져 있으면 새 검색어를 무시하고 이전 화면(예: "우유")을 띄우던 문제 → 검색 링크에 `FLAG_ACTIVITY_CLEAR_TASK`. S24+에서 직접 확인: 쿠팡·컬리·11번가·옥션(`auction://openwindow.v2?targeturl=`)·테무(`temu://…/search_result.html?search_key=`) 검색 결과, G마켓(`gmarket://search?type=page&keyword=`) 검색창에 입력된 상태 · `util/StoreSearch.kt`

- 2026-10-28 · **"쇼핑몰 앱 바로가기"**: 마트 이름으로 쇼핑몰 앱을 정하고(쿠팡·컬리·이마트·다이소몰·홈플러스·롯데마트GO·하나로마트 등 24개, Play 스토어에서 패키지 확인), 그 마트 탭 목록 맨 위에 "○○ 앱 바로가기" 토글(마트별, 기본 ON). 🔍 → 앱(검색 링크는 아래 "검색어 오류 수정" 항목의 실기기 확인 결과 참고, 링크 없는 앱은 첫 화면), 앱 없으면 Play 스토어(웹사이트 폴백 제거). 같은 날 시안 단계였던 "마트 수정 창에서 쇼핑몰 고르기"는 넣지 않음. DB v2→v3(`stores.shop_app_enabled` 기본 1), 백업 JSON `shopAppEnabled`, 설정의 토글 스위치를 공용 컴포넌트로 이동, 1회 안내 카드는 토글 줄로 대체 · `util/StoreSearch.kt`, `data/local/{StoreEntity,StoreDao,AppDatabase}.kt`, `domain/model/Store.kt`, `data/repository/{StoreRepository,BackupRepository}.kt`, `ui/components/ToggleSwitch.kt`(신규), `HomeScreen.kt`, `HomeViewModel.kt`, `ItemRow.kt`, `SettingsScreen.kt`

- 2026-10-28 · 홈 하단 버튼(공유·음성추가·추가) 색을 마트 색 → **브랜드 파랑 고정**(CLAUDE.md FAB 규칙과 맞춤, 탭마다 버튼 색이 바뀌는 게 어색하다는 피드백) · `ui/screens/home/HomeScreen.kt`
- 2026-10-27~28 · (코드 아님) `sample/온보딩흐름_적고붙이기.html`: 온보딩 1장 → 바로 적기 → 적은 개수가 든 2×2 위젯을 홈 화면에 추가하는 첫 실행 흐름 시안 · `sample/온보딩흐름_적고붙이기.html`

## 2026-10-27~28 · v1.2.2 (vc8) — 커밋 99225c0

- 2026-10-27 · **v1.2.2 (vc8)** 위젯 다크 모드가 앱 안의 "다크 모드" 설정 대신 **폰(시스템) 다크 모드만** 따르도록 변경 — 라이트 홈 화면에 검은 위젯(또는 그 반대)이 뜨던 문제. 폰 테마 전환 시 `App.onConfigurationChanged`가 위젯 재렌더 · `widget/BaseGroceryWidget.kt`, `App.kt`, `widget/WidgetColors.kt`, `app/build.gradle.kts`, `CLAUDE.md`
- 2026-10-27 · **쿠팡·컬리 탭 "N에서 찾기"**: 온라인 마트(이름에 쿠팡/컬리 포함) 탭에서만 항목 줄에 🔍 아이콘 + ⋮ 메뉴 "쿠팡에서 찾기" → 마트 앱 검색 링크(쿠팡 `coupang://search?q=`, 컬리 `kurly://search?keyword=`, 실기기 S24+에서 검색 결과 화면 직행 확인) → 웹 검색(컬리만; 쿠팡 웹은 브라우저에 403) → Play 스토어 순으로 열기. 서버·가입 없음, Analytics `store_search`(target=쿠팡/컬리만) · `util/StoreSearch.kt`(신규), `ui/screens/home/components/{ItemRow,ItemList}.kt`, `ui/screens/home/HomeScreen.kt`, `util/Analytics.kt`
- 2026-10-28 · **상품 키워드 사전 자동완성**: `data/local/ProductDictionary.kt`(신규, 실제 장보기·생활용품 키워드 767개, 카테고리별 흔한 순) — 입력 중 "비슷한 항목" 칩 = 이 마트의 최근 등록 상품(앞글자 일치) + 사전(앞글자 → 포함 순, 최대 8개). 다른 마트 기록은 쓰지 않음("3844" 같은 1회성 입력이 제안으로 뜨는 게 거슬린다는 피드백). 사전은 Phase 15 메뉴→재료 데이터의 재료명과 공유 예정 · `AddItemSheet.kt`
- 2026-10-28 · 추가 시트 자동완성을 패널 위 떠 있는 카드(탭 줄 위에 반투명으로 겹쳐 가독성 나쁨)에서 **패널 안 "최근 등록 상품" 칩 줄 자리**로 이동: 입력 중 일치 항목이 있으면 그 줄이 "비슷한 항목 · 누르면 바로 추가" 파란 칩으로 바뀜(탭=바로 등록). 같은 36dp 줄이라 패널 높이 불변 · `ui/screens/home/components/AddItemSheet.kt`
- 2026-10-28 · 🔍 아이콘을 마트 색 + 옅은 원 배경으로(회색이라 안 보인다는 피드백), 줄 컨트롤 간격 6→10dp, 온라인 마트 탭 첫 진입 시 1회 안내 카드 "항목 옆 🔍를 누르면 쿠팡 앱에서 바로 검색돼요"(닫기/첫 사용 시 종료, `has_seen_search_hint`) · `ItemRow.kt`, `ItemList.kt`, `HomeScreen.kt`, `HomeViewModel.kt`, `SettingsDataStore.kt`
- 2026-10-27 · (코드 아님) `sample/` 시안 3종: 완료 애니메이션 20종+최종추천, 설치 직후 위젯 붙이기 흐름(온보딩 1장+2×2 개수형), Nemotron-Personas-Korea 150명 합성 사용자 시뮬레이션 결과 · `sample/*.html`

- 2026-09-27 · 추가 시트가 입력 중 위아래로 튀던 문제 수정: 자동완성(0~5줄)을 패널 안 대신 패널 위 떠 있는 카드로 분리(패널 높이 고정), "최근 등록 상품" 칩 줄은 비어 있어도 같은 높이 유지(첫 등록 시 한 번 커지던 것 제거) · `ui/screens/home/components/AddItemSheet.kt` (커밋 a3a2491)

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

- 2026-06-16 · 탭/필터 스와이프 전환을 네이트식 가로 슬라이드로 변경: 탭 줄 고정·본문만 손가락 방향대로 280ms 슬라이드+페이드(FastOutSlowIn), 부트스트랩은 페이드만. (제거→220ms 크로스페이드→0.97 스케일페이드를 거쳐 최종 슬라이드로 확정) · `ui/screens/home/HomeScreen.kt`, `ui/screens/completed/CompletedScreen.kt` (커밋 `c33ade6`)
- 2026-06-09 · 알림 진동·소리 수정: 채널 enableVibration(true)+패턴, 채널 id item_reminders→item_reminders_v2 교체 · `reminder/ReminderScheduler.kt` (커밋 `0deda0e`)
- 2026-06-09 · 알림 진입 UX 변경: 항목 행 종 아이콘으로 진입(테두리=미설정/꽉찬종=설정·마트색), ⋮ 메뉴 알림 항목 제거 · `ui/screens/home/components/{ItemRow,ItemList}.kt`, `ui/screens/home/HomeScreen.kt` (커밋 `f2f0aec`)
- 2026-06-09 · BootReceiver LOCKED_BOOT_COMPLETED 제거 (Direct Boot 시 DB 접근 불가) · `reminder/BootReceiver.kt` (커밋 `0a34ae2`)
- 2026-06-09 · 항목별 일회성 푸시 알림 기능 (v1.0.0→1.1.0/vc2): 날짜·시각 시트 + 🔔 칩 + 로컬 알림(AlarmManager inexact, 재부팅 재등록) + 알림 탭→마트·항목 하이라이트 + POST_NOTIFICATIONS 13+ 권한. Room v1→v2(reminder_at) · `reminder/*`(신규), `di/ReminderEntryPoint.kt`, `data/local/*`, `util/ReminderFormat.kt`, `AndroidManifest.xml` 외 (커밋 `dce5bf1`)
- 2026-05-26 · 위젯 XLarge breakpoint(320×320, 최대 6마트) + 공유 다이얼로그(부탁 문구 200자·15개 회전) + 공유 SmallFAB + 홈 타이틀 "마트노트" + 위젯 클릭 라우팅 개선 등 5/21~5/26 작업 일괄 · `widget/*`, `ui/screens/home/components/ShareRequestDialog.kt` 외 (커밋 `e1e57c4`~`62e5019` 사이)
- 2026-05-19 · 완료 화면 리스트 스와이프 삭제/되돌리기 제거 (실기기 미작동) (커밋 `8475fc4`)
- 2026-05-19 · 온보딩 위젯 페이지를 마지막으로 이동 + 다크모드 이미지 실기기 캡처로 교체 (커밋 `f652afd`)
- 2026-05-19 · 다크모드 온보딩 페이지 추가 + 온보딩/스플래시 구조 개선 + 스샷2 실기기화 (커밋 `e1e57c4`)
- 2026-05-19 · AdMob ID debug/release 완전 분리 + 릴리스 로그·분석 하드닝 (커밋 `7f3f51b`)
- 2026-05-19 · 아이콘 여백 + 스플래시 일치 + 온보딩→메인 전환 매끄럽게 (커밋 `7fb405d`)
- 2026-05-19 · 디자인 원본 자산 + 디자인 인수인계서 보관 (커밋 `be71e8f`)
- 2026-05-19 · Phase 10 실기기 피드백 수정 (시트 마트선택·배너 노출·위젯 핀 크기) (커밋 `62e5019`)
- 2026-05-18 · 온보딩 일러스트·어댑티브 아이콘 + Play Store 스크린샷 6장 (커밋 `9ee412b`)
- 2026-05-18 · 위젯 핀 사례 교훈 인수인계서 추가 (커밋 `0bd96bf`)
- 2026-05-18 · 위젯 핀 성공 후 홈화면 자동 이동 (커밋 `5767e42`)
- 2026-05-18 · Long(2x4) 위젯 추가 + 5종 적응형 리사이즈, 피커/온보딩 카피 정리 (커밋 `f3d2e9b`)
- 2026-05-18 · 위젯 모델 표시 전용 피벗, 완료 애니메이션 V2, 카피·문서 동기화 (커밋 `f6d8177`)
- 2026-05-18 · 앱명을 마트노트로 변경, 4사이즈 위젯, 마트 관리, UX 폴리싱 (커밋 `987a26b`)
- 2026-05-14 · Phase 3-6 일괄: 풀 앱, 위젯, 광고/결제, 릴리스 (커밋 `e4d78cd`)
- 2026-05-14 · Phase 2: Room 데이터 계층 + Hilt + 개발 테스트 화면 (커밋 `43b245a`)
- 2026-05-14 · Phase 1: 프로젝트 부트스트랩 + 토스 디자인 시스템 (커밋 `85f320d`)
