package com.browser4kids.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 访问日志实体 - 记录通过密码解锁的URL访问
 */
@Entity(tableName = "access_logs")
data class AccessLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
