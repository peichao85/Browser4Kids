package com.browser4kids

import com.browser4kids.data.model.AccessLog
import com.browser4kids.data.model.BrowsingHistory
import com.browser4kids.data.model.RuleType
import com.browser4kids.data.model.WhitelistRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataModelTest {

    // ========== WhitelistRule ==========

    @Test
    fun whitelistRule_defaultId_isZero() {
        val rule = WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN)
        assertEquals(0L, rule.id)
    }

    @Test
    fun whitelistRule_defaultDescription_isEmpty() {
        val rule = WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN)
        assertEquals("", rule.description)
    }

    @Test
    fun whitelistRule_addedTime_isNonZero() {
        val rule = WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN)
        assertTrue(rule.addedTime > 0)
    }

    @Test
    fun whitelistRule_copy_changesPattern() {
        val original = WhitelistRule(id = 1, pattern = "old.com", type = RuleType.DOMAIN, description = "test")
        val copied = original.copy(pattern = "new.com")
        assertEquals("new.com", copied.pattern)
        assertEquals(1L, copied.id)
        assertEquals("test", copied.description)
        assertEquals(RuleType.DOMAIN, copied.type)
    }

    @Test
    fun whitelistRule_equality() {
        val time = System.currentTimeMillis()
        val rule1 = WhitelistRule(id = 1, pattern = "example.com", type = RuleType.DOMAIN, description = "desc", addedTime = time)
        val rule2 = WhitelistRule(id = 1, pattern = "example.com", type = RuleType.DOMAIN, description = "desc", addedTime = time)
        assertEquals(rule1, rule2)
    }

    @Test
    fun whitelistRule_inequality_differentType() {
        val rule1 = WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN)
        val rule2 = WhitelistRule(pattern = "example.com", type = RuleType.EXACT)
        assertNotEquals(rule1.type, rule2.type)
    }

    @Test
    fun ruleType_allValues() {
        val types = RuleType.entries
        assertEquals(3, types.size)
        assertTrue(types.contains(RuleType.EXACT))
        assertTrue(types.contains(RuleType.DOMAIN))
        assertTrue(types.contains(RuleType.WILDCARD))
    }

    // ========== AccessLog ==========

    @Test
    fun accessLog_defaultId_isZero() {
        val log = AccessLog(url = "https://example.com")
        assertEquals(0L, log.id)
    }

    @Test
    fun accessLog_defaultTitle_isNull() {
        val log = AccessLog(url = "https://example.com")
        assertNull(log.title)
    }

    @Test
    fun accessLog_timestamp_isNonZero() {
        val log = AccessLog(url = "https://example.com")
        assertTrue(log.timestamp > 0)
    }

    @Test
    fun accessLog_withTitle() {
        val log = AccessLog(url = "https://example.com", title = "Example Page")
        assertEquals("Example Page", log.title)
    }

    @Test
    fun accessLog_copy_changesUrl() {
        val original = AccessLog(id = 1, url = "https://old.com", title = "Old")
        val copied = original.copy(url = "https://new.com")
        assertEquals("https://new.com", copied.url)
        assertEquals(1L, copied.id)
        assertEquals("Old", copied.title)
    }

    // ========== BrowsingHistory ==========

    @Test
    fun browsingHistory_defaultId_isZero() {
        val history = BrowsingHistory(url = "https://example.com")
        assertEquals(0L, history.id)
    }

    @Test
    fun browsingHistory_defaultTitle_isNull() {
        val history = BrowsingHistory(url = "https://example.com")
        assertNull(history.title)
    }

    @Test
    fun browsingHistory_timestamp_isNonZero() {
        val history = BrowsingHistory(url = "https://example.com")
        assertTrue(history.timestamp > 0)
    }

    @Test
    fun browsingHistory_withAllFields() {
        val time = 1700000000000L
        val history = BrowsingHistory(id = 5, url = "https://example.com", title = "Test", timestamp = time)
        assertEquals(5L, history.id)
        assertEquals("https://example.com", history.url)
        assertEquals("Test", history.title)
        assertEquals(time, history.timestamp)
    }

    @Test
    fun browsingHistory_equality() {
        val time = System.currentTimeMillis()
        val h1 = BrowsingHistory(id = 1, url = "https://a.com", title = "A", timestamp = time)
        val h2 = BrowsingHistory(id = 1, url = "https://a.com", title = "A", timestamp = time)
        assertEquals(h1, h2)
    }
}
