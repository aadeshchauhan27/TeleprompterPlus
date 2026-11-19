package com.teleprompter.app

import com.teleprompter.app.core.util.ScriptCue
import com.teleprompter.app.core.util.ScriptCues
import com.teleprompter.app.core.util.ScriptUtils
import com.teleprompter.app.data.repository.SampleScriptSeeder
import com.teleprompter.app.ui.screens.onboarding.onboardingPages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingAndSampleContentTest {

    @Test
    fun onboarding_hasThreeCompletePages() {
        assertEquals(3, onboardingPages.size)
        onboardingPages.forEach {
            assertTrue("title must not be blank", it.title.isNotBlank())
            assertTrue("body must not be blank", it.body.isNotBlank())
        }
    }

    @Test
    fun onboarding_titlesAreUnique() {
        assertEquals(onboardingPages.size, onboardingPages.map { it.title }.toSet().size)
    }

    @Test
    fun sampleScript_hasTitleAndRealContent() {
        assertTrue(SampleScriptSeeder.TITLE.isNotBlank())
        assertTrue(ScriptUtils.calculateWordCount(SampleScriptSeeder.CONTENT) > 100)
    }

    @Test
    fun sampleScript_demonstratesAPauseCue() {
        val cues = SampleScriptSeeder.CONTENT.lines().mapNotNull { ScriptCues.parse(it) }
        assertTrue("sample should contain at least one cue line", cues.isNotEmpty())
        assertTrue(cues.any { it is ScriptCue.Pause })
    }

    @Test
    fun sampleScript_cueLinesAreNotCountedAsWords() {
        val withCues = ScriptUtils.calculateWordCount(SampleScriptSeeder.CONTENT)
        val naive = SampleScriptSeeder.CONTENT.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
        assertTrue(withCues < naive)
    }
}
