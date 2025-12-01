package com.nkds.hosikoouma.nouma

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nkds.hosikoouma.nouma.ui.theme.NoumaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoumaTheme {
                AppNavigation()
            }
        }
    }
}
