package com.teleprompter.app

import androidx.compose.ui.graphics.Color
import com.teleprompter.app.core.theme.AccentCobalt
import com.teleprompter.app.core.theme.PrompterDefaultBackground
import com.teleprompter.app.core.theme.contrastRatio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {

    @Test
    fun contrastRatio_blackOnWhiteIs21() {
        assertEquals(21f, contrastRatio(Color.Black, Color.White), 0.01f)
    }

    @Test
    fun contrastRatio_identicalColoursIs1() {
        assertEquals(1f, contrastRatio(Color.Red, Color.Red), 0.001f)
    }

    @Test
    fun contrastRatio_isSymmetric() {
        assertEquals(
            contrastRatio(Color(0xFF123456), Color(0xFFABCDEF)),
            contrastRatio(Color(0xFFABCDEF), Color(0xFF123456)),
            0.0001f
        )
    }

    @Test
    fun accent_isReadableOnTheDarkPrompterBackground() {
        // Play button / progress / reading guide sit on the dark background: must be clearly visible
        assertTrue(contrastRatio(AccentCobalt, PrompterDefaultBackground) >= 3f)
    }

    @Test
    fun whiteTextOnAccent_isReadable() {
        assertTrue(contrastRatio(Color.White, AccentCobalt) >= 4.5f)
    }

    @Test
    fun whiteTextOnYellowThemeBackground_isNotReadable() {
        // This is the case the fallback guards against (white script text under the Yellow theme)
        assertTrue(contrastRatio(Color.White, Color(0xFFFEF3C7)) < 3f)
    }
}
