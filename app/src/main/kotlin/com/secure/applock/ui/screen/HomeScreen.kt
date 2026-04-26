package com.secure.applock.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.secure.applock.ui.viewmodel.InstalledAppInfo
import com.secure.applock.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToApps: () -> Unit) {
    val context     = LocalContext.current
    val lockedApps  by viewModel.lockedApps.collectAsStateWithLifecycle()
    val lockEnabled by viewModel.lockEnabled.collectAsStateWithLifecycle()
    val scheme      = MaterialTheme.colorScheme

    LaunchedEffect(Unit) { viewModel.loadInstalledApps(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SecureLock", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.background),
                actions = {
                    IconButton(onClick = onNavigateToApps) {
                        Icon(Icons.Filled.Add, contentDescription = "Add App", tint = scheme.primary)
                    }
                }
            )
        },
        containerColor = scheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {

            // ── Master Lock Toggle ────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = scheme.surfaceVariant)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            if (lockEnabled) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = null, tint = scheme.primary, modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text("App Lock", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(if (lockEnabled) "Active" else "Disabled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (lockEnabled) scheme.primary else scheme.error)
                        }
                    }
                    Switch(
                        checked  = lockEnabled,
                        onCheckedChange = viewModel::setLockEnabled,
                        colors   = SwitchDefaults.colors(checkedThumbColor = scheme.primary)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Stats row ─────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(modifier = Modifier.weight(1f), label = "Locked Apps", value = lockedApps.size.toString(), icon = Icons.Filled.Lock)
                StatCard(modifier = Modifier.weight(1f), label = "Status", value = if (lockEnabled) "ON" else "OFF", icon = Icons.Filled.Shield)
            }

            Spacer(Modifier.height(16.dp))

            if (lockedApps.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.LockOpen, contentDescription = null,
                            modifier = Modifier.size(64.dp), tint = scheme.outline)
                        Text("No locked apps", style = MaterialTheme.typography.titleMedium,
                            color = scheme.onSurface.copy(0.4f))
                        Text("Tap + to add apps", style = MaterialTheme.typography.bodyMedium,
                            color = scheme.outline)
                        FilledTonalButton(onClick = onNavigateToApps) {
                            Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add Apps")
                        }
                    }
                }
            } else {
                Text("Locked Apps", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 4.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(lockedApps, key = { it.packageName }) { app ->
                        LockedAppRow(
                            label     = app.appLabel,
                            onRemove  = {
                                viewModel.toggleAppLock(
                                    InstalledAppInfo(app.packageName, app.appLabel, true)
                                )
                            }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val scheme = MaterialTheme.colorScheme
    Card(modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = scheme.primary, modifier = Modifier.size(22.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = scheme.onSurface.copy(0.6f))
        }
    }
}

@Composable
private fun LockedAppRow(label: String, onRemove: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.Lock, null, tint = scheme.primary, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.RemoveCircleOutline, "Remove", tint = scheme.error)
        }
    }
}
