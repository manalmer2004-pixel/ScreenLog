package com.screenlog.app.data.remote.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.screenlog.app.core.common.Constants
import com.screenlog.app.domain.model.LocalRegistryEntry
import com.screenlog.app.domain.model.LogEntry
import com.screenlog.app.domain.model.Review
import com.screenlog.app.domain.model.User
import com.screenlog.app.domain.model.WatchlistItem
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveUserProfile(user: User) {
        val userMap = hashMapOf(
            "userId" to user.userId,
            "displayName" to user.displayName,
            "email" to user.email,
            "homeCountry" to user.homeCountry,
            "photoUrl" to user.photoUrl,
            "createdAt" to user.createdAt,
            "updatedAt" to user.updatedAt,
            "isModerator" to user.isModerator
        )
        firestore.collection(Constants.USERS_COLLECTION)
            .document(user.userId)
            .set(userMap)
            .await()
    }

    suspend fun getUserProfile(userId: String): User {
        val doc = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .get()
            .await()
        return User(
            userId = doc.getString("userId") ?: userId,
            displayName = doc.getString("displayName") ?: "",
            email = doc.getString("email") ?: "",
            homeCountry = doc.getString("homeCountry") ?: "KE",
            photoUrl = doc.getString("photoUrl"),
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
            isModerator = doc.getBoolean("isModerator") ?: false
        )
    }

    suspend fun saveWatchedLog(log: LogEntry) {
        val logMap = hashMapOf(
            "id" to log.id,
            "userId" to log.userId,
            "titleId" to log.titleId,
            "tmdbId" to log.tmdbId,
            "titleType" to log.titleType,
            "titleName" to log.titleName,
            "posterPath" to log.posterPath,
            "releaseDate" to log.releaseDate,
            "watchedDate" to log.watchedDate,
            "rating" to log.rating,
            "reviewText" to log.reviewText,
            "languageCode" to log.languageCode,
            "containsSpoilers" to log.containsSpoilers,
            "createdAt" to log.createdAt,
            "updatedAt" to log.updatedAt
        )
        // Set under users/{userId}/logs/{logId}
        firestore.collection(Constants.USERS_COLLECTION)
            .document(log.userId)
            .collection(Constants.LOGS_COLLECTION)
            .document(log.id)
            .set(logMap)
            .await()
    }

    suspend fun getUserLogs(userId: String): List<LogEntry> {
        val query = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.LOGS_COLLECTION)
            .get()
            .await()

        return query.documents.map { doc ->
            LogEntry(
                id = doc.getString("id") ?: doc.id,
                userId = doc.getString("userId") ?: userId,
                titleId = doc.getString("titleId") ?: "",
                tmdbId = doc.getString("tmdbId") ?: "",
                titleType = doc.getString("titleType") ?: "movie",
                titleName = doc.getString("titleName") ?: "",
                posterPath = doc.getString("posterPath"),
                releaseDate = doc.getString("releaseDate"),
                watchedDate = doc.getLong("watchedDate") ?: System.currentTimeMillis(),
                rating = doc.getLong("rating")?.toInt() ?: 0,
                reviewText = doc.getString("reviewText"),
                languageCode = doc.getString("languageCode"),
                containsSpoilers = doc.getBoolean("containsSpoilers") ?: false,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                syncStatus = com.screenlog.app.domain.model.SyncStatus.SYNCED
            )
        }
    }

    suspend fun deleteWatchedLog(userId: String, logId: String) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.LOGS_COLLECTION)
            .document(logId)
            .delete()
            .await()
    }

    suspend fun saveWatchlistItem(item: WatchlistItem) {
        val itemMap = hashMapOf(
            "id" to item.id,
            "userId" to item.userId,
            "titleId" to item.titleId,
            "tmdbId" to item.tmdbId,
            "titleType" to item.titleType,
            "titleName" to item.titleName,
            "posterPath" to item.posterPath,
            "releaseDate" to item.releaseDate,
            "addedAt" to item.addedAt
        )
        firestore.collection(Constants.USERS_COLLECTION)
            .document(item.userId)
            .collection(Constants.WATCHLIST_COLLECTION)
            .document(item.id)
            .set(itemMap)
            .await()
    }

    suspend fun getUserWatchlist(userId: String): List<WatchlistItem> {
        val query = firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.WATCHLIST_COLLECTION)
            .get()
            .await()

        return query.documents.map { doc ->
            WatchlistItem(
                id = doc.getString("id") ?: doc.id,
                userId = doc.getString("userId") ?: userId,
                titleId = doc.getString("titleId") ?: "",
                tmdbId = doc.getString("tmdbId") ?: "",
                titleType = doc.getString("titleType") ?: "movie",
                titleName = doc.getString("titleName") ?: "",
                posterPath = doc.getString("posterPath"),
                releaseDate = doc.getString("releaseDate"),
                addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis(),
                syncStatus = "SYNCED"
            )
        }
    }

    suspend fun removeWatchlistItem(userId: String, itemId: String) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .collection(Constants.WATCHLIST_COLLECTION)
            .document(itemId)
            .delete()
            .await()
    }

    suspend fun getTitleReviews(titleId: String): List<Review> {
        val query = firestore.collection(Constants.TITLES_COLLECTION)
            .document(titleId)
            .collection(Constants.REVIEWS_COLLECTION)
            .orderBy("createdAt")
            .get()
            .await()

        return query.documents.map { doc ->
            Review(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                userName = doc.getString("userName") ?: "User #${doc.getString("userId")?.take(4)}",
                titleId = doc.getString("titleId") ?: titleId,
                rating = doc.getLong("rating")?.toInt() ?: 0,
                reviewText = doc.getString("reviewText") ?: "",
                languageCode = doc.getString("languageCode") ?: "en",
                containsSpoilers = doc.getBoolean("containsSpoilers") ?: false,
                flagged = doc.getBoolean("flagged") ?: false,
                flagReason = doc.getString("flagReason"),
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun submitReview(titleId: String, review: Review) {
        val reviewMap = hashMapOf(
            "id" to review.id,
            "userId" to review.userId,
            "userName" to review.userName,
            "titleId" to titleId,
            "rating" to review.rating,
            "reviewText" to review.reviewText,
            "languageCode" to review.languageCode,
            "containsSpoilers" to review.containsSpoilers,
            "flagged" to review.flagged,
            "flagReason" to review.flagReason,
            "createdAt" to review.createdAt
        )
        firestore.collection(Constants.TITLES_COLLECTION)
            .document(titleId)
            .collection(Constants.REVIEWS_COLLECTION)
            .document(review.id)
            .set(reviewMap)
            .await()
    }

    suspend fun deleteReview(titleId: String, reviewId: String) {
        firestore.collection(Constants.TITLES_COLLECTION)
            .document(titleId)
            .collection(Constants.REVIEWS_COLLECTION)
            .document(reviewId)
            .delete()
            .await()
    }

    suspend fun flagReview(titleId: String, reviewId: String, reason: String) {
        val updates = hashMapOf<String, Any>(
            "flagged" to true,
            "flagReason" to reason
        )
        firestore.collection(Constants.TITLES_COLLECTION)
            .document(titleId)
            .collection(Constants.REVIEWS_COLLECTION)
            .document(reviewId)
            .set(updates, SetOptions.merge())
            .await()
    }

    suspend fun getLocalRegistry(): List<LocalRegistryEntry> {
        val query = firestore.collection(Constants.LOCAL_REGISTRY_COLLECTION)
            .get()
            .await()

        return query.documents.map { doc ->
            LocalRegistryEntry(
                registryId = doc.id,
                titleName = doc.getString("titleName") ?: "",
                countryCode = doc.getString("countryCode") ?: "KE",
                year = doc.getLong("year")?.toInt() ?: 0,
                type = doc.getString("type") ?: "movie",
                tmdbId = doc.getString("tmdbId") ?: "",
                source = doc.getString("source") ?: "",
                isLocalContent = doc.getBoolean("isLocalContent") ?: true,
                languages = (doc.get("languages") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        }
    }

    // Helper extension to await task results inside coroutines
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result, null)
                } else {
                    continuation.resumeWith(Result.failure(task.exception ?: Exception("Firestore error")))
                }
            }
        }
    }
}
