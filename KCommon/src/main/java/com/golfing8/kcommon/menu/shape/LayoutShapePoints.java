package com.golfing8.kcommon.menu.shape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a series of points as a shape.
 */
public class LayoutShapePoints implements MenuLayoutShape {
    private final List<MenuCoordinate> coordinates;
    public LayoutShapePoints(List<MenuCoordinate> coordinates) {
        this.coordinates = new ArrayList<>(coordinates);
    }

    @Override
    public List<MenuCoordinate> getInRange() {
        return Collections.unmodifiableList(coordinates);
    }
}
