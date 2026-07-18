package com.screenlog.app.core.di

import android.content.Context
import androidx.room.Room
import com.screenlog.app.core.common.Constants
import com.screenlog.app.data.local.ScreenLogDatabase
import com.screenlog.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ScreenLogDatabase {
        return Room.databaseBuilder(
            context,
            ScreenLogDatabase::class.java,
            Constants.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(db: ScreenLogDatabase): UserDao = db.userDao()

    @Provides
    fun provideTitleDao(db: ScreenLogDatabase): TitleDao = db.titleDao()

    @Provides
    fun provideLogDao(db: ScreenLogDatabase): LogDao = db.logDao()

    @Provides
    fun provideWatchlistDao(db: ScreenLogDatabase): WatchlistDao = db.watchlistDao()

    @Provides
    fun provideReviewDao(db: ScreenLogDatabase): ReviewDao = db.reviewDao()

    @Provides
    fun provideRecommendationDao(db: ScreenLogDatabase): RecommendationDao = db.recommendationDao()
}
