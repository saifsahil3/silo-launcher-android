package com.example.ui.components

import androidx.compose.ui.graphics.Color

sealed class FidgetLevel(
    val levelNumber: Int,
    val totalLengthPx: Float,
    val lineColor: Color,
    val strokeWidthDp: Float,
    val endItemName: String
) {
    object Level1 : FidgetLevel(
        levelNumber = 1,
        totalLengthPx = 5000f,
        lineColor = Color(0xFFC2A649), // Hemp Rope
        strokeWidthDp = 3.5f,
        endItemName = "Vintage Ship Anchor"
    )

    object Level2 : FidgetLevel(
        levelNumber = 2,
        totalLengthPx = 5000f,
        lineColor = Color(0xFF444444), // Lamp Cord
        strokeWidthDp = 2.5f,
        endItemName = "Hanging Light Bulb"
    )

    object Level3 : FidgetLevel(
        levelNumber = 3,
        totalLengthPx = 5000f,
        lineColor = Color(0xFFE0E0E0), // White Kite String
        strokeWidthDp = 1.2f,
        endItemName = "Origami Bird"
    )

    object Level4 : FidgetLevel(
        levelNumber = 4,
        totalLengthPx = 5000f,
        lineColor = Color(0xFF3A5A40), // Vintage Phone Cable
        strokeWidthDp = 2.5f,
        endItemName = "Phone Receiver"
    )

    companion object {
        fun getNextLevel(current: FidgetLevel): FidgetLevel {
            return when (current) {
                is Level1 -> Level2
                is Level2 -> Level3
                is Level3 -> Level4
                is Level4 -> Level1
            }
        }
    }
}
