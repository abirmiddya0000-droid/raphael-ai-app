package com.example.research

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

sealed class ResearchResult {
    data class Success(val title: String, val summary: String, val sourceUrl: String) : ResearchResult()
    data class Error(val message: String) : ResearchResult()
}

interface SearchProvider {
    suspend fun search(query: String): ResearchResult
}

interface PublicPageRetriever {
    suspend fun retrievePageSnippet(url: String): ResearchResult
}

class DuckDuckGoSearchProvider(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) : SearchProvider {

    override suspend fun search(query: String): ResearchResult = withContext(Dispatchers.IO) {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1&skip_disambig=1"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "LUX-Butler/1.0")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ResearchResult.Error("Search service returned HTTP ${response.code}")
                }
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val abstractText = json.optString("AbstractText")
                val heading = json.optString("Heading")
                val abstractUrl = json.optString("AbstractURL")

                if (abstractText.isNotBlank()) {
                    ResearchResult.Success(
                        title = heading.ifBlank { query },
                        summary = abstractText,
                        sourceUrl = abstractUrl.ifBlank { "https://duckduckgo.com/?q=$encoded" }
                    )
                } else {
                    // Check related topics
                    val related = json.optJSONArray("RelatedTopics")
                    if (related != null && related.length() > 0) {
                        val firstTopic = related.optJSONObject(0)
                        val text = firstTopic?.optString("Text") ?: ""
                        val firstUrl = firstTopic?.optString("FirstURL") ?: ""
                        if (text.isNotBlank()) {
                            return@withContext ResearchResult.Success(
                                title = query,
                                summary = text,
                                sourceUrl = firstUrl
                            )
                        }
                    }
                    ResearchResult.Error("No public instant knowledge found for '$query'.")
                }
            }
        } catch (e: IOException) {
            ResearchResult.Error("Network error during web research: ${e.message ?: "Failed to connect"}")
        } catch (e: Exception) {
            ResearchResult.Error("Failed to perform web research: ${e.localizedMessage}")
        }
    }
}

class PublicPageRetrieverImpl(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) : PublicPageRetriever {

    override suspend fun retrievePageSnippet(url: String): ResearchResult = withContext(Dispatchers.IO) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return@withContext ResearchResult.Error("Invalid URL protocol. Only public HTTP/HTTPS pages supported.")
        }

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "LUX-Butler/1.0 (Public research bot; no login bypass)")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ResearchResult.Error("HTTP ${response.code}: Could not retrieve public page.")
                }
                val rawHtml = response.body?.string() ?: ""
                // Simple tag stripper to respect public content without parsing heavy scripts
                val cleanText = rawHtml
                    .replace(Regex("<script[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("<style[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
                    .replace(Regex("<[^>]+>"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()

                val snippet = if (cleanText.length > 500) cleanText.take(500) + "..." else cleanText
                ResearchResult.Success(
                    title = url,
                    summary = snippet,
                    sourceUrl = url
                )
            }
        } catch (e: Exception) {
            ResearchResult.Error("Page could not be retrieved: ${e.message}")
        }
    }
}

class ResearchRouter(
    private val searchProvider: SearchProvider = DuckDuckGoSearchProvider(),
    private val pageRetriever: PublicPageRetriever = PublicPageRetrieverImpl()
) {
    suspend fun performResearch(queryOrUrl: String): ResearchResult {
        return if (queryOrUrl.startsWith("http://") || queryOrUrl.startsWith("https://")) {
            pageRetriever.retrievePageSnippet(queryOrUrl)
        } else {
            searchProvider.search(queryOrUrl)
        }
    }
}
