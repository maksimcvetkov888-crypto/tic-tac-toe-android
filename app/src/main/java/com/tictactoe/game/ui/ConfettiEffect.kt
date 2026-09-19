package com.tictactoe.game.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class Particle(
    val initialX: Float,
    val speedY: Float,
    val speedX: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float
)

@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    particleCount: Int = 45
) {
    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFF00E5FF),
            Color(0xFFFF5252),
            Color(0xFFFFD700),
            Color(0xFF00E676),
            Color(0xFF7C4DFF),
            Color(0xFFFF4081)
        )
        List(particleCount) {
            Particle(
                initialX = Random.nextFloat(),
                speedY = 0.5f + Random.nextFloat() * 0.7f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                size = 14f + Random.nextFloat() * 16f,
                color = colors[Random.nextInt(colors.size)],
                rotationSpeed = 180f + Random.nextFloat() * 360f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val p = progress.value

        particles.forEach { particle ->
            val x = (particle.initialX * width + particle.speedX * p * width) % width
            val y = (p * particle.speedY * height * 1.3f) - 50f
            val currentRotation = p * particle.rotationSpeed

            if (y in -50f..height + 50f) {
                rotate(degrees = currentRotation, pivot = Offset(x, y)) {
                    drawRect(
                        color = particle.color.copy(alpha = (1f - p * 0.4f).coerceIn(0f, 1f)),
                        topLeft = Offset(x - particle.size / 2, y - particle.size / 4),
                        size = Size(particle.size, particle.size / 2)
                    )
                }
            }
        }
    }
}
