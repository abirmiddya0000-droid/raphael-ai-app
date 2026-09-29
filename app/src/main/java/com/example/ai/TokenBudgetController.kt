package com.example.ai

import com.example.data.entities.MemoryEntity

class TokenBudgetController(
    private val maxContextTokens: Int = 3000,
    private val verbatimRecentTurns: Int = 6
) {
    /**
     * Estimates tokens approximately based on character length (approx 4 chars per token).
     */
    fun estimateTokens(text: String): Int {
        if (text.isEmpty()) return 0
        return (text.length + 3) / 4
    }

    /**
     * Compacts conversation history.
     * Keeps the most recent [verbatimRecentTurns] messages completely intact.
     * If total history exceeds budget, older turns are compacted into concise context summaries.
     */
    fun prepareOptimizedMessages(
        messages: List<AIMessage>,
        activeMemories: List<MemoryEntity>
    ): List<AIMessage> {
        if (messages.isEmpty()) return emptyList()

        val recentMessages = if (messages.size > verbatimRecentTurns) {
            messages.takeLast(verbatimRecentTurns)
        } else {
            messages
        }

        val olderMessages = if (messages.size > verbatimRecentTurns) {
            messages.dropLast(verbatimRecentTurns)
        } else {
            emptyList()
        }

        val result = mutableListOf<AIMessage>()

        // Inject compacted summary of older turns if present
        if (olderMessages.isNotEmpty()) {
            val summaryBuilder = StringBuilder("[Prior conversation context summary:\n")
            olderMessages.forEach { msg ->
                val snippet = if (msg.content.length > 80) msg.content.take(80) + "..." else msg.content
                summaryBuilder.append("- ").append(msg.role).append(": ").append(snippet).append("\n")
            }
            summaryBuilder.append("]")
            result.add(AIMessage(role = "system", content = summaryBuilder.toString()))
        }

        // Add recent verbatim turns
        result.addAll(recentMessages)
        return result
    }

    /**
     * Formats user memory facts into a compact, non-redundant system instruction block.
     */
    fun formatMemoryContext(memories: List<MemoryEntity>, ownerName: String): String {
        if (memories.isEmpty()) {
            return "Owner: $ownerName."
        }
        val builder = StringBuilder("Owner: $ownerName. Verified facts & memory:\n")
        memories.take(15).forEach { mem ->
            builder.append("• [${mem.category}] ${mem.key}: ${mem.value}\n")
        }
        return builder.toString().trim()
    }
}
