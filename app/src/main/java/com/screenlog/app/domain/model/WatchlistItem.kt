package com.screenlog.app.domain.model

data class WatchlistItem(
    val id: String,
    val userId: String,
    val titleId: String,
    val tmdbId: String,
    val titleType: String,
    val titleName: String = "",
    val posterPath: String? = null,
    val releaseDate: String? = null,
    val addedAt: Long,
    val syncStatus: String
)
