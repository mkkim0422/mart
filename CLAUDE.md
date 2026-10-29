# CLAUDE.md — 마트노트 (MartNote)

> Permanent project context. **Read this first** at the start of every conversation about this project.

## 1. Identity

| Field | Value |
|---|---|
| App name (KO) | 마트노트 (renamed 2026-05-17 from "장보기 메모") |
| App name (EN) | MartNote |
| Package | `com.rldjrgo.grocerynote` (PERMANENT — set 2026-05-14, cannot change after Play release; package keeps `grocerynote` despite the app rename) |
| Platform | Android only |
| Owner | mkkim0422 (mkkim850422@gmail.com) |
| Repo | https://github.com/mkkim0422/mart |
| Local | `C:\mart` |
| Started | 2026-05-14 |

One-line definition: **마트별 장보기 리스트를 만들고, 홈화면 위젯에서 한눈에 확인한 뒤 앱에서 1초 만에 체크하는 한국형 초심플 앱**

## 2. North Star — 3 Differentiators (do NOT compromise)

1. **Per-mart separated lists** — horizontal scrollable tabs (seed defaults 쿠팡/다이소; user adds 이마트·홈플러스·etc.). **This is the #1 USP.**
2. **At-a-glance home-screen widget** — per-mart lists in 5 sizes (2x1 / 2x2 / 2x4 / 4x2 / 4x4); **freely resizable — every placed widget auto-adapts between all 5 layouts**. **Display + deep-link only.**
3. **Name-only, 1-second add/check** — no price/qty/memo; frictionless.

Widget → app flow: tapping a placed widget opens the app **with that mart preselected**, where the user checks items off via the 1-second completion animation (§11). The widget itself never mutates data. This is deliberate: in-widget checking was **removed on 2026-05-18 (business-model pivot)** so the single banner-ad impression is preserved (revenue) and the widget stays battery-free (`updatePeriodMillis=0`). **Do NOT reintroduce in-widget checking.**

## 3. Hard No (refuse even if user asks until v2.0)

- 가격 / 수량 / 단위 / 메모 입력 — name only, period
- 카톡 Import
- 회원가입 / 로그인 (consider for v1.5 as optional only)
- 카테고리 / 태그
- 통계 탭
- 푸시 알림 남발
- 탭 4개 이상 (max 3 bottom tabs)

## 4. Core Principles (absolute, never violate)

1. **Name only** — adding a name = entire input flow done
2. **1-second add, 1-second check**
3. **Per-mart separation is the spine**
4. **Widget IS the app** — widget UX takes priority over in-app UX
5. No signup, all local
6. Single bottom banner ad (no interstitial, no native, no rewarded) — **2026-08-27: 배너 광고 복구** (`SHOW_ADS=true`); 광고제거 IAP는 Play Console 상품 미등록이라 계속 숨김 (`SHOW_BILLING=false`)
7. Smooth on budget Korean phones — minimize memory/battery/CPU
8. Toss-style minimal — clean and trustworthy

## 5. Audience

Primary: 30~50대 한국 여성 (주부, 워킹맘)
Secondary: 20~40대 1인 가구

## 6. Tech Stack (locked May 2026)

| Layer | Choice | Version |
|---|---|---|
| Language | Kotlin | **2.3.21** |
| AGP | Android Gradle Plugin | **9.2.1** |
| Gradle wrapper | | **9.5.1** |
| KSP | Kotlin Symbol Processing | **2.3.8** |
| UI | Jetpack Compose | BOM **2026.05.00** |
| Compose Compiler | `org.jetbrains.kotlin.plugin.compose` | matches Kotlin |
| Material | Material3 (via BOM) | from BOM |
| Activity | androidx.activity:activity-compose | **1.13.0** |
| Navigation | androidx.navigation:navigation-compose | **2.9.8** |
| Lifecycle | androidx.lifecycle | **2.10.0** |
| Widget | Jetpack Glance | **1.1.1** (1.2.0 still RC) |
| DB | Room | **2.8.4** (Room 3.0 alpha; stay on stable 2.x) |
| DI | Hilt | **2.59.2** (2.59 = first AGP-9-compatible line) |
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

> **Lesson learned (2026-05-14)**: WebSearch hallucinated several version numbers (Hilt 2.57.1, Coroutines 1.11.0, Play-Services-Ads 23.6.0 — none real). For Maven coordinates always query `https://repo1.maven.org/maven2/<group>/<artifact>/maven-metadata.xml` (Maven Central) or `https://dl.google.com/dl/android/maven2/<group>/<artifact>/maven-metadata.xml` (Google Maven) directly — they never lie.

JDK: **21** (Android Studio bundled JBR 21.0.9 in `C:\Program Files\Android\Android Studio\jbr`)
Android SDK: `C:\Users\minkk\AppData\Local\Android\Sdk` (platforms 35, build-tools 35.0.0+)

## 7. SDK Compat

| | Value | Why |
|---|---|---|
| minSdk | **26** (Android 8.0 Oreo, 2017) | covers ~99% of Korean budget phones |
| targetSdk | **36** (Android 16) | required by core/activity ≥1.18/1.13 |
| compileSdk | **36** | matches target |

**Compatibility branching**:
- API 26-30: widget renders, but item-check tap is non-interactive → opens app via deep link to that mart+item (smooth fallback, not just "open app")
- API 31+ (Android 12+): widget item check works via Glance ActionCallback (the killer interaction)
- API 33+: Material You dynamic color is **NOT** used — brand consistency wins

## 8. Performance Budget (budget-phone-first)

- APK ≤ **10MB** (R8 + ProGuard aggressive)
- Avg memory ≤ **50MB**
- Widget: `updatePeriodMillis=0` (no auto-poll), only refresh on user action
- All icons via Vector Drawable; no Lottie / Glide / Coil unless absolutely necessary
- Flows always via `collectAsStateWithLifecycle` (battery)
- Room caching aggressive

## 9. Data Model

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

Seed (RoomCallback on first create) — changed 2026-05-17 (이마트 dropped):
1. 쿠팡 — `#3182F6`, icon `box`
2. 다이소 — `#F04452`, icon `store`
(SettingsViewModel.wipeAllData re-seeds the same two.)

## 10. Design System (Toss-style)

### Colors (Light)
- bg primary `#FFFFFF` / secondary `#F9FAFB` / tertiary `#F2F4F6`
- divider `#E5E8EB`
- text primary `#191F28` / secondary `#4E5968` / tertiary `#8B95A1` / disabled `#C9CDD2`
- brand primary `#3182F6` / hover `#1B64DA` / soft `#E8F3FF`
- success `#2BA471` / danger `#F04452` / warning `#FFA000`

### Colors (Dark)
- bg primary `#17171B` / secondary `#1F1F23` / tertiary `#2B2D31`
- divider `#3A3D42`
- text primary `#F2F4F6` / secondary `#A8B0BA` / tertiary `#6B7684`
- brand primary `#4592FF` / soft `#1A2D4D`

### Mart palette (8) — user picks per mart
| Name | Hex | Suggested |
|---|---|---|
| Blue | `#3182F6` | 쿠팡 |
| Yellow | `#FFB800` | 이마트 |
| Red | `#F04452` | 다이소 |
| Green | `#2BA471` | 홈플러스 |
| Purple | `#8B5CF6` | 마켓컬리 |
| Pink | `#F564A9` | 베이커리 |
| Orange | `#FF8A3D` | 시장 |
| Gray | `#6B7684` | 기타 |

### Typography (Pretendard; user provides font)
| Style | Size | Weight |
|---|---|---|
| Display L | 24sp | Bold |
| Heading L | 20sp | Bold |
| Heading M | 17sp | SemiBold |
| Title | 16sp | Medium |
| Body | 15sp | Regular |
| Body S | 13sp | Regular |
| Caption | 12sp | Regular |
| Micro | 11sp | Medium |

Letter spacing `-0.02em`, line height `1.5`. Only Bold/SemiBold/Medium/Regular weights.

### Spacing
4 / 8 / 12 / 16 / 20 / 24 / 32 / 40 / 48 dp
Screen H padding: 20dp · Card inner: 16~20dp · Component gap: 12~16dp

### Corners
small (chip/badge) 8dp · medium (input/button) 12dp · large (card/modal) 16dp · xlarge (sheet/FAB) 20dp

### Shadows (Toss barely uses them)
- Cards: elevation 0, 0.5dp border instead
- Modal/sheet: elevation 8 (only when needed)
- FAB: elevation 4
- Shadow color `rgba(0,0,0,0.04)`

### Components
- **Primary button** — bg `#3182F6`, white text, h 52dp, r 12dp, weight Medium; tap → 0.95 scale 0.15s
- **Secondary button** — bg `#F2F4F6`, text `#4E5968`, h 52dp
- **Text button** — text `#3182F6`
- **Disabled** — bg `#F2F4F6`, text `#C9CDD2`
- **Input** — border 1dp `#E5E8EB`, r 12dp, h 52dp, padding 16dp; focus border 1.5dp `#3182F6`; placeholder `#8B95A1`
- **Checkbox** — 22dp, r 6dp; unchecked border 1.5dp `#C9CDD2`; checked bg `#3182F6` + white tick
- **Tab bar** — unselected text `#8B95A1`; selected text `#191F28` + 2dp `#3182F6` underline; padding H 16dp, h 48dp
- **FAB** — 56dp circle, bg `#3182F6`, white + 24dp

## 11. UI Structure

Top: shared **`PageTitle`** (`ui/components/PageTitle.kt`) — `headingL` (20sp Bold, -0.02em), textPrimary, left, pad h20/top16/bottom12; params title/subtitle/trailing. All 3 top screens use it (one place to change title sizing).
- Home title: **"구매예정"** (app name lives in launcher/onboarding/Play, not the screen)
- Completed title: **"완료"**  · Settings title: **"설정"**
Tab strip (pill tabs, horizontally scrollable): ●쿠팡(2) ●다이소(3) [+ 추가] [⋮ 마트관리]
Body: items of selected mart (□ name, ⋮ on right)
FAB: Toss-blue + (bottom right)
Bottom nav: 구매예정 / 완료 / 설정 — selected = `#3182F6` for both icon and text (titles match nav labels)

Check anim **V2 (1000ms total, in-app only — `ui/screens/home/components/ItemRow.kt`)**:
- Tap the right-side "완료" button → haptic (LongPress)
- 0–1000ms: a 2dp strikethrough is drawn over the item name, sweeping **left → right** (progress 0→1, `FastOutSlowInEasing`); line color = item text color @ 70% alpha
- 0–250ms: a 28dp **green circle + white ✓** fades + scales (0.6→1.0) into the "완료" slot, then holds until 1000ms. Green = `AppColors.success` (Light `#2BA471` / Dark `#3FBE85`); check mark white
- 1000ms: row removed (data Flow drops it) + `[되돌리기]` Snackbar (Short)
- During play that row's other actions (⋮ menu / rename) are disabled; rows animate independently (fast consecutive completes each finish their own 1s)
- a11y: row contentDescription → "완료됨"; if system `ANIMATOR_DURATION_SCALE == 0`, skip animation and complete instantly
- Constants: `STRIKE_DURATION_MS = 1000`, `CHECK_FADE_IN_MS = 250`

## 12. Widget (most important — invest most time)

**5 Glance widget classes, all adaptive** (each = own class + Receiver + xml info; ALL extend `BaseGroceryWidget` which is `SizeMode.Responsive` over the 5 `WidgetSizes` breakpoints; shared layouts in `widget/common/WidgetCommon.kt` via `AdaptiveContent`). The 5 classes only differ by the xml `targetCell*` (initial pinned size) + picker entry + back-compat; after placement every widget auto-switches between these 5 as the user resizes it:
- **Mini** (2x1, 110×40dp) — `SmallContent(compact)` = **exactly 2** mart count rows (no "외 N" footer)
- **Small** (2x2 ≈ 110×110dp) — per-mart remaining counts (`WidgetStoreCountRow`), ≤4 marts + "외 N개"
- **Long** (2x4 ≈ 110×250dp) — **1 mart** full-height item list + "+ N개 더" footer. Mart pick: 노출순서설정 1순위 → 미완료 최다 → displayOrder
- **Medium** (4x2 ≈ 250×110dp) ★ default — item list: 1 mart full-width / ≥2 top-2 split
- **Large** (4x4 ≈ 250×250dp) — item list: up to 4 marts (1 / 2 / 1+2 / 2x2); marts user-selectable via `SettingsDataStore.largeWidgetStoreIds`

`WidgetSize` enum order = `TWO_BY_ONE, SMALL, LONG, MEDIUM, LARGE` (= picker display order). Picker (`WidgetSizePickerSheet`, used by Home/Settings/Onboarding) lists all 5 with mini-render previews; pick → `requestPinAppWidget` (sets initial size) → app immediately routes to the home screen so the user sees & drags the placed widget into position. Each xml: `minWidth/minHeight` = the widget's NATURAL size (so it pins at the right size — do NOT shrink these or launchers place it tiny), `minResize`=Mini 110×40 + `maxResize`=360dp + `resizeMode=horizontal|vertical` (this is what enables adaptive shrink/grow), `previewLayout` only (no `previewImage` — the static vectors render broken on One UI; Long has its own `widget_preview_long`). Adaptive resize: `BaseGroceryWidget` re-runs `provideContent` on resize, `AdaptiveContent(LocalSize, data)` swaps the layout (no ViewModel/repo change).

**Widget is display + deep-link only — it never mutates data (decided 2026-05-18).**
- Item rows are **read-only** (dot + name). Tap → `OpenStoreAction.forStore(-1L)` → opens MainActivity (HomeScreen, that mart preselected) where the user completes via the §11 V2 animation.
- **No "+" buttons on any widget (removed 2026-10-28, user: "+버튼 그냥 다 빼").** A mart with no items shows "아직 적은 게 없어요" in its area; tapping that empty area → `OpenStoreAction.addToStore(storeId)` (add-item sheet via `DeepLinkBus`, `action=ADD_ITEM`). Long widget empty: "아직 적은 게 없어요 / 누르면 바로 적을 수 있어요" (same action).
- Tapping anywhere else on the card → opens the app.
- **No API branching.** Identical on all supported APIs (26–36). No `CheckItemAction`, no `ActionCallback` (`CheckItemAction.kt` deleted in commit `987a26b`).

`res/xml/grocery_widget_info.xml`:
- `updatePeriodMillis="0"` (battery: only refresh on user action)
- `resizeMode="horizontal|vertical"`
- `widgetCategory="home_screen"`
- API 31+: `targetCellWidth/Height`, `previewLayout`
- `widgetFeatures="reconfigurable"`
- description: "마트별 장보기 리스트"

**Widget dark mode = 폰(시스템) 다크 모드만 따른다 (2026-10-27).** `BaseGroceryWidget`이 `configuration.uiMode`로 in-process 판정해 `LocalWidgetDark`로 내려줌 (Glance day/night ColorProvider는 One UI가 라이트인데 위젯만 다크로 그리는 문제가 있어 안 씀). 앱 안의 설정 "다크 모드" 토글은 **앱 화면에만** 적용되고 위젯엔 영향 없음 — 위젯은 홈 화면 위에 있으니 다른 앱 위젯처럼 홈 화면 테마에 맞춰야 함 (이전엔 앱 설정을 따라가서 라이트 홈 화면에 검은 위젯이 뜨는 문제). 폰 테마가 바뀌면 `App.onConfigurationChanged`가 `WidgetUpdater.updateAll()`로 재렌더(프로세스 살아있을 때), 죽어 있으면 다음 앱 실행/DB 변경 때 따라잡음.

App↔Widget sync: a process-lifetime `WidgetUpdater` (@Singleton, started in `App.onCreate`) subscribes to the items+stores Flows and, debounced 120ms, calls `GlanceAppWidget.updateAll(context)` on all 4 widget classes; every ViewModel mutation also pokes `WidgetUpdater.updateAll()`. Widget→App sync is automatic: widgets subscribe to `widgetDataFlow` via `collectAsState`.

Empty states:
- 0 marts: "마트를 추가해주세요" → tap = open app
- 0 items across all marts: NO placeholder card any more — the normal mart rows/columns render (Small count "—", Medium/Large/Long "아직 적은 게 없어요") (2026-10-28; the old "항목 추가 / 탭해서 추가하세요" card was redundant)

Onboarding last page: "위젯 추가하기" → `AppWidgetManager.requestPinAppWidget()` (Android 8+, depends on launcher); fallback hint: "홈화면을 길게 누르고 위젯 → 마트노트".

**Widget-install nudging (2026-08-27)**: `WidgetPinHelper.anyWidgetPlaced()` = real presence check (`getAppWidgetIds` over the 5 receivers). Home `onResume`마다 `syncWidgetPresence()`가 `hasAddedWidget`을 실제 상태로 동기화 → 유도 배너는 위젯이 실제로 있으면 자동 숨김, 전부 제거되면(닫기 안 눌렀을 때) 복귀. 추가로 **항목 등록 후 추가 흐름이 끝날 때(추가 시트 닫힘/음성추가 종료) `WidgetNudgeSheet`** ("목록을 다 만든 직후"가 유도 타이밍 — 완료 체크는 마트 안이라 부적절, 사용자 피드백 2026-08-27; 0.6s 지연 후): Medium 미리보기 + "위젯 추가하기"→`WidgetSizePickerSheet`. 위젯 미설치면 **15일 간격 재노출**(`widgetNudgeLastShownAt`, 세션당 최대 1회; "평생 1회는 기회가 너무 없다" 사용자 피드백 2026-08-27), 설치돼 있으면 안 뜸. 첫 실행 강제 핀 팝업은 의도적으로 안 함(거부율·런처 편차).

Widget visual (Toss):
- bg `#FFFFFF` (dark `#1F1F23`), r 16dp
- mart header bg = mart color × alpha 0.08
- 0.5dp divider between header and body
- row pad H 12dp, V 8dp; checkbox r 6dp border 1.5dp `#C9CDD2`; checked bg `#3182F6`
- font: system default (Pretendard in Glance is messy)
- text: item `#191F28`/dark `#F2F4F6`; mart name `#4E5968`/dark `#A8B0BA`

Test scenarios (verify each after Phase 4):
1. Add widget from launcher
2. App add → widget reflects instantly
3. Widget item tap → app opens on Home with that mart preselected (check happens in-app)
4. Empty mart area tap → app opens straight into that mart's add-item sheet
5. Resize widget on home screen → auto-adapts between Mini/Small/Long/Medium/Large
6. System dark toggle → widget colors flip
7. 1 day usage → battery impact negligible

## 13. Folder Structure

```
app/src/main/java/com/rldjrgo/grocerynote/
├── App.kt                       @HiltAndroidApp
├── MainActivity.kt              @AndroidEntryPoint
├── data/
│   ├── local/                   Room: entities, DAOs, AppDatabase, converters, callback
│   └── repository/              StoreRepository, ItemRepository (Entity↔Domain mapping)
├── domain/
│   └── model/                   Store, Item domain models
├── ui/
│   ├── theme/                   Color, Typography, Theme, Dimens
│   ├── components/              Reusable: TossButton, TossCheckbox, MartChip, etc.
│   ├── screens/
│   │   ├── home/                HomeScreen, HomeViewModel, AddItemSheet, AddStoreSheet
│   │   ├── completed/           CompletedScreen + ViewModel
│   │   ├── settings/            SettingsScreen + ViewModel
│   │   └── onboarding/          OnboardingScreen
│   └── navigation/              NavGraph, Routes
├── widget/
│   ├── GroceryWidget.kt         GlanceAppWidget
│   ├── WidgetColors.kt          Light/Dark provider
│   └── actions/                 CheckItemAction (API 31+), OpenAppAction (fallback)
├── di/                          Hilt modules: Database, Repository, Widget EntryPoint
└── util/
```

## 14. Phase Plan & Status

- [x] Phase 0 — Decisions confirmed (package=`com.rldjrgo.grocerynote`, repo=`mkkim0422/mart`, dir=`C:\mart`) — 2026-05-14
- [x] Phase 1 — Project bootstrap + Toss design system + git/GitHub + first debug APK — 2026-05-14
- [x] Phase 2 — Data layer (Room + Hilt + DevTest screen, schemas/1.json exported) — 2026-05-14
- [x] Phase 3 — Main UI (tab strip + check anim + AddItem/Store sheets + bottom nav) — 2026-05-14
- [x] Phase 4 — Glance widget (Small/Medium/Large) + day/night — 2026-05-14
  - [removed 2026-05-18 — business-model pivot] in-widget `CheckItemAction` (API 31+) + item-check fallback. Widget is now display + deep-link only.
- [x] Phase 5 — Completed/Settings/Onboarding + DataStore + auto backup + DevTest removed — 2026-05-14
- [x] Phase 6 — AdMob banner + BillingClient (remove_ads) + Firebase Crashlytics/Analytics + signing config + privacy policy + store listing + Release AAB 11.91MB / Release APK 5.91MB — 2026-05-14
- [x] Phase 7 — Rename → 마트노트, 4-size widgets (incl. 2x1 Mini), store-management screen, shared PageTitle, seed = 쿠팡/다이소, **widget-model pivot (display + deep-link only)**, UX polish — 2026-05-18 (commit `987a26b`)
- [x] Phase 8 — Completion animation V2 (1000ms left→right strikethrough + green ✓) + onboarding/store copy realigned + docs↔code reconciled — 2026-05-18
- [x] Phase 9 — New **Long** widget (2x4, 1 mart + items) + **5-type adaptive resize** (`BaseGroceryWidget` SizeMode.Responsive, `AdaptiveContent`) + picker reorder/labels + onboarding copy finalized — 2026-05-18
- [x] Phase 10 — Device-feedback fixes: AddItemSheet **mart-selector dropdown** (pick target mart inside the sheet), Home **banner now visible** (content `weight(1f)`), banner **height reserved** (no pop-in jump), widget xml **minWidth restored** (correct pin size) + previewImage dropped + Long preview, pick-size→**home-screen** placement — 2026-05-18
- [x] Phase 11 — **Per-item one-shot reminders** (v1.0.0→**1.1.0**, vc2): item ⋮ menu "알림 설정/변경/끄기" + date·time `ReminderPickerSheet` + a 🔔 chip under the item name (mart-color); local notification via `AlarmManager.setAndAllowWhileIdle` (inexact — **no** exact-alarm permission, Play/battery safe), `ReminderReceiver` looks up fresh mart+item name at fire time → notif `마트 · 항목` / "이거 살 시간이예요! 🛒", tap → app on that mart with the item highlighted (reuses `DeepLinkBus`+`highlightItemId`); `BootReceiver` re-arms future alarms after reboot (drops past-due); `POST_NOTIFICATIONS` runtime ask (API 33+) at first set; reminder auto-cleared on fire/complete/delete. Room **v1→v2** (`reminder_at` nullable col, `MIGRATION_1_2`). One reminder per item ("초심플"). NOT push-spam — user-initiated, so OK vs §3. — 2026-06-09
- [x] Phase 12 — Post-1.1.0 UX polish (still v1.1.0/vc2): reminder **entry moved to a bell icon on the item row** (outline=unset / filled mart-color=set, one tap → sheet; ⋮ menu reminder items removed) (commit `f2f0aec`); **vibration/sound fix** — channel given `enableVibration(true)`+pattern, channel id `item_reminders`→`item_reminders_v2` (channels are immutable once created) (commit `0deda0e`); `BootReceiver` `LOCKED_BOOT_COMPLETED` removed (Direct Boot can't reach the DB) (commit `0a34ae2`); **tab/filter swipe transition** — settled on **Nate-style horizontal slide** (body slides in the swipe direction, tab/filter strip stays fixed, 280ms slide+fade, bootstrap=fade-only) after trying remove-slide→crossfade→scale-fade (commit `c33ade6`). — 2026-06-16

- [x] Phase 13 — **v1.2.1 (vc7)** 위젯 미표시 근본 원인 수정: 릴리스 R8이 WorkManager `OverwritingInputMerger` 생성자를 제거 → Glance SessionWorker 실패 → 위젯이 항상 미리보기 견본(이마트 5/다이소 3)만 표시(스토어 1.2.0 포함). `proguard-rules.pro` keep 규칙 + 프로세스 시작/앱 이탈 시 자가 복구 렌더. 완료 탭 필터 줄 선택 필 따라가기. 테스트 전용 JSON 내보내기/가져오기는 `SHOW_BACKUP=false`로 숨김 — 2026-09-24
- [x] Phase 14 — **v1.2.2 (vc8)** ① 위젯 다크 모드가 앱 설정 대신 **폰(시스템) 다크 모드만** 따르도록 변경 (§12). 폰 테마 전환 시 `App.onConfigurationChanged` → 위젯 재렌더. ② **온라인 마트 "N에서 찾기"** (`util/StoreSearch.kt`): 마트 이름에 쿠팡/컬리가 들어가면 그 탭의 항목 줄에 🔍 + ⋮ "쿠팡에서 찾기" → 마트 앱 검색 링크(쿠팡 `coupang://search?q=`, 컬리 `kurly://search?keyword=`; 둘 다 실기기에서 검색 결과 직행 확인 2026-10-28) → 웹 검색(**쿠팡 `coupang.com/np/search?q=`·컬리 둘 다** — 실제 브라우저에서 열림 확인 2026-10-28. 스크립트 접속은 쿠팡 봇 차단으로 403이 나와 예전에 "폴백 없음"으로 잘못 판단했었음) → Play 스토어(브라우저도 없을 때만) 순. 오프라인 마트 탭은 변화 없음.
  - **2026-10-28 변경 → "쇼핑몰 앱 바로가기"**: 마트 **이름**으로 앱을 정함(`StoreSearch.appFor`: 쿠팡→쿠팡, 이마트→이마트 앱, 다이소→다이소몰, 홈플러스, 롯데마트GO, 하나로마트, 킴스클럽, 우리동네GS, 포켓CU, SSG, 이마트24/에브리데이/이마트몰, 배민, 올리브영, 11번가, G마켓, 옥션 등 — 패키지는 Play 스토어 KR에서 확인, 더 구체적인 이름이 먼저 매칭). 해당 마트 탭 **목록 맨 위에 "○○ 앱 바로가기" 토글**(마트별, **기본 ON**, DB v3 `stores.shop_app_enabled`). 켜져 있으면 항목 🔍: **항목 검색 결과로 여는 앱** [S24+ 실기기 확인 2026-10-28] = 쿠팡(`coupang://search?q=`), 컬리(`kurly://search?keyword=`), 11번가(`elevenst://loadurl?url=`+검색 URL), 옥션(`auction://openwindow.v2?targeturl=`+검색 URL), 테무(`temu://com.einnovation.temu/search_result.html?search_key=`), G마켓(`gmarket://search?type=page&keyword=` → 검색창에 입력된 상태까지). [APK 분석만, 실기기 미확인] 홈플러스(매니페스트가 `https://mfront.homeplus.co.kr/search`를 받음 → 패키지 지정), SSG(`ssg://execute?landingUrl=`). 검색 링크 Intent에는 **`FLAG_ACTIVITY_CLEAR_TASK`** 필수 — 없으면 이미 켜진 쇼핑 앱이 이전 화면을 그대로 띄움(컬리에서 "우유" 버그). 나머지는 앱 첫 화면으로(검색 링크를 못 찾음), **앱이 없으면 Play 스토어**(웹사이트로 가지 않음). 이름이 어떤 앱과도 안 맞는 마트(시장·약국)는 토글·🔍 없음. 백업 JSON `shopAppEnabled`. ⋮ 메뉴 문구: "쿠팡에서 찾기" / "이마트 앱 열기". 쿠팡 파트너스 링크 전환은 미정 — 2026-10-27
- [x] Phase 14.5 — **v1.3.0 (vc9) 쇼핑몰 앱 바로가기** (마트 이름 → 쇼핑 앱, 목록 위 마트별 토글 기본 ON, 🔍 → 앱 검색/앱 열기/Play 스토어, DB v3) + 온보딩 개편(4장: 마트별·이름만·쇼핑앱·위젯, 마지막 장 두 버튼) + 위젯 "+" 제거·빈 상태 정리 + 홈 FAB 브랜드색 고정. 스토어 자료 `docs/playstore_listing.md` §0~2 — 2026-10-28
- [ ] Phase 15 (다음, 사용자 확정 2026-10-27) — **메뉴 → 재료 자동 제안**: 추가 시트에 요리 이름("된장찌개")을 치면 재료가 칩으로 떠서 탭해 담음. 한국 요리 200~300종 재료표를 앱 내 번들(서버 없음). 완성 시 스토어 첫 스크린샷/홍보 문구를 "메뉴만 치면 장보기 끝"으로. 그 뒤 후보: "다시 살 때 됐어요" 칩(완료 주기, 알림 없음) → 가족 공유 6자리 코드(가입 없음 원칙 재검토 필요)

> Note: an earlier **share** feature also exists (Home share SmallFAB above the add-FAB → `ShareRequestDialog` with a 200-char 부탁 문구, 15 rotating placeholders, footer "마트노트에서 보냄"; 심부름 pivot — no store link) and the **XLarge widget breakpoint** (320×320, up to 6 marts, adaptive-only). Both pre-date Phase 11 in the CHANGELOG (2026-05-26) and ship in v1.1.0.

> Rollback: tag **`rollback-v1.0.0`** (commit 8e692d7) = state right before Phase 11. `git reset --hard rollback-v1.0.0` restores 1.0.0 exactly.

End-of-phase report format:
1. Files created/modified (tree)
2. Build result
3. Phone install status
4. Verification steps for the user (which screen, what to check)
5. Confirmation to proceed

## 15. User-Side Tasks (precise instructions only when needed)

- [x] GitHub repo `mart` created (already exists, public, empty, default `main`)
- [ ] Pretendard font (.ttf x4 weights) drop into `app/src/main/res/font/` — phase 1 will guide
- [ ] Firebase project + `google-services.json` → `app/` — phase 6 will guide
- [ ] AdMob app ID + banner unit ID — phase 6 will guide
- [ ] Google Play Console enrollment ($25) + app listing — phase 6 will guide
- [ ] Keystore password (input + secure store; loss = no more updates ever) — phase 6 will guide
- [ ] Screenshots — phase 6 will provide ADB script

## 16. Operating Notes

- Decisions live here; update this file at the end of each phase.
- Library versions are pinned by `gradle/libs.versions.toml`. Don't change without grep'ing for the ref name.
- User does **not** read code. Reports show: tree of changes + build result + phone install status + verification steps. No code dumps.
- Communicate in Korean.
