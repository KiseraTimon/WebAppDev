package com.example.webappdev.engine.main


object Board {
    const val MAX_COL = 8
    const val MAX_ROW = 8

    // Logical square pixel size used for coordinate conversion.
    const val SQUARE_SIZE: Int = 80
    @JvmField
    val HALF_SQUARE_SIZE: Int = SQUARE_SIZE / 2
}
