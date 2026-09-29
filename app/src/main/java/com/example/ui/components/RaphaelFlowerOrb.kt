package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Living states for RAPHAEL's sacred matrix core.
 */
enum class RaphaelState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    PROCESSING,
    ERROR,
    PERMISSION
}

/**
 * Sacred Flower Sigil Orb: Central circular living orb featuring Raphael's sacred
 * geometric flower (rotating concentric circles and 8 geometric petals in Canvas).
 * Inspired by the Wisdom King / Lord of Wisdom matrix from Tensura.
 */
@Composable
fun RaphaelFlowerOrb(
    state: RaphaelState,
    soundLevel: Float = 0f, // 0f to 10f+ dB input
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "RaphaelOrbTransition")

    // Dynamic rotation speeds based on cognitive state
    val rotationDuration = when (state) {
        RaphaelState.THINKING -> 2800
        RaphaelState.PROCESSING -> 3600
        RaphaelState.LISTENING -> 5000
        RaphaelState.SPEAKING -> 4000
        RaphaelState.ERROR -> 8000
        RaphaelState.PERMISSION -> 4500
        RaphaelState.IDLE -> 12000
    }

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sigil_rotation"
    )

    val counterRotationAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (rotationDuration * 1.5).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    // Breathing pulse for outer aura
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (state == RaphaelState.SPEAKING || state == RaphaelState.LISTENING) 700 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sigil_pulse"
    )

    // Color theme matching the requested Electric Blue Glow & living states
    val coreColor = when (state) {
        RaphaelState.IDLE -> Color(0xFF00E5FF)       // Electric Cyan-Blue
        RaphaelState.LISTENING -> Color(0xFF00F0FF)  // Bright Cyan
        RaphaelState.THINKING -> Color(0xFF38BDF8)   // Azure Deliberation
        RaphaelState.SPEAKING -> Color(0xFF00E5FF)   // Resonant Electric Blue
        RaphaelState.PROCESSING -> Color(0xFF818CF8) // Deep Wisdom Violet-Blue
        RaphaelState.ERROR -> Color(0xFFFF0033)      // Crimson Alert
        RaphaelState.PERMISSION -> Color(0xFFFBBF24) // Royal Amber
    }

    val glowColor = when (state) {
        RaphaelState.IDLE -> Color(0x6600E5FF)
        RaphaelState.LISTENING -> Color(0x9900F0FF)
        RaphaelState.THINKING -> Color(0x8838BDF8)
        RaphaelState.SPEAKING -> Color(0xAA00E5FF)
        RaphaelState.PROCESSING -> Color(0x88818CF8)
        RaphaelState.ERROR -> Color(0x99FF0033)
        RaphaelState.PERMISSION -> Color(0x88FBBF24)
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            )
            .testTag("raphael_flower_orb"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension / 2f) * 0.92f

            // Sound modulation factor
            val audioBoost = (soundLevel.coerceIn(0f, 10f) / 10f) * 0.18f
            val dynamicScale = pulseScale + audioBoost

            // 1. Ambient outer aura radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor,
                        glowColor.copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * dynamicScale * 1.15f
                ),
                radius = radius * dynamicScale * 1.15f,
                center = center
            )

            // 2. Concentric geometric boundary ring (Wisdom Matrix boundary)
            drawCircle(
                color = coreColor.copy(alpha = 0.35f),
                radius = radius * 0.95f,
                center = center,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            )

            // 3. Counter-rotating outer tick-marks ring
            rotate(degrees = counterRotationAngle, pivot = center) {
                val tickRingRadius = radius * 0.88f
                drawCircle(
                    color = coreColor.copy(alpha = 0.5f),
                    radius = tickRingRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // 16 radial matrix ticks
                for (i in 0 until 16) {
                    val angleRad = (i * 22.5 * PI / 180.0).toFloat()
                    val startDist = tickRingRadius - (if (i % 2 == 0) 8.dp.toPx() else 4.dp.toPx())
                    val endDist = tickRingRadius
                    val startX = center.x + startDist * cos(angleRad)
                    val startY = center.y + startDist * sin(angleRad)
                    val endX = center.x + endDist * cos(angleRad)
                    val endY = center.y + endDist * sin(angleRad)

                    drawLine(
                        color = coreColor.copy(alpha = if (i % 4 == 0) 0.8f else 0.4f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (i % 4 == 0) 2.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 4. Sacred Flower 8-Petal Geometry (Rotating)
            rotate(degrees = rotationAngle, pivot = center) {
                drawSacredFlowerPetals(
                    center = center,
                    petalRadius = radius * 0.72f * dynamicScale,
                    color = coreColor
                )
            }

            // 5. Inner Counter-Rotating Hexagram/Octagram Ring
            rotate(degrees = counterRotationAngle * 1.25f, pivot = center) {
                val innerRingRadius = radius * 0.42f
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = innerRingRadius,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                )
            }

            // 6. Central Wisdom Seed Diamond Core
            drawCentralDiamondCore(
                center = center,
                coreSize = radius * 0.28f * (1f + audioBoost * 0.5f),
                color = coreColor
            )
        }
    }
}

/**
 * Draws the 8 sacred geometric flower petals of Raphael with curved bezier precision.
 */
private fun DrawScope.drawSacredFlowerPetals(
    center: Offset,
    petalRadius: Float,
    color: Color
) {
    val petalStrokeWidth = 1.8.dp.toPx()

    for (i in 0 until 8) {
        val angleDeg = i * 45f
        rotate(degrees = angleDeg, pivot = center) {
            val path = Path().apply {
                moveTo(center.x, center.y)
                // Left curve outward to petal tip
                cubicTo(
                    center.x - petalRadius * 0.35f, center.y - petalRadius * 0.45f,
                    center.x - petalRadius * 0.2f, center.y - petalRadius * 0.9f,
                    center.x, center.y - petalRadius
                )
                // Right curve back to center
                cubicTo(
                    center.x + petalRadius * 0.2f, center.y - petalRadius * 0.9f,
                    center.x + petalRadius * 0.35f, center.y - petalRadius * 0.45f,
                    center.x, center.y
                )
                close()
            }

            // Translucent petal fill
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.45f),
                        color.copy(alpha = 0.08f)
                    ),
                    startY = center.y - petalRadius,
                    endY = center.y
                )
            )

            // Petal crisp outer edge
            drawPath(
                path = path,
                color = color.copy(alpha = 0.85f),
                style = Stroke(width = petalStrokeWidth, cap = StrokeCap.Round)
            )

            // Inner harmonic node dot at petal tip
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = Offset(center.x, center.y - petalRadius * 0.82f)
            )
        }
    }
}

/**
 * Draws the central diamond glyph representing the Lord of Wisdom core seed.
 */
private fun DrawScope.drawCentralDiamondCore(
    center: Offset,
    coreSize: Float,
    color: Color
) {
    // Glowing central core fill
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, color, Color.Transparent),
            center = center,
            radius = coreSize * 1.1f
        ),
        radius = coreSize * 1.1f,
        center = center
    )

    // Sharp central diamond
    val diamondPath = Path().apply {
        moveTo(center.x, center.y - coreSize)
        lineTo(center.x + coreSize * 0.65f, center.y)
        lineTo(center.x, center.y + coreSize)
        lineTo(center.x - coreSize * 0.65f, center.y)
        close()
    }

    drawPath(
        path = diamondPath,
        color = Color.White.copy(alpha = 0.95f),
        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Center focal point
    drawCircle(
        color = Color.White,
        radius = 3.dp.toPx(),
        center = center
    )
}
