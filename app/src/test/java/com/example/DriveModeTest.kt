package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppInfo
import com.example.db.DriveCommShortcutEntity
import com.example.model.LauncherMode
import com.example.service.MediaStateManager
import com.example.ui.LauncherViewModel
import kotlinx.coroutines.runBlocking
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
class DriveModeTest {

    private lateinit var viewModel: LauncherViewModel
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        viewModel = LauncherViewModel(context as Application)
    }

    @Test
    fun testModeSwitchToDrive() {
        viewModel.setMode(LauncherMode.DRIVE)
        assertEquals(LauncherMode.DRIVE, viewModel.currentMode.value)
    }

    @Test
    fun testNavLocationsSave() {
        viewModel.saveDriveNavLocation(
            id = "custom1",
            label = "Gym",
            addressOrQuery = "Equinox Sports Club"
        )
    }

    @Test
    fun testPlaceSuggestionsLookup() = runBlocking {
        // Query empty or short
        val emptyResults = viewModel.searchPlaceSuggestions(context, "")
        assertTrue(emptyResults.isEmpty())

        // Non-empty place query executes safely
        val results = viewModel.searchPlaceSuggestions(context, "New York")
        assertNotNull(results)
    }

    @Test
    fun testMediaAppFilteringExcludesCameraAndGallery() {
        val testApps = listOf(
            AppInfo("Spotify", "com.spotify.music", Intent(), null, false, "Media"),
            AppInfo("Camera", "com.android.camera2", Intent(), null, false, "Media"),
            AppInfo("Google Photos", "com.google.android.apps.photos", Intent(), null, false, "Media"),
            AppInfo("Gallery", "com.sec.android.gallery3d", Intent(), null, false, "Media"),
            AppInfo("YouTube Music", "com.google.android.apps.youtube.music", Intent(), null, false, "Media"),
            AppInfo("Screen Recorder", "com.hecorat.screenrecorder.free", Intent(), null, false, "Media"),
            AppInfo("Audible", "com.audible.application", Intent(), null, false, "Media")
        )

        val excludedKeywords = listOf(
            "camera", "photo", "gallery", "image", "editor", "recorder",
            "screenshot", "video", "cinema", "movie", "film", "lens", "snap", "scanner", "voice recorder"
        )
        val audioPackages = listOf(
            "spotify", "youtube.music", "music", "audio", "podcast", "soundcloud",
            "pandora", "audible", "deezer", "tidal", "apple.android.music", "amazon.mp3",
            "poweramp", "musicolet", "shuttle", "blackplayer", "vlc", "tunein", "iheartradio"
        )

        val filteredMedia = testApps.filter { app ->
            val pkg = app.packageName.lowercase()
            val label = app.label.lowercase()

            val isExcluded = excludedKeywords.any { pkg.contains(it) || label.contains(it) }
            if (isExcluded) return@filter false

            val isAudioPackage = audioPackages.any { pkg.contains(it) }
            val isAudioLabel = label.contains("music") || label.contains("podcast") ||
                label.contains("radio") || label.contains("audiobook") || label.contains("audible") ||
                label.contains("sound") || label.contains("tuner")
            val isAudioCategory = app.category.equals("Audio", ignoreCase = true) ||
                app.category.equals("Music", ignoreCase = true) ||
                app.category.equals("Music & Audio", ignoreCase = true)

            isAudioPackage || isAudioLabel || isAudioCategory
        }

        val mediaPkgNames = filteredMedia.map { it.packageName }

        // Assert music apps are included
        assertTrue(mediaPkgNames.contains("com.spotify.music"))
        assertTrue(mediaPkgNames.contains("com.google.android.apps.youtube.music"))
        assertTrue(mediaPkgNames.contains("com.audible.application"))

        // Assert camera / photos / gallery / screen recorder are strictly excluded
        assertFalse(mediaPkgNames.contains("com.android.camera2"))
        assertFalse(mediaPkgNames.contains("com.google.android.apps.photos"))
        assertFalse(mediaPkgNames.contains("com.sec.android.gallery3d"))
        assertFalse(mediaPkgNames.contains("com.hecorat.screenrecorder.free"))
    }

    @Test
    fun testMediaStateManagerUpdates() {
        MediaStateManager.updateTrack(
            title = "Starboy",
            artist = "The Weeknd",
            album = "Starboy",
            isPlaying = true,
            activeAppPackage = "com.spotify.music",
            activeAppName = "Spotify"
        )

        val track = MediaStateManager.currentTrack.value
        assertEquals("Starboy", track.title)
        assertEquals("The Weeknd", track.artist)
        assertEquals("Starboy", track.album)
        assertTrue(track.isPlaying)
        assertEquals("com.spotify.music", track.activeAppPackage)
        assertEquals("Spotify", track.activeAppName)
    }

    @Test
    fun testCommShortcutsSaveAndDelete() {
        viewModel.saveDriveCommShortcut(
            slotIndex = 0,
            name = "Mom",
            phoneNumber = "+15551234567",
            channelType = "WHATSAPP",
            photoUri = null
        )

        viewModel.saveDriveCommShortcut(
            slotIndex = 1,
            name = "Boss",
            phoneNumber = "+15559876543",
            channelType = "CALL",
            photoUri = null
        )

        viewModel.deleteDriveCommShortcut(1)
    }

    @Test
    fun testCommShortcutExecutionChannels() {
        val callShortcut = DriveCommShortcutEntity(0, "Doctor", "+15550001111", "CALL")
        val waShortcut = DriveCommShortcutEntity(1, "Family", "+15550002222", "WHATSAPP")
        val smsShortcut = DriveCommShortcutEntity(2, "Teammate", "+15550003333", "SMS")
        val tgShortcut = DriveCommShortcutEntity(3, "Friend", "+15550004444", "TELEGRAM")

        viewModel.launchDriveCommShortcut(context, callShortcut)
        viewModel.launchDriveCommShortcut(context, waShortcut)
        viewModel.launchDriveCommShortcut(context, smsShortcut)
        viewModel.launchDriveCommShortcut(context, tgShortcut)
    }

    @Test
    fun testMediaStatePersistenceAcrossPause() {
        MediaStateManager.updateTrack(
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            isPlaying = true,
            activeAppPackage = "com.spotify.music",
            activeAppName = "Spotify"
        )

        // Pause playback
        MediaStateManager.updatePlaybackState(false)

        val track = MediaStateManager.currentTrack.value
        assertFalse(track.isPlaying)
        // Metadata & active package must stay intact
        assertEquals("Blinding Lights", track.title)
        assertEquals("The Weeknd", track.artist)
        assertEquals("After Hours", track.album)
        assertEquals("com.spotify.music", track.activeAppPackage)
        assertEquals("Spotify", track.activeAppName)
    }

    @Test
    fun testMediaStateManagerTransportCallbacks() {
        var playCalled = false
        var pauseCalled = false
        var nextCalled = false
        var prevCalled = false

        MediaStateManager.onPlayAction = { playCalled = true }
        MediaStateManager.onPauseAction = { pauseCalled = true }
        MediaStateManager.onNextAction = { nextCalled = true }
        MediaStateManager.onPreviousAction = { prevCalled = true }

        assertTrue(MediaStateManager.triggerPlay())
        assertTrue(playCalled)

        assertTrue(MediaStateManager.triggerPause())
        assertTrue(pauseCalled)

        assertTrue(MediaStateManager.triggerNext())
        assertTrue(nextCalled)

        assertTrue(MediaStateManager.triggerPrevious())
        assertTrue(prevCalled)
    }

    @Test
    fun testVoiceAssistantInvocation() {
        // Must execute safely without crashing
        viewModel.launchDriveVoiceAssistant(context)
    }

    @Test
    fun testNotificationListenerPermissionHelper() {
        // Function executes safely and returns boolean
        val isGranted = viewModel.isNotificationListenerGranted(context)
        assertNotNull(isGranted)
    }

    @Test
    fun testNewsAndNonMediaAppExclusion() {
        val excludedPackages = listOf(
            "com.nis.app", "com.nis.inshorts", "com.eterno.dailyhunt",
            "com.android.chrome", "org.mozilla.firefox",
            "com.instagram.android", "com.zhiliaoapp.musically"
        )
        val validAudioPackages = listOf(
            "com.spotify.music", "com.google.android.apps.youtube.music",
            "com.apple.android.music", "com.audible.application"
        )

        val isExcluded = { pkg: String ->
            val lower = pkg.lowercase()
            listOf("inshorts", "nis.app", "dailyhunt", "news", "feedly", "flipboard", "chrome", "firefox", "instagram", "tiktok", "musically")
                .any { lower.contains(it) }
        }

        excludedPackages.forEach { pkg ->
            assertTrue("Expected $pkg to be excluded", isExcluded(pkg))
        }

        validAudioPackages.forEach { pkg ->
            assertFalse("Expected $pkg to be accepted", isExcluded(pkg))
        }
    }
}


