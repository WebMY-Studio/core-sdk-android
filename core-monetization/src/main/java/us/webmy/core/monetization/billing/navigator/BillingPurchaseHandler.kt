package us.webmy.core.monetization.billing.navigator

import us.webmy.core.monetization.billing.tools.billing.BillingManager
import us.webmy.core.ui.presentation.base.navigator.Navigation
import us.webmy.core.ui.presentation.base.navigator.PurchaseNavigationHandler

class BillingPurchaseHandler(
    private val billingManager: BillingManager,
) : PurchaseNavigationHandler {
    override fun handle(purchase: Navigation.Purchase): Result<Unit> =
        billingManager.purchase(purchase.productId)
}
