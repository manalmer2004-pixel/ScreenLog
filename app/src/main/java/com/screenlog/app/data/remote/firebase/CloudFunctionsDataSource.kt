package com.screenlog.app.data.remote.firebase

import com.google.firebase.functions.FirebaseFunctions
import com.screenlog.app.domain.model.Recommendation
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudFunctionsDataSource @Inject constructor(
    private val functions: FirebaseFunctions
) {
    suspend fun generateRecommendations(userId: String): List<Recommendation> {
        val data = hashMapOf(
            "userId" to userId,
            "limit" to 20
        )
        
        val result = functions
            .getHttpsCallable("generateRecommendations")
            .call(data)
            .await()
            
        // Parse the list from functions result
        val resultList = result.data as? List<Map<String, Any>> ?: return emptyList()
        return resultList.map { item ->
            Recommendation(
                id = item["id"] as? String ?: "",
                userId = userId,
                titleId = item["titleId"] as? String ?: "",
                tmdbId = item["tmdbId"] as? String ?: "",
                titleType = item["titleType"] as? String ?: "movie",
                score = (item["score"] as? Number)?.toDouble() ?: 0.0,
                reason = item["reason"] as? String ?: "Based on your watch history",
                generatedAt = (item["generatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }

    suspend fun flagReview(reviewId: String, titleId: String, reason: String) {
        val data = hashMapOf(
            "reviewId" to reviewId,
            "titleId" to titleId,
            "reason" to reason
        )
        functions
            .getHttpsCallable("flagReview")
            .call(data)
            .await()
    }

    // Helper extension to await task results inside coroutines
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
        return suspendCancellableCoroutine { continuation ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(task.result, null)
                } else {
                    continuation.resumeWith(Result.failure(task.exception ?: Exception("Functions call error")))
                }
            }
        }
    }
}
