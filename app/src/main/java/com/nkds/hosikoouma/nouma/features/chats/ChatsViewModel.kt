package com.nkds.hosikoouma.nouma.features.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nkds.hosikoouma.nouma.data.local.Message
import com.nkds.hosikoouma.nouma.data.repository.ChatRepository
import com.nkds.hosikoouma.nouma.utils.SessionManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val chatId: Int,
    val opponentName: String,
    val lastMessage: Message?,
    val opponentAvatarUri: String?,
    val unreadMessageCount: Int
)

sealed class NewChatResult {
    data class Success(val chatId: Int) : NewChatResult()
    data class Error(val message: String) : NewChatResult()
}

class ChatsViewModel(private val chatRepository: ChatRepository, private val sessionManager: SessionManager) : ViewModel() {

    private val _chatsState = MutableStateFlow<List<ChatUiState>>(emptyList())
    val chatsState = _chatsState.asStateFlow()

    private val _selectedChatIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedChatIds = _selectedChatIds.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation = _showDeleteConfirmation.asStateFlow()

    private val _newChatEvent = MutableSharedFlow<NewChatResult>()
    val newChatEvent = _newChatEvent.asSharedFlow()

    val isSelectionModeActive: Boolean
        get() = _selectedChatIds.value.isNotEmpty()

    fun loadChats() {
        viewModelScope.launch {
            val currentUserId = sessionManager.getUserId()
            if (currentUserId != -1) {
                _chatsState.value = chatRepository.getChatsForUser(currentUserId)
            }
        }
    }

    fun onFindUserClick(username: String) {
        viewModelScope.launch {
            val currentUserId = sessionManager.getUserId()
            if (username.isBlank()) return@launch

            val newChatId = chatRepository.getOrCreateChatWithUserByUsername(currentUserId, username)
            if (newChatId != null) {
                _newChatEvent.emit(NewChatResult.Success(newChatId))
            } else {
                _newChatEvent.emit(NewChatResult.Error("Пользователь не найден или это вы"))
            }
        }
    }

    fun toggleChatSelection(chatId: Int) {
        _selectedChatIds.update {
            if (it.contains(chatId)) {
                it - chatId
            } else {
                it + chatId
            }
        }
    }

    fun clearSelections() {
        _selectedChatIds.value = emptySet()
    }

    fun requestDeletion() {
        _showDeleteConfirmation.value = true
    }

    fun cancelDeletion() {
        _showDeleteConfirmation.value = false
    }

    fun confirmDeletion() {
        viewModelScope.launch {
            val idsToDelete = _selectedChatIds.value.toList()
            chatRepository.deleteChats(idsToDelete)
            clearSelections()
            cancelDeletion()
            loadChats()
        }
    }
}

class ChatsViewModelFactory(
    private val chatRepository: ChatRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatsViewModel(chatRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
