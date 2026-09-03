package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.LauncherMode
import com.example.ui.LauncherViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LauncherViewModelTest {

    private lateinit var viewModel: LauncherViewModel
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        viewModel = LauncherViewModel(context as Application)
    }

    @Test
    fun testModeSwitchingLogic() {
        viewModel.setMode(LauncherMode.FOCUS)
        assertEquals(LauncherMode.FOCUS, viewModel.currentMode.value)

        viewModel.setMode(LauncherMode.CREATOR)
        assertEquals(LauncherMode.CREATOR, viewModel.currentMode.value)
    }

    @Test
    fun testStartCreatorSession() {
        // Initially inactive
        assertFalse(viewModel.creatorSessionState.value.isActive)

        // Start session
        viewModel.startCreatorSession("shoot")
        val state = viewModel.creatorSessionState.value

        assertTrue(state.isActive)
        assertEquals("shoot", state.currentStageId)
        assertEquals(3600, state.secondsRemaining)
        assertTrue(state.isPaused)
        assertFalse(state.showResumePrompt)
    }

    @Test
    fun testPauseAndResumeCreatorSession() {
        viewModel.startCreatorSession("shoot")
        assertTrue(viewModel.creatorSessionState.value.isPaused)

        // Resume session
        viewModel.resumeCreatorSession()
        assertFalse(viewModel.creatorSessionState.value.isPaused)
        assertFalse(viewModel.creatorSessionState.value.showResumePrompt)

        // Pause session
        viewModel.pauseCreatorSession()
        assertTrue(viewModel.creatorSessionState.value.isPaused)
    }

    @Test
    fun testExtendCreatorSession() {
        viewModel.startCreatorSession("shoot")
        val initialSeconds = viewModel.creatorSessionState.value.secondsRemaining

        // Extend by 15 mins
        viewModel.extendCreatorSession(15)
        val extendedSeconds = viewModel.creatorSessionState.value.secondsRemaining

        assertEquals(initialSeconds + (15 * 60), extendedSeconds)
    }

    @Test
    fun testSwitchStageCreatorSession() {
        viewModel.startCreatorSession("shoot")
        assertEquals("shoot", viewModel.creatorSessionState.value.currentStageId)

        // Switch to edit
        viewModel.switchStage("edit")
        val state = viewModel.creatorSessionState.value

        assertEquals("edit", state.currentStageId)
        assertEquals(3600, state.secondsRemaining)
    }

    @Test
    fun testFinishCreatorSession() {
        viewModel.startCreatorSession("shoot")
        assertTrue(viewModel.creatorSessionState.value.isActive)

        // Finish
        viewModel.finishCreatorSession()
        assertFalse(viewModel.creatorSessionState.value.isActive)
    }

    @Test
    fun testIsDefaultLauncher() {
        val isDefault = viewModel.isDefaultLauncher(context)
        assertFalse(isDefault)
    }

    @Test
    fun testFocusTimerInitialState() {
        val timerState = viewModel.focusTimerState.value
        assertFalse(timerState.isRunning)
        assertFalse(timerState.isPaused)
        assertEquals(25, timerState.durationMinutes)
        assertEquals(25 * 60, timerState.secondsRemaining)
    }

    @Test
    fun testStartAndPauseFocusTimer() {
        // Start 25m timer
        viewModel.startFocusTimer(25)
        var state = viewModel.focusTimerState.value
        assertTrue(state.isRunning)
        assertFalse(state.isPaused)
        assertEquals(25 * 60, state.secondsRemaining)
        assertTrue(state.targetEndTimeMillis > System.currentTimeMillis())

        // Pause timer
        viewModel.pauseFocusTimer()
        state = viewModel.focusTimerState.value
        assertFalse(state.isRunning)
        assertTrue(state.isPaused)
        assertTrue(state.secondsRemaining <= 25 * 60)
    }

    @Test
    fun testResumeAndResetFocusTimer() {
        viewModel.startFocusTimer(15)
        viewModel.pauseFocusTimer()
        assertTrue(viewModel.focusTimerState.value.isPaused)

        // Resume timer
        viewModel.resumeFocusTimer()
        assertTrue(viewModel.focusTimerState.value.isRunning)
        assertFalse(viewModel.focusTimerState.value.isPaused)

        // Reset timer
        viewModel.resetFocusTimer(15)
        val resetState = viewModel.focusTimerState.value
        assertFalse(resetState.isRunning)
        assertFalse(resetState.isPaused)
        assertEquals(15 * 60, resetState.secondsRemaining)
    }

    @Test
    fun testSetFocusTimerDuration() {
        viewModel.setFocusTimerDuration(45)
        var state = viewModel.focusTimerState.value
        assertEquals(45, state.durationMinutes)
        assertEquals(45 * 60, state.secondsRemaining)

        viewModel.setFocusTimerDuration(60)
        state = viewModel.focusTimerState.value
        assertEquals(60, state.durationMinutes)
        assertEquals(60 * 60, state.secondsRemaining)
    }

    @Test
    fun testFocusTimerDndSettings() {
        viewModel.updateFocusAutoDnd(false)
        assertFalse(viewModel.focusTimerState.value.autoDndEnabled)

        viewModel.updateFocusAutoDnd(true)
        assertTrue(viewModel.focusTimerState.value.autoDndEnabled)

        viewModel.setFocusDndFilter(android.app.NotificationManager.INTERRUPTION_FILTER_ALARMS)
        assertEquals(android.app.NotificationManager.INTERRUPTION_FILTER_ALARMS, viewModel.focusTimerState.value.selectedDndFilter)
    }
}

