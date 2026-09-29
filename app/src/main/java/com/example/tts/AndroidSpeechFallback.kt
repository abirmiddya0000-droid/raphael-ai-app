package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

class AndroidSpeechFallback(private val context: Context) : TTSProvider {
    override val name: String = "Android System TTS"

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingInitActions = mutableListOf<() -> Unit>()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    engine.language = Locale.US
                    engine.setPitch(1.0f)
                    engine.setSpeechRate(1.0f)
                }
                isInitialized = true
                pendingInitActions.forEach { it.invoke() }
                pendingInitActions.clear()
            }
        }
    }

    fun configureVoice(pitch: Float, speed: Float, isFemale: Boolean) {
        val action: () -> Unit = {
            tts?.let { engine ->
                engine.setPitch(pitch)
                engine.setSpeechRate(speed)
                try {
                    val voices = engine.voices
                    val matchingVoice = voices?.firstOrNull { voice ->
                        val nameLower = voice.name.lowercase()
                        if (isFemale) {
                            nameLower.contains("female") || nameLower.contains("woman") || nameLower.contains("f0")
                        } else {
                            nameLower.contains("male") || nameLower.contains("man") || nameLower.contains("m0")
                        }
                    }
                    if (matchingVoice != null) {
                        engine.voice = matchingVoice
                    }
                } catch (_: Exception) {}
            }
            Unit
        }

        if (isInitialized) {
            action.invoke()
        } else {
            pendingInitActions.add(action)
        }
    }

    override suspend fun synthesizeAndPlay(
        text: String,
        onStart: () -> Unit,
        onDone: () -> Unit
    ): TTSResult = withContext(Dispatchers.Main) {
        val engine = tts ?: return@withContext TTSResult.Error("Android TTS engine not initialized.")

        // Clean any metadata TTS tag so user does not hear "[TTS: ...]" aloud
        val cleanSpeechText = text.replace(Regex("""^\[TTS:[^\]]+\]\s*"""), "").trim()
        if (cleanSpeechText.isEmpty()) {
            onDone()
            return@withContext TTSResult.Success
        }

        val utteranceId = UUID.randomUUID().toString()
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                onStart()
            }

            override fun onDone(id: String?) {
                onDone()
            }

            override fun onError(id: String?) {
                onDone()
            }
        })

        val result = engine.speak(cleanSpeechText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.SUCCESS) {
            TTSResult.Success
        } else {
            TTSResult.Error("Failed to queue system speech synthesis ($result).")
        }
    }

    override fun stop() {
        tts?.stop()
    }

    override fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }
}
