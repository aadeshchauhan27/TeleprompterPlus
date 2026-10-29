package com.teleprompter.app.data.repository

import android.content.Context
import android.net.Uri
import com.teleprompter.app.core.util.ThumbnailUtils
import com.teleprompter.app.data.local.dao.RecordingDao
import com.teleprompter.app.data.local.entity.RecordingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

interface RecordingRepository {
    fun getAllRecordings(): Flow<List<RecordingEntity>>
    fun getRecordingsForScript(scriptId: String): Flow<List<RecordingEntity>>
    fun getRecordingById(id: String): Flow<RecordingEntity?>
    suspend fun saveRecording(
        scriptId: String?,
        title: String,
        tempVideoUri: Uri,
        durationSeconds: Long
    ): RecordingEntity?
    suspend fun renameRecording(id: String, newTitle: String)
    suspend fun deleteRecording(id: String): Boolean
    fun getRecordingsCount(): Flow<Int>
}

class RecordingRepositoryImpl(
    private val context: Context,
    private val recordingDao: RecordingDao
) : RecordingRepository {

    override fun getAllRecordings(): Flow<List<RecordingEntity>> =
        recordingDao.getAllRecordings()

    override fun getRecordingsForScript(scriptId: String): Flow<List<RecordingEntity>> =
        recordingDao.getRecordingsForScript(scriptId)

    override fun getRecordingById(id: String): Flow<RecordingEntity?> =
        recordingDao.getRecordingById(id)

    override suspend fun saveRecording(
        scriptId: String?,
        title: String,
        tempVideoUri: Uri,
        durationSeconds: Long
    ): RecordingEntity? = withContext(Dispatchers.IO) {
        val recordingId = UUID.randomUUID().toString()

        // Persistent recordings folder in app storage
        val recordingsDir = File(context.filesDir, "recordings").apply {
            if (!exists()) mkdirs()
        }
        val targetVideoFile = File(recordingsDir, "recording_$recordingId.mp4")

        // Copy temporary recorded video into persistent storage
        var fileSize = 0L
        try {
            val sourceFile = if (tempVideoUri.scheme == "file") {
                File(tempVideoUri.path ?: return@withContext null)
            } else {
                File(tempVideoUri.path ?: return@withContext null)
            }

            if (sourceFile.exists()) {
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(targetVideoFile).use { output ->
                        input.copyTo(output)
                    }
                }
                fileSize = targetVideoFile.length()
            }
        } catch (_: Exception) {
            return@withContext null
        }

        // Generate thumbnail
        val persistentUri = Uri.fromFile(targetVideoFile)
        val thumbnailPath = ThumbnailUtils.generateAndSaveThumbnail(
            context = context,
            videoUri = persistentUri,
            recordingId = recordingId
        )

        val entity = RecordingEntity(
            id = recordingId,
            scriptId = scriptId,
            title = title.ifBlank { "Untitled Recording" },
            videoUri = persistentUri.toString(),
            thumbnailPath = thumbnailPath,
            durationSeconds = durationSeconds,
            fileSizeBytes = fileSize,
            createdAt = System.currentTimeMillis()
        )

        recordingDao.insertRecording(entity)
        entity
    }

    override suspend fun renameRecording(id: String, newTitle: String) {
        withContext(Dispatchers.IO) {
            recordingDao.updateTitle(id, newTitle.trim())
        }
    }

    override suspend fun deleteRecording(id: String): Boolean = withContext(Dispatchers.IO) {
        val entity = recordingDao.getRecordingByIdDirect(id) ?: return@withContext false
        ThumbnailUtils.deleteRecordingFiles(entity.videoUri, entity.thumbnailPath)
        recordingDao.deleteRecording(id)
        true
    }

    override fun getRecordingsCount(): Flow<Int> =
        recordingDao.getRecordingsCount()
}
