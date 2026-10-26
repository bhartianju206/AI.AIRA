package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraPink

private sealed class MessagePart {
    data class TextPart(val text: String) : MessagePart()
    data class CodePart(val language: String, val code: String) : MessagePart()
}

@Composable
fun AiraMessageRenderer(
    text: String,
    modifier: Modifier = Modifier
) {
    val parts = parseMarkdown(text)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (part in parts) {
            when (part) {
                is MessagePart.TextPart -> {
                    FormattedTextBlock(text = part.text)
                }
                is MessagePart.CodePart -> {
                    CodeSnippetCard(language = part.language, code = part.code)
                }
            }
        }
    }
}

@Composable
private fun FormattedTextBlock(text: String) {
    val lines = text.split("\n")
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                continue
            }

            if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
                val bulletContent = trimmed.substring(2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        color = AiraPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = buildInlineAnnotatedString(bulletContent),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            } else if (trimmed.matches(Regex("^\\d+\\.\\s+.*"))) {
                val numPrefix = trimmed.substringBefore(". ") + "."
                val rest = trimmed.substringAfter(". ")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = numPrefix,
                        color = AiraCyan,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = buildInlineAnnotatedString(rest),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            } else {
                Text(
                    text = buildInlineAnnotatedString(trimmed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
private fun CodeSnippetCard(
    language: String,
    code: String
) {
    val context = LocalContext.current
    val codeBg = Color(0xFF161B22)
    val headerBg = Color(0xFF0D1117)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(codeBg)
            .border(1.dp, Color(0xFF30363D), RoundedCornerShape(10.dp))
    ) {
        Column {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifBlank { "code" },
                    color = Color(0xFF8B949E),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("code", code)
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = Color(0xFF8B949E),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Code content with horizontal scroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(10.dp)
            ) {
                Text(
                    text = code,
                    color = Color(0xFFE6EDF3),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private fun parseMarkdown(text: String): List<MessagePart> {
    val parts = mutableListOf<MessagePart>()
    val lines = text.split("\n")
    var inCodeBlock = false
    var currentLanguage = ""
    val codeBuilder = StringBuilder()
    val textBuilder = StringBuilder()

    for (line in lines) {
        if (line.trim().startsWith("```")) {
            if (inCodeBlock) {
                // End code block
                parts.add(MessagePart.CodePart(currentLanguage, codeBuilder.toString().trimEnd()))
                codeBuilder.clear()
                currentLanguage = ""
                inCodeBlock = false
            } else {
                // Start code block
                if (textBuilder.isNotEmpty()) {
                    parts.add(MessagePart.TextPart(textBuilder.toString().trimEnd()))
                    textBuilder.clear()
                }
                currentLanguage = line.trim().removePrefix("```").trim()
                inCodeBlock = true
            }
        } else {
            if (inCodeBlock) {
                codeBuilder.append(line).append("\n")
            } else {
                textBuilder.append(line).append("\n")
            }
        }
    }

    if (inCodeBlock && codeBuilder.isNotEmpty()) {
        parts.add(MessagePart.CodePart(currentLanguage, codeBuilder.toString().trimEnd()))
    } else if (textBuilder.isNotEmpty()) {
        parts.add(MessagePart.TextPart(textBuilder.toString().trimEnd()))
    }

    if (parts.isEmpty()) {
        parts.add(MessagePart.TextPart(text))
    }

    return parts
}

private fun buildInlineAnnotatedString(text: String) = buildAnnotatedString {
    var cursor = 0
    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    val matches = boldRegex.findAll(text).toList()

    for (match in matches) {
        if (match.range.first > cursor) {
            append(text.substring(cursor, match.range.first))
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) {
        append(text.substring(cursor))
    }
}
