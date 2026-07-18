package com.screenlog.app.data.mapper

import com.screenlog.app.data.local.entity.ReviewEntity
import com.screenlog.app.domain.model.Review

fun ReviewEntity.toDomain(): Review {
    return Review(
        id = id,
        userId = userId,
        userName = userName,
        titleId = titleId,
        rating = rating,
        reviewText = reviewText,
        languageCode = languageCode,
        containsSpoilers = containsSpoilers,
        flagged = flagged,
        flagReason = flagReason,
        createdAt = createdAt
    )
}

fun Review.toEntity(): ReviewEntity {
    return ReviewEntity(
        id = id,
        userId = userId,
        userName = userName,
        titleId = titleId,
        rating = rating,
        reviewText = reviewText,
        languageCode = languageCode,
        containsSpoilers = containsSpoilers,
        flagged = flagged,
        flagReason = flagReason,
        createdAt = createdAt
    )
}
