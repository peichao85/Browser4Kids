package com.browser4kids.util

import java.security.MessageDigest

/**
 * 密码哈希和验证工具类
 */
object PasswordManager {

    /**
     * 使用SHA-256对密码进行哈希
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * 验证密码强度: 至少6位,包含数字和字母
     */
    fun validatePasswordStrength(password: String): PasswordValidation {
        val errors = mutableListOf<String>()

        if (password.length < 6) {
            errors.add("密码长度至少6位")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("密码需要包含数字")
        }
        if (!password.any { it.isLetter() }) {
            errors.add("密码需要包含字母")
        }

        return if (errors.isEmpty()) {
            PasswordValidation(isValid = true, errors = emptyList())
        } else {
            PasswordValidation(isValid = false, errors = errors)
        }
    }
}

data class PasswordValidation(
    val isValid: Boolean,
    val errors: List<String>
)
