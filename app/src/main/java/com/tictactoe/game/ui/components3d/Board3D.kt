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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.tictactoe.game.model.BoardTheme
import com.tictactoe.game.model.CameraPreset
import com.tictactoe.game.model.GameStatus
import com.tictactoe.game.model.GameUiState
import com.tictactoe.game.model.Player
import com.tictactoe.game.ui.theme.BorderSubtle
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun Interactive3DBoard(
    state: GameUiState,
    onCellClick: (Int) -> Unit,
    onImpact: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 3D Orbital Rotation Angles (degrees)
    var dragRotX by remember { mutableFloatStateOf(state.cameraPreset.rotX) }
    var dragRotY by remember { mutableFloatStateOf(state.cameraPreset.rotY) }

    // Respond to preset changes
    LaunchedEffect(state.cameraPreset) {
        dragRotX = state.cameraPreset.rotX
        dragRotY = state.cameraPreset.rotY
    }

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
    val idleSway: Float = sin(idlePhase) * 1.6f

    // Kinetic drop elevation and scale animations per cell
    val cellScales = remember { Array(9) { Animatable(0f) } }
    val cellDropZ = remember { Array(9) { Animatable(0f) } }
    val cameraImpactShake = remember { Animatable(0f) }

    state.board.forEachIndexed { index, player ->
        LaunchedEffect(player) {
            if (player != null) {
                cellDropZ[index].snapTo(95f)
                cellScales[index].snapTo(0.35f)

                launch {
                    cellScales[index].animateTo(
                        targetValue = 1f,
                        animationSpec = spring(stiffness = 420f, dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                }

                cellDropZ[index].animateTo(
                    targetValue = 0f,
                    animationSpec = spring(stiffness = 320f, dampingRatio = Spring.DampingRatioMediumBouncy)
                )

                // Trigger physical impact thud and camera shake on landing
                onImpact()
                cameraImpactShake.snapTo(2.4f)
                cameraImpactShake.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(stiffness = 480f, dampingRatio = Spring.DampingRatioNoBouncy)
                )
            } else {
                cellDropZ[index].snapTo(0f)
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

    // 36 Ambient Starfield Dust Motes
    val ambientStars = remember {
        val rnd = Random(777)
        (0 until 36).map {
            val x = (rnd.nextFloat() - 0.5f) * 440f
            val y = (rnd.nextFloat() - 0.5f) * 440f
            val z = (rnd.nextFloat() - 0.5f) * 260f - 40f
            Star3D(
                initialPos = Vec3(x, y, z),
                size = rnd.nextFloat() * 2.8f + 1.2f,
                baseAlpha = rnd.nextFloat() * 0.45f + 0.25f,
                phaseOffset = rnd.nextFloat() * 6.28f,
                color = Color(0xFFE2E8F0)
            )
        }
    }

    // 3D Victory Particle Pool
    var particles by remember { mutableStateOf<List<Particle3D>>(emptyList()) }

    LaunchedEffect(winningLine) {
        if (winningLine != null && winningLine.size == 3) {
            val random = Random(1337)
            val burstList = mutableListOf<Particle3D>()
            winningLine.forEach { cellIdx ->
                val center = cellPositions[cellIdx]
                repeat(16) {
                    val vx = (random.nextFloat() - 0.5f) * 200f
                    val vy = -(random.nextFloat() * 190f + 80f) // Burst upward
                    val vz = (random.nextFloat() - 0.5f) * 150f
                    val color = if (random.nextBoolean()) state.theme.victoryColor else Color.White
                    burstList.add(
                        Particle3D(
                            pos = center.copy(z = 32f),
                            velocity = Vec3(vx, vy, vz),
                            rotation = Vec3(random.nextFloat() * 6f, random.nextFloat() * 6f, random.nextFloat() * 6f),
                            rotVelocity = Vec3(random.nextFloat() * 8f, random.nextFloat() * 8f, random.nextFloat() * 8f),
                            size = random.nextFloat() * 3.8f + 2.2f,
                            color = color,
                            life = 1f,
                            maxLife = random.nextFloat() * 1.2f + 1.2f
                        )
                    )
                }
            }
            particles = burstList

            var lastTime = System.nanoTime()
            while (isActive && particles.any { it.life > 0.01f }) {
                withFrameNanos { now ->
                    val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
                    lastTime = now
                    particles.forEach { it.update(dt) }
                }
            }
        } else {
            particles = emptyList()
        }
    }

    var projectedCellPolygons by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(6.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Map drag gestures to 3D orbital tilt
                    dragRotX = (dragRotX - dragAmount.y * 0.28f).coerceIn(-40f, 45f)
                    dragRotY = (dragRotY + dragAmount.x * 0.28f).coerceIn(-45f, 45f)
                }
            }
            .pointerInput(state.board, state.status, projectedCellPolygons) {
                detectTapGestures(
                    onDoubleTap = {
                        // Double tap to reset camera to standard 3D perspective
                        dragRotX = 24f
                        dragRotY = -16f
                    },
                    onTap = { tapOffset ->
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
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val shake = cameraImpactShake.value
            val rxRad: Float = (smoothRotX + idleSway + shake) * (PI.toFloat() / 180f)
            val ryRad: Float = (smoothRotY + shake * 0.5f) * (PI.toFloat() / 180f)

            // 0. Ambient 3D Starfield Motes
            ambientStars.forEach { star ->
                val p = star.currentPos(idlePhase).rotate(rxRad * 0.6f, ryRad * 0.6f).project(w, h)
                val alpha = star.currentAlpha(idlePhase)
                drawCircle(
                    color = star.color.copy(alpha = alpha),
                    radius = star.size,
                    center = p.screenPos
                )
            }

            // 1. Perspective 3D Grid Floor Horizon
            draw3DGridFloor(rxRad, ryRad, w, h, state.theme.gridLineColor)

            // 2. Draw 3D Base Monolith Slab with Theme Colors
            draw3DBaseSlab(rxRad, ryRad, w, h, state.theme)

            // 3. Compute and draw 3D Cell Pedestals
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
                    theme = state.theme,
                    screenWidth = w,
                    screenHeight = h
                )
                currentPolys.add(poly)

                // Contact Shadow for active tokens
                val player = state.board[i]
                if (player != null && cellScales[i].value > 0.05f) {
                    val dropRatio = (1f - (cellDropZ[i].value / 95f)).coerceIn(0f, 1f)
                    renderContactShadow(
                        center = cellPos.copy(z = zElevation + 0.5f),
                        radius = 21f * cellScales[i].value,
                        rxRad = rxRad,
                        ryRad = ryRad,
                        screenWidth = w,
                        screenHeight = h,
                        alpha = 0.45f * dropRatio
                    )
                }
            }
            projectedCellPolygons = currentPolys

            // 4. Render 3D Tokens (X and O) with kinetic drop & bounce
            for (i in 0 until 9) {
                val player = state.board[i]
                if (player != null) {
                    val pos = cellPositions[i]
                    val isWinning = winningLine?.contains(i) == true
                    val baseZ = if (isWinning) 26f else 16f
                    val tokenZ = baseZ + cellDropZ[i].value
                    val tokenCenter = pos.copy(z = tokenZ).rotate(rxRad, ryRad)

                    val color = when {
                        isWinning -> state.theme.victoryColor
                        player == Player.X -> state.theme.xColor
                        else -> state.theme.oColor
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

            // 5. Render 3D Volumetric Victory Laser Beam
            if (winningLine != null && winningLine.size == 3) {
                draw3DLaserBeam(
                    startCell = cellPositions[winningLine.first()].copy(z = 28f),
                    endCell = cellPositions[winningLine.last()].copy(z = 28f),
                    rxRad = rxRad,
                    ryRad = ryRad,
                    victoryColor = state.theme.victoryColor,
                    screenWidth = w,
                    screenHeight = h
                )
            }

            // 6. Render 3D Victory Particles Cloud
            if (particles.isNotEmpty()) {
                render3DParticles(
                    particles = particles,
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
 * Draws the perspective 3D coordinate grid floor below the board.
 */
private fun DrawScope.draw3DGridFloor(rx: Float, ry: Float, w: Float, h: Float, gridColor: Color) {
    val floorZ = -45f
    val range = 180f
    val step = 45f

    var x = -range
    while (x <= range) {
        val p1 = Vec3(x, -range, floorZ).rotate(rx, ry).project(w, h).screenPos
        val p2 = Vec3(x, range, floorZ).rotate(rx, ry).project(w, h).screenPos
        drawLine(gridColor, p1, p2, strokeWidth = 1f)
        x += step
    }

    var y = -range
    while (y <= range) {
        val p1 = Vec3(-range, y, floorZ).rotate(rx, ry).project(w, h).screenPos
        val p2 = Vec3(range, y, floorZ).rotate(rx, ry).project(w, h).screenPos
        drawLine(gridColor, p1, p2, strokeWidth = 1f)
        y += step
    }
}

/**
 * Draws the 3D monolith base slab with theme colors.
 */
private fun DrawScope.draw3DBaseSlab(rx: Float, ry: Float, w: Float, h: Float, theme: BoardTheme) {
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
        PolygonFace(listOf(v[0], v[1], v[2], v[3]), Vec3(0f, 0f, 1f), theme.slabColor, metallic = 0.35f, shininess = 28f),
        PolygonFace(listOf(v[3], v[2], v[6], v[7]), Vec3(0f, 1f, 0f), theme.slabSideColor, metallic = 0.35f, shininess = 28f),
        PolygonFace(listOf(v[1], v[5], v[6], v[2]), Vec3(1f, 0f, 0f), theme.slabSideColor, metallic = 0.35f, shininess = 28f),
        PolygonFace(listOf(v[4], v[0], v[3], v[7]), Vec3(-1f, 0f, 0f), theme.slabSideColor, metallic = 0.35f, shininess = 28f),
        PolygonFace(listOf(v[4], v[5], v[1], v[0]), Vec3(0f, -1f, 0f), theme.slabSideColor, metallic = 0.35f, shininess = 28f)
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
    theme: BoardTheme,
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

    val topColor = if (isWinning) theme.victoryColor.copy(alpha = 0.32f) else theme.cellColor
    val sideColor = if (isWinning) theme.victoryColor.copy(alpha = 0.55f) else theme.slabSideColor

    val faces = listOf(
        PolygonFace(listOf(localV[0], localV[1], localV[2], localV[3]), Vec3(0f, 0f, 1f), topColor, metallic = 0.4f, shininess = 32f),
        PolygonFace(listOf(localV[3], localV[2], localV[6], localV[7]), Vec3(0f, 1f, 0f), sideColor, metallic = 0.4f, shininess = 32f),
        PolygonFace(listOf(localV[1], localV[5], localV[6], localV[2]), Vec3(1f, 0f, 0f), sideColor, metallic = 0.4f, shininess = 32f),
        PolygonFace(listOf(localV[4], localV[0], localV[3], localV[7]), Vec3(-1f, 0f, 0f), sideColor, metallic = 0.4f, shininess = 32f),
        PolygonFace(listOf(localV[4], localV[5], localV[1], localV[0]), Vec3(0f, -1f, 0f), sideColor, metallic = 0.4f, shininess = 32f)
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
    victoryColor: Color,
    screenWidth: Float,
    screenHeight: Float
) {
    val p1 = startCell.rotate(rxRad, ryRad).project(screenWidth, screenHeight).screenPos
    val p2 = endCell.rotate(rxRad, ryRad).project(screenWidth, screenHeight).screenPos

    // Layer 1: Volumetric neon aura
    drawLine(
        color = victoryColor.copy(alpha = 0.28f),
        start = p1,
        end = p2,
        strokeWidth = 26f,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    // Layer 2: Radiant laser glow
    drawLine(
        color = victoryColor.copy(alpha = 0.72f),
        start = p1,
        end = p2,
        strokeWidth = 11f,
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
            val shadedColor = Lighting3D.computeShading(
                normal = transformedNormal,
                baseColor = face.baseColor,
                metallic = face.metallic,
                shininess = face.shininess
            )
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
