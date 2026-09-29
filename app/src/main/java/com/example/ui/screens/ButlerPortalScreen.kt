package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CharacterPreset
import com.example.ui.components.BlueEdgeGlowOverlay
import com.example.ui.components.CyberVisualizerOrb
import com.example.ui.components.LuxOrbState
import com.example.ui.components.LuxWaveVisualizer
import com.example.ui.components.RaphaelFlowerOrb
import com.example.ui.components.RaphaelState
import com.example.ui.theme.LuxError
import com.example.ui.theme.LuxNeonCrimson
import com.example.ui.theme.LuxNeonLimeAlpha
import com.example.ui.theme.LuxNeonRed
import com.example.ui.theme.LuxObsidian
import com.example.ui.theme.LuxPureWhite
import com.example.ui.theme.LuxSilver
import com.example.ui.theme.LuxWarning

/**
 * Cyberpunk Anime AI Assistant portal screen matching the user's HTML/CSS specification.
 * Features:
 * - Dynamic character background with vignette gradient overlays
 * - Top header with circular frosted icon buttons (hamburger & bell) and styled user greeting
 * - Glowing audio visualizer orb with pulse rings, center diamond glyph, and equalizer dots
 * - Animated wave visualizer responding in background
 * - Minimalist quick action bar
 */
@Composable
fun ButlerPortalScreen(
    character: CharacterPreset,
    orbState: LuxOrbState,
    statusText: String,
    audioAmplitude: Float = 0f,
    isHeadphoneActive: Boolean = false,
    onCentralButtonClick: () -> Unit,
    onMicClick: () -> Unit,
    onOpenCharacterModal: () -> Unit,
    onOpenNotifications: () -> Unit,
    onHeadphoneClick: () -> Unit = {},
    onToggleMinimalistMode: () -> Unit = {},
    onTextSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val isListening = orbState == LuxOrbState.LISTENING || isHeadphoneActive

    // Container with neon rim border glow matching mobile-frame
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LuxObsidian)
            .border(1.5.dp, LuxNeonLimeAlpha, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .testTag("butler_portal_screen")
    ) {
        // 1. Dynamic Character Background Image
        AsyncImage(
            model = character.imageUrl,
            contentDescription = character.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .testTag("character_bg_image")
        )

        // 2. Darkening & Vignette Gradient Overlays
        // Center radial darkening
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.88f)
                        ),
                        radius = 800f
                    )
                )
        )

        // Top to bottom contrast vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.72f),
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.85f),
                            Color.Black.copy(alpha = 0.98f)
                        )
                    )
                )
        )

        // 3. Minimalist Harmonic Wave Visualizer
        LuxWaveVisualizer(
            isListening = isListening,
            audioAmplitude = audioAmplitude,
            verticalBias = 0.46f,
            modifier = Modifier.fillMaxSize()
        )

        // Electric Blue Glow Screen-Edge Aura
        BlueEdgeGlowOverlay(
            isActive = isListening || orbState == LuxOrbState.SPEAKING || orbState == LuxOrbState.THINKING,
            soundLevel = audioAmplitude,
            modifier = Modifier.fillMaxSize()
        )

        // 4. Foreground Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Header: Hamburger Menu + User Greeting + Bell Notification Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    // Circular Glassmorphic Menu Button (Opens Character Selector Modal)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clickable(onClick = onOpenCharacterModal)
                            .testTag("header_menu_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Choose Character",
                            tint = LuxPureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Dynamic User & Character Greeting (Clickable to switch character)
                    Column(
                        modifier = Modifier
                            .clickable(onClick = onOpenCharacterModal)
                            .testTag("header_greeting_block")
                    ) {
                        Text(
                            text = character.greetingFirstLine,
                            color = LuxPureWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 26.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = character.greetingSecondLine,
                            color = LuxPureWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 26.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "How can I assist you today?",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Tap to change",
                                color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Minimalist Pulse Screen Toggle Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clickable(onClick = onToggleMinimalistMode)
                            .testTag("header_minimalist_mode_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Minimalist Pulse Mode",
                            tint = LuxPureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Notification Bell Icon Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clickable(onClick = onOpenNotifications)
                            .testTag("header_bell_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications & Triggers",
                            tint = LuxPureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Center Audio Visualizer & AI Voice Orb
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val raphaelState = when (orbState) {
                    LuxOrbState.IDLE -> RaphaelState.IDLE
                    LuxOrbState.LISTENING -> RaphaelState.LISTENING
                    LuxOrbState.THINKING -> RaphaelState.THINKING
                    LuxOrbState.SPEAKING -> RaphaelState.SPEAKING
                    LuxOrbState.PROCESSING -> RaphaelState.PROCESSING
                    LuxOrbState.ERROR -> RaphaelState.ERROR
                    LuxOrbState.PERMISSION_REQUIRED -> RaphaelState.PERMISSION
                }

                RaphaelFlowerOrb(
                    state = raphaelState,
                    soundLevel = audioAmplitude,
                    size = 230.dp,
                    onClick = onMicClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Cognitive Status Pill
                val pillBorderColor = when (orbState) {
                    LuxOrbState.ERROR -> LuxError
                    LuxOrbState.PERMISSION_REQUIRED -> LuxWarning
                    LuxOrbState.LISTENING, LuxOrbState.SPEAKING -> Color(0xFF00E5FF)
                    else -> Color.White.copy(alpha = 0.20f)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, pillBorderColor, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("lux_status_pill")
                ) {
                    Text(
                        text = statusText,
                        color = LuxSilver,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bottom Quick Text Command Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 76.dp) // Leaves space for the floating bottom dock
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black.copy(alpha = 0.70f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Ask ${character.name}...",
                                color = Color.White.copy(alpha = 0.40f),
                                fontSize = 13.5.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = LuxPureWhite,
                            unfocusedTextColor = LuxPureWhite
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    onTextSubmit(inputText)
                                    inputText = ""
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_instruction_input")
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onTextSubmit(inputText)
                                inputText = ""
                                focusManager.clearFocus()
                            } else {
                                onMicClick()
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) LuxNeonRed else Color(0xFF00E5FF).copy(alpha = 0.25f))
                            .testTag("home_submit_button")
                    ) {
                        Icon(
                            imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                            contentDescription = if (inputText.isNotBlank()) "Send" else "Voice Input",
                            tint = LuxPureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
