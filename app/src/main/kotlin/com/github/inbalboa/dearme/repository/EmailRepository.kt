package com.github.inbalboa.dearme.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailRepository {
    suspend fun sendEmail(
        email: String,
        password: String,
        smtpServer: String,
        smtpPort: String,
        subject: String,
        body: String,
        header: String,
        footer: String,
        senderName: String = ""
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

            val fromAddress = if (senderName.isNotBlank()) {
                InternetAddress(email, senderName, "UTF-8")
            } else {
                InternetAddress(email)
            }

            val message = MimeMessage(session).apply {
                setFrom(fromAddress)
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
