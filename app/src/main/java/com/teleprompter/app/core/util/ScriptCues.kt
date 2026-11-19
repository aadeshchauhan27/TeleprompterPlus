package com.teleprompter.app.core.util

/**
 * Inline director cues written on their own line of a script, e.g.
 *   [pause]  [pause 3]  [pause 2.5s]  [slow]  [fast]  [normal]
 * Cues are not read aloud: they are shown as small hints and drive the prompter.
 */
sealed interface ScriptCue {
    /** Stop scrolling for [seconds], then continue. */
    data class Pause(val seconds: Float) : ScriptCue

    /** Multiply scroll speed by [factor] from this point on. */
    data class Speed(val factor: Float) : ScriptCue

    val label: String
        get() = when (this) {
            is Pause -> "⏸ pause ${if (seconds % 1f == 0f) seconds.toInt() else seconds}s"
            is Speed -> when {
                factor < 1f -> "🐢 slow down"
                factor > 1f -> "⚡ speed up"
                else -> "▶ normal pace"
            }
        }
}

object ScriptCues {
    const val DEFAULT_PAUSE_SECONDS = 2f
    const val MAX_PAUSE_SECONDS = 60f
    const val SLOW_FACTOR = 0.6f
    const val FAST_FACTOR = 1.5f

    private val CUE = Regex(
        """^\s*\[\s*(pause|slow|fast|normal)(?:\s+(\d+(?:\.\d+)?)\s*s?)?\s*]\s*$""",
        RegexOption.IGNORE_CASE
    )

    /** Returns the cue represented by this whole line, or null if the line is normal script text. */
    fun parse(line: String): ScriptCue? {
        val m = CUE.matchEntire(line) ?: return null
        return when (m.groupValues[1].lowercase()) {
            "pause" -> {
                val secs = m.groupValues[2].toFloatOrNull() ?: DEFAULT_PAUSE_SECONDS
                ScriptCue.Pause(secs.coerceIn(0.5f, MAX_PAUSE_SECONDS))
            }
            "slow" -> ScriptCue.Speed(SLOW_FACTOR)
            "fast" -> ScriptCue.Speed(FAST_FACTOR)
            else -> ScriptCue.Speed(1f)
        }
    }

    /** Script text with cue lines removed (used for word counts and reading time). */
    fun stripCues(text: String): String =
        text.lineSequence().filter { parse(it) == null }.joinToString("\n")

    /** Total pause time declared by cues, in seconds. */
    fun totalPauseSeconds(text: String): Int =
        text.lineSequence().mapNotNull { parse(it) as? ScriptCue.Pause }.sumOf { it.seconds.toDouble() }.toInt()
}
