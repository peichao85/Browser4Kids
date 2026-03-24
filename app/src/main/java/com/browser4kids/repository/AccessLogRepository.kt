package com.browser4kids.repository

import com.browser4kids.data.database.AccessLogDao
import com.browser4kids.data.model.AccessLog
import kotlinx.coroutines.flow.Flow

class AccessLogRepository(private val accessLogDao: AccessLogDao) {

    fun getAllLogs(): Flow<List<AccessLog>> = accessLogDao.getAllLogs()

    fun searchLogs(query: String): Flow<List<AccessLog>> = accessLogDao.searchLogs(query)

    fun getLogsByTimeRange(startTime: Long, endTime: Long): Flow<List<AccessLog>> =
        accessLogDao.getLogsByTimeRange(startTime, endTime)

    suspend fun addLog(url: String, title: String? = null) {
        accessLogDao.insertLog(AccessLog(url = url, title = title))
    }

    suspend fun deleteAllLogs() {
        accessLogDao.deleteAllLogs()
    }

    suspend fun deleteLogsBefore(beforeTime: Long) {
        accessLogDao.deleteLogsBefore(beforeTime)
    }

    fun getLogCount(): Flow<Int> = accessLogDao.getLogCount()
}
