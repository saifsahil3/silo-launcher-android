package com.example

import com.example.model.LauncherMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LauncherModeTest {

    @Test
    fun testLauncherModeEnumContainsAllApps() {
        val modes = LauncherMode.values()
        val allAppsMode = LauncherMode.valueOf("ALL_APPS")
        assertNotNull(allAppsMode)
        assertEquals(LauncherMode.ALL_APPS, allAppsMode)
    }

    @Test
    fun testLauncherModeCount() {
        assertEquals(7, LauncherMode.values().size)
    }
}
