package com.screenlog.app.data.mapper

import com.screenlog.app.data.local.entity.TitleEntity
import com.screenlog.app.data.remote.tmdb.dto.TmdbMovieDetailsDto
import com.screenlog.app.data.remote.tmdb.dto.TmdbTvDetailsDto
import com.screenlog.app.domain.model.Title

fun TitleEntity.toDomain(): Title {
    return Title(
        id = id,
        tmdbId = tmdbId,
        titleType = titleType,
        name = name,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        firstAirDate = firstAirDate,
        originCountry = originCountry,
        genres = genres,
        runtimeMinutes = runtimeMinutes,
        directorNames = directorNames,
        castNames = castNames,
        isLocalContent = isLocalContent,
        localSource = localSource,
        averageRating = averageRating,
        ratingCount = ratingCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Title.toEntity(): TitleEntity {
    return TitleEntity(
        id = id,
        tmdbId = tmdbId,
        titleType = titleType,
        name = name,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        firstAirDate = firstAirDate,
        originCountry = originCountry,
        genres = genres,
        runtimeMinutes = runtimeMinutes,
        directorNames = directorNames,
        castNames = castNames,
        isLocalContent = isLocalContent,
        localSource = localSource,
        averageRating = averageRating,
        ratingCount = ratingCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun TmdbMovieDetailsDto.toDomain(isLocal: Boolean = false, localSource: String? = null): Title {
    return Title(
        id = "movie_$id",
        tmdbId = id.toString(),
        titleType = "movie",
        name = title,
        overview = overview ?: "",
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        firstAirDate = null,
        originCountry = originCountry ?: emptyList(),
        genres = genres?.map { it.name } ?: emptyList(),
        runtimeMinutes = runtime,
        directorNames = emptyList(), // Filled later via Credits call
        castNames = emptyList(),     // Filled later via Credits call
        isLocalContent = isLocal,
        localSource = localSource,
        averageRating = 0.0,
        ratingCount = 0,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

fun TmdbTvDetailsDto.toDomain(isLocal: Boolean = false, localSource: String? = null): Title {
    return Title(
        id = "tv_$id",
        tmdbId = id.toString(),
        titleType = "tv",
        name = name,
        overview = overview ?: "",
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = null,
        firstAirDate = firstAirDate,
        originCountry = originCountry ?: emptyList(),
        genres = genres?.map { it.name } ?: emptyList(),
        runtimeMinutes = episodeRunTime?.firstOrNull(),
        directorNames = emptyList(),
        castNames = emptyList(),
        isLocalContent = isLocal,
        localSource = localSource,
        averageRating = 0.0,
        ratingCount = 0,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
