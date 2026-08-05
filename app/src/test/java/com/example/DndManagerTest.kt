package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.DndManager
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DndManagerTest {

    @Test
    fun testGetDndInterruptionFilterReturnsValidValue() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val filter = DndManager.getDndInterruptionFilter(context)
        assertNotNull(filter)
    }

    @Test
    fun testIsNotificationPolicyAccessGrantedDoesNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val hasAccess = DndManager.isNotificationPolicyAccessGranted(context)
        assertNotNull(hasAccess)
    }
}
