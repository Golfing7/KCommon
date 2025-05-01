package com.golfing8.kcommon.command;

import com.golfing8.kcommon.command.argument.ArgumentContext;
import com.golfing8.kcommon.command.argument.CommandArgument;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Context for a running command. Contains things like the sender, arguments, etc.
 */
@RequiredArgsConstructor
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
     * The next available index of an argument.
     * May meet or exceed {@code arguments.size()}, in which case no argument is available.
     */
    private int argumentIndex = 0;

    /**
     * Gets the sender as a player, or returns null if the sender is not a player.
     *
     * @return the sender as a player.
     */
    @Nullable
    public Player getPlayer() {
        if(!(sender instanceof Player))
            return null;

        return (Player) sender;
    }

    /**
     * Gets an argument at the given index.
     *
     * @param index the index of the argument.
     * @return the argument.
     */
    public String getArg(int index) {
        if(index < 0 || index >= arguments.size())
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
     * @return the next available argument.
     * @param <T> the type.
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
     * @return the next available argument.
     * @param <T> the type.
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
