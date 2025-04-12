package com.golfing8.kcommon.menu.shape;

import com.golfing8.kcommon.menu.MenuUtils;

import java.util.LinkedList;
import java.util.List;

public class LayoutShapeRectangle implements MenuLayoutShape {
    private List<MenuCoordinate> inRange;

    public LayoutShapeRectangle(MenuCoordinate low, MenuCoordinate high) {
        inRange = new LinkedList<>();

        for (int y = low.getY(); y <= high.getY(); y++) {
            for (int x = low.getX(); x <= high.getX(); x++) {
                inRange.add(new MenuCoordinate(x, y));
            }
        }
    }

    public LayoutShapeRectangle(int lowSlot, int highSlot) {
        this(MenuUtils.getCartCoordsFromSlot(lowSlot), MenuUtils.getCartCoordsFromSlot(highSlot));
    }

    public LayoutShapeRectangle(List<MenuCoordinate> coordinates) {
        this.inRange = new LinkedList<>(coordinates);
    }

    @Override
    public List<MenuCoordinate> getInRange() {
        return inRange;
    }
}
