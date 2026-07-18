package com.screenlog.app.domain.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.Review

interface ModerationRepository {
    suspend fun flagReview(reviewId: String, titleId: String, reason: String): Resource<Unit>
    suspend fun getFlaggedReviewsQueue(): Resource<List<Review>>
    suspend fun resolveFlag(reviewId: String, titleId: String, action: String): Resource<Unit> // "approve", "delete"
}
