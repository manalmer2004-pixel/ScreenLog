package com.screenlog.app.domain.model

enum class SyncStatus {
    PENDING_SYNC,
    SYNCED,
    FAILED
}

data class LogEntry(
    val id: String,
    val userId: String,
    val titleId: String,
    val tmdbId: String,
    val titleType: String,
    val titleName: String = "",
    val posterPath: String? = null,
    val releaseDate: String? = null,
    val watchedDate: Long,
    val rating: Int, // 1 to 5 stars
    val reviewText: String?,
    val languageCode: String?, // "en", "sw", etc.
    val containsSpoilers: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: SyncStatus
)
