package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.FidgetLevel
import com.example.ui.components.getSavedFidgetLevel
import com.example.ui.components.saveFidgetLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
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

    @Test
    fun testFidgetLevelPersistenceAndReset() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Default should be Level 1
        assertEquals(1, getSavedFidgetLevel(context))

        // Progress to Level 2 and save
        saveFidgetLevel(context, 2)
        assertEquals(2, getSavedFidgetLevel(context))

        // Progress further to Level 5 and preserve
        saveFidgetLevel(context, 5)
        assertEquals(5, getSavedFidgetLevel(context))

        // Reset should reset back to Level 1
        saveFidgetLevel(context, 1)
        assertEquals(1, getSavedFidgetLevel(context))
    }
}
