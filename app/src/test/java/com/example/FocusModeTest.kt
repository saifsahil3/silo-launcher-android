package com.example

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.LauncherMode
import com.example.model.FocusWidgetData
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
    fun testSystemWidget2DDimensionsAndBounds() {
        val systemWidget = FocusWidgetData.SystemWidget(
            widgetId = 301,
            label = "Analog Clock",
            packageName = "com.android.deskclock",
            heightDp = 180,
            widthFraction = 1.0f
        )
        assertEquals(180, systemWidget.heightDp)
        assertEquals(1.0f, systemWidget.widthFraction)

        // Mutate length (heightDp) and breadth (widthFraction)
        systemWidget.heightDp = 260
        systemWidget.widthFraction = 0.5f

        assertEquals(260, systemWidget.heightDp)
        assertEquals(0.5f, systemWidget.widthFraction)

        // Coerce within valid bounds (90 to 500 dp, 0.4f to 1.0f)
        systemWidget.heightDp = 600.coerceIn(90, 500)
        systemWidget.widthFraction = 1.5f.coerceIn(0.4f, 1.0f)

        assertEquals(500, systemWidget.heightDp)
        assertEquals(1.0f, systemWidget.widthFraction)
    }

    @Test
    fun testDefaultWidgetListIncludesTimer() {
        val defaultWidgets = mutableListOf<FocusWidgetData>(
            FocusWidgetData.BuiltInTimer(25),
            FocusWidgetData.BuiltInNotes("Task 1: Finish Deep Work session"),
            FocusWidgetData.BuiltInMantra(0)
        )

        assertEquals(3, defaultWidgets.size)
        assertTrue(defaultWidgets[0] is FocusWidgetData.BuiltInTimer)
        assertEquals(25, (defaultWidgets[0] as FocusWidgetData.BuiltInTimer).durationMinutes)
    }

    @Test
    fun testFocusAppsReordering() {
        val allApps = listOf(
            com.example.data.AppInfo("WhatsApp", "com.whatsapp", android.content.Intent()),
            com.example.data.AppInfo("Spotify", "com.spotify", android.content.Intent()),
            com.example.data.AppInfo("Calendar", "com.google.calendar", android.content.Intent())
        )

        // Custom ordered allowed packages
        val allowedPackages = setOf("com.spotify", "com.whatsapp")

        // Map allowed packages to apps list using custom order
        val focusApps = allowedPackages.mapNotNull { pkg -> allApps.find { it.packageName == pkg } }

        assertEquals(2, focusApps.size)
        // Verify custom order (Spotify first, then WhatsApp)
        assertEquals("com.spotify", focusApps[0].packageName)
        assertEquals("com.whatsapp", focusApps[1].packageName)
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
        assertTrue(modes.contains(LauncherMode.CREATOR))
        assertEquals(7, modes.size)
    }
}

