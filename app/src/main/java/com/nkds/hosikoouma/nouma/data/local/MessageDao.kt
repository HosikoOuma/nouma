package com.nkds.hosikoouma.nouma.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC")
    fun getMessagesForChat(chatId: Int): Flow<List<Message>>

    @Insert
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageInChat(chatId: Int): Message?

    @Query("SELECT * FROM messages WHERE chatId = :chatId AND type IN ('IMAGE', 'VIDEO') ORDER BY timestamp ASC")
    suspend fun getMediaMessagesForChat(chatId: Int): List<Message>

    @Query("DELETE FROM messages WHERE messageId IN (:messageIds)")
    suspend fun deleteMessagesByIds(messageIds: List<Int>)

    @Update
    suspend fun updateMessage(message: Message)
}
