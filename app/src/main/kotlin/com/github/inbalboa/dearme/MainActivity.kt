package com.github.inbalboa.dearme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.inbalboa.dearme.repository.EmailRepository
import com.github.inbalboa.dearme.repository.PreferencesRepository
import com.github.inbalboa.dearme.ui.EmailScreen
import com.github.inbalboa.dearme.ui.theme.DearMeTheme
import com.github.inbalboa.dearme.viewmodel.EmailViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DearMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val preferencesRepository = PreferencesRepository.getInstance(this@MainActivity)
                    val viewModel: EmailViewModel = viewModel {
                        EmailViewModel(
                            emailRepository = EmailRepository(),
                            preferencesRepository = preferencesRepository
                        )
                    }

                    EmailScreen(viewModel)
                }
            }
        }
    }
}
