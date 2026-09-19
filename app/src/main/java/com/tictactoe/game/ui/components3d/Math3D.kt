package com.tictactoe.game.ui.components3d

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(v: Vec3) = Vec3(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vec3) = Vec3(x - v.x, y - v.y, z - v.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)
    operator fun div(s: Float) = Vec3(x / s, y / s, z / s)

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalize(): Vec3 {
        val len = length()
        return if (len > 0.0001f) this / len else Vec3(0f, 0f, 1f)
    }

    fun dot(v: Vec3): Float = x * v.x + y * v.y + z * v.z

    fun cross(v: Vec3): Vec3 = Vec3(
        y * v.z - z * v.y,
        z * v.x - x * v.z,
        x * v.y - y * v.x
    )

    fun rotateX(angleRad: Float): Vec3 {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vec3(
            x,
            y * cosA - z * sinA,
            y * sinA + z * cosA
        )
    }

    fun rotateY(angleRad: Float): Vec3 {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vec3(
            x * cosA + z * sinA,
            y,
            -x * sinA + z * cosA
        )
    }

    fun rotateZ(angleRad: Float): Vec3 {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        return Vec3(
            x * cosA - y * sinA,
            x * sinA + y * cosA,
            z
        )
    }

    fun rotate(rxRad: Float, ryRad: Float, rzRad: Float = 0f): Vec3 {
        return this.rotateZ(rzRad).rotateY(ryRad).rotateX(rxRad)
    }

    /**
     * Perspective projection from 3D space to 2D screen coordinates.
     */
    fun project(
        screenWidth: Float,
        screenHeight: Float,
        fov: Float = 500f,
        cameraDistance: Float = 600f
    ): ProjectedPoint {
        val viewZ = z + cameraDistance
        val safeZ = if (viewZ < 50f) 50f else viewZ
        val factor = fov / safeZ
        val screenX = screenWidth / 2f + x * factor
        val screenY = screenHeight / 2f + y * factor
        return ProjectedPoint(
            screenPos = Offset(screenX, screenY),
            depth = viewZ,
            scale = factor
        )
    }
}

data class ProjectedPoint(
    val screenPos: Offset,
    val depth: Float,
    val scale: Float
)

data class PolygonFace(
    val vertices: List<Vec3>,
    val normal: Vec3,
    val baseColor: androidx.compose.ui.graphics.Color
)

object Lighting3D {
    val lightDir: Vec3 = Vec3(0.45f, -0.75f, 0.9f).normalize()
    const val ambientLight: Float = 0.38f
    const val diffuseStrength: Float = 0.62f

    fun computeShading(normal: Vec3, baseColor: androidx.compose.ui.graphics.Color): androidx.compose.ui.graphics.Color {
        val diffuse = max(0f, normal.dot(lightDir)) * diffuseStrength
        val brightness = (ambientLight + diffuse).coerceIn(0.2f, 1.2f)

        return androidx.compose.ui.graphics.Color(
            red = (baseColor.red * brightness).coerceIn(0f, 1f),
            green = (baseColor.green * brightness).coerceIn(0f, 1f),
            blue = (baseColor.blue * brightness).coerceIn(0f, 1f),
            alpha = baseColor.alpha
        )
    }
}
