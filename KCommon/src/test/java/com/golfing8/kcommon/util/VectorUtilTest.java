package com.golfing8.kcommon.util;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VectorUtilTest {

    private static final double EPSILON = 1e-9;

    @Test
    @DisplayName("Rotating around the X axis by 90 degrees maps Y onto Z")
    void testRotateAroundX90() {
        Vector vector = new Vector(0, 1, 0);
        Vector result = VectorUtil.rotateAroundX(vector, Math.PI / 2);
        assertEquals(0, result.getX(), EPSILON);
        assertEquals(0, result.getY(), EPSILON);
        assertEquals(1, result.getZ(), EPSILON);
    }

    @Test
    @DisplayName("Rotating around the Y axis by 90 degrees maps X onto -Z")
    void testRotateAroundY90() {
        Vector vector = new Vector(1, 0, 0);
        Vector result = VectorUtil.rotateAroundY(vector, Math.PI / 2);
        assertEquals(0, result.getX(), EPSILON);
        assertEquals(0, result.getY(), EPSILON);
        assertEquals(-1, result.getZ(), EPSILON);
    }

    @Test
    @DisplayName("Rotating around the Z axis by 90 degrees maps X onto Y")
    void testRotateAroundZ90() {
        Vector vector = new Vector(1, 0, 0);
        Vector result = VectorUtil.rotateAroundZ(vector, Math.PI / 2);
        assertEquals(0, result.getX(), EPSILON);
        assertEquals(1, result.getY(), EPSILON);
        assertEquals(0, result.getZ(), EPSILON);
    }

    @Test
    @DisplayName("Rotating by a full 2*PI returns to the original vector")
    void testFullRotationIsIdentity() {
        Vector vector = new Vector(3, 4, 5);
        Vector result = VectorUtil.rotateAroundX(vector, Math.PI * 2);
        assertEquals(3, result.getX(), EPSILON);
        assertEquals(4, result.getY(), EPSILON);
        assertEquals(5, result.getZ(), EPSILON);
    }

    @Test
    @DisplayName("Rotation mutates and returns the same vector instance")
    void testMutatesInPlace() {
        Vector vector = new Vector(0, 1, 0);
        Vector result = VectorUtil.rotateAroundX(vector, Math.PI / 2);
        assertEquals(vector, result);
    }
}
