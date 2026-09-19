package com.tictactoe.game.ui.components3d

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.tictactoe.game.model.GameStatus
import com.tictactoe.game.model.GameUiState
import com.tictactoe.game.model.Player
import com.tictactoe.game.ui.theme.AccentVictory
import com.tictactoe.game.ui.theme.BorderFocus
import com.tictactoe.game.ui.theme.BorderSubtle
import com.tictactoe.game.ui.theme.CellBackground
import com.tictactoe.game.ui.theme.PlayerOFire
import com.tictactoe.game.ui.theme.PlayerXAzure
import com.tictactoe.game.ui.theme.SurfaceCard
import com.tictactoe.game.ui.theme.SurfaceCardElevated
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun Interactive3DBoard(
    state: GameUiState,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 3D Orbital Rotation Angles (degrees)
    var dragRotX by remember { mutableFloatStateOf(24f) }
    var dragRotY by remember { mutableFloatStateOf(-16f) }

    val smoothRotX by animateFloatAsState(
        targetValue = dragRotX,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rotX"
    )
    val smoothRotY by animateFloatAsState(
        targetValue = dragRotY,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rotY"
    )

    // Subtle idle floating breathing motion
    val infiniteTransition = rememberInfiniteTransition(label = "idleBreathing")
    val idlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idlePhase"
    )
    val idleSway: Float = sin(idlePhase) * 1.8f

    // Animated scale and elevation for each cell
    val cellScales = remember { Array(9) { Animatable(0f) } }

    state.board.forEachIndexed { index, player ->
        LaunchedEffect(player) {
            if (player != null) {
                cellScales[index].snapTo(0f)
                cellScales[index].animateTo(
                    targetValue = 1f,
                    animationSpec = spring(stiffness = 380f, dampingRatio = Spring.DampingRatioMediumBouncy)
                )
            } else {
                cellScales[index].snapTo(0f)
            }
        }
    }

    val winningLine = (state.status as? GameStatus.Won)?.line

    // Cell centers in 3D local board coordinates
    val spacing = 78f
    val cellPositions = remember {
        (0 until 9).map { i ->
            val row = i / 3
            val col = i % 3
            Vec3(
                x = (col - 1) * spacing,
                y = (row - 1) * spacing,
                z = 10f
            )
        }
    }

    var projectedCellPolygons by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Map drag gestures to 3D orbital tilt
                    dragRotX = (dragRotX - dragAmount.y * 0.28f).coerceIn(-40f, 45f)
                    dragRotY = (dragRotY + dragAmount.x * 0.28f).coerceIn(-45f, 45f)
                }
            }
            .pointerInput(state.board, state.status, projectedCellPolygons) {
                detectTapGestures { tapOffset ->
                    if (state.isFinished) return@detectTapGestures

                    // 3D Hit testing: test tap point inside projected cell polygons
                    for (index in projectedCellPolygons.indices) {
                        val poly = projectedCellPolygons[index]
                        if (isPointInsidePolygon(tapOffset, poly)) {
                            onCellClick(index)
                            break
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val rxRad: Float = (smoothRotX + idleSway) * (PI.toFloat() / 180f)
            val ryRad: Float = smoothRotY * (PI.toFloat() / 180f)

            // 1. Draw 3D Base Monolith Slab
            draw3DBaseSlab(rxRad, ryRad, w, h)

            // 2. Compute and draw 3D Cell Pedestals
            val currentPolys = mutableListOf<List<Offset>>()
            for (i in 0 until 9) {
                val pos = cellPositions[i]
                val isWinning = winningLine?.contains(i) == true
                val zElevation = if (isWinning) 20f else 10f
                val cellPos = pos.copy(z = zElevation)

                val poly = draw3DCellPedestal(
                    center = cellPos,
                    size = 64f,
                    depth = 10f,
                    rxRad = rxRad,
                    ryRad = ryRad,
                    isWinning = isWinning,
                    screenWidth = w,
                    screenHeight = h
                )
                currentPolys.add(poly)
            }
            projectedCellPolygons = currentPolys

            // 3. Render 3D Tokens (X and O)
            for (i in 0 until 9) {
                val player = state.board[i]
                if (player != null) {
                    val pos = cellPositions[i]
                    val isWinning = winningLine?.contains(i) == true
                    val tokenZ = (if (isWinning) 26f else 16f)
                    val tokenCenter = pos.copy(z = tokenZ).rotate(rxRad, ryRad)

                    val color = when {
                        isWinning -> AccentVictory
                        player == Player.X -> PlayerXAzure
                        else -> PlayerOFire
                    }

                    render3DToken(
                        player = player,
                        positionOffset = tokenCenter,
                        rotX = rxRad,
                        rotY = ryRad,
                        rotZ = 0f,
                        scaleAnim = cellScales[i].value,
                        primaryColor = color,
                        screenWidth = w,
                        screenHeight = h
                    )
                }
            }

            // 4. Render 3D Volumetric Victory Laser Beam
            if (winningLine != null && winningLine.size == 3) {
                draw3DLaserBeam(
                    startCell = cellPositions[winningLine.first()].copy(z = 28f),
                    endCell = cellPositions[winningLine.last()].copy(z = 28f),
                    rxRad = rxRad,
                    ryRad = ryRad,
                    screenWidth = w,
                    screenHeight = h
                )
            }
        }
    }
}

/**
 * Draws the 3D obsidian base monolith with beveled faces.
 */
private fun DrawScope.draw3DBaseSlab(rx: Float, ry: Float, w: Float, h: Float) {
    val halfW = 126f
    val halfH = 126f
    val depth = 16f

    // Soft drop shadow on ground
    val shadowPoints = listOf(
        Vec3(-halfW, -halfH, -depth * 2f),
        Vec3(halfW, -halfH, -depth * 2f),
        Vec3(halfW, halfH, -depth * 2f),
        Vec3(-halfW, halfH, -depth * 2f)
    ).map { it.rotate(rx, ry).project(w, h) }

    drawPolygon(shadowPoints.map { it.screenPos }, Color.Black.copy(alpha = 0.55f))

    // 3D Slab faces
    val v = listOf(
        Vec3(-halfW, -halfH, 0f), // 0
        Vec3(halfW, -halfH, 0f),  // 1
        Vec3(halfW, halfH, 0f),   // 2
        Vec3(-halfW, halfH, 0f),  // 3
        Vec3(-halfW, -halfH, -depth), // 4
        Vec3(halfW, -halfH, -depth),  // 5
        Vec3(halfW, halfH, -depth),   // 6
        Vec3(-halfW, halfH, -depth)   // 7
    )

    val faces = listOf(
        PolygonFace(listOf(v[0], v[1], v[2], v[3]), Vec3(0f, 0f, 1f), SurfaceCard),
        PolygonFace(listOf(v[3], v[2], v[6], v[7]), Vec3(0f, 1f, 0f), SurfaceCardElevated),
        PolygonFace(listOf(v[1], v[5], v[6], v[2]), Vec3(1f, 0f, 0f), SurfaceCardElevated),
        PolygonFace(listOf(v[4], v[0], v[3], v[7]), Vec3(-1f, 0f, 0f), SurfaceCardElevated),
        PolygonFace(listOf(v[4], v[5], v[1], v[0]), Vec3(0f, -1f, 0f), SurfaceCardElevated)
    )

    renderFaces(faces, rx, ry, w, h)
}

/**
 * Draws a 3D elevated cell pedestal and returns the 2D quadrilateral screen coordinates of its top face.
 */
private fun DrawScope.draw3DCellPedestal(
    center: Vec3,
    size: Float,
    depth: Float,
    rxRad: Float,
    ryRad: Float,
    isWinning: Boolean,
    screenWidth: Float,
    screenHeight: Float
): List<Offset> {
    val halfS = size / 2f
    val base = center.z - depth
    val top = center.z

    val localV = listOf(
        Vec3(center.x - halfS, center.y - halfS, top), // 0
        Vec3(center.x + halfS, center.y - halfS, top), // 1
        Vec3(center.x + halfS, center.y + halfS, top), // 2
        Vec3(center.x - halfS, center.y + halfS, top), // 3
        Vec3(center.x - halfS, center.y - halfS, base), // 4
        Vec3(center.x + halfS, center.y - halfS, base), // 5
        Vec3(center.x + halfS, center.y + halfS, base), // 6
        Vec3(center.x - halfS, center.y + halfS, base)  // 7
    )

    val topColor = if (isWinning) AccentVictory.copy(alpha = 0.28f) else CellBackground
    val sideColor = if (isWinning) AccentVictory.copy(alpha = 0.45f) else SurfaceCardElevated

    val faces = listOf(
        PolygonFace(listOf(localV[0], localV[1], localV[2], localV[3]), Vec3(0f, 0f, 1f), topColor),
        PolygonFace(listOf(localV[3], localV[2], localV[6], localV[7]), Vec3(0f, 1f, 0f), sideColor),
        PolygonFace(listOf(localV[1], localV[5], localV[6], localV[2]), Vec3(1f, 0f, 0f), sideColor),
        PolygonFace(listOf(localV[4], localV[0], localV[3], localV[7]), Vec3(-1f, 0f, 0f), sideColor),
        PolygonFace(listOf(localV[4], localV[5], localV[1], localV[0]), Vec3(0f, -1f, 0f), sideColor)
    )

    renderFaces(faces, rxRad, ryRad, screenWidth, screenHeight)

    // Projected top face polygon for touch hit-testing
    return listOf(localV[0], localV[1], localV[2], localV[3]).map {
        it.rotate(rxRad, ryRad).project(screenWidth, screenHeight).screenPos
    }
}

/**
 * Draws the 3D glowing victory laser beam hovering above winning cells.
 */
private fun DrawScope.draw3DLaserBeam(
    startCell: Vec3,
    endCell: Vec3,
    rxRad: Float,
    ryRad: Float,
    screenWidth: Float,
    screenHeight: Float
) {
    val p1 = startCell.rotate(rxRad, ryRad).project(screenWidth, screenHeight).screenPos
    val p2 = endCell.rotate(rxRad, ryRad).project(screenWidth, screenHeight).screenPos

    // Layer 1: Volumetric neon aura
    drawLine(
        color = AccentVictory.copy(alpha = 0.25f),
        start = p1,
        end = p2,
        strokeWidth = 24f,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    // Layer 2: Radiant laser glow
    drawLine(
        color = AccentVictory.copy(alpha = 0.65f),
        start = p1,
        end = p2,
        strokeWidth = 10f,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    // Layer 3: High-energy white core
    drawLine(
        color = Color.White,
        start = p1,
        end = p2,
        strokeWidth = 3.5f,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )
}

private fun DrawScope.renderFaces(
    faces: List<PolygonFace>,
    rx: Float,
    ry: Float,
    w: Float,
    h: Float
) {
    data class ProjectedFace(
        val points: List<Offset>,
        val avgDepth: Float,
        val shadedColor: Color
    )

    val projectedList = mutableListOf<ProjectedFace>()

    faces.forEach { face ->
        val transformedNormal = face.normal.rotate(rx, ry).normalize()
        if (transformedNormal.z > -0.2f) {
            val projectedPoints = face.vertices.map { it.rotate(rx, ry).project(w, h) }
            val avgDepth = projectedPoints.map { it.depth }.average().toFloat()
            val shadedColor = Lighting3D.computeShading(transformedNormal, face.baseColor)
            projectedList.add(ProjectedFace(projectedPoints.map { it.screenPos }, avgDepth, shadedColor))
        }
    }

    projectedList.sortByDescending { it.avgDepth }

    projectedList.forEach { face ->
        drawPolygon(face.points, face.shadedColor)
        drawPolygonOutline(face.points, BorderSubtle)
    }
}

private fun DrawScope.drawPolygon(points: List<Offset>, color: Color) {
    if (points.isEmpty()) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
        close()
    }
    drawPath(path, color, style = Fill)
}

private fun DrawScope.drawPolygonOutline(points: List<Offset>, color: Color) {
    if (points.isEmpty()) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
        close()
    }
    drawPath(path, color, style = Stroke(width = 1f))
}

/**
 * Standard 2D Ray-Casting algorithm to test if point is inside a polygon.
 */
private fun isPointInsidePolygon(pt: Offset, polygon: List<Offset>): Boolean {
    if (polygon.size < 3) return false
    var inside = false
    var j = polygon.size - 1
    for (i in polygon.indices) {
        val xi = polygon[i].x
        val yi = polygon[i].y
        val xj = polygon[j].x
        val yj = polygon[j].y

        val intersect = ((yi > pt.y) != (yj > pt.y)) &&
                (pt.x < (xj - xi) * (pt.y - yi) / (yj - yi + 0.00001f) + xi)
        if (intersect) inside = !inside
        j = i
    }
    return inside
}
