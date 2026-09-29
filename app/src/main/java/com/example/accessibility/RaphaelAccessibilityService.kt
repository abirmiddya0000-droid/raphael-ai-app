package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * RAPHAEL Android Accessibility Service.
 * Allows programmatic automation, finding text/nodes on screen, performing gestures,
 * and navigating apps on behalf of Abir.
 *
 * NOTE: Strictly adheres to the Anti-Simulation Rule: Does NOT claim to play 60 FPS
 * action shooters (e.g. Free Fire / BGMI) directly, as accessibility dispatch has ~200ms latency.
 * Provides strategic tactical guidance and menu navigation instead.
 */
class RaphaelAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: RaphaelAccessibilityService? = null
            private set

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        /**
         * Finds and clicks a view with matching text on screen.
         */
        fun clickNodeWithText(text: String): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            val nodes = root.findAccessibilityNodeInfosByText(text)
            for (node in nodes) {
                if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    return true
                }
                // Try parent if target itself isn't directly clickable
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                        return true
                    }
                    parent = parent.parent
                }
            }
            return false
        }

        /**
         * Scrolls the active window forward (down).
         */
        fun scrollDown(): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            return root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }

        /**
         * Scrolls the active window backward (up).
         */
        fun scrollUp(): Boolean {
            val root = instance?.rootInActiveWindow ?: return false
            return root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        }

        /**
         * Performs a programmatic swipe gesture on screen.
         */
        fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300): Boolean {
            val currentService = instance ?: return false
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            return currentService.dispatchGesture(gesture, null, null)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Observes window state and content changes for contextual awareness
    }

    override fun onInterrupt() {
        _isServiceActive.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceActive.value = false
    }
}
