package com.screenlog.app.data.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.data.local.dao.RecommendationDao
import com.screenlog.app.data.local.entity.RecommendationEntity
import com.screenlog.app.data.mapper.toDomain
import com.screenlog.app.data.mapper.toEntity
import com.screenlog.app.data.remote.firebase.CloudFunctionsDataSource
import com.screenlog.app.domain.model.Recommendation
import com.screenlog.app.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationRepositoryImpl @Inject constructor(
    private val recommendationDao: RecommendationDao,
    private val functionsDataSource: CloudFunctionsDataSource
) : RecommendationRepository {

    override fun getRecommendationsFlow(userId: String): Flow<List<Recommendation>> {
        return recommendationDao.getRecommendationsFlow(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun refreshRecommendations(userId: String): Resource<List<Recommendation>> {
        return try {
            val remoteResults = functionsDataSource.generateRecommendations(userId)
            if (remoteResults.isNotEmpty()) {
                recommendationDao.clearRecommendations(userId)
                recommendationDao.insertRecommendations(remoteResults.map { it.toEntity() })
            }
            Resource.Success(remoteResults)
        } catch (e: Exception) {
            // Local fallback
            Resource.Error(e.message ?: "Failed to generate server recommendations, using cached data.")
        }
    }
}
