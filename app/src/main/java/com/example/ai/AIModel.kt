package com.example.ai

data class AIModelConfig(
    val id: String,
    val displayName: String,
    val apiModelName: String,
    val priority: Int,
    val maxOutputTokens: Int = 4096,
    val supportsStreaming: Boolean = true
)

object ModelRegistry {
    val PRIMARY_REASONING = AIModelConfig(
        id = "gemini-3.8-flash",
        displayName = "Gemini 3.8 Flash (Primary Brain)",
        apiModelName = "gemini-3.8-flash",
        priority = 1
    )

    val SECONDARY_REASONING = AIModelConfig(
        id = "gemini-3.7-flash",
        displayName = "Gemini 3.7 Flash (Deep Logic Fallback)",
        apiModelName = "gemini-3.7-flash",
        priority = 2
    )

    val GEMINI_3_6_FLASH = AIModelConfig(
        id = "gemini-3.6-flash",
        displayName = "Gemini 3.6 Flash",
        apiModelName = "gemini-3.6-flash",
        priority = 3
    )

    val FAST_ROUTER = AIModelConfig(
        id = "gemini-3.5-flash-lite",
        displayName = "Gemini 3.5 Flash Lite (Fast Router)",
        apiModelName = "gemini-3.5-flash-lite",
        priority = 4
    )

    val TERTIARY_FALLBACK = AIModelConfig(
        id = "gemini-3.5-flash",
        displayName = "Gemini 3.5 Flash",
        apiModelName = "gemini-3.5-flash",
        priority = 5
    )

    val GEMINI_3_1_FLASH = AIModelConfig(
        id = "gemini-3.1-flash",
        displayName = "Gemini 3.1 Flash",
        apiModelName = "gemini-3.1-flash",
        priority = 6
    )

    val GEMINI_2_5_FLASH = AIModelConfig(
        id = "gemini-2.5-flash",
        displayName = "Gemini 2.5 Flash",
        apiModelName = "gemini-2.5-flash",
        priority = 7
    )

    val ALL_MODELS = listOf(
        PRIMARY_REASONING,
        SECONDARY_REASONING,
        GEMINI_3_6_FLASH,
        FAST_ROUTER,
        TERTIARY_FALLBACK,
        GEMINI_3_1_FLASH,
        GEMINI_2_5_FLASH
    )

    fun findModelById(id: String): AIModelConfig? {
        return ALL_MODELS.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}
