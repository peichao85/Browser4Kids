package com.browser4kids.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 白名单规则类型
 */
enum class RuleType {
    EXACT,      // 精确URL匹配
    DOMAIN,     // 域名匹配
    WILDCARD    // 通配符匹配
}

/**
 * 白名单规则实体
 */
@Entity(tableName = "whitelist_rules")
data class WhitelistRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String,
    val type: RuleType,
    val description: String = "",
    val addedTime: Long = System.currentTimeMillis()
)
