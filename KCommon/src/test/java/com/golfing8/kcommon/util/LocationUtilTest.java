package com.golfing8.kcommon.util;

import com.golfing8.kcommon.module.test.util.FakeServer;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocationUtilTest {

    private World world;

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
        world = FakeServer.getServer().getWorld("testWorld");
    }

    @Nested
    @DisplayName("forEachLocationInRange")
    class ForEachLocationInRange {
        @Test
        @DisplayName("Visits every location within a cubic range around a point")
        void testCubicRange() {
            Location center = new Location(world, 0, 0, 0);
            List<Location> visited = new ArrayList<>();
            LocationUtil.forEachLocationInRange(center, 1, visited::add);
            // (2*1+1)^3 = 27 locations
            assertEquals(27, visited.size());
        }

        @Test
        @DisplayName("Visits every location within an asymmetric range")
        void testAsymmetricRange() {
            Location center = new Location(world, 5, 5, 5);
            List<Location> visited = new ArrayList<>();
            LocationUtil.forEachLocationInRange(center, 1, 0, 2, visited::add);
            // (2*1+1) * (2*0+1) * (2*2+1) = 3 * 1 * 5 = 15
            assertEquals(15, visited.size());
        }

        @Test
        @DisplayName("Does not mutate the original location")
        void testDoesNotMutateOriginal() {
            Location center = new Location(world, 0, 0, 0);
            LocationUtil.forEachLocationInRange(center, 2, loc -> {});
            assertEquals(0, center.getX());
            assertEquals(0, center.getY());
            assertEquals(0, center.getZ());
        }

        @Test
        @DisplayName("Two-position variant iterates over the enclosed cuboid")
        void testTwoPositionVariant() {
            Location pos1 = new Location(world, 0, 0, 0);
            Location pos2 = new Location(world, 1, 1, 1);
            List<Location> visited = new ArrayList<>();
            LocationUtil.forEachLocationInRange(pos1, pos2, visited::add);
            assertEquals(8, visited.size());
        }

        @Test
        @DisplayName("Two-position variant throws when worlds differ")
        void testTwoPositionVariantDifferentWorlds() {
            World otherWorld = FakeServer.getServer().createWorld("otherWorld", World.Environment.NORMAL);
            Location pos1 = new Location(world, 0, 0, 0);
            Location pos2 = new Location(otherWorld, 1, 1, 1);
            assertThrows(IllegalArgumentException.class, () -> LocationUtil.forEachLocationInRange(pos1, pos2, loc -> {}));
        }
    }

    @Nested
    @DisplayName("encodeBlockLocationToString / decodeFromString")
    class Codec {
        @Test
        @DisplayName("Encodes a location to the expected format")
        void testEncode() {
            Location location = new Location(world, 1.9, 2.1, -3.4);
            String encoded = LocationUtil.encodeBlockLocationToString(location);
            assertEquals("1;2;-4;testWorld", encoded);
        }

        @Test
        @DisplayName("Decodes an encoded location back to its components")
        void testDecode() {
            Location decoded = LocationUtil.decodeFromString("1;2;-4;testWorld");
            assertEquals(1, decoded.getBlockX());
            assertEquals(2, decoded.getBlockY());
            assertEquals(-4, decoded.getBlockZ());
            assertEquals(world, decoded.getWorld());
        }

        @Test
        @DisplayName("Round trips through encode and decode")
        void testRoundTrip() {
            Location original = new Location(world, 10, 20, 30);
            Location roundTripped = LocationUtil.decodeFromString(LocationUtil.encodeBlockLocationToString(original));
            assertEquals(original.getBlockX(), roundTripped.getBlockX());
            assertEquals(original.getBlockY(), roundTripped.getBlockY());
            assertEquals(original.getBlockZ(), roundTripped.getBlockZ());
            assertEquals(original.getWorld(), roundTripped.getWorld());
        }
    }
}
