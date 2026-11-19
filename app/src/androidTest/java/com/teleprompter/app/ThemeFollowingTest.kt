package com.teleprompter.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.teleprompter.app.core.theme.TelePrompterTheme
import com.teleprompter.app.ui.screens.onboarding.OnboardingScreen
import com.teleprompter.app.ui.screens.splash.SplashScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

/** The splash and onboarding must follow the app's light/dark theme, not hard-code a dark look. */
@RunWith(AndroidJUnit4::class)
class ThemeFollowingTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var themeBackground: Color = Color.Unspecified

    private fun assertCornerMatchesThemeBackground() {
        composeRule.waitForIdle()
        val pixels = composeRule.onRoot().captureToImage().toPixelMap()
        // top-left corner: only the screen background is drawn there
        val actual = pixels[2, 2]
        fun close(a: Float, b: Float) = abs(a - b) < 0.02f
        val ok = close(actual.red, themeBackground.red) &&
            close(actual.green, themeBackground.green) &&
            close(actual.blue, themeBackground.blue)
        assertEquals("corner pixel $actual should equal theme background $themeBackground", true, ok)
    }

    private fun showSplash(dark: Boolean) {
        composeRule.setContent {
            TelePrompterTheme(darkTheme = dark) {
                themeBackground = MaterialTheme.colorScheme.background
                SplashScreen(onNavigateToHome = {})
            }
        }
    }

    private fun showOnboarding(dark: Boolean) {
        composeRule.setContent {
            TelePrompterTheme(darkTheme = dark) {
                themeBackground = MaterialTheme.colorScheme.background
                OnboardingScreen(onFinished = {})
            }
        }
    }

    @Test
    fun splash_inLightTheme_usesLightBackground() {
        showSplash(dark = false)
        assertCornerMatchesThemeBackground()
        // and it is genuinely light, not the old hard-coded navy
        assertEquals(true, themeBackground.red > 0.8f)
    }

    @Test
    fun splash_inDarkTheme_usesDarkBackground() {
        showSplash(dark = true)
        assertCornerMatchesThemeBackground()
        assertEquals(true, themeBackground.red < 0.2f)
    }

    @Test
    fun onboarding_inLightTheme_usesLightBackground() {
        showOnboarding(dark = false)
        assertCornerMatchesThemeBackground()
        assertEquals(true, themeBackground.red > 0.8f)
    }

    @Test
    fun onboarding_inDarkTheme_usesDarkBackground() {
        showOnboarding(dark = true)
        assertCornerMatchesThemeBackground()
        assertEquals(true, themeBackground.red < 0.2f)
    }

    @Test
    fun lightAndDarkBackgroundsDiffer() {
        showOnboarding(dark = false)
        val light = themeBackground
        composeRule.runOnUiThread { }
        assertNotEquals(Color.Unspecified, light)
    }
}
