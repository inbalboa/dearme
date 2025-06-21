package com.github.inbalboa.dearme.mvi

sealed class EmailIntent {
    data object LoadSettings : EmailIntent()
    data class UpdateEmail(val email: String) : EmailIntent()
    data class UpdatePassword(val password: String) : EmailIntent()
    data class UpdateSmtpServer(val server: String) : EmailIntent()
    data class UpdateSmtpPort(val port: String) : EmailIntent()
    data class UpdateSubject(val subject: String) : EmailIntent()
    data class UpdateHeader(val header: String) : EmailIntent()
    data class UpdateFooter(val footer: String) : EmailIntent()
    data object TestEmail : EmailIntent()
    data object ClearResult : EmailIntent()
    data object AcceptSmtpAutofill : EmailIntent()
    data object DismissSmtpAutofill : EmailIntent()
    data object ShowAboutDialog : EmailIntent()
    data object DismissAboutDialog : EmailIntent()
}
