package com.screenlog.app.data.local.dao

import androidx.room.*
import com.screenlog.app.data.local.entity.LogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM user_logs WHERE userId = :userId ORDER BY watchedDate DESC")
    fun getUserLogsFlow(userId: String): Flow<List<LogEntity>>

    @Query("SELECT * FROM user_logs WHERE userId = :userId ORDER BY watchedDate DESC")
    suspend fun getUserLogs(userId: String): List<LogEntity>

    @Query("SELECT * FROM user_logs WHERE syncStatus = 'PENDING_SYNC'")
    suspend fun getPendingLogs(): List<LogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<LogEntity>)

    @Query("UPDATE user_logs SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    @Query("DELETE FROM user_logs WHERE id = :id")
    suspend fun deleteLogById(id: String)

    @Delete
    suspend fun deleteLog(log: LogEntity)
}
