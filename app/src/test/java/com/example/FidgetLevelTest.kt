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
        assertEquals("Button", current.endItemName)

        current = FidgetLevel.getNextLevel(current)
        assertEquals(2, current.levelNumber)
        assertEquals("Feather", current.endItemName)

        current = FidgetLevel.getNextLevel(current)
        assertEquals(3, current.levelNumber)
        assertEquals("Sock", current.endItemName)

        current = FidgetLevel.getNextLevel(current)
        assertEquals(4, current.levelNumber)
        assertEquals("T-Shirt", current.endItemName)

        current = FidgetLevel.getNextLevel(current)
        assertEquals(1, current.levelNumber)
    }

    @Test
    fun testStrokeWidthsAreSleek() {
        assertTrue(FidgetLevel.Level1.strokeWidthDp <= 4f)
        assertTrue(FidgetLevel.Level2.strokeWidthDp <= 3f)
        assertTrue(FidgetLevel.Level3.strokeWidthDp <= 3f)
        assertTrue(FidgetLevel.Level4.strokeWidthDp <= 3f)
    }

    @Test
    fun testEngineLevelBridge() {
        val level1 = FidgetLevel.getEngineLevel(1)
        assertEquals(1, level1.id)
        assertEquals("Button", level1.objectType.displayName)

        val level15 = FidgetLevel.getEngineLevel(15)
        assertEquals(15, level15.id)
        assertEquals("Small Sweater", level15.objectType.displayName)
    }
}
