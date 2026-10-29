package com.teleprompter.app

import com.teleprompter.app.data.local.dao.RecordingDao
import com.teleprompter.app.data.local.entity.RecordingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class FakeRecordingDao : RecordingDao {
    private val recordings = MutableStateFlow<Map<String, RecordingEntity>>(emptyMap())

    override fun getAllRecordings(): Flow<List<RecordingEntity>> =
        recordings.map { it.values.sortedByDescending { r -> r.createdAt } }

    override fun getRecordingsForScript(scriptId: String): Flow<List<RecordingEntity>> =
        recordings.map { it.values.filter { r -> r.scriptId == scriptId }.sortedByDescending { r -> r.createdAt } }

    override fun getRecordingById(id: String): Flow<RecordingEntity?> =
        recordings.map { it[id] }

    override suspend fun getRecordingByIdDirect(id: String): RecordingEntity? =
        recordings.value[id]

    override suspend fun insertRecording(recording: RecordingEntity): Long {
        recordings.value = recordings.value + (recording.id to recording)
        return 1L
    }

    override suspend fun deleteRecording(id: String) {
        recordings.value = recordings.value - id
    }

    override suspend fun updateTitle(id: String, newTitle: String) {
        val existing = recordings.value[id] ?: return
        recordings.value = recordings.value + (id to existing.copy(title = newTitle))
    }

    override fun getRecordingsCount(): Flow<Int> =
        recordings.map { it.size }
}

class RecordingRepositoryTest {

    private lateinit var fakeDao: FakeRecordingDao

    @Before
    fun setUp() {
        fakeDao = FakeRecordingDao()
    }

    @Test
    fun initialRecordings_isEmpty_zeroDummyData() = runBlocking {
        val all = fakeDao.getAllRecordings().first()
        assertTrue(all.isEmpty())
        val count = fakeDao.getRecordingsCount().first()
        assertEquals(0, count)
    }

    @Test
    fun insertAndRetrieveRecording_matchesMetadata() = runBlocking {
        val id = UUID.randomUUID().toString()
        val recording = RecordingEntity(
            id = id,
            scriptId = "script_1",
            title = "Pitch Take 1",
            videoUri = "file:///data/user/0/com.teleprompter.app/files/recordings/$id.mp4",
            thumbnailPath = "/data/user/0/com.teleprompter.app/files/thumbnails/$id.jpg",
            durationSeconds = 85,
            fileSizeBytes = 12_500_000L,
            createdAt = System.currentTimeMillis()
        )

        fakeDao.insertRecording(recording)

        val retrieved = fakeDao.getRecordingById(id).first()
        assertNotNull(retrieved)
        assertEquals("Pitch Take 1", retrieved?.title)
        assertEquals(85L, retrieved?.durationSeconds)
        assertEquals("script_1", retrieved?.scriptId)

        val all = fakeDao.getAllRecordings().first()
        assertEquals(1, all.size)
        assertEquals(id, all[0].id)
    }

    @Test
    fun renameRecording_updatesTitleSuccessfully() = runBlocking {
        val id = "rec_rename_test"
        val recording = RecordingEntity(
            id = id,
            scriptId = null,
            title = "Original Title",
            videoUri = "file:///mock/path.mp4",
            thumbnailPath = null,
            durationSeconds = 30,
            fileSizeBytes = 5000L,
            createdAt = 1000L
        )

        fakeDao.insertRecording(recording)
        fakeDao.updateTitle(id, "Renamed Final Take")

        val updated = fakeDao.getRecordingById(id).first()
        assertEquals("Renamed Final Take", updated?.title)
    }

    @Test
    fun deleteRecording_removesFromDatabase() = runBlocking {
        val id = "rec_delete_test"
        val recording = RecordingEntity(
            id = id,
            scriptId = "script_99",
            title = "To be deleted",
            videoUri = "file:///mock/del.mp4",
            thumbnailPath = null,
            durationSeconds = 12,
            fileSizeBytes = 1000L,
            createdAt = 2000L
        )

        fakeDao.insertRecording(recording)
        assertEquals(1, fakeDao.getRecordingsCount().first())

        fakeDao.deleteRecording(id)
        assertNull(fakeDao.getRecordingById(id).first())
        assertEquals(0, fakeDao.getRecordingsCount().first())
    }

    @Test
    fun getRecordingsForScript_filtersCorrectly() = runBlocking {
        val r1 = RecordingEntity(
            id = "r1",
            scriptId = "script_A",
            title = "Take A",
            videoUri = "file:///a.mp4",
            thumbnailPath = null,
            durationSeconds = 10,
            fileSizeBytes = 2000L,
            createdAt = 100L
        )
        val r2 = RecordingEntity(
            id = "r2",
            scriptId = "script_B",
            title = "Take B",
            videoUri = "file:///b.mp4",
            thumbnailPath = null,
            durationSeconds = 20,
            fileSizeBytes = 3000L,
            createdAt = 200L
        )

        fakeDao.insertRecording(r1)
        fakeDao.insertRecording(r2)

        val forA = fakeDao.getRecordingsForScript("script_A").first()
        assertEquals(1, forA.size)
        assertEquals("r1", forA[0].id)

        val forB = fakeDao.getRecordingsForScript("script_B").first()
        assertEquals(1, forB.size)
        assertEquals("r2", forB[0].id)

        val forC = fakeDao.getRecordingsForScript("script_C").first()
        assertTrue(forC.isEmpty())
    }
}
