package com.screenlog.app.domain.model

data class Review(
    val id: String,
    val userId: String,
    val userName: String = "",
    val titleId: String,
    val rating: Int,
    val reviewText: String,
    val languageCode: String,
    val containsSpoilers: Boolean,
    val flagged: Boolean,
    val flagReason: String?,
    val createdAt: Long
)
