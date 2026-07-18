package com.screenlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_logs")
data class LogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val titleId: String,
    val tmdbId: String,
    val titleType: String,
    val titleName: String,
    val posterPath: String?,
    val releaseDate: String?,
    val watchedDate: Long,
    val rating: Int,
    val reviewText: String?,
    val languageCode: String?,
    val containsSpoilers: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: String // "PENDING_SYNC", "SYNCED", "FAILED"
)
