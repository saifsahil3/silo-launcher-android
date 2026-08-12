package com.example.ui.components.engine

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Custom allocation-free Verlet physics engine for rope and 2D rigid-body attached objects.
 * Designed for 60-120 FPS execution on Compose Canvas without UI thread allocations.
 */
class RopePhysicsEngine {

    companion object {
        const val MAX_PARTICLES = 32
        const val DEFAULT_PARTICLES = 28
        const val CONSTRAINT_ITERATIONS = 5
        const val MAX_STRETCH_RATIO = 1.25f
        const val SETTLING_THRESHOLD_SQ = 0.04f
    }

    var numParticles: Int = DEFAULT_PARTICLES
        private set

    // Allocation-free fixed FloatArrays for particle state
    val posX = FloatArray(MAX_PARTICLES)
    val posY = FloatArray(MAX_PARTICLES)
    val prevX = FloatArray(MAX_PARTICLES)
    val prevY = FloatArray(MAX_PARTICLES)
    val accelX = FloatArray(MAX_PARTICLES)
    val accelY = FloatArray(MAX_PARTICLES)
    val isPinned = BooleanArray(MAX_PARTICLES)

    // Attached 2D Rigid Object state
    var objPosX: Float = 0f
    var objPosY: Float = 0f
    var objPrevX: Float = 0f
    var objPrevY: Float = 0f
    var objAngleRad: Float = 0f
    var objAngularVel: Float = 0f

    // Physics parameters from FidgetLevel
    var stiffness: Float = 0.85f
    var damping: Float = 0.95f
    var gravityX: Float = 0f
    var gravityY: Float = 300f
    var targetSegmentLength: Float = 12f
    var objectMass: Float = 1.0f
    var objectGravity: Float = 400f
    var objectDamping: Float = 0.94f

    // Anchor position (fixed wall/origin point)
    var anchorX: Float = 40f
    var anchorY: Float = 100f

    // Interaction state
    var grabbedNodeIndex: Int = -1
    var touchTargetX: Float = 0f
    var touchTargetY: Float = 0f
    var touchSpringStiffness: Float = 250f

    // Progress tracking
    var accumulatedPullDistance: Float = 0f
    var targetLevelLengthPx: Float = 2500f
    var isLevelCompleted: Boolean = false
    var isSleeping: Boolean = false
    var levelAlpha: Float = 1.0f
    var groundY: Float = 240f

    fun loadLevel(level: FidgetLevel, startX: Float, startY: Float, availableWidth: Float, availableHeight: Float = 260f) {
        anchorX = startX
        anchorY = startY
        groundY = (availableHeight - 16f).coerceAtLeast(startY + 30f)
        stiffness = level.stiffness
        damping = level.damping
        gravityX = 0f
        gravityY = 150f
        objectMass = level.objectMass
        objectGravity = level.objectGravity
        objectDamping = level.objectDamping
        targetLevelLengthPx = level.lengthPx

        numParticles = DEFAULT_PARTICLES.coerceIn(24, MAX_PARTICLES)
        targetSegmentLength = (availableWidth * 0.75f) / numParticles

        val dxStep = targetSegmentLength * 0.95f
        for (i in 0 until numParticles) {
            val px = startX + i * dxStep
            val sag = (1f - (abs(i - numParticles / 2f) / (numParticles / 2f))) * 25f
            val py = (startY + sag).coerceAtMost(groundY - 5f)

            posX[i] = px
            posY[i] = py
            prevX[i] = px
            prevY[i] = py
            accelX[i] = 0f
            accelY[i] = 0f
            isPinned[i] = (i == 0)
        }

        // Object attached at end particle
        val tipIdx = numParticles - 1
        objPosX = posX[tipIdx] + 10f
        objPosY = posY[tipIdx].coerceAtMost(groundY - 10f)
        objPrevX = objPosX
        objPrevY = objPosY
        objAngleRad = 0f
        objAngularVel = 0f

        grabbedNodeIndex = -1
        accumulatedPullDistance = 0f
        isLevelCompleted = false
        isSleeping = false
        levelAlpha = 1.0f
    }

    /**
     * Pulls particles toward right edge dump pile resting on the bottom ground floor.
     */
    fun applyPileUpToRight(availableWidth: Float) {
        val dumpTargetX = (availableWidth - 45f).coerceAtLeast(anchorX + 80f)
        val dumpTargetY = groundY - 6f

        for (i in 0 until numParticles) {
            val offsetScatterX = (i % 6) * 5f - 15f
            val offsetScatterY = -(i / 6) * 5f // Stack upwards from ground
            val targetX = dumpTargetX + offsetScatterX
            val targetY = (dumpTargetY + offsetScatterY).coerceAtMost(groundY)

            posX[i] += (targetX - posX[i]) * 0.20f
            posY[i] += (targetY - posY[i]) * 0.20f
            prevX[i] = posX[i]
            prevY[i] = posY[i]
        }

        // Move attached object directly into pile stack resting on ground
        objPosX += (dumpTargetX - objPosX) * 0.22f
        objPosY += (dumpTargetY - 12f - objPosY) * 0.22f
        objPrevX = objPosX
        objPrevY = objPosY
    }

    /**
     * Physics tick called every frame (zero heap allocations).
     */
    fun update(dtSeconds: Float) {
        if (isLevelCompleted) return

        val clampedDt = dtSeconds.coerceIn(0.005f, 0.033f)
        val dtSq = clampedDt * clampedDt

        var totalKineticEnergySq = 0f

        // 1. Particle Verlet Integration Step
        for (i in 0 until numParticles) {
            if (isPinned[i]) continue

            val vx = (posX[i] - prevX[i]) * damping
            val vy = (posY[i] - prevY[i]) * damping

            totalKineticEnergySq += vx * vx + vy * vy

            prevX[i] = posX[i]
            prevY[i] = posY[i]

            val ax = accelX[i] + gravityX
            val ay = accelY[i] + gravityY

            posX[i] += vx + ax * dtSq
            posY[i] += vy + ay * dtSq

            accelX[i] = 0f
            accelY[i] = 0f
        }

        // 2. Touch Spring Force Application
        if (grabbedNodeIndex >= 0 && grabbedNodeIndex < numParticles) {
            val idx = grabbedNodeIndex
            val dx = touchTargetX - posX[idx]
            val dy = touchTargetY - posY[idx]
            val dist = sqrt(dx * dx + dy * dy)

            if (dist > 0.05f) {
                val springForce = dist * touchSpringStiffness
                val fx = (dx / dist) * springForce
                val fy = (dy / dist) * springForce

                posX[idx] += fx * dtSq
                posY[idx] += fy * dtSq
            }

            // Sleep guard override while touched
            isSleeping = false
        } else {
            // Check settling threshold
            if (totalKineticEnergySq < SETTLING_THRESHOLD_SQ) {
                isSleeping = true
            }
        }

        // 3. Distance Constraint Solver Iterations
        for (iter in 0 until CONSTRAINT_ITERATIONS) {
            if (isPinned[0]) {
                posX[0] = anchorX
                posY[0] = anchorY
            }

            for (i in 0 until numParticles - 1) {
                val p1 = i
                val p2 = i + 1

                var dx = posX[p2] - posX[p1]
                var dy = posY[p2] - posY[p1]
                val currentDist = sqrt(dx * dx + dy * dy)

                if (currentDist > 0.001f) {
                    val maxLen = targetSegmentLength * MAX_STRETCH_RATIO
                    val targetDist = if (currentDist > maxLen) maxLen else targetSegmentLength
                    val delta = (currentDist - targetDist) / currentDist
                    val correctionX = dx * 0.5f * delta * stiffness
                    val correctionY = dy * 0.5f * delta * stiffness

                    if (!isPinned[p1]) {
                        val factor = if (!isPinned[p2]) 1.0f else 2.0f
                        posX[p1] += correctionX * factor
                        posY[p1] += correctionY * factor
                    }
                    if (!isPinned[p2]) {
                        val factor = if (!isPinned[p1]) 1.0f else 2.0f
                        posX[p2] -= correctionX * factor
                        posY[p2] -= correctionY * factor
                    }
                }
            }
        }

        // 3.5. Visible Bottom Ground Floor Collision Constraint
        for (i in 0 until numParticles) {
            if (posY[i] > groundY) {
                posY[i] = groundY
                prevY[i] = groundY + (posY[i] - prevY[i]) * 0.35f // Ground friction damping
            }
        }

        // 4. Object Physics Update: Rigidly locked to rope tip particle with damped pendulum swing
        val tipIdx = numParticles - 1
        objPrevX = objPosX
        objPrevY = objPosY
        objPosX = posX[tipIdx]
        objPosY = posY[tipIdx].coerceAtMost(groundY - 8f)

        if (tipIdx >= 1) {
            val segDx = posX[tipIdx] - posX[tipIdx - 1]
            val segDy = posY[tipIdx] - posY[tipIdx - 1]
            if (abs(segDx) > 0.001f || abs(segDy) > 0.001f) {
                // Natural hanging angle along rope tip segment
                val targetAngle = atan2(segDy, segDx) - (Math.PI.toFloat() / 2f)
                var diff = targetAngle - objAngleRad
                while (diff > Math.PI) diff -= (2 * Math.PI).toFloat()
                while (diff < -Math.PI) diff += (2 * Math.PI).toFloat()

                // Heavily damped rotation smoothing (prevents wild spinning)
                objAngularVel = (objAngularVel + diff * 0.12f) * 0.70f
                objAngleRad += objAngularVel
            }
        }

        // Level completion check based on total pull distance
        if (accumulatedPullDistance >= targetLevelLengthPx) {
            isLevelCompleted = true
        }
    }

    /**
     * Grabs the node nearest to touch coordinates.
     */
    fun grabNearestNode(touchX: Float, touchY: Float): Int {
        var closestIdx = -1
        var minSqDist = Float.MAX_VALUE

        for (i in 0 until numParticles) {
            val dx = posX[i] - touchX
            val dy = posY[i] - touchY
            val sqDist = dx * dx + dy * dy
            if (sqDist < minSqDist) {
                minSqDist = sqDist
                closestIdx = i
            }
        }

        // Touch radius threshold (~120px)
        if (minSqDist <= 14400f) {
            grabbedNodeIndex = closestIdx
            touchTargetX = touchX
            touchTargetY = touchY
            return closestIdx
        }
        return -1
    }

    fun updateTouchTarget(touchX: Float, touchY: Float, deltaX: Float) {
        touchTargetX = touchX
        touchTargetY = touchY

        if (deltaX > 0f && grabbedNodeIndex >= 0) {
            accumulatedPullDistance += deltaX
        }
    }

    fun releaseTouch(velocityX: Float, velocityY: Float) {
        if (grabbedNodeIndex >= 0 && grabbedNodeIndex < numParticles) {
            val idx = grabbedNodeIndex
            // Transfer finger momentum directly into Verlet position diff
            prevX[idx] = posX[idx] - velocityX * 0.016f
            prevY[idx] = posY[idx] - velocityY * 0.016f

            // Transfer to attached object
            val tipIdx = numParticles - 1
            if (idx >= tipIdx - 3) {
                objPrevX = objPosX - velocityX * 0.016f
                objPrevY = objPosY - velocityY * 0.016f
            }
        }
        grabbedNodeIndex = -1
    }

    fun getDiscoveryProgress(): Float {
        if (targetLevelLengthPx <= 0f) return 1f
        return (accumulatedPullDistance / (targetLevelLengthPx * 0.35f)).coerceIn(0f, 1f)
    }

    fun getCompletionProgress(): Float {
        if (targetLevelLengthPx <= 0f) return 1f
        return (accumulatedPullDistance / targetLevelLengthPx).coerceIn(0f, 1f)
    }
}
