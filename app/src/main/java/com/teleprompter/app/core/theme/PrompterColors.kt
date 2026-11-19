package com.teleprompter.app.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.teleprompter.app.core.model.PresetThemes

/** Default dark prompter background, used when the selected theme can't be resolved. */
val PrompterDefaultBackground = Color(0xFF070B14)

/** Default light prompter background, used when the text is dark and the theme background clashes. */
val PrompterDefaultLightBackground = Color(0xFFF8FAFC)

/** WCAG-style contrast ratio between two colours (1 = identical, 21 = black on white). */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    val lighter = maxOf(la, lb)
    val darker = minOf(la, lb)
    return (lighter + 0.05f) / (darker + 0.05f)
}

/**
 * Background colour for the prompter / script overlay for the chosen theme. Themes also set the
 * text colour, so the background must follow or light themes become dark-on-dark. If the user's
 * text colour would be hard to read on the theme background (e.g. a script with its own white
 * text under a light theme), fall back to a background that contrasts with the text.
 */
fun prompterBackgroundFor(themeId: String, textColor: Color? = null): Color {
    val theme = PresetThemes.all.find { it.id == themeId }
    val themeBg = theme?.let {
        try {
            Color(android.graphics.Color.parseColor(it.backgroundColorHex))
        } catch (_: Exception) {
            null
        }
    } ?: PrompterDefaultBackground

    if (textColor == null) return themeBg
    if (contrastRatio(textColor, themeBg) >= MIN_TEXT_CONTRAST) return themeBg
    return if (textColor.luminance() > 0.5f) PrompterDefaultBackground else PrompterDefaultLightBackground
}

private const val MIN_TEXT_CONTRAST = 3f
