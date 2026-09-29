package com.example.ai

/**
 * Standard AIProvider interface.
 * All AI execution backends (Gemini, secondary providers, etc.) implement this contract.
 */
interface AIProvider {
    /**
     * Unique identifier for this provider implementation (e.g. "gemini_provider")
     */
    val providerId: String

    /**
     * Check whether this provider supports the requested model configuration
     */
    fun supportsModel(model: AIModelConfig): Boolean

    /**
     * Executes the generation request for a given model
     */
    suspend fun generate(
        model: AIModelConfig,
        request: AIRequest,
        apiKey: String
    ): AIResponseResult
}

/**
 * Router interface defining the model orchestration contract:
 * - Routing requests to primary model (Gemini 3.8 Flash)
 * - Handling sequential fallback to secondary models (Gemini 3.7 Flash, 3.5, 2.5)
 * - Monitoring quota limits, errors, and model health
 */
interface AIRouter {
    /**
     * Quota tracker monitoring model availability and backoff states
     */
    val quotaTracker: QuotaTracker

    /**
     * Routes an AI request through the prioritized model chain with intelligent fallback
     */
    suspend fun routeRequest(
        request: AIRequest,
        apiKey: String
    ): AIResponseResult

    /**
     * Route request specifically targeting a designated model, falling back only if requested
     */
    suspend fun routeRequestWithModel(
        targetModelId: String,
        request: AIRequest,
        apiKey: String,
        allowFallback: Boolean = true
    ): AIResponseResult
}
