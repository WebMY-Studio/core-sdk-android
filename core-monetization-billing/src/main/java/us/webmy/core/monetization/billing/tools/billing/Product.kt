package us.webmy.core.monetization.billing.tools.billing

sealed class Product(
    open val id: String,
    open val isPurchased: Boolean,
    open val title: String,
) {
    data class OneTime(
        override val id: String,
        override val isPurchased: Boolean,
        override val title: String,
        val formattedPrice: String?,
        val consumable: Boolean = false,
    ) : Product(id, isPurchased, title)

    data class Subscription(
        override val id: String,
        override val isPurchased: Boolean,
        override val title: String,
        val offerToken: String?,
        val phases: List<Phase>
    ) : Product(id, isPurchased, title) {
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
