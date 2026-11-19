package com.teleprompter.app

import android.app.Application
import com.teleprompter.app.core.di.AppContainer
import com.teleprompter.app.core.di.DefaultAppContainer
import com.teleprompter.app.data.repository.SampleScriptSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TelePrompterApp : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        com.teleprompter.app.data.sync.SyncWorker.schedule(this)
        appScope.launch {
            try {
                SampleScriptSeeder.seedIfNeeded(this@TelePrompterApp, container.scriptRepository)
            } catch (_: Exception) {
                // Seeding is best-effort; never block startup
            }
        }
    }
}
