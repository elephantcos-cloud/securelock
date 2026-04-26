package com.secure.applock.ui.screen

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.secure.applock.service.AdminReceiver
import com.secure.applock.ui.viewmodel.MainViewModel
import android.provider.Settings
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SetupScreen(viewModel: MainViewModel, onDone: () -> Unit) {
    val context = LocalContext.current
    var step    by remember { mutableIntStateOf(0) }
    var pin1    by remember { mutableStateOf("") }
    var pin2    by remember { mutableStateOf("") }
    var error   by remember { mutableStateOf("") }

    val adminLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { step = 3 }

    val scheme = MaterialTheme.colorScheme

    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = step, label = "setup") { s ->
            when (s) {
                // ── Welcome ────────────────────────────────────────────────
                0 -> StepCard(
                    icon    = Icons.Filled.Security,
                    title   = "Welcome to SecureLock",
                    body    = "Protect your apps with a secure PIN. Let's get you set up in 3 easy steps.",
                    btnText = "Get Started",
                    onNext  = { step = 1 }
                )

                // ── Set PIN ────────────────────────────────────────────────
                1 -> Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text("Set Your PIN", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                    Text("Choose a 6-digit PIN to secure your apps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface.copy(0.6f), textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    PinDots(pin1)
                    if (error.isNotEmpty())
                        Text(error, color = scheme.error, style = MaterialTheme.typography.labelLarge)
                    PinPad(pin1, onKey = { if (pin1.length < 6) pin1 += it }, onDelete = { if (pin1.isNotEmpty()) pin1 = pin1.dropLast(1) })
                    FilledTonalButton(onClick = {
                        if (pin1.length < 4) error = "PIN must be at least 4 digits"
                        else { error = ""; step = 2 }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Continue") }
                }

                // ── Confirm PIN ────────────────────────────────────────────
                2 -> Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text("Confirm Your PIN", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                    PinDots(pin2)
                    if (error.isNotEmpty())
                        Text(error, color = scheme.error, style = MaterialTheme.typography.labelLarge)
                    PinPad(pin2, onKey = { if (pin2.length < 6) pin2 += it }, onDelete = { if (pin2.isNotEmpty()) pin2 = pin2.dropLast(1) })
                    FilledTonalButton(onClick = {
                        if (pin2 != pin1) { error = "PINs don't match. Try again."; pin2 = "" }
                        else {
                            viewModel.setupPassword(pin1)
                            step = 3
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Confirm") }
                    TextButton(onClick = { pin1 = ""; pin2 = ""; step = 1 }) { Text("← Back") }
                }

                // ── Enable Accessibility ───────────────────────────────────
                3 -> StepCard(
                    icon    = Icons.Filled.Accessibility,
                    title   = "Enable Accessibility",
                    body    = "SecureLock needs Accessibility Service to detect when locked apps are opened.\n\nSettings → Accessibility → SecureLock Service → Enable",
                    btnText = "Open Accessibility Settings",
                    onNext  = {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                        step = 4
                    }
                )

                // ── Enable Device Admin ────────────────────────────────────
                4 -> StepCard(
                    icon    = Icons.Filled.AdminPanelSettings,
                    title   = "Uninstall Protection",
                    body    = "Activate Device Admin so SecureLock cannot be uninstalled without your password.",
                    btnText = "Activate Device Admin",
                    onNext  = {
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                ComponentName(context, AdminReceiver::class.java))
                            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                "Required to prevent unauthorized uninstallation.")
                        }
                        adminLauncher.launch(intent)
                    },
                    skipText = "Skip",
                    onSkip  = { finishSetup(viewModel, onDone) }
                )

                // ── All Done ───────────────────────────────────────────────
                5 -> StepCard(
                    icon    = Icons.Filled.CheckCircle,
                    title   = "All Set!",
                    body    = "SecureLock is ready. Select apps to lock from the home screen.",
                    btnText = "Go to Home",
                    onNext  = { finishSetup(viewModel, onDone) }
                )
            }
        }
    }
}

private fun finishSetup(viewModel: MainViewModel, onDone: () -> Unit) {
    viewModel.completeSetup()
    onDone()
}

@Composable
private fun StepCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String, body: String, btnText: String,
    onNext: () -> Unit, skipText: String? = null, onSkip: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()) {

        Icon(icon, contentDescription = null, tint = scheme.primary,
            modifier = Modifier.size(80.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(body, style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurface.copy(0.7f), textAlign = TextAlign.Center,
            lineHeight = 22.sp)
        Spacer(Modifier.height(8.dp))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(btnText, style = MaterialTheme.typography.labelLarge)
        }
        if (skipText != null && onSkip != null) {
            TextButton(onClick = onSkip) { Text(skipText, color = scheme.onSurface.copy(0.5f)) }
        }
    }
}
