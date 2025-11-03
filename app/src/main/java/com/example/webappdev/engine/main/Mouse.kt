package com.example.webappdev.engine.main

// Mouse States
class Mouse {
    // Properties
    var x: Int = 0
    var y: Int = 0
    var pressed: Boolean = false

    // Pressed Mouse
    fun press(px: Int, py: Int) {
        x = px
        y = py
        pressed = true
    }

    // Released Mouse
    fun release(px: Int, py: Int) {
        x = px
        y = py
        pressed = false
    }

    // Moving Mouse
    fun move(px: Int, py: Int) {
        x = px
        y = py
    }
}
