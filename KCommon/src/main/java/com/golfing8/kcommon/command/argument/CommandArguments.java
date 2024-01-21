package com.golfing8.kcommon.command.argument;

import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.argument.type.BooleanCommandArgument;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.Modules;
import com.golfing8.kcommon.struct.time.TimeLength;
import com.golfing8.kcommon.util.MapUtil;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A class storing commonly used command arguments and autofill functions.
 */
@UtilityClass
public final class CommandArguments {
    /**
     * A pattern used to match alphanumeric strings.
     */
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[\\w-]+");

    public static final CommandArgument<String> ANYTHING = new CommandArgument<>("Anything", (ctx) -> Collections.emptyList(), (context) -> true, (s) -> s);

    /** A command argument for all offline players */
    public static final CommandArgument<OfflinePlayer> OFFLINE_PLAYER = new CommandArgument<>("An offline player", (context) -> {
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
    }, (context) -> {
        OfflinePlayer player = Bukkit.getOfflinePlayer(context.getArgument());
        return player != null && player.getName().equalsIgnoreCase(context.getArgument());
    }, Bukkit::getOfflinePlayer);

    /** Used for parsing raw booleans. The formatting of the inputs will be true/false */
    public static final BooleanCommandArgument BOOLEAN = new BooleanCommandArgument(MapUtil.of("true", true, "false", false));

    /** Used for parsing booleans in a nicer format such as yes/no */
    public static final BooleanCommandArgument BOOLEAN_FRIENDLY = new BooleanCommandArgument(MapUtil.of("yes", true, "no", false));
    /** A boolean argument corresponding to the state of something (on/off) */
    public static final BooleanCommandArgument BOOLEAN_STATE = new BooleanCommandArgument(MapUtil.of("on", true, "off", false));

    /**
     * A command argument to auto-complete online players.
     */
    public static final CommandArgument<Player> PLAYER = new CommandArgument<>("An online player", (context) -> {
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
    }, (context) -> {
        Player player = Bukkit.getPlayer(context.getArgument());
        //The double check is necessary as Bukkit.getPlayer()
        //returns the player with a name that starts with the string.
        return player != null && player.getName().equalsIgnoreCase(context.getArgument());
    }, Bukkit::getPlayer);

    /**
     * A command argument for alphanumeric strings. (A-Za-z0-9_)
     */
    public static final CommandArgument<String> ALPHANUMERIC_STRING = new CommandArgument<>("An alphanumeric string", (context) -> {
        return Collections.emptyList();
    }, (context) -> ALPHANUMERIC_PATTERN.matcher(context.getArgument()).matches(), arg -> arg);

    /**
     * A command argument to auto-complete non-negative numbers.
     */
    public static final CommandArgument<Double> NON_NEGATIVE_NUMBER = new CommandArgument<>("A non negative number", (context) -> {
        return Collections.emptyList();
    }, (context) -> {
        try{
            double number = Double.parseDouble(context.getArgument());
            return number >= 0;
        }catch(NumberFormatException exc) {
            return false;
        }
    }, Double::parseDouble);

    /**
     * A command argument to auto-complete doubles.
     */
    public static final CommandArgument<Double> DOUBLE = new CommandArgument<>("A non negative number", (context) -> {
        return Collections.emptyList();
    }, (context) -> {
        try{
            double number = Double.parseDouble(context.getArgument());
            return true;
        }catch(NumberFormatException exc) {
            return false;
        }
    }, Double::parseDouble);

    /**
     * A command argument to auto-complete non-negative integers.
     */
    public static final CommandArgument<Integer> NON_NEGATIVE_INTEGER = new CommandArgument<>("A non negative integer", (context) -> {
        return Collections.emptyList();
    }, (context) -> {
        try{
            int number = Integer.parseInt(context.getArgument());
            return number >= 0;
        }catch(NumberFormatException exc) {
            return false;
        }
    }, Integer::parseInt);

    /**
     * A command argument to auto-complete positive integers.
     */
    public static final CommandArgument<Integer> POSITIVE_INTEGER = new CommandArgument<>("A positive integer", (context) -> {
        return Collections.emptyList();
    }, (context) -> {
        try{
            return Integer.parseInt(context.getArgument()) > 0;
        }catch(NumberFormatException exc) {
            return false;
        }
    }, Integer::parseInt);

    /**
     * A command argument to auto-complete time lengths.
     */
    public static final CommandArgument<TimeLength> TIME = new CommandArgument<>("A length of time", (context) -> {
        return Arrays.asList("1d", "1d,1h", "5m,1s");
    }, (context) -> {
        return TimeLength.parseTime(context.getArgument()) != null;
    }, TimeLength::parseTime);

    /**
     * A command argument for all modules.
     */
    public static final CommandArgument<Module> MODULE = new CommandArgument<>("A module", (context) -> {
        return Modules.getAll().stream().map(Module::getModuleName).collect(Collectors.toList());
    }, (context) -> {
        return Modules.getModule(context.getArgument()) != null;
    }, Modules::getModule);

    /**
     * A command argument for plugins.
     */
    public static final CommandArgument<Plugin> PLUGIN = new CommandArgument<>("plugin", (context) -> {
        return Arrays.stream(Bukkit.getServer().getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.toList());
    }, (context) -> {
        return Bukkit.getServer().getPluginManager().getPlugin(context.getArgument()) != null;
    }, Bukkit.getServer().getPluginManager()::getPlugin);

    /**
     * A command argument for plugins that use this library.
     */
    public static final CommandArgument<KPlugin> KPLUGIN = new CommandArgument<>("kcommon plugin", (context) -> {
        return Arrays.stream(Bukkit.getServer().getPluginManager().getPlugins()).filter(pl -> pl instanceof KPlugin).map(Plugin::getName).collect(Collectors.toList());
    }, (context) -> {
        return Bukkit.getServer().getPluginManager().getPlugin(context.getArgument()) != null;
    }, str -> (KPlugin) Bukkit.getServer().getPluginManager().getPlugin(str));
}
