package com.example.tts

sealed class TTSResult {
    object Success : TTSResult()
    data class Error(val message: String) : TTSResult()
}

interface TTSProvider {
    val name: String
    suspend fun synthesizeAndPlay(
        text: String,
        onStart: () -> Unit,
        onDone: () -> Unit
    ): TTSResult
    fun stop()
    fun release()
}
