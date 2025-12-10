package com.webmy.core_sdk.presentation.adapters.subscriptions

class SubscriptionsUiModel(
    val productId: String,
    val title: String,
    val freeFormatted: String?,
    val formattedPrice: String,
    val formattedPriceWeek: String,
    val isSelected: Boolean
)