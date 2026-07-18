package com.screenlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val titleId: String,
    val tmdbId: String,
    val titleType: String,
    val titleName: String,
    val posterPath: String?,
    val releaseDate: String?,
    val addedAt: Long,
    val syncStatus: String
)
