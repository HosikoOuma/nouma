package com.nkds.hosikoouma.nouma.data.repository

import com.nkds.hosikoouma.nouma.data.local.Chat
import com.nkds.hosikoouma.nouma.data.local.ChatDao
import com.nkds.hosikoouma.nouma.data.local.MessageDao
import com.nkds.hosikoouma.nouma.data.local.User
import com.nkds.hosikoouma.nouma.data.local.UserDao
import com.nkds.hosikoouma.nouma.features.chats.ChatUiState
import com.nkds.hosikoouma.nouma.utils.SecurityUtils

class ChatRepository(private val userDao: UserDao, private val chatDao: ChatDao, private val messageDao: MessageDao) {

    private val botNames = listOf("Алиса", "Ева", "Джарвис")

    private suspend fun ensureBotsExist() {
        botNames.forEach { botName ->
            if (userDao.getUserByUsername(botName) == null) {
                userDao.insertUser(
                    User(
                        username = botName,
                        email = "${botName.lowercase()}@bot.local",
                        passwordHash = SecurityUtils.hashPassword("bot"),
                        isBot = true,
                        nickname = botName
                    )
                )
            }
        }
    }

    suspend fun createInitialChatsForUser(newUserId: Int) {
        ensureBotsExist()
        val bots = userDao.getBots()
        bots.forEach { bot ->
            chatDao.createChatWithParticipants(listOf(newUserId, bot.id))
        }
    }

    suspend fun getChatsForUser(userId: Int): List<ChatUiState> {
        val userChats = chatDao.getUserChats(userId)
        
        return userChats.mapNotNull { chatWithParticipants ->
            val opponent = chatWithParticipants.participants.find { it.id != userId }
            opponent?.let {
                val lastMessage = messageDao.getLastMessageInChat(chatWithParticipants.chat.chatId)
                ChatUiState(
                    chatId = chatWithParticipants.chat.chatId,
                    opponentName = it.nickname ?: it.username,
                    lastMessage = lastMessage,
                    opponentAvatarUri = it.avatarUri,
                    unreadMessageCount = chatWithParticipants.unreadMessageCount
                )
            }
        }
    }

    suspend fun getOrCreateChatWithUserByUsername(currentUserId: Int, targetUsername: String): Int? {
        val targetUser = userDao.getUserByUsername(targetUsername)
        if (targetUser == null || targetUser.id == currentUserId) return null
        return getOrCreateChatWithUser(currentUserId, targetUser.id)
    }

    suspend fun getOrCreateChatWithUser(currentUserId: Int, targetUserId: Int): Int {
        val existingChatId = chatDao.findPrivateChatByUsers(currentUserId, targetUserId)
        if (existingChatId != null) {
            return existingChatId
        }

        val newChatId = chatDao.insertChat(Chat()).toInt()
        chatDao.insertParticipant(com.nkds.hosikoouma.nouma.data.local.ChatParticipant(chatId = newChatId, userId = currentUserId))
        chatDao.insertParticipant(com.nkds.hosikoouma.nouma.data.local.ChatParticipant(chatId = newChatId, userId = targetUserId))
        return newChatId
    }

    suspend fun deleteChats(chatIds: List<Int>) {
        chatDao.deleteChatsByIds(chatIds)
    }
}
