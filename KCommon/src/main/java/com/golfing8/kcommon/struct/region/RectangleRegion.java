package com.golfing8.kcommon.struct.region;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.Iterator;

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
    public Region grow(double toGrow) {
        return new RectangleRegion(this.minX - toGrow, this.maxX + toGrow, this.minZ - toGrow, this.maxZ + toGrow);
    }

    @Override
    public boolean isPositionWithin(Vector vector) {
        return vector.getX() >= this.minX && vector.getX() <= this.maxX &&
                vector.getZ() >= this.minZ && vector.getZ() <= this.maxZ;
    }

    @Override
    public Iterator<BlockVector> iterator() {
        throw new UnsupportedOperationException("Rectangle region iterator not supported!");
    }
}
