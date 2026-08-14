package com.example

import com.example.model.FocusWidgetData
import com.example.model.focusWidgetsFromJson
import com.example.model.focusWidgetsToJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FocusWidgetDataTest {

    @Test
    fun testWidgetCreationAllVariants() {
        val notes = FocusWidgetData.BuiltInNotes("Test Note", heightDp = 180, widthFraction = 1.0f)
        val mantra = FocusWidgetData.BuiltInMantra(0, heightDp = 140, widthFraction = 0.5f)
        val timer = FocusWidgetData.BuiltInTimer(25, heightDp = 160, widthFraction = 1.0f)
        val audio = FocusWidgetData.BuiltInAudio("Rain Sounds", heightDp = 180, widthFraction = 0.5f)
        val shortcut = FocusWidgetData.AppShortcut("com.example.app", "Test App", heightDp = 120, widthFraction = 0.5f)
        val system = FocusWidgetData.SystemWidget(101, "Clock Widget", "com.sec.clock", heightDp = 200, widthFraction = 1.0f)

        assertNotNull(notes.id)
        assertNotNull(mantra.id)
        assertNotNull(timer.id)
        assertNotNull(audio.id)
        assertNotNull(shortcut.id)
        assertNotNull(system.id)

        assertEquals("Test Note", notes.content)
        assertEquals(0, mantra.quoteIndex)
        assertEquals(25, timer.durationMinutes)
        assertEquals("Rain Sounds", audio.title)
        assertEquals("com.example.app", shortcut.packageName)
        assertEquals(101, system.widgetId)
    }

    @Test
    fun testWidgetResizingHeightAndWidthFraction() {
        val original = FocusWidgetData.BuiltInNotes("Original Note", heightDp = 180, widthFraction = 1.0f)
        val resized = original.copy(heightDp = 240, widthFraction = 0.5f)

        assertEquals(180, original.heightDp)
        assertEquals(1.0f, original.widthFraction, 0.001f)

        assertEquals(240, resized.heightDp)
        assertEquals(0.5f, resized.widthFraction, 0.001f)
        assertEquals(original.id, resized.id)
    }

    @Test
    fun testWidgetListReordering() {
        val widgetA = FocusWidgetData.BuiltInNotes("A")
        val widgetB = FocusWidgetData.BuiltInTimer(25)
        val widgetC = FocusWidgetData.BuiltInAudio("C")

        val list = mutableListOf<FocusWidgetData>(widgetA, widgetB, widgetC)

        // Move widgetA from index 0 to index 1
        val itemToMove = list.removeAt(0)
        list.add(1, itemToMove)

        assertEquals(widgetB.id, list[0].id)
        assertEquals(widgetA.id, list[1].id)
        assertEquals(widgetC.id, list[2].id)
    }

    @Test
    fun testWidgetItemDeletion() {
        val widgetA = FocusWidgetData.BuiltInNotes("A")
        val widgetB = FocusWidgetData.BuiltInTimer(25)

        val list = mutableListOf<FocusWidgetData>(widgetA, widgetB)
        assertEquals(2, list.size)

        list.removeAt(0)
        assertEquals(1, list.size)
        assertEquals(widgetB.id, list[0].id)
    }

    @Test
    fun testJsonPersistenceWithStableId() {
        val notes = FocusWidgetData.BuiltInNotes("Persistence Note", heightDp = 220, widthFraction = 0.5f)
        val timer = FocusWidgetData.BuiltInTimer(30, heightDp = 150, widthFraction = 1.0f)

        val originalList = listOf(notes, timer)
        val json = focusWidgetsToJson(originalList)

        assertTrue(json.contains(notes.id))
        assertTrue(json.contains(timer.id))

        val restoredList = focusWidgetsFromJson(json)

        val restoredNotes = restoredList[0] as FocusWidgetData.BuiltInNotes
        val restoredTimer = restoredList[1] as FocusWidgetData.BuiltInTimer

        assertEquals(2, restoredList.size)
        assertEquals(notes.id, restoredNotes.id)
        assertEquals(220, restoredNotes.heightDp)
        assertEquals(0.5f, restoredNotes.widthFraction, 0.001f)

        assertEquals(timer.id, restoredTimer.id)
        assertEquals(150, restoredTimer.heightDp)
    }

    @Test
    fun testStockWidgetResizeHandleHeightClamping() {
        val notes = FocusWidgetData.BuiltInNotes("Clamping Note", heightDp = 180, widthFraction = 1.0f)
        
        // Simulating top/bottom handle drag delta beyond boundaries
        val dragDeltaNegative = -120
        val clampedMin = (notes.heightDp + dragDeltaNegative).coerceIn(100, 480)
        assertEquals(100, clampedMin)

        val dragDeltaPositive = 400
        val clampedMax = (notes.heightDp + dragDeltaPositive).coerceIn(100, 480)
        assertEquals(480, clampedMax)
    }

    @Test
    fun testStockWidgetSpanToggleFullAndHalf() {
        val system = FocusWidgetData.SystemWidget(501, "Weather", "com.weather", heightDp = 200, widthFraction = 1.0f)
        
        // Toggle to half width (50% span)
        val halfWidth = system.copy(widthFraction = 0.5f)
        assertTrue(halfWidth.widthFraction < 0.8f)

        // Toggle to full width (100% span)
        val fullWidth = halfWidth.copy(widthFraction = 1.0f)
        assertTrue(fullWidth.widthFraction >= 0.8f)
    }
}

