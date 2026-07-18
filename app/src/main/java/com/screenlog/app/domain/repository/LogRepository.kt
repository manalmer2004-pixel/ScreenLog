package com.screenlog.app.domain.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.LogEntry
import kotlinx.coroutines.flow.Flow

interface LogRepository {
    fun getUserLogsFlow(userId: String): Flow<List<LogEntry>>
    suspend fun logTitle(
        titleType: String,
        tmdbId: String,
        titleName: String,
        posterPath: String?,
        releaseDate: String?,
        rating: Int,
        reviewText: String?,
        languageCode: String?,
        containsSpoilers: Boolean,
        watchedDate: Long
    ): Resource<LogEntry>
    suspend fun syncPendingLogs(): Resource<Unit>
    suspend fun getLocalAndRemoteLogs(userId: String): Resource<List<LogEntry>>
    suspend fun deleteLog(userId: String, logId: String): Resource<Unit>
    suspend fun updateLog(log: LogEntry): Resource<LogEntry>
}
