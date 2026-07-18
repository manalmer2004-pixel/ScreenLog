package com.screenlog.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.screenlog.app.core.common.Resource
import com.screenlog.app.data.local.dao.WatchlistDao
import com.screenlog.app.data.local.entity.WatchlistEntity
import com.screenlog.app.data.mapper.toDomain
import com.screenlog.app.data.mapper.toEntity
import com.screenlog.app.data.remote.firebase.FirestoreDataSource
import com.screenlog.app.domain.model.WatchlistItem
import com.screenlog.app.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val firebaseAuth: FirebaseAuth
) : WatchlistRepository {

    override fun getWatchlistFlow(userId: String): Flow<List<WatchlistItem>> {
        return watchlistDao.getWatchlistFlow(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addToWatchlist(
        titleType: String,
        tmdbId: String,
        titleName: String,
        posterPath: String?,
        releaseDate: String?
    ): Resource<WatchlistItem> {
        val uid = firebaseAuth.currentUser?.uid ?: return Resource.Error("Unauthorized")
        val itemId = UUID.randomUUID().toString()
        val titleId = "${titleType}_${tmdbId}"

        val item = WatchlistItem(
            id = itemId,
            userId = uid,
            titleId = titleId,
            tmdbId = tmdbId,
            titleType = titleType,
            titleName = titleName,
            posterPath = posterPath,
            releaseDate = releaseDate,
            addedAt = System.currentTimeMillis(),
            syncStatus = "PENDING"
        )

        watchlistDao.insertWatchlistItem(item.toEntity())

        return try {
            firestoreDataSource.saveWatchlistItem(item.copy(syncStatus = "SYNCED"))
            watchlistDao.insertWatchlistItem(item.copy(syncStatus = "SYNCED").toEntity())
            Resource.Success(item.copy(syncStatus = "SYNCED"))
        } catch (e: Exception) {
            Resource.Success(item.copy(syncStatus = "FAILED"))
        }
    }

    override suspend fun removeFromWatchlist(titleType: String, tmdbId: String): Resource<Unit> {
        val uid = firebaseAuth.currentUser?.uid ?: return Resource.Error("Unauthorized")
        return try {
            val cachedItem = watchlistDao.getWatchlistItem(uid, titleType, tmdbId)
            watchlistDao.deleteWatchlistItem(uid, titleType, tmdbId)
            if (cachedItem != null) {
                firestoreDataSource.removeWatchlistItem(uid, cachedItem.id)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to remove from watchlist")
        }
    }

    override suspend fun isTitleInWatchlist(titleType: String, tmdbId: String): Boolean {
        val uid = firebaseAuth.currentUser?.uid ?: return false
        return watchlistDao.getWatchlistItem(uid, titleType, tmdbId) != null
    }

    override suspend fun syncWatchlist(userId: String): Resource<Unit> {
        return try {
            val remoteItems = firestoreDataSource.getUserWatchlist(userId)
            remoteItems.forEach { item ->
                watchlistDao.insertWatchlistItem(item.toEntity())
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to sync watchlist")
        }
    }
}
