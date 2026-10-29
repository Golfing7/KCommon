package com.golfing8.kcommon.struct.spiral;

import lombok.Data;

/**
 * A wrapper class for a {@link SpiralHolder}'s coordinates.
 */
@Data
public class SpiralCoordinate {
    /**
     * The x coordinate of the offset.
     */
    private final int x;
    /**
     * The z coordinate of the offset.
     */
    private final int z;
}
