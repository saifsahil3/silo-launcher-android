package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.ui.components.engine.AttachedObject
import com.example.ui.components.engine.LevelGenerator
import com.example.ui.components.engine.RopePhysicsEngine
import com.example.ui.components.engine.RopeVectorRenderer.drawAttachedObject
import com.example.ui.components.engine.RopeVectorRenderer.drawCompletionEffect
import com.example.ui.components.engine.RopeVectorRenderer.drawRope
import kotlinx.coroutines.isActive

private const val PREFS_NAME = "silo_prefs"
private const val KEY_FIDGET_LEVEL = "fidget_game_level"

fun getSavedFidgetLevel(context: Context): Int {
    return try {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val level = prefs.getInt(KEY_FIDGET_LEVEL, 1)
        if (level <= 0) 1 else level
    } catch (e: Throwable) {
        1
    }
}

fun saveFidgetLevel(context: Context, level: Int) {
    try {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_FIDGET_LEVEL, if (level <= 0) 1 else level).apply()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

enum class LevelTransitionState {
    PLAYING,
    TRANSITION_OUT,
    TRANSITION_IN
}

@Composable
fun RopeFidgetWidget(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var currentLevelId by remember { mutableIntStateOf(getSavedFidgetLevel(context)) }
    val currentLevel = remember(currentLevelId) { LevelGenerator.getLevel(currentLevelId) }

    val physicsEngine = remember { RopePhysicsEngine() }
    val velocityTracker = remember { VelocityTracker() }

    var transitionState by remember { mutableStateOf(LevelTransitionState.PLAYING) }

    // Frame clock ticker for 60-120 FPS animation
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        physicsEngine.loadLevel(
            level = currentLevel,
            startX = 40f,
            startY = 100f,
            availableWidth = 800f
        )

        var lastFrameNanos = 0L
        var transitionTimeSec = 0f

        while (isActive) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos > 0L) {
                    val dtSec = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0.005f, 0.033f)

                    when (transitionState) {
                        LevelTransitionState.PLAYING -> {
                            physicsEngine.update(dtSec)
                            if (physicsEngine.isLevelCompleted) {
                                triggerHaptic(context, isThud = true)
                                transitionState = LevelTransitionState.TRANSITION_OUT
                                transitionTimeSec = 0f
                            }
                        }

                        LevelTransitionState.TRANSITION_OUT -> {
                            transitionTimeSec += dtSec
                            // Apply physics pile up force towards right dump
                            physicsEngine.applyPileUpToRight(availableWidth = 800f)

                            // Smooth fade out over 0.45 seconds
                            val progress = (transitionTimeSec / 0.45f).coerceIn(0f, 1f)
                            physicsEngine.levelAlpha = 1.0f - progress

                            if (progress >= 1.0f) {
                                // Switch to next level and preserve progress
                                val nextLevelId = currentLevelId + 1
                                currentLevelId = nextLevelId
                                saveFidgetLevel(context, nextLevelId)
                                val nextLevel = LevelGenerator.getLevel(nextLevelId)
                                physicsEngine.loadLevel(
                                    level = nextLevel,
                                    startX = 40f,
                                    startY = 100f,
                                    availableWidth = 800f
                                )
                                physicsEngine.levelAlpha = 0f
                                transitionState = LevelTransitionState.TRANSITION_IN
                                transitionTimeSec = 0f
                            }
                        }

                        LevelTransitionState.TRANSITION_IN -> {
                            transitionTimeSec += dtSec
                            physicsEngine.update(dtSec)

                            // Smooth fade in over 0.35 seconds
                            val progress = (transitionTimeSec / 0.35f).coerceIn(0f, 1f)
                            physicsEngine.levelAlpha = progress

                            if (progress >= 1.0f) {
                                physicsEngine.levelAlpha = 1.0f
                                transitionState = LevelTransitionState.PLAYING
                            }
                        }
                    }
                }
                lastFrameNanos = frameNanos
                frameTick = frameNanos
            }
        }
    }

    val objectBitmaps = remember(context) {
        val map = mutableMapOf<com.example.ui.components.engine.AttachedObject, androidx.compose.ui.graphics.ImageBitmap>()
        com.example.ui.components.engine.AttachedObject.values().forEach { obj ->
            val resName = when (obj) {
                com.example.ui.components.engine.AttachedObject.SHIRT -> "shirt"
                com.example.ui.components.engine.AttachedObject.LAMP -> "lamp"
                com.example.ui.components.engine.AttachedObject.ANCHOR -> "anchor"
                com.example.ui.components.engine.AttachedObject.KEY -> "key"
                com.example.ui.components.engine.AttachedObject.BUTTON -> "button"
                com.example.ui.components.engine.AttachedObject.FEATHER -> "feather"
                com.example.ui.components.engine.AttachedObject.SOCK -> "sock"
                com.example.ui.components.engine.AttachedObject.HEADPHONES -> "headphones"
                com.example.ui.components.engine.AttachedObject.SHOE -> "shoe"
                com.example.ui.components.engine.AttachedObject.PADLOCK -> "padlock"
                com.example.ui.components.engine.AttachedObject.BUCKET -> "bucket"
                com.example.ui.components.engine.AttachedObject.SMALL_BELL -> "bell"
                com.example.ui.components.engine.AttachedObject.PLUG -> "plug"
                com.example.ui.components.engine.AttachedObject.LOCK -> "lock"
                com.example.ui.components.engine.AttachedObject.SWEATER -> "sweater"
            }
            var resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
            if (resId == 0 && obj == com.example.ui.components.engine.AttachedObject.LOCK) {
                resId = context.resources.getIdentifier("padlock", "drawable", context.packageName)
            }
            if (resId != 0) {
                try {
                    val bmp = BitmapFactory.decodeResource(context.resources, resId)
                    if (bmp != null) {
                        map[obj] = bmp.asImageBitmap()
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
        map
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111115))
            .pointerInput(currentLevelId, transitionState) {
                if (transitionState != LevelTransitionState.PLAYING) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        velocityTracker.resetTracking()
                        val nodeIdx = physicsEngine.grabNearestNode(offset.x, offset.y)
                        if (nodeIdx >= 0) {
                            triggerHaptic(context, isThud = false)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        velocityTracker.addPosition(change.uptimeMillis, change.position)
                        physicsEngine.updateTouchTarget(
                            touchX = change.position.x,
                            touchY = change.position.y,
                            deltaX = dragAmount.x
                        )
                    },
                    onDragEnd = {
                        val velocity = velocityTracker.calculateVelocity()
                        physicsEngine.releaseTouch(velocity.x, velocity.y)
                    },
                    onDragCancel = {
                        physicsEngine.releaseTouch(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // UI Level Info Overlay (Minimalist Header)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LEVEL ${currentLevel.id}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.5.sp
                )

                // Subtle Reset Button (Resets back to Level 1)
                IconButton(
                    onClick = {
                        triggerHaptic(context, isThud = false)
                        currentLevelId = 1
                        saveFidgetLevel(context, 1)
                        transitionState = LevelTransitionState.PLAYING
                        physicsEngine.levelAlpha = 1.0f
                        val level1 = LevelGenerator.getLevel(1)
                        physicsEngine.loadLevel(
                            level = level1,
                            startX = 40f,
                            startY = 100f,
                            availableWidth = 800f
                        )
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset to Level 1",
                        tint = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Custom Mini-Engine Canvas Render
        Canvas(modifier = Modifier.fillMaxSize()) {
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTick

            // Sync physics ground floor level with visible bottom margin
            physicsEngine.groundY = size.height - 14.dp.toPx()

            // 0. Draw subtle ground floor indicator line
            drawLine(
                color = Color.White.copy(alpha = 0.08f * physicsEngine.levelAlpha),
                start = androidx.compose.ui.geometry.Offset(12.dp.toPx(), physicsEngine.groundY),
                end = androidx.compose.ui.geometry.Offset(size.width - 12.dp.toPx(), physicsEngine.groundY),
                strokeWidth = 1.dp.toPx()
            )

            // 1. Draw Verlet Rope Path
            drawRope(engine = physicsEngine, material = currentLevel.ropeMaterial)

            // 2. Draw Attached Object (Bitmap asset if available, else procedural shape)
            val discoveryProgress = physicsEngine.getDiscoveryProgress()
            val isCompleted = physicsEngine.isLevelCompleted

            drawAttachedObject(
                engine = physicsEngine,
                objType = currentLevel.objectType,
                discoveryProgress = discoveryProgress,
                isCompleted = isCompleted,
                objectBitmap = objectBitmaps[currentLevel.objectType]
            )

            // 3. Visual Completion Ripple Flash (NO SOUND)
            val completionProgress = physicsEngine.getCompletionProgress()
            drawCompletionEffect(engine = physicsEngine, progress = completionProgress)
        }
    }
}

fun triggerHaptic(context: Context, isThud: Boolean = false) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val effectId = if (isThud && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    VibrationEffect.Composition.PRIMITIVE_THUD
                } else {
                    VibrationEffect.Composition.PRIMITIVE_CLICK
                }
                val composition = VibrationEffect.startComposition().addPrimitive(effectId).compose()
                vibrator.vibrate(composition)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(if (isThud) 50L else 10L)
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

