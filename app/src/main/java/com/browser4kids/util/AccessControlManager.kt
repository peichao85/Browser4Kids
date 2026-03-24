package com.browser4kids.util

import com.browser4kids.data.model.WhitelistRule
import com.browser4kids.repository.WhitelistRepository

/**
 * 临时授权信息
 */
data class TemporaryAuthorization(
    val domain: String,
    val remainingSeconds: Long
)

/**
 * 访问控制管理器 - 整合白名单验证逻辑，支持限时临时授权
 */
class AccessControlManager(
    private val whitelistRepository: WhitelistRepository
) {
    // 缓存当前已通过密码解锁的域名 (key=域名, value=到期时间戳毫秒, 0表示永久)
    private val unlockedDomains = mutableMapOf<String, Long>()

    // 缓存白名单规则，供同步检查使用
    @Volatile
    private var cachedRules: List<WhitelistRule> = emptyList()

    /**
     * 更新白名单规则缓存
     */
    suspend fun refreshRulesCache() {
        cachedRules = whitelistRepository.getAllRulesSnapshot()
    }

    /**
     * 同步检查URL是否允许（用于WebViewClient的shouldOverrideUrlLoading）
     * 自动清除已过期的临时授权
     */
    fun isUrlAllowedSync(url: String): Boolean {
        // 检查是否已通过密码解锁（按域名匹配）
        val domain = UrlMatcher.extractDomain(url)
        if (domain != null) {
            val now = System.currentTimeMillis()
            // 清除已过期的授权
            unlockedDomains.entries.removeAll { (_, expiry) ->
                expiry > 0 && expiry <= now
            }
            // 检查是否有有效授权
            if (unlockedDomains.any { (d, _) -> domain == d || domain.endsWith(".$d") }) {
                return true
            }
        }

        // 检查缓存的白名单规则
        return UrlMatcher.isUrlAllowed(url, cachedRules)
    }

    /**
     * 检查URL是否允许访问（异步，从数据库读取最新规则）
     */
    suspend fun isUrlAllowed(url: String): Boolean {
        // 先检查已解锁域名
        val domain = UrlMatcher.extractDomain(url)
        if (domain != null) {
            val now = System.currentTimeMillis()
            unlockedDomains.entries.removeAll { (_, expiry) ->
                expiry > 0 && expiry <= now
            }
            if (unlockedDomains.any { (d, _) -> domain == d || domain.endsWith(".$d") }) {
                return true
            }
        }

        // 刷新缓存并检查白名单
        refreshRulesCache()
        return UrlMatcher.isUrlAllowed(url, cachedRules)
    }

    /**
     * 通过密码解锁后,记录该URL的域名为已授权
     * @param url 被解锁的URL
     * @param durationMinutes 授权时长(分钟), 0表示永久(仅当前会话)
     */
    fun unlockUrl(url: String, durationMinutes: Int = 0) {
        val domain = UrlMatcher.extractDomain(url)
        if (domain != null) {
            val expiry = if (durationMinutes > 0) {
                System.currentTimeMillis() + durationMinutes * 60 * 1000L
            } else {
                0L // 永久(当前会话内)
            }
            unlockedDomains[domain] = expiry
        }
    }

    /**
     * 获取当前有效的临时授权列表（不含永久授权）
     * @return 域名和剩余秒数的列表
     */
    fun getActiveAuthorizations(): List<TemporaryAuthorization> {
        val now = System.currentTimeMillis()
        // 先清除过期的
        unlockedDomains.entries.removeAll { (_, expiry) ->
            expiry > 0 && expiry <= now
        }
        return unlockedDomains
            .filter { (_, expiry) -> expiry > 0 } // 只返回有时限的
            .map { (domain, expiry) ->
                TemporaryAuthorization(
                    domain = domain,
                    remainingSeconds = (expiry - now) / 1000
                )
            }
            .sortedBy { it.remainingSeconds }
    }

    /**
     * 撤销指定域名的临时授权
     */
    fun revokeAuthorization(domain: String) {
        unlockedDomains.remove(domain)
    }

    /**
     * 清除所有已解锁的URL(例如应用重启时)
     */
    fun clearUnlockedUrls() {
        unlockedDomains.clear()
    }
}
