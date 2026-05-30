package us.webmy.core.ui.presentation.base.navigator

/**
 * Optional handler for domain navigation events (Billing, Ads).
 * Implementations live in feature modules (e.g. :core-monetization) and are
 * picked up by [WebmyNavigator] via optional Koin bindings.
 */
interface PurchaseNavigationHandler {
    fun handle(purchase: Navigation.Purchase): Result<Unit>
}

interface AdNavigationHandler {
    fun handle(ad: Navigation.Ad): Result<Unit>
}
