package com.example.voice

import android.content.Context
import com.example.ai.AIMessage
import com.example.ai.AIRouter
import com.example.ai.AIRequest
import com.example.ai.AIResponseResult
import com.example.tts.TTSRouter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class LiveSessionPhase {
    DISCONNECTED,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

data class LiveSessionState(
    val phase: LiveSessionPhase = LiveSessionPhase.DISCONNECTED,
    val lastUserSpeech: String = "",
    val lastLuxSpeech: String = "",
    val errorMessage: String? = null,
    val soundLevel: Float = 0f
)

class LiveConversationManager(
    private val context: Context,
    private val voiceInputManager: VoiceInputManager,
    private val ttsRouter: TTSRouter,
    private val aiRouter: AIRouter,
    private val apiKeyProvider: () -> String,
    private val systemPromptProvider: () -> String
) {
    private val _sessionState = MutableStateFlow(LiveSessionState())
    val sessionState: StateFlow<LiveSessionState> = _sessionState.asStateFlow()

    private var sessionJob: Job? = null
    private var isSessionActive = false
    private val liveTurnHistory = mutableListOf<AIMessage>()

    fun isSessionRunning(): Boolean = isSessionActive

    fun startSession(scope: CoroutineScope) {
        if (isSessionActive) return
        isSessionActive = true
        liveTurnHistory.clear()
        _sessionState.value = LiveSessionState(phase = LiveSessionPhase.LISTENING)

        listenCycle(scope)
    }

    private fun listenCycle(scope: CoroutineScope) {
        if (!isSessionActive) return

        _sessionState.value = _sessionState.value.copy(
            phase = LiveSessionPhase.LISTENING,
            errorMessage = null
        )

        scope.launch(Dispatchers.Main) {
            voiceInputManager.startListening(
                onStateChanged = { voiceState ->
                    when (voiceState) {
                        is VoiceState.Listening -> {
                            _sessionState.value = _sessionState.value.copy(phase = LiveSessionPhase.LISTENING)
                        }
                        is VoiceState.PartialResult -> {
                            _sessionState.value = _sessionState.value.copy(
                                lastUserSpeech = voiceState.text,
                                soundLevel = voiceState.rmsdB
                            )
                        }
                        is VoiceState.Processing -> {
                            _sessionState.value = _sessionState.value.copy(phase = LiveSessionPhase.THINKING)
                        }
                        is VoiceState.Success -> {
                            handleUserSpeech(scope, voiceState.recognizedText)
                        }
                        is VoiceState.Error -> {
                            // If no match or timeout in continuous live mode, gracefully restart listening if session still active
                            if (isSessionActive) {
                                if (voiceState.message.contains("timed out", ignoreCase = true) ||
                                    voiceState.message.contains("No speech", ignoreCase = true)
                                ) {
                                    scope.launch {
                                        delay(400)
                                        listenCycle(scope)
                                    }
                                } else {
                                    _sessionState.value = _sessionState.value.copy(
                                        phase = LiveSessionPhase.ERROR,
                                        errorMessage = voiceState.message
                                    )
                                }
                            }
                        }
                        is VoiceState.Ready, is VoiceState.Idle -> {}
                    }
                },
                onRmsChanged = { rms ->
                    _sessionState.value = _sessionState.value.copy(soundLevel = rms)
                }
            )
        }
    }

    private fun handleUserSpeech(scope: CoroutineScope, userText: String) {
        if (!isSessionActive) return

        liveTurnHistory.add(AIMessage(role = "user", content = userText))
        _sessionState.value = _sessionState.value.copy(
            phase = LiveSessionPhase.THINKING,
            lastUserSpeech = userText
        )

        sessionJob = scope.launch(Dispatchers.IO) {
            val request = AIRequest(
                messages = liveTurnHistory.takeLast(6),
                systemInstruction = systemPromptProvider() + "\n(Note: You are in live voice headphone conversation. Respond naturally, concisely in 1-2 sharp spoken sentences. Do not use markdown or emojis.)",
                temperature = 0.5f,
                maxTokens = 250
            )

            val result = aiRouter.routeRequest(request, apiKeyProvider())

            if (!isSessionActive) return@launch

            when (result) {
                is AIResponseResult.Success -> {
                    val reply = result.text
                    liveTurnHistory.add(AIMessage(role = "lux", content = reply))

                    _sessionState.value = _sessionState.value.copy(
                        phase = LiveSessionPhase.SPEAKING,
                        lastLuxSpeech = reply
                    )

                    ttsRouter.speak(
                        text = reply,
                        onStart = {
                            if (isSessionActive) {
                                _sessionState.value = _sessionState.value.copy(phase = LiveSessionPhase.SPEAKING)
                            }
                        },
                        onDone = {
                            if (isSessionActive) {
                                // Once done speaking, immediately cycle back to listening!
                                listenCycle(scope)
                            }
                        },
                        onError = { err ->
                            if (isSessionActive) {
                                _sessionState.value = _sessionState.value.copy(
                                    phase = LiveSessionPhase.ERROR,
                                    errorMessage = "Audio error: $err"
                                )
                                // Fallback to listen cycle after delay
                                scope.launch {
                                    delay(1000)
                                    listenCycle(scope)
                                }
                            }
                        }
                    )
                }
                is AIResponseResult.Error -> {
                    _sessionState.value = _sessionState.value.copy(
                        phase = LiveSessionPhase.ERROR,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    /**
     * Interruption / Barge-in support:
     * Immediately stops LUX speech and switches directly back to listening.
     */
    fun bargeIn(scope: CoroutineScope) {
        if (!isSessionActive) return
        ttsRouter.stop()
        sessionJob?.cancel()
        listenCycle(scope)
    }

    fun endSession() {
        isSessionActive = false
        sessionJob?.cancel()
        voiceInputManager.stopListening()
        ttsRouter.stop()
        _sessionState.value = LiveSessionState(phase = LiveSessionPhase.DISCONNECTED)
    }
}
