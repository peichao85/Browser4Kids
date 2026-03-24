package com.browser4kids.repository

import com.browser4kids.data.database.BrowsingHistoryDao
import com.browser4kids.data.model.BrowsingHistory
import kotlinx.coroutines.flow.Flow

class BrowsingHistoryRepository(private val browsingHistoryDao: BrowsingHistoryDao) {

    fun getAllHistory(): Flow<List<BrowsingHistory>> = browsingHistoryDao.getAllHistory()

    suspend fun addHistory(url: String, title: String? = null) {
        browsingHistoryDao.insertHistory(BrowsingHistory(url = url, title = title))
    }

    suspend fun deleteAllHistory() {
        browsingHistoryDao.deleteAllHistory()
    }
}
