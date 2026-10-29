package com.teleprompter.app

import com.teleprompter.app.data.local.dao.ScriptDao
import com.teleprompter.app.data.local.entity.ScriptEntity
import com.teleprompter.app.data.repository.ScriptRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeScriptDao : ScriptDao {
    private val scripts = MutableStateFlow<Map<String, ScriptEntity>>(emptyMap())

    override fun getAllScripts(): Flow<List<ScriptEntity>> =
        scripts.map { it.values.filter { s -> !s.isDeleted }.sortedByDescending { s -> s.updatedAt } }

    override fun getRecentScripts(limit: Int): Flow<List<ScriptEntity>> =
        scripts.map { it.values.filter { s -> !s.isDeleted }.sortedByDescending { s -> s.updatedAt }.take(limit) }

    override fun getFavoriteScripts(): Flow<List<ScriptEntity>> =
        scripts.map { it.values.filter { s -> !s.isDeleted && s.isFavorite }.sortedByDescending { s -> s.updatedAt } }

    override fun getScriptsInFolder(folderId: String): Flow<List<ScriptEntity>> =
        scripts.map { it.values.filter { s -> !s.isDeleted && s.folderId == folderId }.sortedByDescending { s -> s.updatedAt } }

    override fun searchScripts(query: String): Flow<List<ScriptEntity>> =
        scripts.map {
            it.values.filter { s ->
                !s.isDeleted && (s.title.contains(query, ignoreCase = true) || s.content.contains(query, ignoreCase = true))
            }.sortedByDescending { s -> s.updatedAt }
        }

    override fun getScriptById(id: String): Flow<ScriptEntity?> =
        scripts.map { it[id]?.takeIf { s -> !s.isDeleted } }

    override suspend fun getScriptByIdDirect(id: String): ScriptEntity? =
        scripts.value[id]?.takeIf { !it.isDeleted }

    override suspend fun insertScript(script: ScriptEntity) {
        scripts.value = scripts.value + (script.id to script)
    }

    override suspend fun insertScripts(scripts: List<ScriptEntity>) {
        this.scripts.value = this.scripts.value + scripts.associateBy { it.id }
    }

    override suspend fun updateScript(script: ScriptEntity) {
        scripts.value = scripts.value + (script.id to script)
    }

    override suspend fun setFavorite(id: String, isFavorite: Boolean, updatedAt: Long) {
        val existing = scripts.value[id] ?: return
        scripts.value = scripts.value + (id to existing.copy(isFavorite = isFavorite, updatedAt = updatedAt))
    }

    override suspend fun moveToFolder(id: String, folderId: String?, updatedAt: Long) {
        val existing = scripts.value[id] ?: return
        scripts.value = scripts.value + (id to existing.copy(folderId = folderId, updatedAt = updatedAt))
    }

    override suspend fun updateLastReadPosition(id: String, position: Int) {
        val existing = scripts.value[id] ?: return
        scripts.value = scripts.value + (id to existing.copy(lastReadPosition = position))
    }

    override suspend fun deleteScript(id: String) {
        scripts.value = scripts.value - id
    }

    override suspend fun softDeleteScript(id: String, updatedAt: Long) {
        val existing = scripts.value[id] ?: return
        scripts.value = scripts.value + (id to existing.copy(isDeleted = true, updatedAt = updatedAt))
    }

    override fun getScriptCount(): Flow<Int> =
        scripts.map { it.values.count { s -> !s.isDeleted } }
}

class ScriptRepositoryTest {

    private lateinit var fakeDao: FakeScriptDao
    private lateinit var repository: ScriptRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeScriptDao()
        repository = ScriptRepositoryImpl(fakeDao)
    }

    @Test
    fun createScript_persistsCorrectWordCountAndTitle() = runBlocking {
        val title = "YouTube Video Intro"
        val content = "Welcome back to the channel. Today we talk about productivity."
        val id = repository.createScript(title, content)

        val retrieved = repository.getScriptByIdDirect(id)
        assertNotNull(retrieved)
        assertEquals(title, retrieved?.title)
        assertEquals(content, retrieved?.content)
        assertEquals(10, retrieved?.wordCount)
        assertTrue(retrieved?.estimatedReadingTime ?: 0 > 0)
    }

    @Test
    fun duplicateScript_createsExactCopy() = runBlocking {
        val id = repository.createScript("Original Script", "Some content here")
        val duplicateId = repository.duplicateScript(id)

        assertNotNull(duplicateId)
        val duplicate = repository.getScriptByIdDirect(duplicateId!!)
        assertEquals("Original Script (Copy)", duplicate?.title)
        assertEquals("Some content here", duplicate?.content)
        assertFalse(duplicate?.isFavorite ?: true)
    }

    @Test
    fun toggleFavorite_flipsStatusCorrectly() = runBlocking {
        val id = repository.createScript("Favorite Test", "Content")
        val initial = repository.getScriptByIdDirect(id)
        assertFalse(initial?.isFavorite ?: true)

        repository.toggleFavorite(id, false)
        val updated = repository.getScriptByIdDirect(id)
        assertTrue(updated?.isFavorite ?: false)

        repository.toggleFavorite(id, true)
        val reverted = repository.getScriptByIdDirect(id)
        assertFalse(reverted?.isFavorite ?: true)
    }

    @Test
    fun deleteScript_removesScriptPermanently() = runBlocking {
        val id = repository.createScript("To Delete", "Content")
        assertNotNull(repository.getScriptByIdDirect(id))

        repository.deleteScript(id)
        val all = repository.getAllScripts().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun scriptSpecificPreferences_areSavedAndRestoredIndependently() = runBlocking {
        val idA = repository.createScript("Script A", "Content A")
        val idB = repository.createScript("Script B", "Content B")

        val scriptA = repository.getScriptByIdDirect(idA)!!
        val scriptB = repository.getScriptByIdDirect(idB)!!

        // Script A customized: Speed = 65, Font = 36, Alignment = LEFT
        val updatedA = scriptA.copy(
            scrollSpeed = 65,
            fontSize = 36,
            textAlignment = "LEFT",
            hasCustomSettings = true
        )
        repository.updateScript(updatedA)

        // Script B customized: Speed = 35, Font = 22, Alignment = RIGHT
        val updatedB = scriptB.copy(
            scrollSpeed = 35,
            fontSize = 22,
            textAlignment = "RIGHT",
            hasCustomSettings = true
        )
        repository.updateScript(updatedB)

        val retrievedA = repository.getScriptByIdDirect(idA)!!
        val retrievedB = repository.getScriptByIdDirect(idB)!!

        assertEquals(65, retrievedA.scrollSpeed)
        assertEquals(36, retrievedA.fontSize)
        assertEquals("LEFT", retrievedA.textAlignment)
        assertTrue(retrievedA.hasCustomSettings)

        assertEquals(35, retrievedB.scrollSpeed)
        assertEquals(22, retrievedB.fontSize)
        assertEquals("RIGHT", retrievedB.textAlignment)
        assertTrue(retrievedB.hasCustomSettings)
    }
}
