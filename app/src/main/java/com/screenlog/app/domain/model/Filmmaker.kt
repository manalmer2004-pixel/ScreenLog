package com.screenlog.app.domain.model

data class Filmmaker(
    val id: String,
    val name: String,
    val countryCode: String,
    val bio: String,
    val notableTitleIds: List<String>,
    val profileImageUrl: String?
)
