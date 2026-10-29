package com.teleprompter.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "scripts",
    indices = [
        Index(value = ["folderId"]),
        Index(value = ["isFavorite"]),
        Index(value = ["updatedAt"])
    ]
)
data class ScriptEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val folderId: String? = null,
    val isFavorite: Boolean = false,
    val lastReadPosition: Int = 0,
    val wordCount: Int = 0,
    val estimatedReadingTime: Int = 0, // in seconds
    val scrollSpeed: Int = 50,
    val fontSize: Int = 28,
    val fontStyle: String = "SANS", // SANS, SERIF, MONO
    val textColorHex: String = "#FFFFFF",
    val bgOpacity: Float = 0.5f,
    val mirrorText: Boolean = false,
    val showSafeAreaGuide: Boolean = true,
    val lineSpacingMultiplier: Float = 1.2f,
    val textOpacity: Float = 1.0f,
    val readingWidthPercent: Int = 85,
    val verticalPositionPercent: Int = 25,
    val textAlignment: String = "CENTER",
    val letterSpacingSp: Float = 0.0f,
    val presetType: String = "CUSTOM",
    val hasCustomSettings: Boolean = false,
    val isDeleted: Boolean = false,
    val isSynced: Boolean = false
)
