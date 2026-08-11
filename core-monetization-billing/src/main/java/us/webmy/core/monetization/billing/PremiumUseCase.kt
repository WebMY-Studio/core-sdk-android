package us.webmy.core.monetization.billing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import us.webmy.core.monetization.billing.BillingManager

interface PremiumUseCase {

    val isPremiumFlow: Flow<Boolean>
}

suspend fun PremiumUseCase.isPremium() = isPremiumFlow.first()
