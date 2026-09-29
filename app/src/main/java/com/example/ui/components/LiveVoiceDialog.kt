package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.LuxBorderSubtle
import com.example.ui.theme.LuxDarkSurface
import com.example.ui.theme.LuxElevatedSurface
import com.example.ui.theme.LuxError
import com.example.ui.theme.LuxObsidian
import com.example.ui.theme.LuxPlatinum
import com.example.ui.theme.LuxPureWhite
import com.example.ui.theme.LuxSilver
import com.example.ui.theme.LuxTextMuted
import com.example.ui.theme.LuxTextSecondary
import com.example.voice.LiveSessionPhase
import com.example.voice.LiveSessionState

@Composable
fun LiveVoiceDialog(
    sessionState: LiveSessionState,
    onBargeIn: () -> Unit,
    onEndSession: () -> Unit
) {
    Dialog(
        onDismissRequest = onEndSession,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LuxObsidian.copy(alpha = 0.96f))
                .padding(24.dp)
        ) {
            // Close / End Session button at top right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(LuxDarkSurface)
                    .border(1.dp, LuxBorderSubtle, CircleShape)
                    .clickable(onClick = onEndSession)
                    .padding(10.dp)
                    .testTag("end_live_session_x_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "End Live Session",
                    tint = LuxPureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Minimalist animated wave visualizer active during user speech / listening
            LuxWaveVisualizer(
                isListening = sessionState.phase == LiveSessionPhase.LISTENING,
                audioAmplitude = sessionState.soundLevel,
                verticalBias = 0.44f,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LIVE TWO-WAY VOICE",
                        color = LuxSilver,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Headphone conversation active",
                        color = LuxTextMuted,
                        fontSize = 11.sp
                    )
                }

                // Center Orb reflecting live state
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        // Barge in on tap
                        if (sessionState.phase == LiveSessionPhase.SPEAKING) {
                            onBargeIn()
                        }
                    }
                ) {
                    val orbState = when (sessionState.phase) {
                        LiveSessionPhase.LISTENING -> LuxOrbState.LISTENING
                        LiveSessionPhase.THINKING -> LuxOrbState.THINKING
                        LiveSessionPhase.SPEAKING -> LuxOrbState.SPEAKING
                        LiveSessionPhase.ERROR -> LuxOrbState.ERROR
                        LiveSessionPhase.DISCONNECTED -> LuxOrbState.IDLE
                    }

                    LuxOrb(
                        state = orbState,
                        size = 220.dp,
                        audioAmplitude = sessionState.soundLevel
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    val phaseLabel = when (sessionState.phase) {
                        LiveSessionPhase.LISTENING -> "LISTENING TO YOU"
                        LiveSessionPhase.THINKING -> "DELIBERATING"
                        LiveSessionPhase.SPEAKING -> "LUX SPEAKING (TAP TO INTERRUPT)"
                        LiveSessionPhase.ERROR -> "SYSTEM ERROR"
                        LiveSessionPhase.DISCONNECTED -> "DISCONNECTED"
                    }

                    Text(
                        text = phaseLabel,
                        color = if (sessionState.phase == LiveSessionPhase.ERROR) LuxError else LuxPureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                // Live Transcripts
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LuxDarkSurface)
                        .border(1.dp, LuxBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (sessionState.lastUserSpeech.isNotEmpty()) {
                        Text(
                            text = "\"${sessionState.lastUserSpeech}\"",
                            color = LuxSilver,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (sessionState.lastLuxSpeech.isNotEmpty()) {
                        Text(
                            text = sessionState.lastLuxSpeech,
                            color = LuxPureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (sessionState.errorMessage != null) {
                        Text(
                            text = sessionState.errorMessage,
                            color = LuxError,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Bottom Stop Button
                Button(
                    onClick = onEndSession,
                    colors = ButtonDefaults.buttonColors(containerColor = LuxElevatedSurface),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .border(1.dp, LuxPureWhite.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .testTag("end_live_session_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = LuxPureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "End Conversation",
                        color = LuxPureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
