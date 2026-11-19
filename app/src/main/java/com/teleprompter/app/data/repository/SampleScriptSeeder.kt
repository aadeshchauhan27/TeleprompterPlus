package com.teleprompter.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

/** Adds a one-time sample script on first launch so new users can try the prompter immediately. */
object SampleScriptSeeder {
    private val SEEDED = booleanPreferencesKey("sample_script_seeded")

    const val TITLE = "Welcome to Teleprompter Plus"

    val CONTENT = """
Welcome to Teleprompter Plus!

This is a sample script so you can try everything right away. Tap Play and read along as the text scrolls.

Controls while the prompter is running:
Tap anywhere on the screen to play or pause.
Drag the text with your finger to move to any spot.
Open the Tune button to change the speed, font size, spacing and colors.
Use volume up and down, or a Bluetooth remote, to change the speed.

Tip: place your phone just below the camera lens and keep your eyes close to the lens. The highlighted band shows the line you should be reading.

Want to record yourself? Go back to the script page and tap Record. The script scrolls over the camera preview while you speak, and your video is saved to the Recordings tab.

Director cues: put [pause 3], [slow], [fast] or [normal] on a line of its own and the prompter will react. They are not read aloud. Here is a two second pause:
[pause 2]
Nice. You are back in the flow.

Try changing the settings now. Make the text bigger. Slow it down. Switch the font. Turn on mirror mode if you plan to use a beam splitter glass.

When you are ready, create your own script with the plus button. You can type it, paste it from the clipboard, or import a text file.

Good luck, and happy recording!
""".trimIndent()

    suspend fun seedIfNeeded(context: Context, scriptRepository: ScriptRepository) {
        val already = context.dataStore.data.first()[SEEDED] ?: false
        if (already) return
        scriptRepository.createScript(title = TITLE, content = CONTENT)
        context.dataStore.edit { it[SEEDED] = true }
    }
}
