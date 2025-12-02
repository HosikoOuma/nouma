package com.nkds.hosikoouma.nouma.features.conversation

import android.net.Uri
import com.nkds.hosikoouma.nouma.data.local.ChatDao
import com.nkds.hosikoouma.nouma.data.local.Message
import com.nkds.hosikoouma.nouma.data.local.MessageDao
import com.nkds.hosikoouma.nouma.data.local.MessageType
import com.nkds.hosikoouma.nouma.data.local.User
import kotlinx.coroutines.flow.Flow

class ConversationRepository(
    private val messageDao: MessageDao,
    private val chatDao: ChatDao
) {
    fun getMessages(chatId: Int): Flow<List<Message>> {
        return messageDao.getMessagesForChat(chatId)
    }

    suspend fun sendTextMessage(chatId: Int, senderId: Int, text: String) {
        val message = Message(
            chatId = chatId,
            senderId = senderId,
            text = text,
            type = MessageType.TEXT
        )
        messageDao.insertMessage(message)
    }

    suspend fun sendImageMessage(chatId: Int, senderId: Int, imageUri: Uri) {
        val message = Message(
            chatId = chatId,
            senderId = senderId,
            text = imageUri.toString(),
            type = MessageType.IMAGE
        )
        messageDao.insertMessage(message)
    }

    suspend fun sendVideoMessage(chatId: Int, senderId: Int, videoUri: Uri) {
        val message = Message(
            chatId = chatId,
            senderId = senderId,
            text = videoUri.toString(),
            type = MessageType.VIDEO
        )
        messageDao.insertMessage(message)
    }

    suspend fun sendFileMessage(chatId: Int, senderId: Int, fileUri: Uri, fileName: String) {
        val message = Message(
            chatId = chatId,
            senderId = senderId,
            text = fileUri.toString(),
            type = MessageType.FILE,
            fileName = fileName
        )
        messageDao.insertMessage(message)
    }

    suspend fun sendMusicMessage(chatId: Int, senderId: Int, musicUri: Uri, fileName: String) {
        val message = Message(
            chatId = chatId,
            senderId = senderId,
            text = musicUri.toString(),
            type = MessageType.MUSIC,
            fileName = fileName
        )
        messageDao.insertMessage(message)
    }

    suspend fun sendVoiceMessage(chatId: Int, senderId: Int, voiceUri: Uri, fileName: String) {
        val message = Message(
            chatId = chatId,
            senderId = senderId,
            text = voiceUri.toString(),
            type = MessageType.VOICE,
            fileName = fileName
        )
        messageDao.insertMessage(message)
    }

    suspend fun getOpponent(chatId: Int, currentUserId: Int): User? {
        val chatWithParticipants = chatDao.getChatWithParticipants(chatId)
        return chatWithParticipants?.participants?.find { it.id != currentUserId }
    }

    suspend fun getMediaMessages(chatId: Int): List<Message> {
        return messageDao.getMediaMessagesForChat(chatId)
    }

    suspend fun deleteMessages(messageIds: List<Int>) {
        messageDao.deleteMessagesByIds(messageIds)
    }

    suspend fun updateMessage(message: Message) {
        messageDao.updateMessage(message)
    }
}
