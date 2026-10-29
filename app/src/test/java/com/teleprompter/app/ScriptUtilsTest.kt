package com.teleprompter.app

import com.teleprompter.app.core.util.ScriptUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class ScriptUtilsTest {

    @Test
    fun calculateWordCount_emptyString_returnsZero() {
        assertEquals(0, ScriptUtils.calculateWordCount(""))
        assertEquals(0, ScriptUtils.calculateWordCount("   "))
        assertEquals(0, ScriptUtils.calculateWordCount("\n\t\r"))
    }

    @Test
    fun calculateWordCount_normalText_returnsCorrectCount() {
        val text = "Welcome back to the channel. In today's video, we're going to talk."
        // 12 words
        assertEquals(12, ScriptUtils.calculateWordCount(text))
    }

    @Test
    fun calculateWordCount_multiLine_returnsCorrectCount() {
        val text = """
            Welcome back to the channel.

            In today's video, we're going to talk about how you can stay productive, focused, and consistent...

            Let's dive in!
        """.trimIndent()
        // 24 words
        assertEquals(24, ScriptUtils.calculateWordCount(text))
    }

    @Test
    fun calculateEstimatedReadingSeconds_variousWordCounts_returnsAccurateSeconds() {
        // At 130 WPM:
        // 130 words = 60s
        assertEquals(60, ScriptUtils.calculateEstimatedReadingSeconds(130, 130))
        // 260 words = 120s
        assertEquals(120, ScriptUtils.calculateEstimatedReadingSeconds(260, 130))
        // 0 words = 0s
        assertEquals(0, ScriptUtils.calculateEstimatedReadingSeconds(0, 130))
        // 1 word = 1s
        assertEquals(1, ScriptUtils.calculateEstimatedReadingSeconds(1, 130))
    }

    @Test
    fun formatReadingTime_returnsCleanReadableFormat() {
        assertEquals("0 min read", ScriptUtils.formatReadingTime(0))
        assertEquals("< 1 min read", ScriptUtils.formatReadingTime(30))
        assertEquals("1 min read", ScriptUtils.formatReadingTime(60))
        assertEquals("2 min read", ScriptUtils.formatReadingTime(120))
        assertEquals("5 min read", ScriptUtils.formatReadingTime(300))
    }

    @Test
    fun formatScriptMeta_matchesMockupFormat() {
        val result = ScriptUtils.formatScriptMeta(48, 120)
        assertEquals("48 words • 2 min read", result)
    }
}
