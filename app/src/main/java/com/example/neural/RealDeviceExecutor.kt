package com.example.neural

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.accessibility.AiraAccessibilityService

object RealDeviceExecutor {

    fun executeAction(context: Context, action: AiraExecutionAction): Boolean {
        return try {
            when (action.kind) {
                ActionKind.SEND_SMS -> {
                    val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:${Uri.encode(action.target)}")
                        putExtra("sms_body", action.payload)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(smsIntent)
                    true
                }

                ActionKind.CALL_PHONE -> {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${Uri.encode(action.target)}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(dialIntent)
                    true
                }

                ActionKind.OPEN_APP -> {
                    when (action.target) {
                        "SETTINGS" -> {
                            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                            true
                        }
                        "CAMERA" -> {
                            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                            true
                        }
                        "YOUTUBE" -> {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            } else {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(webIntent)
                            }
                            true
                        }
                        "WHATSAPP" -> {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            } else {
                                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com")).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(webIntent)
                            }
                            true
                        }
                        else -> {
                            // Find matching app by name
                            val pm = context.packageManager
                            val apps = pm.getInstalledApplications(0)
                            val matched = apps.firstOrNull { app ->
                                pm.getApplicationLabel(app).toString().contains(action.target, ignoreCase = true)
                            }
                            if (matched != null) {
                                val intent = pm.getLaunchIntentForPackage(matched.packageName)
                                if (intent != null) {
                                    context.startActivity(intent)
                                    return true
                                }
                            }
                            // Fallback to web search
                            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                putExtra(SearchManager.QUERY, action.target)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(searchIntent)
                            true
                        }
                    }
                }

                ActionKind.WEB_SEARCH -> {
                    val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(SearchManager.QUERY, action.target)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(searchIntent)
                    true
                }

                ActionKind.SYSTEM_NAV -> {
                    val service = AiraAccessibilityService.instance.value
                    if (service != null && AiraAccessibilityService.isConnected.value) {
                        when (action.target) {
                            "HOME" -> service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME)
                            "BACK" -> service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
                            "RECENTS" -> service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS)
                            "NOTIFICATIONS" -> service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
                            "SCREENSHOT" -> service.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
                            else -> false
                        }
                    } else {
                        // Open accessibility settings if service is off
                        val a11yIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(a11yIntent)
                        true
                    }
                }

                ActionKind.SET_TIMER -> {
                    val seconds = action.target.toIntOrNull() ?: 300
                    val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                        putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                        putExtra(AlarmClock.EXTRA_MESSAGE, "Aira Timer")
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(timerIntent)
                    true
                }

                ActionKind.READ_SCREEN -> {
                    if (action.target == "ACCESSIBILITY_SETTINGS") {
                        val a11yIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(a11yIntent)
                    }
                    true
                }

                ActionKind.CHAT_ONLY -> true
            }
        } catch (_: Exception) {
            false
        }
    }
}
