package com.screenlog.app.data.mapper

import com.screenlog.app.data.local.entity.WatchlistEntity
import com.screenlog.app.domain.model.WatchlistItem

fun WatchlistEntity.toDomain(): WatchlistItem {
    return WatchlistItem(
        id = id,
        userId = userId,
        titleId = titleId,
        tmdbId = tmdbId,
        titleType = titleType,
        titleName = titleName,
        posterPath = posterPath,
        releaseDate = releaseDate,
        addedAt = addedAt,
        syncStatus = syncStatus
    )
}

fun WatchlistItem.toEntity(): WatchlistEntity {
    return WatchlistEntity(
        id = id,
        userId = userId,
        titleId = titleId,
        tmdbId = tmdbId,
        titleType = titleType,
        titleName = titleName,
        posterPath = posterPath,
        releaseDate = releaseDate,
        addedAt = addedAt,
        syncStatus = syncStatus
    )
}
