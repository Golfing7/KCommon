package com.golfing8.kcommon.module;

import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.config.commented.BukkitSection;
import com.golfing8.kcommon.config.commented.MConfiguration;
import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
import com.golfing8.kcommon.config.lang.LangConfigEnum;
import com.golfing8.kcommon.hook.placeholderapi.KPlaceholderDefinition;
import com.golfing8.kcommon.hook.placeholderapi.PlaceholderProvider;
import com.golfing8.kcommon.struct.permission.PermissionContext;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Represents an abstract module with functionality on the server. These modules are the basis of functionality
 * for the KCommon suite of plugins. All 'Features' should be implemented through an extension of this class.
 */
public abstract class Module extends AbstractModule<MConfiguration> implements Listener, LangConfigContainer, PlaceholderProvider, PermissionContext {
    /**
     * Gets a module instance associated with the given type for the call.
     *
     * @param unused an unused array of objects used to reify the generic.
     * @param <T>    the type.
     * @return the module instance.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(@NotNull T... unused) {
        return (T) Modules.getModule((Class<? extends Module>) unused.getClass().getComponentType());
    }

    /**
     * The list of tasks this module is currently running.
     */
    @SuppressWarnings("rawtypes")
    private final Set<ModuleTask> moduleTasks = new HashSet<>();
    /**
     * Sub-listeners registered to this module.
     */
    private final Set<Listener> subListeners = new HashSet<>();
    /**
     * All registered sub modules.
     */
    private final Set<SubModule<?>> subModules = new HashSet<>();
    /**
     * The language config for this module.
     */
    @Getter
    private LangConfig langConfig;
    /**
     * The commands this module controls. Note that all these commands will be removed and
     * destroyed after this module is disabled. This means that the module should register commands
     * every time {@link #onEnable()} is called.
     */
    @Getter
    private final List<MCommand<?>> moduleCommands = new ArrayList<>();
    /**
     * Stores simple placeholders registered to this module.
     */
    @Getter
    private final TreeMap<KPlaceholderDefinition, PlaceholderFunction> placeholders = new TreeMap<>();
    /**
     * Stores relational placeholders registered to this module.
     */
    @Getter
    private final TreeMap<KPlaceholderDefinition, RelPlaceholderFunction> relationalPlaceholders = new TreeMap<>();

    public Module(KPlugin plugin, String moduleName, Set<String> moduleDependencies, Set<String> pluginDependencies) {
        super(plugin, moduleName, moduleDependencies, pluginDependencies);
    }

    public Module(KPlugin plugin, String moduleName) {
        this(plugin, moduleName, Collections.emptySet(), Collections.emptySet());
    }

    protected Module() {
        super();
    }

    @Override
    public KPlugin getPlugin() {
        return (KPlugin) super.getPlugin();
    }

    @Override
    protected ModuleManifest getModuleManifest() {
        return getPlugin().getManifest();
    }

    @Override
    protected MConfiguration createConfig(Path configPath, @Nullable byte[] defaults) {
        YamlConfiguration source = new YamlConfiguration();
        if (defaults != null) {
            try {
                source.loadFromString(new String(defaults, StandardCharsets.UTF_8));
            } catch (InvalidConfigurationException exc) {
                getLogger().log(Level.WARNING, String.format("Failed to load source config %s for module.",
                        configPath.getFileName().toString()), exc);
            }
        }

        MConfiguration configuration = new MConfiguration(configPath, this);
        configuration.setSource(source);
        return configuration;
    }

    @Override
    protected void loadConfigFile(MConfiguration config) {
        config.load();
    }

    @Override
    protected void loadLanguage() {
        //First, load the language config.
        getProfiler().start("loadLangConfig");
        Path langPath = getPlugin().getPluginDataFolder().resolve(getModuleName()).resolve("lang.yml");
        this.langConfig = new LangConfig(langPath);
        this.langConfig.load();
        getProfiler().stop("loadLangConfig");

        // Load extra lang sources.
        if (getModuleInfo() != null) {
            for (Class<?> source : getModuleInfo().langSources()) {
                getProfiler().start("loadLangEnum");
                this.loadLangEnum(source.asSubclass(LangConfigEnum.class));
                getProfiler().stop("loadLangEnum");
            }
        }

        this.loadLangConstants(this.langConfig);

        getProfiler().start("loadContainer");
        this.loadContainer();
        getProfiler().stop("loadContainer");
    }

    @Override
    protected void registerPlatformHooks() {
        //Register this module as a listener.
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());
    }

    @Override
    protected void afterEnable() {
        // Add placeholder hook
        this.getPlugin().getPlaceholderAPIHook().registerProvider(this);

        // We should save the language config once more as it's possible the commands this feature registered added constants.
        this.langConfig.save();
    }

    @Override
    protected void beforeDisable() {
        //Save the lang config.
        this.langConfig.save();
    }

    @Override
    protected void unregisterPlatformHooks() {
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
            } catch (IllegalStateException ignored) {
            } // Can be thrown if the runnable wasn't scheduled yet
        });

        //Unregister sub listeners.
        this.subListeners.forEach(HandlerList::unregisterAll);

        //Unregister sub modules.
        for (SubModule<?> module : new HashSet<>(this.subModules)) {
            module.unregister();
        }

        // Remove placeholder hook
        this.getPlugin().getPlaceholderAPIHook().unregisterProvider(this);

        //Clear some data structures.
        this.subModules.clear();
        this.subListeners.clear();
        this.moduleTasks.clear();
        this.relationalPlaceholders.clear();
        this.placeholders.clear();
    }

    /**
     * Adds a task to this module.
     *
     * @param runnable the runnable
     * @return the module task.
     */
    @SuppressWarnings({"rawtypes", "unchecked", "unused"})
    public synchronized ModuleTask addTask(Runnable runnable) {
        ModuleTask mTask = new ModuleTask(this, runnable);
        this.moduleTasks.add(mTask);
        return mTask;
    }

    /**
     * Adds a task to this module.
     *
     * @param ticker the ticker
     * @return the module task.
     */
    @SuppressWarnings({"rawtypes", "unchecked", "unused"})
    public synchronized ModuleTask addTask(Consumer<ModuleTask> ticker) {
        ModuleTask mTask = new ModuleTask(this, ticker);
        this.moduleTasks.add(mTask);
        return mTask;
    }

    /**
     * Adds the given module task to this module.
     *
     * @param moduleTask the module task.
     * @return the module task.
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
     * Removes the given task from this module.
     *
     * @param task the task.
     */
    @SuppressWarnings("rawtypes")
    public synchronized void removeTask(ModuleTask task) {
        this.moduleTasks.remove(task);
    }

    /**
     * Adds a sub module to this module.
     *
     * @param subModule the sub module.
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
            if (getConfigs().containsKey(configName))
                continue;

            getConfig(configName);
        }

        // Load the config.
        for (MConfiguration configuration : getConfigs().values()) {
            if (!configNames.contains(configuration.getFileNameNoExtension()))
                continue;

            String prefix = subModule.getPrefix();
            ConfigurationSection section = prefix.isEmpty() ? configuration :
                    (configuration.contains(prefix) ? configuration.getConfigurationSection(prefix) : configuration.createSection(prefix));
            boolean save = subModule.loadValues(BukkitSection.of(section));
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
     * Removes the sub module from this module.
     *
     * @param subModule the sub module.
     */
    public final void removeSubModule(SubModule<?> subModule) {
        if (!this.subModules.contains(subModule))
            return;

        subModule.unregister();
        this.subModules.remove(subModule);
    }

    /**
     * Adds a sub listener to this module. It is unregistered when this module is disabled.
     *
     * @param listener the listener.
     */
    public final void addSubListener(Listener listener) {
        getPlugin().getServer().getPluginManager().registerEvents(listener, getPlugin());

        this.subListeners.add(listener);
    }

    /**
     * Removes the sub listener from this module.
     *
     * @param listener the listener.
     */
    public final void removeSubListener(Listener listener) {
        HandlerList.unregisterAll(listener);

        this.subListeners.remove(listener);
    }

    /**
     * Adds a command to this module. It is unregistered when this module is disabled.
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
        return this.getModuleName();
    }

    /**
     * Loads language constants into the given config.
     *
     * @param config the config.
     * @deprecated use {@link com.golfing8.kcommon.config.lang.LangConf} fields instead.
     */
    @Deprecated
    protected void loadLangConstants(LangConfig config) {
        /*Intentionally empty*/
    }

    @Override
    public void addPlaceholder(KPlaceholderDefinition definition, PlaceholderFunction function) {
        this.placeholders.put(definition, function);
    }

    @Override
    public void addRelPlaceholder(KPlaceholderDefinition definition, RelPlaceholderFunction function) {
        this.relationalPlaceholders.put(definition, function);
    }
}
