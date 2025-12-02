package com.nkds.hosikoouma.nouma.utils

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.nkds.hosikoouma.nouma.data.Language
import java.util.Locale

fun setAppLocale(language: Language) {
    val localeList = if (language == Language.SYSTEM) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(language.locale)
    }
    AppCompatDelegate.setApplicationLocales(localeList)
}
