package com.teleprompter.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.teleprompter.app.data.local.entity.FolderEntity
import com.teleprompter.app.data.local.model.FolderWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders ORDER BY createdAt ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("""
        SELECT f.id, f.name, f.colorHex, f.createdAt, f.updatedAt, 
               COUNT(s.id) AS scriptCount
        FROM folders f
        LEFT JOIN scripts s ON s.folderId = f.id AND s.isDeleted = 0
        GROUP BY f.id
        ORDER BY f.createdAt ASC
    """)
    fun getFoldersWithCount(): Flow<List<FolderWithCount>>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    fun getFolderById(id: String): Flow<FolderEntity?>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getFolderByIdDirect(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Query("UPDATE folders SET name = :name, updatedAt = :updatedAt WHERE id = :id")
    suspend fun renameFolder(id: String, name: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolder(id: String)

    @Query("UPDATE scripts SET folderId = NULL WHERE folderId = :folderId")
    suspend fun clearFolderAssociation(folderId: String)
}
