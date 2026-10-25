package com.example.security

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import com.example.accessibility.AiraAccessibilityService

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val technicalName: String,
    val icon: ImageVector,
    val isGranted: Boolean,
    val isCritical: Boolean,
    val actionLabel: String,
    val category: String
)

object PermissionManager {

    fun isAccessibilityEnabled(context: Context): Boolean {
        if (AiraAccessibilityService.isConnected.value) return true
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedPackage = context.packageName
        return enabledServices.any { it.resolveInfo.serviceInfo.packageName == expectedPackage }
    }

    fun isAudioPermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isOverlayPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isBiometricSupported(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
        return keyguardManager?.isDeviceSecure == true
    }

    fun getAllPermissions(context: Context): List<PermissionItem> {
        val isA11y = isAccessibilityEnabled(context)
        val isAudio = isAudioPermissionGranted(context)
        val isOverlay = isOverlayPermissionGranted(context)
        val isNotif = isNotificationPermissionGranted(context)
        val isBio = isBiometricSupported(context)

        return listOf(
            PermissionItem(
                id = "accessibility",
                title = "Accessibility Automation Service",
                description = "Required to inspect screen nodes, autonomously click, type messages, scroll, and perform system gestures.",
                technicalName = "android.permission.BIND_ACCESSIBILITY_SERVICE",
                icon = Icons.Default.Visibility,
                isGranted = isA11y,
                isCritical = true,
                actionLabel = if (isA11y) "Configured" else "Enable in Settings",
                category = "AUTOMATION"
            ),
            PermissionItem(
                id = "audio",
                title = "Voice Input & Audio Telemetry",
                description = "Powers offline speech recognition, natural voice commands, and real-time audio waveform processing.",
                technicalName = "android.permission.RECORD_AUDIO",
                icon = Icons.Default.Mic,
                isGranted = isAudio,
                isCritical = true,
                actionLabel = if (isAudio) "Granted" else "Grant Mic Access",
                category = "VOICE"
            ),
            PermissionItem(
                id = "overlay",
                title = "Display Over Other Apps (HUD)",
                description = "Enables AIRA's floating holographic HUD controller and gesture pill above external Android apps.",
                technicalName = "android.permission.SYSTEM_ALERT_WINDOW",
                icon = Icons.Default.PictureInPicture,
                isGranted = isOverlay,
                isCritical = false,
                actionLabel = if (isOverlay) "Enabled" else "Authorize Overlay",
                category = "INTERFACE"
            ),
            PermissionItem(
                id = "notifications",
                title = "Security & Execution Alerts",
                description = "Pushes immediate alerts for completed autonomous routines and biometric lockdown events.",
                technicalName = "android.permission.POST_NOTIFICATIONS",
                icon = Icons.Default.Notifications,
                isGranted = isNotif,
                isCritical = false,
                actionLabel = if (isNotif) "Allowed" else "Allow Alerts",
                category = "ALERTS"
            ),
            PermissionItem(
                id = "biometrics",
                title = "Hardware Biometric Vault",
                description = "AndroidKeyStore AES-256 cryptographic vault protection for autonomous message dispatch and panic locks.",
                technicalName = "android.permission.USE_BIOMETRIC",
                icon = Icons.Default.Fingerprint,
                isGranted = isBio,
                isCritical = true,
                actionLabel = if (isBio) "Secured" else "Configure Device Lock",
                category = "SECURITY"
            ),
            PermissionItem(
                id = "packages",
                title = "Installed Applications Query",
                description = "Permits AIRA to autonomously launch targeted third-party messaging, camera, and system apps.",
                technicalName = "android.permission.QUERY_ALL_PACKAGES",
                icon = Icons.Default.Apps,
                isGranted = true, // Manifest-granted install time
                isCritical = false,
                actionLabel = "System Granted",
                category = "SYSTEM"
            )
        )
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
