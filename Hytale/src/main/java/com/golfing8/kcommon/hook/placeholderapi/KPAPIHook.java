package com.golfing8.kcommon.hook.placeholderapi;

import com.golfing8.kcommon.KPlugin;
import at.helpch.placeholderapi.expansion.PlaceholderExpansion;
import at.helpch.placeholderapi.expansion.Relational;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers a plugin's module placeholders with the Hytale build of PlaceholderAPI,
 * as {@code %<plugin>_<module>_<placeholder>%}.
 */
public class KPAPIHook extends PlaceholderExpansion implements Relational {
    private final KPlugin plugin;
    private final Map<String, PlaceholderProvider> providerMap = new HashMap<>();

    public KPAPIHook(KPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers the provider, replacing any provider with the same key.
     *
     * @param provider the provider.
     */
    public void registerProvider(@NotNull PlaceholderProvider provider) {
        this.providerMap.put(provider.getPlaceholderKey(), provider);
    }

    /**
     * Unregisters the provider.
     *
     * @param provider the provider.
     */
    public void unregisterProvider(@NotNull PlaceholderProvider provider) {
        this.providerMap.remove(provider.getPlaceholderKey());
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getPluginName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return "Golfing8";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getManifest().getVersion().toString();
    }

    @Override
    public boolean persist() {
        return true;
    }

    private @Nullable PlaceholderProvider provider(String[] split) {
        return split.length == 0 ? null : providerMap.get(split[0]);
    }

    private static String[] rest(String[] split) {
        String[] params = new String[split.length - 1];
        System.arraycopy(split, 1, params, 0, params.length);
        return params;
    }

    @Override
    public @Nullable String onPlaceholderRequest(PlayerRef player, @NotNull String params) {
        if (params.isEmpty())
            return "Placeholder is empty";

        String[] split = params.split("_");
        PlaceholderProvider provider = provider(split);
        if (provider == null)
            return String.format("%s not found", params);

        String result = provider.onPlaceholderRequest(player, rest(split));
        return result == null ? String.format("%s not found", params) : result;
    }

    @Override
    public String onPlaceholderRequest(PlayerRef one, PlayerRef two, String params) {
        if (params == null || params.isEmpty())
            return "Placeholder is empty";

        String[] split = params.split("_");
        PlaceholderProvider provider = provider(split);
        if (provider == null)
            return String.format("%s not found", params);

        String result = provider.onRelationalPlaceholderRequest(one, two, rest(split));
        return result == null ? String.format("%s not found", params) : result;
    }
}
