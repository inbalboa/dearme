package com.github.inbalboa.dearme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.inbalboa.dearme.mvi.EmailIntent
import com.github.inbalboa.dearme.mvi.EmailResult
import com.github.inbalboa.dearme.mvi.EmailState
import com.github.inbalboa.dearme.repository.EmailRepository
import com.github.inbalboa.dearme.repository.PreferencesRepository
import com.github.inbalboa.dearme.util.EmailProviderConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmailViewModel(
    private val emailRepository: EmailRepository = EmailRepository(),
    private val preferencesRepository: PreferencesRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(EmailState())
    val state: StateFlow<EmailState> = _state.asStateFlow()

    fun handleIntent(intent: EmailIntent) {
        when (intent) {
            is EmailIntent.LoadSettings -> loadSettings()
            is EmailIntent.UpdateEmail -> updateEmail(intent.email)
            is EmailIntent.UpdatePassword -> updatePassword(intent.password)
            is EmailIntent.UpdateSmtpServer -> updateSmtpServer(intent.server)
            is EmailIntent.UpdateSmtpPort -> updateSmtpPort(intent.port)
            is EmailIntent.UpdateSubject -> updateSubject(intent.subject)
            is EmailIntent.UpdateHeader -> updateHeader(intent.header)
            is EmailIntent.UpdateFooter -> updateFooter(intent.footer)
            is EmailIntent.TestEmail -> testEmail()
            is EmailIntent.ClearResult -> clearResult()
            is EmailIntent.AcceptSmtpAutofill -> acceptSmtpAutofill()
            is EmailIntent.DismissSmtpAutofill -> dismissSmtpAutofill()
            is EmailIntent.ShowAboutDialog -> showAboutDialog()
            is EmailIntent.DismissAboutDialog -> dismissAboutDialog()
        }
    }

    private fun loadSettings() {
        preferencesRepository?.let { prefs ->
            _state.value = _state.value.copy(
                email = prefs.getEmail(),
                password = prefs.getPassword(),
                smtpServer = prefs.getSmtpServer(),
                smtpPort = prefs.getSmtpPort(),
                subject = prefs.getSubject(),
                header = prefs.getHeader(),
                footer = prefs.getFooter()
            )
        } ?: run {
            // Fallback to default values if no preferences repository
            val defaultConfig = EmailProviderConfig.getDefaultConfig()
            _state.value = _state.value.copy(
                smtpServer = defaultConfig.server,
                smtpPort = defaultConfig.port
            )
        }
    }

    private fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email)
        preferencesRepository?.saveEmail(email)

        // Check if we should suggest SMTP settings based on the email domain
        if (email.isNotBlank() && EmailProviderConfig.isProviderSupported(email)) {
            val smtpConfig = EmailProviderConfig.getSmtpConfig(email)
            val currentState = _state.value

            // Only suggest if current SMTP settings are different from the suggested ones
            if (smtpConfig != null && (currentState.smtpServer != smtpConfig.server || currentState.smtpPort != smtpConfig.port)) {
                _state.value = currentState.copy(
                    smtpAutofillSuggestion = smtpConfig,
                    showSmtpAutofillDialog = true
                )
            }
        }
    }

    private fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password)
        preferencesRepository?.savePassword(password)
    }

    private fun updateSmtpServer(server: String) {
        _state.value = _state.value.copy(smtpServer = server)
        preferencesRepository?.saveSmtpServer(server)
    }

    private fun updateSmtpPort(port: String) {
        _state.value = _state.value.copy(smtpPort = port)
        preferencesRepository?.saveSmtpPort(port)
    }

    private fun updateSubject(subject: String) {
        _state.value = _state.value.copy(subject = subject)
        preferencesRepository?.saveSubject(subject)
    }

    private fun updateHeader(header: String) {
        _state.value = _state.value.copy(header = header)
        preferencesRepository?.saveHeader(header)
    }

    private fun updateFooter(footer: String) {
        _state.value = _state.value.copy(footer = footer)
        preferencesRepository?.saveFooter(footer)
    }

    private fun testEmail() {
        val currentState = _state.value

        if (!isValidEmail(currentState.email)) {
            _state.value = currentState.copy(
                result = EmailResult.Error("Please enter a valid email address")
            )
            return
        }

        if (currentState.password.isBlank()) {
            _state.value = currentState.copy(
                result = EmailResult.Error("Please enter your email password")
            )
            return
        }

        _state.value = currentState.copy(isLoading = true, result = null)

        viewModelScope.launch {
            val result = emailRepository.sendEmail(
                email = currentState.email,
                password = currentState.password,
                smtpServer = currentState.smtpServer,
                smtpPort = currentState.smtpPort,
                subject = currentState.subject.ifBlank { "DearMe: Test Message" },
                body = "It works!",
                header = currentState.header,
                footer = currentState.footer
            )

            _state.value = _state.value.copy(
                isLoading = false,
                result = if (result.isSuccess) {
                    EmailResult.Success
                } else {
                    EmailResult.Error(
                        result.exceptionOrNull()?.message ?: "Failed to send email"
                    )
                }
            )
        }
    }

    private fun clearResult() {
        _state.value = _state.value.copy(result = null)
    }

    private fun acceptSmtpAutofill() {
        val currentState = _state.value
        currentState.smtpAutofillSuggestion?.let { config ->
            _state.value = currentState.copy(
                smtpServer = config.server,
                smtpPort = config.port,
                smtpAutofillSuggestion = null,
                showSmtpAutofillDialog = false
            )

            // Save the updated SMTP settings
            preferencesRepository?.saveSmtpServer(config.server)
            preferencesRepository?.saveSmtpPort(config.port)
        }
    }

    private fun dismissSmtpAutofill() {
        _state.value = _state.value.copy(
            smtpAutofillSuggestion = null,
            showSmtpAutofillDialog = false
        )
    }
    
    private fun showAboutDialog() {
        _state.value = _state.value.copy(showAboutDialog = true)
    }
    
    private fun dismissAboutDialog() {
        _state.value = _state.value.copy(showAboutDialog = false)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
