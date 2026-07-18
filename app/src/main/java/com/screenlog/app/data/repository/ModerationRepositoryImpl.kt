package com.screenlog.app.data.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.data.remote.firebase.CloudFunctionsDataSource
import com.screenlog.app.data.remote.firebase.FirestoreDataSource
import com.screenlog.app.domain.model.Review
import com.screenlog.app.domain.repository.ModerationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModerationRepositoryImpl @Inject constructor(
    private val functionsDataSource: CloudFunctionsDataSource,
    private val firestoreDataSource: FirestoreDataSource
) : ModerationRepository {

    override suspend fun flagReview(reviewId: String, titleId: String, reason: String): Resource<Unit> {
        return try {
            functionsDataSource.flagReview(reviewId, titleId, reason)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to flag review")
        }
    }

    override suspend fun getFlaggedReviewsQueue(): Resource<List<Review>> {
        // Safe mock return for admin panels (MVPs)
        return Resource.Success(emptyList())
    }

    override suspend fun resolveFlag(reviewId: String, titleId: String, action: String): Resource<Unit> {
        return Resource.Success(Unit)
    }
}
