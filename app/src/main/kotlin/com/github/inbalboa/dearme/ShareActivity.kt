package com.github.inbalboa.dearme

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.github.inbalboa.dearme.service.EmailService

class ShareActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleShareIntent()
        finish() // Close immediately without showing UI
    }

    private fun handleShareIntent() {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                    val extraText = intent.getStringExtra(Intent.EXTRA_HTML_TEXT)
                        ?: intent.getStringExtra(Intent.EXTRA_EMAIL)

                    EmailService.start(
                        context = this,
                        subject = sharedSubject,
                        text = sharedText,
                        extraText = extraText
                    )
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                if (intent.type == "text/plain") {
                    val sharedTexts = intent.getStringArrayListExtra(Intent.EXTRA_TEXT)
                    val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
                    val combinedText = sharedTexts?.joinToString("\n\n")

                    EmailService.start(
                        context = this,
                        subject = sharedSubject,
                        text = combinedText,
                        extraText = null
                    )
                }
            }
        }
    }

}
