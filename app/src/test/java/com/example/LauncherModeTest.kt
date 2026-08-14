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

    @Test
    fun testLauncherModeExactOrder() {
        val expectedOrder = listOf(
            LauncherMode.FOCUS,
            LauncherMode.E_PAPER,
            LauncherMode.CREATOR,
            LauncherMode.DRIVE,
            LauncherMode.SLEEP,
            LauncherMode.ALL_APPS,
            LauncherMode.PASS_THROUGH
        )
        val actualOrder = LauncherMode.values().toList()
        assertEquals(expectedOrder, actualOrder)
    }
}
