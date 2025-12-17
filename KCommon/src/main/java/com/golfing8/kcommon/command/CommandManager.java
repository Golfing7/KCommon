package com.golfing8.kcommon.command;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * A class which is useful for accessing the bukkit command map reflectively.
 */
public final class CommandManager {
    @Getter
    private static final CommandManager INSTANCE = new CommandManager();
    private final FieldHandle<CommandMap> commandMapHandle;
    private final FieldHandle<Map<String, Command>> knownCommandsHandle;

    /**
     * The plugin this command manager uses.
     */
    private boolean needsSync;

    private CommandManager() {
        Bukkit.getScheduler().runTaskTimer(KCommon.getInstance(), () -> {
            if (needsSync) {
                needsSync = false;
                syncCommandsIfPossible();
            }
        }, 0, 1);

        commandMapHandle = FieldHandles.getHandle("commandMap", SimplePluginManager.class);
        knownCommandsHandle = FieldHandles.getHandle("knownCommands", SimpleCommandMap.class);
    }

    /**
     * Registers a {@link KCommand} to the bukkit command map.
     *
     * @param plugin  the plugin to register it to.
     * @param command the command.
     * @param sync    if the command map should be flagged for resync.
     * @return the registered plugin command.
     */
    public PluginCommand registerNewCommand(Plugin plugin, KCommand command, boolean sync) {
        PluginCommand pluginCommand = getCommand(plugin, command.getCommandName(), command.getCommandAliases());
        getCommandMap().register(plugin.getName(), pluginCommand);

        //Add the executor and completer.
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);

        if (sync) {
            needsSync = true;
        }
        return pluginCommand;
    }

    /**
     * Unregisters the given command from the bukkit command map.
     *
     * @param command the command to use.
     */
    public void unregisterCommand(KCommand command) {
        deregisterCommand(command.getCommandName(), command.getCommandAliases());
        needsSync = true;
    }

    /**
     * Generates a {@link PluginCommand} instance for use in the command map.
     *
     * @param name    the name of the command.
     * @param aliases the aliases for the command.
     * @return the plugin command.
     */
    private PluginCommand getCommand(Plugin plugin, String name, List<String> aliases) {
        PluginCommand pluginCommand = null;
        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            pluginCommand = c.newInstance(name, plugin);
            pluginCommand.setAliases(aliases);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            KCommon.getInstance().getLogger().log(Level.SEVERE, "Failed to to get plugin command " + name, e);
        }
        return pluginCommand;
    }

    /**
     * Gets the bukkit command map.
     *
     * @return the command map.
     */
    public CommandMap getCommandMap() {
        return Bukkit.getPluginManager() instanceof SimplePluginManager ? this.commandMapHandle.get(Bukkit.getPluginManager()) : null;
    }

    /**
     * Tries to synchronize commands with online clients.
     */
    public void syncCommandsIfPossible() {
        try {
            Class<?> craftServer = Bukkit.getServer().getClass();
            Method syncCommandsMethod = craftServer.getDeclaredMethod("syncCommands");
            syncCommandsMethod.invoke(Bukkit.getServer());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            KCommon.getInstance().getLogger().log(Level.SEVERE, "Failed to sync commands", e);
        }
    }

    /**
     * Unregisters the command from the name and aliases.
     *
     * @param name    the name of the command.
     * @param aliases the aliases of the command.
     */
    private void deregisterCommand(String name, List<String> aliases) {
        if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
            CommandMap commandMap = this.commandMapHandle.get(Bukkit.getPluginManager());
            Map<String, Command> knownCommands = this.knownCommandsHandle.get(commandMap);
            knownCommands.remove(name);
            aliases.forEach(knownCommands::remove);
        }
    }
}
