package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;

import java.util.function.Consumer;

/**
 * Utilities relating to locations.
 */
@UtilityClass
public class LocationUtil {
    /**
     * Loops over all locations expanded from the given location and performs the action.
     *
     * @param location the location.
     * @param range the range.
     * @param action the action.
     */
    public void forEachLocationInRange(Location location, int range, Consumer<Location> action) {
        forEachLocationInRange(location, range, range, range, action);
    }

    /**
     * Loops over all locations expanded from the given location and performs the action.
     *
     * @param location the location.
     * @param xRange the X range.
     * @param yRange the Y range.
     * @param zRange the Z range.
     * @param action the action.
     */
    public void forEachLocationInRange(Location location, int xRange, int yRange, int zRange, Consumer<Location> action) {
        for (int x = -xRange; x <= xRange; x++) {
            for (int y = -yRange; y <= yRange; y++) {
                for (int z = -zRange; z <= zRange; z++) {
                    action.accept(location.clone().add(x, y, z));
                }
            }
        }
    }
}
