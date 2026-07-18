package com.screenlog.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.screenlog.app.core.common.Resource
import com.screenlog.app.data.local.dao.TitleDao
import com.screenlog.app.data.mapper.toDomain
import com.screenlog.app.data.mapper.toEntity
import com.screenlog.app.data.remote.firebase.FirestoreDataSource
import com.screenlog.app.data.remote.tmdb.TmdbApi
import com.screenlog.app.domain.model.Review
import com.screenlog.app.domain.model.Title
import com.screenlog.app.domain.repository.TitleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TitleRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi,
    private val titleDao: TitleDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val firebaseAuth: FirebaseAuth
) : TitleRepository {

    override suspend fun searchTitles(
        query: String,
        typeFilter: String?,
        localOnly: Boolean
    ): Resource<List<Title>> {
        return try {
            if (localOnly) {
                val dbResults = titleDao.searchTitles(query).map { it.toDomain() }
                return Resource.Success(dbResults.filter { it.isLocalContent })
            }
            
            val tmdbResponse = tmdbApi.searchMulti(query)
            val titles = tmdbResponse.results?.mapNotNull { dto ->
                if (dto.mediaType == "movie") {
                    // Simulating check for KFCB local registry match
                    val isLocal = dto.title?.contains("Nairobi", ignoreCase = true) == true ||
                                  dto.title?.contains("Supa", ignoreCase = true) == true
                    Title(
                        id = "movie_${dto.id}",
                        tmdbId = dto.id.toString(),
                        titleType = "movie",
                        name = dto.title ?: "",
                        overview = dto.overview ?: "",
                        posterPath = dto.posterPath,
                        backdropPath = dto.backdropPath,
                        releaseDate = dto.releaseDate,
                        firstAirDate = null,
                        originCountry = dto.originCountry ?: emptyList(),
                        genres = emptyList(),
                        runtimeMinutes = null,
                        directorNames = emptyList(),
                        castNames = emptyList(),
                        isLocalContent = isLocal,
                        localSource = if (isLocal) "KFCB Local Registry Seed" else null,
                        averageRating = 0.0,
                        ratingCount = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                } else if (dto.mediaType == "tv") {
                    val isLocal = dto.name?.contains("County", ignoreCase = true) == true ||
                                  dto.name?.contains("Single", ignoreCase = true) == true
                    Title(
                        id = "tv_${dto.id}",
                        tmdbId = dto.id.toString(),
                        titleType = "tv",
                        name = dto.name ?: "",
                        overview = dto.overview ?: "",
                        posterPath = dto.posterPath,
                        backdropPath = dto.backdropPath,
                        releaseDate = null,
                        firstAirDate = dto.firstAirDate,
                        originCountry = dto.originCountry ?: emptyList(),
                        genres = emptyList(),
                        runtimeMinutes = null,
                        directorNames = emptyList(),
                        castNames = emptyList(),
                        isLocalContent = isLocal,
                        localSource = if (isLocal) "KFCB Local Registry Seed" else null,
                        averageRating = 0.0,
                        ratingCount = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                } else null
            } ?: emptyList()

            // Save searched titles to cache
            titleDao.insertTitles(titles.map { it.toEntity() })
            Resource.Success(titles)
        } catch (e: Exception) {
            // Offline fallback from local cache search
            val dbResults = titleDao.searchTitles(query).map { it.toDomain() }
            if (dbResults.isNotEmpty()) {
                Resource.Success(dbResults)
            } else {
                Resource.Error(e.message ?: "Failed to query titles from TMDB")
            }
        }
    }

    override suspend fun getTitleDetails(titleType: String, tmdbId: String): Resource<Title> {
        return try {
            val title: Title
            if (titleType == "movie") {
                val movieDto = tmdbApi.getMovieDetails(tmdbId)
                val creditsDto = tmdbApi.getMovieCredits(tmdbId)
                
                val castList = creditsDto.cast?.take(5)?.map { it.name } ?: emptyList()
                val directors = creditsDto.crew?.filter { it.job == "Director" }?.map { it.name } ?: emptyList()
                
                // Simulating match against local registry
                val isLocal = movieDto.title.contains("Nairobi", ignoreCase = true) || 
                              movieDto.title.contains("Supa", ignoreCase = true) ||
                              movieDto.originCountry?.contains("KE") == true

                title = movieDto.toDomain(isLocal, if (isLocal) "KFCB Registry" else null).copy(
                    directorNames = directors,
                    castNames = castList
                )
            } else {
                val tvDto = tmdbApi.getTvDetails(tmdbId)
                val creditsDto = tmdbApi.getTvCredits(tmdbId)
                
                val castList = creditsDto.cast?.take(5)?.map { it.name } ?: emptyList()
                val creators = tvDto.createdBy?.map { it.name }?.toMutableList() ?: mutableListOf()
                
                // Fallback to directors from crew if creators list is empty
                if (creators.isEmpty()) {
                    val directors = creditsDto.crew?.filter { it.job == "Director" }?.map { it.name } ?: emptyList()
                    creators.addAll(directors)
                }

                val isLocal = tvDto.name.contains("County", ignoreCase = true) || 
                              tvDto.name.contains("Single", ignoreCase = true) ||
                              tvDto.originCountry?.contains("KE") == true

                title = tvDto.toDomain(isLocal, if (isLocal) "KFCB Registry" else null).copy(
                    directorNames = creators,
                    castNames = castList
                )
            }

            titleDao.insertTitle(title.toEntity())
            Resource.Success(title)
        } catch (e: Exception) {
            val cached = titleDao.getTitle(titleType, tmdbId)
            if (cached != null) {
                Resource.Success(cached.toDomain())
            } else {
                Resource.Error(e.message ?: "Failed to retrieve title details")
            }
        }
    }

    override fun getTitleDetailsFlow(titleType: String, tmdbId: String): Flow<Title?> {
        return titleDao.getTitleFlow(titleType, tmdbId).map { it?.toDomain() }
    }

    override suspend fun getTitleReviews(titleId: String): Resource<List<Review>> {
        return try {
            val reviews = firestoreDataSource.getTitleReviews(titleId)
            Resource.Success(reviews)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to pull community reviews")
        }
    }

    override suspend fun submitReview(
        titleId: String,
        rating: Int,
        text: String,
        language: String,
        containsSpoilers: Boolean
    ): Resource<Review> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("Unauthorized")
            val userProfile = firestoreDataSource.getUserProfile(uid)
            val review = Review(
                id = UUID.randomUUID().toString(),
                userId = uid,
                userName = userProfile.displayName,
                titleId = titleId,
                rating = rating,
                reviewText = text,
                languageCode = language,
                containsSpoilers = containsSpoilers,
                flagged = false,
                flagReason = null,
                createdAt = System.currentTimeMillis()
            )
            firestoreDataSource.submitReview(titleId, review)
            Resource.Success(review)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to submit review")
        }
    }

    override suspend fun deleteReview(titleId: String, reviewId: String): Resource<Unit> {
        return try {
            firestoreDataSource.deleteReview(titleId, reviewId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete review")
        }
    }

    override suspend fun getMoviesByGenre(genreId: String): Resource<List<Title>> {
        return try {
            val response = tmdbApi.discoverMovies(genreId = genreId)
            val titles = response.results?.map { dto ->
                Title(
                    id = "movie_${dto.id}",
                    tmdbId = dto.id.toString(),
                    titleType = "movie",
                    name = dto.title ?: "",
                    overview = dto.overview ?: "",
                    posterPath = dto.posterPath,
                    backdropPath = dto.backdropPath,
                    releaseDate = dto.releaseDate,
                    firstAirDate = null,
                    originCountry = dto.originCountry ?: emptyList(),
                    genres = emptyList(),
                    runtimeMinutes = null,
                    directorNames = emptyList(),
                    castNames = emptyList(),
                    isLocalContent = false,
                    localSource = null,
                    averageRating = dto.voteAverage ?: 0.0,
                    ratingCount = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            } ?: emptyList()
            Resource.Success(titles)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to discover movies by genre")
        }
    }

    override suspend fun getTvShowsByGenre(genreId: String): Resource<List<Title>> {
        return try {
            val response = tmdbApi.discoverTv(genreId = genreId) // Using with_genres for TV
            val titles = response.results?.map { dto ->
                Title(
                    id = "tv_${dto.id}",
                    tmdbId = dto.id.toString(),
                    titleType = "tv",
                    name = dto.name ?: "",
                    overview = dto.overview ?: "",
                    posterPath = dto.posterPath,
                    backdropPath = dto.backdropPath,
                    releaseDate = null,
                    firstAirDate = dto.firstAirDate,
                    originCountry = dto.originCountry ?: emptyList(),
                    genres = emptyList(),
                    runtimeMinutes = null,
                    directorNames = emptyList(),
                    castNames = emptyList(),
                    isLocalContent = false,
                    localSource = null,
                    averageRating = dto.voteAverage ?: 0.0,
                    ratingCount = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            } ?: emptyList()
            Resource.Success(titles)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to discover TV shows by genre")
        }
    }
}
