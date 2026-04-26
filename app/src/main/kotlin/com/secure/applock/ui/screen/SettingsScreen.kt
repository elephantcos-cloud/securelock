package com.secure.applock.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.secure.applock.ui.theme.AppTheme
import com.secure.applock.ui.viewmodel.MainViewModel
import com.secure.applock.util.IconChanger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context      = LocalContext.current
    val currentTheme by viewModel.theme.collectAsStateWithLifecycle()
    val activeAlias  by viewModel.activeAlias.collectAsStateWithLifecycle()
    val scheme       = MaterialTheme.colorScheme

    var showChangePwd by remember { mutableStateOf(false) }
    var showIconPick  by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.background)
            )
        },
        containerColor = scheme.background
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Theme ───────────────────────────────────────────────────────
            item {
                SectionHeader("Appearance")
                ThemeGrid(current = currentTheme, onSelect = viewModel::setTheme)
            }

            // ── Icon & Name ─────────────────────────────────────────────────
            item {
                SectionHeader("App Icon & Name")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconChanger.aliases.forEach { alias ->
                        val isActive = alias.aliasShortName == activeAlias
                        Row(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isActive) scheme.primary.copy(0.15f) else scheme.surfaceVariant)
                                .border(if (isActive) 2.dp else 0.dp, if (isActive) scheme.primary else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setActiveAlias(alias.aliasShortName, context) }
                                .padding(16.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Filled.Apps, null, tint = if (isActive) scheme.primary else scheme.onSurface, modifier = Modifier.size(24.dp))
                                Column {
                                    Text(alias.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text("Icon: ${alias.iconDescription}", style = MaterialTheme.typography.labelMedium, color = scheme.onSurface.copy(0.5f))
                                }
                            }
                            if (isActive) Icon(Icons.Filled.CheckCircle, null, tint = scheme.primary)
                        }
                    }
                }
            }

            // ── Security ────────────────────────────────────────────────────
            item {
                SectionHeader("Security")
                SettingsTile(
                    icon   = Icons.Filled.Password,
                    title  = "Change PIN",
                    subtitle = "Update your unlock PIN",
                    onClick = { showChangePwd = true }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showChangePwd) {
        ChangePinDialog(viewModel = viewModel, onDismiss = { showChangePwd = false })
    }
}

@Composable
private fun ThemeGrid(current: AppTheme, onSelect: (AppTheme) -> Unit) {
    val scheme  = MaterialTheme.colorScheme
    val themeColors = mapOf(
        AppTheme.DARK    to Color(0xFF7B6CF6),
        AppTheme.AMOLED  to Color(0xFF8C7FFF),
        AppTheme.OCEAN   to Color(0xFF00B4D8),
        AppTheme.SUNSET  to Color(0xFFFF6B6B),
        AppTheme.FOREST  to Color(0xFF56C05A),
        AppTheme.LIGHT   to Color(0xFF5C35D9),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppTheme.entries.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { theme ->
                    val isSelected = theme == current
                    Column(
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(scheme.surfaceVariant)
                            .border(if (isSelected) 2.dp else 0.dp, if (isSelected) scheme.primary else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable { onSelect(theme) }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.size(32.dp).clip(CircleShape)
                            .background(themeColors[theme] ?: scheme.primary))
                        Text(theme.displayName, style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) scheme.primary else scheme.onSurface.copy(0.7f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
}

@Composable
private fun SettingsTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String, subtitle: String, onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = scheme.primary, modifier = Modifier.size(24.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = scheme.onSurface.copy(0.5f))
        }
        Icon(Icons.Filled.ChevronRight, null, tint = scheme.outline)
    }
}

@Composable
private fun ChangePinDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var oldPin  by remember { mutableStateOf("") }
    var newPin  by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var step    by remember { mutableIntStateOf(0) } // 0=old, 1=new, 2=confirm
    var error   by remember { mutableStateOf("") }
    val scheme  = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = scheme.surface,
        title = { Text(when(step) { 0 -> "Enter Current PIN"; 1 -> "New PIN"; else -> "Confirm New PIN" }, fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val pin = when(step) { 0 -> oldPin; 1 -> newPin; else -> confirm }
                PinDots(pin)
                if (error.isNotEmpty()) Text(error, color = scheme.error, style = MaterialTheme.typography.labelLarge)
                PinPad(
                    pin     = pin,
                    onKey   = { digit ->
                        when (step) { 0 -> if (oldPin.length < 6) oldPin += digit; 1 -> if (newPin.length < 6) newPin += digit; else -> if (confirm.length < 6) confirm += digit }
                        error = ""
                    },
                    onDelete = {
                        when (step) { 0 -> if (oldPin.isNotEmpty()) oldPin = oldPin.dropLast(1); 1 -> if (newPin.isNotEmpty()) newPin = newPin.dropLast(1); else -> if (confirm.isNotEmpty()) confirm = confirm.dropLast(1) }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when (step) {
                    0 -> { if (oldPin.length >= 4) step = 1 else error = "Enter current PIN" }
                    1 -> { if (newPin.length >= 4) step = 2 else error = "Min 4 digits" }
                    2 -> {
                        if (confirm != newPin) { error = "PINs don't match"; confirm = "" }
                        else viewModel.changePassword(oldPin, newPin) { ok ->
                            if (ok) onDismiss() else { error = "Wrong current PIN"; oldPin = ""; step = 0 }
                        }
                    }
                }
            }) { Text(if (step == 2) "Change" else "Next") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
