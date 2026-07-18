package com.screenlog.app.data.remote.tmdb.dto

import com.google.gson.annotations.SerializedName

data class TmdbTitleDto(
    @SerializedName("id") val id: Int,
    @SerializedName("media_type") val mediaType: String?, // "movie" or "tv"
    @SerializedName("title") val title: String?,         // movies use title
    @SerializedName("name") val name: String?,           // tv shows use name
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("release_date") val releaseDate: String?,   // movies
    @SerializedName("first_air_date") val firstAirDate: String?, // tv
    @SerializedName("origin_country") val originCountry: List<String>?,
    @SerializedName("genre_ids") val genreIds: List<Int>?,
    @SerializedName("vote_average") val voteAverage: Double?
)
