package com.example.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CyberVisualizerOrbTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCyberOrbRendersAndHandlesClick() {
        var clicked = false
        composeTestRule.setContent {
            CyberVisualizerOrb(
                isActive = true,
                audioAmplitude = 10f,
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithTag("cyber_visualizer_orb").assertIsDisplayed()
        composeTestRule.onNodeWithTag("equalizer_dots").assertIsDisplayed()
        composeTestRule.onNodeWithTag("cyber_visualizer_orb").performClick()
        assertTrue(clicked)
    }
}
