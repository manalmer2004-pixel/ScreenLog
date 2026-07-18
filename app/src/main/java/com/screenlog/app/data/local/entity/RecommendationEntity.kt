package com.screenlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommendations")
data class RecommendationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val titleId: String,
    val tmdbId: String,
    val titleType: String,
    val score: Double,
    val reason: String,
    val generatedAt: Long
)
