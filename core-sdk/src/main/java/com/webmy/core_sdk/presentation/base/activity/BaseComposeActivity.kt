package com.webmy.core_sdk.presentation.base.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.webmy.core_sdk.presentation.base.viewmodel.BaseViewModel
import org.koin.androidx.scope.ScopeActivity

abstract class BaseComposeActivity<VM : BaseViewModel> : ScopeActivity() {

    protected abstract val viewModel: VM

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        viewModel

        setContent {
            ScreenContent()
        }
    }

    @Composable
    protected abstract fun ScreenContent()
}