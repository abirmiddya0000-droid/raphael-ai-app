package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LuxPlatinum
import com.example.ui.theme.LuxPureWhite
import com.example.ui.theme.LuxSilver
import kotlin.math.PI
import kotlin.math.sin

/**
 * Minimalist, luxury animated wave visualizer.
 * Appears gracefully in the background when the AI butler is listening,
 * undulating harmonically and reacting dynamically to the user's voice amplitude.
 */
@Composable
fun LuxWaveVisualizer(
    isListening: Boolean,
    modifier: Modifier = Modifier,
    audioAmplitude: Float = 0f,
    verticalBias: Float = 0.42f // Centered around the central interaction orb
) {
    // Graceful fade in / fade out transition
    val visibilityAlpha by animateFloatAsState(
        targetValue = if (isListening) 1f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "wave_visibility_alpha"
    )

    if (visibilityAlpha <= 0.001f) return

    val transition = rememberInfiniteTransition(label = "lux_wave_transition")

    // Continuous wave phase shift 1 (Primary flow)
    val phase1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_1"
    )

    // Phase shift 2 (Secondary counter-harmonic flow)
    val phase2 by transition.animateFloat(
        initialValue = (2 * PI).toFloat(),
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_2"
    )

    // Phase shift 3 (Tertiary subtle micro-ripple)
    val phase3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_3"
    )

    // Smooth normalized voice boost from audio decibels (0 to ~15 dB typical from SpeechRecognizer)
    val voiceModulation = (audioAmplitude.coerceIn(0f, 16f) / 16f)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("lux_wave_visualizer")
    ) {
        val width = size.width
        val height = size.height
        val centerY = height * verticalBias

        val stepPx = 6f
        val pointsCount = (width / stepPx).toInt() + 1

        // Base amplitudes in pixels
        val baseAmp1 = (18.dp.toPx() + voiceModulation * 45.dp.toPx()) * visibilityAlpha
        val baseAmp2 = (12.dp.toPx() + voiceModulation * 32.dp.toPx()) * visibilityAlpha
        val baseAmp3 = (7.dp.toPx() + voiceModulation * 22.dp.toPx()) * visibilityAlpha

        // Wave 1: Primary fundamental wave
        val path1 = Path()
        // Wave 2: Harmonic resonant wave
        val path2 = Path()
        // Wave 3: Delicate fine overtone wave
        val path3 = Path()

        for (i in 0..pointsCount) {
            val x = i * stepPx
            val progress = (x / width).coerceIn(0f, 1f)

            // Hanning taper window so waves vanish cleanly near screen edges
            val envelope = sin(PI * progress).toFloat()

            // Harmonic wave equations
            val y1 = centerY + envelope * baseAmp1 * sin((progress * 2.2 * PI + phase1).toFloat())
            val y2 = centerY + envelope * baseAmp2 * sin((progress * 3.8 * PI + phase2).toFloat())
            val y3 = centerY + envelope * baseAmp3 * sin((progress * 5.2 * PI + phase3).toFloat())

            if (i == 0) {
                path1.moveTo(x, y1)
                path2.moveTo(x, y2)
                path3.moveTo(x, y3)
            } else {
                path1.lineTo(x, y1)
                path2.lineTo(x, y2)
                path3.lineTo(x, y3)
            }
        }

        // Horizontal gradient brushes with smooth alpha falloff at edges
        val brush1 = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                LuxPureWhite.copy(alpha = 0.28f * visibilityAlpha),
                LuxSilver.copy(alpha = 0.32f * visibilityAlpha),
                LuxPureWhite.copy(alpha = 0.28f * visibilityAlpha),
                Color.Transparent
            )
        )

        val brush2 = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                LuxSilver.copy(alpha = 0.16f * visibilityAlpha),
                LuxPlatinum.copy(alpha = 0.22f * visibilityAlpha),
                LuxSilver.copy(alpha = 0.16f * visibilityAlpha),
                Color.Transparent
            )
        )

        val brush3 = Brush.horizontalGradient(
            colors = listOf(
                Color.Transparent,
                LuxPureWhite.copy(alpha = 0.08f * visibilityAlpha),
                LuxSilver.copy(alpha = 0.12f * visibilityAlpha),
                Color.Transparent
            )
        )

        // Draw the 3 minimalist harmonic wave paths
        drawPath(
            path = path3,
            brush = brush3,
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = path2,
            brush = brush2,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = path1,
            brush = brush1,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
