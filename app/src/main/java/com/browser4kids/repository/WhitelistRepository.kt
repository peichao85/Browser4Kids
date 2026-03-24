package com.browser4kids.repository

import com.browser4kids.data.database.WhitelistDao
import com.browser4kids.data.model.RuleType
import com.browser4kids.data.model.WhitelistRule
import kotlinx.coroutines.flow.Flow

class WhitelistRepository(private val whitelistDao: WhitelistDao) {

    fun getAllRules(): Flow<List<WhitelistRule>> = whitelistDao.getAllRules()

    suspend fun getAllRulesSnapshot(): List<WhitelistRule> = whitelistDao.getAllRulesSnapshot()

    suspend fun getRuleById(id: Long): WhitelistRule? = whitelistDao.getRuleById(id)

    suspend fun addRule(rule: WhitelistRule): Result<Long> {
        val duplicate = whitelistDao.findDuplicate(rule.pattern, rule.type)
        if (duplicate != null) {
            return Result.failure(IllegalArgumentException("规则已存在"))
        }
        val id = whitelistDao.insertRule(rule)
        return Result.success(id)
    }

    suspend fun addRules(rules: List<WhitelistRule>) {
        whitelistDao.insertRules(rules)
    }

    suspend fun updateRule(rule: WhitelistRule): Result<Unit> {
        val existing = whitelistDao.findDuplicate(rule.pattern, rule.type)
        if (existing != null && existing.id != rule.id) {
            return Result.failure(IllegalArgumentException("规则已存在"))
        }
        whitelistDao.updateRule(rule)
        return Result.success(Unit)
    }

    suspend fun deleteRule(rule: WhitelistRule) {
        whitelistDao.deleteRule(rule)
    }

    suspend fun deleteAllRules() {
        whitelistDao.deleteAllRules()
    }

    fun getRuleCount(): Flow<Int> = whitelistDao.getRuleCount()
}
