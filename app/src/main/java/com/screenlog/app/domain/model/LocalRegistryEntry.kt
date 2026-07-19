package com.screenlog.app.domain.model

data class LocalRegistryEntry(
    val registryId: String,
    val titleName: String,
    val countryCode: String,
    val year: Int,
    val type: String,
    val tmdbId: String,
    val source: String,
    val isLocalContent: Boolean,
    val languages: List<String>
)
