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
@Setter
public abstract class Drop<T> implements RandomTestable {
    /** The key that was used to load this from the config */
    private String _key;
    /** The chance for this drop */
    private double chance;
    /** The display name of this drop */
    private @Nullable String displayName;
    /** The maximum boost that this drop can use */
    private double maxBoost;

    /** The drop table this drop is linked to. Can be null */
    public Drop(double chance, @Nullable String displayName, double maxBoost) {
        this.chance = chance;
        this.displayName = displayName;
        this.maxBoost = maxBoost;
    }

    public Drop(double chance, @Nullable String displayName) {
        this(chance, displayName, Double.MAX_VALUE);
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
    public void giveTo(Player player) {
        giveTo(new DropContext(player));
    }

    /**
     * Generates drops and gives them to the player.
     *
     * @param context the context.
     */
    public abstract void giveTo(DropContext context);

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
