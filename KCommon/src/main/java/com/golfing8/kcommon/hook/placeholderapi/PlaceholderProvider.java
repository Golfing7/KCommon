package com.golfing8.kcommon.hook.placeholderapi;

import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents functionality for requesting a placeholder throughout the KCommon plugin.
 */
public interface PlaceholderProvider {
    /**
     * The sub-key used when parsing the placeholder from the original.
     *
     * @return the provider key.
     */
    @Nonnull
    String getPlaceholderKey();

    /**
     * Called when a placeholder needs to be parsed. Takes the player as a context and the parameters and converts
     * it into a parsed placeholder.
     *
     * @param player the player the placeholder is parsing on.
     * @param parameters the parameters for the placeholder.
     * @return the parsed placeholder, or null if the default unimplemented placeholder should be returned.
     */
    @Nullable
    default String onPlaceholderRequest(Player player, String[] parameters) {
        return "Unimplemented";
    }

    /**
     * Called when a relational placeholder needs parsed. Takes both players as context and the parameters
     * and converts it into a parsed placeholder.
     *
     * @param p1 the first player.
     * @param p2 the second player.
     * @param parameters the parameters for the placeholder.
     * @return the parsed placeholder, or null if the default unimplemented placeholder should be returned.
     */
    @Nullable
    default String onRelationalPlaceholderRequest(Player p1, Player p2, String[] parameters) {
        return "Unimplemented";
    }
}
