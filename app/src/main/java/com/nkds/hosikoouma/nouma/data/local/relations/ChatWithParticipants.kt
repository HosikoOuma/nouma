package com.nkds.hosikoouma.nouma.data.local.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.nkds.hosikoouma.nouma.data.local.Chat
import com.nkds.hosikoouma.nouma.data.local.ChatParticipant
import com.nkds.hosikoouma.nouma.data.local.User

data class ChatWithParticipants(
    @Embedded val chat: Chat,
    @Relation(
        parentColumn = "chatId",
        entity = User::class,
        entityColumn = "id", // Явно указываем, к какой колонке в User подключаться
        associateBy = Junction(
            value = ChatParticipant::class,
            parentColumn = "chatId",
            entityColumn = "userId"
        )
    )
    val participants: List<User>,
    val unreadMessageCount: Int = 0
)
