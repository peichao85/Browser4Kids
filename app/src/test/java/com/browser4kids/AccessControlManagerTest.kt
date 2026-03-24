package com.browser4kids

import com.browser4kids.data.model.RuleType
import com.browser4kids.data.model.WhitelistRule
import com.browser4kids.repository.WhitelistRepository
import com.browser4kids.util.AccessControlManager
import com.browser4kids.util.UrlMatcher
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AccessControlManagerTest {

    private lateinit var whitelistRepository: WhitelistRepository
    private lateinit var accessControlManager: AccessControlManager

    @Before
    fun setup() {
        whitelistRepository = mockk()
        accessControlManager = AccessControlManager(whitelistRepository)
    }

    // ========== isUrlAllowed - whitelist tests ==========

    @Test
    fun isUrlAllowed_urlInWhitelist_returnsTrue() = runTest {
        val rules = listOf(
            WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN)
        )
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns rules

        assertTrue(accessControlManager.isUrlAllowed("https://example.com/page"))
    }

    @Test
    fun isUrlAllowed_urlNotInWhitelist_returnsFalse() = runTest {
        val rules = listOf(
            WhitelistRule(pattern = "example.com", type = RuleType.DOMAIN)
        )
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns rules

        assertFalse(accessControlManager.isUrlAllowed("https://blocked.com"))
    }

    @Test
    fun isUrlAllowed_emptyWhitelist_returnsFalse() = runTest {
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns emptyList()

        assertFalse(accessControlManager.isUrlAllowed("https://example.com"))
    }

    // ========== unlockUrl tests ==========

    @Test
    fun unlockUrl_thenAllowed() = runTest {
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns emptyList()

        assertFalse(accessControlManager.isUrlAllowed("https://blocked.com"))

        accessControlManager.unlockUrl("https://blocked.com")

        assertTrue(accessControlManager.isUrlAllowed("https://blocked.com"))
    }

    @Test
    fun unlockUrl_unlocksEntireDomain() = runTest {
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns emptyList()

        accessControlManager.unlockUrl("https://site-a.com/page1")

        // 域名级解锁：同域名的所有页面都允许
        assertTrue(accessControlManager.isUrlAllowed("https://site-a.com/page1"))
        assertTrue(accessControlManager.isUrlAllowed("https://site-a.com/page2"))
        // 不同域名仍然不允许
        assertFalse(accessControlManager.isUrlAllowed("https://site-b.com"))
    }

    @Test
    fun unlockUrl_multipleUrls() = runTest {
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns emptyList()

        accessControlManager.unlockUrl("https://site-a.com")
        accessControlManager.unlockUrl("https://site-b.com")

        assertTrue(accessControlManager.isUrlAllowed("https://site-a.com"))
        assertTrue(accessControlManager.isUrlAllowed("https://site-b.com"))
        assertFalse(accessControlManager.isUrlAllowed("https://site-c.com"))
    }

    @Test
    fun unlockUrl_normalizedMatch() = runTest {
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns emptyList()

        accessControlManager.unlockUrl("https://example.com/page/")

        val normalized = UrlMatcher.normalizeUrl("https://example.com/page")
        val normalizedWithSlash = UrlMatcher.normalizeUrl("https://example.com/page/")
        assertTrue(normalized == normalizedWithSlash)
    }

    // ========== clearUnlockedUrls tests ==========

    @Test
    fun clearUnlockedUrls_removesAllUnlocked() = runTest {
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns emptyList()

        accessControlManager.unlockUrl("https://site-a.com")
        accessControlManager.unlockUrl("https://site-b.com")

        assertTrue(accessControlManager.isUrlAllowed("https://site-a.com"))

        accessControlManager.clearUnlockedUrls()

        assertFalse(accessControlManager.isUrlAllowed("https://site-a.com"))
        assertFalse(accessControlManager.isUrlAllowed("https://site-b.com"))
    }

    @Test
    fun clearUnlockedUrls_whitelistStillWorks() = runTest {
        val rules = listOf(
            WhitelistRule(pattern = "allowed.com", type = RuleType.DOMAIN)
        )
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns rules

        accessControlManager.unlockUrl("https://unlocked.com")
        accessControlManager.clearUnlockedUrls()

        assertTrue(accessControlManager.isUrlAllowed("https://allowed.com"))
        assertFalse(accessControlManager.isUrlAllowed("https://unlocked.com"))
    }

    // ========== timed authorization tests ==========

    @Test
    fun unlockUrl_withDuration_allowedBeforeExpiry() = runTest {
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns emptyList()

        // 30分钟授权
        accessControlManager.unlockUrl("https://timed.com", 30)

        assertTrue(accessControlManager.isUrlAllowed("https://timed.com"))
    }

    @Test
    fun getActiveAuthorizations_returnsTimedOnly() = runTest {
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns emptyList()

        // 永久授权
        accessControlManager.unlockUrl("https://permanent.com", 0)
        // 限时授权
        accessControlManager.unlockUrl("https://timed.com", 15)

        val auths = accessControlManager.getActiveAuthorizations()
        assertTrue(auths.size == 1)
        assertTrue(auths[0].domain == "timed.com")
        assertTrue(auths[0].remainingSeconds > 0)
    }

    @Test
    fun revokeAuthorization_removesAccess() = runTest {
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns emptyList()

        accessControlManager.unlockUrl("https://revoke-me.com", 30)
        assertTrue(accessControlManager.isUrlAllowed("https://revoke-me.com"))

        accessControlManager.revokeAuthorization("revoke-me.com")
        assertFalse(accessControlManager.isUrlAllowed("https://revoke-me.com"))
    }

    @Test
    fun getActiveAuthorizations_emptyWhenNone() {
        val auths = accessControlManager.getActiveAuthorizations()
        assertTrue(auths.isEmpty())
    }

    // ========== combined whitelist + unlock tests ==========

    @Test
    fun isUrlAllowed_whitelistAndUnlocked_bothWork() = runTest {
        val rules = listOf(
            WhitelistRule(pattern = "whitelisted.com", type = RuleType.DOMAIN)
        )
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns rules

        accessControlManager.unlockUrl("https://unlocked.com")

        assertTrue(accessControlManager.isUrlAllowed("https://whitelisted.com"))
        assertTrue(accessControlManager.isUrlAllowed("https://unlocked.com"))
        assertFalse(accessControlManager.isUrlAllowed("https://blocked.com"))
    }

    @Test
    fun unlockUrl_duplicateUnlock_noError() = runTest {
        coEvery { whitelistRepository.getAllRulesSnapshot() } returns emptyList()

        accessControlManager.unlockUrl("https://example.com")
        accessControlManager.unlockUrl("https://example.com")

        assertTrue(accessControlManager.isUrlAllowed("https://example.com"))
    }
}
