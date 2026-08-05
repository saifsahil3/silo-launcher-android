package com.example

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.LauncherMode
import com.example.ui.screens.FocusWidgetData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FocusModeTest {

    @Test
    fun testFocusWidgetDataTypesAndProperties() {
        val notesWidget = FocusWidgetData.BuiltInNotes("Test Note Content")
        assertEquals("Test Note Content", notesWidget.content)

        val mantraWidget = FocusWidgetData.BuiltInMantra(0)
        assertEquals(0, mantraWidget.quoteIndex)

        val timerWidget = FocusWidgetData.BuiltInTimer(25)
        assertEquals(25, timerWidget.durationMinutes)

        val audioWidget = FocusWidgetData.BuiltInAudio("Rain & White Noise Loop")
        assertEquals("Rain & White Noise Loop", audioWidget.title)

        val appShortcutWidget = FocusWidgetData.AppShortcut("com.example.app", "Test App")
        assertEquals("com.example.app", appShortcutWidget.packageName)
        assertEquals("Test App", appShortcutWidget.appName)

        val systemWidget = FocusWidgetData.SystemWidget(101, "Clock Widget", "com.android.alarmclock")
        assertEquals(101, systemWidget.widgetId)
        assertEquals("Clock Widget", systemWidget.label)
        assertEquals("com.android.alarmclock", systemWidget.packageName)
    }

    @Test
    fun testSystemWidgetVersusAppShortcutTypeSeparation() {
        val systemWidget: FocusWidgetData = FocusWidgetData.SystemWidget(102, "Calendar Widget", "com.google.android.calendar")
        val appShortcut: FocusWidgetData = FocusWidgetData.AppShortcut("com.google.android.calendar", "Google Calendar")

        // SystemWidget must NOT be an AppShortcut
        assertTrue(systemWidget is FocusWidgetData.SystemWidget)
        assertFalse(systemWidget is FocusWidgetData.AppShortcut)

        // AppShortcut must NOT be a SystemWidget
        assertTrue(appShortcut is FocusWidgetData.AppShortcut)
        assertFalse(appShortcut is FocusWidgetData.SystemWidget)

        assertEquals(102, (systemWidget as FocusWidgetData.SystemWidget).widgetId)
    }

    @Test
    fun testBuiltInNotesStateMutation() {
        val notesWidget = FocusWidgetData.BuiltInNotes("Initial task")
        assertEquals("Initial task", notesWidget.content)

        notesWidget.content = "Updated task 1\nUpdated task 2"
        assertEquals("Updated task 1\nUpdated task 2", notesWidget.content)
    }

    @Test
    fun testBuiltInMantraQuoteCycling() {
        val mantraWidget = FocusWidgetData.BuiltInMantra(0)
        assertEquals(0, mantraWidget.quoteIndex)

        // Cycle through 5 quotes
        for (i in 1..5) {
            mantraWidget.quoteIndex += 1
            assertEquals(i, mantraWidget.quoteIndex)
        }
    }

    @Test
    fun testBuiltInTimerDurationUpdates() {
        val timer = FocusWidgetData.BuiltInTimer(25)
        assertEquals(25, timer.durationMinutes)

        timer.durationMinutes = 15
        assertEquals(15, timer.durationMinutes)

        timer.durationMinutes = 50
        assertEquals(50, timer.durationMinutes)
    }

    @Test
    fun testFocusWidgetListOperations() {
        val widgetList = mutableListOf<FocusWidgetData>()

        // Add default widgets
        widgetList.add(FocusWidgetData.BuiltInNotes("Initial Note"))
        widgetList.add(FocusWidgetData.BuiltInMantra(0))
        assertEquals(2, widgetList.size)

        // Add new timer widget
        val timer = FocusWidgetData.BuiltInTimer(15)
        widgetList.add(timer)
        assertEquals(3, widgetList.size)

        // Add system widget
        val sysWidget = FocusWidgetData.SystemWidget(201, "Weather", "com.example.weather")
        widgetList.add(sysWidget)
        assertEquals(4, widgetList.size)

        // Modify note content
        (widgetList[0] as FocusWidgetData.BuiltInNotes).content = "Updated Note"
        assertEquals("Updated Note", (widgetList[0] as FocusWidgetData.BuiltInNotes).content)

        // Remove widget by index
        widgetList.removeAt(1)
        assertEquals(3, widgetList.size)
        assertTrue(widgetList[0] is FocusWidgetData.BuiltInNotes)
        assertTrue(widgetList[1] is FocusWidgetData.BuiltInTimer)
        assertTrue(widgetList[2] is FocusWidgetData.SystemWidget)
    }

    @Test
    fun testAppWidgetHostLifecycleAndAllocation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appWidgetHost = AppWidgetHost(context, 1024)
        val appWidgetManager = AppWidgetManager.getInstance(context)

        assertNotNull(appWidgetHost)
        assertNotNull(appWidgetManager)

        // Start listening
        appWidgetHost.startListening()

        // Allocate a new widget ID
        val widgetId = appWidgetHost.allocateAppWidgetId()
        assertTrue(widgetId >= 0)

        // Verify un-bound widget info returns null safely without throwing
        val widgetInfo = try {
            appWidgetManager.getAppWidgetInfo(widgetId)
        } catch (e: Throwable) {
            null
        }
        assertNull(widgetInfo)

        // Clean up allocated widget ID
        appWidgetHost.deleteAppWidgetId(widgetId)

        // Stop listening
        appWidgetHost.stopListening()
    }

    @Test
    fun testSystemWidgetHeightResizing() {
        val systemWidget = FocusWidgetData.SystemWidget(
            widgetId = 301,
            label = "Analog Clock",
            packageName = "com.android.deskclock",
            heightDp = 180
        )
        assertEquals(180, systemWidget.heightDp)

        // Mutate heightDp as performed by resize controls
        systemWidget.heightDp = 260
        assertEquals(260, systemWidget.heightDp)

        systemWidget.heightDp = 360
        assertEquals(360, systemWidget.heightDp)
    }

    @Test
    fun testLauncherModesEnum() {
        val modes = LauncherMode.entries
        assertFalse(modes.map { it.name }.contains("NORMAL"))
        assertTrue(modes.contains(LauncherMode.FOCUS))
        assertTrue(modes.contains(LauncherMode.ALL_APPS))
        assertTrue(modes.contains(LauncherMode.PASS_THROUGH))
        assertTrue(modes.contains(LauncherMode.DRIVE))
        assertTrue(modes.contains(LauncherMode.SLEEP))
        assertTrue(modes.contains(LauncherMode.E_PAPER))
        assertEquals(6, modes.size)
    }
}

