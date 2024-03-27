package com.golfing8.kcommon.struct.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

/**
 * An abstract region in three-dimensional space.
 */
public interface Region extends Iterable<BlockVector> {
    /**
     * The 'center' of this region.
     *
     * @return the center.
     */
    BlockVector getCenter();

    /**
     * Gets the maximum X value this region occupies.
     *
     * @return the maximum X value.
     */
    double getMaximumXValue();
    /**
     * Gets the minimum X value this region occupies.
     *
     * @return the minimum X value.
     */
    double getMinimumXValue();

    /**
     * Gets the maximum Y value this region occupies.
     *
     * @return the maximum Y value.
     */
    double getMaximumYValue();
    /**
     * Gets the minimum Y value this region occupies.
     *
     * @return the minimum Y value.
     */
    double getMinimumYValue();

    /**
     * Gets the maximum Z value this region occupies.
     *
     * @return the maximum Z value.
     */
    double getMaximumZValue();
    /**
     * Gets the minimum Z value this region occupies.
     *
     * @return the minimum Z value.
     */
    double getMinimumZValue();

    /**
     * Gets the volume of this region.
     *
     * @return the total volume.
     */
    double getVolume();

    /**
     * Gets the closest distance to the region.
     *
     * @param vector the vector.
     * @return the distance.
     */
    double getDistance(Vector vector);

    /**
     * Gets the closest distance to the region.
     *
     * @param location the location.
     * @return the distance.
     */
    double getDistance(Location location);

    /**
     * Grows this region by the given amount on all axes.
     *
     * @return the cloned grown region.
     */
    Region grow(double toGrow);

    /**
     * Checks if the given vector is within this region.
     *
     * @param vector the vector position.
     * @return true if it's within the region, false if not.
     */
    boolean isPositionWithin(Vector vector);

    /**
     * Gets a random position within this region.
     *
     * @return the random position.
     */
    Vector getRandomPosition();

    /**
     * Checks if the given location is within this region.
     *
     * @param location the vector position.
     * @return true if it's within the region, false if not.
     */
    default boolean isPositionWithin(Location location) {
        return (getWorld() == null || getWorld() == location.getWorld()) && isPositionWithin(location.toVector());
    }

    /**
     * Gets the world associated with this region.
     *
     * @return the world.
     */
    default World getWorld() {
        return null;
    }

    /**
     * Checks if this entity's position is within this region.
     *
     * @param entity the entity.
     * @return true if they are within this region.
     */
    default boolean isWithin(Entity entity) {
        return (getWorld() == null || getWorld() == entity.getWorld()) && isPositionWithin(entity.getLocation().toVector().toBlockVector());
    }
}
