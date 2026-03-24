package com.browser4kids.repository

import android.content.Context
import android.content.SharedPreferences
import com.browser4kids.util.PasswordManager

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "browser4kids_settings"
        private const val KEY_PASSWORD_HASH = "password_hash"
        private const val KEY_RECOVERY_QUESTION = "recovery_question"
        private const val KEY_RECOVERY_ANSWER_HASH = "recovery_answer_hash"
        private const val KEY_HOME_URL = "home_url"
        private const val KEY_SETUP_COMPLETE = "setup_complete"
    }

    fun isSetupComplete(): Boolean = prefs.getBoolean(KEY_SETUP_COMPLETE, false)

    fun setSetupComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_SETUP_COMPLETE, complete).apply()
    }

    fun hasPassword(): Boolean = prefs.getString(KEY_PASSWORD_HASH, null) != null

    fun setPassword(password: String) {
        prefs.edit().putString(KEY_PASSWORD_HASH, PasswordManager.hashPassword(password)).apply()
    }

    fun verifyPassword(password: String): Boolean {
        val storedHash = prefs.getString(KEY_PASSWORD_HASH, null) ?: return false
        return PasswordManager.hashPassword(password) == storedHash
    }

    fun setRecoveryQuestion(question: String, answer: String) {
        prefs.edit()
            .putString(KEY_RECOVERY_QUESTION, question)
            .putString(KEY_RECOVERY_ANSWER_HASH, PasswordManager.hashPassword(answer.lowercase()))
            .apply()
    }

    fun getRecoveryQuestion(): String? = prefs.getString(KEY_RECOVERY_QUESTION, null)

    fun verifyRecoveryAnswer(answer: String): Boolean {
        val storedHash = prefs.getString(KEY_RECOVERY_ANSWER_HASH, null) ?: return false
        return PasswordManager.hashPassword(answer.lowercase()) == storedHash
    }

    fun getHomeUrl(): String = prefs.getString(KEY_HOME_URL, "") ?: ""

    fun setHomeUrl(url: String) {
        prefs.edit().putString(KEY_HOME_URL, url).apply()
    }
}
