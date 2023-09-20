package com.golfing8.kcommon.menu.shape;

import lombok.Data;
import lombok.Setter;

@Data
public class MenuCoordinate {
    private final int x, y;
    /** The page this coordinate belongs to */
    @Setter
    private int page = -1;

    public MenuCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public MenuCoordinate(int x, int y, int page) {
        this.x = x;
        this.y = y;
        this.page = page;
    }

    public String toString() {
        return "x:" + x + " y:" + y;
    }
}
