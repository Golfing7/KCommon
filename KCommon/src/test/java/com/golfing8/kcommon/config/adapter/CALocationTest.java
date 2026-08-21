package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.module.test.util.FakeServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CALocationTest {
    private World world;

    @BeforeEach
    void setUp() {
        world = FakeServer.getServer().getWorld("testWorld");
    }

    @Test
    @DisplayName("Round trips a location with default yaw/pitch, omitting them from the serialized string")
    void testRoundTripWithoutYawPitch() {
        Location location = new Location(world, 1.5, 2.5, 3.5);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(location);
        assertEquals("1.5;2.5;3.5;testWorld", primitive.getPrimitive());

        Location loaded = ConfigTypeRegistry.getFromType(primitive, Location.class);
        assertEquals(location.getX(), loaded.getX());
        assertEquals(location.getY(), loaded.getY());
        assertEquals(location.getZ(), loaded.getZ());
        assertEquals(0.0F, loaded.getYaw());
        assertEquals(0.0F, loaded.getPitch());
        assertEquals("testWorld", loaded.getWorld().getName());
    }

    @Test
    @DisplayName("Round trips a location with non-default yaw/pitch, including them in the serialized string")
    void testRoundTripWithYawPitch() {
        Location location = new Location(world, 10, 20, 30, 45.0F, 10.0F);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(location);
        assertEquals("10.0;20.0;30.0;testWorld;45.0;10.0", primitive.getPrimitive());

        Location loaded = ConfigTypeRegistry.getFromType(primitive, Location.class);
        assertEquals(45.0F, loaded.getYaw());
        assertEquals(10.0F, loaded.getPitch());
    }

    @Test
    @DisplayName("Reads a location from a map-based primitive")
    void testReadFromMap() {
        CALocation adapter = new CALocation();
        ConfigPrimitive mapPrimitive = ConfigPrimitive.ofMap(
                com.golfing8.kcommon.util.MapUtil.of("world", "testWorld", "x", 1.0, "y", 2.0, "z", 3.0, "yaw", 90.0, "pitch", 45.0)
        );

        Location loaded = adapter.toPOJO(mapPrimitive, null);
        assertEquals(1.0, loaded.getX());
        assertEquals(2.0, loaded.getY());
        assertEquals(3.0, loaded.getZ());
        assertEquals(90.0F, loaded.getYaw());
        assertEquals(45.0F, loaded.getPitch());
    }

    @Test
    @DisplayName("Returns null for a null primitive")
    void testNullPrimitive() {
        CALocation adapter = new CALocation();
        assertEquals(null, adapter.toPOJO(ConfigPrimitive.ofNull(), null));
    }
}
