package com.webmy.core_sdk.tools.billing

sealed class Product(
    open val id: String,
    open val isPurchased: Boolean,
    open val title: String,
    open val offerToken: String?,
) {
    data class OneTime(
        override val id: String,
        override val isPurchased: Boolean,
        override val offerToken: String?,
        override val title: String,
        val formattedPrice: String?,
    ) : Product(id, isPurchased, title, offerToken)

    data class Subscription(
        override val id: String,
        override val isPurchased: Boolean,
        override val offerToken: String?,
        override val title: String,
        val phases: List<Phase>
    ) : Product(id, isPurchased, title, offerToken) {
        data class Phase(
            val formattedPrice: String,
            val billingPeriod: String,
            val priceMicros: Long,
            val currency: String,
            val cycles: Int
        )
    }
}

fun List<Product>.containsPurchased(productId: String) =
    find { it.id == productId }?.isPurchased ?: false

