package com.example.tts

import android.content.Context
import android.media.MediaPlayer
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiTTSProvider(
    private val context: Context,
    private val apiKeyProvider: () -> String,
    override val name: String = "Gemini 3.8 Flash-Lite TTS",
    private val modelEndpoint: String = "gemini-3.8-flash-lite-tts",
    private val fallbackEndpoint: String = "gemini-2.5-flash-preview-tts",
    private val voiceName: String = "Charon" // Confident, male voice
) : TTSProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private var mediaPlayer: MediaPlayer? = null
    private var tempAudioFile: File? = null

    override suspend fun synthesizeAndPlay(
        text: String,
        onStart: () -> Unit,
        onDone: () -> Unit
    ): TTSResult = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) {
            return@withContext TTSResult.Error("Gemini API key missing for Cloud TTS.")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelEndpoint:generateContent?key=$apiKey"

        val payload = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", text) })
                    })
                })
            }
            put("contents", contentsArray)

            val speechConfig = JSONObject().apply {
                put("voiceConfig", JSONObject().apply {
                    put("prebuiltVoiceConfig", JSONObject().apply {
                        put("voiceName", voiceName)
                    })
                })
            }

            val genConfig = JSONObject().apply {
                val modalities = JSONArray().apply { put("AUDIO") }
                put("responseModalities", modalities)
                put("speechConfig", speechConfig)
            }
            put("generationConfig", genConfig)
        }

        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()

        try {
            var activeResponse = client.newCall(request).execute()
            if (!activeResponse.isSuccessful && (activeResponse.code == 404 || activeResponse.code == 400) && modelEndpoint != fallbackEndpoint) {
                activeResponse.close()
                val fallbackUrl = "https://generativelanguage.googleapis.com/v1beta/models/$fallbackEndpoint:generateContent?key=$apiKey"
                val fallbackRequest = Request.Builder().url(fallbackUrl).post(requestBody).build()
                activeResponse = client.newCall(fallbackRequest).execute()
            }

            activeResponse.use { response ->
                if (!response.isSuccessful) {
                    val code = response.code
                    return@withContext TTSResult.Error("Cloud TTS request failed with HTTP $code.")
                }

                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                val candidates = json.optJSONArray("candidates")
                val firstPart = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)

                val inlineData = firstPart?.optJSONObject("inlineData")
                val base64Data = inlineData?.optString("data")

                if (base64Data.isNullOrBlank()) {
                    return@withContext TTSResult.Error("Cloud TTS returned no audio data.")
                }

                val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)

                withContext(Dispatchers.Main) {
                    playAudioBytes(audioBytes, onStart, onDone)
                }
                TTSResult.Success
            }
        } catch (e: IOException) {
            TTSResult.Error("Cloud TTS network error: ${e.message ?: "Connection failure"}")
        } catch (e: Exception) {
            TTSResult.Error("Cloud TTS error: ${e.localizedMessage ?: "Unknown failure"}")
        }
    }

    private fun playAudioBytes(
        bytes: ByteArray,
        onStart: () -> Unit,
        onDone: () -> Unit
    ) {
        try {
            stop()
            val tempFile = File.createTempFile("lux_tts_", ".wav", context.cacheDir)
            tempAudioFile = tempFile
            FileOutputStream(tempFile).use { it.write(bytes) }

            mediaPlayer = MediaPlayer().apply {
                setDataSource(tempFile.absolutePath)
                setOnPreparedListener { mp ->
                    onStart()
                    mp.start()
                }
                setOnCompletionListener {
                    onDone()
                    cleanupTemp()
                }
                setOnErrorListener { _, _, _ ->
                    onDone()
                    cleanupTemp()
                    true
                }
                prepareAsync()
            }
        } catch (_: Exception) {
            onDone()
        }
    }

    private fun cleanupTemp() {
        try {
            tempAudioFile?.delete()
            tempAudioFile = null
        } catch (_: Exception) {}
    }

    override fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.reset()
                it.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        cleanupTemp()
    }

    override fun release() {
        stop()
    }
}
