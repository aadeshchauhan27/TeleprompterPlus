package com.teleprompter.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

/** Remembers whether the first-launch walkthrough has been completed or skipped. */
object OnboardingPrefs {
    private val COMPLETED = booleanPreferencesKey("onboarding_completed")

    suspend fun isCompleted(context: Context): Boolean =
        context.dataStore.data.first()[COMPLETED] ?: false

    suspend fun markCompleted(context: Context) {
        context.dataStore.edit { it[COMPLETED] = true }
    }
}
