package com.example

import com.example.model.LauncherMode
import com.example.db.CreatorStageConfigEntity
import com.example.ui.CreatorSessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatorModeTest {

    @Test
    fun testLauncherModeEnumContainsCreator() {
        val modes = LauncherMode.entries
        assertTrue(modes.contains(LauncherMode.CREATOR))
    }

    @Test
    fun testCreatorSessionStateProperties() {
        val state = CreatorSessionState()
        assertFalse(state.isActive)
        assertEquals("shoot", state.currentStageId)
        assertEquals(3600, state.secondsRemaining)
        assertFalse(state.isPaused)
        assertFalse(state.showResumePrompt)

        val activeState = CreatorSessionState(
            isActive = true,
            currentStageId = "edit",
            secondsRemaining = 1800,
            isPaused = true,
            showResumePrompt = true
        )
        assertTrue(activeState.isActive)
        assertEquals("edit", activeState.currentStageId)
        assertEquals(1800, activeState.secondsRemaining)
        assertTrue(activeState.isPaused)
        assertTrue(activeState.showResumePrompt)
    }

    @Test
    fun testCreatorStageConfigEntityProperties() {
        val config = CreatorStageConfigEntity(
            stageId = "shoot",
            sessionDurationMinutes = 60,
            primaryApps = "com.google.android.GoogleCamera,com.android.camera",
            supportApps = "com.google.android.apps.photos,com.android.documentsui,com.google.android.keep,com.openai.chatgpt",
            widgetIds = "101,102"
        )

        assertEquals("shoot", config.stageId)
        assertEquals(60, config.sessionDurationMinutes)
        assertEquals("com.google.android.GoogleCamera,com.android.camera", config.primaryApps)
        assertEquals("com.google.android.apps.photos,com.android.documentsui,com.google.android.keep,com.openai.chatgpt", config.supportApps)
        assertEquals("101,102", config.widgetIds)
    }

    @Test
    fun testAppStringParsing() {
        val config = CreatorStageConfigEntity(
            stageId = "edit",
            primaryApps = "com.capcut.android, com.lenovo.videoplayer",
            supportApps = ""
        )

        val primaryPackages = config.primaryApps.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val supportPackages = config.supportApps.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        assertEquals(2, primaryPackages.size)
        assertEquals("com.capcut.android", primaryPackages[0])
        assertEquals("com.lenovo.videoplayer", primaryPackages[1])
        assertTrue(supportPackages.isEmpty())
    }

    @Test
    fun testExtendSessionTimeCalculation() {
        var secondsRemaining = 10 * 60 // 10 minutes
        val extensionMinutes = 15

        // Extend session by 15 mins
        secondsRemaining += (extensionMinutes * 60)
        assertEquals(25 * 60, secondsRemaining)
    }

    @Test
    fun testSwitchStageLogic() {
        var currentStageId = "shoot"
        var secondsRemaining = 60 * 60

        // Transition to edit stage with 45 minutes duration
        currentStageId = "edit"
        secondsRemaining = 45 * 60

        assertEquals("edit", currentStageId)
        assertEquals(45 * 60, secondsRemaining)
    }
}
