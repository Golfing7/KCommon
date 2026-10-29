package com.golfing8.kcommon.struct.region;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.hypixel.hytale.math.vector.Location;
import com.golfing8.kcommon.nms.struct.Position;
import org.joml.Vector3d;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A cuboid region occupying a rectangular prism area in three-dimensional space.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE) // For serialization
public class CuboidRegion implements Region {
    /**
     * The world that this region belongs to, can be null
     */
    private String world;
    private double minX, maxX, minY, maxY, minZ, maxZ;

    //Creates a cuboid region with all the given bounds.
    public CuboidRegion(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        this(minX, maxX, minY, maxY, minZ, maxZ, null);
    }

    public CuboidRegion(double minX, double maxX, double minY, double maxY, double minZ, double maxZ, String world) {
        this.minX = Math.min(minX, maxX);
        this.maxX = Math.max(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxZ = Math.max(minZ, maxZ);

        this.world = world;
    }

    /**
     * Creates the cuboid region with the minimum and maximum vectors
     *
     * @param minimum the minimum location
     * @param maximum the maximum location
     */
    public CuboidRegion(Position minimum, Position maximum) {
        this(minimum.getX(), maximum.getX(), minimum.getY(), maximum.getY(), minimum.getZ(), maximum.getZ());
    }

    /**
     * Creates the cuboid region with the minimum and maximum locations
     * Assumes both locations use the same world.
     *
     * @param minimum the minimum location
     * @param maximum the maximum location
     */
    public CuboidRegion(Location minimum, Location maximum) {
        this(minimum.getPosition().x, maximum.getPosition().x, minimum.getPosition().y, maximum.getPosition().y, minimum.getPosition().z, maximum.getPosition().z, minimum.getWorld());
    }

    /**
     * Gets a random position within this region.
     *
     * @return the random position.
     */
    public Vector3d getRandomPosition() {
        double x = ThreadLocalRandom.current().nextDouble(this.minX, this.maxX + 1e-6);
        double y = ThreadLocalRandom.current().nextDouble(this.minY, this.maxY + 1e-6);
        double z = ThreadLocalRandom.current().nextDouble(this.minZ, this.maxZ + 1e-6);

        return new Vector3d(x, y, z);
    }

    /**
     * Gets the minimum coordinate corner of this region.
     *
     * @return the minimum.
     */
    public Position getMinimum() {
        return new Position(this.minX, minY, minZ);
    }

    /**
     * Gets the maximum coordinate corner of this region.
     *
     * @return the maximum.
     */
    public Position getMaximum() {
        return new Position(this.maxX, maxY, maxZ);
    }

    @Override
    public Position getCenter() {
        double middleX = (this.maxX + this.minX) / 2D;
        double middleY = (this.maxY + this.minY) / 2D;
        double middleZ = (this.maxZ + this.minZ) / 2D;
        return new Position(middleX, middleY, middleZ);
    }

    @Override
    public double getMaximumXValue() {
        return this.maxX;
    }

    @Override
    public double getMinimumXValue() {
        return this.minX;
    }

    @Override
    public double getMaximumYValue() {
        return this.maxY;
    }

    @Override
    public double getMinimumYValue() {
        return this.minY;
    }

    @Override
    public double getMaximumZValue() {
        return this.maxZ;
    }

    @Override
    public double getMinimumZValue() {
        return this.minZ;
    }

    @Override
    public double getVolume() {
        return (maxX - minX) * (maxY - minY) * (maxZ - minZ);
    }

    @Override
    public double getDistance(Vector3d vector) {
        if (isPositionWithin(vector))
            return 0;

        double distX = vector.x < this.minX ? this.minX - vector.x : vector.x > this.maxX ? vector.x - this.maxX : 0;
        double distY = vector.y < this.minY ? this.minY - vector.y : vector.y > this.maxY ? vector.y - this.maxY : 0;
        double distZ = vector.z < this.minZ ? this.minZ - vector.z : vector.z > this.maxZ ? vector.z - this.maxZ : 0;
        return Math.sqrt(distX * distX + distY * distY + distZ * distZ);
    }

    @Override
    public double getDistance(Location location) {
        if (getWorld() != null && !getWorld().equals(location.getWorld()))
            throw new IllegalArgumentException("Location world does not match.");

        return getDistance(location.getPosition());
    }

    @Override
    public boolean isPositionWithin(Vector3d vector) {
        return vector.x >= this.minX && vector.x <= this.maxX &&
                vector.y >= this.minY && vector.y <= this.maxY &&
                vector.z >= this.minZ && vector.z <= this.maxZ;
    }

    @Override
    public boolean overlapsWith(Region region) {
        if (this.getWorld() != null && !this.getWorld().equals(region.getWorld()))
            return false;

        // Check low end of Y
        if (this.minX > region.getMaximumXValue() || this.maxX < region.getMinimumXValue())
            return false;

        if (this.minY > region.getMaximumYValue() || this.maxY < region.getMinimumYValue())
            return false;

        return this.minZ <= region.getMaximumZValue() && this.maxZ >= region.getMinimumZValue();
    }

    @Override
    public Region grow(double toGrow) {
        return new CuboidRegion(minX - toGrow, maxX + toGrow, minY - toGrow, maxY + toGrow, minZ - toGrow, maxZ + toGrow, world);
    }

    @Override
    public Region shift(Vector3d offset) {
        return new CuboidRegion(minX + offset.x, maxX + offset.x, minY + offset.y, maxY + offset.y, minZ + offset.z, maxZ + offset.z, world);
    }

    @Override
    public Region withWorld(String world) {
        return new CuboidRegion(minX, maxX, minY, maxY, minZ, maxZ, world);
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public @NotNull Iterator<Position> iterator() {
        return new CuboidRegionIterator();
    }

    /**
     * An iterator which will iterate over all blocks in this region.
     */
    public class CuboidRegionIterator implements Iterator<Position> {
        private int currentX, currentY, currentZ;
        private final int maxX, maxY, maxZ;
        private boolean finished = false;

        /**
         * Generates a cuboid region iterator by iterating from the minimum block to the maximum.
         */
        public CuboidRegionIterator() {
            this.currentX = (int) Math.floor(CuboidRegion.this.minX);
            this.currentY = (int) Math.floor(CuboidRegion.this.minY);
            this.currentZ = (int) Math.floor(CuboidRegion.this.minZ);

            this.maxX = (int) Math.floor(CuboidRegion.this.maxX);
            this.maxY = (int) Math.floor(CuboidRegion.this.maxY);
            this.maxZ = (int) Math.floor(CuboidRegion.this.maxZ);
        }

        @Override
        public boolean hasNext() {
            return !finished && (currentX <= maxX || currentY <= maxY || currentZ <= maxZ);
        }

        @Override
        public Position next() {
            if (!hasNext())
                throw new NoSuchElementException("This iterator does not have any elements remaining!");

            Position next = new Position(this.currentX, this.currentY, this.currentZ);

            if (currentX < maxX) {
                currentX++;
            } else if (currentY < maxY) {
                currentX = (int) Math.floor(CuboidRegion.this.minX);
                currentY++;
            } else if (currentZ < maxZ) {
                currentX = (int) Math.floor(CuboidRegion.this.minX);
                currentY = (int) Math.floor(CuboidRegion.this.minY);
                currentZ++;
            } else {
                finished = true;
            }
            return next;
        }
    }
}
