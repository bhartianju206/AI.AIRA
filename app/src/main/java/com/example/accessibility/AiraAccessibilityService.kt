package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiraAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "AIRA Accessibility Service connected")
        _instance.value = this
        _isConnected.value = true
        captureActiveScreen()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: ""
        // Capture on window state changed or content changed
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED
        ) {
            captureActiveScreen(pkg)
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "AIRA Accessibility Service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (_instance.value == this) {
            _instance.value = null
            _isConnected.value = false
        }
    }

    fun captureActiveScreen(explicitPkg: String = ""): ScreenSnapshot {
        val root = rootInActiveWindow
        val nodes = mutableListOf<NodeElement>()
        val texts = mutableListOf<String>()

        if (root != null) {
            traverseNode(root, nodes, texts)
            try {
                root.recycle()
            } catch (_: Exception) {}
        }

        val pkg = if (explicitPkg.isNotBlank()) explicitPkg else (nodes.firstOrNull()?.id?.substringBefore(":") ?: "Android System")
        val summary = if (texts.isNotEmpty()) {
            "Active app: $pkg. Found ${nodes.size} interactive nodes and ${texts.size} readable text blocks. Screen elements: " +
                texts.take(5).joinToString("; ")
        } else {
            "Active screen: $pkg. No visible text nodes detected."
        }

        val snapshot = ScreenSnapshot(
            packageName = pkg,
            timestamp = System.currentTimeMillis(),
            nodeCount = nodes.size,
            interactiveElements = nodes,
            readableTexts = texts,
            summaryText = summary
        )
        _latestScreenSnapshot.value = snapshot
        return snapshot
    }

    private fun traverseNode(
        node: AccessibilityNodeInfo,
        outNodes: MutableList<NodeElement>,
        outTexts: MutableList<String>
    ) {
        val text = node.text?.toString()?.trim() ?: ""
        val desc = node.contentDescription?.toString()?.trim() ?: ""
        val viewId = node.viewIdResourceName ?: ""
        val isClickable = node.isClickable
        val isEditable = node.isEditable
        val isScrollable = node.isScrollable
        val isPassword = node.isPassword

        if (text.isNotBlank() && !isPassword) {
            outTexts.add(text)
        } else if (desc.isNotBlank() && !isPassword) {
            outTexts.add(desc)
        }

        if (isClickable || isEditable || isScrollable || text.isNotBlank() || desc.isNotBlank()) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            outNodes.add(
                NodeElement(
                    id = "$viewId-${outNodes.size}",
                    text = if (isPassword) "••••••" else text,
                    contentDescription = desc,
                    className = node.className?.toString() ?: "",
                    viewId = viewId,
                    isClickable = isClickable,
                    isEditable = isEditable,
                    isScrollable = isScrollable,
                    isPassword = isPassword,
                    bounds = rect
                )
            )
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            traverseNode(child, outNodes, outTexts)
            try {
                child.recycle()
            } catch (_: Exception) {}
        }
    }

    fun findAndClick(targetTextOrId: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val cleanTarget = targetTextOrId.trim()
        val found = findMatchingNode(root, cleanTarget)
        val success = if (found != null) {
            var clicked = found.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (!clicked) {
                // Try parent if child is text view inside clickable button
                var parent = found.parent
                while (parent != null && !clicked) {
                    clicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    parent = parent.parent
                }
            }
            if (!clicked) {
                // Fall back to tap gesture at center of node
                val rect = Rect()
                found.getBoundsInScreen(rect)
                tapAtCoordinates(rect.centerX().toFloat(), rect.centerY().toFloat())
            }
            true
        } else {
            false
        }
        try {
            root.recycle()
        } catch (_: Exception) {}
        return success
    }

    fun findAndSetText(query: String, textToSet: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val target = findEditableNode(root, query)
        val success = if (target != null) {
            val arguments = Bundle().apply {
                putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    textToSet
                )
            }
            target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        } else {
            false
        }
        try {
            root.recycle()
        } catch (_: Exception) {}
        return success
    }

    fun performScroll(forward: Boolean): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollable = findScrollableNode(root)
        val action = if (forward) {
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        } else {
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        }
        val success = scrollable?.performAction(action) ?: false
        try {
            root.recycle()
        } catch (_: Exception) {}
        return success
    }

    fun tapAtCoordinates(x: Float, y: Float): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, 100)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, durationMs: Long = 300L): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        return dispatchGesture(gesture, null, null)
    }

    private fun findMatchingNode(node: AccessibilityNodeInfo, target: String): AccessibilityNodeInfo? {
        val text = node.text?.toString() ?: ""
        val desc = node.contentDescription?.toString() ?: ""
        val viewId = node.viewIdResourceName ?: ""

        if (text.contains(target, ignoreCase = true) ||
            desc.contains(target, ignoreCase = true) ||
            viewId.contains(target, ignoreCase = true)
        ) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findMatchingNode(child, target)
            if (result != null) return result
        }
        return null
    }

    private fun findEditableNode(node: AccessibilityNodeInfo, query: String): AccessibilityNodeInfo? {
        if (node.isEditable) {
            if (query.isBlank() ||
                (node.text?.toString()?.contains(query, ignoreCase = true) == true) ||
                (node.hintText?.toString()?.contains(query, ignoreCase = true) == true)
            ) {
                return node
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findEditableNode(child, query)
            if (result != null) return result
        }
        return null
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findScrollableNode(child)
            if (result != null) return result
        }
        return null
    }

    companion object {
        private const val TAG = "AiraAccessibility"
        private val _instance = MutableStateFlow<AiraAccessibilityService?>(null)
        val instance: StateFlow<AiraAccessibilityService?> = _instance.asStateFlow()

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private val _latestScreenSnapshot = MutableStateFlow(
            ScreenSnapshot(
                packageName = "com.aistudio.aira",
                windowTitle = "AIRA AI Neural Console",
                nodeCount = 14,
                readableTexts = listOf(
                    "AIRA AI Neural Shield Active",
                    "Encrypted Local Engine v4.2",
                    "0.0 KB Data Egress (100% Offline)",
                    "Accessibility Ready"
                ),
                summaryText = "AIRA AI Console active. Local Neural Engine ready for autonomous commands."
            )
        )
        val latestScreenSnapshot: StateFlow<ScreenSnapshot> = _latestScreenSnapshot.asStateFlow()

        fun updateSimulatedSnapshot(snapshot: ScreenSnapshot) {
            _latestScreenSnapshot.value = snapshot
        }
    }
}
