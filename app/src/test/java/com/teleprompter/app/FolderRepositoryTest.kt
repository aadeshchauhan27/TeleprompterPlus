package com.teleprompter.app

import com.teleprompter.app.data.local.dao.FolderDao
import com.teleprompter.app.data.local.entity.FolderEntity
import com.teleprompter.app.data.local.model.FolderWithCount
import com.teleprompter.app.data.repository.FolderRepositoryImpl
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

class FakeFolderDao : FolderDao {
    private val folders = MutableStateFlow<Map<String, FolderEntity>>(emptyMap())

    override fun getAllFolders(): Flow<List<FolderEntity>> =
        folders.map { it.values.sortedBy { f -> f.createdAt } }

    override fun getFoldersWithCount(): Flow<List<FolderWithCount>> =
        folders.map {
            it.values.map { f ->
                FolderWithCount(
                    id = f.id,
                    name = f.name,
                    colorHex = f.colorHex,
                    createdAt = f.createdAt,
                    updatedAt = f.updatedAt,
                    scriptCount = 0
                )
            }.sortedBy { f -> f.createdAt }
        }

    override fun getFolderById(id: String): Flow<FolderEntity?> =
        folders.map { it[id] }

    override suspend fun getFolderByIdDirect(id: String): FolderEntity? =
        folders.value[id]

    override suspend fun insertFolder(folder: FolderEntity) {
        folders.value = folders.value + (folder.id to folder)
    }

    override suspend fun insertFolders(foldersList: List<FolderEntity>) {
        folders.value = folders.value + foldersList.associateBy { it.id }
    }

    override suspend fun updateFolder(folder: FolderEntity) {
        folders.value = folders.value + (folder.id to folder)
    }

    override suspend fun renameFolder(id: String, name: String, updatedAt: Long) {
        val existing = folders.value[id] ?: return
        folders.value = folders.value + (id to existing.copy(name = name, updatedAt = updatedAt))
    }

    override suspend fun deleteFolder(id: String) {
        folders.value = folders.value - id
    }

    override suspend fun clearFolderAssociation(folderId: String) {
        // No-op for folder test
    }
}

class FolderRepositoryTest {

    private lateinit var fakeDao: FakeFolderDao
    private lateinit var repository: FolderRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeFolderDao()
        repository = FolderRepositoryImpl(fakeDao)
    }

    @Test
    fun createFolder_savesFolderCorrectly() = runBlocking {
        val id = repository.createFolder("YouTube", "#3B82F6")
        val folder = repository.getFolderByIdDirect(id)

        assertNotNull(folder)
        assertEquals("YouTube", folder?.name)
        assertEquals("#3B82F6", folder?.colorHex)
    }

    @Test
    fun renameFolder_updatesNameCorrectly() = runBlocking {
        val id = repository.createFolder("Original", "#3B82F6")
        repository.renameFolder(id, "Renamed Folder")

        val folder = repository.getFolderByIdDirect(id)
        assertEquals("Renamed Folder", folder?.name)
    }

    @Test
    fun deleteFolder_removesFolder() = runBlocking {
        val id = repository.createFolder("To Delete", "#3B82F6")
        repository.deleteFolder(id)

        val folder = repository.getFolderByIdDirect(id)
        assertNull(folder)
        val all = repository.getAllFolders().first()
        assertTrue(all.isEmpty())
    }
}
