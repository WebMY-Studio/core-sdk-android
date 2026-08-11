# WebMY Core SDK

Android SDK for shipping apps fast. One init call wires up analytics, remote config, billing, ads, biometrics, navigation, theming. Compose-first. **No DI framework required or imposed** — the SDK resolves its own services internally and exposes them through typed accessors on `WebMY`.

> 🤖 **Building with an AI agent?** Point it at [`AGENTS.md`](AGENTS.md) — full API reference written for LLMs.

---

## What's inside

| Concern | API |
|---|---|
| Single-activity host | `WebmyActivity` — pure Compose, Navigation 3 |
| Navigation | `WebMY.router` + `Navigation` sealed events |
| Analytics | `WebMY.analytics` — Amplitude + Firebase |
| Remote config | `WebMY.remoteConfig` — Firebase RC with suspend getters |
| Preferences | `WebMY.preferences` — SharedPreferences wrapper with Flows |
| Biometrics | `WebMY.biometrics` — BiometricPrompt with session caching |
| Sharing | `WebMY.sharing` — files, text, calendar events |
| Network | `WebMY.network` — Retrofit + OkHttp factory; `WebMY.httpClient` |
| Billing *(opt)* | `WebMY.billing` + `WebMY.premium` — Play Billing, subs + one-time + consumables |
| Ads *(opt)* | `WebMY.ads` + `WebMY.displayAd` — Appodeal with premium gating + interstitial throttling |
| Theming | `AppTheme` + `WebmyTheme.colors/typography/spacings` + pre-built composables |

Package layout: everything consumer-facing lives in shallow packages (`us.webmy.core.analytics`, `us.webmy.core.theme`, `us.webmy.core.components`, `us.webmy.core.navigation`, `us.webmy.core.presentation`, …). Everything under `us.webmy.core.internal.*` is implementation detail — not API, guarded by `@InternalWebmyApi`.

---

## Install

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google(); mavenCentral()
        maven("https://jitpack.io")
        maven("https://artifactory.appodeal.com/appodeal") // only if using ads
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.WebMY-Studio.core-sdk-android:core:1.0.0")

    // Billing + Apphud only, no ad SDKs (subscription-only apps):
    implementation("com.github.WebMY-Studio.core-sdk-android:core-monetization-billing:1.0.0") // optional

    // OR: Billing + Ads + Apphud + Appodeal mediation adapters:
    implementation("com.github.WebMY-Studio.core-sdk-android:core-monetization-ads:1.0.0") // optional
}
```

Multi-module syntax: `com.github.<User>.<Repo>:<module>:<version>`. Latest version: [JitPack](https://jitpack.io/#WebMY-Studio/core-sdk-android).

`maven("https://artifactory.appodeal.com/appodeal")` and the Verve repo (`https://verve.jfrog.io/artifactory/verve-gradle-release/`) are needed **only** with the full `:core-monetization-ads` module — `:core-monetization-billing` resolves from google/mavenCentral/jitpack alone.

> Since 1.0.0 the SDK exposes far fewer transitive compile-time dependencies: ad mediation adapters, Play Billing, Apphud, Facebook, OkHttp logging, Gson converter etc. are `implementation` details. They still ship at runtime, but if your app used one of them directly through the SDK's classpath leak, declare it explicitly now.

### Required manifest placeholders

`:core-monetization-billing` bundles the Facebook Android SDK, whose manifest entries need two placeholders. `:core-monetization-ads` needs a third one for AdMob. Declare them in the consumer app or the merge fails with `Attribute meta-data#…@value … placeholder not substituted`:

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        manifestPlaceholders += mapOf(
            // required by :core-monetization-billing (and by :core-monetization-ads, which depends on it)
            "FACEBOOK_APP_ID" to "<your-facebook-app-id>",
            "FACEBOOK_CLIENT_TOKEN" to "<your-facebook-client-token>",

            // required by :core-monetization-ads only
            "ADMOB_APPLICATION_ID" to "<your-admob-app-id>",
        )
    }
}
```

### Meta events

The SDK logs exactly three Meta events from `:core-monetization-billing`:

| Event | Raw name | When |
|---|---|---|
| `First_Open` | `First_Open` (custom) | Once per install, on first SDK init |
| `StartTrial` | `fb_mobile_start_trial` | A subscription with a free trial phase was actually purchased (not a button tap) |
| `Purchase` | `fb_mobile_purchase` | A direct purchase without a trial, or a trial that converted to paid |

Trial conversion is detected client-side: the trial end time is persisted at `StartTrial`, and if `queryPurchases` still returns the subscription past that time on a later launch, Play has billed the user and `Purchase` is logged once. A trial cancelled before its end never produces a `Purchase`. Events carry price, currency, product id and `fb_order_id` for server-side deduplication; restored purchases are never logged.

**Disable Meta's automatic in-app purchase logging** for the app, otherwise purchases are double-counted and restored purchases are re-logged on every launch (Purchase count ends up tracking active premium users instead of transactions): Meta App Dashboard → Settings → Basic → your Android platform → turn off "Automatically log in-app purchase events". Keep general auto-logging (`AutoLogAppEventsEnabled`) on — it powers install and app-activation events.

---

## Quick start

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        WebMY.init(
            WebMYConfig(
                application = this,
                amplitudeKey = BuildConfig.AMPLITUDE_KEY,           // optional
                remoteConfigUpdateInterval = 1.hours,               // optional
                network = NetworkConfig(enableHttpLogging = BuildConfig.DEBUG),
            )
        )
        WebMY.installUi(
            extraPalettes = listOf(MyAccentPalette),                // optional custom themes
        )

        // Optional add-ons
        WebMY.initBilling(
            subscriptionProductIds = setOf("yearly_premium"),
            premiumProductIds = setOf("yearly_premium"),
        )
        WebMY.initAds(BuildConfig.APPODEAL_KEY)                     // requires initBilling first
    }
}

class MainActivity : WebmyActivity() {
    override fun startScreen(): NavKey = HomeKey

    override fun EntryProviderScope<NavKey>.screens() {
        entry<HomeKey> { HomeScreen() }
        entry<DetailsKey> { key -> DetailsScreen(key.id) }
    }
}
```

That's it. Grab any service as `WebMY.<accessor>` — from composables, ViewModels, or your own DI modules. If you use Koin/Hilt/whatever in the app, just bridge: `single { WebMY.billing }`.

Accessors throw with an actionable message when used before their init step:
- core accessors — after `WebMY.init(...)`
- `WebMY.theme` / `WebMY.router` / `WebMY.onboardingPreferences` — after `WebMY.installUi()`
- `WebMY.billing` / `WebMY.premium` — after `WebMY.initBilling(...)`
- `WebMY.ads` / `WebMY.displayAd` — after `WebMY.initAds(...)`

---

## Navigation

ViewModel calls `Router` synchronously through `navigateTo()`. No flow, no drops, order preserved.

```kotlin
class HomeViewModel : BaseViewModel() {
    fun onSettingsClick() = navigateTo(screen(SettingsKey("42")))
    fun onUrlClick(url: String) = navigateTo(Navigation.Browser(url))
    fun onBack() = navigateTo(Navigation.Back)
}
```

In composables: `WebMY.router.go(Navigation.Back)`.

Available events: `Screen`, `Root`, `Back`, `PopUpTo`, `Browser`, `Email`, `GooglePlay`, `RateApp`, `Finish`, `Sheet`, `DismissSheet`, `Auth.OneTime/Session`.

Screen keys are Navigation 3 `NavKey`s — plain data classes/objects carrying their own arguments; the back stack lives in the `Router` singleton and survives configuration changes (not process death — the app restarts at `startScreen()`).

---

## Billing

```kotlin
class PaywallViewModel : BaseViewModel() {
    private val billing = WebMY.billing

    val products = billing.subscribeProducts()

    fun onBuy(productId: String) {
        viewModelScope.launch {
            when (val out = billing.purchase(productId)) {
                PurchaseOutcome.Success -> navigateTo(Navigation.Back)
                PurchaseOutcome.Pending -> showSnackbar("Pending")
                PurchaseOutcome.Cancelled -> Unit
                is PurchaseOutcome.Failed -> showError(out.error.message)
            }
        }
    }
}
```

`Product.OneTime(consumable = true)` products auto-`consume`; subscriptions + non-consumables auto-`acknowledge`. Both have exponential-backoff retry. Auto-reconnects on billing service disconnect.

Pre-built paywall ViewModels in `us.webmy.core.monetization.billing.paywall`: `BaseOfferPaywallViewModel`, `BasePlanListPaywallViewModel` — subclass them and pass `WebMY.billing` / `WebMY.analytics` (plus your config) to the super constructor.

---

## Ads

```kotlin
class GameViewModel : BaseViewModel() {
    private val ads = WebMY.displayAd

    fun onLevelComplete() = ads.showInterstitial(source = "level_end")
    fun onWatchAd() = ads.showReward(placement = "double_coins") { rewarded ->
        if (rewarded) grantCoins(20)
    }
}
```

Premium check + interstitial throttle are built in. Skip threshold via `InterstitialThrottleConfig` passed to `WebMY.initAds(throttleConfigProvider = { ... })`.

---

## Premium check

```kotlin
class GameViewModel : BaseViewModel() {
    val isPremium: Flow<Boolean> = WebMY.premium.isPremiumFlow

    suspend fun unlockLevel() {
        if (WebMY.premium.isPremium()) doIt() else navigateTo(screen(PaywallKey))
    }
}
```

Checks `purchasedIds` against `premiumProductIds` from `initBilling`. Empty set ⇒ any purchase counts as premium.

---

## Biometrics

```kotlin
class LockViewModel : BaseViewModel() {
    fun unlock() = viewModelScope.launch {
        WebMY.biometrics.performSessionAuthentication() // skipped if authed earlier this session
            .onSuccess { navigateTo(screen(HomeKey)) }
            .onFailure { showError(it.message) }
    }
}
```

Or via Router: `navigateTo(Navigation.Auth.Session(onResult = { ... }))`.

Requires a `FragmentActivity` (`WebmyActivity` qualifies).

---

## Preferences

```kotlin
val prefs = WebMY.preferences

prefs.putString("user_name", "John")
prefs.stringFlow("user_name").collect { ... }
prefs.booleanFlow("dark_mode", defaultValue = false).collect { ... }

prefs.edit {
    putString("a", "1")
    putInt("b", 2)
}
```

---

## Remote config

```kotlin
val msg = WebMY.remoteConfig.getString("welcome_message").getOrElse { "Hello" }
```

Suspend; first call waits for fetch+activate. Enable by passing `remoteConfigUpdateInterval` in `WebMYConfig` — the accessor throws otherwise.

---

## Analytics

```kotlin
WebMY.analytics.logEvent("button_click", mapOf("screen" to "home"))  // Amplitude
WebMY.analytics.logFirebase("level_up", bundleOf("level" to 5))       // Firebase
```

Amplitude only if `amplitudeKey` passed to config. Firebase requires `google-services.json` + plugin.

---

## Network

```kotlin
val api: MyApi = WebMY.network.create("https://api.example.com/")
```

OkHttp client is a singleton (`WebMY.httpClient`). Add interceptors via `NetworkConfig(interceptors = listOf(MyAuth()))`.

---

## Theming

```kotlin
AppTheme {
    WebmyButton(text = "Buy", onClick = {})
    WebmyText("Hello", style = WebmyTheme.typography.bodyM, color = WebmyTheme.colors.textAndIconsPrimary)
    VerticalSpacer { spacing16 }
}
```

Light and dark palettes are built in. Custom themes: subclass `ColorsPalette`, wrap it in a `ThemePalette(id, isDark, palette)` and pass it to `WebMY.installUi(extraPalettes = listOf(...))`. Switch at runtime with `WebMY.theme.select(id)`; observe via `WebMY.theme.theme` (a `StateFlow<ThemeId>`). Selection persists across restarts.

---

## Modules

- **`:core`** — everything above except billing/ads. Single artifact, pulls Compose + Material 3 + Navigation 3 + OkHttp + Retrofit + Firebase + Amplitude.
- **`:core-monetization-billing`** — Billing + Apphud + paywalls only. No ad SDKs, no `AD_ID` permission, no Appodeal/Verve maven repos needed. Opt-in.
- **`:core-monetization-ads`** — Billing + Ads + Apphud + Appodeal mediation adapters (superset of `:core-monetization-billing`). Opt-in.

Init order: `init` → `installUi` → `initBilling` → `initAds`.

---

## Error model

All thrown errors extend `SdkError` (`us.webmy.core.SdkError`):
```
SdkError.NoForegroundActivity
SdkError.NotSupported(reason)
SdkError.BindingMissing(name)
SdkError.Network.{ EmptyBody, HttpError(code, message), Io(cause) }
SdkError.Billing.{ FlowFailed(message), NotAcknowledged(token), Disconnected }
SdkError.Ads.{ LoadFailed(placement), ShowFailed(placement), NotInitialized }
```

`BillingManager.purchase()` returns `PurchaseOutcome.Failed(SdkError.Billing)` instead of throwing.

---

## Migration from 0.x

**Koin is gone.** The SDK no longer starts, loads, or exposes Koin. `KoinMode`, the `extraModules` parameter of `WebMY.init`, and `BaseViewModel : KoinComponent` are removed. Resolve SDK services via `WebMY.<accessor>`; if your app uses Koin, bridge with `single { WebMY.billing }` etc. Custom `ThemePalette`s move from `extraModules` + `named(...)` to `WebMY.installUi(extraPalettes = ...)` (duplicate palette ids now resolve last-wins).

**Packages are flattened.** Old FQN → new FQN:

| 0.x | 1.0 |
|---|---|
| `us.webmy.core.error.SdkError` | `us.webmy.core.SdkError` |
| `us.webmy.core.ui.di.installUi` | `us.webmy.core.installUi` |
| `us.webmy.core.tools.analytics.AnalyticsManager` | `us.webmy.core.analytics.AnalyticsManager` |
| `us.webmy.core.tools.preferences.*`, `us.webmy.core.data.prefs.*` | `us.webmy.core.prefs.*` |
| `us.webmy.core.tools.biometrics.domain.BiometricsService` | `us.webmy.core.biometrics.BiometricsService` |
| `us.webmy.core.tools.remoteconfig.RemoteConfigManager` | `us.webmy.core.remoteconfig.RemoteConfigManager` |
| `us.webmy.core.tools.sharing.*` | `us.webmy.core.sharing.*` |
| `us.webmy.core.data.NetworkApiCreator` | `us.webmy.core.network.NetworkApiCreator` |
| `us.webmy.core.ui.compose.theme.*`, `us.webmy.core.ui.compose.configs.*`, `us.webmy.core.domain.model.*` | `us.webmy.core.theme.*` |
| `us.webmy.core.ui.compose.components.*.*` | `us.webmy.core.components.*` |
| `us.webmy.core.ui.presentation.base.navigator.*` | `us.webmy.core.navigation.*` |
| `us.webmy.core.ui.presentation.base.viewmodel.BaseViewModel` | `us.webmy.core.presentation.BaseViewModel` |
| `us.webmy.core.ui.single.WebmyActivity` | `us.webmy.core.presentation.WebmyActivity` |
| `...billing.tools.billing.*`, `...billing.domain.interactor.*` | `us.webmy.core.monetization.billing.*` |
| `...billing.presentation.paywall.**` | `us.webmy.core.monetization.billing.paywall.*` |
| `...ads.tools.ads.AdsManager`, `...ads.domain.*` | `us.webmy.core.monetization.ads.*` |

Implementation classes (`Real*`, `WebmyRouter`, `SheetController`, biometrics data layer, …) are now `internal` — code that referenced them must switch to the public interfaces/accessors.

---

## Gotchas

- `WebMY.init` is idempotent — second call is a no-op.
- Call `installUi()` after `init()` and before `initBilling/initAds`.
- `initAds` requires `initBilling` first (it needs `PremiumUseCase`) and fails fast with a clear message otherwise.
- `consumableProductIds` must be a subset of `oneTimeProductIds`.
- `RemoteConfigManager` only registered if `remoteConfigUpdateInterval` is non-null.
- Biometrics requires `FragmentActivity` (`WebmyActivity` qualifies).
- Anything under `us.webmy.core.internal.*` (marked `@InternalWebmyApi`) is not API — opting in from app code means accepting breakage without notice.

---

## License

MIT © WebMY
