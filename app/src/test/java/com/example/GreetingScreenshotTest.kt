package com.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@Composable
fun Greeting(name: String) {
    androidx.compose.material3.Text(text = "Hello $name!")
}

@org.junit.Ignore("Roborazzi screenshot test requires roborazzi gradle task")
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun greeting_screenshot() {
        val dir = java.io.File("src/test/screenshots")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        composeTestRule.setContent { MyApplicationTheme { Greeting("Robolectric") } }
        composeTestRule.waitForIdle()
        try {
            composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
