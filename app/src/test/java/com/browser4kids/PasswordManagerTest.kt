package com.browser4kids

import com.browser4kids.util.PasswordManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordManagerTest {

    @Test
    fun hashPassword_sameInput_sameOutput() {
        val hash1 = PasswordManager.hashPassword("abc123")
        val hash2 = PasswordManager.hashPassword("abc123")
        assertEquals(hash1, hash2)
    }

    @Test
    fun hashPassword_differentInput_differentOutput() {
        val hash1 = PasswordManager.hashPassword("abc123")
        val hash2 = PasswordManager.hashPassword("abc124")
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun hashPassword_returnsHexString() {
        val hash = PasswordManager.hashPassword("test")
        assertTrue(hash.matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun validatePasswordStrength_validPassword() {
        val result = PasswordManager.validatePasswordStrength("abc123")
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun validatePasswordStrength_tooShort() {
        val result = PasswordManager.validatePasswordStrength("ab1")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("6") })
    }

    @Test
    fun validatePasswordStrength_noDigits() {
        val result = PasswordManager.validatePasswordStrength("abcdef")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("数字") })
    }

    @Test
    fun validatePasswordStrength_noLetters() {
        val result = PasswordManager.validatePasswordStrength("123456")
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("字母") })
    }

    @Test
    fun validatePasswordStrength_multipleErrors() {
        val result = PasswordManager.validatePasswordStrength("12")
        assertFalse(result.isValid)
        assertTrue(result.errors.size >= 2)
    }
}
