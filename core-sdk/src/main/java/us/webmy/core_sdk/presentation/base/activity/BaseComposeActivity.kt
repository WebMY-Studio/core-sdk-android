package us.webmy.core_sdk.presentation.base.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ScopeActivity
import us.webmy.core_sdk.presentation.base.navigator.Navigation
import us.webmy.core_sdk.presentation.base.navigator.Navigator
import us.webmy.core_sdk.presentation.base.viewmodel.BaseViewModel
import us.webmy.core_sdk.util.observe
import us.webmy.core_sdk_compose.theme.WebmyTheme

abstract class BaseComposeActivity<VM : BaseViewModel> : ScopeActivity() {

    private val navigator: Navigator by inject()

    protected abstract val viewModel: VM

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        viewModel.navigation.observe(this, ::handleNavigation)

        setContent {
            WebmyTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    ScreenContent()
                }
            }
        }
    }

    fun handleNavigation(navigation: Navigation) {
        CoroutineScope(Dispatchers.Main).launch {
            navigator.navigate(this@BaseComposeActivity, navigation)
        }
    }

    @Composable
    protected abstract fun ScreenContent()
}