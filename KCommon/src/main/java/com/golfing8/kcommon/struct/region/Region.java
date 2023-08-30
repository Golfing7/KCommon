package com.golfing8.kcommon.struct.region;

import org.bukkit.entity.Entity;
import org.bukkit.util.BlockVector;

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
     * Checks if the given vector is within this region.
     *
     * @param vector the vector position.
     * @return true if it's within the region, false if not.
     */
    boolean isPositionWithin(BlockVector vector);

    /**
     * Checks if this entity's position is within this region.
     *
     * @param entity the entity.
     * @return true if they are within this region.
     */
    default boolean isWithin(Entity entity) {
        return isPositionWithin(entity.getLocation().toVector().toBlockVector());
    }
}
