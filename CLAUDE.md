# CLAUDE.md — 장보기 메모 (Grocery Note)

> Permanent project context. **Read this first** at the start of every conversation about this project.

## 1. Identity

| Field | Value |
|---|---|
| App name (KO) | 장보기 메모 |
| App name (EN) | Grocery Note |
| Package | `com.rldjrgo.grocerynote` (PERMANENT — set 2026-05-14, cannot change after Play release) |
| Platform | Android only |
| Owner | mkkim0422 (help@sphinfo.co.kr) |
| Repo | https://github.com/mkkim0422/mart |
| Local | `C:\mart` |
| Started | 2026-05-14 |

One-line definition: **마트별 장보기 리스트를 홈화면 위젯에서 바로 체크하는 한국형 초심플 앱**

## 2. North Star — 3 Differentiators (do NOT compromise)

1. **Per-mart separated lists** (이마트, 다이소, 쿠팡 …) — horizontal scrollable tabs
2. **Home screen widget** displays per-mart lists
3. **Widget item tap → instant strikethrough → 1s later disappears → moved to Completed** (the killer feature)

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
6. Single bottom banner ad (no interstitial, no native, no rewarded)
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

Seed (RoomCallback on first create):
1. 이마트 — `#FFB800`, icon `cart`
2. 다이소 — `#FF4D6D`, icon `store`
3. 쿠팡 — `#3182F6`, icon `box`

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

Top: hamburger | "장보기 메모" (Heading M) | search
Tab strip (ScrollableTabRow): ●이마트(5) ●다이소(3) ●쿠팡(2) [+ 추가]
Body: items of selected mart (□ name, ⋮ on right)
FAB: Toss-blue + (bottom right)
Bottom nav: 활성 / 완료 / 설정 — selected = `#3182F6` for both icon and text

Check anim (1.3s total):
- 0.15s checkbox fills `#3182F6`
- 0.3s strikethrough + text → `#8B95A1`
- 0.7s wait
- 0.3s fade out + slide up 8dp
→ moves to Completed

## 12. Widget (most important — invest most time)

3 sizes via `SizeMode.Responsive`:
- **Small** (2x2 ≈ 110×110dp) — single mart, ≤4 items + "외 N개"
- **Medium** (4x2 ≈ 250×110dp) ★ default — 1-2 marts, 3-5 items each
- **Large** (4x4 ≈ 250×250dp) — up to 5 marts, 3-4 items each

Item check (API 31+): `CheckItemAction` (ActionCallback) → `ItemRepository.completeItem(id)` via Hilt EntryPoint → `GroceryWidget().update(...)` → item gone in <1s.

Fallback (API 26-30): tap → `actionStartActivity` with deep-link to mart+item (smooth, not just "open app").

`res/xml/grocery_widget_info.xml`:
- `updatePeriodMillis="0"` (battery: only refresh on user action)
- `resizeMode="horizontal|vertical"`
- `widgetCategory="home_screen"`
- API 31+: `targetCellWidth/Height`, `previewLayout`
- `widgetFeatures="reconfigurable"`
- description: "마트별 장보기 리스트"

Dark mode auto via `GlanceTheme` + `ColorProviders`.

App↔Widget sync: every mutation in HomeViewModel calls `GlanceAppWidgetManager.getGlanceIds(GroceryWidget::class.java).forEach { GroceryWidget().update(ctx, it) }`. Widget→App sync is automatic via Room Flow.

Empty states:
- 0 marts: "마트를 추가해주세요" → tap = open app
- 0 items across all marts: "추가할 항목이 없어요" + "+" → tap = open app

Onboarding last page: "위젯 추가하기" → `AppWidgetManager.requestPinAppWidget()` (Android 8+, depends on launcher); fallback hint: "홈화면을 길게 누르고 위젯 → 장보기 메모".

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
3. (API 31+) Widget check → gone in 1s → in Completed
4. (API 26-30) Widget tap → app opens with mart preselected and item highlighted
5. Resize → Small/Medium/Large auto-switch
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
- [ ] **Phase 3** — Main screen (tab UI, Toss style)
- [ ] Phase 3 — Main screen (tab UI)
- [ ] Phase 4 — Widget (most important)
- [ ] Phase 5 — Completed + Settings + Onboarding + DataStore + Auto Backup
- [ ] Phase 6 — AdMob + Billing + Crashlytics + signing + Play release

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
