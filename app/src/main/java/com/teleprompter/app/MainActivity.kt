package com.teleprompter.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import com.teleprompter.app.core.model.AppThemeMode
import com.teleprompter.app.core.model.TeleprompterSettings
import com.teleprompter.app.core.theme.TelePrompterTheme
import com.teleprompter.app.ui.navigation.TeleprompterNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val settingsRepository = (application as TelePrompterApp).container.settingsRepository

        // Resolve the saved light/dark choice BEFORE the first frame. Otherwise, with the system in
        // dark mode and the app set to Light, the splash starts dark until DataStore finishes loading.
        val initialSettings = runBlocking {
            withTimeoutOrNull(400) { settingsRepository.settingsFlow.first() }
        } ?: TeleprompterSettings()
        val systemNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val initialDark = when (initialSettings.appThemeMode) {
            AppThemeMode.SYSTEM -> systemNight
            AppThemeMode.LIGHT -> false
            AppThemeMode.DARK -> true
        }
        window.setBackgroundDrawable(
            ColorDrawable(if (initialDark) 0xFF0E1626.toInt() else 0xFFF5EFE4.toInt())
        )

        setContent {
            val settings by settingsRepository.settingsFlow.collectAsState(initial = initialSettings)
            val systemInDark = isSystemInDarkTheme()
            val isDark = when (settings.appThemeMode) {
                AppThemeMode.SYSTEM -> systemInDark
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
            }

            // Status / navigation bar icons must follow the app's own light/dark choice,
            // which can differ from the system setting.
            DisposableEffect(isDark) {
                val bars = if (isDark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
                onDispose { }
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
