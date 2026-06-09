# 인수인계서 — ① 위젯 사이즈 자동 반영 ② AdMob 배너 광고

> **목적**: 마트노트(`C:\mart`)에 구현된 두 기능을 **다른 안드로이드 앱**으로 이식한다.
> **대상**: 다른 Claude Code 인스턴스 (이식 작업 수행자).
> **출처 코드 기준일**: 2026-05-19, 커밋 `8475fc4` 시점. 모든 코드 스니펫은 실제 `C:\mart` 소스에서 검증됨.
>
> 이 문서만 보고 작업 가능하도록 자기완결적으로 작성됨. 두 기능은 서로 독립이므로 PART A / PART B를 따로 적용해도 된다.
>
> **이식 대상 앱의 데이터 모델·화면 구조는 마트노트와 다를 수 있다.** "재사용 가능한 메커니즘"과 "앱마다 교체할 부분"을 명확히 구분해 표기했다. `// [교체]` 주석이 붙은 곳이 그 앱에 맞춰 바꿔야 하는 지점이다.

---

## 전제 (이식 대상 앱 환경 확인 먼저)

이식 전 대상 앱의 다음을 확인하고, 다르면 버전만 대상 앱 기준으로 맞춘다(메커니즘은 동일):

| 항목 | 마트노트 값 | 비고 |
|---|---|---|
| minSdk | 26 | 위젯 어댑티브는 26+ 전부 동작(분기 없음). 광고도 26+ OK |
| Jetpack Compose | BOM `2026.05.00` | 위젯/광고 모두 Compose 사용 |
| Glance | `1.1.1` | **위젯 핵심 의존성** |
| Hilt | `2.59.2` | Glance가 `@AndroidEntryPoint`가 아니라서 EntryPoint 패턴 필요 |
| coroutines | `1.10.2` | `debounce`, `combine`, `collectAsState` |
| play-services-ads | `25.2.0` | **광고 핵심 의존성** |
| Kotlin / AGP | 2.3.21 / 9.2.1 | 대상 앱 그대로 사용 가능 |

대상 앱이 Hilt를 안 쓰면 PART A의 데이터 주입부만 대상 앱 DI(수동 싱글톤, Koin 등)로 바꾸면 된다. **어댑티브 리사이즈 메커니즘 자체는 DI와 무관.**

---

# PART A — 위젯 사이즈 조절 시 사이즈별 자동 반영

## A-0. 한 줄 요약

> Glance `SizeMode.Responsive` + `LocalSize.current` + `when` 분기 하나로, **홈화면에서 위젯을 드래그해 크기를 바꾸면 5개 레이아웃(Mini/Small/Long/Medium/Large)이 자동으로 교체**된다. ViewModel·Repo 재생성 없음. 위젯 클래스는 5개지만 전부 같은 base를 상속하며 동작은 동일하고, 5개로 나눈 이유는 "추가 시 초기 크기 지정" + "기존 배치 위젯 호환"뿐이다.

## A-1. 동작 메커니즘 (반드시 이해할 것)

```
사용자가 홈화면에서 위젯 리사이즈
   ↓
런처가 새 크기 통지 → Glance가 SizeMode.Responsive 평가
   ↓
Glance가 등록된 5개 breakpoint 중 "맞는" 것을 골라 provideContent 재실행
   ↓
val size = LocalSize.current      // 현재 실제 크기(DpSize)
val data by widgetDataFlow(ctx).collectAsState(initial = null)
AdaptiveContent(size, data)       // size 기준 when 분기 → 해당 레이아웃 호출
   ↓
선택된 Composable이 Glance 레이아웃 생성 → RemoteViews → 홈화면 즉시 갱신
```

핵심 3요소:
1. **`SizeMode.Responsive(set of DpSize)`** — base 위젯에 1회 선언.
2. **`LocalSize.current`** — provideContent 안에서 런타임 크기 획득.
3. **`AdaptiveContent` 디스패처** — `when` 분기로 크기→레이아웃 매핑. **분기 순서가 정확성의 핵심**(아래 A-5 주의).

데이터는 `widgetDataFlow(...).collectAsState(...)`로 구독 → 위젯 세션이 살아있는 동안 DB 변경이 자동으로 recompose를 트리거. `WidgetUpdater`는 그걸 깨우는 보조 경로(2차).

## A-2. 옮겨야 할 파일 목록

마트노트 경로(`app/src/main/java/com/rldjrgo/grocerynote/`) 기준. 대상 앱 패키지로 바꿔 복사.

| 파일 | 역할 | 이식 시 |
|---|---|---|
| `widget/WidgetSizes.kt` | 5개 breakpoint 정의 | **거의 그대로** (앱이 원하는 dp로 조정 가능) |
| `widget/BaseGroceryWidget.kt` | `SizeMode.Responsive` base 클래스 | 클래스명만 변경, 구조 그대로 |
| `widget/common/WidgetCommon.kt` 中 `AdaptiveContent` | 크기→레이아웃 디스패처 | 분기 구조 그대로, 호출하는 Content는 [교체] |
| `widget/common/WidgetCommon.kt` 中 `widgetDataFlow`/`WidgetData` | 위젯이 읽는 데이터 | **[교체]** — 대상 앱 데이터로 |
| `widget/GroceryWidget{2x1,Small,Long,Medium,Large}.kt` | 5개 concrete + Receiver | 클래스명만 변경 |
| `res/xml/grocery_widget_*_info.xml` × 5 | AppWidget provider 선언 | 그대로 (description/preview만 교체) |
| `res/layout/widget_preview_*.xml` × 5 | picker 미리보기 | **[교체]** — 대상 앱 디자인 |
| `AndroidManifest.xml` receiver × 5 | 위젯 등록 | 그대로 (name 경로만) |
| `util/WidgetUpdater.kt` | 앱→위젯 디바운스 동기화 | 권장 이식, target 목록만 [교체] |
| `di/WidgetEntryPoint.kt` | Glance용 Hilt EntryPoint | [교체] — 대상 앱 의존성으로 |
| `App.kt`(Application) 中 `widgetUpdater().start()` | 프로세스 시작 시 구독 시작 | 한 줄 추가 |

## A-3. 의존성 추가

`gradle/libs.versions.toml`:
```toml
[versions]
glance = "1.1.1"          # 대상 앱 Compose BOM과 호환 확인. 1.2.0은 아직 RC라 1.1.1 권장
coroutines = "1.10.2"
datastore = "1.2.1"       # (위젯이 DataStore 읽을 경우만)

[libraries]
androidx-glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }
androidx-glance-material3  = { group = "androidx.glance", name = "glance-material3",  version.ref = "glance" }
```
`app/build.gradle.kts`:
```kotlin
implementation(libs.androidx.glance.appwidget)
implementation(libs.androidx.glance.material3)
```

## A-4. 핵심 코드 (검증된 실제 소스)

### (1) `WidgetSizes.kt` — breakpoint 정의 (거의 그대로 사용)

```kotlin
package <대상패키지>.widget

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * SizeMode.Responsive 의 breakpoint. 배치된 모든 위젯이 리사이즈될 때 이 5개 사이로 적응.
 * Glance 는 "맞는 것 중 가장 큰" 항목을 고르므로 이 값들은 정확한 매칭 키다.
 */
object WidgetSizes {
    val TwoByOne = DpSize(110.dp, 40.dp)   // Mini   2x1
    val Small    = DpSize(110.dp, 110.dp)  // Small  2x2
    val Long     = DpSize(110.dp, 250.dp)  // Long   2x4
    val Medium   = DpSize(250.dp, 110.dp)  // Medium 4x2  ★ 기본
    val Large    = DpSize(250.dp, 250.dp)  // Large  4x4

    /** SizeMode.Responsive 에 넘기는 집합 (순서 무관, fit 으로 매칭). */
    val responsiveSet = setOf(TwoByOne, Small, Long, Medium, Large)
}
```
> 대상 앱이 5단계가 필요 없으면 줄여도 된다(예: Small/Medium/Large 3개). 단, `responsiveSet`과 `AdaptiveContent` 분기·xml 개수를 함께 맞출 것.

### (2) `BaseWidget.kt` — Responsive base (구조 그대로)

```kotlin
abstract class BaseAppWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(WidgetSizes.responsiveSet)
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current
            val data by widgetDataFlow(context).collectAsState(initial = null)   // [교체] 데이터 소스
            val d = data
            if (d == null) {
                // 로딩 placeholder (크기에 따라 compact 여부만 판단)
                val mini = size.width < WidgetSizes.Medium.width && size.height <= WidgetSizes.Small.height
                WidgetCard { WidgetEmptyState(title = "", hint = "불러오는 중…", compact = mini) }
            } else {
                AdaptiveContent(size, d)   // ← 크기 기반 레이아웃 분기 (핵심)
            }
        }
    }
}
```

### (3) `AdaptiveContent` — 디스패처 (분기 구조 그대로, Content만 교체)

```kotlin
// 모든 위젯이 이 5개 레이아웃 사이로 리사이즈됨.
// SizeMode.Responsive 가 WidgetSizes 중 하나를 넘겨주면 매칭 레이아웃 선택.
@Composable
fun AdaptiveContent(size: DpSize, data: WidgetData) {
    when {
        size.width >= WidgetSizes.Medium.width && size.height >= WidgetSizes.Large.height -> LargeContent(data)
        // ⚠️ 세로로 길고(2x4) 가로보다 안 넓으면 → Long. 이 검사가 Medium 검사보다
        //    반드시 먼저 와야 한다. 런처/Glance 가 2x4 의 width 를 Medium 이상으로
        //    보고하는 경우가 있어, 순서가 바뀌면 세로 위젯이 2분할(Medium)돼 버린다.
        size.height >= WidgetSizes.Long.height && size.height >= size.width -> LongContent(data)
        size.width  >= WidgetSizes.Medium.width -> MediumContent(data)
        size.height >= WidgetSizes.Long.height  -> LongContent(data)
        size.height >= WidgetSizes.Small.height -> SmallContent(data)
        else -> SmallContent(data, compact = true)   // Mini (2x1)
    }
}
```
> `LargeContent / LongContent / MediumContent / SmallContent`는 **[교체]** — 대상 앱이 각 크기에서 무엇을 보여줄지 직접 구현. 디스패처의 `when` **분기 순서와 조건식은 그대로 유지**(A-5 참조).

### (4) `widgetDataFlow` / `WidgetData` — **[전면 교체]**

마트노트 원본(참고용):
```kotlin
data class WidgetData(
    val stores: List<Store>,
    val itemsByStore: Map<Long, List<Item>>,
    val largeStoreIds: List<Long> = emptyList(),
)

// Glance 위젯은 @AndroidEntryPoint 가 아니므로 EntryPointAccessors 로 의존성을 끌어온다.
fun widgetDataFlow(context: Context): Flow<WidgetData> {
    val entry = EntryPointAccessors.fromApplication(
        context.applicationContext, WidgetEntryPoint::class.java,
    )
    return combine(
        entry.storeRepository().observeActiveStores(),
        entry.itemRepository().observeAllItems(),
        entry.settingsDataStore().largeWidgetStoreIds,
    ) { stores, allItems, largeIds -> buildWidgetData(stores, allItems, largeIds) }
        .catch { emit(WidgetData(emptyList(), emptyMap())) }
}
```
**이식 가이드**: `WidgetData`를 대상 앱이 위젯에 띄울 데이터 형태로 새로 정의하고, `widgetDataFlow`는 대상 앱 Repository/DataStore의 `Flow`를 `combine`해서 반환하도록 다시 쓴다. **패턴(EntryPointAccessors → combine → catch fallback → collectAsState)만 유지**하면 리사이즈 메커니즘은 그대로 동작한다. Hilt 미사용 시 `EntryPointAccessors` 대신 대상 앱의 싱글톤 접근 방식 사용.

### (5) 5개 concrete 클래스 + Receiver (클래스명만 변경)

각 파일은 단 두 줄:
```kotlin
class AppWidgetMedium : BaseAppWidget()
class AppWidgetMediumReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AppWidgetMedium()
}
```
2x1 / Small / Long / Medium / Large 5세트 동일. **차이는 오직 연결되는 xml의 `targetCell*`(추가 시 초기 크기)뿐**, 배치 후 동작은 5개 모두 동일.

### (6) `res/xml/*_info.xml` — 5개 (그대로, description/preview만 교체)

Medium 예시(실제 소스):
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"          <!-- 추가 시 자연스러운 기본 크기 (줄이면 런처가 작게 배치함) -->
    android:minHeight="110dp"
    android:minResizeWidth="110dp"    <!-- 사용자가 줄일 수 있는 최소 = Mini -->
    android:minResizeHeight="40dp"
    android:maxResizeWidth="360dp"    <!-- 늘릴 수 있는 최대 -->
    android:maxResizeHeight="360dp"
    android:targetCellWidth="4"       <!-- 이 클래스의 추가 시 초기 셀 (4x2) -->
    android:targetCellHeight="2"
    android:resizeMode="horizontal|vertical"   <!-- ★ 이게 있어야 어댑티브 리사이즈 가능 -->
    android:widgetCategory="home_screen"
    android:updatePeriodMillis="0"             <!-- 배터리: 자동 폴링 안 함 -->
    android:description="@string/widget_description_medium"  <!-- [교체] -->
    android:initialLayout="@layout/widget_preview_medium"    <!-- [교체] -->
    android:previewLayout="@layout/widget_preview_medium"    <!-- [교체] -->
    android:widgetFeatures="reconfigurable" />
```
5개 파일의 차이는 `minWidth/minHeight`(= 그 클래스의 자연 크기)와 `targetCellWidth/Height`뿐:

| xml | minWidth×minHeight | targetCell |
|---|---|---|
| 2x1 (Mini) | 110×40 | 2×1 |
| small | 110×110 | 2×2 |
| long | 110×250 | 2×4 |
| medium | 250×110 | 4×2 |
| large | 250×250 | 4×4 |

`minResize`(110×40), `maxResize`(360×360), `resizeMode`, `updatePeriodMillis=0`는 **5개 전부 동일**.

> ⚠️ **`previewImage` 쓰지 말 것 — `previewLayout`만.** 정적 vector preview가 삼성 One UI에서 깨져 보이는 이슈로 마트노트에서 제거함. preview 레이아웃은 대상 앱 디자인의 정적 미니 렌더로 새로 만든다.

### (7) `AndroidManifest.xml` — receiver 5개 (경로만 변경)

```xml
<receiver android:name=".widget.AppWidgetMediumReceiver" android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data android:name="android.appwidget.provider"
        android:resource="@xml/widget_medium_info" />
</receiver>
```
5세트 등록. (마트노트는 pinning 성공 알림용 `WidgetPinSuccessReceiver`(exported=false)도 있으나, 추가 후 자동 라우팅이 필요 없으면 생략 가능.)

### (8) `WidgetUpdater.kt` — 앱→위젯 디바운스 동기화 (권장 이식)

`@Singleton`. `App.onCreate`에서 `start()` 1회 호출. DB Flow 구독 → 변경 시 trigger → **120ms debounce**로 연속 변경을 1회 렌더로 합침 → 5개 위젯 클래스에 `updateAll(context)`.

```kotlin
@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val applicationScope: CoroutineScope,
    /* [교체] 대상 앱이 위젯에 반영해야 하는 Repository/DataStore */
) {
    private val renderMutex = Mutex()
    private val started = AtomicBoolean(false)
    private val trigger = MutableSharedFlow<Unit>(
        extraBufferCapacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    fun start() {
        if (!started.compareAndSet(false, true)) return
        applicationScope.launch(Dispatchers.IO) {
            combine(/* [교체] 관찰할 Flow들 */) { a, b -> a to b }
                .drop(1)                       // 최초 emission: 배치 시 이미 렌더됨
                .collect { trigger.tryEmit(Unit) }
        }
        applicationScope.launch(Dispatchers.IO) {
            trigger.debounce(120).collect { renderAll() }   // ★ 핵심: 버스트 합치기
        }
    }

    fun updateAll() { trigger.tryEmit(Unit) }   // ViewModel에서 명시 호출(2차 경로)

    private suspend fun renderAll() = renderMutex.withLock {
        listOf(                                  // [교체] 대상 앱 위젯 5종
            AppWidget2x1(), AppWidgetSmall(), AppWidgetLong(),
            AppWidgetMedium(), AppWidgetLarge(),
        ).forEach { runCatching { it.updateAll(context) } }
    }
}
```
`Application.onCreate()`:
```kotlin
runCatching {
    EntryPointAccessors.fromApplication(this, WidgetEntryPoint::class.java)
        .widgetUpdater().start()
}
// + AdMob 초기화(PART B)도 보통 같은 onCreate 에서
```

## A-5. 이식 시 주의 / 함정 (실패 사례 = 그대로 학습)

1. **`AdaptiveContent`의 `when` 분기 순서를 절대 바꾸지 마라.** 특히 `Long`(세로 2x4) 검사가 `Medium` 검사보다 **위**에 있어야 한다. 런처가 2x4 위젯의 width를 Medium 이상으로 보고하는 경우가 실제로 있어서, 순서가 뒤집히면 세로 위젯이 2분할로 잘못 렌더된다. (마트노트에서 실제로 겪고 고친 버그)
2. **xml `minWidth/minHeight`를 줄이지 마라.** 이건 "추가될 때의 자연 크기"다. 작게 적으면 런처가 위젯을 처음부터 쪼끄맣게 배치한다. 줄이는 건 `minResizeWidth/Height`(110×40)로만.
3. **`resizeMode="horizontal|vertical"` 필수.** 없으면 리사이즈 자체가 불가 → 어댑티브가 의미 없음.
4. **`updatePeriodMillis="0"`.** 자동 폴링 끄고, 데이터 동기화는 `collectAsState` + `WidgetUpdater`로만. (배터리)
5. **`previewImage` 금지, `previewLayout`만.** One UI에서 vector preview 깨짐.
6. **Glance 위젯은 `@AndroidEntryPoint`가 안 된다.** 의존성은 반드시 `EntryPointAccessors.fromApplication(...)` + `@EntryPoint @InstallIn(SingletonComponent::class)` 인터페이스로. (Hilt 미사용 앱이면 대상 앱 싱글톤 접근으로 대체)
7. `widgetDataFlow`는 반드시 `.catch { emit(빈데이터) }`로 마감. 위젯 프로세스에서 예외 던지면 위젯이 "문제 발생"으로 죽는다.
8. `WidgetUpdater`의 DB 구독은 `.drop(1)` — 최초 emission은 배치 직후라 중복 렌더. debounce는 120ms 권장(연속 추가 시 카운트가 앱보다 늦지 않으면서 버스트는 합쳐지는 값).

## A-6. 검증 시나리오 (이식 후 실기기에서)

1. 런처에서 위젯 5종(2x1/Small/Long/Medium/Large) 각각 추가 → 초기 크기 맞게 박히는지
2. 홈화면에서 한 위젯을 드래그 리사이즈 → Mini↔Small↔Long↔Medium↔Large 자동 전환되는지
3. 앱에서 데이터 변경 → 위젯 즉시 반영(120ms 내)
4. 시스템 다크모드 토글 → 위젯 색 반전 (Glance `GlanceTheme`/`ColorProviders` 사용 시)
5. 하루 사용 → 배터리 영향 미미 (updatePeriodMillis=0 확인)

---

# PART B — AdMob 배너 광고 (현재와 동일 형태)

## B-0. 한 줄 요약

> 화면 **하단 고정 배너 1개**. 적응형 배너 사이즈, 높이 선점(레이아웃 점프 방지), 450ms 지연 부착(진입 애니메이션 끊김 방지), IAP 광고 제거 연동. **debug=구글 공식 테스트 ID / release=실제 ID**를 build.gradle.kts 단일 소스에서 강제 분리(정책 위반 구조적 차단).

## B-1. 의존성

`gradle/libs.versions.toml`:
```toml
[versions]
playServicesAds = "25.2.0"
[libraries]
play-services-ads = { group = "com.google.android.gms", name = "play-services-ads", version.ref = "playServicesAds" }
```
`app/build.gradle.kts`: `implementation(libs.play.services.ads)`
> 버전 검증은 항상 Maven 메타데이터로: `https://repo1.maven.org/maven2/com/google/android/gms/play-services-ads/maven-metadata.xml`. (WebSearch는 가짜 버전을 지어냄 — 마트노트에서 실제로 당함)

## B-2. debug/release 광고 ID 완전 분리 (3계층) — 가장 중요

**단일 소스 = `app/build.gradle.kts` 상단 4개 상수.** manifest와 BuildConfig가 모두 여기서 파생되므로 절대 섞일 수 없다.

`app/build.gradle.kts` (실제 소스, 그대로 이식):
```kotlin
// --- AdMob IDs ---------------------------------------------------------------
// debug 는 반드시 구글 공식 TEST id, release 는 실제 id. 절대 섞이면 안 됨:
// debug 빌드에서 실제 광고를 노출/클릭하면 AdMob 정책 위반 → 계정 정지 위험.
val admobTestAppId    = "ca-app-pub-3940256099942544~3347511713"   // 구글 공식 TEST — 변경 금지
val admobTestBannerId = "ca-app-pub-3940256099942544/6300978111"   // 구글 공식 TEST — 변경 금지
val admobRealAppId    = "ca-app-pub-3708412629376493~1464671233"   // [교체] 대상 앱의 실제 AdMob 앱 ID
val admobRealBannerId = "ca-app-pub-3708412629376493/1854342020"   // [교체] 대상 앱의 실제 배너 unit ID

android {
    defaultConfig {
        // 안전 기본값: TEST id. 각 buildType 이 아래서 명시 override 하므로
        // 미설정 buildType 도 자동으로 테스트 광고로 떨어짐.
        manifestPlaceholders["admobAppId"] = admobTestAppId
    }
    buildFeatures { buildConfig = true }   // BuildConfig 필드 쓰려면 필수

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            buildConfigField("String", "AD_UNIT_BANNER_ID", "\"$admobTestBannerId\"")
            buildConfigField("String", "ADMOB_APP_ID",       "\"$admobTestAppId\"")
            manifestPlaceholders["admobAppId"] = admobTestAppId
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "AD_UNIT_BANNER_ID", "\"$admobRealBannerId\"")
            buildConfigField("String", "ADMOB_APP_ID",       "\"$admobRealAppId\"")
            manifestPlaceholders["admobAppId"] = admobRealAppId
        }
    }
}
```

`AndroidManifest.xml` (`<application>` 안):
```xml
<!-- buildType 별 주입 (debug=테스트, release=실제). 하드코딩 금지 -->
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="${admobAppId}" />
```

## B-3. 배너 Composable (현재와 동일 형태, 실제 소스)

마트노트 `ui/components/AdBanner.kt` 원본. **IAP(광고 제거) 연동 포함**. 대상 앱에 빌링이 없으면 아래 "빌링 없는 변형" 사용.

```kotlin
@HiltViewModel
class AdBannerViewModel @Inject constructor(
    settings: SettingsDataStore,                 // [교체] 빌링 없으면 ViewModel 통째 삭제
) : ViewModel() {
    val isAdRemoved = settings.isAdRemoved        // IAP 로 광고 제거했는지
}

/**
 * 하단 배너. 광고는 네트워크로 비동기 로드되므로, 슬롯에 적응형 배너 높이를
 * 미리(예약 Box) 잡아둔다 — 안 그러면 AdView 가 0dp 였다가 광고 도착 시
 * "팝인"하며 UI 를 위로 밀어버린다. 높이 예약으로 레이아웃 안정(점프 없음).
 */
@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    viewModel: AdBannerViewModel = hiltViewModel(),
) {
    val isAdRemoved by viewModel.isAdRemoved.collectAsStateWithLifecycle(initialValue = false)
    if (isAdRemoved) return

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val adWidth = configuration.screenWidthDp
    val adSize = remember(adWidth) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
    }

    // AdView 생성은 메인스레드에서 WebView 기반 GMS 표면을 inflate(~100ms+ hitch).
    // 화면 진입 첫 컴포지션 때 돌면 애니메이션이 끊긴다. 정착 후 부착.
    // 아래 예약-높이 Box 덕에 늦은 부착이 보이지 않음.
    var showAd by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(450); showAd = true }

    Box(
        modifier = modifier.fillMaxWidth().height(adSize.height.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (showAd) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(adSize)
                        adUnitId = BuildConfig.AD_UNIT_BANNER_ID   // ← debug/release 자동 분리
                        loadAd(AdRequest.Builder().build())
                    }
                },
            )
        }
    }
}
```

**빌링 없는 변형** (대상 앱에 IAP 없을 때): `AdBannerViewModel`과 `isAdRemoved` 분기 전체 삭제, `@Composable fun AdBanner(modifier)` 시그니처에서 `viewModel` 파라미터 제거, 본문은 `val context...`부터 그대로.

> AdView 생명주기(onPause/onResume/onDestroy) **수동 처리 불필요**: Compose `AndroidView`가 composition 이탈 시 자동 정리. 누수 없음.

## B-4. 배치 (현재와 동일 형태)

화면 루트 `Column`의 **맨 마지막 자식**으로 `AdBanner()`. 그 위 콘텐츠 영역에 `Modifier.weight(1f)`를 줘서 배너가 화면 하단에 고정되게 한다. 마트노트 `HomeScreen.kt`:
```kotlin
Column(Modifier.fillMaxSize().background(colors.bgPrimary).statusBarsPadding()) {
    PageTitle(title = "구매예정")
    // ...탭/리스트... (리스트 영역에 Modifier.weight(1f))
    AdBanner()                  // ← Column 최하단. 높이는 AdBanner 내부에서 예약
}
```

## B-5. MobileAds 초기화 (Application, 실제 소스)

```kotlin
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // 첫 호출이 네트워크 I/O → 메인스레드 밖에서. 실패해도 앱 안 죽게 runCatching.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { MobileAds.initialize(this@App) {} }
        }
        // (PART A) 위젯 동기화 시작도 보통 여기서
        runCatching {
            EntryPointAccessors.fromApplication(this, WidgetEntryPoint::class.java)
                .widgetUpdater().start()
        }
    }
}
```

## B-6. ProGuard / R8 (release 하드닝, 실제 소스)

`app/proguard-rules.pro`:
```proguard
# --- AdMob ---
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# --- release 에서 verbose/debug 로그 제거 (개인정보가 Log.d 로 새던 것 차단).
#     R8 가 호출 + 문자열 빌딩까지 제거. Log.i/w/e 는 크래시 진단용으로 유지 ---
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
```
(대상 앱이 Firebase/Crashlytics·Billing도 쓰면 마트노트 `proguard-rules.pro`의 해당 `-keep`도 함께 가져갈 것.)

## B-7. 다른 앱 적용 시 "딱 이것만 바꾸면 됨" 체크리스트

| # | 파일 | 바꿀 것 | 비고 |
|---|---|---|---|
| 1 | `app/build.gradle.kts` | `admobRealAppId` | AdMob 콘솔에서 발급한 **그 앱의** 실제 앱 ID |
| 2 | `app/build.gradle.kts` | `admobRealBannerId` | AdMob 콘솔의 실제 배너 unit ID |
| 3 | `admobTestAppId` / `admobTestBannerId` | **변경 금지** | 구글 공식 테스트 ID 그대로 |
| 4 | `AndroidManifest.xml` | — | `${admobAppId}` 그대로 (자동 주입) |
| 5 | `AdBanner.kt` | — | `BuildConfig.AD_UNIT_BANNER_ID` 그대로 |
| 6 | `App.kt` | — | `MobileAds.initialize` 그대로 |

AdMob 콘솔 절차: ① 로그인 → ② 앱 추가(대상 앱 패키지명) → **앱 ID** 획득 → ③ 광고 단위 → 배너 생성 → **배너 unit ID** 획득 → ④ 위 #1·#2에 입력 → ⑤ debug 빌드로 테스트 광고 확인 → ⑥ release 빌드 제출.

> **결과 보장**: debug 빌드는 구조적으로 실제 광고 ID를 서빙할 수 없음(정책 위반·계정 정지 방지). 검증은 `Build → Analyze APK`의 DEX 문자열 검색으로 debug=테스트ID만/release=실제ID만 확인.

## B-8. 주의 / 함정

1. **debug에서 실제 광고 ID로 광고 노출/클릭 = AdMob 계정 정지.** 위 단일소스 분리를 반드시 유지. 광고 ID를 코드/매니페스트에 하드코딩하지 마라.
2. 높이 예약(`Box(height = adSize.height.dp)`) 빼지 마라 — 광고 팝인으로 UI가 점프한다.
3. 450ms 지연 부착 — 화면 진입 애니메이션과 AdView inflate(메인스레드 ~100ms hitch) 충돌 방지. 진입 애니메이션 없는 화면이면 줄여도 되나 0은 비권장.
4. `MobileAds.initialize`는 IO 스레드 + `runCatching`. 메인스레드에서 부르면 첫 진입 버벅임.
5. `getCurrentOrientationAnchoredAdaptiveBannerAdSize` 사용(고정 `AdSize.BANNER` 아님) — 기기 폭에 맞춰 수익/표시 최적.

---

## 부록 — 두 기능 공통 의존 (Hilt EntryPoint 패턴)

Glance 위젯·Compose 배너 모두 Hilt를 쓰면 `@EntryPoint`로 의존성을 끌어온다. 대상 앱이 Hilt면 그대로, 아니면 대상 DI로 대체:
```kotlin
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun storeRepository(): StoreRepository       // [교체] 대상 앱 의존성으로
    fun itemRepository(): ItemRepository
    fun settingsDataStore(): SettingsDataStore
    fun widgetUpdater(): WidgetUpdater
}
```

## 작업 순서 권장

1. **PART B(광고) 먼저** — 변경점이 적고 독립적, 빠른 검증 가능.
2. PART A(위젯) — (a) 의존성 → (b) WidgetSizes/Base/AdaptiveContent 골격 → (c) `widgetDataFlow`를 대상 앱 데이터로 교체 → (d) 5 concrete + xml + manifest → (e) WidgetUpdater + Application.start() → (f) 실기기에서 A-6 시나리오 검증.
3. 각 PART 후 debug APK 빌드·실기기 설치·검증 결과 보고(코드 덤프 X, 트리+빌드결과+설치상태+검증절차).
