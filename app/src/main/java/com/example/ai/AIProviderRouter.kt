package com.example.ai

import com.example.data.dao.AuditLogDao
import com.example.data.entities.AuditLogEntity

/**
 * Concrete implementation of AIRouter.
 * Manages model prioritization, sequential fallback from Gemini 3.8 Flash to secondary models,
 * quota tracking, and audit logging.
 */
class AIProviderRouter(
    private val providers: List<AIProvider>,
    override val quotaTracker: QuotaTracker,
    private val auditLogDao: AuditLogDao? = null
) : AIRouter {

    // Convenience constructor for single primary provider (e.g. GeminiProvider)
    constructor(
        geminiProvider: GeminiProvider,
        quotaTracker: QuotaTracker,
        auditLogDao: AuditLogDao? = null
    ) : this(
        providers = listOf(geminiProvider),
        quotaTracker = quotaTracker,
        auditLogDao = auditLogDao
    )

    override suspend fun routeRequest(
        request: AIRequest,
        apiKey: String
    ): AIResponseResult {
        return executeSequentialRouting(
            candidateModels = ModelRegistry.ALL_MODELS,
            request = request,
            apiKey = apiKey,
            allowFallback = true
        )
    }

    override suspend fun routeRequestWithModel(
        targetModelId: String,
        request: AIRequest,
        apiKey: String,
        allowFallback: Boolean
    ): AIResponseResult {
        val targetModel = ModelRegistry.findModelById(targetModelId)
            ?: ModelRegistry.PRIMARY_REASONING

        val orderedCandidates = if (allowFallback) {
            listOf(targetModel) + ModelRegistry.ALL_MODELS.filter { it.id != targetModel.id }
        } else {
            listOf(targetModel)
        }

        return executeSequentialRouting(
            candidateModels = orderedCandidates,
            request = request,
            apiKey = apiKey,
            allowFallback = allowFallback
        )
    }

    private suspend fun executeSequentialRouting(
        candidateModels: List<AIModelConfig>,
        request: AIRequest,
        apiKey: String,
        allowFallback: Boolean
    ): AIResponseResult {
        var lastError: AIResponseResult.Error? = null
        var fallbackOccurred = false
        var fallbackReason: String? = null

        for (i in candidateModels.indices) {
            val model = candidateModels[i]

            // 1. Check if model is on cooldown due to previous quota depletion or rate limit
            if (!quotaTracker.isModelAvailable(model.id)) {
                fallbackOccurred = true
                fallbackReason = "Model ${model.displayName} is in cooldown/quota exhaustion."
                continue
            }

            // 2. Locate suitable provider for this model
            val provider = providers.firstOrNull { it.supportsModel(model) }
            if (provider == null) {
                fallbackOccurred = true
                fallbackReason = "No registered AIProvider supports ${model.displayName}."
                continue
            }

            // 3. Attempt generation
            val result = provider.generate(model, request, apiKey)

            when (result) {
                is AIResponseResult.Success -> {
                    // Record successful turn; reset failure streak for this model
                    quotaTracker.recordSuccess(model.id)

                    return result.copy(
                        fallbackOccurred = fallbackOccurred,
                        fallbackReason = fallbackReason
                    )
                }

                is AIResponseResult.Error -> {
                    lastError = result
                    quotaTracker.recordFailure(model.id, result.statusCode, result.message)

                    // Determine whether to execute sequential fallback
                    val shouldFallback = allowFallback && (
                        result.isQuotaError ||
                        result.statusCode == 429 ||
                        result.statusCode == 404 ||
                        result.statusCode == 503 ||
                        result.isNetworkError
                    )

                    if (shouldFallback && i < candidateModels.size - 1) {
                        val nextModel = candidateModels[i + 1]
                        fallbackOccurred = true
                        fallbackReason = "${model.displayName} failed (${result.message}). Falling back to ${nextModel.displayName}."

                        auditLogDao?.insertLog(
                            AuditLogEntity(
                                action = "MODEL_FALLBACK",
                                details = "Falling back from ${model.displayName} to ${nextModel.displayName}: ${result.message}",
                                authorizedByOwner = true
                            )
                        )
                        // Continue to next model in sequential chain
                        continue
                    } else {
                        // Do not fallback on user content violations or if fallback is exhausted/disallowed
                        break
                    }
                }
            }
        }

        return lastError ?: AIResponseResult.Error(
            message = "All configured AI models are currently unavailable.",
            isQuotaError = true
        )
    }
}
