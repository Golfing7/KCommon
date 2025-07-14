package com.golfing8.kcommon.command;

import com.golfing8.kcommon.command.argument.ArgumentContext;
import com.golfing8.kcommon.command.argument.CommandArgument;
import com.golfing8.kcommon.command.flag.CommandFlag;
import lombok.Getter;
import lombok.var;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context for a running command. Contains things like the sender, arguments, etc.
 */
public class CommandContext {
    /**
     * The sender for this command.
     */
    @Getter
    private final CommandSender sender;
    /**
     * The command alias the player used for this command.
     */
    @Getter
    private final String label;
    /**
     * The arguments the player used for this command.
     */
    @Getter
    private final List<String> arguments;
    /**
     * The command this context is running on.
     */
    @Getter
    private final KCommand command;

    /**
     * Maps states of flags
     */
    private final Map<CommandFlag, TriState> flagStates;
    /**
     * Maps long name flag states
     */
    private final Map<String, TriState> longNameFlagStates;
    /**
     * Maps short name flag states.
     */
    private final Map<Character, TriState> shortNameFlagStates;

    public CommandContext(CommandSender sender, String label, List<String> arguments, KCommand command) {
        this(sender, label, arguments, command, Collections.emptyMap());
    }

    public CommandContext(CommandSender sender, String label, List<String> arguments, KCommand command, Map<CommandFlag, TriState> flagStates) {
        this.sender = sender;
        this.label = label;
        this.arguments = arguments;
        this.command = command;

        this.flagStates = flagStates;
        this.longNameFlagStates = new HashMap<>();
        this.shortNameFlagStates = new HashMap<>();
        for (var entry : flagStates.entrySet()) {
            if (entry.getKey().getShortName() != null)
                this.shortNameFlagStates.put(entry.getKey().getShortName(), entry.getValue());
            this.longNameFlagStates.put(entry.getKey().getFullName(), entry.getValue());
        }
    }

    /**
     * The next available index of an argument.
     * May meet or exceed {@code arguments.size()}, in which case no argument is available.
     */
    private int argumentIndex = 0;

    /**
     * Gets the flag state of the given flag.
     *
     * @param shortName the flag's short name
     * @return the state of the flag
     */
    public TriState getFlagState(char shortName) {
        return this.shortNameFlagStates.getOrDefault(shortName, TriState.NOT_SET);
    }

    /**
     * Gets the flag state of the given flag.
     *
     * @param longName the long name of the flag
     * @return the state of the flag
     */
    public TriState getFlagState(String longName) {
        return this.longNameFlagStates.getOrDefault(longName, TriState.NOT_SET);
    }

    /**
     * Gets the flag state of the given flag.
     *
     * @param flag the flag
     * @return the state of the flag
     */
    public TriState getFlagState(CommandFlag flag) {
        return this.flagStates.getOrDefault(flag, TriState.NOT_SET);
    }

    /**
     * Checks if the sender of this context is a player.
     *
     * @return true if the sender is a player.
     */
    public boolean isSenderPlayer() {
        return sender instanceof Player;
    }

    /**
     * Gets the sender as a player, or returns null if the sender is not a player.
     *
     * @return the sender as a player.
     */
    public Player getPlayer() {
        if (!(sender instanceof Player))
            throw new ClassCastException("Sender is not a player");

        return (Player) sender;
    }

    /**
     * Gets an argument at the given index.
     *
     * @param index the index of the argument.
     * @return the argument.
     */
    public String getArg(int index) {
        if (index < 0 || index >= arguments.size())
            throw new IndexOutOfBoundsException(String.format("Index %s is out of bounds for arguments %s!", index, arguments.toString()));

        return this.arguments.get(index);
    }

    /**
     * Gets an integer from the specific index.
     *
     * @param index the index of the integer.
     * @return the int.
     */
    public int getInt(int index) {
        return Integer.parseInt(getArg(index));
    }

    /**
     * Gets a number from the specific index.
     *
     * @param index the index of the number.
     * @return the number.
     */
    public double getNumber(int index) {
        return Double.parseDouble(getArg(index));
    }

    /**
     * Gets a player from the specific index.
     *
     * @param index the index of the player.
     * @return the player.
     */
    public Player getPlayer(int index) {
        return Bukkit.getPlayer(getArg(index));
    }

    /**
     * Gets an offline player from the specific index.
     *
     * @param index the index of the player.
     * @return the offline player.
     */
    @SuppressWarnings("deprecation")
    public OfflinePlayer getOfflinePlayer(int index) {
        return Bukkit.getOfflinePlayer(getArg(index));
    }

    /**
     * Gets the argument at the given index.
     *
     * @param index the index.
     * @param <T>   the type.
     * @return the next available argument.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(int index) {
        CommandArgument<?> arg = this.command.getCommandArguments().get(index).getArgument();
        // This is a safe cast as prior to creating this CommandContext object, all arguments were verified.
        return (T) arg.getGetter().apply(new ArgumentContext(sender, command, label, arguments.get(index), Collections.unmodifiableList(arguments), index));
    }

    /**
     * Gets the next available argument from the argument list.
     *
     * @param <T> the type.
     * @return the next available argument.
     */
    public <T> T next() {
        return get(this.argumentIndex++);
    }

    /**
     * Joins the remaining arguments to a string delimited by spaces.
     *
     * @return the joined strings. If no arguments remain, an empty string.
     */
    public String joinRemainingToString() {
        return joinRemainingToString(" ");
    }

    /**
     * Joins the remaining arguments to a string delimited by the given string.
     *
     * @param delimiter the delimiter
     * @return the joined strings. If no arguments remain, an empty string.
     */
    public String joinRemainingToString(@NotNull String delimiter) {
        if (this.argumentIndex >= this.arguments.size())
            return "";

        return String.join(delimiter, this.arguments.subList(this.argumentIndex, this.arguments.size()));
    }
}
