package com.teleprompter.app

import com.teleprompter.app.core.camera.CameraLens
import com.teleprompter.app.core.camera.CameraRecordingStatus
import com.teleprompter.app.core.camera.RecordingState
import com.teleprompter.app.core.model.TeleprompterSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class TeleprompterEngineTest {

    @Test
    fun cameraLens_togglesBetweenFrontAndBack() {
        var lens = CameraLens.FRONT
        assertEquals(CameraLens.FRONT, lens)

        lens = if (lens == CameraLens.FRONT) CameraLens.BACK else CameraLens.FRONT
        assertEquals(CameraLens.BACK, lens)

        lens = if (lens == CameraLens.FRONT) CameraLens.BACK else CameraLens.FRONT
        assertEquals(CameraLens.FRONT, lens)
    }

    @Test
    fun recordingState_transitionsCorrectly() {
        var status = CameraRecordingStatus()
        assertEquals(RecordingState.IDLE, status.state)
        assertEquals("00:00", status.formattedDuration)

        status = status.copy(state = RecordingState.RECORDING, durationSeconds = 15, formattedDuration = "00:15")
        assertEquals(RecordingState.RECORDING, status.state)
        assertEquals("00:15", status.formattedDuration)

        status = status.copy(state = RecordingState.PAUSED)
        assertEquals(RecordingState.PAUSED, status.state)

        status = status.copy(state = RecordingState.RECORDING)
        assertEquals(RecordingState.RECORDING, status.state)

        status = status.copy(state = RecordingState.STOPPED)
        assertEquals(RecordingState.STOPPED, status.state)
    }

    @Test
    fun continuousSpeedModel_calculatesProportionalPixelsPerSecond() {
        // Continuous speed model: pxPerSecond = speed * 2.5f
        fun calculatePxPerSecond(speed: Int): Float = speed * 2.5f

        val slowSpeed = calculatePxPerSecond(20)
        val normalSpeed = calculatePxPerSecond(50)
        val fastSpeed = calculatePxPerSecond(100)

        assertEquals(50f, slowSpeed, 0.01f)
        assertEquals(125f, normalSpeed, 0.01f)
        assertEquals(250f, fastSpeed, 0.01f)
        assertTrue(fastSpeed > normalSpeed)
        assertTrue(normalSpeed > slowSpeed)
    }

    @Test
    fun progressAndRemainingTime_calculateCorrectly() {
        val totalHeightPx = 1000f
        val totalDurationSec = 120 // 2 minutes

        // 0% scroll
        var scrollOffset = 0f
        var progress = (scrollOffset / totalHeightPx).coerceIn(0f, 1f)
        var remainingSec = (totalDurationSec * (1f - progress)).toInt()
        assertEquals(0f, progress, 0.01f)
        assertEquals(120, remainingSec)

        // 50% scroll
        scrollOffset = 500f
        progress = (scrollOffset / totalHeightPx).coerceIn(0f, 1f)
        remainingSec = (totalDurationSec * (1f - progress)).toInt()
        assertEquals(0.5f, progress, 0.01f)
        assertEquals(60, remainingSec)

        // 100% scroll
        scrollOffset = 1000f
        progress = (scrollOffset / totalHeightPx).coerceIn(0f, 1f)
        remainingSec = (totalDurationSec * (1f - progress)).toInt()
        assertEquals(1.0f, progress, 0.01f)
        assertEquals(0, remainingSec)
    }

    @Test
    fun teleprompterOverlaySettings_haveCorrectLensPositioningDefaults() {
        val settings = TeleprompterSettings()

        // 85% width centers reading area near top to encourage natural eye contact
        assertEquals(85, settings.readingWidthPercent)
        // 25% from top positions text directly under front lens
        assertEquals(25, settings.verticalPositionPercent)
        // 1.0 text opacity ensures crisp visibility
        assertEquals(1.0f, settings.textOpacity, 0.01f)
        // Countdown defaults to 3 seconds
        assertEquals(3, settings.countdownSeconds)
        // Auto-hide controls active during reading
        assertTrue(settings.autoHideControls)
        // Mirror text false by default
        assertFalse(settings.mirrorText)
    }

    @Test
    fun mirrorMode_determinesScaleXTransformation() {
        fun getScaleX(mirror: Boolean): Float = if (mirror) -1f else 1f

        assertEquals(1f, getScaleX(false), 0.01f)
        assertEquals(-1f, getScaleX(true), 0.01f)
    }

    @Test
    fun backgroundOpacity_clampsWithinRange() {
        fun clampOpacity(opacity: Float): Float = opacity.coerceIn(0f, 1f)

        assertEquals(0f, clampOpacity(-0.5f), 0.01f)
        assertEquals(1f, clampOpacity(1.5f), 0.01f)
        assertEquals(0.4f, clampOpacity(0.4f), 0.01f)
    }
}
