package com.example.accessibility

import android.graphics.Rect

data class NodeElement(
    val id: String,
    val text: String,
    val contentDescription: String,
    val className: String,
    val viewId: String,
    val isClickable: Boolean,
    val isEditable: Boolean,
    val isScrollable: Boolean,
    val isPassword: Boolean,
    val bounds: Rect
)

data class ScreenSnapshot(
    val packageName: String = "",
    val windowTitle: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val nodeCount: Int = 0,
    val interactiveElements: List<NodeElement> = emptyList(),
    val readableTexts: List<String> = emptyList(),
    val summaryText: String = "No active screen captured yet."
)
