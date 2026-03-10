package us.webmy.core_sdk.tools.sharing

import android.content.Intent
import android.provider.CalendarContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat

interface SharingManager {
    fun shareContent(sharing: ContentSharing)
    fun shareText(text: String)
    fun shareEvent(sharing: EventSharing)
}

class RealSharingManager(
    private val activity: AppCompatActivity
) : SharingManager {

    override fun shareContent(sharing: ContentSharing) {
        val intent = ShareCompat.IntentBuilder(activity)
            .setStream(sharing.file.uri)
            .setType(sharing.file.mimeType)
            .setSubject(sharing.subject)
            .setText(sharing.text)
            .apply {
                sharing.to?.let { addEmailTo(it) }
            }
            .intent

        activity.startActivity(intent)
    }

    override fun shareText(text: String) {
        val intent = ShareCompat.IntentBuilder(activity)
            .setType("text/plain")
            .setText(text)
            .intent

        activity.startActivity(intent)
    }

    override fun shareEvent(sharing: EventSharing) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI

            putExtra(CalendarContract.Events.TITLE, sharing.title)
            putExtra(CalendarContract.Events.DTSTART, sharing.startTime)
            putExtra(CalendarContract.Events.DTEND, sharing.endTime)
            putExtra(CalendarContract.Events.ALL_DAY, false)
        }

        activity.startActivity(intent)
    }
}
