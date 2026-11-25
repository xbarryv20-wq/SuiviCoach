package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateUtils {
  private const val ISO_DATE_PATTERN = "yyyy-MM-dd"
  private const val DISPLAY_DATE_PATTERN = "dd/MM/yyyy"

  private fun isoFormatter(): SimpleDateFormat =
    SimpleDateFormat(ISO_DATE_PATTERN, Locale.US).apply {
      isLenient = false
      timeZone = TimeZone.getTimeZone("UTC")
    }

  private fun displayFormatter(): SimpleDateFormat =
    SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }

  fun todayIso(): String {
    val cal = Calendar.getInstance()
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH) + 1
    val d = cal.get(Calendar.DAY_OF_MONTH)
    return String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
  }

  fun currentMonthPrefix(): String {
    val cal = Calendar.getInstance()
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH) + 1
    return String.format(Locale.US, "%04d-%02d", y, m)
  }

  fun nowIsoTimestamp(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date())
  }

  fun addDaysToIso(isoDate: String, days: Int): String {
    val parsed = parseIsoDate(isoDate) ?: parseIsoDate(todayIso())!!
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
      time = parsed
      add(Calendar.DAY_OF_YEAR, days)
    }
    return isoFormatter().format(cal.time)
  }

  fun daysBetween(fromIso: String, toIso: String): Int {
    val d1 = parseIsoDate(fromIso) ?: return 0
    val d2 = parseIsoDate(toIso) ?: return 0
    val diffMs = d2.time - d1.time
    return TimeUnit.MILLISECONDS.toDays(diffMs).toInt()
  }

  fun daysRemainingFromToday(endDateIso: String): Int {
    return daysBetween(todayIso(), endDateIso)
  }

  fun formatDisplay(isoDateOrTimestamp: String): String {
    val datePart = isoDateOrTimestamp.take(10)
    val parsed = parseIsoDate(datePart) ?: return isoDateOrTimestamp
    return displayFormatter().format(parsed)
  }

  fun isValidIsoDate(value: String): Boolean = parseIsoDate(value) != null

  private fun parseIsoDate(isoDate: String): Date? {
    return try {
      isoFormatter().parse(isoDate.take(10))
    } catch (_: Exception) {
      null
    }
  }
}
