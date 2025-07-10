package com.golfing8.kcommon.module;

import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.config.commented.MConfiguration;
import com.golfing8.kcommon.config.generator.ConfigClassSource;
import com.golfing8.kcommon.config.generator.ConfigClassWrapper;
import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
import com.golfing8.kcommon.config.lang.LangConfigEnum;
import com.golfing8.kcommon.data.DataManagerContainer;
import com.golfing8.kcommon.hook.placeholderapi.KPlaceholderDefinition;
import com.golfing8.kcommon.hook.placeholderapi.PlaceholderProvider;
import com.golfing8.kcommon.struct.KNamespacedKey;
import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import com.golfing8.kcommon.struct.helper.terminable.TerminableConsumer;
import com.golfing8.kcommon.struct.helper.terminable.composite.CompositeClosingException;
import com.golfing8.kcommon.struct.helper.terminable.composite.CompositeTerminable;
import com.golfing8.kcommon.struct.permission.PermissionContext;
import com.golfing8.kcommon.util.FileUtil;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Represents an abstract module with functionality on the server. These modules are the basis of functionality
 * for the KCommon suite of plugins. All 'Features' should be implemented through an extension of this class.
 */
public abstract class Module implements Listener, LangConfigContainer, PlaceholderProvider, PermissionContext, TerminableConsumer {
    /**
     * Gets a module instance associated with the given type for the call.
     *
     * @param unused an unused array of objects used to reify the generic.
     * @return the module instance.
     * @param <T> the type.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(@NotNull T... unused) {
        return (T) Modules.getModule((Class<? extends Module>) unused.getClass().getComponentType());
    }

    /**
     * The owning plugin of this module.
     */
    @Getter
    private final KPlugin plugin;
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
    /** The plugin dependencies for this module */
    @Getter
    private final Set<String> pluginDependencies;
    /** The logger used for this module */
    @Getter
    private final Logger logger;
    /** The permission prefix */
    @Getter
    private final String permissionPrefix;
    /** The module info annotation on this class */
    @Getter
    private final @Nullable ModuleInfo moduleInfo;
    /** The terminable for this life cycle of the module. */
    @Getter
    private final CompositeTerminable terminable = CompositeTerminable.create();
    @Override
    public @NotNull <T extends AutoCloseable> T bind(@NotNull T terminable) {
        return this.terminable.bind(terminable);
    }

    /**
     * If this module is enabled or not. This is simply the module's current state.
     * This has no indication of the plugin's enabled state for this module in its manifest.
     */
    @Getter
    private transient boolean enabled;
    /** Used to indicate that the {@link #disable()} method was called during the execution of {@link #onEnable()} */
    private transient boolean prematureDisable;

    /**
     * The list of tasks this module is currently running.
     */
    @SuppressWarnings("rawtypes")
    private final Set<ModuleTask> moduleTasks;
    /**
     * Sub-listeners registered to this module.
     */
    private final Set<Listener> subListeners;
    /**
     * All registered sub modules.
     */
    private final Set<SubModule<?>> subModules;
    /** The path to the data folder */
    private Path dataFolder;
    /**
     * Other configs that are linked to this module.
     */
    @Getter
    private final Map<String, MConfiguration> configs;
    public @NotNull MConfiguration getMainConfig() {
        return configs.get("config");
    }

    /**
     * Gets the config under the given key, or creates one if it wasn't previously registered.
     *
     * @param key the key of the config.
     * @return the config.
     */
    public @NotNull MConfiguration getConfig(String key) {
        MConfiguration configuration = configs.get(key);
        if (configuration == null) {
            configuration = loadConfig(Paths.get(plugin.getDataFolder().getPath(), moduleName, key + ".yml"), true);
            configs.put(key, configuration);
        }
        return configuration;
    }

    /**
     * The language config for this module.
     */
    @Getter
    private LangConfig langConfig;
    /**
     * The config wrapper backing this module.
     */
    @Getter
    private ConfigClassWrapper configWrapper;
    /**
     * The commands this module controls. Note that all these commands will be removed and
     * destroyed after this module is disabled. This means that the module should register commands
     * every time {@link #onEnable()} is called.
     */
    private final List<MCommand<?>> moduleCommands;
    /**
     * Stores simple placeholders registered to this module.
     */
    @Getter
    private final TreeMap<KPlaceholderDefinition, PlaceholderFunction> placeholders;
    /**
     * Stores relational placeholders registered to this module.
     */
    @Getter
    private final TreeMap<KPlaceholderDefinition, RelPlaceholderFunction> relationalPlaceholders;

    public Module(KPlugin plugin, String moduleName, Set<String> moduleDependencies, Set<String> pluginDependencies) {
        this.plugin = plugin;
        this.namespacedKey = new KNamespacedKey(plugin, moduleName);
        this.moduleName = moduleName;
        this.moduleCommands = new ArrayList<>();
        this.moduleTasks = new HashSet<>();
        this.moduleDependencies = new HashSet<>(moduleDependencies);
        this.pluginDependencies = new HashSet<>(pluginDependencies);
        this.placeholders = new TreeMap<>();
        this.relationalPlaceholders = new TreeMap<>();
        this.subListeners = new HashSet<>();
        this.subModules = new HashSet<>();
        this.configs = new ConcurrentHashMap<>();
        this.logger = new ModuleLogger(this);
        this.permissionPrefix = plugin.getName() + "." + this.moduleName;
        this.moduleInfo = null;

        // Try to register this module to the registry.
        if(Modules.moduleExists(this.getNamespacedKey())) {
            getLogger().warning(String.format("Module already exists with name %s!", this.getModuleName()));
            return;
        }
        Modules.registerModule(this);
    }

    public Module(KPlugin plugin, String moduleName) {
        this(plugin, moduleName, Collections.emptySet(), Collections.emptySet());
    }

    protected Module() {
        ModuleInfo info = this.getClass().getAnnotation(ModuleInfo.class);
        if (info == null)
            throw new IllegalArgumentException("Module info missing!");

        this.plugin = (KPlugin) JavaPlugin.getProvidingPlugin(this.getClass());
        this.moduleName = info.name();
        this.namespacedKey = new KNamespacedKey(this.plugin, this.moduleName);
        this.moduleCommands = new ArrayList<>();
        this.moduleTasks = new HashSet<>();
        this.placeholders = new TreeMap<>();
        this.relationalPlaceholders = new TreeMap<>();
        this.moduleDependencies = new HashSet<>(Arrays.asList(info.moduleDependencies()));
        this.pluginDependencies = new HashSet<>(Arrays.asList(info.pluginDependencies()));
        this.subListeners = new HashSet<>();
        this.subModules = new HashSet<>();
        this.configs = new HashMap<>();
        this.logger = new ModuleLogger(this);
        this.permissionPrefix = plugin.getName() + "." + this.moduleName;
        this.moduleInfo = info;

        // Try to register this module to the registry.
        if(Modules.moduleExists(this.getNamespacedKey())) {
            getLogger().warning(String.format("Module already exists with name %s!", this.getModuleName()));
            return;
        }
        Modules.registerModule(this);
    }

    /**
     * Reloads this module.
     */
    public final void reload() {
        this.shutdown();
        this.initialize();
    }

    /**
     * Recursively reloads this module and ALL modules which directly or indirectly depend on this.
     */
    public final void reloadWithDependencies() {
        this.reload();

        for(Module module : Modules.getAll()) {
            if(module.getModuleDependencies().contains(this.getModuleName()))
                module.reloadWithDependencies();
        }
    }

    /**
     * Initializes the module and begins the startup process for it.
     */
    public final void initialize() {
        Modules.registerModule(this);
        if (getPlugin().getManifest().loadModule(this)) {
            if (!enable()) {
                getLogger().info("Failed to enable module.");
                return;
            }
            getLogger().info("Loaded and enabled module.");
        } else {
            getLogger().info("Loaded and disabled module.");
        }
    }

    /**
     * Transitively enables this module. Note that this will not change the module's
     * state as according to its plugin's manifest.
     *
     * @return if the module enabled successfully
     */
    public final boolean enable() {
        // Don't enable twice
        if (enabled)
            return false;

        try {
            this.loadConfigs();
            this.loadContainer();
        } catch (Throwable thr) {
            getLogger().log(Level.SEVERE, "Failed to load due to config error!", thr);
            return false;
        }

        //Register this module as a listener.
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());

        this.prematureDisable = false;
        try {
            this.onEnable();
        } catch (Throwable thr) {
            getLogger().log(Level.SEVERE, "Module failed to enable!", thr);
            return false;
        }

        // It's possible that the implementation of onEnable called disable()
        if (this.prematureDisable) {
            return false;
        }

        // Add placeholder hook
        this.plugin.getPlaceholderAPIHook().registerProvider(this);

        //We should save the language config once more as it's possible the commands this feature registered added constants.
        this.langConfig.save();
        this.enabled = true;
        return true;
    }

    /**
     * Should be called when this module needs to shut down.
     */
    public final void shutdown() {
        this.disable();
        Modules.unregisterModule(this);
    }

    /**
     * Transitively disables this module. Note that this will not change the module's
     * state as according to its plugin's manifest.
     * <p>
     * If this is called from within the implementation of {@link #onEnable()}, the {@link #onDisable()} implementation
     * will NOT be executed. Instead, a flag will be set that the enable process failed.
     * </p>
     */
    public final void disable() {
        // Don't disable self twice.
        if (!enabled) {
            prematureDisable = true;
            return;
        }

        //Save the lang config.
        this.langConfig.save();

        try {
            this.terminable.close();
        } catch (CompositeClosingException exc) {
            getLogger().log(Level.SEVERE, "Failed to close terminable!", exc);
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
        HandlerList.unregisterAll(this);

        //Unregister all commands associated with this module.
        this.moduleCommands.forEach(MCommand::unregister);
        this.moduleCommands.clear();

        //Unregister tasks, clone for concurrency.
        new ArrayList<>(moduleTasks).forEach(runnable -> {
            if (!runnable.isStarted())
                return;

            try {
                runnable.cancel();
            } catch (IllegalStateException ignored) {} // Can be thrown if the runnable wasn't scheduled yet
        });

        //Unregister sub listeners.
        this.subListeners.forEach(HandlerList::unregisterAll);

        //Unregister sub modules.
        for (SubModule<?> module : new HashSet<>(this.subModules)) {
            module.unregister();
        }

        // Remove placeholder hook
        this.plugin.getPlaceholderAPIHook().unregisterProvider(this);

        //Clear some data structures.
        this.subModules.clear();
        this.subListeners.clear();
        this.moduleTasks.clear();
        this.moduleCommands.clear();
        this.configWrapper.unregister();
        this.relationalPlaceholders.clear();
        this.placeholders.clear();
        this.configs.clear();
        this.enabled = false;
    }

    /**
     * Loads a config group from the given name.
     * <p>
     *     Returns a list of configurations under the directory /{@code groupName}/ in the plugin's data folder.
     * </p>
     *
     * @return the config group.
     */
    protected final List<Configuration> loadConfigGroup(String groupName) {
        Path directoryPath = this.dataFolder.resolve(groupName);
        if (!Files.isDirectory(directoryPath)) {
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.list(directoryPath)) {
            List<Configuration> toReturn = new ArrayList<>();
            paths.forEach(path -> {
                toReturn.add(loadConfig(path, true));
            });
            return toReturn;
        } catch (IOException exc) {
            getLogger().log(Level.WARNING, "Failed to load config group: " + groupName, exc);
            return Collections.emptyList();
        }
    }

    /**
     * Loads the main configuration for this module.
     */
    private void loadConfigs() {
        migrateOldConfigs();

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
        this.configWrapper.initConfig();

        //Create the parent directory.
        this.dataFolder = Paths.get(plugin.getDataFolder().getPath(), moduleName);
        boolean firstLoad = Files.notExists(dataFolder);
        try{
            Files.createDirectories(dataFolder.getParent());
        }catch(IOException exc) {
            throw new RuntimeException(String.format("Failed to create parent directory for config file in module %s!", getModuleName()), exc);
        }

        // First copy everything over from our module's directory.
        if (firstLoad) {
            try {
                FileUtil.copyJarElements(plugin.getFile().toPath(), this.moduleName, dataFolder);
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
        for (Configuration configuration : new ArrayList<>(this.configs.values())) {
            // Load with the config wrapper.
            boolean modded = this.configWrapper.loadValues(configuration);
            if (modded) {
                configuration.save();
            }
        }

        //First, load the language config.
        Path langPath = Paths.get(plugin.getDataFolder().getPath(), moduleName, "lang.yml");
        this.langConfig = new LangConfig(langPath);
        this.langConfig.load();

        // Load extra lang sources.
        if (this.moduleInfo != null) {
            for (Class<? extends LangConfigEnum> source : this.moduleInfo.langSources()) {
                this.loadLangEnum(source);
            }
        }

        this.loadLangConstants(this.langConfig);
    }

    /**
     * Attempts to migrate old configs from their old location to the updated one.
     * TODO Remove this after a while.
     */
    private void migrateOldConfigs() {
        Path oldConfigPath = Paths.get(plugin.getDataFolder().getPath(), moduleName + ".yml");
        Path newConfigPath = Paths.get(plugin.getDataFolder().getPath(), moduleName, "config.yml");
        migrateConfig(oldConfigPath, newConfigPath);

        Path oldLangPath = Paths.get(plugin.getDataFolder().getPath(), moduleName + "-lang.yml");
        Path newLangPath = Paths.get(plugin.getDataFolder().getPath(), moduleName, "lang.yml");
        migrateConfig(oldLangPath, newLangPath);
    }

    private void migrateConfig(Path oldPath, Path newPath) {
        if (Files.notExists(oldPath) || Files.exists(newPath))
            return;

        try {
            Files.createDirectories(newPath.getParent());
            Files.move(oldPath, newPath);
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to migrate config from %s to %s!", oldPath, newPath), exc);
        }
    }

    /**
     * Loads the config under the given path.
     *
     * @param configPath the path.
     * @param loadValues if values should be loaded as well.
     */
    private MConfiguration loadConfig(Path configPath, boolean loadValues) {
        YamlConfiguration source = new YamlConfiguration();

        //Create the parent directory.
        try{
            Files.createDirectories(configPath.getParent());
        }catch(IOException exc) {
            throw new RuntimeException(String.format("Failed to create parent directory for config file in module %s!", getModuleName()), exc);
        }

        boolean defaultConfig = configPath.endsWith("config.yml");
        // Test the new location for the resource, otherwise fallback on the old one.
        String resourcePath = "/" + getModuleName() + "/" + configPath.getFileName().toString();
        if (this.plugin.getClass().getResource(resourcePath) == null) {
            resourcePath = "/" + (defaultConfig ? moduleName + ".yml" : configPath.getFileName().toString());
        }

        try(InputStream resource = this.plugin.getClass().getResourceAsStream(resourcePath)) {
            ByteArrayOutputStream streamCloner = new ByteArrayOutputStream();

            //Check that the resource exists
            if(resource == null) {
                if (Files.notExists(configPath))
                    Files.createFile(configPath);
            } else {
                IOUtils.copy(resource, streamCloner);
                if (Files.notExists(configPath)) {
                    Files.write(configPath, streamCloner.toByteArray(), StandardOpenOption.CREATE);
                }

                try {
                    source.load(new InputStreamReader(new ByteArrayInputStream(streamCloner.toByteArray())));
                } catch (InvalidConfigurationException exc) {
                    getLogger().log(Level.WARNING, String.format("Failed to load source config %s for module.",
                            configPath.getFileName().toString()), exc);
                }
            }
        }catch(IOException exc) {
            throw new RuntimeException(String.format("Failed to load config %s for module %s. Is it missing? (Checked under plugin %s)",
                    configPath.getFileName().toString(),
                    getModuleName(),
                    getPlugin().getName()), exc);
        }

        MConfiguration toReturn = new MConfiguration(configPath, this);
        toReturn.setSource(source);
        toReturn.load();

        if (loadValues) {
            // Load with the config wrapper.
            boolean modded = this.configWrapper.loadValues(toReturn);
            if (modded) {
                toReturn.save();
            }
        }
        return toReturn;
    }

    /**
     * Adds the given runnable as a task. The returned runnable will have not started.
     *
     * @param runnable the runnable.
     * @return the task.
     */
    @SuppressWarnings({"rawtypes", "unchecked", "unused"})
    public synchronized ModuleTask addTask(Runnable runnable) {
        ModuleTask mTask = new ModuleTask(this, runnable);
        this.moduleTasks.add(mTask);
        return mTask;
    }

    /**
     * Adds the given module task to this module.
     *
     * @param moduleTask the module task.
     * @return the same task.
     */
    @SuppressWarnings("rawtypes")
    public synchronized ModuleTask addTask(ModuleTask moduleTask) {
        if (moduleTask.getModule() != this) {
            throw new IllegalArgumentException("Module task is linked to " + moduleTask.getModule().getModuleName() + ". It cannot be added to " + this.getModuleName() + "!");
        }

        this.moduleTasks.add(moduleTask);
        return moduleTask;
    }

    /**
     * Removes a task from this module's task list.
     *
     * @param task the task to remove.
     */
    @SuppressWarnings("rawtypes")
    public synchronized void removeTask(ModuleTask task) {
        this.moduleTasks.remove(task);
    }

    /**
     * Adds a submodule to this module.
     *
     * @param subModule the module.
     */
    @SuppressWarnings({"rawtypes", "unchecked", "unused"})
    public final void addSubModule(SubModule<?> subModule) {
        // Already registered.
        if (this.subModules.contains(subModule))
            return;

        this.subModules.add(subModule);
        ((SubModule) subModule).link(this);
        getPlugin().getServer().getPluginManager().registerEvents(subModule, getPlugin());
        Set<String> configNames = subModule.getConfigNames();

        // Make sure all the expected configs are available.
        for (String configName : configNames) {
            if (this.configs.containsKey(configName))
                continue;

            getConfig(configName);
        }

        // Load the config.
        for (MConfiguration configuration : this.configs.values()) {
            if (!configNames.contains(configuration.getFileNameNoExtension()))
                continue;

            String prefix = subModule.getPrefix();
            ConfigurationSection section = prefix.isEmpty() ? configuration :
                    (configuration.contains(prefix) ? configuration.getConfigurationSection(prefix) : configuration.createSection(prefix));
            boolean save = subModule.loadValues(section);
            if (save) {
                configuration.save();
            }
        }

        // Load lang config
        subModule.loadContainer();
        try {
            subModule.onEnable();
        } catch (Throwable thr) {
            getLogger().log(Level.SEVERE, String.format("Failed to enable sub-module %s!", subModule.getClass().getSimpleName()), thr);

            // Unregister the module.
            this.subModules.remove(subModule);
            HandlerList.unregisterAll(subModule);
        }
    }

    /**
     * Removes the submodule.
     *
     * @param subModule the submodule.
     */
    public final void removeSubModule(SubModule<?> subModule) {
        if (!this.subModules.contains(subModule))
            return;

        subModule.unregister();
        this.subModules.remove(subModule);
    }

    /**
     * Adds a sub listener to this module's control.
     *
     * @param listener the sub listener.
     */
    public final void addSubListener(Listener listener) {
        getPlugin().getServer().getPluginManager().registerEvents(listener, getPlugin());

        this.subListeners.add(listener);
    }

    /**
     * Removes the sub listener.
     *
     * @param listener the sub listener.
     */
    public final void removeSubListener(Listener listener) {
        HandlerList.unregisterAll(listener);

        this.subListeners.remove(listener);
    }

    /**
     * Adds the command to this module.
     *
     * @param mCommand the command.
     */
    protected final void addCommand(MCommand<?> mCommand) {
        this.moduleCommands.add(mCommand);
        mCommand.register();
    }

    @NotNull
    @Override
    public String getPlaceholderKey() {
        return this.moduleName;
    }

    /**
     * Used for loading language constants. This is run before the language config has been 'loaded' so
     * these values will not override existing settings in the config.
     *
     * @param config the language config to add the constants to.
     * @deprecated register constants in {@link #onEnable()}, use @LangConf, or use LangConfigEnum
     */
    @Deprecated
    protected void loadLangConstants(LangConfig config) {/*Intentionally empty*/}

    @Override
    public void addPlaceholder(KPlaceholderDefinition definition, PlaceholderFunction function) {
        this.placeholders.put(definition, function);
    }

    @Override
    public void addRelPlaceholder(KPlaceholderDefinition definition, RelPlaceholderFunction function) {
        this.relationalPlaceholders.put(definition, function);
    }

    /**
     * Run when this module is called to enable itself
     */
    public abstract void onEnable();

    /**
     * Run when this module is called to disable itself.
     */
    public abstract void onDisable();
}
