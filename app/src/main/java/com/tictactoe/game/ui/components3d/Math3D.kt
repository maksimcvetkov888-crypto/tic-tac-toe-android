package com.tictactoe.game.ui.components3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
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
     * Perspective projection from 3D space to 2D screen coordinates with responsive viewport scaling.
     */
    fun project(
        screenWidth: Float,
        screenHeight: Float,
        fov: Float = 500f,
        cameraDistance: Float = 600f,
        scaleMultiplier: Float = 1f
    ): ProjectedPoint {
        val viewZ = z + cameraDistance
        val safeZ = if (viewZ < 50f) 50f else viewZ
        val factor = (fov / safeZ) * scaleMultiplier
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
    val baseColor: Color,
    val metallic: Float = 0.4f,
    val shininess: Float = 32f
)

/**
 * Three.js & Blender style 3-Point Studio Lighting with Blinn-Phong Specular & Fresnel Rim.
 */
object Lighting3D {
    val keyLightDir: Vec3 = Vec3(0.52f, -0.78f, 0.85f).normalize()
    const val keyIntensity: Float = 0.62f

    val fillLightDir: Vec3 = Vec3(-0.65f, 0.45f, 0.4f).normalize()
    const val fillIntensity: Float = 0.22f

    val viewDir: Vec3 = Vec3(0f, 0f, 1f)

    const val ambientLight: Float = 0.28f
    const val rimIntensity: Float = 0.35f
    const val specularIntensity: Float = 0.55f

    fun computeShading(
        normal: Vec3,
        baseColor: Color,
        metallic: Float = 0.4f,
        shininess: Float = 32f
    ): Color {
        val n = normal.normalize()

        val diffKey = max(0f, n.dot(keyLightDir)) * keyIntensity
        val diffFill = max(0f, n.dot(fillLightDir)) * fillIntensity
        val totalDiffuse = ambientLight + diffKey + diffFill

        val halfVec = (keyLightDir + viewDir).normalize()
        val nDotH = max(0f, n.dot(halfVec))
        val specFactor = nDotH.pow(shininess) * specularIntensity

        val nDotV = max(0f, n.dot(viewDir))
        val fresnel = (1f - nDotV).pow(2.8f) * rimIntensity

        val r = (baseColor.red * totalDiffuse + specFactor * (1f - metallic * 0.5f) + fresnel * 0.85f).coerceIn(0f, 1f)
        val g = (baseColor.green * totalDiffuse + specFactor * (1f - metallic * 0.5f) + fresnel * 0.85f).coerceIn(0f, 1f)
        val b = (baseColor.blue * totalDiffuse + specFactor * (1f - metallic * 0.5f) + fresnel * 0.85f).coerceIn(0f, 1f)

        return Color(red = r, green = g, blue = b, alpha = baseColor.alpha)
    }
}

/**
 * 3D Particle for victory fireworks and kinetic impact bursts.
 */
data class Particle3D(
    var pos: Vec3,
    var velocity: Vec3,
    var rotation: Vec3,
    var rotVelocity: Vec3,
    val size: Float,
    val color: Color,
    var life: Float = 1f,
    val maxLife: Float = 1f
) {
    fun update(dt: Float) {
        pos += velocity * dt
        velocity = velocity.copy(y = velocity.y + 380f * dt)
        rotation += rotVelocity * dt
        life = (life - dt / maxLife).coerceAtLeast(0f)
    }
}

/**
 * Ambient 3D floating dust/star mote with gentle harmonic drift.
 */
data class Star3D(
    val initialPos: Vec3,
    val size: Float,
    val baseAlpha: Float,
    val phaseOffset: Float,
    val color: Color
) {
    fun currentPos(timeSec: Float): Vec3 {
        val dx = sin(timeSec * 0.4f + phaseOffset) * 16f
        val dy = cos(timeSec * 0.35f + phaseOffset * 1.3f) * 16f
        val dz = sin(timeSec * 0.25f + phaseOffset * 0.7f) * 12f
        return Vec3(initialPos.x + dx, initialPos.y + dy, initialPos.z + dz)
    }

    fun currentAlpha(timeSec: Float): Float {
        val pulse = 0.65f + 0.35f * sin(timeSec * 1.8f + phaseOffset)
        return (baseAlpha * pulse).coerceIn(0.1f, 0.95f)
    }
}

/**
 * Expanding circular 3D shockwave on the board platform upon token impact.
 */
data class Shockwave3D(
    val center: Vec3,
    val maxRadius: Float,
    val color: Color,
    var progress: Float = 0f
) {
    fun update(dt: Float): Boolean {
        progress += dt * 2.4f
        return progress < 1f
    }
}
