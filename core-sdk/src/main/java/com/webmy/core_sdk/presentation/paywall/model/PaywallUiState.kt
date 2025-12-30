package com.webmy.core_sdk.presentation.paywall.model

import com.webmy.core_sdk.presentation.adapters.subscriptions.SubscriptionsUiModel

class PaywallUiState(
    val plans: List<SubscriptionsUiModel>,
    val buttonText: String
)