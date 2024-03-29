package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.struct.random.RandomTestable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Represents a singular drop in a {@link DropTable}.
 */
@Getter
@AllArgsConstructor
public abstract class Drop<T> implements RandomTestable {
    /** The chance for this drop */
    private double chance;
    /** The group of the drop */
    private String dropGroup;
    /**
     * Gets the set of dropped objects.
     *
     * @return a list of drops.
     */
    public abstract List<T> getDrop();

    /**
     * Generates drops and gives them to the player.
     *
     * @param player the player.
     */
    public abstract void giveTo(Player player);

    /**
     * Drops the items at the given location.
     *
     * @param location the location.
     */
    public void dropAt(Location location) {}

    /**
     * If the drop is physical, that means that the {@link #dropAt(Location)} implementation exists.
     *
     * @return if this drop is physical.
     */
    public boolean isPhysical() {
        return false;
    }
}
