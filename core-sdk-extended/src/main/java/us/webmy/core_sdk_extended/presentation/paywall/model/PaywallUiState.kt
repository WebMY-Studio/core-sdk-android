package us.webmy.core_sdk_extended.presentation.paywall.model

import com.webmy.core_sdk.presentation.adapters.subscriptions.SubscriptionsUiModel

class PaywallUiState(
    val plans: List<SubscriptionsUiModel>,
    val buttonText: String
)