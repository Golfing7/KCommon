package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.random.RandomTestable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a singular drop in a {@link DropTable}.
 */
@Getter
public abstract class Drop<T> implements RandomTestable {
    /** The key that was used to load this from the config */
    @Setter
    private String _key;
    /** The chance for this drop */
    private double chance;
    /** The display name of this drop */
    @Getter
    private @Nullable String displayName;
    /** The drop table this drop is linked to. Can be null */

    public Drop(double chance, @Nullable String displayName) {
        this.chance = chance;
        this.displayName = displayName;
    }
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
     * Gives the player this drop, or drops it at the given location.
     *
     * @param context the context.
     * @param location the location.
     */
    public void giveOrDropAt(DropContext context, Location location) {
        if (isPhysical()) {
            dropAt(context, location);
        } else {
            giveTo(context.getPlayer());
        }
    }

    /**
     * Drops the items at the given location.
     *
     * @param location the location.
     */
    public void dropAt(DropContext context, Location location) {}

    /**
     * If the drop is physical, that means that the {@link #dropAt(DropContext, Location)} implementation exists.
     *
     * @return if this drop is physical.
     */
    public boolean isPhysical() {
        return false;
    }
}
