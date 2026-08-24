package com.example.ui.components.drive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Section 5: Persistent Bottom Dock & Voice Trigger
 * Fixed horizontal footer with four standard primary targets:
 *  [Home], [Map], [Media], [Phone]
 * and dedicated Voice Assistant Trigger FAB.
 */
@Composable
fun DriveBottomDock(
    onHomeClick: () -> Unit,
    onMapClick: () -> Unit,
    onMediaClick: () -> Unit,
    onPhoneClick: () -> Unit,
    onVoiceAssistantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Horizontal Dock Bar Surface
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF141720),
            border = BorderStroke(1.dp, Color(0xFF282D3C)),
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("drive_bottom_dock")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // [Home]
                DockItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = true,
                    onClick = onHomeClick,
                    tag = "drive_dock_home"
                )

                // [Map]
                DockItem(
                    icon = Icons.Default.Navigation,
                    label = "Map",
                    isSelected = false,
                    onClick = onMapClick,
                    tag = "drive_dock_map"
                )

                // Center Spacer for Voice FAB
                Spacer(modifier = Modifier.size(54.dp))

                // [Media]
                DockItem(
                    icon = Icons.Default.MusicNote,
                    label = "Media",
                    isSelected = false,
                    onClick = onMediaClick,
                    tag = "drive_dock_media"
                )

                // [Phone]
                DockItem(
                    icon = Icons.Default.Call,
                    label = "Phone",
                    isSelected = false,
                    onClick = onPhoneClick,
                    tag = "drive_dock_phone"
                )
            }
        }

        // Floating Voice Assistant Action Button (Centered & Elevated)
        FloatingActionButton(
            onClick = onVoiceAssistantClick,
            shape = CircleShape,
            containerColor = Color(0xFFFF9800),
            contentColor = Color.Black,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
            modifier = Modifier
                .offset(y = (-18).dp)
                .size(58.dp)
                .testTag("drive_voice_assistant_fab")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFB74D), Color(0xFFFF9800))
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Assistant",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFFFF9800) else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFFFF9800) else Color.White.copy(alpha = 0.7f)
        )
    }
}
