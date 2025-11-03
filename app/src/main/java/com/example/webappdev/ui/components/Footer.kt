package com.example.webappdev.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Footer(
    // Properties

    isInGame: Boolean,
    onExitGame: (() -> Unit)? = null,
    onRestart: (() -> Unit)? = null
) {
    if (isInGame) {
        // In-game Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⏱ Timer: 00:00")
            Text("Captured: ♞ ♝ ♟")
            Row {
                TextButton(onClick = { onRestart?.invoke() }) { Text("Restart") }
                TextButton(onClick = { onExitGame?.invoke() }) { Text("Forfeit") }
            }
        }
    } else {
        // Footer for HomeScreen
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Lock, contentDescription = "Challenges") },
                label = { Text("Challenges") },
                selected = false,
                onClick = {}
            )
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Star, contentDescription = "Ranking") },
                label = { Text("Ranking") },
                selected = false,
                onClick = {}
            )
            NavigationBarItem(
                icon = { Icon(Icons.Filled.AddCircle, contentDescription = "New") },
                label = { Text("New") },
                selected = false,
                onClick = {}
            )
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Refresh, contentDescription = "History") },
                label = { Text("History") },
                selected = false,
                onClick = {}
            )
            NavigationBarItem(
                icon = { Icon(Icons.Filled.Build, contentDescription = "Lessons") },
                label = { Text("Lessons") },
                selected = false,
                onClick = {}
            )
        }
    }
}
