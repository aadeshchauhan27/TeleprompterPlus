package com.teleprompter.app.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ScriptUtils {

    /**
     * Calculates the word count of the given text.
     * Splits by whitespace and ignores empty tokens.
     */
    fun calculateWordCount(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split("\\s+".toRegex()).count { it.isNotBlank() }
    }

    /**
     * Estimates reading time in seconds assuming average speaking speed.
     * Default standard teleprompter pace is 130 words per minute.
     */
    fun calculateEstimatedReadingSeconds(wordCount: Int, wpm: Int = 130): Int {
        if (wordCount <= 0) return 0
        val seconds = (wordCount.toDouble() / wpm * 60).toInt()
        return if (seconds < 1) 1 else seconds
    }

    /**
     * Formats reading time into user-friendly string (e.g. "2 min read", "< 1 min read", "4 min 30 sec read").
     */
    fun formatReadingTime(seconds: Int): String {
        return when {
            seconds <= 0 -> "0 min read"
            seconds < 60 -> "< 1 min read"
            else -> {
                val mins = (seconds + 30) / 60
                "$mins min read"
            }
        }
    }

    /**
     * Formats metadata string matching reference mockup Screen 10 (e.g. "48 words • 2 min read time").
     */
    fun formatScriptMeta(wordCount: Int, readingSeconds: Int): String {
        val readTime = formatReadingTime(readingSeconds)
        return "$wordCount words • $readTime"
    }

    /**
     * Formats a timestamp into readable format.
     */
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
