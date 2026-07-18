package com.screenlog.app.domain.repository

import com.screenlog.app.core.common.Resource
import com.screenlog.app.domain.model.AnalyticsSummary
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun getAnalyticsSummaryFlow(userId: String): Flow<AnalyticsSummary?>
    suspend fun computeAnalyticsSummary(userId: String): Resource<AnalyticsSummary>
}
