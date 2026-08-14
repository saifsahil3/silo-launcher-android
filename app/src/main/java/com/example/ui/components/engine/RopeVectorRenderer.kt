package com.example.ui.components.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural high-realism vector graphics renderer for rope materials,
 * attached 2D vector objects, and visual completion effects with zero runtime heap allocations.
 */
object RopeVectorRenderer {

    // Pre-allocated reusable graphics paths for zero allocation rendering
    private val ropePath = Path()
    private val braidPath = Path()
    private val objectPath = Path()
    private val subDetailPath = Path()

    private fun Color.a(levelAlpha: Float): Color = copy(alpha = alpha * levelAlpha)

    fun DrawScope.drawRope(engine: RopePhysicsEngine, material: RopeMaterial) {
        val count = engine.numParticles
        if (count < 2) return

        val alpha = engine.levelAlpha
        if (alpha <= 0f) return

        ropePath.reset()
        ropePath.moveTo(engine.posX[0], engine.posY[0])

        val strokePx = material.strokeWidthDp.dp.toPx()

        for (i in 1 until count) {
            val x1 = engine.posX[i - 1]
            val y1 = engine.posY[i - 1]
            val x2 = engine.posX[i]
            val y2 = engine.posY[i]

            val midX = (x1 + x2) / 2f
            val midY = (y1 + y2) / 2f

            ropePath.quadraticTo(x1, y1, midX, midY)
        }
        ropePath.lineTo(engine.posX[count - 1], engine.posY[count - 1])

        val primary = material.primaryColor.a(alpha)
        val accent = material.accentColor.a(alpha)

        when (material.style) {
            RopeStyle.CHAIN -> {
                // Render interlocking chain links along particle nodes
                for (i in 0 until count - 1) {
                    val x1 = engine.posX[i]
                    val y1 = engine.posY[i]
                    val x2 = engine.posX[i + 1]
                    val y2 = engine.posY[i + 1]
                    val angle = Math.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble()).toFloat()

                    withTransform({
                        translate(left = (x1 + x2) / 2f, top = (y1 + y2) / 2f)
                        rotate(degrees = Math.toDegrees(angle.toDouble()).toFloat(), pivot = Offset.Zero)
                    }) {
                        drawRoundRect(
                            color = primary,
                            topLeft = Offset(-6.dp.toPx(), -3.5.dp.toPx()),
                            size = Size(12.dp.toPx(), 7.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                            style = Stroke(width = 1.8.dp.toPx())
                        )
                    }
                }
            }
            RopeStyle.BRAIDED -> {
                // Base thick strand
                drawPath(
                    path = ropePath,
                    color = primary,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Secondary overlapping braid pattern
                braidPath.reset()
                for (i in 0 until count step 2) {
                    val bx = engine.posX[i] + sin(i * 1.5f) * 3f
                    val by = engine.posY[i] + cos(i * 1.5f) * 3f
                    if (i == 0) braidPath.moveTo(bx, by) else braidPath.lineTo(bx, by)
                }
                drawPath(
                    path = braidPath,
                    color = accent,
                    style = Stroke(width = strokePx * 0.45f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            RopeStyle.CABLE -> {
                // Heavy outer jacket
                drawPath(
                    path = ropePath,
                    color = primary,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Inner core line
                drawPath(
                    path = ropePath,
                    color = accent,
                    style = Stroke(width = strokePx * 0.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            else -> {
                // Smooth main line with accent highlight overlay
                drawPath(
                    path = ropePath,
                    color = primary,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                if (strokePx > 2f) {
                    drawPath(
                        path = ropePath,
                        color = accent,
                        style = Stroke(width = strokePx * 0.35f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }
        }
    }

    fun DrawScope.drawAttachedObject(
        engine: RopePhysicsEngine,
        objType: AttachedObject,
        discoveryProgress: Float,
        isCompleted: Boolean,
        objectBitmap: androidx.compose.ui.graphics.ImageBitmap? = null
    ) {
        val alpha = engine.levelAlpha
        if (alpha <= 0f) return

        val objX = engine.objPosX
        val objY = engine.objPosY
        val angleDeg = Math.toDegrees(engine.objAngleRad.toDouble()).toFloat()

        withTransform({
            translate(left = objX, top = objY)
            rotate(degrees = angleDeg, pivot = Offset.Zero)
        }) {
            if (objectBitmap != null) {
                val targetW = objType.widthDp.dp.toPx().toInt().coerceAtLeast(36)
                val targetH = (targetW.toFloat() * (objectBitmap.height.toFloat() / objectBitmap.width.toFloat())).toInt()
                drawImage(
                    image = objectBitmap,
                    dstOffset = androidx.compose.ui.unit.IntOffset(-targetW / 2, -targetH / 2),
                    dstSize = androidx.compose.ui.unit.IntSize(targetW, targetH),
                    alpha = alpha
                )
            } else {
                // Immediate reveal from beginning (fallback procedural shape)
                drawVectorObjectShape(objType, isCompleted, alpha)
            }
        }
    }

    private fun DrawScope.drawVectorObjectShape(
        objType: AttachedObject,
        isCompleted: Boolean,
        alpha: Float
    ) {
        val goldPrimary = Color(0xFFF59E0B).a(alpha)
        val goldHighlight = Color(0xFFFDE68A).a(alpha)
        val goldShadow = Color(0xFFB45309).a(alpha)
        val mainColor = (if (isCompleted) Color(0xFFFFB703) else Color(0xFFE2E8F0)).a(alpha)
        val darkColor = Color(0xFF475569).a(alpha)
        val shadowColor = Color(0xFF0F172A).a(alpha)
        val whiteHighlight = Color(0xFFFFFFFF).a(alpha * 0.85f)

        objectPath.reset()
        subDetailPath.reset()

        when (objType) {
            AttachedObject.LAMP -> {
                // Top suspension loop & brass socket cap
                drawCircle(color = goldShadow, radius = 3.dp.toPx(), center = Offset(0f, -14.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))
                drawRoundRect(
                    color = goldPrimary,
                    topLeft = Offset(-5.dp.toPx(), -12.dp.toPx()),
                    size = Size(10.dp.toPx(), 5.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx())
                )
                drawRect(color = goldHighlight, topLeft = Offset(-5.dp.toPx(), -10.dp.toPx()), size = Size(10.dp.toPx(), 1.5.dp.toPx()))

                // Bulb glass body envelope
                objectPath.moveTo(-3.5.dp.toPx(), -7.dp.toPx())
                objectPath.cubicTo(-9.dp.toPx(), -4.dp.toPx(), -11.dp.toPx(), 4.dp.toPx(), -6.dp.toPx(), 10.dp.toPx())
                objectPath.quadraticTo(0f, 13.dp.toPx(), 6.dp.toPx(), 10.dp.toPx())
                objectPath.cubicTo(11.dp.toPx(), 4.dp.toPx(), 9.dp.toPx(), -4.dp.toPx(), 3.5.dp.toPx(), -7.dp.toPx())
                objectPath.close()

                drawPath(path = objectPath, color = Color(0xFFF8FAFC).a(0.25f * alpha))
                drawPath(path = objectPath, color = mainColor, style = Stroke(width = 1.8.dp.toPx()))

                // Glowing tungsten filament wire inside
                subDetailPath.moveTo(-2.5.dp.toPx(), -3.dp.toPx())
                subDetailPath.lineTo(-1.dp.toPx(), 2.dp.toPx())
                subDetailPath.lineTo(0f, 0f)
                subDetailPath.lineTo(1.dp.toPx(), 2.dp.toPx())
                subDetailPath.lineTo(2.5.dp.toPx(), -3.dp.toPx())
                drawPath(path = subDetailPath, color = goldHighlight, style = Stroke(width = 1.4.dp.toPx()))

                // Curved specular glass reflection line
                drawCircle(color = whiteHighlight, radius = 1.5.dp.toPx(), center = Offset(-5.dp.toPx(), -1.dp.toPx()))
            }

            AttachedObject.ANCHOR -> {
                // Top shackle ring
                drawCircle(color = goldPrimary, radius = 4.5.dp.toPx(), center = Offset(0f, -14.dp.toPx()), style = Stroke(width = 2.dp.toPx()))

                // Stock crossbar
                drawRoundRect(
                    color = goldPrimary,
                    topLeft = Offset(-10.dp.toPx(), -8.5.dp.toPx()),
                    size = Size(20.dp.toPx(), 3.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f.dp.toPx())
                )
                // Stock end balls
                drawCircle(color = goldShadow, radius = 2.2.dp.toPx(), center = Offset(-10.dp.toPx(), -7.dp.toPx()))
                drawCircle(color = goldShadow, radius = 2.2.dp.toPx(), center = Offset(10.dp.toPx(), -7.dp.toPx()))

                // Center main shank column
                drawRect(color = goldPrimary, topLeft = Offset(-2.2.dp.toPx(), -10.dp.toPx()), size = Size(4.4.dp.toPx(), 22.dp.toPx()))
                drawLine(color = goldHighlight, start = Offset(-0.8.dp.toPx(), -10.dp.toPx()), end = Offset(-0.8.dp.toPx(), 12.dp.toPx()), strokeWidth = 1.2.dp.toPx())

                // Curved fluke arms
                objectPath.moveTo(-13.dp.toPx(), 4.dp.toPx())
                objectPath.quadraticTo(0f, 17.dp.toPx(), 13.dp.toPx(), 4.dp.toPx())
                drawPath(path = objectPath, color = goldPrimary, style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round))

                // Left & Right pointed fluke tips
                subDetailPath.moveTo(-15.dp.toPx(), 2.dp.toPx())
                subDetailPath.lineTo(-11.dp.toPx(), 4.dp.toPx())
                subDetailPath.lineTo(-14.dp.toPx(), 8.dp.toPx())
                subDetailPath.close()

                subDetailPath.moveTo(15.dp.toPx(), 2.dp.toPx())
                subDetailPath.lineTo(11.dp.toPx(), 4.dp.toPx())
                subDetailPath.lineTo(14.dp.toPx(), 8.dp.toPx())
                subDetailPath.close()

                drawPath(path = subDetailPath, color = goldHighlight)
                // Center crown base
                drawCircle(color = goldShadow, radius = 3.dp.toPx(), center = Offset(0f, 12.dp.toPx()))
            }

            AttachedObject.KEY -> {
                // Ring bow handle with inner decorative cutout
                drawCircle(color = goldPrimary, radius = 8.dp.toPx(), center = Offset(-9.dp.toPx(), 0f), style = Stroke(width = 2.4.dp.toPx()))
                drawCircle(color = goldShadow, radius = 4.dp.toPx(), center = Offset(-9.dp.toPx(), 0f), style = Stroke(width = 1.dp.toPx()))

                // Key stem shaft
                drawRect(color = goldPrimary, topLeft = Offset(-2.dp.toPx(), -1.8.dp.toPx()), size = Size(18.dp.toPx(), 3.6.dp.toPx()))
                drawLine(color = goldHighlight, start = Offset(-1.dp.toPx(), -0.8.dp.toPx()), end = Offset(15.dp.toPx(), -0.8.dp.toPx()), strokeWidth = 1.2.dp.toPx())

                // Key bit notches
                drawRect(color = goldPrimary, topLeft = Offset(9.dp.toPx(), 1.8.dp.toPx()), size = Size(3.5.dp.toPx(), 4.dp.toPx()))
                drawRect(color = goldPrimary, topLeft = Offset(13.5.dp.toPx(), 1.8.dp.toPx()), size = Size(2.5.dp.toPx(), 3.dp.toPx()))
            }

            AttachedObject.BUTTON -> {
                // Outer raised rim
                drawCircle(color = Color(0xFF854D0E).a(alpha), radius = 13.dp.toPx())
                drawCircle(color = Color(0xFFCA8A04).a(alpha), radius = 11.dp.toPx())
                // Recessed inner dish surface
                drawCircle(color = Color(0xFFA16207).a(alpha), radius = 8.5.dp.toPx())
                drawCircle(color = shadowColor, radius = 8.5.dp.toPx(), style = Stroke(width = 1.dp.toPx()))

                // 4 Countersunk sewing holes
                val r = 3.5.dp.toPx()
                drawCircle(color = shadowColor, radius = 1.4.dp.toPx(), center = Offset(-r, -r))
                drawCircle(color = shadowColor, radius = 1.4.dp.toPx(), center = Offset(r, -r))
                drawCircle(color = shadowColor, radius = 1.4.dp.toPx(), center = Offset(-r, r))
                drawCircle(color = shadowColor, radius = 1.4.dp.toPx(), center = Offset(r, r))

                // Thread cross stitching passing through holes
                drawLine(color = Color(0xFFFEF08A).a(alpha), start = Offset(-r, -r), end = Offset(r, r), strokeWidth = 1.2.dp.toPx(), cap = StrokeCap.Round)
                drawLine(color = Color(0xFFFEF08A).a(alpha), start = Offset(r, -r), end = Offset(-r, r), strokeWidth = 1.2.dp.toPx(), cap = StrokeCap.Round)
            }

            AttachedObject.FEATHER -> {
                // Feather central rachis shaft
                drawLine(color = mainColor, start = Offset(-14.dp.toPx(), 6.dp.toPx()), end = Offset(14.dp.toPx(), -6.dp.toPx()), strokeWidth = 1.6.dp.toPx())

                // Upper & lower vane feather curves
                objectPath.moveTo(-14.dp.toPx(), 6.dp.toPx())
                objectPath.quadraticTo(-4.dp.toPx(), -10.dp.toPx(), 14.dp.toPx(), -6.dp.toPx())
                objectPath.quadraticTo(4.dp.toPx(), 8.dp.toPx(), -14.dp.toPx(), 6.dp.toPx())
                drawPath(path = objectPath, color = Color(0xFF38BDF8).a(0.35f * alpha))
                drawPath(path = objectPath, color = Color(0xFF38BDF8).a(alpha), style = Stroke(width = 1.4.dp.toPx()))

                // Faded barb stroke details
                drawLine(color = whiteHighlight, start = Offset(-6.dp.toPx(), 2.dp.toPx()), end = Offset(-2.dp.toPx(), -4.dp.toPx()), strokeWidth = 1.dp.toPx())
                drawLine(color = whiteHighlight, start = Offset(0f, 0f), end = Offset(4.dp.toPx(), -5.dp.toPx()), strokeWidth = 1.dp.toPx())
            }

            AttachedObject.HEADPHONES -> {
                // Arc headband
                drawCircle(color = darkColor, radius = 12.dp.toPx(), center = Offset(0f, -3.dp.toPx()), style = Stroke(width = 2.6.dp.toPx()))
                // Headband comfort cushion pad
                drawCircle(color = mainColor, radius = 12.dp.toPx(), center = Offset(0f, -3.dp.toPx()), style = Stroke(width = 1.2.dp.toPx()))

                // Left & Right Ear cups with cushions
                drawRoundRect(color = darkColor, topLeft = Offset(-14.dp.toPx(), -1.dp.toPx()), size = Size(5.5.dp.toPx(), 11.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
                drawRoundRect(color = shadowColor, topLeft = Offset(-12.5.dp.toPx(), 0.5.dp.toPx()), size = Size(3.5.dp.toPx(), 8.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx()))

                drawRoundRect(color = darkColor, topLeft = Offset(8.5.dp.toPx(), -1.dp.toPx()), size = Size(5.5.dp.toPx(), 11.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
                drawRoundRect(color = shadowColor, topLeft = Offset(9.dp.toPx(), 0.5.dp.toPx()), size = Size(3.5.dp.toPx(), 8.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx()))
            }

            AttachedObject.SHOE -> {
                // Sleek sneaker body
                drawRoundRect(color = mainColor, topLeft = Offset(-13.dp.toPx(), -5.dp.toPx()), size = Size(26.dp.toPx(), 9.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
                // Rubber outsole base
                drawRoundRect(color = Color(0xFFF8FAFC).a(alpha), topLeft = Offset(-13.dp.toPx(), 4.dp.toPx()), size = Size(26.dp.toPx(), 4.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx()))
                // Heel accent & laces
                drawRect(color = darkColor, topLeft = Offset(-13.dp.toPx(), -5.dp.toPx()), size = Size(5.dp.toPx(), 9.dp.toPx()))
                drawLine(color = whiteHighlight, start = Offset(-4.dp.toPx(), -4.dp.toPx()), end = Offset(4.dp.toPx(), -4.dp.toPx()), strokeWidth = 1.2.dp.toPx())
                drawLine(color = whiteHighlight, start = Offset(-2.dp.toPx(), -1.dp.toPx()), end = Offset(6.dp.toPx(), -1.dp.toPx()), strokeWidth = 1.2.dp.toPx())
            }

            AttachedObject.PADLOCK, AttachedObject.LOCK -> {
                // Steel shackle arch
                drawCircle(color = darkColor, radius = 6.5.dp.toPx(), center = Offset(0f, -6.5.dp.toPx()), style = Stroke(width = 2.4.dp.toPx()))
                drawCircle(color = whiteHighlight, radius = 6.5.dp.toPx(), center = Offset(-0.8.dp.toPx(), -6.5.dp.toPx()), style = Stroke(width = 1.dp.toPx()))

                // Solid brass lock body
                drawRoundRect(
                    color = goldPrimary,
                    topLeft = Offset(-9.dp.toPx(), -2.dp.toPx()),
                    size = Size(18.dp.toPx(), 15.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                )
                // Brushed metallic highlight bar
                drawRect(color = goldHighlight, topLeft = Offset(-7.dp.toPx(), -2.dp.toPx()), size = Size(3.dp.toPx(), 15.dp.toPx()))

                // Keyhole
                drawCircle(color = shadowColor, radius = 2.2.dp.toPx(), center = Offset(0f, 4.dp.toPx()))
                drawRect(color = shadowColor, topLeft = Offset(-1.dp.toPx(), 4.dp.toPx()), size = Size(2.dp.toPx(), 4.dp.toPx()))
            }

            AttachedObject.BUCKET -> {
                // Tapered bucket body
                objectPath.moveTo(-11.dp.toPx(), -8.dp.toPx())
                objectPath.lineTo(11.dp.toPx(), -8.dp.toPx())
                objectPath.lineTo(8.dp.toPx(), 10.dp.toPx())
                objectPath.lineTo(-8.dp.toPx(), 10.dp.toPx())
                objectPath.close()

                drawPath(path = objectPath, color = mainColor)
                drawPath(path = objectPath, color = darkColor, style = Stroke(width = 1.6.dp.toPx()))
                // Rolled top rim
                drawRoundRect(color = darkColor, topLeft = Offset(-12.dp.toPx(), -9.5.dp.toPx()), size = Size(24.dp.toPx(), 3.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx()))

                // Wire handle bail arc
                subDetailPath.moveTo(-10.dp.toPx(), -8.dp.toPx())
                subDetailPath.quadraticTo(0f, -18.dp.toPx(), 10.dp.toPx(), -8.dp.toPx())
                drawPath(path = subDetailPath, color = darkColor, style = Stroke(width = 1.5.dp.toPx()))
            }

            AttachedObject.SMALL_BELL -> {
                // Flared bell dome
                objectPath.moveTo(-9.dp.toPx(), 7.dp.toPx())
                objectPath.quadraticTo(-6.dp.toPx(), -8.dp.toPx(), 0f, -10.dp.toPx())
                objectPath.quadraticTo(6.dp.toPx(), -8.dp.toPx(), 9.dp.toPx(), 7.dp.toPx())
                objectPath.close()

                drawPath(path = objectPath, color = goldPrimary)
                drawPath(path = objectPath, color = goldShadow, style = Stroke(width = 1.5.dp.toPx()))
                // Top loop ring & bottom clapper ball
                drawCircle(color = goldShadow, radius = 2.5.dp.toPx(), center = Offset(0f, -12.dp.toPx()), style = Stroke(width = 1.2.dp.toPx()))
                drawCircle(color = shadowColor, radius = 2.2.dp.toPx(), center = Offset(0f, 9.dp.toPx()))
            }

            AttachedObject.PLUG -> {
                // Molded plug block body
                drawRoundRect(color = darkColor, topLeft = Offset(-8.dp.toPx(), -5.dp.toPx()), size = Size(16.dp.toPx(), 11.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5.dp.toPx()))
                // Strain relief tail
                drawRect(color = shadowColor, topLeft = Offset(-3.dp.toPx(), -8.dp.toPx()), size = Size(6.dp.toPx(), 3.dp.toPx()))
                // Dual solid brass prongs with pin holes
                drawRect(color = goldPrimary, topLeft = Offset(-5.dp.toPx(), 6.dp.toPx()), size = Size(2.6.dp.toPx(), 6.dp.toPx()))
                drawRect(color = goldPrimary, topLeft = Offset(2.4.dp.toPx(), 6.dp.toPx()), size = Size(2.6.dp.toPx(), 6.dp.toPx()))
                drawCircle(color = shadowColor, radius = 0.8.dp.toPx(), center = Offset(-3.7.dp.toPx(), 9.5.dp.toPx()))
                drawCircle(color = shadowColor, radius = 0.8.dp.toPx(), center = Offset(3.7.dp.toPx(), 9.5.dp.toPx()))
            }

            else -> {
                // Folded cloth garment shape (Shirt, Sock, Sweater)
                drawRoundRect(
                    color = mainColor,
                    topLeft = Offset(-11.dp.toPx(), -8.dp.toPx()),
                    size = Size(22.dp.toPx(), 16.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
                drawRoundRect(
                    color = darkColor,
                    topLeft = Offset(-11.dp.toPx(), -8.dp.toPx()),
                    size = Size(22.dp.toPx(), 16.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
                    style = Stroke(width = 1.4.dp.toPx())
                )
                // Collar fold line
                subDetailPath.moveTo(-6.dp.toPx(), -8.dp.toPx())
                subDetailPath.lineTo(0f, -3.dp.toPx())
                subDetailPath.lineTo(6.dp.toPx(), -8.dp.toPx())
                drawPath(path = subDetailPath, color = darkColor, style = Stroke(width = 1.2.dp.toPx()))
            }
        }
    }

    fun DrawScope.drawCompletionEffect(engine: RopePhysicsEngine, progress: Float) {
        // Background pulse circle removed per user request
    }
}

