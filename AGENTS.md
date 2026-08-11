# WebMY Core SDK — Agent Guide

Reference for AI coding agents (Claude Code, Cursor, Copilot, etc.) implementing features in Android apps that depend on **WebMY Core SDK** (`com.github.WebMY-Studio:core` + optional monetization modules).

This file describes the *consumer-side* API surface — how to obtain SDK services, how to navigate, how to gate ads behind purchases, how to extend.

> **Always fetch the version matching the consumer's SDK version**, e.g.
> `https://raw.githubusercontent.com/WebMY-Studio/core-sdk-android/v1.0.0/AGENTS.md`

**Since v1.0.0 the SDK has no DI framework.** Koin is gone entirely. Services are resolved via typed accessors on the `WebMY` object (`WebMY.analytics`, `WebMY.router`, `WebMY.billing`, …). There is no `by inject()`, no `koinInject()`, no `koinViewModel()`, no `extraModules`, no `KoinMode`. If the consumer app uses its own DI (Koin, Hilt, manual), bridge SDK services into it: `single { WebMY.billing }`.

---

## 1. Modules and dependencies

```kotlin
// settings.gradle.kts — repositories
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://artifactory.appodeal.com/appodeal") // only if using full :core-monetization-ads (ads)
        maven("https://verve.jfrog.io/artifactory/verve-gradle-release/") // only if using full :core-monetization-ads (ads)
    }
}

// consumer app/build.gradle.kts
dependencies {
    implementation("com.github.WebMY-Studio.core-sdk-android:core:<version>")

    // Optional — billing + Apphud only, no ad SDKs, no extra maven repos needed:
    implementation("com.github.WebMY-Studio.core-sdk-android:core-monetization-billing:<version>")

    // OR — Optional — billing + Apphud + Appodeal ads + mediation adapters:
    implementation("com.github.WebMY-Studio.core-sdk-android:core-monetization-ads:<version>")
}
```

- `:core` — service registry, Compose theme + base UI, Router (Navigation 3), Preferences, Analytics, RemoteConfig, Sharing, Biometrics, Network (OkHttp + Retrofit), single-activity host (`WebmyActivity`).
- `:core-monetization-billing` — Google Play Billing (`BillingManager`), Apphud, Facebook Android SDK, `PremiumUseCase`, paywall base ViewModels. No ad SDKs, no `AD_ID` permission, no Appodeal/Verve maven repos.
- `:core-monetization-ads` — superset of `:core-monetization-billing` — adds Appodeal Ads (`AdsManager`), `DisplayAdUseCase`, and all mediation adapters.

There is **no separate `:core-ui` module** — Compose + base UI live inside `:core`.

### Package layout (v1.0.0)

Public API lives in shallow packages:

| Area | Package |
|---|---|
| Entry point, config, errors | `us.webmy.core` (`WebMY`, `WebMYConfig`, `NetworkConfig`, `SdkError`, `installUi`) |
| Analytics | `us.webmy.core.analytics` |
| Preferences | `us.webmy.core.prefs` |
| Biometrics | `us.webmy.core.biometrics` |
| Remote config | `us.webmy.core.remoteconfig` |
| Sharing | `us.webmy.core.sharing` |
| Network | `us.webmy.core.network` |
| Theming | `us.webmy.core.theme` |
| Compose components | `us.webmy.core.components` |
| Navigation | `us.webmy.core.navigation` |
| Base VM / Activity / Onboarding | `us.webmy.core.presentation` |
| Utils (`Legal`, `Result.flatMap`, flow helpers) | `us.webmy.core.util` |
| Billing | `us.webmy.core.monetization.billing` + `.paywall` |
| Ads | `us.webmy.core.monetization.ads` |

Anything under `us.webmy.core.internal.*` (or `...billing.internal` / `...ads.internal`) is **not API**: classes are Kotlin-`internal` or annotated `@InternalWebmyApi` (`@RequiresOptIn`, level ERROR). Never suggest opting in from consumer code.

### Required manifest placeholders

Both monetization modules ship manifest `meta-data` with placeholders, so the consumer app must declare them or manifest merging fails:

```kotlin
// consumer app/build.gradle.kts
android {
    defaultConfig {
        manifestPlaceholders += mapOf(
            // :core-monetization-billing (Facebook SDK) — also required transitively by :core-monetization-ads
            "FACEBOOK_APP_ID" to "<your-facebook-app-id>",
            "FACEBOOK_CLIENT_TOKEN" to "<your-facebook-client-token>",

            // :core-monetization-ads only
            "ADMOB_APPLICATION_ID" to "<your-admob-app-id>",
        )
    }
}
```

---

## 2. Initialization

In consumer's `Application` subclass:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        WebMY.init(
            WebMYConfig(
                application = this,
                amplitudeKey = BuildConfig.AMPLITUDE_KEY, // optional
                remoteConfigUpdateInterval = 1.hours,     // optional
                network = NetworkConfig(
                    enableHttpLogging = BuildConfig.DEBUG,
                    interceptors = listOf(MyAuthInterceptor()),
                ),
            )
        )
        WebMY.installUi(
            extraPalettes = listOf(                        // optional custom themes
                ThemePalette(id = "accent", isDark = true, palette = AccentColorsPalette()),
            ),
        )

        // Optional extensions:
        WebMY.initBilling(
            subscriptionProductIds = setOf("yearly_premium", "monthly_premium"),
            oneTimeProductIds = setOf("coins_100"),
            consumableProductIds = setOf("coins_100"),     // subset of oneTimeProductIds
            premiumProductIds = setOf("yearly_premium", "monthly_premium"),
        )
        WebMY.initApphud(BuildConfig.APPHUD_KEY)
        WebMY.initAds(
            appodealKey = BuildConfig.APPODEAL_KEY,
            throttleConfigProvider = {
                InterstitialThrottleConfig(firstSkipAdsAmount = 3, skipAdsAmount = 2)
            },
        )
    }
}
```

Rules:
- `WebMY.init` is idempotent — second call is a no-op with warning.
- Order matters: `init` → `installUi` → `initBilling` → `initAds`.
- `installUi()` registers Router, theming, sheets, onboarding prefs. LIGHT and DARK palettes are always registered; `extraPalettes` appends custom ones.
- `initAds` requires `initBilling` first (it resolves `PremiumUseCase`). Calling it earlier throws `IllegalStateException` with an actionable message.
- Accessing an accessor before its init step throws `IllegalStateException` with a message naming the missing call.

---

## 3. Single-activity host

Pure Compose, Navigation 3 — no fragments, no XML. Consumer's `MainActivity` extends `WebmyActivity` and declares its screens:

```kotlin
data object HomeKey : NavKey
data class DetailsKey(val id: String) : NavKey

class MainActivity : WebmyActivity() {
    override fun startScreen(): NavKey = HomeKey

    override fun EntryProviderScope<NavKey>.screens() {
        entry<HomeKey> { HomeScreen() }
        entry<DetailsKey> { key -> DetailsScreen(key.id) }
    }
}
```

Manifest:
```xml
<activity android:name=".MainActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN"/>
        <category android:name="android.intent.category.LAUNCHER"/>
    </intent-filter>
</activity>
```

`WebmyActivity` automatically:
- Wraps content in `AppTheme`.
- Renders the back stack owned by `Router` through a Navigation 3 `NavDisplay`.
- Hosts a Compose overlay for bottom sheets (`Navigation.Sheet`).
- Extends `FragmentActivity` purely because `androidx.biometric` requires one — no fragment is ever added.

The back stack survives configuration changes (it lives in the `Router` singleton) but **not process death** — the app restarts at `startScreen()`.

**There are no `BaseFragment` / `BaseComposeFragment`** — screens are plain composables keyed by `NavKey`. Screen arguments travel inside the key (`DetailsKey(id = "42")`), not in Bundles.

---

## 4. ViewModels and navigation

```kotlin
class HomeViewModel : BaseViewModel() {
    fun onSettingsClick() = navigateTo(screen(SettingsKey(userId = "42")))
    fun onBackClick() = navigateTo(Navigation.Back)
    fun onLinkClick(url: String) = navigateTo(Navigation.Browser(url))
}
```

`BaseViewModel` (`us.webmy.core.presentation`) provides:
- `navigateTo(nav): Result<Unit>` — synchronously calls `WebMY.router.go(nav)`. Order preserved, no flows, no drops.
- `navigateWhenResumed(nav)` — defers until the host activity is RESUMED.

In composables, navigate via `WebMY.router.go(...)`.

Available `Navigation` cases (sealed, `us.webmy.core.navigation`):
- `Screen(key, addToBackStack)` — use helper `screen(key, addToBackStack = true)`
- `Root(key)` — reset stack to a single root
- `Back`, `PopUpTo(key, inclusive = false)`
- `Browser(url)`, `Email(email, subject, text)`, `GooglePlay(applicationId)`, `RateApp`
- `Finish`
- `Sheet(content: @Composable () -> Unit)`, `DismissSheet`
- `Auth.OneTime(onResult)`, `Auth.Session(onResult)` — biometrics

There is **no `Navigation.Ad` or `Navigation.Purchase`** — use `WebMY.displayAd` and `WebMY.billing` directly.

ViewModel construction is plain — no DI required. A screen payload is just a constructor parameter taken from the `NavKey`:

```kotlin
class DetailsViewModel(private val id: String) : BaseViewModel()

// in the entry:
entry<DetailsKey> { key ->
    val vm: DetailsViewModel = viewModel { DetailsViewModel(key.id) }
    DetailsScreen(vm)
}
```

---

## 5. SDK services — typed accessors

All services are lazy singletons behind `WebMY`. No injection framework involved.

### From `:core` (available after `WebMY.init`)
| Accessor | Type | Purpose |
|------|------|---------|
| `WebMY.analytics` | `AnalyticsManager` | `logEvent(name, props)` (Amplitude) + `logFirebase(name, bundle)` |
| `WebMY.preferences` | `Preferences` | SharedPreferences wrapper + Flows (`stringFlow/booleanFlow/intFlow/longFlow/stringSetFlow`) |
| `WebMY.sharing` | `SharingManager` | `shareContent / shareText / shareEvent` |
| `WebMY.biometrics` | `BiometricsService` | `suspend performSessionAuthentication / performOneTimeAuthentication` |
| `WebMY.network` | `NetworkApiCreator` | `create<MyApi>(baseUrl)` Retrofit factory |
| `WebMY.httpClient` | `OkHttpClient` | the SDK's singleton OkHttp client |
| `WebMY.activityProvider` | `ActivityProvider` | current foreground Activity (`current` nullable, `requireCurrent()` throws `SdkError.NoForegroundActivity`) |
| `WebMY.remoteConfig` | `RemoteConfigManager` | `suspend getString/getBoolean/getLong/getDouble` — throws unless `remoteConfigUpdateInterval` was set |

### After `WebMY.installUi()`
| Accessor | Type | Purpose |
|------|------|---------|
| `WebMY.router` | `Router` | navigation events + `backStack` |
| `WebMY.theme` | `WebmyThemeController` | theme switching/observing |
| `WebMY.onboardingPreferences` | `OnboardingShownPreferences` | onboarding-shown flag, `flow() / value() / setValue()` |

### From `:core-monetization-billing` (after `WebMY.initBilling`; also present in `:core-monetization-ads`)
| Accessor | Type | Purpose |
|------|------|---------|
| `WebMY.billing` | `BillingManager` | `subscribeProducts(): Flow<List<Product>>`, `suspend purchase(productId): PurchaseOutcome`, `awaitInitialized()`, `canBePurchased(id)` |
| `WebMY.premium` | `PremiumUseCase` | `isPremiumFlow: Flow<Boolean>`, extension `suspend isPremium()` |

### From `:core-monetization-ads` only (after `WebMY.initAds`)
| Accessor | Type | Purpose |
|------|------|---------|
| `WebMY.displayAd` | `DisplayAdUseCase` | `showBanner(container) / hideBanner(container)`, `showInterstitial(source)`, `showReward(placement, source, grantWhenPremium, onResult)` — premium gating + throttle built in |
| `WebMY.ads` | `AdsManager` | low-level Appodeal wrapper — prefer `WebMY.displayAd` |

---

## 6. Billing — purchase flow

```kotlin
class PaywallViewModel : BaseViewModel() {
    private val billing = WebMY.billing

    val products = billing.subscribeProducts()
        .map { it.filterIsInstance<Product.Subscription>() }

    fun onBuyClick(productId: String) {
        viewModelScope.launch {
            when (val outcome = billing.purchase(productId)) {
                PurchaseOutcome.Success   -> navigateTo(Navigation.Back)
                PurchaseOutcome.Pending   -> showSnackbar("Payment pending")
                PurchaseOutcome.Cancelled -> Unit
                is PurchaseOutcome.Failed -> showError(outcome.error.message)
            }
        }
    }
}
```

Or extend a base paywall VM (`us.webmy.core.monetization.billing.paywall`):
```kotlin
class MyPaywallViewModel : BasePlanListPaywallViewModel(
    config = PlanListPaywallConfig(...),
    billingManager = WebMY.billing,
    analyticsManager = WebMY.analytics,
)
```

Subclasses available:
- `BaseOfferPaywallViewModel(config: OfferPaywallConfig, billingManager, analyticsManager)` — single offer screen (base price + discount). UI state: `OfferUiState`.
- `BasePlanListPaywallViewModel(config: PlanListPaywallConfig, billingManager, analyticsManager)` — selectable plan list. UI state: `PaywallUiState` (wraps `SubscriptionsUiModel` per plan).

Both extend `BasePaywallViewModel(billingManager, analyticsManager)` which pre-wires `purchase()` + `purchase_*` analytics events.

### Products
`BillingManager.subscribeProducts()` returns `Flow<List<Product>>` with:
- `Product.OneTime(id, isPurchased, title, formattedPrice, consumable)`
- `Product.Subscription(id, isPurchased, title, offerToken, phases: List<Phase>)` — `Phase(formattedPrice, billingPeriod, priceMicros, ...)`

`isPurchased` is always `false` for `consumable` products (they re-purchase). For consumables the SDK calls `consumePurchase` automatically with exponential backoff retry; for non-consumables it acknowledges.

### Premium check
```kotlin
if (WebMY.premium.isPremium()) hideAds() else showInterstitial()
```
`isPremiumFlow` checks purchased ids against the `premiumProductIds` set passed to `initBilling`. If empty, falls back to "any purchased product = premium".

---

## 7. Ads — `DisplayAdUseCase`

```kotlin
class GameViewModel : BaseViewModel() {
    private val ads = WebMY.displayAd

    fun onLevelComplete() {
        ads.showInterstitial(source = "level_complete") // skipped if premium, throttled by config
    }

    fun onWatchAdClick() {
        ads.showReward(placement = "double_coins") { rewarded ->
            if (rewarded) grantCoins(20)
        }
    }
}
```

Premium gating + interstitial throttling are inside the `DisplayAdUseCase` implementation. Do not call `WebMY.ads` (raw `AdsManager`) directly unless bypassing premium intentionally.

---

## 8. Biometrics

```kotlin
viewModelScope.launch {
    WebMY.biometrics.performSessionAuthentication() // Result<Unit>; no-op if already authed this session
        .onSuccess { unlockScreen() }
        .onFailure { showError(it.message) }
}
```

Or via Router:
```kotlin
navigateTo(Navigation.Auth.Session(onResult = { result ->
    result.onSuccess { unlockScreen() }
}))
```

Requires the foreground Activity to be a `FragmentActivity` (`WebmyActivity` is). Throws `SdkError.NotSupported` otherwise.

---

## 9. Preferences

```kotlin
val prefs = WebMY.preferences

prefs.putString("user_name", "John")
val name = prefs.getString("user_name") // nullable, no default
val email = prefs.getString("email", "default@x.com")

// Reactive
prefs.stringFlow("user_name").collect { ... }
prefs.booleanFlow("dark_mode", defaultValue = false).collect { ... }

// Batch
prefs.edit {
    putString("a", "1")
    putInt("b", 2)
}
```

For one-off boolean flags, create your own `SingleValuePrefs<T>` impl (mirror `OnboardingShownPreferences`, both in `us.webmy.core.prefs`).

---

## 10. Remote Config

Only registered if `WebMYConfig(remoteConfigUpdateInterval = ...)` is non-null AND `google-services.json` is set up. `WebMY.remoteConfig` throws otherwise.

```kotlin
viewModelScope.launch {
    WebMY.remoteConfig.getString("welcome_message")
        .onSuccess { showWelcome(it) }
}
```

`getString/getBoolean/getLong/getDouble` are suspend; they wait until first fetch+activate completes. **Known limitation:** if the first fetch fails, calls block forever. Wrap in `withTimeout` if unsure.

---

## 11. Analytics

```kotlin
WebMY.analytics.logEvent("button_click", mapOf("screen" to "home"))  // Amplitude
WebMY.analytics.logFirebase("level_up", bundleOf("level" to 5))       // Firebase
```

Amplitude is enabled only if `WebMYConfig(amplitudeKey = ...)` is non-null. Firebase Analytics is always available (requires `google-services.json` + Firebase plugin in consumer app).

---

## 12. Sharing

```kotlin
WebMY.sharing.shareText("Check this out: https://example.com")
WebMY.sharing.shareContent(ContentSharing(
    text = "screenshot",
    file = FileSharing(uri = fileUri, mimeType = "image/png"),
))
WebMY.sharing.shareEvent(EventSharing(title = "Meeting", startTime = ..., endTime = ...))
```

Requires foreground Activity — usually fine when triggered by user interaction.

---

## 13. Network

```kotlin
val api: MyApi = WebMY.network.create("https://api.example.com/")
```

OkHttp client is a singleton (`WebMY.httpClient`) built from `NetworkConfig`. To add interceptors at init time, pass them in `WebMYConfig.network.interceptors`. To enable HTTP body logging, pass `enableHttpLogging = true`.

---

## 14. Theming

`AppTheme { content() }` (in `us.webmy.core.theme`) wraps Material3 with the active WebMY palette + typography + spacings. `WebmyActivity` already wraps its content, so inside any SDK-hosted screen just read tokens:
```kotlin
WebmyTheme.colors.textAndIconsPrimary
WebmyTheme.typography.bodyM
WebmyTheme.spacings.spacing16
```

Pre-built composables in `us.webmy.core.components`: `WebmyButton`, `WebmyText`, `WebmySurface`, `WebmySwitch`, `WebmyCircularProgressIndicator`, plus `VerticalSpacer { spacing16 }` / `HorizontalSpacer { spacing8 }` (lambda receives `WebmySpacings`).

### Multi-theme model

The SDK supports an arbitrary number of themes (not just light/dark). A theme is a single object:
```kotlin
class ThemePalette(
    val id: ThemeId,        // typealias ThemeId = String
    val isDark: Boolean,    // drives status-bar / nav-bar icon contrast
    val palette: ColorsPalette,
)
```
Built-in ids live in `BuildInThemeIds.LIGHT` / `BuildInThemeIds.DARK` (`DEFAULT = LIGHT`). The SDK does **not** store a display name — the consumer app owns the `id → title` mapping, so adding/renaming a theme never touches the SDK.

Built-in themes: **Light** (default) and **Dark**. The chosen theme is **persisted automatically** and survives process restart. Status-bar / navigation-bar icon appearance follows the active theme's `isDark` automatically.

### Switching / observing the theme

`WebMY.theme` (a `WebmyThemeController`) is the single entry point:
```kotlin
val controller = WebMY.theme

controller.themes                // List<ThemePalette> — all registered themes (for a picker)
controller.theme                 // StateFlow<ThemeId> — currently selected id
controller.select(themeId)       // change + persist; every AppTheme recomposes
controller.palette(themeId)      // ColorsPalette for an id (falls back to default)
controller.isDark(themeId)       // Boolean for an id
controller.get(themeId)          // the ThemePalette for an id (falls back to default)
```

Theme picker example — title is resolved consumer-side by id:
```kotlin
@StringRes
fun themeTitleRes(id: ThemeId): Int = when (id) {
    BuildInThemeIds.DARK -> CoreR.string.webmy_theme_dark   // us.webmy.core.R
    "accent"             -> R.string.theme_accent           // your app's R
    else                 -> CoreR.string.webmy_theme_light
}

@Composable
fun ThemePicker() {
    val controller = WebMY.theme
    val current by controller.theme.collectAsState()
    controller.themes.forEach { theme ->
        Row(Modifier.clickable { controller.select(theme.id) }) {
            Text(stringResource(themeTitleRes(theme.id)))
            if (current == theme.id) Text("✓")
        }
    }
}
```
> Built-in `webmy_theme_light` / `webmy_theme_dark` strings live in `:core`. With non-transitive R classes (AGP default), reference them via the core package R: `us.webmy.core.R` (aliased `CoreR` above), not your app's `R`.

### Implementing a new theme (consumer side)

1. **Define a palette** — subclass `ColorsPalette` (in `us.webmy.core.theme`), override every token (Compose `Color`). `ColorsPalette` is color-only; `isDark` lives on the `ThemePalette`:
```kotlin
class AccentColorsPalette : ColorsPalette() {
    override val backgroundPrimary = Color(0xFF1A1230)
    override val textAndIconsPrimary = Color(0xFFEDE9FB)
    // ... override ALL remaining tokens
}
```

2. **Register it via `installUi`** — no DI, no qualifiers:
```kotlin
WebMY.installUi(
    extraPalettes = listOf(
        ThemePalette(id = "accent", isDark = true, palette = AccentColorsPalette()),
    ),
)
```
Set `isDark` correctly — it drives status-bar icon contrast when this theme is active. Duplicate ids resolve last-wins.

3. **Map the id to a display name** wherever you render the picker (your `strings.xml` + a `when(id)` like `themeTitleRes` above). The SDK never asks for it.

4. **Select it** anywhere: `WebMY.theme.select("accent")`.

Notes:
- Adding a new color token to `ColorsPalette` in a future SDK version is a breaking change for consumer palettes (it's an `abstract` member) — pin your SDK version and re-build palettes on upgrade.
- One-off palette override for a subtree: `AppTheme(colors = MyPalette(), isDark = true) { ... }` — the `AppTheme` overload with explicit colors needs no registered theme.

---

## 15. Errors

All SDK-thrown errors are subclasses of `SdkError: Throwable` (`us.webmy.core.SdkError`):
- `SdkError.NoForegroundActivity`
- `SdkError.NotSupported(reason)`
- `SdkError.NotImplemented(feature)`
- `SdkError.BindingMissing(name)`
- `SdkError.Network.{ EmptyBody, HttpError(code, message), Io(cause) }`
- `SdkError.Billing.{ NotAcknowledged(token), FlowFailed(message), Disconnected }`
- `SdkError.Ads.{ LoadFailed(placement), ShowFailed(placement), NotInitialized }`

`BillingManager` returns `PurchaseOutcome.Failed(SdkError.Billing)` instead of throwing. Missing-service accessors (`WebMY.billing` before `initBilling`, etc.) throw `IllegalStateException` with a message naming the required init call.

---

## 16. Onboarding

Extend `BaseOnboardingViewModel` (`us.webmy.core.presentation`):
```kotlin
class MyOnboardingViewModel : BaseOnboardingViewModel<MyPageModel>(
    onboardingShownPreferences = WebMY.onboardingPreferences,
    analyticsManager = WebMY.analytics,
) {
    override val onboardingModels: List<MyPageModel> = listOf(
        MyPageModel(index = 0, title = "Welcome"),
        MyPageModel(index = 1, title = "Features"),
    )

    override fun navigateNext() {
        navigateTo(Navigation.Root(HomeKey))
    }
}

data class MyPageModel(override val index: Int, val title: String) : OnboardingModel
```

`onCloseClick()` and `onNextClick()` are pre-wired (analytics + shown-flag). `currentItem: SharedFlow<T>` exposes the active page.

---

## 17. Building a new feature — quick checklist

When implementing a new screen in a consumer app:

1. **Declare a `NavKey`** — a data class/object carrying the screen's arguments.
2. **Create a composable screen** and a ViewModel extending `BaseViewModel`. Pull SDK services via `WebMY.<accessor>`; navigation via `navigateTo(...)`.
3. **Register the entry** in `MainActivity.screens()`: `entry<MyKey> { key -> MyScreen(key) }`.
4. **Open it from elsewhere:** `navigateTo(screen(MyKey(...)))`.

When implementing a paywall:
1. Decide layout: single offer (`BaseOfferPaywallViewModel`) or plan list (`BasePlanListPaywallViewModel`).
2. Build config (`OfferPaywallConfig` / `PlanListPaywallConfig`).
3. Subclass the base, passing `WebMY.billing` + `WebMY.analytics` to the super constructor.
4. Bind UI to `offerUiState` / `paywallUiState`.
5. Call `onContinueClick()` from the CTA.

When adding ad gating:
1. Use `WebMY.displayAd`.
2. Call `showInterstitial / showReward / showBanner` from the ViewModel.
3. Premium check + throttle are handled internally.

---

## 18. Common gotchas

- **`WebMY.application` or any accessor before `init()`** → `IllegalStateException`. Always init in `Application.onCreate`.
- **`WebMY.theme` / `WebMY.router` before `installUi()`** → `IllegalStateException("UI services not installed...")`.
- **`WebMY.billing` / `WebMY.premium` before `initBilling()`** → `IllegalStateException("Billing is not initialized...")`.
- **`initAds` before `initBilling`** → `IllegalStateException("initAds requires billing...")`. Call `initBilling` even with empty product sets if you want ads.
- **`initAds` unavailable with `:core-monetization-billing`** → that module has no ad SDKs. `initBilling`/`initApphud` are available in both monetization modules.
- **`BillingManager.purchase()` before `awaitInitialized()` succeeded** → returns `PurchaseOutcome.Failed("BillingManager not initialized")`. Either await or check `subscribeProducts()` first.
- **`WebMY.remoteConfig` throws** → `remoteConfigUpdateInterval` is `null` in `WebMYConfig`. Pass any non-null `Duration` to enable.
- **Biometrics on non-`FragmentActivity`** → throws `SdkError.NotSupported`. Use `WebmyActivity` or another `FragmentActivity` subclass.
- **`consumableProductIds` not subset of `oneTimeProductIds`** → throws `IllegalArgumentException` at billing init.
- **Calling `WebMY.init` twice** → second call is a silent no-op with `Log.w("WebMY", ...)`.
- **Imports fail after upgrading from 0.x** → packages were flattened in 1.0.0; see the migration table in `README.md`.

---

## 19. Versioning

This document is **versioned with the SDK**. To pin docs to a specific SDK version:
```
https://raw.githubusercontent.com/WebMY-Studio/core-sdk-android/v1.0.0/AGENTS.md
```
Or `main` for the latest:
```
https://raw.githubusercontent.com/WebMY-Studio/core-sdk-android/main/AGENTS.md
```

If you (the AI agent) detect that the consumer's `implementation("com.github.WebMY-Studio.core-sdk-android:core:X.Y.Z")` version doesn't match this doc's API (e.g. the app still passes `koinMode` or `extraModules` — that's 0.x), fetch the matching version explicitly.
