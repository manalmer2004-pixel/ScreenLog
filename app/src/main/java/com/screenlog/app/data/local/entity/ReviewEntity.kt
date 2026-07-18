package com.screenlog.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val titleId: String,
    val rating: Int,
    val reviewText: String,
    val languageCode: String,
    val containsSpoilers: Boolean,
    val flagged: Boolean,
    val flagReason: String?,
    val createdAt: Long
)
