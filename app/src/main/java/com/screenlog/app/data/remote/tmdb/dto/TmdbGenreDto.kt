package com.screenlog.app.data.remote.tmdb.dto

import com.google.gson.annotations.SerializedName

data class TmdbGenreDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)
