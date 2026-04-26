package com.secure.applock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import com.secure.applock.ui.screen.PinDots
import com.secure.applock.ui.screen.PinPad
import com.secure.applock.ui.theme.AppTheme
import com.secure.applock.ui.theme.SecureLockTheme
import com.secure.applock.ui.viewmodel.MainViewModel
import com.secure.applock.util.UnlockState

class LockActivity : ComponentActivity() {

    companion object { const val EXTRA_PACKAGE = "target_package" }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetPackage = intent.getStringExtra(EXTRA_PACKAGE) ?: run { finish(); return }

        setContent {
            val theme by viewModel.theme.collectAsStateWithLifecycle()
            SecureLockTheme(theme) {
                LockScreen(
                    targetPackage = targetPackage,
                    viewModel     = viewModel,
                    onUnlocked    = { UnlockState.unlock(targetPackage); finish() },
                    onDismiss     = { finish() }
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing — user must enter PIN or tap cancel
    }
}

@Composable
private fun LockScreen(
    targetPackage: String,
    viewModel: MainViewModel,
    onUnlocked: () -> Unit,
    onDismiss: () -> Unit
) {
    var pin        by remember { mutableStateOf("") }
    val wrongPwd   by viewModel.wrongPassword.collectAsStateWithLifecycle()
    val scheme     = MaterialTheme.colorScheme

    LaunchedEffect(wrongPwd) {
        if (wrongPwd) { kotlinx.coroutines.delay(600); pin = ""; viewModel.resetWrongPassword() }
    }

    Box(
        Modifier.fillMaxSize().background(scheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment  = Alignment.CenterHorizontally,
            verticalArrangement  = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(32.dp))

            Icon(Icons.Filled.Lock, contentDescription = null,
                tint = scheme.primary, modifier = Modifier.size(56.dp))

            Text("App Locked", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Enter your PIN to continue", style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurface.copy(0.6f), textAlign = TextAlign.Center)

            Spacer(Modifier.height(8.dp))
            PinDots(pin)

            AnimatedVisibility(wrongPwd, enter = fadeIn()) {
                Text("Wrong PIN", color = scheme.error, style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(8.dp))

            PinPad(
                pin      = pin,
                onKey    = { digit ->
                    if (pin.length < 6) {
                        pin += digit
                        if (pin.length >= 4) {
                            viewModel.verifyPassword(pin) { ok -> if (ok) onUnlocked() }
                        }
                    }
                },
                onDelete = { if (pin.isNotEmpty()) pin = pin.dropLast(1) }
            )

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = scheme.onSurface.copy(0.4f))
            }
        }
    }
}
