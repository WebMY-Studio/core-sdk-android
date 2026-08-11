package us.webmy.core.monetization.billing

import us.webmy.core.SdkError

sealed interface PurchaseOutcome {
    data object Success : PurchaseOutcome
    data object Cancelled : PurchaseOutcome
    data object Pending : PurchaseOutcome
    data class Failed(val error: SdkError.Billing) : PurchaseOutcome
}
