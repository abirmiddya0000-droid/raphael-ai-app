package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.CharacterPreset
import com.example.ui.components.LuxOrbState

/**
 * Primary Home Screen featuring both the cyberpunk anime AI assistant portal
 * and the clean, minimalist Compose screen layout with a central pulse animation.
 */
@Composable
fun HomeScreen(
    character: CharacterPreset,
    orbState: LuxOrbState,
    statusText: String,
    isHeadphoneActive: Boolean,
    audioAmplitude: Float = 0f,
    onOrbClick: () -> Unit,
    onMicClick: () -> Unit,
    onOpenCharacterModal: () -> Unit,
    onOpenNotifications: () -> Unit,
    onHeadphoneClick: () -> Unit = {},
    onTextSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMinimalistMode by rememberSaveable { mutableStateOf(false) }

    if (isMinimalistMode) {
        MinimalistPulseScreen(
            character = character,
            orbState = orbState,
            statusText = statusText,
            audioAmplitude = audioAmplitude,
            isHeadphoneActive = isHeadphoneActive,
            onMicClick = onMicClick,
            onHeadphoneClick = onHeadphoneClick,
            onOpenCharacterModal = onOpenCharacterModal,
            onToggleMinimalistMode = { isMinimalistMode = false },
            onTextSubmit = onTextSubmit,
            modifier = modifier
        )
    } else {
        ButlerPortalScreen(
            character = character,
            orbState = orbState,
            statusText = statusText,
            audioAmplitude = audioAmplitude,
            isHeadphoneActive = isHeadphoneActive,
            onCentralButtonClick = onOrbClick,
            onMicClick = onMicClick,
            onOpenCharacterModal = onOpenCharacterModal,
            onOpenNotifications = onOpenNotifications,
            onHeadphoneClick = onHeadphoneClick,
            onToggleMinimalistMode = { isMinimalistMode = true },
            onTextSubmit = onTextSubmit,
            modifier = modifier
        )
    }
}
