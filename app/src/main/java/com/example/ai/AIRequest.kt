package com.example.ai

data class AIMessage(
    val role: String, // "user", "model", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AIRequest(
    val messages: List<AIMessage>,
    val systemInstruction: String? = null,
    val temperature: Float = 0.6f,
    val maxTokens: Int = 2048,
    val stream: Boolean = false
)

sealed class AIResponseResult {
    data class Success(
        val text: String,
        val modelUsed: String,
        val inputTokens: Int,
        val outputTokens: Int,
        val fallbackOccurred: Boolean = false,
        val fallbackReason: String? = null
    ) : AIResponseResult()

    data class Error(
        val message: String,
        val isQuotaError: Boolean = false,
        val isNetworkError: Boolean = false,
        val statusCode: Int? = null
    ) : AIResponseResult()
}
