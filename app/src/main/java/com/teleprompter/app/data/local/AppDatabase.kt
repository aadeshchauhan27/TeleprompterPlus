package com.teleprompter.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.teleprompter.app.core.util.ScriptUtils
import com.teleprompter.app.data.local.dao.FolderDao
import com.teleprompter.app.data.local.dao.RecordingDao
import com.teleprompter.app.data.local.dao.ScriptDao
import com.teleprompter.app.data.local.entity.FolderEntity
import com.teleprompter.app.data.local.entity.RecordingEntity
import com.teleprompter.app.data.local.entity.ScriptEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [ScriptEntity::class, FolderEntity::class, RecordingEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scriptDao(): ScriptDao
    abstract fun folderDao(): FolderDao
    abstract fun recordingDao(): RecordingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teleprompter.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val folderDao = database.folderDao()

            val youtubeFolderId = UUID.randomUUID().toString()
            val instagramFolderId = UUID.randomUUID().toString()
            val workFolderId = UUID.randomUUID().toString()
            val presentationsFolderId = UUID.randomUUID().toString()
            val personalFolderId = UUID.randomUUID().toString()

            val initialFolders = listOf(
                FolderEntity(id = youtubeFolderId, name = "YouTube", colorHex = "#3B82F6"),
                FolderEntity(id = instagramFolderId, name = "Instagram", colorHex = "#8B5CF6"),
                FolderEntity(id = workFolderId, name = "Work", colorHex = "#F59E0B"),
                FolderEntity(id = presentationsFolderId, name = "Presentations", colorHex = "#F43F5E"),
                FolderEntity(id = personalFolderId, name = "Personal", colorHex = "#14B8A6")
            )
            folderDao.insertFolders(initialFolders)
            // Fresh install starts with zero fake historical scripts as per Phase 5 criteria
        }
    }
}
