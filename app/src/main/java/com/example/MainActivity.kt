package com.example

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.permissions.CapabilityStatus
import com.example.ui.LuxViewModel
import com.example.ui.components.CharacterSwitcherModal
import com.example.ui.components.LiveVoiceDialog
import com.example.ui.components.LuxBottomBar
import com.example.ui.components.LuxCyberBottomDock
import com.example.ui.components.LuxScreen
import com.example.ui.components.LuxTopBar
import com.example.ui.screens.CapabilitiesScreen
import com.example.ui.screens.ChatHistoryScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.LuxObsidian
import com.example.ui.theme.MyApplicationTheme
import com.example.voice.LiveSessionPhase

class MainActivity : ComponentActivity() {

    private val viewModel: LuxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                LuxAppContent(viewModel = viewModel, activity = this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshCapabilities()
    }
}

@Composable
fun LuxAppContent(viewModel: LuxViewModel, activity: ComponentActivity) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val activeMessages by viewModel.activeMessages.collectAsState()
    val activeConvId by viewModel.activeConversationId.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val capabilities by viewModel.capabilities.collectAsState()
    val liveSessionState by viewModel.liveSessionState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val soundLevel by viewModel.soundLevel.collectAsState()
    val selectedCharacter by viewModel.selectedCharacter.collectAsState()
    val isCharacterModalOpen by viewModel.isCharacterModalOpen.collectAsState()

    // Runtime Permission Launchers
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.refreshCapabilities()
        if (isGranted) {
            viewModel.startMicrophoneInput()
        } else {
            Toast.makeText(activity, "Microphone permission is required for voice input.", Toast.LENGTH_SHORT).show()
        }
    }

    val headphoneMicPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.refreshCapabilities()
        if (isGranted) {
            viewModel.toggleHeadphoneSession()
        } else {
            Toast.makeText(activity, "Microphone permission is required for live voice conversation.", Toast.LENGTH_SHORT).show()
        }
    }

    val genericPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.refreshCapabilities()
    }

    // MediaProjection screen capture launcher
    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            Toast.makeText(activity, "Screen Capture session authorized.", Toast.LENGTH_SHORT).show()
            viewModel.refreshCapabilities()
        } else {
            Toast.makeText(activity, "Screen capture was declined.", Toast.LENGTH_SHORT).show()
        }
    }

    // Hardware back handler returns to Home screen if on another screen
    BackHandler(enabled = currentScreen != LuxScreen.HOME) {
        viewModel.navigateTo(LuxScreen.HOME)
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            if (currentScreen != LuxScreen.HOME) {
                LuxTopBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        bottomBar = {
            LuxCyberBottomDock(
                currentScreen = currentScreen,
                isListening = orbState == com.example.ui.components.LuxOrbState.LISTENING,
                onNavigate = { viewModel.navigateTo(it) },
                onMicClick = {
                    val cap = capabilities.find { it.id == "CAP_MIC" }
                    if (cap?.status == CapabilityStatus.GRANTED) {
                        viewModel.startMicrophoneInput()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(LuxObsidian)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(LuxObsidian)
        ) {
            when (currentScreen) {
                LuxScreen.HOME -> {
                    HomeScreen(
                        character = selectedCharacter,
                        orbState = orbState,
                        statusText = statusText,
                        isHeadphoneActive = liveSessionState.phase != LiveSessionPhase.DISCONNECTED,
                        audioAmplitude = soundLevel,
                        onOrbClick = {
                            val cap = capabilities.find { it.id == "CAP_MIC" }
                            if (cap?.status == CapabilityStatus.GRANTED) {
                                viewModel.startMicrophoneInput()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onMicClick = {
                            val cap = capabilities.find { it.id == "CAP_MIC" }
                            if (cap?.status == CapabilityStatus.GRANTED) {
                                viewModel.startMicrophoneInput()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onOpenCharacterModal = {
                            viewModel.openCharacterModal()
                        },
                        onOpenNotifications = {
                            viewModel.navigateTo(LuxScreen.TRIGGERS)
                        },
                        onHeadphoneClick = {
                            val cap = capabilities.find { it.id == "CAP_MIC" }
                            if (cap?.status == CapabilityStatus.GRANTED) {
                                viewModel.toggleHeadphoneSession()
                            } else {
                                headphoneMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onTextSubmit = { instruction ->
                            viewModel.processInstruction(instruction)
                            viewModel.navigateTo(LuxScreen.CHAT)
                        }
                    )
                }

                LuxScreen.CHAT -> {
                    val currentConv = conversations.find { it.id == activeConvId }
                    val convTitle = currentConv?.title ?: "Briefing"
                    ChatScreen(
                        messages = activeMessages,
                        isLoading = isLoading,
                        currentModelBadge = viewModel.activeModel,
                        conversationTitle = convTitle,
                        onSendMessage = { viewModel.processInstruction(it) },
                        onNewConversation = { viewModel.startNewConversation() },
                        onBack = { viewModel.navigateTo(LuxScreen.HOME) },
                        onOpenHistory = { viewModel.navigateTo(LuxScreen.HISTORY) },
                        onSpeakMessage = { viewModel.speakText(it) },
                        onCopyMessage = { viewModel.copyMessageToClipboard(it) },
                        onDeleteMessage = { viewModel.deleteMessage(it) },
                        onRetry = { viewModel.retryLastMessage() }
                    )
                }

                LuxScreen.TRIGGERS -> {
                    CapabilitiesScreen(
                        capabilities = capabilities,
                        onRefresh = { viewModel.refreshCapabilities() },
                        onRequestPermission = { cap ->
                            when (cap.id) {
                                "CAP_MIC" -> genericPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                "CAP_CAMERA" -> genericPermissionLauncher.launch(Manifest.permission.CAMERA)
                                "CAP_NOTIF" -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        genericPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.permissionManager.openSpecialAccessSettings(cap.id)
                                    }
                                }
                                "CAP_LOCATION" -> genericPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                "CAP_SCREEN_CAPTURE" -> {
                                    val mpManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                                    if (mpManager != null) {
                                        screenCaptureLauncher.launch(mpManager.createScreenCaptureIntent())
                                    }
                                }
                                "CAP_OVERLAY" -> {
                                    viewModel.toggleFloatingOrb(activity, true)
                                }
                                else -> {
                                    viewModel.permissionManager.openSpecialAccessSettings(cap.id)
                                }
                            }
                        }
                    )
                }

                LuxScreen.HISTORY -> {
                    ChatHistoryScreen(
                        conversations = conversations,
                        activeConversationId = activeConvId,
                        onSelectConversation = { viewModel.selectConversation(it) },
                        onNewConversation = { viewModel.startNewConversation() },
                        onDeleteConversation = { viewModel.deleteConversation(it) },
                        onClearAllConversations = { viewModel.clearAllConversations() },
                        onVerifyOwnerPin = { viewModel.verifyOwnerPin(it) },
                        onBack = { viewModel.navigateTo(LuxScreen.CHAT) }
                    )
                }

                LuxScreen.SETTINGS -> {
                    SettingsScreen(
                        capabilities = capabilities,
                        memories = memories,
                        isMemoryEnabled = true,
                        ownerName = viewModel.ownerName,
                        totalInputTokens = viewModel.totalInputTokens,
                        totalOutputTokens = viewModel.totalOutputTokens,
                        activeTTSProvider = viewModel.activeTTSProvider,
                        activeModel = viewModel.activeModel,
                        selectedCharacter = selectedCharacter,
                        onSelectCharacter = { viewModel.selectCharacter(it) },
                        onOpenCharacterModal = { viewModel.openCharacterModal() },
                        onRefreshCapabilities = { viewModel.refreshCapabilities() },
                        onRequestPermission = { cap ->
                            when (cap.id) {
                                "CAP_MIC" -> genericPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                "CAP_CAMERA" -> genericPermissionLauncher.launch(Manifest.permission.CAMERA)
                                "CAP_NOTIF" -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        genericPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.permissionManager.openSpecialAccessSettings(cap.id)
                                    }
                                }
                                "CAP_LOCATION" -> genericPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                "CAP_SCREEN_CAPTURE" -> {
                                    val mpManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                                    if (mpManager != null) {
                                        screenCaptureLauncher.launch(mpManager.createScreenCaptureIntent())
                                    }
                                }
                                "CAP_OVERLAY" -> {
                                    viewModel.toggleFloatingOrb(activity, true)
                                }
                                else -> {
                                    viewModel.permissionManager.openSpecialAccessSettings(cap.id)
                                }
                            }
                        },
                        onToggleMemory = { viewModel.toggleMemoryRetention(it) },
                        onAddMemory = { cat, k, v -> viewModel.addMemory(cat, k, v) },
                        onDeleteMemory = { viewModel.deleteMemory(it) },
                        onClearAllMemories = { viewModel.clearAllMemories() }
                    )
                }
            }

            // Live Voice Headphone Session Modal (Visible when user activates Headphone conversation)
            if (liveSessionState.phase != LiveSessionPhase.DISCONNECTED) {
                LiveVoiceDialog(
                    sessionState = liveSessionState,
                    onBargeIn = { viewModel.bargeInLiveSession() },
                    onEndSession = { viewModel.endLiveSession() }
                )
            }

            // Character Persona Switcher Modal Overlay
            CharacterSwitcherModal(
                isOpen = isCharacterModalOpen,
                selectedCharacterId = selectedCharacter.id,
                onSelectCharacter = { viewModel.selectCharacter(it) },
                onDismiss = { viewModel.closeCharacterModal() }
            )
        }
    }
}
