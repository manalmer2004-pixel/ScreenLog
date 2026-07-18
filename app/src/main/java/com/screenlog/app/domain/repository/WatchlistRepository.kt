package com.screenlog.app.domain.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.WatchlistItem
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun getWatchlistFlow(userId: String): Flow<List<WatchlistItem>>
    suspend fun addToWatchlist(
        titleType: String,
        tmdbId: String,
        titleName: String,
        posterPath: String?,
        releaseDate: String?
    ): Resource<WatchlistItem>
    suspend fun removeFromWatchlist(titleType: String, tmdbId: String): Resource<Unit>
    suspend fun isTitleInWatchlist(titleType: String, tmdbId: String): Boolean
    suspend fun syncWatchlist(userId: String): Resource<Unit>
}
