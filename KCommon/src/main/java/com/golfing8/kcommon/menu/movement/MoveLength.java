package com.golfing8.kcommon.menu.movement;

import com.golfing8.kcommon.menu.shape.MenuCoordinate;

public class MoveLength {
    private final MenuCoordinate[] coordinates;

    public MoveLength(MenuCoordinate[] coordinates){
        this.coordinates = coordinates;
    }

    public MenuCoordinate[] getCoordinates() {
        return coordinates;
    }
}
