package com.screenlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "titles")
data class TitleEntity(
    @PrimaryKey val id: String,
    val tmdbId: String,
    val titleType: String,
    val name: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val firstAirDate: String?,
    val originCountry: List<String>, // Needs TypeConverter
    val genres: List<String>,        // Needs TypeConverter
    val runtimeMinutes: Int?,
    val directorNames: List<String>, // Needs TypeConverter
    val castNames: List<String>,     // Needs TypeConverter
    val isLocalContent: Boolean,
    val localSource: String?,
    val averageRating: Double,
    val ratingCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)
