package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) : AIProvider {

    override val providerId: String = "gemini_official_provider"

    override fun supportsModel(model: AIModelConfig): Boolean {
        return model.id.startsWith("gemini")
    }

    override suspend fun generate(
        model: AIModelConfig,
        request: AIRequest,
        apiKey: String
    ): AIResponseResult = withContext(Dispatchers.IO) {
        val isUsingBackendBridge = apiKey.isBlank()
        val url = if (isUsingBackendBridge) {
            "https://luxion-1.onrender.com/api/chat"
        } else {
            "https://generativelanguage.googleapis.com/v1beta/models/${model.apiModelName}:generateContent?key=$apiKey"
        }

        val payload = JSONObject().apply {
            // Contents array
            val contentsArray = JSONArray()
            request.messages.forEach { msg ->
                if (msg.role != "system") {
                    val contentObj = JSONObject().apply {
                        put("role", if (msg.role == "lux" || msg.role == "model") "model" else "user")
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", msg.content) })
                        }
                        put("parts", partsArray)
                    }
                    contentsArray.put(contentObj)
                }
            }
            // Ensure at least one content entry exists
            if (contentsArray.length() == 0) {
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", "Hello") })
                    })
                })
            }
            put("contents", contentsArray)

            // System instruction
            val systemText = request.systemInstruction
            if (!systemText.isNullOrBlank()) {
                val sysObj = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemText) })
                    })
                }
                put("systemInstruction", sysObj)
            }

            // Google Search Grounding Tool for real-time web & news
            val toolsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("googleSearch", JSONObject())
                })
            }
            put("tools", toolsArray)

            // Generation config
            val configObj = JSONObject().apply {
                put("temperature", request.temperature)
                put("maxOutputTokens", request.maxTokens)
            }
            put("generationConfig", configObj)

            if (isUsingBackendBridge) {
                put("model", model.apiModelName)
            }
        }

        val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
        val httpRequest = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(httpRequest).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val code = response.code

                if (!response.isSuccessful) {
                    var errorMsg = "HTTP $code"
                    var isQuota = (code == 429)
                    try {
                        val errorJson = JSONObject(responseBody).optJSONObject("error")
                        val serverMessage = errorJson?.optString("message")
                        if (!serverMessage.isNullOrBlank()) {
                            errorMsg = serverMessage
                        }
                        if (errorMsg.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
                            errorMsg.contains("quota", ignoreCase = true)
                        ) {
                            isQuota = true
                        }
                    } catch (_: Exception) {}

                    return@withContext AIResponseResult.Error(
                        message = errorMsg,
                        isQuotaError = isQuota,
                        isNetworkError = false,
                        statusCode = code
                    )
                }

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    val promptFeedback = jsonResponse.optJSONObject("promptFeedback")
                    val blockReason = promptFeedback?.optString("blockReason") ?: "Empty response"
                    return@withContext AIResponseResult.Error(
                        message = "Content blocked or empty: $blockReason",
                        statusCode = 200
                    )
                }

                val firstCandidate = candidates.getJSONObject(0)
                val parts = firstCandidate.optJSONObject("content")?.optJSONArray("parts")
                val textBuilder = StringBuilder()
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val partText = parts.getJSONObject(i).optString("text")
                        if (partText.isNotEmpty()) {
                            textBuilder.append(partText)
                        }
                    }
                }

                val replyText = textBuilder.toString().ifEmpty { "..." }

                val usage = jsonResponse.optJSONObject("usageMetadata")
                val promptTokens = usage?.optInt("promptTokenCount") ?: 0
                val candidateTokens = usage?.optInt("candidatesTokenCount") ?: 0

                return@withContext AIResponseResult.Success(
                    text = replyText,
                    modelUsed = model.id,
                    inputTokens = promptTokens,
                    outputTokens = candidateTokens
                )
            }
        } catch (e: IOException) {
            return@withContext AIResponseResult.Error(
                message = "Network error: ${e.message ?: "Failed to connect"}",
                isNetworkError = true
            )
        } catch (e: Exception) {
            return@withContext AIResponseResult.Error(
                message = "Unexpected error: ${e.localizedMessage ?: "Unknown failure"}"
            )
        }
    }
}
