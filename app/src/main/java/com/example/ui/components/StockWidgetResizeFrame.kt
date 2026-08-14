package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.withTimeout

@Composable
fun StockWidgetResizeFrame(
    widgetId: String,
    isSelected: Boolean,
    isDragging: Boolean,
    widthFraction: Float,
    heightDp: Int,
    onEnterEditMode: () -> Unit,
    onDismissEditMode: () -> Unit,
    onWidthFractionChange: (Float) -> Unit,
    onHeightDpChange: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidthPx = remember(configuration.screenWidthDp, density) {
        with(density) { configuration.screenWidthDp.dp.toPx() }
    }

    var curHeight by remember(heightDp) { mutableIntStateOf(heightDp) }
    var curWidth by remember(widthFraction) { mutableFloatStateOf(widthFraction) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(isSelected) {
                if (!isSelected) {
                    awaitEachGesture {
                        val down = awaitFirstDown(pass = PointerEventPass.Initial)
                        val longPressTimeout = viewConfig.longPressTimeoutMillis
                        try {
                            withTimeout(longPressTimeout) {
                                waitForUpOrCancellation(pass = PointerEventPass.Initial)
                            }
                        } catch (e: PointerEventTimeoutCancellationException) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEnterEditMode()
                            down.consume()
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Floating Quick Action Toolbar (visible when selected)
            AnimatedVisibility(
                visible = isSelected && !isDragging,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E202B),
                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.6f)),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .zIndex(20f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Done / Dismiss button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF10B981),
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDismissEditMode()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        // Delete button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEF4444),
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onRemove()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Widget Card Container with Bounding Box & Handles
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (isSelected) (curWidth * 0.94f).coerceIn(0.35f, 0.94f) else curWidth.coerceIn(0.35f, 1.0f))
                    .height(curHeight.dp)
                    .then(
                        if (isSelected) {
                            Modifier
                                .border(
                                    border = BorderStroke(2.dp, Color(0xFF6366F1)),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .background(
                                    color = Color(0xFF14151B),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(3.dp)
                        } else {
                            Modifier
                        }
                    )
            ) {
                // Actual Widget Content
                content()

                // Touch shield overlay when selected (blocks child views from consuming drag/tap and prevents app launching)
                if (isSelected && !isDragging) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(10f)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    down.consume()
                                    waitForUpOrCancellation()?.consume()
                                }
                            }
                    )
                }

                // 4-Directional Stock Resize Handles (Visible when selected)
                if (isSelected && !isDragging) {
                    // Top Handle (Expanded Touch Hitbox)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-14).dp)
                            .size(width = 64.dp, height = 44.dp)
                            .zIndex(25f)
                            .pointerInput(widgetId) {
                                detectDragGestures(
                                    onDragEnd = { onHeightDpChange(curHeight) }
                                ) { change, dragAmount ->
                                    change.consume()
                                    // Dragging up increases height
                                    val delta = (-dragAmount.y / 1.5f).toInt()
                                    val newH = (curHeight + delta).coerceIn(100, 480)
                                    if (newH != curHeight) {
                                        curHeight = newH
                                    }
                                }
                            }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF6366F1),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(width = 38.dp, height = 14.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 16.dp, height = 2.dp)
                                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }

                    // Bottom Handle (Expanded Touch Hitbox)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = 14.dp)
                            .size(width = 64.dp, height = 44.dp)
                            .zIndex(25f)
                            .pointerInput(widgetId) {
                                detectDragGestures(
                                    onDragEnd = { onHeightDpChange(curHeight) }
                                ) { change, dragAmount ->
                                    change.consume()
                                    // Dragging down increases height
                                    val delta = (dragAmount.y / 1.5f).toInt()
                                    val newH = (curHeight + delta).coerceIn(100, 480)
                                    if (newH != curHeight) {
                                        curHeight = newH
                                    }
                                }
                            }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF6366F1),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(width = 38.dp, height = 14.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 16.dp, height = 2.dp)
                                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }

                    // Left Handle (Expanded Touch Hitbox)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = (-14).dp)
                            .size(width = 48.dp, height = 64.dp)
                            .zIndex(25f)
                            .pointerInput(widgetId) {
                                detectDragGestures(
                                    onDragEnd = { onWidthFractionChange(curWidth) }
                                ) { change, dragAmount ->
                                    change.consume()
                                    // Dragging left increases width, dragging right decreases width
                                    val delta = -dragAmount.x / (screenWidthPx.coerceAtLeast(100f) * 0.75f)
                                    val newW = (curWidth + delta).coerceIn(0.35f, 1.0f)
                                    if (newW != curWidth) {
                                        curWidth = newW
                                    }
                                }
                            }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF6366F1),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(width = 14.dp, height = 38.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 2.dp, height = 16.dp)
                                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }

                    // Right Handle (Expanded Touch Hitbox)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = 14.dp)
                            .size(width = 48.dp, height = 64.dp)
                            .zIndex(25f)
                            .pointerInput(widgetId) {
                                detectDragGestures(
                                    onDragEnd = { onWidthFractionChange(curWidth) }
                                ) { change, dragAmount ->
                                    change.consume()
                                    // Dragging right increases width, dragging left decreases width
                                    val delta = dragAmount.x / (screenWidthPx.coerceAtLeast(100f) * 0.75f)
                                    val newW = (curWidth + delta).coerceIn(0.35f, 1.0f)
                                    if (newW != curWidth) {
                                        curWidth = newW
                                    }
                                }
                            }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF6366F1),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(width = 14.dp, height = 38.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 2.dp, height = 16.dp)
                                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }

                    // 4 Corner Dots
                    CornerDot(modifier = Modifier.align(Alignment.TopStart).offset(x = (-3).dp, y = (-3).dp))
                    CornerDot(modifier = Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = (-3).dp))
                    CornerDot(modifier = Modifier.align(Alignment.BottomStart).offset(x = (-3).dp, y = 3.dp))
                    CornerDot(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 3.dp, y = 3.dp))
                }
            }
        }
    }
}

@Composable
private fun CornerDot(modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = Color(0xFF818CF8),
        modifier = modifier
            .size(8.dp)
            .zIndex(16f)
            .border(1.dp, Color.White, CircleShape)
    ) {}
}
