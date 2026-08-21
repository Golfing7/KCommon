package com.golfing8.kcommon.struct.ptr;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FinalPointerTest {

    @Test
    void testGetReturnsConstructedValue() {
        FinalPointer<String> pointer = new FinalPointer<>("value");
        assertEquals("value", pointer.get());
    }

    @Test
    void testSetThrows() {
        FinalPointer<String> pointer = new FinalPointer<>("value");
        assertThrows(UnsupportedOperationException.class, () -> pointer.set("other"));
    }

    @Test
    void testEqualsComparesUnderlyingValueAcrossPointerTypes() {
        FinalPointer<String> a = new FinalPointer<>("value");
        FinalPointer<String> b = new FinalPointer<>("value");
        FinalPointer<String> different = new FinalPointer<>("other");

        assertEquals(a, b);
        assertNotEquals(a, different);
        assertNotEquals(a, "not a pointer");
    }

    @Test
    void testHashCodeMatchesForEqualValues() {
        FinalPointer<String> a = new FinalPointer<>("value");
        FinalPointer<String> b = new FinalPointer<>("value");

        assertEquals(a.hashCode(), b.hashCode());
    }
}
