package us.webmy.core.monetization.billing

import kotlinx.coroutines.flow.Flow

interface BillingManager {

    suspend fun awaitInitialized(): Result<Unit>

    fun subscribeProducts(): Flow<List<Product>>

    suspend fun purchase(productId: String): PurchaseOutcome

    fun canBePurchased(productId: String): Result<Boolean>
}
