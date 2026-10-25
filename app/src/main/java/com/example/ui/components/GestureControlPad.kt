package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraGreen
import com.example.ui.theme.AiraPurple
import com.example.ui.theme.AiraRed
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.atan2

@Composable
fun GestureControlPad(
    onCircleGesture: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val points = remember { mutableStateListOf<Offset>() }
    var detectedGestureText by remember { mutableStateOf("Touch pad ready for gestures") }

    fun vibrateShort() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(35)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .testTag("gesture_control_pad")
    ) {
        // Touch gesture capture canvas & trail
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            vibrateShort()
                            detectedGestureText = "Double Tap: Halted Action"
                            onDoubleTap()
                        },
                        onLongPress = {
                            vibrateShort()
                            detectedGestureText = "Long Press: Listening to Voice..."
                            onLongPress()
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            points.clear()
                            points.add(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            points.add(change.position)
                        },
                        onDragEnd = {
                            if (points.size >= 6) {
                                val first = points.first()
                                val last = points.last()
                                val dy = last.y - first.y
                                val dx = last.x - first.x

                                // Check if trajectory resembles a loop/circle (start and end close together)
                                val distanceStartEnd = (last - first).getDistance()
                                val totalPathLength = (1 until points.size).sumOf {
                                    (points[it] - points[it - 1]).getDistance().toDouble()
                                }.toFloat()

                                if (distanceStartEnd < 110f && totalPathLength > 240f) {
                                    vibrateShort()
                                    detectedGestureText = "Circle (O): Scan & Read Screen"
                                    onCircleGesture()
                                } else if (abs(dy) > abs(dx) && abs(dy) > 100f) {
                                    if (dy < 0) {
                                        vibrateShort()
                                        detectedGestureText = "Swipe Up: Quick Action Drawer"
                                        onSwipeUp()
                                    } else {
                                        vibrateShort()
                                        detectedGestureText = "Swipe Down: Biometric Lockdown"
                                        onSwipeDown()
                                    }
                                }
                            }
                            points.clear()
                        },
                        onDragCancel = {
                            points.clear()
                        }
                    )
                }
        ) {
            // Background subtle grid lines
            val step = 32.dp.toPx()
            for (x in 0..(size.width / step).toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.03f),
                    start = Offset(x * step, 0f),
                    end = Offset(x * step, size.height)
                )
            }
            for (y in 0..(size.height / step).toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.03f),
                    start = Offset(0f, y * step),
                    end = Offset(size.width, y * step)
                )
            }

            // Glowing touch trail
            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(AiraPurple, AiraCyan, Color.White)
                    ),
                    style = Stroke(
                        width = 5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }

        // Overlay Guide & Feedback
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEURAL GESTURE PAD",
                    style = MaterialTheme.typography.labelSmall,
                    color = AiraCyan,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = detectedGestureText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Quick legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GesturePill(icon = Icons.Default.Refresh, label = "Draw (O) Scan", color = AiraCyan)
                GesturePill(icon = Icons.Default.PlayArrow, label = "Swipe ↑ Routine", color = AiraGreen)
                GesturePill(icon = Icons.Default.Fingerprint, label = "Swipe ↓ Lock", color = AiraRed)
                GesturePill(icon = Icons.Default.Mic, label = "Hold Voice", color = AiraPurple)
                GesturePill(icon = Icons.Default.Stop, label = "2x Tap Halt", color = Color.White)
            }
        }
    }
}

@Composable
private fun GesturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.12f))
                .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 9.sp
        )
    }
}
