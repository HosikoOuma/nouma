package com.nkds.hosikoouma.nouma.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nkds.hosikoouma.nouma.R

enum class Language(val locale: String) {
    SYSTEM(""), // Пустая строка означает использование системного языка
    RUSSIAN("ru"),
    ENGLISH("en");

    @Composable
    fun getDisplayName(): String {
        return when (this) {
            SYSTEM -> stringResource(R.string.language_system)
            RUSSIAN -> stringResource(R.string.language_russian)
            ENGLISH -> stringResource(R.string.language_english)
        }
    }
}
