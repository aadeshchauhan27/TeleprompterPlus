package com.teleprompter.app.data.local.model

data class FolderWithCount(
    val id: String,
    val name: String,
    val colorHex: String,
    val createdAt: Long,
    val updatedAt: Long,
    val scriptCount: Int
)
