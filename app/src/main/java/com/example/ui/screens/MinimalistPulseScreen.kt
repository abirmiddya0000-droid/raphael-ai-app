package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CharacterPreset
import com.example.ui.components.LuxOrbState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Clean, minimalist Compose screen layout featuring a single central pulse animation
 * representing the AI butler's active listening, thinking, and speaking states.
 */
@Composable
fun MinimalistPulseScreen(
    character: CharacterPreset,
    orbState: LuxOrbState,
    statusText: String,
    audioAmplitude: Float,
    isHeadphoneActive: Boolean,
    onMicClick: () -> Unit,
    onHeadphoneClick: () -> Unit,
    onOpenCharacterModal: () -> Unit = {},
    onToggleMinimalistMode: () -> Unit,
    onTextSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val isListening = orbState == LuxOrbState.LISTENING

    val quickPrompts = listOf(
        "Notice: Battery check",
        "Report: System status",
        "Toggle flashlight",
        "Research AI architectures",
        "Clear cache"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030508))
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("minimalist_pulse_screen")
    ) {
        // Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Minimal Top HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // System Identity Title with live state dot (Clickable to switch persona)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenCharacterModal)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("minimal_character_selector_btn")
                ) {
                    val indicatorColor = when (orbState) {
                        LuxOrbState.LISTENING -> Color(0xFF00E5FF)
                        LuxOrbState.THINKING, LuxOrbState.PROCESSING -> Color(0xFF7C4DFF)
                        LuxOrbState.SPEAKING -> Color(0xFF18FFFF)
                        LuxOrbState.ERROR -> Color(0xFFFF5252)
                        else -> Color(0xFF00B0FF).copy(alpha = 0.5f)
                    }

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(indicatorColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${character.name.uppercase()} // CORE",
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.2.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Action buttons: Hologram switch & Live Session
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Headphone live mode indicator
                    IconButton(
                        onClick = onHeadphoneClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isHeadphoneActive) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                            .border(1.dp, if (isHeadphoneActive) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.10f), CircleShape)
                            .testTag("minimal_headphone_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Live Voice Session",
                            tint = if (isHeadphoneActive) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Switch back to Hologram mode
                    IconButton(
                        onClick = onToggleMinimalistMode,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
                            .testTag("switch_view_mode_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Switch View Mode",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Central Pulse Animation Layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // The Central Pulse Animation Component
                CentralPulseVisualizer(
                    orbState = orbState,
                    audioAmplitude = audioAmplitude,
                    onClick = onMicClick,
                    modifier = Modifier.size(280.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Minimalist State Headline
                val stateHeadline = when (orbState) {
                    LuxOrbState.LISTENING -> "LISTENING"
                    LuxOrbState.THINKING -> "THINKING..."
                    LuxOrbState.PROCESSING -> "PROCESSING..."
                    LuxOrbState.SPEAKING -> "REPORTING..."
                    LuxOrbState.ERROR -> "SYSTEM NOTICE"
                    LuxOrbState.PERMISSION_REQUIRED -> "PERMISSION REQUIRED"
                    LuxOrbState.IDLE -> "STANDBY"
                }

                Text(
                    text = stateHeadline,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.5.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.testTag("minimal_state_headline")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descriptive Status Subtext
                Text(
                    text = statusText.ifEmpty { "Tap core to initiate voice transmission" },
                    color = Color.White.copy(alpha = 0.50f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.8.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .testTag("minimal_status_subtext")
                )
            }

            // 3. Minimalist Bottom Input & Quick Prompts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quick Suggestion Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickPrompts) { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                .clickable { onTextSubmit(prompt) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = prompt,
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.4.sp
                            )
                        }
                    }
                }

                // Minimal Floating Input Dock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice Mic Action Button
                    IconButton(
                        onClick = onMicClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isListening) Color(0xFF00E5FF).copy(alpha = 0.20f) else Color.Transparent)
                            .testTag("minimal_mic_btn")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = if (isListening) "Listening active" else "Start Voice Input",
                            tint = if (isListening) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // TextField Input
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 10.dp)
                    ) {
                        if (textInput.isEmpty()) {
                            Text(
                                text = "Ask ${character.name}...",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                        BasicTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            ),
                            cursorBrush = SolidColor(Color(0xFF00E5FF)),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (textInput.isNotBlank()) {
                                        val query = textInput.trim()
                                        textInput = ""
                                        onTextSubmit(query)
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("minimal_text_input")
                        )
                    }

                    // Send Button
                    AnimatedVisibility(
                        visible = textInput.isNotBlank(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    val query = textInput.trim()
                                    textInput = ""
                                    onTextSubmit(query)
                                }
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF))
                                .testTag("minimal_send_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Send",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Single central pulse animation representing the AI butler's active listening or thinking states.
 * - LISTENING: Audio-reactive electric cyan concentric ripples propagating outwards with high fidelity.
 * - THINKING / PROCESSING: Rotational orbital phase-shift rings in electric violet & cyan.
 * - IDLE: Calm, serene meditative breathing pulse in deep sapphire.
 */
@Composable
fun CentralPulseVisualizer(
    orbState: LuxOrbState,
    audioAmplitude: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 280.dp
) {
    val isListening = orbState == LuxOrbState.LISTENING
    val isThinking = orbState == LuxOrbState.THINKING || orbState == LuxOrbState.PROCESSING
    val isSpeaking = orbState == LuxOrbState.SPEAKING

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")

    // 1. Listening fast pulse (850ms cycle)
    val listeningPulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "listening_pulse"
    )

    // 2. Thinking harmonic rotation (3200ms cycle)
    val thinkingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking_rotation"
    )

    // 3. Thinking breathing pulse (1800ms cycle)
    val thinkingBreathing by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinking_breathing"
    )

    // 4. Idle calm heartbeat (3000ms cycle)
    val idleBreathing by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_breathing"
    )

    // Color transition based on state
    val coreColor by animateColorAsState(
        targetValue = when {
            isListening -> Color(0xFF00E5FF)
            isThinking -> Color(0xFF7C4DFF)
            isSpeaking -> Color(0xFF00B0FF)
            orbState == LuxOrbState.ERROR -> Color(0xFFFF5252)
            else -> Color(0xFF00E5FF).copy(alpha = 0.85f)
        },
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "core_color"
    )

    val haloColor by animateColorAsState(
        targetValue = when {
            isListening -> Color(0xFF18FFFF)
            isThinking -> Color(0xFF651FFF)
            isSpeaking -> Color(0xFF00E5FF)
            orbState == LuxOrbState.ERROR -> Color(0xFFFF1744)
            else -> Color(0xFF0288D1).copy(alpha = 0.40f)
        },
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "halo_color"
    )

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("central_pulse_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension * 0.16f
            val maxRadius = this.size.minDimension * 0.46f

            // Dynamic scale factor based on amplitude
            val ampScale = if (isListening || isSpeaking) (1f + audioAmplitude.coerceIn(0f, 1f) * 0.45f) else 1f

            // --- Layer 1: Ambient Background Bloom ---
            val ambientBloomRadius = when {
                isListening -> maxRadius * 0.95f * ampScale
                isThinking -> maxRadius * 0.85f * thinkingBreathing
                else -> maxRadius * 0.70f * idleBreathing
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        haloColor.copy(alpha = if (isListening) 0.32f else 0.18f),
                        haloColor.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = ambientBloomRadius
                ),
                radius = ambientBloomRadius,
                center = center
            )

            // --- Layer 2: Concentric Harmonic Pulse Waves ---
            when {
                isListening -> {
                    // Active Listening: 3 propagating ripple rings
                    for (i in 0..2) {
                        val phase = (listeningPulseProgress + i * 0.333f) % 1f
                        val rippleRadius = baseRadius + phase * (maxRadius - baseRadius) * ampScale
                        val rippleAlpha = ((1f - phase) * 0.65f).coerceIn(0f, 1f)

                        drawCircle(
                            color = coreColor.copy(alpha = rippleAlpha),
                            radius = rippleRadius,
                            center = center,
                            style = Stroke(
                                width = (2.2f * (1f - phase * 0.5f)).dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }

                    // Radiating audio tick indicators (12 ticks around core)
                    val tickRadius = baseRadius * 1.55f * ampScale
                    val tickLength = 10f * (1f + audioAmplitude * 2f)
                    for (k in 0 until 12) {
                        val angle = (k * (360f / 12f)) * (PI.toFloat() / 180f)
                        val startX = center.x + cos(angle) * tickRadius
                        val startY = center.y + sin(angle) * tickRadius
                        val endX = center.x + cos(angle) * (tickRadius + tickLength)
                        val endY = center.y + sin(angle) * (tickRadius + tickLength)

                        drawLine(
                            color = coreColor.copy(alpha = 0.45f + audioAmplitude * 0.5f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 2f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                isThinking -> {
                    // Active Thinking: Counter-rotating orbital rings with harmonic phase nodes
                    rotate(thinkingRotation, pivot = center) {
                        // Ring 1 (Forward rotation)
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    coreColor.copy(alpha = 0.8f),
                                    haloColor.copy(alpha = 0.9f),
                                    Color.Transparent
                                ),
                                center = center
                            ),
                            radius = baseRadius * 1.6f * thinkingBreathing,
                            center = center,
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    rotate(-thinkingRotation * 1.4f, pivot = center) {
                        // Ring 2 (Counter rotation)
                        drawCircle(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    haloColor.copy(alpha = 0.7f),
                                    Color(0xFFE040FB).copy(alpha = 0.85f),
                                    Color.Transparent
                                ),
                                center = center
                            ),
                            radius = baseRadius * 2.2f * thinkingBreathing,
                            center = center,
                            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Orbital nodes (4 floating calculation quanta)
                    val orbitalDistance = baseRadius * 1.9f * thinkingBreathing
                    for (m in 0 until 4) {
                        val nodeAngle = (thinkingRotation * 0.8f + m * 90f) * (PI.toFloat() / 180f)
                        val nx = center.x + cos(nodeAngle) * orbitalDistance
                        val ny = center.y + sin(nodeAngle) * orbitalDistance
                        drawCircle(
                            color = Color(0xFF00E5FF),
                            radius = 3.5.dp.toPx(),
                            center = Offset(nx, ny)
                        )
                    }
                }

                else -> {
                    // Idle Calm Breathing State
                    val calmRadius1 = baseRadius * 1.45f * idleBreathing
                    val calmRadius2 = baseRadius * 1.95f * idleBreathing

                    drawCircle(
                        color = haloColor.copy(alpha = 0.20f),
                        radius = calmRadius1,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    drawCircle(
                        color = haloColor.copy(alpha = 0.10f),
                        radius = calmRadius2,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // --- Layer 3: Central Pulse Core Orb ---
            val currentCoreRadius = when {
                isListening -> baseRadius * ampScale
                isThinking -> baseRadius * thinkingBreathing
                isSpeaking -> baseRadius * (1f + audioAmplitude * 0.35f)
                else -> baseRadius * idleBreathing
            }

            // Outer Core Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreColor.copy(alpha = 0.95f),
                        coreColor.copy(alpha = 0.40f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentCoreRadius * 1.4f
                ),
                radius = currentCoreRadius * 1.4f,
                center = center
            )

            // Inner Core Solid Energy
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        coreColor,
                        haloColor
                    ),
                    center = center,
                    radius = currentCoreRadius
                ),
                radius = currentCoreRadius,
                center = center
            )

            // Center Micro Glyphs
            val glyphSize = currentCoreRadius * 0.32f
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(center.x - glyphSize, center.y),
                end = Offset(center.x + glyphSize, center.y),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(center.x, center.y - glyphSize),
                end = Offset(center.x, center.y + glyphSize),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}
