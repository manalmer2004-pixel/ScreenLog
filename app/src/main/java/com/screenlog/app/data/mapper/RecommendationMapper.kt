package com.screenlog.app.data.mapper

import com.screenlog.app.data.local.entity.RecommendationEntity
import com.screenlog.app.domain.model.Recommendation

fun RecommendationEntity.toDomain(): Recommendation {
    return Recommendation(
        id = id,
        userId = userId,
        titleId = titleId,
        tmdbId = tmdbId,
        titleType = titleType,
        score = score,
        reason = reason,
        generatedAt = generatedAt
    )
}

fun Recommendation.toEntity(): RecommendationEntity {
    return RecommendationEntity(
        id = id,
        userId = userId,
        titleId = titleId,
        tmdbId = tmdbId,
        titleType = titleType,
        score = score,
        reason = reason,
        generatedAt = generatedAt
    )
}
