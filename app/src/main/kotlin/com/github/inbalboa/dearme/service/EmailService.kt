package com.github.inbalboa.dearme.service

import android.content.Context
import android.content.Intent
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.github.inbalboa.dearme.repository.EmailRepository
import com.github.inbalboa.dearme.repository.PreferencesRepository
import com.github.inbalboa.dearme.util.UrlTitleFetcher
import kotlinx.coroutines.launch

class EmailService : LifecycleService() {

    companion object {
        private const val EXTRA_SUBJECT = "extra_subject"
        private const val EXTRA_TEXT = "extra_text"
        private const val EXTRA_EXTRA_TEXT = "extra_extra_text"

        fun start(
            context: Context,
            subject: String?,
            text: String?,
            extraText: String?
        ) {
            val intent = Intent(context, EmailService::class.java).apply {
                putExtra(EXTRA_SUBJECT, subject)
                putExtra(EXTRA_TEXT, text)
                putExtra(EXTRA_EXTRA_TEXT, extraText)
            }
            context.startService(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        intent?.let { handleEmailSending(it) }

        return START_NOT_STICKY
    }

    private fun handleEmailSending(intent: Intent) {
        val subject = intent.getStringExtra(EXTRA_SUBJECT)
        val text = intent.getStringExtra(EXTRA_TEXT)
        val extraText = intent.getStringExtra(EXTRA_EXTRA_TEXT)

        val preferencesRepository = PreferencesRepository.getInstance(this)
        val emailRepository = EmailRepository()

        // Load saved preferences
        val email = preferencesRepository.getEmail()
        val senderName = preferencesRepository.getSenderName()
        val password = preferencesRepository.getPassword()
        val smtpServer = preferencesRepository.getSmtpServer()
        val smtpPort = preferencesRepository.getSmtpPort()
        val savedSubject = preferencesRepository.getSubject()
        val header = preferencesRepository.getHeader()
        val footer = preferencesRepository.getFooter()

        // Validate settings
        if (!isValidEmail(email) || password.isBlank() || smtpServer.isBlank() || smtpPort.isBlank()) {
            Toast.makeText(this, "Email not configured. Please set up your email in the app first.", Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }

        // Combine shared text content
        val sharedText = buildString {
            text?.let { append(it) }
            extraText?.let {
                if (isNotEmpty() && it.isNotBlank()) append("\n\n")
                append(it)
            }
        }

        if (sharedText.isBlank()) {
            Toast.makeText(this, "No content to share", Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }

        // Show sending toast
        Toast.makeText(this, "Self-emailing...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            val emailSubject = subject?.takeIf { it.isNotBlank() }
                ?: savedSubject.ifBlank {
                    if (UrlTitleFetcher.isUrl(sharedText.trim())) {
                        try {
                            val fetchedTitle = UrlTitleFetcher.fetchTitle(sharedText.trim())
                            return@ifBlank fetchedTitle ?: "DearMe"
                        } catch (_: Exception) {}
                    }
                    "DearMe"
                }

            try {
                emailRepository.sendEmail(
                    email = email,
                    password = password,
                    smtpServer = smtpServer,
                    smtpPort = smtpPort,
                    subject = emailSubject,
                    body = sharedText,
                    header = header,
                    footer = footer,
                    senderName = senderName
                )
                // TODO: somehow show the result to the user
            } catch (_: Exception) {
                // TODO: log error or something
            } finally {
                stopSelf()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}
