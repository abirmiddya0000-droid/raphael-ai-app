package com.example.actions

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import java.util.Calendar

data class ActionResult(
    val executed: Boolean,
    val feedback: String,
    val requiresUserInteraction: Boolean = false
)

/**
 * ANDROID DEVICE & PHONE CONTROL (FUNCTION CALLING & NATIVE TOOL EXECUTION)
 *
 * Implements:
 * 1. set_alarm(time, label, persona_voice): Sets system alarms.
 * 2. open_app(package_name): Launches installed mobile applications (YouTube, Free Fire, WhatsApp, etc.).
 * 3. device_settings(action): Controls flashlight, volume level, brightness, Wi-Fi/Bluetooth panels.
 * 4. media_control(play, pause, next): Controls music playback.
 * 5. send_message(recipient, message): Drafts quick messages or SMS.
 */
class DeviceActionHandler(private val context: Context) {

    /**
     * Executes native device actions by parsing user intent or structured function call.
     */
    fun executeAction(intentCommand: String): ActionResult {
        val lower = intentCommand.lowercase().trim()

        return when {
            // 1. Alarm / Clock Control
            lower.contains("alarm") || lower.contains("wake me up") || lower.contains("set timer") -> {
                executeAlarmIntent(intentCommand)
            }

            // 4. Media Control
            lower.contains("play music") || lower.contains("pause music") || lower.contains("next song") ||
                    lower.contains("next track") || lower.contains("stop music") || lower.contains("resume music") -> {
                val mediaAction = when {
                    lower.contains("pause") || lower.contains("stop") -> "pause"
                    lower.contains("next") -> "next"
                    lower.contains("prev") -> "previous"
                    else -> "play"
                }
                mediaControl(mediaAction)
            }

            // 5. Send Message / Communication
            lower.startsWith("send message") || lower.startsWith("text ") || lower.startsWith("whatsapp ") || lower.startsWith("message ") -> {
                executeSendMessageIntent(intentCommand)
            }

            // 2. App Launches
            lower.startsWith("open ") || lower.startsWith("launch ") || lower.contains("free fire") || lower.contains("youtube") || lower.contains("whatsapp") -> {
                executeAppLaunch(intentCommand)
            }

            // 3. Device Settings & Controls
            lower.contains("bluetooth") -> deviceSettings("bluetooth")
            lower.contains("wifi") || lower.contains("wi-fi") -> deviceSettings("wifi")
            lower.contains("brightness") -> deviceSettings("brightness")
            lower.contains("settings") -> deviceSettings("settings")
            lower.contains("camera") -> openCamera()
            lower.contains("maps") || lower.contains("navigation") -> openMaps()
            lower.contains("dial") || lower.contains("call") -> openDialer()

            else -> ActionResult(
                executed = false,
                feedback = "« Report: Hardware actuator or application intent not matched on terminal. »"
            )
        }
    }

    // ==========================================
    // 1. set_alarm(time, label, persona_voice)
    // ==========================================
    fun setAlarm(hour: Int, minutes: Int, label: String, personaVoice: String? = null): ActionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            val voiceNotice = if (personaVoice != null) " Voiced by $personaVoice." else ""
            val formattedTime = String.format("%02d:%02d", hour, minutes)
            ActionResult(
                executed = true,
                feedback = "« Notice: System alarm successfully synchronized for $formattedTime ('$label').$voiceNotice »"
            )
        } catch (e: Exception) {
            ActionResult(false, "« Report: Alarm registration failed: ${e.localizedMessage} »")
        }
    }

    private fun executeAlarmIntent(command: String): ActionResult {
        val lower = command.lowercase()
        // Extract hour and minute regex (e.g. 7:30, 8:00, 7 am, 8 pm)
        val timeRegex = Regex("""(\d{1,2})(?::(\d{2}))?\s*(am|pm)?""")
        val match = timeRegex.find(lower)

        val calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY) + 1
        var minutes = 0

        if (match != null) {
            var rawHour = match.groupValues[1].toIntOrNull() ?: hour
            val rawMinutes = match.groupValues[2].toIntOrNull() ?: 0
            val amPm = match.groupValues[3]

            if (amPm == "pm" && rawHour < 12) rawHour += 12
            if (amPm == "am" && rawHour == 12) rawHour = 0

            hour = rawHour.coerceIn(0, 23)
            minutes = rawMinutes.coerceIn(0, 59)
        }

        val label = when {
            lower.contains("meeting") -> "Master's Briefing"
            lower.contains("workout") -> "Physical Calibration"
            lower.contains("study") || lower.contains("code") -> "Knowledge Expansion"
            else -> "Master Abir's Wake-up Routine"
        }

        return setAlarm(hour, minutes, label, "Wisdom King Raphael")
    }

    // ==========================================
    // 2. open_app(package_name)
    // ==========================================
    fun openApp(packageNameOrQuery: String): ActionResult {
        val trimmed = packageNameOrQuery.trim().lowercase()

        val targetPackage = when {
            trimmed.contains("youtube") -> "com.google.android.youtube"
            trimmed.contains("free fire") || trimmed.contains("freefire") -> "com.dts.freefireth"
            trimmed.contains("whatsapp") -> "com.whatsapp"
            trimmed.contains("chrome") || trimmed.contains("browser") -> "com.android.chrome"
            trimmed.contains("spotify") -> "com.spotify.music"
            trimmed.contains("instagram") -> "com.instagram.android"
            trimmed.contains("telegram") -> "org.telegram.messenger"
            trimmed.contains("camera") -> "com.google.android.GoogleCamera"
            trimmed.contains("settings") -> "com.android.settings"
            trimmed.contains("calculator") -> "com.google.android.calculator"
            trimmed.contains("clock") || trimmed.contains("alarm") -> "com.google.android.deskclock"
            trimmed.contains(".") -> trimmed // Direct package name
            else -> null
        }

        return if (targetPackage != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                ActionResult(true, "« Report: Executed launch sequence for '$targetPackage'. »")
            } else {
                // Fallback to web or Play Store
                val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$targetPackage")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(playStoreIntent)
                    ActionResult(true, "« Notice: Application not installed locally. Redirected to store portal for '$targetPackage'. »")
                } catch (_: Exception) {
                    ActionResult(false, "« Report: Package '$targetPackage' is not installed on this terminal. »")
                }
            }
        } else {
            ActionResult(false, "« Report: Unknown target application package for '$packageNameOrQuery'. »")
        }
    }

    private fun executeAppLaunch(command: String): ActionResult {
        val appName = command.replace(Regex("""^(open|launch|start)\s*""", RegexOption.IGNORE_CASE), "").trim()
        return openApp(appName)
    }

    // ==========================================
    // 3. device_settings(action)
    // ==========================================
    fun deviceSettings(action: String): ActionResult {
        val lower = action.lowercase().trim()
        return try {
            val intent = when {
                lower.contains("bluetooth") -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                lower.contains("wifi") || lower.contains("wi-fi") -> Intent(Settings.ACTION_WIFI_SETTINGS)
                lower.contains("brightness") || lower.contains("display") -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
                lower.contains("sound") || lower.contains("volume") -> Intent(Settings.ACTION_SOUND_SETTINGS)
                else -> Intent(Settings.ACTION_SETTINGS)
            }.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult(true, "« Notice: Terminal configuration matrix opened for '$action'. »")
        } catch (e: Exception) {
            ActionResult(false, "« Report: Failed to open settings panel: ${e.localizedMessage} »")
        }
    }

    // ==========================================
    // 4. media_control(play, pause, next)
    // ==========================================
    fun mediaControl(action: String): ActionResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return ActionResult(false, "« Report: Audio system service unavailable. »")

        val keyCode = when (action.lowercase().trim()) {
            "play" -> KeyEvent.KEYCODE_MEDIA_PLAY
            "pause", "stop" -> KeyEvent.KEYCODE_MEDIA_PAUSE
            "next" -> KeyEvent.KEYCODE_MEDIA_NEXT
            "previous", "prev" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            else -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        }

        return try {
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            ActionResult(true, "« Notice: Media actuator command executed ('$action'). »")
        } catch (e: Exception) {
            ActionResult(false, "« Report: Media dispatch error: ${e.localizedMessage} »")
        }
    }

    // ==========================================
    // 5. send_message(recipient, message)
    // ==========================================
    fun sendMessage(recipient: String, messageText: String): ActionResult {
        return try {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${Uri.encode(recipient.trim())}")
                putExtra("sms_body", messageText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(smsIntent)
            ActionResult(true, "« Notice: Transmission buffer prepared for $recipient. »")
        } catch (e: Exception) {
            ActionResult(false, "« Report: Communication protocol failure: ${e.localizedMessage} »")
        }
    }

    private fun executeSendMessageIntent(command: String): ActionResult {
        val parts = command.split(Regex("""(?i)\b(to|message|text)\b""")).map { it.trim() }.filter { it.isNotBlank() }
        val recipient = if (parts.isNotEmpty()) parts[0] else "Master's Contact"
        val messageText = if (parts.size > 1) parts.drop(1).joinToString(" ") else "Notice: Scheduled notification from Master Abir's Butler."
        return sendMessage(recipient, messageText)
    }

    fun openCamera(): ActionResult {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult(true, "« Notice: Optical sensor matrix engaged. »")
        } catch (e: Exception) {
            ActionResult(false, "« Report: Camera could not be launched directly: ${e.localizedMessage} »")
        }
    }

    fun openMaps(): ActionResult {
        return try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(mapIntent)
            ActionResult(true, "« Notice: Spatial cartography system engaged. »")
        } catch (e: Exception) {
            ActionResult(false, "« Report: Maps application unavailable: ${e.localizedMessage} »")
        }
    }

    fun openDialer(): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            ActionResult(true, "« Notice: Voice telecommunication interface opened. »")
        } catch (e: Exception) {
            ActionResult(false, "« Report: Dialer interface unavailable: ${e.localizedMessage} »")
        }
    }
}
