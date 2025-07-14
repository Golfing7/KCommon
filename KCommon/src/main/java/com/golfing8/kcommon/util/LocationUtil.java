package com.golfing8.kcommon.util;

import com.golfing8.kcommon.struct.region.CuboidRegion;
import com.golfing8.kcommon.struct.region.Region;
import com.google.common.base.Preconditions;
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
     * @param range    the range.
     * @param action   the action.
     */
    public void forEachLocationInRange(Location location, int range, Consumer<Location> action) {
        forEachLocationInRange(location, range, range, range, action);
    }

    /**
     * Loops over all locations expanded from the given location and performs the action.
     *
     * @param location the location.
     * @param xRange   the X range.
     * @param yRange   the Y range.
     * @param zRange   the Z range.
     * @param action   the action.
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

    /**
     * Performs the given action once for every block position within the given positions
     *
     * @param pos1   the first position
     * @param pos2   the second position
     * @param action the action to perform
     */
    public void forEachLocationInRange(Location pos1, Location pos2, Consumer<Location> action) {
        Preconditions.checkArgument(pos1.getWorld() == pos2.getWorld(), "Worlds do not equal");

        // Piggyback off of cuboid region
        Region region = new CuboidRegion(pos1, pos2);
        region.forEach(vec -> action.accept(vec.toLocation(pos1.getWorld())));
    }
}
