package com.nkds.hosikoouma.nouma.data.repository

import android.content.Context
import com.nkds.hosikoouma.nouma.data.FontChoice
import com.nkds.hosikoouma.nouma.data.Language
import com.nkds.hosikoouma.nouma.data.Theme

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("NoumaSettings", Context.MODE_PRIVATE)

    fun getTheme(): Theme {
        return Theme.valueOf(prefs.getString("theme", Theme.SYSTEM.name) ?: Theme.SYSTEM.name)
    }

    fun setTheme(theme: Theme) {
        prefs.edit().putString("theme", theme.name).apply()
    }

    fun getFont(): FontChoice {
        return FontChoice.valueOf(prefs.getString("font", FontChoice.SYSTEM.name) ?: FontChoice.SYSTEM.name)
    }

    fun setFont(font: FontChoice) {
        prefs.edit().putString("font", font.name).apply()
    }

    fun getLanguage(): Language {
        return Language.valueOf(prefs.getString("language", Language.SYSTEM.name) ?: Language.SYSTEM.name)
    }

    fun setLanguage(language: Language) {
        prefs.edit().putString("language", language.name).apply()
    }
}
