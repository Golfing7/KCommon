package com.golfing8.kcommon.nms.struct;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Contains directions for blocks
 */
@AllArgsConstructor
public enum Direction {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    EAST(1, 0, 0),
    WEST(-1, 0, 0),
    ;

    @Getter
    private int xShift, yShift, zShift;

    /**
     * Gets a direction from the given ordinal
     *
     * @param ordinal the ordinal
     * @return the direction
     */
    public static Direction fromOrdinal(int ordinal) {
        return values()[Math.abs(ordinal % values().length)];
    }
}
