package com.browser4kids.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.browser4kids.data.model.AccessLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessLogDao {

    @Query("SELECT * FROM access_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AccessLog>>

    @Query("SELECT * FROM access_logs WHERE url LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchLogs(query: String): Flow<List<AccessLog>>

    @Query("SELECT * FROM access_logs WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getLogsByTimeRange(startTime: Long, endTime: Long): Flow<List<AccessLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AccessLog): Long

    @Query("DELETE FROM access_logs")
    suspend fun deleteAllLogs()

    @Query("DELETE FROM access_logs WHERE timestamp < :beforeTime")
    suspend fun deleteLogsBefore(beforeTime: Long)

    @Query("SELECT COUNT(*) FROM access_logs")
    fun getLogCount(): Flow<Int>
}
