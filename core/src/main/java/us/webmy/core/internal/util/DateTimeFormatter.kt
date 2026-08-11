package us.webmy.core.internal.util

import us.webmy.core.internal.InternalWebmyApi
import android.content.Context
import us.webmy.core.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@InternalWebmyApi
object DateTimeFormatter {

    private val zoneId = ZoneId.systemDefault()
    private val formatterFull: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
    private val formatterDate: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun formatTimestampFull(timestamp: Long) = timestamp.getDateTime().format(formatterFull)

    fun formatTimestampDate(timestamp: Long) = timestamp.getDateTime().format(formatterDate)

    fun formatDuration(duration: Long, useExtended: Boolean): String {
        val hours = duration / 3600000
        val minutes = (duration % 3600000) / 60000
        val seconds = (duration % 60000) / 1000
        val centiseconds = (duration % 1000) / 100

        return if (hours == 0L && !useExtended) {
            String.format("%02d:%02d,%01d", minutes, seconds, centiseconds)
        } else {
            String.format("%02d:%02d:%02d,%01d", hours, minutes, seconds, centiseconds)
        }
    }

    fun formatPeriod(context: Context, period: String, cycleCount: Int): String {
        return formatPeriod(context, Period.parse(period).multipliedBy(cycleCount))
    }

    fun formatPeriod(context: Context, period: Period): String {
        val years = period.years
        val months = period.months
        val days = period.days

        if (years > 1) return context.resources.getQuantityString(R.plurals.years, years, years)
        if (years == 1) return context.getString(R.string.year)

        if (months == 12) return context.getString(R.string.year)
        if (months > 1) return context.resources.getQuantityString(R.plurals.months, months, months)
        if (months == 1) return context.getString(R.string.month)

        if (days % 7 == 0 && days != 0) {
            val weeks = days / 7
            return if (weeks > 1) {
                context.resources.getQuantityString(R.plurals.weeks, weeks, weeks)
            } else {
                context.getString(R.string.week)
            }
        }

        if (days > 1) return context.resources.getQuantityString(R.plurals.days, days, days)
        if (days == 1) return context.getString(R.string.day)

        return ""
    }

    private fun Long.getDateTime() = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)
}

