package com.teleprompter.app

import com.teleprompter.app.core.util.ScriptCue
import com.teleprompter.app.core.util.ScriptCues
import com.teleprompter.app.core.util.ScriptUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScriptCuesTest {

    @Test
    fun parse_pauseVariants() {
        assertEquals(ScriptCue.Pause(2f), ScriptCues.parse("[pause]"))
        assertEquals(ScriptCue.Pause(3f), ScriptCues.parse("[pause 3]"))
        assertEquals(ScriptCue.Pause(2.5f), ScriptCues.parse("  [ Pause 2.5s ]  "))
    }

    @Test
    fun parse_pauseIsClamped() {
        assertEquals(ScriptCue.Pause(60f), ScriptCues.parse("[pause 999]"))
        assertEquals(ScriptCue.Pause(0.5f), ScriptCues.parse("[pause 0.1]"))
    }

    @Test
    fun parse_speedCues() {
        assertEquals(ScriptCue.Speed(ScriptCues.SLOW_FACTOR), ScriptCues.parse("[slow]"))
        assertEquals(ScriptCue.Speed(ScriptCues.FAST_FACTOR), ScriptCues.parse("[FAST]"))
        assertEquals(ScriptCue.Speed(1f), ScriptCues.parse("[normal]"))
    }

    @Test
    fun parse_normalTextIsNotACue() {
        assertNull(ScriptCues.parse("Hello [pause] world"))
        assertNull(ScriptCues.parse("[pause 3] and more"))
        assertNull(ScriptCues.parse("[unknown]"))
        assertNull(ScriptCues.parse(""))
    }

    @Test
    fun wordCount_ignoresCueLines() {
        val text = "Hello there\n[pause 2]\nhow are you\n[slow]"
        assertEquals(5, ScriptUtils.calculateWordCount(text))
        assertEquals(2, ScriptCues.totalPauseSeconds(text))
    }
}
