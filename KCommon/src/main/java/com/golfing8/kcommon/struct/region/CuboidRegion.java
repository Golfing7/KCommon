package com.golfing8.kcommon.struct.region;

import org.bukkit.util.BlockVector;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A cuboid region occupying a rectangular prism area in three-dimensional space.
 */
public class CuboidRegion implements Region {
    /**
     * The absolute center of this region.
     */
    private final BlockVector center;
    private final double minX, maxX, minY, maxY, minZ, maxZ;

    //Creates a cuboid region with all the given bounds.
    public CuboidRegion(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.maxX = Math.max(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxZ = Math.max(minZ, maxZ);

        //Resolve the middle of the location.
        double middleX = (this.maxX + this.minX) / 2D;
        double middleY = (this.maxY + this.minY) / 2D;
        double middleZ = (this.maxZ + this.minZ) / 2D;
        this.center = new BlockVector(middleX, middleY, middleZ);
    }

    /**
     * Creates the cuboid region with the minimum and maximum vectors
     *
     * @param minimum the minimum location
     * @param maximum the maximum location
     */
    public CuboidRegion(BlockVector minimum, BlockVector maximum) {
        this(minimum.getX(), maximum.getX(), minimum.getY(), maximum.getY(), minimum.getZ(), maximum.getZ());
    }

    /**
     * Gets the minimum coordinate corner of this region.
     *
     * @return the minimum.
     */
    public BlockVector getMinimum() {
        return new BlockVector(this.minX, minY, minZ);
    }

    /**
     * Gets the maximum coordinate corner of this region.
     *
     * @return the maximum.
     */
    public BlockVector getMaximum() {
        return new BlockVector(this.maxX, maxY, maxZ);
    }

    @Override
    public BlockVector getCenter() {
        return new BlockVector(this.center);
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
    public boolean isPositionWithin(BlockVector vector) {
        return vector.getX() >= this.minX && vector.getX() <= this.maxX &&
                vector.getY() >= this.minY && vector.getY() <= this.maxY &&
                vector.getZ() >= this.minZ && vector.getZ() <= this.maxZ;
    }

    @Override
    public Iterator<BlockVector> iterator() {
        return new CuboidRegionIterator();
    }

    /**
     * An iterator which will iterate over all blocks in this region.
     */
    public class CuboidRegionIterator implements Iterator<BlockVector> {
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
            return !(finished) && (currentX <= maxX || currentY <= maxY || currentZ <= maxZ);
        }

        @Override
        public BlockVector next() {
            if(!hasNext())
                throw new NoSuchElementException("This iterator does not have any elements remaining!");

            BlockVector next = new BlockVector(this.currentX, this.currentY, this.currentZ);

            if(currentX < maxX) {
                currentX++;
            }else if(currentY < maxY) {
                currentX = (int) Math.floor(CuboidRegion.this.minX);
                currentY++;
            }else if(currentZ < maxZ) {
                currentX = (int) Math.floor(CuboidRegion.this.minX);
                currentY = (int) Math.floor(CuboidRegion.this.minY);
                currentZ++;
            }else {
                finished = true;
            }
            return next;
        }
    }
}
