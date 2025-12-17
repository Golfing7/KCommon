package com.golfing8.kcommon.struct.region;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a region that exists on the X-Z plane and infinitely on the Y plane.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE) // For serialization
public class RectangleRegion implements Region {
    private double minX, maxX;
    private double minZ, maxZ;
    private World world;

    public RectangleRegion(double minX, double maxX, double minZ, double maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public RectangleRegion(double minX, double maxX, double minZ, double maxZ, World world) {
        this(minX, maxX, minZ, maxZ);
        this.world = world;
    }

    /**
     * Checks if the region overlaps with this region.
     *
     * @param region the region.
     * @return if it overlaps.
     */
    public boolean overlaps(RectangleRegion region) {
        if (this.minX > region.maxX || region.minX > this.maxX)
            return false;

        return !(this.minZ > region.maxZ) && !(region.minZ > this.maxZ);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public BlockVector getCenter() {
        // Because this region doesn't exist on the Y axis, we simply use 0.
        return new BlockVector((maxX + minX) / 2, 0, (maxZ + minZ) / 2);
    }

    @Override
    public double getMaximumXValue() {
        return maxX;
    }

    @Override
    public double getMinimumXValue() {
        return minX;
    }

    @Override
    public double getMaximumYValue() {
        return Double.MAX_VALUE;
    }

    @Override
    public double getMinimumYValue() {
        return Double.MIN_VALUE;
    }

    @Override
    public double getMaximumZValue() {
        return maxZ;
    }

    @Override
    public double getMinimumZValue() {
        return minZ;
    }

    @Override
    public double getVolume() {
        return 0.0D; // Planes don't have volume.
    }

    /**
     * Gets the area of the region.
     *
     * @return the area.
     */
    public double getArea() {
        return (maxX - minX) * (maxZ - minZ);
    }

    /**
     * Gets the block area of the region.
     *
     * @return the block area.
     */
    public int getBlockArea() {
        return (int) ((Math.ceil(maxX + 1e-7) - minX) * (Math.ceil(maxZ + 1e-7) - minZ));
    }

    @Override
    public double getDistance(Vector vector) {
        if (isPositionWithin(vector))
            return 0;

        double distX = vector.getX() < this.minX ? this.minX - vector.getX() : vector.getX() > this.maxX ? vector.getX() - this.maxX : 0;
        double distZ = vector.getZ() < this.minZ ? this.minZ - vector.getZ() : vector.getZ() > this.maxZ ? vector.getZ() - this.maxZ : 0;
        return Math.sqrt(distX * distX + distZ * distZ);
    }

    @Override
    public double getDistance(Location location) {
        if (getWorld() != null && location.getWorld() != getWorld())
            throw new IllegalArgumentException("Location world does not match.");

        return getDistance(location.toVector());
    }

    @Override
    public RectangleRegion grow(double toGrow) {
        return new RectangleRegion(this.minX - toGrow, this.maxX + toGrow, this.minZ - toGrow, this.maxZ + toGrow, world);
    }

    @Override
    public Region shift(Vector offset) {
        return new RectangleRegion(minX + offset.getX(), maxX + offset.getX(), minZ + offset.getZ(), maxZ + offset.getZ(), world);
    }

    @Override
    public Region withWorld(World world) {
        return new RectangleRegion(minX, maxX, minZ, maxZ, world);
    }

    @Override
    public boolean isPositionWithin(Vector vector) {
        return vector.getX() >= this.minX && vector.getX() <= this.maxX &&
                vector.getZ() >= this.minZ && vector.getZ() <= this.maxZ;
    }

    @Override
    public boolean overlapsWith(Region region) {
        if (this.getWorld() != null && region.getWorld() != this.getWorld())
            return false;

        // Check low end of Y
        if (this.minX > region.getMaximumXValue() || this.maxX < region.getMinimumXValue())
            return false;

        return this.minZ <= region.getMaximumZValue() && this.maxZ >= region.getMinimumZValue();
    }

    @Override
    public Vector getRandomPosition() {
        double x = ThreadLocalRandom.current().nextDouble(this.minX, this.maxX + 1e-6);
        double z = ThreadLocalRandom.current().nextDouble(this.minZ, this.maxZ + 1e-6);

        return new Vector(x, 0, z);
    }

    @Override
    @Contract(" -> fail")
    public @NotNull Iterator<BlockVector> iterator() {
        throw new UnsupportedOperationException("Rectangle region iterator not supported!");
    }
}
