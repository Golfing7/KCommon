package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.struct.random.RandomTestable;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.entity.entities.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Represents a singular drop in a {@link DropTable}.
 */
@Getter
@Setter
@CASerializable.Options(typeResolverEnum = Drop.Type.class)
public class Drop<T> implements RandomTestable, CASerializable {
    /**
     * The drop types a config can choose with its {@code type} key
     */
    @AllArgsConstructor
    public enum Type implements CASerializable.TypeResolver {
        ITEMDROP(ItemDrop.class, "ItemDrop"),
        COMMANDDROP(CommandDrop.class, "CommandDrop");

        @Getter
        private final Class<? extends CASerializable> type;
        private final String id;

        @Override
        public String toString() {
            return id;
        }
    }

    /**
     * The key that was used to load this from the config
     */
    private String _key;
    /**
     * The chance for this drop
     */
    private double chance;
    /**
     * The display name of this drop
     */
    private @Nullable String displayName;
    /**
     * The maximum boost that this drop can use
     */
    private double maxBoost = 1.0;
    /**
     * All keys and objects defined by the config. Can be null if this drop wasn't defined by a config.
     */
    private transient @Nullable Map<String, Object> configDefinition;

    protected Drop() {
    }

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
    public List<T> getDrop() {
        throw new UnsupportedOperationException();
    }

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
    public void giveTo(DropContext context) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gives the player this drop, or drops it at the given location.
     *
     * @param context  the context.
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
    public void dropAt(DropContext context, Location location) {
    }

    /**
     * If the drop is physical, that means that the {@link #dropAt(DropContext, Location)} implementation exists.
     *
     * @return if this drop is physical.
     */
    public boolean isPhysical() {
        return false;
    }
}
