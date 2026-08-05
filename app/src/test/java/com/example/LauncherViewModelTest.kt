package com.example

import com.example.model.LauncherMode
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherViewModelTest {

    @Test
    fun testModeSwitchingLogic() {
        var currentMode = LauncherMode.FOCUS
        currentMode = LauncherMode.ALL_APPS
        assertEquals(LauncherMode.ALL_APPS, currentMode)

        currentMode = LauncherMode.DRIVE
        assertEquals(LauncherMode.DRIVE, currentMode)
    }
}
