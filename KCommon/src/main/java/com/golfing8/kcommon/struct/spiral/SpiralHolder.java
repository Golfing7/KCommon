package com.golfing8.kcommon.struct.spiral;

/**
 * A class that 'spirals' out every tick.
 */
public class SpiralHolder {
    /**
     * The current X coordinate.
     */
    private int currentX;
    /**
     * The current Z coordinate.
     */
    private int currentZ;
    /**
     * The radius of the spiral.
     */
    private int radius;

    public SpiralHolder() {
        this.currentX = this.radius = this.currentZ = 0;
    }

    /**
     * Steps this holder forward.
     */
    private void advance() {
        //Bump out currentX first, always.
        if (this.currentX == radius && this.currentZ == -radius) {
            this.currentX++;
            this.radius++;
            return;
        }

        if (this.currentX == radius && this.currentZ < radius) {
            this.currentZ++;
            return;
        }

        if (this.currentZ == radius && this.currentX > -radius) {
            this.currentX--;
            return;
        }

        if (this.currentX == -radius && this.currentZ > -radius) {
            this.currentZ--;
            return;
        }

        if (this.currentZ == -radius && this.currentX < radius) {
            this.currentX++;
        }
    }

    /**
     * Gets the next spiral coordinate available.
     *
     * @return the next coordinate.
     */
    public SpiralCoordinate next() {
        SpiralCoordinate coord = new SpiralCoordinate(this.currentX, this.currentZ);
        advance();
        return coord;
    }

    /**
     * Gets the next available spiral coordinate, but does not advance.
     *
     * @return the next coordinate.
     */
    public SpiralCoordinate peekNext() {
        return new SpiralCoordinate(this.currentX, this.currentZ);
    }
}
