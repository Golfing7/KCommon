package com.golfing8.kcommon.command.argument;

import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.argument.type.BooleanCommandArgument;
import com.golfing8.kcommon.config.ConfigPath;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.Modules;
import com.golfing8.kcommon.struct.KNamespacedKey;
import com.golfing8.kcommon.struct.time.TimeLength;
import com.golfing8.kcommon.util.MapUtil;
import com.google.common.collect.Lists;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
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

    public static final CommandArgument<String> ANYTHING = new CommandArgument<>("Anything", ctx -> Collections.emptyList(), context -> true, ArgumentContext::getArgument);

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
    public static final CommandArgument<PlayerRef> PLAYER = new CommandArgument<>("An online player", context -> {
        return Universe.get().getPlayers().stream().map(PlayerRef::getUsername).collect(Collectors.toList());
    }, context -> {
        return Universe.get().getPlayerByUsername(context.getArgument(), NameMatching.EXACT_IGNORE_CASE) != null;
    }, ctx -> Universe.get().getPlayerByUsername(ctx.getArgument(), NameMatching.EXACT_IGNORE_CASE));

    /**
     * A command argument for alphanumeric strings. (A-Za-z0-9_)
     */
    public static final CommandArgument<String> ALPHANUMERIC_STRING = new CommandArgument<>("An alphanumeric string", context -> {
        return Collections.emptyList();
    }, context -> ALPHANUMERIC_PATTERN.matcher(context.getArgument()).matches(), ArgumentContext::getArgument);

    /**
     * A command argument to auto-complete non-negative numbers.
     */
    public static final CommandArgument<Double> NON_NEGATIVE_NUMBER = new CommandArgument<>("A non negative number", context -> {
        return Collections.emptyList();
    }, context -> {
        try {
            double number = Double.parseDouble(context.getArgument());
            if (Double.isNaN(number) || Double.isInfinite(number))
                return false;

            return number >= 0;
        } catch (NumberFormatException exc) {
            return false;
        }
    }, ctx -> Double.parseDouble(ctx.getArgument()));

    /**
     * A command argument to auto-complete non-negative numbers.
     */
    public static final CommandArgument<Double> POSITIVE_NUMBER = new CommandArgument<>("A positive number", context -> {
        return Collections.emptyList();
    }, context -> {
        try {
            double number = Double.parseDouble(context.getArgument());
            if (Double.isNaN(number) || Double.isInfinite(number))
                return false;

            return number > 0;
        } catch (NumberFormatException exc) {
            return false;
        }
    }, ctx -> Double.parseDouble(ctx.getArgument()));

    /**
     * A command argument to auto-complete non-negative longs.
     */
    public static final CommandArgument<Long> NON_NEGATIVE_LONG = new CommandArgument<>("A non negative integer", context -> {
        return Collections.emptyList();
    }, context -> {
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
    public static final CommandArgument<Double> DOUBLE = new CommandArgument<>("A number", context -> {
        return Collections.emptyList();
    }, context -> {
        try {
            double number = Double.parseDouble(context.getArgument());

            return !Double.isNaN(number) && !Double.isInfinite(number);
        } catch (NumberFormatException exc) {
            return false;
        }
    }, ctx -> Double.parseDouble(ctx.getArgument()));

    /**
     * A command argument for integers.
     */
    public static final CommandArgument<Integer> INTEGER = new CommandArgument<>("An integer", context -> {
        return Collections.emptyList();
    }, context -> {
        try {
            Integer.parseInt(context.getArgument());
            return true;
        } catch (NumberFormatException exc) {
            return false;
        }
    }, ctx -> Integer.parseInt(ctx.getArgument()));

    /**
     * A command argument to auto-complete non-negative integers.
     */
    public static final CommandArgument<Integer> NON_NEGATIVE_INTEGER = new CommandArgument<>("A non negative integer", context -> {
        return Collections.emptyList();
    }, context -> {
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
    public static final CommandArgument<Integer> POSITIVE_INTEGER = new CommandArgument<>("A positive integer", context -> {
        return Collections.emptyList();
    }, context -> {
        try {
            return Integer.parseInt(context.getArgument()) > 0;
        } catch (NumberFormatException exc) {
            return false;
        }
    }, ctx -> Integer.parseInt(ctx.getArgument()));

    /**
     * A command argument to auto-complete time lengths.
     */
    public static final CommandArgument<TimeLength> TIME = new CommandArgument<>("A length of time", context -> {
        return Arrays.asList("1d", "1d,1h", "5m,1s");
    }, context -> {
        return TimeLength.parseTime(context.getArgument()) != null;
    }, ctx -> TimeLength.parseTime(ctx.getArgument()));

    /**
     * A command argument to auto-complete local date times.
     */
    public static final CommandArgument<LocalDateTime> LOCAL_DATE_TIME = new CommandArgument<>("A local date time", context -> {
        return Lists.newArrayList(
                "2007-12-03T10:15:30",
                "2020-12-03T10:15:30"
        );
    }, context -> {
        try {
            LocalDateTime.parse(context.getArgument());
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }, ctx -> LocalDateTime.parse(ctx.getArgument()));

    /**
     * A command argument for all modules.
     */
    public static final CommandArgument<Module> MODULE = new CommandArgument<>("A module", context -> {
        return Modules.getAll().stream().map(Module::getModuleName).collect(Collectors.toList());
    }, context -> findModule(context.getArgument()) != null, ctx -> findModule(ctx.getArgument()));

    /**
     * A command argument for plugins.
     */
    public static final CommandArgument<PluginBase> PLUGIN = new CommandArgument<>("plugin", context -> {
        return PluginManager.get().getPlugins().stream().map(plugin -> plugin.getManifest().getName()).collect(Collectors.toList());
    }, context -> {
        return findPlugin(context.getArgument()) != null;
    }, ctx -> findPlugin(ctx.getArgument()));

    /**
     * A command argument for plugins that use this library.
     */
    public static final CommandArgument<KPlugin> KPLUGIN = new CommandArgument<>("kcommon plugin", context -> {
        return PluginManager.get().getPlugins().stream().filter(pl -> pl instanceof KPlugin).map(plugin -> plugin.getManifest().getName()).collect(Collectors.toList());
    }, context -> {
        return findPlugin(context.getArgument()) instanceof KPlugin;
    }, ctx -> (KPlugin) findPlugin(ctx.getArgument()));

    /**
     * A command argument for parsing java UUIDs.
     */
    public static final CommandArgument<UUID> UUID = new CommandArgument<>("uuid", context -> Collections.emptyList(), context -> {
        try {
            java.util.UUID.fromString(context.getArgument());
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }, ctx -> java.util.UUID.fromString(ctx.getArgument()));

    public static final CommandArgument<ConfigPath> CONFIG_PATH = new CommandArgument<>("config path", context -> Collections.emptyList(), context -> true, ctx -> ConfigPath.parse(ctx.getArgument()));

    public static final CommandArgument<World> WORLD = new CommandArgument<>("world", context -> {
        return Universe.get().getWorlds().keySet();
    }, context -> {
        return Universe.get().getWorld(context.getArgument()) != null;
    }, ctx -> Universe.get().getWorld(ctx.getArgument()));

    public static final CommandArgument<IntegerOperation> INTEGER_OPERATION = new CommandArgument<>("operation", context -> {
        return Arrays.stream(IntegerOperation.values()).map(IntegerOperation::getStringRepresentation).collect(Collectors.toSet());
    }, context -> {
        return IntegerOperation.getFromStringRepresentation(context.getArgument()) != null;
    }, ctx -> IntegerOperation.getFromStringRepresentation(ctx.getArgument()));

    /**
     * Finds a module by its name, or by its {@code plugin:module} namespaced key.
     *
     * @param argument the argument.
     * @return the module, or null if there isn't one.
     */
    private static @Nullable Module findModule(String argument) {
        if (!argument.contains(":"))
            return Modules.getModule(argument);

        String[] split = argument.toLowerCase(Locale.ROOT).split(":", 2);
        try {
            return Modules.getModule(new KNamespacedKey(split[0], split[1]));
        } catch (IllegalArgumentException exc) {
            // Not a valid namespaced key, so it can't name a module.
            return null;
        }
    }

    /**
     * Finds a plugin by its manifest name.
     *
     * @param name the name.
     * @return the plugin, or null if there isn't one.
     */
    private static @Nullable PluginBase findPlugin(String name) {
        for (PluginBase plugin : PluginManager.get().getPlugins()) {
            if (plugin.getManifest().getName().equals(name))
                return plugin;
        }
        return null;
    }
}
