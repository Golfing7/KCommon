package com.golfing8.kcommon.menu.shape;

import com.golfing8.kcommon.menu.MenuUtils;

import java.util.LinkedList;
import java.util.List;

public class ShapeRectangle implements MenuShape {
    private List<MenuCoordinate> inRange;

    public ShapeRectangle(MenuCoordinate low, MenuCoordinate high) {
        inRange = new LinkedList<>();

        for (int x = low.getX(); x <= high.getX(); x++) {
            for (int y = low.getY(); y <= high.getY(); y++) {
                inRange.add(new MenuCoordinate(x, y));
            }
        }
    }

    public ShapeRectangle(int lowSlot, int highSlot) {
        this(MenuUtils.getCartCoordsFromSlot(lowSlot), MenuUtils.getCartCoordsFromSlot(highSlot));
    }

    @Override
    public List<MenuCoordinate> getInRange() {
        return inRange;
    }
}
