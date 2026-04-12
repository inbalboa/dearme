package com.github.inbalboa.dearme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.github.inbalboa.dearme.repository.EmailRepository
import com.github.inbalboa.dearme.repository.PreferencesRepository
import com.github.inbalboa.dearme.util.UrlTitleFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShareActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleShareIntent()
    }

    private fun handleShareIntent() {
        val (subject, text, extraText) = when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    Triple(
                        intent.getStringExtra(Intent.EXTRA_SUBJECT),
                        intent.getStringExtra(Intent.EXTRA_TEXT),
                        intent.getStringExtra(Intent.EXTRA_HTML_TEXT)
                            ?: intent.getStringExtra(Intent.EXTRA_EMAIL)
                    )
                } else {
                    finish()
                    return
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                if (intent.type == "text/plain") {
                    Triple(
                        intent.getStringExtra(Intent.EXTRA_SUBJECT),
                        intent.getStringArrayListExtra(Intent.EXTRA_TEXT)?.joinToString("\n\n"),
                        null
                    )
                } else {
                    finish()
                    return
                }
            }
            else -> {
                finish()
                return
            }
        }

        sendEmail(subject, text, extraText)
    }

    private fun sendEmail(subject: String?, text: String?, extraText: String?) {
        val preferencesRepository = PreferencesRepository.getInstance(this)
        val emailRepository = EmailRepository()

        val email = preferencesRepository.getEmail()
        val password = preferencesRepository.getPassword()
        val smtpServer = preferencesRepository.getSmtpServer()
        val smtpPort = preferencesRepository.getSmtpPort()
        val savedSubject = preferencesRepository.getSubject()
        val header = preferencesRepository.getHeader()
        val footer = preferencesRepository.getFooter()

        if (!isValidEmail(email) || password.isBlank() || smtpServer.isBlank() || smtpPort.isBlank()) {
            Toast.makeText(this, "Email not configured. Please set up your email in the app first.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val sharedText = buildString {
            text?.let { append(it) }
            extraText?.let {
                if (isNotEmpty() && it.isNotBlank()) append("\n\n")
                append(it)
            }
        }

        if (sharedText.isBlank()) {
            Toast.makeText(this, "No content to share", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val emailSubject = subject?.takeIf { it.isNotBlank() }
                ?: savedSubject.ifBlank {
                    if (UrlTitleFetcher.isUrl(sharedText.trim())) {
                        try {
                            val fetchedTitle = withContext(Dispatchers.IO) {
                                UrlTitleFetcher.fetchTitle(sharedText.trim())
                            }
                            return@ifBlank fetchedTitle ?: "DearMe"
                        } catch (_: Exception) {}
                    }
                    "DearMe"
                }

            try {
                val result = emailRepository.sendEmail(
                    email = email,
                    password = password,
                    smtpServer = smtpServer,
                    smtpPort = smtpPort,
                    subject = emailSubject,
                    body = sharedText,
                    header = header,
                    footer = footer
                )

                if (result.isSuccess) {
                    Toast.makeText(applicationContext, "Email to myself sent successfully", Toast.LENGTH_LONG).show()
                } else {
                    val errorMessage = EmailRepository.userFriendlyError(result.exceptionOrNull())
                    Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                val errorMessage = EmailRepository.userFriendlyError(e)
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_LONG).show()
            } finally {
                finish()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
