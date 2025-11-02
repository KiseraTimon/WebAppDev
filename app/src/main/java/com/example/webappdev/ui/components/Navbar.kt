package com.example.webappdev.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.webappdev.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navbar(user: User, isInGame: Boolean) {
    TopAppBar(
        title = {
            Column {
                Text(text = user.name, fontWeight = FontWeight.Bold)
                Text(text = user.email, style = MaterialTheme.typography.bodySmall)
            }
        },
        actions = {
            if (isInGame) {
                Text(
                    text = "Points: ${user.points}",
                    modifier = Modifier.padding(end = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    )
}
