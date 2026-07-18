package com.screenlog.app.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val DEFAULT_DATE_FORMAT = "yyyy-MM-dd"
    private const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"

    fun formatLongToDateString(timeInMillis: Long): String {
        val sdf = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date(timeInMillis))
    }

    fun formatYearFromDateString(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "N/A"
        return try {
            val parser = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.getDefault())
            val date = parser.parse(dateString)
            val formatter = SimpleDateFormat("yyyy", Locale.getDefault())
            date?.let { formatter.format(it) } ?: "N/A"
        } catch (e: Exception) {
            "N/A"
        }
    }
}
