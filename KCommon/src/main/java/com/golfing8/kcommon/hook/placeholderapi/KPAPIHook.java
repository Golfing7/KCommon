package com.golfing8.kcommon.hook.placeholderapi;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * This class controls the hook into PlaceholderAPI for KCommon.
 */
@RequiredArgsConstructor
public class KPAPIHook extends PlaceholderExpansion implements Relational {
    /**
     * The plugin.
     */
    private final Plugin plugin;
    /**
     * The provider map, used to map parameters to their given provider.
     */
    private final Map<String, PlaceholderProvider> providerMap = new HashMap<>();

    /**
     * Registers a sub-provider for the PAPI hook.
     * If a provider is already registered, the old is replaced.
     *
     * @param provider the provider to register.
     */
    public void registerProvider(@Nonnull PlaceholderProvider provider) {
        this.providerMap.put(provider.getPlaceholderKey(), provider);
    }

    /**
     * Unregisters the given provider for this PAPI hook.
     *
     * @param provider the provider to unregister.
     */
    public void unregisterProvider(@Nonnull PlaceholderProvider provider) {
        this.providerMap.remove(provider.getPlaceholderKey());
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return "Golfing8";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if(params.isEmpty())
            return "Placeholder is empty";

        //Try looking for the provider.
        String[] parameterSplit = params.split("_");
        if(parameterSplit.length == 0)
            return "Placeholder is empty";

        //Create the new params array.
        PlaceholderProvider provider = this.providerMap.get(parameterSplit[0]);
        if (provider == null) {
            return String.format("%s not found", params);
        }

        String[] newParams = new String[parameterSplit.length - 1];
        System.arraycopy(parameterSplit, 1, newParams, 0, newParams.length);
        String result = provider.onPlaceholderRequest(player, newParams);
        return result == null ? String.format("%s not found", params) : result;
    }

    @Override
    public String onPlaceholderRequest(Player player, Player player1, String s) {
        if(s == null || s.isEmpty())
            return "Placeholder is empty";

        //Try looking for the provider.
        String[] parameterSplit = s.split(" +");
        if(parameterSplit.length == 0)
            return "Placeholder is empty";

        //Create the new params array.
        String[] newParams = new String[parameterSplit.length - 1];
        System.arraycopy(parameterSplit, 1, newParams, 0, newParams.length);
        PlaceholderProvider provider = this.providerMap.get(parameterSplit[0]);
        String result = provider.onRelationalPlaceholderRequest(player, player1, newParams);
        return result == null ? String.format("%s not found", s) : result;
    }
}
