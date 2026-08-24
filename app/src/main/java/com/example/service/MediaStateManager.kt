package com.example.service

import android.graphics.Bitmap
import com.example.ui.DriveMediaTrackInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton repository holding real-time media track metadata, playback state, and album art.
 */
object MediaStateManager {
    private val _currentTrack = MutableStateFlow(DriveMediaTrackInfo())
    val currentTrack: StateFlow<DriveMediaTrackInfo> = _currentTrack.asStateFlow()

    // Active controller action callbacks
    var onPlayAction: (() -> Unit)? = null
    var onPauseAction: (() -> Unit)? = null
    var onNextAction: (() -> Unit)? = null
    var onPreviousAction: (() -> Unit)? = null

    fun updateTrack(
        title: String,
        artist: String,
        album: String = "",
        isPlaying: Boolean,
        activeAppPackage: String?,
        activeAppName: String,
        albumArtBitmap: Bitmap? = null,
        albumArtUri: String? = null
    ) {
        val prev = _currentTrack.value

        val resolvedTitle = if (title.isNotBlank()) title else prev.title.takeIf { it != "No media playing" } ?: "Playing Audio"
        val resolvedArtist = if (artist.isNotBlank()) artist else prev.artist.takeIf { it != "Tap an app below to play" } ?: activeAppName
        val resolvedAlbum = if (album.isNotBlank()) album else prev.album
        val resolvedPackage = activeAppPackage ?: prev.activeAppPackage
        val resolvedAppName = if (activeAppName.isNotBlank()) activeAppName else prev.activeAppName
        val resolvedArt = albumArtBitmap ?: if (activeAppPackage == prev.activeAppPackage) prev.albumArtBitmap else null
        val resolvedArtUri = albumArtUri ?: if (activeAppPackage == prev.activeAppPackage) prev.albumArtUri else null

        _currentTrack.value = DriveMediaTrackInfo(
            title = resolvedTitle,
            artist = resolvedArtist,
            album = resolvedAlbum,
            isPlaying = isPlaying,
            activeAppPackage = resolvedPackage,
            activeAppName = resolvedAppName.ifBlank { "Music" },
            albumArtBitmap = resolvedArt,
            albumArtUri = resolvedArtUri
        )
    }

    fun updatePlaybackState(isPlaying: Boolean) {
        _currentTrack.value = _currentTrack.value.copy(isPlaying = isPlaying)
    }

    fun triggerPlay(): Boolean {
        return onPlayAction?.let {
            it.invoke()
            updatePlaybackState(true)
            true
        } ?: false
    }

    fun triggerPause(): Boolean {
        return onPauseAction?.let {
            it.invoke()
            updatePlaybackState(false)
            true
        } ?: false
    }

    fun triggerNext(): Boolean {
        return onNextAction?.let {
            it.invoke()
            true
        } ?: false
    }

    fun triggerPrevious(): Boolean {
        return onPreviousAction?.let {
            it.invoke()
            true
        } ?: false
    }
}

