package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lux_secure_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_OWNER_NAME = "owner_name"
        private const val KEY_OWNER_PIN = "owner_pin"
        private const val KEY_MEMORY_ENABLED = "memory_enabled"
        private const val KEY_TTS_ENABLED = "tts_enabled"
        private const val KEY_TTS_PROVIDER = "tts_provider" // GEMINI_LITE, GEMINI_FLASH, SYSTEM
        private const val KEY_TOTAL_INPUT_TOKENS = "total_input_tokens"
        private const val KEY_TOTAL_OUTPUT_TOKENS = "total_output_tokens"
        private const val KEY_OVERLAY_ENABLED = "overlay_enabled"
        private const val KEY_PRIMARY_MODEL = "primary_model"
        private const val KEY_LAST_CONVERSATION_ID = "last_conversation_id"
        private const val KEY_CHARACTER_ID = "selected_character_id"
    }

    var selectedCharacterId: String
        get() = prefs.getString(KEY_CHARACTER_ID, "raphael_wisdom") ?: "raphael_wisdom"
        set(value) = prefs.edit().putString(KEY_CHARACTER_ID, value).apply()

    var ownerName: String
        get() = prefs.getString(KEY_OWNER_NAME, "Abir") ?: "Abir"
        set(value) = prefs.edit().putString(KEY_OWNER_NAME, value).apply()

    var ownerPin: String
        get() = prefs.getString(KEY_OWNER_PIN, "0000") ?: "0000"
        set(value) = prefs.edit().putString(KEY_OWNER_PIN, value).apply()

    var isMemoryEnabled: Boolean
        get() = prefs.getBoolean(KEY_MEMORY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_MEMORY_ENABLED, value).apply()

    var isTtsEnabled: Boolean
        get() = prefs.getBoolean(KEY_TTS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_TTS_ENABLED, value).apply()

    var ttsProvider: String
        get() = prefs.getString(KEY_TTS_PROVIDER, "GEMINI_LITE") ?: "GEMINI_LITE"
        set(value) = prefs.edit().putString(KEY_TTS_PROVIDER, value).apply()

    var isOverlayEnabled: Boolean
        get() = prefs.getBoolean(KEY_OVERLAY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, value).apply()

    var primaryModel: String
        get() = prefs.getString(KEY_PRIMARY_MODEL, "gemini-3.8-flash") ?: "gemini-3.8-flash"
        set(value) = prefs.edit().putString(KEY_PRIMARY_MODEL, value).apply()

    var totalInputTokens: Long
        get() = prefs.getLong(KEY_TOTAL_INPUT_TOKENS, 0L)
        set(value) = prefs.edit().putLong(KEY_TOTAL_INPUT_TOKENS, value).apply()

    var totalOutputTokens: Long
        get() = prefs.getLong(KEY_TOTAL_OUTPUT_TOKENS, 0L)
        set(value) = prefs.edit().putLong(KEY_TOTAL_OUTPUT_TOKENS, value).apply()

    var lastActiveConversationId: Long
        get() = prefs.getLong(KEY_LAST_CONVERSATION_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_LAST_CONVERSATION_ID, value).apply()

    fun recordTokenUsage(input: Int, output: Int) {
        totalInputTokens += input
        totalOutputTokens += output
    }
}
