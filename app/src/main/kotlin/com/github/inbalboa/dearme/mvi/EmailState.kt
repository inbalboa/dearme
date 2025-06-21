package com.github.inbalboa.dearme.mvi

import com.github.inbalboa.dearme.util.SmtpConfig

data class EmailState(
    val email: String = "",
    val password: String = "",
    val smtpServer: String = "smtp.gmail.com",
    val smtpPort: String = "587",
    val subject: String = "",
    val header: String = "",
    val footer: String = "",
    val isLoading: Boolean = false,
    val result: EmailResult? = null,
    val smtpAutofillSuggestion: SmtpConfig? = null,
    val showSmtpAutofillDialog: Boolean = false,
    val showAboutDialog: Boolean = false
)

sealed class EmailResult {
    data object Success : EmailResult()
    data class Error(val message: String) : EmailResult()
} 
