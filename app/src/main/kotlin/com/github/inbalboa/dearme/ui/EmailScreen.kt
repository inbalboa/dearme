package com.github.inbalboa.dearme.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.inbalboa.dearme.mvi.EmailIntent
import com.github.inbalboa.dearme.mvi.EmailResult
import com.github.inbalboa.dearme.viewmodel.EmailViewModel

@Composable
@Preview
fun EmailScreen(
    viewModel: EmailViewModel = viewModel { EmailViewModel() }
) {
    val state by viewModel.state.collectAsState()

    state.result?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.handleIntent(EmailIntent.ClearResult) },
            title = {
                Text(
                    text = when (result) {
                        is EmailResult.Success -> "Success"
                        is EmailResult.Error -> "Error"
                    }
                )
            },
            text = {
                Text(
                    text = when (result) {
                        is EmailResult.Success -> "Test email sent successfully! Check your inbox."
                        is EmailResult.Error -> result.message
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.handleIntent(EmailIntent.ClearResult) }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (state.showSmtpAutofillDialog && state.smtpAutofillSuggestion != null) {
        AlertDialog(
            onDismissRequest = { viewModel.handleIntent(EmailIntent.DismissSmtpAutofill) },
            title = {
                Text("${state.smtpAutofillSuggestion!!.providerName} Provider Detected")
            },
            text = {
                Column {
                    Text(
                        text = "Would you like to automatically configure the SMTP settings for ${state.smtpAutofillSuggestion!!.providerName}?"
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.handleIntent(EmailIntent.AcceptSmtpAutofill) }
                ) {
                    Text("Yes, Configure")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.handleIntent(EmailIntent.DismissSmtpAutofill) }
                ) {
                    Text("No, Thanks")
                }
            }
        )
    }

        if (state.showAboutDialog) {
        LocalContext.current

        AlertDialog(
            onDismissRequest = { viewModel.handleIntent(EmailIntent.DismissAboutDialog) },
            title = {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                    Text("About DearMe")
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "DearMe is an app to send emails to yourself by sharing from others apps.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Version: ${com.github.inbalboa.dearme.BuildConfig.VERSION_NAME}",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // GitHub link
                    val githubUrl = "https://github.com/inbalboa/dearme"
                    val githubText = buildAnnotatedString {
                        withLink(
                            LinkAnnotation.Url(
                                url = githubUrl,
                                styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                )
                            )
                        ) {
                            append(githubUrl)
                        }
                    }

                    Text(
                        text = githubText,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.handleIntent(EmailIntent.DismissAboutDialog) }
                ) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with About button
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "DearMe Configuration",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { viewModel.handleIntent(EmailIntent.ShowAboutDialog) }
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "About",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            }
        }

        // Email Configuration Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Email",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.handleIntent(EmailIntent.UpdateEmail(it)) },
                    label = { Text("Your Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    isError = state.emailError != null,
                    supportingText = state.emailError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.handleIntent(EmailIntent.UpdatePassword(it)) },
                    label = { Text("App Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = state.passwordError != null,
                    supportingText = state.passwordError?.let { { Text(it) } }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.smtpServer,
                        onValueChange = { viewModel.handleIntent(EmailIntent.UpdateSmtpServer(it)) },
                        label = { Text("SMTP Server") },
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                        isError = state.smtpServerError != null,
                        supportingText = state.smtpServerError?.let { { Text(it) } }
                    )

                    OutlinedTextField(
                        value = state.smtpPort,
                        onValueChange = { viewModel.handleIntent(EmailIntent.UpdateSmtpPort(it)) },
                        label = { Text("Port") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = state.smtpPortError != null,
                        supportingText = state.smtpPortError?.let { { Text(it) } }
                    )
                }
            }
        }

        // Message Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Message",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = state.subject,
                    onValueChange = { viewModel.handleIntent(EmailIntent.UpdateSubject(it)) },
                    label = { Text("Subject (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.header,
                    onValueChange = { viewModel.handleIntent(EmailIntent.UpdateHeader(it)) },
                    label = { Text("Header (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = state.footer,
                    onValueChange = { viewModel.handleIntent(EmailIntent.UpdateFooter(it)) },
                    label = { Text("Footer (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    maxLines = 3
                )
            }
        }

        // Send Button
        Button(
            onClick = { viewModel.handleIntent(EmailIntent.TestEmail) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !state.isLoading && state.emailError == null && state.smtpServerError == null && state.smtpPortError == null
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sending...")
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test the connection")
            }
        }
    }
}
