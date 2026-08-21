package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import org.bukkit.Color;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CAColorBukkitTest {

    @Test
    @DisplayName("Round trips a color through a hex string")
    void testRoundTrip() {
        Color color = Color.fromRGB(0x1A2B3C);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(color);
        assertEquals("1A2B3C", primitive.getPrimitive());

        Color loaded = ConfigTypeRegistry.getFromType(primitive, Color.class);
        assertEquals(color, loaded);
    }

    @Test
    @DisplayName("Reads a color from a named Bukkit Color constant field")
    void testReadByName() {
        Color loaded = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofString("RED"), Color.class);
        assertEquals(Color.RED, loaded);
    }

    @Test
    @DisplayName("Reads a color from an integer RGB value")
    void testReadFromInteger() {
        CAColorBukkit adapter = new CAColorBukkit();
        Color loaded = adapter.toPOJO(ConfigPrimitive.ofInt(0xFF0000), null);
        assertEquals(Color.RED, loaded);
    }

    @Test
    @DisplayName("Falls back to white for an unrecognized color name")
    void testUnrecognizedNameFallsBackToWhite() {
        Color result = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofString("NOT_A_REAL_COLOR"), Color.class);
        assertEquals(Color.WHITE, result);
    }
}
