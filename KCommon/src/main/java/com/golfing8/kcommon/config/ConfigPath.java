package com.golfing8.kcommon.config;

import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.config.commented.MConfiguration;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.Modules;
import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A representation of a path in a config.
 */
@AllArgsConstructor
public class ConfigPath {
    /**
     * The module where the path is pointing
     */
    private final @Nullable String moduleName;
    /**
     * The config where the path is pointing
     */
    private final @Nullable String configName;
    /**
     * The path in the config where this is pointing
     */
    private final String path;

    /**
     * Enumerates all possible config entries.
     * <p>
     * If {@link #moduleName} is specified, this will override the value.
     * </p>
     *
     * @param context the context of the path.
     * @return the entries.
     */
    public List<ConfigEntry> enumerate(Module context) {
        List<ConfigEntry> entries = new ArrayList<>();
        for (MConfiguration config : context.getConfigs().values()) {
            entries.addAll(enumerate(config));
        }
        return entries;
    }

    /**
     * Enumerates all possible config entries.
     *
     * @param context the context of the path.
     * @return the entries.
     */
    public List<ConfigEntry> enumerate(ConfigurationSection context) {
        if (context.contains(path)) {
            return Collections.singletonList(new ConfigEntry(context, path));
        }
        return Collections.emptyList();
    }

    /**
     * Enumerates all possible config entries.
     * <p>
     * Returns an empty list if the module name is not specified.
     * </p>
     *
     * @return the entries.
     */
    public List<ConfigEntry> enumerate() {
        if (moduleName == null) {
            List<ConfigEntry> entries = new ArrayList<>();
            for (Module module : Modules.getAll()) {
                entries.addAll(enumerate(module));
            }
            return entries;
        }

        Module module = Modules.getModule(moduleName);
        if (module == null)
            return Collections.emptyList();

        if (configName == null) {
            return enumerate(module);
        }

        MConfiguration configuration = module.getConfig(configName);
        return enumerate(configuration);
    }

    /**
     * Parses a config path with the given context.
     *
     * @param moduleContext the module context.
     * @param configContext the config context.
     * @param path          the path.
     * @return the config path.
     */
    public static ConfigPath parseWithContext(@Nullable Module moduleContext, @Nullable Configuration configContext, String path) {
        String[] parts = path.split("[:;]");
        if (parts.length == 1) {
            return new ConfigPath(moduleContext != null ? moduleContext.getModuleName() : null, configContext != null ? configContext.getFileNameNoExtension() : null, parts[0]);
        }

        if (parts.length == 2) {
            return new ConfigPath(moduleContext != null ? moduleContext.getModuleName() : null, parts[0], parts[1]);
        }

        return new ConfigPath(parts[0], parts[1], parts[2]);
    }

    /**
     * Parses the config path from the given string.
     *
     * @param path the path.
     * @return the parsed path.
     */
    public static ConfigPath parse(String path) {
        String[] parts = path.split("[:;]");
        if (parts.length == 1) {
            return new ConfigPath(null, null, parts[0]);
        }

        if (parts.length == 2) {
            return new ConfigPath(null, parts[0], parts[1]);
        }

        return new ConfigPath(parts[0], parts[1], parts[2]);
    }
}
