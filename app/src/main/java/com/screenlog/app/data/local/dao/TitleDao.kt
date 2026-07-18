package com.screenlog.app.data.local.dao

import androidx.room.*
import com.screenlog.app.data.local.entity.TitleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TitleDao {
    @Query("SELECT * FROM titles WHERE titleType = :titleType AND tmdbId = :tmdbId LIMIT 1")
    fun getTitleFlow(titleType: String, tmdbId: String): Flow<TitleEntity?>

    @Query("SELECT * FROM titles WHERE titleType = :titleType AND tmdbId = :tmdbId LIMIT 1")
    suspend fun getTitle(titleType: String, tmdbId: String): TitleEntity?

    @Query("SELECT * FROM titles WHERE isLocalContent = 1")
    suspend fun getLocalTitles(): List<TitleEntity>

    @Query("SELECT * FROM titles WHERE name LIKE '%' || :query || '%'")
    suspend fun searchTitles(query: String): List<TitleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitle(title: TitleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTitles(titles: List<TitleEntity>)
}
