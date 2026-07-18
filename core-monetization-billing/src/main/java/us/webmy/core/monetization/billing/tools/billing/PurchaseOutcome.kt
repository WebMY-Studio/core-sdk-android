package us.webmy.core.monetization.billing.tools.billing

import us.webmy.core.error.SdkError

sealed interface PurchaseOutcome {
    data object Success : PurchaseOutcome
    data object Cancelled : PurchaseOutcome
    data object Pending : PurchaseOutcome
    data class Failed(val error: SdkError.Billing) : PurchaseOutcome
}
