package com.teleprompter.app.data.repository

import com.teleprompter.app.data.local.dao.FolderDao
import com.teleprompter.app.data.local.entity.FolderEntity
import com.teleprompter.app.data.local.model.FolderWithCount
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface FolderRepository {
    fun getAllFolders(): Flow<List<FolderEntity>>
    fun getFoldersWithCount(): Flow<List<FolderWithCount>>
    fun getFolderById(id: String): Flow<FolderEntity?>
    suspend fun getFolderByIdDirect(id: String): FolderEntity?
    suspend fun createFolder(name: String, colorHex: String = "#3B82F6"): String
    suspend fun renameFolder(id: String, name: String)
    suspend fun deleteFolder(id: String)
}

class FolderRepositoryImpl(
    private val folderDao: FolderDao
) : FolderRepository {

    override fun getAllFolders(): Flow<List<FolderEntity>> = folderDao.getAllFolders()

    override fun getFoldersWithCount(): Flow<List<FolderWithCount>> = folderDao.getFoldersWithCount()

    override fun getFolderById(id: String): Flow<FolderEntity?> = folderDao.getFolderById(id)

    override suspend fun getFolderByIdDirect(id: String): FolderEntity? = folderDao.getFolderByIdDirect(id)

    override suspend fun createFolder(name: String, colorHex: String): String {
        val newId = UUID.randomUUID().toString()
        val folder = FolderEntity(
            id = newId,
            name = name,
            colorHex = colorHex,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        folderDao.insertFolder(folder)
        return newId
    }

    override suspend fun renameFolder(id: String, name: String) {
        folderDao.renameFolder(id, name)
    }

    override suspend fun deleteFolder(id: String) {
        folderDao.clearFolderAssociation(id)
        folderDao.deleteFolder(id)
    }
}
