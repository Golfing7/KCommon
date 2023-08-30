package com.golfing8.kcommon.command.argument;

import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.Modules;
import com.golfing8.kcommon.struct.time.TimeLength;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

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
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("\\w+");

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
}
