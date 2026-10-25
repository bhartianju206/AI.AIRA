package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraPurple
import com.example.ui.theme.DarkBackground

@Composable
fun CyberGridBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NebulaDrift")
    val driftOffset by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Drift"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Base background
            drawRect(color = DarkBackground)

            // Top-left glowing cyan nebula
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AiraCyan.copy(alpha = 0.12f),
                        AiraCyan.copy(alpha = 0.04f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.2f + driftOffset, size.height * 0.15f),
                    radius = size.width * 0.6f
                ),
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.2f + driftOffset, size.height * 0.15f)
            )

            // Bottom-right glowing purple nebula
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AiraPurple.copy(alpha = 0.15f),
                        AiraPurple.copy(alpha = 0.03f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.85f - driftOffset, size.height * 0.75f),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.85f - driftOffset, size.height * 0.75f)
            )

            // High-tech subtle grid pattern
            val gridSpacing = 42.dp.toPx()
            val gridColor = Color.White.copy(alpha = 0.025f)
            val numCols = (size.width / gridSpacing).toInt()
            val numRows = (size.height / gridSpacing).toInt()

            for (i in 0..numCols) {
                val x = i * gridSpacing
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }

            for (j in 0..numRows) {
                val y = j * gridSpacing
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            // Tech reticle corner accents
            val bracketLen = 16.dp.toPx()
            val bracketColor = AiraCyan.copy(alpha = 0.3f)
            val pad = 12.dp.toPx()

            // Top-left bracket
            drawLine(bracketColor, Offset(pad, pad), Offset(pad + bracketLen, pad), 2f)
            drawLine(bracketColor, Offset(pad, pad), Offset(pad, pad + bracketLen), 2f)

            // Top-right bracket
            drawLine(bracketColor, Offset(size.width - pad, pad), Offset(size.width - pad - bracketLen, pad), 2f)
            drawLine(bracketColor, Offset(size.width - pad, pad), Offset(size.width - pad, pad + bracketLen), 2f)
        }

        content()
    }
}
