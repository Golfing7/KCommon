package com.golfing8.kcommon.menu.shape;

import com.golfing8.kcommon.menu.MenuUtils;
import lombok.Data;

/**
 * A coordinate within a menu.
 * <p>
 * X and Y coordinates start at the top left of the menu at 1,1
 * </p>
 */
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

    /**
     * Converts this coordinate to a slot number
     * Assumes normal 9 wide inventory
     *
     * @return the slot
     */
    public int toSlot() {
        return MenuUtils.getSlotFromCartCoords(x, y);
    }

    /**
     * Converts to a string
     *
     * @return the string
     */
    public String toString() {
        return "x:" + x + " y:" + y;
    }
}
