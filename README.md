# WebMY Android SDK

An open-source **Android SDK** to **bootstrap app development** with a lot of pre-integrated modules — all built with **Koin** for dependency injection.

---

## ✨ Features

✅ Firebase Remote Config  
✅ Amplitude Analytics  
✅ Appodeal Ads Integration  
✅ One-Time In-App Purchases (Billing)  
✅ Shared Preferences Helper

🧩 **Coming Soon**
- Firebase Push Notifications
- Firebase Analytics
- Billing for Subscriptions
- Display banners on Jetpack Compose
- Facebook SDK Integration
- Demo project
- Ability to disable ADS (now app crashes without properly configured ads)

---

## Installation

### Step 1 — Add Repositories

Add these repositories to your **root `build.gradle.kts`** or **`settings.gradle.kts`** file:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://artifactory.appodeal.com/appodeal") }
    }
}
```

### Step 2 — Add Dependency

Add this dependency to your **app module's `build.gradle.kts`** file:

```kotlin
dependencies {
    implementation("com.github.WebMY-Studio:core-sdk-android:$versionName")
}
```

> Replace `$versionName` with the latest release from [JitPack](https://jitpack.io/#WebMY-Studio/core-sdk-android).


## Initialization

In your Application class, initialize the SDK as follows:

```kotlin
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = Config.Builder(this)
            .setKoinMode(KoinMode.START) // or KoinMode.LOAD
            .enableAnalytics("your_amplitude_api_key")
            .enableAds("your_appodeal_app_key")
            .enableBilling(listOf("product_id_1", "product_id_2"))
            .enableRemoteConfig()
            .build()

        WebMY.INSTANCE.init(config)
    }
}
```

## Dependency Injection (Koin Integration)

The SDK uses Koin for dependency injection.
Select the proper KoinMode for your app:

```kotlin
enum class KoinMode {
    /**
     * Use this if you use another DI or none at all.
     */
    START,

    /**
     * Use this if Koin is already started in your app.
     */
    LOAD
}
```

# 📊 Analytics (Amplitude)

To enable **Amplitude Analytics** integration, add your Amplitude API key during SDK configuration:

```kotlin
enableAnalytics("your_amplitude_api_key")
```

By default, the SDK uses the **EU server zone**.  
You can use this integration to log events, track user behavior, and analyze engagement metrics across your app.

```kotlin
val analyticsManager by inject<AnalyticsManager>()
analyticsManager.logEvent("eventName", mapOf("propertyKey" to "propertyValue"))
```

---

# 💰 Billing (One-Time Purchases)

To enable **in-app one-time purchases**, add your product IDs for Google Play Console when initializing the SDK:

```kotlin
enableBilling(listOf("product_id_1", "product_id_2"))
```

The SDK handles Google Play Billing integration for simple purchase flows.
> ⚠️ Subscription billing is not yet supported — it’s **coming soon**.

To access billing functionality after initialization:

```kotlin
val billingManager by inject<BillingManager>()
billingManager.products // returns Flow of List of Product with basic info
billingManager.fetchProducts() // method to fetch products from Google Play manually. Also called in init {} block
billingManager.purchase(activity, "product_id") // method to start purchase flow
```

---

# 🔧 Firebase Integration

To integrate Firebase features like **Remote Config** and **Crashlytics**, follow these steps:

### 1️⃣ Add the `google-services.json` file
Place your `google-services.json` file in the **app module** directory.

### 2️⃣ Apply Firebase plugins
In your **app module’s** `build.gradle.kts`:

```kotlin
plugins {
alias(libs.plugins.google.services)
alias(libs.plugins.firebase.crashlytics)
}
```

In your **root** `build.gradle.kts`:

```kotlin
plugins {
alias(libs.plugins.google.services) apply false
alias(libs.plugins.firebase.crashlytics) apply false
}
```

### 3️⃣ Enable Remote Config in SDK
Enable Firebase Remote Config in your configuration:

```kotlin
enableRemoteConfig()
```
> by default remote config update interval is set to 0 but you can define a custom interval

After initialization, you can access the `RemoteConfigManager` via Koin:

```kotlin
val remoteConfigManager by inject<RemoteConfigManager>()
```
then to fetch value use

```kotlin
remoteConfigManager.getValue() // Not recommended
```
or
```kotlin
remoteConfigManager.getSyncedValue()
```

---

# Ads Integration (Appodeal)

The SDK provides built-in **Appodeal Ads** integration for banner, interstitial, and rewarded ads.

### Step 1 — Add Manifest Placeholders

**Kotlin DSL**
```kotlin
manifestPlaceholders["ADMOB_APPLICATION_ID"] = localProperties.readSecret("ADMOB_APPLICATION_ID")
```

**Groovy DSL**
```groovy
manifestPlaceholders = [ADMOB_APPLICATION_ID: readRawSecret("ADMOB_APPLICATION_ID")]
```

### Step 2 — Add Your AdMob App ID

In your `local.properties` file:

```
ADMOB_APPLICATION_ID=ca-app-pub-XXXXXXXX~YYYYYYYY
```

### Step 3 — Enable Ads in Config

```kotlin
enableAds("your_appodeal_app_key")
```

Then access the Ads Manager instance:

```kotlin
val adsManager by inject<AdsManager>()
```

## 🏳️ Banner Ad

Displays a banner in the given container.

```kotlin
adsManager.showBanner(activity, bannerContainer)
```

- `activity`: current `Activity`
- `bannerContainer`: `FrameLayout` in layout
- Returns `true` if shown

---

## 🎁 Rewarded Ad

Shows a rewarded ad and returns a result in callback.

```kotlin
adsManager.showReward(activity, "coins_reward") { rewarded ->
if (rewarded) {
    // give user 1 ChocoPie or zabka hot-dog
}
}
```

- `placement`: optional tag
- `rewarded`: `true` if user earned reward

---

## 🚪 Interstitial Ad

Displays an interstitial ad.

```kotlin
adsManager.showInter(activity)
```

Returns `true` if shown, `false` otherwise.


> 💡 Ensure that you already added appodeal Maven Repo:
> ```kotlin
> maven { url = uri("https://artifactory.appodeal.com/appodeal") }
> ```

---

# ⚙️ Preferences

Use the `Preferences` interface to store and observe local data (a wrapper around `SharedPreferences`).

Inject with Koin:

```kotlin
val preferences by inject<Preferences>()
```

Basic usage:

```kotlin
preferences.putString("user_name", "John")
val name = preferences.getString("user_name")

preferences.putBoolean("isLoggedIn", true)
val logged = preferences.getBoolean("isLoggedIn", false)
```

You can also observe values as **Flows**:

```kotlin
preferences.stringFlow("user_name")
```

Supports `String`, `Boolean`, `Int`, `Long`, and `Set<String>` types — plus reactive flows for each.

# 🧾 License

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2025 WebMY

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

# 💬 Contributing

Contributions are welcome! 🎉  
If you’d like to extend the SDK with additional modules (like **Firebase Push** or **Billing Subscriptions**), please:

1. Fork the repository
2. Create a feature branch (`feature/new-module`)
3. Submit a Pull Request

Be sure to follow existing code style and documentation guidelines.

---

# 👨‍💻 Author

**WebMY SDK**  
_Accelerate your Android development with a unified, modular, and DI-friendly SDK._