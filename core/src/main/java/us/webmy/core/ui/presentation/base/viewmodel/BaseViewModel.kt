package us.webmy.core.ui.presentation.base.viewmodel

import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import us.webmy.core.ui.presentation.base.navigator.Navigation
import us.webmy.core.ui.presentation.base.navigator.Router

open class BaseViewModel : ViewModel(), KoinComponent {

    private val router: Router by inject()

    fun navigateTo(navigation: Navigation): Result<Unit> = router.go(navigation)
}
