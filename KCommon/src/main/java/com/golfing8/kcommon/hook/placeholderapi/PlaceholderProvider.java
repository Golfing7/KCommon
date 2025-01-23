package com.golfing8.kcommon.hook.placeholderapi;

import lombok.var;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

/**
 * Represents functionality for requesting a placeholder throughout the KCommon plugin.
 */
public interface PlaceholderProvider {
    interface PlaceholderFunction {
        String get(OfflinePlayer player, String[] args);
    }
    interface RelPlaceholderFunction {
        String get(OfflinePlayer player, Player other, String[] args);
    }

    /**
     * The sub-key used when parsing the placeholder from the original.
     *
     * @return the provider key.
     */
    @Nonnull
    String getPlaceholderKey();

    /**
     * Gets all registered placeholders
     * @return the placeholders
     */
    Map<KPlaceholderDefinition, PlaceholderFunction> getPlaceholders();
    /**
     * Gets all registered relational placeholders
     * @return the placeholders
     */
    Map<KPlaceholderDefinition, RelPlaceholderFunction> getRelationalPlaceholders();

    /**
     * Registers a new simple placeholder for this provider.
     *
     * @param definition the definition of the placeholder.
     * @param function its provider function.
     */
    void addPlaceholder(KPlaceholderDefinition definition, PlaceholderFunction function);

    /**
     * Registers a relational placeholder for this provider.
     *
     * @param definition the definition of the placeholder.
     * @param function its provider function.
     */
    void addRelPlaceholder(KPlaceholderDefinition definition, RelPlaceholderFunction function);

    /**
     * Called when a placeholder needs to be parsed. Takes the player as a context and the parameters and converts
     * it into a parsed placeholder.
     *
     * @param player the player the placeholder is parsing on.
     * @param parameters the parameters for the placeholder.
     * @return the parsed placeholder, or null if the default unimplemented placeholder should be returned.
     */
    @Nullable
    default String onPlaceholderRequest(OfflinePlayer player, String[] parameters) {
        Map.Entry<KPlaceholderDefinition, PlaceholderFunction> bestEntry = null;
        int mostMatches = 0;
        for (var entry : getPlaceholders().entrySet()) {
            int matchCount = entry.getKey().getMatchCount(parameters);
            if (matchCount < entry.getKey().getSplitLabel().length) // If we didn't match the full label, don't do anything.
                continue;

            if (matchCount <= mostMatches) // Only update if we beat our previous best.
                continue;

            mostMatches = matchCount;
            bestEntry = entry;
        }

        if (bestEntry == null) {
            return "Unimplemented";
        }
        return bestEntry.getValue().get(player, Arrays.copyOfRange(parameters, mostMatches, parameters.length));
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
        Map.Entry<KPlaceholderDefinition, RelPlaceholderFunction> bestEntry = null;
        int mostMatches = 0;
        for (var entry : getRelationalPlaceholders().entrySet()) {
            int matchCount = entry.getKey().getMatchCount(parameters);
            if (matchCount < entry.getKey().getSplitLabel().length) // If we didn't match the full label, don't do anything.
                continue;

            if (matchCount <= mostMatches) // Only update if we beat our previous best.
                continue;

            mostMatches = matchCount;
            bestEntry = entry;
        }

        if (bestEntry == null) {
            return "Unimplemented";
        }
        return bestEntry.getValue().get(p1, p2, Arrays.copyOfRange(parameters, mostMatches, parameters.length));
    }
}
