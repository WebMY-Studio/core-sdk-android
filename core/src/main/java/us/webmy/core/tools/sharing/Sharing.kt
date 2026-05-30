package us.webmy.core.tools.sharing

import android.net.Uri

class FileSharing(val uri: Uri, val mimeType: String)

class ContentSharing(
    val subject: String? = null,
    val text: String? = null,
    val to: String? = null,
    val file: FileSharing
) {

    companion object {
        fun file(
            text: String? = null,
            subject: String? = null,
            to: String? = null,
            uri: Uri,
            mimeType: String
        ): ContentSharing {
            return ContentSharing(subject, text, to, FileSharing(uri, mimeType))
        }
    }
}

class EventSharing(
    val title: String,
    val description: String? = null,
    val startTime: Long? = null,
    val endTime: Long? = null
)