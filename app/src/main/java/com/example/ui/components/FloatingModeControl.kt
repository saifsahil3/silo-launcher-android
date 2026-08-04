package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LauncherMode
import com.example.service.PassThroughOverlayService
import com.example.util.PassThroughManager
import kotlin.math.roundToInt

enum class FloatingMenuState {
    CLOSED,
    QUICK_ACTIONS,
    MODE_LIST
}

@Composable
fun FloatingModeControl(
    currentMode: LauncherMode,
    onModeSelected: (LauncherMode) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuState by remember { mutableStateOf(FloatingMenuState.CLOSED) }

    // Drag offset support for Pass-Through mode
    val isDraggable = currentMode == LauncherMode.PASS_THROUGH
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(currentMode) {
        offsetX = 0f
        offsetY = 0f
    }

    val modeColor = when (currentMode) {
        LauncherMode.NORMAL -> Color(0xFF2E7D32)
        LauncherMode.FOCUS -> Color(0xFF7B1FA2)
        LauncherMode.DRIVE -> Color(0xFFE65100)
        LauncherMode.SLEEP -> Color(0xFF283593)
        LauncherMode.E_PAPER -> Color(0xFF8D6E63)
        LauncherMode.PASS_THROUGH -> Color(0xFF00695C)
    }

    val modeIcon = when (currentMode) {
        LauncherMode.NORMAL -> Icons.Default.GridView
        LauncherMode.FOCUS -> Icons.Default.Psychology
        LauncherMode.DRIVE -> Icons.Default.DirectionsCar
        LauncherMode.SLEEP -> Icons.Default.Bedtime
        LauncherMode.E_PAPER -> Icons.AutoMirrored.Filled.MenuBook
        LauncherMode.PASS_THROUGH -> Icons.Default.Layers
    }

    Box(
        modifier = modifier
            .padding(16.dp)
            .then(
                if (isDraggable) {
                    Modifier.offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom
        ) {
            // STAGE 2: Mode Selection List Card
            AnimatedVisibility(
                visible = menuState == FloatingMenuState.MODE_LIST,
                enter = fadeIn() + scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut() + scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E1E24),
                    tonalElevation = 8.dp,
                    shadowElevation = 16.dp,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, modeColor.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .width(260.dp)
                        .padding(bottom = 12.dp)
                        .testTag("floating_mode_menu_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = null,
                                    tint = modeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Select Mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            IconButton(
                                onClick = { menuState = FloatingMenuState.CLOSED },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("floating_close_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        FloatingModeOption(
                            mode = LauncherMode.NORMAL,
                            currentMode = currentMode,
                            icon = Icons.Default.GridView,
                            label = "Normal",
                            accentColor = Color(0xFF2E7D32),
                            onSelect = {
                                menuState = FloatingMenuState.CLOSED
                                onModeSelected(LauncherMode.NORMAL)
                            }
                        )

                        FloatingModeOption(
                            mode = LauncherMode.FOCUS,
                            currentMode = currentMode,
                            icon = Icons.Default.Psychology,
                            label = "Focus",
                            accentColor = Color(0xFF7B1FA2),
                            onSelect = {
                                menuState = FloatingMenuState.CLOSED
                                onModeSelected(LauncherMode.FOCUS)
                            }
                        )

                        FloatingModeOption(
                            mode = LauncherMode.DRIVE,
                            currentMode = currentMode,
                            icon = Icons.Default.DirectionsCar,
                            label = "Drive",
                            accentColor = Color(0xFFE65100),
                            onSelect = {
                                menuState = FloatingMenuState.CLOSED
                                onModeSelected(LauncherMode.DRIVE)
                            }
                        )

                        FloatingModeOption(
                            mode = LauncherMode.SLEEP,
                            currentMode = currentMode,
                            icon = Icons.Default.Bedtime,
                            label = "Sleep",
                            accentColor = Color(0xFF283593),
                            onSelect = {
                                menuState = FloatingMenuState.CLOSED
                                onModeSelected(LauncherMode.SLEEP)
                            }
                        )

                        FloatingModeOption(
                            mode = LauncherMode.E_PAPER,
                            currentMode = currentMode,
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            label = "E-Paper",
                            accentColor = Color(0xFF8D6E63),
                            onSelect = {
                                menuState = FloatingMenuState.CLOSED
                                onModeSelected(LauncherMode.E_PAPER)
                            }
                        )

                        FloatingModeOption(
                            mode = LauncherMode.PASS_THROUGH,
                            currentMode = currentMode,
                            icon = Icons.Default.Layers,
                            label = "Pass-Through",
                            accentColor = Color(0xFF00695C),
                            onSelect = {
                                menuState = FloatingMenuState.CLOSED
                                onModeSelected(LauncherMode.PASS_THROUGH)
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

            // STAGE 1: Quick Actions Row (Swap Mode & Settings)
            AnimatedVisibility(
                visible = menuState == FloatingMenuState.QUICK_ACTIONS,
                enter = fadeIn() + scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut() + scaleOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF1E1E24),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, modeColor.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .testTag("floating_quick_actions")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Swap Mode Button
                        Surface(
                            shape = CircleShape,
                            color = modeColor.copy(alpha = 0.2f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { menuState = FloatingMenuState.MODE_LIST }
                                .testTag("quick_action_swap_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = "Swap Mode",
                                    tint = modeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Swap",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Settings Button
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    menuState = FloatingMenuState.CLOSED
                                    onOpenSettings()
                                }
                                .testTag("quick_action_settings_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Settings",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            var dragDistance by remember { mutableFloatStateOf(0f) }

            // Primary Floating Action Button
            FloatingActionButton(
                onClick = {
                    if (dragDistance < 10f) {
                        menuState = if (menuState == FloatingMenuState.CLOSED) {
                            FloatingMenuState.QUICK_ACTIONS
                        } else {
                            FloatingMenuState.CLOSED
                        }
                    }
                },
                containerColor = modeColor,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp, 12.dp),
                modifier = Modifier
                    .size(56.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    .then(
                        if (isDraggable) {
                            Modifier.pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { dragDistance = 0f },
                                    onDragEnd = {
                                        if (dragDistance < 10f) {
                                            menuState = if (menuState == FloatingMenuState.CLOSED) {
                                                FloatingMenuState.QUICK_ACTIONS
                                            } else {
                                                FloatingMenuState.CLOSED
                                            }
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragDistance += kotlin.math.abs(dragAmount.x) + kotlin.math.abs(dragAmount.y)
                                        offsetX += dragAmount.x
                                        offsetY += dragAmount.y
                                    }
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
                    .testTag("floating_mode_button")
            ) {
                Icon(
                    imageVector = if (menuState != FloatingMenuState.CLOSED) Icons.Default.Close else modeIcon,
                    contentDescription = "Mode Control",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun FloatingModeOption(
    mode: LauncherMode,
    currentMode: LauncherMode,
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onSelect: () -> Unit
) {
    val isSelected = mode == currentMode

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.25f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelect() }
            .padding(vertical = 2.dp)
            .testTag("floating_mode_option_${mode.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) accentColor else accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
