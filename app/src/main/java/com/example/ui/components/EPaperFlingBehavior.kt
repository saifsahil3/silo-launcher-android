package com.example.ui.components

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.abs

/**
 * Custom E-Paper Fling Behavior that applies heavy friction dampening and velocity clamping
 * to replicate the mechanical, deliberate scrolling of physical e-ink devices (Kindle, Boox, Kobo)
 * without high-speed continuous gliding or LCD motion blur.
 */
class EPaperFlingBehavior(
    private val decayAnimationSpec: DecayAnimationSpec<Float> = exponentialDecay(frictionMultiplier = 3.6f),
    private val velocityDampingFactor: Float = 0.38f,
    private val maxVelocityCap: Float = 2200f
) : FlingBehavior {

    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        // Clamp and dampen initial fling velocity to eliminate continuous gliding
        val clampedVelocity = initialVelocity.coerceIn(-maxVelocityCap, maxVelocityCap) * velocityDampingFactor
        if (abs(clampedVelocity) < 60f) {
            return 0f
        }

        var lastValue = 0f
        var velocityRemaining = clampedVelocity

        AnimationState(
            initialValue = 0f,
            initialVelocity = clampedVelocity
        ).animateDecay(decayAnimationSpec) {
            val delta = value - lastValue
            val consumed = scrollBy(delta)
            lastValue = value
            velocityRemaining = velocity

            // If bounds reached or scroll consumed differs from delta, finish
            if (abs(delta - consumed) > 0.5f) {
                cancelAnimation()
            }
        }

        return velocityRemaining
    }
}

@Composable
fun rememberEPaperFlingBehavior(
    frictionMultiplier: Float = 3.6f,
    velocityDampingFactor: Float = 0.38f
): EPaperFlingBehavior {
    return remember(frictionMultiplier, velocityDampingFactor) {
        EPaperFlingBehavior(
            decayAnimationSpec = exponentialDecay(frictionMultiplier = frictionMultiplier),
            velocityDampingFactor = velocityDampingFactor
        )
    }
}
