package com.browser4kids.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.browser4kids.data.model.BrowsingHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowsingHistoryDao {

    @Query("SELECT * FROM browsing_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<BrowsingHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: BrowsingHistory): Long

    @Query("DELETE FROM browsing_history")
    suspend fun deleteAllHistory()
}
