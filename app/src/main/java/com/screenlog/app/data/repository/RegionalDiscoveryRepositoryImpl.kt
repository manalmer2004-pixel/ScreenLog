package com.screenlog.app.data.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.data.remote.tmdb.TmdbApi
import com.screenlog.app.domain.model.Filmmaker
import com.screenlog.app.domain.model.Region
import com.screenlog.app.domain.model.Title
import com.screenlog.app.domain.repository.RegionalDiscoveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegionalDiscoveryRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi
) : RegionalDiscoveryRepository {

    override fun getSupportedRegions(): Flow<List<Region>> = flow {
        emit(
            listOf(
                Region("KE", "Kenya", true),
                Region("TZ", "Tanzania", true),
                Region("UG", "Uganda", true),
                Region("RW", "Rwanda", true)
            )
        )
    }

    override suspend fun getTopRatedInRegion(countryCode: String): Resource<List<Title>> {
        // Mock seed titles for Kenya and East Africa regional charts
        val titles = if (countryCode == "KE") {
            listOf(
                Title("movie_141045", "141045", "movie", "Nairobi Half Life", "A young aspiring actor from upcountry moves to Nairobi...", null, null, "2012-10-10", null, listOf("KE"), listOf("Drama", "Action"), 96, listOf("Tosh Gitonga"), listOf("Joseph Wairimu"), true, "KFCB Local Registry", 4.8, 120, 0, 0),
                Title("movie_495449", "495449", "movie", "Supa Modo", "A terminally ill 9-year-old girl in Kenya dreams of becoming a superhero...", null, null, "2018-02-18", null, listOf("KE"), listOf("Drama", "Fantasy"), 74, listOf("Likarion Wainaina"), listOf("Stycie Waweru"), true, "KFCB Local Registry", 4.9, 110, 0, 0)
            )
        } else {
            emptyList()
        }
        return Resource.Success(titles)
    }

    override suspend fun getCuratedLocalProductions(countryCode: String): Resource<List<Title>> {
        return getTopRatedInRegion(countryCode)
    }

    override suspend fun getEmergingFilmmakers(countryCode: String): Resource<List<Filmmaker>> {
        val filmmakers = if (countryCode == "KE") {
            listOf(
                Filmmaker("fm-001", "Tosh Gitonga", "KE", "Critically acclaimed Kenyan director known for directing Nairobi Half Life and Volume.", listOf("movie_141045"), null),
                Filmmaker("fm-002", "Likarion Wainaina", "KE", "Kenyan film director and cinematographer known for Supa Modo.", listOf("movie_495449"), null),
                Filmmaker("fm-003", "Wanuri Kahiu", "KE", "Award-winning Kenyan film director and writer known for Rafiki and From a Whisper.", emptyList(), null)
            )
        } else {
            emptyList()
        }
        return Resource.Success(filmmakers)
    }

    override suspend fun getLocallyProduced(): Resource<List<Title>> {
        return try {
            val movieResponse = tmdbApi.discoverMovies(countryCode = "KE")
            val tvResponse = tmdbApi.discoverTv(countryCode = "KE")
            
            val movieTitles = movieResponse.results?.map { dto ->
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
                    originCountry = dto.originCountry ?: listOf("KE"),
                    genres = emptyList(),
                    runtimeMinutes = null,
                    directorNames = emptyList(),
                    castNames = emptyList(),
                    isLocalContent = true,
                    localSource = "TMDB Kenya",
                    averageRating = dto.voteAverage ?: 0.0,
                    ratingCount = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            } ?: emptyList()

            val tvTitles = tvResponse.results?.map { dto ->
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
                    originCountry = dto.originCountry ?: listOf("KE"),
                    genres = emptyList(),
                    runtimeMinutes = null,
                    directorNames = emptyList(),
                    castNames = emptyList(),
                    isLocalContent = true,
                    localSource = "TMDB Kenya",
                    averageRating = dto.voteAverage ?: 0.0,
                    ratingCount = 0,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            } ?: emptyList()

            Resource.Success((movieTitles + tvTitles).sortedByDescending { it.averageRating })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch locally produced content")
        }
    }

    override suspend fun getRegionallyProduced(): Resource<List<Title>> {
        return try {
            // East Africa (excluding Kenya): TZ, UG, RW
            val countries = listOf("TZ", "UG", "RW")
            val allTitles = mutableListOf<Title>()
            
            for (country in countries) {
                val movieResponse = tmdbApi.discoverMovies(countryCode = country)
                val tvResponse = tmdbApi.discoverTv(countryCode = country)
                
                movieResponse.results?.forEach { dto ->
                    allTitles.add(Title(
                        id = "movie_${dto.id}",
                        tmdbId = dto.id.toString(),
                        titleType = "movie",
                        name = dto.title ?: "",
                        overview = dto.overview ?: "",
                        posterPath = dto.posterPath,
                        backdropPath = dto.backdropPath,
                        releaseDate = dto.releaseDate,
                        firstAirDate = null,
                        originCountry = dto.originCountry ?: listOf(country),
                        genres = emptyList(),
                        runtimeMinutes = null,
                        directorNames = emptyList(),
                        castNames = emptyList(),
                        isLocalContent = false,
                        localSource = "TMDB Regional",
                        averageRating = dto.voteAverage ?: 0.0,
                        ratingCount = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    ))
                }

                tvResponse.results?.forEach { dto ->
                    allTitles.add(Title(
                        id = "tv_${dto.id}",
                        tmdbId = dto.id.toString(),
                        titleType = "tv",
                        name = dto.name ?: "",
                        overview = dto.overview ?: "",
                        posterPath = dto.posterPath,
                        backdropPath = dto.backdropPath,
                        releaseDate = null,
                        firstAirDate = dto.firstAirDate,
                        originCountry = dto.originCountry ?: listOf(country),
                        genres = emptyList(),
                        runtimeMinutes = null,
                        directorNames = emptyList(),
                        castNames = emptyList(),
                        isLocalContent = false,
                        localSource = "TMDB Regional",
                        averageRating = dto.voteAverage ?: 0.0,
                        ratingCount = 0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            }
            
            Resource.Success(allTitles.distinctBy { it.tmdbId }.sortedByDescending { it.averageRating })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch regionally produced content")
        }
    }
}
