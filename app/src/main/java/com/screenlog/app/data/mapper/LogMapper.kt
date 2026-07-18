package com.screenlog.app.data.mapper

import com.screenlog.app.data.local.entity.LogEntity
import com.screenlog.app.domain.model.LogEntry
import com.screenlog.app.domain.model.SyncStatus

fun LogEntity.toDomain(): LogEntry {
    return LogEntry(
        id = id,
        userId = userId,
        titleId = titleId,
        tmdbId = tmdbId,
        titleType = titleType,
        titleName = titleName,
        posterPath = posterPath,
        releaseDate = releaseDate,
        watchedDate = watchedDate,
        rating = rating,
        reviewText = reviewText,
        languageCode = languageCode,
        containsSpoilers = containsSpoilers,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = when (syncStatus) {
            "SYNCED" -> SyncStatus.SYNCED
            "FAILED" -> SyncStatus.FAILED
            else -> SyncStatus.PENDING_SYNC
        }
    )
}

fun LogEntry.toEntity(): LogEntity {
    return LogEntity(
        id = id,
        userId = userId,
        titleId = titleId,
        tmdbId = tmdbId,
        titleType = titleType,
        titleName = titleName,
        posterPath = posterPath,
        releaseDate = releaseDate,
        watchedDate = watchedDate,
        rating = rating,
        reviewText = reviewText,
        languageCode = languageCode,
        containsSpoilers = containsSpoilers,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus.name
    )
}
