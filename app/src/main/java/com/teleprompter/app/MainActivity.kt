package com.teleprompter.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.teleprompter.app.core.model.AppThemeMode
import com.teleprompter.app.core.model.TeleprompterSettings
import com.teleprompter.app.core.theme.TelePrompterTheme
import com.teleprompter.app.ui.navigation.TeleprompterNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val settingsRepository = (application as TelePrompterApp).container.settingsRepository

        setContent {
            val settings by settingsRepository.settingsFlow.collectAsState(initial = TeleprompterSettings())
            val systemInDark = isSystemInDarkTheme()
            val isDark = when (settings.appThemeMode) {
                AppThemeMode.SYSTEM -> systemInDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            TelePrompterTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TeleprompterNavHost()
                }
            }
        }
    }
}
