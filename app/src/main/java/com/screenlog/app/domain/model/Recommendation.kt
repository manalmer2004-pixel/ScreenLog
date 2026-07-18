package com.screenlog.app.domain.model

data class Recommendation(
    val id: String,
    val userId: String,
    val titleId: String,
    val tmdbId: String,
    val titleType: String,
    val score: Double, // 0.0 to 5.0
    val reason: String, // E.g., "Recommended because users with similar ratings liked this"
    val generatedAt: Long
)
