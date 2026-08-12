package com.example.ui.components

import androidx.compose.ui.graphics.Color
import com.example.ui.components.engine.LevelGenerator

sealed class FidgetLevel(
    val levelNumber: Int,
    val totalLengthPx: Float,
    val lineColor: Color,
    val strokeWidthDp: Float,
    val endItemName: String
) {
    object Level1 : FidgetLevel(
        levelNumber = 1,
        totalLengthPx = 2000f,
        lineColor = Color(0xFFD4AF37),
        strokeWidthDp = 1.2f,
        endItemName = "Button"
    )

    object Level2 : FidgetLevel(
        levelNumber = 2,
        totalLengthPx = 1500f,
        lineColor = Color(0xFFFFF8DC),
        strokeWidthDp = 1.2f,
        endItemName = "Feather"
    )

    object Level3 : FidgetLevel(
        levelNumber = 3,
        totalLengthPx = 2500f,
        lineColor = Color(0xFFE2E8F0),
        strokeWidthDp = 2.8f,
        endItemName = "Sock"
    )

    object Level4 : FidgetLevel(
        levelNumber = 4,
        totalLengthPx = 4000f,
        lineColor = Color(0xFFCBD5E1),
        strokeWidthDp = 2.8f,
        endItemName = "T-Shirt"
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

        fun getEngineLevel(levelId: Int): com.example.ui.components.engine.FidgetLevel {
            return LevelGenerator.getLevel(levelId)
        }
    }
}
