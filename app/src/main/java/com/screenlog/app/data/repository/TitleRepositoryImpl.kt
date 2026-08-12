package com.screenlog.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.screenlog.app.core.common.Resource
import com.screenlog.app.data.local.dao.TitleDao
import com.screenlog.app.data.mapper.toDomain
import com.screenlog.app.data.mapper.toEntity
import com.screenlog.app.data.remote.firebase.FirestoreDataSource
import com.screenlog.app.data.remote.tmdb.TmdbApi
import com.screenlog.app.data.remote.tmdb.dto.TmdbTitleDto
import com.screenlog.app.domain.model.LocalRegistryEntry
import com.screenlog.app.domain.model.Review
import com.screenlog.app.domain.model.Title
import com.screenlog.app.domain.repository.TitleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    // In-memory cache of the KFCB/local registry
    private var registryEntriesCache: List<LocalRegistryEntry>? = null
    private val registryMutex = Mutex()

    private suspend fun getLocalRegistryEntries(): List<LocalRegistryEntry> {
        registryEntriesCache?.let { return it }
        return registryMutex.withLock {
            registryEntriesCache?.let { return it }
            val entries = try {
                firestoreDataSource.getLocalRegistry()
            } catch (e: Exception) {
                emptyList()
            }
            registryEntriesCache = entries
            entries
        }
    }

    private suspend fun getLocalRegistryMap(): Map<String, String> {
        val entries = getLocalRegistryEntries()
        return entries.associate { it.tmdbId to (it.source.ifBlank { "KFCB Local Registry" }) }
    }

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

            val registryEntries = getLocalRegistryEntries()
            val registryByTmdbId = registryEntries
                .filter { it.tmdbId.all { c -> c.isDigit() } && it.tmdbId.isNotBlank() }
                .associateBy { it.tmdbId }

            // Local-only entries (no real TMDB ID) matched by title name
            val localOnlyMatches = registryEntries
                .filter { !it.tmdbId.all { c -> c.isDigit() } || it.tmdbId.isBlank() }
                .filter { it.titleName.contains(query, ignoreCase = true) }
                .map { it.toLocalOnlyTitle() }

            val tmdbResponse = tmdbApi.searchMulti(query)
            val tmdbTitles = tmdbResponse.results?.mapNotNull { dto ->
                if (dto.mediaType == "movie") {
                    val entry = registryByTmdbId[dto.id.toString()]
                    buildMovieTitleFromDto(dto, entry)
                } else if (dto.mediaType == "tv") {
                    val entry = registryByTmdbId[dto.id.toString()]
                    buildTvTitleFromDto(dto, entry)
                } else null
            } ?: emptyList()

            val combined = localOnlyMatches + tmdbTitles
            titleDao.insertTitles(combined.map { it.toEntity() })
            Resource.Success(combined)
        } catch (e: Exception) {
            val dbResults = titleDao.searchTitles(query).map { it.toDomain() }
            if (dbResults.isNotEmpty()) {
                Resource.Success(dbResults)
            } else {
                Resource.Error(e.message ?: "Failed to query titles from TMDB")
            }
        }
    }

    private fun LocalRegistryEntry.toLocalOnlyTitle(): Title {
        return Title(
            id = "local_${registryId}",
            tmdbId = "local_${registryId}",
            titleType = type,
            name = titleName,
            overview = synopsis,
            posterPath = posterUrl.ifBlank { null },
            backdropPath = null,
            releaseDate = if (type == "movie") "$year-01-01" else null,
            firstAirDate = if (type == "tv") "$year-01-01" else null,
            originCountry = listOf(countryCode),
            genres = genres,
            runtimeMinutes = null,
            directorNames = if (director.isNotBlank()) listOf(director) else emptyList(),
            castNames = emptyList(),
            isLocalContent = true,
            localSource = source.ifBlank { "KFCB Local Registry" },
            averageRating = 0.0,
            ratingCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun buildMovieTitleFromDto(dto: TmdbTitleDto, entry: LocalRegistryEntry?): Title {
        return Title(
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
            isLocalContent = entry != null,
            localSource = entry?.source?.ifBlank { "KFCB Local Registry" },
            averageRating = dto.voteAverage ?: 0.0,
            ratingCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun buildTvTitleFromDto(dto: TmdbTitleDto, entry: LocalRegistryEntry?): Title {
        return Title(
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
            isLocalContent = entry != null,
            localSource = entry?.source?.ifBlank { "KFCB Local Registry" },
            averageRating = dto.voteAverage ?: 0.0,
            ratingCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun getTitleDetails(titleType: String, tmdbId: String): Resource<Title> {
        if (tmdbId.startsWith("local_")) {
            val registryId = tmdbId.removePrefix("local_")
            val entries = getLocalRegistryEntries()
            val entry = entries.find { it.registryId == registryId }
            return if (entry != null) {
                val title = entry.toLocalOnlyTitle()
                titleDao.insertTitle(title.toEntity())
                Resource.Success(title)
            } else {
                val cached = titleDao.getTitle(titleType, tmdbId)
                if (cached != null) Resource.Success(cached.toDomain())
                else Resource.Error("Local title not found")
            }
        }

        return try {
            val registryMap = getLocalRegistryMap()
            val localSource = registryMap[tmdbId]
            val isLocal = localSource != null

            val title: Title
            if (titleType == "movie") {
                val movieDto = tmdbApi.getMovieDetails(tmdbId)
                val creditsDto = tmdbApi.getMovieCredits(tmdbId)

                val castList = creditsDto.cast?.take(5)?.map { it.name } ?: emptyList()
                val directors = creditsDto.crew?.filter { it.job == "Director" }?.map { it.name } ?: emptyList()

                title = movieDto.toDomain(isLocal, localSource).copy(
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

                title = tvDto.toDomain(isLocal, localSource).copy(
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
        containsSpoilers: Boolean,
        reviewId: String?
    ): Resource<Review> {
        return try {
            val uid = firebaseAuth.currentUser?.uid ?: throw Exception("Unauthorized")
            val userProfile = firestoreDataSource.getUserProfile(uid)
            val review = Review(
                id = reviewId ?: UUID.randomUUID().toString(),
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

    override suspend fun flagReview(titleId: String, reviewId: String, reason: String): Resource<Unit> {
        return try {
            firestoreDataSource.flagReview(titleId, reviewId, reason)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to flag review")
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