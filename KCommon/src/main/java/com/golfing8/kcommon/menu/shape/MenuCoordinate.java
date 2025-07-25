package com.golfing8.kcommon.menu.shape;

import com.golfing8.kcommon.menu.MenuUtils;
import lombok.Data;

@Data
public class MenuCoordinate {
    private final int x, y;

    public MenuCoordinate(int slot) {
        this.x = (slot % 9) + 1;
        this.y = (slot / 9) + 1;
    }

    public MenuCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int toSlot() {
        return MenuUtils.getSlotFromCartCoords(x, y);
    }

    public String toString() {
        return "x:" + x + " y:" + y;
    }
}
