package com.example.permissions

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat

enum class CapabilityStatus {
    AVAILABLE,
    GRANTED,
    NOT_GRANTED,
    NOT_SUPPORTED,
    REQUIRES_SPECIAL_ACCESS,
    ERROR
}

data class CapabilityInfo(
    val id: String,
    val name: String,
    val description: String,
    val requiredPermission: String,
    val status: CapabilityStatus,
    val isSpecialAccess: Boolean = false,
    val minSdk: Int = 24,
    val limitations: String,
    val verified: Boolean
)

class PermissionManager(private val context: Context) {

    fun checkAllCapabilities(): List<CapabilityInfo> {
        val list = mutableListOf<CapabilityInfo>()

        // 1. Microphone
        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val micSupported = context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        list.add(
            CapabilityInfo(
                id = "CAP_MIC",
                name = "Microphone & Voice Input",
                description = "Captures spoken voice input for LUX speech recognition.",
                requiredPermission = Manifest.permission.RECORD_AUDIO,
                status = when {
                    !micSupported -> CapabilityStatus.NOT_SUPPORTED
                    hasMic -> CapabilityStatus.GRANTED
                    else -> CapabilityStatus.NOT_GRANTED
                },
                limitations = "Requires explicit user trigger. Never listens covertly.",
                verified = hasMic
            )
        )

        // 2. Camera
        val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val cameraSupported = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        list.add(
            CapabilityInfo(
                id = "CAP_CAMERA",
                name = "Visual Camera Analysis",
                description = "Captures imagery when explicitly requested for visual understanding.",
                requiredPermission = Manifest.permission.CAMERA,
                status = when {
                    !cameraSupported -> CapabilityStatus.NOT_SUPPORTED
                    hasCamera -> CapabilityStatus.GRANTED
                    else -> CapabilityStatus.NOT_GRANTED
                },
                limitations = "Active only while camera session is visible on screen.",
                verified = hasCamera
            )
        )

        // 3. Notifications
        val notifStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (granted) CapabilityStatus.GRANTED else CapabilityStatus.NOT_GRANTED
        } else {
            CapabilityStatus.GRANTED
        }
        list.add(
            CapabilityInfo(
                id = "CAP_NOTIF",
                name = "System Notifications",
                description = "Dispatches task completion and background assistant updates.",
                requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else "Installed",
                status = notifStatus,
                limitations = "Subject to device notification and DND settings.",
                verified = notifStatus == CapabilityStatus.GRANTED
            )
        )

        // 4. Location
        val hasLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        list.add(
            CapabilityInfo(
                id = "CAP_LOCATION",
                name = "Geographic Location",
                description = "Provides local context for weather, directions, and nearby queries.",
                requiredPermission = Manifest.permission.ACCESS_FINE_LOCATION,
                status = if (hasLocation) CapabilityStatus.GRANTED else CapabilityStatus.NOT_GRANTED,
                limitations = "Coarse or fine coordinate access only when prompted.",
                verified = hasLocation
            )
        )

        // 5. Display Over Other Apps (Floating Orb)
        val canDrawOverlay = Settings.canDrawOverlays(context)
        list.add(
            CapabilityInfo(
                id = "CAP_OVERLAY",
                name = "Display Over Other Apps (Floating LUX Orb)",
                description = "Enables the floating draggable LUX orb widget above other apps.",
                requiredPermission = "android.permission.SYSTEM_ALERT_WINDOW",
                status = if (canDrawOverlay) CapabilityStatus.GRANTED else CapabilityStatus.REQUIRES_SPECIAL_ACCESS,
                isSpecialAccess = true,
                limitations = "Requires manual enablement in Android system settings.",
                verified = canDrawOverlay
            )
        )

        // 6. Screen Capture
        list.add(
            CapabilityInfo(
                id = "CAP_SCREEN_CAPTURE",
                name = "Screen Capture & Understanding",
                description = "Analyzes visible on-screen context via legitimate Android MediaProjection.",
                requiredPermission = "android.media.projection.MediaProjection",
                status = CapabilityStatus.REQUIRES_SPECIAL_ACCESS,
                isSpecialAccess = true,
                limitations = "Requires explicit Android system permission prompt per session. Never accesses app storage/passwords.",
                verified = true
            )
        )

        // 7. Accessibility
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val isA11yEnabled = am?.isEnabled == true
        list.add(
            CapabilityInfo(
                id = "CAP_A11Y",
                name = "Accessibility Assistance",
                description = "Aids in screen reading and interaction for assisted navigation.",
                requiredPermission = "android.permission.BIND_ACCESSIBILITY_SERVICE",
                status = if (isA11yEnabled) CapabilityStatus.GRANTED else CapabilityStatus.REQUIRES_SPECIAL_ACCESS,
                isSpecialAccess = true,
                limitations = "Respects Android sandboxing; cannot bypass system security.",
                verified = isA11yEnabled
            )
        )

        // 8. Battery Optimization / Background
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isIgnoringBattery = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        list.add(
            CapabilityInfo(
                id = "CAP_BATTERY",
                name = "Unrestricted Background Operation",
                description = "Ensures background tasks and live headphone sessions are not killed prematurely.",
                requiredPermission = "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
                status = if (isIgnoringBattery) CapabilityStatus.GRANTED else CapabilityStatus.REQUIRES_SPECIAL_ACCESS,
                isSpecialAccess = true,
                limitations = "Managed by Android battery power-saving policies.",
                verified = isIgnoringBattery
            )
        )

        // 9. System Speech Synthesis (TTS)
        list.add(
            CapabilityInfo(
                id = "CAP_TTS",
                name = "Speech Synthesis Engine",
                description = "Android local Text-To-Speech engine for offline spoken responses.",
                requiredPermission = "System TextToSpeech Engine",
                status = CapabilityStatus.GRANTED,
                limitations = "Dependent on installed system voice data.",
                verified = true
            )
        )

        return list
    }

    fun openSpecialAccessSettings(capabilityId: String) {
        when (capabilityId) {
            "CAP_OVERLAY" -> {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val genericIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(genericIntent)
                }
            }
            "CAP_A11Y" -> {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            "CAP_BATTERY" -> {
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${context.packageName}")
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                try {
                    context.startActivity(intent)
                } catch (_: Exception) {
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallbackIntent)
                }
            }
            "CAP_NOTIF" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            }
            else -> {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${context.packageName}")
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(intent)
            }
        }
    }
}
