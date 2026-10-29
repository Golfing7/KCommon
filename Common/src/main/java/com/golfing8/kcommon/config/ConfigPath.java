package com.golfing8.kcommon.config;

import com.golfing8.kcommon.module.AbstractModule;
import com.golfing8.kcommon.module.ModuleRegistry;
import lombok.AllArgsConstructor;
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
    public List<ConfigEntry> enumerate(AbstractModule<?> context) {
        List<ConfigEntry> entries = new ArrayList<>();
        for (KFileConfig config : context.getConfigs().values()) {
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
    public List<ConfigEntry> enumerate(KSection context) {
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
            for (AbstractModule<?> module : ModuleRegistry.getAll()) {
                entries.addAll(enumerate(module));
            }
            return entries;
        }

        AbstractModule<?> module = ModuleRegistry.getModule(moduleName);
        if (module == null)
            return Collections.emptyList();

        if (configName == null) {
            return enumerate(module);
        }

        KFileConfig configuration = module.getConfig(configName);
        return enumerate(configuration);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (moduleName != null)
            builder.append(moduleName).append(":");
        if (configName != null)
            builder.append(configName).append(":");
        builder.append(path);
        return builder.toString();
    }

    /**
     * Parses a config path with the given context.
     *
     * @param moduleContext the module context.
     * @param configContext the config context.
     * @param path          the path.
     * @return the config path.
     */
    public static ConfigPath parseWithContext(@Nullable AbstractModule<?> moduleContext, @Nullable KFileConfig configContext, String path) {
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
