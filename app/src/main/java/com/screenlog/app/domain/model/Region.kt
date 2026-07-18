package com.screenlog.app.domain.model

data class Region(
    val countryCode: String, // E.g., "KE", "UG", "TZ"
    val countryName: String,
    val supported: Boolean
)
