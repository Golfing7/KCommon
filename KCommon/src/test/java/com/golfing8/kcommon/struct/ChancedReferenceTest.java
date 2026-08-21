package com.golfing8.kcommon.struct;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ChancedReferenceTest {

    @Test
    void testGuaranteedChanceAlwaysReturns() {
        ChancedReference<String> reference = new ChancedReference<>(100.0D, "value");
        for (int i = 0; i < 50; i++) {
            assertEquals(Optional.of("value"), reference.get());
        }
    }

    @Test
    void testZeroChanceNeverReturns() {
        ChancedReference<String> reference = new ChancedReference<>(0.0D, "value");
        for (int i = 0; i < 50; i++) {
            assertEquals(Optional.empty(), reference.get());
        }
    }

    @Test
    void testDefaultConstructorUses100PercentChance() {
        ChancedReference<String> reference = new ChancedReference<>("value");
        assertEquals(100.0D, reference.getChance());
        assertEquals(Optional.of("value"), reference.get());
    }

    @Test
    void testZeroChanceWithBoostCanSucceed() {
        // getChance() * boost > roll * 100. With chance 0, no boost can push it over 0.
        ChancedReference<String> reference = new ChancedReference<>(0.0D, "value");
        assertEquals(Optional.empty(), reference.get(1000.0D));
    }

    @Test
    void testGetReturnsReference() {
        Object ref = new Object();
        ChancedReference<Object> reference = new ChancedReference<>(100.0D, ref);
        assertSame(ref, reference.getReference());
    }
}
