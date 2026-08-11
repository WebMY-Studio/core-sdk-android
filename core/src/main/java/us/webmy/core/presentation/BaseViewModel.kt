package us.webmy.core.presentation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.withStateAtLeast
import kotlinx.coroutines.launch
import us.webmy.core.WebMY
import us.webmy.core.navigation.Navigation
import us.webmy.core.navigation.Router
import us.webmy.core.util.ActivityProvider

open class BaseViewModel : ViewModel() {

    private val router: Router get() = WebMY.router

    private val activityProvider: ActivityProvider get() = WebMY.activityProvider

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
