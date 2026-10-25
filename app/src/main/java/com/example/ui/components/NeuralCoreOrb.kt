package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AiraCyan
import com.example.ui.theme.AiraGreen
import com.example.ui.theme.AiraPurple
import com.example.ui.theme.AiraRed
import com.example.voice.VoiceState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NeuralCoreOrb(
    isExecuting: Boolean,
    isLocked: Boolean,
    voiceState: VoiceState,
    audioRms: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransitions")

    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingRot1"
    )

    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingRot2"
    )

    val rotation3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingRot3"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    val coreGlowColor = when {
        isLocked -> AiraRed
        voiceState == VoiceState.LISTENING -> AiraGreen
        isExecuting -> AiraCyan
        else -> AiraPurple
    }

    Box(
        modifier = modifier
            .size(210.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 105.dp),
                onClick = onClick
            )
            .testTag("neural_core_orb"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.70f
            val dynamicScale = pulseScale + (audioRms * 0.025f).coerceAtMost(0.35f)
            val radius = baseRadius * dynamicScale

            // Multi-layer Ambient Ethereal Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreGlowColor.copy(alpha = 0.45f),
                        coreGlowColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.55f
                ),
                radius = radius * 1.55f,
                center = center
            )

            // Radial Voice Equalizer Frequency Rays
            val rayCount = 28
            val isAudioActive = voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING
            for (i in 0 until rayCount) {
                val angleDeg = (i * (360f / rayCount)) + (rotation1 * 0.5f)
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val rayVariance = if (isAudioActive) {
                    (sin(i * 1.5 + rotation1 * 0.1) * (audioRms * 3.5f + 8f)).toFloat()
                } else {
                    (sin(i * 0.8) * 4f).toFloat()
                }
                val startDist = radius * 0.82f
                val endDist = (radius * 0.98f) + rayVariance.coerceAtLeast(0f)

                val sx = center.x + startDist * cos(angleRad).toFloat()
                val sy = center.y + startDist * sin(angleRad).toFloat()
                val ex = center.x + endDist * cos(angleRad).toFloat()
                val ey = center.y + endDist * sin(angleRad).toFloat()

                drawLine(
                    color = coreGlowColor.copy(alpha = if (isAudioActive) 0.8f else 0.35f),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Central Holographic Energy Iris
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        AiraCyan.copy(alpha = 0.85f),
                        coreGlowColor.copy(alpha = 0.65f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 0.60f
                ),
                radius = radius * 0.60f,
                center = center
            )

            // Ring 1 - Outer Cyber Orbit with 4 Glowing Nodes
            rotate(rotation1, pivot = center) {
                drawCircle(
                    color = AiraCyan.copy(alpha = 0.75f),
                    radius = radius * 0.95f,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )
                for (i in 0 until 4) {
                    val angle = Math.toRadians(i * 90.0)
                    val px = center.x + (radius * 0.95f) * cos(angle).toFloat()
                    val py = center.y + (radius * 0.95f) * sin(angle).toFloat()
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = AiraCyan,
                        radius = 7.dp.toPx(),
                        center = Offset(px, py),
                        style = Stroke(1.5.dp.toPx())
                    )
                }
            }

            // Ring 2 - Dashed Counter-Rotating Shield
            rotate(rotation2, pivot = center) {
                drawCircle(
                    color = AiraPurple.copy(alpha = 0.70f),
                    radius = radius * 1.12f,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                    )
                )
            }

            // Ring 3 - Outer Thin Reticle with 3 Precision Pips
            rotate(rotation3, pivot = center) {
                drawCircle(
                    color = coreGlowColor.copy(alpha = 0.45f),
                    radius = radius * 1.25f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
                for (i in 0 until 3) {
                    val angle = Math.toRadians(i * 120.0)
                    val px = center.x + (radius * 1.25f) * cos(angle).toFloat()
                    val py = center.y + (radius * 1.25f) * sin(angle).toFloat()
                    drawCircle(
                        color = coreGlowColor,
                        radius = 3.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}
