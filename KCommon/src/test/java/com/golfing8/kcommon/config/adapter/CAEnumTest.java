package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CAEnumTest {

    enum Suit {
        HEARTS, SPADES, CLUBS, DIAMONDS
    }

    @Test
    @DisplayName("Round trips an enum constant by name")
    void testRoundTripsEnumConstant() {
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(Suit.SPADES);
        Suit result = ConfigTypeRegistry.getFromType(primitive, Suit.class);
        assertEquals(Suit.SPADES, result);
    }

    @Test
    @DisplayName("An unrecognized enum name deserializes to null")
    void testUnrecognizedNameReturnsNull() {
        ConfigPrimitive primitive = ConfigPrimitive.ofString("NOT_A_SUIT");
        Suit result = ConfigTypeRegistry.getFromType(primitive, Suit.class);
        assertNull(result);
    }

    @Test
    @DisplayName("Enum name matching is case-sensitive")
    void testEnumMatchingIsCaseSensitive() {
        ConfigPrimitive primitive = ConfigPrimitive.ofString("spades");
        Suit result = ConfigTypeRegistry.getFromType(primitive, Suit.class);
        assertNull(result);
    }
}
