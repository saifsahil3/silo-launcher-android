package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LauncherMode
import com.example.service.PassThroughOverlayService
import com.example.util.PassThroughManager

@Composable
fun ModeSelectorHeader(
    currentMode: LauncherMode,
    onModeSelected: (LauncherMode) -> Unit,
    batteryLevel: Int,
    isCharging: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var showVerticalOverlay by remember { mutableStateOf(false) }

    val modeColor = when (currentMode) {
        LauncherMode.FOCUS -> Color(0xFF7B1FA2)
        LauncherMode.ALL_APPS -> Color(0xFF7B1FA2)
        LauncherMode.DRIVE -> Color(0xFFE65100)
        LauncherMode.SLEEP -> Color(0xFF283593)
        LauncherMode.E_PAPER -> Color(0xFF8D6E63)
        LauncherMode.PASS_THROUGH -> Color(0xFF00695C)
        LauncherMode.CREATOR -> Color(0xFF3F51B5)
    }

    val modeIcon = when (currentMode) {
        LauncherMode.FOCUS -> Icons.Default.Psychology
        LauncherMode.ALL_APPS -> Icons.Default.GridView
        LauncherMode.DRIVE -> Icons.Default.DirectionsCar
        LauncherMode.SLEEP -> Icons.Default.Bedtime
        LauncherMode.E_PAPER -> Icons.AutoMirrored.Filled.MenuBook
        LauncherMode.PASS_THROUGH -> Icons.Default.Layers
        LauncherMode.CREATOR -> Icons.Default.VideoCall
    }

    val modeTitle = when (currentMode) {
        LauncherMode.FOCUS -> "Focus"
        LauncherMode.ALL_APPS -> "All Apps"
        LauncherMode.DRIVE -> "Drive"
        LauncherMode.SLEEP -> "Sleep"
        LauncherMode.E_PAPER -> "E-Paper"
        LauncherMode.PASS_THROUGH -> "Pass-Through"
        LauncherMode.CREATOR -> "Creator"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Single compact Mode Icon Button that expands
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .testTag("mode_selector_single_icon")
                        .clip(RoundedCornerShape(24.dp))
                        .clickable {
                            if (!isExpanded) {
                                isExpanded = true
                            } else {
                                showVerticalOverlay = !showVerticalOverlay
                            }
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        // Current Mode Icon Indicator
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(modeColor)
                        ) {
                            Icon(
                                imageVector = modeIcon,
                                contentDescription = modeTitle,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Expanded State content
                        AnimatedVisibility(visible = isExpanded) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = modeTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Swap Icon
                                Surface(
                                    shape = CircleShape,
                                    color = modeColor.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            showVerticalOverlay = !showVerticalOverlay
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.SwapVert,
                                            contentDescription = "Swap Mode",
                                            tint = modeColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Header Right Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Small Vertical Overlay of Mode Icons
            AnimatedVisibility(
                visible = showVerticalOverlay,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 12.dp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(180.dp)
                        .border(
                            width = 1.dp,
                            color = modeColor.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ModeItemOption(
                            mode = LauncherMode.FOCUS,
                            currentMode = currentMode,
                            icon = Icons.Default.Psychology,
                            label = "Focus",
                            accentColor = Color(0xFF7B1FA2),
                            onSelect = {
                                showVerticalOverlay = false
                                isExpanded = false
                                onModeSelected(LauncherMode.FOCUS)
                            }
                        )

                        ModeItemOption(
                            mode = LauncherMode.DRIVE,
                            currentMode = currentMode,
                            icon = Icons.Default.DirectionsCar,
                            label = "Drive",
                            accentColor = Color(0xFFE65100),
                            onSelect = {
                                showVerticalOverlay = false
                                isExpanded = false
                                onModeSelected(LauncherMode.DRIVE)
                            }
                        )

                        ModeItemOption(
                            mode = LauncherMode.SLEEP,
                            currentMode = currentMode,
                            icon = Icons.Default.Bedtime,
                            label = "Sleep",
                            accentColor = Color(0xFF283593),
                            onSelect = {
                                showVerticalOverlay = false
                                isExpanded = false
                                onModeSelected(LauncherMode.SLEEP)
                            }
                        )

                        ModeItemOption(
                            mode = LauncherMode.E_PAPER,
                            currentMode = currentMode,
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            label = "E-Paper",
                            accentColor = Color(0xFF8D6E63),
                            onSelect = {
                                showVerticalOverlay = false
                                isExpanded = false
                                onModeSelected(LauncherMode.E_PAPER)
                            }
                        )

                        ModeItemOption(
                            mode = LauncherMode.PASS_THROUGH,
                            currentMode = currentMode,
                            icon = Icons.Default.Layers,
                            label = "Pass-Through",
                            accentColor = Color(0xFF00695C),
                            onSelect = {
                                showVerticalOverlay = false
                                isExpanded = false
                                onModeSelected(LauncherMode.PASS_THROUGH)
                                // Directly launch stock launcher pass-through
                                PassThroughManager.setPassThroughActive(context, true)
                                if (!android.provider.Settings.canDrawOverlays(context)) {
                                    try {
                                        val intent = android.content.Intent(
                                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            android.net.Uri.parse("package:${context.packageName}")
                                        ).apply {
                                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Throwable) {
                                        e.printStackTrace()
                                    }
                                }
                                PassThroughOverlayService.startService(context)
                                PassThroughManager.launchPassThroughLauncher(context)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeItemOption(
    mode: LauncherMode,
    currentMode: LauncherMode,
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onSelect: () -> Unit
) {
    val isSelected = mode == currentMode

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor else accentColor.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Color.White else accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

