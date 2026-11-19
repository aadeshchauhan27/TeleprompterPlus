package com.teleprompter.app

import com.teleprompter.app.core.util.PrompterMath
import org.junit.Assert.assertEquals
import org.junit.Test

class PrompterMathTest {

    @Test
    fun pixelsPerSecond_scalesWithSpeedAndDensity() {
        assertEquals(50f * 2f, PrompterMath.pixelsPerSecond(speed = 50, density = 2f), 0.001f)
        assertEquals(50f * 3.5f, PrompterMath.pixelsPerSecond(speed = 50, density = 3.5f), 0.001f)
    }

    @Test
    fun pixelsPerSecond_appliesCueSpeedFactor() {
        val normal = PrompterMath.pixelsPerSecond(60, 2f)
        assertEquals(normal * 0.6f, PrompterMath.pixelsPerSecond(60, 2f, speedFactor = 0.6f), 0.001f)
        assertEquals(normal * 1.5f, PrompterMath.pixelsPerSecond(60, 2f, speedFactor = 1.5f), 0.001f)
    }

    @Test
    fun pixelsPerSecond_neverNegative() {
        assertEquals(0f, PrompterMath.pixelsPerSecond(-10, 2f), 0f)
        assertEquals(0f, PrompterMath.pixelsPerSecond(50, 2f, speedFactor = -1f), 0f)
    }

    @Test
    fun scrollSeconds_isDistanceOverSpeed() {
        // 20 paragraphs * 100px = 2000px at 100px/s = 20s
        assertEquals(20f, PrompterMath.scrollSeconds(100f, 20, 100f), 0.001f)
    }

    @Test
    fun scrollSeconds_unknownInputsGiveZero() {
        assertEquals(0f, PrompterMath.scrollSeconds(0f, 20, 100f), 0f)
        assertEquals(0f, PrompterMath.scrollSeconds(100f, 0, 100f), 0f)
        assertEquals(0f, PrompterMath.scrollSeconds(100f, 20, 0f), 0f)
    }

    @Test
    fun wpm_isWordsPerScrollMinute() {
        // 130 words over 60s = 130 WPM; over 30s = 260 WPM
        assertEquals(130, PrompterMath.wpm(130, 60f))
        assertEquals(260, PrompterMath.wpm(130, 30f))
    }

    @Test
    fun wpm_unknownGivesZero() {
        assertEquals(0, PrompterMath.wpm(0, 60f))
        assertEquals(0, PrompterMath.wpm(130, 0f))
    }

    @Test
    fun remainingSeconds_shrinksWithProgressAndIncludesPauses() {
        assertEquals(60, PrompterMath.remainingSeconds(scrollSeconds = 50f, pauseSeconds = 10, progress = 0f))
        assertEquals(30, PrompterMath.remainingSeconds(50f, 10, 0.5f))
        assertEquals(0, PrompterMath.remainingSeconds(50f, 10, 1f))
    }

    @Test
    fun remainingSeconds_clampsProgress() {
        assertEquals(60, PrompterMath.remainingSeconds(50f, 10, -1f))
        assertEquals(0, PrompterMath.remainingSeconds(50f, 10, 5f))
    }

    @Test
    fun formatClock_formatsMinutesAndSeconds() {
        assertEquals("0:00", PrompterMath.formatClock(0))
        assertEquals("0:05", PrompterMath.formatClock(5))
        assertEquals("1:30", PrompterMath.formatClock(90))
        assertEquals("12:09", PrompterMath.formatClock(729))
        assertEquals("0:00", PrompterMath.formatClock(-4))
    }
}
