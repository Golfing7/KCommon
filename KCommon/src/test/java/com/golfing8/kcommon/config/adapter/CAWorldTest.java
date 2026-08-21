package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.module.test.util.FakeServer;
import org.bukkit.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CAWorldTest {

    @Test
    @DisplayName("Round trips a world by name")
    void testRoundTrip() {
        World world = FakeServer.getServer().getWorld("testWorld");

        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(world);
        assertEquals("testWorld", primitive.getPrimitive());

        World loaded = ConfigTypeRegistry.getFromType(primitive, World.class);
        assertEquals(world, loaded);
    }

    @Test
    @DisplayName("Returns null for a null primitive")
    void testNullPrimitive() {
        CAWorld adapter = new CAWorld();
        assertNull(adapter.toPOJO(ConfigPrimitive.ofNull(), null));
    }

    @Test
    @DisplayName("Returns null for an unrecognized world name")
    void testUnknownWorld() {
        FakeServer.getServer();
        World loaded = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofString("does-not-exist"), World.class);
        assertNull(loaded);
    }
}
