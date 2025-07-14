package com.golfing8.kcommon.menu.movement;

import com.golfing8.kcommon.menu.shape.MenuCoordinate;

import java.util.List;

public class MoveLength {
    private final List<MenuCoordinate> coordinates;

    public MoveLength(List<MenuCoordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public List<MenuCoordinate> getCoordinates() {
        return coordinates;
    }
}
