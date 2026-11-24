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
2. **At-a-glance home-screen widget** — 2 widgets (2026-11-12): **개수 위젯** (per-mart counts) + **목록 위젯** (one mart or all marts, scrollable, **○ checks in place**). Freely resizable.
3. **Name-only, 1-second add/check** — no price/qty/memo; frictionless.

Widget → app flow: tapping an item name / mart header opens the app **with that mart preselected**; the "+" opens the add-item sheet. **In-widget checking is back (2026-11-12, user decision)** — it was removed 2026-05-18 to force app opens for the banner ad, but competitors (Google Keep's note widget) check from the home screen and a display-only widget was a reason to leave; with few users the extra ad impressions were worth little. Still battery-free: `updatePeriodMillis=0`, the widget only redraws on taps / data changes.

## 3. Hard No (refuse even if user asks until v2.0)

- 가격 / 수량 / 단위 / 메모 입력 — name only, period
- 카톡 Import (대화 내보내기/대화 읽기). ※ 시스템 공유창으로 사용자가 직접 보낸 글을 줄 단위로 항목화하는 것은 허용 — 저장 전 확인 시트를 거침 (사용자 승인 2026-11-12)
- 회원가입 / 로그인 (consider for v1.5 as optional only)
- 카테고리 / 태그
- 통계 탭
- 푸시 알림 남발
- 탭 5개 이상 (max 4 bottom tabs — 2026-11-19 사용자 결정으로 "요리별 재료" 탭이 4번째로 추가됨. 그 전엔 max 3)

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
- Widget ○ check (Glance `ActionCallback`) works on every supported API (26+); ○/✓/+ are vector drawables because Glance `cornerRadius` only rounds on API 31+
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
Body: items of selected mart — row = name (+ 🔔 time chip if a reminder is set) · [🔍 shop-app, only when 쇼핑몰 앱 바로가기 on] · [완료] · **≡ drag handle**. **No ⋮ menu since 2026-11-13** (5 items was too much): **tap the name → `ItemEditSheet` "항목 편집"** = name field · mart chips (move) · 알림 row (→ `ReminderPickerSheet`) · 삭제 (→ confirm) / 저장; "○○에서 찾기" dropped (same as 🔍).
**Reorder: drag the ≡ handle (immediate) or long-press anywhere on the row** (`sh.calvin.reorderable`, same as tab strip) → `items.display_order`, widget follows.
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

**2 widgets (2026-11-12, was 5 sizes)** — both extend `BaseGroceryWidget` (loads data, resolves dark, calls `Content(data)`); layouts in `widget/common/WidgetCommon.kt`. Class names kept from the old set so placed widgets carry over; the old Small/Long/Large receivers were removed (users who had those must re-add — accepted while installs are low).
- **개수 위젯** = `GroceryWidget2x1` (receiver label "개수 위젯", pins at 2x1 — 2x2 looked like 2x4 on One UI tall cells; grows to 2x2+). `SizeMode.Exact` → `CountContent` fits rows to the real height: 2x1 strip = 2 rows, no "+"; otherwise rows above a bottom-right "+". Row tap → that mart in the app.
- **목록 위젯** = `GroceryWidgetMedium` (label "목록 위젯", pins at 4x2, any size ≥2x2). `SizeMode.Single` → `ListContent`: **ONE `LazyColumn`** (scrolls). Shows the mart picked at placement (`WidgetStateKeys.MartId` in the widget's Glance Preferences) or 전체 (`ALL_MARTS`) = every mart stacked in tab order (노출순서설정 marts first), each under its header. **Never two marts side by side and never 2+ LazyColumns in one widget** — multiple collection views made Glance fail to inflate (blank, dead widget, 2026-05). Tried and rejected by the user: 2-mart split (4x2 "가장 불편"), in-widget mart tabs ("과해").

Taps (both widgets):
- **○** (list rows) → `CheckItemAction` (`actionRunCallback`): adds the id to `WidgetStateKeys.Checking` → ✓ + strikethrough for 600ms (widgets can't animate) → clears reminder, `completeItem`, `Analytics.itemCompleted(fromWidget = true)` → the row drops from every widget via the data flow. Undo = 완료 탭.
- **item name / mart header** → `OpenStoreAction.forStore(storeId)` (app on that mart).
- **"+"** (one per widget, bottom-right, `widget_fab`): one-mart list widget → `OpenStoreAction.addToStore(martId)` (add sheet for that mart); 전체 list widget & count widget → **just open the app** (`forStore(-1)`, home) — defaulting to the first mart opened 쿠팡 when the user meant another (user 2026-11-12).
- empty mart "아직 적은 게 없어요" → add sheet for that mart.

Choosing the list widget's mart:
- **In-app picker** (`WidgetSizePickerSheet`, Home/Settings/Onboarding): tap a card = select only (blue border + radio); 목록 위젯 always pins as 전체 — no mart question in-app (user 2026-11-12: mart screen and inline mart chips both unneeded); one mart = long-press widget → 설정; bottom "홈 화면에 추가" → `WidgetPinHelper.pinWidget(LIST, martId)`; the mart rides on the pin callback (`FLAG_MUTABLE` so the launcher can add `EXTRA_APPWIDGET_ID`) → `WidgetPinSuccessReceiver` → `applyWidgetMart`.
- **System picker / long-press → 설정**: `WidgetConfigActivity` (`android:configure`, `widgetFeatures="reconfigurable|configuration_optional"` — Android 12+ may place it as 전체 without asking). Sets RESULT_OK up front so backing out keeps the widget (CANCELED would delete a fresh one).

xml (`grocery_widget_2x1_info` / `grocery_widget_medium_info`): `minWidth/minHeight` = natural pin size (do NOT shrink), `targetCell*` 2x1 / 4x2 (4x3 often hit "이 페이지에는 공간이 없어요"), `updatePeriodMillis="0"`, `resizeMode="horizontal|vertical"`, `previewLayout` only (`widget_preview_2x1`, `widget_preview_medium`; no `previewImage` — renders broken on One UI). R8: `-keep class * extends ActionCallback { <init>(); }` (created by reflection).

**Widget dark mode = 폰(시스템) 다크 모드만 따른다 (2026-10-27).** `BaseGroceryWidget`이 `configuration.uiMode`로 in-process 판정해 `LocalWidgetDark`로 내려줌 (Glance day/night ColorProvider는 One UI가 라이트인데 위젯만 다크로 그리는 문제가 있어 안 씀). 앱 안의 설정 "다크 모드" 토글은 **앱 화면에만** 적용되고 위젯엔 영향 없음 — 위젯은 홈 화면 위에 있으니 다른 앱 위젯처럼 홈 화면 테마에 맞춰야 함 (이전엔 앱 설정을 따라가서 라이트 홈 화면에 검은 위젯이 뜨는 문제). 폰 테마가 바뀌면 `App.onConfigurationChanged`가 `WidgetUpdater.updateAll()`로 재렌더(프로세스 살아있을 때), 죽어 있으면 다음 앱 실행/DB 변경 때 따라잡음.

App↔Widget sync: a process-lifetime `WidgetUpdater` (@Singleton, started in `App.onCreate`) subscribes to the items+stores Flows and, debounced 120ms, calls `GlanceAppWidget.updateAll(context)` on both widget classes; every ViewModel mutation also pokes `WidgetUpdater.updateAll()`. Widget→App sync is automatic: widgets subscribe to `widgetDataFlow` via `collectAsState`.

Empty states:
- 0 marts: "마트를 추가해주세요" → tap = open app
- 0 items: no placeholder card — count rows show "—", list shows "아직 적은 게 없어요" per mart (tap → add sheet)

Onboarding last page: "위젯 추가하기" → `AppWidgetManager.requestPinAppWidget()` (Android 8+, depends on launcher); fallback hint: "홈화면을 길게 누르고 위젯 → 마트노트".

**Widget-install nudging (2026-08-27)**: `WidgetPinHelper.anyWidgetPlaced()` = real presence check (`getAppWidgetIds` over the 2 receivers). Home `onResume`마다 `syncWidgetPresence()`가 `hasAddedWidget`을 실제 상태로 동기화 → 유도 배너는 위젯이 실제로 있으면 자동 숨김, 전부 제거되면(닫기 안 눌렀을 때) 복귀. 추가로 **항목 등록 후 추가 흐름이 끝날 때(추가 시트 닫힘/음성추가 종료) `WidgetNudgeSheet`** ("목록을 다 만든 직후"가 유도 타이밍 — 완료 체크는 마트 안이라 부적절, 사용자 피드백 2026-08-27; 0.6s 지연 후): 목록 위젯 미리보기 + "위젯 추가하기"→`WidgetSizePickerSheet`. 위젯 미설치면 **15일 간격 재노출**(`widgetNudgeLastShownAt`, 세션당 최대 1회; "평생 1회는 기회가 너무 없다" 사용자 피드백 2026-08-27), 설치돼 있으면 안 뜸. 첫 실행 강제 핀 팝업은 의도적으로 안 함(거부율·런처 편차).

Widget visual (Toss):
- bg `#FFFFFF` (dark `#1F1F23`), r 16dp
- mart header bg = mart color × alpha 0.08
- 0.5dp divider between header and body
- list row: ○ 20dp (`widget_check_off`, 1.5dp `#C9CDD2`) in a 36dp touch box; mid-check ✓ green `#2BA471` (`widget_check_on`) + gray strikethrough name; "+" 36dp brand circle (`widget_fab`)
- font: system default (Pretendard in Glance is messy)
- text: item `#191F28`/dark `#F2F4F6`; mart name `#4E5968`/dark `#A8B0BA`

Test scenarios (verify each after Phase 4):
1. Add widget from launcher
2. App add → widget reflects instantly
3. Widget ○ tap → ✓ briefly, then the item leaves every widget and shows in the app's 완료 tab
4. Item name tap → app on that mart; "+" / empty mart → add-item sheet for that mart
5. Resize: 개수 2x1 ↔ 2x2+ (rows fit, "+" only when not 2x1); 목록 any size, always scrolls
8. 목록 위젯 mart: in-app picker choice applied on pin; long-press → 설정 changes it; 전체 stacks marts
6. System dark toggle → widget colors flip
7. 1 day usage → battery impact negligible

## 12.5 iOS app (`ios/`, started 2026-11-13)

- **SwiftUI, iOS 17+**, SwiftData in App Group `group.com.rldjrgo.grocerynote` (app + widget share it). Bundle id `com.rldjrgo.grocerynote` (widget `.widget`). Scheme `martnote://` (`add?s=&c=&e=&i=` received list, `store/<uid>`, `add-item/<uid>`, `open`).
- **No Mac here**: project is **XcodeGen** (`ios/project.yml`; `.xcodeproj` is generated, gitignored). CI `.github/workflows/ios.yml` builds on a GitHub macOS runner (simulator, unsigned) on every push to `ios/**`. Read compile errors from the check-run annotations (public API, xcbeautify `--renderer github-actions`).
- Parity with Android 1.5.0: mart tabs, name-only add (recent chips), 1s strike+✓ complete + 되돌리기, ≡ reorder (List onMove), tap name → 항목 편집, 완료 탭 (sections, swipe 되돌리기/삭제), 설정 (위젯 안내 — iOS can't pin widgets, 마트 관리, 다크 모드, 문의, 데이터 지우기), 알림 (UNUserNotification), 음성추가 (Speech ko-KR), 공유 (부탁 문구 + is.gd link, same base64url page), 받은 목록 (same-name mart / "새로 만들기" / dedupe), 온보딩 4장 (SF Symbols).
- Widgets: 목록 위젯 (`AppIntentConfiguration` + `SelectMartIntent`, medium/large, ○ = `Button(intent: CompleteItemIntent)`, no scroll on iOS → "+N개 더", "+" link) and 개수 위젯 (small/medium). `reloadAllTimelines()` on every change (`ModelContext.commit()`), timeline policy `.never`.
- 쇼핑 앱 바로가기 on iOS = the shop's https search URL (opens the app via universal links if installed, else Safari) — which shops actually jump into their app is **unverified on an iPhone**.
- Cross-process: the widget's ○ (`CompleteItemIntent`) saves in the widget process → sets `externalChange` in App Group defaults → app calls `DataStore.refresh()` (new container) when it becomes active (iOS 17 SwiftData doesn't merge other-process saves). Widget/intent always use `DataStore.freshContext()`. Item delete from 항목 편집 runs after the sheet is gone (deleting a model a sheet still shows crashed).
- Also: 자동완성 (`ProductDictionary.swift`, generated from Android's), swipe list ←→ = next/prev mart, 위젯 안내 배너 (hidden once `WidgetCenter.getCurrentConfigurations` finds one), 별점 (`requestReview`), `PrivacyInfo.xcprivacy` (UserDefaults CA92.1/1C8F.1) in both targets.
- Not yet: AdMob (needs an iOS app id), Firebase, Share Extension (receive text from other apps), Home Screen quick action, widget 노출순서, real-screen onboarding images, signing/TestFlight (needs Apple Developer account), list page iOS button (after App Store id exists).

## 12.5 리텐션 알림(넛지) — 1.7.0 (2026-11-22 사용자 확정, 코드 `notify/`)

서버/FCM 없음. `NudgeScheduler`가 하루 2번(아침 기본 9시·저녁 기본 18시, inexact `setAndAllowWhileIdle`, 요청코드 9000/9001) `NudgeReceiver`를 깨우고, `NudgeEngine.decide(slot, snapshot)`이 **그날 보낼 알림 1개**만 고른다(순수 함수, 안드로이드 의존 없음). 설정·이력은 `SettingsDataStore.nudgePrefs`(`NudgePrefs`).
- **하루 최대 1건.** 예외: 아침에 시즌 알림이 나간 날은 저녁 배송 알림 1건 더(사용자 합의 "날짜형 있는 날만 2건"; 정밀검사 전엔 이 예외가 실제로는 동작하지 않았음 — 2026-11-22 수정).
- 아침: SEASON > FIRST_LIST > STALE_LIST. 저녁: DELIVERY만.
- FIRST_LIST(D-15): 설치(`firstInstallTime`) 20시간 후 항목(완료 포함) 0개면 평생 1번. 탭 → 추가 시트.
- STALE_LIST(A-3): 생성 5일 지난 미완료 항목이 있는 마트, 가장 많은 마트 1개, 마트별 5일 쿨다운. 탭 → 그 마트.
- DELIVERY(A-6): 이름이 `StoreSearch.appFor` 로 컬리/쿠팡에 매핑되고(쇼핑앱 토글 ON) 미완료 ≥1, 마트별 3일 쿨다운. 문구는 "오늘 주문하면 내일 새벽" — **마감 시각(23시 등)은 지역마다 달라 적지 않음**. 탭 → 그 마트.
- SEASON(B-11·12): `SeasonCalendar` 날짜표. 설·추석 2027~2030 양력(**`korean-lunar-calendar`(KASI 기반)로 계산 대조**; 2027 설 = 2/7, **2028 설 = 1/27** — 둘 다 중국 춘절보다 하루 늦음. 처음엔 웹 요약을 믿고 2028을 1/26으로 넣었다가 정밀검사에서 잡음 → 날짜는 반드시 계산으로 검증), 김장 11/15·캠핑 5/1·봄소풍 4/20·가을소풍 10/5(제품 가정). D-7(소풍 D-5)부터 3일 창, 키별 1회. 탭 → 요리별 재료 탭에서 그 요리(`DeepLinkBus.Payload.recipeName` → `RecipesViewModel.openByName`). 표는 2030까지 — 연 1회 연장 필요, 끝나면 조용히 안 나감.
- 제외(사용자 결정): A-8 주간 요약(열어도 할 일 없음), C-14 월급날. 2차: B-9 저녁 메뉴, C-13 행사일(11/11·블프만). 3차: E-20 위치.
- **문구 원칙(사용자 지적 2026-11-22: "쿠팡 목록 1개가 기다리고 있어요"는 어색, "설까지 일주일…"은 안 끌림)**: 사용자가 적은 **항목 이름을 제목 맨 앞에**, "목록 N개가 …" 식 목록 주어 금지, 온라인 마트도 있으니 "갈 때" 금지, 숫자는 실제 값만(지어낸 통계 금지), 할인·행사 문구 없음. 확정 문구 — 첫 목록 "오늘 뭐 사야 하죠? 지금 적어 두세요 / 이름만 적으면 끝, 10초" · 남은 목록 "우유, 아직 안 사셨죠? / 쿠팡에서 살 것 1개 · 5일째 그대로예요" · 배송 마감 "오늘 쿠팡 주문 마감 전이에요 / 우유 외 2개 담아 두셨어요" · 시즌 "설 차례상 재료 19가지, 빠진 거 없는지 확인해요 / 설까지 7일 · 눌러서 마트 목록에 담기"(재료 수는 요리표 main 개수, D-day는 계산).
- 채널 4개(종류별 `nudge_start/list/delivery/season`, 중요도 DEFAULT) → 폰 설정에서 종류별 차단 가능. 앱 설정 "알림" 절 = **2줄**(2026-11-23 사용자 결정 — 종류 3개 토글·시간 2줄은 "과하다"로 뺌): "장보기 알림" 토글 + "알림 시간" 한 줄(→ `NudgeTimeSheet`, 아침 6~11시·저녁 16~21시 칩) + 폰 알림 꺼짐이면 설정 앱으로 가는 줄. 종류별 끄기는 알림 길게 누르기(채널)로만; `NudgePrefs`의 종류별 값·엔진 검사는 남아 있으나 UI 없음(항상 켜짐).
- 권한: **온보딩 마지막 장(위젯 장) 진입 시 한 줄 설명 다이얼로그 → 시스템 팝업**(13+). 거부하면 다시 안 묻고(`permissionAsked`) 설정 줄로 안내. 기존 사용자(온보딩 안 봄)는 권한이 없으면 알림이 조용히 안 나감 — 홈에서 1회 유도는 미구현(검토 항목).
- 재부팅(`BootReceiver`)·프로세스 시작(`App.onCreate`)·설정 변경 시 `ensureArmed()`. 마스터 OFF면 알람 자체 제거.
- Analytics: `nudge_sent{type}`, `nudge_opened{type}`(MainActivity 인텐트 extra), `notif_permission{granted,where}`. 종류별 열림률로 약한 알림을 뺀다.
- 단위 테스트 `app/src/test/.../notify/NudgeEngineTest.kt`(6개: 첫 목록·남은 목록·배송·하루 상한·설 문구·날짜표). **이 PC에서는 `gradlew testDebugUnitTest`가 기존 테스트까지 ClassNotFound로 실패**(테스트 워커 문제, 원인 미확인 — 한글 경로 추정이나 ASCII 드라이브로도 재현) → 컴파일된 클래스를 `java -cp … org.junit.runner.JUnitCore`로 직접 실행해 6개 통과 확인(2026-11-22).
- **알려진 빈틈**: 온보딩에서 "건너뛰기"를 누르면 마지막 장을 안 거쳐 알림 권한을 묻지 않음. 기존 사용자(온보딩 이미 봄)도 마찬가지 → 홈 1회 안내 필요 여부 사용자 결정 대기.
- 수동 시험(**디버그 빌드만**): `adb shell am start -n com.rldjrgo.grocerynote/.MainActivity --es debug_nudge_slot MORNING` (EVENING) → `MainActivity.debugRunNudge` → `NudgeRunner.run`(재예약 없이). adb로 리시버에 직접 브로드캐스트는 Android 14+에서 "not exported"로 차단돼 **안 된다**(2026-11-22 에뮬레이터에서 확인, `dumpsys activity broadcasts history`에 skipped by policy). 하루 1건 상한 때문에 두 번째는 안 뜸 — 날짜를 바꿔야 함: 에뮬레이터는 `settings put global auto_time 0` 후 `cmd alarm set-time <epochMillis>`(auto_time을 안 끄면 몇 초 뒤 원래 시각으로 되돌아감). 로그 태그 `Nudge`.

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
│   └── actions/                 CheckItemAction (○ check, all APIs) + WidgetStateKeys, OpenStoreAction
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
- [x] Phase 7 — Rename → 마트노트, 4-size widgets (incl. 2x1 Mini), store-management screen, shared PageTitle, seed = 쿠팡/다이소, **widget-model pivot (display + deep-link only)**, UX polish — 2026-05-18 (commit `24eb07e`)
- [x] Phase 8 — Completion animation V2 (1000ms left→right strikethrough + green ✓) + onboarding/store copy realigned + docs↔code reconciled — 2026-05-18
- [x] Phase 9 — New **Long** widget (2x4, 1 mart + items) + **5-type adaptive resize** (`BaseGroceryWidget` SizeMode.Responsive, `AdaptiveContent`) + picker reorder/labels + onboarding copy finalized — 2026-05-18
- [x] Phase 10 — Device-feedback fixes: AddItemSheet **mart-selector dropdown** (pick target mart inside the sheet), Home **banner now visible** (content `weight(1f)`), banner **height reserved** (no pop-in jump), widget xml **minWidth restored** (correct pin size) + previewImage dropped + Long preview, pick-size→**home-screen** placement — 2026-05-18
- [x] Phase 11 — **Per-item one-shot reminders** (v1.0.0→**1.1.0**, vc2): item ⋮ menu "알림 설정/변경/끄기" + date·time `ReminderPickerSheet` + a 🔔 chip under the item name (mart-color); local notification via `AlarmManager.setAndAllowWhileIdle` (inexact — **no** exact-alarm permission, Play/battery safe), `ReminderReceiver` looks up fresh mart+item name at fire time → notif `마트 · 항목` / "이거 살 시간이예요! 🛒", tap → app on that mart with the item highlighted (reuses `DeepLinkBus`+`highlightItemId`); `BootReceiver` re-arms future alarms after reboot (drops past-due); `POST_NOTIFICATIONS` runtime ask (API 33+) at first set; reminder auto-cleared on fire/complete/delete. Room **v1→v2** (`reminder_at` nullable col, `MIGRATION_1_2`). One reminder per item ("초심플"). NOT push-spam — user-initiated, so OK vs §3. — 2026-06-09
- [x] Phase 12 — Post-1.1.0 UX polish (still v1.1.0/vc2): reminder **entry moved to a bell icon on the item row** (outline=unset / filled mart-color=set, one tap → sheet; ⋮ menu reminder items removed) (commit `2e675f5`); **vibration/sound fix** — channel given `enableVibration(true)`+pattern, channel id `item_reminders`→`item_reminders_v2` (channels are immutable once created) (commit `722a02a`); `BootReceiver` `LOCKED_BOOT_COMPLETED` removed (Direct Boot can't reach the DB) (commit `bbfc8e1`); **tab/filter swipe transition** — settled on **Nate-style horizontal slide** (body slides in the swipe direction, tab/filter strip stays fixed, 280ms slide+fade, bootstrap=fade-only) after trying remove-slide→crossfade→scale-fade (commit `cc6ffb8`). — 2026-06-16

- [x] Phase 13 — **v1.2.1 (vc7)** 위젯 미표시 근본 원인 수정: 릴리스 R8이 WorkManager `OverwritingInputMerger` 생성자를 제거 → Glance SessionWorker 실패 → 위젯이 항상 미리보기 견본(이마트 5/다이소 3)만 표시(스토어 1.2.0 포함). `proguard-rules.pro` keep 규칙 + 프로세스 시작/앱 이탈 시 자가 복구 렌더. 완료 탭 필터 줄 선택 필 따라가기. 테스트 전용 JSON 내보내기/가져오기는 `SHOW_BACKUP=false`로 숨김 — 2026-09-24
- [x] Phase 14 — **v1.2.2 (vc8)** ① 위젯 다크 모드가 앱 설정 대신 **폰(시스템) 다크 모드만** 따르도록 변경 (§12). 폰 테마 전환 시 `App.onConfigurationChanged` → 위젯 재렌더. ② **온라인 마트 "N에서 찾기"** (`util/StoreSearch.kt`): 마트 이름에 쿠팡/컬리가 들어가면 그 탭의 항목 줄에 🔍 + ⋮ "쿠팡에서 찾기" → 마트 앱 검색 링크(쿠팡 `coupang://search?q=`, 컬리 `kurly://search?keyword=`; 둘 다 실기기에서 검색 결과 직행 확인 2026-10-28) → 웹 검색(**쿠팡 `coupang.com/np/search?q=`·컬리 둘 다** — 실제 브라우저에서 열림 확인 2026-10-28. 스크립트 접속은 쿠팡 봇 차단으로 403이 나와 예전에 "폴백 없음"으로 잘못 판단했었음) → Play 스토어(브라우저도 없을 때만) 순. 오프라인 마트 탭은 변화 없음.
  - **2026-10-28 변경 → "쇼핑몰 앱 바로가기"**: 마트 **이름**으로 앱을 정함(`StoreSearch.appFor`: 쿠팡→쿠팡, 이마트→이마트 앱, 다이소→다이소몰, 홈플러스, 롯데마트GO, 하나로마트, 킴스클럽, 우리동네GS, 포켓CU, SSG, 이마트24/에브리데이/이마트몰, 배민, 올리브영, 11번가, G마켓, 옥션 등 — 패키지는 Play 스토어 KR에서 확인, 더 구체적인 이름이 먼저 매칭). 해당 마트 탭 **목록 맨 위에 "○○ 앱 바로가기" 토글**(마트별, **기본 ON**, DB v3 `stores.shop_app_enabled`). 켜져 있으면 항목 🔍: **항목 검색 결과로 여는 앱** [S24+ 실기기 확인 2026-10-28] = 쿠팡(`coupang://search?q=`), 컬리(`kurly://search?keyword=`), 11번가(`elevenst://loadurl?url=`+검색 URL), 옥션(`auction://openwindow.v2?targeturl=`+검색 URL), 테무(`temu://com.einnovation.temu/search_result.html?search_key=`), G마켓(`gmarket://search?type=page&keyword=` → 검색창에 입력된 상태까지). [APK 분석만, 실기기 미확인] 홈플러스(매니페스트가 `https://mfront.homeplus.co.kr/search`를 받음 → 패키지 지정), SSG(`ssg://execute?landingUrl=`). 검색 링크 Intent 플래그 = **`NEW_TASK | CLEAR_TOP`** (2026-11-21; SINGLE_TOP·CLEAR_TASK 넣지 말 것). 플래그 없이는 이미 켜진 쇼핑 앱이 이전 화면을 그대로 띄움(컬리 "우유" 버그, 2026-10-28). 10-28에 넣었던 `CLEAR_TASK`는 쇼핑 앱을 매번 콜드 스타트시켜 하루 1회 이벤트 팝업이 뜨고, 팝업을 닫으면 앱 홈으로 가며 검색어가 유실됨(사용자 보고 2026-11-21, 여러 앱 공통) → 제거. `CLEAR_TOP|SINGLE_TOP`은 알리가 이전 검색어를 유지해서(결과 화면이 onNewIntent 무시) 탈락, `CLEAR_TOP` 단독은 그 화면을 새 Intent로 다시 만들어 전부 통과. S24+에서 앱이 켜진 채 두 번째 검색어 전환까지 확인한 앱: 쿠팡·컬리·11번가·옥션·테무·홈플러스·알리·G마켓·SSG(9개 전부). 같은 날 링크 교정: G마켓 `gmarket://search?keyword=`(기존 `?type=page&keyword=`는 빈 검색창만 열림), SSG `https://m.ssg.com/applink/redirect.ssg?url=<검색 URL>`(기존 `ssg://execute?landingUrl=`은 홈만 열림). 앱이 완전히 꺼진 상태에서 그날 팝업을 아직 안 봤다면 팝업 뒤 검색어가 지워지는 건 쇼핑 앱 쪽 동작이라 남음. 나머지는 앱 첫 화면으로(검색 링크를 못 찾음), **앱이 없으면 Play 스토어**(웹사이트로 가지 않음). 이름이 어떤 앱과도 안 맞는 마트(시장·약국)는 토글·🔍 없음. 백업 JSON `shopAppEnabled`. ⋮ 메뉴 문구: "쿠팡에서 찾기" / "이마트 앱 열기". 쿠팡 파트너스 링크 전환은 미정 — 2026-10-27
- [x] Phase 14.5 — **v1.3.0 (vc9) 쇼핑몰 앱 바로가기** (마트 이름 → 쇼핑 앱, 목록 위 마트별 토글 기본 ON, 🔍 → 앱 검색/앱 열기/Play 스토어, DB v3) + 온보딩 개편(4장: 마트별·이름만·쇼핑앱·위젯, 마지막 장 두 버튼) + 위젯 "+" 제거·빈 상태 정리 + 홈 FAB 브랜드색 고정. 스토어 자료 `docs/playstore_listing.md` §0~2 — 2026-10-28
- [x] Phase 14.6 — **v1.4.0 (vc10) "카톡 나에게 보내기 대신"** (앱을 만든 이유 = 카톡 나에게 보내기에 적은 장보기가 다른 글에 묻힘 → 유입 메시지·기능을 이 방향으로): ① 여러 줄 공유 → `SharedTextParser` + `SharedItemsSheet`로 줄마다 항목, 마트 골라 한 번에 담기 ② 항목 길게 눌러 순서 바꾸기 ③ 줄의 🔔 버튼 제거 → ⋮ 메뉴 ④ 공유 메시지 끝 Play 설치 링크 ⑤ Analytics `reminder_set`/`shared_items_added`/`items_reordered` ⑥ 스토어 문구 초안 `docs/playstore_listing.md` — 2026-11-12
- [x] Phase 14.7 — **위젯 2종 + 위젯에서 체크** (v1.4.0): 5종 → 개수 위젯·목록 위젯, 목록 위젯은 마트 하나/전체를 골라 놓고(앱 안 선택 또는 `WidgetConfigActivity`) 스크롤 목록 하나 + ○ 체크 + "+" 하나. 시안 `sample/위젯2종_시안.html` — 2026-11-12
- [x] **Phase 14.8 — v1.5.0 (vc12) "링크 눌러 바로 담기"** (2026-11-13; 1.4.0은 출시 안 하고 함께 묶음; **Play 프로덕션에 12 (1.5.0) 등록 2026-11-13** — 먼저 올린 11은 별점 창 추가로 교체, 같은 버전 코드는 재업로드 불가): 공유 메시지에 링크 하나(목록은 `#` 뒤라 서버로 안 감). 받는 사람이 누르면 마트노트 있음 → 앱에서 담기 / 없음 → Play 스토어. 정적 페이지 = `gh-pages` 브랜치 `l/index.html` → `https://mkkim0422.github.io/mart/l/#<base64url(UTF-8 "마트
색hex
이모지
항목
…")>` (짧은 형식; 페이지는 예전 `#s=…&c=…&e=…&i=…`도 읽음). 안드로이드: 도착 즉시 1회 `intent://add?…#Intent;scheme=martnote;package=…;S.browser_fallback_url=<Play>;end` (+ "마트노트에 바로 담기" 버튼) → 앱 `martnote://add` (MainActivity VIEW 필터 → `DeepLinkBus.linkItems` → `SharedItemsSheet` "받은 장보기 리스트 N개", 보낸 마트 이름과 같은 마트 자동 선택; 없으면 "＋ <마트> 새로 만들기" 칩이 먼저 골라져 담기 = 그 마트 생성(링크의 `c`=색·`e`=이모지 그대로) + 항목). 아이폰·PC: 목록만(눌러서 줄 긋기) + "갤럭시에서만" 안내. 공유 메시지 끝 줄 "👉 눌러서 마트노트에 바로 담기: <링크>" (예전 Play 링크 + "갤럭시라면" 문구 대체 — OS 구분을 페이지가 함). 서버·카카오 SDK 없음. 크롬에서 앱 열기·앱 없을 때 Play 스토어 확인(S24+), **카톡 인앱 브라우저는 사용자 확인 필요**. Analytics `list_link_opened`. **gh-pages에는 `privacy.html`(Play 개인정보처리방침 URL)도 있음 — 지우거나 Pages 소스를 바꾸면 심사 거부됨(2026-11-13 실제로 거부).** 공유 시 `util/LinkShortener`가 is.gd → tinyurl로 줄임(가입·키 없음, 실패 시 긴 링크) — 목록이 단축 서비스로 나가는 유일한 경로, 사용자 승인 2026-11-13. 이유: 지금 "공유 → 마트노트로 옮기기"는 공유 메뉴를 알아야 해서 실효성 낮음(스토어 이미지에서도 뺌) — 가족끼리 목록을 주고받는 흐름 + 설치 유입.
- **ASO 메모 (2026-11-13, Play KR 검색 확인)**: "마트노트" 검색 1위(공개 정상), "장보기 메모/장보기 리스트/쇼핑 체크리스트" 상위 20 밖. 다운로드 50+·리뷰 13 → 순위 막힘 원인은 키워드가 아니라 설치·리뷰 수. 경쟁: "쇼핑 목록 - 간단한 노트와 장보기 메모장"(com.komorebi.shoppinglist, 10만+, 같은 위젯 체크 USP), "장보기 메모" 이름 앱 2개. 대응: 앱 안 별점 창(10번째 완료 때 1회, `REVIEW_AT`), 이름은 유지, 설치 늘리기(카페·블로그·소액 광고)는 사용자 몫.
- **ASO 메모 2 (2026-11-19, 검색어 131개를 Play KR 비로그인 검색으로 확인 — 표 `sample/검색어_확인.html`)**: 설치 약 70(2026-09-14 출시). 잡히는 건 이름 단어끼리의 조합 13개뿐("마트 메모"·"마트 체크리스트" 1위, "마트 장보기 메모" 2위 등)이고 전부 유입이 거의 없는 말. **"장보기 메모"는 이름이 그 말로 시작하는 앱 2개(설치 930 · 9.8만)가 1·2위, 3위부터 일반 메모 앱** → 이름 일치로 뚫리는 유일한 큰 검색어(2위 앱이 9년간 9.8만 = 수요 있음). "장보기 리스트/쇼핑 리스트/장보기 목록/장보기"는 이름이 정확히 맞아도 14위(수백만 설치 앱이 위). 요리 쪽("요리 재료" 등)은 만개의레시피가 잡고 있어 이름에 넣지 않음. 경쟁 거의 없는 말(설명 글에만 넣음): 김장 준비물·김장 재료, 캠핑 체크리스트, 차례상 장보기, 생필품 리스트, 장바구니 리스트, 소풍 준비물. **결정(사용자 2026-11-19): 스토어 앱 이름 → "장보기 메모 - 마트노트: 마트별 쇼핑 리스트 위젯"(28자, 1.6.0과 함께 적용; 런처 이름은 "마트노트" 유지).** 판단 기준 = 출시 3주 뒤 "장보기 메모" 3위 안; 5위 밖이면 "장보기 메모 - 마트노트"로 줄여 재시도. 검색 유입은 잘돼도 하루 10명 안팎 — 큰 유입은 여전히 스토어 밖(블로그·쇼츠).
- [x] **Phase 14.9 — v1.5.1 (vc13)** (2026-11-13, Play 제출): 첫 실행 "자주 가는 곳을 골라 주세요"(`MartPickerSheet`, 신규 설치·항목 없을 때만) + 마트 추천 목록 공용화(`ui/components/MartPresets.kt`: 이마트·홈플러스·롯데마트·코스트코·마켓컬리·올리브영·편의점·시장·약국·**📝메모·✏️준비물** — 장보기 외 2개만, 이미 있는 이름 숨김, 이름+색+아이콘 한 번에) + 설정 > 정보 "개인정보처리방침"(Play 정책: 앱 안에도 필요) + 앱 안 별점 창(10번째 완료, `REVIEW_AT`). 1.5.0은 개인정보처리방침 URL 404로 거부 → 1.5.1로 대체.
- **보류 결정 (2026-11-13)**: iOS(`ios/` 코드는 유지, 재개 조건 = 안드로이드 설치 1,000+ 또는 아이폰 요청), 유료 광고. 수익 후보 = 쿠팡 파트너스. 홍보 우선순위·글은 `홍보자료/index.html`.
- **2026-11-17 스토어 그래픽 v6** (`docs/playstore_assets/v6_store/`, 생성기 `sample/스토어그래픽_시안.html`): 대표 그래픽 A2 "장보기 리스트, 홈 화면에서 바로 체크", 상세 6장 순서 = 묻힌 메모 → 위젯 체크 → 마트별 → 이름만 → 쇼핑앱 검색 → 가족 링크. 문구 규칙: 사람이 실제로 하는 말로("동그라미", "링크·사진에 묻혀서" 같은 설명조·AI투 금지), 상세 이미지는 한 장에 폰 하나, 문제 장면은 답을 강조하지 않음.
- **2026-11-17 기능 검토 결론**: 큰 새 기능보다 설치 수가 병목. Phase 15(메뉴→재료)·가족 실시간 공유는 **보류**(데이터 보고 판단: `list_link_opened` 등), 사진 OCR은 뺌. 할 일 후보 = 백업 규칙 버그 의심 수정(DataStore 경로·Room `-wal`, CHANGELOG 2026-11-17) + 중복 이름 표시.
- **원스토어 빌드 (작업 중, 브랜치 `wip/onestore`, 빌드 실패 상태로 중단 2026-11-17)**: play/onestore flavor, 원스토어는 Play 결제 라이브러리 제외·별점은 원스토어로·공유는 Play로. 원스토어 등록은 생활앱 유입이 적어 우선순위 낮음. 이어서 하려면 브랜치에서 빌드 오류부터 확인.
- [ ] **Phase 15 재개 (2026-11-19, 사용자 결정 — 작업 중, 미커밋·미출시, 실기기 미확인)** — **"요리별 재료" 탭** (하단 탭 2번째: 구매예정 / 요리별 재료 / 완료 / 설정; 화면 제목 "뭘 넣더라?"). 장면 = 마트에서 "김치찌개에 뭘 넣더라". 요리 검색·분류 목록 → 요리를 누르면 재료 칩(재료 / 넣어도 좋은 것 / 양념 "집에 없는 것만" / 준비물) → 마트 골라 "○○에 N개 담기". 규칙(사용자 확정): **아무것도 미리 체크하지 않음**(집에 뭐가 있는지 모름; 재료·준비물 칸에만 "전체 선택"), **이미 어느 마트 목록에 있는 이름은 비활성 + "○○에 있음"**(이름 완전 일치만), **양(그램·개수) 없음 — 양은 사용자 몫**, 조리법은 앱에 안 넣고 "만드는 법 보기" → 유튜브 검색(행사 분류 제외), "최근 본 요리" 8개. 데이터 = `app/src/main/assets/recipes.txt` (약 300개, 한 줄 = `이름|이모지|다른 이름·오타|재료|넣어도 좋은 것|양념|준비물`, `## 분류`; 약 60KB, 탭 처음 열 때 1회 로드). 다른 이름 기준 = "그 이름으로 찾은 사람이 이 재료를 보고 맞다고 할 때만"(참치찌개≠고추장 참치찌개, 까르보나라≠투움바처럼 다른 요리는 줄을 나눔; 다른 이름에 든 재료는 '재료' 칸에 있어야 함). 2026-11 인기 레시피(요리당 6개) 대조 + 독립 검수 2회 거침, 그래도 재료 구분은 최종적으로 사람 눈 확인 필요. 코드: `data/recipes/RecipeBook.kt`, `ui/screens/recipes/`, Analytics `recipe_opened`/`recipe_items_added`/`recipe_howto`(2단계 = 앱 안 간단 조리 순서, howto 수치 보고 판단). 웹 버전(`web/jang/`, 양 계산 포함)은 검색 경쟁력 없다고 판단해 보류. 검색이 주 사용 경로라 분류 세분화·분류 필터는 안 함(사용자 2026-11-19); 대신 "자주 찾는 요리" 버튼 줄. **⚠ 출시 전 필수: `recipe_search_miss`가 '없는 요리 검색어'(글자만, 20자)를 Analytics로 보냄 — 사용자가 친 글이 기기 밖으로 나가는 유일한 곳(사용자 결정 2026-11-19). gh-pages `privacy.html`에 문구 추가 + Play Console 데이터 보안 양식에 "앱 활동 > 인앱 검색 기록(분석 목적)" 신고를 먼저 해야 함. 안 하고 올리면 정책 위반.** **보류(사용자 2026-11-19 "킵하자")**: 내 레시피 직접 추가(로그인 없이 기기 저장, 검색 결과 없을 때 진입) · 레시피 공유/제안하기(공개 공유는 사용자 수·신고/차단 의무·품질 문제로 시기상조, 대안 = 메일로 제안 → 운영자가 검수해 기본 표에 추가). 그 밖의 후보: 초성 검색. 아래는 예전 계획:
- [ ] **Phase 16 — v1.7.0 리텐션 알림 1차** (2026-11-22 착수, 사용자 확정 범위: 기반 + D-15·A-3·A-6·B-11·B-12; §12.5). 1.6.1(vc15)은 🔍 검색어 수정만 먼저 배포(커밋 5669550).
- [x] **v1.7.0 (vc16) 준비 목록 + 장보기 알림 (2026-11-24, 빌드·S24+ 확인, Play 미제출)** — 출시 노트 `docs/playstore_listing.md` §0(240자), 자세한 설명 3)번 갱신, 온보딩 3번째 장 문구 "고르기만 하면 살 게 나와요" + 새 캡처(하단 탭 "준비 목록"). 남은 사용자 결정: 온보딩 "건너뛰기"·기존 사용자 알림 권한 안내(§12.5 알려진 빈틈), 새 목록 내용 사람 검수. 스토어 스크린샷 4번째(요리별 재료 캡처)는 예전 탭 이름 그대로.
- [x] **"요리별 재료" → "준비 목록" (2026-11-24 사용자 결정, 1.7.0에 알림과 함께)**: 하단 탭·화면 제목 "준비 목록"(아이콘 Checklist), 부제 "고르면 살 것이 나와요". 화면 = 검색 → **칩 한 줄**(전체 / 🍲요리 / 🎉명절·행사 / ⛺나들이·여행 / 🎒아이·학교 / 🤒아플 때 / 🏠생활 — 7개가 한계, 반려동물은 사용자가 뺌) → 목록. 전체 = 최근 본 것·자주 찾는 것 + 칩마다 4개 + "더 보기"; 요리 칩만 소제목(찌개·국·탕…); 검색은 칩 무관 전체, 결과 카드에 칩 이름. 데이터 `recipes.txt`에 `@그룹|이모지` 줄 추가(파서 `RecipeBook`, `Recipe.group`/`isFood`). 같은 7칸 형식, 요리가 아닌 목록은 칸 이름만 "꼭 필요한 것 / 있으면 좋은 것 / 집에 있는지 확인(없는 것만) / 준비물", "만드는 법 보기"는 요리만. 기존 행사·모임 14개를 명절·행사/나들이·여행/생활로 나누고 새 목록 20개(물놀이·등산·해외여행 가기 전 / 출산 준비물·출산 가방·이유식·어린이집·유치원·초등 입학·새학기 학용품 / 아이 열날 때·감기·몸살·장염·배탈·입원 준비물·상비약 채우기 / 생필품 채우기·대청소·이사 청소·장마·여름·겨울 준비·미세먼지·다이어트 장보기) — **내용은 초안, 사람 검수 필요**. 규칙: '살 것'만(여권 같은 챙길 것 제외), 약은 종류 이름만(용량·복용법 없음), 수능 준비물은 반입 규정이 민감해 뺌. Analytics `recipe_opened`에 `group` 추가. 시안 `sample/준비목록_카테고리_시안.html`.
- [ ] (예전 계획) Phase 15 (보류 2026-11-17 — 원래 사용자 확정 2026-10-27) — **메뉴 → 재료 자동 제안**: 추가 시트에 요리 이름("된장찌개")을 치면 재료가 칩으로 떠서 탭해 담음. 한국 요리 200~300종 재료표를 앱 내 번들(서버 없음). 완성 시 스토어 첫 스크린샷/홍보 문구를 "메뉴만 치면 장보기 끝"으로. 그 뒤 후보: "다시 살 때 됐어요" 칩(완료 주기, 알림 없음) → 가족 공유 6자리 코드(가입 없음 원칙 재검토 필요)

> Note: an earlier **share** feature also exists (Home share SmallFAB above the add-FAB → `ShareRequestDialog` with a 200-char 부탁 문구, 15 rotating placeholders, footer "마트노트에서 보냄" + "👉 눌러서 마트노트에 바로 담기: <list page link>" (1.5.0; the page decides Android → app/Play, iPhone → list only); 심부름 pivot) and the **XLarge widget breakpoint** (320×320, up to 6 marts, adaptive-only). Both pre-date Phase 11 in the CHANGELOG (2026-05-26) and ship in v1.1.0.

> Rollback: tag **`rollback-v1.0.0`** (commit e91918b) = state right before Phase 11. `git reset --hard rollback-v1.0.0` restores 1.0.0 exactly.

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
