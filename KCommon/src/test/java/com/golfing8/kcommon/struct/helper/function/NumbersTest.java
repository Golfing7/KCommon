package com.golfing8.kcommon.struct.helper.function;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.*;

class NumbersTest {

    @Test
    void testParseNullableValidAndInvalid() {
        assertNotNull(Numbers.parseNullable("123"));
        assertNull(Numbers.parseNullable("not a number"));
    }

    @Test
    void testParseReturnsOptional() {
        assertTrue(Numbers.parse("123").isPresent());
        assertFalse(Numbers.parse("nope").isPresent());
    }

    @Test
    void testParseIntegerValidAndInvalid() {
        assertEquals(OptionalInt.of(42), Numbers.parseInteger("42"));
        assertEquals(OptionalInt.empty(), Numbers.parseInteger("abc"));
    }

    @Test
    void testParseIntegerOptAndNullable() {
        assertEquals(Integer.valueOf(42), Numbers.parseIntegerNullable("42"));
        assertNull(Numbers.parseIntegerNullable("abc"));
        assertEquals(Optional.of(42), Numbers.parseIntegerOpt("42"));
        assertEquals(Optional.empty(), Numbers.parseIntegerOpt("abc"));
    }

    @Test
    void testParseLongValidAndInvalid() {
        assertEquals(OptionalLong.of(42L), Numbers.parseLong("42"));
        assertEquals(OptionalLong.empty(), Numbers.parseLong("abc"));
        assertEquals(Long.valueOf(42L), Numbers.parseLongNullable("42"));
        assertNull(Numbers.parseLongNullable("abc"));
    }

    @Test
    void testParseFloatValidAndInvalid() {
        assertTrue(Numbers.parseFloat("1.5").isPresent());
        assertFalse(Numbers.parseFloat("abc").isPresent());
        assertEquals(Float.valueOf(1.5f), Numbers.parseFloatNullable("1.5"));
        assertNull(Numbers.parseFloatNullable("abc"));
    }

    @Test
    void testParseDoubleValidAndInvalid() {
        assertTrue(Numbers.parseDouble("1.5").isPresent());
        assertFalse(Numbers.parseDouble("abc").isPresent());
        assertEquals(Double.valueOf(1.5), Numbers.parseDoubleNullable("1.5"));
        assertNull(Numbers.parseDoubleNullable("abc"));
    }

    @Test
    void testParseByteValidAndInvalid() {
        assertEquals(Byte.valueOf((byte) 5), Numbers.parseByteNullable("5"));
        assertNull(Numbers.parseByteNullable("abc"));
        assertTrue(Numbers.parseByteOpt("5").isPresent());
        assertFalse(Numbers.parseByteOpt("abc").isPresent());
    }
}
