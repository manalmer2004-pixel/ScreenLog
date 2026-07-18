package com.screenlog.app.domain.model

data class Title(
    val id: String,
    val tmdbId: String,
    val titleType: String, // "movie" or "tv"
    val name: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val firstAirDate: String?,
    val originCountry: List<String>,
    val genres: List<String>,
    val runtimeMinutes: Int?,
    val directorNames: List<String>,
    val castNames: List<String>,
    val isLocalContent: Boolean,
    val localSource: String?,
    val averageRating: Double,
    val ratingCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)
