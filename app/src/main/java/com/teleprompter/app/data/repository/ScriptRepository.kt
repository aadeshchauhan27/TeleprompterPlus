package com.teleprompter.app.data.repository

import com.teleprompter.app.core.util.ScriptUtils
import com.teleprompter.app.data.local.dao.ScriptDao
import com.teleprompter.app.data.local.entity.ScriptEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface ScriptRepository {
    fun getAllScripts(): Flow<List<ScriptEntity>>
    fun getRecentScripts(limit: Int = 10): Flow<List<ScriptEntity>>
    fun getFavoriteScripts(): Flow<List<ScriptEntity>>
    fun getScriptsInFolder(folderId: String): Flow<List<ScriptEntity>>
    fun searchScripts(query: String): Flow<List<ScriptEntity>>
    fun getScriptById(id: String): Flow<ScriptEntity?>
    suspend fun getScriptByIdDirect(id: String): ScriptEntity?
    suspend fun createScript(title: String, content: String, folderId: String? = null): String
    suspend fun updateScript(script: ScriptEntity)
    suspend fun deleteScript(id: String)
    suspend fun duplicateScript(id: String): String?
    suspend fun toggleFavorite(id: String, currentStatus: Boolean)
    suspend fun moveToFolder(id: String, folderId: String?)
    suspend fun updateLastReadPosition(id: String, position: Int)
}

class ScriptRepositoryImpl(
    private val scriptDao: ScriptDao
) : ScriptRepository {

    override fun getAllScripts(): Flow<List<ScriptEntity>> = scriptDao.getAllScripts()

    override fun getRecentScripts(limit: Int): Flow<List<ScriptEntity>> =
        scriptDao.getRecentScripts(limit)

    override fun getFavoriteScripts(): Flow<List<ScriptEntity>> =
        scriptDao.getFavoriteScripts()

    override fun getScriptsInFolder(folderId: String): Flow<List<ScriptEntity>> =
        scriptDao.getScriptsInFolder(folderId)

    override fun searchScripts(query: String): Flow<List<ScriptEntity>> =
        scriptDao.searchScripts(query)

    override fun getScriptById(id: String): Flow<ScriptEntity?> =
        scriptDao.getScriptById(id)

    override suspend fun getScriptByIdDirect(id: String): ScriptEntity? =
        scriptDao.getScriptByIdDirect(id)

    override suspend fun createScript(title: String, content: String, folderId: String?): String {
        val wordCount = ScriptUtils.calculateWordCount(content)
        val readSeconds = ScriptUtils.calculateEstimatedReadingSeconds(wordCount)
        val newId = UUID.randomUUID().toString()
        val script = ScriptEntity(
            id = newId,
            title = title.ifBlank { "Untitled Script" },
            content = content,
            folderId = folderId,
            wordCount = wordCount,
            estimatedReadingTime = readSeconds,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        scriptDao.insertScript(script)
        return newId
    }

    override suspend fun updateScript(script: ScriptEntity) {
        val wordCount = ScriptUtils.calculateWordCount(script.content)
        val readSeconds = ScriptUtils.calculateEstimatedReadingSeconds(wordCount)
        val updated = script.copy(
            wordCount = wordCount,
            estimatedReadingTime = readSeconds,
            updatedAt = System.currentTimeMillis()
        )
        scriptDao.updateScript(updated)
    }

    override suspend fun deleteScript(id: String) {
        scriptDao.deleteScript(id)
    }

    override suspend fun duplicateScript(id: String): String? {
        val original = scriptDao.getScriptByIdDirect(id) ?: return null
        val newId = UUID.randomUUID().toString()
        val copy = original.copy(
            id = newId,
            title = "${original.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isFavorite = false
        )
        scriptDao.insertScript(copy)
        return newId
    }

    override suspend fun toggleFavorite(id: String, currentStatus: Boolean) {
        scriptDao.setFavorite(id, !currentStatus)
    }

    override suspend fun moveToFolder(id: String, folderId: String?) {
        scriptDao.moveToFolder(id, folderId)
    }

    override suspend fun updateLastReadPosition(id: String, position: Int) {
        scriptDao.updateLastReadPosition(id, position)
    }
}
