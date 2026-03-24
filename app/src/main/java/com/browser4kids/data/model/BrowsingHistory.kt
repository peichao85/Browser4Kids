package com.browser4kids.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 浏览历史记录实体
 */
@Entity(tableName = "browsing_history")
data class BrowsingHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
