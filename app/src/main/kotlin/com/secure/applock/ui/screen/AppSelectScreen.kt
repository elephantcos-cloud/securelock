package com.secure.applock.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.secure.applock.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val allApps by viewModel.installedApps.collectAsStateWithLifecycle()
    var query   by remember { mutableStateOf("") }
    val scheme  = MaterialTheme.colorScheme

    val filtered = remember(allApps, query) {
        if (query.isBlank()) allApps
        else allApps.filter { it.label.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Apps to Lock", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.background)
            )
        },
        containerColor = scheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {

            // Search bar
            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                placeholder   = { Text("Search apps...") },
                leadingIcon   = { Icon(Icons.Filled.Search, null) },
                modifier      = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape         = RoundedCornerShape(14.dp),
                singleLine    = true,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = scheme.primary,
                    unfocusedBorderColor = scheme.outline.copy(0.4f),
                    focusedContainerColor   = scheme.surfaceVariant,
                    unfocusedContainerColor = scheme.surfaceVariant
                )
            )

            Spacer(Modifier.height(4.dp))
            Text("${filtered.size} apps", style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurface.copy(0.5f), modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(filtered, key = { it.packageName }) { app ->
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(scheme.surfaceVariant)
                            .clickable { viewModel.toggleAppLock(app) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)) {
                            Icon(
                                if (app.isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                contentDescription = null,
                                tint = if (app.isLocked) scheme.primary else scheme.outline,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(app.label, style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium, maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                                Text(app.packageName, style = MaterialTheme.typography.labelMedium,
                                    color = scheme.onSurface.copy(0.4f), maxLines = 1,
                                    overflow = TextOverflow.Ellipsis)
                            }
                        }
                        Switch(
                            checked         = app.isLocked,
                            onCheckedChange = { viewModel.toggleAppLock(app) },
                            colors          = SwitchDefaults.colors(checkedThumbColor = scheme.primary)
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
