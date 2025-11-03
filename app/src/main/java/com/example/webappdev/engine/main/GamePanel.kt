package com.example.webappdev.engine.main

import com.example.webappdev.engine.piece.*
import kotlin.math.abs

// Game Controller
class GamePanel {

    // Colors
    companion object {
        const val WHITE: Int = 0
        const val BLACK: Int = 1
    }

    // Pieces collections
    val pieces: ArrayList<Piece> = ArrayList()
    val simPieces: ArrayList<Piece> = ArrayList()
    var castlingPiece: Piece? = null

    // Game fundamentals
    private val board = Board
    private val mouse = Mouse()

    // Promotion options
    private val promotionPieces: ArrayList<Piece> = ArrayList()
    private var activePiece: Piece? = null
    private var checkingPiece: Piece? = null
    var currentColor: Int = WHITE

    // Game-state booleans
    var canMove: Boolean = false
    var validSquare: Boolean = false
    var promotion: Boolean = false
    var gameOver: Boolean = false
    var stalemate: Boolean = false

    init {
        // No UI initializations;
    }

    // Initializing Pieces on Standard Starting Position
    fun initialize() {
        pieces.clear()
        simPieces.clear()
        castlingPiece = null
        activePiece = null
        checkingPiece = null
        currentColor = WHITE
        gameOver = false
        stalemate = false
        promotion = false
        canMove = false
        validSquare = false

        // Whites
        pieces.add(Pawn(WHITE, 0, 6))
        pieces.add(Pawn(WHITE, 1, 6))
        pieces.add(Pawn(WHITE, 2, 6))
        pieces.add(Pawn(WHITE, 3, 6))
        pieces.add(Pawn(WHITE, 4, 6))
        pieces.add(Pawn(WHITE, 5, 6))
        pieces.add(Pawn(WHITE, 6, 6))
        pieces.add(Pawn(WHITE, 7, 6))
        pieces.add(Rook(WHITE, 0, 7))
        pieces.add(Rook(WHITE, 7, 7))
        pieces.add(Knight(WHITE, 1, 7))
        pieces.add(Knight(WHITE, 6, 7))
        pieces.add(Bishop(WHITE, 2, 7))
        pieces.add(Bishop(WHITE, 5, 7))
        pieces.add(Queen(WHITE, 3, 7))
        pieces.add(King(WHITE, 4, 7))

        // Blacks
        pieces.add(Pawn(BLACK, 0, 1))
        pieces.add(Pawn(BLACK, 1, 1))
        pieces.add(Pawn(BLACK, 2, 1))
        pieces.add(Pawn(BLACK, 3, 1))
        pieces.add(Pawn(BLACK, 4, 1))
        pieces.add(Pawn(BLACK, 5, 1))
        pieces.add(Pawn(BLACK, 6, 1))
        pieces.add(Pawn(BLACK, 7, 1))
        pieces.add(Rook(BLACK, 0, 0))
        pieces.add(Rook(BLACK, 7, 0))
        pieces.add(Knight(BLACK, 1, 0))
        pieces.add(Knight(BLACK, 6, 0))
        pieces.add(Bishop(BLACK, 2, 0))
        pieces.add(Bishop(BLACK, 5, 0))
        pieces.add(Queen(BLACK, 3, 0))
        pieces.add(King(BLACK, 4, 0))

        // Preparing Simulated Pieces
        copyPieces(pieces, simPieces)
    }

    /**
    * Input Helpers
    */

    // Handling Key Presses
    fun touchDown(px: Int, py: Int) {
        mouse.press(px, py)
        // Process pick-up on press
        updateSelectionOnPress()
    }

    // Handling Mouse Movements
    fun touchMove(px: Int, py: Int) {
        mouse.move(px, py)
        // Simulate dragging behavior
        if (activePiece != null) simulate()
    }

    // Handling Key Releases
    fun touchUp(px: Int, py: Int) {
        mouse.release(px, py)
        // Finalizing Movement
        if (activePiece != null) {
            if (validSquare) {
                // Confirming Movement
                copyPieces(simPieces, pieces)
                activePiece!!.updatePosition()
                if (castlingPiece != null) {
                    castlingPiece!!.updatePosition()
                }

                if (isKingInCheck && isCheckmate) {
                    gameOver = true
                } else if (isStalemate()) {
                    stalemate = true
                } else {
                    if (canPromote()) {
                        promotion = true
                    } else {
                        changePlayer()
                    }
                }
            } else {
                // Reverting on Invalid Moves
                copyPieces(simPieces, pieces)
                activePiece!!.resetPosition()
                activePiece = null
            }
        }
    }

    /**
     * Square Coordinate Helpers
     */

    fun selectSquare(col: Int, row: Int) {
        val px = col * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
        val py = row * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
        touchDown(px, py)
    }

    fun moveSelectedTo(col: Int, row: Int) {
        val px = col * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
        val py = row * Board.SQUARE_SIZE + Board.HALF_SQUARE_SIZE
        touchMove(px, py)
        touchUp(px, py)
    }

    /**
     * Core
     */

    private fun updateSelectionOnPress() {
        // Selecting Present-of Colour Piece if no Active Piece
        if (activePiece == null) {
            // Converting Mouse Coordinates
            val mx = mouse.x / Board.SQUARE_SIZE
            val my = mouse.y / Board.SQUARE_SIZE
            for (piece in simPieces) {
                if (piece.color == currentColor && piece.col == mx && piece.row == my) {
                    activePiece = piece
                    break
                }
            }
        } else {
            // Simulating Move
            simulate()
        }
    }

    /**
     * Simulating Currently Held Piece
     * Called in Drag/Release Events
     * Checks if the Piece can move and if the square is valid
     */

    private fun simulate() {
        canMove = false
        validSquare = false

        // Copying Piece to Sim List
        copyPieces(pieces, simPieces)

        // Reseting Castling Piece Placeholders
        if (castlingPiece != null) {
            castlingPiece!!.col = castlingPiece!!.preCol
            castlingPiece!!.x = castlingPiece!!.getX(castlingPiece!!.col)
            castlingPiece = null
        }

        // Updating Active Piece Coords based on Moue Movements
        activePiece!!.x = mouse.x - Board.HALF_SQUARE_SIZE
        activePiece!!.y = mouse.y - Board.HALF_SQUARE_SIZE
        activePiece!!.col = activePiece!!.getCol(activePiece!!.x)
        activePiece!!.row = activePiece!!.getRow(activePiece!!.y)

        // Checking for legal squares
        if (activePiece!!.canMove(activePiece!!.col, activePiece!!.row)) {
            canMove = true

            // Removing from sim list on collisions
            if (activePiece!!.hittingPiece != null) {
                simPieces.remove(activePiece!!.hittingPiece)
            }

            checkCastling()

            if (!isIllegal(activePiece!!) && !opponentCanCaptureKing()) {
                validSquare = true
            }
        }
    }

    // Checking Kings for Illegal Positions
    private fun isIllegal(king: Piece): Boolean {
        if (king.type == Type.KING) {
            for (piece in simPieces) {
                if (piece !== king && piece.color != king.color && piece.canMove(king.col, king.row)) {
                    return true
                }
            }
        }
        return false
    }

    // Checking for Mates & Checkmates
    private fun opponentCanCaptureKing(): Boolean {
        val king = getKing(false)
        for (piece in simPieces) {
            if (piece.color != king.color && piece.canMove(king.col, king.row)) {
                return true
            }
        }
        return false
    }

    private val isKingInCheck: Boolean
        get() {
            val king = getKing(true) // opponent king
            if (activePiece!!.canMove(king.col, king.row)) {
                checkingPiece = activePiece
                return true
            } else {
                checkingPiece = null
            }
            return false
        }

    private fun getKing(opponent: Boolean): Piece {
        for (piece in simPieces) {
            if (opponent) {
                if (piece.type == Type.KING && piece.color != currentColor) return piece
            } else {
                if (piece.type == Type.KING && piece.color == currentColor) return piece
            }
        }
        throw IllegalStateException("King not found - corrupted game state")
    }

    private val isCheckmate: Boolean
        get() {
            // Opponent's King
            val king = getKing(true)
            if (kingCanMove(king)) return false

            // If king cannot move, we try to block the checking piece's attacking path
            val cp = checkingPiece ?: return true
            val colDiff = abs(cp.col - king.col)
            val rowDiff = abs(cp.row - king.row)

            // vertical/horizontal/diagonal blocking logic
            if (colDiff == 0) {
                if (cp.row < king.row) {
                    for (r in cp.row until king.row) {
                        for (piece in simPieces) {
                            if (piece !== king && piece.color != currentColor && piece.canMove(cp.col, r)) {
                                return false
                            }
                        }
                    }
                } else {
                    for (r in cp.row downTo (king.row + 1)) {
                        for (piece in simPieces) {
                            if (piece !== king && piece.color != currentColor && piece.canMove(cp.col, r)) {
                                return false
                            }
                        }
                    }
                }
            } else if (rowDiff == 0) {
                if (cp.col < king.col) {
                    for (c in cp.col until king.col) {
                        for (piece in simPieces) {
                            if (piece !== king && piece.color != currentColor && piece.canMove(c, cp.row)) {
                                return false
                            }
                        }
                    }
                } else {
                    for (c in cp.col downTo (king.col + 1)) {
                        for (piece in simPieces) {
                            if (piece !== king && piece.color != currentColor && piece.canMove(c, cp.row)) {
                                return false
                            }
                        }
                    }
                }
            } else if (colDiff == rowDiff) {
                // diagonal checking piece, iterate squares on diagonal between checking piece and king
                if (cp.row < king.row) {
                    if (cp.col < king.col) {
                        var c = cp.col
                        var r = cp.row
                        while (c < king.col) {
                            for (piece in simPieces) {
                                if (piece !== king && piece.color != currentColor && piece.canMove(c, r)) return false
                            }
                            c++; r++
                        }
                    } else {
                        var c = cp.col
                        var r = cp.row
                        while (c > king.col) {
                            for (piece in simPieces) {
                                if (piece !== king && piece.color != currentColor && piece.canMove(c, r)) return false
                            }
                            c--; r++
                        }
                    }
                } else {
                    if (cp.col < king.col) {
                        var c = cp.col
                        var r = cp.row
                        while (c < king.col) {
                            for (piece in simPieces) {
                                if (piece !== king && piece.color != currentColor && piece.canMove(c, r)) return false
                            }
                            c++; r--
                        }
                    } else {
                        var c = cp.col
                        var r = cp.row
                        while (c > king.col) {
                            for (piece in simPieces) {
                                if (piece !== king && piece.color != currentColor && piece.canMove(c, r)) return false
                            }
                            c--; r--
                        }
                    }
                }
            }
            return true
        }

    private fun kingCanMove(king: Piece): Boolean {
        // Testing all adjacent squares
        val deltas = listOf(-1 to -1, 0 to -1, 1 to -1, -1 to 0, 1 to 0, -1 to 1, 0 to 1, 1 to 1)
        for ((dc, dr) in deltas) {
            if (isValidMove(king, dc, dr)) return true
        }
        return false
    }

    private fun isValidMove(king: Piece, colPlus: Int, rowPlus: Int): Boolean {
        var isValidMove = false
        // Temporarily updating king position
        king.col += colPlus
        king.row += rowPlus
        if (king.canMove(king.col, king.row)) {
            if (king.hittingPiece != null) simPieces.remove(king.hittingPiece)
            if (!isIllegal(king)) isValidMove = true
        }
        // Reset
        king.resetPosition()
        copyPieces(pieces, simPieces)
        return isValidMove
    }

    private fun isStalemate(): Boolean {
        var count = 0
        for (piece in simPieces) {
            if (piece.color != currentColor) count++
        }
        if (count == 1) {
            if (!kingCanMove(getKing(true))) return true
        }
        return false
    }

    private fun checkCastling() {
        if (castlingPiece != null) {
            if (castlingPiece!!.col == 0) {
                castlingPiece!!.col += 3
            } else if (castlingPiece!!.col == 7) {
                castlingPiece!!.col -= 2
            }
            castlingPiece!!.x = castlingPiece!!.getX(castlingPiece!!.col)
        }
    }

    private fun changePlayer() {
        if (currentColor == WHITE) {
            currentColor = BLACK
            for (piece in pieces) if (piece.color == BLACK) piece.twoStepped = false
        } else {
            currentColor = WHITE
            for (piece in pieces) if (piece.color == WHITE) piece.twoStepped = false
        }
        activePiece = null
    }

    private fun canPromote(): Boolean {
        if (activePiece!!.type == Type.PAWN) {
            if (currentColor == WHITE && activePiece!!.row == 0 || currentColor == BLACK && activePiece!!.row == 7) {
                promotionPieces.clear()
                promotionPieces.add(Rook(currentColor, 9, 2))
                promotionPieces.add(Knight(currentColor, 9, 3))
                promotionPieces.add(Bishop(currentColor, 9, 4))
                promotionPieces.add(Queen(currentColor, 9, 5))
                return true
            }
        }
        return false
    }

    // Pawn Promotions
    fun promoteTo(index: Int) {
        if (!promotion) return
        if (index < 0 || index >= promotionPieces.size) return

        val chosenType = promotionPieces[index].type
        when (chosenType) {
            Type.ROOK -> simPieces.add(Rook(currentColor, activePiece!!.col, activePiece!!.row))
            Type.KNIGHT -> simPieces.add(Knight(currentColor, activePiece!!.col, activePiece!!.row))
            Type.BISHOP -> simPieces.add(Bishop(currentColor, activePiece!!.col, activePiece!!.row))
            Type.QUEEN -> simPieces.add(Queen(currentColor, activePiece!!.col, activePiece!!.row))
            else -> {}
        }
        simPieces.remove(activePiece)
        copyPieces(simPieces, pieces)
        activePiece = null
        promotion = false
        changePlayer()
    }

    // Copying Source List to Target List
    private fun copyPieces(source: ArrayList<Piece>, target: ArrayList<Piece>) {
        target.clear()
        for (p in source) {
            target.add(p.copyForSim())
        }
    }

    // Piece Position Lookup
    fun findPieceAt(col: Int, row: Int): Piece? {
        for (p in pieces) {
            if (p.col == col && p.row == row) return p
        }
        return null
    }

    // Piece Current Position
    fun getCurrentLayout(): Array<Array<String>> {
        val grid = Array(8) { Array(8) { "" } }
        for (p in pieces) {
            val code = (if (p.color == WHITE) "w" else "b") + when (p.type) {
                Type.PAWN -> "P"
                Type.ROOK -> "R"
                Type.KNIGHT -> "N"
                Type.BISHOP -> "B"
                Type.QUEEN -> "Q"
                Type.KING -> "K"
                else -> "?"
            }
            if (p.row in 0..7 && p.col in 0..7) grid[p.row][p.col] = code
        }
        return grid
    }

    // Game Reset Back to Original Positions
    fun resetGame() {
        initialize()
    }

    // Coordinate Conversion Helpers
    fun pixelToCol(px: Int): Int = (px + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE
    fun pixelToRow(py: Int): Int = (py + Board.HALF_SQUARE_SIZE) / Board.SQUARE_SIZE
}
