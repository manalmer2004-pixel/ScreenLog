package com.screenlog.app.data.local.dao

import androidx.room.*
import com.screenlog.app.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE titleId = :titleId ORDER BY createdAt DESC")
    fun getReviewsForTitleFlow(titleId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Query("DELETE FROM reviews WHERE titleId = :titleId")
    suspend fun clearReviewsForTitle(titleId: String)
}
