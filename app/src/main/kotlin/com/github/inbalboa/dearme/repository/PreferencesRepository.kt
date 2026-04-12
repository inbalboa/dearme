package com.github.inbalboa.dearme.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesRepository(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        "dearme_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_EMAIL = "email"
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

    fun savePassword(password: String) {
        preferences.edit { putString(KEY_PASSWORD, password)}
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

    fun getPassword(): String = preferences.getString(KEY_PASSWORD, null) ?: ""

    fun getSmtpServer(): String = preferences.getString(KEY_SMTP_SERVER, null) ?: "smtp.gmail.com"

    fun getSmtpPort(): String = preferences.getString(KEY_SMTP_PORT, null) ?: "587"

    fun getSubject(): String = preferences.getString(KEY_SUBJECT, null) ?: ""

    fun getHeader(): String = preferences.getString(KEY_HEADER, null) ?: ""

    fun getFooter(): String = preferences.getString(KEY_FOOTER, null) ?: "Sent from DearMe"

}
