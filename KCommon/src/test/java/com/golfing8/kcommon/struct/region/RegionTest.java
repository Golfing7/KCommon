package com.golfing8.kcommon.struct.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegionTest {

    @Test
    @DisplayName("isWithin(Entity) is true when the entity's location falls inside the region")
    void testIsWithinEntityTrue() {
        World world = mock(World.class);
        Region region = new CuboidRegion(0, 10, 0, 10, 0, 10, world);

        Entity entity = mock(Entity.class);
        Location location = new Location(world, 5, 5, 5);
        when(entity.getLocation()).thenReturn(location);
        when(entity.getWorld()).thenReturn(world);

        assertTrue(region.isWithin(entity));
    }

    @Test
    @DisplayName("isWithin(Entity) is false when the entity's location falls outside the region")
    void testIsWithinEntityFalse() {
        World world = mock(World.class);
        Region region = new CuboidRegion(0, 10, 0, 10, 0, 10, world);

        Entity entity = mock(Entity.class);
        Location location = new Location(world, 50, 50, 50);
        when(entity.getLocation()).thenReturn(location);
        when(entity.getWorld()).thenReturn(world);

        assertFalse(region.isWithin(entity));
    }

    @Test
    @DisplayName("A world-less region matches an entity in any world as long as position matches")
    void testWorldlessRegionIgnoresWorld() {
        Region region = new CuboidRegion(0, 10, 0, 10, 0, 10);

        World world = mock(World.class);
        Entity entity = mock(Entity.class);
        Location location = new Location(world, 5, 5, 5);
        when(entity.getLocation()).thenReturn(location);
        when(entity.getWorld()).thenReturn(world);

        assertTrue(region.isWithin(entity));
    }
}
