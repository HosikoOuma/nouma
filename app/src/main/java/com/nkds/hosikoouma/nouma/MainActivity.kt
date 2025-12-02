package com.nkds.hosikoouma.nouma

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nkds.hosikoouma.nouma.data.local.AppDatabase
import com.nkds.hosikoouma.nouma.data.repository.SettingsRepository
import com.nkds.hosikoouma.nouma.data.repository.UserRepository
import com.nkds.hosikoouma.nouma.features.settings.SettingsViewModel
import com.nkds.hosikoouma.nouma.features.settings.SettingsViewModelFactory
import com.nkds.hosikoouma.nouma.ui.theme.NoumaTheme
import com.nkds.hosikoouma.nouma.utils.SessionManager
import com.nkds.hosikoouma.nouma.utils.setAppLocale

class MainActivity : AppCompatActivity() { // <-- ИЗМЕНЕНО
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Устанавливаем язык при старте
        val settingsRepository = SettingsRepository(this)
        setAppLocale(settingsRepository.getLanguage())

        enableEdgeToEdge()
        setContent {
            val db = AppDatabase.getDatabase(this)
            val sessionManager = SessionManager(this)
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    UserRepository(db.userDao()),
                    settingsRepository,
                    sessionManager
                )
            )

            val uiState by settingsViewModel.uiState.collectAsState()

            NoumaTheme(
                theme = uiState.selectedTheme,
                fontChoice = uiState.selectedFont
            ) {
                AppNavigation(settingsViewModel = settingsViewModel)
            }
        }
    }
}
