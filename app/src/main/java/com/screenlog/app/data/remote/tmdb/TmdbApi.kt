package com.screenlog.app.data.remote.tmdb

import com.screenlog.app.data.remote.tmdb.dto.TmdbCreditsDto
import com.screenlog.app.data.remote.tmdb.dto.TmdbMovieDetailsDto
import com.screenlog.app.data.remote.tmdb.dto.TmdbSearchResponseDto
import com.screenlog.app.data.remote.tmdb.dto.TmdbTvDetailsDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbSearchResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: String
    ): TmdbMovieDetailsDto

    @GET("tv/{series_id}")
    suspend fun getTvDetails(
        @Path("series_id") seriesId: String
    ): TmdbTvDetailsDto

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: String
    ): TmdbCreditsDto

    @GET("tv/{series_id}/credits")
    suspend fun getTvCredits(
        @Path("series_id") seriesId: String
    ): TmdbCreditsDto

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") genreId: String? = null,
        @Query("with_origin_country") countryCode: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): TmdbSearchResponseDto

    @GET("discover/tv")
    suspend fun discoverTv(
        @Query("with_genres") genreId: String? = null,
        @Query("with_origin_country") countryCode: String? = null,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): TmdbSearchResponseDto
}
