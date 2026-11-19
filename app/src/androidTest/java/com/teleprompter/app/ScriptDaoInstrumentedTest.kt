package com.teleprompter.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.teleprompter.app.data.local.AppDatabase
import com.teleprompter.app.data.local.dao.ScriptDao
import com.teleprompter.app.data.local.entity.ScriptEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Runs the real Room/SQLite queries (the unit tests only use an in-memory fake). */
@RunWith(AndroidJUnit4::class)
class ScriptDaoInstrumentedTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ScriptDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.scriptDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun script(id: String, title: String = id, content: String = "content of $id", updatedAt: Long = 1L) =
        ScriptEntity(id = id, title = title, content = content, createdAt = 1L, updatedAt = updatedAt)

    @Test
    fun insertAndReadBack() = runBlocking {
        dao.insertScript(script("a", title = "Alpha"))
        assertEquals("Alpha", dao.getScriptByIdDirect("a")?.title)
    }

    @Test
    fun softDelete_hidesFromListAndAppearsInTrash() = runBlocking {
        dao.insertScript(script("keep"))
        dao.insertScript(script("gone"))

        dao.softDeleteScript("gone")

        assertEquals(listOf("keep"), dao.getAllScripts().first().map { it.id })
        assertEquals(listOf("gone"), dao.getDeletedScripts().first().map { it.id })
        assertNull(dao.getScriptByIdDirect("gone"))
    }

    @Test
    fun restore_bringsScriptBackAndBumpsUpdatedAt() = runBlocking {
        dao.insertScript(script("a", updatedAt = 5L))
        dao.softDeleteScript("a")

        dao.restoreScript("a", updatedAt = 99L)

        val restored = dao.getScriptByIdDirect("a")
        assertNotNull(restored)
        assertFalse(restored!!.isDeleted)
        assertEquals(99L, restored.updatedAt)
        assertTrue(dao.getDeletedScripts().first().isEmpty())
    }

    @Test
    fun getAllScriptsForSync_includesSoftDeletedRows() = runBlocking {
        dao.insertScript(script("live"))
        dao.insertScript(script("dead"))
        dao.softDeleteScript("dead")

        val ids = dao.getAllScriptsForSync().map { it.id }.toSet()
        assertEquals(setOf("live", "dead"), ids)
    }

    @Test
    fun search_matchesTitleAndContent_andIgnoresDeleted() = runBlocking {
        dao.insertScript(script("1", title = "Morning routine", content = "wake up"))
        dao.insertScript(script("2", title = "Other", content = "the routine continues"))
        dao.insertScript(script("3", title = "Routine deleted", content = "x"))
        dao.softDeleteScript("3")

        val hits = dao.searchScripts("routine").first().map { it.id }.toSet()
        assertEquals(setOf("1", "2"), hits)
    }

    @Test
    fun recentScripts_areOrderedNewestFirstAndLimited() = runBlocking {
        dao.insertScript(script("old", updatedAt = 1L))
        dao.insertScript(script("mid", updatedAt = 2L))
        dao.insertScript(script("new", updatedAt = 3L))

        assertEquals(listOf("new", "mid"), dao.getRecentScripts(2).first().map { it.id })
    }

    @Test
    fun updateLastReadPosition_persists() = runBlocking {
        dao.insertScript(script("a"))
        dao.updateLastReadPosition("a", 7)
        assertEquals(7, dao.getScriptByIdDirect("a")?.lastReadPosition)
    }
}
