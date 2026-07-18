package com.screenlog.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.screenlog.app.data.local.converters.Converters
import com.screenlog.app.data.local.dao.*
import com.screenlog.app.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        TitleEntity::class,
        LogEntity::class,
        WatchlistEntity::class,
        ReviewEntity::class,
        RecommendationEntity::class,
        RegionEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ScreenLogDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun titleDao(): TitleDao
    abstract fun logDao(): LogDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun reviewDao(): ReviewDao
    abstract fun recommendationDao(): RecommendationDao
}
