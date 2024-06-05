package com.golfing8.kcommon.module;

import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.config.commented.MConfiguration;
import com.golfing8.kcommon.config.generator.ConfigClass;
import com.golfing8.kcommon.config.generator.ConfigClassWrapper;
import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.data.DataManagerContainer;
import com.golfing8.kcommon.hook.placeholderapi.KPlaceholderDefinition;
import com.golfing8.kcommon.hook.placeholderapi.PlaceholderProvider;
import com.golfing8.kcommon.struct.KNamespacedKey;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.util.MS;
import com.golfing8.kcommon.util.StringUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Level;

/**
 * Represents an abstract module with functionality on the server. These modules are the basis of functionality
 * for the KCommon suite of plugins. All 'Features' should be implemented through an extension of this class.
 */
public abstract class Module implements Listener, LangConfigContainer, PlaceholderProvider {

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

    /**
     * If this module is enabled or not. This is simply the module's current state.
     * This has no indication of the plugin's enabled state for this module in its manifest.
     */
    @Getter
    private transient boolean enabled;

    /**
     * The list of tasks this module is currently running.
     */
    private final Set<BukkitRunnable> moduleTasks;
    /**
     * Sub-listeners registered to this module.
     */
    private final Set<Listener> subListeners;
    /**
     * All registered sub modules.
     */
    private final Set<SubModule<?>> subModules;

    /**
     * The main config for this module.
     */
    @Getter
    private MConfiguration mainConfig;
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

        // Try to register this module to the registry.
        if(Modules.moduleExists(this.getNamespacedKey())) {
            plugin.getLogger().warning(String.format("Module already exists with name %s!", this.getModuleName()));
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

        // Try to register this module to the registry.
        if(Modules.moduleExists(this.getNamespacedKey())) {
            plugin.getLogger().warning(String.format("Module already exists with name %s!", this.getModuleName()));
            return;
        }
        Modules.registerModule(this);
    }

    /**
     * Reloads this module.
     */
    public void reload() {
        this.shutdown();
        this.initialize();
    }

    /**
     * Recursively reloads this module and ALL modules which directly or indirectly depend on this.
     */
    public void reloadWithDependencies() {
        this.reload();

        for(Module module : Modules.getAll()) {
            if(module.getModuleDependencies().contains(this.getModuleName()))
                module.reloadWithDependencies();
        }
    }

    /**
     * Initializes the module and begins the startup process for it.
     */
    public void initialize() {
        Modules.registerModule(this);
        if (getPlugin().getManifest().loadModule(this)) {
            enable();
            plugin.getLogger().info(String.format("Loaded and enabled module: %s", this.getModuleName()));
        } else {
            plugin.getLogger().info(String.format("Loaded and disabled module: %s", this.getModuleName()));
        }
    }

    /**
     * Transitively enables this module. Note that this will not change the module's
     * state as according to its plugin's manifest.
     */
    public void enable() {
        // Don't enable twice
        if (enabled)
            return;

        loadConfigs();

        //Register this module as a listener.
        this.getPlugin().getServer().getPluginManager().registerEvents(this, this.getPlugin());

        this.loadLangFields();
        this.onEnable();
        this.enabled = true;

        //We should save the language config once more as it's possible the commands this feature registered added constants.
        this.langConfig.save();
    }

    /**
     * Should be called when this module needs to shut down.
     */
    public void shutdown() {
        this.disable();
        Modules.unregisterModule(this);
    }

    /**
     * Transitively disables this module. Note that this will not change the module's
     * state as according to its plugin's manifest.
     */
    public void disable() {
        // Don't disable self twice.
        if (!enabled)
            return;

        //Save the lang config.
        this.langConfig.save();

        this.onDisable();
        // If this module supports data managers, shut them down.
        if (this instanceof DataManagerContainer) {
            ((DataManagerContainer) this).shutdownDataManagers();
        }
        HandlerList.unregisterAll(this);

        //Unregister all commands associated with this module.
        this.moduleCommands.forEach(MCommand::unregister);
        this.moduleCommands.clear();
        this.enabled = false;

        //Unregister tasks, clone for concurrency.
        new ArrayList<>(moduleTasks).forEach(runnable -> {
            try {
                runnable.cancel();
            } catch (IllegalStateException ignored) {} // Can be thrown if the runnable wasn't scheduled yet
        });

        //Unregister sub listeners.
        this.subListeners.forEach(HandlerList::unregisterAll);

        //Unregister sub modules.
        for (SubModule<?> module : new HashSet<>(this.subModules)) {
            module.onDisable();
            HandlerList.unregisterAll(module);
        }

        //Clear some data structures.
        this.subModules.clear();
        this.subListeners.clear();
        this.moduleTasks.clear();
        this.moduleCommands.clear();
        this.configWrapper.unregister();
        this.relationalPlaceholders.clear();
        this.placeholders.clear();
    }

    /**
     * Gets the child config from its class.
     *
     * @param cfgClass the config class.
     * @return the instance.
     * @param <T> the type of config.
     */
    public <T extends ConfigClass> T getCfg(Class<T> cfgClass) {
        return this.configWrapper.getChild(cfgClass);
    }

    /**
     * Loads the main configuration for this module.
     */
    private void loadConfigs() {
        //Load the constants first.
        String configName = this.getModuleName() + ".yml";
        Path configPath = Paths.get(plugin.getDataFolder().getPath(), configName);

        YamlConfiguration source = new YamlConfiguration();

        //Create the parent directory.
        try{
            Files.createDirectories(configPath.getParent());
        }catch(IOException exc) {
            throw new RuntimeException(String.format("Failed to create parent directory for config file in module %s!", getModuleName()), exc);
        }

        try(InputStream resource = this.plugin.getClass().getResourceAsStream("/" + configName)) {
            ByteArrayOutputStream streamCloner = new ByteArrayOutputStream();

            //Check that the resource exists
            if(resource == null) {
                if (Files.notExists(configPath))
                    Files.createFile(configPath);
            } else {
                // Read content
                //Read from the input stream and write the output stream
                byte[] buffer = new byte[1024];
                int len;
                while ((len = resource.read(buffer)) != -1) {
                    streamCloner.write(buffer, 0, len);
                }

                if (Files.notExists(configPath)) {
                    Files.write(configPath, streamCloner.toByteArray(), StandardOpenOption.CREATE);
                }

                try {
                    source.load(new InputStreamReader(new ByteArrayInputStream(streamCloner.toByteArray())));
                } catch (InvalidConfigurationException exc) {
                    getPlugin().getLogger().log(Level.WARNING, String.format("Failed to load source config for module %s.", getModuleName()), exc);
                }
            }
        }catch(IOException exc) {
            throw new RuntimeException(String.format("Failed to load config for module %s. Is it missing? (Checked under plugin %s)",
                    getModuleName(),
                    getPlugin().getName()), exc);
        }

        //Load the configuration and set it.
        this.mainConfig = new MConfiguration(configPath, this);
        this.mainConfig.setSource(source);
        this.mainConfig.load();

        //First, load the language config.
        Path langPath = Paths.get(plugin.getDataFolder().getPath(), moduleName + "-lang.yml");
        this.langConfig = new LangConfig(langPath);
        this.langConfig.load();
        this.loadLangConstants(this.langConfig);

        // Finally, load the config wrapper.
        this.configWrapper = new ConfigClassWrapper(null, this.getClass(), this);
        this.configWrapper.setRequireAnnotation(true);
        this.configWrapper.initConfig();
        boolean modded = this.configWrapper.loadValues(this.mainConfig);
        if (modded) {
            mainConfig.save();
        }
    }

    /**
     * Adds the given runnable as a task. The returned runnable will have not started.
     *
     * @param runnable the runnable.
     * @return the task.
     */
    protected synchronized ModuleTask addTask(Runnable runnable) {
        ModuleTask mTask = new ModuleTask(this, runnable);
        this.moduleTasks.add(mTask);
        return mTask;
    }

    /**
     * Removes a task from this module's task list.
     *
     * @param task the task to remove.
     */
    protected synchronized BukkitRunnable removeTask(BukkitRunnable task) {
        this.moduleTasks.remove(task);
        return task;
    }

    /**
     * The task to add to this module. Note that this module will not call the runTask family of methods for
     * the given task.
     *
     * @param task the task.
     */
    protected synchronized BukkitRunnable addTask(BukkitRunnable task) {
        this.moduleTasks.add(task);
        return task;
    }

    /**
     * Adds a submodule to this module.
     *
     * @param subModule the module.
     */
    public final void addSubModule(SubModule<?> subModule) {
        // Already registered.
        if (this.subModules.contains(subModule))
            return;

        this.subModules.add(subModule);
        getPlugin().getServer().getPluginManager().registerEvents(subModule, getPlugin());
        subModule.onEnable();

        // Load the config.
        boolean save = subModule.loadValues(getMainConfig().createSection(subModule.getPrefix()));
        if (save) {
            this.mainConfig.save();
        }

        // Load lang config
        subModule.loadLangFields();
    }

    /**
     * Removes the submodule.
     *
     * @param subModule the submodule.
     */
    public final void removeSubModule(SubModule<?> subModule) {
        if (!this.subModules.contains(subModule))
            return;

        this.subModules.remove(subModule);
        HandlerList.unregisterAll(subModule);
        subModule.onDisable();
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
    protected void addCommand(MCommand<?> mCommand) {
        this.moduleCommands.add(mCommand);
        mCommand.register();
    }

    /**
     * Sends a message to the given sender.
     *
     * @param sender the sender to receive the message.
     * @param message the message to send to the player.
     * @param placeholders the object placeholders, should be in the format of:
     *                     PLACEHOLDER_KEY, value, PLACEHOLDER_KEY2, value2
     */
    public void sendMessage(CommandSender sender, Message message, Object... placeholders) {
        message.getMessages().forEach(string -> MS.pass(sender, string, placeholders));
    }

    /**
     * Sends a message to the given sender.
     *
     * @param sender the sender to receive the message.
     * @param message the message to send to the player.
     * @param placeholders the placeholder objects.
     */
    public void sendMessage(CommandSender sender, Message message, Placeholder... placeholders) {
        MS.parseAll(message.getMessages(), placeholders).forEach(string -> MS.pass(sender, string));
    }

    /**
     * Sends a message to the given sender.
     *
     * @param sender the sender to receive the message.
     * @param message the message to send to the player.
     * @param placeholders the placeholder objects.
     */
    public void sendMessage(CommandSender sender, Message message, Collection<Placeholder> placeholders) {
        MS.parseAll(message.getMessages(), placeholders).forEach(string -> MS.pass(sender, string));
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
     * @deprecated register constants in {@link #onEnable()}
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
