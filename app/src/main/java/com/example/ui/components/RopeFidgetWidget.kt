package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun RopeFidgetWidget(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentLevel by remember { mutableStateOf<FidgetLevel>(FidgetLevel.Level1) }
    var currentLockedThreshold by remember { mutableFloatStateOf(-1f) }
    val resolvedKnots = remember { mutableStateListOf<Float>() }

    var accumulatedDistance by remember { mutableFloatStateOf(0f) }
    var lastHapticDistance by remember { mutableFloatStateOf(0f) }

    val knotThresholds = listOf(0.3f, 0.6f, 0.9f)

    val updatedLevel by rememberUpdatedState(currentLevel)
    val updatedLockedThreshold by rememberUpdatedState(currentLockedThreshold)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        if (updatedLockedThreshold > 0f) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            if (abs(offset.x - centerX) < 120f && abs(offset.y - centerY) < 120f) {
                                resolvedKnots.add(updatedLockedThreshold)
                                currentLockedThreshold = -1f
                                triggerHaptic(context, true)
                            }
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (updatedLockedThreshold < 0f) {
                            val targetDist = max(0f, accumulatedDistance - updatedLevel.totalLengthPx * 0.05f)
                            coroutineScope.launch {
                                val anim = Animatable(accumulatedDistance)
                                anim.animateTo(targetDist, tween(300)) {
                                    accumulatedDistance = value
                                }
                            }
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (updatedLockedThreshold > 0f) return@detectHorizontalDragGestures

                        if (dragAmount > 0) {
                            var newDist = accumulatedDistance + dragAmount

                            for (t in knotThresholds) {
                                val tDist = updatedLevel.totalLengthPx * t
                                if (accumulatedDistance < tDist && newDist >= tDist) {
                                    if (!resolvedKnots.contains(t)) {
                                        newDist = tDist
                                        currentLockedThreshold = t
                                        triggerHaptic(context, true)
                                        break
                                    }
                                }
                            }

                            accumulatedDistance = newDist

                            // Micro haptic click every 50px
                            if (newDist - lastHapticDistance >= 50f) {
                                triggerHaptic(context, false)
                                lastHapticDistance = newDist - (newDist % 50f)
                            }

                            // Level completion check
                            if (newDist >= updatedLevel.totalLengthPx) {
                                triggerHaptic(context, true)
                                currentLevel = FidgetLevel.getNextLevel(updatedLevel)
                                resolvedKnots.clear()
                                lastHapticDistance = 0f
                                accumulatedDistance = 0f
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val d = accumulatedDistance
        val L = currentLevel.totalLengthPx

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val centerY = h / 2f

            // Calculate where the rope tip & object are dynamically located
            val itemDistVisible = 600f
            val itemProgress = ((d - (L - itemDistVisible)) / itemDistVisible).coerceIn(0f, 1f)
            val endX = w - 40.dp.toPx() - ((1f - itemProgress) * 50.dp.toPx())
            val strokePx = currentLevel.strokeWidthDp.dp.toPx()

            // 1. Draw Wavy Main Line Path
            val path = Path()
            path.moveTo(0f, centerY)

            val steps = 40
            val dx = max(1f, endX / steps)
            val phase = -d * 0.03f

            for (i in 1..steps) {
                val x = i * dx
                val normX = (x / endX).coerceIn(0f, 1f)

                // Physical curves & sag
                val sag = sin(normX * Math.PI.toFloat()) * 12f
                val ripple1 = sin(normX * 14f + phase) * 5f
                val ripple2 = cos(normX * 7f - phase) * 2.5f

                val y = centerY + sag + ripple1 + ripple2
                path.lineTo(x, y)
            }

            // Dynamic Sliding Gradient Texture
            val baseColor = currentLevel.lineColor
            val lightColor = baseColor.copy(alpha = 0.95f)
            val darkColor = Color(
                red = (baseColor.red * 0.65f),
                green = (baseColor.green * 0.65f),
                blue = (baseColor.blue * 0.65f),
                alpha = baseColor.alpha
            )

            val textureOffset = (d * 1.2f) % 80f
            val ropeGradient = Brush.linearGradient(
                colors = listOf(
                    baseColor,
                    lightColor,
                    darkColor,
                    baseColor
                ),
                start = Offset(textureOffset, 0f),
                end = Offset(textureOffset + 80f, 0f),
                tileMode = TileMode.Repeated
            )

            drawPath(
                path = path,
                brush = ropeGradient,
                style = Stroke(
                    width = strokePx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // 2. Fluid & Dynamic Continuous Bezier Coiling Pile Stack
            val maxCoils = 16
            val coilsCount = ((d / L) * maxCoils).toInt()

            if (coilsCount > 0) {
                val pilePath = Path()
                val baseX = endX - 10.dp.toPx()

                for (i in 0 until coilsCount) {
                    val t = i.toFloat()
                    val angle = t * 1.8f + (d * 0.003f)
                    val rx = 8.dp.toPx() + sin(t * 0.7f) * 4.dp.toPx()
                    val ry = 10.dp.toPx() + cos(t * 0.9f) * 5.dp.toPx()

                    val loopX = baseX + sin(angle) * rx
                    val loopY = centerY + cos(angle * 1.3f) * ry

                    if (i == 0) {
                        pilePath.moveTo(loopX, loopY)
                    } else {
                        val prevAngle = (t - 1f) * 1.8f + (d * 0.003f)
                        val prevX = baseX + sin(prevAngle) * rx
                        val prevY = centerY + cos(prevAngle * 1.3f) * ry

                        pilePath.cubicTo(
                            (prevX + loopX) / 2f + sin(t) * 6.dp.toPx(),
                            prevY - 8.dp.toPx(),
                            (prevX + loopX) / 2f - cos(t) * 6.dp.toPx(),
                            loopY + 8.dp.toPx(),
                            loopX, loopY
                        )
                    }
                }

                drawPath(
                    path = pilePath,
                    color = baseColor,
                    style = Stroke(
                        width = max(1.2f, strokePx * 0.8f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 3. Draw Interlocking Knots
            knotThresholds.forEach { t ->
                if (!resolvedKnots.contains(t)) {
                    val tDist = L * t
                    val knotX = (w / 2f) - (tDist - d)

                    if (knotX > -30f && knotX < w + 30f) {
                        drawCircle(
                            color = baseColor,
                            radius = strokePx * 2.5f,
                            center = Offset(knotX, centerY)
                        )
                        drawCircle(
                            color = Color(0xFF111111),
                            radius = strokePx * 1.2f,
                            center = Offset(knotX, centerY)
                        )
                    }
                }
            }

            // 4. Draw Attached & Tilted Dragged End Objects
            if (d > L - itemDistVisible) {
                val isComplete = d >= L
                drawEndItem(
                    level = currentLevel,
                    progress = itemProgress,
                    isComplete = isComplete,
                    ropeEndX = endX,
                    centerY = centerY
                )
            }
        }
    }
}

// Draw Rich 3D Shaded Objects Physically Tied & Dragged by the Rope Tip
private fun DrawScope.drawEndItem(
    level: FidgetLevel,
    progress: Float,
    isComplete: Boolean,
    ropeEndX: Float,
    centerY: Float
) {
    val tiltAngle = -15f * (1f - progress)

    withTransform({
        translate(left = ropeEndX, top = centerY)
        rotate(degrees = tiltAngle, pivot = Offset.Zero)
    }) {
        when (level) {
            is FidgetLevel.Level1 -> { // Vintage Solid Brass Anchor tied to Rope Tip
                val baseColor = Color(0xFFC2A649)
                val darkGold = Color(0xFF8C7326)
                val highlight = Color(0xFFFFE082)

                // Rope Knot tied through Shackle Ring
                drawCircle(color = darkGold, radius = 5.dp.toPx(), center = Offset(4.dp.toPx(), 0f), style = Stroke(width = 2.5.dp.toPx()))
                drawCircle(color = Color(0xFF4E3706), radius = 2.5.dp.toPx(), center = Offset(4.dp.toPx(), 0f))

                // Anchor Shank
                drawRect(color = baseColor, topLeft = Offset(8.dp.toPx(), -2.5.dp.toPx()), size = Size(20.dp.toPx(), 5.dp.toPx()))
                drawRect(color = darkGold, topLeft = Offset(8.dp.toPx(), 0f), size = Size(20.dp.toPx(), 2.5.dp.toPx()))

                // Stock (Crossbar)
                drawRect(color = baseColor, topLeft = Offset(13.dp.toPx(), -10.dp.toPx()), size = Size(3.5.dp.toPx(), 20.dp.toPx()))
                drawCircle(color = highlight, radius = 2.5.dp.toPx(), center = Offset(14.5.dp.toPx(), -10.dp.toPx()))
                drawCircle(color = highlight, radius = 2.5.dp.toPx(), center = Offset(14.5.dp.toPx(), 10.dp.toPx()))

                // Fluke Arms
                val flukePath = Path().apply {
                    moveTo(24.dp.toPx(), -14.dp.toPx())
                    lineTo(28.dp.toPx(), -14.dp.toPx())
                    quadraticTo(28.dp.toPx(), 0f, 28.dp.toPx(), 14.dp.toPx())
                    lineTo(24.dp.toPx(), 14.dp.toPx())
                    quadraticTo(24.dp.toPx(), 0f, 28.dp.toPx(), 0f)
                    close()
                }
                drawPath(path = flukePath, color = darkGold)
                drawPath(path = flukePath, color = baseColor, style = Stroke(width = 1.5.dp.toPx()))
            }

            is FidgetLevel.Level2 -> { // Industrial Glass Light Bulb on Cord Tip
                val bulbColor = if (isComplete) Color(0xFFFFB703) else Color(0xFFAAAAAA)
                val glowColor = Color(0xFFFFB703).copy(alpha = 0.35f)

                if (isComplete) {
                    drawCircle(color = glowColor, radius = 20.dp.toPx(), center = Offset(18.dp.toPx(), 0f))
                }

                // Metal Cap attached to Cord
                drawRect(color = Color(0xFF666666), topLeft = Offset(0f, -4.dp.toPx()), size = Size(6.dp.toPx(), 8.dp.toPx()))
                drawLine(color = Color(0xFF333333), start = Offset(3.dp.toPx(), -4.dp.toPx()), end = Offset(3.dp.toPx(), 4.dp.toPx()), strokeWidth = 1.5.dp.toPx())

                // Glass Body
                val glassPath = Path().apply {
                    moveTo(6.dp.toPx(), -4.dp.toPx())
                    cubicTo(12.dp.toPx(), -14.dp.toPx(), 26.dp.toPx(), -14.dp.toPx(), 28.dp.toPx(), 0f)
                    cubicTo(26.dp.toPx(), 14.dp.toPx(), 12.dp.toPx(), 14.dp.toPx(), 6.dp.toPx(), 4.dp.toPx())
                    close()
                }
                drawPath(path = glassPath, color = Color.White.copy(alpha = 0.2f))
                drawPath(path = glassPath, color = bulbColor, style = Stroke(width = 1.8.dp.toPx()))

                // Glowing Filament
                val filamentPath = Path().apply {
                    moveTo(12.dp.toPx(), -3.dp.toPx())
                    lineTo(18.dp.toPx(), -1.5.dp.toPx())
                    lineTo(20.dp.toPx(), 0f)
                    lineTo(18.dp.toPx(), 1.5.dp.toPx())
                    lineTo(12.dp.toPx(), 3.dp.toPx())
                }
                drawPath(path = filamentPath, color = bulbColor, style = Stroke(width = 1.5.dp.toPx()))
            }

            is FidgetLevel.Level3 -> { // Folded Origami Bird tied to Kite String
                val birdColor = Color(0xFFFAFAFA)
                val shadowColor = Color(0xFFCCCCCC)
                val driftY = if (isComplete) -20.dp.toPx() else 0f

                withTransform({ translate(top = driftY) }) {
                    val topWing = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(14.dp.toPx(), -14.dp.toPx())
                        lineTo(20.dp.toPx(), -3.dp.toPx())
                        close()
                    }
                    val body = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(20.dp.toPx(), -3.dp.toPx())
                        lineTo(24.dp.toPx(), 0f)
                        lineTo(14.dp.toPx(), 12.dp.toPx())
                        close()
                    }
                    drawPath(path = topWing, color = birdColor)
                    drawPath(path = topWing, color = shadowColor, style = Stroke(width = 1.dp.toPx()))
                    drawPath(path = body, color = shadowColor)
                    drawPath(path = body, color = Color.White, style = Stroke(width = 1.2.dp.toPx()))
                }
            }

            is FidgetLevel.Level4 -> { // Molded Vintage Phone Receiver on Wire Tip
                val phoneColor = Color(0xFF3A5A40)
                val darkPhone = Color(0xFF1E3323)

                // Strain Relief Cap
                drawRect(color = darkPhone, topLeft = Offset(0f, -3.dp.toPx()), size = Size(4.dp.toPx(), 6.dp.toPx()))

                // Handle Body
                val handlePath = Path().apply {
                    moveTo(4.dp.toPx(), -3.dp.toPx())
                    cubicTo(10.dp.toPx(), -9.dp.toPx(), 20.dp.toPx(), -9.dp.toPx(), 26.dp.toPx(), -3.dp.toPx())
                    lineTo(26.dp.toPx(), 3.dp.toPx())
                    cubicTo(20.dp.toPx(), 9.dp.toPx(), 10.dp.toPx(), 9.dp.toPx(), 4.dp.toPx(), 3.dp.toPx())
                    close()
                }
                drawPath(path = handlePath, color = phoneColor)
                drawPath(path = handlePath, color = darkPhone, style = Stroke(width = 1.5.dp.toPx()))

                // Earpieces
                drawCircle(color = darkPhone, radius = 5.dp.toPx(), center = Offset(4.dp.toPx(), -5.dp.toPx()))
                drawCircle(color = darkPhone, radius = 5.dp.toPx(), center = Offset(26.dp.toPx(), -5.dp.toPx()))
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effectId = if (isThud) VibrationEffect.Composition.PRIMITIVE_THUD else VibrationEffect.Composition.PRIMITIVE_CLICK
                val composition = VibrationEffect.startComposition().addPrimitive(effectId).compose()
                vibrator.vibrate(composition)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(if (isThud) 50L else 10L)
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}
