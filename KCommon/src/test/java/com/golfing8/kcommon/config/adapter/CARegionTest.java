package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.module.test.util.FakeServer;
import com.golfing8.kcommon.struct.region.CuboidRegion;
import com.golfing8.kcommon.struct.region.RectangleRegion;
import com.golfing8.kcommon.struct.region.Region;
import org.bukkit.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CARegionTest {

    @Test
    @DisplayName("Round trips a cuboid region")
    void testRoundTripCuboid() {
        World world = FakeServer.getServer().getWorld("testWorld");
        CuboidRegion region = new CuboidRegion(0, 10, 0, 5, 0, 10, world);

        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive((Region) region);
        Region loaded = ConfigTypeRegistry.getFromType(primitive, Region.class);

        assertEquals(region.getMinimumXValue(), loaded.getMinimumXValue());
        assertEquals(region.getMaximumXValue(), loaded.getMaximumXValue());
        assertEquals(region.getMinimumYValue(), loaded.getMinimumYValue());
        assertEquals(region.getMaximumYValue(), loaded.getMaximumYValue());
        assertEquals(region.getMinimumZValue(), loaded.getMinimumZValue());
        assertEquals(region.getMaximumZValue(), loaded.getMaximumZValue());
        assertEquals(world, loaded.getWorld());
    }

    @Test
    @DisplayName("Round trips a rectangle region")
    void testRoundTripRectangle() {
        World world = FakeServer.getServer().getWorld("testWorld");
        RectangleRegion region = new RectangleRegion(0, 10, 0, 10, world);

        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive((Region) region);
        Region loaded = ConfigTypeRegistry.getFromType(primitive, Region.class);

        assertEquals(region.getMinimumXValue(), loaded.getMinimumXValue());
        assertEquals(region.getMaximumXValue(), loaded.getMaximumXValue());
        assertEquals(region.getMinimumZValue(), loaded.getMinimumZValue());
        assertEquals(region.getMaximumZValue(), loaded.getMaximumZValue());
    }
}
