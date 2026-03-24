package com.browser4kids

import com.browser4kids.util.PasswordManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PasswordManager 扩展测试 - 覆盖更多边界情况
 */
class PasswordManagerExtendedTest {

    // ========== hashPassword 边界测试 ==========

    @Test
    fun hashPassword_emptyString_returnsValidHash() {
        val hash = PasswordManager.hashPassword("")
        assertTrue(hash.matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun hashPassword_longPassword_returnsValidHash() {
        val longPassword = "a".repeat(1000) + "1"
        val hash = PasswordManager.hashPassword(longPassword)
        assertTrue(hash.matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun hashPassword_specialCharacters() {
        val hash = PasswordManager.hashPassword("p@\$\$w0rd!#%^&*()")
        assertTrue(hash.matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun hashPassword_unicodeChinese() {
        val hash = PasswordManager.hashPassword("密码abc123")
        assertTrue(hash.matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun hashPassword_differentInputs_allDifferent() {
        val hashes = setOf(
            PasswordManager.hashPassword("abc123"),
            PasswordManager.hashPassword("abc124"),
            PasswordManager.hashPassword("ABC123"),
            PasswordManager.hashPassword("123abc"),
            PasswordManager.hashPassword("abc1234")
        )
        assertEquals(5, hashes.size) // 所有哈希值应不同
    }

    @Test
    fun hashPassword_deterministicAcrossMultipleCalls() {
        val password = "mySecurePassword1"
        val hashes = (1..10).map { PasswordManager.hashPassword(password) }
        assertTrue(hashes.all { it == hashes[0] })
    }

    @Test
    fun hashPassword_caseSensitive() {
        val lower = PasswordManager.hashPassword("password1")
        val upper = PasswordManager.hashPassword("Password1")
        assertNotEquals(lower, upper)
    }

    @Test
    fun hashPassword_sha256_length64() {
        // SHA-256 产生 32 字节 = 64 个十六进制字符
        val hash = PasswordManager.hashPassword("test123")
        assertEquals(64, hash.length)
    }

    // ========== validatePasswordStrength 边界测试 ==========

    @Test
    fun validatePassword_exactlyMinLength_valid() {
        val result = PasswordManager.validatePasswordStrength("abc123")
        assertTrue(result.isValid)
        assertEquals(0, result.errors.size)
    }

    @Test
    fun validatePassword_fiveChars_invalid() {
        val result = PasswordManager.validatePasswordStrength("abc12")
        assertFalse(result.isValid)
    }

    @Test
    fun validatePassword_longPassword_valid() {
        val result = PasswordManager.validatePasswordStrength("abcdefghijklmnop1234567890")
        assertTrue(result.isValid)
    }

    @Test
    fun validatePassword_onlySpecialChars_invalid() {
        val result = PasswordManager.validatePasswordStrength("!@#$%^&*()")
        assertFalse(result.isValid)
        // 应有至少两个错误: 缺数字、缺字母
        assertTrue(result.errors.size >= 2)
    }

    @Test
    fun validatePassword_lettersAndSpecial_noDigits() {
        val result = PasswordManager.validatePasswordStrength("abcdef!")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("数字") })
    }

    @Test
    fun validatePassword_digitsAndSpecial_noLetters() {
        val result = PasswordManager.validatePasswordStrength("123456!")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("字母") })
    }

    @Test
    fun validatePassword_emptyString_threeErrors() {
        val result = PasswordManager.validatePasswordStrength("")
        assertFalse(result.isValid)
        // 长度不够、缺数字、缺字母 = 3 个错误
        assertEquals(3, result.errors.size)
    }

    @Test
    fun validatePassword_chineseAndDigits_valid() {
        // 中文字符算作"字母"(isLetter为true)
        val result = PasswordManager.validatePasswordStrength("密码密码12")
        assertTrue(result.isValid)
    }

    @Test
    fun validatePassword_uppercaseLettersAndDigits_valid() {
        val result = PasswordManager.validatePasswordStrength("ABC123")
        assertTrue(result.isValid)
    }

    @Test
    fun validatePassword_mixedCase_valid() {
        val result = PasswordManager.validatePasswordStrength("AbCdEf1")
        assertTrue(result.isValid)
    }

    @Test
    fun validatePassword_singleChar_allErrors() {
        val result = PasswordManager.validatePasswordStrength("a")
        assertFalse(result.isValid)
        assertTrue(result.errors.size >= 2) // 长度不够 + 缺数字
    }
}
