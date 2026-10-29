package com.teleprompter.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.teleprompter.app.data.local.entity.ScriptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {

    @Query("SELECT * FROM scripts WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllScripts(): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE isDeleted = 0 ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentScripts(limit: Int = 10): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE isDeleted = 0 AND isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteScripts(): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE isDeleted = 0 AND folderId = :folderId ORDER BY updatedAt DESC")
    fun getScriptsInFolder(folderId: String): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun searchScripts(query: String): Flow<List<ScriptEntity>>

    @Query("SELECT * FROM scripts WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getScriptById(id: String): Flow<ScriptEntity?>

    @Query("SELECT * FROM scripts WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getScriptByIdDirect(id: String): ScriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: ScriptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScripts(scripts: List<ScriptEntity>)

    @Update
    suspend fun updateScript(script: ScriptEntity)

    @Query("UPDATE scripts SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE scripts SET folderId = :folderId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun moveToFolder(id: String, folderId: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE scripts SET lastReadPosition = :position WHERE id = :id")
    suspend fun updateLastReadPosition(id: String, position: Int)

    @Query("DELETE FROM scripts WHERE id = :id")
    suspend fun deleteScript(id: String)

    @Query("UPDATE scripts SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteScript(id: String, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM scripts WHERE isDeleted = 0")
    fun getScriptCount(): Flow<Int>
}
