package com.teleprompter.app

import android.app.Application
import com.teleprompter.app.core.di.AppContainer
import com.teleprompter.app.core.di.DefaultAppContainer

class TelePrompterApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
