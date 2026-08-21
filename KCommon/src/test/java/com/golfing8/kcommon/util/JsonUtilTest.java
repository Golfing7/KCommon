package com.golfing8.kcommon.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    @Test
    @DisplayName("Reads each supported boxed numeric/string type")
    void testReadsSupportedTypes() {
        assertEquals(1.5, JsonUtil.readByType(new JsonPrimitive(1.5), Double.class));
        assertEquals(1.5f, JsonUtil.readByType(new JsonPrimitive(1.5f), Float.class));
        assertEquals(123L, JsonUtil.readByType(new JsonPrimitive(123L), Long.class));
        assertEquals(123, JsonUtil.readByType(new JsonPrimitive(123), Integer.class));
        assertEquals((short) 5, JsonUtil.readByType(new JsonPrimitive(5), Short.class));
        assertEquals((byte) 5, JsonUtil.readByType(new JsonPrimitive(5), Byte.class));
        assertEquals("hello", JsonUtil.readByType(new JsonPrimitive("hello"), String.class));
    }

    @Test
    @DisplayName("Reads a single-element JSON array into an array of the component type")
    void testReadsSingleElementArray() {
        JsonArray array = new JsonArray();
        array.add(1);

        Integer[] result = JsonUtil.readByType(array, Integer[].class);
        assertArrayEquals(new Integer[]{1}, result);
    }

    @Test
    @DisplayName("Reads a multi-element array, preserving element order")
    void testReadsMultiElementArray() {
        JsonArray array = new JsonArray();
        array.add(1);
        array.add(2);
        array.add(3);

        Integer[] result = JsonUtil.readByType(array, Integer[].class);
        assertArrayEquals(new Integer[]{1, 2, 3}, result);
    }

    @Test
    @DisplayName("Throws for an unsupported class type")
    void testUnsupportedTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.readByType(new JsonPrimitive(1), Boolean.class));
    }

    @Test
    @DisplayName("Throws when a non-array element is requested as an array type")
    void testArrayMismatchThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.readByType(new JsonPrimitive(1), Integer[].class));
    }
}
