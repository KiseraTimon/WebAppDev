package com.example.webappdev.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.webappdev.engine.main.GamePanel
import com.example.webappdev.engine.main.GamePanel.Companion.WHITE
import com.example.webappdev.model.User
import com.example.webappdev.ui.components.Footer
import com.example.webappdev.ui.components.Navbar

@Composable
fun GameScreen(
    user: User,
    onExitGame: () -> Unit,
    onRestart: () -> Unit
) {
    // --- Game State Controller ---
    val controller = remember { GamePanel() }
    var boardLayout by remember { mutableStateOf(controller.getCurrentLayout()) }
    var currentTurn by remember { mutableStateOf("White") }
    var statusMessage by remember { mutableStateOf("White to move") }
    var capturedPieces by remember { mutableStateOf(mutableListOf<String>()) }
    var showPromotionDialog by remember { mutableStateOf(false) }

    // --- Initialize game ---
    LaunchedEffect(Unit) {
        controller.initialize()
        boardLayout = controller.getCurrentLayout()
    }

    // --- Scaffold layout ---
    Scaffold(
        topBar = {
            Navbar(
                user = user,
                isInGame = true
            )
        },
        bottomBar = {
            Footer(
                isInGame = true,
                onExitGame = onExitGame,
                onRestart = {
                    controller.resetGame()
                    boardLayout = controller.getCurrentLayout()
                    currentTurn = "White"
                    statusMessage = "White to move"
                    capturedPieces.clear()
                    user.points = 0
                    onRestart()
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Turn and status
            Text(
                text = statusMessage,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Chess board
            ChessBoard(
                board = boardLayout,
                onSquareClick = { col, row ->
                    handleBoardTap(
                        controller,
                        col,
                        row,
                        onUpdate = { boardLayout = controller.getCurrentLayout() },
                        onCapture = { pieceCode ->
                            capturedPieces.add(pieceCode)
                            user.points += pieceValue(pieceCode)
                        },
                        onTurnSwitch = { isWhiteTurn ->
                            currentTurn = if (isWhiteTurn) "White" else "Black"
                            statusMessage = "$currentTurn to move"
                        },
                        onPromotion = {
                            showPromotionDialog = true
                        },
                        onCheckmate = {
                            statusMessage = "Checkmate! $currentTurn wins!"
                        },
                        onStalemate = {
                            statusMessage = "Stalemate! Game Drawn."
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Display captured pieces
            if (capturedPieces.isNotEmpty()) {
                Text(
                    text = "Captured: " + capturedPieces.joinToString(" ") { pieceToSymbol(it) },
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            }

            // Promotion dialog
            if (showPromotionDialog) {
                PromotionDialog(
                    onSelect = { index ->
                        controller.promoteTo(index)
                        boardLayout = controller.getCurrentLayout()
                        showPromotionDialog = false
                        statusMessage = "$currentTurn promoted a pawn!"
                    }
                )
            }
        }
    }
}

/**
 * Handles board tap logic: selecting & moving pieces, switching turns, scoring, etc.
 */
private var selectedSquare: Pair<Int, Int>? = null

private fun handleBoardTap(
    controller: GamePanel,
    col: Int,
    row: Int,
    onUpdate: () -> Unit,
    onCapture: (String) -> Unit,
    onTurnSwitch: (Boolean) -> Unit,
    onPromotion: () -> Unit,
    onCheckmate: () -> Unit,
    onStalemate: () -> Unit
) {
    val selected = selectedSquare
    if (selected == null) {
        val piece = controller.findPieceAt(col, row)
        if (piece != null && !controller.gameOver && !controller.stalemate) {
            selectedSquare = col to row
        }
    } else {
        val (fromCol, fromRow) = selected
        controller.selectSquare(fromCol, fromRow)
        val beforeCount = controller.getCurrentLayout().flatten().count { it.isNotEmpty() }
        controller.moveSelectedTo(col, row)
        val afterCount = controller.getCurrentLayout().flatten().count { it.isNotEmpty() }
        selectedSquare = null

        // Check capture
        if (afterCount < beforeCount) {
            // Find what was captured (approximation)
            onCapture("bP") // Placeholder: you can extend engine to return actual captured code
        }

        // Check promotion
        if (controller.promotion) {
            onPromotion()
            return
        }

        // Check end conditions
        if (controller.gameOver) {
            onCheckmate()
            return
        }

        if (controller.stalemate) {
            onStalemate()
            return
        }

        // Switch turn
        onTurnSwitch(controller.currentColor == WHITE)
        onUpdate()
    }
}

/**
 * Simple piece-to-symbol mapping
 */
private fun pieceToSymbol(code: String): String = when (code) {
    "wK" -> "♔"; "wQ" -> "♕"; "wR" -> "♖"; "wB" -> "♗"; "wN" -> "♘"; "wP" -> "♙"
    "bK" -> "♚"; "bQ" -> "♛"; "bR" -> "♜"; "bB" -> "♝"; "bN" -> "♞"; "bP" -> "♟"
    else -> ""
}

/**
 * Assigns points by captured piece type
 */
private fun pieceValue(code: String): Int = when (code.last()) {
    'P' -> 1
    'N', 'B' -> 3
    'R' -> 5
    'Q' -> 9
    else -> 0
}

/**
 * Composable 8x8 chessboard UI
 */
@Composable
fun ChessBoard(
    board: Array<Array<String>>,
    onSquareClick: (Int, Int) -> Unit
) {
    val lightColor = Color(0xFFEEEED2)
    val darkColor = Color(0xFF769656)

    Column(modifier = Modifier.size(360.dp)) {
        for (row in 0 until 8) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0 until 8) {
                    val color = if ((row + col) % 2 == 0) lightColor else darkColor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(color)
                            .clickable { onSquareClick(col, row) },
                        contentAlignment = Alignment.Center
                    ) {
                        val piece = board[row][col]
                        if (piece.isNotEmpty()) {
                            Text(
                                text = pieceToSymbol(piece),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (piece.startsWith("w")) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Promotion selection dialog (Queen, Rook, Bishop, Knight)
 */
@Composable
fun PromotionDialog(onSelect: (Int) -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Promote Pawn") },
        text = {
            Column {
                listOf("Queen", "Rook", "Bishop", "Knight").forEachIndexed { index, name ->
                    TextButton(onClick = { onSelect(index) }) {
                        Text(text = name, fontSize = 18.sp)
                    }
                }
            }
        },
        confirmButton = {}
    )
}
