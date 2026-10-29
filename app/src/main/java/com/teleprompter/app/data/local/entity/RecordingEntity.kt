package com.teleprompter.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "recordings",
    indices = [
        Index(value = ["scriptId"]),
        Index(value = ["createdAt"])
    ]
)
data class RecordingEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val scriptId: String? = null,
    val title: String,
    val videoUri: String,
    val thumbnailPath: String? = null,
    val durationSeconds: Long = 0,
    val fileSizeBytes: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
