package com.example

import com.example.ui.components.FidgetLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FidgetLevelTest {

    @Test
    fun testLevelProgressionSequence() {
        var current: FidgetLevel = FidgetLevel.Level1
        assertEquals(1, current.levelNumber)
        assertEquals("Vintage Ship Anchor", current.endItemName)

        current = FidgetLevel.getNextLevel(current)
        assertEquals(2, current.levelNumber)
        assertEquals("Hanging Light Bulb", current.endItemName)

        current = FidgetLevel.getNextLevel(current)
        assertEquals(3, current.levelNumber)
        assertEquals("Origami Bird", current.endItemName)

        current = FidgetLevel.getNextLevel(current)
        assertEquals(4, current.levelNumber)
        assertEquals("Phone Receiver", current.endItemName)

        current = FidgetLevel.getNextLevel(current)
        assertEquals(1, current.levelNumber)
    }

    @Test
    fun testStrokeWidthsAreSleek() {
        assertTrue(FidgetLevel.Level1.strokeWidthDp <= 4f)
        assertTrue(FidgetLevel.Level2.strokeWidthDp <= 3f)
        assertTrue(FidgetLevel.Level3.strokeWidthDp <= 2f)
        assertTrue(FidgetLevel.Level4.strokeWidthDp <= 3f)
    }
}
