package com.screenlog.app.domain.model

data class User(
    val userId: String,
    val displayName: String,
    val email: String,
    val homeCountry: String,
    val photoUrl: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isModerator: Boolean
)
