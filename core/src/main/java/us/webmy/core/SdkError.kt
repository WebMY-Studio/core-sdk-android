package us.webmy.core

sealed class SdkError(message: String, cause: Throwable? = null) : Throwable(message, cause) {

    class NotImplemented(feature: String) : SdkError("Not implemented: $feature")

    class NoForegroundActivity : SdkError("No foreground Activity available")

    class NoHostActivity : SdkError("No Host Activity available")

    class NotSupported(reason: String) : SdkError("Not supported: $reason")

    sealed class Network(message: String, cause: Throwable? = null) : SdkError(message, cause) {
        class EmptyBody : Network("HTTP response body is empty")
        class HttpError(val code: Int, message: String) : Network("HTTP $code: $message")
        class Io(cause: Throwable) : Network("Network IO error", cause)
    }

    sealed class Billing(message: String, cause: Throwable? = null) : SdkError(message, cause) {
        class NotAcknowledged(val purchaseToken: String) : Billing("Purchase not acknowledged: $purchaseToken")
        class FlowFailed(message: String) : Billing(message)
        class Disconnected : Billing("Billing client disconnected")
    }

    sealed class Ads(message: String) : SdkError(message) {
        class LoadFailed(val placement: String?) : Ads("Ad failed to load${placement?.let { " at $it" } ?: ""}")
        class ShowFailed(val placement: String?) : Ads("Ad failed to show${placement?.let { " at $it" } ?: ""}")
        class NotInitialized : Ads("Ads SDK not initialized")
    }

    class BindingMissing(name: String) : SdkError("Required binding missing: $name")
}
