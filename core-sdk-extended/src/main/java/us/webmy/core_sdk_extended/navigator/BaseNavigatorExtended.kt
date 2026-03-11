package us.webmy.core_sdk_extended.navigator

import androidx.appcompat.app.AppCompatActivity
import us.webmy.core_sdk.presentation.base.navigator.BaseNavigator
import us.webmy.core_sdk.presentation.base.navigator.Navigation
import us.webmy.core_sdk_extended.tools.billing.BillingManager

abstract class BaseNavigatorExtended(
    private val billingManager: BillingManager
) : BaseNavigator() {

    override fun navigate(activity: AppCompatActivity, nav: Navigation) {
        when (nav) {
            is Navigation.Purchase -> billingManager.purchase(activity, nav.productId)
            else -> super.navigate(activity, nav)
        }
    }
}