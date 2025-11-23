package com.webmy.core_sdk.tools.sharing

import android.content.Intent
import android.provider.CalendarContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat

interface SharingManager {
    fun shareContent(activity: AppCompatActivity, sharing: ContentSharing)
    fun shareText(activity: AppCompatActivity, text: String)
    fun shareEvent(activity: AppCompatActivity, sharing: EventSharing)
}

class RealSharingManager() : SharingManager {

    override fun shareContent(activity: AppCompatActivity, sharing: ContentSharing) {
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

    override fun shareText(activity: AppCompatActivity, text: String) {
        val intent = ShareCompat.IntentBuilder(activity)
            .setType("text/plain")
            .setText(text)
            .intent

        activity.startActivity(intent)
    }

    override fun shareEvent(activity: AppCompatActivity, sharing: EventSharing) {
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
