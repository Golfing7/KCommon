package com.golfing8.kcommon.menu.shape;

import lombok.Data;
import lombok.Setter;

@Data
public class MenuCoordinate {
    private final int x, y;

    public MenuCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "x:" + x + " y:" + y;
    }
}
