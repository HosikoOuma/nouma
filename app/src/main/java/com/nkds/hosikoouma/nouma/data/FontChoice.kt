package com.nkds.hosikoouma.nouma.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nkds.hosikoouma.nouma.R

enum class FontChoice {
    SYSTEM,
    GOOGLE_SANS,
    JETBRAINS_MONO;

    @Composable
    fun getDisplayName(): String {
        return when (this) {
            SYSTEM -> stringResource(R.string.font_system)
            GOOGLE_SANS -> stringResource(R.string.font_google_sans)
            JETBRAINS_MONO -> stringResource(R.string.font_jetbrains_mono)
        }
    }
}
