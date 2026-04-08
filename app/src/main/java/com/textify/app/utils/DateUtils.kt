package com.textify.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Ahora"
            diff < 3_600_000 -> "${diff / 60_000} min"
            diff < 86_400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(timestamp))
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(Date(timestamp))
        }
    }
}