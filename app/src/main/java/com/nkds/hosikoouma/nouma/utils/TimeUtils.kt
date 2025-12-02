package com.nkds.hosikoouma.nouma.utils

import android.content.Context
import com.nkds.hosikoouma.nouma.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun formatTimestamp(context: Context, timestamp: Long): String {
    val messageTime = Calendar.getInstance()
    messageTime.timeInMillis = timestamp

    val now = Calendar.getInstance()

    // Сегодня
    if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)) {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageTime.time)
    }

    // Вчера
    now.add(Calendar.DAY_OF_YEAR, -1)
    if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR)) {
        return context.getString(R.string.time_yesterday)
    }

    // Давно
    return SimpleDateFormat("dd.MM", Locale.getDefault()).format(messageTime.time)
}
