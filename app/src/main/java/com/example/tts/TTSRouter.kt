package com.example.tts

import android.content.Context
import com.example.data.CharacterPreset
import com.example.data.PreferencesManager

class TTSRouter(
    context: Context,
    apiKeyProvider: () -> String,
    private val preferencesManager: PreferencesManager
) {
    val gemini38FlashLiteTTS: TTSProvider = GeminiTTSProvider(
        context = context,
        apiKeyProvider = apiKeyProvider,
        name = "Gemini 3.8 Flash-Lite TTS",
        modelEndpoint = "gemini-3.8-flash-lite-tts",
        fallbackEndpoint = "gemini-2.5-flash-preview-tts",
        voiceName = "Aoede"
    )

    val geminiFlashLiteTTS: TTSProvider = gemini38FlashLiteTTS

    val geminiFlashTTS: TTSProvider = GeminiTTSProvider(
        context = context,
        apiKeyProvider = apiKeyProvider,
        name = "Gemini Flash TTS",
        modelEndpoint = "gemini-2.5-flash-preview-tts",
        voiceName = "Charon"
    )

    val androidSpeechFallback = AndroidSpeechFallback(context)

    var activeProviderName: String = gemini38FlashLiteTTS.name
        private set

    fun configureVoiceForPersona(persona: CharacterPreset) {
        androidSpeechFallback.configureVoice(
            pitch = persona.ttsPitch,
            speed = persona.ttsSpeed,
            isFemale = persona.isFemaleVoice
        )
    }

    suspend fun speak(
        text: String,
        onStart: () -> Unit,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!preferencesManager.isTtsEnabled) {
            onDone()
            return
        }

        // Clean any metadata tag for speech synthesis
        val cleanSpeechText = text.replace(Regex("""^\[TTS:[^\]]+\]\s*"""), "").trim()
        if (cleanSpeechText.isEmpty()) {
            onDone()
            return
        }

        val providersToTry: List<TTSProvider> = when (preferencesManager.ttsProvider) {
            "SYSTEM" -> listOf(androidSpeechFallback, gemini38FlashLiteTTS)
            "GEMINI_FLASH" -> listOf(geminiFlashTTS, gemini38FlashLiteTTS, androidSpeechFallback)
            else -> listOf(gemini38FlashLiteTTS, geminiFlashTTS, androidSpeechFallback)
        }

        var lastError = "TTS audio synthesis failed."

        for (provider in providersToTry) {
            activeProviderName = provider.name
            val result = provider.synthesizeAndPlay(cleanSpeechText, onStart, onDone)
            when (result) {
                is TTSResult.Success -> return
                is TTSResult.Error -> {
                    lastError = "${provider.name}: ${result.message}"
                }
            }
        }

        onError(lastError)
    }

    fun stop() {
        geminiFlashLiteTTS.stop()
        geminiFlashTTS.stop()
        androidSpeechFallback.stop()
    }

    fun release() {
        geminiFlashLiteTTS.release()
        geminiFlashTTS.release()
        androidSpeechFallback.release()
    }
}
