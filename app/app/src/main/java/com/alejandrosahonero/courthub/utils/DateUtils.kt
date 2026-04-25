package com.alejandrosahonero.courthub.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    fun epochToReadable(epochMillis: Long): String {
        val date = Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        return "%02d/%02d/%d %02d:%02d".format(
            date.dayOfMonth, date.monthValue, date.year,
            date.hour, date.minute
        )
    }

    fun todayString(): String =
        LocalDate.now()
            .format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun parseLocalDate(dateString: String): LocalDate =
        LocalDate.parse(
            dateString,
            DateTimeFormatter.ISO_LOCAL_DATE
        )

    fun endTimeFromStart(startTime: String): String {
        val hour = startTime.split(":")[0].toInt()
        return formatHour(hour + 1)
    }

    fun formatHour(hour: Int): String = "%02d:00".format(hour)
}