package com.example.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * NotificationListenerService that observes active MediaSessions and Notifications
 * to extract real-time Song Name, Artist, Album, Album Artwork, and Active App Package.
 */
class SiloNotificationListenerService : NotificationListenerService() {

    private var sessionManager: MediaSessionManager? = null
    private val activeControllers = mutableListOf<MediaController>()
    private var currentActiveController: MediaController? = null

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        updateActiveControllers(controllers)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            sessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val componentName = ComponentName(this, SiloNotificationListenerService::class.java)
            sessionManager?.addOnActiveSessionsChangedListener(sessionListener, componentName)
            val initialControllers = sessionManager?.getActiveSessions(componentName)
            updateActiveControllers(initialControllers)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            sessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
            clearCallbacks()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun isExcludedApp(pkg: String?): Boolean {
        if (pkg.isNullOrBlank()) return true
        val lower = pkg.lowercase()
        val excluded = listOf(
            "inshorts", "nis.app", "dailyhunt", "news", "feedly", "flipboard",
            "reddit", "twitter", "x.android", "facebook", "instagram", "threads",
            "tiktok", "musically", "snapchat", "chrome", "firefox", "browser", "opera",
            "camera", "gallery", "photos", "recorder", "screenshot"
        )
        return excluded.any { lower.contains(it) }
    }

    private fun isAudioPlayerPackage(pkg: String?): Boolean {
        if (pkg.isNullOrBlank()) return false
        val lower = pkg.lowercase()
        val audioKeywords = listOf(
            "spotify", "youtube.music", "music", "audio", "podcast", "soundcloud",
            "pandora", "audible", "deezer", "tidal", "apple.android.music", "amazon.mp3",
            "poweramp", "musicolet", "shuttle", "blackplayer", "vlc", "tunein", "iheartradio",
            "pocketcasts", "castbox", "stitcher", "player", "radio", "fm", "sound", "bandcamp"
        )
        return !isExcludedApp(pkg) && audioKeywords.any { lower.contains(it) }
    }

    private fun updateActiveControllers(controllers: List<MediaController>?) {
        clearCallbacks()
        if (controllers.isNullOrEmpty()) return

        activeControllers.addAll(controllers)

        // 1. First priority: Playing controller from genuine non-excluded app
        val playingAudioController = controllers.find {
            it.playbackState?.state == PlaybackState.STATE_PLAYING && !isExcludedApp(it.packageName)
        }

        // 2. Second priority: Matching currently tracked activeAppPackage
        val currentlyTrackedPkg = MediaStateManager.currentTrack.value.activeAppPackage
        val currentAppController = if (!currentlyTrackedPkg.isNullOrBlank()) {
            controllers.find { it.packageName == currentlyTrackedPkg && !isExcludedApp(it.packageName) }
        } else null

        // 3. Third priority: Controller that is an identified audio player app
        val audioAppController = controllers.find { isAudioPlayerPackage(it.packageName) }

        // 4. Fourth priority: Any non-excluded controller
        val nonExcludedController = controllers.find { !isExcludedApp(it.packageName) }

        val targetController = playingAudioController
            ?: currentAppController
            ?: audioAppController
            ?: nonExcludedController

        targetController?.let { controller ->
            bindController(controller)
        }
    }

    private fun bindController(controller: MediaController) {
        if (isExcludedApp(controller.packageName)) return
        currentActiveController = controller
        wireTransportControls(controller)
        extractAndBroadcastMetadata(controller)

        controller.registerCallback(object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) {
                extractAndBroadcastMetadata(controller)
            }

            override fun onPlaybackStateChanged(state: PlaybackState?) {
                extractAndBroadcastMetadata(controller)
            }
        })
    }

    private fun wireTransportControls(controller: MediaController) {
        MediaStateManager.onPlayAction = {
            try {
                controller.transportControls.play()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        MediaStateManager.onPauseAction = {
            try {
                controller.transportControls.pause()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        MediaStateManager.onNextAction = {
            try {
                controller.transportControls.skipToNext()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        MediaStateManager.onPreviousAction = {
            try {
                controller.transportControls.skipToPrevious()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun extractAndBroadcastMetadata(controller: MediaController) {
        try {
            val pkg = controller.packageName
            if (isExcludedApp(pkg)) return

            val metadata = controller.metadata
            val playbackState = controller.playbackState
            val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

            val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                ?: ""
            val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_COMPOSER)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
                ?: ""
            val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)
                ?: ""

            var artBitmap: Bitmap? = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
            val artUri = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_ART_URI)
                ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI)

            val appLabel = try {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
            } catch (e: Exception) {
                pkg.substringAfterLast(".").replaceFirstChar { it.uppercase() }
            }

            if (title.isNotBlank() || isPlaying || !pkg.isNullOrBlank()) {
                MediaStateManager.updateTrack(
                    title = title.ifBlank { "Audio Playing" },
                    artist = artist.ifBlank { appLabel },
                    album = album,
                    isPlaying = isPlaying,
                    activeAppPackage = pkg,
                    activeAppName = appLabel,
                    albumArtBitmap = artBitmap,
                    albumArtUri = artUri
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName
        if (isExcludedApp(pkg)) return

        val notification = sbn.notification ?: return
        val isMedia = notification.category == Notification.CATEGORY_TRANSPORT ||
            notification.extras.containsKey(Notification.EXTRA_MEDIA_SESSION) ||
            notification.extras.containsKey("android.mediaSession")

        if (isMedia) {
            val title = notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: notification.extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()
                ?: ""
            val text = notification.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                ?: notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                ?: notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
                ?: ""

            val appLabel = try {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
            } catch (e: Exception) {
                "Music"
            }

            if (title.isNotBlank()) {
                val largeIcon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notification.getLargeIcon()?.loadDrawable(this)?.let { drawable ->
                        if (drawable is android.graphics.drawable.BitmapDrawable) drawable.bitmap else null
                    }
                } else {
                    @Suppress("DEPRECATION")
                    notification.extras.getParcelable(Notification.EXTRA_LARGE_ICON) as? Bitmap
                }

                MediaStateManager.updateTrack(
                    title = title,
                    artist = text.ifBlank { appLabel },
                    album = "",
                    isPlaying = true,
                    activeAppPackage = pkg,
                    activeAppName = appLabel,
                    albumArtBitmap = largeIcon
                )
            }
        }
    }

    private fun clearCallbacks() {
        activeControllers.clear()
        currentActiveController = null
    }
}


