# 마트노트 (MartNote) — AI 협업용 종합 인수인계서

> **이 문서의 정체**: Claude AI · Gemini AI (또는 다른 코딩 에이전트)가 **이 저장소에 처음 진입했을 때 단독으로 작업을 이어받을 수 있도록** 만든 자기완결적 인수인계서.
> **작성일**: 2026-05-25 · **기준 커밋**: `8475fc4` (`main`) + 워킹트리 5개 파일 수정 (위젯 클릭 라우팅) · **언어 규칙**: 사용자와는 **한국어**로만 대화.
> **읽는 순서**: ① 이 문서 → ② `CLAUDE.md`(영구 헌장) → ③ `docs/HANDOVER.md`(2026-05-18 시점 스냅샷, 일부 항목은 이후 해소됨) → ④ `docs/HANDOFF_위젯자동크기_광고.md`(위젯·광고 이식 가이드, 다른 앱용) → ⑤ 코드.
>
> **이 문서는 위 4문서의 상위 인덱스 + 최신 사실 정정본**입니다. CLAUDE.md / HANDOVER.md 와 충돌하는 부분이 있으면 **이 문서가 우선**입니다 (§13 변경 이력 참조).

---

## 0. AI 에이전트를 위한 30초 브리핑

| 질문 | 답 |
|---|---|
| 이 앱은 뭐? | 한국 마트별 장보기 리스트 + 홈화면 위젯. Android 전용. v1.0.0 출시 준비 단계. |
| 누가 만들고 있는데? | mkkim0422 (1인 인디 개발자, 코드 안 읽음, 실기기 검증 위주). 한국어로만 소통. |
| 지금 어디까지 왔는데? | **Phase 1~10 완료 + 출시 직전 폴리싱 단계.** APK/AAB 빌드 됨, 실기기 동작 확인 완료. 사용자 측 자산(키스토어/실 광고ID/스크린샷·일부)이 마지막 차단 요소. |
| 가장 자주 하는 실수 3개? | (1) WebSearch로 버전 환각 → 항상 Maven 메타데이터 직접 조회. (2) 코드 덤프로 보고 → 사용자는 "변경 트리 + 빌드 결과 + 실기기 확인 절차"만 원함. (3) CLAUDE.md를 절대 진실로 보고 코드와 어긋난 채 그대로 따름 → 본 문서 §13 변경 이력 우선. |
| 사용자가 코드를 안 읽는다는 게 진심? | 진심. 보고는 **무엇이 바뀌었고 / 빌드는 성공했고 / 폰에 깔렸고 / 어디서 무엇을 확인하면 됨** 4줄. 코드 스니펫 X. |
| 위젯 탭 → 체크 "킬러 기능"은? | **2026-05-18 commit `f6d8177`에서 의도적으로 제거됨.** 위젯은 표시 + 딥링크 전용. CLAUDE.md / HANDOVER.md 의 "회귀" 표현은 오해. §3.2 참조. |

---

## 1. 프로젝트 신원

| 필드 | 값 |
|---|---|
| 앱명 (KO) | **마트노트** (2026-05-17 "장보기 메모"에서 개명) |
| 앱명 (EN) | MartNote |
| 패키지 | `com.rldjrgo.grocerynote` (**영구** — Play 출시 후 변경 불가, 개명 이후에도 패키지는 `grocerynote` 유지) |
| 플랫폼 | Android 전용 (iOS 계획 없음) |
| 저장소 | https://github.com/mkkim0422/mart (브랜치 `main`) |
| 로컬 경로 | `C:\mart` (Windows 11 Home) |
| 소유자 | mkkim0422 |
| 사용자 메일 | help@sphinfo.co.kr (스토어/공식 문의) · mkkim850422@gmail.com (앱 내 피드백 발신) |
| 시작일 | 2026-05-14 |
| 현재 버전 | versionName `1.0.0`, versionCode `1` |
| 빌드 산출물 기록 | Release APK 5.91MB, AAB 11.91MB (목표 APK ≤10MB 충족) |

---

## 2. 북극성 — 3대 차별점 (절대 타협 금지)

1. **마트별 분리 리스트** — 가로 스크롤 탭으로 마트마다 따로 (쿠팡/다이소/이마트/홈플러스 등). **#1 USP.**
2. **홈화면에서 한눈 확인 위젯** — 5사이즈(2x1/2x2/2x4/4x2/4x4)로 자유 리사이즈 → **배치된 위젯 모두 자동 어댑티브**. **표시 + 딥링크 전용** (2026-05-18 피벗 이후).
3. **이름만, 1초 추가/체크** — 가격·수량·메모 없음.

위젯 → 앱 흐름: 위젯 탭 → 앱이 **그 마트가 선택된 상태로** 열림 → 사용자는 앱 안에서 §4의 V2 완료 애니메이션(1초 줄긋기+초록✓)으로 체크. **위젯 자체는 데이터를 절대 바꾸지 않음.**

> **위젯에서 직접 체크 다시 넣지 마라.** 2026-05-18 비즈니스 모델 피벗(`f6d8177`) — 단일 배너 광고 노출(수익) + 위젯 배터리 0 (`updatePeriodMillis=0`) 보존을 위해 의도적으로 제거됨.

---

## 3. 절대 원칙 (Hard No) — v2.0까지 금지

CLAUDE.md §3·§4에서 변경 없음. **사용자가 직접 요청해도 거절**:
- 가격/수량/단위/메모 입력 (이름만)
- 카톡 Import
- 회원가입/로그인 (v1.5에 옵션 검토만)
- 카테고리/태그
- 통계 탭
- 푸시 알림 남발
- 하단 탭 4개 이상 (최대 3개)

---

## 4. 핵심 원칙 (절대 깨지 마라)

1. **이름만 입력** — 이름 적기 = 전체 입력 끝
2. **1초 추가, 1초 체크**
3. **마트별 분리가 척추**
4. **위젯이 곧 앱** — 위젯 UX가 인앱 UX보다 우선
5. 회원가입 없음, 전부 로컬
6. 하단 배너 광고 1개만 (전면/네이티브/리워드 없음)
7. 보급형 한국 폰에서 부드럽게 (메모리·배터리·CPU 최소화)
8. 토스 스타일 미니멀

### V2 완료 애니메이션 (인앱 전용, 1초 합계)
파일: `ui/screens/home/components/ItemRow.kt`
- 우측 "완료" 버튼 탭 → 햅틱(LongPress)
- 0~1000ms: 이름 위에 2dp 줄긋기가 **좌→우** 진행 (`FastOutSlowInEasing`, 색 = 텍스트 색 @ alpha 70%)
- 0~250ms: 28dp **초록 원 + 흰 ✓** 가 "완료" 슬롯에 페이드+스케일(0.6→1.0)로 등장 후 1000ms까지 유지. 초록 = `AppColors.success` (Light `#2BA471` / Dark `#3FBE85`)
- 1000ms: 행 제거 + `[되돌리기]` Snackbar (Short)
- 진행 중 그 행의 다른 액션(⋮/이름 수정) 비활성. 여러 행 동시 진행 가능 (각자 1초 따로 종료)
- a11y: contentDescription = "완료됨"; 시스템 `ANIMATOR_DURATION_SCALE == 0`이면 애니메이션 스킵하고 즉시 완료
- 상수: `STRIKE_DURATION_MS = 1000`, `CHECK_FADE_IN_MS = 250`

---

## 5. 대상 사용자

- **1차**: 30~50대 한국 여성 (주부, 워킹맘)
- **2차**: 20~40대 1인 가구

---

## 6. 기술 스택 (2026-05 고정 — 절대 마음대로 바꾸지 말 것)

라이브러리 버전은 `gradle/libs.versions.toml`로 핀 고정. 변경하려면 ref 이름 grep 후 일괄 변경, **그리고 Maven 메타데이터로 실재 여부 검증**.

| 레이어 | 선택 | 버전 |
|---|---|---|
| 언어 | Kotlin | **2.3.21** |
| AGP | Android Gradle Plugin | **9.2.1** |
| Gradle wrapper | | **9.5.1** |
| KSP | | **2.3.8** |
| UI | Jetpack Compose | BOM **2026.05.00** |
| Compose Compiler | `org.jetbrains.kotlin.plugin.compose` | matches Kotlin |
| Material | Material3 (BOM 경유) | from BOM |
| Activity | androidx.activity:activity-compose | **1.13.0** |
| Navigation | androidx.navigation:navigation-compose | **2.9.8** |
| Lifecycle | androidx.lifecycle | **2.10.0** |
| Widget | **Jetpack Glance** | **1.1.1** (1.2.0 아직 RC) |
| DB | Room | **2.8.4** (Room 3.0 alpha — stable 2.x 유지) |
| DI | Hilt | **2.59.2** (2.59 = first AGP-9 compat) |
| Hilt Nav Compose | androidx.hilt:hilt-navigation-compose | **1.3.0** |
| Async | kotlinx-coroutines | **1.10.2** |
| Prefs | androidx.datastore:datastore-preferences | **1.2.1** |
| Core | androidx.core:core-ktx | **1.18.0** |
| AppCompat | androidx.appcompat:appcompat | **1.7.1** |
| Splash | androidx.core:core-splashscreen | **1.2.0** |
| Ads | com.google.android.gms:play-services-ads | **25.2.0** |
| Firebase | com.google.firebase:firebase-bom | **34.13.0** |
| google-services plugin | com.google.gms.google-services | **4.4.4** |
| Crashlytics plugin | com.google.firebase.crashlytics | **3.0.7** |
| Billing | com.android.billingclient:billing-ktx | **8.3.0** |
| 드래그 재정렬 | sh.calvin.reorderable | **3.1.0** |

JDK: **21** (Android Studio 번들 JBR `C:\Program Files\Android\Android Studio\jbr`), 바이트코드는 Java 17
Android SDK: `C:\Users\minkk\AppData\Local\Android\Sdk` (platforms 36, build-tools 36.0.0+)

### 6.1 SDK 정책

| | 값 | 이유 |
|---|---|---|
| minSdk | **26** (Android 8.0 Oreo, 2017) | 한국 보급형 폰 ~99% 커버 |
| targetSdk | **36** (Android 16) | core/activity ≥1.18/1.13 요구 |
| compileSdk | **36** | target과 일치 |

**SDK 분기**:
- API 26~30: 위젯 어댑티브 정상 (탭 → 앱 열림, 그 마트 선택). **인앱 체크박스 토글 없음** — 모든 API에서 위젯은 표시 전용
- API 31+: 동일 (이전엔 `CheckItemAction` 위젯 내 체크 있었으나 `987a26b`에서 제거됨)
- API 33+: Material You 동적 색 **사용 안 함** (브랜드 일관성)

### 6.2 라이브러리 버전 환각 교훈 (반드시 지킬 것)

> 2026-05-14 사건: WebSearch가 Hilt 2.57.1 / Coroutines 1.11.0 / Play-Services-Ads 23.6.0 같은 **실재하지 않는** 버전을 자신감 있게 답함. Maven 메타데이터 직접 조회로 발견.

**모든 Maven 좌표는 메타데이터 XML로 검증**:
- Maven Central: `https://repo1.maven.org/maven2/<group>/<artifact>/maven-metadata.xml`
- Google Maven: `https://dl.google.com/dl/android/maven2/<group>/<artifact>/maven-metadata.xml`

WebSearch · ChatGPT · Gemini · Claude 모두 이 부분은 거짓말함. 메타데이터 XML만 진실.

---

## 7. 데이터 모델

### 7.1 Room 스키마 (`grocery_note.db`, version 1, exported to `app/schemas/1.json`)

```
Store(
  id: Long PK auto,
  name: String,
  colorHex: String,
  iconKey: String,
  displayOrder: Int,
  isArchived: Boolean,
  createdAt: Long
)
indices: stores.displayOrder

Item(
  id: Long PK auto,
  storeId: Long FK -> Store(id) ON DELETE CASCADE,
  name: String,
  isCompleted: Boolean,
  completedAt: Long?,
  displayOrder: Int,
  createdAt: Long
)
indices: items.storeId, items.isCompleted
```

### 7.2 시드 (RoomCallback, 최초 생성 시) — 2026-05-17 변경 후 고정
1. 쿠팡 — `#3182F6`, icon `emoji:🚀`
2. 다이소 — `#F04452`, icon `store`

(`SettingsViewModel.wipeAllData`는 동일 2개를 재시드.)

### 7.3 `iconKey` 규약
- `emoji:🚀` 형태(이모지 직접) 또는 `store`/`box` 같은 사전 정의 키
- `Store.emoji()` 확장 함수가 키 → 이모지 매핑

### 7.4 마트 삭제 정책 (소프트 → 하드)
1. 사용자가 "삭제" 클릭 → `isArchived = true` (소프트)
2. 5초 [되돌리기] Snackbar 노출
3. 미복구 → finalize: hard delete + 아이템 CASCADE

---

## 8. 아키텍처 / 폴더 구조

```
app/src/main/java/com/rldjrgo/grocerynote/
├── App.kt                       @HiltAndroidApp
├── MainActivity.kt              @AndroidEntryPoint
├── data/
│   ├── local/                   Room: entities, DAOs, AppDatabase, converters, callback, SettingsDataStore
│   ├── billing/                 BillingRepository (v8)
│   └── repository/              StoreRepository, ItemRepository (Entity↔Domain 매핑)
├── domain/
│   └── model/                   Store, Item 도메인 모델
├── ui/
│   ├── theme/                   Color, Typography, Theme, Dimens
│   ├── components/              PageTitle, BottomNavBar, AdBanner, SwipeNav, UndoSnackbar,
│   │                            WidgetSizePickerSheet, WidgetStoreSelectionSheet
│   ├── screens/
│   │   ├── onboarding/          OnboardingScreen (4페이지, 위젯 추가가 마지막)
│   │   ├── home/                HomeScreen, HomeViewModel + components/(StoreTabBar, ItemRow, ItemList, AddItemSheet, AddStoreSheet)
│   │   ├── completed/           CompletedScreen + ViewModel
│   │   ├── settings/            SettingsScreen + ViewModel
│   │   └── store/               StoreManageScreen + ViewModel
│   └── navigation/              AppNavigation, Routes, DeepLinkBus
├── widget/
│   ├── BaseGroceryWidget.kt     SizeMode.Responsive base
│   ├── WidgetSizes.kt           5개 DpSize breakpoint
│   ├── GroceryWidget{2x1,Small,Long,Medium,Large}.kt  5개 구체 클래스
│   ├── WidgetPinSuccessReceiver.kt  핀 성공 → 홈화면 라우팅
│   ├── WidgetColors.kt          Light/Dark ColorProvider
│   ├── common/                  WidgetCommon.kt (widgetDataFlow, AdaptiveContent), WidgetLayoutCalculator
│   ├── components/              WidgetStoreHeader, WidgetStoreCountRow, WidgetItemRow, WidgetEmptyState
│   └── actions/                 OpenStoreAction (탭 → 앱 열기, 마트/항목 딥링크)
├── di/                          DatabaseModule, CoroutineModule, WidgetEntryPoint
└── util/                        WidgetUpdater, WidgetPinHelper, Analytics
```

### 8.1 앱 ↔ 위젯 동기화

- **앱 → 위젯**: 모든 ViewModel 변경(add/complete/move/delete/rename/reorder/store) 후 `widgetUpdater.updateAll()` 호출
- `WidgetUpdater` (`util/WidgetUpdater.kt`, `@Singleton`):
  - `App.onCreate`에서 `start()` 1회 호출
  - items + stores Flow를 프로세스 수명 동안 구독 → 변경 시 trigger → **120ms 디바운스**로 버스트 합침
  - 5개 위젯 클래스 각각 `GlanceAppWidget.updateAll(context)` 호출
  - 위젯 인스턴스가 1개라도 배치돼 있으면 `settings.hasAddedWidget = true` 기록
- **위젯 → 앱**: 위젯이 `widgetDataFlow(context)` (stores + allItems + largeWidgetStoreIds 결합)를 `collectAsState`로 직접 구독 → DB 변경 시 자동 재구성
- **이중 안전장치**: 위젯 Flow 자체 구독 + `updateAll` 디바운스 wake 경로

### 8.2 Hilt 패턴 — Glance용 EntryPoint

Glance 위젯은 `@AndroidEntryPoint`를 못 씀. `EntryPointAccessors.fromApplication(...)` + `@EntryPoint @InstallIn(SingletonComponent::class)` 인터페이스로 의존성 끌어옴.

```kotlin
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun storeRepository(): StoreRepository
    fun itemRepository(): ItemRepository
    fun settingsDataStore(): SettingsDataStore
    fun widgetUpdater(): WidgetUpdater
}
```

---

## 9. 화면 — 상세 (현재 코드 기준, 2026-05-25)

### 9.1 온보딩 (`ui/screens/onboarding/OnboardingScreen.kt`)

**4페이지** `HorizontalPager` (2026-05-19 `e1e57c4` `f652afd`에서 3→4페이지 + 위젯 페이지를 **마지막으로** 이동):

| # | 제목 | 부제 | 일러스트 |
|---|---|---|---|
| 1 | 마트별로 따로 정리하세요 | 쿠팡, 다이소 따로따로, 어디 가서도 헷갈리지 않게 | `onboarding_page1` |
| 2 | 어두운 화면도 자연스럽게 | 설정에서 다크 모드, 밤에도 눈이 편해요 | `onboarding_page4` (다크모드 실기기 캡처) |
| 3 | 이름만 적으면 끝 | 가격도 수량도 필요 없어요. 빠르고 심플하게 | `onboarding_page3` |
| 4 | 위젯으로 한눈에 확인 | 마트 가기 전에 홈화면에서 바로 확인, 탭하면 그 마트 리스트로 이어져요 | `onboarding_page2` |

- 우상단 "건너뛰기", 하단 "다음"/"시작하기", 인디케이터 점
- **마지막 페이지**(위젯)에서 "지금 위젯 추가하기" CTA → `WidgetSizePickerSheet` → `requestPinAppWidget` → `WidgetPinSuccessReceiver`가 홈으로 라우팅
- 완료 시 `settings.setOnboardingSeen()` → HOME으로 `popUpTo(ONBOARDING, inclusive)`
- **설정의 "인트로 미리보기" 메뉴는 2026-05-21 자로 숨김 처리됨** (워킹트리 수정. `SettingsScreen.kt`에 주석으로 남아 다시 노출 가능)

### 9.2 홈 / 구매예정 (`ui/screens/home/HomeScreen.kt` + `HomeViewModel.kt`)

**상단**: `PageTitle("구매예정")` (공유 컴포넌트). 위젯 미추가 & 배너 미닫음이면 파란 안내 배너 (위젯 추가 CTA + 닫기).

**탭바** (`StoreTabBar`): 가로 스크롤 알약 탭. 선택=마트색 채움, 비선택=흰 카드+헤어라인. 각 탭에 미완료 개수 배지. 끝에 점선 "+ 추가"(마트 추가 시트), 원형 "⋮"(마트 관리 화면). 신규 추가 마트는 파란 테두리 펄스 + 자동 스크롤.

**본문**: 선택 마트의 활성 항목 리스트. 마트 전환 시 좌우 슬라이드 `AnimatedContent` + 화면 가로 스와이프로 인접 마트 이동 (`swipeBetweenTabs`).

**항목 행** (`ItemRow`): 이름 + 우측 "완료" 버튼(56×32) + ⋮ 메뉴(이름 수정/다른 마트로 이동/삭제). **"완료" 버튼 탭** → 햅틱 + §4 V2 1초 애니메이션 → 1000ms 시점에 행 제거 + [되돌리기] Snackbar.

**빈 상태**: 마트 0개 → `EmptyStores`(마트 추가 유도) / 항목 0개 → `EmptyItems`(추가 버튼)

**FAB**: 우하단 확장형(현재 마트색) "추가" → `AddItemSheet`

**하단**: `AdBanner` (높이 예약 → 광고 도착 시 점프 없음)

**다이얼로그**: 이름 수정, 다른 마트로 이동, 삭제 확인 (`ConfirmDialog` — 공용, 설정·마트관리에서도 재사용)

**ViewModel 동작**: store/selectedStore/activeItems/recentNames/highlight/counts/banner Flow를 `combine`. 전체삭제 후 stale storeId 방어 (FK 실패로 항목이 조용히 안 저장되는 버그 방지). 모든 변경 후 `widgetUpdater.updateAll()`. "자주 사는 항목"은 **마트별**.

### 9.3 항목 추가 시트 (`ui/screens/home/components/AddItemSheet.kt`)

- Material3 ModalBottomSheet **아님** — 커스텀 `Dialog`. 풀스크린 윈도우 + `SOFT_INPUT_ADJUST_RESIZE` + `imePadding()` → 키보드와 함께 패널이 **단일 모션**으로 상승
- dim 0.45, 바깥 탭 dismiss (`dismissOnClickOutside=false`로 자체 처리)
- 진입 시 자동 포커스 + 키보드. 입력 후 추가 시 **연속 추가 모드** (필드 비우고 "✓ '{이름}' 추가됨 — 계속 입력하세요" 1.5초 토스트)
- 자동완성: 입력 접두사로 최근 이름 최대 5개
- "최근 등록 상품" 칩 최대 10개 (탭=추가, X=마트별 최근목록에서 삭제 확인 다이얼로그)
- IME Done 또는 마트색 "추가" 버튼으로 제출
- **시트 안에서 마트 선택 가능** (2026-05-19 Phase 10 추가, `62e5019`): 시트 상단 드롭다운으로 추가할 마트를 시트 안에서 변경

### 9.4 마트 추가/수정 시트 (`ui/screens/home/components/AddStoreSheet.kt`)

- 이름 입력 + 색 8종 + 아이콘/이모지 14종
- 수정 모드는 `title`/`confirmLabel`/`initial*` 파라미터로 동일 시트 재사용 (홈·마트관리 공유)

### 9.5 완료 (`ui/screens/completed/CompletedScreen.kt` + `CompletedViewModel.kt`)

- `PageTitle("완료")` + 필터 칩(전체 + 마트별, 완료 개수 배지). 화면 가로 스와이프로 필터 전환
- 본문: **날짜 버킷 그룹핑** (오늘/어제/이번 주/이번 달/이전). 행: 체크 아이콘 + 줄긋기 이름 + 마트 배지 + ⋮
- **⚠️ 2026-05-19 `8475fc4`에서 행 스와이프 삭제/되돌리기 제거됨** — 실기기에서 스와이프가 안정적으로 잡히지 않아 ⋮ 메뉴만 남김
- 행 인터랙션: ⋮ 메뉴로 "다시 구매예정으로" / "삭제"
- 되돌리기 Snackbar: 재활성/삭제 각각 [되돌리기] (삭제는 스냅샷 복원)
- 하단 요약: "이번 주 N개 / 이번 달 N개" + `AdBanner`
- 빈 상태: "아직 완료한 항목이 없어요"

### 9.6 설정 (`ui/screens/settings/SettingsScreen.kt` + `SettingsViewModel.kt`)

섹션형 LazyColumn:

| 섹션 | 항목 |
|---|---|
| 위젯 | "위젯 추가하기" (→ `WidgetSizePickerSheet`) · "노출순서설정" (서브: 위젯 우선순위 최대 4개 → `WidgetStoreSelectionSheet`) |
| 일반 | "다크 모드" 토글 (44×26 알약, On/Off만 — 내부 enum은 Auto/On/Off지만 UI엔 Auto 없음) |
| 데이터 | "전체 삭제" (2단계 확인 → `db.clearAllTables()` → 쿠팡/다이소 재시드 → 토스트) |
| 결제 | `BuildConfig.SHOW_BILLING == true`일 때 (현재 debug+release 모두 true): "광고 제거 (₩1,900)" / 구매 후 "광고 제거됨" |
| 정보 | "버전 1.0.0" · ~~"인트로 미리보기"~~ (2026-05-21 숨김) · "피드백 보내기" (브리지 시트 → `mailto:mkkim850422@gmail.com`) · "별점 주기" (`market://`) · "친구에게 추천" (공유 인텐트) |

### 9.7 마트 관리 (`ui/screens/store/StoreManageScreen.kt` + `StoreManageViewModel.kt`)

- 상단바: ✕ 닫기 · "마트 관리" · "완료". 하단바 숨김 (`STORE_MANAGE` 라우트)
- 행: 드래그 핸들(≡, 롱프레스 없이 즉시 드래그) + 색 점 + 이모지 + 이름 + "N개 항목" + ⋮(수정/삭제)
- 재정렬: `sh.calvin.reorderable` 로컬 작업본 → 즉시 반영, 드래그 종료 시 `persistOrder` 1회 커밋 (스냅백 방지 머지 로직)
- 수정: `AddStoreSheet` 재사용 (프리필)
- 삭제: 항목 N개 경고 다이얼로그 → 소프트 삭제(archive) → "✓ '{이름}' 삭제됨 [되돌리기]" → 미복구 시 finalize
- 하단 풀폭 "+ 마트 추가하기"

---

## 10. 위젯 — 5종 어댑티브 (가장 중요한 기능)

### 10.1 구조

5개 Glance 위젯 클래스, 모두 `BaseGroceryWidget`(SizeMode.Responsive)를 상속:

```
WidgetSizes.responsiveSet = setOf(
    DpSize(110.dp,  40.dp),  // TWO_BY_ONE  Mini
    DpSize(110.dp, 110.dp),  // SMALL       2x2
    DpSize(110.dp, 250.dp),  // LONG        2x4
    DpSize(250.dp, 110.dp),  // MEDIUM      4x2 ★ 기본
    DpSize(250.dp, 250.dp),  // LARGE       4x4
)
```

| 위젯 | 크기 | 내용 | 피커 설명 |
|---|---|---|---|
| **Mini** (`GroceryWidget2x1`) | 2×1 ≈110×40 | `SmallContent(compact=true)` — 마트 개수 행 **정확히 2개** ("외 N" 없음) | "2개 마트 표기" |
| **Small** (`GroceryWidgetSmall`) | 2×2 ≈110×110 | 마트별 잔여 개수 행 (`WidgetStoreCountRow`) ≤4 + "외 N개 마트" | "3개 마트 표기" |
| **Long** (`GroceryWidgetLong`) | 2×4 ≈110×250 | **1마트** 풀높이 항목 리스트 + "+ N개 더" 푸터. 마트 픽: 노출순서설정 1순위 → 미완료 최다 → displayOrder | "1개 마트 항목 표기" |
| **Medium** (`GroceryWidgetMedium`) ★기본 | 4×2 ≈250×110 | 항목 리스트: 1마트=풀폭 / ≥2마트=상위 2개 좌우 분할 | "2개 마트 항목 표기" |
| **Large** (`GroceryWidgetLarge`) | 4×4 ≈250×250 | 항목 리스트 최대 4마트 (1 / 2 / 1+2 / 2×2). 표시 마트는 사용자 지정 (`SettingsDataStore.largeWidgetStoreIds`) | "4개 마트 항목 표기" |

### 10.2 어댑티브 메커니즘 (핵심)

```
사용자가 홈화면에서 위젯 리사이즈
  → 런처가 새 크기 통지 → Glance가 SizeMode.Responsive 평가
  → Glance가 5개 breakpoint 중 "fit"인 것 선택 → provideContent 재실행
  → val size = LocalSize.current
  → val data by widgetDataFlow(ctx).collectAsState(initial = null)
  → AdaptiveContent(size, data) — size 기준 when 분기
  → 선택된 Composable이 Glance 레이아웃 생성 → RemoteViews → 홈화면 즉시 갱신
```

**⚠️ `AdaptiveContent`의 `when` 분기 순서 절대 바꾸지 말 것** (`widget/common/WidgetCommon.kt`):

```kotlin
when {
    size.width >= Medium.width && size.height >= Large.height -> LargeContent(data)
    // ⚠️ Long 검사가 Medium 검사보다 반드시 먼저: 런처가 2x4 위젯의 width를
    //    Medium 이상으로 보고하는 경우가 있어, 순서가 바뀌면 세로가 2분할 됨
    size.height >= Long.height && size.height >= size.width -> LongContent(data)
    size.width  >= Medium.width -> MediumContent(data)
    size.height >= Long.height  -> LongContent(data)
    size.height >= Small.height -> SmallContent(data)
    else -> SmallContent(data, compact = true)   // Mini
}
```

### 10.3 위젯 데이터 흐름

`widgetDataFlow(context)` (`widget/common/WidgetCommon.kt`):
```kotlin
combine(
    storeRepository.observeActiveStores(),
    itemRepository.observeAllItems(),
    settingsDataStore.largeWidgetStoreIds,
) { stores, items, largeIds -> buildWidgetData(...) }
    .catch { emit(WidgetData(emptyList(), emptyMap())) }   // ★ 위젯 프로세스 안 죽게
```

### 10.4 위젯 탭 동작 — 표시 + 딥링크 전용

**위젯은 데이터를 절대 바꾸지 않음.** 모든 탭은 앱을 열고, 어떤 마트가 선택되어 어떤 화면으로 갈지만 결정.

| 탭 위치 | 동작 |
|---|---|
| 항목 행 (read-only 점+이름) | `OpenStoreAction.forStore(storeId = item.storeId)` → 앱 열림, **그 마트 선택**, 항목 하이라이트 |
| 마트 헤더 (이모지+이름+카운트) | `OpenStoreAction.forStore(storeId = store.id)` → 앱 열림, **그 마트 선택** |
| 마트 헤더 우측 "+" | `OpenStoreAction.addToStore(storeId)` → 앱 열림 + **그 마트의 추가 시트 자동 오픈** (`action=ADD_ITEM`, `DeepLinkBus` 경유) |
| 빈 영역 / 빈 상태 | `OpenStoreAction.forStore(storeId = store.id)` (mart 컨텍스트 있으면) 또는 `storeId = -1L`로 HOME 강제 |

**2026-05-25 워킹트리 수정** (`WidgetItemRow.kt`, `WidgetStoreCountRow.kt`, `WidgetStoreHeader.kt`, `WidgetCommon.kt`):
- 이전: 항목 행/마트 행/헤더 클릭 모두 `storeId = -1L` (앱만 열림, 마트 미선택)
- 현재: 각 컴포넌트가 **자신의 `storeId`로 명시적 click 부착** → Glance/RemoteViews가 부모 Column의 click으로 안정적으로 버블링되지 않는 문제 해결, 위젯에서 어디를 탭하든 **그 마트로 정확히 라우팅**

### 10.5 위젯 XML (`res/xml/widget_*_info.xml` × 5)

**모든 5개 공통**:
- `resizeMode="horizontal|vertical"` ← 어댑티브 리사이즈 필수
- `updatePeriodMillis="0"` ← 배터리 (자동 폴링 안 함)
- `widgetCategory="home_screen"`
- `widgetFeatures="reconfigurable"`
- `previewLayout` 사용, **`previewImage` 금지** (One UI에서 vector preview 깨짐)
- `minResizeWidth="110dp" minResizeHeight="40dp"` (Mini까지 줄이기 허용)
- `maxResizeWidth="360dp" maxResizeHeight="360dp"`

**다른 점은 `minWidth/minHeight`(추가 시 자연 크기)와 `targetCellWidth/Height`(초기 셀)뿐**:

| xml | minWidth × minHeight | targetCell |
|---|---|---|
| 2x1 (Mini) | 110×40 | 2×1 |
| small | 110×110 | 2×2 |
| long | 110×250 | 2×4 |
| medium | 250×110 | 4×2 |
| large | 250×250 | 4×4 |

> ⚠️ `minWidth/minHeight`를 줄이면 런처가 추가 시 작게 배치함. 줄이는 건 `minResize`로만.

### 10.6 위젯 핀 (배치) 흐름

1. 사용자가 설정/온보딩/홈 배너에서 위젯 사이즈 선택 → `WidgetSizePickerSheet`
2. `WidgetPinHelper.pinWidget(size)` → `AppWidgetManager.requestPinAppWidget()` 호출 (Android 8+)
3. 핀 미지원 런처(드물게) → 수동 안내 폴백 ("홈 길게 누름 → 위젯 → 마트노트")
4. 핀 성공 시 `WidgetPinSuccessReceiver`가 인텐트 수신 → MainActivity를 HOME으로 라우팅 (사용자가 즉시 배치된 위젯을 볼 수 있게)

### 10.7 위젯 시각

- bg `#FFFFFF` (dark `#1F1F23`), r 16dp
- 마트 헤더 bg = 마트색 × alpha 0.08
- 0.5dp divider (헤더와 본문 사이)
- 행 padding H 12dp / V 8dp; 색 점 아이콘
- 폰트: 시스템 기본 (Glance에서 Pretendard는 까다로움)
- 텍스트: 항목 `#191F28`/dark `#F2F4F6`; 마트명 `#4E5968`/dark `#A8B0BA`

---

## 11. 광고 / 수익화 / 결제

### 11.1 AdMob 배너 (현재)

**구조** (`ui/components/AdBanner.kt`):
- 화면 하단 고정, 적응형 배너 (`getCurrentOrientationAnchoredAdaptiveBannerAdSize`)
- 높이 예약(`Box(height = adSize.height.dp)`) → 광고 도착 시 UI 점프 없음
- **450ms 지연 부착** (`LaunchedEffect(Unit) { delay(450); showAd = true }`) — 진입 애니메이션과 AdView inflate(WebView 기반 ~100ms+ hitch) 충돌 방지
- `BuildConfig.AD_UNIT_BANNER_ID` 사용 → debug/release 자동 분리
- IAP `isAdRemoved` 구독 → 구매한 사용자에겐 컴포넌트 자체가 `return`

### 11.2 debug/release ID 완전 분리 (3계층, `app/build.gradle.kts` 상단 단일 소스)

```kotlin
val admobTestAppId    = "ca-app-pub-3940256099942544~3347511713"   // 구글 공식 TEST — 변경 금지
val admobTestBannerId = "ca-app-pub-3940256099942544/6300978111"   // 구글 공식 TEST — 변경 금지
val admobRealAppId    = "ca-app-pub-3708412629376493~1464671233"   // ★ AdMob 콘솔 실제 ID
val admobRealBannerId = "ca-app-pub-3708412629376493/1854342020"   // ★ AdMob 콘솔 실제 ID
```

- 매니페스트 `<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="${admobAppId}"/>` ← `manifestPlaceholders`로 buildType별 자동 주입
- BuildConfig 필드 (`AD_UNIT_BANNER_ID`, `ADMOB_APP_ID`)도 buildType별 분리
- ProGuard `-assumenosideeffects`로 release에서 `Log.v/d` 호출 + 문자열 빌딩까지 제거 (개인정보 누출 차단)
- **debug에서 실제 광고 ID 노출/클릭 = AdMob 정책 위반 → 계정 정지**. 이 구조가 그걸 구조적으로 차단.

### 11.3 인앱 결제 (BillingClient v8)

- `data/billing/BillingRepository.kt` — `remove_ads` 상품
- `BuildConfig.SHOW_BILLING` 게이트로 설정 화면 노출 제어 (현재 debug/release 모두 `true`)
- ⚠️ **Play Console에 실제 상품 미등록 상태** → 사용자가 누르면 실패. v1.0 출시 전 상품 등록 또는 `SHOW_BILLING=false`로 일시 차단 결정 필요

### 11.4 Firebase

- Analytics + Crashlytics (자동 초기화)
- `google-services.json`은 플레이스홀더 → 출시 전 실제 프로젝트로 교체

---

## 12. 빌드 / 서명 / 배포

### 12.1 빌드 커맨드

```powershell
# 디버그 APK
./gradlew assembleDebug

# 폰 설치
./gradlew installDebug

# 릴리스 (키스토어 없으면 unsigned로 빌드만 됨)
./gradlew bundleRelease     # AAB (Play 업로드용)
./gradlew assembleRelease   # APK

# 스크린샷 (ADB 스크립트)
scripts/capture_screenshots.ps1
```

### 12.2 서명 (조건부)

`app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        val ksFile = (keystoreProps["KEYSTORE_FILE"] as String?)?.let { rootProject.file(it) }
        // .jks 가 디스크에 있고 비번이 있을 때만 서명 → 키스토어 없이도 unsigned 빌드 통과
        if (ksFile != null && ksFile.exists() && (keystoreProps["KEYSTORE_PASSWORD"] as String?).isNullOrBlank().not()) {
            storeFile = ksFile
            storePassword = keystoreProps["KEYSTORE_PASSWORD"] as String?
            keyAlias = keystoreProps["KEY_ALIAS"] as String?
            keyPassword = keystoreProps["KEY_PASSWORD"] as String?
        }
    }
}
```

루트에 `keystore.properties` (gitignored) + `.jks` 배치 시에만 서명. 둘 다 없으면 unsigned release 빌드.

### 12.3 ProGuard (release 하드닝)

`app/proguard-rules.pro`:
- AdMob/Firebase/Billing 클래스 `-keep`
- `Log.v` / `Log.d` `-assumenosideeffects`로 제거 (R8가 호출+문자열 빌딩 까지 삭제)

### 12.4 패키지 / 로케일

- `applicationId`: `com.rldjrgo.grocerynote` (영구)
- `localeFilters`: ko, en (만 포함)
- 자동 백업 룰: `backup_rules.xml`, `data_extraction_rules.xml` 존재

---

## 13. 변경 이력 — CLAUDE.md / 이전 인수인계서 정정

> **이 섹션이 가장 중요.** CLAUDE.md / docs/HANDOVER.md 와 충돌하면 **이 표가 우선**입니다.

| 항목 | CLAUDE.md / HANDOVER.md 서술 | 실제 코드 (2026-05-25) | 변경 커밋 |
|---|---|---|---|
| 위젯 탭 → 즉시 체크 (3대 차별점 #2) | "킬러 기능, `CheckItemAction`(API 31+)" | **삭제됨. 위젯은 표시 + 딥링크 전용** | `f6d8177` (2026-05-18 비즈니스 모델 피벗) |
| 인앱 체크 애니메이션 | "1.3초 (0.15s 채움/0.3s 줄긋기/0.7s 대기/0.3s 페이드)" | **V2: 1초 좌→우 줄긋기 + 초록 ✓** | `f6d8177` |
| 시드 마트 | "이마트/다이소" 또는 "쿠팡/다이소/이마트" 잔재 | **쿠팡(🚀) / 다이소(store)** 2개만 | 2026-05-17 |
| 위젯 종류 | "Small/Medium/Large 3종" → "4종" | **5종 (Mini/Small/Long/Medium/Large)** + 어댑티브 | `f3d2e9b` (2026-05-18) |
| 위젯 추가 후 폰 동작 | (명시 없음) | **핀 성공 시 자동으로 홈화면 라우팅** | `5767e42` (2026-05-18) |
| AddItemSheet 마트 선택 | (명시 없음) | **시트 내부에서 마트 변경 가능** | `62e5019` (2026-05-19 Phase 10) |
| 온보딩 페이지 | "3페이지" | **4페이지, 위젯이 마지막** | `e1e57c4` `f652afd` (2026-05-19) |
| 다크모드 온보딩 | (없음) | **2페이지로 추가, 실기기 캡처 사용** | `e1e57c4` (2026-05-19) |
| 완료 화면 스와이프 | "스와이프 — 오른쪽=재구매예정/왼쪽=삭제" | **스와이프 제거** (실기기 미작동), ⋮ 메뉴만 | `8475fc4` (2026-05-19) |
| 설정 "인트로 미리보기" | "Dialog로 재생 가능" | **메뉴 숨김** (코드 주석으로 보존) | 2026-05-21 (워킹트리) |
| AdMob ID 분리 | (단순 분리 언급) | **3계층 단일 소스 + manifestPlaceholders + ProGuard 로그 제거** | `7f3f51b` (2026-05-19) |
| README targetSdk | "35 / Android 15" | **36 / Android 16** | README가 구버전 |
| Manifest tools:targetApi | "35" | 36과 불일치 (코드 영향 없음) | — |
| 피드백 메일 | 단일 명시 없음 | 앱 내 피드백 = `mkkim850422@gmail.com`, 공식 = `help@sphinfo.co.kr` (의도된 분리) | — |
| 위젯 항목 행 등 클릭 라우팅 | (모두 `storeId = -1L`) | **각 컴포넌트가 자기 `storeId`로 클릭 부착** → 그 마트로 라우팅 | 2026-05-25 (워킹트리 미커밋) |

### 13.1 Phase 진행 상황 (CLAUDE.md §14 갱신)

- [x] Phase 0~10 완료 (CLAUDE.md 참조)
- [x] **Post-Phase 10 폴리싱** (2026-05-18 ~ 2026-05-19, 9개 커밋):
  - `5767e42` 위젯 핀 성공 시 홈 자동 이동
  - `9ee412b` 온보딩 일러스트 + 어댑티브 아이콘 + Play Store 스크린샷 6장
  - `0bd96bf` 인수인계서(위젯 핀 사례) 보존
  - `be71e8f` 디자인 원본 자산 보존
  - `7fb405d` 아이콘 여백 + 스플래시 일치 + 온보딩→메인 전환 매끄럽게
  - `7f3f51b` AdMob ID debug/release 완전 분리 + 릴리스 로그 하드닝
  - `e1e57c4` `f652afd` 다크모드 온보딩 페이지 + 위젯 페이지 마지막으로
  - `8475fc4` 완료 화면 스와이프 제거 (실기기 미작동)
- [ ] **워킹트리 미커밋** (2026-05-25): 위젯 클릭 라우팅 + 인트로 미리보기 메뉴 숨김 → 사용자 검증 후 커밋 예정

---

## 14. 출시 차단 요소 (사용자 측 자산)

CLAUDE.md §15 + 코드 실측. **앱 코드는 완성, 아래는 사용자가 외부에서 준비할 자산**:

| 항목 | 상태 | 출시 영향 |
|---|---|---|
| Pretendard 폰트 4종 → `app/src/main/res/font/` | 미완 | 디자인 스펙상 타이포 (현재 시스템 폰트) |
| Firebase 실제 `google-services.json` → `app/` | 미완 (플레이스홀더) | Analytics/Crashlytics 실데이터 안 모임 |
| AdMob 실제 앱ID/배너 unitID | **`build.gradle.kts`에 실제 ID 들어있음** ★ 확인 필요 | 출시 시 수익, 정책 위반 방지 |
| Play Console 등록 ($25) + 스토어 등록정보 | 미완 (`docs/store_listing_ko.md` 초안 있음) | 출시 불가 |
| 키스토어 (`.jks`) + 비번 → `keystore.properties` | 미완 | **분실 시 영구 업데이트 불가**. 현재 unsigned 빌드만 |
| 스크린샷 | **6장 완료** (`docs/playstore_assets/final/`) | 스토어 등록 가능 |
| 개인정보처리방침 호스팅 | `docs/privacy_policy_{ko,en}.md` 작성됨, GitHub Pages URL 미배포 | 데이터안전 섹션 |
| BillingClient 실제 상품 (`remove_ads`) | Play Console 미등록 | `SHOW_BILLING=true`인데 실패 → 끄거나 등록 결정 |

---

## 15. 다음 작업 후보 (우선순위)

### v1.0 출시 직전 (코드 변경 거의 없음)

1. **워킹트리 5개 파일 커밋 결정**: 위젯 클릭 라우팅 개선 + 인트로 미리보기 숨김 (사용자 검증 후)
2. README의 SDK 표기 (35→36) 정정
3. 온보딩/Play 카피의 "이마트" 잔재 확인 (시드에서 제거됨)
4. `SHOW_BILLING` 정책 결정 (Play Console 상품 등록 vs 일시 OFF)
5. 출시 자산 준비 (§14)
6. 서명 release AAB → Play 비공개 테스트 → 출시

### v1.0.x 핫픽스 후보

- 위젯/앱 동기화 누락 (저사양 폰) 모니터링
- Crashlytics 첫 크래시 트라이앵글 운영

### v1.5 (CLAUDE.md "검토" 표기)

- 광고 제거 IAP 정식화 (상품 등록 + QA)
- **회원가입은 옵션으로만** 검토 (강제 X)
- DataStore 미사용 키 정리 (`onboarding_completed_count` 등)

### v2.0 (Hard No 해제 가능 시점)

- 가격/수량/단위/메모, 카테고리/태그, 통계 탭 재검토
- **단, §4 핵심 원칙(이름만/1초/마트분리/위젯우선/로컬/미니멀) 깨지 않는 선에서만**

---

## 16. AI 협업 운영 규칙

### 16.1 사용자와 소통할 때

- **언어**: 한국어 (영어 코드 식별자는 그대로 두되 설명은 한국어)
- **사용자는 코드를 안 읽음**. 보고는 **이 4줄 구조**로:
  1. 변경된 파일 트리 (몇 줄)
  2. 빌드 결과 (성공/실패 + APK 크기)
  3. 폰 설치 상태 (`installDebug` 성공 여부)
  4. 폰에서 확인할 절차 (어느 화면, 어떤 버튼, 무엇이 보여야 함)
- **코드 덤프 금지**. 사용자가 명시적으로 "코드 보여줘" 한 경우만.
- **이모지 사용 금지** (사용자가 요청하지 않는 한)
- 결정 사항은 `CLAUDE.md` 또는 이 문서에 반영. 페이즈 종료 시 `Phase 진행 상황` 갱신.

### 16.2 코드 작업할 때

- 라이브러리 버전은 `gradle/libs.versions.toml`에서만 변경. 변경 시 ref 이름 grep 후 일괄.
- **모든 새 버전은 Maven 메타데이터 XML로 실재 검증** (§6.2). WebSearch는 거짓말함.
- 위젯 작업은 무조건 실기기 검증 (에뮬레이터에서 안 잡히는 이슈 다수: One UI preview, 핀 콜백, 어댑티브 분기, 클릭 라우팅 등)
- **위젯 ↔ 앱 동기화**: 모든 ViewModel 변경 후 `widgetUpdater.updateAll()` 호출
- **위젯에서 데이터 변경 절대 X** (§2-3 위젯은 표시 + 딥링크 전용)
- 새 화면 추가 시 `PageTitle` 공유 컴포넌트 사용
- 한국어 문자열은 모두 인라인 (현재 strings.xml 미사용. 향후 다국어 시 일괄 추출)

### 16.3 보고 템플릿

```
변경:
  - app/.../FileA.kt (수정)
  - app/.../FileB.kt (신규)

빌드: assembleDebug 성공 (5.91 MB)
설치: installDebug 성공
확인:
  1. 앱 실행 → 구매예정 탭
  2. 쿠팡 탭 → "+" 버튼 → 항목 추가
  3. 추가된 항목 우측 "완료" 탭 → 1초 줄긋기 후 사라짐 + 되돌리기 스낵바
```

### 16.4 충돌 해결 우선순위

1. **이 문서 (HANDOFF_AI협업_종합.md)** — 최신 사실
2. **CLAUDE.md** — 영구 헌장 (북극성, Hard No, 디자인 시스템, 스택 버전)
3. **docs/HANDOVER.md** — 2026-05-18 시점 스냅샷 (이후 변경된 항목 있음)
4. **docs/HANDOFF_위젯자동크기_광고.md** — 다른 앱으로 위젯/광고 메커니즘 이식할 때만
5. **docs/CLAUDE-LESSONS.md** — 과거 사례에서 얻은 교훈
6. **docs/design_handoff.md** — 디자인 시스템 인수인계

---

## 17. AI 협업 시 위험 신호 (즉시 사용자에게 확인)

다음 상황이면 **자율 작업 중단하고 사용자 확인**:

1. **위젯 내 체크 / 데이터 변경 요청** — §2 명시적 피벗(`f6d8177`)에 반함. "위젯 표시 + 딥링크" 원칙 깨지면 비즈니스 모델 깨짐.
2. **가격/수량/메모/카테고리/통계/푸시 등 §3 Hard No 항목 추가 요청** — v2.0 이전엔 거절.
3. **라이브러리 메이저 버전 업** — Glance 1.1.1 → 1.2.0, Hilt 2.59 → 2.6x 등은 검증 안 됨. 사용자 확인 + Maven 메타데이터 + 호환성 매트릭스 체크.
4. **패키지명 / `applicationId` 변경 요청** — Play 출시 후 영구 변경 불가, 사용자 확인 + 위험 명시.
5. **빌드 게이트 우회 요청** (`--no-verify`, ProGuard 비활성, AdMob 테스트 ID release 사용 등) — 정책 위반 또는 운영 사고 위험.
6. **DB 스키마 변경** — version up + 마이그레이션 필수. 사용자 확인 + `app/schemas/N.json` diff.
7. **`CheckItemAction.kt` 같은 파일 복원** — 의도된 삭제(`987a26b`). 복구는 비즈니스 모델 회귀.

---

## 18. 부록 — 자주 쓰는 빌드 명령 & 검증 시나리오

### 18.1 빌드

```powershell
./gradlew assembleDebug         # debug APK
./gradlew installDebug          # 폰 설치
./gradlew bundleRelease         # release AAB (unsigned 가능)
./gradlew clean                 # 클린
```

### 18.2 위젯 검증 (실기기, 5종 모두)

1. 런처 길게 누름 → 위젯 → "마트노트" → 5종 표시 확인
2. 각 종 추가 → 초기 크기가 targetCell 대로 박히는지
3. 드래그 리사이즈 → Mini↔Small↔Long↔Medium↔Large 자동 전환
4. 앱에서 항목 추가/완료 → 위젯 120ms 내 반영
5. 위젯 항목 행 탭 → 앱이 그 마트 선택 + 항목 하이라이트
6. 위젯 마트 헤더 "+" 탭 → 앱이 그 마트 추가 시트로 직행
7. 시스템 다크모드 토글 → 위젯 색 자동 반전

### 18.3 광고 검증

- debug 빌드 → 테스트 광고 표시 (`ca-app-pub-3940256099942544/6300978111`)
- release 빌드 (서명 후) → 실제 광고
- "광고 제거" 구매 후 → 배너 사라짐 (모든 화면)

### 18.4 데이터 검증

- 전체삭제 → 쿠팡/다이소 2개 재시드 확인
- 마트 삭제 → 5초 [되돌리기] → 미복구 시 finalize (항목 CASCADE)
- 항목 완료 → 완료 탭 날짜 버킷에 표시 → 다시 구매예정 가능

---

## 19. 빠른 참조 — AI 에이전트가 자주 묻는 것

| 질문 | 답 / 어디 |
|---|---|
| 어디서 시작? | `MainActivity.kt` → `AppRoot` → `AppNavHost` |
| 위젯 핵심 파일? | `widget/BaseGroceryWidget.kt`, `widget/common/WidgetCommon.kt` (AdaptiveContent + widgetDataFlow), `widget/actions/OpenStoreAction.kt` |
| DB 스키마? | `data/local/*Entity.kt`, version 1, `app/schemas/1.json` |
| 다크 모드 키? | `data/local/SettingsDataStore.kt` `darkMode` (Auto/On/Off enum) |
| 색상 토큰? | `ui/theme/Color.kt` (`AppColors`, light/dark) |
| 위젯에서 앱으로 딥링크? | `widget/actions/OpenStoreAction.kt` (`forStore`, `addToStore`) + `MainActivity.DeepLinkBus` |
| AdMob ID 위치? | `app/build.gradle.kts` 상단 4개 상수 (단일 소스) |
| 라우트 정의? | `ui/navigation/Routes.kt` |
| 빌드/서명 설정? | `app/build.gradle.kts` + `keystore.properties`(gitignored) |
| 위젯 picker UI? | `ui/components/WidgetSizePickerSheet.kt` |
| 위젯 핀 헬퍼? | `util/WidgetPinHelper.kt` + `widget/WidgetPinSuccessReceiver.kt` |
| 앱→위젯 동기화? | `util/WidgetUpdater.kt` (120ms 디바운스, `App.onCreate`에서 `start()`) |

---

## 20. 마지막 한마디 — 이 문서를 받는 AI에게

이 프로젝트는 **1인 인디 개발자 + AI 협업 + 실기기 검증** 사이클로 굴러갑니다. 사용자는:

- 코드를 읽지 않습니다. **결과만** 봅니다.
- 한국어로만 대화합니다.
- "AI가 자신감 있게 거짓말한 경험"을 여러 번 했습니다. 모르면 모른다고, 검증 못 했으면 못 했다고 명시해 주세요.
- "근데 이렇게 하면 안 될까요?" 같은 **제안**은 환영합니다. **무단 변경**은 싫어합니다.
- 출시가 임박했습니다. **새 기능보다는 폴리싱·검증·문서 정합성·출시 자산 처리 우선**입니다.

작업 시작 전에 이 문서 + `CLAUDE.md` + 변경 이력(§13)을 반드시 한 번 훑어주세요. 행운을 빕니다.

— 인수인계 작성: Claude Opus 4.7 (1M context), 2026-05-25
