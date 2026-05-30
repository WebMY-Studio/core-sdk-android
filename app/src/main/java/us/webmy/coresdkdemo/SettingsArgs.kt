package us.webmy.coresdkdemo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SettingsArgs(
    val userId: String,
    val title: String,
) : Parcelable
