package us.webmy.core.ui.single

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import us.webmy.core.ui.presentation.base.navigator.ComposeSheetContent

/**
 * Holds the current Compose bottom-sheet content. Observed by [WebmyAppHost]
 * to render a ModalBottomSheet on top of the NavHost.
 */
class SheetController {

    var content: ComposeSheetContent? by mutableStateOf(null)
        private set

    fun show(sheet: ComposeSheetContent) {
        content = sheet
    }

    fun dismiss() {
        content = null
    }

    val isShowing: Boolean get() = content != null
}
