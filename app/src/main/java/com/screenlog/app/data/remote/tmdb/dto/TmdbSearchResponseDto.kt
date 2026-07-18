package com.screenlog.app.data.remote.tmdb.dto

import com.google.gson.annotations.SerializedName

data class TmdbSearchResponseDto(
    @SerializedName("page") val page: Int,
    @SerializedName("results") val results: List<TmdbTitleDto>?,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)
