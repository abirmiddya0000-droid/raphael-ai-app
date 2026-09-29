package com.example.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Encapsulates the distinct states of SpeechRecognizer voice capture.
 */
sealed class VoiceState {
    object Idle : VoiceState()
    object Ready : VoiceState()
    data class Listening(val isStreaming: Boolean = false) : VoiceState()
    data class PartialResult(val text: String, val rmsdB: Float = 0f) : VoiceState()
    object Processing : VoiceState()
    data class Success(val recognizedText: String) : VoiceState()
    data class Error(val message: String, val errorCode: Int = -1) : VoiceState()
}

/**
 * Robust manager for Android's SpeechRecognizer API.
 * Captures user speech with live partial results and feeds directly into
 * the AI Butler processing loop.
 */
class VoiceInputManager(private val context: Context) {
    private var speechRecognizer: SpeechRecognizer? = null
    var currentState: VoiceState = VoiceState.Idle
        private set

    private var currentRms: Float = 0f

    /**
     * Checks if SpeechRecognizer service is available on this device.
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Checks whether RECORD_AUDIO permission has been granted.
     */
    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Initiates voice capture using the Android SpeechRecognizer API.
     * Runs strictly on the Main thread as required by Android speech recognition.
     */
    suspend fun startListening(
        onStateChanged: (VoiceState) -> Unit,
        onRmsChanged: ((Float) -> Unit)? = null
    ) = withContext(Dispatchers.Main) {
        if (!hasPermission()) {
            val err = VoiceState.Error("Microphone permission required for voice input.")
            currentState = err
            onStateChanged(err)
            return@withContext
        }

        if (!isAvailable()) {
            val err = VoiceState.Error("Speech recognition service is not available on this device.")
            currentState = err
            onStateChanged(err)
            return@withContext
        }

        // Clean up any stale recognizer instance before launching
        stopListening()

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        currentState = VoiceState.Listening(isStreaming = false)
                        onStateChanged(currentState)
                    }

                    override fun onBeginningOfSpeech() {
                        currentState = VoiceState.Listening(isStreaming = true)
                        onStateChanged(currentState)
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        currentRms = rmsdB
                        onRmsChanged?.invoke(rmsdB)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        currentState = VoiceState.Processing
                        onStateChanged(VoiceState.Processing)
                    }

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak clearly."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech input timed out."
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network issue during speech recognition."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition engine busy. Resetting..."
                            SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error."
                            SpeechRecognizer.ERROR_SERVER -> "Speech recognition server error."
                            else -> "Speech recognition error ($error)."
                        }
                        currentState = VoiceState.Error(message, errorCode = error)
                        onStateChanged(currentState)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()?.trim() ?: ""
                        if (partial.isNotEmpty()) {
                            currentState = VoiceState.PartialResult(partial, currentRms)
                            onStateChanged(currentState)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim() ?: ""
                        if (text.isNotEmpty()) {
                            currentState = VoiceState.Success(text)
                            onStateChanged(currentState)
                        } else {
                            currentState = VoiceState.Error("No clear speech detected.")
                            onStateChanged(currentState)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            // Build speech recognizer intent with streaming partials enabled
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                // Snappy pause detection (1.5s silence to trigger completion)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            val err = VoiceState.Error("Failed to initialize voice recognition: ${e.message}")
            currentState = err
            onStateChanged(err)
        }
    }

    /**
     * Halts speech recognition and safely destroys the active recognizer.
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        currentRms = 0f
        currentState = VoiceState.Idle
    }

    /**
     * Cancels active recognition without triggering results.
     */
    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        currentRms = 0f
        currentState = VoiceState.Idle
    }
}
