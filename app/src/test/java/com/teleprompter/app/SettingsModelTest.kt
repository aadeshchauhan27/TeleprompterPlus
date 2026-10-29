package com.teleprompter.app

import com.teleprompter.app.core.model.FontStyleType
import com.teleprompter.app.core.model.PresetThemes
import com.teleprompter.app.core.model.TeleprompterSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsModelTest {

    @Test
    fun defaultSettings_haveCorrectMockupValues() {
        val settings = TeleprompterSettings()
        // Mockup Screen 5 defaults: speed = 50, size = 28
        assertEquals(50, settings.scrollSpeed)
        assertEquals(28, settings.fontSize)
        assertEquals(FontStyleType.SANS, settings.fontStyle)
        assertEquals("#FFFFFF", settings.textColorHex)
        assertEquals(0.5f, settings.bgOpacity, 0.01f)
        assertEquals(1.0f, settings.lineSpacingMultiplier, 0.01f)
        assertFalse(settings.mirrorText)
        assertTrue(settings.showSafeAreaGuide)
        assertEquals("DARK", settings.activeThemeId)
    }

    @Test
    fun presetThemes_containsAll6MockupThemes() {
        assertEquals(6, PresetThemes.all.size)

        val light = PresetThemes.all.find { it.id == "LIGHT" }
        assertNotNull(light)
        assertFalse(light!!.isDark)

        val dark = PresetThemes.all.find { it.id == "DARK" }
        assertNotNull(dark)
        assertTrue(dark!!.isDark)
        assertEquals("#0F172A", dark.backgroundColorHex)

        val studio = PresetThemes.all.find { it.id == "STUDIO" }
        assertNotNull(studio)
        assertEquals("#000000", studio!!.backgroundColorHex)

        val yellow = PresetThemes.all.find { it.id == "YELLOW" }
        assertNotNull(yellow)

        val green = PresetThemes.all.find { it.id == "GREEN" }
        assertNotNull(green)

        val minimal = PresetThemes.all.find { it.id == "MINIMAL" }
        assertNotNull(minimal)
    }

    @Test
    fun fontStyleType_matchesLabels() {
        assertEquals("Sans", FontStyleType.SANS.label)
        assertEquals("Serif", FontStyleType.SERIF.label)
        assertEquals("Mono", FontStyleType.MONO.label)
    }

    @Test
    fun presets_applyExpectedConfigurations() {
        val base = TeleprompterSettings()

        val creator = base.applyPreset(com.teleprompter.app.core.model.PresetType.CREATOR)
        assertEquals(com.teleprompter.app.core.model.PresetType.CREATOR, creator.presetType)
        assertEquals(50, creator.scrollSpeed)
        assertEquals(28, creator.fontSize)
        assertEquals(85, creator.readingWidthPercent)
        assertEquals(com.teleprompter.app.core.model.TextAlignmentType.CENTER, creator.textAlignment)

        val interview = base.applyPreset(com.teleprompter.app.core.model.PresetType.INTERVIEW)
        assertEquals(com.teleprompter.app.core.model.PresetType.INTERVIEW, interview.presetType)
        assertEquals(38, interview.scrollSpeed)
        assertEquals(FontStyleType.SERIF, interview.fontStyle)
        assertEquals(80, interview.readingWidthPercent)
        assertEquals(com.teleprompter.app.core.model.TextAlignmentType.LEFT, interview.textAlignment)
        assertEquals(5, interview.countdownSeconds)

        val reels = base.applyPreset(com.teleprompter.app.core.model.PresetType.REELS)
        assertEquals(com.teleprompter.app.core.model.PresetType.REELS, reels.presetType)
        assertEquals(62, reels.scrollSpeed)
        assertEquals(32, reels.fontSize)
        assertEquals(70, reels.readingWidthPercent)
    }

    @Test
    fun videoQuality_hasUserFriendlyLabels() {
        assertEquals("High (1080p)", com.teleprompter.app.core.model.VideoQualityPreference.HIGH.label)
        assertEquals("Standard (720p)", com.teleprompter.app.core.model.VideoQualityPreference.STANDARD.label)
        assertEquals("Ultra (4K)", com.teleprompter.app.core.model.VideoQualityPreference.ULTRA.label)
    }
}
