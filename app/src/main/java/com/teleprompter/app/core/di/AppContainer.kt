package com.teleprompter.app.core.di

import android.content.Context
import com.teleprompter.app.core.auth.AuthRepository
import com.teleprompter.app.core.auth.AuthRepositoryImpl
import com.teleprompter.app.core.camera.CameraEngine
import com.teleprompter.app.data.local.AppDatabase
import com.teleprompter.app.data.repository.FolderRepository
import com.teleprompter.app.data.repository.FolderRepositoryImpl
import com.teleprompter.app.data.repository.RecordingRepository
import com.teleprompter.app.data.repository.RecordingRepositoryImpl
import com.teleprompter.app.data.repository.ScriptRepository
import com.teleprompter.app.data.repository.ScriptRepositoryImpl
import com.teleprompter.app.data.repository.SettingsRepository
import com.teleprompter.app.data.repository.SettingsRepositoryImpl
import com.teleprompter.app.data.sync.SyncRepository
import com.teleprompter.app.data.sync.SyncRepositoryImpl

interface AppContainer {
    val scriptRepository: ScriptRepository
    val folderRepository: FolderRepository
    val settingsRepository: SettingsRepository
    val recordingRepository: RecordingRepository
    val cameraEngine: CameraEngine
    val authRepository: AuthRepository
    val syncRepository: SyncRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    override val scriptRepository: ScriptRepository by lazy {
        ScriptRepositoryImpl(database.scriptDao())
    }

    override val folderRepository: FolderRepository by lazy {
        FolderRepositoryImpl(database.folderDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context)
    }

    override val recordingRepository: RecordingRepository by lazy {
        RecordingRepositoryImpl(context, database.recordingDao())
    }

    override val cameraEngine: CameraEngine by lazy {
        CameraEngine(context)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(context)
    }

    override val syncRepository: SyncRepository by lazy {
        SyncRepositoryImpl(
            context = context,
            scriptDao = database.scriptDao(),
            settingsRepository = settingsRepository,
            authRepository = authRepository
        )
    }
}
