package com.github.inbalboa.dearme.util

data class SmtpConfig(
    val server: String,
    val port: String,
    val providerName: String
)

object EmailProviderConfig {
    private val providerConfigs = mapOf(
        // Gmail
        "gmail.com" to SmtpConfig("smtp.gmail.com", "587", "Gmail"),
        "googlemail.com" to SmtpConfig("smtp.gmail.com", "587", "Gmail"),

        // Outlook/Hotmail/Live
        "outlook.com" to SmtpConfig("smtp-mail.outlook.com", "587", "Outlook"),
        "hotmail.com" to SmtpConfig("smtp-mail.outlook.com", "587", "Hotmail"),
        "live.com" to SmtpConfig("smtp-mail.outlook.com", "587", "Live"),
        "msn.com" to SmtpConfig("smtp-mail.outlook.com", "587", "MSN"),

        // Yahoo
        "yahoo.com" to SmtpConfig("smtp.mail.yahoo.com", "587", "Yahoo"),
        "yahoo.co.uk" to SmtpConfig("smtp.mail.yahoo.com", "587", "Yahoo UK"),
        "yahoo.ca" to SmtpConfig("smtp.mail.yahoo.com", "587", "Yahoo Canada"),
        "yahoo.fr" to SmtpConfig("smtp.mail.yahoo.com", "587", "Yahoo France"),
        "yahoo.de" to SmtpConfig("smtp.mail.yahoo.com", "587", "Yahoo Germany"),
        "yahoo.it" to SmtpConfig("smtp.mail.yahoo.com", "587", "Yahoo Italy"),
        "yahoo.es" to SmtpConfig("smtp.mail.yahoo.com", "587", "Yahoo Spain"),
        "yahoo.com.au" to SmtpConfig("smtp.mail.yahoo.com", "587", "Yahoo Australia"),
        "yahoo.com.br" to SmtpConfig("smtp.mail.yahoo.com", "587", "Yahoo Brazil"),
        "ymail.com" to SmtpConfig("smtp.mail.yahoo.com", "587", "Yahoo Mail"),
        "rocketmail.com" to SmtpConfig("smtp.mail.yahoo.com", "587", "Yahoo Rocketmail"),

        // Apple iCloud
        "icloud.com" to SmtpConfig("smtp.mail.me.com", "587", "iCloud"),
        "me.com" to SmtpConfig("smtp.mail.me.com", "587", "iCloud"),
        "mac.com" to SmtpConfig("smtp.mail.me.com", "587", "iCloud"),

        // AOL
        "aol.com" to SmtpConfig("smtp.aol.com", "587", "AOL"),

        // Yandex
        "yandex.com" to SmtpConfig("smtp.yandex.com", "587", "Yandex"),
        "yandex.ru" to SmtpConfig("smtp.yandex.ru", "587", "Yandex Russia"),

        // Zoho
        "zoho.com" to SmtpConfig("smtp.zoho.com", "587", "Zoho"),
        "zohomail.com" to SmtpConfig("smtp.zoho.com", "587", "Zoho Mail"),

        // ProtonMail
        "protonmail.com" to SmtpConfig("127.0.0.1", "1025", "ProtonMail"),
        "proton.me" to SmtpConfig("127.0.0.1", "1025", "Proton"),

        // FastMail
        "fastmail.com" to SmtpConfig("smtp.fastmail.com", "587", "FastMail"),
        "fastmail.fm" to SmtpConfig("smtp.fastmail.com", "587", "FastMail"),

        // Mail.com
        "mail.com" to SmtpConfig("smtp.mail.com", "587", "Mail.com")
    )

    /**
     * Gets SMTP configuration for a given email address
     * @param email The email address to check
     * @return SmtpConfig if provider is supported, null otherwise
     */
    fun getSmtpConfig(email: String): SmtpConfig? {
        val domain = extractDomain(email)
        return providerConfigs[domain?.lowercase()]
    }

    fun getDefaultConfig(): SmtpConfig {
        return providerConfigs["gmail.com"]!!
    }

    /**
     * Checks if the email domain is supported for autofill
     * @param email The email address to check
     * @return true if supported, false otherwise
     */
    fun isProviderSupported(email: String): Boolean {
        val domain = extractDomain(email)
        return providerConfigs.containsKey(domain?.lowercase())
    }

    /**
     * Extracts domain from email address
     * @param email The email address
     * @return domain part of the email or null if invalid
     */
    private fun extractDomain(email: String): String? {
        if (!isValidEmailFormat(email)) return null

        val atIndex = email.lastIndexOf('@')
        return if (atIndex > 0 && atIndex < email.length - 1) {
            email.substring(atIndex + 1)
        } else {
            null
        }
    }

    /**
     * Basic email format validation
     * @param email The email address to validate
     * @return true if format is valid, false otherwise
     */
    private fun isValidEmailFormat(email: String): Boolean {
        return email.contains('@') &&
               email.indexOf('@') > 0 &&
               email.lastIndexOf('@') < email.length - 1 &&
               email.lastIndexOf('@') == email.indexOf('@') // Only one @ symbol
    }
}
