# WebMY Core SDK — Полное ревью

**Дата:** 2026-05-30
**Версия SDK:** 0.5.0
**Branch:** feature/huge-refactoring-and-ready-to-prod
**Общая оценка:** 5/10

Каркас разумный — Koin модули, `Navigator` фасад, разделение `core` / `core-ui` / `core-monetization`.
Но архитектурных проблем много: моно-модуль монетизации с 100+ ad adapter'ами, мутабельный singleton `WebMY`,
навигатор с самоубийственным `asyncScope.cancel()`, баг в `BaseOfferPaywallViewModel`, мёртвые биндинги,
ноль тестов, README не соответствует коду.

---

## Содержание

- [🔴 Критичные баги (B1–B10)](#-критичные-баги)
- [🟠 Архитектурные проблемы (A1–A15)](#-архитектурные-проблемы)
- [🟡 Code quality (Q1–Q20)](#-code-quality)
- [🟢 Что хорошо](#-что-хорошо)
- [План улучшений (8 спринтов)](#план-улучшений)
- [Метрики качества](#метрики-качества)

---

## 🔴 Критичные баги

### B3. `RemoteConfigManager` подвисает навсегда пр
и ошибке

**Файл:** `core/src/main/java/us/webmy/core/tools/remoteconfig/RemoteConfigManager.kt:48`

```kotlin
remoteConfig.fetchAndActivate()
    .executeSuspend()
    .onSuccess { isSynced.value = true }
```

При ошибке `isSynced` навсегда `false` → `awaitTrue()` блокирует все `getString/getBoolean/...` бесконечно.
Нет таймаута, нет fallback на cached value.

**Fix:** emit terminal Failed state, expose error, поддержать fallback на cached.

---

### B4. `HttpLoggingInterceptor` зависит от `BuildConfig.DEBUG` библиотеки

**Файл:** `core/src/main/java/us/webmy/core/di/SDKModule.kt:117`

```kotlin
if (BuildConfig.DEBUG) {
    builder.addInterceptor(HttpLoggingInterceptor().setLevel(BODY))
}
```

В опубликованном артефакте `BuildConfig.DEBUG` ВСЕГДА `false`. Consumer не сможет включить логи без пересборки SDK.

**Fix:** `NetworkConfig.enableHttpLogging: Boolean` параметр в публичном API.

---

### B5. `BillingManager` не consume() одноразовые расходники

**Файл:** `core-monetization/src/main/java/us/webmy/core/monetization/billing/tools/billing/BillingManager.kt`

Только `acknowledgePurchase`, нет `consumeAsync`. Расходные продукты (coins, hints) после первой покупки
навсегда `isPurchased=true`, повторная покупка `canBePurchased=false`.

**Fix:** `Product.OneTime(consumable: Boolean = false)` + consume flow для consumable.

---

### B6. `BillingManager` не переподключается

**Файл:** `core-monetization/src/main/java/us/webmy/core/monetization/billing/tools/billing/BillingManager.kt:187`

```kotlin
override fun onBillingServiceDisconnected() {
    Log.w(TAG, "billing service disconnected")
    // Don't fail init — `purchase()` will surface failure later.
}
```

После разрыва `purchase()` будет молча падать. Стандартная практика — exponential backoff reconnect.

**Fix:** retry с backoff + re-fetch products после reconnect.

---

### B7. `AdsHandler` игнорирует `grantWhenPremium`

**Файл:** `core-monetization/src/main/java/us/webmy/core/monetization/ads/navigator/AdsHandler.kt:13`

```kotlin
is Navigation.Ad.Reward -> adsManager.showReward(
    source = ad.source,
    placement = ad.placement,
    rewardCallback = ad.onResult,
)
```

Маршрут через Navigator обходит premium-проверку `AdsPremiumManager`. Премиум-пользователи смотрят rewarded ads.

**Fix:** AdsHandler должен использовать `AdsPremiumManager` или дублировать premium-check.

---

### B8. README врёт про API

**Файл:** `README.md`

Несоответствия с актуальным кодом:

| README | Реальность |
|--------|-----------|
| `Config.Builder(this).setKoinMode(...)` | `WebMYConfig(application, koinMode, ...)` data class |
| `WebMY.INSTANCE.init(config)` | `WebMY.init(config)` — нет `INSTANCE` поля у `object` |
| `enableAnalytics("key")` | `WebMYConfig(amplitudeKey = "key")` |
| `enableAds("key")` | `WebMY.initAds("key")` extension |
| `enableBilling(list)` | `WebMY.initBilling(...)` extension |
| `enableRemoteConfig()` | `WebMYConfig(remoteConfigUpdateInterval = ...)` |
| `BillingManager.fetchProducts()` | нет такого метода |
| `BillingManager.purchase(activity, id)` | `purchase(id)` — Activity из ActivityProvider |
| `RemoteConfigManager.getSyncedValue()` | `getString/getBoolean/getLong/getDouble` |
| "Subscription billing — coming soon" | уже поддержано (`Product.Subscription`) |

**Fix:** переписать README с нуля.

---

### B9. `RealAnalyticsManager.firebase` объявлен nullable, но в DI всегда non-null

**Файл:** `core/src/main/java/us/webmy/core/tools/analytics/AnalyticsManager.kt:15`

```kotlin
internal class RealAnalyticsManager(
    private val amplitude: Amplitude?,
    private val firebase: FirebaseAnalytics?
)
```

В SDKModule.kt:81 — `firebase = get<FirebaseAnalytics>()`. Тип nullable — мёртвая ветка `firebase?.logEvent`.

**Fix:** убрать nullable, либо честно поддержать отключение Firebase.

---

### B10. `PushAndTag.kt` делает `git add .`

**Файл:** `buildSrc/src/main/kotlin/PushAndTag.kt:27`

```kotlin
runCommand("git", "add", ".")
```

Закоммитит секреты, временные файлы, `local.properties` если он не в `.gitignore`.

**Fix:** `git add -u` для уже tracked файлов, либо явный whitelist путей.

---

## 🟠 Архитектурные проблемы

### A1. Монолит `:core-monetization` (~120 строк зависимостей)

**Файл:** `core-monetization/build.gradle.kts:33-143`

Тянет Billing + Adapty + Facebook + AppodealAds + ВСЕ ad network adapters:
Amazon, Pangle, Mintegral, Bytedance, Vungle, IronSource, Unity, Yandex, VK, MyTarget, Ogury, Smaato, и т.д.

Consumer которому нужен только Billing получает ~50 MB APK bloat.

**Split на:**
- `:billing` — Google Play Billing + paywall ViewModels
- `:billing-adapty` — Adapty integration
- `:ads-core` — `AdsManager` интерфейс + `AdsPremiumManager`
- `:ads-appodeal` — Appodeal impl + adapters
- `:ads-admob` — отдельная альтернатива

---

### A2. `WebMY` — mutable singleton с `lateinit var application`

**Файл:** `core/src/main/java/us/webmy/core/WebMY.kt:11`

```kotlin
object WebMY {
    lateinit var application: Application
        private set

    fun init(config: WebMYConfig, extraModules: List<Module> = emptyList()) {
        application = config.application
        ...
    }
}
```

Проблемы:
- Повторный `init()` молча перезаписывает. Нет idempotency guard.
- Тесты не могут переинициализировать чисто.
- `application` mutable через `init()`.

**Fix:** `internal` field, guard на повторный init с warning.

---

### A3. `coreUiModule()` — extraModule, не extension на WebMY

**Файл:** `core-ui/src/main/java/us/webmy/core/ui/di/CoreUiModule.kt:14`

Inconsistent: `WebMY.initAds`, `WebMY.initBilling`, `WebMY.initAdapty` — extensions.
`coreUi` — отдельная функция, передаваемая в `extraModules`.

**Fix:** единый стиль `WebMY.installUi()` extension.

---

### A4. Optional Koin binding через `getOrNull`

**Файл:** `core-ui/src/main/java/us/webmy/core/ui/di/CoreUiModule.kt:21`

```kotlin
purchaseHandler = getOrNull<PurchaseNavigationHandler>(),
adHandler = getOrNull<AdNavigationHandler>(),
```

Зависит от порядка `loadKoinModules`. Если `coreUiModule()` загружен ДО `billingModule`,
handler навсегда `null` (single уже создан с null'ами).

**Fix:** lazy resolution через `inject<>` внутри Navigator.

---

### A5. `Result<T>` (kotlin.Result) в публичном API

Kotlin документация явно говорит: `Result` не предназначен для return type публичного API.
В SDK везде. Компилятор обычно ругается.

**Fix:** sealed class `Outcome<T, E: SdkError>` либо throws + suspend.

---

### A6. `Preferences` over-engineered обвязка над SharedPreferences

**Файл:** `core/src/main/java/us/webmy/core/tools/preferences/Preferences.kt`

Проблемы:
- 9 типов put/get + 6 Flow методов + Editor с другим Editor → дублирование Android API
- `keyFlow/keysFlow` дизайн запутанный: emits `List<String>`, потом `map { it.first() }`
- `RealPreferences.listeners: mutableSetOf<>` — race не-thread-safe и **никогда не читается**, dead state
- DataStore — стандарт 2026, SharedPreferences deprecated по духу. Нужна абстракция для swap impl.

---

### A7. `Navigator` смешивает routing + side-effects + business logic

`Navigation.Sheet`, `Auth`, `Ad`, `Purchase`, `RateApp` — каждый растягивает Navigator.
Если завтра Push subscription, AppLink — Navigator пухнет.

**Fix:** Navigator только routing, остальное — отдельные handler bus'ы или прямые managers.

---

### A8. `BaseViewModel` `SharedFlow(replay=0)` теряет events

**Файл:** `core-ui/src/main/java/us/webmy/core/ui/presentation/base/viewmodel/BaseViewModel.kt:15`

```kotlin
private val _navigation = MutableSharedFlow<Navigation>()

fun navigateTo(navigation: Navigation) {
    viewModelScope.launch { _navigation.emit(navigation) }
}
```

Если view не подписана в момент emit, событие теряется. При configuration change теряется.

**Fix:** `Channel<Navigation>(Channel.BUFFERED)` либо `SharedFlow(replay=1)` с consume-on-collect.

---

### A9. `BillingManager.purchase()` — Result<Unit> сразу, но success ≠ purchased

Возвращает success при ЗАПУСКЕ billing flow. Реальный результат — через `subscribeProducts()`.
Caller не понимает что произошло.

**Fix:** `suspend fun purchase(): PurchaseOutcome.Success/Cancelled/Pending/Failed`.

---

### A10. `PremiumInteractor.isPremium` только subscriptions

**Файл:** `core-monetization/src/main/java/us/webmy/core/monetization/billing/domain/interactor/PremiumInteractor.kt:28`

```kotlin
override val isPremiumFlow = subscriptionsFlow
    .map { products -> products.any { it.isPurchased } }
```

`AdsPremiumManager.isPremiumFlow` смотрит на все product types. Inconsistent — два источника истины.

---

### A11. `RealBiometricsService` Mutex впустую

Factory создаёт новый service на каждом `inject()` → новый Mutex.
Защиты от concurrent calls между разными inject'ами нет.

**Fix:** singleton service либо session-scoped Mutex.

---

### A12. `:core-ui` смешивает XML + Compose

`BaseFragment<VM, VB>` (ViewBinding), `AppButton` (AppCompatTextView), `BaseBottomSheetFragment` (Material XML)
рядом с `BaseComposeFragment`, `WebmyText`, `WebmyButton`.

Consumer на чистом Compose тянет AppCompat + Material XML.

**Split:** `:core-ui-compose` + `:core-ui-legacy-xml`.

---

### A13. `OnboardingShownPreferences` — слишком конкретно для core

Single boolean spec в core модуле. Должно быть в core-ui или вообще убрано (consumer сам хранит).

---

### A14. `ActivityProvider` хранит ссылку на Activity глобально

Race окей (`compareAndSet`), но managers вызывают methods ожидая foreground Activity.
Если manager вызван из background coroutine когда Activity нет → exception / silent skip.

**Fix:** async API возвращающий awaiting current Activity.

---

### A15. `RealBillingManager` — God object 328 строк

**Файл:** `core-monetization/src/main/java/us/webmy/core/monetization/billing/tools/billing/BillingManager.kt`

Один класс делает: BillingClient wrapper + cache + retry + flow source + product builder.

**Split:** `BillingClientWrapper`, `ProductsRepository`, `PurchaseAcknowledger`, `BillingManagerFacade`.

---

## 🟡 Code quality

| ID | Файл / место | Проблема |
|----|--------------|----------|
| Q1 | `NetworkApiCreator.create` | каждый вызов создаёт новый Retrofit — нет переиспользования |
| Q2 | `SDKModule.kt:128` | `single<Gson> { Gson() }` зарегистрирован но не используется |
| Q3 | `SDKModule.kt:106-124` | `OkHttpClient.Builder` и `OkHttpClient` оба single — странно |
| Q4 | `NetworkApiCreator.kt:6` | `import kotlin.jvm.java` — IDE-сгенерированный мусор |
| Q5 | `SharingManager.kt:21` | требует `AppCompatActivity`, хотя достаточно `Activity` |
| Q6 | `RemoteConfigManager.dispose()` | кто его зовёт? Singleton живёт всю app |
| Q7 | `Flows.kt:55` `currentTimestampFlow` | infinite `while(true)` — OK через cooperative cancellation |
| Q8 | `Versions.kt:7` | `versionCode = currentTimeMillis()/1000` — поломает Play Store update |
| Q9 | `Result.kt:18` | `failure(message) = Throwable(message)` — лучше `SdkError` подтип |
| Q10 | `KoinMode.LOAD` | нет проверки что `startKoin{}` уже вызван |
| Q11 | `AdsManager.kt` | magic strings "ad_load_failed", "type" to "inter" — extract в const |
| Q12 | `BillingManager.kt` 328 строк | God object, split |
| Q13 | весь SDK | ноль тестов — `src/test/` и `src/androidTest/` пусты |
| Q14 | все модули | нет ProGuard consumer rules — minify сломает |
| Q15 | `gradle.properties:26` | `r8.strictFullModeForKeepRules=false` скрывает R8 ошибки |
| Q16 | `gradle.properties` | нет `org.gradle.parallel/caching=true` — медленные builds |
| Q17 | `AdsManager.init()` | `Appodeal.initialize` на main thread — ANR risk |
| Q18 | `core/src/main/res/values-*/` | 10 локализаций — что там переведено? Возможно мёртвые ресурсы |
| Q19 | `BasePaywallViewModel.init {}` | side effects в init родителя — subclass не может отменить |
| Q20 | `WebMYExtended.initAdapty` | активирует Adapty но не регистрирует ничего в Koin |

---

## 🟢 Что хорошо

- Koin модули разделены, есть `START/LOAD` mode для co-existence с consumer DI
- `SdkError` sealed hierarchy
- `ActivityProvider` через `ActivityLifecycleCallbacks` — стандарт
- `AdsPremiumManager` идея с throttle interstitial — норм
- `WebmyActivity` single-activity host с sheet overlay — чисто
- `BiometricsServiceFactory` factory pattern
- BOM для Firebase / Compose / Koin
- Version catalog (`libs.versions.toml`)

---

## План улучшений

### Sprint 1 — Critical bugfixes (1 неделя)
1. **B1**: фикс `BaseOfferPaywallViewModel.phaseFor` — использовать параметр `planId`
2. **B2**: пересоздавать scope в Navigator или `cancelChildren()` вместо `cancel()`
3. **B3**: terminal state Failed для RemoteConfig + timeout + fallback на cached
4. **B4**: `NetworkConfig.enableHttpLogging: Boolean` параметр
5. **B7**: AdsHandler учитывает `grantWhenPremium`
6. **B8**: переписать README под актуальный API
7. **B9**: убрать nullable у `firebase` в `RealAnalyticsManager`
8. **B10**: `PushAndTag` — `git add -u` или whitelist
9. Тесты под все B1-B7 фиксы

### Sprint 2 — Module split (2 недели)
1. `:core-monetization` → `:billing`, `:billing-adapty`, `:ads-core`, `:ads-appodeal`
2. `:core-ui` → `:core-ui-compose` + `:core-ui-legacy` (опционально)
3. Каждый модуль публикуется отдельным Maven artifact
4. **Backwards compat:** оставить старые API как `@Deprecated typealias` → minor version, не major.

### Sprint 3 — Billing рефакторинг (1 неделя)
1. **B5**: `Product.OneTime.consumable` + consume flow
2. **B6**: reconnect logic с exponential backoff
3. Split `RealBillingManager` 328 строк → 4 класса (Client/Repo/Ack/Facade)
4. `purchase()` → suspend, возвращает `PurchaseOutcome`
5. `PremiumInteractor` — одна логика premium через `BillingManager` + product type filter

### Sprint 4 — Public API polish (1 неделя)
1. Заменить `kotlin.Result` на собственный sealed `Outcome<T, E: SdkError>`
2. `WebMY.init` — idempotent (warning при повторном init)
3. `WebMY.installUi()` extension — единый стиль с `initAds/initBilling`
4. Lazy resolution navigation handlers (убрать `getOrNull`)
5. Убрать AppCompat зависимость из `SharingManager`
6. Удалить dead code: `Gson` binding, `RealPreferences.listeners`, `kotlin.jvm.java` import, `dispose()` на RemoteConfigManager

### Sprint 5 — Build hygiene (3 дня)
1. ProGuard consumer rules в каждом модуле
2. `r8.strictFullModeForKeepRules=true` + fix findings
3. `org.gradle.parallel/caching=true`
4. Typesafe project accessors
5. Lint baseline + CI gate
6. Detekt + ktlint

### Sprint 6 — Test coverage (2 недели)
1. JUnit5 + MockK setup для core
2. Тесты Billing: connect, fetchProducts, ack retry, premium flag
3. Тесты Navigator: каждый case
4. Тесты RemoteConfigManager: success / failure / timeout
5. Тесты Preferences flows
6. Robolectric для Application-bound юнитов
7. CI: GitHub Actions → unit tests + JitPack publish on tag

### Sprint 7 — DataStore + Modern stack (2 недели)
1. Альтернативный `DataStorePreferences` impl
2. `Preferences` урезать до 4 типов + putBoolean/getBoolean — остальное extension
3. Compose Navigation вместо FragmentManager (опционально, под флагом)
4. Migration guide

### Sprint 8 — DevX (1 неделя)
1. KDoc на каждый public symbol
2. Sample проекты: minimal, billing-only, full
3. Dokka docs → GitHub Pages
4. Migration guides 0.5 → 0.6

---

## Метрики качества

| Метрика | Сейчас | Цель |
|---------|--------|------|
| Test coverage | 0% | ≥70% core, ≥50% UI |
| `:billing` APK size impact | ~50 MB | <2 MB |
| `:ads-appodeal` opt-in | нет | да |
| Public API surface | mutable singleton | immutable facade |
| Dead bindings | 3+ | 0 |
| README accuracy | ~30% | 100% |
| ProGuard consumer rules | 0 | full |

---

**Подготовлено:** SDK review session, 2026-05-30.
