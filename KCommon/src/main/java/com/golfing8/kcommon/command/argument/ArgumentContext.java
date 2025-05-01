package com.golfing8.kcommon.command.argument;

import com.golfing8.kcommon.command.KCommand;
import lombok.Data;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Context used when completing an argument
 */
@Data
public class ArgumentContext {
    /**
     * The command sender for the argument.
     */
    private final CommandSender sender;
    /**
     * The PKCommand the argument belongs to.
     */
    private final KCommand command;
    /**
     * The label that was used.
     */
    private final String label;
    /**
     * The argument that was used.
     */
    private final String argument;
    /**
     * A list of all arguments that the player provided
     * <p>
     * Note that while parsing, all arguments that occur *before* this argument WILL be sanitized.
     * Types are guaranteed as far as your command arguments are concerned.
     * For arguments that occur AFTER this argument, no guarantees are made on presence or correctness.
     * </p>
     */
    private final List<String> allArguments;
    /** The index that this argument was provided. */
    private final int argumentIndex;
}
