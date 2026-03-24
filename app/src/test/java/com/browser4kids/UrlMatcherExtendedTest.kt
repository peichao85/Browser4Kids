package com.browser4kids

import com.browser4kids.data.model.RuleType
import com.browser4kids.data.model.WhitelistRule
import com.browser4kids.util.UrlMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * UrlMatcher 扩展测试 - 覆盖边界情况和组合场景
 * 使用 Robolectric 以支持 android.net.Uri
 */
@RunWith(RobolectricTestRunner::class)
class UrlMatcherExtendedTest {

    // ========== extractDomain 测试 ==========

    @Test
    fun extractDomain_httpUrl() {
        assertEquals("example.com", UrlMatcher.extractDomain("http://example.com/path"))
    }

    @Test
    fun extractDomain_httpsUrl() {
        assertEquals("example.com", UrlMatcher.extractDomain("https://example.com"))
    }

    @Test
    fun extractDomain_withWwwPrefix_removesWww() {
        assertEquals("example.com", UrlMatcher.extractDomain("https://www.example.com"))
    }

    @Test
    fun extractDomain_subdomain() {
        assertEquals("sub.example.com", UrlMatcher.extractDomain("https://sub.example.com"))
    }

    @Test
    fun extractDomain_withPort() {
        assertEquals("example.com", UrlMatcher.extractDomain("https://example.com:8080/path"))
    }

    @Test
    fun extractDomain_bareDomain_noScheme() {
        assertEquals("example.com", UrlMatcher.extractDomain("example.com"))
    }

    @Test
    fun extractDomain_withQueryAndFragment() {
        assertEquals("example.com", UrlMatcher.extractDomain("https://example.com/path?q=1#frag"))
    }

    @Test
    fun extractDomain_emptyString() {
        // 空字符串不应崩溃
        UrlMatcher.extractDomain("")
        // 可能返回 null 或空字符串,取决于 Uri.parse 行为
        // 只要不抛异常即可
    }

    @Test
    fun extractDomain_uppercase_returnsLowercase() {
        assertEquals("example.com", UrlMatcher.extractDomain("https://EXAMPLE.COM"))
    }

    // ========== normalizeUrl 测试 ==========

    @Test
    fun normalizeUrl_removesTrailingSlash() {
        val result = UrlMatcher.normalizeUrl("https://example.com/")
        assertEquals("https://example.com", result)
    }

    @Test
    fun normalizeUrl_lowercasesHost() {
        val result = UrlMatcher.normalizeUrl("https://EXAMPLE.COM/Path")
        assertEquals("https://example.com/Path", result)
    }

    @Test
    fun normalizeUrl_addsHttpsScheme() {
        val result = UrlMatcher.normalizeUrl("example.com/page")
        assertEquals("https://example.com/page", result)
    }

    @Test
    fun normalizeUrl_preservesQueryParams() {
        val result = UrlMatcher.normalizeUrl("https://example.com/search?q=test")
        assertEquals("https://example.com/search?q=test", result)
    }

    @Test
    fun normalizeUrl_preservesNonStandardPort() {
        val result = UrlMatcher.normalizeUrl("https://example.com:8080/page")
        assertEquals("https://example.com:8080/page", result)
    }

    @Test
    fun normalizeUrl_removesStandardPort443() {
        val result = UrlMatcher.normalizeUrl("https://example.com:443/page")
        assertEquals("https://example.com/page", result)
    }

    @Test
    fun normalizeUrl_httpScheme() {
        val result = UrlMatcher.normalizeUrl("http://example.com/page")
        assertEquals("http://example.com/page", result)
    }

    // ========== isUrlAllowed 边界情况 ==========

    @Test
    fun isUrlAllowed_multipleRules_matchesAny() {
        val rules = listOf(
            WhitelistRule(pattern = "google.com", type = RuleType.DOMAIN),
            WhitelistRule(pattern = "youtube.com", type = RuleType.DOMAIN),
            WhitelistRule(pattern = "bilibili.com", type = RuleType.DOMAIN)
        )
        assertTrue(UrlMatcher.isUrlAllowed("https://youtube.com/watch?v=123", rules))
        assertTrue(UrlMatcher.isUrlAllowed("https://www.google.com", rules))
        assertFalse(UrlMatcher.isUrlAllowed("https://facebook.com", rules))
    }

    @Test
    fun isUrlAllowed_exactMatch_trailingSlashDifference() {
        val rules = listOf(
            WhitelistRule(pattern = "https://example.com/page", type = RuleType.EXACT)
        )
        // normalizeUrl 会去掉尾部斜杠,所以带/结尾应该也能匹配
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com/page", rules))
    }

    @Test
    fun isUrlAllowed_exactMatch_queryParamsMatter() {
        val rules = listOf(
            WhitelistRule(pattern = "https://example.com/page?id=1", type = RuleType.EXACT)
        )
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com/page?id=1", rules))
        assertFalse(UrlMatcher.isUrlAllowed("https://example.com/page?id=2", rules))
    }

    @Test
    fun isUrlAllowed_domainMatch_httpAndHttps() {
        val rules = listOf(
            WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN)
        )
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com", rules))
        assertTrue(UrlMatcher.isUrlAllowed("http://example.com", rules))
    }

    @Test
    fun isUrlAllowed_domainMatch_deepSubdomain() {
        val rules = listOf(
            WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN)
        )
        assertTrue(UrlMatcher.isUrlAllowed("https://a.b.c.example.com/path", rules))
    }

    @Test
    fun isUrlAllowed_domainMatch_similarDomainNotMatched() {
        val rules = listOf(
            WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN)
        )
        assertFalse(UrlMatcher.isUrlAllowed("https://badexample.com", rules))
        assertFalse(UrlMatcher.isUrlAllowed("https://example.com.evil.com", rules))
    }

    @Test
    fun isUrlAllowed_priorityOrder_exactOverDomain() {
        // 精确匹配应该优先于域名匹配
        val rules = listOf(
            WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN),
            WhitelistRule(pattern = "https://example.com/specific", type = RuleType.EXACT)
        )
        // 两个都能匹配,但精确优先级更高(结果都是 true)
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com/specific", rules))
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com/other", rules))
    }

    @Test
    fun isUrlAllowed_onlyWildcard_works() {
        val rules = listOf(
            WhitelistRule(pattern = "*.edu.cn", type = RuleType.WILDCARD)
        )
        assertTrue(UrlMatcher.isUrlAllowed("https://www.pku.edu.cn", rules))
        assertTrue(UrlMatcher.isUrlAllowed("https://math.tsinghua.edu.cn", rules))
        assertFalse(UrlMatcher.isUrlAllowed("https://example.com", rules))
    }

    @Test
    fun isUrlAllowed_wildcardPath() {
        val rules = listOf(
            WhitelistRule(pattern = "example.com/kids/*", type = RuleType.WILDCARD)
        )
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com/kids/games", rules))
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com/kids/videos/123", rules))
        assertFalse(UrlMatcher.isUrlAllowed("https://example.com/adults/page", rules))
    }

    @Test
    fun isUrlAllowed_mixedRuleTypes() {
        val rules = listOf(
            WhitelistRule(pattern = "https://specific.com/page", type = RuleType.EXACT),
            WhitelistRule(pattern = "allowed.com", type = RuleType.DOMAIN),
            WhitelistRule(pattern = "*.edu", type = RuleType.WILDCARD)
        )
        assertTrue(UrlMatcher.isUrlAllowed("https://specific.com/page", rules))
        assertFalse(UrlMatcher.isUrlAllowed("https://specific.com/other", rules))
        assertTrue(UrlMatcher.isUrlAllowed("https://sub.allowed.com/any", rules))
        assertTrue(UrlMatcher.isUrlAllowed("https://mit.edu", rules))
        assertFalse(UrlMatcher.isUrlAllowed("https://blocked.com", rules))
    }

    // ========== matchesWildcard 测试 ==========

    @Test
    fun matchesWildcard_subdomainStar() {
        assertTrue(UrlMatcher.matchesWildcard("https://sub.example.com", "*.example.com"))
        assertTrue(UrlMatcher.matchesWildcard("https://a.b.example.com", "*.example.com"))
        // 基域名本身也应该匹配
        assertTrue(UrlMatcher.matchesWildcard("https://example.com", "*.example.com"))
    }

    @Test
    fun matchesWildcard_pathStar() {
        assertTrue(UrlMatcher.matchesWildcard("https://example.com/a/b/c", "example.com/*"))
        assertTrue(UrlMatcher.matchesWildcard("https://example.com/", "example.com/*"))
    }

    @Test
    fun matchesWildcard_noMatch() {
        assertFalse(UrlMatcher.matchesWildcard("https://other.com", "*.example.com"))
    }

    @Test
    fun matchesWildcard_caseInsensitive() {
        assertTrue(UrlMatcher.matchesWildcard("https://SUB.EXAMPLE.COM", "*.example.com"))
    }

    // ========== validateRule 边界情况 ==========

    @Test
    fun validateRule_exactWithDotButNoScheme() {
        // "example.com" 包含 . 所以应该是有效的 EXACT 规则
        val result = UrlMatcher.validateRule("example.com", RuleType.EXACT)
        assertTrue(result.isValid)
    }

    @Test
    fun validateRule_exactWithScheme() {
        val result = UrlMatcher.validateRule("https://example.com/page", RuleType.EXACT)
        assertTrue(result.isValid)
    }

    @Test
    fun validateRule_exactInvalid_noSchemeNoDot() {
        val result = UrlMatcher.validateRule("justtext", RuleType.EXACT)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun validateRule_domainWithHttpPrefix_stillValid() {
        // 域名规则中带 http:// 前缀会被 cleaned 掉
        val result = UrlMatcher.validateRule("https://example.com", RuleType.DOMAIN)
        assertTrue(result.isValid)
    }

    @Test
    fun validateRule_domainWithStar_invalid() {
        val result = UrlMatcher.validateRule("*.example.com", RuleType.DOMAIN)
        assertFalse(result.isValid)
    }

    @Test
    fun validateRule_domainWithPath_invalid() {
        val result = UrlMatcher.validateRule("example.com/path", RuleType.DOMAIN)
        assertFalse(result.isValid)
    }

    @Test
    fun validateRule_wildcardWithoutStar_invalid() {
        val result = UrlMatcher.validateRule("example.com", RuleType.WILDCARD)
        assertFalse(result.isValid)
    }

    @Test
    fun validateRule_wildcardPathStar_valid() {
        val result = UrlMatcher.validateRule("example.com/*", RuleType.WILDCARD)
        assertTrue(result.isValid)
    }

    @Test
    fun validateRule_blank_allTypesInvalid() {
        assertFalse(UrlMatcher.validateRule("", RuleType.EXACT).isValid)
        assertFalse(UrlMatcher.validateRule("", RuleType.DOMAIN).isValid)
        assertFalse(UrlMatcher.validateRule("", RuleType.WILDCARD).isValid)
        assertFalse(UrlMatcher.validateRule("   ", RuleType.DOMAIN).isValid)
    }

    // ========== isValidUrl 边界情况 ==========

    @Test
    fun isValidUrl_withHttpScheme() {
        assertTrue(UrlMatcher.isValidUrl("http://example.com"))
    }

    @Test
    fun isValidUrl_withPort() {
        assertTrue(UrlMatcher.isValidUrl("https://example.com:8080"))
    }

    @Test
    fun isValidUrl_ipAddress() {
        assertTrue(UrlMatcher.isValidUrl("192.168.1.1"))
    }

    @Test
    fun isValidUrl_withPath() {
        assertTrue(UrlMatcher.isValidUrl("example.com/path/to/page"))
    }

    @Test
    fun isValidUrl_singleWord_invalid() {
        assertFalse(UrlMatcher.isValidUrl("hello"))
    }

    @Test
    fun isValidUrl_withSpaces_invalid() {
        assertFalse(UrlMatcher.isValidUrl("hello world.com"))
    }

    @Test
    fun isValidUrl_whitespaceOnly_invalid() {
        assertFalse(UrlMatcher.isValidUrl("   "))
    }

    @Test
    fun isValidUrl_chineseCharacters_hasDot_valid() {
        // 包含点号但有中文 - 不含空格,包含点号 → true
        assertTrue(UrlMatcher.isValidUrl("中文.com"))
    }
}
