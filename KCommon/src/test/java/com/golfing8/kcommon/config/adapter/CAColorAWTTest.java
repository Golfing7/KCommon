package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CAColorAWTTest {

    @Test
    @DisplayName("Round trips a color through its hex string")
    void testRoundTripsHexColor() {
        Color original = new Color(0x1A, 0x2B, 0x3C);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        assertEquals("1A2B3C", primitive.getPrimitive());

        Color result = ConfigTypeRegistry.getFromType(primitive, Color.class);
        assertEquals(original, result);
    }

    @Test
    @DisplayName("Deserializes a named AWT color field, case-insensitively")
    void testDeserializeNamedColor() {
        ConfigPrimitive primitive = ConfigPrimitive.ofString("red");
        Color result = ConfigTypeRegistry.getFromType(primitive, Color.class);
        assertEquals(Color.RED, result);
    }

    @Test
    @DisplayName("Defaults to WHITE for an unrecognized non-hex, non-named value")
    void testUnrecognizedNameDefaultsToWhite() {
        ConfigPrimitive primitive = ConfigPrimitive.ofString("not-a-color-zz");
        Color result = ConfigTypeRegistry.getFromType(primitive, Color.class);
        assertEquals(Color.WHITE, result);
    }

    @Test
    @DisplayName("Deserializes an integer RGB value directly")
    void testDeserializeIntegerRgb() {
        ConfigPrimitive primitive = ConfigPrimitive.of(0x00FF00);
        Color result = ConfigTypeRegistry.getFromType(primitive, Color.class);
        assertEquals(new Color(0x00FF00), result);
    }
}
