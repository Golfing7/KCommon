package com.golfing8.kcommon.command.argument;

import com.golfing8.kcommon.command.KCommand;
import lombok.Data;
import org.bukkit.command.CommandSender;

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
}
