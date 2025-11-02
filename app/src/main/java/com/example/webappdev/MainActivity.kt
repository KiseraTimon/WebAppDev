package com.example.webappdev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.webappdev.model.User
import com.example.webappdev.ui.screens.GameScreen
import com.example.webappdev.ui.screens.HomeScreen
import com.example.webappdev.ui.theme.WebAppDevTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebAppDevTheme {
                // Dummy user for now
                val user = remember { User("Kisera", "kisera@example.com") }

                // State to switch between home and game screens
                var isInGame by remember { mutableStateOf(false) }

                if (isInGame) {
                    GameScreen(
                        user = user,
                        onExitGame = { isInGame = false },
                        onRestart = { /* Restart logic */ }
                    )
                } else {
                    HomeScreen(
                        user = user,
                        onStartGame = { isInGame = true }
                    )
                }
            }
        }
    }
}
