package com.screenlog.app.domain.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.Recommendation
import kotlinx.coroutines.flow.Flow

interface RecommendationRepository {
    fun getRecommendationsFlow(userId: String): Flow<List<Recommendation>>
    suspend fun refreshRecommendations(userId: String): Resource<List<Recommendation>>
}
