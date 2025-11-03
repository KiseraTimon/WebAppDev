package com.example.webappdev.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.webappdev.model.User
import com.example.webappdev.ui.components.Navbar
import com.example.webappdev.ui.components.Footer

@Composable
fun GameScreen(user: User, onExitGame: () -> Unit, onRestart: () -> Unit) {
    Scaffold(
        topBar = { Navbar(user = user, isInGame = true) },
        bottomBar = {
            Footer(
                isInGame = true,
                onExitGame = onExitGame,
                onRestart = onRestart
            )
        }
    ) { padding ->
        // Chess Placeholder Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        )
    }
}
