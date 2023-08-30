package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.menu.movement.MoveLength;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;

/**
 * Util class used for some methods in the Menu classes
 */
public final class MenuUtils {
    /**
     * Returns the slot number of two coordinates in a cartesian coordinate system
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the slot number of two given coordinates
     */
    public static int getSlotFromCartCoords(int x, int y) {
        return (y - 1) * 9 + (x - 1);
    }

    public static MenuCoordinate getCartCoordsFromSlot(int slot) {
        int x = slot % 9 + 1;
        int y = slot / 9 + 1;
        return new MenuCoordinate(x, y);
    }

    /**
     * Gets the progression of slots from point slotStart to end slotTarget.
     *
     * @param slotStart     The starting point slot
     * @param slotTarget    The end point slot
     * @param allowDiagonal If we can move diagonally
     * @return The coordinate set between point a and point b
     */
    public static MoveLength calculateMovement(int slotStart, int slotTarget, boolean allowDiagonal) {
        MenuCoordinate start = getCartCoordsFromSlot(slotStart), target = getCartCoordsFromSlot(slotTarget);
        int xStart = start.getX(), yStart = start.getY(), xEnd = target.getX(), yEnd = target.getY();

        int xDist = Math.abs(xStart - xEnd);
        int yDist = Math.abs(yStart - yEnd);
        int minDistance = Math.min(xDist, yDist);
        int maxDistance = Math.max(xDist, yDist);

        int totalDistance = allowDiagonal ?
                minDistance + (maxDistance - minDistance) :
                xDist + yDist;

        MenuCoordinate[] toReturn = new MenuCoordinate[totalDistance + 1];

        toReturn[0] = new MenuCoordinate(xStart, yStart);

        for (int i = 1; i <= totalDistance; i++) {
            if (allowDiagonal) {
                if (xDist > 0 && yDist > 0) {
                    --xDist;
                    --yDist;
                    toReturn[i] = new MenuCoordinate(xStart > xEnd ? --xStart : ++xStart, yStart > yEnd ? --yStart : ++yStart);
                } else {
                    if (xDist >= yDist) {
                        --xDist;
                        toReturn[i] = new MenuCoordinate(xStart > xEnd ? --xStart : ++xStart, yStart);
                    } else {
                        --yDist;
                        toReturn[i] = new MenuCoordinate(xStart, yStart > yEnd ? --yStart : ++yStart);
                    }
                }
            } else {
                if (xDist >= yDist) {
                    --xDist;
                    toReturn[i] = new MenuCoordinate(xStart > xEnd ? --xStart : ++xStart, yStart);
                } else {
                    --yDist;
                    toReturn[i] = new MenuCoordinate(xStart, yStart > yEnd ? --yStart : ++yStart);
                }
            }
        }
        return new MoveLength(toReturn);
    }
}
