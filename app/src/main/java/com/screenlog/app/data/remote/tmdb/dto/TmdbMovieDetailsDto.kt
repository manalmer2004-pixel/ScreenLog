package com.screenlog.app.data.remote.tmdb.dto

import com.google.gson.annotations.SerializedName

data class TmdbMovieDetailsDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("origin_country") val originCountry: List<String>?,
    @SerializedName("genres") val genres: List<TmdbGenreDto>?,
    @SerializedName("runtime") val runtime: Int?,
    @SerializedName("vote_average") val voteAverage: Double?
)
