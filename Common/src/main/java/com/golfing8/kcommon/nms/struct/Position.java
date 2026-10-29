package com.golfing8.kcommon.nms.struct;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A basic block position
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Position {
    private final int x, y, z;

    /**
     * Shifts the position by the given offset
     *
     * @param x x offset
     * @param y y offset
     * @param z z offset
     * @return the new position
     */
    public Position shift(int x, int y, int z) {
        return new Position(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Shifts the position in the given direction
     *
     * @param direction the direction
     * @return the new position
     */
    public Position shift(Direction direction) {
        return new Position(this.x + direction.getXShift(), this.y + direction.getYShift(), this.z + direction.getZShift());
    }

    public Position() {
        this.x = this.y = this.z = 0;
    }

    public Position(double x, double y, double z) {
        this.x = (int) Math.floor(x);
        this.y = (int) Math.floor(y);
        this.z = (int) Math.floor(z);
    }
}
