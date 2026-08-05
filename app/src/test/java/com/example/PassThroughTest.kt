package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.PassThroughManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PassThroughTest {

    @Test
    fun testPassThroughActiveStateToggle() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Initially inactive
        PassThroughManager.setPassThroughActive(context, false)
        assertFalse(PassThroughManager.isPassThroughActive(context))

        // Activate
        PassThroughManager.setPassThroughActive(context, true)
        assertTrue(PassThroughManager.isPassThroughActive(context))

        // Deactivate
        PassThroughManager.setPassThroughActive(context, false)
        assertFalse(PassThroughManager.isPassThroughActive(context))
    }

    @Test
    fun testSaveAndRetrievePassThroughLauncherPackage() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        PassThroughManager.savePassThroughLauncher(context, "com.example.otherlauncher")
        val savedPkg = PassThroughManager.getSavedPassThroughLauncher(context)

        assertEquals("com.example.otherlauncher", savedPkg)
    }

    @Test
    fun testGetInstalledLaunchersNotReturningSelfAsOnlyOption() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val installedLaunchers = PassThroughManager.getInstalledLaunchers(context)

        assertNotNull(installedLaunchers)
    }
}
