package com.secure.applock.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinDots(pin: String, maxLen: Int = 6, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        repeat(maxLen) { i ->
            Box(
                Modifier.size(14.dp).clip(CircleShape).background(
                    if (i < pin.length) scheme.primary else scheme.outline.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
fun PinPad(pin: String, onKey: (String) -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        keys.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { key ->
                    when {
                        key.isEmpty() -> Spacer(Modifier.size(72.dp))
                        key == "⌫"   -> IconButton(onClick = onDelete, modifier = Modifier.size(72.dp)) {
                            Icon(Icons.Filled.Backspace, contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(26.dp))
                        }
                        else -> FilledTonalButton(
                            onClick = { onKey(key) },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(key, fontSize = 22.sp, fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}
