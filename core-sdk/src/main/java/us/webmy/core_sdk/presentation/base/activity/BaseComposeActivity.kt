package us.webmy.core_sdk.presentation.base.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.androidx.scope.ScopeActivity
import us.webmy.core_sdk.presentation.base.viewmodel.BaseViewModel
import us.webmy.core_sdk_compose.theme.WebmyTheme

abstract class BaseComposeActivity<VM : BaseViewModel> : ScopeActivity() {

    protected abstract val viewModel: VM

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        viewModel

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

    @Composable
    protected abstract fun ScreenContent()
}