package com.tictactoe.game

import com.tictactoe.game.ui.components3d.Vec3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class Math3DTest {

    @Test
    fun testVectorOperations() {
        val v1 = Vec3(1f, 2f, 3f)
        val v2 = Vec3(4f, 5f, 6f)

        val sum = v1 + v2
        assertEquals(5f, sum.x, 0.001f)
        assertEquals(7f, sum.y, 0.001f)
        assertEquals(9f, sum.z, 0.001f)

        val dot = v1.dot(v2)
        // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        assertEquals(32f, dot, 0.001f)

        val norm = Vec3(3f, 0f, 4f).normalize()
        assertEquals(0.6f, norm.x, 0.001f)
        assertEquals(0f, norm.y, 0.001f)
        assertEquals(0.8f, norm.z, 0.001f)
        assertEquals(1f, norm.length(), 0.001f)
    }

    @Test
    fun testVectorCrossProduct() {
        val xUnit = Vec3(1f, 0f, 0f)
        val yUnit = Vec3(0f, 1f, 0f)
        val zUnit = xUnit.cross(yUnit)

        assertEquals(0f, zUnit.x, 0.001f)
        assertEquals(0f, zUnit.y, 0.001f)
        assertEquals(1f, zUnit.z, 0.001f)
    }

    @Test
    fun testPerspectiveProjection() {
        val pointNear = Vec3(0f, 0f, 0f).project(screenWidth = 800f, screenHeight = 600f)
        val pointFar = Vec3(0f, 0f, 400f).project(screenWidth = 800f, screenHeight = 600f)

        assertEquals(400f, pointNear.screenPos.x, 0.001f)
        assertEquals(300f, pointNear.screenPos.y, 0.001f)

        // Point farther away has smaller perspective scale
        assertTrue(pointFar.scale < pointNear.scale)
        assertTrue(pointFar.depth > pointNear.depth)
    }

    @Test
    fun testRotationZ() {
        val v = Vec3(1f, 0f, 0f)
        val rotated = v.rotateZ(PI.toFloat() / 2f) // 90 deg rotation
        assertEquals(0f, rotated.x, 0.001f)
        assertEquals(1f, rotated.y, 0.001f)
        assertEquals(0f, rotated.z, 0.001f)
    }
}
