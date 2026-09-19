package com.tictactoe.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tictactoe.game.model.BoardTheme
import com.tictactoe.game.model.CameraPreset

/**
 * Custom Canvas vector icons that render 100% consistently across all Android versions
 * with zero dependency on emoji fonts or system character sets.
 */

@Composable
fun SoundVectorIcon(
    isEnabled: Boolean,
    color: Color = Color.White,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Speaker cone path
        val speakerPath = Path().apply {
            moveTo(w * 0.12f, h * 0.36f)
            lineTo(w * 0.32f, h * 0.36f)
            lineTo(w * 0.54f, h * 0.16f)
            lineTo(w * 0.54f, h * 0.84f)
            lineTo(w * 0.32f, h * 0.64f)
            lineTo(w * 0.12f, h * 0.64f)
            close()
        }
        drawPath(speakerPath, color, style = Fill)

        if (isEnabled) {
            // Sound wave 1
            val wave1 = Path().apply {
                moveTo(w * 0.68f, h * 0.34f)
                quadraticBezierTo(w * 0.78f, h * 0.50f, w * 0.68f, h * 0.66f)
            }
            drawPath(wave1, color, style = Stroke(width = 1.8f, cap = StrokeCap.Round))

            // Sound wave 2
            val wave2 = Path().apply {
                moveTo(w * 0.82f, h * 0.22f)
                quadraticBezierTo(w * 0.98f, h * 0.50f, w * 0.82f, h * 0.78f)
            }
            drawPath(wave2, color, style = Stroke(width = 1.8f, cap = StrokeCap.Round))
        } else {
            // Mute slash
            drawLine(
                color = color.copy(alpha = 0.85f),
                start = Offset(w * 0.65f, h * 0.32f),
                end = Offset(w * 0.92f, h * 0.68f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color.copy(alpha = 0.85f),
                start = Offset(w * 0.92f, h * 0.32f),
                end = Offset(w * 0.65f, h * 0.68f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun HapticsVectorIcon(
    isEnabled: Boolean,
    color: Color = Color.White,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Phone body
        val phoneRect = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = w * 0.32f,
                    top = h * 0.16f,
                    right = w * 0.68f,
                    bottom = h * 0.84f,
                    cornerRadius = CornerRadius(2.5f, 2.5f)
                )
            )
        }
        drawPath(phoneRect, color, style = Stroke(width = 1.8f))

        if (isEnabled) {
            // Left vibration ripple
            val leftWave = Path().apply {
                moveTo(w * 0.18f, h * 0.32f)
                quadraticBezierTo(w * 0.10f, h * 0.50f, w * 0.18f, h * 0.68f)
            }
            drawPath(leftWave, color, style = Stroke(width = 1.6f, cap = StrokeCap.Round))

            // Right vibration ripple
            val rightWave = Path().apply {
                moveTo(w * 0.82f, h * 0.32f)
                quadraticBezierTo(w * 0.90f, h * 0.50f, w * 0.82f, h * 0.68f)
            }
            drawPath(rightWave, color, style = Stroke(width = 1.6f, cap = StrokeCap.Round))
        } else {
            // Disabled diagonal slash
            drawLine(
                color = color.copy(alpha = 0.8f),
                start = Offset(w * 0.18f, h * 0.82f),
                end = Offset(w * 0.82f, h * 0.18f),
                strokeWidth = 1.8f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun ThemeVectorIcon(
    theme: BoardTheme,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val r = w * 0.44f
        val center = Offset(w / 2f, h / 2f)

        // Draw 3 color arcs representing the theme's palette
        drawArc(
            color = theme.slabSideColor,
            startAngle = 0f,
            sweepAngle = 120f,
            useCenter = true,
            size = Size(r * 2f, r * 2f),
            topLeft = Offset(center.x - r, center.y - r)
        )
        drawArc(
            color = theme.xColor,
            startAngle = 120f,
            sweepAngle = 120f,
            useCenter = true,
            size = Size(r * 2f, r * 2f),
            topLeft = Offset(center.x - r, center.y - r)
        )
        drawArc(
            color = theme.oColor,
            startAngle = 240f,
            sweepAngle = 120f,
            useCenter = true,
            size = Size(r * 2f, r * 2f),
            topLeft = Offset(center.x - r, center.y - r)
        )

        // Crisp border
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = r,
            center = center,
            style = Stroke(width = 1.2f)
        )
    }
}

@Composable
fun UndoVectorIcon(
    color: Color = Color.White,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val arrow = Path().apply {
            moveTo(w * 0.45f, h * 0.18f)
            lineTo(w * 0.18f, h * 0.45f)
            lineTo(w * 0.45f, h * 0.72f)
        }
        drawPath(arrow, color, style = Stroke(width = 2f, cap = StrokeCap.Round))

        val arc = Path().apply {
            moveTo(w * 0.22f, h * 0.45f)
            quadraticBezierTo(w * 0.78f, h * 0.38f, w * 0.78f, h * 0.82f)
        }
        drawPath(arc, color, style = Stroke(width = 2f, cap = StrokeCap.Round))
    }
}

@Composable
fun FlameStreakIcon(
    color: Color,
    size: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val flame = Path().apply {
            moveTo(w * 0.50f, h * 0.08f)
            quadraticBezierTo(w * 0.78f, h * 0.32f, w * 0.78f, h * 0.62f)
            quadraticBezierTo(w * 0.78f, h * 0.92f, w * 0.50f, h * 0.92f)
            quadraticBezierTo(w * 0.22f, h * 0.92f, w * 0.22f, h * 0.62f)
            quadraticBezierTo(w * 0.22f, h * 0.38f, w * 0.44f, h * 0.34f)
            quadraticBezierTo(w * 0.32f, h * 0.48f, w * 0.40f, h * 0.62f)
            quadraticBezierTo(w * 0.55f, h * 0.45f, w * 0.50f, h * 0.08f)
            close()
        }
        drawPath(flame, color, style = Fill)
    }
}

@Composable
fun AiVectorIcon(
    color: Color = Color.White,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Chip central square
        val chipRect = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = w * 0.28f,
                    top = h * 0.28f,
                    right = w * 0.72f,
                    bottom = h * 0.72f,
                    cornerRadius = CornerRadius(2f, 2f)
                )
            )
        }
        drawPath(chipRect, color, style = Stroke(width = 1.4f))

        // Central core dot
        drawCircle(
            color = color,
            radius = w * 0.08f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // Pins (top, bottom, left, right)
        val stroke = 1.2f
        // Top pins
        drawLine(color, Offset(w * 0.40f, h * 0.12f), Offset(w * 0.40f, h * 0.28f), strokeWidth = stroke)
        drawLine(color, Offset(w * 0.60f, h * 0.12f), Offset(w * 0.60f, h * 0.28f), strokeWidth = stroke)
        // Bottom pins
        drawLine(color, Offset(w * 0.40f, h * 0.72f), Offset(w * 0.40f, h * 0.88f), strokeWidth = stroke)
        drawLine(color, Offset(w * 0.60f, h * 0.72f), Offset(w * 0.60f, h * 0.88f), strokeWidth = stroke)
        // Left pins
        drawLine(color, Offset(w * 0.12f, h * 0.40f), Offset(w * 0.28f, h * 0.40f), strokeWidth = stroke)
        drawLine(color, Offset(w * 0.12f, h * 0.60f), Offset(w * 0.28f, h * 0.60f), strokeWidth = stroke)
        // Right pins
        drawLine(color, Offset(w * 0.72f, h * 0.40f), Offset(w * 0.88f, h * 0.40f), strokeWidth = stroke)
        drawLine(color, Offset(w * 0.72f, h * 0.60f), Offset(w * 0.88f, h * 0.60f), strokeWidth = stroke)
    }
}

@Composable
fun DevHudVectorIcon(
    isActive: Boolean,
    color: Color = Color.White,
    size: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Outer frame
        val frame = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = w * 0.15f,
                    top = h * 0.20f,
                    right = w * 0.85f,
                    bottom = h * 0.80f,
                    cornerRadius = CornerRadius(2f, 2f)
                )
            )
        }
        drawPath(frame, color, style = Stroke(width = 1.3f))

        // Pulse / waveform lines inside
        val wave = Path().apply {
            moveTo(w * 0.22f, h * 0.52f)
            lineTo(w * 0.38f, h * 0.52f)
            lineTo(w * 0.48f, h * 0.32f)
            lineTo(w * 0.56f, h * 0.68f)
            lineTo(w * 0.66f, h * 0.52f)
            lineTo(w * 0.78f, h * 0.52f)
        }
        drawPath(wave, color, style = Stroke(width = 1.4f, cap = StrokeCap.Round))
    }
}

