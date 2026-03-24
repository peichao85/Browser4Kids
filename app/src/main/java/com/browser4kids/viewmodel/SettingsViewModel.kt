package com.browser4kids.viewmodel

import android.app.Application
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.browser4kids.Browser4KidsApplication
import com.browser4kids.data.model.AccessLog
import com.browser4kids.data.model.BrowsingHistory
import com.browser4kids.data.model.RuleType
import com.browser4kids.data.model.WhitelistRule
import com.browser4kids.repository.AccessLogRepository
import com.browser4kids.repository.BrowsingHistoryRepository
import com.browser4kids.repository.SettingsRepository
import com.browser4kids.repository.WhitelistRepository
import com.browser4kids.util.UrlMatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val message: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as Browser4KidsApplication
    private val whitelistRepository = WhitelistRepository(app.database.whitelistDao())
    private val accessLogRepository = AccessLogRepository(app.database.accessLogDao())
    private val browsingHistoryRepository = BrowsingHistoryRepository(app.database.browsingHistoryDao())
    val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val whitelistRules: Flow<List<WhitelistRule>> = whitelistRepository.getAllRules()
    val accessLogs: Flow<List<AccessLog>> = accessLogRepository.getAllLogs()
    val browsingHistory: Flow<List<BrowsingHistory>> = browsingHistoryRepository.getAllHistory()
    val ruleCount: Flow<Int> = whitelistRepository.getRuleCount()
    val logCount: Flow<Int> = accessLogRepository.getLogCount()

    // 白名单管理

    fun addRule(pattern: String, type: RuleType, description: String = "") {
        viewModelScope.launch {
            val validation = UrlMatcher.validateRule(pattern, type)
            if (!validation.isValid) {
                _uiState.value = _uiState.value.copy(message = validation.errorMessage)
                return@launch
            }
            val cleanPattern = when (type) {
                RuleType.DOMAIN -> pattern.removePrefix("http://").removePrefix("https://").removeSuffix("/").lowercase()
                RuleType.EXACT -> if (!pattern.contains("://")) "https://$pattern" else pattern
                RuleType.WILDCARD -> pattern.lowercase()
            }
            val rule = WhitelistRule(pattern = cleanPattern, type = type, description = description)
            val result = whitelistRepository.addRule(rule)
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(message = e.message)
            }
            result.onSuccess {
                _uiState.value = _uiState.value.copy(message = "规则已添加")
            }
        }
    }

    fun addRules(rules: List<WhitelistRule>) {
        viewModelScope.launch {
            whitelistRepository.addRules(rules)
            _uiState.value = _uiState.value.copy(message = "已批量添加 ${rules.size} 条规则")
        }
    }

    fun updateRule(rule: WhitelistRule) {
        viewModelScope.launch {
            val result = whitelistRepository.updateRule(rule)
            result.onFailure { e ->
                _uiState.value = _uiState.value.copy(message = e.message)
            }
            result.onSuccess {
                _uiState.value = _uiState.value.copy(message = "规则已更新")
            }
        }
    }

    fun deleteRule(rule: WhitelistRule) {
        viewModelScope.launch {
            whitelistRepository.deleteRule(rule)
        }
    }

    suspend fun testUrlAsync(url: String): Boolean {
        val rules = whitelistRepository.getAllRulesSnapshot()
        return UrlMatcher.isUrlAllowed(url, rules)
    }

    // 密码管理

    fun changePassword(oldPassword: String, newPassword: String): Boolean {
        if (!settingsRepository.verifyPassword(oldPassword)) {
            _uiState.value = _uiState.value.copy(message = "旧密码不正确")
            return false
        }
        settingsRepository.setPassword(newPassword)
        _uiState.value = _uiState.value.copy(message = "密码已修改")
        return true
    }

    fun resetPasswordWithRecovery(answer: String, newPassword: String): Boolean {
        if (!settingsRepository.verifyRecoveryAnswer(answer)) {
            _uiState.value = _uiState.value.copy(message = "答案不正确")
            return false
        }
        settingsRepository.setPassword(newPassword)
        _uiState.value = _uiState.value.copy(message = "密码已重置")
        return true
    }

    // 日志管理

    fun searchLogs(query: String): Flow<List<AccessLog>> = accessLogRepository.searchLogs(query)

    fun clearAccessLogs() {
        viewModelScope.launch {
            accessLogRepository.deleteAllLogs()
            _uiState.value = _uiState.value.copy(message = "访问日志已清除")
        }
    }

    // 浏览数据管理

    fun clearBrowsingHistory() {
        viewModelScope.launch {
            browsingHistoryRepository.deleteAllHistory()
            _uiState.value = _uiState.value.copy(message = "浏览历史已清除")
        }
    }

    fun clearCache() {
        WebStorage.getInstance().deleteAllData()
        _uiState.value = _uiState.value.copy(message = "缓存已清除")
    }

    fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
        _uiState.value = _uiState.value.copy(message = "Cookies已清除")
    }

    fun clearAllBrowsingData() {
        viewModelScope.launch {
            browsingHistoryRepository.deleteAllHistory()
            WebStorage.getInstance().deleteAllData()
            CookieManager.getInstance().removeAllCookies(null)
            _uiState.value = _uiState.value.copy(message = "所有浏览数据已清除")
        }
    }

    // 应用设置

    fun setHomeUrl(url: String) {
        settingsRepository.setHomeUrl(url)
        _uiState.value = _uiState.value.copy(message = "主页已设置")
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    // 预置网站数据
    companion object {
        val PRESET_SITES = listOf(
            WhitelistRule(pattern = "youtube.com", type = RuleType.DOMAIN, description = "YouTube视频"),
            WhitelistRule(pattern = "bilibili.com", type = RuleType.DOMAIN, description = "哔哩哔哩"),
            WhitelistRule(pattern = "wikipedia.org", type = RuleType.DOMAIN, description = "维基百科"),
            WhitelistRule(pattern = "kids.nationalgeographic.com", type = RuleType.DOMAIN, description = "国家地理儿童版"),
            WhitelistRule(pattern = "pbskids.org", type = RuleType.DOMAIN, description = "PBS Kids"),
            WhitelistRule(pattern = "khanacademy.org", type = RuleType.DOMAIN, description = "Khan Academy"),
            WhitelistRule(pattern = "scratch.mit.edu", type = RuleType.DOMAIN, description = "Scratch编程"),
            WhitelistRule(pattern = "code.org", type = RuleType.DOMAIN, description = "Code.org编程学习"),
            WhitelistRule(pattern = "coolmath-games.com", type = RuleType.DOMAIN, description = "数学游戏"),
            WhitelistRule(pattern = "funbrain.com", type = RuleType.DOMAIN, description = "趣味学习游戏"),
            WhitelistRule(pattern = "starfall.com", type = RuleType.DOMAIN, description = "Starfall学习"),
            WhitelistRule(pattern = "abcya.com", type = RuleType.DOMAIN, description = "ABCya教育游戏"),
        )
    }
}
