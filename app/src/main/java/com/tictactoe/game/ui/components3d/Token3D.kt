package com.tictactoe.game.ui.components3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.tictactoe.game.model.Player
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object Token3DGeometry {

    /**
     * Generates a 3D mesh for the 'X' token.
     * Consists of two intersecting 3D rectangular bars with beveled faces and metallic sheen.
     */
    fun createXMesh(scale: Float = 36f, depth: Float = 8.5f, color: Color): List<PolygonFace> {
        val faces = mutableListOf<PolygonFace>()
        val halfW = scale * 0.22f
        val halfL = scale * 0.86f
        val halfD = depth

        // Create 3D bar aligned with diagonal 1
        faces.addAll(createRotatedBox(halfW, halfL, halfD, PI.toFloat() / 4f, color))
        // Create 3D bar aligned with diagonal 2
        faces.addAll(createRotatedBox(halfW, halfL, halfD, -PI.toFloat() / 4f, color))

        return faces
    }

    /**
     * Generates a 3D mesh for the 'O' token.
     * Consists of an extruded 32-segment beveled cylindrical ring for buttery smooth curvature.
     */
    fun createOMesh(outerRadius: Float = 30f, innerRadius: Float = 17f, depth: Float = 8.5f, color: Color): List<PolygonFace> {
        val faces = mutableListOf<PolygonFace>()
        val segments = 32
        val halfD = depth

        val outerPoints = (0 until segments).map { i ->
            val angle = 2f * PI.toFloat() * i / segments
            Vec3(cos(angle) * outerRadius, sin(angle) * outerRadius, 0f)
        }
        val innerPoints = (0 until segments).map { i ->
            val angle = 2f * PI.toFloat() * i / segments
            Vec3(cos(angle) * innerRadius, sin(angle) * innerRadius, 0f)
        }

        for (i in 0 until segments) {
            val next = (i + 1) % segments

            // Top front face quad (+Z)
            faces.add(
                PolygonFace(
                    vertices = listOf(
                        outerPoints[i].copy(z = halfD),
                        outerPoints[next].copy(z = halfD),
                        innerPoints[next].copy(z = halfD),
                        innerPoints[i].copy(z = halfD)
                    ),
                    normal = Vec3(0f, 0f, 1f),
                    baseColor = color,
                    metallic = 0.60f,
                    shininess = 44f
                )
            )

            // Outer wall quad
            val midAngle = 2f * PI.toFloat() * (i + 0.5f) / segments
            val outerNormal = Vec3(cos(midAngle), sin(midAngle), 0f)
            faces.add(
                PolygonFace(
                    vertices = listOf(
                        outerPoints[i].copy(z = -halfD),
                        outerPoints[next].copy(z = -halfD),
                        outerPoints[next].copy(z = halfD),
                        outerPoints[i].copy(z = halfD)
                    ),
                    normal = outerNormal,
                    baseColor = color,
                    metallic = 0.60f,
                    shininess = 44f
                )
            )

            // Inner wall quad
            val innerNormal = Vec3(-cos(midAngle), -sin(midAngle), 0f)
            faces.add(
                PolygonFace(
                    vertices = listOf(
                        innerPoints[i].copy(z = halfD),
                        innerPoints[next].copy(z = halfD),
                        innerPoints[next].copy(z = -halfD),
                        innerPoints[i].copy(z = -halfD)
                    ),
                    normal = innerNormal,
                    baseColor = color,
                    metallic = 0.60f,
                    shininess = 44f
                )
            )
        }

        return faces
    }

    private fun createRotatedBox(halfW: Float, halfL: Float, halfD: Float, angleZ: Float, color: Color): List<PolygonFace> {
        val baseVertices = listOf(
            Vec3(-halfW, -halfL, halfD),  // 0
            Vec3(halfW, -halfL, halfD),   // 1
            Vec3(halfW, halfL, halfD),    // 2
            Vec3(-halfW, halfL, halfD),   // 3
            Vec3(-halfW, -halfL, -halfD), // 4
            Vec3(halfW, -halfL, -halfD),  // 5
            Vec3(halfW, halfL, -halfD),   // 6
            Vec3(-halfW, halfL, -halfD)   // 7
        ).map { it.rotateZ(angleZ) }

        return listOf(
            // Top (+Z)
            PolygonFace(listOf(baseVertices[0], baseVertices[1], baseVertices[2], baseVertices[3]), Vec3(0f, 0f, 1f).rotateZ(angleZ), color, metallic = 0.65f, shininess = 52f),
            // Bottom (-Z)
            PolygonFace(listOf(baseVertices[5], baseVertices[4], baseVertices[7], baseVertices[6]), Vec3(0f, 0f, -1f).rotateZ(angleZ), color, metallic = 0.65f, shininess = 52f),
            // Front (+Y)
            PolygonFace(listOf(baseVertices[3], baseVertices[2], baseVertices[6], baseVertices[7]), Vec3(0f, 1f, 0f).rotateZ(angleZ), color, metallic = 0.65f, shininess = 52f),
            // Back (-Y)
            PolygonFace(listOf(baseVertices[4], baseVertices[5], baseVertices[1], baseVertices[0]), Vec3(0f, -1f, 0f).rotateZ(angleZ), color, metallic = 0.65f, shininess = 52f),
            // Right (+X)
            PolygonFace(listOf(baseVertices[1], baseVertices[5], baseVertices[6], baseVertices[2]), Vec3(1f, 0f, 0f).rotateZ(angleZ), color, metallic = 0.65f, shininess = 52f),
            // Left (-X)
            PolygonFace(listOf(baseVertices[4], baseVertices[0], baseVertices[3], baseVertices[7]), Vec3(-1f, 0f, 0f).rotateZ(angleZ), color, metallic = 0.65f, shininess = 52f)
        )
    }
}

/**
 * 3D Renderer for tokens with Painter's depth sorting and dynamic Studio 3-Point PBR shading.
 */
fun DrawScope.render3DToken(
    player: Player,
    positionOffset: Vec3,
    rotX: Float,
    rotY: Float,
    rotZ: Float = 0f,
    scaleAnim: Float = 1f,
    primaryColor: Color,
    screenWidth: Float,
    screenHeight: Float,
    scaleMultiplier: Float = 1f
) {
    if (scaleAnim <= 0.01f) return

    val mesh = when (player) {
        Player.X -> Token3DGeometry.createXMesh(scale = 32f * scaleAnim, depth = 7f * scaleAnim, color = primaryColor)
        Player.O -> Token3DGeometry.createOMesh(outerRadius = 28f * scaleAnim, innerRadius = 16f * scaleAnim, depth = 7f * scaleAnim, color = primaryColor)
    }

    data class TransformedFace(
        val projectedPoints: List<ProjectedPoint>,
        val avgDepth: Float,
        val shadedColor: Color,
        val edgeColor: Color
    )

    val transformedFaces = mutableListOf<TransformedFace>()

    mesh.forEach { face ->
        val transformedNormal = face.normal.rotate(rxRad = rotX, ryRad = rotY, rzRad = rotZ).normalize()

        if (transformedNormal.z > -0.15f) {
            val transformedVertices = face.vertices.map { v ->
                val rotated = v.rotate(rxRad = rotX, ryRad = rotY, rzRad = rotZ)
                (rotated + positionOffset).project(screenWidth, screenHeight, scaleMultiplier = scaleMultiplier)
            }

            val avgDepth = transformedVertices.map { it.depth }.average().toFloat()
            val shadedColor = Lighting3D.computeShading(
                normal = transformedNormal,
                baseColor = face.baseColor,
                metallic = face.metallic,
                shininess = face.shininess
            )
            val edgeColor = shadedColor.copy(alpha = 0.65f)

            transformedFaces.add(TransformedFace(transformedVertices, avgDepth, shadedColor, edgeColor))
        }
    }

    // Painter's algorithm: sort farthest to nearest
    transformedFaces.sortByDescending { it.avgDepth }

    transformedFaces.forEach { face ->
        val path = Path().apply {
            val pts = face.projectedPoints
            if (pts.isNotEmpty()) {
                moveTo(pts[0].screenPos.x, pts[0].screenPos.y)
                for (i in 1 until pts.size) {
                    lineTo(pts[i].screenPos.x, pts[i].screenPos.y)
                }
                close()
            }
        }

        drawPath(path = path, color = face.shadedColor, style = Fill)
        drawPath(path = path, color = face.edgeColor, style = Stroke(width = 1f))
    }
}

/**
 * Renders contact shadow on the pedestal surface beneath the token.
 */
fun DrawScope.renderContactShadow(
    center: Vec3,
    radius: Float,
    rxRad: Float,
    ryRad: Float,
    screenWidth: Float,
    screenHeight: Float,
    scaleMultiplier: Float = 1f,
    alpha: Float = 0.45f
) {
    if (alpha <= 0.01f) return
    val segments = 12
    val points = (0 until segments).map { i ->
        val ang = 2f * PI.toFloat() * i / segments
        val p = Vec3(center.x + cos(ang) * radius, center.y + sin(ang) * radius, center.z)
        p.rotate(rxRad, ryRad).project(screenWidth, screenHeight, scaleMultiplier = scaleMultiplier).screenPos
    }

    val path = Path().apply {
        if (points.isNotEmpty()) {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
            close()
        }
    }
    drawPath(path, Color.Black.copy(alpha = alpha), style = Fill)
}

/**
 * Renders expanding circular 3D shockwave ripples across the board upon token landing.
 */
fun DrawScope.render3DShockwaves(
    shockwaves: List<Shockwave3D>,
    rxRad: Float,
    ryRad: Float,
    screenWidth: Float,
    screenHeight: Float,
    scaleMultiplier: Float = 1f
) {
    shockwaves.forEach { sw ->
        if (sw.progress >= 1f) return@forEach
        val currentRadius = sw.maxRadius * sw.progress
        val alpha = (1f - sw.progress).coerceIn(0f, 1f) * 0.75f
        val segments = 16

        val points = (0 until segments).map { i ->
            val ang = 2f * PI.toFloat() * i / segments
            val p = Vec3(sw.center.x + cos(ang) * currentRadius, sw.center.y + sin(ang) * currentRadius, sw.center.z)
            p.rotate(rxRad, ryRad).project(screenWidth, screenHeight, scaleMultiplier = scaleMultiplier).screenPos
        }

        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                close()
            }
        }
        drawPath(path, sw.color.copy(alpha = alpha), style = Stroke(width = (3f * (1f - sw.progress)).coerceAtLeast(1f)))
    }
}

/**
 * Renders a cloud of 3D polygonal particles for victory fireworks.
 */
fun DrawScope.render3DParticles(
    particles: List<Particle3D>,
    rxRad: Float,
    ryRad: Float,
    screenWidth: Float,
    screenHeight: Float,
    scaleMultiplier: Float = 1f
) {
    particles.forEach { p ->
        if (p.life <= 0.01f) return@forEach
        val center = p.pos.rotate(rxRad, ryRad).project(screenWidth, screenHeight, scaleMultiplier = scaleMultiplier)

        val s = p.size * (fovScale(center.depth)) * scaleMultiplier
        val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)

        val ang = p.rotation.z
        val c = cos(ang) * s
        val sn = sin(ang) * s
        val ox = center.screenPos.x
        val oy = center.screenPos.y

        val path = Path().apply {
            moveTo(ox - sn, oy - c)
            lineTo(ox + c, oy - sn)
            lineTo(ox + sn, oy + c)
            lineTo(ox - c, oy + sn)
            close()
        }

        drawPath(path, p.color.copy(alpha = alpha), style = Fill)
    }
}

private fun fovScale(depth: Float): Float {
    return (500f / depth.coerceAtLeast(50f)).coerceIn(0.4f, 2.5f)
}
