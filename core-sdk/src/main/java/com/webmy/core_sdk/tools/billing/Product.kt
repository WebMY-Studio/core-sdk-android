package com.webmy.core_sdk.tools.billing

sealed class Product(
    open val id: String,
    open val formattedPrice: String?,
    open val isPurchased: Boolean,

    open val offerToken: String?,
) {
    data class OneTime(
        override val id: String,
        override val formattedPrice: String?,
        override val isPurchased: Boolean,
        override val offerToken: String?,
    ) : Product(id, formattedPrice, isPurchased, offerToken)

    data class Subscription(
        override val id: String,
        override val formattedPrice: String?,
        override val isPurchased: Boolean,
        override val offerToken: String?,
    ) : Product(id, formattedPrice, isPurchased, offerToken)
}

fun List<Product>.containsPurchased(productId: String) =
    find { it.id == productId }?.isPurchased ?: false

