package com.nkds.hosikoouma.nouma.data.local

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "chat_participants",
    primaryKeys = ["chatId", "userId"], // Составной первичный ключ
    foreignKeys = [
        ForeignKey(
            entity = Chat::class,
            parentColumns = ["chatId"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChatParticipant(
    val chatId: Int,
    val userId: Int
)
