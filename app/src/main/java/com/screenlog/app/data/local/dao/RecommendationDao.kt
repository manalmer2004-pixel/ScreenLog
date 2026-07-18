package com.screenlog.app.data.local.dao

import androidx.room.*
import com.screenlog.app.data.local.entity.RecommendationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendationDao {
    @Query("SELECT * FROM recommendations WHERE userId = :userId ORDER BY score DESC")
    fun getRecommendationsFlow(userId: String): Flow<List<RecommendationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendations(recommendations: List<RecommendationEntity>)

    @Query("DELETE FROM recommendations WHERE userId = :userId")
    suspend fun clearRecommendations(userId: String)
}
