package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.CommandManager;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.data.DataManager;
import com.golfing8.kcommon.data.DataManagerAbstract;
import com.golfing8.kcommon.data.DataManagerContainer;
import com.golfing8.kcommon.data.local.DataManagerLocal;
import com.golfing8.kcommon.data.remote.DataManagerRemote;
import com.golfing8.kcommon.db.MongoConnector;
import com.golfing8.kcommon.db.redis.RedisAdapter;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.Modules;
import com.mongodb.connection.ServerDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Gathers the information shown by {@link KStatusCommand} and its sub commands.
 * <br>
 * Everything here reads framework state that is only changed on the server thread, so it should be called from it.
 * The exceptions are {@link #countStored(DataManager)} and {@link #getStoredSize(DataManager)} which do I/O and
 * should only be called off of it.
 */
@UtilityClass
public class KStatusInfo {
    /**
     * Orders plugins by their name, ignoring case.
     */
    private static final Comparator<Plugin> PLUGIN_ORDER = Comparator.comparing(Plugin::getName, String.CASE_INSENSITIVE_ORDER);

    /**
     * A data manager along with the container class that registered it.
     */
    @Getter
    @AllArgsConstructor
    public static final class DataManagerEntry {
        /**
         * The class of the container the manager was registered to.
         */
        private final Class<?> owner;
        /**
         * The plugin that the manager stores its data for.
         */
        private final Plugin plugin;
        /**
         * The manager.
         */
        private final DataManager<?> manager;

        /**
         * Checks if the objects of this manager are stored in mongo.
         *
         * @return true if remote
         */
        public boolean isRemote() {
            return manager instanceof DataManagerRemote;
        }
    }

    /**
     * Gets every enabled plugin built on KCommon.
     *
     * @return the plugins, sorted by name
     */
    public static List<KPlugin> getPlugins() {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .filter(plugin -> plugin instanceof KPlugin && plugin.isEnabled())
                .map(plugin -> (KPlugin) plugin)
                .sorted(PLUGIN_ORDER)
                .collect(Collectors.toList());
    }

    /**
     * Gets the registered modules, optionally only those of one plugin.
     *
     * @param plugin the plugin to filter by, or null for every plugin
     * @return the modules, sorted by plugin then name
     */
    public static List<Module> getModules(@Nullable Plugin plugin) {
        return Modules.getAll().stream()
                .filter(module -> plugin == null || module.getPlugin() == plugin)
                .sorted(Comparator.comparing(Module::getPlugin, PLUGIN_ORDER)
                        .thenComparing(Module::getModuleName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Gets the registered top level commands, optionally only those of one plugin.
     * <br>
     * Commands of plugins that have been disabled are left out, as they can no longer be run.
     *
     * @param plugin the plugin to filter by, or null for every plugin
     * @return the commands, sorted by plugin then name
     */
    public static List<KCommand> getCommands(@Nullable Plugin plugin) {
        return CommandManager.getINSTANCE().getRegisteredCommands().stream()
                .filter(command -> command.getPlugin().isEnabled())
                .filter(command -> plugin == null || command.getPlugin() == plugin)
                .sorted(Comparator.comparing(KCommand::getPlugin, PLUGIN_ORDER)
                        .thenComparing(KCommand::getCommandName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Counts the given command along with all of its sub commands, recursively.
     *
     * @param command the command
     * @return the size of the command tree
     */
    public static int countCommandTree(@NotNull KCommand command) {
        int count = 1;
        for (KCommand subcommand : command.getSubcommands()) {
            count += countCommandTree(subcommand);
        }
        return count;
    }

    /**
     * Gets the registered data managers, optionally only those of one plugin.
     *
     * @param plugin the plugin to filter by, or null for every plugin
     * @return the data managers, sorted by plugin then key
     */
    public static List<DataManagerEntry> getDataManagers(@Nullable Plugin plugin) {
        List<DataManagerEntry> entries = new ArrayList<>();
        for (Map.Entry<Class<? extends DataManagerContainer>, Map<String, DataManager<?>>> entry : DataManagerContainer.dataManagers.entrySet()) {
            for (DataManager<?> manager : entry.getValue().values()) {
                Plugin owningPlugin = manager instanceof DataManagerAbstract ?
                        ((DataManagerAbstract<?>) manager).getPlugin() : JavaPlugin.getProvidingPlugin(entry.getKey());
                if (plugin != null && owningPlugin != plugin)
                    continue;

                entries.add(new DataManagerEntry(entry.getKey(), owningPlugin, manager));
            }
        }
        entries.sort(Comparator.comparing(DataManagerEntry::getPlugin, PLUGIN_ORDER)
                .thenComparing(entry -> entry.getManager().getKey(), String.CASE_INSENSITIVE_ORDER));
        return entries;
    }

    /**
     * Counts the data managers registered by the given module.
     *
     * @param module the module
     * @return the number of data managers
     */
    public static int countDataManagers(@NotNull Module module) {
        Map<String, DataManager<?>> managers = DataManagerContainer.dataManagers.get(module.getClass());
        return managers == null ? 0 : managers.size();
    }

    /**
     * Counts the placeholders, relational ones included, that the given module provides.
     *
     * @param module the module
     * @return the number of placeholders
     */
    public static int countPlaceholders(@NotNull Module module) {
        return module.getPlaceholders().size() + module.getRelationalPlaceholders().size();
    }

    /**
     * Gets a readable name for the container that registered a data manager.
     *
     * @param owner the class of the container
     * @return the module name if the container is a module, otherwise the class name
     */
    public static String getOwnerName(@NotNull Class<?> owner) {
        if (Module.class.isAssignableFrom(owner)) {
            @SuppressWarnings("unchecked")
            Module module = Modules.getModule((Class<? extends Module>) owner);
            if (module != null)
                return module.getModuleName();
        }
        return owner.getSimpleName();
    }

    /**
     * Counts the objects a data manager has in storage, which can be more than what it has cached.
     * <br>
     * This does I/O, so never call it on the server thread.
     *
     * @param manager the manager
     * @return the number of stored objects, or -1 if it couldn't be counted
     */
    public static long countStored(@NotNull DataManager<?> manager) {
        try {
            if (manager instanceof DataManagerLocal) {
                try (Stream<Path> files = Files.walk(((DataManagerLocal<?>) manager).getDirectoryPrefix())) {
                    return files.filter(KStatusInfo::isDataFile).count();
                }
            } else if (manager instanceof DataManagerRemote) {
                return ((DataManagerRemote<?>) manager).getCollection().estimatedDocumentCount();
            }
        } catch (Exception exc) {
            return -1L;
        }
        return -1L;
    }

    /**
     * Gets the number of bytes a data manager takes up on disk.
     * <br>
     * This does I/O, so never call it on the server thread.
     *
     * @param manager the manager
     * @return the size in bytes, or -1 if it isn't stored on disk or couldn't be measured
     */
    public static long getStoredSize(@NotNull DataManager<?> manager) {
        if (!(manager instanceof DataManagerLocal))
            return -1L;

        try (Stream<Path> files = Files.walk(((DataManagerLocal<?>) manager).getDirectoryPrefix())) {
            long total = 0;
            for (Path path : (Iterable<Path>) files.filter(KStatusInfo::isDataFile)::iterator) {
                total += Files.size(path);
            }
            return total;
        } catch (IOException | RuntimeException exc) {
            return -1L;
        }
    }

    private static boolean isDataFile(Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json");
    }

    /**
     * Gets the servers of the mongo cluster as the driver currently sees them.
     * <br>
     * This doesn't do any I/O, the driver keeps it up to date with its own heartbeats.
     *
     * @return the servers, or an empty list if mongo isn't connected
     */
    public static List<ServerDescription> getMongoServers() {
        MongoConnector connector = KCommon.getInstance().getConnector();
        if (connector == null || connector.getClient() == null)
            return Collections.emptyList();

        return connector.getClient().getClusterDescription().getServerDescriptions();
    }

    /**
     * Describes the state of the mongo connection in one line.
     *
     * @return the description
     */
    public static String describeMongo() {
        MongoConnector connector = KCommon.getInstance().getConnector();
        if (connector == null || connector.getClient() == null)
            return "&7not configured";

        List<ServerDescription> servers = getMongoServers();
        long up = servers.stream().filter(ServerDescription::isOk).count();
        return String.format("%s &7(database %s, %d/%d servers up)",
                up > 0 ? "&aconnected" : "&cunreachable", connector.getDatabase().getName(), up, servers.size());
    }

    /**
     * Checks if redis is turned on in KCommon's config.
     *
     * @return true if enabled
     */
    public static boolean isRedisEnabled() {
        return KCommon.getInstance().getConfig().getBoolean("redis.enabled", false);
    }

    /**
     * Describes the state of the redis connection in one line.
     *
     * @return the description
     */
    public static String describeRedis() {
        if (!isRedisEnabled())
            return "&7disabled";

        RedisAdapter adapter = KCommon.getInstance().getRedisAdapter();
        if (!adapter.isConnected())
            return "&cnot connected &7(enabled in config, check the console)";

        return String.format("&aconnected &7(%s, %d channels)", adapter.getAddress(), adapter.getChannels().size());
    }

    /**
     * Formats a boolean for display.
     *
     * @param value the value
     * @return a colored yes or no
     */
    public static String yesNo(boolean value) {
        return value ? "&ayes" : "&cno";
    }

    /**
     * Formats a number of bytes for display.
     *
     * @param bytes the bytes
     * @return the formatted size
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + "B";
        if (bytes < 1024L * 1024L)
            return String.format("%.1fKB", bytes / 1024D);
        if (bytes < 1024L * 1024L * 1024L)
            return String.format("%.1fMB", bytes / (1024D * 1024D));
        return String.format("%.2fGB", bytes / (1024D * 1024D * 1024D));
    }
}
