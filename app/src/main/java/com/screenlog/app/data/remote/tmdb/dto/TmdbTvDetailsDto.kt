package com.screenlog.app.data.remote.tmdb.dto

import com.google.gson.annotations.SerializedName

data class TmdbTvDetailsDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("origin_country") val originCountry: List<String>?,
    @SerializedName("genres") val genres: List<TmdbGenreDto>?,
    @SerializedName("created_by") val createdBy: List<TmdbCreatorDto>?,
    @SerializedName("episode_run_time") val episodeRunTime: List<Int>?,
    @SerializedName("vote_average") val voteAverage: Double?
)

data class TmdbCreatorDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)
