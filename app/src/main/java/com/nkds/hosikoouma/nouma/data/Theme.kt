package com.nkds.hosikoouma.nouma.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.nkds.hosikoouma.nouma.R

enum class Theme {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED;

    @Composable
    fun getDisplayName(): String {
        return when (this) {
            SYSTEM -> stringResource(R.string.theme_system)
            LIGHT -> stringResource(R.string.theme_light)
            DARK -> stringResource(R.string.theme_dark)
            AMOLED -> stringResource(R.string.theme_amoled)
        }
    }
}
