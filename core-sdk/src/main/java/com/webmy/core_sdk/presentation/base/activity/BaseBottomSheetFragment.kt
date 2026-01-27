package com.webmy.core_sdk.presentation.base.activity

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.fragmentScope
import org.koin.core.scope.Scope

abstract class BaseBottomSheetFragment(
    @LayoutRes contentLayoutId: Int
) : BottomSheetDialogFragment(contentLayoutId),
    AndroidScopeComponent {
    override val scope: Scope by fragmentScope()

    open val halfExpandedEnabled = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!halfExpandedEnabled) disableHalfExpanded()
    }

    private fun disableHalfExpanded() {
        dialog?.let {
            val sheet = it as BottomSheetDialog
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            sheet.behavior.skipCollapsed = true
        }
    }
}