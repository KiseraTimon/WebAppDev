package com.example.webappdev.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.webappdev.model.User
import com.example.webappdev.ui.components.ChessButton
import com.example.webappdev.ui.components.Navbar
import com.example.webappdev.ui.components.Footer

@Composable
fun HomeScreen(user: User, onStartGame: () -> Unit) {
    Scaffold(
        topBar = { Navbar(user = user, isInGame = false) },
        bottomBar = { Footer(isInGame = false) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center
        ) {
            // Opening GameScreen
            ChessButton("New Game") { onStartGame() }

            // Opening TimerScreen
            ChessButton("Match Timer") { /* Open timer dialog */ }

            // Opening SettingsScreen
            ChessButton("Settings") { /* Navigate to settings */ }

            // Opening Accounts Screen
            ChessButton("Account") { /* Open account page */ }
        }
    }
}
