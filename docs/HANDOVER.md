# 마트노트(MartNote) 인수인계서

> **이 문서의 목적**: 다른 Claude AI 세션(또는 사람)이 이 프로젝트를 처음부터 끝까지 교차검증할 수 있도록, "어떻게 시작했고 → 어떻게 지금까지 왔으며 → 현재 화면이 어떤 상태이고 → 다음에 무엇을 할지"를 코드 근거와 함께 빠짐없이 정리한 문서입니다.
> **검증 기준일**: 2026-05-18 · **검증 커밋**: `987a26b` (main) · **작성**: 전체 60개 Kotlin 파일(약 7,152 LOC) 직접 정독 후 작성.
> **읽는 순서**: 본 문서 → `CLAUDE.md`(영구 설계 헌장) → 코드. **단, 8장 "스펙↔코드 불일치"를 반드시 먼저 확인하세요. CLAUDE.md의 일부 핵심 서술이 현재 코드와 다릅니다.**

---

## 1. 한눈에 보기

| 항목 | 값 |
|---|---|
| 앱 이름 | 마트노트 (EN: MartNote) — 2026-05-17 "장보기 메모"에서 개명 |
| 한 줄 정의 | 마트별 장보기 리스트를 홈화면 위젯에서 보고, 앱에서 빠르게 체크하는 한국형 초심플 앱 |
| 패키지(영구) | `com.rldjrgo.grocerynote` (Play 출시 후 변경 불가, 개명 후에도 패키지는 grocerynote 유지) |
| 저장소 | https://github.com/mkkim0422/mart (브랜치 `main`) |
| 소유자 | mkkim0422 / help@sphinfo.co.kr |
| 플랫폼 | Android 전용 (minSdk 26 / target·compileSdk 36) |
| 버전 | versionName `1.0.0`, versionCode `1` |
| 시작일 | 2026-05-14 |
| 현재 상태 | 기능 구현 완료(Phase 1~6) + 1차 대규모 리뉴얼 완료. **출시 전 사용자 측 자산(키스토어/실 광고ID/스크린샷 등) 미완** |
| 빌드 산출물(기록) | Release APK 5.91MB, AAB 11.91MB (목표 APK ≤10MB 충족) |

---

## 2. 어떻게 시작했나 (기획 의도)

### 2.1 문제 정의
한국 사용자는 마트마다 살 물건이 다른데(이마트 vs 다이소 vs 쿠팡), 장보기 앱은 보통 **한 리스트에 다 섞어** 적게 만든다 → 마트에 가서 다시 골라야 함. 기존 가계부/가격비교 앱은 무겁고 복잡함.

### 2.2 북극성 — 3대 차별점 (절대 타협 금지, CLAUDE.md §2)
1. **마트별 분리 리스트** — 가로 스크롤 탭으로 마트마다 따로
2. **홈화면 위젯**이 마트별 리스트를 보여줌
3. **위젯에서 항목을 탭하면 즉시 체크 → 완료로 이동** ("킬러 기능"으로 기획됨)

> ⚠️ **3번은 현재 코드에서 제거된 상태입니다. 8장을 반드시 확인하세요.**

### 2.3 핵심 원칙 (CLAUDE.md §4)
- **이름만 입력** — 가격/수량/단위/메모 없음
- 1초 추가, 1초 체크
- 마트별 분리가 척추
- 위젯이 곧 앱 (위젯 UX 최우선)
- 회원가입 없음, 전부 로컬 저장
- 하단 배너 광고 1개만 (전면/네이티브/리워드 광고 없음)
- 보급형 한국 폰에서 부드럽게 (APK ≤10MB, 메모리 ≤50MB)
- 토스 스타일 미니멀 디자인

### 2.4 절대 금지(v2.0까지) — Hard No (CLAUDE.md §3)
가격/수량/단위/메모 입력 · 카톡 Import · 회원가입/로그인(v1.5에 옵션 검토만) · 카테고리/태그 · 통계 탭 · 푸시 남발 · 하단 탭 4개 이상.

### 2.5 타겟
- 1차: 30~50대 한국 여성(주부·워킹맘)
- 2차: 20~40대 1인 가구

---

## 3. 어떻게 지금까지 왔나 (개발 타임라인)

git 히스토리 기준 4개 커밋으로 압축되어 있습니다.

### 커밋 1 — `85f320d` (2026-05-14 22:46) · Phase 1
프로젝트 부트스트랩 + 토스 디자인 시스템.
- Gradle wrapper, Hilt-ready 매니페스트, 토스 팔레트/타이포/스페이싱 토큰, 다크모드, 8색 마트 컬러
- **교훈 기록**: WebSearch가 라이브러리 버전을 환각(Hilt 2.57.1 등 실재하지 않음) → 이후 Maven 메타데이터 직접 조회로 버전 확정
- 스택 고정: AGP 9.2.1, Kotlin 2.3.21, KSP 2.3.8, Hilt 2.59.2, Compose BOM 2026.05.00, Glance 1.1.1, Room 2.8.4, Nav 2.9.8 / JDK 21 → Java 17 바이트코드 / SDK 26~36

### 커밋 2 — `43b245a` (2026-05-14 22:57) · Phase 2
Room 데이터 계층 + Hilt + 개발용 테스트 화면.
- 2테이블 스키마(stores ⤳ items, FK CASCADE), Flow 기반 DAO, 시드 DB(당시 이마트/다이소/쿠팡), 도메인 모델, 리포지토리, Hilt DatabaseModule
- `app/schemas/1.json` 내보내기(향후 마이그레이션 diff용)
- DevTestScreen을 MainActivity에 임시 연결(폰에서 시드/추가/완료/삭제 검증용, Phase 3에서 교체 예정)

### 커밋 3 — `e4d78cd` (2026-05-14 23:41) · Phase 3~6 일괄
앱 전체 + 위젯 + 광고/결제 + 릴리스를 한 번에.
- **Phase 3 메인 UI**: 가로 마트 탭, 1.3초 줄긋기+페이드 체크 애니메이션, 항목 추가 시트(자동완성·최근항목 칩), 마트 추가 시트(14 이모지×8색), 하단 3탭 네비
- **Phase 4 위젯(킬러 기능)**: `SizeMode.Responsive` 단일 Glance 위젯(Small/Medium/Large). API 31+는 `CheckItemAction`으로 위젯에서 직접 완료, API 26~30은 `OpenStoreAction` 딥링크 폴백. 주/야간 ColorProvider (Glance color 팩토리 위치를 AAR 디스어셈블로 찾아냄)
- **Phase 5 완료/설정/온보딩**: 날짜 버킷 그룹핑, 마트 필터 칩, DataStore 기반 다크모드/온보딩 게이트, SAF JSON 내보내기, 2단계 전체삭제, 3페이지 온보딩
- **Phase 6 광고/결제/릴리스**: AdMob 적응형 배너, BillingClient v8(remove_ads), Firebase Analytics/Crashlytics 자동 초기화, 조건부 서명(.jks 있을 때만 서명 → 키스토어 없이도 unsigned 릴리스 빌드 가능), ProGuard 규칙, APK 5.91MB

### 커밋 4 — `987a26b` (2026-05-18 08:28) · 1차 대규모 리뉴얼 (현재 HEAD)
"rename to 마트노트, 4-size widgets, store management, UX polish"
- 앱명 장보기 메모 → **마트노트**(패키지 불변)
- 위젯: 단일 Responsive → **4개 독립 Glance 위젯**(신규 2x1 "Mini" 포함), 개수 기반 피커 설명, 빈 상태 재디자인
- **마트 관리 화면 신규**: 탭바 ⋮ → 드래그 재정렬(sh.calvin.reorderable 3.1.0), 추가 시트 스타일 편집, 소프트 삭제+되돌리기
- 공유 `PageTitle`(구매예정/완료/설정)
- 기본 시드 마트 → **쿠팡(🚀)/다이소** (이마트 제거)
- "완료" 탭 시 **즉시 제거**(애니메이션 지연 삭제)
- 설정: "노출순서설정" 리네임, 피드백 브리지 시트(mkkim850422@gmail.com), 인트로 미리보기
- ⚠️ **이 커밋에서 `CheckItemAction.kt`(36줄)와 단일 `GroceryWidget.kt`(259줄)가 삭제됨** → 8장 참조

---

## 4. 현재 기술 스택 & 빌드 구성

- **언어/빌드**: Kotlin 2.3.21 · AGP 9.2.1 · Gradle 9.5.1 · KSP 2.3.8 · JDK 21(→ Java 17 바이트코드)
- **UI**: Jetpack Compose (BOM 2026.05.00) · Material3 · Navigation Compose 2.9.8
- **위젯**: Jetpack Glance 1.1.1
- **DB**: Room 2.8.4 (스키마 v1, exportSchema=true)
- **DI**: Hilt 2.59.2 + hilt-navigation-compose 1.3.0
- **비동기/저장**: kotlinx-coroutines 1.10.2 · DataStore Preferences 1.2.1
- **드래그 재정렬**: sh.calvin.reorderable 3.1.0
- **수익화/분석**: play-services-ads 25.2.0 · billing-ktx 8.3.0 · firebase-bom 34.13.0(analytics+crashlytics)
- **SDK 분기 정책(CLAUDE.md §7)**: API 26~30은 위젯 비대화형→딥링크 폴백 / API 31+ 대화형 / API 33+ Material You 동적색 미사용(브랜드 일관성)
- **BuildConfig 플래그**: `SHOW_BILLING=true`(debug·release 모두), `AD_UNIT_BANNER_ID`/`ADMOB_APP_ID`는 **테스트 ID 하드코딩 상태**
- **서명**: `keystore.properties` + `.jks`가 존재하고 비번이 있을 때만 release 서명. 없으면 unsigned 빌드 통과
- **로케일**: ko, en만 포함
- 라이브러리 버전은 `gradle/libs.versions.toml`로 핀 고정. 변경 시 ref 이름 grep 필수.

---

## 5. 데이터 모델 & 동기화 아키텍처

### 5.1 Room 스키마 (DB `grocery_note.db`, version 1)
```
Store(id PK auto, name, colorHex, iconKey, displayOrder, isArchived, createdAt)   index: displayOrder
Item (id PK auto, storeId FK→Store ON DELETE CASCADE, name, isCompleted,
      completedAt?, displayOrder, createdAt)                                       index: storeId, isCompleted
```
- **시드**(최초 생성 시 RoomCallback): 쿠팡 `#3182F6` `emoji:🚀`, 다이소 `#F04452` `store`. `SettingsViewModel.wipeAllData()`도 동일 2개 재시드.
- `iconKey`는 `emoji:🚀` 형태(이모지 직접) 또는 `store`/`box` 같은 키. `Store.emoji()` 확장이 매핑.
- 마트 삭제는 **소프트 삭제**(`isArchived`) → 5초 되돌리기 스낵바 → 미복구 시 hard delete(아이템 CASCADE).

### 5.2 계층 구조
`Room DAO → Repository(Entity↔Domain 매핑) → ViewModel(StateFlow) → Compose 화면`
폴더: `data/{local,repository,billing}` · `domain/model` · `ui/{theme,components,navigation,screens}` · `widget/{common,components,actions}` · `di` · `util`

### 5.3 앱↔위젯 양방향 동기화 (중요)
- **앱 → 위젯**: 모든 ViewModel 변경(add/complete/move/delete/rename/reorder/store)이 `WidgetUpdater.updateAll()` 호출.
- **WidgetUpdater**(`util/WidgetUpdater.kt`, @Singleton): `App.onCreate`에서 `start()`. items+stores Flow를 프로세스 수명 동안 구독 → DB 변경 시 trigger, **120ms 디바운스**로 버스트를 1회 렌더로 합침. 4개 위젯 클래스 각각 `GlanceAppWidget.updateAll(context)` 호출. 위젯 인스턴스가 1개라도 배치돼 있으면 `settings.hasAddedWidget=true` 기록.
- **위젯 → 앱**: 위젯이 `widgetDataFlow(context)`(stores + allItems + largeWidgetStoreIds 결합)를 `collectAsState`로 직접 구독 → DB 변경 시 자동 재구성. Hilt `WidgetEntryPoint`로 리포지토리 접근.
- 이중 안전장치: 위젯은 자체 Flow 구독으로도 갱신되고, `updateAll`은 wake/폴백 경로.

---

## 6. 화면 네비게이션 구조

`MainActivity(@AndroidEntryPoint)` → `AppRoot` → Scaffold(하단 `BottomNavBar`) → `AppNavHost`.

- **시작 분기**: `settings.hasSeenOnboarding`이 false면 `ONBOARDING`, 아니면 `HOME`
- **라우트**: `ONBOARDING` · `HOME` · `COMPLETED` · `SETTINGS` · `STORE_MANAGE`
- **하단 탭(3개)**: 구매예정(HOME) / 완료(COMPLETED) / 설정(SETTINGS) — 선택 색 `#3182F6`. `ONBOARDING`·`STORE_MANAGE`에서는 하단바 숨김
- **전환 애니메이션**: 좌우 슬라이드 300ms (push=Left, pop=Right)
- **딥링크 버스**(`DeepLinkBus`, MainActivity 내 object): 위젯/공유/숏컷 인텐트를 Hilt 생성 전 단계에서 수신.
  - `ACTION_SEND text/plain`(카톡 등 공유) → 항목추가 시트에 텍스트 프리필
  - `action=ADD_ITEM` + `store_id`(위젯 "+"/앱 숏컷) → 해당 마트 추가 시트 자동
  - `action=OPEN_HOME`(빈 위젯 탭) → 홈으로 강제 이동
  - `HOME_DEEPLINK_STORE_ARG`/`ITEM_ARG` → 해당 마트 선택 + 항목 하이라이트(1.5초)
  - 앱이 설정/완료 화면에 있을 때 딥링크가 와도 먼저 HOME으로 라우팅 후 HomeScreen이 버스를 소비

---

## 7. 지금까지 만든 화면 — 상세

### 7.1 온보딩 (`ui/screens/onboarding/OnboardingScreen.kt`)
- `HorizontalPager` 3페이지. 우상단 "건너뛰기", 하단 "다음"/"시작하기", 인디케이터 점
- 페이지 카피: ①"마트별로 따로 적어두세요 / 이마트, 다이소, 쿠팡… 한 번에 정리" ②"홈화면 위젯에서 바로 체크 / 앱 안 열고도 한 번에" ③"심플하게, 이름만 적으면 끝 / 복잡한 정보 없이, 빠르게"
- 마지막 페이지에 "지금 위젯 추가하기" → `WidgetSizePickerSheet`
- 완료 시 `settings.setOnboardingSeen()` → HOME으로(`popUpTo(ONBOARDING, inclusive)`)
- 설정 > "인트로 미리보기"에서 전체화면 Dialog로 재생(완료 시 닫기만)
- ⚠️ 카피 불일치: 시드에서 이마트가 빠졌는데 ①페이지 텍스트는 여전히 "이마트, 다이소, 쿠팡"

### 7.2 홈 / 구매예정 (`ui/screens/home/HomeScreen.kt` + `HomeViewModel.kt`)
**상단**: 공유 `PageTitle("구매예정")`. 위젯 미추가 & 미닫음이면 파란 배너("위젯을 추가하면 홈화면에서 바로 체크" + 추가 버튼 + 닫기).
**탭바**(`StoreTabBar`): 가로 스크롤 알약 탭. 선택=마트색 채움, 비선택=흰 카드+헤어라인. 각 탭에 미완료 개수 배지. 끝에 점선 "+ 추가"(마트 추가 시트), 원형 "⋮"(마트 관리 화면). 신규 추가 마트는 파란 테두리 펄스+자동 스크롤.
**본문**: 선택 마트의 활성 항목 리스트. 마트 전환 시 좌우 슬라이드 `AnimatedContent` + 화면 가로 스와이프로 이전/다음 마트 이동(`swipeBetweenTabs`).
**항목 행**(`ItemRow`): 이름 + 우측 "완료" 버튼(56×32) + ⋮ 메뉴(이름 수정/다른 마트로 이동/삭제). "완료" 탭 → 햅틱 + 즉시 완료 처리(리스트가 바로 행 제거) → **"✓ {이름} 구매 완료" + [되돌리기] 스낵바**(Short). ⚠️ 행 전체 탭은 완료 아님(명시적 버튼만). ⚠️ 파일에 1.3초 애니메이션 상수(`CHECK_FILL_MS` 등)가 남아있으나 **사용 안 함**(dead code) — 8장.
**빈 상태**: 마트 0개 → `EmptyStores`(마트 추가 유도) / 항목 0개 → `EmptyItems`(추가 버튼).
**FAB**: 우하단 확장형(현재 마트색) "추가" → `AddItemSheet`.
**하단**: `AdBanner`.
**다이얼로그**: 이름 수정, 다른 마트로 이동(다른 마트 목록), 삭제 확인(`ConfirmDialog` — 공용, 설정·마트관리에서도 재사용).
**ViewModel 동작**: store/selectedStore/activeItems/recentNames/highlight/counts/banner Flow를 combine. 전체삭제 후 stale storeId 방어(FK 실패로 항목이 조용히 안 저장되는 버그 방지). 모든 변경 후 `widgetUpdater.updateAll()`. "자주 사는 항목"은 **마트별**.

### 7.3 항목 추가 시트 (`ui/screens/home/components/AddItemSheet.kt`)
- Material3 ModalBottomSheet가 **아님** — 커스텀 `Dialog`. 풀스크린 윈도우 + `SOFT_INPUT_ADJUST_RESIZE` + `imePadding()`으로 키보드와 함께 패널이 **단일 모션**으로 상승(예전 3단 떨림 제거). dim 0.45, 바깥 탭 dismiss(단 `dismissOnClickOutside=false`로 자체 처리).
- 진입 시 자동 포커스+키보드. 입력 후 추가하면 **연속 추가 모드**(필드 비우고 "✓ '{이름}' 추가됨 — 계속 입력하세요" 1.5초 토스트).
- 자동완성: 입력 접두사로 최근 이름 최대 5개. "최근 등록 상품" 칩 최대 10개(탭=추가, X=마트별 최근목록에서 삭제 확인 다이얼로그).
- IME Done 또는 마트색 "추가" 버튼으로 제출. 닫기/완료 시 입력 중이면 커밋 후 닫음.

### 7.4 마트 추가/수정 시트 (`ui/screens/home/components/AddStoreSheet.kt`)
- 이름 입력 + 색 8종 + 아이콘/이모지 선택(14종). 수정 모드는 `title`/`confirmLabel`/`initial*` 파라미터로 동일 시트 재사용(홈·마트관리에서 공유).

### 7.5 완료 (`ui/screens/completed/CompletedScreen.kt` + `CompletedViewModel.kt`)
- `PageTitle("완료")` + 필터 칩(전체 + 마트별, 완료 개수 배지). 화면 가로 스와이프로 필터 전환.
- 본문: **날짜 버킷 그룹핑**(오늘/어제/이번 주/이번 달/이전). 행: 체크 아이콘 + 줄긋기 이름 + 마트 배지 + ⋮.
- 행 인터랙션: **스와이프** — 오른쪽=다시 구매예정으로(success색), 왼쪽=삭제(danger색). ⋮ 메뉴도 동일 2개.
- 되돌리기 스낵바: 재활성/삭제 각각 [되돌리기](삭제는 스냅샷 복원).
- 하단 요약: "이번 주 N개 / 이번 달 N개" + `AdBanner`. 빈 상태: "아직 완료한 항목이 없어요".

### 7.6 설정 (`ui/screens/settings/SettingsScreen.kt` + `SettingsViewModel.kt`)
섹션형 LazyColumn:
- **위젯**: "위젯 추가하기"(→ `WidgetSizePickerSheet`) · "노출순서설정"(서브: 위젯 우선순위 최대 4개 → `WidgetStoreSelectionSheet`)
- **일반**: "다크 모드" 토글(44×26 알약, On/Off만 — Auto는 토글 UI에 없음. 내부 enum은 Auto/On/Off)
- **데이터**: "전체 삭제"(2단계 확인 → `db.clearAllTables()` → 쿠팡/다이소 재시드 → 토스트)
- **결제**(`BuildConfig.SHOW_BILLING`일 때만, 현재 true): "광고 제거 (₩1,900)" / 구매 후 "광고 제거됨"
- **정보**: 버전 표시 · "인트로 미리보기"(온보딩 Dialog) · "피드백 보내기"(브리지 시트 → `mailto:mkkim850422@gmail.com`) · "별점 주기"(`market://`) · "친구에게 추천"(공유 인텐트)
- ⚠️ 피드백 주소(`mkkim850422@gmail.com`)와 스토어 문의(`help@sphinfo.co.kr`)가 불일치 — 의도 확인 필요.

### 7.7 마트 관리 (`ui/screens/store/StoreManageScreen.kt` + `StoreManageViewModel.kt`)
- 상단바: ✕ 닫기 · "마트 관리" · "완료". 하단바 숨김 화면(`STORE_MANAGE`).
- 행: 드래그 핸들(≡, 롱프레스 없이 즉시 드래그) + 색 점 + 이모지 + 이름 + "N개 항목" + ⋮(수정/삭제).
- 재정렬: `sh.calvin.reorderable` 로컬 작업본으로 즉시 반영, 드래그 종료 시 `persistOrder` 1회 커밋(스냅백 방지 머지 로직 있음).
- 수정: `AddStoreSheet` 재사용(프리필). 삭제: 항목 N개 경고 다이얼로그 → **소프트 삭제(archive)** → "✓ '{이름}' 삭제됨 [되돌리기]" → 미복구 시 finalize(hard delete, 항목 CASCADE).
- 하단 풀폭 "+ 마트 추가하기".

### 7.8 위젯 — 4종 독립 Glance 위젯 (`widget/`)
공통: 카드 어디를 탭해도 앱 열림(`WidgetCard` → `OpenStoreAction.forStore(-1L)`), 마트 헤더 우측 "+" 탭은 해당 마트 추가 시트(`OpenStoreAction.addToStore`). `updatePeriodMillis=0`(배터리), 주/야간 자동(GlanceTheme/ColorProvider). 각 위젯 = 클래스+Receiver+xml info, 공통 로직은 `widget/common/WidgetCommon.kt`.

| 위젯 | 크기 | 내용 | 피커 설명 |
|---|---|---|---|
| **Mini** (`GroceryWidget2x1`) | 2×1 110×40 | `SmallContent(compact=true)` — 마트 개수 행 **정확히 2개**(3번째는 잘림, "외 N" 없음) | "2개 마트 표기" |
| **Small** (`GroceryWidgetSmall`) | 2×2 | 마트별 잔여 개수 행(`WidgetStoreCountRow`) ≤4 + "외 N개 마트" | "3개 마트 표기" |
| **Medium** (`GroceryWidgetMedium`) ★기본 | 4×2 | 항목 리스트: 1마트=풀폭 / 2마트=좌우 분할(상위 2개) | "2개 마트 항목 표기" (추천) |
| **Large** (`GroceryWidgetLarge`) | 4×4 | 항목 리스트 최대 4마트(1 / 2 / 1+2 / 2×2). 표시 마트는 `largeWidgetStoreIds`로 사용자 지정 가능, 없으면 자동 | "4개 마트 항목 표기" |

- `WidgetSize` enum 순서: `TWO_BY_ONE, SMALL, MEDIUM, LARGE`. `WidgetSizePickerSheet`(홈/설정/온보딩 공용)가 4종을 실제 위젯 미니렌더 미리보기와 함께 표시 → 선택 시 `requestPinAppWidget`. 런처가 핀 미지원이면 수동 안내 가이드로 폴백.
- 항목 행(`WidgetItemRow`)은 **읽기 전용**(점 + 이름). 탭하면 `OpenStoreAction.forStore(-1L)`로 앱만 열림. **위젯 내 체크박스 토글/완료 처리 없음** → 8장.
- 마트 헤더(`WidgetStoreHeader`): 틴트 이모지 배지 + 이름 + 개수 알약 + "+" 버튼.
- 빈 상태(`WidgetEmptyState`): 마트 0 "마트를 추가해주세요/탭하면 앱이 열려요", 항목 0 "항목 추가/탭해서 추가하세요", 2x1은 축약("탭해서 추가").

### 7.9 공통 컴포넌트
`PageTitle`(3개 상단 화면 공유 제목) · `BottomNavBar` · `SwipeNav`(swipeBetweenTabs) · `UndoSnackbar`/`UndoSnackbarHost` · `AdBanner`(AndroidView 적응형 배너, `isAdRemoved`면 숨김) · `WidgetSizePickerSheet` · `WidgetStoreSelectionSheet`(Large 위젯 표시 마트 최대 4개 선택).

---

## 8. ⚠️ 중대 발견 — 스펙(CLAUDE.md)과 코드 불일치 (교차검증 핵심)

> 교차검증 AI는 **CLAUDE.md만 믿으면 안 됩니다.** 아래는 코드로 확인된 사실입니다.

### 8.1 [치명] 위젯 탭→체크 "킬러 기능"이 코드에서 제거됨
- **CLAUDE.md 주장**(§2-3, §11-12, §22-30): "위젯 항목 탭 → 즉시 줄긋기 → 1초 후 사라짐 → 완료로 이동"이 **3대 차별점의 핵심**이며 `CheckItemAction`(Glance ActionCallback, API 31+)으로 구현.
- **실제 코드**: 커밋 `987a26b`에서 `widget/actions/CheckItemAction.kt`(36줄)와 단일 `GroceryWidget.kt`(259줄) **삭제됨**. 현재 `widget/actions/`에는 `OpenStoreAction.kt`만 존재. `WidgetItemRow.kt` 주석: *"Simple read-only row"* — 탭 시 앱만 열림.
- **결론**: **현재 위젯은 "표시 + 딥링크 전용"**. 위젯에서 직접 체크하는 기획상 1순위 차별점이 동작하지 않음. 의도된 제거인지(아키텍처 피벗) / 복구 대상인지 **사용자 확인 필수**. CLAUDE.md §2,3,11,12,14, 테스트 시나리오 3·4번이 현실과 어긋남.

### 8.2 인앱 체크 애니메이션 제거
- CLAUDE.md §11은 1.3초(0.15s 채움/0.3s 줄긋기/0.7s 대기/0.3s 페이드) 애니메이션 명시. 실제 `ItemRow.kt`는 상수만 남고 `complete`가 즉시 `onCompleteAnimDone()` 호출(애니메이션 없음). 대신 [되돌리기] 스낵바로 대체(의도된 변경 — 커밋 메시지에 명시).

### 8.3 기타 불일치 (경미하나 검증 시 혼동 유발)
- `README.md`: targetSdk를 35/Android15로 표기 → 실제 36. README가 구버전.
- `AndroidManifest.xml`: `tools:targetApi="35"` vs build.gradle target/compile 36.
- CLAUDE.md §14 Phase 표가 동일 항목을 [x]/[ ] 두 번 나열(중복 잔재).
- 시드에서 이마트 제거됐으나 온보딩 1페이지 카피·`store_listing_ko.md`는 여전히 "이마트" 언급.
- 피드백 메일(`mkkim850422@gmail.com`) vs 공식 문의(`help@sphinfo.co.kr`) 불일치.
- `SHOW_BILLING=true`(코드)인데 CLAUDE/주석은 "v1.5까지 결제 UI 숨김"이라 서술 → 현재 결제 행이 **노출**됨.
- CLAUDE.md `Local: C:\mart` vs 실제 작업 경로 다름(영향 없음, 참고).

---

## 9. 알려진 미완료 / 사용자 측 TODO (출시 차단 요소)

CLAUDE.md §15 + 코드 기준. **앱 코드는 완성, 아래는 사용자가 외부에서 준비할 자산**:

| 항목 | 상태 | 영향 |
|---|---|---|
| Pretendard 폰트 4종 → `app/src/main/res/font/` | 미완 | 디자인 스펙상 타이포(현재 시스템 폰트) |
| Firebase 실제 `google-services.json` → `app/` | 미완(플레이스홀더) | Analytics/Crashlytics 실데이터 |
| AdMob 실제 앱ID/배너 unitID | **테스트 ID 하드코딩** | 출시 시 수익 0, 정책 위반 가능 |
| Play Console 등록($25) + 스토어 등록정보 | 미완 (`docs/store_listing_ko.md` 초안 있음) | 출시 불가 |
| 키스토어(.jks)+비번 → `keystore.properties` | 미완 | **분실 시 영구 업데이트 불가**. 현재 unsigned 빌드만 |
| 스크린샷 | 미완 (`scripts/capture_screenshots.ps1` ADB 스크립트 있음) | 스토어 등록 필수 |
| 개인정보처리방침 호스팅 | `docs/privacy_policy_{ko,en}.md` 작성됨, GitHub Pages URL 미배포 | 데이터안전 섹션 |

---

## 10. 다음 버전 계획

> CLAUDE.md에 명시적 v1.5/v2.0 백로그 표는 없음. 아래는 §3 Hard No의 "검토 시점"과 코드 신호(숨긴 결제 등)에서 도출.

### 10.1 v1.0 출시 직전 (코드 변경 없음, 자산만)
9장 TODO 전부 처리 → 서명 release AAB → Play 비공개 테스트 → 출시.

### 10.2 v1.0.x 핫픽스 후보 (의사결정 필요)
- **위젯 탭→체크 킬러 기능 복구 여부 결정**(8.1). 복구 시 `CheckItemAction` 재도입(API 31+ ActionCallback → `ItemRepository.completeItem` via WidgetEntryPoint), API 26~30 폴백 유지.
- 카피/문서 정합화(이마트 잔재, README SDK, 메일 주소, CLAUDE.md §14 중복, SHOW_BILLING 주석 vs 값).

### 10.3 v1.5 (CLAUDE.md상 "검토" 표기 항목)
- **광고 제거 인앱결제 정식화**: `data/billing/BillingRepository`(v8) 이미 구현·컴파일됨, `SHOW_BILLING` 게이트로 노출 제어 → 실제 상품ID 등록 + QA.
- **회원가입/로그인은 "옵션으로만" 검토**(여전히 강제 가입 금지). 클라우드 백업 옵션 가능성.
- DataStore에 이미 존재하나 미사용/부분사용 키 정리: `onboarding_completed_count` 등.

### 10.4 v2.0 (Hard No 해제 가능 시점)
이 시점까지 절대 금지였던 항목들의 **재검토 후보**: 가격/수량/단위/메모, 카테고리/태그, 통계 탭 등. **단, 추가해도 §4 핵심 원칙(이름만·1초·마트분리·위젯 우선·로컬·미니멀)을 깨지 않는 선에서만.**

### 10.5 백로그(코드 흔적 기반 개선 아이디어)
- 위젯 항목 재정렬/직접 편집 UX, Large 위젯 표시 마트 UX 다듬기
- Room 마이그레이션 파이프라인(스키마 v2 대비, `schemas/1.json` 기준 diff)
- 접근성(콘텐츠 설명은 다수 있음, TalkBack 동선 점검)
- 자동 백업 정책(`backup_rules.xml`/`data_extraction_rules.xml` 존재) 검증

---

## 11. 교차검증 체크리스트 (검증 AI에게 던질 질문)

1. **위젯 킬러 기능**: CLAUDE.md가 1순위 차별점이라 한 "위젯 탭→체크"가 코드에 없다(8.1). 이게 의도된 제거인가, 복구해야 하는 회귀(regression)인가?
2. **북극성 정합성**: 위젯 체크가 없는 현재 앱이 §2 북극성을 충족하는가? 충족 못 한다면 v1.0 출시 가능 여부 재판단.
3. **데이터 무결성**: 전체삭제 후 stale `selectedStoreId` 방어 로직(HomeViewModel)이 모든 경로(앱 숏컷·공유·위젯 딥링크)에서 안전한가?
4. **위젯 동기화**: 120ms 디바운스 + 프로세스 수명 Flow 구독이 위젯 다수 배치/저사양 폰에서 누락 없이 1초 내 반영되는가?
5. **수익화 게이트**: `SHOW_BILLING=true`로 결제 UI가 노출되는데 실제 상품 미등록 → 사용자가 누르면? (BillingRepository 실패 처리 확인)
6. **광고 정책**: 테스트 AdMob ID로 출시하면 정책 위반 — 빌드 게이트나 체크리스트로 막혀 있는가?
7. **소프트 삭제 일관성**: 마트 archive vs 항목 hard delete, 되돌리기 윈도우 종료 시점/프로세스 종료 시 finalize 보장?
8. **문서 신뢰도**: CLAUDE.md를 코드 사실에 맞게 갱신할 것인가, 아니면 코드를 스펙에 맞출 것인가(특히 8.1).
9. **온보딩/스토어 카피**의 "이마트" 잔재 등 사용자 노출 텍스트 정합성.
10. **출시 차단 자산**(9장) 중 사용자가 즉시 착수 가능한 항목 우선순위.

---

## 12. 빌드 / 실행

> 환경: Windows. JDK 21(Android Studio 번들 JBR). Android SDK platforms 36 / build-tools 35+.

```powershell
# 디버그 APK
./gradlew assembleDebug
# 폰 설치
./gradlew installDebug
# 릴리스 (키스토어 없으면 unsigned로 통과)
./gradlew bundleRelease   # AAB
./gradlew assembleRelease # APK
# 스크린샷(ADB)
scripts/capture_screenshots.ps1
```
- 서명하려면 루트에 `keystore.properties`(`KEYSTORE_FILE`/`KEYSTORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD`) + `.jks` 배치(둘 다 gitignore).
- Room 스키마는 `app/schemas/`에 export됨 — DB 구조 변경 시 version 올리고 마이그레이션 작성.

---

## 13. 운영 규칙 (CLAUDE.md §16 — 유지)
- 모든 결정은 CLAUDE.md에 기록, 페이즈 종료 시 갱신. **본 인수인계서 발견사항(8장)을 CLAUDE.md에 반영할지 결정 필요.**
- 라이브러리 버전은 `gradle/libs.versions.toml`로만 변경(ref grep 후).
- **사용자는 코드를 읽지 않음.** 보고는 변경 트리 + 빌드 결과 + 폰 설치 상태 + 검증 단계로. 코드 덤프 금지.
- **한국어로 소통.**
</content>
</invoke>
