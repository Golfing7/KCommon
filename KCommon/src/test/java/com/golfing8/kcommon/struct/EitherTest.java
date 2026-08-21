package com.golfing8.kcommon.struct;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EitherTest {

    @Test
    void testLeftHoldsLeftValueOnly() {
        Either<String, Integer> either = Either.left("hello");

        assertTrue(either.left().isPresent());
        assertEquals("hello", either.left().get());
        assertFalse(either.right().isPresent());
        assertEquals("hello", either.get());
    }

    @Test
    void testRightHoldsRightValueOnly() {
        Either<String, Integer> either = Either.right(42);

        assertTrue(either.right().isPresent());
        assertEquals(42, either.right().get());
        assertFalse(either.left().isPresent());
        assertEquals(42, either.get());
    }
}
