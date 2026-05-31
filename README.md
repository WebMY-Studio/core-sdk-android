# WebMY Core SDK

Android SDK for shipping apps fast. One init call wires up DI, analytics, remote config, billing, ads, biometrics, navigation, theming. Compose-first, Koin-based.

> 🤖 **Building with an AI agent?** Point it at [`AGENTS.md`](AGENTS.md) — full API reference written for LLMs.

---

## What's inside

| Concern | API |
|---|---|
| Single-activity host | `WebmyActivity` + `BaseFragment` / `BaseComposeFragment` |
| Navigation | `Router` + `Navigation` sealed events |
| DI | Koin (start it or load alongside yours) |
| Analytics | `AnalyticsManager` — Amplitude + Firebase |
| Remote config | `RemoteConfigManager` — Firebase RC with suspend getters |
| Preferences | `Preferences` — SharedPreferences wrapper with Flows |
| Biometrics | `BiometricsService` — BiometricPrompt with session caching |
| Sharing | `SharingManager` — files, text, calendar events |
| Network | `NetworkApiCreator` — Retrofit + OkHttp factory |
| Billing *(opt)* | `BillingManager` + `PremiumUseCase` — Play Billing, subs + one-time + consumables |
| Ads *(opt)* | `DisplayAdUseCase` — Appodeal with premium gating + interstitial throttling |
| Theming | `AppTheme` + `WebmyTheme.colors/typography/spacings` + pre-built composables |

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
    implementation("com.github.WebMY-Studio:core:<version>")
    implementation("com.github.WebMY-Studio:core-monetization:<version>") // optional
}
```

Latest version: [JitPack](https://jitpack.io/#WebMY-Studio/core-sdk-android).

---

## Quick start

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        WebMY.init(
            config = WebMYConfig(
                application = this,
                koinMode = KoinMode.START,
                amplitudeKey = BuildConfig.AMPLITUDE_KEY,           // optional
                remoteConfigUpdateInterval = 1.hours,               // optional
                network = NetworkConfig(enableHttpLogging = BuildConfig.DEBUG),
            ),
            extraModules = listOf(appModule),                       // your Koin modules
        )
        WebMY.installUi()

        // Optional add-ons
        WebMY.initBilling(
            subscriptionProductIds = setOf("yearly_premium"),
            premiumProductIds = setOf("yearly_premium"),
        )
        WebMY.initAds(BuildConfig.APPODEAL_KEY)
    }
}

class MainActivity : WebmyActivity() {
    override fun createStartFragment() = HomeFragment()
}
```

That's it. Inject anything below via Koin (`by inject()` / `koinInject()`).

---

## Navigation

ViewModel calls `Router` synchronously through `navigateTo()`. No flow, no drops, order preserved.

```kotlin
class HomeViewModel : BaseViewModel() {
    fun onSettingsClick() = navigateTo(screen<SettingsFragment>(SettingsArgs("42")))
    fun onUrlClick(url: String) = navigateTo(Navigation.Browser(url))
    fun onBack() = navigateTo(Navigation.Back)
}
```

Available events: `Screen`, `Back`, `PopUpTo`, `Browser`, `Email`, `GooglePlay`, `RateApp`, `Finish`, `Sheet`, `DismissSheet`, `Auth.OneTime/Session`.

Fragment args via Parcelable:
```kotlin
@Parcelize
data class SettingsArgs(val userId: String) : Parcelable

// in fragment
val args: SettingsArgs by requireArgs()
```

---

## Fragments

**Compose:**
```kotlin
class HomeFragment : BaseComposeFragment() {
    @Composable override fun ScreenContent() {
        val vm: HomeViewModel = koinViewModel()
        // ...
    }
}
```

**XML + ViewBinding:**
```kotlin
class SettingsFragment : BaseFragment<SettingsVM, FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate,
) {
    override val viewModel: SettingsVM by viewModel()
    override fun initView() { /* binding.* setup */ }
    override fun observe(viewModel: SettingsVM) { /* flows */ }
}
```

---

## Billing

```kotlin
class PaywallViewModel(
    private val billing: BillingManager,
) : BaseViewModel() {
    val products = billing.subscribeProducts()

    fun onBuy(productId: String) {
        viewModelScope.launch {
            when (val out = billing.purchase(productId)) {
                PurchaseOutcome.Success -> navigateTo(Navigation.Finish)
                PurchaseOutcome.Pending -> showSnackbar("Pending")
                PurchaseOutcome.Cancelled -> Unit
                is PurchaseOutcome.Failed -> showError(out.error.message)
            }
        }
    }
}
```

`Product.OneTime(consumable = true)` products auto-`consume`; subscriptions + non-consumables auto-`acknowledge`. Both have exponential-backoff retry. Auto-reconnects on billing service disconnect.

Pre-built paywall ViewModels: `BaseOfferPaywallViewModel`, `BasePlanListPaywallViewModel`.

---

## Ads

```kotlin
class GameViewModel(
    private val ads: DisplayAdUseCase,
) : BaseViewModel() {
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
class GameViewModel(private val premium: PremiumUseCase) : BaseViewModel() {
    val isPremium: Flow<Boolean> = premium.isPremiumFlow

    suspend fun unlockLevel() {
        if (premium.isPremium()) doIt() else navigateTo(screen<PaywallFragment>())
    }
}
```

Checks `purchasedIds` against `premiumProductIds` from `initBilling`. Empty set ⇒ any purchase counts as premium.

---

## Biometrics

```kotlin
class LockViewModel(private val bio: BiometricsService) : BaseViewModel() {
    fun unlock() = viewModelScope.launch {
        bio.performSessionAuthentication()       // skipped if authed earlier this session
            .onSuccess { navigateTo(screen<HomeFragment>()) }
            .onFailure { showError(it.message) }
    }
}
```

Or via Router: `navigateTo(Navigation.Auth.Session(onResult = { ... }))`.

Requires `WebmyActivity` (or any `FragmentActivity`).

---

## Preferences

```kotlin
val prefs: Preferences by inject()

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
val rc: RemoteConfigManager by inject()
val msg = rc.getString("welcome_message").getOrElse { "Hello" }
```

Suspend; first call waits for fetch+activate. Enable by passing `remoteConfigUpdateInterval` in `WebMYConfig`.

---

## Analytics

```kotlin
val a: AnalyticsManager by inject()
a.logEvent("button_click", mapOf("screen" to "home"))   // Amplitude
a.logFirebase("level_up", bundleOf("level" to 5))        // Firebase
```

Amplitude only if `amplitudeKey` passed to config. Firebase requires `google-services.json` + plugin.

---

## Network

```kotlin
val creator: NetworkApiCreator by inject()
val api: MyApi = creator.create("https://api.example.com/")
```

OkHttp client is a singleton. Add interceptors via `NetworkConfig(interceptors = listOf(MyAuth()))`.

---

## Theming

```kotlin
AppTheme {
    WebmyButton(text = "Buy", onClick = {})
    WebmyText("Hello", style = WebmyTheme.typography.bodyM, color = WebmyTheme.colors.textAndIconsPrimary)
    Spacers.Vertical(WebmyTheme.spacings.m)
}
```

Override palette via `CompositionLocalProvider(LocalColorsPalette provides MyPalette)`.

---

## Modules

- **`:core`** — everything above except billing/ads. Single artifact, pulls Compose + Material + Koin + OkHttp + Firebase + Amplitude.
- **`:core-monetization`** — Billing + Ads + Adapty + Appodeal mediation adapters. Opt-in.

---

## Error model

All thrown errors extend `SdkError`:
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

## Gotchas

- `WebMY.init` is idempotent — second call is a no-op.
- Call `installUi()` after `init()` and before `initBilling/initAds`.
- `initAds` requires `initBilling` first (it needs `PremiumUseCase`).
- `consumableProductIds` must be a subset of `oneTimeProductIds`.
- `RemoteConfigManager` only registered if `remoteConfigUpdateInterval` is non-null.
- Biometrics requires `FragmentActivity` (`WebmyActivity` qualifies).

---

## License

MIT © WebMY
