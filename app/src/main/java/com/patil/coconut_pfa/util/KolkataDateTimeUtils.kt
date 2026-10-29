package com.patil.coconut_pfa.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object KolkataDateTimeUtils {
    val ZONE_KOLKATA: ZoneId = ZoneId.of("Asia/Kolkata")
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun currentEpochMillis(): Long = System.currentTimeMillis()

    fun currentLocalDate(): LocalDate = LocalDate.now(ZONE_KOLKATA)

    fun currentDateString(): String = currentLocalDate().format(DATE_FORMATTER)

    fun epochMillisToLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZONE_KOLKATA)
            .toLocalDate()
    }

    fun localDateToStartOfDayEpochMillis(localDate: LocalDate): Long {
        return localDate.atStartOfDay(ZONE_KOLKATA).toInstant().toEpochMilli()
    }

    fun dateStringToEpochMillis(dateString: String): Long {
        val localDate = LocalDate.parse(dateString, DATE_FORMATTER)
        return localDateToStartOfDayEpochMillis(localDate)
    }

    fun epochMillisToDateString(epochMillis: Long): String {
        return epochMillisToLocalDate(epochMillis).format(DATE_FORMATTER)
    }
}
