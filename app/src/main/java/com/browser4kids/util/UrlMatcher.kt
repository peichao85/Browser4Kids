package com.browser4kids.util

import android.net.Uri
import com.browser4kids.data.model.RuleType
import com.browser4kids.data.model.WhitelistRule

/**
 * URL匹配工具类 - 支持精确匹配、域名匹配、通配符匹配
 */
object UrlMatcher {

    /**
     * 检查URL是否被白名单允许
     * 匹配优先级: 精确URL > 域名 > 通配符
     */
    fun isUrlAllowed(url: String, rules: List<WhitelistRule>): Boolean {
        val normalizedUrl = normalizeUrl(url)

        // 1. 精确匹配
        if (rules.any { it.type == RuleType.EXACT && normalizeUrl(it.pattern) == normalizedUrl }) {
            return true
        }

        // 2. 域名匹配
        val domain = extractDomain(normalizedUrl)
        if (domain != null) {
            for (rule in rules.filter { it.type == RuleType.DOMAIN }) {
                val ruleDomain = rule.pattern.lowercase().removePrefix("www.")
                if (domain == ruleDomain || domain.endsWith(".$ruleDomain")) {
                    return true
                }
            }
        }

        // 3. 通配符匹配
        for (rule in rules.filter { it.type == RuleType.WILDCARD }) {
            if (matchesWildcard(normalizedUrl, rule.pattern)) {
                return true
            }
        }

        return false
    }

    /**
     * 从URL中提取域名(不含端口)
     */
    fun extractDomain(url: String): String? {
        if (url.isBlank()) return null
        return try {
            val uri = Uri.parse(if (url.contains("://")) url else "https://$url")
            val host = uri.host?.lowercase()?.removePrefix("www.") ?: return null
            // 验证域名至少包含一个点，避免返回 "v1"、"utils" 等非域名
            if (!host.contains(".")) return null
            host
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 标准化URL: 转小写schema和host,去除尾部斜杠
     */
    fun normalizeUrl(url: String): String {
        return try {
            val uri = Uri.parse(if (url.contains("://")) url else "https://$url")
            val scheme = uri.scheme?.lowercase() ?: "https"
            val host = uri.host?.lowercase() ?: return url.lowercase()
            val port = if (uri.port > 0 && uri.port != 80 && uri.port != 443) ":${uri.port}" else ""
            val path = uri.path?.trimEnd('/') ?: ""
            val query = if (uri.query != null) "?${uri.query}" else ""
            "$scheme://$host$port$path$query"
        } catch (e: Exception) {
            url.lowercase()
        }
    }

    /**
     * 通配符匹配: 支持 * 匹配任意字符序列
     * 例: *.example.com 匹配 sub.example.com
     * 例: example.com/[*] 匹配 example.com/any/path
     */
    fun matchesWildcard(url: String, pattern: String): Boolean {
        val normalizedUrl = normalizeUrl(url)

        // 将pattern中的域名部分与URL域名部分匹配
        val patternLower = pattern.lowercase()

        // 如果pattern以*.开头,匹配子域名
        if (patternLower.startsWith("*.")) {
            val baseDomain = patternLower.removePrefix("*.")
            val urlDomain = extractDomain(normalizedUrl) ?: return false
            return urlDomain == baseDomain || urlDomain.endsWith(".$baseDomain")
        }

        // 如果pattern以 /[*] 结尾或包含 /[*],匹配路径
        if (patternLower.contains("/*")) {
            val patternParts = patternLower.split("/*", limit = 2)
            val patternBase = normalizeUrl(patternParts[0])
            return normalizedUrl.startsWith(patternBase)
        }

        // 通用通配符: 将*转为正则
        val regex = buildString {
            append("^")
            for (char in patternLower) {
                when (char) {
                    '*' -> append(".*")
                    '.' -> append("\\.")
                    '?' -> append("\\?")
                    else -> append(char)
                }
            }
            append("$")
        }

        return try {
            Regex(regex).matches(normalizedUrl)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 验证URL规则格式是否有效
     */
    fun validateRule(pattern: String, type: RuleType): RuleValidation {
        if (pattern.isBlank()) {
            return RuleValidation(false, "规则不能为空")
        }

        return when (type) {
            RuleType.EXACT -> {
                if (!pattern.contains("://") && !pattern.contains(".")) {
                    RuleValidation(false, "请输入有效的URL,例如 https://example.com/page")
                } else {
                    RuleValidation(true)
                }
            }
            RuleType.DOMAIN -> {
                val cleaned = pattern.removePrefix("http://").removePrefix("https://")
                    .removeSuffix("/")
                if (!cleaned.contains(".") || cleaned.contains("/") || cleaned.contains("*")) {
                    RuleValidation(false, "请输入有效的域名,例如 example.com")
                } else {
                    RuleValidation(true)
                }
            }
            RuleType.WILDCARD -> {
                if (!pattern.contains("*")) {
                    RuleValidation(false, "通配符规则需要包含*,例如 *.example.com")
                } else {
                    RuleValidation(true)
                }
            }
        }
    }

    /**
     * 判断输入是否为有效的URL格式
     */
    fun isValidUrl(input: String): Boolean {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return false

        // 包含协议头的URL
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return try {
                val uri = Uri.parse(trimmed)
                uri.host != null && uri.host!!.contains(".")
            } catch (e: Exception) {
                false
            }
        }

        // 不含协议头但看起来像域名
        return trimmed.contains(".") && !trimmed.contains(" ")
    }
}

data class RuleValidation(
    val isValid: Boolean,
    val errorMessage: String? = null
)
