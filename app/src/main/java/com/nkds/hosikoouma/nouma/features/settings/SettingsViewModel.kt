package com.nkds.hosikoouma.nouma.features.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nkds.hosikoouma.nouma.data.FontChoice
import com.nkds.hosikoouma.nouma.data.Language
import com.nkds.hosikoouma.nouma.data.Theme
import com.nkds.hosikoouma.nouma.data.local.User
import com.nkds.hosikoouma.nouma.data.repository.SettingsRepository
import com.nkds.hosikoouma.nouma.data.repository.UserRepository
import com.nkds.hosikoouma.nouma.utils.SessionManager
import com.nkds.hosikoouma.nouma.utils.setAppLocale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val user: User? = null,
    val isEditing: Boolean = false,
    val error: String? = null,
    val selectedTheme: Theme = Theme.SYSTEM,
    val selectedFont: FontChoice = FontChoice.SYSTEM,
    val selectedLanguage: Language = Language.SYSTEM,

    // Temporary state for editing
    val tempNickname: String = "",
    val tempUsername: String = "",
    val tempEmail: String = "",
    val tempBio: String = "",
    val tempAvatarUri: Uri? = null
)

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialSettings()
    }

    private fun loadInitialSettings() {
        _uiState.update {
            it.copy(
                selectedTheme = settingsRepository.getTheme(),
                selectedFont = settingsRepository.getFont(),
                selectedLanguage = settingsRepository.getLanguage()
            )
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != -1) {
                val user = userRepository.getUserById(userId)
                _uiState.update { it.copy(user = user) }
            }
        }
    }

    fun onThemeChange(theme: Theme) {
        settingsRepository.setTheme(theme)
        _uiState.update { it.copy(selectedTheme = theme) }
    }

    fun onFontChange(font: FontChoice) {
        settingsRepository.setFont(font)
        _uiState.update { it.copy(selectedFont = font) }
    }

    fun onLanguageChange(language: Language) {
        settingsRepository.setLanguage(language)
        _uiState.update { it.copy(selectedLanguage = language) }
        setAppLocale(language)
    }

    fun onEdit() {
        _uiState.value.user?.let {
            _uiState.update { currentState ->
                currentState.copy(
                    isEditing = true,
                    tempNickname = it.nickname ?: it.username,
                    tempUsername = it.username,
                    tempEmail = it.email,
                    tempBio = it.bio ?: "",
                    tempAvatarUri = null
                )
            }
        }
    }

    fun onCancel() {
        _uiState.update { it.copy(isEditing = false, error = null) }
    }

    fun onSave() {
        val state = _uiState.value
        val currentUser = state.user ?: return

        viewModelScope.launch {
            val existingUserByUsername = userRepository.getUserByUsername(state.tempUsername)
            if (existingUserByUsername != null && existingUserByUsername.id != currentUser.id) {
                _uiState.update { it.copy(error = "Это имя пользователя уже занято") }
                return@launch
            }

            val existingUserByEmail = userRepository.getUserByEmail(state.tempEmail)
            if (existingUserByEmail != null && existingUserByEmail.id != currentUser.id) {
                _uiState.update { it.copy(error = "Эта почта уже занята") }
                return@launch
            }

            val updatedUser = currentUser.copy(
                nickname = state.tempNickname,
                username = state.tempUsername,
                email = state.tempEmail,
                bio = state.tempBio,
                avatarUri = state.tempAvatarUri?.toString() ?: currentUser.avatarUri
            )

            userRepository.updateUser(updatedUser)
            _uiState.update { it.copy(user = updatedUser, isEditing = false, error = null) }
        }
    }
    
    fun onTempNicknameChanged(nickname: String) {
        _uiState.update { it.copy(tempNickname = nickname) }
    }
    fun onTempUsernameChanged(username: String) {
        _uiState.update { it.copy(tempUsername = username) }
    }
    fun onTempEmailChanged(email: String) {
        _uiState.update { it.copy(tempEmail = email) }
    }
    fun onTempBioChanged(bio: String) {
        _uiState.update { it.copy(tempBio = bio) }
    }
    fun onTempAvatarUriChanged(uri: Uri?) {
        _uiState.update { it.copy(tempAvatarUri = uri) }
    }

    fun onLogout() {
        // Reset all state to default
        _uiState.value = SettingsUiState()
        // Reload initial settings that are not user-specific
        loadInitialSettings()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

class SettingsViewModelFactory(
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(userRepository, settingsRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
