package com.secure.applock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.secure.applock.ui.navigation.AppNavigation
import com.secure.applock.ui.screen.PinDots
import com.secure.applock.ui.screen.PinPad
import com.secure.applock.ui.screen.SetupScreen
import com.secure.applock.ui.theme.SecureLockTheme
import com.secure.applock.ui.viewmodel.MainViewModel
import com.secure.applock.util.UnlockState

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val theme         by viewModel.theme.collectAsStateWithLifecycle()
            val isSetupDone   by viewModel.isSetupDone.collectAsStateWithLifecycle()
            val isAuth        by viewModel.isAuthenticated.collectAsStateWithLifecycle()

            SecureLockTheme(theme) {
                AnimatedContent(targetState = Triple(isSetupDone, isAuth, true), label = "root") { (setup, auth, _) ->
                    when {
                        !setup -> SetupScreen(viewModel, onDone = { viewModel.completeSetup() })
                        !auth  -> AuthGate(viewModel, onAuthenticated = {})
                        else   -> AppNavigation(viewModel)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Lock the app itself every time it goes to background
        viewModel.logout()
    }
}

@Composable
private fun AuthGate(viewModel: MainViewModel, onAuthenticated: () -> Unit) {
    var pin      by remember { mutableStateOf("") }
    val wrongPwd by viewModel.wrongPassword.collectAsStateWithLifecycle()
    val scheme   = MaterialTheme.colorScheme

    LaunchedEffect(wrongPwd) {
        if (wrongPwd) { kotlinx.coroutines.delay(500); pin = ""; viewModel.resetWrongPassword() }
    }

    Box(
        Modifier.fillMaxSize().background(scheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(40.dp))
            Icon(Icons.Filled.Lock, null, tint = scheme.primary, modifier = Modifier.size(64.dp))
            Text("SecureLock", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Enter your PIN", style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface.copy(0.6f), textAlign = TextAlign.Center)

            Spacer(Modifier.height(8.dp))
            PinDots(pin)

            if (wrongPwd)
                Text("Wrong PIN", color = scheme.error, style = MaterialTheme.typography.labelLarge)

            Spacer(Modifier.height(8.dp))
            PinPad(
                pin      = pin,
                onKey    = { digit ->
                    if (pin.length < 6) {
                        pin += digit
                        if (pin.length >= 4) {
                            viewModel.verifyPassword(pin) { /* state handles nav */ }
                        }
                    }
                },
                onDelete = { if (pin.isNotEmpty()) pin = pin.dropLast(1) }
            )
        }
    }
}
