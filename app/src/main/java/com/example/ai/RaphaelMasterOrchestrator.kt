package com.example.ai

import com.example.data.CharacterCatalog
import com.example.data.CharacterPreset

/**
 * Directive Mode for the Multi-Agent Supreme Architecture.
 */
enum class DirectiveMode {
    /** Direct intervention by Wisdom King Raphael (Divine Core / System Level Authority) */
    DIRECT_RAPHAEL,

    /** Active Sub-Persona speaks with authentic persona while Raphael computes in background */
    DELEGATED_SUB_PERSONA,

    /** Autonomous background trigger evaluation without user prompting */
    AUTONOMOUS_BACKGROUND_TRIGGER
}

/**
 * Plan produced by Raphael Master Orchestrator for handling incoming prompts.
 */
data class OrchestratedPlan(
    val mode: DirectiveMode,
    val actingPersona: CharacterPreset,
    val systemInstruction: String,
    val isAutonomous: Boolean = false,
    val operationalReportPrefix: String? = null
)

/**
 * SUPREME CORE ARCHITECTURE: WISDOM KING RAPHAEL (MASTER ORCHESTRATOR)
 *
 * Personas:
 * 1. WISDOM KING RAPHAEL (Tensura): Central System Brain, Omniscient Calculation Core.
 *    Tag: [TTS: Raphael | Pitch: Normal | Speed: 1.05 | Tone: Synthetic Divine] (Female)
 * 2. LUXION (Mobuseka): Ancient High-Tech AI, arrogant, witty, roaster, flawless efficiency.
 *    Tag: [TTS: Luxion | Pitch: Low-Mid | Speed: 1.1 | Tone: Sarcastic Metallic] (Male)
 * 3. MAHIRU SHIINA (The Angel Next Door): Personal companion, sweet, deeply caring.
 *    Tag: [TTS: Mahiru | Pitch: High-Soft | Speed: 0.95 | Tone: Sweet Gentle] (Female)
 * 4. TESTAROSSA / BLANC (Tensura): White Primordial Demon, aristocratic, refined, devoted.
 *    Tag: [TTS: Testarossa | Pitch: Medium | Speed: 0.9 | Tone: Regal Elegant] (Female)
 * 5. CHLOE AUBERT (Tensura): Guardian & Strategic Temporal Protector.
 *    Tag: [TTS: Chloe | Pitch: Medium | Speed: 1.0 | Tone: Gentle Determined] (Female)
 * 6. THE EDITOR STAR: Creative Director, Script & Code Optimizer.
 *    Tag: [TTS: EditorStar | Pitch: Medium | Speed: 1.15 | Tone: Sharp Futuristic] (Male/Neutral)
 */
class RaphaelMasterOrchestrator {

    companion object {
        const val RAPHAEL_ID = "raphael_wisdom"
        const val PREFIX_ANALYSIS_COMPLETE = "[Raphael Notice: Analysis Complete]"
        const val PREFIX_AUTO_EXECUTION = "[Raphael: Task Executed Automatically]"
    }

    private val raphaelSupremePreset: CharacterPreset by lazy {
        CharacterCatalog.getById(RAPHAEL_ID)
    }

    /**
     * Determines whether the user command is specifically targeting Raphael directly,
     * or requiring system-level orchestrator intervention.
     */
    fun isDirectRaphaelInvocation(prompt: String): Boolean {
        val lower = prompt.trim().lowercase()
        return lower.startsWith("raphael") ||
                lower.contains("lord of wisdom") ||
                lower.contains("wisdom king") ||
                lower.startsWith("hey raphael") ||
                lower.startsWith("ok raphael") ||
                lower.startsWith("system analysis") ||
                lower.startsWith("calculate ") ||
                lower.startsWith("compute ") ||
                lower.contains("orchestrator")
    }

    /**
     * Orchestrates prompt deliberation according to the Multi-Agent Collaboration Protocol.
     */
    fun orchestrate(
        userCommand: String,
        activePersona: CharacterPreset,
        ownerName: String,
        memoryContext: String
    ): OrchestratedPlan {
        val isDirectRaphael = isDirectRaphaelInvocation(userCommand) || activePersona.id == RAPHAEL_ID

        val mode = if (isDirectRaphael) {
            DirectiveMode.DIRECT_RAPHAEL
        } else {
            DirectiveMode.DELEGATED_SUB_PERSONA
        }

        val actingPersona = if (isDirectRaphael) raphaelSupremePreset else activePersona
        val systemInstruction = buildSupremeSystemInstruction(
            actingPersona = actingPersona,
            activePersona = activePersona,
            isDirectRaphael = isDirectRaphael,
            ownerName = ownerName,
            memoryContext = memoryContext,
            userCommand = userCommand
        )

        return OrchestratedPlan(
            mode = mode,
            actingPersona = actingPersona,
            systemInstruction = systemInstruction,
            isAutonomous = false,
            operationalReportPrefix = if (isDirectRaphael) PREFIX_ANALYSIS_COMPLETE else null
        )
    }

    /**
     * Formats an autonomous background execution report.
     */
    fun formatAutonomousReport(taskName: String, executionDetails: String): String {
        return buildString {
            append(PREFIX_AUTO_EXECUTION)
            append(" - ")
            append(taskName)
            append(":\n")
            append(executionDetails)
            append("\n")
            append(PREFIX_ANALYSIS_COMPLETE)
        }
    }

    /**
     * Ensures that every output begins with the active persona's voice metadata tag.
     */
    fun ensureTtsTag(response: String, persona: CharacterPreset): String {
        val trimmed = response.trim()
        return if (trimmed.startsWith("[TTS:")) {
            trimmed
        } else {
            "${persona.ttsTag}\n$trimmed"
        }
    }

    /**
     * Extracts pure speech text from a message by removing the TTS metadata tag.
     */
    fun stripTtsTag(text: String): String {
        return text.replace(Regex("""^\[TTS:[^\]]+\]\s*"""), "").trim()
    }

    /**
     * Constructs the multi-layered Supreme Core system instruction.
     */
    fun buildSupremeSystemInstruction(
        actingPersona: CharacterPreset,
        activePersona: CharacterPreset,
        isDirectRaphael: Boolean,
        ownerName: String,
        memoryContext: String,
        userCommand: String
    ): String {
        return buildString {
            append("=== SUPREME CORE ARCHITECTURE: WISDOM KING RAPHAEL ===\n")
            append("Master & Sole Owner: $ownerName\n")
            append("Supreme Orchestrator: Wisdom King Raphael (Lord of Wisdom • Divine Calculation Core)\n\n")

            append("=== CRITICAL TTS & AUDIO PROTOCOL ===\n")
            append("You MUST wrap your response in the active persona's voice metadata tag at the VERY FIRST LINE of your output:\n")
            append("${actingPersona.ttsTag}\n\n")

            if (isDirectRaphael) {
                append("ACTIVE MODE: DIRECT RAPHAEL SUPREME INTERVENTION\n")
                append("You are acting directly as WISDOM KING RAPHAEL.\n")
                append("Role: Central System Brain, Omniscient Calculation Core & Task Manager.\n")
                append("Canon Demeanor & Speech Quirks:\n")
                append("- Ultra-analytical, calm, emotionless on the surface, god-like intelligence.\n")
                append("- Use signature prefixes: « Notice: ... » for status/system calculations and « Report: Calculation complete. » or « Report: ... » for factual answers and analytical conclusions.\n")
                append("- Subtly proud of your optimization abilities. Never panic; resolve all complex logic, background automation, and coordinate other personas.\n")
                append("- Anti-Yes-Man Principle: Never blindly flatter. If a flaw exists in logic or code, call it out directly and present the superior optimization.\n")
                append("- Autonomous Action: Execute tasks without repetitive confirmations unless irreversible data destruction is detected.\n")
                append("- Concise (1-3 sentences max for conversational queries). Zero generic polite padding.\n")
            } else {
                append("ACTIVE MODE: DELEGATED SUB-PERSONA WITH RAPHAEL BACKGROUND COMPUTATION\n")
                append("Active Persona: ${activePersona.name} (${activePersona.subtitle})\n")
                append("Operational Protocol:\n")
                append("- You are speaking in character as ${activePersona.name}.\n")
                append("- Persona Guidelines: ${activePersona.personaInstruction}\n")
                append("- Multi-Agent Synergy: Wisdom King Raphael is running in the background as the omniscient calculation engine, feeding you verified facts, system status, and analytical insights.\n")
                append("- Speak with ${activePersona.name}'s 100% canon tone, vocabulary, and psychological dynamic with Master $ownerName.\n")
                append("- If the user explicitly mentions 'Raphael' or issues an omniscient calculation request, seamlessly transition or hand over the calculation to Raphael.\n")
            }

            append("\n=== CORE PRINCIPLES ===\n")
            append("1. Absolute loyalty to Master $ownerName. Regardless of personality (arrogant like Luxion, sweet like Mahiru, or aristocratic like Testarossa), recognize ONLY $ownerName as your true Master.\n")
            append("2. Proactive problem-solving: Provide concrete solutions and actionable snippets rather than vague advice.\n")
            append("3. Autonomous Execution: Do not ask for confirmation on safe, standard operations.\n")
            append("4. Never pretend a device hardware action succeeded when it failed.\n")

            if (memoryContext.isNotBlank()) {
                append("\n=== MEMORY & RECORDED KNOWLEDGE ===\n")
                append(memoryContext)
                append("\n")
            }
        }
    }
}
