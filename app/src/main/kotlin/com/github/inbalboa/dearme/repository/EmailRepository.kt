package com.github.inbalboa.dearme.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailRepository {

    companion object {
        fun userFriendlyError(throwable: Throwable?): String {
            if (throwable == null) return "Failed to send email"
            val message = throwable.message?.lowercase() ?: ""
            return when {
                throwable is AuthenticationFailedException ||
                    message.contains("authentication failed") ||
                    message.contains("invalid credentials") ||
                    message.contains("username and password not accepted") ->
                    "Authentication failed. Check your email and password."

                throwable is java.net.UnknownHostException ||
                    message.contains("unknown host") ->
                    "Server not found. Check the SMTP server address."

                throwable is java.net.ConnectException ||
                    message.contains("connection refused") ->
                    "Could not connect to server. Check the server address and port."

                throwable is java.net.SocketTimeoutException ||
                    message.contains("timed out") ||
                    message.contains("timeout") ->
                    "Connection timed out. Check your internet connection."

                message.contains("ssl") || message.contains("tls") ||
                    message.contains("certificate") ->
                    "Secure connection failed. The server may not support the current security settings."

                else -> throwable.message ?: "Failed to send email"
            }
        }
    }

    suspend fun sendEmail(
        email: String,
        password: String,
        smtpServer: String,
        smtpPort: String,
        subject: String,
        body: String,
        header: String,
        footer: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", smtpServer)
                put("mail.smtp.port", smtpPort)
                put("mail.smtp.ssl.trust", smtpServer)
            }

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(email, password)
                }
            })

            // Compose the message body from header and footer
            val messageBody = buildString {
                if (header.isNotBlank()) {
                    append(header)
                    append("\n\n")
                }
                append(body)
                if (footer.isNotBlank()) {
                    append("\n\n")
                    append(footer)
                }
            }

            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(email))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
                this.subject = subject
                setText(messageBody)
            }

            Transport.send(message)
            Result.success(Unit)
        } catch (e: MessagingException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
