package com.nkds.hosikoouma.nouma.features.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.nkds.hosikoouma.nouma.data.local.Message
import com.nkds.hosikoouma.nouma.features.conversation.ConversationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MediaViewerUiState(
    val mediaItems: List<Message> = emptyList(),
    val initialPage: Int = 0
)

class MediaViewerViewModel(
    private val repository: ConversationRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MediaViewerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMedia()
    }

    private fun loadMedia() {
        viewModelScope.launch {
            val chatId = savedStateHandle.get<String>("chatId")?.toIntOrNull() ?: -1
            val clickedMessageId = savedStateHandle.get<String>("messageId")?.toIntOrNull() ?: -1

            if (chatId != -1) {
                val mediaMessages = repository.getMediaMessages(chatId)
                val initialIndex = mediaMessages.indexOfFirst { it.messageId == clickedMessageId }.coerceAtLeast(0)
                
                _uiState.update {
                    it.copy(
                        mediaItems = mediaMessages,
                        initialPage = initialIndex
                    )
                }
            }
        }
    }
}

class MediaViewerViewModelFactory(
    private val repository: ConversationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(MediaViewerViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            @Suppress("UNCHECKED_CAST")
            return MediaViewerViewModel(repository, savedStateHandle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
