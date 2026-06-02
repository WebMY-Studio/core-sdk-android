package us.webmy.core.ui.presentation.base.viewmodel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.withStateAtLeast
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import us.webmy.core.ui.presentation.base.navigator.Navigation
import us.webmy.core.ui.presentation.base.navigator.Router
import us.webmy.core.util.ActivityProvider

open class BaseViewModel : ViewModel(), KoinComponent {

    private val router: Router by inject()

    private val activityProvider: ActivityProvider by inject()

    fun navigateTo(navigation: Navigation): Result<Unit> = router.go(navigation)

    fun navigateWhenResumed(navigation: Navigation) = runCatching {
        val activity = activityProvider.requireHost()
        viewModelScope.launch {
            activity.lifecycle.withStateAtLeast(Lifecycle.State.RESUMED) {
                navigateTo(navigation)
            }
        }
    }
}
