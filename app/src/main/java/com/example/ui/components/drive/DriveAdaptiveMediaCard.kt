package com.example.ui.components.drive

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppInfo
import com.example.ui.DriveMediaTrackInfo
import com.example.util.toImageBitmapSafe

/**
 * Modern Minimalist Adaptive Media Controller for Drive Mode
 * - Stays visible even when paused if background audio session exists
 * - Opens the EXACT app currently playing/paused upon tapping badge or card
 * - Real-time metadata, artwork bitmap, and tactile transport controls
 */
@Composable
fun DriveAdaptiveMediaCard(
    trackInfo: DriveMediaTrackInfo,
    mediaApps: List<AppInfo>,
    onLaunchMediaApp: (AppInfo) -> Unit,
    onMediaKeyClick: (Int) -> Unit,
    onOpenCurrentMediaApp: () -> Unit,
    onEnableNotificationAccess: (() -> Unit)? = null,
    isNotificationAccessGranted: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val hasActivePackage = !trackInfo.activeAppPackage.isNullOrBlank()
    val hasValidTitle = trackInfo.title.isNotBlank() && trackInfo.title != "No media playing"
    val isPlayingOrActive = trackInfo.isPlaying || hasActivePackage || hasValidTitle

    // Resolve active app icon drawable
    val activeAppIconBitmap = remember(trackInfo.activeAppPackage, mediaApps) {
        val pkg = trackInfo.activeAppPackage
        if (!pkg.isNullOrBlank()) {
            val matchingApp = mediaApps.find { it.packageName == pkg }
            matchingApp?.iconDrawable?.toImageBitmapSafe() ?: try {
                context.packageManager.getApplicationIcon(pkg).toImageBitmapSafe()
            } catch (e: Throwable) {
                null
            }
        } else {
            null
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141722)),
        border = BorderStroke(1.dp, Color(0xFF252C3D)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("drive_adaptive_media_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar: Mode Label + Active Player App Badge Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = if (trackInfo.isPlaying) Color(0xFF10B981) else Color(0xFF94A3B8)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = if (trackInfo.isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (trackInfo.isPlaying) "NOW PLAYING" else if (isPlayingOrActive) "PAUSED" else "AUDIO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (trackInfo.isPlaying) Color(0xFF6EE7B7) else Color(0xFFCBD5E1),
                        letterSpacing = 1.sp
                    )
                }

                // Active App Icon & Name Badge (Tapping opens the EXACT app that is playing / owning session)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E2536),
                    border = BorderStroke(1.dp, Color(0xFF2F3B54)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenCurrentMediaApp() }
                        .testTag("drive_media_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeAppIconBitmap != null) {
                            Image(
                                bitmap = activeAppIconBitmap,
                                contentDescription = trackInfo.activeAppName,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (trackInfo.isPlaying) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = trackInfo.activeAppName.ifBlank { "Music" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Notification Access Helper banner if access not enabled yet
            if (!isNotificationAccessGranted && onEnableNotificationAccess != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E2638),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onEnableNotificationAccess() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grant notification access to display live track info & album art",
                            fontSize = 11.sp,
                            color = Color(0xFF93C5FD),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (isPlayingOrActive) {
                // ACTIVE / PAUSED STATE: Artwork + Song Details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenCurrentMediaApp() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Album Artwork
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF1A1F2C))
                            .border(1.dp, Color(0xFF2E384D), RoundedCornerShape(16.dp))
                    ) {
                        if (trackInfo.albumArtBitmap != null) {
                            Image(
                                bitmap = trackInfo.albumArtBitmap.asImageBitmap(),
                                contentDescription = trackInfo.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF0F766E), Color(0xFF10B981))
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Track Title, Artist, & Album
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trackInfo.title.ifBlank { "Playing Audio" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (trackInfo.album.isNotBlank()) "${trackInfo.artist} • ${trackInfo.album}" else trackInfo.artist.ifBlank { trackInfo.activeAppName },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Stretched Full-Width Transport Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Previous Track Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1A202E),
                        border = BorderStroke(1.dp, Color(0xFF2A3449)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onMediaKeyClick(KeyEvent.KEYCODE_MEDIA_PREVIOUS) }
                            .testTag("drive_media_prev_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Center Primary Play/Pause Pill Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (trackInfo.isPlaying) Color(0xFF10B981) else Color(0xFF2563EB),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onMediaKeyClick(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) }
                            .testTag("drive_music_play_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (trackInfo.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Next Track Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1A202E),
                        border = BorderStroke(1.dp, Color(0xFF2A3449)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onMediaKeyClick(KeyEvent.KEYCODE_MEDIA_NEXT) }
                            .testTag("drive_media_next_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Below Row: Clean horizontal list of music apps as icons only
            if (mediaApps.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "MUSIC & AUDIO APPS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.45f),
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        mediaApps.forEach { app ->
                            val isCurrentlyActiveApp = trackInfo.activeAppPackage == app.packageName
                            MusicAppIconTile(
                                appInfo = app,
                                isHighlighted = isCurrentlyActiveApp,
                                onClick = { onLaunchMediaApp(app) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MusicAppIconTile(
    appInfo: AppInfo,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageBitmap = remember(appInfo) {
        appInfo.iconDrawable?.toImageBitmapSafe() ?: try {
            context.packageManager.getApplicationIcon(appInfo.packageName).toImageBitmapSafe()
        } catch (e: Throwable) {
            null
        }
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isHighlighted) Color(0xFF1E2A24) else Color(0xFF181C28),
        border = BorderStroke(
            1.dp,
            if (isHighlighted) Color(0xFF10B981) else Color(0xFF283042)
        ),
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("media_app_icon_${appInfo.packageName}")
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = appInfo.label,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = appInfo.label,
                    tint = if (isHighlighted) Color(0xFF10B981) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

