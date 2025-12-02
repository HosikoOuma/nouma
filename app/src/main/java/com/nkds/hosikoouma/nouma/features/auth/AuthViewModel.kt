package com.nkds.hosikoouma.nouma.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nkds.hosikoouma.nouma.R
import com.nkds.hosikoouma.nouma.data.FontChoice
import com.nkds.hosikoouma.nouma.data.Theme
import com.nkds.hosikoouma.nouma.data.local.User
import com.nkds.hosikoouma.nouma.data.repository.AuthRepository
import com.nkds.hosikoouma.nouma.data.repository.ChatRepository
import com.nkds.hosikoouma.nouma.data.repository.SettingsRepository
import com.nkds.hosikoouma.nouma.utils.SecurityUtils
import com.nkds.hosikoouma.nouma.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun registerUser(username: String, email: String, password: String, confirm: String) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error(R.string.error_fields_cannot_be_empty)
            return
        }

        if (password != confirm) {
            _authState.value = AuthState.Error(R.string.error_passwords_do_not_match)
            return
        }

        viewModelScope.launch {
            try {
                if (authRepository.findUserByUsername(username) != null) {
                    _authState.value = AuthState.Error(R.string.error_username_taken)
                    return@launch
                }
                if (authRepository.findUserByEmail(email) != null) {
                    _authState.value = AuthState.Error(R.string.error_email_taken)
                    return@launch
                }

                val passwordHash = SecurityUtils.hashPassword(password)
                val user = User(username = username, email = email, passwordHash = passwordHash)
                
                val newUserId = authRepository.registerUser(user).toInt()
                
                chatRepository.createInitialChatsForUser(newUserId)

                _authState.value = AuthState.Success("Регистрация прошла успешно!") // Эта строка для Toast, оставим пока
            } catch (e: Exception) {
                _authState.value = AuthState.ErrorString(e.message ?: "Произошла неизвестная ошибка")
            }
        }
    }

    fun loginUser(usernameOrEmail: String, password: String) {
        if (usernameOrEmail.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error(R.string.error_fields_cannot_be_empty)
            return
        }

        viewModelScope.launch {
            val user = authRepository.findUserByUsername(usernameOrEmail) ?: authRepository.findUserByEmail(usernameOrEmail)

            if (user == null) {
                _authState.value = AuthState.ErrorString("Пользователь не найден")
                return@launch
            }

            val passwordHash = SecurityUtils.hashPassword(password)
            if (user.passwordHash == passwordHash) {
                sessionManager.setLoggedIn(true)
                sessionManager.saveUserId(user.id)
                _authState.value = AuthState.Success("Вход выполнен успешно!")
            } else {
                _authState.value = AuthState.ErrorString("Неверный пароль")
            }
        }
    }
    
    fun logout() {
        sessionManager.setLoggedIn(false)
        sessionManager.saveUserId(-1)
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val messageResId: Int) : AuthState()
    data class ErrorString(val message: String) : AuthState()
}

class AuthViewModelFactory(
    private val authRepository: AuthRepository, 
    private val chatRepository: ChatRepository, 
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository, chatRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
