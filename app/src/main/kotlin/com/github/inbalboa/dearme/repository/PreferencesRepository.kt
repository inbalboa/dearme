package com.github.inbalboa.dearme.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.github.inbalboa.dearme.util.PasswordEncryptor

class PreferencesRepository private constructor(context: Context) {
    private val appContext = context.applicationContext

    private val preferences: SharedPreferences = appContext.getSharedPreferences(
        "dearme_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        @Volatile
        private var instance: PreferencesRepository? = null

        fun getInstance(context: Context): PreferencesRepository =
            instance ?: synchronized(this) {
                instance ?: PreferencesRepository(context).also { instance = it }
            }

        private const val KEY_EMAIL = "email"
        private const val KEY_SENDER_NAME = "sender_name"
        private const val KEY_PASSWORD = "password"
        private const val KEY_SMTP_SERVER = "smtp_server"
        private const val KEY_SMTP_PORT = "smtp_port"
        private const val KEY_SUBJECT = "subject"
        private const val KEY_HEADER = "header"
        private const val KEY_FOOTER = "footer"
    }

    // Save methods
    fun saveEmail(email: String) {
        preferences.edit { putString(KEY_EMAIL, email) }
    }

    fun saveSenderName(senderName: String) {
        preferences.edit { putString(KEY_SENDER_NAME, senderName) }
    }

    fun savePassword(password: String) {
        val encrypted = if (password.isNotEmpty()) PasswordEncryptor.encrypt(password) else ""
        preferences.edit { putString(KEY_PASSWORD, encrypted) }
    }

    fun saveSmtpServer(server: String) {
        preferences.edit { putString(KEY_SMTP_SERVER, server)}
    }

    fun saveSmtpPort(port: String) {
        preferences.edit { putString(KEY_SMTP_PORT, port)}
    }

    fun saveSubject(subject: String) {
        preferences.edit { putString(KEY_SUBJECT, subject)}
    }

    fun saveHeader(header: String) {
        preferences.edit { putString(KEY_HEADER, header) }
    }

    fun saveFooter(footer: String) {
        preferences.edit { putString(KEY_FOOTER, footer) }
    }

    // Load methods
    fun getEmail(): String = preferences.getString(KEY_EMAIL, null) ?: ""

    fun getSenderName(): String = preferences.getString(KEY_SENDER_NAME, null) ?: ""

    fun getPassword(): String {
        val encrypted = preferences.getString(KEY_PASSWORD, null)
        if (encrypted.isNullOrEmpty()) return ""
        return try {
            PasswordEncryptor.decrypt(encrypted)
        } catch (_: Exception) {
            ""
        }
    }

    fun getSmtpServer(): String = preferences.getString(KEY_SMTP_SERVER, null) ?: "smtp.gmail.com"

    fun getSmtpPort(): String = preferences.getString(KEY_SMTP_PORT, null) ?: "587"

    fun getSubject(): String = preferences.getString(KEY_SUBJECT, null) ?: ""

    fun getHeader(): String = preferences.getString(KEY_HEADER, null) ?: ""

    fun getFooter(): String = preferences.getString(KEY_FOOTER, null) ?: "Sent from DearMe"

}
