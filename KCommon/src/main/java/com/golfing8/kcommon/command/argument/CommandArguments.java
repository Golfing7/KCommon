package com.golfing8.kcommon.command.argument;

import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.command.argument.type.BooleanCommandArgument;
import com.golfing8.kcommon.config.ConfigPath;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.Modules;
import com.golfing8.kcommon.struct.KNamespacedKey;
import com.golfing8.kcommon.struct.time.TimeLength;
import com.golfing8.kcommon.util.MapUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
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

    public static final CommandArgument<String> ANYTHING = new CommandArgument<>("Anything", (ctx) -> Collections.emptyList(), (context) -> true, ArgumentContext::getArgument);

    /**
     * A command argument for all offline players
     */
    public static final CommandArgument<OfflinePlayer> OFFLINE_PLAYER = new CommandArgument<>("An offline player", (context) -> {
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
    }, (context) -> {
        OfflinePlayer player = Bukkit.getOfflinePlayer(context.getArgument());
        return player != null && player.getName().equalsIgnoreCase(context.getArgument());
    }, ctx -> Bukkit.getOfflinePlayer(ctx.getArgument()));

    /**
     * A command argument for all offline players that have played before
     */
    public static final CommandArgument<OfflinePlayer> OFFLINE_PLAYER_PLAYED_BEFORE = new CommandArgument<>("An offline player", (context) -> {
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
    }, (context) -> {
        OfflinePlayer ifCached = NMS.getTheNMS().getOfflinePlayerIfCached(context.getArgument());
        return ifCached != null && ifCached.getName().equalsIgnoreCase(context.getArgument());
    }, ctx -> Bukkit.getOfflinePlayer(ctx.getArgument()));

    /**
     * Used for parsing raw booleans. The formatting of the inputs will be true/false
     */
    public static final BooleanCommandArgument BOOLEAN = new BooleanCommandArgument(MapUtil.of("true", true, "false", false));

    /**
     * Used for parsing booleans in a nicer format such as yes/no
     */
    public static final BooleanCommandArgument BOOLEAN_FRIENDLY = new BooleanCommandArgument(MapUtil.of("yes", true, "no", false));
    /**
     * A boolean argument corresponding to the state of something (on/off)
     */
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
    }, ctx -> Bukkit.getPlayer(ctx.getArgument()));

    /**
     * A command argument for alphanumeric strings. (A-Za-z0-9_)
     */
    public static final CommandArgument<String> ALPHANUMERIC_STRING = new CommandArgument<>("An alphanumeric string", (context) -> {
        return Collections.emptyList();
    }, (context) -> ALPHANUMERIC_PATTERN.matcher(context.getArgument()).matches(), ArgumentContext::getArgument);

    /**
     * A command argument to auto-complete non-negative numbers.
     */
    public static final CommandArgument<Double> NON_NEGATIVE_NUMBER = new CommandArgument<>("A non negative number", (context) -> {
        return Collections.emptyList();
    }, (context) -> {
        try {
            double number = Double.parseDouble(context.getArgument());
            return number >= 0;
        } catch (NumberFormatException exc) {
            return false;
        }
    }, ctx -> Double.parseDouble(ctx.getArgument()));

    /**
     * A command argument to auto-complete non-negative longs.
     */
    public static final CommandArgument<Long> NON_NEGATIVE_LONG = new CommandArgument<>("A non negative integer", (context) -> {
        return Collections.emptyList();
    }, (context) -> {
        try {
            long number = Long.parseLong(context.getArgument());
            return number >= 0;
        } catch (NumberFormatException exc) {
            return false;
        }
    }, ctx -> Long.parseLong(ctx.getArgument()));

    /**
     * A command argument to auto-complete doubles.
     */
    public static final CommandArgument<Double> DOUBLE = new CommandArgument<>("A non negative number", (context) -> {
        return Collections.emptyList();
    }, (context) -> {
        try {
            double number = Double.parseDouble(context.getArgument());
            return true;
        } catch (NumberFormatException exc) {
            return false;
        }
    }, ctx -> Double.parseDouble(ctx.getArgument()));

    /**
     * A command argument to auto-complete non-negative integers.
     */
    public static final CommandArgument<Integer> NON_NEGATIVE_INTEGER = new CommandArgument<>("A non negative integer", (context) -> {
        return Collections.emptyList();
    }, (context) -> {
        try {
            int number = Integer.parseInt(context.getArgument());
            return number >= 0;
        } catch (NumberFormatException exc) {
            return false;
        }
    }, ctx -> Integer.parseInt(ctx.getArgument()));

    /**
     * A command argument to auto-complete positive integers.
     */
    public static final CommandArgument<Integer> POSITIVE_INTEGER = new CommandArgument<>("A positive integer", (context) -> {
        return Collections.emptyList();
    }, (context) -> {
        try {
            return Integer.parseInt(context.getArgument()) > 0;
        } catch (NumberFormatException exc) {
            return false;
        }
    }, ctx -> Integer.parseInt(ctx.getArgument()));

    /**
     * A command argument to auto-complete time lengths.
     */
    public static final CommandArgument<TimeLength> TIME = new CommandArgument<>("A length of time", (context) -> {
        return Arrays.asList("1d", "1d,1h", "5m,1s");
    }, (context) -> {
        return TimeLength.parseTime(context.getArgument()) != null;
    }, ctx -> TimeLength.parseTime(ctx.getArgument()));

    /**
     * A command argument for all modules.
     */
    public static final CommandArgument<Module> MODULE = new CommandArgument<>("A module", (context) -> {
        return Modules.getAll().stream().map(Module::getModuleName).collect(Collectors.toList());
    }, (context) -> {
        String argument = context.getArgument().toLowerCase();
        if (argument.contains(":")) {
            String[] split = argument.split(":");
            Plugin plugin = Bukkit.getPluginManager().getPlugin(split[0]);
            if (plugin != null) {
                return Modules.getModule(new KNamespacedKey(plugin, split[1])) != null;
            } else {
                return Modules.getModule(new KNamespacedKey(split[0], split[1])) != null;
            }
        }
        return Modules.getModule(context.getArgument()) != null;
    }, (ctx) -> {
        if (ctx.getArgument().contains(":")) {
            String[] split = ctx.getArgument().split(":");
            Plugin plugin = Bukkit.getPluginManager().getPlugin(split[0]);
            if (plugin != null) {
                return Modules.getModule(new KNamespacedKey(plugin, split[1]));
            } else {
                return Modules.getModule(new KNamespacedKey(split[0], split[1]));
            }
        }
        return Modules.getModule(ctx.getArgument());
    });

    /**
     * A command argument for plugins.
     */
    public static final CommandArgument<Plugin> PLUGIN = new CommandArgument<>("plugin", (context) -> {
        return Arrays.stream(Bukkit.getServer().getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.toList());
    }, (context) -> {
        return Bukkit.getServer().getPluginManager().getPlugin(context.getArgument()) != null;
    }, ctx -> Bukkit.getServer().getPluginManager().getPlugin(ctx.getArgument()));

    /**
     * A command argument for plugins that use this library.
     */
    public static final CommandArgument<KPlugin> KPLUGIN = new CommandArgument<>("kcommon plugin", (context) -> {
        return Arrays.stream(Bukkit.getServer().getPluginManager().getPlugins()).filter(pl -> pl instanceof KPlugin).map(Plugin::getName).collect(Collectors.toList());
    }, (context) -> {
        return Bukkit.getServer().getPluginManager().getPlugin(context.getArgument()) != null;
    }, ctx -> (KPlugin) Bukkit.getServer().getPluginManager().getPlugin(ctx.getArgument()));

    /**
     * A command argument for parsing java UUIDs.
     */
    public static final CommandArgument<UUID> UUID = new CommandArgument<>("uuid", (context) -> Collections.emptyList(), (context) -> {
        try {
            java.util.UUID.fromString(context.getArgument());
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }, ctx -> java.util.UUID.fromString(ctx.getArgument()));

    public static final CommandArgument<ConfigPath> CONFIG_PATH = new CommandArgument<>("config path", (context) -> Collections.emptyList(), (context) -> true, ctx -> ConfigPath.parse(ctx.getArgument()));

    public static final CommandArgument<World> WORLD = new CommandArgument<>("world", (context) -> {
        return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
    }, (context) -> {
        return Bukkit.getWorld(context.getArgument()) != null;
    }, ctx -> Bukkit.getWorld(ctx.getArgument()));
}
