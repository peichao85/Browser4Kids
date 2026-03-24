package com.browser4kids.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.browser4kids.data.model.WhitelistRule
import kotlinx.coroutines.flow.Flow

@Dao
interface WhitelistDao {

    @Query("SELECT * FROM whitelist_rules ORDER BY addedTime DESC")
    fun getAllRules(): Flow<List<WhitelistRule>>

    @Query("SELECT * FROM whitelist_rules")
    suspend fun getAllRulesSnapshot(): List<WhitelistRule>

    @Query("SELECT * FROM whitelist_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): WhitelistRule?

    @Query("SELECT * FROM whitelist_rules WHERE pattern = :pattern AND type = :type LIMIT 1")
    suspend fun findDuplicate(pattern: String, type: com.browser4kids.data.model.RuleType): WhitelistRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: WhitelistRule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<WhitelistRule>)

    @Update
    suspend fun updateRule(rule: WhitelistRule)

    @Delete
    suspend fun deleteRule(rule: WhitelistRule)

    @Query("DELETE FROM whitelist_rules")
    suspend fun deleteAllRules()

    @Query("SELECT COUNT(*) FROM whitelist_rules")
    fun getRuleCount(): Flow<Int>
}
