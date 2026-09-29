package com.example.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Foreground service providing passive listening / hotword monitoring for "Hey Raphael".
 * Displays persistent status notification and wakes Raphael upon hotword activation.
 */
class RaphaelHotwordService : Service(), RecognitionListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    companion object {
        const val CHANNEL_ID = "raphael_hotword_channel"
        const val NOTIFICATION_ID = 903
        const val ACTION_HOTWORD_TRIGGERED = "com.example.raphael.HOTWORD_TRIGGERED"

        private val _isHotwordActive = MutableStateFlow(false)
        val isHotwordActive: StateFlow<Boolean> = _isHotwordActive.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, RaphaelHotwordService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, RaphaelHotwordService::class.java)
            context.stopService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        _isHotwordActive.value = true
        initRecognizer()
        startHotwordListening()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "RAPHAEL Hotword Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors for 'Hey Raphael' voice activation"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RAPHAEL Passive Ears Engaged")
            .setContentText("Awaiting 'Hey Raphael' hotword from Abir.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(this@RaphaelHotwordService)
            }
        }
    }

    private fun startHotwordListening() {
        if (isListening || speechRecognizer == null) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        try {
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (_: Exception) {
            scheduleRestart()
        }
    }

    private fun scheduleRestart() {
        isListening = false
        serviceScope.launch {
            delay(1200)
            if (_isHotwordActive.value) {
                startHotwordListening()
            }
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val detected = matches?.any { text ->
            val lower = text.lowercase()
            lower.contains("raphael") || lower.contains("hey raphael")
        } ?: false

        if (detected) {
            // Hotword triggered: Summon Raphael into interactive voice mode
            val wakeIntent = Intent(this, MainActivity::class.java).apply {
                action = ACTION_HOTWORD_TRIGGERED
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(wakeIntent)
        }

        scheduleRestart()
    }

    override fun onError(error: Int) {
        scheduleRestart()
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val detected = matches?.any { text ->
            val lower = text.lowercase()
            lower.contains("hey raphael") || lower.contains("raphael")
        } ?: false
        if (detected) {
            speechRecognizer?.stopListening()
            val wakeIntent = Intent(this, MainActivity::class.java).apply {
                action = ACTION_HOTWORD_TRIGGERED
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(wakeIntent)
        }
    }
    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        _isHotwordActive.value = false
        speechRecognizer?.destroy()
        speechRecognizer = null
        serviceScope.cancel()
    }
}
