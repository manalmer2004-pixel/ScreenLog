package com.screenlog.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.screenlog.app.core.common.Resource
import com.screenlog.app.data.local.dao.LogDao
import com.screenlog.app.data.local.entity.LogEntity
import com.screenlog.app.data.mapper.toDomain
import com.screenlog.app.data.mapper.toEntity
import com.screenlog.app.data.remote.firebase.FirestoreDataSource
import com.screenlog.app.domain.model.LogEntry
import com.screenlog.app.domain.model.Review
import com.screenlog.app.domain.model.SyncStatus
import com.screenlog.app.domain.repository.LogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepositoryImpl @Inject constructor(
    private val logDao: LogDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val firebaseAuth: FirebaseAuth
) : LogRepository {

    override fun getUserLogsFlow(userId: String): Flow<List<LogEntry>> {
        return logDao.getUserLogsFlow(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun logTitle(
        titleType: String,
        tmdbId: String,
        titleName: String,
        posterPath: String?,
        releaseDate: String?,
        rating: Int,
        reviewText: String?,
        languageCode: String?,
        containsSpoilers: Boolean,
        watchedDate: Long,
        logId: String?
    ): Resource<LogEntry> {
        val uid = firebaseAuth.currentUser?.uid ?: return Resource.Error("Unauthorized")
        val finalLogId = logId ?: UUID.randomUUID().toString()
        val titleId = "${titleType}_${tmdbId}"

        val logEntry = LogEntry(
            id = finalLogId,
            userId = uid,
            titleId = titleId,
            tmdbId = tmdbId,
            titleType = titleType,
            titleName = titleName,
            posterPath = posterPath,
            releaseDate = releaseDate,
            watchedDate = watchedDate,
            rating = rating,
            reviewText = reviewText,
            languageCode = languageCode ?: "en",
            containsSpoilers = containsSpoilers,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_SYNC
        )

        // Write to local database first
        logDao.insertLog(logEntry.toEntity())

        // Try syncing to Firestore
        return try {
            firestoreDataSource.saveWatchedLog(logEntry.copy(syncStatus = SyncStatus.SYNCED))
            logDao.updateSyncStatus(finalLogId, "SYNCED")
            Resource.Success(logEntry.copy(syncStatus = SyncStatus.SYNCED))
        } catch (e: Exception) {
            logDao.updateSyncStatus(finalLogId, "FAILED")
            Resource.Success(logEntry.copy(syncStatus = SyncStatus.FAILED))
        }
    }

    override suspend fun syncPendingLogs(): Resource<Unit> {
        return try {
            val pending = logDao.getPendingLogs()
            pending.forEach { logEntity ->
                val domainLog = logEntity.toDomain()
                try {
                    firestoreDataSource.saveWatchedLog(domainLog.copy(syncStatus = SyncStatus.SYNCED))
                    logDao.updateSyncStatus(logEntity.id, "SYNCED")
                } catch (e: Exception) {
                    logDao.updateSyncStatus(logEntity.id, "FAILED")
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to sync pending logs")
        }
    }

    override suspend fun getLocalAndRemoteLogs(userId: String): Resource<List<LogEntry>> {
        return try {
            val remoteLogs = firestoreDataSource.getUserLogs(userId)
            // Sync remote logs to local DB
            remoteLogs.forEach { log ->
                logDao.insertLog(log.toEntity())
            }
            val local = logDao.getUserLogs(userId).map { it.toDomain() }
            Resource.Success(local)
        } catch (e: Exception) {
            val local = logDao.getUserLogs(userId).map { it.toDomain() }
            if (local.isNotEmpty()) {
                Resource.Success(local)
            } else {
                Resource.Error(e.message ?: "Failed to fetch logs list")
            }
        }
    }

    override suspend fun deleteLog(userId: String, logId: String, titleId: String?): Resource<Unit> {
        return try {
            // Delete private log
            firestoreDataSource.deleteWatchedLog(userId, logId)
            
            // Delete public review if titleId is known (we use logId as reviewId now)
            if (titleId != null) {
                firestoreDataSource.deleteReview(titleId, logId)
            }
            
            logDao.deleteLogById(logId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            // Even if network fails, we can delete locally
            logDao.deleteLogById(logId)
            Resource.Success(Unit)
        }
    }

    override suspend fun updateLog(log: LogEntry): Resource<LogEntry> {
        return try {
            val updatedLog = log.copy(updatedAt = System.currentTimeMillis())
            logDao.insertLog(updatedLog.toEntity()) // Room handles update with onConflictReplace
            
            firestoreDataSource.saveWatchedLog(updatedLog.copy(syncStatus = SyncStatus.SYNCED))
            
            // Also update the public review if it exists (using log.id as reviewId)
            if (!log.reviewText.isNullOrBlank()) {
                val userProfile = firestoreDataSource.getUserProfile(log.userId)
                val review = Review(
                    id = log.id,
                    userId = log.userId,
                    userName = userProfile.displayName,
                    titleId = log.titleId,
                    rating = log.rating,
                    reviewText = log.reviewText,
                    languageCode = log.languageCode ?: "en",
                    containsSpoilers = log.containsSpoilers,
                    flagged = false,
                    flagReason = null,
                    createdAt = log.createdAt
                )
                firestoreDataSource.submitReview(log.titleId, review)
            } else {
                // If they cleared the review text, we might want to delete the public review
                firestoreDataSource.deleteReview(log.titleId, log.id)
            }

            logDao.updateSyncStatus(log.id, "SYNCED")
            Resource.Success(updatedLog.copy(syncStatus = SyncStatus.SYNCED))
        } catch (e: Exception) {
            logDao.updateSyncStatus(log.id, "FAILED")
            Resource.Success(log.copy(syncStatus = SyncStatus.FAILED))
        }
    }
}
