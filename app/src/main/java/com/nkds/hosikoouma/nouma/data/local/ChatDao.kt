package com.nkds.hosikoouma.nouma.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.nkds.hosikoouma.nouma.data.local.relations.ChatWithParticipants

@Dao
interface ChatDao {

    @Insert
    suspend fun insertChat(chat: Chat): Long

    @Insert
    suspend fun insertParticipant(participant: ChatParticipant)

    @Transaction
    @Query("SELECT * FROM chats WHERE chatId IN (SELECT chatId FROM chat_participants WHERE userId = :userId)")
    suspend fun getUserChats(userId: Int): List<ChatWithParticipants>

    @Transaction
    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    suspend fun getChatWithParticipants(chatId: Int): ChatWithParticipants?

    @Query("""
        SELECT T1.chatId FROM chat_participants AS T1
        INNER JOIN chat_participants AS T2 ON T1.chatId = T2.chatId
        WHERE T1.userId = :userId1 AND T2.userId = :userId2
        AND (SELECT COUNT(*) FROM chat_participants WHERE chatId = T1.chatId) = 2
    """)
    suspend fun findPrivateChatByUsers(userId1: Int, userId2: Int): Int?

    @Transaction
    suspend fun createChatWithParticipants(participants: List<Int>) {
        val chatId = insertChat(Chat()).toInt()
        participants.forEach { userId ->
            insertParticipant(ChatParticipant(chatId = chatId, userId = userId))
        }
    }

    @Query("DELETE FROM chats WHERE chatId IN (:chatIds)")
    suspend fun deleteChatsByIds(chatIds: List<Int>)
}
