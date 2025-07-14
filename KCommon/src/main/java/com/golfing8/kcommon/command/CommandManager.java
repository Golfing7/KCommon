package com.golfing8.kcommon.command;

import com.golfing8.kcommon.KCommon;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * A class which is useful for accessing the bukkit command map reflectively.
 */
public class CommandManager {
    @Getter
    private static final CommandManager instance = new CommandManager();

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
            e.printStackTrace();
        }
        return pluginCommand;
    }

    /**
     * Gets the bukkit command map.
     *
     * @return the command map.
     */
    public static CommandMap getCommandMap() {
        CommandMap commandMap = null;

        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");

                f.setAccessible(true);

                commandMap = (CommandMap) f.get(Bukkit.getServer());
            }
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }

        return commandMap;
    }

    public void syncCommandsIfPossible() {
        try {
            Class<?> craftServer = Bukkit.getServer().getClass();
            Method syncCommandsMethod = craftServer.getDeclaredMethod("syncCommands");

            syncCommandsMethod.invoke(Bukkit.getServer());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

        }
    }

    /**
     * Unregisters the command from the name and aliases.
     *
     * @param name    the name of the command.
     * @param aliases the aliases of the command.
     */
    @SuppressWarnings("unchecked")
    private void deregisterCommand(String name, List<String> aliases) {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);

                CommandMap commandMap = (CommandMap) f.get(Bukkit.getPluginManager());

                Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");

                field.setAccessible(true);

                HashMap<String, Command> knownCommands = (HashMap<String, Command>) field.get(commandMap);

                knownCommands.remove(name);

                aliases.forEach(knownCommands::remove);
            }
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }
    }
}
