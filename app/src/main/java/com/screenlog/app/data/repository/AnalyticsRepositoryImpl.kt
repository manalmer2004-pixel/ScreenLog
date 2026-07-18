package com.screenlog.app.data.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.data.local.dao.LogDao
import com.screenlog.app.data.local.dao.TitleDao
import com.screenlog.app.data.mapper.toDomain
import com.screenlog.app.domain.model.AnalyticsSummary
import com.screenlog.app.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val logDao: LogDao,
    private val titleDao: TitleDao
) : AnalyticsRepository {

    override fun getAnalyticsSummaryFlow(userId: String): Flow<AnalyticsSummary?> {
        return logDao.getUserLogsFlow(userId).map { logs ->
            if (logs.isEmpty()) null else calculateStats(logs.map { it.toDomain() })
        }
    }

    override suspend fun computeAnalyticsSummary(userId: String): Resource<AnalyticsSummary> {
        return try {
            val logs = logDao.getUserLogs(userId).map { it.toDomain() }
            if (logs.isEmpty()) {
                Resource.Error("No logged titles found.")
            } else {
                Resource.Success(calculateStats(logs))
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to compute stats")
        }
    }

    private suspend fun calculateStats(logs: List<com.screenlog.app.domain.model.LogEntry>): AnalyticsSummary {
        val total = logs.size
        var movies = 0
        var tv = 0
        var ratingSum = 0.0
        val ratingDist = mutableMapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
        val logsByMonth = mutableMapOf<String, Int>()
        
        val genreCounts = mutableMapOf<String, Int>()
        val countryCounts = mutableMapOf<String, Int>()
        val directorCounts = mutableMapOf<String, Int>()

        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        logs.forEach { log ->
            if (log.titleType == "movie") movies++ else tv++
            ratingSum += log.rating
            ratingDist[log.rating] = (ratingDist[log.rating] ?: 0) + 1
            
            val monthStr = sdf.format(Date(log.watchedDate))
            logsByMonth[monthStr] = (logsByMonth[monthStr] ?: 0) + 1

            // Fetch matched title from local DB for rich attributes (genres, directors, countries)
            val cachedTitle = titleDao.getTitle(log.titleType, log.tmdbId)
            if (cachedTitle != null) {
                cachedTitle.genres.forEach { g ->
                    genreCounts[g] = (genreCounts[g] ?: 0) + 1
                }
                cachedTitle.originCountry.forEach { c ->
                    countryCounts[c] = (countryCounts[c] ?: 0) + 1
                }
                cachedTitle.directorNames.forEach { d ->
                    directorCounts[d] = (directorCounts[d] ?: 0) + 1
                }
            }
        }

        return AnalyticsSummary(
            totalLogged = total,
            movieCount = movies,
            tvCount = tv,
            averageRating = if (total > 0) ratingSum / total else 0.0,
            topGenres = genreCounts.toList().sortedByDescending { it.second }.take(5),
            topCountries = countryCounts.toList().sortedByDescending { it.second }.take(5),
            favoriteDirectors = directorCounts.toList().sortedByDescending { it.second }.take(5),
            logsByMonth = logsByMonth,
            ratingDistribution = ratingDist
        )
    }
}
