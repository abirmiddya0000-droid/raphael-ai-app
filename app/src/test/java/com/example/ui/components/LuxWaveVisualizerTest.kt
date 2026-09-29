package com.example.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LuxWaveVisualizerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testWaveVisualizerAppearsWhenListening() {
        composeTestRule.setContent {
            LuxWaveVisualizer(
                isListening = true,
                audioAmplitude = 8.5f
            )
        }

        composeTestRule.onNodeWithTag("lux_wave_visualizer").assertExists()
    }

    @Test
    fun testWaveVisualizerHiddenWhenNotListening() {
        composeTestRule.setContent {
            LuxWaveVisualizer(
                isListening = false,
                audioAmplitude = 0f
            )
        }

        // When not listening and alpha reaches 0, the Canvas returns early and is not emitted
        composeTestRule.onNodeWithTag("lux_wave_visualizer").assertDoesNotExist()
    }
}
