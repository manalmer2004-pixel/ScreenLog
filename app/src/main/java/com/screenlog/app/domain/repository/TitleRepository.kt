package com.screenlog.app.domain.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.Title
import com.screenlog.app.domain.model.Review
import kotlinx.coroutines.flow.Flow

interface TitleRepository {
    suspend fun searchTitles(query: String, typeFilter: String?, localOnly: Boolean): Resource<List<Title>>
    suspend fun getTitleDetails(titleType: String, tmdbId: String): Resource<Title>
    fun getTitleDetailsFlow(titleType: String, tmdbId: String): Flow<Title?>
    suspend fun getTitleReviews(titleId: String): Resource<List<Review>>
    suspend fun submitReview(titleId: String, rating: Int, text: String, language: String, containsSpoilers: Boolean): Resource<Review>
    suspend fun deleteReview(titleId: String, reviewId: String): Resource<Unit>
    suspend fun getMoviesByGenre(genreId: String): Resource<List<Title>>
    suspend fun getTvShowsByGenre(genreId: String): Resource<List<Title>>
}
