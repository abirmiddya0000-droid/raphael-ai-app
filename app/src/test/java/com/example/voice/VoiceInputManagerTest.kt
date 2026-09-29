package com.example.voice

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VoiceInputManagerTest {

    private lateinit var context: Context
    private lateinit var voiceInputManager: VoiceInputManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        voiceInputManager = VoiceInputManager(context)
    }

    @Test
    fun testInitialStateIsIdle() {
        assertEquals(VoiceState.Idle, voiceInputManager.currentState)
    }

    @Test
    fun testStopAndCancelResetToIdle() {
        voiceInputManager.stopListening()
        assertEquals(VoiceState.Idle, voiceInputManager.currentState)

        voiceInputManager.cancelListening()
        assertEquals(VoiceState.Idle, voiceInputManager.currentState)
    }

    @Test
    fun testVoiceStatePayloads() {
        val successState = VoiceState.Success("What is on my calendar?")
        assertEquals("What is on my calendar?", successState.recognizedText)

        val partialState = VoiceState.PartialResult("What is on", 12.5f)
        assertEquals("What is on", partialState.text)
        assertEquals(12.5f, partialState.rmsdB, 0.001f)

        val errorState = VoiceState.Error("Microphone error", 3)
        assertEquals("Microphone error", errorState.message)
        assertEquals(3, errorState.errorCode)

        val listeningState = VoiceState.Listening(isStreaming = true)
        assertTrue(listeningState.isStreaming)
    }

    @Test
    fun testManagerInstantiationNotNull() {
        assertNotNull(voiceInputManager)
    }
}
