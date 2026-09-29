package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuxBorderGlow
import com.example.ui.theme.LuxError
import com.example.ui.theme.LuxObsidian
import com.example.ui.theme.LuxPlatinum
import com.example.ui.theme.LuxPureWhite
import com.example.ui.theme.LuxSilver
import com.example.ui.theme.LuxWarning
import kotlin.math.cos
import kotlin.math.sin

enum class LuxOrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    PROCESSING,
    ERROR,
    PERMISSION_REQUIRED
}

@Composable
fun LuxOrb(
    state: LuxOrbState,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    audioAmplitude: Float = 0f
) {
    val transition = rememberInfiniteTransition(label = "lux_orb_transition")

    // Slow breathing for Idle & general pulse
    val idlePulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )

    // Continuous rotation for Thinking / Processing
    val continuousRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                when (state) {
                    LuxOrbState.PROCESSING -> 1800
                    LuxOrbState.THINKING -> 3200
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "continuous_rotation"
    )

    // Fast ripple for Listening
    val listeningRipple by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "listening_ripple"
    )

    // Speaking rhythmic pulsation
    val speakingPulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_pulse"
    )

    // Glitch/stutter for Error
    val errorJitter by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "error_jitter"
    )

    // Warning slow beacon for Permission Required
    val warningBeacon by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warning_beacon"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.width * 0.36f

            when (state) {
                LuxOrbState.IDLE -> {
                    val r = baseRadius * idlePulse
                    // Outer subtle silver halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(LuxSilver.copy(alpha = 0.12f), Color.Transparent),
                            center = center,
                            radius = r * 1.3f
                        ),
                        radius = r * 1.3f,
                        center = center
                    )
                    // Deep obsidian orb with metallic rim
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF18181B), Color(0xFF09090B), LuxObsidian),
                            center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                            radius = r
                        ),
                        radius = r,
                        center = center
                    )
                    // Platinum delicate perimeter ring
                    drawCircle(
                        color = LuxBorderGlow.copy(alpha = 0.6f),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                    // Tiny orbiting ambient node
                    val angleRad = Math.toRadians(continuousRotation.toDouble())
                    val nodePos = Offset(
                        (center.x + (r * 1.12f) * cos(angleRad)).toFloat(),
                        (center.y + (r * 1.12f) * sin(angleRad)).toFloat()
                    )
                    drawCircle(color = LuxPlatinum.copy(alpha = 0.7f), radius = 2.5f, center = nodePos)
                }

                LuxOrbState.LISTENING -> {
                    val r = baseRadius * (1f + (audioAmplitude / 30f).coerceIn(0f, 0.2f))
                    // Expanding concentric soundwave ripples
                    drawCircle(
                        color = LuxPureWhite.copy(alpha = ((1.35f - listeningRipple) / 0.5f).coerceIn(0f, 0.45f)),
                        radius = baseRadius * listeningRipple,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                    drawCircle(
                        color = LuxSilver.copy(alpha = ((1.25f - (listeningRipple * 0.9f)) / 0.4f).coerceIn(0f, 0.35f)),
                        radius = baseRadius * (listeningRipple * 0.85f),
                        center = center,
                        style = Stroke(width = 1f)
                    )
                    // Focused luminous sphere
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF27272A), Color(0xFF141418), LuxObsidian),
                            center = center,
                            radius = r
                        ),
                        radius = r,
                        center = center
                    )
                    drawCircle(
                        color = LuxPureWhite,
                        radius = r,
                        center = center,
                        style = Stroke(width = 2.2f)
                    )
                    // Soundwave tick marks around circumference
                    for (i in 0 until 12) {
                        val angle = Math.toRadians((i * 30).toDouble())
                        val tickLen = 6f + (audioAmplitude.coerceIn(0f, 15f) * 0.8f)
                        val start = Offset(
                            (center.x + (r + 4f) * cos(angle)).toFloat(),
                            (center.y + (r + 4f) * sin(angle)).toFloat()
                        )
                        val end = Offset(
                            (center.x + (r + 4f + tickLen) * cos(angle)).toFloat(),
                            (center.y + (r + 4f + tickLen) * sin(angle)).toFloat()
                        )
                        drawLine(
                            color = LuxPureWhite.copy(alpha = 0.75f),
                            start = start,
                            end = end,
                            strokeWidth = 1.5f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                LuxOrbState.THINKING -> {
                    val r = baseRadius * 0.98f
                    // Dual orbiting luminous nodes circulating counter-clockwise & clockwise
                    val rad1 = Math.toRadians(continuousRotation.toDouble())
                    val rad2 = Math.toRadians(-continuousRotation.toDouble() + 180)

                    // Elliptical orbital tracks
                    drawCircle(
                        color = LuxBorderGlow.copy(alpha = 0.4f),
                        radius = r * 1.15f,
                        center = center,
                        style = Stroke(width = 1f)
                    )

                    // Core sphere
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF222226), Color(0xFF0F0F12), LuxObsidian),
                            center = center,
                            radius = r
                        ),
                        radius = r,
                        center = center
                    )
                    drawCircle(
                        color = LuxSilver.copy(alpha = 0.7f),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.8f)
                    )

                    // Orbiting node 1
                    val node1 = Offset(
                        (center.x + (r * 1.15f) * cos(rad1)).toFloat(),
                        (center.y + (r * 1.15f) * sin(rad1)).toFloat()
                    )
                    drawCircle(color = LuxPureWhite, radius = 3.8f, center = node1)
                    // Orbiting node 2
                    val node2 = Offset(
                        (center.x + (r * 1.15f) * cos(rad2)).toFloat(),
                        (center.y + (r * 1.15f) * sin(rad2)).toFloat()
                    )
                    drawCircle(color = LuxPlatinum, radius = 3.2f, center = node2)
                }

                LuxOrbState.SPEAKING -> {
                    val r = baseRadius * speakingPulse
                    // Harmonics expansion waves
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(LuxPureWhite.copy(alpha = 0.18f), Color.Transparent),
                            center = center,
                            radius = r * 1.4f
                        ),
                        radius = r * 1.4f,
                        center = center
                    )
                    // Dynamic pulsating metallic core
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF323238), Color(0xFF18181D), LuxObsidian),
                            center = center,
                            radius = r
                        ),
                        radius = r,
                        center = center
                    )
                    drawCircle(
                        color = LuxPureWhite,
                        radius = r,
                        center = center,
                        style = Stroke(width = 2.5f)
                    )
                    // Vocal wave arcs
                    for (i in 1..3) {
                        drawCircle(
                            color = LuxSilver.copy(alpha = 0.35f / i),
                            radius = r + (i * 10f * speakingPulse),
                            center = center,
                            style = Stroke(width = 1.2f)
                        )
                    }
                }

                LuxOrbState.PROCESSING -> {
                    val r = baseRadius * 0.95f
                    // Dense quantum ring spin
                    val angle = continuousRotation
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(Color.Transparent, LuxPureWhite, Color.Transparent),
                            center = center
                        ),
                        radius = r * 1.2f,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF27272A), LuxObsidian),
                            center = center,
                            radius = r
                        ),
                        radius = r,
                        center = center
                    )
                    drawCircle(
                        color = LuxPlatinum,
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                }

                LuxOrbState.ERROR -> {
                    val r = baseRadius * errorJitter
                    // Muted crimson/silver stabilization glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(LuxError.copy(alpha = 0.15f), Color.Transparent),
                            center = center,
                            radius = r * 1.25f
                        ),
                        radius = r * 1.25f,
                        center = center
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF1F1212), LuxObsidian),
                            center = center,
                            radius = r
                        ),
                        radius = r,
                        center = center
                    )
                    drawCircle(
                        color = LuxError.copy(alpha = 0.85f),
                        radius = r,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                }

                LuxOrbState.PERMISSION_REQUIRED -> {
                    val r = baseRadius * 0.96f
                    // Warning pulsing halo
                    drawCircle(
                        color = LuxWarning.copy(alpha = ((1.2f - warningBeacon) / 0.3f).coerceIn(0f, 0.4f)),
                        radius = baseRadius * warningBeacon,
                        center = center,
                        style = Stroke(width = 2f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF1C1A14), LuxObsidian),
                            center = center,
                            radius = r
                        ),
                        radius = r,
                        center = center
                    )
                    drawCircle(
                        color = LuxWarning.copy(alpha = 0.9f),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.8f)
                    )
                }
            }
        }
    }
}
