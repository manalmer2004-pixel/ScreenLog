package com.screenlog.app.domain.model

data class AnalyticsSummary(
    val totalLogged: Int,
    val movieCount: Int,
    val tvCount: Int,
    val averageRating: Double,
    val topGenres: List<Pair<String, Int>>,
    val topCountries: List<Pair<String, Int>>,
    val favoriteDirectors: List<Pair<String, Int>>,
    val logsByMonth: Map<String, Int>, // Key: "yyyy-MM" e.g., "2026-06" -> Count
    val ratingDistribution: Map<Int, Int> // Key: Rating (1-5) -> Count
)
