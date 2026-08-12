package com.example

import com.example.ui.components.engine.AttachedObject
import com.example.ui.components.engine.LevelGenerator
import com.example.ui.components.engine.RopeMaterial
import com.example.ui.components.engine.RopePhysicsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FidgetEngineTest {

    @Test
    fun testHandcraftedFirst15LevelsSequence() {
        val level1 = LevelGenerator.getLevel(1)
        assertEquals(1, level1.id)
        assertEquals(RopeMaterial.THREAD, level1.ropeMaterial)
        assertEquals(AttachedObject.BUTTON, level1.objectType)
        assertEquals("tiny/light", level1.feelDescription)

        val level2 = LevelGenerator.getLevel(2)
        assertEquals(2, level2.id)
        assertEquals(RopeMaterial.THREAD, level2.ropeMaterial)
        assertEquals(AttachedObject.FEATHER, level2.objectType)

        val level5 = LevelGenerator.getLevel(5)
        assertEquals(5, level5.id)
        assertEquals(RopeMaterial.SHOELACE, level5.ropeMaterial)
        assertEquals(AttachedObject.SHOE, level5.objectType)

        val level11 = LevelGenerator.getLevel(11)
        assertEquals(11, level11.id)
        assertEquals(RopeMaterial.BRAIDED_ROPE, level11.ropeMaterial)
        assertEquals(AttachedObject.ANCHOR, level11.objectType)

        val level15 = LevelGenerator.getLevel(15)
        assertEquals(15, level15.id)
        assertEquals(RopeMaterial.WOOL, level15.ropeMaterial)
        assertEquals(AttachedObject.SWEATER, level15.objectType)
    }

    @Test
    fun testProceduralLevelGenerator() {
        val level16 = LevelGenerator.getLevel(16)
        assertEquals(16, level16.id)
        assertNotNull(level16.ropeMaterial)
        assertNotNull(level16.objectType)
        assertTrue(level16.lengthPx >= 1000f)

        val level100 = LevelGenerator.getLevel(100)
        assertEquals(100, level100.id)
        assertNotNull(level100.ropeMaterial)
        assertNotNull(level100.objectType)
    }

    @Test
    fun testPhysicsEngineInitializationAndUpdate() {
        val engine = RopePhysicsEngine()
        val level1 = LevelGenerator.getLevel(1)

        engine.loadLevel(level1, startX = 40f, startY = 100f, availableWidth = 800f)

        assertEquals(RopePhysicsEngine.DEFAULT_PARTICLES, engine.numParticles)
        assertEquals(40f, engine.posX[0], 0.01f)
        assertEquals(100f, engine.posY[0], 0.01f)
        assertFalse(engine.isLevelCompleted)

        // Perform 60 physics steps (1 second)
        for (i in 0 until 60) {
            engine.update(0.016f)
        }

        // Verify particle coordinates are clean numbers (no NaN / Infinity)
        for (i in 0 until engine.numParticles) {
            assertFalse(engine.posX[i].isNaN())
            assertFalse(engine.posY[i].isNaN())
            assertFalse(engine.posX[i].isInfinite())
            assertFalse(engine.posY[i].isInfinite())
        }
    }

    @Test
    fun testPhysicsTouchGrabAndRelease() {
        val engine = RopePhysicsEngine()
        val level1 = LevelGenerator.getLevel(1)
        engine.loadLevel(level1, startX = 40f, startY = 100f, availableWidth = 800f)

        val grabbedIdx = engine.grabNearestNode(40f, 100f)
        assertEquals(0, grabbedIdx)

        engine.updateTouchTarget(100f, 100f, deltaX = 60f)
        assertEquals(60f, engine.accumulatedPullDistance, 0.1f)

        engine.releaseTouch(velocityX = 500f, velocityY = 0f)
        assertEquals(-1, engine.grabbedNodeIndex)
    }
}
