package com.webmy.core_sdk.presentation.base.navigator

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseNavigator(
    val activity: AppCompatActivity
) {

    private val filePickerLauncher = activity.registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        selectFileCallback?.invoke(it)
        selectFileCallback = null
    }

    private var selectFileCallback: ((Uri?) -> Unit)? = null

    fun finish() {
        activity.finish()
    }

    abstract fun open(target: NavigationTarget)

    fun openBrowser(url: String) {
        activity.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    fun openEmailApp(email: String, subject: String?, text: String?) {
        try {
            Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:$email".toUri()
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, text)
                activity.startActivity(this)
            }
        } catch (_: Exception) {
        }
    }

    fun openGooglePlay(applicationId: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "market://details?id=$applicationId".toUri()
                setPackage("com.android.vending")
            }
            activity.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to browser if Play Store app is not available
            val url = "https://play.google.com/store/apps/details?id=$applicationId"
            openBrowser(url)
        }
    }

    fun showBottomSheet(dialog: BottomSheetDialogFragment) {
        dialog.show(activity.supportFragmentManager, dialog.javaClass.simpleName)
    }

    fun selectFile(input: String, callback: (Uri?) -> Unit) {
        selectFileCallback = callback
        filePickerLauncher.launch(input)
    }
}

fun <T : Parcelable> Intent.withPayload(payload: T): Intent {
    return apply {
        putExtra(payload::class.java.name, payload)
    }
}