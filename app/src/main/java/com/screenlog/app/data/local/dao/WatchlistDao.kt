package com.screenlog.app.data.local.dao

import androidx.room.*
import com.screenlog.app.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWatchlistFlow(userId: String): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE userId = :userId AND titleType = :titleType AND tmdbId = :tmdbId LIMIT 1")
    suspend fun getWatchlistItem(userId: String, titleType: String, tmdbId: String): WatchlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(items: List<WatchlistEntity>)

    @Query("DELETE FROM watchlist WHERE userId = :userId AND titleType = :titleType AND tmdbId = :tmdbId")
    suspend fun deleteWatchlistItem(userId: String, titleType: String, tmdbId: String)
}
