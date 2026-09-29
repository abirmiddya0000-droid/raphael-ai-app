package com.example.ai

import java.util.concurrent.ConcurrentHashMap

enum class ModelHealthStatus {
    HEALTHY,
    RATE_LIMITED,
    QUOTA_EXHAUSTED,
    UNAVAILABLE,
    ERROR
}

data class ModelStatus(
    val modelId: String,
    val health: ModelHealthStatus = ModelHealthStatus.HEALTHY,
    val failureCount: Int = 0,
    val lastError: String? = null,
    val cooldownUntil: Long = 0L,
    val totalSuccessfulRequests: Int = 0
)

class QuotaTracker {
    private val modelStatuses = ConcurrentHashMap<String, ModelStatus>()

    init {
        ModelRegistry.ALL_MODELS.forEach { model ->
            modelStatuses[model.id] = ModelStatus(modelId = model.id)
        }
    }

    fun isModelAvailable(modelId: String): Boolean {
        val status = modelStatuses[modelId] ?: return true
        val now = System.currentTimeMillis()
        if (status.cooldownUntil > now) {
            return false
        }
        return status.health == ModelHealthStatus.HEALTHY || status.health == ModelHealthStatus.ERROR
    }

    fun recordSuccess(modelId: String) {
        val current = modelStatuses[modelId] ?: ModelStatus(modelId)
        modelStatuses[modelId] = current.copy(
            health = ModelHealthStatus.HEALTHY,
            failureCount = 0,
            lastError = null,
            cooldownUntil = 0L,
            totalSuccessfulRequests = current.totalSuccessfulRequests + 1
        )
    }

    fun recordFailure(modelId: String, statusCode: Int?, errorMessage: String) {
        val current = modelStatuses[modelId] ?: ModelStatus(modelId)
        val now = System.currentTimeMillis()
        val (health, cooldownMs) = when (statusCode) {
            429 -> Pair(ModelHealthStatus.RATE_LIMITED, 60_000L) // 1 minute backoff
            404 -> Pair(ModelHealthStatus.UNAVAILABLE, 300_000L) // 5 minutes backoff
            503 -> Pair(ModelHealthStatus.UNAVAILABLE, 30_000L) // 30 sec backoff
            else -> {
                if (errorMessage.contains("quota", ignoreCase = true) ||
                    errorMessage.contains("RESOURCE_EXHAUSTED", ignoreCase = true)
                ) {
                    Pair(ModelHealthStatus.QUOTA_EXHAUSTED, 120_000L)
                } else {
                    Pair(ModelHealthStatus.ERROR, 10_000L)
                }
            }
        }

        modelStatuses[modelId] = current.copy(
            health = health,
            failureCount = current.failureCount + 1,
            lastError = errorMessage,
            cooldownUntil = now + cooldownMs
        )
    }

    fun getStatus(modelId: String): ModelStatus {
        return modelStatuses[modelId] ?: ModelStatus(modelId)
    }

    fun getAllStatuses(): List<ModelStatus> {
        return modelStatuses.values.toList()
    }
}
