package com.tictactoe.game.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BorderBeamContainer(
    modifier: Modifier = Modifier,
    colorFrom: Color = Color(0xFF38BDF8),
    colorTo: Color = Color.Transparent,
    strokeWidth: Dp = 1.5.dp,
    durationMillis: Int = 3000,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "borderBeamTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderBeamAngle"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .drawWithContent {
                drawContent()

                val strokePx = strokeWidth.toPx()
                val radiusPx = shape.topStart.toPx(size, this)
                val halfStroke = strokePx / 2f
                val arcSize = Size(size.width - strokePx, size.height - strokePx)

                rotate(degrees = angle, pivot = center) {
                    val brush = Brush.sweepGradient(
                        0.0f to colorFrom,
                        0.25f to colorTo,
                        0.75f to Color.Transparent,
                        1.0f to colorFrom,
                        center = center
                    )
                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(halfStroke, halfStroke),
                        size = arcSize,
                        cornerRadius = CornerRadius(radiusPx),
                        style = Stroke(width = strokePx)
                    )
                }
            },
        content = content
    )
}
