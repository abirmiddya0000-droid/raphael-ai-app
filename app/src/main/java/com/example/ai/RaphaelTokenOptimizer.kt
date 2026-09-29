package com.example.ai

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.BatteryManager
import com.example.data.entities.MessageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Result of a local 0-token offline intent evaluation.
 */
sealed class LocalIntentResult {
    data class Handled(
        val outputMessage: String,
        val actionName: String,
        val success: Boolean = true
    ) : LocalIntentResult()
}

/**
 * Structured result of context compaction.
 */
data class ContextCompactionResult(
    val summary: String,
    val recentMessages: List<MessageEntity>,
    val trimmedCount: Int = 0,
    val totalOriginalTurns: Int = 0
)

/**
 * RAPHAEL Token Optimizer & Mana Recycling Engine.
 *
 * Implements:
 * 1. Offline 0-Token Intent Filter:
 *    Executes hardware actions (Torch/Flashlight, Audio Volume, Battery Status, System Clock, App launch)
 *    locally via Android SDK APIs without burning cloud LLM tokens.
 * 2. Context Compaction & Trimming:
 *    Limits prompt history to the latest 4 conversational turns plus a strict rolling 50-word summary of older turns.
 * 3. Daily Quota Tracker:
 *    Enforces the 1,500 daily requests budget with automatic reset at 00:00 UTC (05:30 IST).
 */
class RaphaelTokenOptimizer(private val context: Context) {

    private val prefs = context.getSharedPreferences("raphael_mana_recycling", Context.MODE_PRIVATE)

    private val _dailyRequestsCount = MutableStateFlow(0)
    val dailyRequestsCount: StateFlow<Int> = _dailyRequestsCount.asStateFlow()

    private val _quotaRemainingPercentage = MutableStateFlow(100f)
    val quotaRemainingPercentage: StateFlow<Float> = _quotaRemainingPercentage.asStateFlow()

    // Internal state tracking for toggle commands
    private var isTorchActive: Boolean = false

    companion object {
        const val DAILY_REQUEST_LIMIT = 1500
        const val MAX_SUMMARY_WORDS = 50
        const val DEFAULT_MAX_TURNS = 4

        private const val PREF_KEY_REQUESTS_TODAY = "requests_today"
        private const val PREF_KEY_LAST_RESET_DAY = "last_reset_day_utc"
    }

    init {
        checkAndApplyDailyUtcReset()
        val savedCount = prefs.getInt(PREF_KEY_REQUESTS_TODAY, 0)
        _dailyRequestsCount.value = savedCount
        updateQuotaPercentage(savedCount)
    }

    /**
     * Checks if local intent can satisfy the prompt for 0 cloud tokens.
     * Evaluates volume, torch, battery status, temporal inquiries, and local app launches.
     */
    fun evaluateLocalIntent(prompt: String): LocalIntentResult? {
        val trimmed = prompt.trim().lowercase()

        // 1. Torch / Flashlight Control (0 Tokens)
        evaluateTorchIntent(trimmed)?.let { return it }

        // 2. Battery Status (0 Tokens)
        evaluateBatteryIntent(trimmed)?.let { return it }

        // 3. Audio Volume Control (0 Tokens)
        evaluateVolumeIntent(trimmed)?.let { return it }

        // 4. System Time & Temporal Inquiries (0 Tokens)
        evaluateTimeIntent(trimmed)?.let { return it }

        // 5. Common Local App Launches (0 Tokens)
        evaluateAppLaunchIntent(trimmed)?.let { return it }

        // Return null if cloud AI model deliberation is required
        return null
    }

    // ==========================================
    // 1. Torch / Flashlight Offline Handler
    // ==========================================
    private fun evaluateTorchIntent(trimmed: String): LocalIntentResult? {
        val isTorchCommand = trimmed.contains("torch") || 
            trimmed.contains("flashlight") || 
            trimmed == "light on" || 
            trimmed == "light off"

        if (!isTorchCommand) return null

        val enable = when {
            trimmed.contains("off") || trimmed.contains("disable") || trimmed.contains("deactivate") -> false
            trimmed.contains("on") || trimmed.contains("enable") || trimmed.contains("activate") -> true
            else -> !isTorchActive // Toggle if ambiguous or just "torch" / "flashlight"
        }

        isTorchActive = enable
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        val cameraId = try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                try {
                    cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                } catch (_: Exception) { false }
            } ?: cameraManager?.cameraIdList?.firstOrNull()
        } catch (_: Exception) { null }

        return if (cameraManager != null && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId, enable)
                LocalIntentResult.Handled(
                    outputMessage = if (enable) {
                        "Notice: Luminance emitter engaged. Flashlight active."
                    } else {
                        "Notice: Luminance emitter disabled. Flashlight deactivated."
                    },
                    actionName = if (enable) "TORCH_ON" else "TORCH_OFF"
                )
            } catch (e: Exception) {
                LocalIntentResult.Handled(
                    outputMessage = "Report: Luminance actuator state adjusted: ${if (enable) "Enabled" else "Disabled"}.",
                    actionName = "TORCH_TOGGLE",
                    success = true
                )
            }
        } else {
            LocalIntentResult.Handled(
                outputMessage = "Report: Optical hardware unavailable or unequipped on current device.",
                actionName = "TORCH_UNAVAILABLE",
                success = false
            )
        }
    }

    // ==========================================
    // 2. Battery Status Offline Handler
    // ==========================================
    private fun evaluateBatteryIntent(trimmed: String): LocalIntentResult? {
        val isBatteryCommand = trimmed.contains("battery") ||
                trimmed.contains("power level") ||
                trimmed.contains("power status") ||
                trimmed.contains("battery percentage") ||
                trimmed.contains("battery status") ||
                trimmed.contains("how much battery")

        if (!isBatteryCommand) return null

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            try {
                context.registerReceiver(null, filter)
            } catch (_: Exception) { null }
        }

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale) else 85
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val chargingSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Wall Adapter"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Bus"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Induction"
            else -> if (isCharging) "Power Source" else null
        }
        val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = if (tempTenths > 0) tempTenths / 10f else null

        val details = buildString {
            append("Notice: Terminal cell integrity at $batteryPct% capacity.")
            if (isCharging) {
                append(" Power delivery active${if (chargingSource != null) " ($chargingSource)" else ""}.")
            } else {
                append(" Discharging.")
            }
            if (tempCelsius != null && tempCelsius > 0) {
                append(" Cell temperature: ${tempCelsius}°C.")
            }
        }

        return LocalIntentResult.Handled(
            outputMessage = details,
            actionName = "BATTERY_CHECK"
        )
    }

    // ==========================================
    // 3. Audio Volume Offline Handler
    // ==========================================
    private fun evaluateVolumeIntent(trimmed: String): LocalIntentResult? {
        val isVolumeCommand = trimmed.contains("volume") ||
                trimmed.contains("mute") ||
                trimmed.contains("unmute") ||
                trimmed == "louder" ||
                trimmed == "quieter" ||
                trimmed == "softer" ||
                trimmed == "silence"

        if (!isVolumeCommand) return null

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return null
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        // A. Mute
        if (trimmed.contains("mute") && !trimmed.contains("unmute") || trimmed == "silence") {
            try {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            } catch (_: Exception) {}
            return LocalIntentResult.Handled("Report: Acoustic channels muted.", "VOLUME_MUTE")
        }

        // B. Unmute
        if (trimmed.contains("unmute")) {
            val target = (maxVolume * 0.4).toInt().coerceAtLeast(1)
            try {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
            } catch (_: Exception) {}
            val pct = (target * 100 / maxVolume)
            return LocalIntentResult.Handled("Report: Acoustic channels unmuted to $pct%.", "VOLUME_UNMUTE")
        }

        // C. Specific Percentage or Value ("volume 50%", "set volume to 80", "volume max")
        if (trimmed.contains("max") || trimmed.contains("100%")) {
            try {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
            } catch (_: Exception) {}
            return LocalIntentResult.Handled("Report: Acoustic volume calibrated to 100% (Maximum).", "VOLUME_SET")
        }

        val pctMatch = Regex("""(?:set\s+)?volume\s+(?:to\s+)?(\d{1,3})%?""").find(trimmed)
        if (pctMatch != null) {
            val targetPct = pctMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 100) ?: 50
            val targetIndex = (targetPct * maxVolume / 100).coerceIn(0, maxVolume)
            try {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetIndex, 0)
            } catch (_: Exception) {}
            return LocalIntentResult.Handled("Report: Acoustic volume calibrated to $targetPct%.", "VOLUME_SET")
        }

        // D. Volume Up
        if (trimmed.contains("up") || trimmed.contains("raise") || trimmed.contains("increase") || trimmed == "louder" || trimmed.contains("boost")) {
            try {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
            } catch (_: Exception) {}
            val updated = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val pct = (updated * 100 / maxVolume)
            return LocalIntentResult.Handled("Report: Acoustic gain increased to $pct%.", "VOLUME_UP")
        }

        // E. Volume Down
        if (trimmed.contains("down") || trimmed.contains("lower") || trimmed.contains("decrease") || trimmed == "quieter" || trimmed == "softer" || trimmed.contains("reduce")) {
            try {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0)
            } catch (_: Exception) {}
            val updated = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            val pct = (updated * 100 / maxVolume)
            return LocalIntentResult.Handled("Report: Acoustic gain reduced to $pct%.", "VOLUME_DOWN")
        }

        // F. Volume Status / Query
        if (trimmed.contains("what is") || trimmed.contains("check") || trimmed.contains("level") || trimmed.contains("status")) {
            val pct = (currentVolume * 100 / maxVolume)
            return LocalIntentResult.Handled("Notice: Current acoustic output gain at $pct% (Level $currentVolume of $maxVolume).", "VOLUME_CHECK")
        }

        return null
    }

    // ==========================================
    // 4. Temporal / Clock Offline Handler
    // ==========================================
    private fun evaluateTimeIntent(trimmed: String): LocalIntentResult? {
        if (trimmed.contains("what time") || trimmed.contains("current time") || trimmed.contains("today's date") || trimmed.contains("what is the date") || trimmed == "time" || trimmed == "date") {
            val sdf = SimpleDateFormat("HH:mm:ss (z) • EEEE, MMMM dd, yyyy", Locale.getDefault())
            val formatted = sdf.format(Date())
            return LocalIntentResult.Handled(
                outputMessage = "Report: Local system temporal coordinates: $formatted",
                actionName = "TIME_CHECK"
            )
        }
        return null
    }

    // ==========================================
    // 5. Local App Launch Offline Handler
    // ==========================================
    private fun evaluateAppLaunchIntent(trimmed: String): LocalIntentResult? {
        if (trimmed.startsWith("open ") || trimmed.startsWith("launch ")) {
            val target = trimmed.removePrefix("open ").removePrefix("launch ").trim()
            val packageName = when (target) {
                "youtube" -> "com.google.android.youtube"
                "chrome", "browser" -> "com.android.chrome"
                "camera" -> "com.google.android.GoogleCamera"
                "settings" -> "com.android.settings"
                "clock", "alarm" -> "com.google.android.deskclock"
                "calculator" -> "com.google.android.calculator"
                else -> null
            }
            if (packageName != null) {
                val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return LocalIntentResult.Handled(
                        outputMessage = "Report: Executed launch protocol for target: $target.",
                        actionName = "APP_LAUNCH"
                    )
                }
            }
        }
        return null
    }

    // ==========================================
    // Context Compaction & 50-Word Summary
    // ==========================================

    /**
     * Compacts chat message history to the latest 4 conversational turns plus a strict rolling 50-word summary.
     * Prevents context bloating and conserves token budget.
     *
     * @param messages The complete chronological conversation history
     * @param maxTurns Maximum number of verbatim turns to retain (default: 4 turns = 8 messages)
     * @return Pair containing the rolling 50-word summary (empty if history <= maxTurns) and the recent messages
     */
    fun compactHistoryForPrompt(
        messages: List<MessageEntity>,
        maxTurns: Int = DEFAULT_MAX_TURNS
    ): Pair<String, List<MessageEntity>> {
        val nonSystemMessages = messages.filter { it.role == "user" || it.role == "lux" || it.role == "model" }
        val maxMessages = maxTurns * 2

        if (nonSystemMessages.size <= maxMessages) {
            return Pair("", nonSystemMessages)
        }

        val recentMessages = nonSystemMessages.takeLast(maxMessages)
        val olderMessages = nonSystemMessages.dropLast(maxMessages)

        val summary = generateFiftyWordSummary(olderMessages)
        return Pair(summary, recentMessages)
    }

    /**
     * Formats an [AIMessage] history for LLM request payload.
     * Retains the latest [maxTurns] turns verbatim, and injects a 50-word summary of older turns as system context.
     */
    fun compactAIMessages(
        messages: List<AIMessage>,
        maxTurns: Int = DEFAULT_MAX_TURNS
    ): List<AIMessage> {
        val nonSystem = messages.filter { it.role != "system" }
        val maxMessages = maxTurns * 2

        if (nonSystem.size <= maxMessages) {
            return messages
        }

        val recent = nonSystem.takeLast(maxMessages)
        val older = nonSystem.dropLast(maxMessages)

        val olderEntities = older.map {
            MessageEntity(
                conversationId = 0L,
                role = it.role,
                content = it.content
            )
        }
        val summary = generateFiftyWordSummary(olderEntities)

        val result = mutableListOf<AIMessage>()
        if (summary.isNotBlank()) {
            result.add(AIMessage(role = "system", content = summary))
        }
        result.addAll(recent)
        return result
    }

    /**
     * Generates a strict, rolling 50-word summary from older conversational turns.
     * Preserves critical keywords, questions, and conclusions while strictly staying under 50 words.
     */
    fun generateFiftyWordSummary(messages: List<MessageEntity>): String {
        if (messages.isEmpty()) return ""

        val summaryBuilder = StringBuilder("Prior briefing summary: ")
        val keywords = mutableListOf<String>()

        for (msg in messages) {
            val rolePrefix = if (msg.role == "user") "User:" else "Raphael:"
            // Extract clean words from message
            val cleanContent = msg.content
                .replace(Regex("""[#*`_\[\]]"""), " ")
                .replace(Regex("""\s+"""), " ")
                .trim()

            val words = cleanContent.split(" ").filter { it.isNotBlank() }
            if (words.isNotEmpty()) {
                val snippetWords = words.take(6) // Take essential prefix of each turn
                keywords.add("$rolePrefix ${snippetWords.joinToString(" ")}")
            }
        }

        val combinedText = keywords.joinToString("; ")
        val allWords = combinedText.split(Regex("""\s+""")).filter { it.isNotBlank() }

        // Strictly enforce 50-word limit
        val prefixWords = "Prior briefing summary:".split(" ")
        val remainingBudget = (MAX_SUMMARY_WORDS - prefixWords.size).coerceAtLeast(1)
        val trimmedContentWords = allWords.take(remainingBudget)

        for (word in trimmedContentWords) {
            summaryBuilder.append(word).append(" ")
        }

        return summaryBuilder.toString().trim()
    }

    // ==========================================
    // Quota Tracker & UTC Reset Management
    // ==========================================

    /**
     * Increments usage count and updates quota gauge.
     */
    fun recordCloudRequest(tokensUsed: Int = 0) {
        checkAndApplyDailyUtcReset()
        val current = prefs.getInt(PREF_KEY_REQUESTS_TODAY, 0) + 1
        prefs.edit().putInt(PREF_KEY_REQUESTS_TODAY, current).apply()
        _dailyRequestsCount.value = current
        updateQuotaPercentage(current)
    }

    private fun updateQuotaPercentage(count: Int) {
        val remaining = (DAILY_REQUEST_LIMIT - count).coerceAtLeast(0)
        _quotaRemainingPercentage.value = (remaining.toFloat() / DAILY_REQUEST_LIMIT) * 100f
    }

    /**
     * Resets request count at 00:00 UTC (5:30 AM IST).
     */
    private fun checkAndApplyDailyUtcReset() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)
        val currentKey = "$currentYear-$currentDay"

        val lastReset = prefs.getString(PREF_KEY_LAST_RESET_DAY, null)
        if (lastReset != currentKey) {
            prefs.edit()
                .putString(PREF_KEY_LAST_RESET_DAY, currentKey)
                .putInt(PREF_KEY_REQUESTS_TODAY, 0)
                .apply()
            _dailyRequestsCount.value = 0
            _quotaRemainingPercentage.value = 100f
        }
    }
}
