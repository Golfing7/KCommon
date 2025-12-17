package com.golfing8.kcommon.menu.shape;

import java.util.List;

/**
 * An abstract shape that contains slots in a menu
 */
public interface MenuLayoutShape {
    /**
     * Gets all coordinates within the shape
     *
     * @return the coordinates
     */
    List<MenuCoordinate> getInRange();
}
