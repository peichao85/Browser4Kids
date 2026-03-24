package com.browser4kids

import com.browser4kids.data.model.RuleType
import com.browser4kids.data.model.WhitelistRule
import com.browser4kids.util.UrlMatcher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UrlMatcherTest {

    // 精确匹配测试

    @Test
    fun exactMatch_sameUrl_returnsTrue() {
        val rules = listOf(WhitelistRule(pattern = "https://example.com/page", type = RuleType.EXACT))
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com/page", rules))
    }

    @Test
    fun exactMatch_differentUrl_returnsFalse() {
        val rules = listOf(WhitelistRule(pattern = "https://example.com/page", type = RuleType.EXACT))
        assertFalse(UrlMatcher.isUrlAllowed("https://example.com/other", rules))
    }

    @Test
    fun exactMatch_caseInsensitiveHost() {
        val rules = listOf(WhitelistRule(pattern = "https://Example.COM/page", type = RuleType.EXACT))
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com/page", rules))
    }

    // 域名匹配测试

    @Test
    fun domainMatch_sameDomain_returnsTrue() {
        val rules = listOf(WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN))
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com", rules))
    }

    @Test
    fun domainMatch_withPath_returnsTrue() {
        val rules = listOf(WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN))
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com/any/path", rules))
    }

    @Test
    fun domainMatch_subdomain_returnsTrue() {
        val rules = listOf(WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN))
        assertTrue(UrlMatcher.isUrlAllowed("https://sub.example.com/page", rules))
    }

    @Test
    fun domainMatch_wwwPrefix_returnsTrue() {
        val rules = listOf(WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN))
        assertTrue(UrlMatcher.isUrlAllowed("https://www.example.com", rules))
    }

    @Test
    fun domainMatch_differentDomain_returnsFalse() {
        val rules = listOf(WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN))
        assertFalse(UrlMatcher.isUrlAllowed("https://other.com", rules))
    }

    @Test
    fun domainMatch_partialDomainName_returnsFalse() {
        val rules = listOf(WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN))
        assertFalse(UrlMatcher.isUrlAllowed("https://notexample.com", rules))
    }

    // 通配符匹配测试

    @Test
    fun wildcardMatch_subdomainPattern() {
        val rules = listOf(WhitelistRule(pattern = "*.example.com", type = RuleType.WILDCARD))
        assertTrue(UrlMatcher.isUrlAllowed("https://sub.example.com", rules))
    }

    @Test
    fun wildcardMatch_baseDomainAlsoMatches() {
        val rules = listOf(WhitelistRule(pattern = "*.example.com", type = RuleType.WILDCARD))
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com", rules))
    }

    @Test
    fun wildcardMatch_pathPattern() {
        val rules = listOf(WhitelistRule(pattern = "example.com/*", type = RuleType.WILDCARD))
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com/any/path", rules))
    }

    @Test
    fun wildcardMatch_noMatch() {
        val rules = listOf(WhitelistRule(pattern = "*.example.com", type = RuleType.WILDCARD))
        assertFalse(UrlMatcher.isUrlAllowed("https://other.com", rules))
    }

    // 优先级测试

    @Test
    fun priority_exactMatchFirst() {
        val rules = listOf(
            WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN),
            WhitelistRule(pattern = "https://example.com/page", type = RuleType.EXACT)
        )
        assertTrue(UrlMatcher.isUrlAllowed("https://example.com/page", rules))
    }

    // 空规则测试

    @Test
    fun emptyRules_returnsFalse() {
        assertFalse(UrlMatcher.isUrlAllowed("https://example.com", emptyList()))
    }

    // URL验证测试

    @Test
    fun isValidUrl_validHttps() {
        assertTrue(UrlMatcher.isValidUrl("https://example.com"))
    }

    @Test
    fun isValidUrl_validDomain() {
        assertTrue(UrlMatcher.isValidUrl("example.com"))
    }

    @Test
    fun isValidUrl_invalidText() {
        assertFalse(UrlMatcher.isValidUrl("hello world"))
    }

    @Test
    fun isValidUrl_emptyString() {
        assertFalse(UrlMatcher.isValidUrl(""))
    }

    // 规则验证测试

    @Test
    fun validateRule_validDomain() {
        val result = UrlMatcher.validateRule("example.com", RuleType.DOMAIN)
        assertTrue(result.isValid)
    }

    @Test
    fun validateRule_invalidDomain_withSlash() {
        val result = UrlMatcher.validateRule("example.com/path", RuleType.DOMAIN)
        assertFalse(result.isValid)
    }

    @Test
    fun validateRule_validWildcard() {
        val result = UrlMatcher.validateRule("*.example.com", RuleType.WILDCARD)
        assertTrue(result.isValid)
    }

    @Test
    fun validateRule_invalidWildcard_noStar() {
        val result = UrlMatcher.validateRule("example.com", RuleType.WILDCARD)
        assertFalse(result.isValid)
    }

    @Test
    fun validateRule_emptyPattern() {
        val result = UrlMatcher.validateRule("", RuleType.DOMAIN)
        assertFalse(result.isValid)
    }
}
