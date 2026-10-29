package com.golfing8.kcommon.hook.placeholderapi;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Map;

/**
 * Provides placeholders to PlaceholderAPI under a key, usually a module's name.
 */
public interface PlaceholderProvider {
    /**
     * Computes a placeholder for a player.
     */
    interface PlaceholderFunction {
        String get(@Nullable PlayerRef player, String[] args);
    }

    /**
     * Computes a placeholder relating two players.
     */
    interface RelPlaceholderFunction {
        String get(PlayerRef player, PlayerRef other, String[] args);
    }

    /**
     * Gets the key placeholders of this provider are registered under, usually the module name.
     *
     * @return the key.
     */
    @Nonnull
    String getPlaceholderKey();

    /**
     * Gets the placeholders of this provider.
     *
     * @return the placeholders.
     */
    Map<KPlaceholderDefinition, PlaceholderFunction> getPlaceholders();

    /**
     * Gets the relational placeholders of this provider.
     *
     * @return the placeholders.
     */
    Map<KPlaceholderDefinition, RelPlaceholderFunction> getRelationalPlaceholders();

    /**
     * Adds a placeholder.
     *
     * @param definition the definition.
     * @param function   the function computing it.
     */
    void addPlaceholder(KPlaceholderDefinition definition, PlaceholderFunction function);

    /**
     * Adds a relational placeholder.
     *
     * @param definition the definition.
     * @param function   the function computing it.
     */
    void addRelPlaceholder(KPlaceholderDefinition definition, RelPlaceholderFunction function);

    /**
     * Resolves a placeholder for the player.
     *
     * @param player     the player, or null.
     * @param parameters the placeholder split on '_', without the provider key.
     * @return the value.
     */
    @Nullable
    default String onPlaceholderRequest(@Nullable PlayerRef player, String[] parameters) {
        Map.Entry<KPlaceholderDefinition, PlaceholderFunction> bestEntry = best(getPlaceholders(), parameters);
        if (bestEntry == null)
            return "Unimplemented";

        int matches = bestEntry.getKey().getSplitLabel().length;
        return bestEntry.getValue().get(player, Arrays.copyOfRange(parameters, matches, parameters.length));
    }

    /**
     * Resolves a relational placeholder.
     *
     * @param p1         the first player.
     * @param p2         the second player.
     * @param parameters the placeholder split on '_', without the provider key.
     * @return the value.
     */
    @Nullable
    default String onRelationalPlaceholderRequest(PlayerRef p1, PlayerRef p2, String[] parameters) {
        Map.Entry<KPlaceholderDefinition, RelPlaceholderFunction> bestEntry = best(getRelationalPlaceholders(), parameters);
        if (bestEntry == null)
            return "Unimplemented";

        int matches = bestEntry.getKey().getSplitLabel().length;
        return bestEntry.getValue().get(p1, p2, Arrays.copyOfRange(parameters, matches, parameters.length));
    }

    /**
     * Finds the definition that matches the most of the parameters, the same way the Bukkit provider does.
     */
    @Nullable
    static <F> Map.Entry<KPlaceholderDefinition, F> best(Map<KPlaceholderDefinition, F> definitions, String[] parameters) {
        Map.Entry<KPlaceholderDefinition, F> bestEntry = null;
        int mostMatches = 0;
        for (Map.Entry<KPlaceholderDefinition, F> entry : definitions.entrySet()) {
            int matchCount = entry.getKey().getMatchCount(parameters);
            if (matchCount < entry.getKey().getSplitLabel().length || matchCount <= mostMatches)
                continue;

            mostMatches = matchCount;
            bestEntry = entry;
        }
        return bestEntry;
    }
}
