# WebMY Core SDK — Agent Guide

Reference for AI coding agents (Claude Code, Cursor, Copilot, etc.) implementing features in Android apps that depend on **WebMY Core SDK** (`com.github.WebMY-Studio:core` + optional `:core-monetization`).

This file describes the *consumer-side* API surface — what is injectable, how to navigate, how to gate ads behind purchases, how to extend.

> **Always fetch the version matching the consumer's SDK version**, e.g.
> `https://raw.githubusercontent.com/WebMY-Studio/core-sdk-android/v0.6.0/AGENTS.md`

---

## 1. Modules and dependencies

```kotlin
// settings.gradle.kts — repositories
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://artifactory.appodeal.com/appodeal") // only if using ads
    }
}

// consumer app/build.gradle.kts
dependencies {
    implementation("com.github.WebMY-Studio.core-sdk-android:core:<version>")
    // Optional — only if using billing/ads
    implementation("com.github.WebMY-Studio.core-sdk-android:core-monetization:<version>")
}
```

- `:core` — DI (Koin), Compose theme + base UI, Router, Preferences, Analytics, RemoteConfig, Sharing, Biometrics, Network (OkHttp + Retrofit), CSV, single-activity host (`WebmyActivity`).
- `:core-monetization` — Google Play Billing (`BillingManager`), Adapty, Appodeal Ads (`AdsManager`), `PremiumUseCase`, `DisplayAdUseCase`.

There is **no separate `:core-ui` module** — Compose + base UI live inside `:core`.

---

## 2. Initialization

In consumer's `Application` subclass:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        WebMY.init(
            config = WebMYConfig(
                application = this,
                koinMode = KoinMode.START, // or KoinMode.LOAD if Koin already started
                amplitudeKey = BuildConfig.AMPLITUDE_KEY, // optional
                remoteConfigUpdateInterval = 1.hours, // optional
                network = NetworkConfig(
                    enableHttpLogging = BuildConfig.DEBUG,
                    interceptors = listOf(MyAuthInterceptor()),
                ),
            ),
            extraModules = listOf(appModule), // consumer's own Koin module(s)
        )
        WebMY.installUi() // registers Router, SheetController, OnboardingShownPreferences

        // Optional extensions:
        WebMY.initBilling(
            subscriptionProductIds = setOf("yearly_premium", "monthly_premium"),
            oneTimeProductIds = setOf("coins_100"),
            consumableProductIds = setOf("coins_100"),     // subset of oneTimeProductIds
            premiumProductIds = setOf("yearly_premium", "monthly_premium"),
        )
        WebMY.initAdapty(BuildConfig.ADAPTY_KEY)
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
- `KoinMode.START` calls `startKoin{}` internally and registers `androidContext`. Use `LOAD` if consumer app already initialized Koin elsewhere — in that case consumer MUST have called `androidContext(this)` in their own `startKoin{}` (required by `:core` network/preferences bindings).
- `installUi()` must be called **after** `init()` and **before** `initBilling/initAds` if the latter need anything from `:core` UI.
- `initAds` requires `initBilling` to be called first (it injects `PremiumUseCase`).

---

## 3. Single-activity host

Consumer's `MainActivity` extends `WebmyActivity`:

```kotlin
class MainActivity : WebmyActivity() {
    override fun createStartFragment(): Fragment = HomeFragment()
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
- Binds `Router` to its `supportFragmentManager` + container.
- Hosts a Compose overlay for bottom sheets (`Navigation.Sheet`).
- Replaces the container with `createStartFragment()` on first launch.

---

## 4. Fragments

### XML + ViewBinding
```kotlin
class SettingsFragment : BaseFragment<SettingsViewModel, FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate,
) {
    override val viewModel: SettingsViewModel by viewModel()

    override fun initView() { /* set up listeners using `binding` */ }
    override fun observe(viewModel: SettingsViewModel) { /* collect flows */ }
}
```

### Compose
```kotlin
class HomeFragment : BaseComposeFragment() {
    @Composable
    override fun ScreenContent() {
        val vm: HomeViewModel = koinViewModel()
        // ...
    }
}
```

`BaseFragment` / `BaseComposeFragment` are zero-cost wrappers. Neither subscribes to anything implicitly — navigation is fired synchronously by the ViewModel via injected `Router`.

---

## 5. ViewModels and navigation

```kotlin
class HomeViewModel : BaseViewModel() {
    fun onSettingsClick() {
        navigateTo(screen<SettingsFragment>(SettingsArgs(userId = "42")))
    }

    fun onBackClick() = navigateTo(Navigation.Back)

    fun onLinkClick(url: String) = navigateTo(Navigation.Browser(url))
}
```

`BaseViewModel` is `KoinComponent`. `navigateTo(nav)` synchronously calls injected `Router.go(nav)`. Order of calls is preserved. No `MutableSharedFlow`, no drops.

Available `Navigation` cases (sealed):
- `Screen(fragmentClass, args, addToBackStack)` — use helper `screen<F>(payload, addToBackStack)`
- `Back`, `PopUpTo(tag, inclusive)`
- `Browser(url)`, `Email(email, subject, text)`, `GooglePlay(appId)`, `RateApp`
- `Finish`
- `Sheet(@Composable content)`, `DismissSheet`
- `Auth.OneTime(onResult)`, `Auth.Session(onResult)` — biometrics

There is **no `Navigation.Ad` or `Navigation.Purchase`** — use `DisplayAdUseCase` and `BillingManager` directly.

### Fragment args (Parcelable payload)
```kotlin
@Parcelize
data class SettingsArgs(val userId: String, val title: String) : Parcelable

// destination fragment
val args: SettingsArgs = requireArgs()
```

### Payload straight into ViewModel (Koin `parametersOf`)

Skip reading args in the fragment — inject the payload into the ViewModel constructor:

```kotlin
// VM
class SettingsViewModel(private val args: SettingsArgs) : BaseViewModel()

// Koin module
viewModel { (args: SettingsArgs) -> SettingsViewModel(args) }

// Fragment
override val viewModel: SettingsViewModel by viewModel {
    parametersOf(requireArgs<SettingsArgs>())
}
```

Process-death-safe: Fragment args (Bundle) are restored by the system, so Koin recreates the VM with the same payload. Works with `BaseFragment` and `BaseComposeFragment` (use `koinViewModel { parametersOf(...) }` in Compose).

---

## 6. Injectable singletons (via Koin `by inject()` / `koinInject()` / `get<>()`)

### From `:core`
| Type | Purpose |
|------|---------|
| `Router` | navigation events |
| `ActivityProvider` | current foreground Activity (`current` nullable, `requireCurrent()` throws `SdkError.NoForegroundActivity`) |
| `Preferences` | SharedPreferences wrapper + Flows (`stringFlow/booleanFlow/intFlow/longFlow/stringSetFlow`) |
| `AnalyticsManager` | `logEvent(name, props)` (Amplitude) + `logFirebase(name, bundle)` |
| `RemoteConfigManager` | `suspend getString/getBoolean/getLong/getDouble` — registered only if `remoteConfigUpdateInterval != null` |
| `SharingManager` | `shareContent / shareText / shareEvent` |
| `BiometricsService` | `suspend performSessionAuthentication / performOneTimeAuthentication` |
| `NetworkApiCreator` | `create<MyApi>(baseUrl)` Retrofit factory |
| `CsvFetcher` | `suspend byUrl(url, mapper)` |
| `OnboardingShownPreferences` | single-value flag, `flow() / value() / setValue()` |
| `SheetController` | imperatively show/dismiss Compose bottom sheets (usually use `Navigation.Sheet`) |

### From `:core-monetization` (only if installed)
| Type | Purpose |
|------|---------|
| `BillingManager` | `subscribeProducts(): Flow<List<Product>>`, `suspend purchase(productId): PurchaseOutcome`, `awaitInitialized()`, `canBePurchased(id)` |
| `PremiumUseCase` | `isPremiumFlow: Flow<Boolean>`, `subscriptionsFlow`, extension `suspend isPremium()` |
| `DisplayAdUseCase` | `showBanner(container) / hideBanner(container)`, `showInterstitial(source)`, `showReward(placement, source, grantWhenPremium, onResult)` — premium gating + throttle built in |
| `AdsManager` | low-level Appodeal wrapper — prefer `DisplayAdUseCase` |

---

## 7. Billing — purchase flow

```kotlin
class PaywallViewModel(
    private val billingManager: BillingManager,
    private val premium: PremiumUseCase,
) : BaseViewModel() {

    val products = billingManager.subscribeProducts()
        .map { it.filterIsInstance<Product.Subscription>() }

    fun onBuyClick(productId: String) {
        viewModelScope.launch {
            when (val outcome = billingManager.purchase(productId)) {
                PurchaseOutcome.Success   -> navigateTo(Navigation.Back)
                PurchaseOutcome.Pending   -> showSnackbar("Payment pending")
                PurchaseOutcome.Cancelled -> Unit
                is PurchaseOutcome.Failed -> showError(outcome.error.message)
            }
        }
    }
}
```

Or extend `BasePaywallViewModel`:
```kotlin
class MyPaywallViewModel(
    config: PlanListPaywallConfig,
    billingManager: BillingManager,
    premium: PremiumUseCase,
    analytics: AnalyticsManager,
) : BasePlanListPaywallViewModel(config, billingManager, premium, analytics) {
    override val originProperty = "main_screen"
}
```

Subclasses available:
- `BaseOfferPaywallViewModel(OfferPaywallConfig, ...)` — single offer screen (base price + discount).
- `BasePlanListPaywallViewModel(PlanListPaywallConfig, ...)` — selectable plan list.

### Products
`BillingManager.subscribeProducts()` returns `Flow<List<Product>>` with:
- `Product.OneTime(id, isPurchased, title, formattedPrice, consumable)`
- `Product.Subscription(id, isPurchased, title, offerToken, phases)`

`isPurchased` is always `false` for `consumable` products (they re-purchase). For consumables the SDK calls `consumePurchase` automatically with exponential backoff retry; for non-consumables it acknowledges.

### Premium check
```kotlin
if (premium.isPremium()) hideAds() else showInterstitial()
```
`isPremiumFlow` checks `purchasedIds` against the `premiumProductIds` set passed to `initBilling`. If empty, falls back to "any purchased product = premium".

---

## 8. Ads — `DisplayAdUseCase`

```kotlin
class GameViewModel(
    private val ads: DisplayAdUseCase,
) : BaseViewModel() {

    fun onLevelComplete() {
        ads.showInterstitial(source = "level_complete") // skipped if premium, throttled by config
    }

    fun onWatchAdClick() {
        ads.showReward(placement = "double_coins") { rewarded ->
            if (rewarded) grantCoins(20)
        }
    }
}

// Composable
@Composable
fun BannerSlot() {
    AndroidView(factory = { FrameLayout(it).also { container ->
        // ads.showBanner(container) — call from a side-effect, not directly in factory in real code
    }})
}
```

Premium gating + interstitial throttling are inside `RealDisplayAdUseCase`. Do not call `AdsManager` directly unless you're bypassing premium intentionally.

---

## 9. Biometrics

```kotlin
val biometrics: BiometricsService by inject()

viewModelScope.launch {
    biometrics.performSessionAuthentication() // returns Result<Unit>; ignores if already authed this session
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

Requires consumer's Activity to be a `FragmentActivity` (`WebmyActivity` is). Throws `SdkError.NotSupported` otherwise.

---

## 10. Preferences

```kotlin
val prefs: Preferences by inject()

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

For one-off boolean flags, create your own `SingleValuePrefs<T>` impl (mirror `OnboardingShownPreferences`).

---

## 11. Remote Config

Only registered if `WebMYConfig(remoteConfigUpdateInterval = ...)` is non-null AND `google-services.json` is set up.

```kotlin
val rc: RemoteConfigManager by inject()

viewModelScope.launch {
    rc.getString("welcome_message")
        .onSuccess { showWelcome(it) }
}
```

`getString/getBoolean/getLong/getDouble` are suspend; they wait until first fetch+activate completes. **Known limitation:** if the first fetch fails, calls block forever. Wrap in `withTimeout` if unsure.

---

## 12. Analytics

```kotlin
val a: AnalyticsManager by inject()
a.logEvent("button_click", mapOf("screen" to "home"))    // Amplitude
a.logFirebase("level_up", bundleOf("level" to 5))         // Firebase
```

Amplitude is enabled only if `WebMYConfig(amplitudeKey = ...)` is non-null. Firebase Analytics is always available (requires `google-services.json` + Firebase plugin in consumer app).

---

## 13. Sharing

```kotlin
val sharing: SharingManager by inject()

sharing.shareText("Check this out: https://example.com")
sharing.shareContent(ContentSharing.file(
    text = "screenshot",
    uri = fileUri,
    mimeType = "image/png",
))
sharing.shareEvent(EventSharing(title = "Meeting", startTime = ..., endTime = ...))
```

Requires foreground Activity — usually fine from a Fragment / Composable triggered by user.

---

## 14. Network

```kotlin
val creator: NetworkApiCreator by inject()
val api: MyApi = creator.create("https://api.example.com/")
```

OkHttp client is a singleton built from `NetworkConfig`. To add interceptors at init time, pass them in `WebMYConfig.network.interceptors`. To enable HTTP body logging, pass `enableHttpLogging = true`.

---

## 15. Theming

`AppTheme { content() }` wraps Material3 with the active WebMY palette + typography + spacings. `BaseComposeFragment` and `WebmyActivity` already wrap their content in `AppTheme`, so inside any SDK screen you just read tokens:
```kotlin
WebmyTheme.colors.textAndIconsPrimary
WebmyTheme.typography.bodyM
WebmyTheme.spacings.m
```

Pre-built composables under `us.webmy.core.ui.compose.components.*`: `WebmyButton`, `WebmyText`, `WebmySurface`, `WebmySwitch`, `WebmyCircularProgressIndicator`, plus `Spacers.Horizontal(size)` / `Spacers.Vertical(size)`.

### Multi-theme model

The SDK supports an arbitrary number of themes (not just light/dark). A theme is a single object:
```kotlin
class ThemePalette(
    val id: ThemeId,        // typealias ThemeId = String
    val isDark: Boolean,    // drives status-bar / nav-bar icon contrast
    val palette: ColorsPalette,
)
```
Built-in ids live in `BuildInThemeIds.LIGHT` / `BuildInThemeIds.DARK` (`DEFAULT = LIGHT`). The SDK does **not** store a display name — the consumer app owns the `id → title` mapping (see below), so adding/renaming a theme never touches the SDK.

Built-in themes: **Light** (default) and **Dark**. The chosen theme is **persisted automatically** and survives process restart. Status-bar / navigation-bar icon appearance follows the active theme's `isDark` automatically — no manual window handling.

### Switching / observing the theme

Inject `WebmyThemeController` (singleton) — the single entry point for all theme operations:
```kotlin
val controller: WebmyThemeController by inject()      // or koinInject() in Compose

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
fun ThemePicker(controller: WebmyThemeController = koinInject()) {
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

1. **Define a palette** — subclass `ColorsPalette`, override every token (Compose `Color`). `ColorsPalette` is color-only; `isDark` lives on the `ThemePalette`, not here:
```kotlin
class AccentColorsPalette : ColorsPalette() {
    override val backgroundPrimary = Color(0xFF1A1230)
    override val textAndIconsPrimary = Color(0xFFEDE9FB)
    // ... override ALL remaining tokens
}
```

2. **Register the theme in your Koin module** with a unique `named(...)` qualifier (required — `getAll<ThemePalette>()` only sees distinct qualifiers). The controller picks it up automatically:
```kotlin
val appModule = module {
    val accentId = "accent"
    single(named(accentId)) {
        ThemePalette(id = accentId, isDark = true, palette = AccentColorsPalette())
    }
}
```
Set `isDark` correctly — it drives status-bar icon contrast when this theme is active.

3. **Map the id to a display name** wherever you render the picker (your `strings.xml` + a `when(id)` like `themeTitleRes` above). The SDK never asks for it.

4. **Select it** anywhere: `controller.select("accent")`.

Notes:
- Themes must be registered in a Koin module passed to `WebMY.init(extraModules = ...)` so they are loaded before the first `AppTheme` composition.
- Adding a new color token to `ColorsPalette` in a future SDK version is a breaking change for consumer palettes (it's an `abstract` member) — pin your SDK version and re-build palettes on upgrade.
- For a one-off palette override without registering a theme, you can still provide directly: `CompositionLocalProvider(LocalColorsPalette provides MyPalette) { ... }`.

---

## 16. Errors

All SDK-thrown errors are subclasses of `SdkError: Throwable`:
- `SdkError.NoForegroundActivity`
- `SdkError.NotSupported(reason)`
- `SdkError.NotImplemented(feature)`
- `SdkError.BindingMissing(name)`
- `SdkError.Network.{ EmptyBody, HttpError(code, message), Io(cause) }`
- `SdkError.Billing.{ NotAcknowledged(token), FlowFailed(message), Disconnected }`
- `SdkError.Ads.{ LoadFailed(placement), ShowFailed(placement), NotInitialized }`

`BillingManager` returns `PurchaseOutcome.Failed(SdkError.Billing)` instead of throwing.

---

## 17. Onboarding

Extend `BaseOnboardingViewModel`:
```kotlin
class MyOnboardingViewModel(
    onboardingPrefs: OnboardingShownPreferences,
    analytics: AnalyticsManager,
) : BaseOnboardingViewModel<MyPageModel>(onboardingPrefs, analytics) {

    override val onboardingModels: List<MyPageModel> = listOf(
        MyPageModel(index = 0, ...),
        MyPageModel(index = 1, ...),
    )

    override fun navigateNext() {
        navigateTo(screen<MainScreenFragment>(addToBackStack = false))
    }
}

data class MyPageModel(override val index: Int, val title: String) : OnboardingModel
```

`onCloseClick()` and `onNextClick()` are pre-wired. `currentItem: SharedFlow<T>` exposes the active page.

---

## 18. Building a new feature — quick checklist

When implementing a new screen in a consumer app:

1. **Create Fragment** — `BaseFragment` (XML) or `BaseComposeFragment` (Compose).
2. **Create ViewModel** — extend `BaseViewModel`. Inject what you need via Koin. Call `navigateTo(...)` for navigation.
3. **Register ViewModel in Koin module:** `viewModel { MyViewModel(get(), get()) }`. If the screen takes a payload, use `viewModel { (args: MyArgs) -> MyViewModel(args, get()) }` and resolve via `by viewModel { parametersOf(requireArgs<MyArgs>()) }`.
4. **Add Koin module to `WebMY.init(extraModules = listOf(appModule))`**.
5. **Open it from elsewhere:** `navigateTo(screen<MyFragment>(MyArgs(...)))`.

When implementing a paywall:
1. Decide layout: single offer (`BaseOfferPaywallViewModel`) or plan list (`BasePlanListPaywallViewModel`).
2. Build config (`OfferPaywallConfig` / `PlanListPaywallConfig`).
3. Subclass the base, override `originProperty` for analytics.
4. Bind UI to `offerUiStateFlow` / `paywallUiState`.
5. Call `onContinueClick()` from the CTA.

When adding ad gating:
1. Inject `DisplayAdUseCase`.
2. Call `showInterstitial / showReward / showBanner` from the ViewModel.
3. Premium check + throttle are handled internally.

---

## 19. Common gotchas

- **`WebMY.application` accessed before `init()`** → throws `IllegalStateException("WebMY.init(...) not called")`. Always init in `Application.onCreate`.
- **`Router.go(Navigation.Screen)` before `WebmyActivity.onCreate`** → throws `SdkError.BindingMissing`. Router binds in Activity `onCreate`.
- **`BillingManager.purchase()` before `awaitInitialized()` succeeded** → returns `PurchaseOutcome.Failed("BillingManager not initialized")`. Either await or check `subscribeProducts()` first.
- **`DisplayAdUseCase` without `initBilling`** → Koin resolution fails (`PremiumUseCase` missing). Call `initBilling` even with empty product sets if you want ads.
- **`RemoteConfigManager` injection fails** → `remoteConfigUpdateInterval` is `null` in `WebMYConfig`. Pass any non-null `Duration` to enable.
- **Biometrics on non-`FragmentActivity`** → throws `SdkError.NotSupported`. Consumer must use `WebmyActivity` or another `FragmentActivity` subclass.
- **`consumableProductIds` not subset of `oneTimeProductIds`** → throws `IllegalArgumentException` at `RealBillingManager` init.
- **Calling `WebMY.init` twice** → second call is a silent no-op with `Log.w("WebMY", ...)`.

---

## 20. Versioning

This document is **versioned with the SDK**. To pin docs to a specific SDK version:
```
https://raw.githubusercontent.com/WebMY-Studio/core-sdk-android/v0.6.0/AGENTS.md
```
Or `main` for the latest:
```
https://raw.githubusercontent.com/WebMY-Studio/core-sdk-android/main/AGENTS.md
```

If you (the AI agent) detect that the consumer's `implementation("com.github.WebMY-Studio:core:X.Y.Z")` version doesn't match this doc's API, fetch the matching version explicitly.
