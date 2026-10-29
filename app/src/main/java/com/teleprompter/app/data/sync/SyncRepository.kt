package com.teleprompter.app.data.sync

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.teleprompter.app.core.auth.AuthRepository
import com.teleprompter.app.core.model.TeleprompterSettings
import com.teleprompter.app.data.local.dao.ScriptDao
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.data.repository.SettingsRepository
import com.teleprompter.app.data.repository.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

interface SyncRepository {
    val syncStatus: StateFlow<SyncStatus>
    val isCloudSyncEnabled: StateFlow<Boolean>
    val lastSyncTime: StateFlow<Long>
    suspend fun setCloudSyncEnabled(enabled: Boolean)
    suspend fun syncNow(): Result<Unit>
}

class SyncRepositoryImpl(
    private val context: Context,
    private val scriptDao: ScriptDao,
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : SyncRepository {

    private object SyncKeys {
        val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_timestamp")
    }

    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (_: Exception) {
            false
        }

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    override val isCloudSyncEnabled: StateFlow<Boolean> = context.dataStore.data
        .map { it[SyncKeys.CLOUD_SYNC_ENABLED] ?: true }
        .stateIn(scope, SharingStarted.Eagerly, true)

    override val lastSyncTime: StateFlow<Long> = context.dataStore.data
        .map { it[SyncKeys.LAST_SYNC_TIME] ?: 0L }
        .stateIn(scope, SharingStarted.Eagerly, 0L)

    override suspend fun setCloudSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SyncKeys.CLOUD_SYNC_ENABLED] = enabled }
        if (enabled) {
            syncNow()
        } else {
            _syncStatus.value = SyncStatus.DISABLED
        }
    }

    override suspend fun syncNow(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = authRepository.currentUser.value
        if (user == null || user.isAnonymous) {
            _syncStatus.value = SyncStatus.IDLE
            return@withContext Result.success(Unit)
        }

        val syncEnabled = isCloudSyncEnabled.first()
        if (!syncEnabled) {
            _syncStatus.value = SyncStatus.DISABLED
            return@withContext Result.success(Unit)
        }

        if (!isFirebaseAvailable) {
            // Firebase project not configured or offline: handle gracefully without error
            _syncStatus.value = SyncStatus.OFFLINE
            return@withContext Result.success(Unit)
        }

        _syncStatus.value = SyncStatus.SYNCING

        try {
            val firestore = FirebaseFirestore.getInstance()
            val userId = user.id
            val userScriptsCollection = firestore.collection("users").document(userId).collection("scripts")

            // 1. Fetch remote scripts
            val remoteSnapshot = try {
                userScriptsCollection.get().await()
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.OFFLINE
                return@withContext Result.failure(e)
            }

            val remoteScriptsMap = mutableMapOf<String, Map<String, Any>>()
            for (doc in remoteSnapshot.documents) {
                val data = doc.data
                if (data != null) {
                    remoteScriptsMap[doc.id] = data
                }
            }

            // 2. Fetch all local scripts (including soft-deleted)
            val localScripts = scriptDao.getAllScripts().first()
            val localScriptsMap = localScripts.associateBy { it.id }

            // 3. Process Local -> Remote Sync
            for (local in localScripts) {
                val remoteData = remoteScriptsMap[local.id]
                val remoteUpdatedAt = (remoteData?.get("updatedAt") as? Number)?.toLong() ?: 0L

                if (remoteData == null || local.updatedAt >= remoteUpdatedAt) {
                    // Local is newer or doesn't exist remotely -> push to cloud
                    val scriptMap = mapOf(
                        "id" to local.id,
                        "title" to local.title,
                        "content" to local.content,
                        "folderId" to (local.folderId ?: ""),
                        "isFavorite" to local.isFavorite,
                        "wordCount" to local.wordCount,
                        "estimatedReadingTime" to local.estimatedReadingTime,
                        "scrollSpeed" to local.scrollSpeed,
                        "fontSize" to local.fontSize,
                        "fontStyle" to local.fontStyle,
                        "textColorHex" to local.textColorHex,
                        "bgOpacity" to local.bgOpacity.toDouble(),
                        "textOpacity" to local.textOpacity.toDouble(),
                        "readingWidthPercent" to local.readingWidthPercent,
                        "verticalPositionPercent" to local.verticalPositionPercent,
                        "lineSpacingMultiplier" to local.lineSpacingMultiplier.toDouble(),
                        "letterSpacingSp" to local.letterSpacingSp.toDouble(),
                        "textAlignment" to local.textAlignment,
                        "mirrorText" to local.mirrorText,
                        "showSafeAreaGuide" to local.showSafeAreaGuide,
                        "presetType" to local.presetType,
                        "hasCustomSettings" to local.hasCustomSettings,
                        "isDeleted" to local.isDeleted,
                        "updatedAt" to local.updatedAt,
                        "createdAt" to local.createdAt
                    )
                    userScriptsCollection.document(local.id).set(scriptMap, SetOptions.merge()).await()
                }
            }

            // 4. Process Remote -> Local Sync (Deterministic conflict strategy)
            for ((scriptId, remote) in remoteScriptsMap) {
                val remoteUpdatedAt = (remote["updatedAt"] as? Number)?.toLong() ?: 0L
                val local = localScriptsMap[scriptId]

                if (local == null || remoteUpdatedAt > local.updatedAt) {
                    val isDeleted = remote["isDeleted"] as? Boolean ?: false
                    if (!isDeleted) {
                        val mergedEntity = ScriptEntity(
                            id = scriptId,
                            title = remote["title"] as? String ?: "Untitled Script",
                            content = remote["content"] as? String ?: "",
                            folderId = (remote["folderId"] as? String)?.ifBlank { null },
                            isFavorite = remote["isFavorite"] as? Boolean ?: false,
                            wordCount = (remote["wordCount"] as? Number)?.toInt() ?: 0,
                            estimatedReadingTime = (remote["estimatedReadingTime"] as? Number)?.toInt() ?: 0,
                            scrollSpeed = (remote["scrollSpeed"] as? Number)?.toInt() ?: 50,
                            fontSize = (remote["fontSize"] as? Number)?.toInt() ?: 28,
                            fontStyle = remote["fontStyle"] as? String ?: "SANS",
                            textColorHex = remote["textColorHex"] as? String ?: "#FFFFFF",
                            bgOpacity = (remote["bgOpacity"] as? Number)?.toFloat() ?: 0.5f,
                            textOpacity = (remote["textOpacity"] as? Number)?.toFloat() ?: 1.0f,
                            readingWidthPercent = (remote["readingWidthPercent"] as? Number)?.toInt() ?: 85,
                            verticalPositionPercent = (remote["verticalPositionPercent"] as? Number)?.toInt() ?: 25,
                            lineSpacingMultiplier = (remote["lineSpacingMultiplier"] as? Number)?.toFloat() ?: 1.2f,
                            letterSpacingSp = (remote["letterSpacingSp"] as? Number)?.toFloat() ?: 0.0f,
                            textAlignment = remote["textAlignment"] as? String ?: "CENTER",
                            mirrorText = remote["mirrorText"] as? Boolean ?: false,
                            showSafeAreaGuide = remote["showSafeAreaGuide"] as? Boolean ?: true,
                            presetType = remote["presetType"] as? String ?: "CUSTOM",
                            hasCustomSettings = remote["hasCustomSettings"] as? Boolean ?: false,
                            isDeleted = false,
                            isSynced = true,
                            createdAt = (remote["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            updatedAt = remoteUpdatedAt
                        )
                        scriptDao.insertScript(mergedEntity)
                    } else if (local != null) {
                        scriptDao.deleteScript(scriptId)
                    }
                }
            }

            // 5. Sync User Settings to Cloud (never videos!)
            val currentSettings = settingsRepository.settingsFlow.first()
            val settingsDoc = firestore.collection("users").document(userId).collection("settings").document("preferences")
            val settingsMap = mapOf(
                "scrollSpeed" to currentSettings.scrollSpeed,
                "fontSize" to currentSettings.fontSize,
                "fontStyle" to currentSettings.fontStyle.name,
                "textColorHex" to currentSettings.textColorHex,
                "bgOpacity" to currentSettings.bgOpacity.toDouble(),
                "textOpacity" to currentSettings.textOpacity.toDouble(),
                "readingWidthPercent" to currentSettings.readingWidthPercent,
                "verticalPositionPercent" to currentSettings.verticalPositionPercent,
                "presetType" to currentSettings.presetType.name,
                "textAlignment" to currentSettings.textAlignment.name,
                "mirrorText" to currentSettings.mirrorText,
                "showSafeAreaGuide" to currentSettings.showSafeAreaGuide,
                "countdownSeconds" to currentSettings.countdownSeconds,
                "updatedAt" to System.currentTimeMillis()
            )
            settingsDoc.set(settingsMap, SetOptions.merge()).await()

            // Update last sync time
            val now = System.currentTimeMillis()
            context.dataStore.edit { it[SyncKeys.LAST_SYNC_TIME] = now }
            _syncStatus.value = SyncStatus.SUCCESS
            Result.success(Unit)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        }
    }
}
