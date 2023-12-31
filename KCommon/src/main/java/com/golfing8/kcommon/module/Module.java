package com.golfing8.kcommon.module;

import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.config.commented.MConfiguration;
import com.golfing8.kcommon.config.generator.ConfigClass;
import com.golfing8.kcommon.config.generator.ConfigClassWrapper;
import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.data.DataManager;
import com.golfing8.kcommon.data.DataManagerContainer;
import com.golfing8.kcommon.data.DataSerializable;
import com.golfing8.kcommon.data.local.DataManagerLocal;
import com.golfing8.kcommon.hook.placeholderapi.PlaceholderProvider;
import com.golfing8.kcommon.struct.PermissionLevel;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.util.MS;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Represents an abstract module with functionality on the server. These modules are the basis of functionality
 * for the KCommon suite of plugins. All 'Features' should be implemented through an extension of this class.
 */
public abstract class Module implements Listener, LangConfigContainer, DataManagerContainer, PlaceholderProvider {

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
     * Maps a permission label to its given permission level.
     */
    private final Map<String, PermissionLevel> permissionLevels;
    /**
     * The map of all data managers used by this module.
     */
    private final Map<String, DataManager<?>> dataManagers;
    /**
     * Maps the datamanager's data class to the data manager itself.
     */
    private final Map<Class<?>, DataManager<?>> c2DataManager;

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
     * The base permission level's description
     */
    @Getter @Setter
    private String basePermLevelDesc = "The base permission of this module";
    /**
     * The admin permission level's description
     */
    @Getter @Setter
    private String adminPermLevelDesc = "The admin level permission of this module";
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

    public Module(KPlugin plugin, String moduleName, Set<String> moduleDependencies, Set<String> pluginDependencies) {
        this.plugin = plugin;
        this.moduleName = moduleName;
        this.moduleCommands = new ArrayList<>();
        this.permissionLevels = new HashMap<>();
        this.dataManagers = new HashMap<>();
        this.c2DataManager = new HashMap<>();
        this.moduleTasks = new HashSet<>();
        this.moduleDependencies = new HashSet<>(moduleDependencies);
        this.pluginDependencies = new HashSet<>(pluginDependencies);
        this.subListeners = new HashSet<>();
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
        this.moduleCommands = new ArrayList<>();
        this.permissionLevels = new HashMap<>();
        this.dataManagers = new HashMap<>();
        this.c2DataManager = new HashMap<>();
        this.moduleTasks = new HashSet<>();
        this.moduleDependencies = new HashSet<>(Arrays.asList(info.moduleDependencies()));
        this.pluginDependencies = new HashSet<>(Arrays.asList(info.pluginDependencies()));
        this.subListeners = new HashSet<>();
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
            if(module.getModuleDependencies().contains(this.getClass()))
                module.reloadWithDependencies();
        }
    }

    /**
     * Initializes the module and begins the startup process for it.
     */
    public void initialize() {
        if(Modules.moduleExists(this.getModuleName())) {
            plugin.getLogger().warning(String.format("Module already exists with name %s!", this.getModuleName()));
            return;
        }
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

        this.registerPermissions();
        this.registerSubPermissions();
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
        this.dataManagers.values().forEach(DataManager::shutdown);
        HandlerList.unregisterAll(this);

        //Unregister all commands associated with this module.
        this.moduleCommands.forEach(MCommand::unregister);
        this.moduleCommands.clear();
        this.enabled = false;

        //Unregister tasks, clone for concurrency.
        new ArrayList<>(moduleTasks).forEach(BukkitRunnable::cancel);

        //Unregister sub listeners.
        this.subListeners.forEach(HandlerList::unregisterAll);

        //Clear some data structures.
        this.subListeners.clear();
        this.moduleTasks.clear();
        this.dataManagers.clear();
        this.moduleCommands.clear();
        this.c2DataManager.clear();
        this.permissionLevels.clear();
        this.configWrapper.unregister();
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

        if(Files.notExists(configPath)) {
            //Create the parent directory.
            try{
                Files.createDirectories(configPath.getParent());
            }catch(IOException exc) {
                throw new RuntimeException(String.format("Failed to create parent directory for config file in module %s!", getModuleName()), exc);
            }

            try(InputStream resource =
                        this.plugin.getClass().getResourceAsStream("/" + configName);
                FileOutputStream writer = new FileOutputStream(configPath.toFile())) {

                //Check that the resource exists
                if(resource == null) {
                    if (Files.notExists(configPath))
                        Files.createFile(configPath);
                } else {
                    //Read from the input stream and write the output stream
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = resource.read(buffer)) != -1) {
                        writer.write(buffer, 0, len);
                    }
                    writer.flush();
                }
            }catch(IOException exc) {
                throw new RuntimeException(String.format("Failed to load config for module %s. Is it missing? (Checked under plugin %s)",
                        getModuleName(),
                        getPlugin().getName()), exc);
            }
        }

        //Load the configuration and set it.
        this.mainConfig = new MConfiguration(configPath, this);
        this.mainConfig.load();

        //First, load the language config.
        Path langPath = Paths.get(plugin.getDataFolder().getPath(), moduleName + "-lang.yml");
        this.langConfig = new LangConfig(langPath);
        this.langConfig.load();
        this.loadLangConstants(this.langConfig);
        this.langConfig.save();

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
     * Registers default permission levels to this module.
     */
    private void registerPermissions() {
        this.permissionLevels.put("base", new PermissionLevel(0.0, "base", this.getBasePermLevelDesc()));
        this.permissionLevels.put("admin", new PermissionLevel(100.0, "admin", this.getAdminPermLevelDesc()));
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
     * Adds a sub listener to this module's control.
     *
     * @param listener the sub listener.
     */
    protected final void addSubListener(Listener listener) {
        getPlugin().getServer().getPluginManager().registerEvents(listener, getPlugin());

        this.subListeners.add(listener);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T extends DataSerializable> Map<Class<T>, DataManager<T>> getDataManagerMap() {
        return (Map) c2DataManager;
    }

    /**
     * Adds a data manager with the given key and data class to this module's data manager map
     *
     * @param key the key of the data manager
     * @param dataClass the data class
     * @param <T> the type of the data
     */
    public final <T extends DataSerializable> DataManager<T> addDataManager(String key, Class<T> dataClass) {
        DataManager<T> dataManager = new DataManagerLocal<>(key, plugin, dataClass);
        this.c2DataManager.put(dataClass, dataManager);
        this.dataManagers.put(key.toLowerCase(), dataManager);
        return dataManager;
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
     * A method for subclasses to use when registering custom permission levels.
     * Called immediately before the calling the {@link #onEnable()} method.
     */
    protected void registerSubPermissions() {/*Empty by intent*/}

    /**
     * Used for loading language constants. This is run before the language config has been 'loaded' so
     * these values will not override existing settings in the config.
     *
     * @param config the language config to add the constants to.
     */
    protected void loadLangConstants(LangConfig config) {/*Intentionally empty*/}

    /**
     * Run when this module is called to enable itself
     */
    public abstract void onEnable();

    /**
     * Run when this module is called to disable itself.
     */
    public abstract void onDisable();
}
