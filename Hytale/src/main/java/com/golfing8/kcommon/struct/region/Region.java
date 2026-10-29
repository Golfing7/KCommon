package com.golfing8.kcommon.struct.region;

import com.golfing8.kcommon.nms.struct.Position;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

/**
 * An abstract region in three-dimensional space, optionally bound to a world by name.
 */
public interface Region extends Iterable<Position> {
    /**
     * Gets the block at the center of the region.
     */
    Position getCenter();

    /**
     * Gets the largest X coordinate in the region.
     */
    double getMaximumXValue();

    /**
     * Gets the smallest X coordinate in the region.
     */
    double getMinimumXValue();

    /**
     * Gets the largest Y coordinate in the region.
     */
    double getMaximumYValue();

    /**
     * Gets the smallest Y coordinate in the region.
     */
    double getMinimumYValue();

    /**
     * Gets the largest Z coordinate in the region.
     */
    double getMaximumZValue();

    /**
     * Gets the smallest Z coordinate in the region.
     */
    double getMinimumZValue();

    /**
     * Gets the volume of the region.
     */
    double getVolume();

    /**
     * Gets the distance from the position to the region, 0 if it's inside.
     */
    double getDistance(Vector3d vector);

    /**
     * Gets the distance from the location to the region, 0 if it's inside.
     */
    double getDistance(Location location);

    /**
     * Gets a copy of this region grown by the amount on every side.
     */
    Region grow(double toGrow);

    /**
     * Gets a copy of this region moved by the offset.
     */
    Region shift(Vector3d offset);

    /**
     * Gets a copy of this region in the given world.
     */
    Region withWorld(String world);

    /**
     * Checks if the position is inside the region.
     */
    boolean isPositionWithin(Vector3d vector);

    /**
     * Checks if the regions overlap.
     */
    boolean overlapsWith(Region region);

    /**
     * Gets a random position inside the region.
     */
    Vector3d getRandomPosition();

    /**
     * Checks if the block is inside the region.
     */
    default boolean isPositionWithin(Position position) {
        return isPositionWithin(new Vector3d(position.getX(), position.getY(), position.getZ()));
    }

    /**
     * Checks if the location is inside the region and its world.
     */
    default boolean isPositionWithin(Location location) {
        return (getWorld() == null || getWorld().equals(location.getWorld())) && isPositionWithin(location.getPosition());
    }

    /**
     * Gets the name of the world this region is in.
     *
     * @return the world name, or null if the region applies to every world.
     */
    default @Nullable String getWorld() {
        return null;
    }

    /**
     * Checks if the player is inside the region and its world.
     */
    default boolean isWithin(PlayerRef player) {
        World world = player.getWorldUuid() == null ? null : Universe.get().getWorld(player.getWorldUuid());
        if (getWorld() != null && (world == null || !getWorld().equals(world.getName())))
            return false;

        return isPositionWithin(player.getTransform().getPosition());
    }
}
