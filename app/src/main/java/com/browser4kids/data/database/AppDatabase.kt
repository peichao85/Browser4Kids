package com.browser4kids.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.browser4kids.data.model.AccessLog
import com.browser4kids.data.model.BrowsingHistory
import com.browser4kids.data.model.WhitelistRule

@Database(
    entities = [WhitelistRule::class, AccessLog::class, BrowsingHistory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun whitelistDao(): WhitelistDao
    abstract fun accessLogDao(): AccessLogDao
    abstract fun browsingHistoryDao(): BrowsingHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "browser4kids.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
