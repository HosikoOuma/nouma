package com.nkds.hosikoouma.nouma.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val messageId: Int = 0,
    val chatId: Int,
    val senderId: Int,
    val text: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType,
    val fileName: String? = null // Имя файла для типа FILE
)
