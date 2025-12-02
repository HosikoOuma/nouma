package com.nkds.hosikoouma.nouma.features.conversation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.nkds.hosikoouma.nouma.data.local.Message
import com.nkds.hosikoouma.nouma.data.local.User
import com.nkds.hosikoouma.nouma.features.bot.BotScript
import com.nkds.hosikoouma.nouma.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConversationUiState(
    val messages: List<Message> = emptyList(),
    val opponent: User? = null,
    val messageToEdit: Message? = null
)

class ConversationViewModel(
    private val repository: ConversationRepository,
    private val sessionManager: SessionManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: Int = savedStateHandle.get<String>("chatId")?.toIntOrNull() ?: -1
    val currentUserId: Int = sessionManager.getUserId()

    private val _messages = repository.getMessages(chatId)
    private val _opponent = MutableStateFlow<User?>(null)
    private val _messageToEdit = MutableStateFlow<Message?>(null)

    val uiState = combine(_messages, _opponent, _messageToEdit) { messages, opponent, messageToEdit ->
        ConversationUiState(messages, opponent, messageToEdit)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConversationUiState()
    )

    private val _selectedMessageIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedMessageIds = _selectedMessageIds.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation = _showDeleteConfirmation.asStateFlow()

    init {
        loadOpponent()
    }

    fun toggleMessageSelection(messageId: Int) {
        _selectedMessageIds.update {
            if (it.contains(messageId)) it - messageId else it + messageId
        }
    }

    fun clearSelections() {
        _selectedMessageIds.value = emptySet()
    }

    fun requestMessageDeletion() {
        _showDeleteConfirmation.value = true
    }

    fun cancelMessageDeletion() {
        _showDeleteConfirmation.value = false
    }

    fun confirmMessageDeletion() {
        viewModelScope.launch {
            repository.deleteMessages(_selectedMessageIds.value.toList())
            clearSelections()
            cancelMessageDeletion()
        }
    }

    fun startEditingMessage() {
        val messageId = _selectedMessageIds.value.firstOrNull() ?: return
        val message = uiState.value.messages.find { it.messageId == messageId } ?: return
        _messageToEdit.value = message
        clearSelections()
    }

    fun cancelEditing() {
        _messageToEdit.value = null
    }

    fun confirmEditing(newText: String) {
        val message = _messageToEdit.value ?: return
        viewModelScope.launch {
            repository.updateMessage(message.copy(text = newText))
            cancelEditing()
        }
    }

    private fun loadOpponent() {
        viewModelScope.launch {
            _opponent.value = repository.getOpponent(chatId, currentUserId)
        }
    }

    fun sendTextMessage(text: String) {
        if (text.isNotBlank()) {
            viewModelScope.launch {
                repository.sendTextMessage(chatId, currentUserId, text)
                handleBotResponse(text)
            }
        }
    }

    fun sendImageMessage(uri: Uri) {
        viewModelScope.launch {
            repository.sendImageMessage(chatId, currentUserId, uri)
        }
    }

    fun sendVideoMessage(uri: Uri) {
        viewModelScope.launch {
            repository.sendVideoMessage(chatId, currentUserId, uri)
        }
    }

    fun sendFileMessage(uri: Uri, fileName: String) {
        viewModelScope.launch {
            repository.sendFileMessage(chatId, currentUserId, uri, fileName)
        }
    }

    fun sendMusicMessage(uri: Uri, fileName: String) {
        viewModelScope.launch {
            repository.sendMusicMessage(chatId, currentUserId, uri, fileName)
        }
    }

    fun sendVoiceMessage(uri: Uri, fileName: String) {
        viewModelScope.launch {
            repository.sendVoiceMessage(chatId, currentUserId, uri, fileName)
        }
    }

    private suspend fun handleBotResponse(userMessage: String) {
        val opponent = _opponent.value
        if (opponent != null && opponent.isBot) {
            val botResponse = BotScript.getResponse(opponent.username, userMessage)
            if (botResponse != null) {
                delay(1000) // Имитация печати
                repository.sendTextMessage(chatId, opponent.id, botResponse)
            }
        }
    }
}

class ConversationViewModelFactory(
    private val repository: ConversationRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(ConversationViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            @Suppress("UNCHECKED_CAST")
            return ConversationViewModel(repository, sessionManager, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
