package com.screenlog.app.core.common

object Constants {
    const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
    
    const val DATABASE_NAME = "screenlog_db"
    
    // Firestore Collection Names
    const val USERS_COLLECTION = "users"
    const val TITLES_COLLECTION = "titles"
    const val LOGS_COLLECTION = "logs"
    const val WATCHLIST_COLLECTION = "watchlist"
    const val REVIEWS_COLLECTION = "reviews"
    const val RECOMMENDATIONS_COLLECTION = "recommendations"
    const val REGIONS_COLLECTION = "regions"
    const val CHARTS_COLLECTION = "charts"
    const val LOCAL_REGISTRY_COLLECTION = "localContentRegistry"
    const val MODERATION_QUEUE_COLLECTION = "moderationQueue"
}
