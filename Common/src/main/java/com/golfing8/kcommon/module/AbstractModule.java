package com.golfing8.kcommon.module;

import com.golfing8.kcommon.util.StringUtilCore;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.KPluginContext;
import com.golfing8.kcommon.config.KFileConfig;
import com.golfing8.kcommon.config.generator.ConfigClassSource;
import com.golfing8.kcommon.config.generator.ConfigClassWrapper;
import com.golfing8.kcommon.data.DataManagerContainer;
import com.golfing8.kcommon.platform.Platform;
import com.golfing8.kcommon.struct.KNamespacedKey;
import com.golfing8.kcommon.struct.helper.terminable.TerminableConsumer;
import com.golfing8.kcommon.struct.helper.terminable.composite.CompositeClosingException;
import com.golfing8.kcommon.struct.helper.terminable.composite.CompositeTerminable;
import com.golfing8.kcommon.struct.profiler.HighLowAverageProfiler;
import com.golfing8.kcommon.struct.profiler.IMethodProfiler;
import com.golfing8.kcommon.util.FileUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The platform independent part of a module.
 * <p>
 * Each platform has its own {@code Module} class extending this one. Shared code works with this type,
 * plugin code keeps extending {@code Module}. The platform class fills in the hooks for listeners,
 * commands, language files and placeholders.
 * </p>
 *
 * @param <C> the type of config this platform's modules use.
 */
public abstract class AbstractModule<C extends KFileConfig> implements TerminableConsumer {
    /**
     * The owning plugin of this module.
     */
    private final KPluginContext plugin;
    /**
     * The name of this module.
     */
    @Getter
    private final String moduleName;
    /**
     * The namespace key of this module
     */
    @Getter
    private final KNamespacedKey namespacedKey;
    /**
     * The module dependencies for this module.
     */
    @Getter
    private final Set<String> moduleDependencies;
    /**
     * The plugin dependencies for this module
     */
    @Getter
    private final Set<String> pluginDependencies;
    /**
     * The logger used for this module
     */
    @Getter
    private final Logger logger;
    /**
     * The permission prefix
     */
    @Getter
    private final String permissionPrefix;
    /**
     * The module info annotation on this class
     */
    @Getter
    private final @Nullable ModuleInfo moduleInfo;
    /**
     * The terminable for this life cycle of the module.
     */
    @Getter
    private final CompositeTerminable terminable = CompositeTerminable.create();
    /**
     * If this module is enabled or not. This is simply the module's current state.
     * This has no indication of the plugin's enabled state for this module in its manifest.
     */
    @Getter
    private transient boolean enabled;
    /**
     * Used to indicate that the {@link #disable()} method was called during the execution of {@link #onEnable()}
     */
    private transient boolean prematureDisable;
    /**
     * If this module is in debug mode.
     */
    @Getter
    @Setter
    private boolean moduleDebug;
    /**
     * The path to the data folder
     */
    private Path dataFolder;
    /**
     * Other configs that are linked to this module.
     */
    @Getter
    private final Map<String, C> configs;
    /**
     * The config wrapper backing this module.
     */
    @Getter
    private ConfigClassWrapper configWrapper;
    /**
     * The module's profiler
     */
    @Getter
    private final IMethodProfiler profiler;

    protected AbstractModule(KPluginContext plugin, String moduleName, Set<String> moduleDependencies, Set<String> pluginDependencies) {
        this.plugin = plugin;
        this.namespacedKey = new KNamespacedKey(plugin, moduleName);
        this.moduleName = moduleName;
        this.moduleDependencies = new HashSet<>(moduleDependencies);
        this.pluginDependencies = new HashSet<>(pluginDependencies);
        this.configs = new ConcurrentHashMap<>();
        this.logger = new ModuleLogger(this);
        this.permissionPrefix = plugin.getPluginName() + "." + this.moduleName;
        this.moduleInfo = null;
        this.profiler = new HighLowAverageProfiler(this.moduleName);
        register();
    }

    protected AbstractModule() {
        ModuleInfo info = this.getClass().getAnnotation(ModuleInfo.class);
        if (info == null)
            throw new IllegalArgumentException("Module info missing for class " + getClass().getName() + "!");

        this.plugin = Platform.get().getProvidingPlugin(this.getClass());
        this.moduleName = info.name();
        this.namespacedKey = new KNamespacedKey(this.plugin, this.moduleName);
        this.moduleDependencies = new HashSet<>(Arrays.asList(info.moduleDependencies()));
        this.pluginDependencies = new HashSet<>(Arrays.asList(info.pluginDependencies()));
        this.configs = new HashMap<>();
        this.logger = new ModuleLogger(this);
        this.permissionPrefix = plugin.getPluginName() + "." + this.moduleName;
        this.moduleInfo = info;
        this.profiler = new HighLowAverageProfiler(this.moduleName);
        register();
    }

    private void register() {
        // Try to register this module to the registry.
        if (ModuleRegistry.moduleExists(this.getNamespacedKey())) {
            getLogger().warning(String.format("Module already exists with name %s!", this.getModuleName()));
            return;
        }
        ModuleRegistry.registerModule(this);
    }

    /**
     * Gets the plugin that owns this module. Each platform narrows this to its {@code KPlugin}.
     *
     * @return the plugin.
     */
    public KPluginContext getPlugin() {
        return plugin;
    }

    @Override
    public @NotNull <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.terminable.bind(terminable);
    }

    public @NotNull C getMainConfig() {
        return configs.get("config");
    }

    /**
     * Writes a changed {@code @Conf} field back to this module's main config and saves it.
     *
     * @param fieldName the name of the static config field.
     * @param value     its new value.
     */
    public final void saveConfigValue(String fieldName, Object value) {
        ConfigTypeRegistry.setInConfig(getMainConfig(), StringUtilCore.camelToYaml(fieldName), value);
        getMainConfig().save();
    }

    /**
     * Gets the config under the given key, or creates one if it wasn't previously registered.
     *
     * @param key the key of the config.
     * @return the config.
     */
    public @NotNull C getConfig(String key) {
        C configuration = configs.get(key);
        if (configuration == null) {
            configuration = loadConfig(plugin.getPluginDataFolder().resolve(moduleName).resolve(key + ".yml"), true);
            configs.put(key, configuration);
        }
        return configuration;
    }

    /**
     * Reloads this module.
     */
    public final void reload() {
        this.shutdown();
        this.initialize();
    }

    /**
     * Reloads this module and every module that depends on it.
     */
    public final void reloadWithDependencies() {
        this.reload();

        for (AbstractModule<?> module : ModuleRegistry.getAll()) {
            if (module.getModuleDependencies().contains(this.getModuleName()))
                module.reloadWithDependencies();
        }
    }

    /**
     * Registers this module and enables it if its plugin's manifest allows it.
     */
    public final void initialize() {
        profiler.start("initialize");
        ModuleRegistry.registerModule(this);
        if (getModuleManifest().loadModule(this)) {
            if (!enable()) {
                getLogger().info("Failed to enable module.");
                return;
            }
            getLogger().info("Loaded and enabled module.");
        } else {
            getLogger().info("Loaded and disabled module.");
        }
        profiler.stop("initialize");
    }

    /**
     * Enables this module.
     *
     * @return true if the module was enabled.
     */
    public final boolean enable() {
        // Don't enable twice
        if (enabled)
            return false;

        try {
            profiler.start("enable");
            try {
                profiler.start("loadConfigs");
                this.loadConfigs();
                profiler.stop("loadConfigs");
                profiler.start("loadLanguage");
                this.loadLanguage();
                profiler.stop("loadLanguage");
            } catch (Throwable thr) {
                getLogger().log(Level.SEVERE, "Failed to load due to config error!", thr);
                return false;
            }

            this.registerPlatformHooks();

            this.prematureDisable = false;
            try {
                profiler.start("onEnable");
                this.onEnable();
                profiler.stop("onEnable");
            } catch (Throwable thr) {
                getLogger().log(Level.SEVERE, "Module failed to enable!", thr);
                return false;
            }

            // It's possible that the implementation of onEnable called disable()
            if (this.prematureDisable) {
                return false;
            }

            this.afterEnable();
            this.enabled = true;
            return true;
        } finally {
            profiler.stop("enable");
        }
    }

    /**
     * Turns this module on or off and remembers the choice in its plugin's module manifest.
     *
     * @param active true to enable the module, false to disable it.
     * @return false if the module was already in that state.
     */
    public final boolean setActive(boolean active) {
        if (getModuleManifest().isActive(this) == active && this.enabled == active)
            return false;

        getModuleManifest().setActive(this, active);
        if (active) {
            this.enable();
        } else {
            this.disable();
        }
        return true;
    }

    /**
     * Disables this module and removes it from the registry.
     */
    public final void shutdown() {
        this.disable();
        ModuleRegistry.unregisterModule(this);
    }

    /**
     * Disables this module.
     */
    public final void disable() {
        // Don't disable self twice.
        if (!enabled) {
            prematureDisable = true;
            return;
        }

        try {
            profiler.start("disable");
            this.beforeDisable();

            try {
                this.terminable.close();
            } catch (CompositeClosingException exc) {
                getLogger().log(Level.SEVERE, "Failed to close terminable!", exc);
                exc.printAllStackTraces();
            }
            try {
                this.onDisable();
            } catch (Throwable thr) {
                getLogger().log(Level.SEVERE, "Module failed to disable!", thr);
                return;
            }
            // If this module supports data managers, shut them down.
            if (this instanceof DataManagerContainer) {
                ((DataManagerContainer) this).shutdownDataManagers();
            }

            this.unregisterPlatformHooks();

            this.configWrapper.unregister();
            this.configs.clear();
            this.enabled = false;
        } finally {
            profiler.stop("disable");
        }
    }

    /**
     * Loads all configs in the given group, a sub folder of this module's data folder.
     *
     * @param groupName the name of the group.
     * @return the configs in the group.
     */
    protected final List<C> loadConfigGroup(String groupName) {
        Path directoryPath = this.dataFolder.resolve(groupName);
        if (!Files.isDirectory(directoryPath)) {
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.list(directoryPath)) {
            List<C> toReturn = new ArrayList<>();
            paths.forEach(path -> {
                toReturn.add(loadConfig(path, false));
            });
            return toReturn;
        } catch (IOException exc) {
            getLogger().log(Level.WARNING, "Failed to load config group: " + groupName, exc);
            return Collections.emptyList();
        }
    }

    /**
     * Loads every config of this module and the values linked to them.
     */
    private void loadConfigs() {
        // Get the config wrapper ready for loading
        this.configWrapper = new ConfigClassWrapper(null, this.getClass(), this);

        // Load extra sources.
        if (this.moduleInfo != null) {
            for (Class<? extends ConfigClassSource> source : this.moduleInfo.configSources()) {
                this.configWrapper.addSource(source);
            }
        }

        this.configWrapper.setConfigMappingEnabled(true);
        this.configWrapper.setRequireAnnotation(true);
        profiler.start("initConfig");
        this.configWrapper.initConfig();
        profiler.stop("initConfig");

        //Create the parent directory.
        this.dataFolder = plugin.getPluginDataFolder().resolve(moduleName);
        boolean firstLoad = Files.notExists(dataFolder);
        try {
            Files.createDirectories(dataFolder.getParent());
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to create parent directory for config file in module %s!", getModuleName()), exc);
        }

        // First copy everything over from our module's directory.
        if (firstLoad) {
            try {
                FileUtil.copyJarElements(plugin.getPluginJar(), this.moduleName, dataFolder);
            } catch (IOException exc) {
                getLogger().log(Level.SEVERE, "Failed to copy module files!", exc);
            }
        }

        this.configs.put("config", loadConfig(dataFolder.resolve("config.yml"), false));
        // Load the EXPECTED configs
        for (String expectedConfig : this.configWrapper.getConfigNames()) {
            // Don't re-register the main config
            if (expectedConfig.equals("config"))
                continue;

            this.configs.put(expectedConfig, loadConfig(dataFolder.resolve(expectedConfig + ".yml"), false));
        }
        // Load the configs that are simply present.
        try (Stream<Path> files = Files.list(dataFolder)) {
            files.forEach(path -> {
                if (Files.isDirectory(path))
                    return;

                if (!path.getFileName().toString().endsWith(".yml"))
                    return;

                String configName = path.getFileName().toString().replace(".yml", "");
                if (this.configs.containsKey(configName))
                    return;

                this.configs.put(configName, loadConfig(path, false));
            });
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to list files in parent directory in module %s!", getModuleName()), exc);
        }
        // Load @Conf values.
        for (C configuration : new ArrayList<>(this.configs.values())) {
            // Load with the config wrapper.
            boolean modded = this.configWrapper.loadValues(configuration);
            if (modded) {
                configuration.save();
            }
        }

        // Finally, load the debug flag
        this.moduleDebug = getMainConfig().getBoolean("module-debug", false);
    }

    /**
     * Loads the config at the given path, with the matching resource in the plugin jar as its defaults.
     *
     * @param configPath the config path.
     * @param loadValues if the {@link com.golfing8.kcommon.config.generator.Conf} values should be loaded into the config.
     * @return the config.
     */
    private C loadConfig(Path configPath, boolean loadValues) {
        String resourcePath = "/" + getModuleName() + "/" + configPath.getFileName().toString();
        profiler.start("loadConfig_" + resourcePath);

        //Create the parent directory.
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to create parent directory for config file in module %s!", getModuleName()), exc);
        }

        boolean defaultConfig = configPath.endsWith("config.yml");
        // Test the new location for the resource, otherwise fallback on the old one.
        if (this.plugin.getClass().getResource(resourcePath) == null) {
            resourcePath = "/" + (defaultConfig ? moduleName + ".yml" : configPath.getFileName().toString());
        }

        byte[] defaults = null;
        try (InputStream resource = this.plugin.getClass().getResourceAsStream(resourcePath)) {
            //Check that the resource exists
            if (resource == null) {
                if (Files.notExists(configPath))
                    Files.createFile(configPath);
            } else {
                ByteArrayOutputStream streamCloner = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                while ((read = resource.read(buffer)) != -1) {
                    streamCloner.write(buffer, 0, read);
                }
                defaults = streamCloner.toByteArray();
                if (Files.notExists(configPath)) {
                    Files.write(configPath, defaults, StandardOpenOption.CREATE);
                }
            }
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to load config %s for module %s. Is it missing? (Checked under plugin %s)",
                    configPath.getFileName().toString(),
                    getModuleName(),
                    getPlugin().getPluginName()), exc);
        }

        C toReturn = createConfig(configPath, defaults);
        profiler.start("loadConfig_" + resourcePath + "_load");
        loadConfigFile(toReturn);
        profiler.stop("loadConfig_" + resourcePath + "_load");

        if (loadValues) {
            // Load with the config wrapper.
            profiler.start("loadConfig_" + resourcePath + "_loadValues");
            boolean modded = this.configWrapper.loadValues(toReturn);
            profiler.stop("loadConfig_" + resourcePath + "_loadValues");
            if (modded) {
                toReturn.save();
            }
        }
        profiler.stop("loadConfig_" + resourcePath);
        return toReturn;
    }

    /**
     * Gets the module manifest of the plugin that owns this module.
     *
     * @return the manifest.
     */
    protected abstract ModuleManifest getModuleManifest();

    /**
     * Creates the platform's config object for the given file. The file is loaded right after.
     *
     * @param configPath the path of the config file.
     * @param defaults   the default contents from the plugin jar, or null if there are none.
     * @return the config.
     */
    protected abstract C createConfig(Path configPath, @Nullable byte[] defaults);

    /**
     * Loads the given config from its file.
     *
     * @param config the config.
     */
    protected abstract void loadConfigFile(C config);

    /**
     * Loads this module's language file. Called after the configs are loaded.
     */
    protected abstract void loadLanguage();

    /**
     * Registers this module with the platform, for example as an event listener. Called right before {@link #onEnable()}.
     */
    protected abstract void registerPlatformHooks();

    /**
     * Called after {@link #onEnable()} succeeded.
     */
    protected abstract void afterEnable();

    /**
     * Called at the start of {@link #disable()}.
     */
    protected abstract void beforeDisable();

    /**
     * Removes everything this module registered with the platform. Called after {@link #onDisable()}.
     */
    protected abstract void unregisterPlatformHooks();

    /**
     * Called when the module is enabled.
     */
    public abstract void onEnable();

    /**
     * Called when the module is disabled.
     */
    public abstract void onDisable();
}
