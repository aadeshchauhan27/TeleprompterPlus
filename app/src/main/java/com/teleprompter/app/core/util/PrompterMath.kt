package com.teleprompter.app.core.util

/** Pure arithmetic behind the prompter's scroll speed and its time-left / WPM readout. */
object PrompterMath {
    /** One speed step equals this many dp per second (density independent). */
    const val DP_PER_SECOND_PER_SPEED_STEP = 1.0f

    /** Scroll velocity in pixels per second for a speed setting on a screen with [density] px per dp. */
    fun pixelsPerSecond(speed: Int, density: Float, speedFactor: Float = 1f): Float =
        (speed * DP_PER_SECOND_PER_SPEED_STEP * density * speedFactor).coerceAtLeast(0f)

    /** Total time to scroll [paragraphCount] paragraphs of average height [avgItemPx]; 0 when unknown. */
    fun scrollSeconds(avgItemPx: Float, paragraphCount: Int, pxPerSecond: Float): Float {
        if (avgItemPx <= 0f || paragraphCount <= 0 || pxPerSecond <= 0f) return 0f
        return avgItemPx * paragraphCount / pxPerSecond
    }

    /** Words per minute implied by reading [wordCount] words over [scrollSeconds]; 0 when unknown. */
    fun wpm(wordCount: Int, scrollSeconds: Float): Int {
        if (wordCount <= 0 || scrollSeconds <= 0f) return 0
        return (wordCount / (scrollSeconds / 60f)).toInt()
    }

    /** Seconds left at [progress] (0..1), including the share of cue pauses still ahead. */
    fun remainingSeconds(scrollSeconds: Float, pauseSeconds: Int, progress: Float): Int {
        val left = 1f - progress.coerceIn(0f, 1f)
        return ((scrollSeconds + pauseSeconds) * left).toInt()
    }

    /** Formats seconds as m:ss. */
    fun formatClock(totalSeconds: Int): String {
        val s = totalSeconds.coerceAtLeast(0)
        return "%d:%02d".format(s / 60, s % 60)
    }
}
